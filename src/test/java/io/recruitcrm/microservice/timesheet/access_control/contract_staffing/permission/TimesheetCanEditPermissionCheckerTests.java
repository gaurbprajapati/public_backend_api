package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimesheetCanEditPermissionChecker Tests")
class TimesheetCanEditPermissionCheckerTests {

	@Mock
	private AuthHolder authHolder;

	@Mock
	private TimesheetRepository timesheetRepository;

	@Mock
	private AccessControlChecker accessControlChecker;

	@InjectMocks
	private TimesheetCanEditPermissionChecker timesheetCanEditPermissionChecker;

	@Test
	@DisplayName("Check permission should validate candidate edit permission when candidate exists")
	void testCheckPermissionCandidateExistsChecksCandidateEditPermission() {
		Integer timesheetId = 1;
		Integer accountId = 100;
		Integer candidateOwnerId = 50;

		Candidate candidate = new Candidate();
		candidate.setId(999);
		candidate.setOwnerId(candidateOwnerId);

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.EDIT_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);
		willDoNothing().given(this.accessControlChecker)
			.allows(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT,
					candidateOwnerId);

		assertThatCode(() -> this.timesheetCanEditPermissionChecker.checkPermission(context))
			.doesNotThrowAnyException();

		then(this.timesheetRepository).should().getCandidateLinkedToTimesheet(timesheetId, accountId);
		then(this.accessControlChecker).should()
			.allows(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT,
					candidateOwnerId);
	}

	@Test
	@DisplayName("Check permission should throw unauthorized access when candidate is missing")
	void testCheckPermissionCandidateMissingThrowsUnauthorizedAccess() {
		Integer timesheetId = 1;
		Integer accountId = 100;

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.EDIT_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(null);

		assertThatThrownBy(() -> this.timesheetCanEditPermissionChecker.checkPermission(context))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Candidate not found for timesheet ID");

		then(this.accessControlChecker).should(never()).allows(any(), any(), any());
	}

	@Test
	@DisplayName("Check permission should throw null pointer exception when context is null")
	void testCheckPermissionNullContextThrowsNullPointerException() {
		assertThatThrownBy(() -> this.timesheetCanEditPermissionChecker.checkPermission(null))
			.isInstanceOf(NullPointerException.class);
	}

}
