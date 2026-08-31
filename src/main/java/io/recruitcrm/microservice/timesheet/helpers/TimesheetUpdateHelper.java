package io.recruitcrm.microservice.timesheet.helpers;

import io.recruitcrm.microservice.timesheet.dto.time_log.TimeDetailSummaryDto;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class TimesheetUpdateHelper {

	private final TimesheetRepository timesheetRepository;

	public TimesheetUpdateHelper(TimesheetRepository timesheetRepository) {
		this.timesheetRepository = timesheetRepository;
	}

	public void updateTimesheetLastModified(Integer timesheetId, Integer userId, Integer userTypeId) {
		Integer currentUNIXTimestamp = (int) Instant.now().getEpochSecond();
		this.timesheetRepository.updateTimesheetLastModified(timesheetId, userId, userTypeId, currentUNIXTimestamp);
	}

	public void updateTimesheetTimeDetails(Integer timesheetId, Integer totalTime, Integer totalWorkTime) {
		this.timesheetRepository.updateTimesheetTimeDetails(timesheetId, totalTime, totalWorkTime);
	}

	/**
	 * Combined batch update: updates last modified metadata AND time details for multiple
	 * timesheets in a single query.
	 * @param timesheetIds all timesheet IDs whose last modified fields should be updated
	 * @param userId user who made the modification
	 * @param userTypeId user type of the modifier
	 * @param timeDetails optional per-timesheet time details (may be null or empty)
	 */
	public void batchUpdateTimesheetLastModifiedWithTimeDetails(List<Integer> timesheetIds, Integer userId,
			Integer userTypeId, List<TimeDetailSummaryDto> timeDetails) {
		Integer currentUNIXTimestamp = (int) Instant.now().getEpochSecond();
		this.timesheetRepository.batchUpdateTimesheetLastModifiedWithTimeDetails(timesheetIds, userId, userTypeId,
				currentUNIXTimestamp, timeDetails);
	}

}