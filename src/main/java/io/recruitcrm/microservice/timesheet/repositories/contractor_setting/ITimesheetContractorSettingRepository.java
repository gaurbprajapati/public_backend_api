package io.recruitcrm.microservice.timesheet.repositories.contractor_setting;

import io.recruitcrm.microservice.timesheet.dto.contractor_setting.OccupiedSlotsQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto;

import java.util.List;

public interface ITimesheetContractorSettingRepository {

	List<OccupiedSlotsQueryResultDto> findTimesheetsWithinDateRangeAndContractors(Integer startDate, Integer endDate,
			List<Integer> contractorIds, Integer jobId);

	List<OccupiedSlotsQueryResultDto> findTimesheetsForContractorJobPairs(Integer startDate, Integer endDate,
			List<ContractorJobPairDto> contractorJobPairs);

}
