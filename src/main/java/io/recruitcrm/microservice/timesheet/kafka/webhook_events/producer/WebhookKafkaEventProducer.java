/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.kafka.webhook_events.producer;

import io.recruitcrm.microservice.timesheet.kafka.webhook_events.configuration.WebhookKafkaEventProducerConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class WebhookKafkaEventProducer implements IWebhookKafkaEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	private final String topicName;

	public WebhookKafkaEventProducer(
			@Qualifier(WebhookKafkaEventProducerConfiguration.KAFKA_TEMPLATE_BEAN_NAME) KafkaTemplate<String, Object> kafkaTemplate,
			WebhookKafkaEventProducerConfiguration webhookKafkaEventProducerConfiguration) {
		this.kafkaTemplate = kafkaTemplate;
		this.topicName = webhookKafkaEventProducerConfiguration.getTopicName();
	}

	@Override
	public CompletableFuture<SendResult<String, Object>> sendMessageAsync(String key, Object message) {
		return this.kafkaTemplate.send(this.topicName, key, message);
	}

	@Override
	public CompletableFuture<SendResult<String, Object>> sendMessageAsync(Object message) {
		return this.kafkaTemplate.send(this.topicName, message);
	}

	@Override
	public void sendMessage(String key, Object message) {
		this.kafkaTemplate.send(this.topicName, key, message);
	}

	@Override
	public void sendMessage(Object message) {
		this.kafkaTemplate.send(this.topicName, message);
	}

}
