package io.recruitcrm.microservice.timesheet.services.contractor_setting;

import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum;
import io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Calculator for finding free time slots between occupied timesheet periods.
 *
 * <p>
 * This class handles three frequency types: WEEKLY, BIWEEKLY, and MONTHLY.
 *
 * <h2>Slot Rules:</h2>
 * <ul>
 * <li><b>Middle slots:</b> Fixed size (7/14/monthly days), must start on startDay and end
 * on startDay-1</li>
 * <li><b>Starting partial:</b> Any length, ends on startDay-1</li>
 * <li><b>Ending partial:</b> Any length, starts on startDay</li>
 * </ul>
 *
 * <h2>Start Day:</h2>
 * <ul>
 * <li>Weekly/Biweekly: 1-7 (Monday=1, Sunday=7)</li>
 * <li>Monthly: 1-28 or 100 (100 = last day of month)</li>
 * </ul>
 */
@Component
public class FreeSlotCalculator {

	private static final int LAST_DAY_OF_MONTH_CODE = 100;

	private static final int WEEKLY_SLOT_DAYS = 7;

	private static final int BIWEEKLY_SLOT_DAYS = 14;

	/**
	 * Calculates all free slots within the given date range, avoiding occupied periods.
	 * @param globalStartEpoch Job start date in epoch seconds
	 * @param globalEndEpoch Job end date in epoch seconds
	 * @param startDay The day slots should start on (1-7 for weekly/biweekly, 1-28 or 100
	 * for monthly)
	 * @param frequencyId Frequency type ID (WEEKLY, BIWEEKLY, MONTHLY)
	 * @param occupiedRanges List of occupied ranges as [startEpoch, endEpoch] pairs
	 * @return List of free slots as TimeSlotsResultBodyDto
	 */
	public List<TimeSlotsResultBodyDto> calculateFreeSlots(Integer globalStartEpoch, Integer globalEndEpoch,
			Integer startDay, Integer frequencyId, List<List<Integer>> occupiedRanges) {
		return this.calculateFreeSlots(globalStartEpoch, globalEndEpoch, startDay, frequencyId, occupiedRanges, true,
				true);
	}

	/**
	 * Calculates all free slots within the given date range, avoiding occupied periods,
	 * with control over boundary partial slots.
	 *
	 * <p>
	 * Partial slots at the global boundaries are only valid when those boundaries are the
	 * actual job start/end dates. In bulk (multi-job) calculations the global range is
	 * the intersection window of several jobs, so a boundary partial is only common to
	 * all jobs when every job actually starts/ends on that boundary.
	 * @param allowStartingPartial whether a partial slot may be emitted at the global
	 * start (true only if the global start is the actual job start for all jobs involved)
	 * @param allowEndingPartial whether a partial slot may be emitted at the global end
	 * (true only if the global end is the actual job end for all jobs involved)
	 */
	public List<TimeSlotsResultBodyDto> calculateFreeSlots(Integer globalStartEpoch, Integer globalEndEpoch,
			Integer startDay, Integer frequencyId, List<List<Integer>> occupiedRanges, boolean allowStartingPartial,
			boolean allowEndingPartial) {

		// Convert to LocalDate for easier manipulation
		LocalDate globalStart = this.toLocalDate(globalStartEpoch);
		LocalDate globalEnd = this.toLocalDate(globalEndEpoch);

		// Convert occupied ranges to LocalDate pairs and merge overlapping
		List<DateRange> occupiedDateRanges = this.convertAndMergeOccupiedRanges(occupiedRanges);

		// Find all gaps (free ranges)
		List<DateRange> gaps = this.findGaps(occupiedDateRanges, globalStart, globalEnd);

		// Generate slots for each gap
		List<TimeSlotsResultBodyDto> allSlots = new ArrayList<>();
		for (DateRange gap : gaps) {
			List<DateRange> slotsForGap = this.generateSlotsForGap(gap, startDay, frequencyId, globalStart, globalEnd,
					allowStartingPartial, allowEndingPartial);
			for (DateRange slot : slotsForGap) {
				allSlots.add(this.toTimeSlotsResultBodyDto(slot));
			}
		}

		// Sort by start date
		allSlots.sort(Comparator.comparingInt(TimeSlotsResultBodyDto::getStartDate));

		return allSlots;
	}

	/**
	 * Converts occupied ranges from epoch seconds to LocalDate and merges overlapping
	 * ranges.
	 */
	private List<DateRange> convertAndMergeOccupiedRanges(List<List<Integer>> occupiedRanges) {
		if (occupiedRanges == null || occupiedRanges.isEmpty()) {
			return new ArrayList<>();
		}

		// Convert to DateRange and sort by start date
		List<DateRange> ranges = occupiedRanges.stream()
			.map((range) -> new DateRange(this.toLocalDate(range.get(0)), this.toLocalDate(range.get(1))))
			.sorted(Comparator.comparing(DateRange::start))
			.toList();

		// Merge overlapping or adjacent ranges
		List<DateRange> merged = new ArrayList<>();
		for (DateRange current : ranges) {
			if (merged.isEmpty()) {
				merged.add(current);
			}
			else {
				DateRange last = merged.get(merged.size() - 1);
				// Check if current overlaps or is adjacent to last
				if (!current.start().isAfter(last.end().plusDays(1))) {
					// Merge: extend the last range if current ends later
					merged.set(merged.size() - 1, new DateRange(last.start(),
							current.end().isAfter(last.end()) ? current.end() : last.end()));
				}
				else {
					merged.add(current);
				}
			}
		}

		return merged;
	}

	/**
	 * Finds all gaps (free ranges) between occupied ranges within the global range.
	 */
	private List<DateRange> findGaps(List<DateRange> occupiedRanges, LocalDate globalStart, LocalDate globalEnd) {
		List<DateRange> gaps = new ArrayList<>();

		if (occupiedRanges.isEmpty()) {
			// No occupied ranges - entire range is a gap
			gaps.add(new DateRange(globalStart, globalEnd));
			return gaps;
		}

		// Gap before first occupied range
		DateRange firstOccupied = occupiedRanges.get(0);
		if (globalStart.isBefore(firstOccupied.start())) {
			gaps.add(new DateRange(globalStart, firstOccupied.start().minusDays(1)));
		}

		// Gaps between occupied ranges
		for (int i = 0; i < occupiedRanges.size() - 1; i++) {
			LocalDate gapStart = occupiedRanges.get(i).end().plusDays(1);
			LocalDate gapEnd = occupiedRanges.get(i + 1).start().minusDays(1);
			if (!gapStart.isAfter(gapEnd)) {
				gaps.add(new DateRange(gapStart, gapEnd));
			}
		}

		// Gap after last occupied range
		DateRange lastOccupied = occupiedRanges.get(occupiedRanges.size() - 1);
		if (lastOccupied.end().isBefore(globalEnd)) {
			gaps.add(new DateRange(lastOccupied.end().plusDays(1), globalEnd));
		}

		return gaps;
	}

	/**
	 * Generates all slots (starting partial + full slots + ending partial) for a single
	 * gap.
	 *
	 * <p>
	 * Rules:
	 * <ul>
	 * <li>Starting partial: ONLY if gap starts on globalStart (job start), must end on
	 * startDay-1</li>
	 * <li>Ending partial: ONLY if gap ends on globalEnd (job end), must start on
	 * startDay</li>
	 * <li>Middle slots: MUST be exactly 7/14/monthly days, start on startDay, end on
	 * startDay-1</li>
	 * <li>Middle gaps that can't fit full slots remain UNFILLED</li>
	 * </ul>
	 */
	private List<DateRange> generateSlotsForGap(DateRange gap, Integer startDay, Integer frequencyId,
			LocalDate globalStart, LocalDate globalEnd, boolean allowStartingPartial, boolean allowEndingPartial) {
		List<DateRange> slots = new ArrayList<>();

		LocalDate gapStart = gap.start();
		LocalDate gapEnd = gap.end();

		boolean isAtJobStart = gapStart.equals(globalStart) && allowStartingPartial;
		boolean isAtJobEnd = gapEnd.equals(globalEnd) && allowEndingPartial;

		if (gapStart.equals(gapEnd)) {
			this.addSingleDaySlotIfValid(slots, gapStart, isAtJobStart, isAtJobEnd, startDay, frequencyId);
			return slots;
		}

		LocalDate firstAlignedStart = this.findNextAlignedStartDay(gapStart, startDay, frequencyId);

		if (isAtJobStart && firstAlignedStart.isAfter(gapStart)) {
			LocalDate startingPartialEnd = this.findStartingPartialEndDate(gapStart, startDay, frequencyId);
			if (!startingPartialEnd.isAfter(gapEnd)) {
				slots.add(new DateRange(gapStart, startingPartialEnd));
			}
		}

		LocalDate currentStart = firstAlignedStart;
		while (!currentStart.isAfter(gapEnd)) {
			LocalDate slotEnd = this.getSlotEndDate(currentStart, startDay, frequencyId);
			if (slotEnd.isAfter(gapEnd)) {
				break;
			}
			slots.add(new DateRange(currentStart, slotEnd));
			currentStart = slotEnd.plusDays(1);
		}

		if (isAtJobEnd && !currentStart.isAfter(gapEnd)) {
			slots.add(new DateRange(currentStart, gapEnd));
		}

		return slots;
	}

	/**
	 * Handles single-day gap: only adds a slot if the gap is at a job boundary and, for
	 * single-day jobs, the day is aligned with the configured start day.
	 */
	private void addSingleDaySlotIfValid(List<DateRange> slots, LocalDate gapStart, boolean isAtJobStart,
			boolean isAtJobEnd, Integer startDay, Integer frequencyId) {
		if (!isAtJobStart && !isAtJobEnd) {
			return;
		}
		if (isAtJobStart && isAtJobEnd && !this.isAlignedWithStartDay(gapStart, startDay, frequencyId)) {
			return;
		}
		slots.add(new DateRange(gapStart, gapStart));
	}

	/**
	 * Finds the valid end date for a starting partial slot. Starting partial must end on
	 * startDay-1 (e.g., Tuesday if startDay is Wednesday).
	 */
	private LocalDate findStartingPartialEndDate(LocalDate gapStart, Integer startDay, Integer frequencyId) {
		if (this.isMonthly(frequencyId)) {
			return this.findMonthlyStartingPartialEndDate(gapStart, startDay);
		}
		else {
			return this.findWeeklyStartingPartialEndDate(gapStart, startDay);
		}
	}

	/**
	 * For weekly/biweekly: Starting partial must end on startDay-1.
	 * @param startDay Day of week (1=Monday, 7=Sunday)
	 * @return The first occurrence of startDay-1 on or after gapStart
	 */
	private LocalDate findWeeklyStartingPartialEndDate(LocalDate gapStart, Integer startDay) {
		// startDay-1: If Wednesday(3), end on Tuesday(2). If Monday(1), end on
		// Sunday(7).
		int endDayOfWeek = (startDay == 1) ? 7 : startDay - 1;

		int currentDayOfWeek = gapStart.getDayOfWeek().getValue();
		int daysToAdd = (endDayOfWeek - currentDayOfWeek + 7) % 7;

		return gapStart.plusDays(daysToAdd);
	}

	/**
	 * For monthly: Starting partial must end on startDay-1.
	 * @param startDay Day of month (1-28) or 100 (last day)
	 * @return The first occurrence of startDay-1 on or after gapStart
	 */
	protected LocalDate findMonthlyStartingPartialEndDate(LocalDate gapStart, Integer startDay) {
		if (startDay == 1) {
			return YearMonth.of(gapStart.getYear(), gapStart.getMonthValue()).atEndOfMonth();
		}

		int targetEndDay = switch (startDay) {
			case LAST_DAY_OF_MONTH_CODE -> {
				int lastDay = YearMonth.of(gapStart.getYear(), gapStart.getMonthValue()).lengthOfMonth();
				yield lastDay - 1;
			}
			default -> startDay - 1;
		};

		if (gapStart.getDayOfMonth() <= targetEndDay) {
			return LocalDate.of(gapStart.getYear(), gapStart.getMonthValue(), targetEndDay);
		}

		LocalDate nextMonth = gapStart.plusMonths(1);
		int nextTargetEndDay = switch (startDay) {
			case LAST_DAY_OF_MONTH_CODE -> {
				int lastDay = YearMonth.of(nextMonth.getYear(), nextMonth.getMonthValue()).lengthOfMonth();
				yield lastDay - 1;
			}
			default -> startDay - 1;
		};
		int maxDay = YearMonth.of(nextMonth.getYear(), nextMonth.getMonthValue()).lengthOfMonth();
		return LocalDate.of(nextMonth.getYear(), nextMonth.getMonthValue(), Math.min(nextTargetEndDay, maxDay));
	}

	/**
	 * Finds the next occurrence of the start day on or after the given date.
	 */
	private LocalDate findNextAlignedStartDay(LocalDate date, Integer startDay, Integer frequencyId) {
		if (this.isMonthly(frequencyId)) {
			return this.findNextMonthlyStartDay(date, startDay);
		}
		else {
			return this.findNextWeeklyStartDay(date, startDay);
		}
	}

	/**
	 * Finds the next occurrence of the weekly/biweekly start day.
	 * @param startDay Day of week (1=Monday, 7=Sunday)
	 */
	private LocalDate findNextWeeklyStartDay(LocalDate date, Integer startDay) {
		int currentDayOfWeek = date.getDayOfWeek().getValue();
		int daysToAdd = (startDay - currentDayOfWeek + 7) % 7;
		return date.plusDays(daysToAdd);
	}

	/**
	 * Finds the next occurrence of the monthly start day.
	 * @param startDay Day of month (1-28) or 100 (last day)
	 */
	private LocalDate findNextMonthlyStartDay(LocalDate date, Integer startDay) {
		int targetDay = this.resolveMonthlyStartDay(startDay, date.getYear(), date.getMonthValue());

		if (date.getDayOfMonth() <= targetDay) {
			// Target day is in current month
			int maxDayInMonth = YearMonth.of(date.getYear(), date.getMonthValue()).lengthOfMonth();
			return LocalDate.of(date.getYear(), date.getMonthValue(), Math.min(targetDay, maxDayInMonth));
		}
		else {
			// Target day is in next month
			LocalDate nextMonth = date.plusMonths(1).withDayOfMonth(1);
			int nextTargetDay = this.resolveMonthlyStartDay(startDay, nextMonth.getYear(), nextMonth.getMonthValue());
			int maxDayInMonth = YearMonth.of(nextMonth.getYear(), nextMonth.getMonthValue()).lengthOfMonth();
			return LocalDate.of(nextMonth.getYear(), nextMonth.getMonthValue(), Math.min(nextTargetDay, maxDayInMonth));
		}
	}

	/**
	 * Calculates the end date for a slot starting on the given date.
	 */
	private LocalDate getSlotEndDate(LocalDate startDate, Integer startDay, Integer frequencyId) {
		if (this.isWeekly(frequencyId)) {
			return startDate.plusDays((long) WEEKLY_SLOT_DAYS - 1);
		}
		else if (this.isBiweekly(frequencyId)) {
			return startDate.plusDays((long) BIWEEKLY_SLOT_DAYS - 1);
		}
		else {
			// Monthly: end is one day before start day of next month
			return this.getMonthlySlotEndDate(startDate, startDay);
		}
	}

	/**
	 * Calculates the end date for a monthly slot.
	 */
	private LocalDate getMonthlySlotEndDate(LocalDate startDate, Integer startDay) {
		// Move to next month
		LocalDate nextMonth = startDate.plusMonths(1);
		int nextTargetDay = this.resolveMonthlyStartDay(startDay, nextMonth.getYear(), nextMonth.getMonthValue());
		int maxDayInMonth = YearMonth.of(nextMonth.getYear(), nextMonth.getMonthValue()).lengthOfMonth();
		LocalDate nextStartDay = LocalDate.of(nextMonth.getYear(), nextMonth.getMonthValue(),
				Math.min(nextTargetDay, maxDayInMonth));
		return nextStartDay.minusDays(1);
	}

	/**
	 * Resolves the actual day number for monthly start day, handling the special code 100
	 * (last day).
	 */
	private int resolveMonthlyStartDay(Integer startDay, int year, int month) {
		if (startDay == LAST_DAY_OF_MONTH_CODE) {
			return YearMonth.of(year, month).lengthOfMonth();
		}
		return startDay;
	}

	/**
	 * Returns true if {@code date} falls exactly on the configured start day.
	 * <ul>
	 * <li>Weekly/Biweekly: date's day-of-week equals startDay (1=Mon … 7=Sun)</li>
	 * <li>Monthly: date's day-of-month equals the resolved start day (100 → last day of
	 * month)</li>
	 * </ul>
	 */
	private boolean isAlignedWithStartDay(LocalDate date, Integer startDay, Integer frequencyId) {
		if (this.isMonthly(frequencyId)) {
			int resolvedDay = this.resolveMonthlyStartDay(startDay, date.getYear(), date.getMonthValue());
			return date.getDayOfMonth() == resolvedDay;
		}
		return date.getDayOfWeek().getValue() == startDay;
	}

	// ========== Frequency Type Helpers ==========

	private boolean isWeekly(Integer frequencyId) {
		return TimesheetSettingFrequencyTypeEnum.WEEKLY.getId().equals(frequencyId);
	}

	private boolean isBiweekly(Integer frequencyId) {
		return TimesheetSettingFrequencyTypeEnum.BIWEEKLY.getId().equals(frequencyId);
	}

	private boolean isMonthly(Integer frequencyId) {
		return TimesheetSettingFrequencyTypeEnum.MONTHLY.getId().equals(frequencyId);
	}

	// ========== Date/Time Conversion Helpers ==========

	/**
	 * Converts epoch seconds to LocalDate (uses UTC timezone).
	 */
	private LocalDate toLocalDate(Integer epochSeconds) {
		return LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC).toLocalDate();
	}

	/**
	 * Converts DateRange to TimeSlotsResultBodyDto with proper timestamps. Start:
	 * 00:00:00, End: 23:59:59
	 */
	private TimeSlotsResultBodyDto toTimeSlotsResultBodyDto(DateRange range) {
		int startEpoch = (int) range.start().atStartOfDay().toEpochSecond(ZoneOffset.UTC);
		int endEpoch = (int) range.end().atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC);
		return new TimeSlotsResultBodyDto(startEpoch, endEpoch);
	}

	/**
	 * Represents a date range [start, end] inclusive.
	 */
	private record DateRange(LocalDate start, LocalDate end) {
	}

}
