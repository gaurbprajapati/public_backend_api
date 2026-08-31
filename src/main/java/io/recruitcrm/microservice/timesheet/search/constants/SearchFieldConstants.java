package io.recruitcrm.microservice.timesheet.search.constants;

/**
 * Constants for search field names and group types used in filter node creation. This
 * class centralizes all string literals to avoid magic strings and improve
 * maintainability.
 */
public final class SearchFieldConstants {

	private SearchFieldConstants() {
		throw new UnsupportedOperationException("Utility class");
	}

	// ===== Group Types =====
	/**
	 * Group type constant for contractor filters.
	 */
	public static final String GROUP_TYPE_CONTRACTORS = "contractors";

	// ===== Database Field Names =====
	/**
	 * Status field name constant.
	 */
	public static final String FIELD_STATUS = "status";

	/**
	 * Added on field name constant.
	 */
	public static final String FIELD_ADDED_ON = "added_on";

	/**
	 * Timesheet period field name constant.
	 */
	public static final String FIELD_TIMESHEET_PERIOD = "timesheetPeriod";

	/**
	 * Company name field name constant.
	 */
	public static final String FIELD_COMPANY_NAME = "companyName";

	// ===== Deal Field Names =====
	/**
	 * Deal name field name constant.
	 */
	public static final String FIELD_DEAL_NAME = "dealName";

	/**
	 * Associated deal field name constant.
	 */
	public static final String FIELD_ASSOCIATED_DEAL = "associatedDeal";

	/**
	 * Deal field name constant.
	 */
	public static final String FIELD_DEAL = "deal";

	// ===== Job Field Names =====
	/**
	 * Job name field name constant (snake_case).
	 */
	public static final String FIELD_JOB_NAME_SNAKE = "job_name";

	/**
	 * Job name field name constant (camelCase).
	 */
	public static final String FIELD_JOB_NAME_CAMEL = "jobName";

	/**
	 * Job field name constant.
	 */
	public static final String FIELD_JOB = "job";

	// ===== Timesheet Status Field Names =====
	/**
	 * Timesheet status field name constant (snake_case).
	 */
	public static final String FIELD_TIMESHEET_STATUS_SNAKE = "timesheet_status";

	/**
	 * Timesheet status field name constant (camelCase).
	 */
	public static final String FIELD_TIMESHEET_STATUS_CAMEL = "timesheetStatus";

	/**
	 * Timesheet status ID field name constant (snake_case).
	 */
	public static final String FIELD_TIMESHEET_STATUS_ID_SNAKE = "timesheet_status_id";

	/**
	 * Timesheet status ID field name constant (camelCase).
	 */
	public static final String FIELD_TIMESHEET_STATUS_ID_CAMEL = "timesheetStatusId";

}
