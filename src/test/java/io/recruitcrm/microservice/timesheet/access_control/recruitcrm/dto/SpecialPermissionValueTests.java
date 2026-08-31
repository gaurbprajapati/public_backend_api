package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.PermissionLevel;
import io.recruitcrm.microservice.timesheet.testdata.SpecialPermissionValueTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SpecialPermissionValue Tests")
class SpecialPermissionValueTests {

	@Test
	@DisplayName("getStringValue should return configured label")
	void testGetStringValueReturnsConfiguredLabel() {
		// Given
		SpecialPermissionValue value = SpecialPermissionValue.BOTH;

		// When
		String result = value.getStringValue();

		// Then
		assertThat(result).isEqualTo("Both");
	}

	@Test
	@DisplayName("getIntValue should return configured integer value")
	void testGetIntValueReturnsConfiguredIntegerValue() {
		// Given
		SpecialPermissionValue value = SpecialPermissionValue.BOTH;

		// When
		int result = value.getIntValue();

		// Then
		assertThat(result).isEqualTo(4);
	}

	@Test
	@DisplayName("fromString should return null for null value")
	void testFromStringWithNullReturnsNull() {
		// Given
		String value = null;

		// When
		SpecialPermissionValue result = SpecialPermissionValue.fromString(value);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("fromString should resolve valid value case insensitively")
	void testFromStringWithValidValueReturnsEnum() {
		// Given
		String value = SpecialPermissionValueTestDataFactory.VALID_STRING_OWNED_ONLY_MIXED_CASE;

		// When
		SpecialPermissionValue result = SpecialPermissionValue.fromString(value);

		// Then
		assertThat(result).isEqualTo(SpecialPermissionValue.OWNED_ONLY);
	}

	@Test
	@DisplayName("fromString should return null for unknown value")
	void testFromStringWithInvalidValueReturnsNull() {
		// Given
		String value = SpecialPermissionValueTestDataFactory.INVALID_STRING_PERMISSION_VALUE;

		// When
		SpecialPermissionValue result = SpecialPermissionValue.fromString(value);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("fromInt should return enum for valid integer value")
	void testFromIntWithValidValueReturnsEnum() {
		// Given
		int value = SpecialPermissionValueTestDataFactory.VALID_INTEGER_EVERYTHING;

		// When
		SpecialPermissionValue result = SpecialPermissionValue.fromInt(value);

		// Then
		assertThat(result).isEqualTo(SpecialPermissionValue.EVERYTHING);
	}

	@Test
	@DisplayName("fromInt should return first matching enum for shared integer value")
	void testFromIntWithSharedValueReturnsFirstEnum() {
		// Given
		int value = SpecialPermissionValueTestDataFactory.VALID_INTEGER_SHARED_LEVEL;

		// When
		SpecialPermissionValue result = SpecialPermissionValue.fromInt(value);

		// Then
		assertThat(result).isEqualTo(SpecialPermissionValue.CANDIDATES_ONLY);
	}

	@Test
	@DisplayName("fromInt should return null for invalid integer")
	void testFromIntWithInvalidValueReturnsNull() {
		// Given
		int value = SpecialPermissionValueTestDataFactory.INVALID_INTEGER_PERMISSION_VALUE;

		// When
		SpecialPermissionValue result = SpecialPermissionValue.fromInt(value);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("isValidStringValue should return true for valid string")
	void testIsValidStringValueWithValidValueReturnsTrue() {
		// Given
		String value = SpecialPermissionValueTestDataFactory.VALID_STRING_OWNED_ONLY_MIXED_CASE;

		// When
		boolean result = SpecialPermissionValue.isValidStringValue(value);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isValidStringValue should return false for invalid string")
	void testIsValidStringValueWithInvalidValueReturnsFalse() {
		// Given
		String value = SpecialPermissionValueTestDataFactory.INVALID_STRING_PERMISSION_VALUE;

		// When
		boolean result = SpecialPermissionValue.isValidStringValue(value);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isValidIntValue should return true for valid integer")
	void testIsValidIntValueWithValidValueReturnsTrue() {
		// Given
		int value = SpecialPermissionValueTestDataFactory.VALID_INTEGER_EVERYTHING;

		// When
		boolean result = SpecialPermissionValue.isValidIntValue(value);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isValidIntValue should return false for invalid integer")
	void testIsValidIntValueWithInvalidValueReturnsFalse() {
		// Given
		int value = SpecialPermissionValueTestDataFactory.INVALID_INTEGER_PERMISSION_VALUE;

		// When
		boolean result = SpecialPermissionValue.isValidIntValue(value);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("toPermissionLevel should map NOTHING to NO")
	void testToPermissionLevelForNothingReturnsNo() {
		// Given
		SpecialPermissionValue value = SpecialPermissionValue.NOTHING;

		// When
		PermissionLevel result = value.toPermissionLevel();

		// Then
		assertThat(result).isEqualTo(PermissionLevel.NO);
	}

	@Test
	@DisplayName("toPermissionLevel should map OWNED_ONLY to OWNED_ONLY")
	void testToPermissionLevelForOwnedOnlyReturnsOwnedOnly() {
		// Given
		SpecialPermissionValue value = SpecialPermissionValue.OWNED_ONLY;

		// When
		PermissionLevel result = value.toPermissionLevel();

		// Then
		assertThat(result).isEqualTo(PermissionLevel.OWNED_ONLY);
	}

	@Test
	@DisplayName("toPermissionLevel should map TEAM_ONLY to TEAM_ONLY")
	void testToPermissionLevelForTeamOnlyReturnsTeamOnly() {
		// Given
		SpecialPermissionValue value = SpecialPermissionValue.TEAM_ONLY;

		// When
		PermissionLevel result = value.toPermissionLevel();

		// Then
		assertThat(result).isEqualTo(PermissionLevel.TEAM_ONLY);
	}

	@Test
	@DisplayName("toPermissionLevel should map EVERYTHING to EVERYTHING")
	void testToPermissionLevelForEverythingReturnsEverything() {
		// Given
		SpecialPermissionValue value = SpecialPermissionValue.EVERYTHING;

		// When
		PermissionLevel result = value.toPermissionLevel();

		// Then
		assertThat(result).isEqualTo(PermissionLevel.EVERYTHING);
	}

	@Test
	@DisplayName("toPermissionLevel should map CANDIDATES_ONLY to YES")
	void testToPermissionLevelForCandidatesOnlyReturnsYes() {
		// Given
		SpecialPermissionValue value = SpecialPermissionValue.CANDIDATES_ONLY;

		// When
		PermissionLevel result = value.toPermissionLevel();

		// Then
		assertThat(result).isEqualTo(PermissionLevel.YES);
	}

	@Test
	@DisplayName("toPermissionLevel should map CONTACTS_ONLY to YES")
	void testToPermissionLevelForContactsOnlyReturnsYes() {
		// Given
		SpecialPermissionValue value = SpecialPermissionValue.CONTACTS_ONLY;

		// When
		PermissionLevel result = value.toPermissionLevel();

		// Then
		assertThat(result).isEqualTo(PermissionLevel.YES);
	}

	@Test
	@DisplayName("toPermissionLevel should map BOTH to YES")
	void testToPermissionLevelForBothReturnsYes() {
		// Given
		SpecialPermissionValue value = SpecialPermissionValue.BOTH;

		// When
		PermissionLevel result = value.toPermissionLevel();

		// Then
		assertThat(result).isEqualTo(PermissionLevel.YES);
	}

}
