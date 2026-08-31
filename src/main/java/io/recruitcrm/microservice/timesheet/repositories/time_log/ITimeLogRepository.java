package io.recruitcrm.microservice.timesheet.repositories.time_log;

import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimesheetJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationQueryResultDto;

import java.util.List;
import java.util.Map;

public interface ITimeLogRepository {

	void createTimesheetLog(Integer date, Integer dateTypeId, Integer timesheetId);

	void saveTimeLog(TimeLog timeLog);

	Boolean validateByDate(Integer jobId, Integer contractorId, Long startDate, Long endDate, Integer accountId);

	void createBulkTimesheetLogs(List<TimeLog> timeLogs);

	/**
	 * Batch fetch formatted time logs for multiple timesheets. This method replaces N
	 * individual queries with 1 optimized batch query.
	 * @param timesheetIds List of timesheet IDs to fetch time logs for
	 * @param accountId Account ID for security filtering
	 * @return Map of timesheet ID to formatted time log string
	 */
	Map<Integer, String> getTimeLogsForTimesheets(List<Integer> timesheetIds, Integer accountId);

	/**
	 * Batch fetch structured time logs for dynamic column expansion. Returns time logs as
	 * separate date entries for dynamic column creation.
	 * @param timesheetIds List of timesheet IDs to fetch time logs for
	 * @param accountId Account ID for security filtering
	 * @return Map of timesheet ID to Map of date columns (e.g., "Thursday, 10 Jul 2025"
	 * -> "8.00 hours")
	 */
	Map<Integer, Map<String, String>> getStructuredTimeLogsForTimesheets(List<Integer> timesheetIds, Integer accountId);

	/**
	 * Batch fetch structured overtime hours data for dynamic column expansion. Returns
	 * overtime hours as separate date entries for dynamic column creation.
	 * @param timesheetIds List of timesheet IDs to fetch overtime hours for
	 * @param accountId Account ID for security filtering
	 * @return Map of timesheet ID to Map of date columns (e.g., "Thursday, 10 Jul 2025"
	 * -> "2.00 hours")
	 */
	Map<Integer, Map<String, String>> getStructuredOvertimeHoursForTimesheets(List<Integer> timesheetIds,
			Integer accountId);

	/**
	 * Batch fetch structured total time data for dynamic column expansion. Returns total
	 * time as separate date entries for dynamic column creation.
	 * @param timesheetIds List of timesheet IDs to fetch total time for
	 * @param accountId Account ID for security filtering
	 * @return Map of timesheet ID to Map of date columns (e.g., "Thursday, 10 Jul 2025"
	 * -> "10.00")
	 */
	Map<Integer, Map<String, String>> getStructuredTotalTimeForTimesheets(List<Integer> timesheetIds,
			Integer accountId);

	/**
	 * Batch fetch structured effective work hours data for dynamic column expansion.
	 * Returns total_time as decimal hours for dynamic column creation.
	 * @param timesheetIds List of timesheet IDs to fetch effective work hours for
	 * @param accountId Account ID for security filtering
	 * @return Map of timesheet ID to Map of date columns (e.g., "Thursday, 10 Jul 2025"
	 * -> "10.5")
	 */
	Map<Integer, Map<String, String>> getStructuredEffectiveWorkHoursForTimesheets(List<Integer> timesheetIds,
			Integer accountId);

	/**
	 * Batch fetch structured break intervals for dynamic column expansion. Time-interval
	 * logging: comma-separated start-end ranges (e.g. "09:00-09:15, 12:00-13:00").
	 * Hours-based logging: break hours as decimal (e.g. "0.50").
	 * @param timesheetIds List of timesheet IDs to fetch break intervals for
	 * @param accountId Account ID for security filtering
	 * @return Map of timesheet ID to Map of date columns (e.g., "Thursday, 10 Jul 2025"
	 * -> "09:00-09:15, 12:00-13:00" or "0.50")
	 */
	Map<Integer, Map<String, String>> getStructuredBreakIntervalsForTimesheets(List<Integer> timesheetIds,
			Integer accountId);

	/**
	 * Batch fetch structured remarks for dynamic column expansion. Time-interval logging:
	 * comma-separated remarks per time range. Hours-based logging: single remark.
	 * @param timesheetIds List of timesheet IDs to fetch remarks for
	 * @param accountId Account ID for security filtering
	 * @return Map of timesheet ID to Map of date columns (e.g., "Thursday, 10 Jul 2025"
	 * -> "Remark 1, Remark 2" or "Single remark")
	 */
	Map<Integer, Map<String, String>> getStructuredRemarksForTimesheets(List<Integer> timesheetIds, Integer accountId);

	/**
	 * Batch delete time logs by timesheet IDs using JOOQ bulk delete
	 * @param timesheetIds List of timesheet IDs to delete time logs for
	 */
	void deleteByTimesheetIdIn(List<Integer> timesheetIds);

	/**
	 * Batch fetch time log IDs by timesheet IDs using JOOQ
	 * @param timesheetIds List of timesheet IDs to fetch time log IDs for
	 * @return List of time log IDs
	 */
	List<Integer> findTimeLogIdsByTimesheetIdIn(List<Integer> timesheetIds);

	/**
	 * Batch upsert time logs using native SQL INSERT ... ON DUPLICATE KEY UPDATE Works
	 * for both ENTER_WORK_TIME and ENTER_START_END_TIME work log types
	 * @param values List of TimeLogUpsertDto objects containing time log data
	 * @return Number of rows affected (inserted + updated)
	 */
	int batchUpsert(List<io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogUpsertDto> values);

	/**
	 * Fetches job name, slug and assignment ID for each timesheet. Joins cst_timesheet_t
	 * → cst_timesheet_setting_t → cst_timesheet_setting_association_t → tbljob,
	 * tblassignjobcandidate.
	 * @param timesheetIds List of timesheet IDs to fetch job data for
	 * @param accountId Account ID for security filtering
	 * @return Map of timesheet ID to job name, slug and assignment ID
	 */
	Map<Integer, TimesheetJobQueryResultDto> findCompanyByTimesheetIds(List<Integer> timesheetIds, Integer accountId);

	/**
	 * Fetches time log data for migration (total_time, over_time, work_time,
	 * work_start_time, work_end_time).
	 * @param timesheetIds List of timesheet IDs to fetch time logs for
	 * @return List of time log migration DTOs
	 */
	List<TimeLogMigrationDto> findTimeLogsForMigration(List<Integer> timesheetIds);

	/**
	 * Fetch time logs for migration from cst_time_log_t. Uses offset and batch size
	 * applied to the full table ordered by id. Does not filter by migration status - the
	 * caller must check which records need migration.
	 * @param batchSize Maximum number of time logs to fetch
	 * @param offset Number of time logs to skip (for pagination)
	 * @return List of time log migration DTOs
	 */
	List<TimeLogMigrationQueryResultDto> findTimeLogsForMigration(int batchSize, int offset);

	/**
	 * Count time logs that do not yet have entries in cst_time_log_interval_t.
	 * @return Count of unmigrated time logs
	 */
	long countUnmigratedTimeLogs();

}
