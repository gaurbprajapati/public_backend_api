/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.portals.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contact entry for bulk client portal invite requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPortalStatusBulkContactDto {

	private String email;

	private String firstName;

	private String lastName;

	private Integer rcrmContactId;

}
