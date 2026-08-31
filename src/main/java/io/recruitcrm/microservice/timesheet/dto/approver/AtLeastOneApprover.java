package io.recruitcrm.microservice.timesheet.dto.approver;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AtLeastOneApproverValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneApprover {

	String message() default "At least one approver must be provided in agencyIds or clientIds";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
