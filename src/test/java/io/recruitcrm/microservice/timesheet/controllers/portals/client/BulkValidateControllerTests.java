package io.recruitcrm.microservice.timesheet.controllers.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.portals.client.IBulkValidateService;
import io.recruitcrm.microservice.timesheet.testdata.BulkValidateTestDataFactory;
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
 * Unit tests for BulkValidateController. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class BulkValidateControllerTests {

	@Mock
	private IBulkValidateService bulkValidateService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private BulkValidateController bulkValidateController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Bulk validate returns validation response successfully")
	void testBulkValidateValidRequestReturnsBulkValidateResponse() {
		// Given
		BulkValidateRequestBodyDto request = BulkValidateTestDataFactory.createBulkValidateRequest();
		BulkValidateResponseBodyDto expectedResponse = BulkValidateTestDataFactory.createBulkValidateResponse();
		ResponseEntity<APINormalResponse<BulkValidateResponseBodyDto>> expectedResponseEntity = BulkValidateTestDataFactory
			.createBulkValidateSuccessResponse(expectedResponse);

		given(this.bulkValidateService.bulkValidate(request)).willReturn(expectedResponse);
		given(this.apiResponder.respond(expectedResponse,
				BulkValidateTestDataFactory.Messages.BULK_VALIDATION_COMPLETED, APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.bulkValidateController.bulkValidate(request);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.bulkValidateService).should().bulkValidate(request);
		then(this.apiResponder).should()
			.respond(expectedResponse, BulkValidateTestDataFactory.Messages.BULK_VALIDATION_COMPLETED,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
