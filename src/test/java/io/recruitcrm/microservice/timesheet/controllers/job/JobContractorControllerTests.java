package io.recruitcrm.microservice.timesheet.controllers.job;

import io.recruitcrm.microservice.timesheet.dto.job.GetTimesheetEnabledAssignedCandidatesRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.job.TimesheetEnabledAssignedCandidateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIErrorResponse;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.job.IJobContractorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobContractorController Tests")
class JobContractorControllerTests {

	@Mock
	private IJobContractorService jobContractorService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private JobContractorController jobContractorController;

	@BeforeEach
	void setUp() {
		this.jobContractorController = new JobContractorController(this.jobContractorService, this.apiResponder);
	}

	@Test
	@DisplayName("Get timesheet enabled assigned candidates successfully")
	void testGetTimesheetEnabledAssignedCandidatesValidRequestReturnsCandidates() {
		// Arrange
		List<Integer> jobIds = List.of(1, 2);
		GetTimesheetEnabledAssignedCandidatesRequestBodyDto request = new GetTimesheetEnabledAssignedCandidatesRequestBodyDto(
				jobIds);
		List<TimesheetEnabledAssignedCandidateResponseBodyDto> expectedCandidates = List
			.of(TimesheetEnabledAssignedCandidateResponseBodyDto.builder().id(10).jobId(1).build());
		ResponseEntity<APINormalResponse<List<TimesheetEnabledAssignedCandidateResponseBodyDto>>> expectedResponseEntity = new ResponseEntity<>(
				new APINormalResponse<>(expectedCandidates), HttpStatus.OK);

		Mockito.when(this.jobContractorService.getTimesheetEnabledAssignedCandidates(jobIds))
			.thenReturn(expectedCandidates);
		Mockito.when(this.apiResponder.respond(expectedCandidates,
				"Timesheet enabled assigned candidates fetched successfully", APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.jobContractorController.getTimesheetEnabledAssignedCandidates(request);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.jobContractorService).getTimesheetEnabledAssignedCandidates(jobIds);
		Mockito.verify(this.apiResponder)
			.respond(expectedCandidates, "Timesheet enabled assigned candidates fetched successfully",
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("getTimesheetEnabledAssignedCandidates logs and returns an error response when the service throws")
	void testGetTimesheetEnabledAssignedCandidatesReturnsErrorWhenServiceThrows() {
		// Arrange
		List<Integer> jobIds = List.of(101);
		GetTimesheetEnabledAssignedCandidatesRequestBodyDto request = new GetTimesheetEnabledAssignedCandidatesRequestBodyDto(
				jobIds);
		RuntimeException failure = new RuntimeException("service unavailable");
		ResponseEntity<APIErrorResponse> errorResponse = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

		Mockito.when(this.jobContractorService.getTimesheetEnabledAssignedCandidates(jobIds)).thenThrow(failure);
		Mockito
			.when(this.apiResponder.respondWithError(failure, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR))
			.thenReturn(errorResponse);

		// Act
		ResponseEntity<?> response = this.jobContractorController.getTimesheetEnabledAssignedCandidates(request);

		// Assert
		assertThat(response).isEqualTo(errorResponse);
		Mockito.verify(this.jobContractorService).getTimesheetEnabledAssignedCandidates(jobIds);
		Mockito.verify(this.apiResponder)
			.respondWithError(failure, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
