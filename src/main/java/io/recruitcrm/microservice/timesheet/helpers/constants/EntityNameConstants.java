package io.recruitcrm.microservice.timesheet.helpers.constants;

/**
 * Constants for entity names used in ResourceNotFoundException and similar contexts.
 */
public final class EntityNameConstants {

	private EntityNameConstants() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * Entity name for Timesheet - used in ResourceNotFoundException
	 */
	public static final String TIMESHEET = "Timesheet";

	/**
	 * Entity name for TimesheetSetting - used in ResourceNotFoundException
	 */
	public static final String TIMESHEET_SETTING = "TimesheetSetting";

	/**
	 * Entity name for TimesheetReimbursement - used in ResourceNotFoundException
	 */
	public static final String REIMBURSEMENT = "TimesheetReimbursement";

}
