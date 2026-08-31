package io.recruitcrm.microservice.timesheet.testdata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetEmailValidationRepository}
 * tests.
 */
public final class TimesheetEmailValidationRepositoryTestDataFactory {

	private TimesheetEmailValidationRepositoryTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getDefaultAccountId() {
		return 45;
	}

	public static Integer getEntityTypeContractor() {
		return 3;
	}

	public static Integer getEntityTypeNonContractor() {
		return 1;
	}

	public static List<Integer> createTimesheetIds() {
		return Arrays.asList(53981, 53982);
	}

	public static List<Integer> createEmptyTimesheetIds() {
		return Collections.emptyList();
	}

}
