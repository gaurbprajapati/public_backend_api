/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.controllers.portals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.portals.IContractorTimesheetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for ContractorTimesheetController class. Tests all methods for 100% line and
 * branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class ContractorTimesheetControllerTests {

	@Mock
	private IContractorTimesheetService contractorTimesheetService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private ContractorTimesheetController contractorTimesheetController;

	private static final Integer DEFAULT_CONTRACTOR_ID = 1;

	private static final String SUCCESS_MESSAGE = "Timesheet enabled status fetched successfully";

	// ========== Helper Methods ==========

	private static ResponseEntity<APINormalResponse<Integer>> createSuccessResponse(Integer data) {
		APINormalResponse<Integer> response = new APINormalResponse<>(data, SUCCESS_MESSAGE);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ========== isTimesheetEnabled Tests ==========

	@Nested
	@DisplayName("isTimesheetEnabled Tests")
	class IsTimesheetEnabledTests {

		@Test
		@DisplayName("Should return 1 when timesheet is enabled")
		void testIsTimesheetEnabledTimesheetEnabledReturnsOne() {
			// Given
			Integer expectedResponse = 1;
			ResponseEntity<APINormalResponse<Integer>> expectedResponseEntity = createSuccessResponse(expectedResponse);

			given(ContractorTimesheetControllerTests.this.contractorTimesheetService
				.isTimesheetEnabled(DEFAULT_CONTRACTOR_ID)).willReturn(expectedResponse);
			given(ContractorTimesheetControllerTests.this.apiResponder.respond(expectedResponse, SUCCESS_MESSAGE,
					APIResponseType.SUCCESS, HttpStatus.OK))
				.willReturn(expectedResponseEntity);

			// When
			ResponseEntity<?> response = ContractorTimesheetControllerTests.this.contractorTimesheetController
				.isTimesheetEnabled(DEFAULT_CONTRACTOR_ID);

			// Then
			assertThat(response).isEqualTo(expectedResponseEntity);
		}

		@Test
		@DisplayName("Should return 0 when timesheet is disabled")
		void testIsTimesheetEnabledTimesheetDisabledReturnsZero() {
			// Given
			Integer expectedResponse = 0;
			ResponseEntity<APINormalResponse<Integer>> expectedResponseEntity = createSuccessResponse(expectedResponse);

			given(ContractorTimesheetControllerTests.this.contractorTimesheetService
				.isTimesheetEnabled(DEFAULT_CONTRACTOR_ID)).willReturn(expectedResponse);
			given(ContractorTimesheetControllerTests.this.apiResponder.respond(expectedResponse, SUCCESS_MESSAGE,
					APIResponseType.SUCCESS, HttpStatus.OK))
				.willReturn(expectedResponseEntity);

			// When
			ResponseEntity<?> response = ContractorTimesheetControllerTests.this.contractorTimesheetController
				.isTimesheetEnabled(DEFAULT_CONTRACTOR_ID);

			// Then
			assertThat(response).isEqualTo(expectedResponseEntity);
		}

		@Test
		@DisplayName("Should return null when no contract jobs exist")
		void testIsTimesheetEnabledNoContractJobsReturnsNull() {
			// Given
			Integer expectedResponse = null;
			ResponseEntity<APINormalResponse<Integer>> expectedResponseEntity = createSuccessResponse(expectedResponse);

			given(ContractorTimesheetControllerTests.this.contractorTimesheetService
				.isTimesheetEnabled(DEFAULT_CONTRACTOR_ID)).willReturn(expectedResponse);
			given(ContractorTimesheetControllerTests.this.apiResponder.respond(expectedResponse, SUCCESS_MESSAGE,
					APIResponseType.SUCCESS, HttpStatus.OK))
				.willReturn(expectedResponseEntity);

			// When
			ResponseEntity<?> response = ContractorTimesheetControllerTests.this.contractorTimesheetController
				.isTimesheetEnabled(DEFAULT_CONTRACTOR_ID);

			// Then
			assertThat(response).isEqualTo(expectedResponseEntity);
		}

	}

}
