/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.kafka.webhook_events.configuration;

import io.recruitcrm.microservice.timesheet.kafka.KafkaConfigurationHelper;
import lombok.Getter;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebhookKafkaEventProducerConfiguration {

	@Value("${spring.kafka.webhook-events.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.webhook-events.security.protocol}")
	private String securityProtocol;

	@Value("${spring.kafka.webhook-events.sasl.mechanism}")
	private String saslMechanism;

	@Value("${spring.kafka.webhook-events.sasl.username}")
	private String saslUsername;

	@Value("${spring.kafka.webhook-events.sasl.password}")
	private String saslPassword;

	@Value("${spring.kafka.webhook-events.consumer.group-id}")
	private String groupId;

	@Value("${spring.kafka.webhook-events.consumer.enable-partition-eof}")
	private boolean enablePartitionEOF;

	@Value("${spring.kafka.webhook-events.consumer.session-timeout-ms}")
	private int sessionTimeoutMs;

	@Getter
	@Value("${spring.kafka.webhook-events.topic.name}")
	private String topicName;

	public static final String KAFKA_TEMPLATE_BEAN_NAME = "webhookEventKafkaTemplate";

	public static final String KAFKA_EVENT_PRODUCER_BEAN_NAME = "webhookEventKafkaProducer";

	@Bean
	public ProducerFactory<String, Object> webhookEventProducerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
		configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, this.securityProtocol);
		configProps.put(SaslConfigs.SASL_MECHANISM, this.saslMechanism);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		configProps.put(SaslConfigs.SASL_JAAS_CONFIG,
				KafkaConfigurationHelper.getJaasConfig(this.saslUsername, this.saslPassword));
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean(name = WebhookKafkaEventProducerConfiguration.KAFKA_TEMPLATE_BEAN_NAME)
	public KafkaTemplate<String, Object> webhookEventKafkaTemplate() {
		return new KafkaTemplate<>(webhookEventProducerFactory());
	}

}
