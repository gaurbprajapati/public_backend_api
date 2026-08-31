/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.recruitcrm.microservice.timesheet.testdata.ContractStaffingPermissionLevelTestDataFactory;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Contract staffing PermissionLevel Tests")
class PermissionLevelTests {

	private static String expectedLabel(PermissionLevel level) {
		return switch (level) {
			case YES -> "Yes";
			case NO -> "No";
		};
	}

	private static Arguments permissionLevelAndCanonicalLabelArguments(PermissionLevel level) {
		return Arguments.of(level, expectedLabel(level));
	}

	static Stream<Arguments> permissionLevelsAndCanonicalLabels() {
		return Arrays.stream(PermissionLevel.values())
			.map(PermissionLevelTests::permissionLevelAndCanonicalLabelArguments);
	}

	@Test
	@DisplayName("values should contain exactly YES and NO")
	void testValuesContainsExactlyYesAndNo() {
		// When
		PermissionLevel[] values = PermissionLevel.values();

		// Then
		assertThat(values).hasSize(2).containsExactlyInAnyOrder(PermissionLevel.YES, PermissionLevel.NO);
	}

	@ParameterizedTest
	@EnumSource(PermissionLevel.class)
	@DisplayName("toString should return the configured label for each constant")
	void testToStringReturnsLabelForEachEnum(PermissionLevel level) {
		// Given
		String expected = expectedLabel(level);

		// When
		String result = level.toString();

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource("permissionLevelsAndCanonicalLabels")
	@DisplayName("fromValue should resolve each canonical label")
	void testFromValueWithCanonicalLabelReturnsMatchingEnum(PermissionLevel expected, String label) {
		// When
		PermissionLevel result = PermissionLevel.fromValue(label);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@EnumSource(PermissionLevel.class)
	@DisplayName("fromValue should match labels case-insensitively")
	void testFromValueIsCaseInsensitive(PermissionLevel expected) {
		// Given
		String upperCaseLabel = expectedLabel(expected).toUpperCase();

		// When
		PermissionLevel result = PermissionLevel.fromValue(upperCaseLabel);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = { "YES", "yes", "YeS", "yEs" })
	@DisplayName("fromValue should accept mixed casing for Yes label")
	void testFromValueAcceptsMixedCaseForYes(String input) {
		// When
		PermissionLevel result = PermissionLevel.fromValue(input);

		// Then
		assertThat(result).isEqualTo(PermissionLevel.YES);
	}

	@Test
	@DisplayName("fromValue should throw when label is unknown")
	void testFromValueWithUnknownLabelThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> PermissionLevel
			.fromValue(ContractStaffingPermissionLevelTestDataFactory.INVALID_PERMISSION_LEVEL_LABEL))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Unknown permission level:")
			.hasMessageContaining(ContractStaffingPermissionLevelTestDataFactory.INVALID_PERMISSION_LEVEL_LABEL);
	}

	@ParameterizedTest
	@NullSource
	@DisplayName("fromValue should throw when value is null")
	void testFromValueWithNullThrowsIllegalArgumentException(String value) {
		// When & Then
		assertThatThrownBy(() -> PermissionLevel.fromValue(value)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Unknown permission level:")
			.hasMessageContaining("null");
	}

}
