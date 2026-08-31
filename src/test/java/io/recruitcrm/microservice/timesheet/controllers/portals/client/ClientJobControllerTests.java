package io.recruitcrm.microservice.timesheet.controllers.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.ClientJobResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.portals.client.IClientJobService;
import io.recruitcrm.microservice.timesheet.testdata.ClientJobTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Unit tests for ClientJobController. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class ClientJobControllerTests {

	@Mock
	private IClientJobService clientJobService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private ClientJobController clientJobController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Fetch client jobs returns job list successfully")
	void testFetchClientJobsReturnsJobList() {
		// Given
		List<ClientJobResponseBodyDto> expectedResponse = ClientJobTestDataFactory.createJobResponseList();
		ResponseEntity<APINormalResponse<List<ClientJobResponseBodyDto>>> expectedResponseEntity = ClientJobTestDataFactory
			.createSuccessResponse(expectedResponse);

		given(this.clientJobService.fetchClientJobs()).willReturn(expectedResponse);
		given(this.apiResponder.respond(expectedResponse, ClientJobTestDataFactory.Messages.JOBS_FETCHED_SUCCESSFULLY,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.clientJobController.fetchClientJobs();

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.clientJobService).should().fetchClientJobs();
		then(this.apiResponder).should()
			.respond(expectedResponse, ClientJobTestDataFactory.Messages.JOBS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
