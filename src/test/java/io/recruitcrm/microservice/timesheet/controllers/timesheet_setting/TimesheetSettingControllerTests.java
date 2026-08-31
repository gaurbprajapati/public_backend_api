package io.recruitcrm.microservice.timesheet.controllers.timesheet_setting;

import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.EnableTimesheetSettingRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingPreferenceResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIErrorResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.timesheet_setting.TimesheetSettingService;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetSettingTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TimesheetSettingControllerTests {

	@Mock
	private TimesheetSettingService timesheetSettingService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private TimesheetSettingController timesheetSettingController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Get timesheet setting by assignment id successfully")
	void testGetTimesheetSettingByAssignmentIdValidIdsReturnsSetting() {
		// Arrange
		Integer jobId = TimesheetSettingTestDataFactory.getDefaultJobId();
		Integer contractorId = TimesheetSettingTestDataFactory.getDefaultContractorId();
		TimesheetSettingResponseBodyDto expectedSetting = TimesheetSettingTestDataFactory
			.createTimesheetSettingResponse();
		ResponseEntity<APINormalResponse<TimesheetSettingResponseBodyDto>> expectedResponseEntity = TimesheetSettingTestDataFactory
			.createTimesheetSettingSuccessResponse(expectedSetting);

		Mockito.when(this.timesheetSettingService.getTimesheetSettingByAssignmentId(jobId, contractorId))
			.thenReturn(expectedSetting);
		Mockito
			.when(this.apiResponder.respond(expectedSetting,
					TimesheetSettingTestDataFactory.Messages.TIMESHEET_SETTING_FETCHED_SUCCESSFULLY))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetSettingController.getTimesheetSettingByAssignmentId(jobId,
				contractorId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetSettingService).getTimesheetSettingByAssignmentId(jobId, contractorId);
		Mockito.verify(this.apiResponder)
			.respond(expectedSetting, TimesheetSettingTestDataFactory.Messages.TIMESHEET_SETTING_FETCHED_SUCCESSFULLY);
	}

	@Test
	@DisplayName("Create bulk timesheet settings successfully")
	void testCreateBulkTimesheetSettingsValidRequestCreatesSettings() {
		// Arrange
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequest();
		ResponseEntity<?> expectedResponseEntity = TimesheetSettingTestDataFactory.createVoidSuccessResponse();

		Mockito.doNothing().when(this.timesheetSettingService).createBulkTimesheetSettings(request);
		@SuppressWarnings("unchecked")
		ResponseEntity<APINormalResponse<Object>> mockResponse = (ResponseEntity<APINormalResponse<Object>>) expectedResponseEntity;
		Mockito
			.when(this.apiResponder.respond(null,
					TimesheetSettingTestDataFactory.Messages.TIMESHEET_SETTING_CREATED_SUCCESSFULLY))
			.thenReturn(mockResponse);

		// Act
		ResponseEntity<?> response = this.timesheetSettingController.createBulkTimesheetSettings(request);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetSettingService).createBulkTimesheetSettings(request);
		Mockito.verify(this.apiResponder)
			.respond(null, TimesheetSettingTestDataFactory.Messages.TIMESHEET_SETTING_CREATED_SUCCESSFULLY);
	}

	@Test
	@DisplayName("Create bulk timesheet settings successfully when isRemarkMandatory is not provided")
	void testCreateBulkTimesheetSettingsWhenIsRemarkMandatoryNotProvidedCreatesSettings() {
		// Arrange
		TimesheetSettingBulkRequestBodyDto request = TimesheetSettingTestDataFactory
			.createTimesheetSettingBulkRequest();
		request.setIsRemarkMandatory(null);
		ResponseEntity<?> expectedResponseEntity = TimesheetSettingTestDataFactory.createVoidSuccessResponse();

		Mockito.doNothing().when(this.timesheetSettingService).createBulkTimesheetSettings(request);
		@SuppressWarnings("unchecked")
		ResponseEntity<APINormalResponse<Object>> mockResponse = (ResponseEntity<APINormalResponse<Object>>) expectedResponseEntity;
		Mockito
			.when(this.apiResponder.respond(null,
					TimesheetSettingTestDataFactory.Messages.TIMESHEET_SETTING_CREATED_SUCCESSFULLY))
			.thenReturn(mockResponse);

		// Act
		ResponseEntity<?> response = this.timesheetSettingController.createBulkTimesheetSettings(request);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetSettingService).createBulkTimesheetSettings(request);
		Mockito.verify(this.apiResponder)
			.respond(null, TimesheetSettingTestDataFactory.Messages.TIMESHEET_SETTING_CREATED_SUCCESSFULLY);
	}

	@Test
	@DisplayName("Get timesheet setting date validation successfully")
	void testGetTimesheetSettingDateValidationValidRequestReturnsTrue() {
		// Arrange
		Integer jobId = TimesheetSettingTestDataFactory.getDefaultJobId();
		Integer contractorId = TimesheetSettingTestDataFactory.getDefaultContractorId();
		Long startDate = TimesheetSettingTestDataFactory.getDefaultStartDate().longValue();
		Long endDate = TimesheetSettingTestDataFactory.getDefaultEndDate().longValue();
		ResponseEntity<APINormalResponse<Boolean>> expectedResponseEntity = TimesheetSettingTestDataFactory
			.createBooleanSuccessResponse(true);

		Mockito
			.when(this.timesheetSettingService.getTimesheetSettingDateValidation(jobId, contractorId, startDate,
					endDate))
			.thenReturn(true);
		Mockito
			.when(this.apiResponder.respond(true,
					TimesheetSettingTestDataFactory.Messages.TIMESHEET_SETTING_VALIDATION_SUCCESSFULLY))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetSettingController.getTimesheetSettingDateValidation(jobId,
				contractorId, startDate, endDate);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetSettingService)
			.getTimesheetSettingDateValidation(jobId, contractorId, startDate, endDate);
		Mockito.verify(this.apiResponder)
			.respond(true, TimesheetSettingTestDataFactory.Messages.TIMESHEET_SETTING_VALIDATION_SUCCESSFULLY);
	}

	@Test
	@DisplayName("Get timesheet setting date validation with validation error")
	void testGetTimesheetSettingDateValidationInvalidRequestReturnsError() {
		// Arrange
		Integer jobId = TimesheetSettingTestDataFactory.getDefaultJobId();
		Integer contractorId = TimesheetSettingTestDataFactory.getDefaultContractorId();
		Long startDate = TimesheetSettingTestDataFactory.getDefaultStartDate().longValue();
		Long endDate = TimesheetSettingTestDataFactory.getDefaultEndDate().longValue();
		ResponseEntity<APIErrorResponse> expectedResponseEntity = TimesheetSettingTestDataFactory.createErrorResponse();

		Mockito
			.when(this.timesheetSettingService.getTimesheetSettingDateValidation(jobId, contractorId, startDate,
					endDate))
			.thenReturn(false);
		Mockito
			.when(this.apiResponder.respondWithError(ArgumentMatchers.any(ValidationErrorException.class),
					ArgumentMatchers.eq(APIResponseType.WARNING), ArgumentMatchers.eq(HttpStatus.BAD_REQUEST)))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetSettingController.getTimesheetSettingDateValidation(jobId,
				contractorId, startDate, endDate);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetSettingService)
			.getTimesheetSettingDateValidation(jobId, contractorId, startDate, endDate);
		Mockito.verify(this.apiResponder)
			.respondWithError(ArgumentMatchers.any(ValidationErrorException.class),
					ArgumentMatchers.eq(APIResponseType.WARNING), ArgumentMatchers.eq(HttpStatus.BAD_REQUEST));
	}

	@Test
	@DisplayName("Get enabled assignment ids successfully")
	void testGetEnabledAssigmentIdsValidRequestReturnsIds() {
		// Arrange
		EnableTimesheetSettingRequestBodyDto request = TimesheetSettingTestDataFactory
			.createEnableTimesheetSettingRequestBodyDto();
		List<Integer> expectedIds = TimesheetSettingTestDataFactory.createEnabledAssignmentIds();
		ResponseEntity<APINormalResponse<List<Integer>>> expectedResponseEntity = TimesheetSettingTestDataFactory
			.createEnabledAssignmentIdsSuccessResponse(expectedIds);

		Mockito.when(this.timesheetSettingService.getEnabledAssigmentIds(request)).thenReturn(expectedIds);
		Mockito.when(this.apiResponder.respond(expectedIds,
				TimesheetSettingTestDataFactory.Messages.TIMESHEET_SETTING_ENABLED_ASSIGNMENT_IDS_SUCCESSFULLY_FETCHED))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetSettingController.getEnabledAssigmentIds(request);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetSettingService).getEnabledAssigmentIds(request);
		Mockito.verify(this.apiResponder)
			.respond(expectedIds,
					TimesheetSettingTestDataFactory.Messages.TIMESHEET_SETTING_ENABLED_ASSIGNMENT_IDS_SUCCESSFULLY_FETCHED);
	}

	@Test
	@DisplayName("Get user timesheet setting preference successfully")
	void testGetUserTimesheetSettingPreferenceReturnsPreference() {
		// Arrange
		TimesheetSettingPreferenceResponseBodyDto expectedPreference = TimesheetSettingTestDataFactory
			.createTimesheetSettingPreferenceResponse();
		ResponseEntity<APINormalResponse<TimesheetSettingPreferenceResponseBodyDto>> expectedResponseEntity = TimesheetSettingTestDataFactory
			.createTimesheetSettingPreferenceSuccessResponse(expectedPreference);

		Mockito.when(this.timesheetSettingService.getUserTimesheetSettingPreference()).thenReturn(expectedPreference);
		Mockito
			.when(this.apiResponder.respond(expectedPreference,
					TimesheetSettingTestDataFactory.Messages.TIMESHEET_SETTING_PREFERENCE_FETCHED_SUCCESSFULLY))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetSettingController.getUserTimesheetSettingPreference();

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetSettingService).getUserTimesheetSettingPreference();
		Mockito.verify(this.apiResponder)
			.respond(expectedPreference,
					TimesheetSettingTestDataFactory.Messages.TIMESHEET_SETTING_PREFERENCE_FETCHED_SUCCESSFULLY);
	}

}