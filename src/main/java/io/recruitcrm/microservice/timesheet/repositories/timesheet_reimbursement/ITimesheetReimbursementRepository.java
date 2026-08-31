package io.recruitcrm.microservice.timesheet.repositories.timesheet_reimbursement;

import io.recruitcrm.microservice.timesheet.dto.jobs.ReimbursementSubmissionReminderWindowRowDto;

import java.util.List;

public interface ITimesheetReimbursementRepository {

	Integer getReimbursementCountByTimesheetIdAndEntity(Integer timesheetId, Integer entityType, Integer entityId,
			Integer accountId);

	/**
	 * Find reimbursement and timesheet IDs where EITHER (a) a reimbursement transitioned
	 * to SUBMITTED (pending) within {@code [fromEpoch, toEpoch)} on status history
	 * {@code created_on}, matching first-time submissions ({@code prev_status} is null),
	 * resubmissions after rejection ({@code prev_status} is rejected), and reopen after
	 * approval ({@code prev_status} is approved), excluding {@code submitted → submitted}
	 * updates; OR (b) the reimbursement's client-share toggle was switched ON within
	 * {@code [fromEpoch, toEpoch)} on client-share history {@code created_on}. Current
	 * submitted state is verified via {@code cst_timesheet_reimbursement_t.status} for
	 * both cases.
	 * @param fromEpoch Window start, inclusive (UNIX seconds)
	 * @param toEpoch Window end, exclusive (UNIX seconds)
	 * @param submittedStatusId The status ID representing SUBMITTED status
	 * @param rejectedStatusId The status ID representing REJECTED status
	 * @param approvedStatusId The status ID representing APPROVED status
	 * @return Matching reimbursement/timesheet ID pairs
	 */
	List<ReimbursementSubmissionReminderWindowRowDto> findReimbursementsWhereTransitionedToSubmittedInWindow(
			long fromEpoch, long toEpoch, int submittedStatusId, int rejectedStatusId, int approvedStatusId);

}
