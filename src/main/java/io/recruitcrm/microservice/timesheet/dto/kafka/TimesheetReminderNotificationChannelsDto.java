package io.recruitcrm.microservice.timesheet.dto.kafka;

/**
 * Delivery channel flags for {@link TimesheetReminderNotificationPayloadDto}.
 */
public record TimesheetReminderNotificationChannelsDto(boolean sendInappNotification, boolean sendEmailNotification,
		boolean sendPortalNotification) {

	public static final TimesheetReminderNotificationChannelsDto APPROVED = new TimesheetReminderNotificationChannelsDto(
			false, true, true);

	public static final TimesheetReminderNotificationChannelsDto REIMBURSEMENT_STATUS = new TimesheetReminderNotificationChannelsDto(
			true, true, true);

	public static final TimesheetReminderNotificationChannelsDto SUBMITTED = new TimesheetReminderNotificationChannelsDto(
			true, false, true);

	public static final TimesheetReminderNotificationChannelsDto SHARED_WITH_CLIENT_UPDATED = new TimesheetReminderNotificationChannelsDto(
			false, false, true);

}
