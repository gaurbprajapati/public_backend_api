package io.recruitcrm.microservice.timesheet.helpers.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccessControlCheckerExampleConstants Tests")
class AccessControlCheckerExampleConstantsTests {

	@Test
	@DisplayName("SUCCESSFUL_SUFFIX constant has expected value")
	void testSuccessfulSuffixConstantHasExpectedValue() {
		// Given

		// When & Then
		assertThat(AccessControlCheckerExampleConstants.SUCCESSFUL_SUFFIX).isEqualTo(" successful");
	}

	@Test
	@DisplayName("BULK_PERMISSION_CHECK_COMPLETED_PREFIX constant has expected value")
	void testBulkPermissionCheckCompletedPrefixConstantHasExpectedValue() {
		// Given

		// When & Then
		assertThat(AccessControlCheckerExampleConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX)
			.isEqualTo("Bulk permission check completed: ");
	}

	@Test
	@DisplayName("CHECKING_BULK_PERMISSIONS_FOR_PREFIX constant has expected value")
	void testCheckingBulkPermissionsForPrefixConstantHasExpectedValue() {
		// Given

		// When & Then
		assertThat(AccessControlCheckerExampleConstants.CHECKING_BULK_PERMISSIONS_FOR_PREFIX)
			.isEqualTo("Checking bulk permissions for ");
	}

	@Test
	@DisplayName("constructor should be private and throw unsupported operation exception")
	void testConstructorShouldBePrivateAndThrowUnsupportedOperationException() throws NoSuchMethodException {
		// Given
		Constructor<AccessControlCheckerExampleConstants> constructor = AccessControlCheckerExampleConstants.class
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
