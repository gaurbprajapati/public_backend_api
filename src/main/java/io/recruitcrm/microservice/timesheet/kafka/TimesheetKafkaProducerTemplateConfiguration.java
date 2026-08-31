/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TimesheetKafkaProducerTemplateConfiguration {

	@Value("${spring.kafka.sasl.username:}")
	private String saslUsername;

	@Value("${spring.kafka.sasl.password:}")
	private String saslPassword;

	@Value("${spring.kafka.sasl.mechanism:}")
	private String saslMechanism;

	@Bean(name = "kafkaTemplate")
	@ConditionalOnMissingBean(name = "kafkaTemplate")
	@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
	public KafkaTemplate<String, String> kafkaTemplate(final KafkaProperties kafkaProperties) {
		final Map<String, Object> properties = new HashMap<>(kafkaProperties.buildProducerProperties());
		enrichProducerPropertiesWithSaslJaasIfNeeded(properties, this.saslUsername, this.saslPassword,
				this.saslMechanism);
		final ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(properties);
		return new KafkaTemplate<>(producerFactory);
	}

	/**
	 * Spring Boot's {@link KafkaProperties} does not map
	 * {@code spring.kafka.sasl.username} / {@code password} into the client config. When
	 * {@code security.protocol} uses SASL, the broker still requires
	 * {@link SaslConfigs#SASL_JAAS_CONFIG} (or a global JAAS file). This aligns the main
	 * producer with
	 * {@link io.recruitcrm.microservice.timesheet.kafka.webhook_events.configuration.WebhookKafkaEventProducerConfiguration}
	 * by building SCRAM JAAS from credentials. Assumes SCRAM when mechanism is unset
	 * (same as {@link KafkaConfigurationHelper#getJaasConfig}).
	 */
	static void enrichProducerPropertiesWithSaslJaasIfNeeded(final Map<String, Object> producerProperties,
			final String saslUsername, final String saslPassword, final String saslMechanism) {
		final Object protocolObj = producerProperties.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG);
		if (!(protocolObj instanceof String protocol) || !protocol.contains("SASL")) {
			return;
		}
		final Object existingJaas = producerProperties.get(SaslConfigs.SASL_JAAS_CONFIG);
		if (existingJaas instanceof String jaas && StringUtils.hasText(jaas)) {
			return;
		}
		if (!StringUtils.hasText(saslUsername) || !StringUtils.hasText(saslPassword)) {
			return;
		}
		final Object mechObj = producerProperties.get(SaslConfigs.SASL_MECHANISM);
		final String mechanismFromProps = (mechObj instanceof String s) ? s : "";
		if (!StringUtils.hasText(mechanismFromProps)) {
			final String mechanism = (StringUtils.hasText(saslMechanism)) ? saslMechanism : "SCRAM-SHA-256";
			producerProperties.put(SaslConfigs.SASL_MECHANISM, mechanism);
		}
		producerProperties.put(SaslConfigs.SASL_JAAS_CONFIG,
				KafkaConfigurationHelper.getJaasConfig(saslUsername, saslPassword));
	}

}
