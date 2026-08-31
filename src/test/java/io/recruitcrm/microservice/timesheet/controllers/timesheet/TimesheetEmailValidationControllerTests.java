package io.recruitcrm.microservice.timesheet.controllers.timesheet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetEmailValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ValidateTimesheetEmailRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.timesheet.TimesheetEmailValidationService;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetEmailValidationTestDataFactory;
import java.util.Objects;
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

@ExtendWith(MockitoExtension.class)
class TimesheetEmailValidationControllerTests {

	@Mock
	private TimesheetEmailValidationService timesheetEmailValidationService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private TimesheetEmailValidationController timesheetEmailValidationController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Validate timesheet emails with contractor request returns success response")
	void testValidateTimesheetEmailsValidContractorRequestReturnsSuccess() {
		// Arrange
		ValidateTimesheetEmailRequestBodyDto request = TimesheetEmailValidationTestDataFactory
			.createValidateTimesheetEmailRequestContractor();
		TimesheetEmailValidationResponseBodyDto expectedData = TimesheetEmailValidationTestDataFactory
			.createTimesheetEmailValidationResponseBodyDto();
		ResponseEntity<APINormalResponse<TimesheetEmailValidationResponseBodyDto>> expectedResponseEntity = TimesheetEmailValidationTestDataFactory
			.createTimesheetEmailValidationSuccessResponse(expectedData);

		Mockito.when(this.timesheetEmailValidationService.validateTimesheetEmails(request)).thenReturn(expectedData);
		Mockito.when(this.apiResponder.respond(expectedData,
				TimesheetEmailValidationTestDataFactory.Messages.TIMESHEET_VALIDATION_FETCHED_SUCCESSFULLY,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetEmailValidationController.validateTimesheetEmails(request);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetEmailValidationService).validateTimesheetEmails(request);
		Mockito.verify(this.apiResponder)
			.respond(expectedData,
					TimesheetEmailValidationTestDataFactory.Messages.TIMESHEET_VALIDATION_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Validate timesheet emails with approver request returns success response")
	void testValidateTimesheetEmailsValidApproverRequestReturnsSuccess() {
		// Arrange
		ValidateTimesheetEmailRequestBodyDto request = TimesheetEmailValidationTestDataFactory
			.createValidateTimesheetEmailRequestApprover();
		TimesheetEmailValidationResponseBodyDto expectedData = TimesheetEmailValidationTestDataFactory
			.createEmptyTimesheetEmailValidationResponseBodyDto();
		ResponseEntity<APINormalResponse<TimesheetEmailValidationResponseBodyDto>> expectedResponseEntity = TimesheetEmailValidationTestDataFactory
			.createTimesheetEmailValidationSuccessResponse(expectedData);

		Mockito.when(this.timesheetEmailValidationService.validateTimesheetEmails(request)).thenReturn(expectedData);
		Mockito.when(this.apiResponder.respond(expectedData,
				TimesheetEmailValidationTestDataFactory.Messages.TIMESHEET_VALIDATION_FETCHED_SUCCESSFULLY,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetEmailValidationController.validateTimesheetEmails(request);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull().isInstanceOf(APINormalResponse.class);
		@SuppressWarnings("unchecked")
		APINormalResponse<TimesheetEmailValidationResponseBodyDto> body = (APINormalResponse<TimesheetEmailValidationResponseBodyDto>) Objects
			.requireNonNull(response.getBody());
		assertThat(body.getData()).isEqualTo(expectedData);
		Mockito.verify(this.timesheetEmailValidationService).validateTimesheetEmails(request);
		Mockito.verify(this.apiResponder)
			.respond(expectedData,
					TimesheetEmailValidationTestDataFactory.Messages.TIMESHEET_VALIDATION_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Validate timesheet emails with multiple timesheet ids delegates to service")
	void testValidateTimesheetEmailsMultipleTimesheetIdsDelegatesToService() {
		// Arrange
		ValidateTimesheetEmailRequestBodyDto request = TimesheetEmailValidationTestDataFactory
			.createValidateTimesheetEmailRequestContractorMultipleIds();
		TimesheetEmailValidationResponseBodyDto expectedData = TimesheetEmailValidationTestDataFactory
			.createTimesheetEmailValidationResponseBodyDto();
		ResponseEntity<APINormalResponse<TimesheetEmailValidationResponseBodyDto>> expectedResponseEntity = TimesheetEmailValidationTestDataFactory
			.createTimesheetEmailValidationSuccessResponse(expectedData);

		Mockito.when(this.timesheetEmailValidationService.validateTimesheetEmails(request)).thenReturn(expectedData);
		Mockito.when(this.apiResponder.respond(expectedData,
				TimesheetEmailValidationTestDataFactory.Messages.TIMESHEET_VALIDATION_FETCHED_SUCCESSFULLY,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetEmailValidationController.validateTimesheetEmails(request);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetEmailValidationService).validateTimesheetEmails(request);
	}

	@Test
	@DisplayName("Validate timesheet emails propagates runtime exception when service throws")
	void testValidateTimesheetEmailsServiceThrowsRuntimeExceptionPropagates() {
		// Arrange
		ValidateTimesheetEmailRequestBodyDto request = TimesheetEmailValidationTestDataFactory
			.createValidateTimesheetEmailRequestApprover();
		Mockito.when(this.timesheetEmailValidationService.validateTimesheetEmails(request))
			.thenThrow(new RuntimeException("temporary failure"));

		// Act & Assert
		assertThatThrownBy(() -> this.timesheetEmailValidationController.validateTimesheetEmails(request))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("temporary failure");
		Mockito.verifyNoInteractions(this.apiResponder);
	}

	@Test
	@DisplayName("Validate timesheet emails propagates exception when service throws")
	void testValidateTimesheetEmailsServiceThrowsPropagatesException() {
		// Arrange
		ValidateTimesheetEmailRequestBodyDto request = TimesheetEmailValidationTestDataFactory
			.createValidateTimesheetEmailRequestContractor();
		Mockito.when(this.timesheetEmailValidationService.validateTimesheetEmails(request))
			.thenThrow(new ValidationErrorException("entity_type_id must be 1 or 3"));

		// Act & Assert
		assertThatThrownBy(() -> this.timesheetEmailValidationController.validateTimesheetEmails(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("entity_type_id must be 1 or 3");
		Mockito.verify(this.timesheetEmailValidationService).validateTimesheetEmails(request);
		Mockito.verifyNoInteractions(this.apiResponder);
	}

}
