package io.recruitcrm.microservice.timesheet.helpers.constants;

/**
 * Constants class for repository parameters used across the application. This class
 * contains common parameter names used in repository queries.
 */
public final class RepositoryParameterConstants {

	private RepositoryParameterConstants() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * Parameter name for account ID used in repository queries
	 */
	public static final String ACCOUNT_ID = "accountId";

	/**
	 * Parameter name for job ID used in repository queries
	 */
	public static final String JOB_ID = "jobId";

	/**
	 * Parameter name for contractor ID used in repository queries
	 */
	public static final String CONTRACTOR_ID = "contractorId";

	/**
	 * Parameter name for timesheet ID used in repository queries
	 */
	public static final String TIMESHEET_ID = "timesheetId";

	/**
	 * Parameter name for timesheet IDs (list) used in repository queries
	 */
	public static final String TIMESHEET_IDS = "timesheetIds";

	/**
	 * Parameter name for open status used in repository queries
	 */
	public static final String OPEN_STATUS = "openStatus";

	/**
	 * Parameter name for contractor IDs (list) used in repository queries
	 */
	public static final String CONTRACTOR_IDS = "contractorIds";

	/**
	 * Parameter name for dates used in repository queries
	 */
	public static final String DATES = "dates";

	/**
	 * Parameter name for user ID used in repository queries
	 */
	public static final String USER_ID = "userId";

	/**
	 * Parameter name for user type ID used in repository queries
	 */
	public static final String USER_TYPE_ID = "userTypeId";

}