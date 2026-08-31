/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Entity;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.GlobalPermission;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum Permission {

	CONFIGURE_TIMESHEET_SETTINGS("configure_timesheet_settings",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT),
					new LinkedEntityPermission(Entity.JOBS,
							io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT)),
			List.of()),
	VIEW_TIMESHEET("view_timesheet",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_VIEW),
					new LinkedEntityPermission(Entity.JOBS,
							io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_VIEW)),
			List.of()),
	EDIT_TIMESHEET("edit_timesheet",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT),
					new LinkedEntityPermission(Entity.JOBS,
							io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT)

			), List.of()),
	MODIFY_TIMESHEET_PAY_BILL_STRUCTURE("modify_timesheet_pay_bill_structure",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT),
					new LinkedEntityPermission(Entity.JOBS,
							io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT)),
			List.of()),
	CREATE_TIMESHEET("create_timesheet",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT)),
			List.of()),
	CREATE_TIMESHEET_SETTINGS("create_timesheet_settings",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT)),
			List.of()),
	DELETE_TIMESHEET("delete_timesheet",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_DELETE)),
			List.of()),
	SUBMIT_TIMESHEET("submit_timesheet",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT)),
			List.of()),
	APPROVE_TIMESHEET("approve_timesheet",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT)),
			List.of()),
	REJECT_TIMESHEET("reject_timesheet",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT)),
			List.of()),
	EXPORT_TIMESHEET("export_timesheet", List.of(),
			List.of(new LinkedEntityGlobalPermission(Entity.GLOBAL, GlobalPermission.EXPORT_TO_CSV))),
	EXPORT_TIMESHEET_REPORT("export_timesheet_report", List.of(),
			List.of(new LinkedEntityGlobalPermission(Entity.GLOBAL, GlobalPermission.EXPORT_TO_CSV))),
	ADD_TIME_IN_TIMESHEET("add_time_in_timesheet",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_EDIT)),
			List.of()),
	VIEW_CONTRACTOR_DETAILS_PAGE("view_contractor_details_page",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_VIEW)),
			List.of()),
	VIEW_CONTRACTOR_DETAILS("view_contractor_details",
			List.of(new LinkedEntityPermission(Entity.CANDIDATES,
					io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission.CAN_VIEW)),
			List.of());

	private final String label;

	@Getter
	private final List<LinkedEntityPermission> linkedEntities;

	@Getter
	private final List<LinkedEntityGlobalPermission> linkedEntityGlobalPermissions;

	Permission(String label, List<LinkedEntityPermission> linkedEntities,
			List<LinkedEntityGlobalPermission> linkedEntityGlobalPermissions) {
		this.label = label;
		this.linkedEntities = linkedEntities;
		this.linkedEntityGlobalPermissions = linkedEntityGlobalPermissions;
	}

	public static Permission fromValue(String value) {
		for (Permission permission : Permission.values()) {
			if (permission.label.equals(value)) {
				return permission;
			}
		}
		throw new IllegalArgumentException("Unknown action: " + value);
	}

	@Override
	public String toString() {
		return this.label;
	}

	public record LinkedEntityPermission(Entity entity,
			io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.Permission permission) {

		@NotNull
		@Override
		public String toString() {
			return "LinkedEntityPermission{" + "entity=" + this.entity + ", permission=" + this.permission + '}';
		}
	}

	public record LinkedEntityGlobalPermission(Entity entity,
			io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.GlobalPermission globalPermission) {

		@NotNull
		@Override
		public String toString() {
			return "LinkedEntityGlobalPermission{" + "entity=" + this.entity + ", globalPermission="
					+ this.globalPermission + '}';
		}
	}

}
