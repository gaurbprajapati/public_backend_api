package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess;

/**
 * Test data factory for {@link JobTimesheetAccess} persistence tests.
 */
public final class JobTimesheetAccessTestDataFactory {

	private JobTimesheetAccessTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static final Integer DEFAULT_CAN_CREATE = Integer.valueOf(1);

	public static final Integer DEFAULT_CAN_EDIT = Integer.valueOf(1);

	public static final Integer DEFAULT_CAN_DELETE = Integer.valueOf(0);

	public static final Integer CAN_CREATE_DISABLED = Integer.valueOf(0);

	/**
	 * Creates a new {@link JobTimesheetAccess} with the given job and account ids and
	 * default permission flags.
	 */
	public static JobTimesheetAccess createJobTimesheetAccess(Integer jobId, Integer accountId) {
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setJobId(jobId);
		access.setAccountId(accountId);
		access.setCanCreate(JobTimesheetAccessTestDataFactory.DEFAULT_CAN_CREATE);
		access.setCanEdit(JobTimesheetAccessTestDataFactory.DEFAULT_CAN_EDIT);
		access.setCanDelete(JobTimesheetAccessTestDataFactory.DEFAULT_CAN_DELETE);
		return access;
	}

	/**
	 * Creates a {@link JobTimesheetAccess} with explicit permission flags.
	 */
	public static JobTimesheetAccess createJobTimesheetAccess(Integer jobId, Integer accountId, Integer canCreate,
			Integer canEdit, Integer canDelete) {
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setJobId(jobId);
		access.setAccountId(accountId);
		access.setCanCreate(canCreate);
		access.setCanEdit(canEdit);
		access.setCanDelete(canDelete);
		return access;
	}

}
