/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.controllers.portals.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusBulkResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusUpdateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusUpdateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.constants.ClientPortalStatusConstants;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.portals.client.IClientPortalStatusService;
import io.recruitcrm.microservice.timesheet.services.portals.client.IClientPortalStatusService.ClientPortalStatusUpdateResult;
import io.recruitcrm.microservice.timesheet.testdata.ClientPortalStatusTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for {@link ClientPortalStatusController}.
 */
@ExtendWith(MockitoExtension.class)
class ClientPortalStatusControllerTests {

	@Mock
	private IClientPortalStatusService clientPortalStatusService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private ClientPortalStatusController clientPortalStatusController;

	@Test
	@DisplayName("Get portal status returns success response")
	void testGetPortalStatusValidRequestReturnsSuccessResponse() {
		// Given
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer rcrmContactId = ClientPortalStatusTestDataFactory.getDefaultRcrmContactId();
		ClientPortalStatusResponseBodyDto responseBody = ClientPortalStatusTestDataFactory.createResponseBody();
		ResponseEntity<APINormalResponse<ClientPortalStatusResponseBodyDto>> expectedResponseEntity = ClientPortalStatusTestDataFactory
			.createSuccessResponseEntity(responseBody);

		given(this.clientPortalStatusService.getPortalStatus(email, rcrmContactId)).willReturn(responseBody);
		given(this.apiResponder.respond(responseBody, ClientPortalStatusConstants.FETCH_SUCCESS_MESSAGE,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.clientPortalStatusController.getPortalStatus(email, rcrmContactId);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.clientPortalStatusService).should().getPortalStatus(email, rcrmContactId);
		then(this.apiResponder).should()
			.respond(responseBody, ClientPortalStatusConstants.FETCH_SUCCESS_MESSAGE, APIResponseType.SUCCESS,
					HttpStatus.OK);
	}

	@Test
	@DisplayName("Update portal status returns success response")
	void testUpdatePortalStatusValidRequestReturnsSuccessResponse() {
		// Given
		ClientPortalStatusUpdateRequestBodyDto request = ClientPortalStatusTestDataFactory.createSendInviteRequest();
		ClientPortalStatusUpdateResult updateResult = ClientPortalStatusTestDataFactory.createUpdateResult();
		ClientPortalStatusUpdateResponseBodyDto responseBody = updateResult.responseBody();
		ResponseEntity<APINormalResponse<ClientPortalStatusUpdateResponseBodyDto>> expectedResponseEntity = new ResponseEntity<>(
				new APINormalResponse<>(responseBody), HttpStatus.OK);

		given(this.clientPortalStatusService.updatePortalStatus(request)).willReturn(updateResult);
		given(this.apiResponder.respond(responseBody, ClientPortalStatusConstants.INVITE_SUCCESS_MESSAGE,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.clientPortalStatusController.updatePortalStatus(request);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.clientPortalStatusService).should().updatePortalStatus(request);
		then(this.apiResponder).should()
			.respond(responseBody, ClientPortalStatusConstants.INVITE_SUCCESS_MESSAGE, APIResponseType.SUCCESS,
					HttpStatus.OK);
	}

	@Test
	@DisplayName("Bulk enable portal returns success response")
	void testBulkEnablePortalValidRequestReturnsSuccessResponse() {
		// Given
		ClientPortalStatusBulkRequestBodyDto request = ClientPortalStatusTestDataFactory.createBulkInviteRequest();
		ClientPortalStatusBulkResponseBodyDto responseBody = ClientPortalStatusTestDataFactory
			.createBulkInviteResponse();
		ResponseEntity<APINormalResponse<ClientPortalStatusBulkResponseBodyDto>> expectedResponseEntity = new ResponseEntity<>(
				new APINormalResponse<>(responseBody), HttpStatus.OK);

		given(this.clientPortalStatusService.bulkEnablePortal(request)).willReturn(responseBody);
		given(this.apiResponder.respond(responseBody, ClientPortalStatusConstants.BULK_INVITE_SUCCESS_MESSAGE,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.clientPortalStatusController.bulkEnablePortal(request);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.clientPortalStatusService).should().bulkEnablePortal(request);
		then(this.apiResponder).should()
			.respond(responseBody, ClientPortalStatusConstants.BULK_INVITE_SUCCESS_MESSAGE, APIResponseType.SUCCESS,
					HttpStatus.OK);
	}

}
