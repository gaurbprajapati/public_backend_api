package io.recruitcrm.microservice.timesheet.controllers.timesheet;

import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DeleteTimesheetsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAccessControlResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.SearchEntityRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.SearchEntityResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetMigrationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetMigrationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateJobTimesheetAccessControlRequestBodyDto;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.responses.APIErrorResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.timesheet.TimesheetService;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TimesheetControllerTests {

	@Mock
	private TimesheetService timesheetService;

	@Mock
	private APIResponder apiResponder;

	@Mock
	private AuthHolder auth;

	private TimesheetController timesheetController;

	@BeforeEach
	void setUp() {
		this.timesheetController = new TimesheetController(this.timesheetService, this.apiResponder, this.auth,
				"local");
	}

	@Test
	@DisplayName("Create timesheets successfully")
	void testCreateTimesheetsValidRequestCreatesTimesheets() {
		// Arrange
		Integer jobId = TimesheetTestDataFactory.getDefaultJobId();
		CreateBulkTimesheetRequestBodyDto requestObject = TimesheetTestDataFactory.createBulkTimesheetRequest();
		ResponseEntity<?> expectedResponseEntity = TimesheetTestDataFactory.createVoidSuccessResponse();

		Mockito.doNothing()
			.when(this.timesheetService)
			.createTimesheets(jobId, requestObject.getContractorIds(), requestObject.getTimesheetDates());
		@SuppressWarnings("unchecked")
		ResponseEntity<APINormalResponse<Object>> mockResponse = (ResponseEntity<APINormalResponse<Object>>) expectedResponseEntity;
		Mockito.when(this.apiResponder.respond(null, TimesheetTestDataFactory.Messages.TIMESHEETS_CREATED_SUCCESSFULLY))
			.thenReturn(mockResponse);

		// Act
		ResponseEntity<?> response = this.timesheetController.createTimesheets(requestObject, jobId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService)
			.createTimesheets(jobId, requestObject.getContractorIds(), requestObject.getTimesheetDates());
		Mockito.verify(this.apiResponder)
			.respond(null, TimesheetTestDataFactory.Messages.TIMESHEETS_CREATED_SUCCESSFULLY);
	}

	@Test
	@DisplayName("Delete timesheet by id successfully")
	void testDeleteTimesheetValidIdDeletesTimesheet() {
		// Arrange
		Integer timesheetId = TimesheetTestDataFactory.getDefaultTimesheetId();
		ResponseEntity<?> expectedResponseEntity = TimesheetTestDataFactory.createVoidSuccessResponse();

		Mockito.doNothing().when(this.timesheetService).deleteTimesheet(timesheetId);
		@SuppressWarnings("unchecked")
		ResponseEntity<APINormalResponse<Object>> mockResponse = (ResponseEntity<APINormalResponse<Object>>) expectedResponseEntity;
		Mockito.when(this.apiResponder.respond(null, TimesheetTestDataFactory.Messages.TIMESHEET_DELETED_SUCCESSFULLY))
			.thenReturn(mockResponse);

		// Act
		ResponseEntity<?> response = this.timesheetController.deleteTimesheet(timesheetId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService).deleteTimesheet(timesheetId);
		Mockito.verify(this.apiResponder)
			.respond(null, TimesheetTestDataFactory.Messages.TIMESHEET_DELETED_SUCCESSFULLY);
	}

	@Test
	@DisplayName("Get timesheet status history successfully")
	void testGetTimesheetStatusHistoryValidIdReturnsStatusHistory() {
		// Arrange
		Integer timesheetId = TimesheetTestDataFactory.getDefaultTimesheetId();
		TimesheetStatusHistoryResponseBodyDto expectedStatusHistory = TimesheetTestDataFactory
			.createTimesheetStatusHistoryResponse();
		ResponseEntity<APINormalResponse<TimesheetStatusHistoryResponseBodyDto>> expectedResponseEntity = TimesheetTestDataFactory
			.createTimesheetStatusHistorySuccessResponse(expectedStatusHistory);

		Mockito.when(this.timesheetService.getTimesheetStatusHistory(timesheetId)).thenReturn(expectedStatusHistory);
		Mockito.when(this.apiResponder.respond(expectedStatusHistory,
				TimesheetTestDataFactory.Messages.TIMESHEET_STATUS_HISTORY_FETCHED_SUCCESSFULLY,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetController.getTimesheetStatusHistory(timesheetId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService).getTimesheetStatusHistory(timesheetId);
		Mockito.verify(this.apiResponder)
			.respond(expectedStatusHistory,
					TimesheetTestDataFactory.Messages.TIMESHEET_STATUS_HISTORY_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Delete multiple timesheets successfully")
	void testDeleteTimesheetsValidRequestDeletesTimesheets() {
		// Arrange
		DeleteTimesheetsRequestBodyDto requestDto = TimesheetTestDataFactory.createDeleteTimesheetsRequest();
		ResponseEntity<?> expectedResponseEntity = TimesheetTestDataFactory.createVoidSuccessResponse();

		Mockito.doNothing().when(this.timesheetService).deleteTimesheets(requestDto.getTimesheetIds());
		@SuppressWarnings("unchecked")
		ResponseEntity<APINormalResponse<Object>> mockResponse = (ResponseEntity<APINormalResponse<Object>>) expectedResponseEntity;
		Mockito
			.when(this.apiResponder.respond(null, TimesheetTestDataFactory.Messages.TIMESHEETS_DELETED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(mockResponse);

		// Act
		ResponseEntity<?> response = this.timesheetController.deleteTimesheets(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService).deleteTimesheets(requestDto.getTimesheetIds());
		Mockito.verify(this.apiResponder)
			.respond(null, TimesheetTestDataFactory.Messages.TIMESHEETS_DELETED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK);
	}

	@Test
	@DisplayName("Get timesheets list by deal id successfully")
	void testGetTimesheetsListByDealIdValidRequestReturnsTimesheets() {
		// Arrange
		Integer dealId = TimesheetTestDataFactory.getDefaultDealId();
		SearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createSearchRequest();
		PaginationRequestBodyDto paginationRequestBodyDto = TimesheetTestDataFactory.createPaginationRequest();

		List<TimesheetListResponseBodyDto> expectedTimesheets = TimesheetTestDataFactory
			.createTimesheetListResponseList();
		ResponseEntity<APINormalResponse<List<TimesheetListResponseBodyDto>>> expectedResponseEntity = TimesheetTestDataFactory
			.createTimesheetListSuccessResponse(expectedTimesheets);

		Mockito
			.when(this.timesheetService.getTimesheetsListByDealId(dealId, searchRequestBodyDto,
					paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedTimesheets);
		Mockito
			.when(this.apiResponder.respond(expectedTimesheets,
					TimesheetTestDataFactory.Messages.TIMESHEETS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetController.getTimesheetsListByDealId(dealId, searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService)
			.getTimesheetsListByDealId(dealId, searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedTimesheets, TimesheetTestDataFactory.Messages.TIMESHEETS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get timesheets list by job and contractor id successfully")
	void testGetTimesheetsListByJobAndContractorIdValidRequestReturnsTimesheets() {
		// Arrange
		Integer jobId = TimesheetTestDataFactory.getDefaultJobId();
		Integer contractorId = TimesheetTestDataFactory.getDefaultContractorId();
		SearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createSearchRequest();
		PaginationRequestBodyDto paginationRequestBodyDto = TimesheetTestDataFactory.createPaginationRequest();

		List<TimesheetListResponseBodyDto> expectedTimesheets = TimesheetTestDataFactory
			.createTimesheetListResponseList();
		ResponseEntity<APINormalResponse<List<TimesheetListResponseBodyDto>>> expectedResponseEntity = TimesheetTestDataFactory
			.createTimesheetListSuccessResponse(expectedTimesheets);

		Mockito.when(this.timesheetService.getTimesheetsListByJobAndContractorId(jobId, contractorId,
				searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedTimesheets);
		Mockito
			.when(this.apiResponder.respond(expectedTimesheets,
					TimesheetTestDataFactory.Messages.TIMESHEETS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetController.getTimesheetsListByJobAndContractorId(jobId, contractorId,
				searchRequestBodyDto, paginationRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService)
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, searchRequestBodyDto,
					paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedTimesheets, TimesheetTestDataFactory.Messages.TIMESHEETS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get timesheets list by entity id successfully")
	void testGetTimesheetsListByEntityIdValidRequestReturnsTimesheets() {
		// Given
		SearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createSearchRequest();
		PaginationRequestBodyDto paginationRequestBodyDto = TimesheetTestDataFactory.createPaginationRequest();

		List<TimesheetListResponseBodyDto> expectedTimesheets = TimesheetTestDataFactory
			.createTimesheetListResponseList();
		ResponseEntity<APINormalResponse<List<TimesheetListResponseBodyDto>>> expectedResponseEntity = TimesheetTestDataFactory
			.createTimesheetListSuccessResponse(expectedTimesheets);

		given(this.timesheetService.getTimesheetsListByEntityId(searchRequestBodyDto,
				paginationRequestBodyDto.toPageable()))
			.willReturn(expectedTimesheets);
		given(this.apiResponder.respond(expectedTimesheets,
				TimesheetTestDataFactory.Messages.TIMESHEETS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.timesheetController.getTimesheetsListByEntityId(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.timesheetService).should()
			.getTimesheetsListByEntityId(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		then(this.apiResponder).should()
			.respond(expectedTimesheets, TimesheetTestDataFactory.Messages.TIMESHEETS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get timesheets count by entity id successfully")
	void testGetTimesheetsCountByEntityIdValidRequestReturnsCount() {
		// Given
		SearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createSearchRequest();
		TimesheetCountResponseBodyDto expectedCount = TimesheetTestDataFactory.createTimesheetCountResponse();
		ResponseEntity<APINormalResponse<TimesheetCountResponseBodyDto>> expectedResponseEntity = TimesheetTestDataFactory
			.createTimesheetCountSuccessResponse(expectedCount);

		given(this.timesheetService.getTimesheetsCountByEntityId(searchRequestBodyDto)).willReturn(expectedCount);
		given(this.apiResponder.respond(expectedCount,
				TimesheetTestDataFactory.Messages.TIMESHEET_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.timesheetController.getTimesheetsCountByEntityId(searchRequestBodyDto);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.timesheetService).should().getTimesheetsCountByEntityId(searchRequestBodyDto);
		then(this.apiResponder).should()
			.respond(expectedCount, TimesheetTestDataFactory.Messages.TIMESHEET_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get timesheets count by entity id with timesheetIds delegates to service")
	void testGetTimesheetsCountByEntityIdWithTimesheetIdsDelegatesToService() {
		SearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createSearchRequest();
		searchRequestBodyDto.setTimesheetIds(List.of(101, 102));
		TimesheetCountResponseBodyDto expectedCount = TimesheetTestDataFactory.createTimesheetCountResponse();
		ResponseEntity<APINormalResponse<TimesheetCountResponseBodyDto>> expectedResponseEntity = TimesheetTestDataFactory
			.createTimesheetCountSuccessResponse(expectedCount);

		given(this.timesheetService.getTimesheetsCountByEntityId(searchRequestBodyDto)).willReturn(expectedCount);
		given(this.apiResponder.respond(expectedCount,
				TimesheetTestDataFactory.Messages.TIMESHEET_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		ResponseEntity<?> response = this.timesheetController.getTimesheetsCountByEntityId(searchRequestBodyDto);

		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.timesheetService).should().getTimesheetsCountByEntityId(searchRequestBodyDto);
	}

	@Test
	@DisplayName("Update job timesheet access control successfully")
	void testUpdateJobTimesheetAccessControlValidRequestUpdatesAccessControl() {
		// Arrange
		Integer jobId = TimesheetTestDataFactory.getDefaultJobId();
		UpdateJobTimesheetAccessControlRequestBodyDto requestDto = new UpdateJobTimesheetAccessControlRequestBodyDto();
		ResponseEntity<?> expectedResponseEntity = TimesheetTestDataFactory.createVoidSuccessResponse();

		Mockito.doNothing().when(this.timesheetService).updateJobTimesheetAccessControl(jobId, requestDto);
		@SuppressWarnings("unchecked")
		ResponseEntity<APINormalResponse<Object>> mockResponse = (ResponseEntity<APINormalResponse<Object>>) expectedResponseEntity;
		Mockito
			.when(this.apiResponder.respond(null, "Timesheet Access Controls Updated successfully",
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(mockResponse);

		// Act
		ResponseEntity<?> response = this.timesheetController.updateJobTimesheetAccessControl(jobId, requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService).updateJobTimesheetAccessControl(jobId, requestDto);
		Mockito.verify(this.apiResponder)
			.respond(null, "Timesheet Access Controls Updated successfully", APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get timesheet job access info successfully")
	void testGetTimesheetJobAccessInfoValidJobIdReturnsAccessInfo() {
		// Arrange
		Integer jobId = TimesheetTestDataFactory.getDefaultJobId();
		TimesheetJobAccessControlResponseBodyDto expectedAccessInfo = new TimesheetJobAccessControlResponseBodyDto();
		ResponseEntity<APINormalResponse<TimesheetJobAccessControlResponseBodyDto>> expectedResponseEntity = new ResponseEntity<>(
				new APINormalResponse<>(expectedAccessInfo), HttpStatus.OK);

		Mockito.when(this.timesheetService.getTimesheetJobAccessInfo(jobId)).thenReturn(expectedAccessInfo);
		Mockito
			.when(this.apiResponder.respond(expectedAccessInfo, "Timesheet job access info fetched successfully",
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetController.getTimesheetJobAccessInfo(jobId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService).getTimesheetJobAccessInfo(jobId);
		Mockito.verify(this.apiResponder)
			.respond(expectedAccessInfo, "Timesheet job access info fetched successfully", APIResponseType.SUCCESS,
					HttpStatus.OK);
	}

	@Test
	@DisplayName("Create bulk timesheets for multiple jobs successfully")
	void testCreateBulkTimesheetsForMultipleJobsValidRequestCreatesTimesheets() {
		// Arrange
		io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto requestDto = TimesheetTestDataFactory
			.createBulkTimesheetsForMultipleJobsRequest();
		ResponseEntity<?> expectedResponseEntity = TimesheetTestDataFactory.createVoidSuccessResponse();

		Mockito.doNothing().when(this.timesheetService).createBulkTimesheetsForMultipleJobs(requestDto);
		@SuppressWarnings("unchecked")
		ResponseEntity<APINormalResponse<Object>> mockResponse = (ResponseEntity<APINormalResponse<Object>>) expectedResponseEntity;
		Mockito
			.when(this.apiResponder.respond(null, "Timesheets created successfully for multiple jobs",
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(mockResponse);

		// Act
		ResponseEntity<?> response = this.timesheetController.createBulkTimesheetsForMultipleJobs(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService).createBulkTimesheetsForMultipleJobs(requestDto);
		Mockito.verify(this.apiResponder)
			.respond(null, "Timesheets created successfully for multiple jobs", APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search timesheets successfully")
	void testSearchTimesheetsValidRequestReturnsTimesheets() {
		// Arrange
		TimesheetSearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createTimesheetSearchRequest();
		PaginationRequestBodyDto paginationRequestBodyDto = TimesheetTestDataFactory.createPaginationRequest();
		List<TimesheetListResponseBodyDto> expectedTimesheets = TimesheetTestDataFactory
			.createTimesheetListResponseList();
		ResponseEntity<APINormalResponse<List<TimesheetListResponseBodyDto>>> expectedResponseEntity = TimesheetTestDataFactory
			.createTimesheetListSuccessResponse(expectedTimesheets);

		Mockito
			.when(this.timesheetService.searchTimesheets(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedTimesheets);
		Mockito
			.when(this.apiResponder.respond(expectedTimesheets, "Timesheets fetched successfully",
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetController.searchTimesheets(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService)
			.searchTimesheets(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedTimesheets, "Timesheets fetched successfully", APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search timesheets count successfully")
	void testSearchTimesheetsCountValidRequestReturnsCount() {
		// Arrange
		TimesheetSearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createTimesheetSearchRequest();
		Long expectedCount = TimesheetTestDataFactory.getDefaultTimesheetCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = TimesheetTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.timesheetService.searchTimesheetsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount, "Timesheet count fetched successfully",
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetController.searchTimesheetsCount(searchRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService).searchTimesheetsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, "Timesheet count fetched successfully", APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search timesheets count with timesheetIds delegates to service")
	void testSearchTimesheetsCountWithTimesheetIdsDelegatesToService() {
		TimesheetSearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createTimesheetSearchRequest();
		searchRequestBodyDto.setTimesheetIds(List.of(10, 20, 30));
		Long expectedCount = 2L;
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = TimesheetTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.timesheetService.searchTimesheetsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount, "Timesheet count fetched successfully",
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		ResponseEntity<?> response = this.timesheetController.searchTimesheetsCount(searchRequestBodyDto);

		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService).searchTimesheetsCount(searchRequestBodyDto);
	}

	@Test
	@DisplayName("Search timesheets with isReimbursement delegates to service")
	void testSearchTimesheetsWithIsReimbursementDelegatesToService() {
		TimesheetSearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createTimesheetSearchRequest();
		searchRequestBodyDto.setIsReimbursement(true);
		PaginationRequestBodyDto paginationRequestBodyDto = TimesheetTestDataFactory.createPaginationRequest();
		List<TimesheetListResponseBodyDto> expectedTimesheets = TimesheetTestDataFactory
			.createTimesheetListResponseList();
		ResponseEntity<APINormalResponse<List<TimesheetListResponseBodyDto>>> expectedResponseEntity = TimesheetTestDataFactory
			.createTimesheetListSuccessResponse(expectedTimesheets);

		Mockito
			.when(this.timesheetService.searchTimesheets(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedTimesheets);
		Mockito
			.when(this.apiResponder.respond(expectedTimesheets, "Timesheets fetched successfully",
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		ResponseEntity<?> response = this.timesheetController.searchTimesheets(searchRequestBodyDto,
				paginationRequestBodyDto);

		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService)
			.searchTimesheets(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
	}

	@Test
	@DisplayName("Search timesheets count with isReimbursement delegates to service")
	void testSearchTimesheetsCountWithIsReimbursementDelegatesToService() {
		TimesheetSearchRequestBodyDto searchRequestBodyDto = TimesheetTestDataFactory.createTimesheetSearchRequest();
		searchRequestBodyDto.setIsReimbursement(true);
		Long expectedCount = 4L;
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = TimesheetTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.timesheetService.searchTimesheetsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount, "Timesheet count fetched successfully",
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		ResponseEntity<?> response = this.timesheetController.searchTimesheetsCount(searchRequestBodyDto);

		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService).searchTimesheetsCount(searchRequestBodyDto);
	}

	@Test
	@DisplayName("Search entity successfully")
	void testSearchEntityValidRequestReturnsSearchResult() {
		// Arrange
		SearchEntityRequestBodyDto requestDto = new SearchEntityRequestBodyDto("test", true, true, false, false);
		SearchEntityResponseBodyDto expectedResult = new SearchEntityResponseBodyDto(java.util.Map.of());
		ResponseEntity<APINormalResponse<SearchEntityResponseBodyDto>> expectedResponseEntity = new ResponseEntity<>(
				new APINormalResponse<>(expectedResult), HttpStatus.OK);

		Mockito.when(this.timesheetService.searchEntity(requestDto)).thenReturn(expectedResult);
		Mockito
			.when(this.apiResponder.respond(expectedResult, "Entities searched successfully", APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetController.searchEntity(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService).searchEntity(requestDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedResult, "Entities searched successfully", APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Migrate timesheet total columns successfully")
	void testMigrateTimesheetTotalColumnsValidRequestReturnsMigrationResult() {
		// Arrange
		TimesheetMigrationRequestBodyDto requestDto = new TimesheetMigrationRequestBodyDto();
		requestDto.setBatchSize(100);
		requestDto.setOffset(0);
		TimesheetMigrationResponseBodyDto expectedResult = TimesheetMigrationResponseBodyDto.builder()
			.totalProcessed(10)
			.successCount(10)
			.failureCount(0)
			.hasMore(false)
			.nextOffset(10)
			.build();
		ResponseEntity<APINormalResponse<TimesheetMigrationResponseBodyDto>> expectedResponseEntity = new ResponseEntity<>(
				new APINormalResponse<>(expectedResult), HttpStatus.OK);

		Mockito.when(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).thenReturn(1);
		Mockito.when(this.auth.getAuthenticationPrincipalUniqueIdentifier()).thenReturn(1);
		Mockito.when(this.timesheetService.migrateTimesheetTotalColumns(requestDto)).thenReturn(expectedResult);
		Mockito
			.when(this.apiResponder.respond(expectedResult, "Migration completed", APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetController.migrateTimesheetTotalColumns(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetService).migrateTimesheetTotalColumns(requestDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedResult, "Migration completed", APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Migrate timesheet total columns handles exception")
	void testMigrateTimesheetTotalColumnsServiceThrowsReturnsError() {
		// Arrange
		TimesheetMigrationRequestBodyDto requestDto = new TimesheetMigrationRequestBodyDto();
		requestDto.setBatchSize(100);
		requestDto.setOffset(0);
		RuntimeException ex = new RuntimeException("Migration failed");
		ResponseEntity<APIErrorResponse> expectedErrorEntity = new ResponseEntity<>(new APIErrorResponse(ex),
				HttpStatus.INTERNAL_SERVER_ERROR);

		Mockito.when(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).thenReturn(1);
		Mockito.when(this.auth.getAuthenticationPrincipalUniqueIdentifier()).thenReturn(1);
		Mockito.when(this.timesheetService.migrateTimesheetTotalColumns(requestDto)).thenThrow(ex);
		Mockito.when(this.apiResponder.respondWithError(ex)).thenReturn(expectedErrorEntity);

		// Act
		ResponseEntity<?> response = this.timesheetController.migrateTimesheetTotalColumns(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedErrorEntity);
		Mockito.verify(this.timesheetService).migrateTimesheetTotalColumns(requestDto);
		Mockito.verify(this.apiResponder).respondWithError(ex);
	}

	@Test
	@DisplayName("Migrate timesheet total columns in prod with unauthorized user returns error")
	void testMigrateTimesheetTotalColumnsProdUnauthorizedReturnsError() {
		// Arrange
		TimesheetController prodController = new TimesheetController(this.timesheetService, this.apiResponder,
				this.auth, "prod");
		TimesheetMigrationRequestBodyDto requestDto = new TimesheetMigrationRequestBodyDto();
		requestDto.setBatchSize(100);
		requestDto.setOffset(0);
		ResponseEntity<APIErrorResponse> expectedErrorEntity = new ResponseEntity<>(
				new APIErrorResponse(new RuntimeException("You are not authorised to do this migration.")),
				HttpStatus.FORBIDDEN);

		Mockito.when(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).thenReturn(999);
		Mockito.when(this.auth.getAuthenticationPrincipalUniqueIdentifier()).thenReturn(999);
		Mockito.when(this.apiResponder.respondWithError(Mockito.any(Exception.class))).thenReturn(expectedErrorEntity);

		// Act
		ResponseEntity<?> response = prodController.migrateTimesheetTotalColumns(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedErrorEntity);
		Mockito.verify(this.timesheetService, Mockito.never()).migrateTimesheetTotalColumns(requestDto);
		Mockito.verify(this.apiResponder).respondWithError(Mockito.any(Exception.class));
	}

}