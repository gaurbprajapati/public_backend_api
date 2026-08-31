package io.recruitcrm.microservice.timesheet.services.sendgrid;

import java.util.Map;

/**
 * Sends emails via SendGrid dynamic templates.
 */
public interface ISendGridEmailService {

	/**
	 * @param templateId SendGrid template ID (e.g. {@code d-xxxxxxxx})
	 * @param recipientEmail recipient address
	 * @param dynamicTemplateData template substitution payload
	 * ({@code dynamic_template_data})
	 */
	void sendEmailWithTemplate(String templateId, String recipientEmail, Map<String, Object> dynamicTemplateData);

}
