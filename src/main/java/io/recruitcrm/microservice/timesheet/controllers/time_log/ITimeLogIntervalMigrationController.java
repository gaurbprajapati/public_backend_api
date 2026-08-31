package io.recruitcrm.microservice.timesheet.controllers.time_log;

import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationRequestBodyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller interface for time log interval migration endpoint.
 */
public interface ITimeLogIntervalMigrationController {

	/**
	 * Migrate a batch of time logs from cst_time_log_t and cst_time_log_break_interval_t
	 * to cst_time_log_interval_t.
	 * @param request Migration request with batch size and offset
	 * @return Response with migrated count and whether more records remain
	 */
	ResponseEntity<?> migrateTimeLogsToIntervalTable(
			@Validated @RequestBody TimeLogIntervalMigrationRequestBodyDto request);

}
