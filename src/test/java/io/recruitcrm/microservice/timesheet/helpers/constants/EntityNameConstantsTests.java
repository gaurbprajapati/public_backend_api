package io.recruitcrm.microservice.timesheet.helpers.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EntityNameConstants Tests")
class EntityNameConstantsTests {

	@Test
	@DisplayName("Constructor throws UnsupportedOperationException when invoked via reflection")
	void testPrivateConstructorThrowsWhenInvokedViaReflection() throws Exception {
		Constructor<EntityNameConstants> constructor = EntityNameConstants.class.getDeclaredConstructor();
		constructor.setAccessible(true);

		Throwable thrown = catchThrowable(constructor::newInstance);

		assertThat(thrown).isInstanceOf(InvocationTargetException.class)
			.cause()
			.isInstanceOf(UnsupportedOperationException.class)
			.hasMessageContaining("utility class");
	}

}
