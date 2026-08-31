package io.recruitcrm.microservice.timesheet.services.sendgrid;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.exceptions.ExternalServiceException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.constants.SendGridSettingsConstants;
import io.recruitcrm.microservice.timesheet.repositories.settings.ISettingsRepository;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Albatross equivalent of {@code SendGridApi::send_email_using_sendgrid_template_id}.
 */
@Service
public class SendGridEmailService implements ISendGridEmailService {

	private static final String SERVICE_NAME = "SendGrid";

	private static final int ACCEPTED_STATUS = 202;

	private static final int OK_STATUS = 200;

	private final ISettingsRepository settingsRepository;

	private final Logger logger;

	public SendGridEmailService(ISettingsRepository settingsRepository,
			@Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) Logger logger) {
		this.settingsRepository = settingsRepository;
		this.logger = logger;
	}

	@Override
	public void sendEmailWithTemplate(String templateId, String recipientEmail,
			Map<String, Object> dynamicTemplateData) {
		if (!StringUtils.hasText(templateId)) {
			throw new ValidationErrorException("SendGrid template ID must not be blank");
		}
		if (!StringUtils.hasText(recipientEmail)) {
			throw new ValidationErrorException("Recipient email must not be blank");
		}

		String sendgridApiKey = this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDGRID_KEY);
		if (!StringUtils.hasText(sendgridApiKey)) {
			throw new ValidationErrorException(
					"No API key found to send email. Please update API key to activate email sending.");
		}

		String senderEmail = this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDER_EMAIL);
		if (!StringUtils.hasText(senderEmail)) {
			throw new ValidationErrorException("Sender email is not configured in system settings.");
		}

		Map<String, Object> templatePayload = (dynamicTemplateData != null) ? dynamicTemplateData
				: Collections.emptyMap();
		Mail mail = this.buildTemplateMail(templateId, recipientEmail, senderEmail, templatePayload);

		try {
			SendGrid sendGrid = new SendGrid(sendgridApiKey);
			Request request = new Request();
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sendGrid.api(request);
			int statusCode = response.getStatusCode();
			this.logger.logInfo(MessageFormat.format("SendGrid template email to {0}, template {1}, status {2}",
					recipientEmail, templateId, statusCode));
			this.ensureSuccessfulStatus(statusCode, response.getBody());
		}
		catch (IOException exception) {
			throw new ExternalServiceException(SERVICE_NAME, "send template email", exception.getMessage(), exception);
		}
	}

	private Mail buildTemplateMail(String templateId, String recipientEmail, String senderEmail,
			Map<String, Object> dynamicTemplateData) {
		Email from = new Email(senderEmail, SendGridSettingsConstants.SENDER_DISPLAY_NAME);
		Mail mail = new Mail();
		mail.setFrom(from);
		mail.setTemplateId(templateId);
		mail.setReplyTo(new Email(senderEmail, SendGridSettingsConstants.SENDER_DISPLAY_NAME));

		Personalization personalization = new Personalization();
		personalization.addTo(new Email(recipientEmail));
		for (Map.Entry<String, Object> entry : dynamicTemplateData.entrySet()) {
			String key = entry.getKey();
			if (key != null) {
				personalization.addDynamicTemplateData(key, entry.getValue());
			}
		}
		mail.addPersonalization(personalization);
		return mail;
	}

	private void ensureSuccessfulStatus(int statusCode, String responseBody) {
		if ((statusCode == ACCEPTED_STATUS) || (statusCode == OK_STATUS)) {
			return;
		}
		String details = Objects.toString(responseBody, "no response body");
		throw new ExternalServiceException(SERVICE_NAME, "send template email",
				MessageFormat.format("unexpected status {0}: {1}", statusCode, details));
	}

}
