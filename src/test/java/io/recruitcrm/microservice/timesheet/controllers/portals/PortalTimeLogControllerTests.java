package io.recruitcrm.microservice.timesheet.controllers.portals;

import io.recruitcrm.microservice.timesheet.dto.portal.DeletePortalTimesheetsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.timesheet.TimesheetService;
import io.recruitcrm.microservice.timesheet.services.timesheet_logs.TimesheetLogsService;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetLogsTestDataFactory;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetTestDataFactory;
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
class PortalTimeLogControllerTests {

	@Mock
	private TimesheetLogsService timesheetLogsService;

	@Mock
	private TimesheetService timesheetService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private PortalTimeLogController contractorTimeLogController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Get portal time logs successfully")
	void testGetPortalTimeLogsValidRequestReturnsTimesheet() {
		// Arrange
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		PortalTimesheetResponseBodyDto expectedTimesheet = TimesheetLogsTestDataFactory
			.createContractorTimesheetResponse();
		ResponseEntity<APINormalResponse<PortalTimesheetResponseBodyDto>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createContractorTimesheetSuccessResponse(expectedTimesheet);

		Mockito.when(this.timesheetLogsService.getPortalTimeLogs(timesheetId)).thenReturn(expectedTimesheet);
		Mockito
			.when(this.apiResponder.respond(expectedTimesheet,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorTimeLogController.getPortalTimeLogs(timesheetId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetLogsService).getPortalTimeLogs(timesheetId);
		Mockito.verify(this.apiResponder)
			.respond(expectedTimesheet, TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Delete portal timesheets successfully")
	void testDeletePortalTimesheetsValidRequestDeletesTimesheets() {
		// Arrange
		DeletePortalTimesheetsRequestBodyDto requestDto = TimesheetTestDataFactory
			.createDeletePortalTimesheetsRequest();
		ResponseEntity<APINormalResponse<Void>> expectedResponseEntity = TimesheetTestDataFactory
			.createVoidSuccessResponse();

		Mockito.doNothing()
			.when(this.timesheetService)
			.deletePortalTimesheets(requestDto.getTimesheetId(), requestDto.getJobId());
		Mockito.when(
				this.apiResponder.<Void>respond(null, TimesheetTestDataFactory.Messages.TIMESHEETS_DELETED_SUCCESSFULLY,
						APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorTimeLogController.deletePortalTimesheets(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService)
			.deletePortalTimesheets(requestDto.getTimesheetId(), requestDto.getJobId());
		Mockito.verify(this.apiResponder)
			.<Void>respond(null, TimesheetTestDataFactory.Messages.TIMESHEETS_DELETED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
