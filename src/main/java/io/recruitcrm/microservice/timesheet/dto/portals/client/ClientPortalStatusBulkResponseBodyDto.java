/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.portals.client;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for {@code POST /v1/portal/client/portal-status/bulk}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPortalStatusBulkResponseBodyDto {

	private List<String> invited;

	private List<ClientPortalStatusBulkSkippedContactDto> skipped;

	private int invitedCount;

	private int skippedCount;

}
