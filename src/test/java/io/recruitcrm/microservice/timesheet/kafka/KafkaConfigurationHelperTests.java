package io.recruitcrm.microservice.timesheet.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.kafka.constants.KafkaJaasConstants;
import org.apache.kafka.common.security.scram.ScramLoginModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KafkaConfigurationHelperTests {

	@Test
	@DisplayName("getJaasConfig should return SCRAM JAAS string with username and password")
	void testGetJaasConfigValidCredentialsReturnsScramJaasString() {
		// Given
		String username = "kafkaUser";
		String password = "kafkaSecret";

		// When
		String jaas = KafkaConfigurationHelper.getJaasConfig(username, password);

		// Then
		String expected = String.format("%s %s %s=\"%s\" %s=\"%s\";", ScramLoginModule.class.getName(),
				KafkaJaasConstants.CONTROL_FLAG_REQUIRED, KafkaJaasConstants.OPTION_USERNAME, username,
				KafkaJaasConstants.OPTION_PASSWORD, password);
		assertThat(jaas).isEqualTo(expected);
	}

}
