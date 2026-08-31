package io.recruitcrm.microservice.timesheet.services.time_log;

import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationResponseBodyDto;

/**
 * Service interface for migrating time log data from cst_time_log_t and
 * cst_time_log_break_interval_t to cst_time_log_interval_t.
 */
public interface ITimeLogIntervalMigrationService {

	/**
	 * Migrate a batch of time logs to the cst_time_log_interval_t table. Fetches time
	 * logs that were not yet migrated, builds break interval JSON from
	 * cst_time_log_break_interval_t, and inserts into cst_time_log_interval_t.
	 * @param request Migration request with batch size and offset
	 * @return Response with migrated count and whether more records remain
	 */
	TimeLogIntervalMigrationResponseBodyDto migrateTimeLogsToIntervalTable(
			TimeLogIntervalMigrationRequestBodyDto request);

}
