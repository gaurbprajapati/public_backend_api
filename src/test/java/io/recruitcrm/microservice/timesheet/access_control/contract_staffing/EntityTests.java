/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.recruitcrm.microservice.timesheet.testdata.ContractStaffingEntityTestDataFactory;
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

@DisplayName("Contract staffing Entity enum Tests")
class EntityTests {

	private static String expectedLabel(Entity entity) {
		return switch (entity) {
			case TIMESHEET -> "timesheet";
			case TIMESHEET_SETTINGS -> "timesheet_settings";
			case CANDIDATE -> "candidate";
			case JOB -> "job";
		};
	}

	private static Arguments entityAndCanonicalLabelArguments(Entity entity) {
		return Arguments.of(entity, expectedLabel(entity));
	}

	static Stream<Arguments> entitiesAndCanonicalLabels() {
		return Arrays.stream(Entity.values()).map(EntityTests::entityAndCanonicalLabelArguments);
	}

	@Test
	@DisplayName("values should contain exactly the four defined entities")
	void testValuesContainsExactlyFourEntities() {
		// When
		Entity[] values = Entity.values();

		// Then
		assertThat(values).hasSize(4)
			.containsExactlyInAnyOrder(Entity.TIMESHEET, Entity.TIMESHEET_SETTINGS, Entity.CANDIDATE, Entity.JOB);
	}

	@ParameterizedTest
	@EnumSource(Entity.class)
	@DisplayName("toString should return the configured label for each constant")
	void testToStringReturnsLabelForEachEnum(Entity entity) {
		// Given
		String expected = expectedLabel(entity);

		// When
		String result = entity.toString();

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource("entitiesAndCanonicalLabels")
	@DisplayName("fromValue should resolve each canonical label")
	void testFromValueWithCanonicalLabelReturnsMatchingEnum(Entity expected, String label) {
		// When
		Entity result = Entity.fromValue(label);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@EnumSource(Entity.class)
	@DisplayName("fromValue should match labels case-insensitively")
	void testFromValueIsCaseInsensitive(Entity expected) {
		// Given
		String upperCaseLabel = expectedLabel(expected).toUpperCase();

		// When
		Entity result = Entity.fromValue(upperCaseLabel);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = { "Timesheet", "TIMEsheet", "TiMeShEeT" })
	@DisplayName("fromValue should accept mixed casing for timesheet label")
	void testFromValueAcceptsMixedCaseForTimesheet(String input) {
		// When
		Entity result = Entity.fromValue(input);

		// Then
		assertThat(result).isEqualTo(Entity.TIMESHEET);
	}

	@Test
	@DisplayName("fromValue should throw when label is unknown")
	void testFromValueWithUnknownLabelThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> Entity.fromValue(ContractStaffingEntityTestDataFactory.INVALID_ENTITY_LABEL))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No enum constant for value:")
			.hasMessageContaining(ContractStaffingEntityTestDataFactory.INVALID_ENTITY_LABEL);
	}

	@ParameterizedTest
	@NullSource
	@DisplayName("fromValue should throw when value is null")
	void testFromValueWithNullThrowsIllegalArgumentException(String value) {
		// When & Then
		assertThatThrownBy(() -> Entity.fromValue(value)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No enum constant for value:")
			.hasMessageContaining("null");
	}

}
