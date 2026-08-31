package io.recruitcrm.microservice.timesheet.repositories.timesheet_setting;

import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingUserPreference;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingBreakInfoDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingTemplateWorkDayDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ITimesheetSettingRepository {

	Boolean validateTimesheetSettingsConsistency(List<Integer> contractorIds, int contractorCount);

	List<TimesheetSetting> findLatestTimesheetSettingsByJobIdAndContractorIds(Integer jobId,
			List<Integer> contractorIds);

	Optional<TimesheetSetting> findByJobIdContractorId(Integer jobId, Integer contractorId);

	TimesheetSetting createTimesheetSetting(TimesheetSetting timesheetSetting);

	Optional<TimesheetSetting> findByIdAndAccountId(Integer id, Integer accountId);

	Boolean validateTimesheetsExist(List<Integer> timeLogDates, Integer accountId, Integer jobId,
			List<Integer> contractorIds);

	List<Integer> fetchEnabledAssignmentIds(List<Integer> assignmentIds, Integer accountId);

	List<TimesheetSettingTemplateWorkDayDto> findTimesheetSettingsWithTemplateWorkDayByIds(
			List<Integer> timesheetSettingIds);

	void saveUserPreference(String timesheetSettingJson, Integer addedBy, Integer addedByUserTypeId, Integer accountId);

	Optional<TimesheetSettingUserPreference> findUserPreferenceByAccountIdAndUserId(Integer accountId, Integer userId);

	List<TimesheetSetting> findLatestTimesheetSettingsForContractorJobPairs(
			List<ContractorJobPairDto> contractorJobPairs);

	void updateIsRemarkMandatoryByAssociationIds(List<Integer> associationIds, Integer isRemarkMandatory);

	void updateIsUnplannedHoursPayEnabledByAssociationIds(List<Integer> associationIds,
			Integer isUnplannedHoursPayEnabled);

	/**
	 * Fetches work_log_type for migration by timesheet setting IDs.
	 * @param timesheetSettingIds List of timesheet setting IDs
	 * @return Map of timesheet setting ID to work_log_type (1 = WORK_HOUR, 2 =
	 * START_AND_END_TIME)
	 */
	Map<Integer, Integer> findWorkLogTypeByIdIn(List<Integer> timesheetSettingIds);

	/**
	 * Fetches calculate_break_time and break_time_threshold for migration by timesheet
	 * setting IDs.
	 * @param timesheetSettingIds List of timesheet setting IDs
	 * @return Map of timesheet setting ID to break info (calculateBreakTime,
	 * breakTimeThreshold in seconds)
	 */
	Map<Integer, TimesheetSettingBreakInfoDto> findBreakInfoByIdIn(List<Integer> timesheetSettingIds);

}
