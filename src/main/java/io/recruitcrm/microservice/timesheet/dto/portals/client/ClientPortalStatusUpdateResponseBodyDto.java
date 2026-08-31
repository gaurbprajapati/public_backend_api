/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.portals.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for {@code POST /v1/portal/client/portal-status}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPortalStatusUpdateResponseBodyDto {

	private Integer portalStatusId;

	private String portalStatusLabel;

	private String email;

}
