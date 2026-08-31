package io.recruitcrm.microservice.timesheet.helpers.constants;

/**
 * Keys in {@code tblsasettings}, aligned with Albatross {@code SendGridApi}.
 */
public final class SendGridSettingsConstants {

	public static final String SENDGRID_KEY = "Sendgrid Key";

	public static final String SENDER_EMAIL = "Sender Email";

	public static final String SENDER_DISPLAY_NAME = "Recruit CRM";

	/**
	 * Dummy keys in {@code tblsasettings}; replace with production template IDs when
	 * available.
	 */
	public static final String CLIENT_PORTAL_INVITE_TEMPLATE_ID = "invite_client_portal_email_template_id";

	public static final String CLIENT_PORTAL_DISABLE_TEMPLATE_ID = "disable_client_portal_email_template_id";

	public static final String CLIENT_PORTAL_REENABLE_TEMPLATE_ID = "enable_client_portal_email_template_id";

	private SendGridSettingsConstants() {
	}

}
