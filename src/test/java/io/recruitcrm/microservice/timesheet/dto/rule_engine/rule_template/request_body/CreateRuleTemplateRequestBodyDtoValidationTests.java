package io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body;

import io.recruitcrm.microservice.timesheet.testdata.RuleTemplateTestDataFactory;
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
 * Validation tests for CreateRuleTemplateRequestBodyDto constraint annotations.
 */
class CreateRuleTemplateRequestBodyDtoValidationTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		this.validator = factory.getValidator();
	}

	@Test
	@DisplayName("Valid DTO has no violations")
	void validDtoHasNoViolations() {
		CreateRuleTemplateRequestBodyDto dto = RuleTemplateTestDataFactory.createRuleTemplateRequest();

		Set<ConstraintViolation<CreateRuleTemplateRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("calculateBreakTime false is valid")
	void calculateBreakTimeFalseIsValid() {
		CreateRuleTemplateRequestBodyDto dto = RuleTemplateTestDataFactory.createRuleTemplateRequest();
		dto.setCalculateBreakTime(false);

		Set<ConstraintViolation<CreateRuleTemplateRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("calculateBreakTime true produces violation")
	void calculateBreakTimeTrueProducesViolation() {
		CreateRuleTemplateRequestBodyDto dto = RuleTemplateTestDataFactory.createRuleTemplateRequest();
		dto.setCalculateBreakTime(true);

		Set<ConstraintViolation<CreateRuleTemplateRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("calculateBreakTime must be false (0)");
	}

	@Test
	@DisplayName("calculateBreakTime null is valid because field is optional")
	void calculateBreakTimeNullIsValid() {
		CreateRuleTemplateRequestBodyDto dto = RuleTemplateTestDataFactory.createRuleTemplateRequest();
		dto.setCalculateBreakTime(null);

		Set<ConstraintViolation<CreateRuleTemplateRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("workDayIds cannot be null")
	void workDayIdsCannotBeNull() {
		CreateRuleTemplateRequestBodyDto dto = RuleTemplateTestDataFactory.createRuleTemplateRequest();
		dto.setWorkDayIds(null);

		Set<ConstraintViolation<CreateRuleTemplateRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Work days cannot be null");
	}

	@Test
	@DisplayName("templateName cannot be blank")
	void templateNameCannotBeBlank() {
		CreateRuleTemplateRequestBodyDto dto = RuleTemplateTestDataFactory.createRuleTemplateRequest();
		dto.setTemplateName("");

		Set<ConstraintViolation<CreateRuleTemplateRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("Template name is required");
	}

}
