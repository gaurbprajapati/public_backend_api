package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;

import java.util.Arrays;
import java.util.List;

/**
 * Test data factory for {@link TimesheetApprovalRepository} tests.
 */
public final class TimesheetApprovalRepositoryTestDataFactory {

	public static final Integer DEFAULT_TIMESHEET_ID = 101;

	public static final Integer DEFAULT_USER_ID = 202;

	public static final Integer DEFAULT_USER_TYPE_ID = 1;

	public static final Integer DEFAULT_APPROVAL_STATUS_TYPE_ID = 3;

	public static final String DEFAULT_REMARK = "Approved from test";

	private TimesheetApprovalRepositoryTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static List<Integer> createTimesheetIds() {
		return Arrays.asList(101, 102, 103);
	}

	public static TimesheetApproval createTimesheetApproval(Integer id, Integer timesheetId, Integer statusTypeId) {
		TimesheetApproval approval = new TimesheetApproval();
		approval.setId(id);
		approval.setTimesheetId(timesheetId);
		approval.setEntityId(DEFAULT_USER_ID);
		approval.setUserTypeId(DEFAULT_USER_TYPE_ID);
		approval.setTimesheetApprovalStatusTypeId(statusTypeId);
		approval.setCreatedOn(1715000000);
		approval.setRemark(DEFAULT_REMARK);
		return approval;
	}

	public static List<TimesheetApproval> createTimesheetApprovalList() {
		return Arrays.asList(createTimesheetApproval(1, 101, 1), createTimesheetApproval(2, 102, 2));
	}

	public static int getDefaultFromEpoch() {
		return 1_710_000_000;
	}

	public static int getDefaultToEpoch() {
		return 1_710_086_400;
	}

	public static int getSubmittedStatusId() {
		return ApprovalStatusEnum.SUBMITTED.getId();
	}

	public static int getOpenStatusId() {
		return ApprovalStatusEnum.OPEN.getId();
	}

	public static int getRejectedStatusId() {
		return ApprovalStatusEnum.REJECTED.getId();
	}

}
