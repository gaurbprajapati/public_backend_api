package io.recruitcrm.microservice.timesheet.kafka.constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("KafkaJaasConstants Tests")
class KafkaJaasConstantsTests {

	@Test
	@DisplayName("constants should expose expected JAAS configuration values")
	void testKafkaJaasConstantsShouldMatchExpectedValues() {
		// Given

		// When and Then
		assertThat(KafkaJaasConstants.CONTROL_FLAG_REQUIRED).isEqualTo("required");
		assertThat(KafkaJaasConstants.OPTION_USERNAME).isEqualTo("username");
		assertThat(KafkaJaasConstants.OPTION_PASSWORD).isEqualTo("password");
	}

	@Test
	@DisplayName("constructor should be private and throw unsupported operation exception")
	void testConstructorShouldBePrivateAndThrowUnsupportedOperationException() throws NoSuchMethodException {
		// Given
		Constructor<KafkaJaasConstants> constructor = KafkaJaasConstants.class.getDeclaredConstructor();

		// When
		constructor.setAccessible(true);

		// Then
		assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class)
			.hasRootCauseMessage("Utility class");
	}

}
