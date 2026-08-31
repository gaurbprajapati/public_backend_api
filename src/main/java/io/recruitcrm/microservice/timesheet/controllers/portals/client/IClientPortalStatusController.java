/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.controllers.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusUpdateRequestBodyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

/**
 * Controller contract for client portal status endpoints.
 */
public interface IClientPortalStatusController {

	/**
	 * Returns the client portal status for a contact email within the authenticated
	 * user's account.
	 * @param email contact email address
	 * @param rcrmContactId RCRM contact identifier used for contact view access control
	 * @return API response containing portal status details
	 */
	ResponseEntity<?> getPortalStatus(String email, Integer rcrmContactId);

	/**
	 * Updates client portal status for a contact email within an account.
	 * @param request update request payload
	 * @return API response containing updated portal status details
	 */
	ResponseEntity<?> updatePortalStatus(@Valid ClientPortalStatusUpdateRequestBodyDto request);

	/**
	 * Sends client portal invitations to multiple contacts in one request.
	 * @param request bulk invite request payload
	 * @return API response containing invited and skipped contact summaries
	 */
	ResponseEntity<?> bulkEnablePortal(@Valid ClientPortalStatusBulkRequestBodyDto request);

}
