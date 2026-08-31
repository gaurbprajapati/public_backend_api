package io.recruitcrm.microservice.timesheet.dto.approver;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AtLeastOneApproverValidator
		implements ConstraintValidator<AtLeastOneApprover, ApproverRequestResponseBodyDto> {

	@Override
	public boolean isValid(ApproverRequestResponseBodyDto value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}

		boolean hasAgencyApprovers = value.getAgencyIds() != null && !value.getAgencyIds().isEmpty();
		boolean hasClientApprovers = value.getClientIds() != null && !value.getClientIds().isEmpty();

		return hasAgencyApprovers || hasClientApprovers;
	}

}
