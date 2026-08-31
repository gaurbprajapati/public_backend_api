package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Job;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasePermissionChecker Tests - New Helper Methods")
class BasePermissionCheckerTests {

	@Mock
	private AuthHolder authHolder;

	@Mock
	private TimesheetRepository timesheetRepository;

	@Mock
	private AccessControlChecker accessControlChecker;

	@Test
	@DisplayName("getJobForTimesheet should return job when job exists")
	void testGetJobForTimesheetJobExistsReturnsJob() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;
		Job job = new Job();
		job.setId(1);
		job.setOwnerId(50);

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getJobLinkedToTimesheet(timesheetId, accountId)).willReturn(job);

		// When
		Job result = checker.getJobForTimesheet(timesheetId);

		// Then
		assertThat(result).isNotNull().isEqualTo(job);
		assertThat(result.getId()).isEqualTo(Integer.valueOf(1));
	}

	@Test
	@DisplayName("getJobForTimesheet should throw exception when job is null")
	void testGetJobForTimesheetJobNullThrowsException() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getJobLinkedToTimesheet(timesheetId, accountId)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> checker.getJobForTimesheet(timesheetId))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Job not found for timesheet ID: 1");
	}

	@Test
	@DisplayName("getJobForTimesheetOrNull should return job when job exists")
	void testGetJobForTimesheetOrNullJobExistsReturnsJob() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;
		Job job = new Job();
		job.setId(1);
		job.setOwnerId(50);

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getJobLinkedToTimesheet(timesheetId, accountId)).willReturn(job);

		// When
		Job result = checker.getJobForTimesheetOrNull(timesheetId);

		// Then
		assertThat(result).isNotNull().isEqualTo(job);
		assertThat(result.getId()).isEqualTo(Integer.valueOf(1));
	}

	@Test
	@DisplayName("getJobForTimesheetOrNull should return null when job is null without throwing exception")
	void testGetJobForTimesheetOrNullJobNullReturnsNull() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getJobLinkedToTimesheet(timesheetId, accountId)).willReturn(null);

		// When
		Job result = checker.getJobForTimesheetOrNull(timesheetId);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getCandidateForTimesheetOrNull should return candidate when candidate exists")
	void testGetCandidateForTimesheetOrNullCandidateExistsReturnsCandidate() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;
		Candidate candidate = new Candidate();
		candidate.setId(1);
		candidate.setOwnerId(50);

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		// When
		Candidate result = checker.getCandidateForTimesheetOrNull(timesheetId);

		// Then
		assertThat(result).isNotNull().isEqualTo(candidate);
		assertThat(result.getId()).isEqualTo(Integer.valueOf(1));
	}

	@Test
	@DisplayName("getCandidateForTimesheetOrNull should return null when candidate is null")
	void testGetCandidateForTimesheetOrNullCandidateNullReturnsNull() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(null);

		// When
		Candidate result = checker.getCandidateForTimesheetOrNull(timesheetId);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getJobForTimesheetFromBulkContext should return job when job exists in bulk context")
	void testGetJobForTimesheetFromBulkContextJobExistsReturnsJob() {
		// Given
		Integer timesheetId = 1;
		Job job = new Job();
		job.setId(1);
		job.setOwnerId(50);

		Map<Integer, Job> jobsByTimesheetId = new HashMap<>();
		jobsByTimesheetId.put(timesheetId, job);

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.jobsByTimesheetId(jobsByTimesheetId)
			.build();

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		// When
		Job result = checker.getJobForTimesheetFromBulkContext(timesheetId, bulkContext);

		// Then
		assertThat(result).isNotNull().isEqualTo(job);
		assertThat(result.getId()).isEqualTo(Integer.valueOf(1));
	}

	@Test
	@DisplayName("getJobForTimesheetFromBulkContext should throw exception when job is null")
	void testGetJobForTimesheetFromBulkContextJobNullThrowsException() {
		// Given
		Integer timesheetId = 1;

		Map<Integer, Job> jobsByTimesheetId = new HashMap<>();
		// No job for this timesheetId

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.jobsByTimesheetId(jobsByTimesheetId)
			.build();

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		// When & Then
		assertThatThrownBy(() -> checker.getJobForTimesheetFromBulkContext(timesheetId, bulkContext))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Job not found for timesheet ID: 1");
	}

	@Test
	@DisplayName("getJobForTimesheetFromBulkContextOrNull should return job when job exists")
	void testGetJobForTimesheetFromBulkContextOrNullJobExistsReturnsJob() {
		// Given
		Integer timesheetId = 1;
		Job job = new Job();
		job.setId(1);
		job.setOwnerId(50);

		Map<Integer, Job> jobsByTimesheetId = new HashMap<>();
		jobsByTimesheetId.put(timesheetId, job);

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.jobsByTimesheetId(jobsByTimesheetId)
			.build();

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		// When
		Job result = checker.getJobForTimesheetFromBulkContextOrNull(timesheetId, bulkContext);

		// Then
		assertThat(result).isNotNull().isEqualTo(job);
		assertThat(result.getId()).isEqualTo(Integer.valueOf(1));
	}

	@Test
	@DisplayName("getJobForTimesheetFromBulkContextOrNull should return null when job is null without throwing exception")
	void testGetJobForTimesheetFromBulkContextOrNullJobNullReturnsNull() {
		// Given
		Integer timesheetId = 1;

		Map<Integer, Job> jobsByTimesheetId = new HashMap<>();
		// No job for this timesheetId

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.jobsByTimesheetId(jobsByTimesheetId)
			.build();

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		// When
		Job result = checker.getJobForTimesheetFromBulkContextOrNull(timesheetId, bulkContext);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getCandidateForTimesheetFromBulkContext should return candidate when candidate exists")
	void testGetCandidateForTimesheetFromBulkContextCandidateExistsReturnsCandidate() {
		// Given
		Integer timesheetId = 1;
		Candidate candidate = new Candidate();
		candidate.setId(1);
		candidate.setOwnerId(50);

		Map<Integer, Candidate> candidatesByTimesheetId = new HashMap<>();
		candidatesByTimesheetId.put(timesheetId, candidate);

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.candidatesByTimesheetId(candidatesByTimesheetId)
			.build();

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		// When
		Candidate result = checker.getCandidateForTimesheetFromBulkContext(timesheetId, bulkContext);

		// Then
		assertThat(result).isNotNull().isEqualTo(candidate);
		assertThat(result.getId()).isEqualTo(Integer.valueOf(1));
	}

	@Test
	@DisplayName("checkPermission convenience overload should build context and delegate to checkPermission")
	void testCheckPermissionConvenienceOverloadBuildsContextAndDelegates() {
		// Given
		Integer timesheetId = 1;
		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		// When & Then - the test implementation has an empty body so it should not throw
		assertThatCode(() -> checker.checkPermission(
				io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity.TIMESHEET,
				io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission.APPROVE_TIMESHEET,
				io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel.YES, timesheetId))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("checkPermissionWithBulkContext default implementation should fall back to checkPermission")
	void testCheckPermissionWithBulkContextDefaultFallsBackToCheckPermission() {
		// Given
		Integer timesheetId = 1;
		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder().build();
		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity.TIMESHEET)
			.permission(
					io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission.APPROVE_TIMESHEET)
			.permissionLevel(io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		// When & Then - default impl delegates to the empty-bodied checkPermission
		assertThatCode(() -> checker.checkPermissionWithBulkContext(context, bulkContext)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("getCandidateForTimesheetFromBulkContext should throw exception when candidate is null")
	void testGetCandidateForTimesheetFromBulkContextCandidateNullThrowsException() {
		// Given
		Integer timesheetId = 1;

		Map<Integer, Candidate> candidatesByTimesheetId = new HashMap<>();
		// No candidate for this timesheetId

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.candidatesByTimesheetId(candidatesByTimesheetId)
			.build();

		TestPermissionChecker checker = new TestPermissionChecker(this.authHolder, this.timesheetRepository,
				this.accessControlChecker);

		// When & Then
		assertThatThrownBy(() -> checker.getCandidateForTimesheetFromBulkContext(timesheetId, bulkContext))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Candidate not found for timesheet ID: 1");
	}

	/**
	 * Concrete implementation of BasePermissionChecker for testing purposes Inner class
	 * placed at end to comply with InnerTypeLast checkstyle rule
	 */
	private static class TestPermissionChecker extends BasePermissionChecker {

		protected TestPermissionChecker(AuthHolder authHolder, TimesheetRepository timesheetRepository,
				AccessControlChecker accessControlChecker) {
			super(authHolder, timesheetRepository, accessControlChecker);
		}

		@Override
		public void checkPermission(@NonNull PermissionCheckInternalContext context) {
			// Test implementation
		}

	}

}
