package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Job;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimesheetApprovalPermissionChecker Tests")
class TimesheetApprovalPermissionCheckerTests {

	@Mock
	private AuthHolder authHolder;

	@Mock
	private TimesheetRepository timesheetRepository;

	@Mock
	private AccessControlChecker accessControlChecker;

	@InjectMocks
	private TimesheetApprovalPermissionChecker timesheetApprovalPermissionChecker;

	@Test
	@DisplayName("Check permission should perform access control checks when candidate and job exist")
	void testCheckPermissionCandidateAndJobExistPerformsAccessControlChecks() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;
		Integer userId = 10;
		Integer candidateOwnerId = 50;
		Integer jobOwnerId = 60;

		Candidate candidate = new Candidate();
		candidate.setId(1);
		candidate.setOwnerId(candidateOwnerId);

		Job job = new Job();
		job.setId(1);
		job.setOwnerId(jobOwnerId);

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.APPROVE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.timesheetRepository.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId()))
			.willReturn(true);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);
		given(this.timesheetRepository.getJobLinkedToTimesheet(timesheetId, accountId)).willReturn(job);
		willDoNothing().given(this.accessControlChecker)
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					anyInt());

		// When
		assertThatCode(() -> this.timesheetApprovalPermissionChecker.checkPermission(context))
			.doesNotThrowAnyException();

		// Then
		then(this.timesheetRepository).should()
			.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId());
		then(this.timesheetRepository).should().getCandidateLinkedToTimesheet(timesheetId, accountId);
		then(this.timesheetRepository).should().getJobLinkedToTimesheet(timesheetId, accountId);
		then(this.accessControlChecker).should()
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(candidateOwnerId));
		then(this.accessControlChecker).should()
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(jobOwnerId));
	}

	@Test
	@DisplayName("Check permission should bypass candidate check when candidate is null but check job")
	void testCheckPermissionCandidateNullBypassesCandidateCheckButChecksJob() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;
		Integer userId = 10;
		Integer jobOwnerId = 60;

		Job job = new Job();
		job.setId(1);
		job.setOwnerId(jobOwnerId);

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.APPROVE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.timesheetRepository.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId()))
			.willReturn(true);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(null);
		given(this.timesheetRepository.getJobLinkedToTimesheet(timesheetId, accountId)).willReturn(job);
		willDoNothing().given(this.accessControlChecker)
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(jobOwnerId));

		// When
		assertThatCode(() -> this.timesheetApprovalPermissionChecker.checkPermission(context))
			.doesNotThrowAnyException();

		// Then
		then(this.timesheetRepository).should()
			.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId());
		then(this.timesheetRepository).should().getCandidateLinkedToTimesheet(timesheetId, accountId);
		then(this.timesheetRepository).should().getJobLinkedToTimesheet(timesheetId, accountId);
		then(this.accessControlChecker).should()
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(jobOwnerId));
	}

	@Test
	@DisplayName("Check permission should throw exception when user is not an approver")
	void testCheckPermissionUserNotApproverThrowsException() {
		// Given
		Integer timesheetId = 1;
		Integer userId = 10;

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.APPROVE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.timesheetRepository.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId()))
			.willReturn(false);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		// When & Then
		assertThatThrownBy(() -> this.timesheetApprovalPermissionChecker.checkPermission(context))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("is not an approver for timesheet ID");

		then(this.timesheetRepository).should()
			.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId());
		then(this.timesheetRepository).should(never()).getCandidateLinkedToTimesheet(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Check permission with bulk context should perform access control checks when candidate and job exist")
	void testCheckPermissionWithBulkContextCandidateAndJobExistPerformsAccessControlChecks() {
		// Given
		Integer timesheetId = 1;
		Integer userId = 10;
		Integer candidateOwnerId = 50;
		Integer jobOwnerId = 60;

		Candidate candidate = new Candidate();
		candidate.setId(1);
		candidate.setOwnerId(candidateOwnerId);

		Job job = new Job();
		job.setId(1);
		job.setOwnerId(jobOwnerId);

		Map<Integer, Candidate> candidatesByTimesheetId = new HashMap<>();
		candidatesByTimesheetId.put(timesheetId, candidate);

		Map<Integer, Job> jobsByTimesheetId = new HashMap<>();
		jobsByTimesheetId.put(timesheetId, job);

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.candidatesByTimesheetId(candidatesByTimesheetId)
			.jobsByTimesheetId(jobsByTimesheetId)
			.build();

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.APPROVE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.timesheetRepository.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId()))
			.willReturn(true);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		willDoNothing().given(this.accessControlChecker)
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					anyInt());

		// When
		assertThatCode(
				() -> this.timesheetApprovalPermissionChecker.checkPermissionWithBulkContext(context, bulkContext))
			.doesNotThrowAnyException();

		// Then
		then(this.timesheetRepository).should()
			.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId());
		then(this.accessControlChecker).should()
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(candidateOwnerId));
		then(this.accessControlChecker).should()
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(jobOwnerId));
	}

	@Test
	@DisplayName("Check permission with bulk context should bypass candidate check when candidate is null")
	void testCheckPermissionWithBulkContextCandidateNullBypassesCandidateCheck() {
		// Given
		Integer timesheetId = 1;
		Integer userId = 10;
		Integer jobOwnerId = 60;

		Job job = new Job();
		job.setId(1);
		job.setOwnerId(jobOwnerId);

		Map<Integer, Candidate> candidatesByTimesheetId = new HashMap<>();
		// No candidate for this timesheetId - simulates deleted candidate

		Map<Integer, Job> jobsByTimesheetId = new HashMap<>();
		jobsByTimesheetId.put(timesheetId, job);

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.candidatesByTimesheetId(candidatesByTimesheetId)
			.jobsByTimesheetId(jobsByTimesheetId)
			.build();

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.APPROVE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.timesheetRepository.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId()))
			.willReturn(true);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		willDoNothing().given(this.accessControlChecker)
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(jobOwnerId));

		// When
		assertThatCode(
				() -> this.timesheetApprovalPermissionChecker.checkPermissionWithBulkContext(context, bulkContext))
			.doesNotThrowAnyException();

		// Then
		then(this.timesheetRepository).should()
			.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId());
		then(this.accessControlChecker).should()
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(jobOwnerId));
	}

	@Test
	@DisplayName("Check permission should bypass job check when job is null")
	void testCheckPermissionJobNullBypassesJobCheck() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;
		Integer userId = 10;
		Integer candidateOwnerId = 50;

		Candidate candidate = new Candidate();
		candidate.setId(1);
		candidate.setOwnerId(candidateOwnerId);

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.APPROVE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.timesheetRepository.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId()))
			.willReturn(true);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);
		given(this.timesheetRepository.getJobLinkedToTimesheet(timesheetId, accountId)).willReturn(null);
		willDoNothing().given(this.accessControlChecker)
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(candidateOwnerId));

		// When
		assertThatCode(() -> this.timesheetApprovalPermissionChecker.checkPermission(context))
			.doesNotThrowAnyException();

		// Then
		then(this.timesheetRepository).should()
			.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId());
		then(this.timesheetRepository).should().getCandidateLinkedToTimesheet(timesheetId, accountId);
		then(this.timesheetRepository).should().getJobLinkedToTimesheet(timesheetId, accountId);
		then(this.accessControlChecker).should()
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(candidateOwnerId));
	}

	@Test
	@DisplayName("Check permission should bypass both candidate and job checks when both are null")
	void testCheckPermissionBothCandidateAndJobNullBypassesBothChecks() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;
		Integer userId = 10;

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.APPROVE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.timesheetRepository.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId()))
			.willReturn(true);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(null);
		given(this.timesheetRepository.getJobLinkedToTimesheet(timesheetId, accountId)).willReturn(null);

		// When
		assertThatCode(() -> this.timesheetApprovalPermissionChecker.checkPermission(context))
			.doesNotThrowAnyException();

		// Then
		then(this.timesheetRepository).should()
			.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId());
		then(this.timesheetRepository).should().getCandidateLinkedToTimesheet(timesheetId, accountId);
		then(this.timesheetRepository).should().getJobLinkedToTimesheet(timesheetId, accountId);
		then(this.accessControlChecker).should(never())
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					anyInt());
	}

	@Test
	@DisplayName("Check permission with bulk context should bypass job check when job is null")
	void testCheckPermissionWithBulkContextJobNullBypassesJobCheck() {
		// Given
		Integer timesheetId = 1;
		Integer userId = 10;
		Integer candidateOwnerId = 50;

		Candidate candidate = new Candidate();
		candidate.setId(1);
		candidate.setOwnerId(candidateOwnerId);

		Map<Integer, Candidate> candidatesByTimesheetId = new HashMap<>();
		candidatesByTimesheetId.put(timesheetId, candidate);

		Map<Integer, Job> jobsByTimesheetId = new HashMap<>();
		// No job for this timesheetId - simulates deleted job

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.candidatesByTimesheetId(candidatesByTimesheetId)
			.jobsByTimesheetId(jobsByTimesheetId)
			.build();

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.APPROVE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.timesheetRepository.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId()))
			.willReturn(true);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		willDoNothing().given(this.accessControlChecker)
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(candidateOwnerId));

		// When
		assertThatCode(
				() -> this.timesheetApprovalPermissionChecker.checkPermissionWithBulkContext(context, bulkContext))
			.doesNotThrowAnyException();

		// Then
		then(this.timesheetRepository).should()
			.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId());
		then(this.accessControlChecker).should()
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(candidateOwnerId));
	}

	@Test
	@DisplayName("Check permission with bulk context should bypass both checks when both candidate and job are null")
	void testCheckPermissionWithBulkContextBothCandidateAndJobNullBypassesBothChecks() {
		// Given
		Integer timesheetId = 1;
		Integer userId = 10;

		Map<Integer, Candidate> candidatesByTimesheetId = new HashMap<>();
		// No candidate for this timesheetId - simulates deleted candidate

		Map<Integer, Job> jobsByTimesheetId = new HashMap<>();
		// No job for this timesheetId - simulates deleted job

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.candidatesByTimesheetId(candidatesByTimesheetId)
			.jobsByTimesheetId(jobsByTimesheetId)
			.build();

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.APPROVE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.timesheetRepository.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId()))
			.willReturn(true);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		// When
		assertThatCode(
				() -> this.timesheetApprovalPermissionChecker.checkPermissionWithBulkContext(context, bulkContext))
			.doesNotThrowAnyException();

		// Then
		then(this.timesheetRepository).should()
			.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId());
		then(this.accessControlChecker).should(never())
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					anyInt());
	}

	@Test
	@DisplayName("Check permission with bulk context should throw exception when user is not an approver")
	void testCheckPermissionWithBulkContextUserNotApproverThrowsException() {
		// Given
		Integer timesheetId = 1;
		Integer userId = 10;

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.candidatesByTimesheetId(new HashMap<>())
			.jobsByTimesheetId(new HashMap<>())
			.build();

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.APPROVE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.timesheetRepository.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId()))
			.willReturn(false);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetApprovalPermissionChecker.checkPermissionWithBulkContext(context, bulkContext))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("is not an approver for timesheet ID");

		then(this.timesheetRepository).should()
			.validateIsApprover(timesheetId, userId, UserTypeEnum.AGENCY_RECRUITER.getId());
		then(this.accessControlChecker).should(never())
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					anyInt());
	}

}
