/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.recruitcrm.microservice.timesheet.testdata.RecruitCrmCoreEntityTestDataFactory;
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

@DisplayName("RecruitCRM access control Entity enum Tests")
class EntityTests {

	private static String expectedLabel(Entity entity) {
		return switch (entity) {
			case CANDIDATES -> "candidates";
			case CONTACTS -> "contacts";
			case JOBS -> "jobs";
			case COMPANIES -> "companies";
			case DEALS -> "deals";
			case PLACEMENT_BILLING -> "placementbilling";
			case TASK_MEETINGS -> "taskmeetings";
			case NOTES -> "notes";
			case CALL_LOG -> "calllog";
			case FILES -> "files";
			case GLOBAL -> "global";
		};
	}

	private static Arguments entityAndCanonicalLabelArguments(Entity entity) {
		return Arguments.of(entity, expectedLabel(entity));
	}

	static Stream<Arguments> entitiesAndCanonicalLabels() {
		return Arrays.stream(Entity.values()).map(EntityTests::entityAndCanonicalLabelArguments);
	}

	@Test
	@DisplayName("values should contain exactly the eleven defined entities")
	void testValuesContainsExactlyElevenEntities() {
		// When
		Entity[] values = Entity.values();

		// Then
		assertThat(values).hasSize(11)
			.containsExactlyInAnyOrder(Entity.CANDIDATES, Entity.CONTACTS, Entity.JOBS, Entity.COMPANIES, Entity.DEALS,
					Entity.PLACEMENT_BILLING, Entity.TASK_MEETINGS, Entity.NOTES, Entity.CALL_LOG, Entity.FILES,
					Entity.GLOBAL);
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
	@ValueSource(strings = { "Candidates", "CANDIDATES", "cAnDiDaTeS" })
	@DisplayName("fromValue should accept mixed casing for candidates label")
	void testFromValueAcceptsMixedCaseForCandidates(String input) {
		// When
		Entity result = Entity.fromValue(input);

		// Then
		assertThat(result).isEqualTo(Entity.CANDIDATES);
	}

	@Test
	@DisplayName("fromValue should throw when label is unknown")
	void testFromValueWithUnknownLabelThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> Entity.fromValue(RecruitCrmCoreEntityTestDataFactory.INVALID_ENTITY_LABEL))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(RecruitCrmCoreEntityTestDataFactory.FROM_VALUE_NOT_FOUND_MESSAGE_PREFIX)
			.hasMessageContaining(RecruitCrmCoreEntityTestDataFactory.INVALID_ENTITY_LABEL);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "" })
	@DisplayName("fromValue should throw when value is null or blank label")
	void testFromValueWithNullOrBlankThrowsIllegalArgumentException(String value) {
		// When & Then
		assertThatThrownBy(() -> Entity.fromValue(value)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(RecruitCrmCoreEntityTestDataFactory.FROM_VALUE_NOT_FOUND_MESSAGE_PREFIX);
	}

}
