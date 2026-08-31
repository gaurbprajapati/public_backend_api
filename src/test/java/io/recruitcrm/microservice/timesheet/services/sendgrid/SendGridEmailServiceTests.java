package io.recruitcrm.microservice.timesheet.services.sendgrid;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockConstruction;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.exceptions.ExternalServiceException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.constants.SendGridSettingsConstants;
import io.recruitcrm.microservice.timesheet.repositories.settings.ISettingsRepository;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendGridEmailServiceTests {

	private static final String TEMPLATE_ID = "d-test-template";

	private static final String RECIPIENT = "user@example.com";

	private static final String API_KEY = "SG.test-key";

	private static final String SENDER = "noreply@recruitcrm.com";

	@Mock
	private ISettingsRepository settingsRepository;

	@Mock
	private Logger logger;

	private SendGridEmailService sendGridEmailService;

	@BeforeEach
	void setUp() {
		this.sendGridEmailService = new SendGridEmailService(this.settingsRepository, this.logger);
	}

	@Test
	@DisplayName("sendEmailWithTemplate should send when settings and SendGrid response are valid")
	void testSendEmailWithTemplateSuccess() {
		// Given
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDGRID_KEY)).willReturn(API_KEY);
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDER_EMAIL)).willReturn(SENDER);

		try (var mockedSendGrid = mockConstruction(SendGrid.class, (mock,
				context) -> given(mock.api(any(Request.class))).willReturn(new Response(202, "accepted", Map.of())))) {
			// When / Then
			assertThatCode(() -> this.sendGridEmailService.sendEmailWithTemplate(TEMPLATE_ID, RECIPIENT,
					Map.of("name", "Alex")))
				.doesNotThrowAnyException();
		}
	}

	@Test
	@DisplayName("sendEmailWithTemplate should throw when template id is blank")
	void testSendEmailWithTemplateBlankTemplateIdThrows() {
		assertThatThrownBy(() -> this.sendGridEmailService.sendEmailWithTemplate("  ", RECIPIENT, Map.of()))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("template ID");
	}

	@Test
	@DisplayName("sendEmailWithTemplate should throw when recipient email is blank")
	void testSendEmailWithTemplateBlankRecipientThrows() {
		assertThatThrownBy(() -> this.sendGridEmailService.sendEmailWithTemplate(TEMPLATE_ID, "", Map.of()))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Recipient email");
	}

	@Test
	@DisplayName("sendEmailWithTemplate should throw when SendGrid API key is missing in database")
	void testSendEmailWithTemplateMissingApiKeyThrows() {
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDGRID_KEY)).willReturn("");

		assertThatThrownBy(() -> this.sendGridEmailService.sendEmailWithTemplate(TEMPLATE_ID, RECIPIENT, Map.of()))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("API key");
	}

	@Test
	@DisplayName("sendEmailWithTemplate should throw when sender email is missing in database")
	void testSendEmailWithTemplateMissingSenderThrows() {
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDGRID_KEY)).willReturn(API_KEY);
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDER_EMAIL)).willReturn("");

		assertThatThrownBy(() -> this.sendGridEmailService.sendEmailWithTemplate(TEMPLATE_ID, RECIPIENT, Map.of()))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Sender email");
	}

	@Test
	@DisplayName("sendEmailWithTemplate should throw ExternalServiceException when SendGrid returns error status")
	void testSendEmailWithTemplateErrorStatusThrows() {
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDGRID_KEY)).willReturn(API_KEY);
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDER_EMAIL)).willReturn(SENDER);

		try (var mockedSendGrid = mockConstruction(SendGrid.class,
				(mock, context) -> given(mock.api(any(Request.class)))
					.willReturn(new Response(401, "unauthorized", Map.of())))) {
			assertThatThrownBy(() -> this.sendGridEmailService.sendEmailWithTemplate(TEMPLATE_ID, RECIPIENT, Map.of()))
				.isInstanceOf(ExternalServiceException.class)
				.hasMessageContaining("SendGrid");
		}
	}

	@Test
	@DisplayName("sendEmailWithTemplate should wrap IOException from SendGrid client")
	void testSendEmailWithTemplateIOExceptionThrows() {
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDGRID_KEY)).willReturn(API_KEY);
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDER_EMAIL)).willReturn(SENDER);

		try (var mockedSendGrid = mockConstruction(SendGrid.class,
				(mock, context) -> given(mock.api(any(Request.class))).willThrow(new IOException("network failure")))) {
			assertThatThrownBy(() -> this.sendGridEmailService.sendEmailWithTemplate(TEMPLATE_ID, RECIPIENT, Map.of()))
				.isInstanceOf(ExternalServiceException.class)
				.hasMessageContaining("network failure");
		}
	}

	@Test
	@DisplayName("sendEmailWithTemplate should succeed when SendGrid returns OK status")
	void testSendEmailWithTemplateOkStatusSucceeds() {
		// Given
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDGRID_KEY)).willReturn(API_KEY);
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDER_EMAIL)).willReturn(SENDER);

		try (var mockedSendGrid = mockConstruction(SendGrid.class,
				(mock, context) -> given(mock.api(any(Request.class))).willReturn(new Response(200, "ok", Map.of())))) {
			// When / Then
			assertThatCode(() -> this.sendGridEmailService.sendEmailWithTemplate(TEMPLATE_ID, RECIPIENT,
					Map.of("name", "Alex")))
				.doesNotThrowAnyException();
		}
	}

	@Test
	@DisplayName("sendEmailWithTemplate should skip dynamic template entries with null keys")
	void testSendEmailWithTemplateNullKeyEntrySkipped() {
		// Given
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDGRID_KEY)).willReturn(API_KEY);
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDER_EMAIL)).willReturn(SENDER);

		Map<String, Object> payloadWithNullKey = new HashMap<>();
		payloadWithNullKey.put(null, "ignored");
		payloadWithNullKey.put("name", "Alex");

		try (var mockedSendGrid = mockConstruction(SendGrid.class, (mock,
				context) -> given(mock.api(any(Request.class))).willReturn(new Response(202, "accepted", Map.of())))) {
			// When / Then
			assertThatCode(
					() -> this.sendGridEmailService.sendEmailWithTemplate(TEMPLATE_ID, RECIPIENT, payloadWithNullKey))
				.doesNotThrowAnyException();
		}
	}

	@Test
	@DisplayName("sendEmailWithTemplate should accept null payload as empty dynamic data")
	void testSendEmailWithTemplateNullPayloadSucceeds() {
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDGRID_KEY)).willReturn(API_KEY);
		given(this.settingsRepository.getValueByKey(SendGridSettingsConstants.SENDER_EMAIL)).willReturn(SENDER);

		try (var mockedSendGrid = mockConstruction(SendGrid.class, (mock,
				context) -> given(mock.api(any(Request.class))).willReturn(new Response(202, "accepted", Map.of())))) {
			assertThatCode(() -> this.sendGridEmailService.sendEmailWithTemplate(TEMPLATE_ID, RECIPIENT, null))
				.doesNotThrowAnyException();
		}
	}

}
