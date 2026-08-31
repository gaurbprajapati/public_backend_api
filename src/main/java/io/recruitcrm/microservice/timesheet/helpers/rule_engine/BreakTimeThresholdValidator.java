package io.recruitcrm.microservice.timesheet.helpers.rule_engine;

import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;

public final class BreakTimeThresholdValidator {

	private BreakTimeThresholdValidator() {
		// Private constructor to prevent instantiation of utility class
	}

	public static void validateBreakTimeThreshold(Boolean calculateBreakTime, Integer breakTimeThreshold) {
		// When calculateBreakTime is false (0), breakTimeThreshold is required
		if (Boolean.FALSE.equals(calculateBreakTime) && breakTimeThreshold == null) {
			throw new ValidationErrorException(
					"Break time threshold is required when calculate break time is disabled");
		}
	}

}
