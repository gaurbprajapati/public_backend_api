package io.recruitcrm.microservice.timesheet.dto.jobs;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidSubmissionReminderWindowValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSubmissionReminderWindow {

	String message() default "from must be strictly before to";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
