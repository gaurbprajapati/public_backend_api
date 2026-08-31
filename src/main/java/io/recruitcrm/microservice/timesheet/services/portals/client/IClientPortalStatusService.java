/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusUpdateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusUpdateResponseBodyDto;

/**
 * Service for client portal status operations.
 */
public interface IClientPortalStatusService {

	/**
	 * Returns the client portal status for the given email within the authenticated
	 * user's account.
	 * @param email contact email address to look up
	 * @param rcrmContactId RCRM contact identifier used for contact view access control
	 * @return portal status response; defaults to not-sent when no record exists
	 */
	ClientPortalStatusResponseBodyDto getPortalStatus(String email, Integer rcrmContactId);

	/**
	 * Updates client portal status for the given action.
	 * @param request update request payload
	 * @return update response body and success message
	 */
	ClientPortalStatusUpdateResult updatePortalStatus(ClientPortalStatusUpdateRequestBodyDto request);

	/**
	 * Sends client portal invitations to multiple contacts with per-contact skip
	 * handling.
	 * @param request bulk invite request payload
	 * @return invited and skipped contact summaries
	 */
	ClientPortalStatusBulkResponseBodyDto bulkEnablePortal(ClientPortalStatusBulkRequestBodyDto request);

	/**
	 * Service-layer result for portal status update operations.
	 *
	 * @param responseBody response payload
	 * @param successMessage controller success message
	 */
	record ClientPortalStatusUpdateResult(ClientPortalStatusUpdateResponseBodyDto responseBody, String successMessage) {
	}

}
