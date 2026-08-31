package io.recruitcrm.microservice.timesheet.services.export;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.microservice.timesheet.testdata.WorkDaysConverterTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for WorkDaysConverter. Tests work days JSON conversion scenarios using real
 * ObjectMapper and comprehensive coverage for all public methods and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkDaysConverter Tests")
class WorkDaysConverterTests {

	private WorkDaysConverter workDaysConverter;

	@BeforeEach
	void setUp() {
		// Use real ObjectMapper for testing since TypeReference mocking is complex
		this.workDaysConverter = new WorkDaysConverter(new ObjectMapper());
	}

	@Test
	@DisplayName("Convert work days to names should return single day name for valid single day JSON")
	void testConvertWorkDaysToNamesValidSingleDayJsonReturnsSingleDayName() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createValidSingleDayJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_SINGLE_DAY;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should return multiple day names for valid multiple days JSON")
	void testConvertWorkDaysToNamesValidMultipleDaysJsonReturnsMultipleDayNames() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createValidMultipleDaysJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_MULTIPLE_DAYS;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should return all day names for valid all days JSON")
	void testConvertWorkDaysToNamesValidAllDaysJsonReturnsAllDayNames() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createValidAllDaysJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_ALL_DAYS;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should return weekend day names for valid weekend days JSON")
	void testConvertWorkDaysToNamesValidWeekendDaysJsonReturnsWeekendDayNames() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createValidWeekendDaysJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_WEEKEND_DAYS;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should handle string day IDs correctly")
	void testConvertWorkDaysToNamesStringDayIdsHandlesCorrectly() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createValidStringDayIdsJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_STRING_DAY_IDS;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should return empty string for null JSON")
	void testConvertWorkDaysToNamesNullJsonReturnsEmptyString() {
		// Given
		String workDaysJson = null;
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_EMPTY_RESULT;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should return empty string for empty JSON")
	void testConvertWorkDaysToNamesEmptyJsonReturnsEmptyString() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createEmptyJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_EMPTY_RESULT;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should return empty string for whitespace JSON")
	void testConvertWorkDaysToNamesWhitespaceJsonReturnsEmptyString() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createWhitespaceJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_EMPTY_RESULT;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should return empty string for empty array JSON")
	void testConvertWorkDaysToNamesEmptyArrayJsonReturnsEmptyString() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createEmptyArrayJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_EMPTY_RESULT;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should return empty string for invalid JSON")
	void testConvertWorkDaysToNamesInvalidJsonReturnsEmptyString() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createInvalidJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_EMPTY_RESULT;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should filter out null work day IDs")
	void testConvertWorkDaysToNamesNullWorkDayIdsFiltersOut() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createNullWorkDayIdJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_EMPTY_RESULT;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should filter out missing work day IDs")
	void testConvertWorkDaysToNamesMissingWorkDayIdsFiltersOut() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createMissingWorkDayIdJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_EMPTY_RESULT;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should filter out invalid day IDs")
	void testConvertWorkDaysToNamesInvalidDayIdsFiltersOut() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createInvalidDayIdJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_EMPTY_RESULT;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Convert work days to names should filter out invalid day IDs and keep valid ones")
	void testConvertWorkDaysToNamesMixedValidInvalidFiltersInvalidKeepsValid() {
		// Given
		String workDaysJson = WorkDaysConverterTestDataFactory.createMixedValidInvalidJson();
		String expectedResult = WorkDaysConverterTestDataFactory.EXPECTED_MIXED_VALID_INVALID;

		// When
		String result = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);

		// Then
		assertThat(result).isEqualTo(expectedResult);
	}

}