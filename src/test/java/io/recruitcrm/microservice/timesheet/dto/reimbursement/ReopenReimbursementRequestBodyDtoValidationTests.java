package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import io.recruitcrm.microservice.timesheet.testdata.ReimbursementTestDataFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validation tests for ReopenReimbursementRequestBodyDto constraint annotations.
 */
class ReopenReimbursementRequestBodyDtoValidationTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		this.validator = factory.getValidator();
	}

	@Test
	@DisplayName("Valid DTO with remark has no violations")
	void validDtoWithRemarkHasNoViolations() {
		ReopenReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReopenReimbursementRequest();

		Set<ConstraintViolation<ReopenReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("DTO without remark produces violation")
	void dtoWithoutRemarkProducesViolation() {
		ReopenReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReopenReimbursementRequestNoRemark();

		Set<ConstraintViolation<ReopenReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("remark: must not be blank");
	}

	@Test
	@DisplayName("DTO with empty remark produces violation")
	void dtoWithEmptyRemarkProducesViolation() {
		ReopenReimbursementRequestBodyDto dto = new ReopenReimbursementRequestBodyDto("");

		Set<ConstraintViolation<ReopenReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("remark: must not be blank");
	}

	@Test
	@DisplayName("Remark exceeds max length produces violation")
	void remarkExceedsMaxLengthProducesViolation() {
		ReopenReimbursementRequestBodyDto dto = new ReopenReimbursementRequestBodyDto("a".repeat(1001));

		Set<ConstraintViolation<ReopenReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("remark: size must be between 1 and 1000");
	}

	@Test
	@DisplayName("Remark at max length has no violations")
	void remarkAtMaxLengthHasNoViolations() {
		ReopenReimbursementRequestBodyDto dto = new ReopenReimbursementRequestBodyDto("a".repeat(1000));

		Set<ConstraintViolation<ReopenReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Remark at boundary 999 has no violations")
	void remarkAtBoundary999HasNoViolations() {
		ReopenReimbursementRequestBodyDto dto = new ReopenReimbursementRequestBodyDto("a".repeat(999));

		Set<ConstraintViolation<ReopenReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

}
