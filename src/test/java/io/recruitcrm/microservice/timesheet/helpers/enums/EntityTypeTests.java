package io.recruitcrm.microservice.timesheet.helpers.enums;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.testdata.EntityTypeTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("EntityType Tests")
class EntityTypeTests {

	@Test
	@DisplayName("values should contain only the candidate entity type")
	void testValuesContainsOnlyCandidateEntityType() {
		// When
		EntityType[] values = EntityType.values();

		// Then
		assertThat(values).containsExactly(EntityType.CANDIDATE);
	}

	@ParameterizedTest
	@EnumSource(EntityType.class)
	@DisplayName("getId should return configured database id for each enum constant")
	void testGetIdReturnsConfiguredIdForEachEnumConstant(EntityType entityType) {
		// Given
		int expectedId = EntityTypeTestDataFactory.CANDIDATE_ENTITY_TYPE_ID;

		// When
		int result = entityType.getId();

		// Then
		assertThat(result).isEqualTo(expectedId);
	}

	@Test
	@DisplayName("fromId should resolve candidate id to enum constant")
	void testFromIdWithCandidateIdReturnsCandidate() {
		// When
		EntityType result = EntityType.fromId(EntityTypeTestDataFactory.CANDIDATE_ENTITY_TYPE_ID);

		// Then
		assertThat(result).isEqualTo(EntityType.CANDIDATE);
	}

	@Test
	@DisplayName("fromId should return null when id is unknown")
	void testFromIdWithUnknownIdReturnsNull() {
		// When
		EntityType result = EntityType.fromId(EntityTypeTestDataFactory.UNKNOWN_ENTITY_TYPE_ID);

		// Then
		assertThat(result).isNull();
	}

}
