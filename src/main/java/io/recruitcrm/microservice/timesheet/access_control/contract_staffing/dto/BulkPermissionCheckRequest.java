/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.BulkDataLoadingStrategy;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkPermissionCheckRequest {

	/**
	 * List of permission check requests to be processed in bulk
	 */
	private List<BulkPermissionCheckItem> items;

	/**
	 * Optional explicit data loading strategy. If not provided, the strategy will be
	 * automatically determined based on the entity types in the request items. This
	 * allows callers to override the default strategy when needed.
	 */
	private BulkDataLoadingStrategy explicitStrategy;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class BulkPermissionCheckItem {

		/**
		 * The entity to check permissions for
		 */
		private Entity entity;

		/**
		 * Permission check context containing permission and level
		 */
		private PermissionCheckContext permissionCheckContext;

		/**
		 * Access control metadata context
		 */
		private AccessControlCheckMetadataContext accessControlCheckMetadataContext;

	}

}