package io.recruitcrm.microservice.timesheet.helpers.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.recruitcrm.microservice.timesheet.testdata.EntityTypeEnumTestDataFactory;
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

@DisplayName("EntityTypeEnum Tests")
class EntityTypeEnumTests {

	private static int expectedId(EntityTypeEnum type) {
		return switch (type) {
			case COMPANY -> 3;
			case JOB -> 4;
			case CANDIDATE -> 5;
			case DEAL -> 11;
		};
	}

	private static String expectedEntityName(EntityTypeEnum type) {
		return switch (type) {
			case COMPANY -> "company";
			case JOB -> "job";
			case CANDIDATE -> "candidate";
			case DEAL -> "deal";
		};
	}

	static Stream<Arguments> entityTypesAndExpectedIds() {
		return Arrays.stream(EntityTypeEnum.values())
			.map((entityType) -> Arguments.of(entityType, expectedId(entityType)));
	}

	@Test
	@DisplayName("values should contain exactly the four defined entity types")
	void testValuesContainsExactlyFourEntityTypes() {
		// When
		EntityTypeEnum[] values = EntityTypeEnum.values();

		// Then
		assertThat(values).hasSize(4)
			.containsExactlyInAnyOrder(EntityTypeEnum.COMPANY, EntityTypeEnum.JOB, EntityTypeEnum.CANDIDATE,
					EntityTypeEnum.DEAL);
	}

	@ParameterizedTest
	@EnumSource(EntityTypeEnum.class)
	@DisplayName("getId should return the configured database id for each enum constant")
	void testGetIdReturnsConfiguredIdForEachEnum(EntityTypeEnum entityType) {
		// Given
		int expected = expectedId(entityType);

		// When
		Integer result = entityType.getId();

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@EnumSource(EntityTypeEnum.class)
	@DisplayName("getEntityName should return the configured entity name for each enum constant")
	void testGetEntityNameReturnsConfiguredNameForEachEnum(EntityTypeEnum entityType) {
		// Given
		String expected = expectedEntityName(entityType);

		// When
		String result = entityType.getEntityName();

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@EnumSource(EntityTypeEnum.class)
	@DisplayName("getIdAsString should match String.valueOf(getId) for each enum constant")
	void testGetIdAsStringMatchesStringValueOfId(EntityTypeEnum entityType) {
		// When
		String result = entityType.getIdAsString();

		// Then
		assertThat(result).isEqualTo(String.valueOf(entityType.getId()));
	}

	@ParameterizedTest
	@MethodSource("entityTypesAndExpectedIds")
	@DisplayName("fromId should resolve each known id to the matching enum")
	void testFromIdWithValidIdsReturnsMatchingEnum(EntityTypeEnum expected, int id) {
		// When
		EntityTypeEnum result = EntityTypeEnum.fromId(id);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("fromId should throw when id is unknown")
	void testFromIdWithUnknownIdThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> EntityTypeEnum.fromId(EntityTypeEnumTestDataFactory.INVALID_ENTITY_ID))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No EntityType found for ID:")
			.hasMessageContaining(String.valueOf(EntityTypeEnumTestDataFactory.INVALID_ENTITY_ID));
	}

	@Test
	@DisplayName("fromId should throw when id is null")
	void testFromIdWithNullThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> EntityTypeEnum.fromId(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No EntityType found for ID:")
			.hasMessageContaining("null");
	}

	@ParameterizedTest
	@EnumSource(EntityTypeEnum.class)
	@DisplayName("fromEntityName should resolve canonical lowercase names")
	void testFromEntityNameWithCanonicalNamesReturnsEnum(EntityTypeEnum expected) {
		// Given
		String entityName = expected.getEntityName();

		// When
		EntityTypeEnum result = EntityTypeEnum.fromEntityName(entityName);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = { "COMPANY", "Company", "JOB", "Job", "CANDIDATE", "Candidate", "DEAL", "Deal" })
	@DisplayName("fromEntityName should be case insensitive")
	void testFromEntityNameIsCaseInsensitive(String mixedCaseName) {
		// When
		EntityTypeEnum result = EntityTypeEnum.fromEntityName(mixedCaseName);

		// Then
		assertThat(result.getEntityName()).isEqualTo(mixedCaseName.toLowerCase());
	}

	@Test
	@DisplayName("fromEntityName should resolve mixed character casing")
	void testFromEntityNameWithMixedCasingReturnsCompany() {
		// When
		EntityTypeEnum result = EntityTypeEnum.fromEntityName(EntityTypeEnumTestDataFactory.MIXED_CASE_COMPANY_NAME);

		// Then
		assertThat(result).isEqualTo(EntityTypeEnum.COMPANY);
	}

	@Test
	@DisplayName("fromEntityName should throw when name is unknown")
	void testFromEntityNameWithUnknownNameThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> EntityTypeEnum.fromEntityName(EntityTypeEnumTestDataFactory.INVALID_ENTITY_NAME))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No EntityType found for entity name:")
			.hasMessageContaining(EntityTypeEnumTestDataFactory.INVALID_ENTITY_NAME);
	}

	@ParameterizedTest
	@NullSource
	@DisplayName("fromEntityName should throw when name is null")
	void testFromEntityNameWithNullThrowsIllegalArgumentException(String entityName) {
		// When & Then
		assertThatThrownBy(() -> EntityTypeEnum.fromEntityName(entityName)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No EntityType found for entity name:")
			.hasMessageContaining("null");
	}

}
