/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.portals.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for client portal status lookup by email and account.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPortalStatusResponseBodyDto {

	private Integer portalStatusId;

	private Integer companyId;

	private String vmsUserEmail;

	private Integer vmsUserId;

	private Integer inviteCount;

	private Integer inviteSentOn;

	private Integer inviteSentByUserId;

	private Integer updatedOn;

	private Boolean portalExistsOnCrossAgency;

}
