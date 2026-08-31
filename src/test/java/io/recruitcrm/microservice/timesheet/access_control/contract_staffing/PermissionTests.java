/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.recruitcrm.microservice.timesheet.testdata.ContractStaffingPermissionTestDataFactory;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

@DisplayName("Contract staffing Permission Tests")
class PermissionTests {

	private static String expectedLabel(Permission permission) {
		return switch (permission) {
			case CONFIGURE_TIMESHEET_SETTINGS -> "configure_timesheet_settings";
			case VIEW_TIMESHEET -> "view_timesheet";
			case EDIT_TIMESHEET -> "edit_timesheet";
			case MODIFY_TIMESHEET_PAY_BILL_STRUCTURE -> "modify_timesheet_pay_bill_structure";
			case CREATE_TIMESHEET -> "create_timesheet";
			case CREATE_TIMESHEET_SETTINGS -> "create_timesheet_settings";
			case DELETE_TIMESHEET -> "delete_timesheet";
			case SUBMIT_TIMESHEET -> "submit_timesheet";
			case APPROVE_TIMESHEET -> "approve_timesheet";
			case REJECT_TIMESHEET -> "reject_timesheet";
			case EXPORT_TIMESHEET -> "export_timesheet";
			case EXPORT_TIMESHEET_REPORT -> "export_timesheet_report";
			case ADD_TIME_IN_TIMESHEET -> "add_time_in_timesheet";
			case VIEW_CONTRACTOR_DETAILS_PAGE -> "view_contractor_details_page";
			case VIEW_CONTRACTOR_DETAILS -> "view_contractor_details";
		};
	}

	private static Arguments permissionAndCanonicalLabelArguments(Permission permission) {
		return Arguments.of(permission, expectedLabel(permission));
	}

	static Stream<Arguments> permissionsAndCanonicalLabels() {
		return Arrays.stream(Permission.values()).map(PermissionTests::permissionAndCanonicalLabelArguments);
	}

	@Test
	@DisplayName("values should contain all fifteen permission constants")
	void testValuesContainsExactlyFifteenPermissions() {
		// When
		Permission[] values = Permission.values();

		// Then
		assertThat(values).hasSize(ContractStaffingPermissionTestDataFactory.PERMISSION_ENUM_CONSTANT_COUNT)
			.containsExactlyInAnyOrder(Permission.CONFIGURE_TIMESHEET_SETTINGS, Permission.VIEW_TIMESHEET,
					Permission.EDIT_TIMESHEET, Permission.MODIFY_TIMESHEET_PAY_BILL_STRUCTURE,
					Permission.CREATE_TIMESHEET, Permission.CREATE_TIMESHEET_SETTINGS, Permission.DELETE_TIMESHEET,
					Permission.SUBMIT_TIMESHEET, Permission.APPROVE_TIMESHEET, Permission.REJECT_TIMESHEET,
					Permission.EXPORT_TIMESHEET, Permission.EXPORT_TIMESHEET_REPORT, Permission.ADD_TIME_IN_TIMESHEET,
					Permission.VIEW_CONTRACTOR_DETAILS_PAGE, Permission.VIEW_CONTRACTOR_DETAILS);
	}

	@ParameterizedTest
	@EnumSource(Permission.class)
	@DisplayName("toString should return the configured label for each constant")
	void testToStringReturnsLabelForEachEnum(Permission permission) {
		// Given
		String expected = expectedLabel(permission);

		// When
		String result = permission.toString();

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource("permissionsAndCanonicalLabels")
	@DisplayName("fromValue should resolve each exact canonical label")
	void testFromValueWithCanonicalLabelReturnsMatchingPermission(Permission expected, String label) {
		// When
		Permission result = Permission.fromValue(label);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("fromValue should be case-sensitive (not ignore case)")
	void testFromValueWithDifferentCasingThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> Permission.fromValue("VIEW_TIMESHEET")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(ContractStaffingPermissionTestDataFactory.UNKNOWN_ACTION_MESSAGE_PREFIX)
			.hasMessageContaining("VIEW_TIMESHEET");
	}

	@Test
	@DisplayName("fromValue should throw when action is unknown")
	void testFromValueWithUnknownActionThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(
				() -> Permission.fromValue(ContractStaffingPermissionTestDataFactory.INVALID_PERMISSION_ACTION_VALUE))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(ContractStaffingPermissionTestDataFactory.UNKNOWN_ACTION_MESSAGE_PREFIX)
			.hasMessageContaining(ContractStaffingPermissionTestDataFactory.INVALID_PERMISSION_ACTION_VALUE);
	}

	@ParameterizedTest
	@NullSource
	@DisplayName("fromValue should throw when value is null")
	void testFromValueWithNullThrowsIllegalArgumentException(String value) {
		// When & Then
		assertThatThrownBy(() -> Permission.fromValue(value)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(ContractStaffingPermissionTestDataFactory.UNKNOWN_ACTION_MESSAGE_PREFIX)
			.hasMessageContaining("null");
	}

	@ParameterizedTest
	@EnumSource(Permission.class)
	@DisplayName("getLinkedEntities and getLinkedEntityGlobalPermissions should never be null")
	void testLinkedPermissionListsAreNeverNull(Permission permission) {
		// When
		List<Permission.LinkedEntityPermission> linkedEntities = permission.getLinkedEntities();
		List<Permission.LinkedEntityGlobalPermission> globalLinked = permission.getLinkedEntityGlobalPermissions();

		// Then
		assertThat(linkedEntities).isNotNull();
		assertThat(globalLinked).isNotNull();
	}

	@Test
	@DisplayName("EXPORT_TIMESHEET should rely on global linked permissions only")
	void testExportTimesheetUsesGlobalLinkedPermissionsOnly() {
		// When
		Permission exportTimesheet = Permission.EXPORT_TIMESHEET;

		// Then
		assertThat(exportTimesheet.getLinkedEntities()).isEmpty();
		assertThat(exportTimesheet.getLinkedEntityGlobalPermissions()).hasSize(1);
	}

	@Test
	@DisplayName("LinkedEntityPermission toString should include type and fields")
	void testLinkedEntityPermissionToStringContainsDetails() {
		// Given
		Permission.LinkedEntityPermission linked = Permission.VIEW_TIMESHEET.getLinkedEntities().get(0);

		// When
		String representation = linked.toString();

		// Then
		assertThat(representation).contains("LinkedEntityPermission").contains("entity=").contains("permission=");
	}

	@Test
	@DisplayName("LinkedEntityGlobalPermission toString should include type and fields")
	void testLinkedEntityGlobalPermissionToStringContainsDetails() {
		// Given
		Permission.LinkedEntityGlobalPermission linked = Permission.EXPORT_TIMESHEET.getLinkedEntityGlobalPermissions()
			.get(0);

		// When
		String representation = linked.toString();

		// Then
		assertThat(representation).contains("LinkedEntityGlobalPermission")
			.contains("entity=")
			.contains("globalPermission=");
	}

}
