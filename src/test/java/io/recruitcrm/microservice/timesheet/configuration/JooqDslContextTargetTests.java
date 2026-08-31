package io.recruitcrm.microservice.timesheet.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JooqDslContextTarget Tests")
class JooqDslContextTargetTests {

	@Test
	@DisplayName("constants should expose expected DSL context bean names")
	void testJooqDslContextTargetShouldMatchExpectedValues() {
		// Given

		// When and Then
		assertThat(JooqDslContextTarget.RECRUITCRM_AURORA).isEqualTo("auroraDbDSLContext");
		assertThat(JooqDslContextTarget.RECRUITCRM_SINGLESTORE).isEqualTo("singleStoreDslContext");
	}

	@Test
	@DisplayName("constructor should be private and throw unsupported operation exception")
	void testConstructorShouldBePrivateAndThrowUnsupportedOperationException() throws NoSuchMethodException {
		// Given
		Constructor<JooqDslContextTarget> constructor = JooqDslContextTarget.class.getDeclaredConstructor();

		// When
		constructor.setAccessible(true);

		// Then
		assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class)
			.hasRootCauseMessage("This is a utility class and cannot be instantiated");
	}

}
