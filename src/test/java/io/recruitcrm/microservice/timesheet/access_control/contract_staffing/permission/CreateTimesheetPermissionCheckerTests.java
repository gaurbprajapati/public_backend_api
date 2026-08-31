package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateTimesheetPermissionChecker Tests")
class CreateTimesheetPermissionCheckerTests {

	@Mock
	private AuthHolder authHolder;

	@Mock
	private TimesheetRepository timesheetRepository;

	@Mock
	private AccessControlChecker accessControlChecker;

	@Test
	@DisplayName("Should delegate CAN_ADD permission check (non-bulk)")
	void testCheckPermissionDelegatesCanAddToAccessControlChecker() {
		// Given
		CreateTimesheetPermissionChecker checker = new CreateTimesheetPermissionChecker(this.authHolder,
				this.timesheetRepository, this.accessControlChecker);

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.CREATE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(1)
			.build();

		// When
		checker.checkPermission(context);

		// Then
		verify(this.accessControlChecker).allows(
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Entity.CANDIDATES,
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_ADD, (Integer) null);
	}

	@Test
	@DisplayName("Should throw NullPointerException when context is null (non-bulk)")
	void testCheckPermissionNullContextThrowsNullPointerException() {
		// Given
		CreateTimesheetPermissionChecker checker = new CreateTimesheetPermissionChecker(this.authHolder,
				this.timesheetRepository, this.accessControlChecker);

		// When & Then
		assertThatThrownBy(() -> checker.checkPermission(null)).isInstanceOf(NullPointerException.class);
		verifyNoInteractions(this.accessControlChecker);
	}

	@Test
	@DisplayName("Should delegate CAN_ADD permission check (bulk)")
	void testCheckPermissionWithBulkContextDelegatesCanAddToAccessControlChecker() {
		// Given
		CreateTimesheetPermissionChecker checker = new CreateTimesheetPermissionChecker(this.authHolder,
				this.timesheetRepository, this.accessControlChecker);

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.CREATE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(1)
			.build();

		BulkPermissionCheckContext bulkPermissionCheckContext = new BulkPermissionCheckContext();

		// When
		checker.checkPermissionWithBulkContext(context, bulkPermissionCheckContext);

		// Then
		verify(this.accessControlChecker).allows(
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Entity.CANDIDATES,
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_ADD, (Integer) null);
	}

	@Test
	@DisplayName("Should throw NullPointerException when context is null (bulk)")
	void testCheckPermissionWithBulkContextNullContextThrowsNullPointerException() {
		// Given
		CreateTimesheetPermissionChecker checker = new CreateTimesheetPermissionChecker(this.authHolder,
				this.timesheetRepository, this.accessControlChecker);

		BulkPermissionCheckContext bulkPermissionCheckContext = new BulkPermissionCheckContext();

		// When & Then
		assertThatThrownBy(() -> checker.checkPermissionWithBulkContext(null, bulkPermissionCheckContext))
			.isInstanceOf(NullPointerException.class);
		verifyNoInteractions(this.accessControlChecker);
	}

	@Test
	@DisplayName("Should throw NullPointerException when bulk context is null (bulk)")
	void testCheckPermissionWithBulkContextNullBulkContextThrowsNullPointerException() {
		// Given
		CreateTimesheetPermissionChecker checker = new CreateTimesheetPermissionChecker(this.authHolder,
				this.timesheetRepository, this.accessControlChecker);

		PermissionCheckInternalContext context = PermissionCheckInternalContext.builder()
			.entity(Entity.TIMESHEET)
			.permission(Permission.CREATE_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.timesheetId(1)
			.build();

		// When & Then
		assertThatThrownBy(() -> checker.checkPermissionWithBulkContext(context, null))
			.isInstanceOf(NullPointerException.class);
		verifyNoInteractions(this.accessControlChecker);
	}

}
