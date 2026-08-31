package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import io.recruitcrm.microservice.timesheet.testdata.ReimbursementTestDataFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validation tests for UpdateReimbursementStatusRequestBodyDto constraint annotations.
 */
class UpdateReimbursementStatusRequestBodyDtoValidationTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		this.validator = factory.getValidator();
	}

	static Stream<UpdateReimbursementStatusRequestBodyDto> validDtoProvider() {
		return Stream.of(
				new UpdateReimbursementStatusRequestBodyDto(ReimbursementTestDataFactory.getStatusApproved(),
						"Approved by manager"),
				new UpdateReimbursementStatusRequestBodyDto(ReimbursementTestDataFactory.getStatusRejected(),
						"Rejected due to policy"),
				new UpdateReimbursementStatusRequestBodyDto(ReimbursementTestDataFactory.getStatusApproved(), null));
	}

	@ParameterizedTest
	@MethodSource("validDtoProvider")
	@DisplayName("Valid DTO has no violations")
	void validDtoHasNoViolations(UpdateReimbursementStatusRequestBodyDto dto) {
		Set<ConstraintViolation<UpdateReimbursementStatusRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Status null produces violation")
	void statusNullProducesViolation() {
		UpdateReimbursementStatusRequestBodyDto dto = new UpdateReimbursementStatusRequestBodyDto(null, "Some remark");

		Set<ConstraintViolation<UpdateReimbursementStatusRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Status ID cannot be null");
	}

	@Test
	@DisplayName("Remark exceeds max length produces violation")
	void remarkExceedsMaxLengthProducesViolation() {
		UpdateReimbursementStatusRequestBodyDto dto = new UpdateReimbursementStatusRequestBodyDto(
				ReimbursementTestDataFactory.getStatusApproved(), "a".repeat(1001));

		Set<ConstraintViolation<UpdateReimbursementStatusRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Remark must not exceed 1000 characters");
	}

	@Test
	@DisplayName("Remark at max length has no violations")
	void remarkAtMaxLengthHasNoViolations() {
		UpdateReimbursementStatusRequestBodyDto dto = new UpdateReimbursementStatusRequestBodyDto(
				ReimbursementTestDataFactory.getStatusApproved(), "a".repeat(1000));

		Set<ConstraintViolation<UpdateReimbursementStatusRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Remark empty string has no violations")
	void remarkEmptyStringHasNoViolations() {
		UpdateReimbursementStatusRequestBodyDto dto = new UpdateReimbursementStatusRequestBodyDto(
				ReimbursementTestDataFactory.getStatusApproved(), "");

		Set<ConstraintViolation<UpdateReimbursementStatusRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Multiple validation errors produces multiple violations")
	void multipleValidationErrorsProducesMultipleViolations() {
		UpdateReimbursementStatusRequestBodyDto dto = new UpdateReimbursementStatusRequestBodyDto(null,
				"a".repeat(1001));

		Set<ConstraintViolation<UpdateReimbursementStatusRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).hasSize(2);
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Status ID cannot be null", "Remark must not exceed 1000 characters");
	}

}
