package io.recruitcrm.microservice.timesheet.controllers.invoice;

import io.recruitcrm.microservice.timesheet.dto.invoice.BillDetailsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.BulkInvoiceValidationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.BulkInvoiceValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPayBillHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.invoice.TimesheetInvoiceService;
import io.recruitcrm.microservice.timesheet.testdata.InvoiceTestDataFactory;
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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTests {

	@Mock
	private TimesheetInvoiceService timesheetInvoiceService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private TimesheetInvoiceController timesheetInvoiceController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Get timesheet pay bill history successfully")
	void testGetTimesheetPayBillHistoryValidIdReturnsHistory() {
		// Arrange
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		TimesheetPayBillHistoryResponseBodyDto expectedHistory = InvoiceTestDataFactory
			.createTimesheetPayBillHistoryResponse();
		ResponseEntity<APINormalResponse<TimesheetPayBillHistoryResponseBodyDto>> expectedResponseEntity = InvoiceTestDataFactory
			.createTimesheetPayBillHistorySuccessResponse(expectedHistory);

		Mockito.when(this.timesheetInvoiceService.getTimesheetPayBillHistory(timesheetId)).thenReturn(expectedHistory);
		Mockito.when(this.apiResponder.respond(expectedHistory,
				InvoiceTestDataFactory.Messages.TIMESHEET_PAY_BILL_HISTORY_FETCHED_SUCCESSFULLY,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetInvoiceController.getTimesheetPayBillHistory(timesheetId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetInvoiceService).getTimesheetPayBillHistory(timesheetId);
		Mockito.verify(this.apiResponder)
			.respond(expectedHistory, InvoiceTestDataFactory.Messages.TIMESHEET_PAY_BILL_HISTORY_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get bill details successfully")
	void testGetBillDetailsValidIdReturnsDetails() {
		// Arrange
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		BillDetailsResponseBodyDto expectedDetails = InvoiceTestDataFactory.createBillDetailsResponse();
		ResponseEntity<APINormalResponse<BillDetailsResponseBodyDto>> expectedResponseEntity = InvoiceTestDataFactory
			.createBillDetailsSuccessResponse(expectedDetails);

		Mockito.when(this.timesheetInvoiceService.getBillDetailsByTimesheetId(timesheetId)).thenReturn(expectedDetails);
		Mockito.when(this.apiResponder.respond(expectedDetails,
				InvoiceTestDataFactory.Messages.TIMESHEET_BILL_DETAILS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetInvoiceController.getBillDetails(timesheetId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetInvoiceService).getBillDetailsByTimesheetId(timesheetId);
		Mockito.verify(this.apiResponder)
			.respond(expectedDetails, InvoiceTestDataFactory.Messages.TIMESHEET_BILL_DETAILS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Validate timesheets for invoice successfully")
	void testValidateTimesheetsForInvoiceValidRequestReturnsValidation() {
		// Arrange
		BulkInvoiceValidationRequestBodyDto requestBodyDto = InvoiceTestDataFactory
			.createBulkInvoiceValidationRequest();
		BulkInvoiceValidationResponseBodyDto expectedValidation = InvoiceTestDataFactory
			.createBulkInvoiceValidationResponse();
		ResponseEntity<APINormalResponse<BulkInvoiceValidationResponseBodyDto>> expectedResponseEntity = InvoiceTestDataFactory
			.createBulkInvoiceValidationSuccessResponse(expectedValidation);

		Mockito.when(this.timesheetInvoiceService.validateTimesheetsForInvoice(requestBodyDto.getTimesheetIds()))
			.thenReturn(expectedValidation);
		Mockito.when(this.apiResponder.respond(expectedValidation,
				InvoiceTestDataFactory.Messages.TIMESHEET_VALIDATION_COMPLETED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetInvoiceController.validateTimesheetsForInvoice(requestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetInvoiceService).validateTimesheetsForInvoice(requestBodyDto.getTimesheetIds());
		Mockito.verify(this.apiResponder)
			.respond(expectedValidation, InvoiceTestDataFactory.Messages.TIMESHEET_VALIDATION_COMPLETED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

}