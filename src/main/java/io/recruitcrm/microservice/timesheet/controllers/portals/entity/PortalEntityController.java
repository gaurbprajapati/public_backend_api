/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.controllers.portals.entity;

import io.recruitcrm.microservice.timesheet.dto.portal.PortalEntityInfoResponseDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.portals.entity.IPortalEntityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for portal entity operations. Provides endpoints for retrieving entity
 * information (entity type and entity ID) from authenticated principals.
 */
@RestController
@RequestMapping("/v1/portal/entity-info")
public class PortalEntityController implements IPortalEntityController {

	private final APIResponder apiResponder;

	private final IPortalEntityService portalEntityService;

	public PortalEntityController(APIResponder apiResponder, IPortalEntityService portalEntityService) {
		this.apiResponder = apiResponder;
		this.portalEntityService = portalEntityService;
	}

	@Override
	@GetMapping
	public ResponseEntity<?> getPortalEntityInfo() {
		PortalEntityInfoResponseDto response = this.portalEntityService.getPortalEntityInfo();
		return this.apiResponder.respond(response, "Portal entity information fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
