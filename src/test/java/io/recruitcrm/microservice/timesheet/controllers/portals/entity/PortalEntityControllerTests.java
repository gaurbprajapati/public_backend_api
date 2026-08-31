/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.controllers.portals.entity;

import io.recruitcrm.microservice.timesheet.dto.portal.PortalEntityInfoResponseDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.services.portals.entity.IPortalEntityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Unit tests for PortalEntityController class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class PortalEntityControllerTests {

	@Mock
	private APIResponder apiResponder;

	@Mock
	private IPortalEntityService portalEntityService;

	@InjectMocks
	private PortalEntityController portalEntityController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Get portal entity info successfully")
	void testGetPortalEntityInfoValidRequestReturnsEntityInfo() {
		// Given
		PortalEntityInfoResponseDto expectedResponse = new PortalEntityInfoResponseDto(3, 1);
		ResponseEntity<APINormalResponse<PortalEntityInfoResponseDto>> expectedResponseEntity = new ResponseEntity<>(
				new APINormalResponse<>(expectedResponse), HttpStatus.OK);

		given(this.portalEntityService.getPortalEntityInfo()).willReturn(expectedResponse);
		given(this.apiResponder.respond(expectedResponse, "Portal entity information fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.portalEntityController.getPortalEntityInfo();

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.portalEntityService).should().getPortalEntityInfo();
		then(this.apiResponder).should()
			.respond(expectedResponse, "Portal entity information fetched successfully", APIResponseType.SUCCESS,
					HttpStatus.OK);
	}

}
