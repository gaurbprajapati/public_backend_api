package io.recruitcrm.microservice.timesheet.dto.approver;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApproverRequestResponseBodyDto Validation Tests")
class ApproverRequestResponseBodyDtoValidationTests {

	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			validator = factory.getValidator();
		}
	}

	@Test
	@DisplayName("No violations when both lists have approvers")
	void testValidationBothListsHaveApproversNoViolations() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(Arrays.asList(1, 2));
		dto.setClientIds(Arrays.asList(3, 4));

		// Act
		Set<ConstraintViolation<ApproverRequestResponseBodyDto>> violations = validator.validate(dto);

		// Assert
		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("No violations when only agencyIds has approvers")
	void testValidationOnlyAgencyIdsHasApproversNoViolations() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(Arrays.asList(1));
		dto.setClientIds(Collections.emptyList());

		// Act
		Set<ConstraintViolation<ApproverRequestResponseBodyDto>> violations = validator.validate(dto);

		// Assert
		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("No violations when only clientIds has approvers")
	void testValidationOnlyClientIdsHasApproversNoViolations() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(Collections.emptyList());
		dto.setClientIds(Arrays.asList(3));

		// Act
		Set<ConstraintViolation<ApproverRequestResponseBodyDto>> violations = validator.validate(dto);

		// Assert
		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Violation when both lists are empty")
	void testValidationBothListsEmptyHasViolation() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(Collections.emptyList());
		dto.setClientIds(Collections.emptyList());

		// Act
		Set<ConstraintViolation<ApproverRequestResponseBodyDto>> violations = validator.validate(dto);

		// Assert
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage())
			.isEqualTo("At least one approver must be provided in agencyIds or clientIds");
	}

	@Test
	@DisplayName("Violation when both lists are null")
	void testValidationBothListsNullHasViolation() {
		// Arrange
		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		dto.setAgencyIds(null);
		dto.setClientIds(null);

		// Act
		Set<ConstraintViolation<ApproverRequestResponseBodyDto>> violations = validator.validate(dto);

		// Assert
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage())
			.isEqualTo("At least one approver must be provided in agencyIds or clientIds");
	}

}
