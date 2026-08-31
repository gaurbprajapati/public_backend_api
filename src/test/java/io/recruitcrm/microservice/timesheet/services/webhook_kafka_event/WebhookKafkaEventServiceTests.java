package io.recruitcrm.microservice.timesheet.services.webhook_kafka_event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import io.recruitcrm.entity.model.WebhookSubscription;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.kafka.webhook_events.model.TimesheetApprovedWebhookEvent;
import io.recruitcrm.microservice.timesheet.kafka.webhook_events.producer.WebhookKafkaEventProducer;
import io.recruitcrm.microservice.timesheet.repositories.WebhookSubscriptionRepository;
import io.recruitcrm.logging.logger.Logger;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebhookKafkaEventServiceTests {

	@Mock
	private AuthHolder auth;

	@Mock
	private Validator validator;

	@Mock
	private WebhookKafkaEventProducer webhookKafkaEventProducer;

	@Mock
	private WebhookSubscriptionRepository webhookSubscriptionRepository;

	@Mock
	private Logger logger;

	@InjectMocks
	private WebhookKafkaEventService webhookKafkaEventService;

	@Test
	@DisplayName("triggerTimesheetWebhookEvent should skip when account id is null")
	void testTriggerTimesheetWebhookEventNullAccountIdSkipsAndLogs() {
		// Given
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(null);

		// When
		this.webhookKafkaEventService.triggerTimesheetWebhookEvent(WebhookEvent.TIMESHEET_APPROVED, List.of(1));

		// Then
		then(this.logger).should()
			.logInfo("Skipping time sheet webhook: account ID is null (auth context may be missing)");
		then(this.webhookSubscriptionRepository).should(never()).findByAccountIdAndEvent(any(), any());
		then(this.webhookKafkaEventProducer).should(never()).sendMessage(any());
	}

	@Test
	@DisplayName("triggerTimesheetWebhookEvent should skip when entity ids are null")
	void testTriggerTimesheetWebhookEventNullEntityIdsSkipsAndLogs() {
		// Given
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(10);

		// When
		this.webhookKafkaEventService.triggerTimesheetWebhookEvent(WebhookEvent.TIMESHEET_APPROVED, null);

		// Then
		then(this.logger).should().logInfo("Skipping time sheet webhook: entity IDs are null or empty");
		then(this.webhookSubscriptionRepository).should(never()).findByAccountIdAndEvent(any(), any());
		then(this.webhookKafkaEventProducer).should(never()).sendMessage(any());
	}

	@Test
	@DisplayName("triggerTimesheetWebhookEvent should skip when entity ids are empty")
	void testTriggerTimesheetWebhookEventEmptyEntityIdsSkipsAndLogs() {
		// Given
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(10);

		// When
		this.webhookKafkaEventService.triggerTimesheetWebhookEvent(WebhookEvent.TIMESHEET_APPROVED,
				Collections.emptyList());

		// Then
		then(this.logger).should().logInfo("Skipping time sheet webhook: entity IDs are null or empty");
		then(this.webhookSubscriptionRepository).should(never()).findByAccountIdAndEvent(any(), any());
		then(this.webhookKafkaEventProducer).should(never()).sendMessage(any());
	}

	@Test
	@DisplayName("triggerTimesheetWebhookEvent should no-op when no subscription exists")
	void testTriggerTimesheetWebhookEventNoSubscriptionNoOps() {
		// Given
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(10);
		given(this.webhookSubscriptionRepository.findByAccountIdAndEvent(10, "timesheet.approved"))
			.willReturn(Optional.empty());

		// When
		this.webhookKafkaEventService.triggerTimesheetWebhookEvent(WebhookEvent.TIMESHEET_APPROVED, List.of(1));

		// Then
		then(this.webhookSubscriptionRepository).should().findByAccountIdAndEvent(10, "timesheet.approved");
		then(this.webhookKafkaEventProducer).should(never()).sendMessage(any());
		then(this.logger).should(never()).logInfo(anyString());
	}

	@Test
	@DisplayName("triggerTimesheetWebhookEvent should send when subscription exists for timesheet approved")
	void testTriggerTimesheetWebhookEventSubscriptionExistsSendsKafkaMessage() {
		// Given
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(10);
		given(this.webhookSubscriptionRepository.findByAccountIdAndEvent(10, "timesheet.approved"))
			.willReturn(Optional.of(mock(WebhookSubscription.class)));
		given(this.validator.validate(any(TimesheetApprovedWebhookEvent.class))).willReturn(Collections.emptySet());
		willDoNothing().given(this.webhookKafkaEventProducer).sendMessage(any());

		// When
		this.webhookKafkaEventService.triggerTimesheetWebhookEvent(WebhookEvent.TIMESHEET_APPROVED, List.of(101, 102));

		// Then
		then(this.webhookKafkaEventProducer).should().sendMessage(any(TimesheetApprovedWebhookEvent.class));
		then(this.validator).should().validate(any(TimesheetApprovedWebhookEvent.class));
	}

	@Test
	@DisplayName("triggerTimesheetWebhookEvent should throw when validation fails")
	void testTriggerTimesheetWebhookEventValidationFailsThrowsIllegalArgumentException() {
		// Given
		@SuppressWarnings("unchecked")
		ConstraintViolation<TimesheetApprovedWebhookEvent> violation = mock(ConstraintViolation.class);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(10);
		given(this.webhookSubscriptionRepository.findByAccountIdAndEvent(10, "timesheet.approved"))
			.willReturn(Optional.of(mock(WebhookSubscription.class)));
		given(this.validator.validate(any(TimesheetApprovedWebhookEvent.class))).willReturn(Set.of(violation));

		// When & Then
		WebhookEvent event = WebhookEvent.TIMESHEET_APPROVED;
		List<Integer> entityIds = List.of(101);
		assertThatThrownBy(() -> this.webhookKafkaEventService.triggerTimesheetWebhookEvent(event, entityIds))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Invalid TimeSheetApprovedWebhookEvent");
		then(this.webhookKafkaEventProducer).should(never()).sendMessage(any());
	}

	@Test
	@DisplayName("triggerTimesheetWebhookEvent should throw when event type is not implemented")
	void testTriggerTimesheetWebhookEventUnsupportedEventTypeThrowsIllegalArgumentException() {
		// Given
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(10);
		given(this.webhookSubscriptionRepository.findByAccountIdAndEvent(10, "timesheet.submitted"))
			.willReturn(Optional.of(mock(WebhookSubscription.class)));

		// When & Then
		WebhookEvent event = WebhookEvent.TIMESHEET_SUBMITTED;
		List<Integer> entityIds = List.of(1);
		assertThatThrownBy(() -> this.webhookKafkaEventService.triggerTimesheetWebhookEvent(event, entityIds))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Unsupported time sheet webhook event");
		then(this.webhookKafkaEventProducer).should(never()).sendMessage(any());
		then(this.validator).should(never()).validate(any(TimesheetApprovedWebhookEvent.class));
	}

}
