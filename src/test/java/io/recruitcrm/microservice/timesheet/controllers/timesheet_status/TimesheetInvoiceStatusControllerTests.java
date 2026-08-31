package io.recruitcrm.microservice.timesheet.controllers.timesheet_status;

import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetPayBillStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.timesheet_status.TimesheetInvoiceStatusService;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetStatusTestDataFactory;
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
class TimesheetInvoiceStatusControllerTests {

	@Mock
	private TimesheetInvoiceStatusService timesheetInvoiceStatusService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private TimesheetInvoiceStatusController timesheetInvoiceStatusController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Update timesheet pay bill status successfully")
	void testUpdateTimesheetPayBillStatusValidRequestUpdatesStatus() {
		// Arrange
		Integer timesheetId = TimesheetStatusTestDataFactory.getDefaultTimesheetId();
		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = TimesheetStatusTestDataFactory
			.createUpdateTimesheetPayBillStatusRequest();
		ResponseEntity<APINormalResponse<Void>> expectedResponseEntity = TimesheetStatusTestDataFactory
			.createVoidSuccessResponse();

		Mockito.doNothing()
			.when(this.timesheetInvoiceStatusService)
			.updateTimesheetPayBillStatus(timesheetId, requestDto);
		Mockito
			.when(this.apiResponder.respond(null, "Timesheet updated successfully", APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity<APINormalResponse<Object>>) (ResponseEntity<?>) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetInvoiceStatusController.updateTimesheetPayBillStatus(timesheetId,
				requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetInvoiceStatusService).updateTimesheetPayBillStatus(timesheetId, requestDto);
		Mockito.verify(this.apiResponder)
			.respond(null, "Timesheet updated successfully", APIResponseType.SUCCESS, HttpStatus.OK);
	}

}