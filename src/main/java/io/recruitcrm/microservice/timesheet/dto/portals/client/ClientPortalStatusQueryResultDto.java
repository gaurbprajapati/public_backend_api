/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.portals.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal query projection for {@code client_portal_status_t} rows.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPortalStatusQueryResultDto {

	private Integer portalStatusId;

	private Integer companyId;

	private String vmsUserEmail;

	private Integer vmsUserId;

	private Integer inviteCount;

	private Integer inviteSentOn;

	private Integer inviteSentByUserId;

	private Integer updatedOn;

}
