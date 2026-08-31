package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUpdateTimeLogsRequestBodyDto {

	private Boolean isApproved;

	private Integer jobId;

	private List<BulkTimeLogRequestBodyDto> timeLogs;

	private List<TimeDetailSummaryDto> timeDetails;

	private Boolean save = false;

	private List<TimesheetTimeDetailDto> totalTimeDetail;

	/**
	 * Optional. Timesheet IDs where no logs were changed but metadata (updated_on,
	 * updated_by, status history) should be updated. Used when user submits/saves without
	 * editing any time logs - e.g. single timesheet resubmit or bulk submit where some
	 * timesheets had no changes.
	 */
	private List<Integer> timesheetIdNoLogChanges;

}