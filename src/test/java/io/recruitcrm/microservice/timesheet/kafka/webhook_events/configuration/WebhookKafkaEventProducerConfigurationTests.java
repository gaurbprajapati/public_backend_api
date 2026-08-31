package io.recruitcrm.microservice.timesheet.kafka.webhook_events.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.kafka.KafkaConfigurationHelper;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;

class WebhookKafkaEventProducerConfigurationTests {

	private static final String BOOTSTRAP = "localhost:9092";

	private WebhookKafkaEventProducerConfiguration configuration;

	@BeforeEach
	void setUp() {
		this.configuration = new WebhookKafkaEventProducerConfiguration();
		ReflectionTestUtils.setField(this.configuration, "bootstrapServers", BOOTSTRAP);
		ReflectionTestUtils.setField(this.configuration, "securityProtocol", "SASL_SSL");
		ReflectionTestUtils.setField(this.configuration, "saslMechanism", "SCRAM-SHA-256");
		ReflectionTestUtils.setField(this.configuration, "saslUsername", "user");
		ReflectionTestUtils.setField(this.configuration, "saslPassword", "secret");
		ReflectionTestUtils.setField(this.configuration, "topicName", "webhook-topic");
	}

	@Test
	@DisplayName("webhookEventProducerFactory should configure bootstrap, security, SASL, and serializers")
	void testWebhookEventProducerFactoryValidFieldsBuildsProducerFactoryWithExpectedConfig() {
		// Given — configuration from @BeforeEach
		// When
		ProducerFactory<String, Object> factory = this.configuration.webhookEventProducerFactory();

		// Then
		assertThat(factory).isInstanceOf(DefaultKafkaProducerFactory.class).extracting((producerFactory) -> {
			@SuppressWarnings("unchecked")
			DefaultKafkaProducerFactory<String, Object> dkpf = (DefaultKafkaProducerFactory<String, Object>) producerFactory;
			return dkpf.getConfigurationProperties();
		})
			.asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
			.containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP)
			.containsEntry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
			.containsEntry(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256")
			.containsEntry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class)
			.containsEntry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class)
			.containsEntry(SaslConfigs.SASL_JAAS_CONFIG, KafkaConfigurationHelper.getJaasConfig("user", "secret"));
	}

	@Test
	@DisplayName("webhookEventKafkaTemplate should wrap producer factory")
	void testWebhookEventKafkaTemplateValidFactoryReturnsKafkaTemplate() {
		// Given — configuration from @BeforeEach
		// When
		KafkaTemplate<String, Object> template = this.configuration.webhookEventKafkaTemplate();

		// Then
		assertThat(template).isNotNull().extracting(KafkaTemplate::getDefaultTopic).isNull();
	}

}
