package io.recruitcrm.microservice.timesheet.helpers.constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RepositoryParameterConstants Tests")
class RepositoryParameterConstantsTests {

	@Test
	@DisplayName("all repository parameter constants should expose expected values")
	void testRepositoryParameterConstantsValuesShouldMatchExpected() {
		// Given

		// When and Then
		assertThat(RepositoryParameterConstants.ACCOUNT_ID).isEqualTo("accountId");
		assertThat(RepositoryParameterConstants.JOB_ID).isEqualTo("jobId");
		assertThat(RepositoryParameterConstants.CONTRACTOR_ID).isEqualTo("contractorId");
		assertThat(RepositoryParameterConstants.TIMESHEET_ID).isEqualTo("timesheetId");
		assertThat(RepositoryParameterConstants.TIMESHEET_IDS).isEqualTo("timesheetIds");
		assertThat(RepositoryParameterConstants.OPEN_STATUS).isEqualTo("openStatus");
		assertThat(RepositoryParameterConstants.CONTRACTOR_IDS).isEqualTo("contractorIds");
		assertThat(RepositoryParameterConstants.DATES).isEqualTo("dates");
		assertThat(RepositoryParameterConstants.USER_ID).isEqualTo("userId");
		assertThat(RepositoryParameterConstants.USER_TYPE_ID).isEqualTo("userTypeId");
	}

	@Test
	@DisplayName("constructor should be private and throw unsupported operation exception")
	void testConstructorPrivateAndThrowsUnsupportedOperationException() throws NoSuchMethodException {
		// Given
		Constructor<RepositoryParameterConstants> constructor = RepositoryParameterConstants.class
			.getDeclaredConstructor();

		// When
		constructor.setAccessible(true);

		// Then
		assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class)
			.hasRootCauseMessage("This is a utility class and cannot be instantiated");
	}

}
