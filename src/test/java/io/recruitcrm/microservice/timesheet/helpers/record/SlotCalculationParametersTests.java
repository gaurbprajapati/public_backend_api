package io.recruitcrm.microservice.timesheet.helpers.record;

import io.recruitcrm.microservice.timesheet.testdata.SlotCalculationParametersTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SlotCalculationParameters Tests")
class SlotCalculationParametersTests {

	private static final int CUSTOM_SLOT_SECONDS = 43200;

	@Test
	@DisplayName("record should expose slot seconds from default test data factory")
	void testRecordAccessorWithDefaultFactoryData() {
		// Given
		SlotCalculationParameters parameters = SlotCalculationParametersTestDataFactory.createParameters();

		// When and Then
		assertThat(parameters.slotSeconds()).isEqualTo(SlotCalculationParametersTestDataFactory.DEFAULT_SLOT_SECONDS);
	}

	@Test
	@DisplayName("record should support value equality and consistent hash code")
	void testRecordEqualityAndHashCode() {
		// Given
		SlotCalculationParameters left = SlotCalculationParametersTestDataFactory.createParameters(CUSTOM_SLOT_SECONDS);
		SlotCalculationParameters right = SlotCalculationParametersTestDataFactory
			.createParameters(CUSTOM_SLOT_SECONDS);

		// When and Then
		assertThat(left).isEqualTo(right).hasSameHashCodeAs(right);
	}

	@Test
	@DisplayName("record toString should include component name and value")
	void testRecordToStringContainsComponent() {
		// Given
		SlotCalculationParameters parameters = SlotCalculationParametersTestDataFactory
			.createParameters(CUSTOM_SLOT_SECONDS);

		// When
		String result = parameters.toString();

		// Then
		assertThat(result).contains("slotSeconds=" + CUSTOM_SLOT_SECONDS);
	}

}
