package io.recruitcrm.microservice.timesheet.services.contractor_setting;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum;
import io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FreeSlotCalculatorTests {

	private final FreeSlotCalculator freeSlotCalculator = new FreeSlotCalculator();

	private static final Integer WEEKLY = TimesheetSettingFrequencyTypeEnum.WEEKLY.getId();

	private static final Integer MONTHLY = TimesheetSettingFrequencyTypeEnum.MONTHLY.getId();

	private static final Integer MONDAY = 1;

	private static final Integer LAST_DAY_OF_MONTH = 100;

	private static int startOfDayEpoch(int year, int month, int day) {
		return (int) LocalDateTime.of(year, month, day, 0, 0).toEpochSecond(ZoneOffset.UTC);
	}

	private static int endOfDayEpoch(int year, int month, int day) {
		return (int) LocalDateTime.of(year, month, day, 23, 59, 59).toEpochSecond(ZoneOffset.UTC);
	}

	@Test
	@DisplayName("Calculate free slots should skip a single-day gap that is not at a job boundary")
	void testCalculateFreeSlotsSingleDayMiddleGapNotAtBoundaryReturnsNoSlotForGap() {
		// Given - job Jul 1 - Jul 31 2025, weekly starting Monday. Occupied Jul 7-13 and
		// Jul 15-27 leave a single free Monday (Jul 14) in the middle of the range.
		List<List<Integer>> occupiedRanges = Arrays.asList(
				Arrays.asList(startOfDayEpoch(2025, 7, 7), endOfDayEpoch(2025, 7, 13)),
				Arrays.asList(startOfDayEpoch(2025, 7, 15), endOfDayEpoch(2025, 7, 27)));

		// When
		List<TimeSlotsResultBodyDto> result = this.freeSlotCalculator.calculateFreeSlots(startOfDayEpoch(2025, 7, 1),
				endOfDayEpoch(2025, 7, 31), MONDAY, WEEKLY, occupiedRanges);

		// Then - only starting partial Jul 1-6 and ending partial Jul 28-31; Jul 14 is
		// not emitted because a middle gap cannot hold a partial slot
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getStartDate()).isEqualTo(startOfDayEpoch(2025, 7, 1));
		assertThat(result.get(0).getEndDate()).isEqualTo(endOfDayEpoch(2025, 7, 6));
		assertThat(result.get(1).getStartDate()).isEqualTo(startOfDayEpoch(2025, 7, 28));
		assertThat(result.get(1).getEndDate()).isEqualTo(endOfDayEpoch(2025, 7, 31));
	}

	@Test
	@DisplayName("Calculate free slots should emit a single-day slot when the gap is at the job start only")
	void testCalculateFreeSlotsSingleDayGapAtJobStartReturnsSlot() {
		// Given - job Jul 1 - Jul 31 2025, occupied Jul 2 - Jul 31 leaves only Jul 1 free
		List<List<Integer>> occupiedRanges = List
			.of(Arrays.asList(startOfDayEpoch(2025, 7, 2), endOfDayEpoch(2025, 7, 31)));

		// When
		List<TimeSlotsResultBodyDto> result = this.freeSlotCalculator.calculateFreeSlots(startOfDayEpoch(2025, 7, 1),
				endOfDayEpoch(2025, 7, 31), MONDAY, WEEKLY, occupiedRanges);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getStartDate()).isEqualTo(startOfDayEpoch(2025, 7, 1));
		assertThat(result.get(0).getEndDate()).isEqualTo(endOfDayEpoch(2025, 7, 1));
	}

	@Test
	@DisplayName("Calculate free slots should emit a single-day slot when the gap is at the job end only")
	void testCalculateFreeSlotsSingleDayGapAtJobEndReturnsSlot() {
		// Given - job Jul 1 - Jul 31 2025, occupied Jul 1 - Jul 30 leaves only Jul 31
		// free
		List<List<Integer>> occupiedRanges = List
			.of(Arrays.asList(startOfDayEpoch(2025, 7, 1), endOfDayEpoch(2025, 7, 30)));

		// When
		List<TimeSlotsResultBodyDto> result = this.freeSlotCalculator.calculateFreeSlots(startOfDayEpoch(2025, 7, 1),
				endOfDayEpoch(2025, 7, 31), MONDAY, WEEKLY, occupiedRanges);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getStartDate()).isEqualTo(startOfDayEpoch(2025, 7, 31));
		assertThat(result.get(0).getEndDate()).isEqualTo(endOfDayEpoch(2025, 7, 31));
	}

	@Test
	@DisplayName("Calculate free slots for a single-day job aligned with the weekly start day should return one slot")
	void testCalculateFreeSlotsSingleDayJobAlignedWeeklyReturnsSlot() {
		// Given - single-day job on Monday Jul 7 2025 with weekly start day Monday
		List<TimeSlotsResultBodyDto> result = this.freeSlotCalculator.calculateFreeSlots(startOfDayEpoch(2025, 7, 7),
				endOfDayEpoch(2025, 7, 7), MONDAY, WEEKLY, List.of());

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getStartDate()).isEqualTo(startOfDayEpoch(2025, 7, 7));
		assertThat(result.get(0).getEndDate()).isEqualTo(endOfDayEpoch(2025, 7, 7));
	}

	@Test
	@DisplayName("Calculate free slots for a single-day job misaligned with the weekly start day should return empty")
	void testCalculateFreeSlotsSingleDayJobMisalignedWeeklyReturnsEmpty() {
		// Given - single-day job on Tuesday Jul 8 2025 with weekly start day Monday
		List<TimeSlotsResultBodyDto> result = this.freeSlotCalculator.calculateFreeSlots(startOfDayEpoch(2025, 7, 8),
				endOfDayEpoch(2025, 7, 8), MONDAY, WEEKLY, List.of());

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Calculate free slots for a single-day job aligned with the monthly start day should return one slot")
	void testCalculateFreeSlotsSingleDayJobAlignedMonthlyReturnsSlot() {
		// Given - single-day job on Jul 15 2025 with monthly start day 15
		List<TimeSlotsResultBodyDto> result = this.freeSlotCalculator.calculateFreeSlots(startOfDayEpoch(2025, 7, 15),
				endOfDayEpoch(2025, 7, 15), 15, MONTHLY, List.of());

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getStartDate()).isEqualTo(startOfDayEpoch(2025, 7, 15));
		assertThat(result.get(0).getEndDate()).isEqualTo(endOfDayEpoch(2025, 7, 15));
	}

	@Test
	@DisplayName("Calculate free slots for a single-day job on the last day of month with start day 100 should return one slot")
	void testCalculateFreeSlotsSingleDayJobLastDayOfMonthCodeReturnsSlot() {
		// Given - single-day job on Jul 31 2025 with monthly start day 100 (last day)
		List<TimeSlotsResultBodyDto> result = this.freeSlotCalculator.calculateFreeSlots(startOfDayEpoch(2025, 7, 31),
				endOfDayEpoch(2025, 7, 31), LAST_DAY_OF_MONTH, MONTHLY, List.of());

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getStartDate()).isEqualTo(startOfDayEpoch(2025, 7, 31));
		assertThat(result.get(0).getEndDate()).isEqualTo(endOfDayEpoch(2025, 7, 31));
	}

	@Test
	@DisplayName("Calculate free slots for a single-day job misaligned with the monthly start day should return empty")
	void testCalculateFreeSlotsSingleDayJobMisalignedMonthlyReturnsEmpty() {
		// Given - single-day job on Jul 15 2025 with monthly start day 10
		List<TimeSlotsResultBodyDto> result = this.freeSlotCalculator.calculateFreeSlots(startOfDayEpoch(2025, 7, 15),
				endOfDayEpoch(2025, 7, 15), 10, MONTHLY, List.of());

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Calculate free slots should skip the starting partial when it cannot fit inside the gap")
	void testCalculateFreeSlotsStartingPartialExceedingGapEndIsSkipped() {
		// Given - job starts Tuesday Jul 1 2025 but Jul 3 - Jul 31 is occupied; the
		// starting partial would need to run to Sunday Jul 6, past the gap end Jul 2
		List<List<Integer>> occupiedRanges = List
			.of(Arrays.asList(startOfDayEpoch(2025, 7, 3), endOfDayEpoch(2025, 7, 31)));

		// When
		List<TimeSlotsResultBodyDto> result = this.freeSlotCalculator.calculateFreeSlots(startOfDayEpoch(2025, 7, 1),
				endOfDayEpoch(2025, 7, 31), MONDAY, WEEKLY, occupiedRanges);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Calculate free slots should treat null occupied ranges the same as no occupied ranges")
	void testCalculateFreeSlotsNullOccupiedRangesReturnsAllSlots() {
		// When - job Jul 1 - Jul 31 2025, weekly Monday, null occupied ranges
		List<TimeSlotsResultBodyDto> result = this.freeSlotCalculator.calculateFreeSlots(startOfDayEpoch(2025, 7, 1),
				endOfDayEpoch(2025, 7, 31), MONDAY, WEEKLY, null);

		// Then - starting partial, 3 full weeks, ending partial
		assertThat(result).hasSize(5);
		assertThat(result.get(0).getStartDate()).isEqualTo(startOfDayEpoch(2025, 7, 1));
		assertThat(result.get(4).getEndDate()).isEqualTo(endOfDayEpoch(2025, 7, 31));
	}

	@Test
	@DisplayName("Calculate free slots should keep the longer range when merging a fully contained occupied range")
	void testCalculateFreeSlotsContainedOccupiedRangeIsMergedIntoEnclosingRange() {
		// Given - Jul 10-13 is fully inside Jul 7-20, so the merge keeps Jul 7-20
		List<List<Integer>> occupiedRanges = Arrays.asList(
				Arrays.asList(startOfDayEpoch(2025, 7, 7), endOfDayEpoch(2025, 7, 20)),
				Arrays.asList(startOfDayEpoch(2025, 7, 10), endOfDayEpoch(2025, 7, 13)));

		// When
		List<TimeSlotsResultBodyDto> result = this.freeSlotCalculator.calculateFreeSlots(startOfDayEpoch(2025, 7, 1),
				endOfDayEpoch(2025, 7, 31), MONDAY, WEEKLY, occupiedRanges);

		// Then - starting partial Jul 1-6, full week Jul 21-27, ending partial Jul 28-31
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getStartDate()).isEqualTo(startOfDayEpoch(2025, 7, 1));
		assertThat(result.get(0).getEndDate()).isEqualTo(endOfDayEpoch(2025, 7, 6));
		assertThat(result.get(1).getStartDate()).isEqualTo(startOfDayEpoch(2025, 7, 21));
		assertThat(result.get(1).getEndDate()).isEqualTo(endOfDayEpoch(2025, 7, 27));
		assertThat(result.get(2).getStartDate()).isEqualTo(startOfDayEpoch(2025, 7, 28));
		assertThat(result.get(2).getEndDate()).isEqualTo(endOfDayEpoch(2025, 7, 31));
	}

	@Test
	@DisplayName("Find monthly starting partial end date with start day 100 should end on last day minus one")
	void testFindMonthlyStartingPartialEndDateLastDayCodeReturnsLastDayMinusOne() {
		// When - July 2025 has 31 days, so the partial must end on the 30th
		LocalDate result = this.freeSlotCalculator.findMonthlyStartingPartialEndDate(LocalDate.of(2025, 7, 10),
				LAST_DAY_OF_MONTH);

		// Then
		assertThat(result).isEqualTo(LocalDate.of(2025, 7, 30));
	}

	@Test
	@DisplayName("Find monthly starting partial end date with start day 1 should end on the last day of the month")
	void testFindMonthlyStartingPartialEndDateStartDayOneReturnsEndOfMonth() {
		// When
		LocalDate result = this.freeSlotCalculator.findMonthlyStartingPartialEndDate(LocalDate.of(2025, 7, 10), 1);

		// Then
		assertThat(result).isEqualTo(LocalDate.of(2025, 7, 31));
	}

	@Test
	@DisplayName("Find monthly starting partial end date with start day 100 past the target should roll to next month")
	void testFindMonthlyStartingPartialEndDateLastDayCodeRollsToNextMonth() {
		// When - gap starts Jul 31, past the July target (30th); August has 31 days so
		// the partial must end on Aug 30
		LocalDate result = this.freeSlotCalculator.findMonthlyStartingPartialEndDate(LocalDate.of(2025, 7, 31),
				LAST_DAY_OF_MONTH);

		// Then
		assertThat(result).isEqualTo(LocalDate.of(2025, 8, 30));
	}

	@Test
	@DisplayName("Find monthly starting partial end date past the target day should roll to the next month")
	void testFindMonthlyStartingPartialEndDatePastTargetDayRollsToNextMonth() {
		// When - gap starts Jul 20 with start day 15, so the partial must end Aug 14
		LocalDate result = this.freeSlotCalculator.findMonthlyStartingPartialEndDate(LocalDate.of(2025, 7, 20), 15);

		// Then
		assertThat(result).isEqualTo(LocalDate.of(2025, 8, 14));
	}

}
