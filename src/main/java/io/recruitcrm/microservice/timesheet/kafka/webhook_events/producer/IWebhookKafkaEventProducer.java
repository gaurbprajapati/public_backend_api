/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.kafka.webhook_events.producer;

import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

public interface IWebhookKafkaEventProducer {

	CompletableFuture<SendResult<String, Object>> sendMessageAsync(String key, Object message);

	CompletableFuture<SendResult<String, Object>> sendMessageAsync(Object message);

	void sendMessage(String key, Object message);

	void sendMessage(Object message);

}
