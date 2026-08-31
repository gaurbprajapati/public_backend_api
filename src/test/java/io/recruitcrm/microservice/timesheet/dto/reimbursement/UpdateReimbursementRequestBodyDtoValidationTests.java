package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import io.recruitcrm.microservice.timesheet.testdata.ReimbursementTestDataFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validation tests for UpdateReimbursementRequestBodyDto constraint annotations.
 */
class UpdateReimbursementRequestBodyDtoValidationTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		this.validator = factory.getValidator();
	}

	@Test
	@DisplayName("Valid DTO with all fields has no violations")
	void validDtoWithAllFieldsHasNoViolations() {
		UpdateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.updateReimbursementRequest();

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Valid DTO with partial fields has no violations")
	void validDtoWithPartialFieldsHasNoViolations() {
		UpdateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.updateReimbursementRequestPartial();

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Valid DTO with only amount has no violations")
	void validDtoWithOnlyAmountHasNoViolations() {
		UpdateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.updateReimbursementRequestAmountOnly();

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Empty DTO has no violations since all fields are optional")
	void emptyDtoHasNoViolations() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Description blank produces violation")
	void descriptionBlankProducesViolation() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setDescription("");

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Description must be between 1 and 100 characters");
	}

	@Test
	@DisplayName("Description exceeds max length produces violation")
	void descriptionExceedsMaxLengthProducesViolation() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setDescription("a".repeat(101));

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Description must be between 1 and 100 characters");
	}

	@Test
	@DisplayName("Description at max length has no violations")
	void descriptionAtMaxLengthHasNoViolations() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setDescription("a".repeat(100));

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Description at min length has no violations")
	void descriptionAtMinLengthHasNoViolations() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setDescription("a");

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Amount zero produces violation")
	void amountZeroProducesViolation() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setAmount(BigDecimal.ZERO);

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Amount must be greater than 0");
	}

	@Test
	@DisplayName("Amount negative produces violation")
	void amountNegativeProducesViolation() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setAmount(new BigDecimal("-1.00"));

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Amount must be greater than 0");
	}

	@Test
	@DisplayName("Amount positive has no violations")
	void amountPositiveHasNoViolations() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setAmount(new BigDecimal("0.01"));

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Amount exceeds max value produces violation")
	void amountExceedsMaxValueProducesViolation() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setAmount(new BigDecimal("10000000000"));

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Amount must not exceed 9999999999");
	}

	@Test
	@DisplayName("Amount at max value has no violations")
	void amountAtMaxValueHasNoViolations() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setAmount(new BigDecimal("9999999999"));

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("DocumentToken exceeds max length produces violation")
	void documentTokenExceedsMaxLengthProducesViolation() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setDocumentToken("https://example.com/" + "a".repeat(1000));

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Document Token must not exceed 1000 characters");
	}

	@Test
	@DisplayName("DocumentToken at max length has no violations")
	void documentTokenAtMaxLengthHasNoViolations() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setDocumentToken("a".repeat(1000));

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("DocumentToken null has no violations")
	void documentTokenNullHasNoViolations() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setDocumentToken(null);

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("FileName exceeds max length produces violation")
	void fileNameExceedsMaxLengthProducesViolation() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setFileName("a".repeat(256));

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("File name must not exceed 255 characters");
	}

	@Test
	@DisplayName("FileName at max length has no violations")
	void fileNameAtMaxLengthHasNoViolations() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setFileName("a".repeat(255));

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("FileName null has no violations")
	void fileNameNullHasNoViolations() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setFileName(null);

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Multiple validation errors produces multiple violations")
	void multipleValidationErrorsProducesMultipleViolations() {
		UpdateReimbursementRequestBodyDto dto = new UpdateReimbursementRequestBodyDto();
		dto.setDescription("");
		dto.setAmount(BigDecimal.ZERO);
		dto.setDocumentToken("a".repeat(1001));
		dto.setFileName("a".repeat(256));

		Set<ConstraintViolation<UpdateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).hasSize(4);
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Description must be between 1 and 100 characters",
				"Amount must be greater than 0", "Document Token must not exceed 1000 characters",
				"File name must not exceed 255 characters");
	}

}
