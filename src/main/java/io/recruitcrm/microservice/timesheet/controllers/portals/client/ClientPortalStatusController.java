/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.controllers.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusUpdateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.constants.ClientPortalStatusConstants;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.portals.client.IClientPortalStatusService.ClientPortalStatusUpdateResult;
import io.recruitcrm.microservice.timesheet.services.portals.client.IClientPortalStatusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for client portal status lookup used by the RCRM frontend.
 */
@RestController
@RequestMapping("/v1/portal/client/portal-status")
public class ClientPortalStatusController implements IClientPortalStatusController {

	private final IClientPortalStatusService clientPortalStatusService;

	private final APIResponder apiResponder;

	public ClientPortalStatusController(IClientPortalStatusService clientPortalStatusService,
			APIResponder apiResponder) {
		this.clientPortalStatusService = clientPortalStatusService;
		this.apiResponder = apiResponder;
	}

	@Override
	@GetMapping
	public ResponseEntity<?> getPortalStatus(@RequestParam(name = "email", required = false) String email,
			@RequestParam(name = "contactId", required = false) Integer rcrmContactId) {
		ClientPortalStatusResponseBodyDto response = this.clientPortalStatusService.getPortalStatus(email,
				rcrmContactId);
		return this.apiResponder.respond(response, ClientPortalStatusConstants.FETCH_SUCCESS_MESSAGE,
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@PostMapping
	public ResponseEntity<?> updatePortalStatus(@Valid @RequestBody ClientPortalStatusUpdateRequestBodyDto request) {
		ClientPortalStatusUpdateResult result = this.clientPortalStatusService.updatePortalStatus(request);
		return this.apiResponder.respond(result.responseBody(), result.successMessage(), APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PostMapping("/bulk")
	public ResponseEntity<?> bulkEnablePortal(@Valid @RequestBody ClientPortalStatusBulkRequestBodyDto request) {
		ClientPortalStatusBulkResponseBodyDto response = this.clientPortalStatusService.bulkEnablePortal(request);
		return this.apiResponder.respond(response, ClientPortalStatusConstants.BULK_INVITE_SUCCESS_MESSAGE,
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
