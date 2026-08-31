package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import io.recruitcrm.microservice.timesheet.testdata.SpecialPermissionFieldHandlerTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SpecialPermissionFieldHandler Tests")
class SpecialPermissionFieldHandlerTests {

	@Test
	@DisplayName("constructor should be private and throw illegal state exception")
	void testConstructorPrivateAndThrowsIllegalStateException() throws NoSuchMethodException {
		// Given
		Constructor<SpecialPermissionFieldHandler> constructor = SpecialPermissionFieldHandler.class
			.getDeclaredConstructor();

		// When
		constructor.setAccessible(true);

		// Then
		assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(IllegalStateException.class)
			.hasRootCauseMessage("Utility class");
	}

	@Test
	@DisplayName("isSpecialPermissionField should return true for known built in field case insensitively")
	void testIsSpecialPermissionFieldWithKnownFieldReturnsTrue() {
		// Given
		String lowerCaseField = SpecialPermissionFieldHandlerTestDataFactory.BUILT_IN_FIELD_LOWER_CASE;
		String mixedCaseField = SpecialPermissionFieldHandlerTestDataFactory.BUILT_IN_FIELD_MIXED_CASE;

		// When
		boolean lowerCaseResult = SpecialPermissionFieldHandler.isSpecialPermissionField(lowerCaseField);
		boolean mixedCaseResult = SpecialPermissionFieldHandler.isSpecialPermissionField(mixedCaseField);

		// Then
		assertThat(lowerCaseResult).isTrue();
		assertThat(mixedCaseResult).isTrue();
	}

	@Test
	@DisplayName("isSpecialPermissionField should return false for unknown field")
	void testIsSpecialPermissionFieldWithUnknownFieldReturnsFalse() {
		// Given
		String field = SpecialPermissionFieldHandlerTestDataFactory.NON_SPECIAL_FIELD;

		// When
		boolean result = SpecialPermissionFieldHandler.isSpecialPermissionField(field);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("registerSpecialField should register new field and match case insensitively")
	void testRegisterSpecialFieldRegistersFieldSuccessfully() {
		// Given
		String customField = SpecialPermissionFieldHandlerTestDataFactory.REGISTRABLE_FIELD_MIXED_CASE;

		// When
		SpecialPermissionFieldHandler.registerSpecialField(customField);
		boolean result = SpecialPermissionFieldHandler.isSpecialPermissionField(customField.toLowerCase());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("processSpecialPermissionValue should return same integer for valid integer input")
	void testProcessSpecialPermissionValueWithValidIntegerReturnsSameValue() {
		// Given
		Integer value = Integer.valueOf(SpecialPermissionFieldHandlerTestDataFactory.VALID_INTEGER_PERMISSION_VALUE);

		// When
		int result = SpecialPermissionFieldHandler.processSpecialPermissionValue(value);

		// Then
		assertThat(result).isEqualTo(value.intValue());
	}

	@Test
	@DisplayName("processSpecialPermissionValue should map valid string input to integer")
	void testProcessSpecialPermissionValueWithValidStringReturnsMappedInteger() {
		// Given
		String value = SpecialPermissionFieldHandlerTestDataFactory.VALID_STRING_PERMISSION_VALUE;

		// When
		int result = SpecialPermissionFieldHandler.processSpecialPermissionValue(value);

		// Then
		assertThat(result).isEqualTo(1);
	}

	@Test
	@DisplayName("processSpecialPermissionValue should throw when input value is null")
	void testProcessSpecialPermissionValueWithNullThrowsIllegalArgumentException() {
		// Given
		Object value = null;

		// When and Then
		assertThatThrownBy(() -> SpecialPermissionFieldHandler.processSpecialPermissionValue(value))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Permission value cannot be null");
	}

	@Test
	@DisplayName("processSpecialPermissionValue should throw when integer value is invalid")
	void testProcessSpecialPermissionValueWithInvalidIntegerThrowsIllegalArgumentException() {
		// Given
		Integer value = Integer.valueOf(SpecialPermissionFieldHandlerTestDataFactory.INVALID_INTEGER_PERMISSION_VALUE);

		// When and Then
		assertThatThrownBy(() -> SpecialPermissionFieldHandler.processSpecialPermissionValue(value))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid integer permission value: " + value);
	}

	@Test
	@DisplayName("processSpecialPermissionValue should throw when string value is invalid")
	void testProcessSpecialPermissionValueWithInvalidStringThrowsIllegalArgumentException() {
		// Given
		String value = SpecialPermissionFieldHandlerTestDataFactory.INVALID_STRING_PERMISSION_VALUE;

		// When and Then
		assertThatThrownBy(() -> SpecialPermissionFieldHandler.processSpecialPermissionValue(value))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid string permission value: " + value);
	}

	@Test
	@DisplayName("processSpecialPermissionValue should throw when value type is unsupported")
	void testProcessSpecialPermissionValueWithUnsupportedTypeThrowsIllegalArgumentException() {
		// Given
		Object value = List.of("unsupported");

		// When and Then
		assertThatThrownBy(() -> SpecialPermissionFieldHandler.processSpecialPermissionValue(value))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Unsupported value type: " + value.getClass().getName());
	}

	@Test
	@DisplayName("getStringRepresentation should return mapped value for valid integer")
	void testGetStringRepresentationWithValidIntegerReturnsMappedString() {
		// Given
		int value = SpecialPermissionFieldHandlerTestDataFactory.VALID_INTEGER_PERMISSION_VALUE;

		// When
		String result = SpecialPermissionFieldHandler.getStringRepresentation(value);

		// Then
		assertThat(result).isEqualTo("Everything");
	}

	@Test
	@DisplayName("getStringRepresentation should throw when integer value is invalid")
	void testGetStringRepresentationWithInvalidIntegerThrowsIllegalArgumentException() {
		// Given
		int value = SpecialPermissionFieldHandlerTestDataFactory.INVALID_INTEGER_PERMISSION_VALUE;

		// When and Then
		assertThatThrownBy(() -> SpecialPermissionFieldHandler.getStringRepresentation(value))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid integer permission value: " + value);
	}

}
