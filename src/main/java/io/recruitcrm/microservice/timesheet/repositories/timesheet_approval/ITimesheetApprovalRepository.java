package io.recruitcrm.microservice.timesheet.repositories.timesheet_approval;

import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;

import java.util.List;

public interface ITimesheetApprovalRepository {

	void createTimesheetApproval(Integer timesheetId, Integer userId, Integer userTypeId, Integer approvalStatusTypeId,
			String remark);

	void createBulkTimesheetApprovals(List<TimesheetApproval> timesheetApprovals);

	/**
	 * Batch delete timesheet approvals by timesheet IDs using JOOQ bulk delete
	 * @param timesheetIds List of timesheet IDs to delete approvals for
	 */
	void deleteByTimesheetIdIn(List<Integer> timesheetIds);

	/**
	 * Bulk fetch latest approval entities for multiple timesheets using JPQL
	 * @param timesheetIds List of timesheet IDs to fetch latest approvals for
	 * @return List of latest TimesheetApproval entities for each timesheet ID
	 */
	List<TimesheetApproval> findLatestApprovalEntitiesByTimesheetIds(List<Integer> timesheetIds);

	/**
	 * Find timesheet IDs where a SUBMITTED approval row exists within
	 * {@code [fromEpoch, toEpoch)}, the immediately preceding approval row (by
	 * {@code id}) had status OPEN or REJECTED, and the timesheet's latest approval
	 * (globally, by max {@code id}) is still SUBMITTED. This matches both first-time
	 * submissions ({@code open → submitted}) and resubmissions
	 * ({@code rejected → submitted}) that occurred within the window and have not since
	 * moved to another status.
	 * @param fromEpoch Window start, inclusive (UNIX seconds)
	 * @param toEpoch Window end, exclusive (UNIX seconds)
	 * @param submittedStatusId The status ID representing SUBMITTED status
	 * @param openStatusId The status ID representing OPEN status
	 * @param rejectedStatusId The status ID representing REJECTED status
	 * @return Timesheet IDs matching the criteria
	 */
	List<Integer> findTimesheetIdsWhereTransitionedToSubmittedInWindow(long fromEpoch, long toEpoch,
			int submittedStatusId, int openStatusId, int rejectedStatusId);

}
