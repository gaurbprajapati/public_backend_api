package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission.BasePermissionChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission.PermissionCheckerFactory;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccessLevelHandler (Contract Staffing) Tests")
class AccessLevelHandlerContractStaffingTests {

	@Mock
	private PermissionCheckerFactory permissionCheckerFactory;

	@Mock
	private BasePermissionChecker checker;

	@Test
	@DisplayName("Should throw UnauthorizedAccessException when permission checker factory returns null")
	void testCheckAccessCheckerNullThrowsUnauthorizedAccessException() {
		// Given
		AccessLevelHandler handler = new AccessLevelHandler(this.permissionCheckerFactory);

		PermissionCheckContext permissionCheckContext = PermissionCheckContext.builder()
			.permission(Permission.SUBMIT_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.build();

		AccessControlCheckMetadataContext accessControlCheckMetadataContext = AccessControlCheckMetadataContext
			.builder()
			.timesheetId(1)
			.build();

		given(this.permissionCheckerFactory.getPermissionChecker(permissionCheckContext.getPermission()))
			.willReturn(null);

		// When & Then
		assertThatThrownBy(() -> handler.checkAccess(Entity.TIMESHEET_SETTINGS, permissionCheckContext,
				accessControlCheckMetadataContext))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("No permission checker exists for")
			.hasMessageContaining(permissionCheckContext.getPermission().toString())
			.hasMessageContaining(Entity.TIMESHEET_SETTINGS.toString());

		verifyNoInteractions(this.checker);
	}

	@Test
	@DisplayName("Should build internal context and delegate permission check to checker")
	void testCheckAccessCheckerAvailableDelegatesToChecker() {
		// Given
		AccessLevelHandler handler = new AccessLevelHandler(this.permissionCheckerFactory);

		Entity entity = Entity.TIMESHEET_SETTINGS;
		PermissionCheckContext permissionCheckContext = PermissionCheckContext.builder()
			.permission(Permission.SUBMIT_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.build();

		AccessControlCheckMetadataContext accessControlCheckMetadataContext = AccessControlCheckMetadataContext
			.builder()
			.timesheetId(123)
			.build();

		given(this.permissionCheckerFactory.getPermissionChecker(permissionCheckContext.getPermission()))
			.willReturn(this.checker);

		ArgumentCaptor<PermissionCheckInternalContext> internalContextCaptor = ArgumentCaptor
			.forClass(PermissionCheckInternalContext.class);

		// When
		handler.checkAccess(entity, permissionCheckContext, accessControlCheckMetadataContext);

		// Then
		verify(this.permissionCheckerFactory).getPermissionChecker(permissionCheckContext.getPermission());
		verify(this.checker).checkPermission(internalContextCaptor.capture());

		PermissionCheckInternalContext captured = internalContextCaptor.getValue();
		org.assertj.core.api.Assertions.assertThat(captured.getEntity()).isSameAs(entity);
		org.assertj.core.api.Assertions.assertThat(captured.getPermission())
			.isSameAs(permissionCheckContext.getPermission());
		org.assertj.core.api.Assertions.assertThat(captured.getPermissionLevel())
			.isSameAs(permissionCheckContext.getPermissionLevel());
		org.assertj.core.api.Assertions.assertThat(captured.getTimesheetId())
			.isEqualTo(accessControlCheckMetadataContext.getTimesheetId());
	}

	@Test
	@DisplayName("Should throw NullPointerException when entity is null")
	void testCheckAccessNullEntityThrowsNullPointerException() {
		// Given
		AccessLevelHandler handler = new AccessLevelHandler(this.permissionCheckerFactory);
		PermissionCheckContext permissionCheckContext = PermissionCheckContext.builder()
			.permission(Permission.SUBMIT_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.build();
		AccessControlCheckMetadataContext accessControlCheckMetadataContext = AccessControlCheckMetadataContext
			.builder()
			.timesheetId(1)
			.build();

		// When & Then
		assertThatThrownBy(() -> handler.checkAccess(null, permissionCheckContext, accessControlCheckMetadataContext))
			.isInstanceOf(NullPointerException.class);

		verifyNoInteractions(this.permissionCheckerFactory);
	}

	@Test
	@DisplayName("Should throw NullPointerException when permissionCheckContext is null")
	void testCheckAccessNullPermissionCheckContextThrowsNullPointerException() {
		// Given
		AccessLevelHandler handler = new AccessLevelHandler(this.permissionCheckerFactory);
		AccessControlCheckMetadataContext accessControlCheckMetadataContext = AccessControlCheckMetadataContext
			.builder()
			.timesheetId(1)
			.build();

		// When & Then
		assertThatThrownBy(
				() -> handler.checkAccess(Entity.TIMESHEET_SETTINGS, null, accessControlCheckMetadataContext))
			.isInstanceOf(NullPointerException.class);

		verifyNoInteractions(this.permissionCheckerFactory);
	}

	@Test
	@DisplayName("Should throw NullPointerException when accessControlCheckMetadataContext is null")
	void testCheckAccessNullAccessControlMetadataContextThrowsNullPointerException() {
		// Given
		AccessLevelHandler handler = new AccessLevelHandler(this.permissionCheckerFactory);
		PermissionCheckContext permissionCheckContext = PermissionCheckContext.builder()
			.permission(Permission.SUBMIT_TIMESHEET)
			.permissionLevel(PermissionLevel.YES)
			.build();

		// When & Then
		assertThatThrownBy(() -> handler.checkAccess(Entity.TIMESHEET_SETTINGS, permissionCheckContext, null))
			.isInstanceOf(NullPointerException.class);

		verifyNoInteractions(this.permissionCheckerFactory);
	}

}
