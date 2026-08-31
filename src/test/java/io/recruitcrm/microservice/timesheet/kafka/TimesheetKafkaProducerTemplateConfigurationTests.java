package io.recruitcrm.microservice.timesheet.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TimesheetKafkaProducerTemplateConfigurationTests {

	@Test
	@DisplayName("enrichProducerPropertiesWithSaslJaasIfNeeded should no-op for PLAINTEXT")
	void testEnrichProducerPropertiesWithSaslJaasIfNeededPlaintextLeavesMapUnchanged() {
		final Map<String, Object> props = new HashMap<>();
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
		TimesheetKafkaProducerTemplateConfiguration.enrichProducerPropertiesWithSaslJaasIfNeeded(props, "u", "p",
				"SCRAM-SHA-256");
		assertThat(props).doesNotContainKey(SaslConfigs.SASL_JAAS_CONFIG);
	}

	@Test
	@DisplayName("enrichProducerPropertiesWithSaslJaasIfNeeded should add JAAS when SASL and credentials present")
	void testEnrichProducerPropertiesWithSaslJaasIfNeededSaslSslAddsJaasAndDefaultMechanism() {
		final Map<String, Object> props = new HashMap<>();
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		TimesheetKafkaProducerTemplateConfiguration.enrichProducerPropertiesWithSaslJaasIfNeeded(props, "alice",
				"secret", "");
		assertThat(props).containsEntry(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256")
			.containsEntry(SaslConfigs.SASL_JAAS_CONFIG, KafkaConfigurationHelper.getJaasConfig("alice", "secret"));
	}

	@Test
	@DisplayName("enrichProducerPropertiesWithSaslJaasIfNeeded should respect explicit mechanism")
	void testEnrichProducerPropertiesWithSaslJaasIfNeededUsesExplicitMechanism() {
		final Map<String, Object> props = new HashMap<>();
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
		TimesheetKafkaProducerTemplateConfiguration.enrichProducerPropertiesWithSaslJaasIfNeeded(props, "u", "p",
				"SCRAM-SHA-512");
		assertThat(props).containsEntry(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
	}

	@Test
	@DisplayName("enrichProducerPropertiesWithSaslJaasIfNeeded should not override existing JAAS")
	void testEnrichProducerPropertiesWithSaslJaasIfNeededExistingJaasUnchanged() {
		final Map<String, Object> props = new HashMap<>();
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		props.put(SaslConfigs.SASL_JAAS_CONFIG, "existing");
		TimesheetKafkaProducerTemplateConfiguration.enrichProducerPropertiesWithSaslJaasIfNeeded(props, "u", "p", "");
		assertThat(props).containsEntry(SaslConfigs.SASL_JAAS_CONFIG, "existing");
	}

	@Test
	@DisplayName("enrichProducerPropertiesWithSaslJaasIfNeeded should skip when credentials missing")
	void testEnrichProducerPropertiesWithSaslJaasIfNeededMissingCredentialsNoJaas() {
		final Map<String, Object> props = new HashMap<>();
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		TimesheetKafkaProducerTemplateConfiguration.enrichProducerPropertiesWithSaslJaasIfNeeded(props, "", "secret",
				"");
		assertThat(props).doesNotContainKey(SaslConfigs.SASL_JAAS_CONFIG);
	}

	@Test
	@DisplayName("enrichProducerPropertiesWithSaslJaasIfNeeded should not replace mechanism already in map")
	void testEnrichProducerPropertiesWithSaslJaasIfNeededKeepsMechanismFromProperties() {
		final Map<String, Object> props = new HashMap<>();
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
		TimesheetKafkaProducerTemplateConfiguration.enrichProducerPropertiesWithSaslJaasIfNeeded(props, "u", "p",
				"SCRAM-SHA-256");
		assertThat(props).containsEntry(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512")
			.containsEntry(SaslConfigs.SASL_JAAS_CONFIG, KafkaConfigurationHelper.getJaasConfig("u", "p"));
	}

	@Test
	@DisplayName("enrichProducerPropertiesWithSaslJaasIfNeeded should no-op when security protocol is not a string")
	void testEnrichProducerPropertiesWithSaslJaasIfNeededNonStringProtocolLeavesMapUnchanged() {
		final Map<String, Object> props = new HashMap<>();
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, 123);
		TimesheetKafkaProducerTemplateConfiguration.enrichProducerPropertiesWithSaslJaasIfNeeded(props, "u", "p",
				"SCRAM-SHA-256");
		assertThat(props).doesNotContainKey(SaslConfigs.SASL_JAAS_CONFIG);
	}

	@Test
	@DisplayName("enrichProducerPropertiesWithSaslJaasIfNeeded should skip when only password is missing")
	void testEnrichProducerPropertiesWithSaslJaasIfNeededMissingPasswordNoJaas() {
		final Map<String, Object> props = new HashMap<>();
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		TimesheetKafkaProducerTemplateConfiguration.enrichProducerPropertiesWithSaslJaasIfNeeded(props, "alice", "",
				"");
		assertThat(props).doesNotContainKey(SaslConfigs.SASL_JAAS_CONFIG);
	}

	@Test
	@DisplayName("enrichProducerPropertiesWithSaslJaasIfNeeded should add JAAS when existing JAAS is blank")
	void testEnrichProducerPropertiesWithSaslJaasIfNeededBlankExistingJaasAddsJaas() {
		final Map<String, Object> props = new HashMap<>();
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		props.put(SaslConfigs.SASL_JAAS_CONFIG, "   ");
		TimesheetKafkaProducerTemplateConfiguration.enrichProducerPropertiesWithSaslJaasIfNeeded(props, "u", "p",
				"SCRAM-SHA-512");
		assertThat(props).containsEntry(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512")
			.containsEntry(SaslConfigs.SASL_JAAS_CONFIG, KafkaConfigurationHelper.getJaasConfig("u", "p"));
	}

	@Test
	@DisplayName("kafkaTemplate should create KafkaTemplate from producer properties")
	void testKafkaTemplateCreatesKafkaTemplateFromProducerProperties() {
		// Given
		TimesheetKafkaProducerTemplateConfiguration configuration = new TimesheetKafkaProducerTemplateConfiguration();
		ReflectionTestUtils.setField(configuration, "saslUsername", "kafka-user");
		ReflectionTestUtils.setField(configuration, "saslPassword", "kafka-pass");
		ReflectionTestUtils.setField(configuration, "saslMechanism", "SCRAM-SHA-256");
		KafkaProperties kafkaProperties = new KafkaProperties();
		kafkaProperties.setBootstrapServers(List.of("localhost:9092"));

		// When
		KafkaTemplate<String, String> kafkaTemplate = configuration.kafkaTemplate(kafkaProperties);

		// Then
		assertThat(kafkaTemplate).isNotNull();
		assertThat(kafkaTemplate.getProducerFactory()).isNotNull();
	}

}
