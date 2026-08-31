package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.contractor_setting.OccupiedSlotsQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto;

import java.util.Arrays;
import java.util.List;

/**
 * Test data factory for TimesheetContractorSettingRepository tests.
 */
public final class TimesheetContractorSettingRepositoryTestDataFactory {

	public static final Integer DEFAULT_START_DATE = 1704067200;

	public static final Integer DEFAULT_END_DATE = 1704153599;

	public static final Integer DEFAULT_JOB_ID = 500;

	public static final Integer DEFAULT_CONTRACTOR_ID = 101;

	private TimesheetContractorSettingRepositoryTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static OccupiedSlotsQueryResultDto createOccupiedSlot(Integer id, Integer periodStart, Integer periodEnd,
			Integer contractorId, Integer jobId, Integer timesheetStartDay) {
		return new OccupiedSlotsQueryResultDto(id, periodStart, periodEnd, contractorId, jobId, timesheetStartDay);
	}

	public static List<OccupiedSlotsQueryResultDto> createOccupiedSlots() {
		return Arrays.asList(createOccupiedSlot(1, DEFAULT_START_DATE, DEFAULT_END_DATE, 101, 500, 1),
				createOccupiedSlot(2, DEFAULT_START_DATE + 86400, DEFAULT_END_DATE + 86400, 102, 501, 7));
	}

	public static List<Integer> createContractorIds() {
		return Arrays.asList(101, 102);
	}

	public static ContractorJobPairDto createContractorJobPair(Integer contractorId, Integer jobId) {
		return new ContractorJobPairDto(contractorId, jobId);
	}

	public static List<ContractorJobPairDto> createContractorJobPairs() {
		return Arrays.asList(createContractorJobPair(101, 500), createContractorJobPair(102, 501));
	}

}
