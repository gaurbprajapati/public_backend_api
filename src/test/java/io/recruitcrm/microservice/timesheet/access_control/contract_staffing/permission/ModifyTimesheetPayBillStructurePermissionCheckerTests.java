package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

import io.recruitcrm.entity.model.Job;
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
@DisplayName("ModifyTimesheetPayBillStructurePermissionChecker Tests")
class ModifyTimesheetPayBillStructurePermissionCheckerTests {

	@Mock
	private AuthHolder authHolder;

	@Mock
	private TimesheetRepository timesheetRepository;

	@Mock
	private AccessControlChecker accessControlChecker;

	@InjectMocks
	private ModifyTimesheetPayBillStructurePermissionChecker permissionChecker;

	@Test
	@DisplayName("Check permission should perform job access control check when job exists")
	void testCheckPermissionJobExistsPerformsAccessControlCheck() {
		// Given
		Integer timesheetId = 1;
		Integer accountId = 100;
		Integer jobOwnerId = 60;

		Job job = new Job();
		job.setId(1);
		job.setOwnerId(jobOwnerId);

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.MODIFY_TIMESHEET_PAY_BILL_STRUCTURE)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getJobLinkedToTimesheet(timesheetId, accountId)).willReturn(job);
		willDoNothing().given(this.accessControlChecker)
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(jobOwnerId));

		// When
		assertThatCode(() -> this.permissionChecker.checkPermission(context)).doesNotThrowAnyException();

		// Then
		then(this.timesheetRepository).should().getJobLinkedToTimesheet(timesheetId, accountId);
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

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.MODIFY_TIMESHEET_PAY_BILL_STRUCTURE)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetRepository.getJobLinkedToTimesheet(timesheetId, accountId)).willReturn(null);

		// When
		assertThatCode(() -> this.permissionChecker.checkPermission(context)).doesNotThrowAnyException();

		// Then
		then(this.timesheetRepository).should().getJobLinkedToTimesheet(timesheetId, accountId);
		then(this.accessControlChecker).should(never())
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					anyInt());
	}

	@Test
	@DisplayName("Check permission with bulk context should perform job access control check when job exists")
	void testCheckPermissionWithBulkContextJobExistsPerformsAccessControlCheck() {
		// Given
		Integer timesheetId = 1;
		Integer jobOwnerId = 60;

		Job job = new Job();
		job.setId(1);
		job.setOwnerId(jobOwnerId);

		Map<Integer, Job> jobsByTimesheetId = new HashMap<>();
		jobsByTimesheetId.put(timesheetId, job);

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.jobsByTimesheetId(jobsByTimesheetId)
			.build();

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.MODIFY_TIMESHEET_PAY_BILL_STRUCTURE)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		willDoNothing().given(this.accessControlChecker)
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(jobOwnerId));

		// When
		assertThatCode(() -> this.permissionChecker.checkPermissionWithBulkContext(context, bulkContext))
			.doesNotThrowAnyException();

		// Then
		then(this.accessControlChecker).should()
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					eq(jobOwnerId));
	}

	@Test
	@DisplayName("Check permission with bulk context should bypass job check when job is null")
	void testCheckPermissionWithBulkContextJobNullBypassesJobCheck() {
		// Given
		Integer timesheetId = 1;

		Map<Integer, Job> jobsByTimesheetId = new HashMap<>();
		// No job for this timesheetId - simulates deleted job

		BulkPermissionCheckContext bulkContext = BulkPermissionCheckContext.builder()
			.jobsByTimesheetId(jobsByTimesheetId)
			.build();

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.MODIFY_TIMESHEET_PAY_BILL_STRUCTURE)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(timesheetId)
			.build();

		// When
		assertThatCode(() -> this.permissionChecker.checkPermissionWithBulkContext(context, bulkContext))
			.doesNotThrowAnyException();

		// Then
		then(this.accessControlChecker).should(never())
			.allows(any(), any(io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.class),
					anyInt());
	}

}
