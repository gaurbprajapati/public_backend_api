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
 * Validation tests for CreateReimbursementRequestBodyDto constraint annotations.
 */
class CreateReimbursementRequestBodyDtoValidationTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		this.validator = factory.getValidator();
	}

	@Test
	@DisplayName("Valid DTO has no violations")
	void validDtoHasNoViolations() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Valid DTO without file has no violations")
	void validDtoWithoutFileHasNoViolations() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequestWithoutFile();

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Description null produces violation")
	void descriptionNullProducesViolation() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setDescription(null);

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Description cannot be blank");
	}

	@Test
	@DisplayName("Description blank produces violation")
	void descriptionBlankProducesViolation() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setDescription("");

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Description cannot be blank");
	}

	@Test
	@DisplayName("Description exceeds max length produces violation")
	void descriptionExceedsMaxLengthProducesViolation() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setDescription("a".repeat(101));

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Description must not exceed 100 characters");
	}

	@Test
	@DisplayName("Description at max length has no violations")
	void descriptionAtMaxLengthHasNoViolations() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setDescription("a".repeat(100));

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Amount null produces violation")
	void amountNullProducesViolation() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setAmount(null);

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Amount cannot be null");
	}

	@Test
	@DisplayName("Amount zero produces violation")
	void amountZeroProducesViolation() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setAmount(BigDecimal.ZERO);

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Amount must be greater than 0");
	}

	@Test
	@DisplayName("Amount negative produces violation")
	void amountNegativeProducesViolation() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setAmount(new BigDecimal("-1.00"));

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Amount must be greater than 0");
	}

	@Test
	@DisplayName("Amount at minimum value has no violations")
	void amountAtMinimumValueHasNoViolations() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setAmount(new BigDecimal("0.01"));

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Amount exceeds max value produces violation")
	void amountExceedsMaxValueProducesViolation() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setAmount(new BigDecimal("10000000000"));

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Amount must not exceed 9999999999");
	}

	@Test
	@DisplayName("Amount at max value has no violations")
	void amountAtMaxValueHasNoViolations() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setAmount(new BigDecimal("9999999999"));

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("DocumentToken exceeds max length produces violation")
	void documentTokenExceedsMaxLengthProducesViolation() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setDocumentToken("https://example.com/" + "a".repeat(1000));

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Document Token must not exceed 1000 characters");
	}

	@Test
	@DisplayName("DocumentToken at max length has no violations")
	void documentTokenAtMaxLengthHasNoViolations() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setDocumentToken("a".repeat(1000));

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("DocumentToken null has no violations")
	void documentTokenNullHasNoViolations() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setDocumentToken(null);

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("FileName exceeds max length produces violation")
	void fileNameExceedsMaxLengthProducesViolation() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setFileName("a".repeat(256));

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("File name must not exceed 255 characters");
	}

	@Test
	@DisplayName("FileName at max length has no violations")
	void fileNameAtMaxLengthHasNoViolations() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setFileName("a".repeat(255));

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("FileName null has no violations")
	void fileNameNullHasNoViolations() {
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();
		dto.setFileName(null);

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("Multiple validation errors produces multiple violations")
	void multipleValidationErrorsProducesMultipleViolations() {
		CreateReimbursementRequestBodyDto dto = new CreateReimbursementRequestBodyDto();
		dto.setDescription(null);
		dto.setAmount(null);

		Set<ConstraintViolation<CreateReimbursementRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Description cannot be blank", "Amount cannot be null");
	}

}
