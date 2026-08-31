/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.controllers.portals.entity;

import org.springframework.http.ResponseEntity;

/**
 * Controller interface for portal entity operations. Provides endpoints for retrieving
 * entity information from authenticated principals.
 */
public interface IPortalEntityController {

	/**
	 * Get portal entity information (entity type and entity ID) from the authenticated
	 * principal.
	 * @return ResponseEntity containing PortalEntityInfoResponseDto
	 */
	ResponseEntity<?> getPortalEntityInfo();

}
