package io.recruitcrm.microservice.timesheet.dto.approver;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtLeastOneApproverValidator Tests")
class AtLeastOneApproverValidatorTests {

	private AtLeastOneApproverValidator validator;

	@Mock
	private ConstraintValidatorContext context;

	@BeforeEach
	void setUp() {
		this.validator = new AtLeastOneApproverValidator();
	}

	@ParameterizedTest(name = "Valid={2} when agencyIds={0} and clientIds={1}")
	@MethodSource("approverIdCombinations")
	@DisplayName("Covers all null/empty/non-empty combinations for agencyIds and clientIds")
	void testIsValidCoversAllListCombinations(List<Integer> agencyIds, List<Integer> clientIds, boolean expectedValid) {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(agencyIds);
		dto.setClientIds(clientIds);

		// Act
		boolean result = this.validator.isValid(dto, this.context);

		// Assert
		assertThat(result).isEqualTo(expectedValid);
	}

	private static Stream<Arguments> approverIdCombinations() {
		List<Integer> nonEmptyList = List.of(1);
		List<Integer> nonEmptyListWithNullElement = Arrays.asList((Integer) null);

		return Stream.of(Arguments.of(null, null, false),
				Arguments.of(Collections.emptyList(), Collections.emptyList(), false),
				Arguments.of(Collections.emptyList(), null, false), Arguments.of(null, Collections.emptyList(), false),

				Arguments.of(nonEmptyList, Collections.emptyList(), true),
				Arguments.of(Collections.emptyList(), nonEmptyList, true), Arguments.of(nonEmptyList, null, true),
				Arguments.of(null, nonEmptyList, true), Arguments.of(nonEmptyList, nonEmptyList, true),

				Arguments.of(nonEmptyListWithNullElement, Collections.emptyList(), true),
				Arguments.of(Collections.emptyList(), nonEmptyListWithNullElement, true),
				Arguments.of(nonEmptyListWithNullElement, null, true),
				Arguments.of(null, nonEmptyListWithNullElement, true),
				Arguments.of(nonEmptyListWithNullElement, nonEmptyListWithNullElement, true));
	}

	@Test
	@DisplayName("Valid when null value is passed")
	void testIsValidNullValueReturnsTrue() {
		// Act
		boolean result = this.validator.isValid(null, this.context);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Valid when both agencyIds and clientIds have values")
	void testIsValidBothListsHaveValuesReturnsTrue() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(Arrays.asList(1, 2));
		dto.setClientIds(Arrays.asList(3, 4));

		// Act
		boolean result = this.validator.isValid(dto, this.context);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Valid when only agencyIds has values")
	void testIsValidOnlyAgencyIdsHasValuesReturnsTrue() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(Arrays.asList(1, 2));
		dto.setClientIds(Collections.emptyList());

		// Act
		boolean result = this.validator.isValid(dto, this.context);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Valid when only clientIds has values")
	void testIsValidOnlyClientIdsHasValuesReturnsTrue() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(Collections.emptyList());
		dto.setClientIds(Arrays.asList(3, 4));

		// Act
		boolean result = this.validator.isValid(dto, this.context);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Valid when agencyIds has values and clientIds is null")
	void testIsValidAgencyIdsHasValuesClientIdsNullReturnsTrue() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(List.of(1));
		dto.setClientIds(null);

		// Act
		boolean result = this.validator.isValid(dto, this.context);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Valid when clientIds has values and agencyIds is null")
	void testIsValidClientIdsHasValuesAgencyIdsNullReturnsTrue() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(null);
		dto.setClientIds(List.of(3));

		// Act
		boolean result = this.validator.isValid(dto, this.context);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Invalid when both agencyIds and clientIds are empty")
	void testIsValidBothListsEmptyReturnsFalse() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(Collections.emptyList());
		dto.setClientIds(Collections.emptyList());

		// Act
		boolean result = this.validator.isValid(dto, this.context);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Invalid when both agencyIds and clientIds are null")
	void testIsValidBothListsNullReturnsFalse() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(null);
		dto.setClientIds(null);

		// Act
		boolean result = this.validator.isValid(dto, this.context);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Invalid when agencyIds is empty and clientIds is null")
	void testIsValidAgencyIdsEmptyClientIdsNullReturnsFalse() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(Collections.emptyList());
		dto.setClientIds(null);

		// Act
		boolean result = this.validator.isValid(dto, this.context);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Invalid when agencyIds is null and clientIds is empty")
	void testIsValidAgencyIdsNullClientIdsEmptyReturnsFalse() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(null);
		dto.setClientIds(Collections.emptyList());

		// Act
		boolean result = this.validator.isValid(dto, this.context);

		// Assert
		assertThat(result).isFalse();
	}

}
