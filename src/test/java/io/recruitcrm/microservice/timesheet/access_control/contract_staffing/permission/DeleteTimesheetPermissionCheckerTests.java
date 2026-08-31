package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
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
@DisplayName("DeleteTimesheetPermissionChecker Tests")
class DeleteTimesheetPermissionCheckerTests {

	@Mock
	private AuthHolder authHolder;

	@Mock
	private TimesheetRepository timesheetRepository;

	@Mock
	private AccessControlChecker accessControlChecker;

	@InjectMocks
	private DeleteTimesheetPermissionChecker deleteTimesheetPermissionChecker;

	@Test
	@DisplayName("Check permission should perform access control check when candidate exists")
	void testCheckPermissionCandidateExistsPerformsAccessControlCheck() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;
		Integer candidateOwnerId = 50;

		Candidate candidate = new Candidate();
		candidate.setId(1);
		candidate.setOwnerId(candidateOwnerId);

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.DELETE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);
		willDoNothing().given(this.accessControlChecker)
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(candidateOwnerId));

		// When
		assertThatCode(() -> this.deleteTimesheetPermissionChecker.checkPermission(context)).doesNotThrowAnyException();

		// Then
		then(this.timesheetRepository).should().getCandidateLinkedToTimesheet(timesheetId, accountId);
		then(this.accessControlChecker).should()
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(candidateOwnerId));
	}

	@Test
	@DisplayName("Check permission should bypass access control check when candidate is null")
	void testCheckPermissionCandidateNullBypassesAccessControlCheck() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.DELETE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(null);

		// When
		assertThatCode(() -> this.deleteTimesheetPermissionChecker.checkPermission(context)).doesNotThrowAnyException();

		// Then
		then(this.timesheetRepository).should().getCandidateLinkedToTimesheet(timesheetId, accountId);
		then(this.accessControlChecker).should(never())
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					anyInt());
	}

	@Test
	@DisplayName("Check permission with bulk context should perform access control check when candidate exists")
	void testCheckPermissionWithBulkContextCandidateExistsPerformsAccessControlCheck() {
		// Given
		Integer timesheetId = 1;
		Integer candidateOwnerId = 50;

		Candidate candidate = new Candidate();
		candidate.setId(1);
		candidate.setOwnerId(candidateOwnerId);

		Map<Integer, Candidate> candidatesByTimesheetId = new HashMap<>();
		candidatesByTimesheetId.put(timesheetId, candidate);

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.candidatesByTimesheetId(candidatesByTimesheetId)
			.build();

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.DELETE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		willDoNothing().given(this.accessControlChecker)
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(candidateOwnerId));

		// When
		assertThatCode(() -> this.deleteTimesheetPermissionChecker.checkPermissionWithBulkContext(context, bulkContext))
			.doesNotThrowAnyException();

		// Then
		then(this.accessControlChecker).should()
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(candidateOwnerId));
	}

	@Test
	@DisplayName("Check permission with bulk context should bypass access control check when candidate is null")
	void testCheckPermissionWithBulkContextCandidateNullBypassesAccessControlCheck() {
		// Given
		Integer timesheetId = 1;

		Map<Integer, Candidate> candidatesByTimesheetId = new HashMap<>();
		// No candidate for this timesheetId - simulates deleted candidate

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.candidatesByTimesheetId(candidatesByTimesheetId)
			.build();

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.DELETE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		// When
		assertThatCode(() -> this.deleteTimesheetPermissionChecker.checkPermissionWithBulkContext(context, bulkContext))
			.doesNotThrowAnyException();

		// Then
		then(this.accessControlChecker).should(never())
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					anyInt());
	}

}
