package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.job.TimesheetEnabledAssignedCandidateQueryResultDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.repositories.job.JobContractorRepository}
 * tests.
 */
public final class JobContractorRepositoryTestDataFactory {

	public static final Integer DEFAULT_ACCOUNT_ID = 100;

	private JobContractorRepositoryTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static List<Integer> createJobIds() {
		return Arrays.asList(11, 22, 33);
	}

	public static List<Integer> createOwnedJobIds() {
		return Arrays.asList(11, 22);
	}

	public static List<Integer> createEmptyJobIds() {
		return Collections.emptyList();
	}

	public static TimesheetEnabledAssignedCandidateQueryResultDto createAssignedCandidate() {
		return new TimesheetEnabledAssignedCandidateQueryResultDto(1, 1001, "John", "Doe", "john.doe@example.com",
				"john-doe", 5, "Software Engineer", "Acme Corp", "1234567890", "profile.jpg", 11, 20240101, 20240131, 1,
				1);
	}

	public static List<TimesheetEnabledAssignedCandidateQueryResultDto> createAssignedCandidates() {
		TimesheetEnabledAssignedCandidateQueryResultDto candidateTwo = new TimesheetEnabledAssignedCandidateQueryResultDto(
				2, 1002, "Jane", "Smith", "jane.smith@example.com", "jane-smith", 6, "QA Engineer", "Beta Inc",
				"9876543210", "profile2.jpg", 22, 20240201, 20240228, 2, 2);
		return Arrays.asList(createAssignedCandidate(), candidateTwo);
	}

}
