/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.webhook_kafka_event;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.kafka.webhook_events.model.TimesheetApprovedWebhookEvent;
import io.recruitcrm.microservice.timesheet.kafka.webhook_events.producer.WebhookKafkaEventProducer;
import io.recruitcrm.microservice.timesheet.repositories.WebhookSubscriptionRepository;
import io.recruitcrm.entity.model.WebhookSubscription;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;

@Service
public class WebhookKafkaEventService {

	private final AuthHolder auth;

	private final Validator validator;

	private final WebhookKafkaEventProducer webhookKafkaEventProducer;

	private final WebhookSubscriptionRepository webhookSubscriptionRepository;

	private final Logger logger;

	public WebhookKafkaEventService(AuthHolder auth, Validator validator,
			WebhookKafkaEventProducer webhookKafkaEventProducer,
			WebhookSubscriptionRepository webhookSubscriptionRepository,
			@Qualifier(LoggerConfiguration.ASYNC_CONTEXT_LOGGER) Logger logger) {
		this.auth = auth;
		this.validator = validator;
		this.webhookKafkaEventProducer = webhookKafkaEventProducer;
		this.webhookSubscriptionRepository = webhookSubscriptionRepository;
		this.logger = logger;
	}

	/**
	 * Triggers a time sheet webhook event. Builds and sends the appropriate Kafka message
	 * based on the event type. No-op if account or entity IDs are missing or no
	 * subscription exists for the event.
	 * @param eventType the webhook event type (e.g. TIMESHEET_APPROVED)
	 * @param entityIds list of entity IDs for the payload (e.g. approved timesheet IDs)
	 */
	public void triggerTimesheetWebhookEvent(WebhookEvent eventType, List<Integer> entityIds) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		if (accountId == null) {
			this.logger.logInfo("Skipping time sheet webhook: account ID is null (auth context may be missing)");
			return;
		}
		if (entityIds == null || entityIds.isEmpty()) {
			this.logger.logInfo("Skipping time sheet webhook: entity IDs are null or empty");
			return;
		}
		Optional<WebhookSubscription> subscription = this.webhookSubscriptionRepository
			.findByAccountIdAndEvent(accountId, eventType.getEventName());
		if (subscription.isEmpty()) {
			return;
		}
		if (eventType == WebhookEvent.TIMESHEET_APPROVED) {
			this.sendTimesheetApprovedEvent(accountId, entityIds);
		}
		else {
			throw new IllegalArgumentException("Unsupported time sheet webhook event: " + eventType);
		}
	}

	private void sendTimesheetApprovedEvent(Integer accountId, List<Integer> timesheetIds) {
		TimesheetApprovedWebhookEvent event = new TimesheetApprovedWebhookEvent();
		event.setEventName(WebhookEvent.TIMESHEET_APPROVED.getEventName());
		event.setPayload(new TimesheetApprovedWebhookEvent.Payload(accountId, timesheetIds));
		Set<ConstraintViolation<TimesheetApprovedWebhookEvent>> violations = this.validator.validate(event);
		if (!violations.isEmpty()) {
			throw new IllegalArgumentException("Invalid TimeSheetApprovedWebhookEvent: " + violations);
		}
		this.webhookKafkaEventProducer.sendMessage(event);
	}

}
