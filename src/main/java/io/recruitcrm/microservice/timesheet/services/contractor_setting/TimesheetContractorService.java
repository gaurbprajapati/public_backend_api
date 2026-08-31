package io.recruitcrm.microservice.timesheet.services.contractor_setting;

import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingFrequencyTypeEnum;
import io.recruitcrm.entity.model.AssignCandidateJob;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.dao.assigned_candidate.AssignCandidateJobJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.ContractorTimesheetSettingResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.GetContractorListRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.OccupiedSlotsQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.EmptySlotRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.record.MonthlySlotCalculationContext;
import io.recruitcrm.microservice.timesheet.helpers.record.SevenDaySlotCalculationContext;
import io.recruitcrm.microservice.timesheet.helpers.record.SlotCalculationParameters;
import io.recruitcrm.microservice.timesheet.repositories.contractor_setting.TimesheetContractorSettingRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_setting.TimesheetSettingRepository;
import io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TimesheetContractorService implements ITimesheetContractorService {

	private final TimesheetSettingRepository timesheetSettingRepository;

	private final AssignCandidateJobJpaRepository assignCandidateJobJpaRepository;

	private final TimesheetContractorSettingRepository timesheetContractorSettingRepository;

	private final AuthHolder auth;

	private final PortalAccessControlService portalAccessControlService;

	private final FreeSlotCalculator freeSlotCalculator;

	public TimesheetContractorService(TimesheetSettingRepository timesheetSettingRepository,
			AssignCandidateJobJpaRepository assignCandidateJobJpaRepository,
			TimesheetContractorSettingRepository timesheetContractorSettingRepository, AuthHolder auth,
			PortalAccessControlService portalAccessControlService, FreeSlotCalculator freeSlotCalculator) {
		this.timesheetSettingRepository = timesheetSettingRepository;
		this.assignCandidateJobJpaRepository = assignCandidateJobJpaRepository;
		this.timesheetContractorSettingRepository = timesheetContractorSettingRepository;
		this.auth = auth;
		this.portalAccessControlService = portalAccessControlService;
		this.freeSlotCalculator = freeSlotCalculator;
	}

	@Override
	public Map<Integer, ContractorTimesheetSettingResponseBodyDto> getContractorTimesheetSettings(
			GetContractorListRequestBodyDto requestDto) {

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		List<ContractorTimesheetSettingResponseBodyDto> responseList = new ArrayList<>();
		Map<Integer, ContractorTimesheetSettingResponseBodyDto> responseMap = new HashMap<>();

		for (Integer contractorId : requestDto.getContractorIds()) {
			Integer jobId = requestDto.getJobId();

			// Step 1: Check if assignment exists
			AssignCandidateJob assignment = this.assignCandidateJobJpaRepository
				.findByJobIdAndCandidateIdAndAccountId(jobId, contractorId, accountId);
			if (assignment == null) {
				throw new ResourceNotFoundException(
						"TimesheetSetting not found for Job Id: " + jobId + " candidate Id : " + contractorId);
			}

			// Step 2: Check if timesheet settings exists with jobId and candidateId
			TimesheetSetting timesheetSetting = this.timesheetSettingRepository
				.findByJobIdContractorId(jobId, contractorId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"TimesheetSetting not found for Job ID: " + jobId + " and Contractor ID: " + contractorId));

			ContractorTimesheetSettingResponseBodyDto responseDto = new ContractorTimesheetSettingResponseBodyDto();
			responseDto.setTimesheetSettingId(timesheetSetting.getId());
			responseDto.setStartDate(timesheetSetting.getJobStartDate());
			responseDto.setEndDate(timesheetSetting.getJobEndDate());
			responseDto.setTimesheetStartDate(timesheetSetting.getTimesheetStartDay());
			responseDto.setTimesheetFrequency(timesheetSetting.getTimesheetFrequency());
			responseMap.put(contractorId, responseDto);
			responseList.add(responseDto);
		}

		return responseMap;
	}

	@Override
	public List<TimeSlotsResultBodyDto> getFreeSlots(EmptySlotRequestBodyDto requestDto, Integer timesheetFrequencyId) {
		AuthPrincipal principal = this.auth.getUnifiedPrincipal();
		if (principal == null || principal.getPrincipalType() == null) {
			throw new UnauthorizedAccessException("Unknown persona type");
		}

		switch (principal.getPrincipalType()) {
			case USER -> {
				return this.getFreeSlotsForUser(requestDto, timesheetFrequencyId);
			}
			case CONTRACTOR -> {
				return this.getFreeSlotsForContractor(requestDto, timesheetFrequencyId);
			}
			case CONTACT -> {
				return this.getFreeSlotsForContact(requestDto, timesheetFrequencyId, principal);
			}
			default -> throw new UnauthorizedAccessException("Unknown persona type");
		}
	}

	/**
	 * Get free slots for USER persona (agency users)
	 */
	private List<TimeSlotsResultBodyDto> getFreeSlotsForUser(EmptySlotRequestBodyDto requestDto,
			Integer timesheetFrequencyId) {

		return this.performFreeSlotsCalculation(requestDto, timesheetFrequencyId);
	}

	/**
	 * Get free slots for CONTRACTOR persona
	 */
	private List<TimeSlotsResultBodyDto> getFreeSlotsForContractor(EmptySlotRequestBodyDto requestDto,
			Integer timesheetFrequencyId) {

		return this.performFreeSlotsCalculation(requestDto, timesheetFrequencyId);
	}

	private List<TimeSlotsResultBodyDto> getFreeSlotsForContact(EmptySlotRequestBodyDto requestDto,
			Integer timesheetFrequencyId, AuthPrincipal principal) {

		ContactPrincipal contactPrincipal = (ContactPrincipal) principal;
		Integer clientId = contactPrincipal.getContactId();
		Integer jobId = requestDto.getJobId();

		// access control
		PortalTimesheetPermissionDto permissions = this.portalAccessControlService.validatePortalAccessControl(jobId,
				clientId);

		// Step 2: Check CREATE_TIMESHEET permission specifically
		if (permissions.getCanCreate() == null || permissions.getCanCreate() != 1) {
			throw new UnauthorizedAccessException("Unauthorized access for create timesheet");
		}

		return this.performFreeSlotsCalculation(requestDto, timesheetFrequencyId);
	}

	/**
	 * Common free slots calculation logic. Called after persona-specific access control
	 * validation.
	 *
	 * <p>
	 * Uses the FreeSlotCalculator which handles all frequency types (weekly, biweekly,
	 * monthly) with proper alignment and partial slot generation.
	 */
	private List<TimeSlotsResultBodyDto> performFreeSlotsCalculation(EmptySlotRequestBodyDto requestDto,
			Integer timesheetFrequencyId) {

		validateFreeSlotsDateRange(requestDto.getStartDate(), requestDto.getEndDate());

		List<OccupiedSlotsQueryResultDto> occupiedSlotsQueryResultDtos = this.timesheetContractorSettingRepository
			.findTimesheetsWithinDateRangeAndContractors(requestDto.getStartDate(), requestDto.getEndDate(),
					requestDto.getContractorIds(), requestDto.getJobId());

		List<List<Integer>> occupiedRanges = occupiedSlotsQueryResultDtos.stream()
			.map((dto) -> List.of(dto.getPeriodStart(), dto.getPeriodEnd()))
			.toList();

		return this.freeSlotCalculator.calculateFreeSlots(requestDto.getStartDate(), requestDto.getEndDate(),
				requestDto.getTimesheetStartDay(), timesheetFrequencyId, occupiedRanges);
	}

	public List<TimeSlotsResultBodyDto> getFreeSlotsBetweenStartAndEndTime(List<List<Integer>> allOccupiedSlots,
			Integer globalStart, Integer globalEnd, Integer timesheetStartDay, Integer timesheetFrequencyId) {
		SlotCalculationParameters parameters = this.createSlotCalculationParameters(timesheetFrequencyId);

		if (allOccupiedSlots.isEmpty()) {
			return this.splitByFrequency(globalStart, globalEnd, parameters.slotSeconds(), timesheetStartDay,
					timesheetFrequencyId, globalStart);
		}

		List<List<Integer>> mergedOccupiedSlots = this.mergeOverlappingSlots(allOccupiedSlots);
		return this.calculateFreeSlots(mergedOccupiedSlots, globalStart, globalEnd, parameters.slotSeconds(),
				timesheetStartDay, timesheetFrequencyId);
	}

	private SlotCalculationParameters createSlotCalculationParameters(Integer timesheetFrequencyId) {
		int slotSeconds = 6 * 24 * 60 * 60; // 7 days by default
		if (timesheetFrequencyId.equals(TimesheetSettingFrequencyTypeEnum.BIWEEKLY.getId())) {
			slotSeconds = 13 * 24 * 60 * 60; // 14 days in seconds
		}
		else if (timesheetFrequencyId.equals(TimesheetSettingFrequencyTypeEnum.MONTHLY.getId())) {
			slotSeconds = 29 * 24 * 60 * 60; // 30 days in seconds
		}
		return new SlotCalculationParameters(slotSeconds);
	}

	private List<List<Integer>> mergeOverlappingSlots(List<List<Integer>> allOccupiedSlots) {
		// Step 1: Sort all intervals by start time
		List<List<Integer>> sortedOccupiedSlots = allOccupiedSlots.stream()
			.sorted(Comparator.comparingInt((interval) -> interval.get(0)))
			.toList();

		// Step 2: Merge overlapping intervals
		List<List<Integer>> merged = new ArrayList<>();
		for (List<Integer> interval : sortedOccupiedSlots) {
			if (merged.isEmpty() || merged.get(merged.size() - 1).get(1) < interval.get(0)) {
				merged.add(new ArrayList<>(interval));
			}
			else {
				List<Integer> last = merged.get(merged.size() - 1);
				last.set(1, Math.max(last.get(1), interval.get(1)));
			}
		}
		return merged;
	}

	public List<TimeSlotsResultBodyDto> calculateFreeSlots(List<List<Integer>> merged, Integer globalStart,
			Integer globalEnd, final int slotSeconds, Integer timesheetStartDay, Integer timesheetFrequencyId) {
		List<TimeSlotsResultBodyDto> freeSlots = new ArrayList<>();

		// Free time before the first occupied slot
		if (globalStart < merged.get(0).get(0)) {
			freeSlots.addAll(this.splitByFrequency(globalStart, merged.get(0).get(0), slotSeconds, timesheetStartDay,
					timesheetFrequencyId, globalStart));
		}

		// Free time between occupied intervals
		for (int i = 1; i < merged.size(); i++) {
			int prevSlotEnd = merged.get(i - 1).get(1);
			int currSlotStart = merged.get(i).get(0);

			if (prevSlotEnd < currSlotStart) {
				freeSlots.addAll(this.splitByFrequency(prevSlotEnd, currSlotStart, slotSeconds, timesheetStartDay,
						timesheetFrequencyId, globalStart));
			}
		}

		// Free time after the last occupied slot
		int lastSlotEnd = merged.get(merged.size() - 1).get(1);
		if (lastSlotEnd < globalEnd) {
			freeSlots.addAll(this.splitByFrequency(lastSlotEnd, globalEnd, slotSeconds, timesheetStartDay,
					timesheetFrequencyId, globalStart));
		}
		return freeSlots;
	}

	private List<TimeSlotsResultBodyDto> splitByFrequency(int start, int end, final int slotSeconds,
			int timesheetStartDay, Integer timesheetFrequencyId, Integer globalStart) {
		if (timesheetFrequencyId.equals(TimesheetSettingFrequencyTypeEnum.BIWEEKLY.getId())) {
			return this.splitIntoFourteenDaySlots(start, end, slotSeconds, timesheetStartDay, globalStart);
		}
		else if (timesheetFrequencyId.equals(TimesheetSettingFrequencyTypeEnum.MONTHLY.getId())) {
			return this.splitIntoThirtyDaySlots(start, end, timesheetStartDay);
		}
		else {
			return this.splitIntoSevenDaySlots(start, end, slotSeconds, timesheetStartDay);
		}
	}

	/**
	 * Splits the interval [start, end) into full 7-day slots. Discards any remaining time
	 * less than 7 days.
	 */
	private List<TimeSlotsResultBodyDto> splitIntoSevenDaySlots(int start, int end, final int sevenDaysSeconds,
			int timesheetStartDay) {
		List<TimeSlotsResultBodyDto> slots = new ArrayList<>();
		SevenDaySlotCalculationContext context = this.initializeSevenDaySlotContext(start, timesheetStartDay);

		int currentStart = this.determineSevenDaySlotCurrentStart(context, end);

		// Now, create full 7-day slots from currentStart
		while (currentStart + sevenDaysSeconds < end) {
			int slotEnd = currentStart + sevenDaysSeconds + 86399;
			TimeSlotsResultBodyDto slot = new TimeSlotsResultBodyDto();
			slot.setStartDate(currentStart);
			slot.setEndDate(slotEnd);
			slots.add(slot);
			currentStart = slotEnd + 1; // Increase the start day by one
		}
		return slots;
	}

	private SevenDaySlotCalculationContext initializeSevenDaySlotContext(int start, int timesheetStartDay) {
		LocalDateTime globalStartDateTime = LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC);
		int globalStartDayOfWeek = globalStartDateTime.getDayOfWeek().getValue(); // 1=Monday,
																					// 7=Sunday

		// Check if start + 1 second changes to the next day, then add 1 second to start
		LocalDateTime startPlusOneSecond = LocalDateTime.ofEpochSecond(start + 1L, 0, ZoneOffset.UTC);
		int adjustedStart = start;
		LocalDateTime adjustedStartDateTime = globalStartDateTime;
		int adjustedStartDayOfWeek = globalStartDayOfWeek;

		if (startPlusOneSecond.getDayOfYear() != globalStartDateTime.getDayOfYear()
				|| startPlusOneSecond.getYear() != globalStartDateTime.getYear()) {
			adjustedStart = start + 1;
			adjustedStartDateTime = startPlusOneSecond;
			adjustedStartDayOfWeek = adjustedStartDateTime.getDayOfWeek().getValue();
		}

		// Find the first occurrence of the timesheetStartDay on or after globalStart
		int daysToFirstStartDay = (timesheetStartDay - adjustedStartDayOfWeek + 7) % 7;
		LocalDateTime firstStartDayDateTime = adjustedStartDateTime.plusDays(daysToFirstStartDay)
			.withHour(0)
			.withMinute(0)
			.withSecond(0);
		int firstStartDayEpoch = (int) firstStartDayDateTime.toEpochSecond(ZoneOffset.UTC);

		return new SevenDaySlotCalculationContext(adjustedStart, adjustedStartDateTime, adjustedStartDayOfWeek,
				firstStartDayEpoch, timesheetStartDay);
	}

	private int determineSevenDaySlotCurrentStart(SevenDaySlotCalculationContext context, int end) {
		List<TimeSlotsResultBodyDto> slots = new ArrayList<>();

		// If global start is after the first start day in the week, create a partial slot
		if (context.adjustedStart() > context.firstStartDayEpoch()) {
			// Partial slot: from global start to next occurrence of timesheetStartDay
			LocalDateTime nextStartDayDateTime = context.adjustedStartDateTime()
				.plusDays((7 - (context.adjustedStartDayOfWeek() - context.timesheetStartDay() + 7) % 7) % 7)
				.withHour(0)
				.withMinute(0)
				.withSecond(0);
			int nextStartDayEpoch = (int) nextStartDayDateTime.toEpochSecond(ZoneOffset.UTC);
			int partialSlotEnd = Math.min(nextStartDayEpoch, end);
			if (partialSlotEnd > context.adjustedStart()) {
				TimeSlotsResultBodyDto partialSlot = new TimeSlotsResultBodyDto();
				partialSlot.setStartDate(context.adjustedStart());
				partialSlot.setEndDate(partialSlotEnd);
				slots.add(partialSlot);
			}
			return partialSlotEnd;
		}
		else {
			// Align to the first start day on or after global start
			return context.firstStartDayEpoch();
		}
	}

	// 14-day slot splitting
	private List<TimeSlotsResultBodyDto> splitIntoFourteenDaySlots(int start, int end, final int fourteenDaysSeconds,
			int timesheetStartDay, Integer globalStart) {
		List<TimeSlotsResultBodyDto> slots = new ArrayList<>();
		int currentStart = this.determineFourteenDaySlotCurrentStart(start, timesheetStartDay, globalStart);

		while (currentStart + fourteenDaysSeconds < end) {
			int slotEnd = currentStart + fourteenDaysSeconds + 86399;
			TimeSlotsResultBodyDto slot = new TimeSlotsResultBodyDto();
			slot.setStartDate(currentStart);
			slot.setEndDate(slotEnd);
			slots.add(slot);
			currentStart = slotEnd + 1; // Next slot starts after 1 day
		}
		return slots;
	}

	private int determineFourteenDaySlotCurrentStart(int start, int timesheetStartDay, Integer globalStart) {
		if (start != globalStart) {
			return getCurrentStart(start, timesheetStartDay);
		}
		else {
			// When no occupied slots exist, find the first timesheetStartDay that is at
			// least 7 days after start
			LocalDateTime startDateTime = LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC);
			LocalDateTime targetDateTime = startDateTime.plusDays(7); // At least 7 days
																		// after start

			// Find the first occurrence of timesheetStartDay on or after targetDateTime
			int targetDayOfWeek = targetDateTime.getDayOfWeek().getValue();
			int daysToAdd = (timesheetStartDay - targetDayOfWeek + 7) % 7;

			LocalDateTime firstValidStartDay = targetDateTime.plusDays(daysToAdd)
				.withHour(0)
				.withMinute(0)
				.withSecond(0);

			return (int) firstValidStartDay.toEpochSecond(ZoneOffset.UTC);
		}
	}

	private static int getCurrentStart(int start, int timesheetStartDay) {
		LocalDateTime globalStartDateTime = LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC);
		int globalStartDayOfWeek = globalStartDateTime.getDayOfWeek().getValue();
		int daysToFirstStartDay = (timesheetStartDay - globalStartDayOfWeek + 7) % 7;
		LocalDateTime firstStartDayDateTime = globalStartDateTime.plusDays(daysToFirstStartDay)
			.withHour(0)
			.withMinute(0)
			.withSecond(0);
		int firstStartDayEpoch = (int) firstStartDayDateTime.toEpochSecond(ZoneOffset.UTC);
		return Math.max(start, firstStartDayEpoch);
	}

	// 30-day slot splitting
	private List<TimeSlotsResultBodyDto> splitIntoThirtyDaySlots(int start, int end, int timesheetStartDay) {
		// in this method we find free slots between start and end
		List<TimeSlotsResultBodyDto> slots = new ArrayList<>();
		MonthlySlotCalculationContext context = this.initializeMonthlySlotContext(start, timesheetStartDay);

		int currentStart = context.currentStartEpoch();
		int relativeOneMonthFromCurrentStartEpoch = context.relativeOneMonthFromCurrentStartEpoch();
		LocalDateTime oneMonthFromCurrentStart = context.oneMonthFromCurrentStart();

		while (currentStart < start) {
			currentStart += ((int) oneMonthFromCurrentStart.toEpochSecond(ZoneOffset.UTC) - currentStart);
		}
		while (currentStart + relativeOneMonthFromCurrentStartEpoch < end) {
			int slotEnd = (int) LocalDateTime.ofEpochSecond(currentStart, 0, ZoneOffset.UTC)
				.plusMonths(1)
				.minusSeconds(1)
				.toEpochSecond(ZoneOffset.UTC);
			TimeSlotsResultBodyDto slot = new TimeSlotsResultBodyDto();
			slot.setStartDate(currentStart);
			slot.setEndDate(slotEnd);
			slots.add(slot);
			currentStart = slotEnd + 1; // Next slot starts after 1 day
			LocalDateTime dateTime = LocalDateTime.ofEpochSecond(currentStart, 0, ZoneOffset.UTC);
			LocalDateTime oneMonthFromCurrentStartMinusOne = dateTime.plusMonths(1).minusDays(1);
			int oneMonthFromCurrentStartEpoch = (int) oneMonthFromCurrentStartMinusOne.toEpochSecond(ZoneOffset.UTC);
			relativeOneMonthFromCurrentStartEpoch = oneMonthFromCurrentStartEpoch - currentStart;
		}
		return slots;
	}

	private MonthlySlotCalculationContext initializeMonthlySlotContext(int start, int timesheetStartDay) {
		int currentStart = timesheetStartDay; // start day of the month
		// converting start to LocalDateTime to get the month and year
		LocalDateTime dateTime = LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC);
		int month = dateTime.getMonthValue();
		int year = dateTime.getYear();

		if (currentStart == 100) {
			currentStart = this.calculateLastDayOfMonth(month, year);
		}

		// generating a date for currentStart
		dateTime = LocalDateTime.of(year, month, currentStart, 0, 0, 0);

		// Convert to epoch seconds
		int currentStartEpoch = (int) dateTime.toEpochSecond(ZoneOffset.UTC);
		LocalDateTime oneMonthFromCurrentStart = dateTime.plusMonths(1);
		LocalDateTime oneMonthFromCurrentStartMinusOne = dateTime.plusMonths(1).minusDays(1);
		int oneMonthFromCurrentStartEpoch = (int) oneMonthFromCurrentStartMinusOne.toEpochSecond(ZoneOffset.UTC);
		int relativeOneMonthFromCurrentStartEpoch = oneMonthFromCurrentStartEpoch - currentStartEpoch;

		return new MonthlySlotCalculationContext(currentStartEpoch, oneMonthFromCurrentStart,
				relativeOneMonthFromCurrentStartEpoch);
	}

	private int calculateLastDayOfMonth(int month, int year) {
		return switch (month) {
			case 1, 3, 5, 7, 8, 10, 12 -> 31;
			case 2 -> ((year % 4 == 0) && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
			default -> 30;
		};
	}

	/**
	 * Finds breakpoint based on frequency type and timesheet start day within the given
	 * date range.
	 * @param frequencyType The frequency type (WEEKLY, BIWEEKLY, MONTHLY)
	 * @param globalStart Global start date in epoch seconds
	 * @param globalEnd Global end date in epoch seconds
	 * @param timesheetStartDay For weekly/biweekly: day of week (1=Monday, 7=Sunday), For
	 * monthly: day of month (1-31)
	 * @return Optional containing the breakpoint epoch if found, empty otherwise
	 */
	public Integer findBreakpoint(Integer frequencyType, Integer globalStart, Integer globalEnd,
			Integer timesheetStartDay, Boolean endingPartialSlot) {

		if (TimesheetSettingFrequencyTypeEnum.WEEKLY.getId().equals(frequencyType)) {
			return this.findWeeklyBreakpoint(globalStart, globalEnd, timesheetStartDay, endingPartialSlot);
		}
		else if (TimesheetSettingFrequencyTypeEnum.BIWEEKLY.getId().equals(frequencyType)) {
			return this.findBiweeklyBreakpoint(globalStart, globalEnd, timesheetStartDay, endingPartialSlot);
		}
		else if (TimesheetSettingFrequencyTypeEnum.MONTHLY.getId().equals(frequencyType)) {
			return this.findMonthlyBreakpoint(globalStart, globalEnd, timesheetStartDay, endingPartialSlot);
		}
		else {
			return 0;
		}
	}

	/**
	 * Finds weekly breakpoint - returns first occurrence of timesheetStartDay between
	 * global start and end.
	 */
	private Integer findWeeklyBreakpoint(Integer globalStart, Integer globalEnd, Integer timesheetStartDay,
			Boolean endingPartialSlot) {
		if (endingPartialSlot.equals(Boolean.FALSE)) {
			// since if endingPartialSlot is false it means that now only front slot has
			// to be set and to set it the breakpoint will be global end date
			return globalEnd;
		}
		LocalDateTime startDateTime = LocalDateTime.ofEpochSecond(globalStart, 0, ZoneOffset.UTC);
		LocalDateTime endDateTime = LocalDateTime.ofEpochSecond(globalEnd, 0, ZoneOffset.UTC);

		// Check if the timesheet start day lies within the date range
		LocalDateTime currentDateTime = startDateTime;

		while (!currentDateTime.isAfter(endDateTime)) {
			if (currentDateTime.getDayOfWeek().getValue() == timesheetStartDay) {
				// Found the timesheet start day within range
				LocalDateTime breakpointDateTime = currentDateTime.minusDays(1)
					.withHour(23)
					.withMinute(59)
					.withSecond(59);
				return (int) breakpointDateTime.toEpochSecond(ZoneOffset.UTC);
			}
			currentDateTime = currentDateTime.plusDays(1);
		}

		return 0;
	}

	/**
	 * Finds biweekly breakpoint - returns second occurrence of timesheetStartDay between
	 * global start and end.
	 */
	private Integer findBiweeklyBreakpoint(Integer globalStart, Integer globalEnd, Integer timesheetStartDay,
			Boolean endingPartialSlot) {

		LocalDateTime startDateTime = LocalDateTime.ofEpochSecond(globalStart, 0, ZoneOffset.UTC);
		LocalDateTime endDateTime = LocalDateTime.ofEpochSecond(globalEnd, 0, ZoneOffset.UTC);

		if (endingPartialSlot.equals(Boolean.FALSE)) {
			if (endDateTime.getDayOfWeek().getValue() == timesheetStartDay
					&& Math.abs(startDateTime.getDayOfMonth() - endDateTime.getDayOfMonth()) > 7) {
				return globalEnd;
			}
			// since if endingPartialSlot is false it means that now only front slot has
			// to be set and to set it the breakpoint will be global end date
			return 0;
		}

		LocalDateTime currentDateTime = startDateTime;
		int occurrenceCount = 0;

		while (!currentDateTime.isAfter(endDateTime)) {
			if (currentDateTime.getDayOfWeek().getValue() == timesheetStartDay) {
				occurrenceCount++;

				if (occurrenceCount == 2) {
					// Found the second occurrence
					LocalDateTime breakpointDateTime = currentDateTime.minusDays(1)
						.withHour(23)
						.withMinute(59)
						.withSecond(59);
					return (int) breakpointDateTime.toEpochSecond(ZoneOffset.UTC);
				}
			}
			currentDateTime = currentDateTime.plusDays(1);
		}

		// Return empty if we don't have at least 2 occurrences
		return 0;
	}

	/**
	 * Finds monthly breakpoint - returns first occurrence of timesheetStartDay (date)
	 * between global start and end.
	 */
	private Integer findMonthlyBreakpoint(Integer globalStart, Integer globalEnd, Integer timesheetStartDay,
			Boolean endingPartialSlot) {

		LocalDateTime startDateTime = LocalDateTime.ofEpochSecond(globalStart, 0, ZoneOffset.UTC);
		LocalDateTime endDateTime = LocalDateTime.ofEpochSecond(globalEnd, 0, ZoneOffset.UTC);

		LocalDateTime currentDateTime = startDateTime;

		if (timesheetStartDay == 100) {
			timesheetStartDay = calculateLastDayOfMonth(startDateTime.getMonthValue(), startDateTime.getYear());
		}

		if (endingPartialSlot.equals(Boolean.FALSE)) {
			if (endDateTime.plusDays(1).getDayOfMonth() == timesheetStartDay) {
				// since if endingPartialSlot is false it means that now only front slot
				// has
				// to be set and to set it the breakpoint will be global end date
				return (int) endDateTime.plusDays(1).toEpochSecond(ZoneOffset.UTC);
			}
			else {
				return globalEnd;
			}
		}

		while (!currentDateTime.isAfter(endDateTime)) {
			if (currentDateTime.getDayOfMonth() == timesheetStartDay) {
				LocalDateTime breakpointDateTime = currentDateTime.minusDays(1)
					.withHour(23)
					.withMinute(59)
					.withSecond(59);

				// Found the timesheet start day (date) within range

				return (int) breakpointDateTime.toEpochSecond(ZoneOffset.UTC);

			}
			currentDateTime = currentDateTime.plusDays(1);
		}

		return 0;
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimeSlotsResultBodyDto> getBulkFreeSlots(BulkEmptySlotRequestBodyDto requestDto) {
		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		switch (principal.getPrincipalType()) {
			case USER -> {
				return this.getBulkFreeSlotsForUser(requestDto);
			}
			case CONTRACTOR -> {
				return this.getBulkFreeSlotsForContractor(requestDto);
			}
			case CONTACT -> {
				return this.getBulkFreeSlotsForContact(requestDto, principal);
			}
			default -> throw new UnauthorizedAccessException("Unknown persona type");
		}
	}

	/**
	 * Get bulk free slots for USER persona (agency users)
	 */
	private List<TimeSlotsResultBodyDto> getBulkFreeSlotsForUser(BulkEmptySlotRequestBodyDto requestDto) {
		return this.performBulkFreeSlotsCalculation(requestDto);
	}

	/**
	 * Get bulk free slots for CONTRACTOR persona
	 */
	private List<TimeSlotsResultBodyDto> getBulkFreeSlotsForContractor(BulkEmptySlotRequestBodyDto requestDto) {
		return this.performBulkFreeSlotsCalculation(requestDto);
	}

	/**
	 * Get bulk free slots for CONTACT persona (client portal)
	 */
	private List<TimeSlotsResultBodyDto> getBulkFreeSlotsForContact(BulkEmptySlotRequestBodyDto requestDto,
			AuthPrincipal principal) {
		ContactPrincipal contactPrincipal = (ContactPrincipal) principal;
		Integer clientId = contactPrincipal.getContactId();

		// Validate access control for all unique jobIds from the pairs
		List<Integer> uniqueJobIds = requestDto.getContractorJobPairs()
			.stream()
			.map(ContractorJobPairDto::getJobId)
			.distinct()
			.toList();

		for (Integer jobId : uniqueJobIds) {
			PortalTimesheetPermissionDto permissions = this.portalAccessControlService
				.validatePortalAccessControl(jobId, clientId);

			if (permissions.getCanCreate() == null || permissions.getCanCreate() != 1) {
				throw new UnauthorizedAccessException("Unauthorized access for create timesheet for job: " + jobId);
			}
		}

		return this.performBulkFreeSlotsCalculation(requestDto);
	}

	/**
	 * Common bulk free slots calculation logic. Validates timesheet settings consistency
	 * and calculates common slots by merging all occupied slots from specific
	 * contractor-job pairs.
	 */
	private List<TimeSlotsResultBodyDto> performBulkFreeSlotsCalculation(BulkEmptySlotRequestBodyDto requestDto) {
		validateFreeSlotsDateRange(requestDto.getMaxJobStartDate(), requestDto.getMinJobEndDate());

		List<ContractorJobPairDto> contractorJobPairs = requestDto.getContractorJobPairs();

		// Step 1: Fetch all timesheet settings for specific contractor-job pairs
		List<TimesheetSetting> allSettings = this.timesheetSettingRepository
			.findLatestTimesheetSettingsForContractorJobPairs(contractorJobPairs);

		// Step 2: Validate that settings exist for all contractor-job pairs
		if (allSettings.isEmpty()) {
			return new ArrayList<>();
		}

		// Step 3: Validate all settings have the same frequency and start day
		Integer referenceFrequency = allSettings.get(0).getTimesheetFrequency();
		Integer referenceStartDay = allSettings.get(0).getTimesheetStartDay();

		for (TimesheetSetting setting : allSettings) {
			if (!Objects.equals(setting.getTimesheetFrequency(), referenceFrequency)
					|| !Objects.equals(setting.getTimesheetStartDay(), referenceStartDay)) {
				return new ArrayList<>();
			}
		}

		// Step 4: Fetch ALL occupied slots for specific contractor-job pairs in one query
		List<OccupiedSlotsQueryResultDto> occupiedSlotsQueryResultDtos = this.timesheetContractorSettingRepository
			.findTimesheetsForContractorJobPairs(requestDto.getMaxJobStartDate(), requestDto.getMinJobEndDate(),
					requestDto.getContractorJobPairs());

		// Convert to list of [start, end] pairs
		List<List<Integer>> occupiedRanges = occupiedSlotsQueryResultDtos.stream()
			.map((dto) -> List.of(dto.getPeriodStart(), dto.getPeriodEnd()))
			.toList();

		// Step 5: Boundary partial slots are only common to all pairs when the window
		// boundary is the actual job start/end date for every pair. The window is built
		// by the caller as max(jobStartDate)/min(jobEndDate) over the selected jobs, so
		// the boundary is shared by every job exactly when all settings agree on that
		// job date. A single pair therefore always allows partials, matching
		// /free-slots. Settings are compared among themselves (not against the request
		// epochs) so stored and request timestamps may differ in time-of-day.
		boolean allowStartingPartial = allSettings.stream()
			.map((setting) -> toUtcDate(setting.getJobStartDate()))
			.distinct()
			.count() == 1;
		boolean allowEndingPartial = allSettings.stream()
			.map((setting) -> toUtcDate(setting.getJobEndDate()))
			.distinct()
			.count() == 1;

		// Step 6: Calculate free slots using the calculator
		return this.freeSlotCalculator.calculateFreeSlots(requestDto.getMaxJobStartDate(),
				requestDto.getMinJobEndDate(), referenceStartDay, referenceFrequency, occupiedRanges,
				allowStartingPartial, allowEndingPartial);
	}

	private static LocalDate toUtcDate(Integer epochSeconds) {
		if (epochSeconds == null) {
			return null;
		}
		return Instant.ofEpochSecond(epochSeconds).atZone(ZoneOffset.UTC).toLocalDate();
	}

	protected void validateFreeSlotsDateRange(Integer startDate, Integer endDate) {
		validateEpochYear(startDate, "Job Start Date");
		validateEpochYear(endDate, "Job End Date");

		LocalDate start = Instant.ofEpochSecond(startDate).atZone(ZoneOffset.UTC).toLocalDate();
		LocalDate end = Instant.ofEpochSecond(endDate).atZone(ZoneOffset.UTC).toLocalDate();
		if (end.isAfter(start.plusYears(10).minusDays(1))) {
			throw new ValidationErrorException("Job Start Date and Job End Date must not exceed a span of 10 years");
		}
	}

	protected void validateEpochYear(Integer epochSeconds, String fieldName) {
		int year = Instant.ofEpochSecond(epochSeconds).atZone(ZoneOffset.UTC).getYear();
		if (year < 1970 || year > 9999) {
			throw new ValidationErrorException(
					"Invalid value for Year in " + fieldName + ". Year must be between 1970 and 9999");
		}
	}

}
