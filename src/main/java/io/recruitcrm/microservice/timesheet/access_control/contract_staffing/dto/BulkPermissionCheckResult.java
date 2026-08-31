/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkPermissionCheckResult {

	/**
	 * List of permission check results
	 */
	private List<BulkPermissionCheckResultItem> results;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class BulkPermissionCheckResultItem {

		/**
		 * Whether the permission check was successful
		 */
		private boolean allowed;

		/**
		 * Error message if permission was denied, null if allowed
		 */
		private String errorMessage;

	}

}