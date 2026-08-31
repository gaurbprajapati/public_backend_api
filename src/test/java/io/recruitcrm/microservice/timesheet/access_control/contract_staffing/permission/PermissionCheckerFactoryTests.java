/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionCheckerFactory Tests")
class PermissionCheckerFactoryTests {

	@Mock
	private CreateTimesheetPermissionChecker createTimesheetPermissionChecker;

	@Mock
	private DeleteTimesheetPermissionChecker deleteTimesheetPermissionChecker;

	@Mock
	private ViewTimesheetPermissionChecker viewTimesheetPermissionChecker;

	@Mock
	private ModifyTimesheetPayBillStructurePermissionChecker modifyTimesheetPayBillStructurePermissionChecker;

	@Mock
	private TimesheetApprovalPermissionChecker timesheetApprovalPermissionChecker;

	@Mock
	private SubmitTimesheetSettingsPermissionChecker submitTimesheetSettingsPermissionChecker;

	@Mock
	private TimesheetCanEditPermissionChecker timesheetCanEditPermissionChecker;

	private PermissionCheckerFactory permissionCheckerFactory;

	@BeforeEach
	void setUp() {
		this.permissionCheckerFactory = new PermissionCheckerFactory(this.createTimesheetPermissionChecker,
				this.deleteTimesheetPermissionChecker, this.viewTimesheetPermissionChecker,
				this.modifyTimesheetPayBillStructurePermissionChecker, this.timesheetApprovalPermissionChecker,
				this.submitTimesheetSettingsPermissionChecker, this.timesheetCanEditPermissionChecker);
	}

	@Test
	@DisplayName("getPermissionChecker should return the mapped checker for a supported permission")
	void testGetPermissionCheckerSupportedPermissionReturnsChecker() {
		// When
		BasePermissionChecker result = this.permissionCheckerFactory.getPermissionChecker(Permission.APPROVE_TIMESHEET);

		// Then
		assertThat(result).isEqualTo(this.timesheetApprovalPermissionChecker);
	}

	@Test
	@DisplayName("getPermissionChecker should throw UnauthorizedAccessException for an unsupported permission")
	void testGetPermissionCheckerUnsupportedPermissionThrowsException() {
		// When & Then
		assertThatThrownBy(() -> this.permissionCheckerFactory.getPermissionChecker(Permission.VIEW_CONTRACTOR_DETAILS))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("is not supported in the current implementation");
	}

}
