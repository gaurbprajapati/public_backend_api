package io.recruitcrm.microservice.timesheet.dto.timesheet_setting;

import io.recruitcrm.microservice.timesheet.testdata.TimesheetSettingTestDataFactory;
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
 * Validation tests for TimesheetSettingBulkRequestBodyDto constraint annotations.
 */
class TimesheetSettingBulkRequestBodyDtoValidationTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		this.validator = factory.getValidator();
	}

	@Test
	@DisplayName("Valid DTO has no violations including primitive payRate and billRate fields")
	void validDtoHasNoViolations() {
		TimesheetSettingBulkRequestBodyDto dto = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();

		Set<ConstraintViolation<TimesheetSettingBulkRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
		assertThat(dto.getPayRate()).isEqualTo(25.0f);
		assertThat(dto.getBillRate()).isEqualTo(30.0f);
	}

	@Test
	@DisplayName("workTime null is valid because field has no Bean Validation constraints")
	void workTimeNullIsValid() {
		TimesheetSettingBulkRequestBodyDto dto = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		dto.setWorkTime(null);

		Set<ConstraintViolation<TimesheetSettingBulkRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("workStartTime null is valid because field has no Bean Validation constraints")
	void workStartTimeNullIsValid() {
		TimesheetSettingBulkRequestBodyDto dto = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		dto.setWorkStartTime(null);

		Set<ConstraintViolation<TimesheetSettingBulkRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("calculateBreakTime null produces violation")
	void calculateBreakTimeNullProducesViolation() {
		TimesheetSettingBulkRequestBodyDto dto = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		dto.setCalculateBreakTime(null);

		Set<ConstraintViolation<TimesheetSettingBulkRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("calculateBreakTime is required and must be false");
	}

	@Test
	@DisplayName("calculateBreakTime false is valid")
	void calculateBreakTimeFalseIsValid() {
		TimesheetSettingBulkRequestBodyDto dto = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		dto.setCalculateBreakTime(false);

		Set<ConstraintViolation<TimesheetSettingBulkRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("calculateBreakTime true produces violation")
	void calculateBreakTimeTrueProducesViolation() {
		TimesheetSettingBulkRequestBodyDto dto = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		dto.setCalculateBreakTime(true);

		Set<ConstraintViolation<TimesheetSettingBulkRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isNotEmpty();
		Set<String> messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
		assertThat(messages).contains("calculateBreakTime must be false (0)");
	}

	@Test
	@DisplayName("workTime and workStartTime both null are valid")
	void workTimeAndWorkStartTimeBothNullAreValid() {
		TimesheetSettingBulkRequestBodyDto dto = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequestBodyDto();
		dto.setWorkTime(null);
		dto.setWorkStartTime(null);

		Set<ConstraintViolation<TimesheetSettingBulkRequestBodyDto>> violations = this.validator.validate(dto);

		assertThat(violations).isEmpty();
	}

}
