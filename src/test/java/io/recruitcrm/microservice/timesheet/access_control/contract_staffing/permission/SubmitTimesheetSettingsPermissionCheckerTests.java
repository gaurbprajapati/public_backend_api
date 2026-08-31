package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubmitTimesheetSettingsPermissionChecker Tests")
class SubmitTimesheetSettingsPermissionCheckerTests {

	@Mock
	private AuthHolder authHolder;

	@Mock
	private TimesheetRepository timesheetRepository;

	@Mock
	private AccessControlChecker accessControlChecker;

	@Test
	@DisplayName("Should allow CAN_EDIT when candidate exists (non-bulk)")
	void testCheckPermissionCandidateExistsAllocsCanEditOnCandidateOwner() {
		// Given
		Integer timesheetId = 10;
		Integer orgId = 100;
		Integer ownerId = 200;

		Candidate candidate = new Candidate();
		candidate.setId(1);
		candidate.setOwnerId(ownerId);

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET_SETTINGS)
			.permission(Permission.SUBMIT_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		SubmitTimesheetSettingsPermissionChecker checker = new SubmitTimesheetSettingsPermissionChecker(this.authHolder,
				this.timesheetRepository, this.accessControlChecker);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(orgId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, orgId)).willReturn(candidate);

		// When
		checker.checkPermission(context);

		// Then
		verify(this.accessControlChecker).allows(
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Entity.CANDIDATES,
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT, ownerId);
	}

	@Test
	@DisplayName("Should throw UnauthorizedAccessException when candidate missing (non-bulk)")
	void testCheckPermissionCandidateMissingThrowsUnauthorized() {
		// Given
		Integer timesheetId = 10;
		Integer orgId = 100;

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET_SETTINGS)
			.permission(Permission.SUBMIT_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		SubmitTimesheetSettingsPermissionChecker checker = new SubmitTimesheetSettingsPermissionChecker(this.authHolder,
				this.timesheetRepository, this.accessControlChecker);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(orgId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, orgId)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> checker.checkPermission(context)).isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Candidate not found for timesheet ID: 10");
		verifyNoInteractions(this.accessControlChecker);
	}

	@Test
	@DisplayName("Should throw NullPointerException when context is null (non-bulk)")
	void testCheckPermissionNullContextThrowsNullPointerException() {
		// Given
		SubmitTimesheetSettingsPermissionChecker checker = new SubmitTimesheetSettingsPermissionChecker(this.authHolder,
				this.timesheetRepository, this.accessControlChecker);

		// When & Then
		assertThatThrownBy(() -> checker.checkPermission(null)).isInstanceOf(NullPointerException.class);
		verifyNoInteractions(this.accessControlChecker);
	}

	@Test
	@DisplayName("Should allow CAN_EDIT when candidate exists (bulk)")
	void testCheckPermissionWithBulkContextCandidateExistsAllocsCanEditOnCandidateOwner() {
		// Given
		Integer timesheetId = 10;
		Integer ownerId = 200;

		Candidate candidate = new Candidate();
		candidate.setId(1);
		candidate.setOwnerId(ownerId);

		Map<Integer, Candidate> candidatesByTimesheetId = new HashMap<>();
		candidatesByTimesheetId.put(timesheetId, candidate);

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.candidatesByTimesheetId(candidatesByTimesheetId)
			.build();

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET_SETTINGS)
			.permission(Permission.SUBMIT_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		SubmitTimesheetSettingsPermissionChecker checker = new SubmitTimesheetSettingsPermissionChecker(this.authHolder,
				this.timesheetRepository, this.accessControlChecker);

		// When
		checker.checkPermissionWithBulkContext(context, bulkContext);

		// Then
		verify(this.accessControlChecker).allows(
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Entity.CANDIDATES,
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT, ownerId);
	}

	@Test
	@DisplayName("Should throw UnauthorizedAccessException when candidate missing (bulk)")
	void testCheckPermissionWithBulkContextCandidateMissingThrowsUnauthorized() {
		// Given
		Integer timesheetId = 10;

		Map<Integer, Candidate> candidatesByTimesheetId = new HashMap<>();
		// No candidate for timesheetId

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.candidatesByTimesheetId(candidatesByTimesheetId)
			.build();

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET_SETTINGS)
			.permission(Permission.SUBMIT_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		SubmitTimesheetSettingsPermissionChecker checker = new SubmitTimesheetSettingsPermissionChecker(this.authHolder,
				this.timesheetRepository, this.accessControlChecker);

		// When & Then
		assertThatThrownBy(() -> checker.checkPermissionWithBulkContext(context, bulkContext))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Candidate not found for timesheet ID: 10");
		verifyNoInteractions(this.accessControlChecker);
	}

	@Test
	@DisplayName("Should throw NullPointerException when context is null (bulk)")
	void testCheckPermissionWithBulkContextNullContextThrowsNullPointerException() {
		// Given
		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.candidatesByTimesheetId(Map.of())
			.build();

		SubmitTimesheetSettingsPermissionChecker checker = new SubmitTimesheetSettingsPermissionChecker(this.authHolder,
				this.timesheetRepository, this.accessControlChecker);

		// When & Then
		assertThatThrownBy(() -> checker.checkPermissionWithBulkContext(null, bulkContext))
			.isInstanceOf(NullPointerException.class);
		verifyNoInteractions(this.accessControlChecker);
	}

	@Test
	@DisplayName("Should throw NullPointerException when bulkContext is null (bulk)")
	void testCheckPermissionWithBulkContextNullBulkContextThrowsNullPointerException() {
		// Given
		Integer timesheetId = 10;

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET_SETTINGS)
			.permission(Permission.SUBMIT_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		SubmitTimesheetSettingsPermissionChecker checker = new SubmitTimesheetSettingsPermissionChecker(this.authHolder,
				this.timesheetRepository, this.accessControlChecker);

		// When & Then
		assertThatThrownBy(() -> checker.checkPermissionWithBulkContext(context, null))
			.isInstanceOf(NullPointerException.class);
		verifyNoInteractions(this.accessControlChecker);
	}

}
