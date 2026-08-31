package io.recruitcrm.microservice.timesheet.services.export;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.testdata.PostProcessingRequirementsTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for PostProcessingRequirements record. Tests all methods and scenarios for
 * post-processing requirements using BDDMockito style and comprehensive coverage for all
 * public methods and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PostProcessingRequirements Tests")
class PostProcessingRequirementsTests {

	@Test
	@DisplayName("Has any processing should return true when all processing enabled")
	void testHasAnyProcessingAllProcessingEnabledReturnsTrue() {
		// Given
		PostProcessingRequirements requirements = PostProcessingRequirementsTestDataFactory
			.createAllProcessingEnabled();

		// When
		boolean result = requirements.hasAnyProcessing();

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Has any processing should return false when all processing disabled")
	void testHasAnyProcessingAllProcessingDisabledReturnsFalse() {
		// Given
		PostProcessingRequirements requirements = PostProcessingRequirementsTestDataFactory
			.createAllProcessingDisabled();

		// When
		boolean result = requirements.hasAnyProcessing();

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Has any processing should return true when only work days processing enabled")
	void testHasAnyProcessingOnlyWorkDaysProcessingEnabledReturnsTrue() {
		// Given
		PostProcessingRequirements requirements = PostProcessingRequirementsTestDataFactory
			.createOnlyWorkDaysProcessing();

		// When
		boolean result = requirements.hasAnyProcessing();

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Has any processing should return true when only resource URL processing enabled")
	void testHasAnyProcessingOnlyResourceUrlProcessingEnabledReturnsTrue() {
		// Given
		PostProcessingRequirements requirements = PostProcessingRequirementsTestDataFactory
			.createOnlyResourceUrlProcessing();

		// When
		boolean result = requirements.hasAnyProcessing();

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Has any processing should return true when only user field processing enabled")
	void testHasAnyProcessingOnlyUserFieldProcessingEnabledReturnsTrue() {
		// Given
		PostProcessingRequirements requirements = PostProcessingRequirementsTestDataFactory
			.createOnlyUserFieldProcessing();

		// When
		boolean result = requirements.hasAnyProcessing();

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Has any processing should return true when only custom column processing enabled")
	void testHasAnyProcessingOnlyCustomColumnProcessingEnabledReturnsTrue() {
		// Given
		PostProcessingRequirements requirements = PostProcessingRequirementsTestDataFactory
			.createOnlyCustomColumnProcessing();

		// When
		boolean result = requirements.hasAnyProcessing();

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Has any processing should return true when multiple processing types enabled")
	void testHasAnyProcessingMultipleProcessingTypesEnabledReturnsTrue() {
		// Given
		PostProcessingRequirements requirements = PostProcessingRequirementsTestDataFactory
			.createWorkDaysAndResourceUrlProcessing();

		// When
		boolean result = requirements.hasAnyProcessing();

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Needs work days processing should return correct value")
	void testNeedsWorkDaysProcessingReturnsCorrectValue() {
		// Given
		PostProcessingRequirements enabledRequirements = PostProcessingRequirementsTestDataFactory
			.createOnlyWorkDaysProcessing();
		PostProcessingRequirements disabledRequirements = PostProcessingRequirementsTestDataFactory
			.createAllProcessingDisabled();

		// When
		boolean enabledResult = enabledRequirements.needsWorkDaysProcessing();
		boolean disabledResult = disabledRequirements.needsWorkDaysProcessing();

		// Then
		assertThat(enabledResult).isTrue();
		assertThat(disabledResult).isFalse();
	}

	@Test
	@DisplayName("Needs resource URL processing should return correct value")
	void testNeedsResourceUrlProcessingReturnsCorrectValue() {
		// Given
		PostProcessingRequirements enabledRequirements = PostProcessingRequirementsTestDataFactory
			.createOnlyResourceUrlProcessing();
		PostProcessingRequirements disabledRequirements = PostProcessingRequirementsTestDataFactory
			.createAllProcessingDisabled();

		// When
		boolean enabledResult = enabledRequirements.needsResourceUrlProcessing();
		boolean disabledResult = disabledRequirements.needsResourceUrlProcessing();

		// Then
		assertThat(enabledResult).isTrue();
		assertThat(disabledResult).isFalse();
	}

	@Test
	@DisplayName("Needs user field processing should return correct value")
	void testNeedsUserFieldProcessingReturnsCorrectValue() {
		// Given
		PostProcessingRequirements enabledRequirements = PostProcessingRequirementsTestDataFactory
			.createOnlyUserFieldProcessing();
		PostProcessingRequirements disabledRequirements = PostProcessingRequirementsTestDataFactory
			.createAllProcessingDisabled();

		// When
		boolean enabledResult = enabledRequirements.needsUserFieldProcessing();
		boolean disabledResult = disabledRequirements.needsUserFieldProcessing();

		// Then
		assertThat(enabledResult).isTrue();
		assertThat(disabledResult).isFalse();
	}

	@Test
	@DisplayName("Needs custom column processing should return correct value")
	void testNeedsCustomColumnProcessingReturnsCorrectValue() {
		// Given
		PostProcessingRequirements enabledRequirements = PostProcessingRequirementsTestDataFactory
			.createOnlyCustomColumnProcessing();
		PostProcessingRequirements disabledRequirements = PostProcessingRequirementsTestDataFactory
			.createAllProcessingDisabled();

		// When
		boolean enabledResult = enabledRequirements.needsCustomColumnProcessing();
		boolean disabledResult = disabledRequirements.needsCustomColumnProcessing();

		// Then
		assertThat(enabledResult).isTrue();
		assertThat(disabledResult).isFalse();
	}

	@Test
	@DisplayName("Record should have correct values for all processing enabled")
	void testRecordAllProcessingEnabledHasCorrectValues() {
		// Given
		PostProcessingRequirements requirements = PostProcessingRequirementsTestDataFactory
			.createAllProcessingEnabled();

		// When & Then
		assertThat(requirements.needsWorkDaysProcessing()).isTrue();
		assertThat(requirements.needsResourceUrlProcessing()).isTrue();
		assertThat(requirements.needsUserFieldProcessing()).isTrue();
		assertThat(requirements.needsCustomColumnProcessing()).isTrue();
		assertThat(requirements.hasAnyProcessing()).isTrue();
	}

	@Test
	@DisplayName("Record should have correct values for all processing disabled")
	void testRecordAllProcessingDisabledHasCorrectValues() {
		// Given
		PostProcessingRequirements requirements = PostProcessingRequirementsTestDataFactory
			.createAllProcessingDisabled();

		// When & Then
		assertThat(requirements.needsWorkDaysProcessing()).isFalse();
		assertThat(requirements.needsResourceUrlProcessing()).isFalse();
		assertThat(requirements.needsUserFieldProcessing()).isFalse();
		assertThat(requirements.needsCustomColumnProcessing()).isFalse();
		assertThat(requirements.hasAnyProcessing()).isFalse();
	}

	@Test
	@DisplayName("Record should have correct values for mixed processing requirements")
	void testRecordMixedProcessingRequirementsHasCorrectValues() {
		// Given
		PostProcessingRequirements requirements = PostProcessingRequirementsTestDataFactory
			.createUserFieldAndCustomColumnProcessing();

		// When & Then
		assertThat(requirements.needsWorkDaysProcessing()).isFalse();
		assertThat(requirements.needsResourceUrlProcessing()).isFalse();
		assertThat(requirements.needsUserFieldProcessing()).isTrue();
		assertThat(requirements.needsCustomColumnProcessing()).isTrue();
		assertThat(requirements.hasAnyProcessing()).isTrue();
	}

	@Test
	@DisplayName("Record should have correct values for three processing types enabled")
	void testRecordThreeProcessingTypesEnabledHasCorrectValues() {
		// Given
		PostProcessingRequirements requirements = PostProcessingRequirementsTestDataFactory
			.createThreeProcessingTypesEnabled();

		// When & Then
		assertThat(requirements.needsWorkDaysProcessing()).isTrue();
		assertThat(requirements.needsResourceUrlProcessing()).isTrue();
		assertThat(requirements.needsUserFieldProcessing()).isTrue();
		assertThat(requirements.needsCustomColumnProcessing()).isFalse();
		assertThat(requirements.hasAnyProcessing()).isTrue();
	}

	@Test
	@DisplayName("Record equality should work correctly")
	void testRecordEqualityWorksCorrectly() {
		// Given
		PostProcessingRequirements requirements1 = PostProcessingRequirementsTestDataFactory
			.createAllProcessingEnabled();
		PostProcessingRequirements requirements2 = PostProcessingRequirementsTestDataFactory
			.createAllProcessingEnabled();
		PostProcessingRequirements requirements3 = PostProcessingRequirementsTestDataFactory
			.createAllProcessingDisabled();

		// When & Then
		assertThat(requirements1).isEqualTo(requirements2).isNotEqualTo(requirements3).hasSameHashCodeAs(requirements2);
		assertThat(requirements1.hashCode()).isNotEqualTo(requirements3.hashCode());
	}

	@Test
	@DisplayName("Record toString should work correctly")
	void testRecordToStringWorksCorrectly() {
		// Given
		PostProcessingRequirements requirements = PostProcessingRequirementsTestDataFactory
			.createAllProcessingEnabled();

		// When
		String result = requirements.toString();

		// Then
		assertThat(result).isNotNull().contains("PostProcessingRequirements").contains("true");
	}

}
