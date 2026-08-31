package io.recruitcrm.microservice.timesheet.dto.jobs;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidSubmissionReminderWindowValidator
		implements ConstraintValidator<ValidSubmissionReminderWindow, TimesheetSubmissionReminderJobRequestBodyDto> {

	@Override
	public boolean isValid(TimesheetSubmissionReminderJobRequestBodyDto value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		return value.from().isBefore(value.to());
	}

}
