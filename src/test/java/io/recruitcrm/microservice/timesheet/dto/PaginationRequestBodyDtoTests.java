package io.recruitcrm.microservice.timesheet.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PaginationRequestBodyDto class.
 */
class PaginationRequestBodyDtoTests {

	@ParameterizedTest
	@CsvSource({ "2, 25, 1, 25", // Valid page and size
			", 25, 0, 25", // Null page
			"0, 25, 0, 25", // Zero page
			"-1, 25, 0, 25", // Negative page
			"2, , 1, 100" // Null size
	})
	@DisplayName("To pageable - Various page and size combinations")
	void toPageableVariousPageAndSizeCombinations(Integer page, Integer size, Integer expectedPageNumber,
			Integer expectedPageSize) {
		// Arrange
		PaginationRequestBodyDto dto = new PaginationRequestBodyDto(page, size);

		// Act
		Pageable pageable = dto.toPageable();

		// Assert
		assertThat(pageable.getPageNumber()).isEqualTo(expectedPageNumber);
		assertThat(pageable.getPageSize()).isEqualTo(expectedPageSize);
	}

	@ParameterizedTest
	@CsvSource({ "2, 0, 1, 100, 'Zero size'", "2, -5, 1, 100, 'Negative size'", "2, 100, 1, 100, 'Size equals default'",
			"2, 101, 1, 100, 'Size exceeds default'" })
	@DisplayName("To pageable - Size validation scenarios")
	void toPageableSizeValidationScenarios(Integer page, Integer size, Integer expectedPageNumber,
			Integer expectedPageSize, String scenario) {
		// Arrange
		PaginationRequestBodyDto dto = new PaginationRequestBodyDto(page, size);

		// Act
		Pageable pageable = dto.toPageable();

		// Assert
		assertThat(pageable.getPageNumber()).isEqualTo(expectedPageNumber);
		assertThat(pageable.getPageSize()).isEqualTo(expectedPageSize);
	}

	@Test
	@DisplayName("To pageable - Both null values")
	void toPageableBothNullValues() {
		// Arrange
		Integer page = null;
		Integer size = null;
		PaginationRequestBodyDto dto = new PaginationRequestBodyDto(page, size);

		// Act
		Pageable pageable = dto.toPageable();

		// Assert
		assertThat(pageable.getPageNumber()).isZero(); // default page
		assertThat(pageable.getPageSize()).isEqualTo(100); // default size
	}

	@Test
	@DisplayName("To pageable - Page one")
	void toPageablePageOne() {
		// Arrange
		Integer page = 1;
		Integer size = 25;
		PaginationRequestBodyDto dto = new PaginationRequestBodyDto(page, size);

		// Act
		Pageable pageable = dto.toPageable();

		// Assert
		assertThat(pageable.getPageNumber()).isZero(); // page - 1
		assertThat(pageable.getPageSize()).isEqualTo(25);
	}

	@Test
	@DisplayName("To pageable - Maximum valid size")
	void toPageableMaximumValidSize() {
		// Arrange
		Integer page = 2;
		Integer size = 100; // maximum valid size
		PaginationRequestBodyDto dto = new PaginationRequestBodyDto(page, size);

		// Act
		Pageable pageable = dto.toPageable();

		// Assert
		assertThat(pageable.getPageNumber()).isEqualTo(1); // page - 1
		assertThat(pageable.getPageSize()).isEqualTo(100);
	}

}