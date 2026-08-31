/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.portals.entity;

import io.recruitcrm.microservice.timesheet.dto.portal.PortalEntityInfoResponseDto;

/**
 * Service interface for portal entity operations. Handles extraction and retrieval of
 * entity information (entity type and entity ID) from authenticated principals.
 */
public interface IPortalEntityService {

	/**
	 * Get portal entity information from the authenticated principal. Extracts entity
	 * type and entity ID from the JWT token.
	 * @return PortalEntityInfoResponseDto containing entity type and entity ID
	 */
	PortalEntityInfoResponseDto getPortalEntityInfo();

}
