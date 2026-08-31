/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for portal entity information. Returns the entity type and entity ID
 * extracted from the authenticated principal's JWT token. This is used by portal
 * endpoints to identify whether the request is from a contractor or contact.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortalEntityInfoResponseDto {

	/**
	 * Entity type (3 = contractor, 1 = contact)
	 */
	private Integer entityType;

	/**
	 * Entity ID (contractor ID or contact ID)
	 */
	private Integer entityId;

}
