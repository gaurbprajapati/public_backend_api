package io.recruitcrm.microservice.timesheet.kafka.webhook_events.producer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import io.recruitcrm.microservice.timesheet.kafka.webhook_events.configuration.WebhookKafkaEventProducerConfiguration;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class WebhookKafkaEventProducerTests {

	private static final String TOPIC = "webhook-events-topic";

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Mock
	private WebhookKafkaEventProducerConfiguration webhookKafkaEventProducerConfiguration;

	private WebhookKafkaEventProducer webhookKafkaEventProducer;

	@BeforeEach
	void setUp() {
		given(this.webhookKafkaEventProducerConfiguration.getTopicName()).willReturn(TOPIC);
		this.webhookKafkaEventProducer = new WebhookKafkaEventProducer(this.kafkaTemplate,
				this.webhookKafkaEventProducerConfiguration);
	}

	@Test
	@DisplayName("sendMessageAsync with key should send to configured topic and return future")
	void testSendMessageAsyncWithKeyValidInputsSendsToTopicAndReturnsFuture() {
		// Given
		String key = "evt-key";
		Object payload = "payload";
		SendResult<String, Object> sendResult = mock(SendResult.class);
		given(this.kafkaTemplate.send(TOPIC, key, payload)).willReturn(CompletableFuture.completedFuture(sendResult));

		// When
		CompletableFuture<SendResult<String, Object>> future = this.webhookKafkaEventProducer.sendMessageAsync(key,
				payload);

		// Then
		assertThat(future).isCompletedWithValue(sendResult);
		then(this.kafkaTemplate).should().send(TOPIC, key, payload);
	}

	@Test
	@DisplayName("sendMessageAsync without key should send to configured topic and return future")
	void testSendMessageAsyncWithoutKeyValidPayloadSendsToTopicAndReturnsFuture() {
		// Given
		Object payload = "payload-only";
		SendResult<String, Object> sendResult = mock(SendResult.class);
		given(this.kafkaTemplate.send(TOPIC, payload)).willReturn(CompletableFuture.completedFuture(sendResult));

		// When
		CompletableFuture<SendResult<String, Object>> future = this.webhookKafkaEventProducer.sendMessageAsync(payload);

		// Then
		assertThat(future).isCompletedWithValue(sendResult);
		then(this.kafkaTemplate).should().send(TOPIC, payload);
	}

	@Test
	@DisplayName("sendMessage with key should invoke kafka template send")
	void testSendMessageWithKeyValidInputsInvokesKafkaTemplateSend() {
		// Given
		String key = "k";
		Object payload = "p";
		given(this.kafkaTemplate.send(TOPIC, key, payload))
			.willReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

		// When
		this.webhookKafkaEventProducer.sendMessage(key, payload);

		// Then
		then(this.kafkaTemplate).should().send(TOPIC, key, payload);
	}

	@Test
	@DisplayName("sendMessage without key should invoke kafka template send")
	void testSendMessageWithoutKeyValidPayloadInvokesKafkaTemplateSend() {
		// Given
		Object payload = "p";
		given(this.kafkaTemplate.send(TOPIC, payload))
			.willReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

		// When
		this.webhookKafkaEventProducer.sendMessage(payload);

		// Then
		then(this.kafkaTemplate).should().send(TOPIC, payload);
	}

}
