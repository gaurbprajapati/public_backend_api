/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.portals.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Skipped contact entry for bulk client portal invite responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPortalStatusBulkSkippedContactDto {

	private String email;

	private String firstName;

	private String reason;

}
