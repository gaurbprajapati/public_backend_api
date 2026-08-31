package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.AccessControlDto;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.GlobalPermissions;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.SpecialPermissionFieldHandler;
import io.recruitcrm.microservice.timesheet.exceptions.access_control.InvalidPermissionLevelException;
import io.recruitcrm.microservice.timesheet.exceptions.access_control.UnknownAccessLevelException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@DisplayName("AccessLevelHandler")
class AccessLevelHandlerTests {

	@Test
	@DisplayName("Throws when GLOBAL entity is used with non-ALLOWED permission")
	void testGetAccessLevelThrowsWhenGlobalPermissionIsNotAllowed() {
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()),
						Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.YES)));

		AccessControlDto accessControlDto = new AccessControlDto();
		assertThatThrownBy(() -> getAccessLevel(handler, Entity.GLOBAL, accessControlDto, Permission.CAN_VIEW,
				GlobalPermission.REPORTS))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("Only ALLOWED permission is supported");
	}

	@Test
	@DisplayName("Throws when entity is not supported")
	void testGetAccessLevelThrowsWhenEntityNotSupported() {
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()), Map.of()));

		AccessControlDto accessControlDto = new AccessControlDto();
		assertThatThrownBy(
				() -> getAccessLevel(handler, Entity.CANDIDATES, accessControlDto, Permission.CAN_VIEW, null))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("Unknown entity");
	}

	@Test
	@DisplayName("Global access requires GlobalPermission")
	void testGetAccessLevelGlobalAccessRequiresGlobalPermission() {
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()),
						Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.YES)));

		AccessControlDto accessControlDto = new AccessControlDto();
		assertThatThrownBy(() -> getAccessLevel(handler, Entity.GLOBAL, accessControlDto, Permission.ALLOWED, null))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("GlobalPermission must be specified");
	}

	@Test
	@DisplayName("Global access throws when access level is not GlobalPermissions")
	void testGetAccessLevelGlobalAccessThrowsForInvalidType() {
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new Object()),
						Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.YES)));

		AccessControlDto accessControlDto = new AccessControlDto();
		assertThatThrownBy(() -> getAccessLevel(handler, Entity.GLOBAL, accessControlDto, Permission.ALLOWED,
				GlobalPermission.REPORTS))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("Invalid access level type for global permissions");
	}

	@Test
	@DisplayName("Global access throws when global permission getter missing")
	void testGetAccessLevelGlobalAccessThrowsWhenGlobalPermissionGetterMissing() {
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()), Map.of()));

		AccessControlDto accessControlDto = new AccessControlDto();
		assertThatThrownBy(() -> getAccessLevel(handler, Entity.GLOBAL, accessControlDto, Permission.ALLOWED,
				GlobalPermission.REPORTS))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("Unknown global permission type");
	}

	@Test
	@DisplayName("Global access returns level and validates binary permission levels")
	void testGetAccessLevelGlobalAccessReturnsAndValidatesBinaryLevels() {
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()),
						Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.NO)));

		PermissionLevel level = handler.getAccessLevel(Entity.GLOBAL, new AccessControlDto(), Permission.ALLOWED,
				GlobalPermission.REPORTS);

		assertThat(level).isEqualTo(PermissionLevel.NO);
	}

	@Test
	@DisplayName("Global access throws InvalidPermissionLevelException when non-binary level is returned for ALLOWED")
	void testGetAccessLevelGlobalAccessThrowsWhenNonBinaryLevelReturned() {
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()),
						Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.EVERYTHING)));

		AccessControlDto accessControlDto = new AccessControlDto();
		assertThatThrownBy(() -> getAccessLevel(handler, Entity.GLOBAL, accessControlDto, Permission.ALLOWED,
				GlobalPermission.REPORTS))
			.isInstanceOf(InvalidPermissionLevelException.class)
			.hasMessageContaining("Invalid permission level");
	}

	@Test
	@DisplayName("Entity access resolves PermissionLevel from BasicAccessLevel fields")
	void testGetAccessLevelEntityAccessResolvesFromBasicAccessLevel() {
		BasicAccessLevel basicAccessLevel = new BasicAccessLevel() {
			@Override
			public String getCanAdd() {
				return PermissionLevel.YES.getLabel();
			}

			@Override
			public String getCanEdit() {
				return PermissionLevel.NO.getLabel();
			}

			@Override
			public String getCanView() {
				return PermissionLevel.EVERYTHING.getLabel();
			}

			@Override
			public String getCanDelete() {
				return PermissionLevel.NOTHING.getLabel();
			}
		};

		AccessLevelHandler handler = new AccessLevelHandler(buildInitializer(
				Map.of(Entity.CALL_LOG, (dto) -> basicAccessLevel, Entity.GLOBAL, (dto) -> new GlobalPermissions()),
				Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.YES)));
		AccessControlDto accessControlDto = new AccessControlDto();

		assertThat(handler.getAccessLevel(Entity.CALL_LOG, new AccessControlDto(), Permission.CAN_ADD, null))
			.isEqualTo(PermissionLevel.YES);
		assertThatThrownBy(() -> getAccessLevel(handler, Entity.CALL_LOG, accessControlDto, Permission.CAN_EDIT, null))
			.isInstanceOf(InvalidPermissionLevelException.class)
			.hasMessageContaining("YES and NO are not allowed for this permission type");
		assertThat(handler.getAccessLevel(Entity.CALL_LOG, new AccessControlDto(), Permission.CAN_VIEW, null))
			.isEqualTo(PermissionLevel.EVERYTHING);
		assertThat(handler.getAccessLevel(Entity.CALL_LOG, new AccessControlDto(), Permission.CAN_DELETE, null))
			.isEqualTo(PermissionLevel.NOTHING);
	}

	@Test
	@DisplayName("Entity access throws for unsupported FILE_ACCESS on non-ExtendedAccessLevel")
	void testGetAccessLevelThrowsForExtendedPermissionOnBasicLevel() {
		BasicAccessLevel basicAccessLevel = mock(BasicAccessLevel.class);
		given(basicAccessLevel.getCanAdd()).willReturn(PermissionLevel.YES.getLabel());
		given(basicAccessLevel.getCanEdit()).willReturn(PermissionLevel.NO.getLabel());
		given(basicAccessLevel.getCanView()).willReturn(PermissionLevel.EVERYTHING.getLabel());
		given(basicAccessLevel.getCanDelete()).willReturn(PermissionLevel.NOTHING.getLabel());

		AccessLevelHandler handler = new AccessLevelHandler(buildInitializer(
				Map.of(Entity.CALL_LOG, (dto) -> basicAccessLevel, Entity.GLOBAL, (dto) -> new GlobalPermissions()),
				Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.YES)));

		AccessControlDto accessControlDto = new AccessControlDto();
		assertThatThrownBy(
				() -> getAccessLevel(handler, Entity.CALL_LOG, accessControlDto, Permission.FILE_ACCESS, null))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("does not support");
	}

	@Test
	@DisplayName("Entity access resolves FILE_ACCESS and OWNER_CHANGE from ExtendedAccessLevel")
	void testGetAccessLevelResolvesFromExtendedAccessLevel() {
		ExtendedAccessLevel extendedAccessLevel = new ExtendedAccessLevel() {
			@Override
			public String getFileAccess() {
				return PermissionLevel.TEAM_ONLY.getLabel();
			}

			@Override
			public String getOwnerChange() {
				return PermissionLevel.OWNED_ONLY.getLabel();
			}

			@Override
			public String getCanAdd() {
				return PermissionLevel.YES.getLabel();
			}

			@Override
			public String getCanEdit() {
				return PermissionLevel.NO.getLabel();
			}

			@Override
			public String getCanView() {
				return PermissionLevel.EVERYTHING.getLabel();
			}

			@Override
			public String getCanDelete() {
				return PermissionLevel.NOTHING.getLabel();
			}
		};

		AccessLevelHandler handler = new AccessLevelHandler(buildInitializer(
				Map.of(Entity.CANDIDATES, (dto) -> extendedAccessLevel, Entity.GLOBAL,
						(dto) -> new GlobalPermissions()),
				Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.YES)));

		assertThat(handler.getAccessLevel(Entity.CANDIDATES, new AccessControlDto(), Permission.FILE_ACCESS, null))
			.isEqualTo(PermissionLevel.TEAM_ONLY);
		assertThat(handler.getAccessLevel(Entity.CANDIDATES, new AccessControlDto(), Permission.OWNER_CHANGE, null))
			.isEqualTo(PermissionLevel.OWNED_ONLY);
	}

	@Test
	@DisplayName("Throws for unknown action on BasicAccessLevel")
	void testGetAccessLevelThrowsForUnknownAction() {
		BasicAccessLevel basicAccessLevel = mock(BasicAccessLevel.class);
		given(basicAccessLevel.getCanAdd()).willReturn(PermissionLevel.YES.getLabel());
		given(basicAccessLevel.getCanEdit()).willReturn(PermissionLevel.NO.getLabel());
		given(basicAccessLevel.getCanView()).willReturn(PermissionLevel.EVERYTHING.getLabel());
		given(basicAccessLevel.getCanDelete()).willReturn(PermissionLevel.NOTHING.getLabel());

		AccessLevelHandler handler = new AccessLevelHandler(buildInitializer(
				Map.of(Entity.CALL_LOG, (dto) -> basicAccessLevel, Entity.GLOBAL, (dto) -> new GlobalPermissions()),
				Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.YES)));

		AccessControlDto accessControlDto = new AccessControlDto();
		assertThatThrownBy(() -> getAccessLevel(handler, Entity.CALL_LOG, accessControlDto, Permission.ALLOWED, null))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("Unknown action");
	}

	@Test
	@DisplayName("Throws when access level object is invalid type or null")
	void testGetAccessLevelThrowsForInvalidAccessLevelType() {
		AccessLevelHandler handler = new AccessLevelHandler(buildInitializer(
				Map.of(Entity.CALL_LOG, (dto) -> null, Entity.GLOBAL, (dto) -> new GlobalPermissions()),
				Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.YES)));

		AccessControlDto accessControlDto = new AccessControlDto();
		assertThatThrownBy(() -> getAccessLevel(handler, Entity.CALL_LOG, accessControlDto, Permission.CAN_VIEW, null))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("Invalid access level type");
	}

	@Test
	@DisplayName("Throws when access level object is invalid non-null type")
	void testGetAccessLevelThrowsForInvalidAccessLevelTypeNonNull() {
		AccessLevelHandler handler = new AccessLevelHandler(buildInitializer(
				Map.of(Entity.CALL_LOG, (dto) -> new Object(), Entity.GLOBAL, (dto) -> new GlobalPermissions()),
				Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.YES)));

		AccessControlDto accessControlDto = new AccessControlDto();
		assertThatThrownBy(() -> getAccessLevel(handler, Entity.CALL_LOG, accessControlDto, Permission.CAN_VIEW, null))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("Invalid access level type");
	}

	@Test
	@DisplayName("Throws InvalidPermissionLevelException when binary level is used for non-binary permission")
	void testGetAccessLevelThrowsWhenBinaryLevelUsedForNonBinaryPermission() {
		BasicAccessLevel basicAccessLevel = mock(BasicAccessLevel.class);
		given(basicAccessLevel.getCanView()).willReturn(PermissionLevel.YES.getLabel());

		AccessLevelHandler handler = new AccessLevelHandler(buildInitializer(
				Map.of(Entity.CALL_LOG, (dto) -> basicAccessLevel, Entity.GLOBAL, (dto) -> new GlobalPermissions()),
				Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.YES)));

		AccessControlDto accessControlDto = new AccessControlDto();
		assertThatThrownBy(() -> getAccessLevel(handler, Entity.CALL_LOG, accessControlDto, Permission.CAN_VIEW, null))
			.isInstanceOf(InvalidPermissionLevelException.class)
			.hasMessageContaining("YES and NO are not allowed for this permission type");
	}

	@Test
	@DisplayName("Special permission handler throws when access level is not GlobalPermissions (invoked reflectively)")
	void testHandleSpecialPermissionFieldThrowsWhenAccessLevelIsNotGlobalPermissions() throws Exception {
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()),
						Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.NO)));

		Method method = AccessLevelHandler.class.getDeclaredMethod("handleSpecialPermissionField", Object.class,
				Permission.class);
		method.setAccessible(true);

		Object invalidAccessLevel = new Object();
		assertThatThrownBy(() -> method.invoke(handler, invalidAccessLevel, Permission.CAN_ADD))
			.satisfies((throwable) -> {
				Throwable rootCause = getRootCause(throwable);
				assertThat(rootCause).isInstanceOf(UnknownAccessLevelException.class);
				assertThat(rootCause.getMessage()).contains("Invalid access level type for special permission field");
			});
	}

	@Test
	@DisplayName("Special permission handler wraps IllegalArgumentException (invoked reflectively)")
	void testHandleSpecialPermissionFieldWrapsIllegalArgumentException() throws Exception {
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()),
						Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.NO)));

		Method method = AccessLevelHandler.class.getDeclaredMethod("handleSpecialPermissionField", Object.class,
				Permission.class);
		method.setAccessible(true);

		GlobalPermissions globalPermissions = new GlobalPermissions();
		assertThatThrownBy(() -> method.invoke(handler, globalPermissions, Permission.CAN_ADD))
			.satisfies((throwable) -> {
				Throwable rootCause = getRootCause(throwable);
				assertThat(rootCause).isInstanceOf(UnknownAccessLevelException.class);
				assertThat(rootCause.getMessage()).contains("Error processing special permission field");
			});
	}

	@Test
	@DisplayName("Permission level validation is skipped for special permission fields (invoked reflectively)")
	void testValidatePermissionLevelSkipsForSpecialPermissionFields() throws Exception {
		SpecialPermissionFieldHandler.registerSpecialField(Permission.CAN_ADD.name().toLowerCase());

		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()),
						Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.NO)));

		Method method = AccessLevelHandler.class.getDeclaredMethod("validatePermissionLevel", Permission.class,
				PermissionLevel.class, Entity.class);
		method.setAccessible(true);

		Permission permission = Permission.CAN_VIEW;
		PermissionLevel level = PermissionLevel.YES;

		assertThatThrownBy(() -> method.invoke(handler, permission, level, Entity.GLOBAL)).satisfies((throwable) -> {
			Throwable rootCause = getRootCause(throwable);
			assertThat(rootCause).isInstanceOf(InvalidPermissionLevelException.class);
		});

		Permission specialPermission = Permission.CAN_ADD;
		method.invoke(handler, specialPermission, level, Entity.GLOBAL);
	}

	@Test
	@DisplayName("Global non-allowed permission is rejected during direct access-level resolution")
	void testGetAccessLevelGlobalSpecialPermissionBranchIsEvaluated() {
		SpecialPermissionFieldHandler.registerSpecialField(Permission.CAN_ADD.name().toLowerCase());
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()), Map.of()));
		AccessControlDto accessControlDto = new AccessControlDto();

		assertThatThrownBy(() -> getAccessLevel(handler, Entity.GLOBAL, accessControlDto, Permission.CAN_ADD, null))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("Only ALLOWED permission is supported");
	}

	@Test
	@DisplayName("Special permission field detection covers true and false conditions")
	void testIsSpecialPermissionFieldCoversTrueAndFalseConditions() throws Exception {
		SpecialPermissionFieldHandler.registerSpecialField(Permission.CAN_ADD.name().toLowerCase());
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()), Map.of()));

		Method method = AccessLevelHandler.class.getDeclaredMethod("isSpecialPermissionField", Entity.class,
				Permission.class);
		method.setAccessible(true);

		Object globalSpecialFieldResult = method.invoke(handler, Entity.GLOBAL, Permission.CAN_ADD);
		Object nonGlobalResult = method.invoke(handler, Entity.CALL_LOG, Permission.CAN_ADD);
		Object globalNonSpecialFieldResult = method.invoke(handler, Entity.GLOBAL, Permission.CAN_VIEW);

		assertThat(globalSpecialFieldResult).isEqualTo(Boolean.TRUE);
		assertThat(nonGlobalResult).isEqualTo(Boolean.FALSE);
		assertThat(globalNonSpecialFieldResult).isEqualTo(Boolean.FALSE);
	}

	@Test
	@DisplayName("Special permission handler returns mapped permission level for valid processed value")
	void testHandleSpecialPermissionFieldReturnsMappedPermissionLevelForValidProcessedValue() throws Exception {
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()),
						Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.NO)));
		Method method = AccessLevelHandler.class.getDeclaredMethod("handleSpecialPermissionField", Object.class,
				Permission.class);
		method.setAccessible(true);

		try (MockedStatic<io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.SpecialPermissionFieldHandler> specialPermissionFieldHandlerMock = mockStatic(
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.SpecialPermissionFieldHandler.class)) {
			specialPermissionFieldHandlerMock.when(
					() -> io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.SpecialPermissionFieldHandler
						.processSpecialPermissionValue(null))
				.thenReturn(2);

			Object result = method.invoke(handler, new GlobalPermissions(), Permission.CAN_ADD);
			assertThat(result).isEqualTo(PermissionLevel.TEAM_ONLY);
		}
	}

	@Test
	@DisplayName("Special permission handler throws when processed value maps to no special permission value")
	void testHandleSpecialPermissionFieldThrowsWhenProcessedValueIsInvalid() throws Exception {
		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()),
						Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.NO)));
		Method method = AccessLevelHandler.class.getDeclaredMethod("handleSpecialPermissionField", Object.class,
				Permission.class);
		method.setAccessible(true);

		try (MockedStatic<io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.SpecialPermissionFieldHandler> specialPermissionFieldHandlerMock = mockStatic(
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.SpecialPermissionFieldHandler.class)) {
			specialPermissionFieldHandlerMock.when(
					() -> io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.SpecialPermissionFieldHandler
						.processSpecialPermissionValue(null))
				.thenReturn(99);

			assertThatThrownBy(() -> method.invoke(handler, new GlobalPermissions(), Permission.CAN_ADD))
				.satisfies((throwable) -> {
					Throwable rootCause = getRootCause(throwable);
					assertThat(rootCause).isInstanceOf(UnknownAccessLevelException.class);
					assertThat(rootCause.getMessage()).contains("Invalid permission value 99");
				});
		}
	}

	@Test
	@DisplayName("handleSpecialPermissionField converts permission name to lowercase for field lookup")
	void testHandleSpecialPermissionFieldConvertsPermissionToLowercase() throws Exception {
		String lowerCaseFieldName = Permission.CAN_ADD.name().toLowerCase();
		SpecialPermissionFieldHandler.registerSpecialField(lowerCaseFieldName);

		AccessLevelHandler handler = new AccessLevelHandler(
				buildInitializer(Map.of(Entity.GLOBAL, (dto) -> new GlobalPermissions()),
						Map.of(GlobalPermission.REPORTS, (gp) -> PermissionLevel.NO)));

		Method isSpecialMethod = AccessLevelHandler.class.getDeclaredMethod("isSpecialPermissionField", Entity.class,
				Permission.class);
		isSpecialMethod.setAccessible(true);
		assertThat(isSpecialMethod.invoke(handler, Entity.GLOBAL, Permission.CAN_ADD)).isEqualTo(Boolean.TRUE);
		assertThat(SpecialPermissionFieldHandler.isSpecialPermissionField(lowerCaseFieldName)).isTrue();

		Method method = AccessLevelHandler.class.getDeclaredMethod("handleSpecialPermissionField", Object.class,
				Permission.class);
		method.setAccessible(true);

		GlobalPermissions globalPermissions = new GlobalPermissions();
		PermissionLevel result = (PermissionLevel) method.invoke(handler, globalPermissions, Permission.CAN_ADD);

		assertThat(result).isEqualTo(PermissionLevel.NO);
	}

	private static AccessLevelInitializer buildInitializer(Map<Entity, Function<AccessControlDto, ?>> entityGetters,
			Map<GlobalPermission, Function<GlobalPermissions, PermissionLevel>> globalGetters) {
		AccessLevelInitializer initializer = mock(AccessLevelInitializer.class);
		given(initializer.initializeEntityAccessLevelGetters()).willReturn(entityGetters);
		given(initializer.initializeGlobalPermissionGetters()).willReturn(globalGetters);
		return initializer;
	}

	private static PermissionLevel getAccessLevel(AccessLevelHandler handler, Entity entity,
			AccessControlDto accessControlDto, Permission permission, GlobalPermission globalPermission) {
		return handler.getAccessLevel(entity, accessControlDto, permission, globalPermission);
	}

	private static Throwable getRootCause(Throwable throwable) {
		Throwable rootCause = throwable;
		while (rootCause.getCause() != null) {
			rootCause = rootCause.getCause();
		}
		return rootCause;
	}

}
