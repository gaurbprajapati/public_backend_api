/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.controllers.contractor.contractor_job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.contractor.IContractorJobService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for ContractorJobJobController class. Tests all methods for 100% line and
 * branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class ContractorJobJobControllerTests {

	@Mock
	private IContractorJobService contractorJobService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private ContractorJobJobController contractorJobJobController;

	private static final String SUCCESS_MESSAGE = "Contractor jobs fetched successfully";

	// ========== Helper Methods ==========

	private static List<ContractorJobResponseBodyDto> createContractorJobList() {
		return Arrays.asList(new ContractorJobResponseBodyDto(1, "Job 1", "Company 1"),
				new ContractorJobResponseBodyDto(2, "Job 2", null));
	}

	private static ResponseEntity<APINormalResponse<List<ContractorJobResponseBodyDto>>> createSuccessResponse(
			List<ContractorJobResponseBodyDto> data) {
		APINormalResponse<List<ContractorJobResponseBodyDto>> response = new APINormalResponse<>(data, SUCCESS_MESSAGE);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ========== getContractorJobs Tests ==========

	@Test
	@DisplayName("Should return contractor jobs successfully")
	void testGetContractorJobsValidRequestReturnsJobs() {
		// Given
		List<ContractorJobResponseBodyDto> expectedJobs = createContractorJobList();
		ResponseEntity<APINormalResponse<List<ContractorJobResponseBodyDto>>> expectedResponse = createSuccessResponse(
				expectedJobs);

		given(this.contractorJobService.getContractorJobs()).willReturn(expectedJobs);
		given(this.apiResponder.respond(expectedJobs, SUCCESS_MESSAGE, APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponse);

		// When
		ResponseEntity<?> response = this.contractorJobJobController.getContractorJobs();

		// Then
		assertThat(response).isEqualTo(expectedResponse);
	}

	@Test
	@DisplayName("Should return empty list when no jobs found")
	void testGetContractorJobsNoJobsReturnsEmptyList() {
		// Given
		List<ContractorJobResponseBodyDto> emptyJobs = List.of();
		ResponseEntity<APINormalResponse<List<ContractorJobResponseBodyDto>>> expectedResponse = createSuccessResponse(
				emptyJobs);

		given(this.contractorJobService.getContractorJobs()).willReturn(emptyJobs);
		given(this.apiResponder.respond(emptyJobs, SUCCESS_MESSAGE, APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponse);

		// When
		ResponseEntity<?> response = this.contractorJobJobController.getContractorJobs();

		// Then
		assertThat(response).isEqualTo(expectedResponse);
	}

}
