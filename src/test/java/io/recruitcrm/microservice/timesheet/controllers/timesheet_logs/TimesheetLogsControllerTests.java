package io.recruitcrm.microservice.timesheet.controllers.timesheet_logs;

import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingErrorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetSettingErrorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.BulkTimeLogRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.contractor.FetchBulkContractorTimelogResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.FetchBulkTimelogResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.FetchBulkTimelogValidatedResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.contractor.FetchContractorBulkTimelogValidatedResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.timesheet_logs.TimesheetLogsService;
import io.recruitcrm.microservice.timesheet.services.timesheet_status.TimesheetInvoiceStatusService;
import io.recruitcrm.microservice.timesheet.services.validator.ValidatorService;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetLogsTestDataFactory;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TimesheetLogsControllerTests {

	@Mock
	private TimesheetLogsService timesheetLogsService;

	@Mock
	private APIResponder apiResponder;

	@Mock
	private ValidatorService validatorService;

	@Mock
	private TimesheetInvoiceStatusService timesheetInvoiceStatusService;

	@InjectMocks
	private TimesheetLogsController timesheetLogsController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Get time logs by timesheet id successfully")
	void testGetTimeLogsByTimesheetIdValidIdReturnsTimeLogs() {
		// Arrange
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		TimesheetResponseBodyDto expectedTimesheet = TimesheetLogsTestDataFactory.createTimesheetResponse();
		ResponseEntity<APINormalResponse<TimesheetResponseBodyDto>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createTimesheetResponseSuccessResponse(expectedTimesheet);

		Mockito.when(this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId)).thenReturn(expectedTimesheet);
		Mockito
			.when(this.apiResponder.respond(expectedTimesheet,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.getTimeLogsByTimesheetId(timesheetId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		assertThat(expectedTimesheet.getTimesheetStartDay()).isEqualTo(1);
		Mockito.verify(this.timesheetLogsService).getTimeLogsByTimesheetId(timesheetId);
		Mockito.verify(this.apiResponder)
			.respond(expectedTimesheet, TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get time logs by timesheet id with agency recruiter approver successfully")
	void testGetTimeLogsByTimesheetIdWithAgencyRecruiterApproverReturnsTimeLogs() {
		// Arrange
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		TimesheetResponseBodyDto expectedTimesheet = TimesheetLogsTestDataFactory.createTimesheetResponse();
		expectedTimesheet.setApprovedBy("Test User");
		ResponseEntity<APINormalResponse<TimesheetResponseBodyDto>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createTimesheetResponseSuccessResponse(expectedTimesheet);

		Mockito.when(this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId)).thenReturn(expectedTimesheet);
		Mockito
			.when(this.apiResponder.respond(expectedTimesheet,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.getTimeLogsByTimesheetId(timesheetId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		assertThat(expectedTimesheet.getApprovedBy()).isEqualTo("Test User");
		Mockito.verify(this.timesheetLogsService).getTimeLogsByTimesheetId(timesheetId);
		Mockito.verify(this.apiResponder)
			.respond(expectedTimesheet, TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get time logs by timesheet id with company contact approver successfully")
	void testGetTimeLogsByTimesheetIdWithCompanyContactApproverReturnsTimeLogs() {
		// Arrange
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		TimesheetResponseBodyDto expectedTimesheet = TimesheetLogsTestDataFactory.createTimesheetResponse();
		expectedTimesheet.setApprovedBy("Test Contact");
		ResponseEntity<APINormalResponse<TimesheetResponseBodyDto>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createTimesheetResponseSuccessResponse(expectedTimesheet);

		Mockito.when(this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId)).thenReturn(expectedTimesheet);
		Mockito
			.when(this.apiResponder.respond(expectedTimesheet,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.getTimeLogsByTimesheetId(timesheetId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		assertThat(expectedTimesheet.getApprovedBy()).isEqualTo("Test Contact");
		Mockito.verify(this.timesheetLogsService).getTimeLogsByTimesheetId(timesheetId);
		Mockito.verify(this.apiResponder)
			.respond(expectedTimesheet, TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Bulk update time logs successfully")
	void testBulkUpdateTimeLogsValidRequestUpdatesLogs() {
		// Arrange
		BulkUpdateTimeLogsRequestBodyDto requestDto = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		ResponseEntity<APINormalResponse<Void>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createVoidSuccessResponse();

		Mockito.doNothing().when(this.timesheetLogsService).bulkUpdateTimeLogs(requestDto);
		Mockito
			.when(this.apiResponder.<Void>respond(null,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_BULK_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.bulkUpdateTimeLogs(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetLogsService).bulkUpdateTimeLogs(requestDto);
		Mockito.verify(this.apiResponder)
			.<Void>respond(null, TimesheetLogsTestDataFactory.Messages.TIME_LOGS_BULK_UPDATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Bulk update time logs with null isApproved should update successfully")
	void testBulkUpdateTimeLogsNullIsApprovedUpdatesLogs() {
		// Arrange
		BulkUpdateTimeLogsRequestBodyDto requestDto = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		requestDto.setIsApproved(null);
		ResponseEntity<APINormalResponse<Void>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createVoidSuccessResponse();

		Mockito.doNothing().when(this.timesheetLogsService).bulkUpdateTimeLogs(requestDto);
		Mockito
			.when(this.apiResponder.<Void>respond(null,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_BULK_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.bulkUpdateTimeLogs(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetLogsService).bulkUpdateTimeLogs(requestDto);
		Mockito.verify(this.apiResponder)
			.<Void>respond(null, TimesheetLogsTestDataFactory.Messages.TIME_LOGS_BULK_UPDATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Bulk update time logs with false isApproved should update successfully")
	void testBulkUpdateTimeLogsFalseIsApprovedUpdatesLogs() {
		// Arrange
		BulkUpdateTimeLogsRequestBodyDto requestDto = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		requestDto.setIsApproved(false);
		ResponseEntity<APINormalResponse<Void>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createVoidSuccessResponse();

		Mockito.doNothing().when(this.timesheetLogsService).bulkUpdateTimeLogs(requestDto);
		Mockito
			.when(this.apiResponder.<Void>respond(null,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_BULK_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.bulkUpdateTimeLogs(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetLogsService).bulkUpdateTimeLogs(requestDto);
		Mockito.verify(this.apiResponder)
			.<Void>respond(null, TimesheetLogsTestDataFactory.Messages.TIME_LOGS_BULK_UPDATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Bulk update time logs with empty time logs list should update successfully")
	void testBulkUpdateTimeLogsEmptyTimeLogsListUpdatesLogs() {
		// Arrange
		BulkUpdateTimeLogsRequestBodyDto requestDto = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		requestDto.setTimeLogs(Arrays.asList());
		ResponseEntity<APINormalResponse<Void>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createVoidSuccessResponse();

		Mockito.doNothing().when(this.timesheetLogsService).bulkUpdateTimeLogs(requestDto);
		Mockito
			.when(this.apiResponder.<Void>respond(null,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_BULK_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.bulkUpdateTimeLogs(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetLogsService).bulkUpdateTimeLogs(requestDto);
		Mockito.verify(this.apiResponder)
			.<Void>respond(null, TimesheetLogsTestDataFactory.Messages.TIME_LOGS_BULK_UPDATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Bulk update time logs with single time log should update successfully")
	void testBulkUpdateTimeLogsSingleTimeLogUpdatesLogs() {
		// Arrange
		BulkUpdateTimeLogsRequestBodyDto requestDto = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		requestDto.setTimeLogs(Arrays.asList(TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest()));
		ResponseEntity<APINormalResponse<Void>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createVoidSuccessResponse();

		Mockito.doNothing().when(this.timesheetLogsService).bulkUpdateTimeLogs(requestDto);
		Mockito
			.when(this.apiResponder.<Void>respond(null,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_BULK_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.bulkUpdateTimeLogs(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetLogsService).bulkUpdateTimeLogs(requestDto);
		Mockito.verify(this.apiResponder)
			.<Void>respond(null, TimesheetLogsTestDataFactory.Messages.TIME_LOGS_BULK_UPDATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get bulk time logs successfully")
	void testGetBulkTimeLogsValidRequestReturnsLogs() {
		// Arrange
		BulkTimeLogRequestBodyDto requestBodyDto = TimesheetLogsTestDataFactory.createBulkTimeLogRequest();
		FetchBulkTimelogValidatedResponseBodyDto validatedResponse = TimesheetLogsTestDataFactory
			.createFetchBulkTimelogValidatedResponse();
		FetchBulkTimelogResultBodyDto expectedResult = TimesheetLogsTestDataFactory.createFetchBulkTimelogResult();
		ResponseEntity<APINormalResponse<FetchBulkTimelogResultBodyDto>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createFetchBulkTimelogResultSuccessResponse(expectedResult);

		Mockito.when(this.validatorService.validateTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds()))
			.thenReturn(validatedResponse);
		Mockito.when(this.timesheetLogsService.getAllTimeLogs(requestBodyDto.getTimesheetIds(),
				validatedResponse.getTimesheetAndSettingValidatorResponseBodyDtos(),
				validatedResponse.getTimesheetSettingErrorResponseBodyDtos()))
			.thenReturn(expectedResult);
		Mockito
			.when(this.apiResponder.respond(expectedResult,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.getBulkTimeLogs(requestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.validatorService).validateTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds());
		Mockito.verify(this.timesheetLogsService)
			.getAllTimeLogs(requestBodyDto.getTimesheetIds(),
					validatedResponse.getTimesheetAndSettingValidatorResponseBodyDtos(),
					validatedResponse.getTimesheetSettingErrorResponseBodyDtos());
		Mockito.verify(this.apiResponder)
			.respond(expectedResult, TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get contractor bulk time logs successfully")
	void testGetContractorBulkTimeLogsValidRequestReturnsLogs() {
		// Arrange
		BulkTimeLogRequestBodyDto requestBodyDto = TimesheetLogsTestDataFactory.createBulkTimeLogRequest();
		FetchContractorBulkTimelogValidatedResponseBodyDto validatedResponse = TimesheetLogsTestDataFactory
			.createFetchContractorBulkTimelogValidatedResponse();
		FetchBulkContractorTimelogResultBodyDto expectedResult = TimesheetLogsTestDataFactory
			.createFetchBulkContractorTimelogResult();
		ResponseEntity<APINormalResponse<FetchBulkContractorTimelogResultBodyDto>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createFetchBulkContractorTimelogResultSuccessResponse(expectedResult);

		Mockito.when(this.validatorService.validateContractorTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds()))
			.thenReturn(validatedResponse);
		Mockito.when(this.timesheetLogsService.getContractorAllTimeLogs(requestBodyDto.getTimesheetIds(),
				validatedResponse.getTimesheetAndSettingValidatorResponseBodyDtos(), validatedResponse.getErrorData()))
			.thenReturn(expectedResult);
		Mockito
			.when(this.apiResponder.respond(expectedResult,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.getContractorBulkTimeLogs(requestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.validatorService).validateContractorTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds());
		Mockito.verify(this.timesheetLogsService)
			.getContractorAllTimeLogs(requestBodyDto.getTimesheetIds(),
					validatedResponse.getTimesheetAndSettingValidatorResponseBodyDtos(),
					validatedResponse.getErrorData());
		Mockito.verify(this.apiResponder)
			.respond(expectedResult, TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get bulk time logs should set contractors error data from validated response")
	void testGetBulkTimeLogsValidRequestSetsContractorsErrorData() {
		// Arrange
		BulkTimeLogRequestBodyDto requestBodyDto = TimesheetLogsTestDataFactory.createBulkTimeLogRequest();
		FetchBulkTimelogValidatedResponseBodyDto validatedResponse = TimesheetLogsTestDataFactory
			.createFetchBulkTimelogValidatedResponse();
		TimesheetSettingErrorResponseBodyDto errorDto = TimesheetLogsTestDataFactory
			.createTimesheetSettingErrorResponse();
		validatedResponse.setTimesheetSettingErrorResponseBodyDtos(List.of(errorDto));
		FetchBulkTimelogResultBodyDto expectedResult = TimesheetLogsTestDataFactory.createFetchBulkTimelogResult();
		ResponseEntity<APINormalResponse<FetchBulkTimelogResultBodyDto>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createFetchBulkTimelogResultSuccessResponse(expectedResult);

		Mockito.when(this.validatorService.validateTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds()))
			.thenReturn(validatedResponse);
		Mockito.when(this.timesheetLogsService.getAllTimeLogs(requestBodyDto.getTimesheetIds(),
				validatedResponse.getTimesheetAndSettingValidatorResponseBodyDtos(),
				validatedResponse.getTimesheetSettingErrorResponseBodyDtos()))
			.thenReturn(expectedResult);
		Mockito
			.when(this.apiResponder.respond(expectedResult,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.getBulkTimeLogs(requestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		assertThat(expectedResult.getContractorsErrorData()).containsExactly(errorDto);
		Mockito.verify(this.validatorService).validateTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds());
		Mockito.verify(this.timesheetLogsService)
			.getAllTimeLogs(requestBodyDto.getTimesheetIds(),
					validatedResponse.getTimesheetAndSettingValidatorResponseBodyDtos(),
					validatedResponse.getTimesheetSettingErrorResponseBodyDtos());
	}

	@Test
	@DisplayName("Get contractor bulk time logs should set error data from validated response")
	void testGetContractorBulkTimeLogsValidRequestSetsErrorData() {
		// Arrange
		BulkTimeLogRequestBodyDto requestBodyDto = TimesheetLogsTestDataFactory.createBulkTimeLogRequest();
		FetchContractorBulkTimelogValidatedResponseBodyDto validatedResponse = TimesheetLogsTestDataFactory
			.createFetchContractorBulkTimelogValidatedResponse();
		ContractorTimesheetSettingErrorResponseBodyDto errorDto = TimesheetLogsTestDataFactory
			.createContractorTimesheetSettingErrorResponse();
		validatedResponse.setErrorData(List.of(errorDto));
		FetchBulkContractorTimelogResultBodyDto expectedResult = TimesheetLogsTestDataFactory
			.createFetchBulkContractorTimelogResult();
		ResponseEntity<APINormalResponse<FetchBulkContractorTimelogResultBodyDto>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createFetchBulkContractorTimelogResultSuccessResponse(expectedResult);

		Mockito.when(this.validatorService.validateContractorTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds()))
			.thenReturn(validatedResponse);
		Mockito.when(this.timesheetLogsService.getContractorAllTimeLogs(requestBodyDto.getTimesheetIds(),
				validatedResponse.getTimesheetAndSettingValidatorResponseBodyDtos(), validatedResponse.getErrorData()))
			.thenReturn(expectedResult);
		Mockito
			.when(this.apiResponder.respond(expectedResult,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.getContractorBulkTimeLogs(requestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		assertThat(expectedResult.getErrorData()).containsExactly(errorDto);
		Mockito.verify(this.validatorService).validateContractorTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds());
		Mockito.verify(this.timesheetLogsService)
			.getContractorAllTimeLogs(requestBodyDto.getTimesheetIds(),
					validatedResponse.getTimesheetAndSettingValidatorResponseBodyDtos(),
					validatedResponse.getErrorData());
	}

	@Test
	@DisplayName("Get bulk time logs - null timesheet ids throws validation error")
	void testGetBulkTimeLogsNullTimesheetIdsThrowsValidationErrorException() {
		BulkTimeLogRequestBodyDto requestBodyDto = new BulkTimeLogRequestBodyDto();
		requestBodyDto.setTimesheetIds(null);

		assertThatThrownBy(() -> this.timesheetLogsController.getBulkTimeLogs(requestBodyDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("At least one timesheet id is required");

		Mockito.verify(this.validatorService, Mockito.never()).validateTimeLogsBeforeUpdate(Mockito.any());
		Mockito.verify(this.timesheetLogsService, Mockito.never())
			.getAllTimeLogs(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	@DisplayName("Get bulk time logs - empty timesheet ids throws validation error")
	void testGetBulkTimeLogsEmptyTimesheetIdsThrowsValidationErrorException() {
		BulkTimeLogRequestBodyDto requestBodyDto = TimesheetLogsTestDataFactory.createBulkTimeLogRequest();
		requestBodyDto.setTimesheetIds(Collections.emptyList());

		assertThatThrownBy(() -> this.timesheetLogsController.getBulkTimeLogs(requestBodyDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("At least one timesheet id is required");

		Mockito.verify(this.validatorService, Mockito.never()).validateTimeLogsBeforeUpdate(Mockito.any());
		Mockito.verify(this.timesheetLogsService, Mockito.never())
			.getAllTimeLogs(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	@DisplayName("Get bulk time logs with isRemarkMandatory true should return logs")
	void testGetBulkTimeLogsWithIsRemarkMandatoryTrueReturnsLogs() {
		// Arrange
		BulkTimeLogRequestBodyDto requestBodyDto = TimesheetLogsTestDataFactory.createBulkTimeLogRequest();
		FetchBulkTimelogValidatedResponseBodyDto validatedResponse = TimesheetLogsTestDataFactory
			.createFetchBulkTimelogValidatedResponse();
		FetchBulkTimelogResultBodyDto expectedResult = TimesheetLogsTestDataFactory.createFetchBulkTimelogResult();
		// Set isRemarkMandatory to true in the first contractor's log data
		if (expectedResult.getContractorsLogData() != null && !expectedResult.getContractorsLogData().isEmpty()) {
			expectedResult.getContractorsLogData().getFirst().setIsRemarkMandatory(1);
		}
		ResponseEntity<APINormalResponse<FetchBulkTimelogResultBodyDto>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createFetchBulkTimelogResultSuccessResponse(expectedResult);

		Mockito.when(this.validatorService.validateTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds()))
			.thenReturn(validatedResponse);
		Mockito.when(this.timesheetLogsService.getAllTimeLogs(requestBodyDto.getTimesheetIds(),
				validatedResponse.getTimesheetAndSettingValidatorResponseBodyDtos(),
				validatedResponse.getTimesheetSettingErrorResponseBodyDtos()))
			.thenReturn(expectedResult);
		Mockito
			.when(this.apiResponder.respond(expectedResult,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.getBulkTimeLogs(requestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.validatorService).validateTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds());
		Mockito.verify(this.timesheetLogsService)
			.getAllTimeLogs(requestBodyDto.getTimesheetIds(),
					validatedResponse.getTimesheetAndSettingValidatorResponseBodyDtos(),
					validatedResponse.getTimesheetSettingErrorResponseBodyDtos());
	}

	@Test
	@DisplayName("Get contractor bulk time logs with isRemarkMandatory true should return logs")
	void testGetContractorBulkTimeLogsWithIsRemarkMandatoryTrueReturnsLogs() {
		// Arrange
		BulkTimeLogRequestBodyDto requestBodyDto = TimesheetLogsTestDataFactory.createBulkTimeLogRequest();
		FetchContractorBulkTimelogValidatedResponseBodyDto validatedResponse = TimesheetLogsTestDataFactory
			.createFetchContractorBulkTimelogValidatedResponse();
		FetchBulkContractorTimelogResultBodyDto expectedResult = TimesheetLogsTestDataFactory
			.createFetchBulkContractorTimelogResult();
		ResponseEntity<APINormalResponse<FetchBulkContractorTimelogResultBodyDto>> expectedResponseEntity = TimesheetLogsTestDataFactory
			.createFetchBulkContractorTimelogResultSuccessResponse(expectedResult);

		Mockito.when(this.validatorService.validateContractorTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds()))
			.thenReturn(validatedResponse);
		Mockito.when(this.timesheetLogsService.getContractorAllTimeLogs(requestBodyDto.getTimesheetIds(),
				validatedResponse.getTimesheetAndSettingValidatorResponseBodyDtos(), validatedResponse.getErrorData()))
			.thenReturn(expectedResult);
		Mockito
			.when(this.apiResponder.respond(expectedResult,
					TimesheetLogsTestDataFactory.Messages.TIME_LOGS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetLogsController.getContractorBulkTimeLogs(requestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.validatorService).validateContractorTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds());
		Mockito.verify(this.timesheetLogsService)
			.getContractorAllTimeLogs(requestBodyDto.getTimesheetIds(),
					validatedResponse.getTimesheetAndSettingValidatorResponseBodyDtos(),
					validatedResponse.getErrorData());
	}

	@Test
	@DisplayName("Get contractor bulk time logs - null timesheet ids throws validation error")
	void testGetContractorBulkTimeLogsNullTimesheetIdsThrowsValidationErrorException() {
		BulkTimeLogRequestBodyDto requestBodyDto = new BulkTimeLogRequestBodyDto();
		requestBodyDto.setTimesheetIds(null);

		assertThatThrownBy(() -> this.timesheetLogsController.getContractorBulkTimeLogs(requestBodyDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("At least one timesheet id is required");

		Mockito.verify(this.validatorService, Mockito.never()).validateContractorTimeLogsBeforeUpdate(Mockito.any());
		Mockito.verify(this.timesheetLogsService, Mockito.never())
			.getContractorAllTimeLogs(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	@DisplayName("Get contractor bulk time logs - empty timesheet ids throws validation error")
	void testGetContractorBulkTimeLogsEmptyTimesheetIdsThrowsValidationErrorException() {
		BulkTimeLogRequestBodyDto requestBodyDto = TimesheetLogsTestDataFactory.createBulkTimeLogRequest();
		requestBodyDto.setTimesheetIds(Collections.emptyList());

		assertThatThrownBy(() -> this.timesheetLogsController.getContractorBulkTimeLogs(requestBodyDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("At least one timesheet id is required");

		Mockito.verify(this.validatorService, Mockito.never()).validateContractorTimeLogsBeforeUpdate(Mockito.any());
		Mockito.verify(this.timesheetLogsService, Mockito.never())
			.getContractorAllTimeLogs(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	@DisplayName("Update timesheet status successfully")
	void testUpdateTimesheetStatusValidRequestUpdatesStatus() {
		// Given
		Integer timesheetId = TimesheetStatusTestDataFactory.getDefaultTimesheetId();
		UpdateTimesheetStatusRequestBodyDto requestDto = TimesheetStatusTestDataFactory
			.createUpdateTimesheetStatusRequest();
		ResponseEntity<APINormalResponse<Void>> expectedResponseEntity = TimesheetStatusTestDataFactory
			.createVoidSuccessResponse();

		org.mockito.BDDMockito.willDoNothing()
			.given(this.timesheetInvoiceStatusService)
			.updateTimesheetStatus(timesheetId, requestDto);
		org.mockito.BDDMockito.given(this.apiResponder.<Void>respond(null,
				TimesheetStatusTestDataFactory.Messages.TIMESHEET_STATUS_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.CREATED))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.timesheetLogsController.updateTimesheetStatus(timesheetId, requestDto);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		org.mockito.BDDMockito.then(this.timesheetInvoiceStatusService)
			.should()
			.updateTimesheetStatus(timesheetId, requestDto);
		org.mockito.BDDMockito.then(this.apiResponder)
			.should()
			.<Void>respond(null, TimesheetStatusTestDataFactory.Messages.TIMESHEET_STATUS_UPDATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.CREATED);
	}

}