package io.recruitcrm.microservice.timesheet.controllers.reimbursement;

import io.recruitcrm.microservice.timesheet.dto.reimbursement.CreateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementListItemResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdatePayableBillableRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateShareWithClientRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReopenReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.reimbursement.TimesheetReimbursementService;
import io.recruitcrm.microservice.timesheet.testdata.ReimbursementTestDataFactory;
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
class TimesheetReimbursementControllerTests {

	@Mock
	private TimesheetReimbursementService timesheetReimbursementService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private TimesheetReimbursementController timesheetReimbursementController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Create reimbursement successfully")
	void testCreateReimbursementValidRequestCreatesReimbursement() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto requestDto = ReimbursementTestDataFactory.createReimbursementRequest();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.createReimbursementResponse();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.createReimbursementSuccessResponse(expectedResponse);

		Mockito.when(this.timesheetReimbursementService.createReimbursement(timesheetId, requestDto))
			.thenReturn(expectedResponse);
		Mockito
			.when(this.apiResponder.respond(expectedResponse,
					ReimbursementTestDataFactory.Messages.REIMBURSEMENT_CREATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.CREATED))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.createReimbursement(timesheetId, requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetReimbursementService).createReimbursement(timesheetId, requestDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedResponse, ReimbursementTestDataFactory.Messages.REIMBURSEMENT_CREATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.CREATED);
	}

	@Test
	@DisplayName("Create reimbursement without file successfully")
	void testCreateReimbursementWithoutFileValidRequestCreatesReimbursement() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto requestDto = ReimbursementTestDataFactory
			.createReimbursementRequestWithoutFile();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createReimbursementResponseWithoutFile();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.createReimbursementSuccessResponse(expectedResponse);

		Mockito.when(this.timesheetReimbursementService.createReimbursement(timesheetId, requestDto))
			.thenReturn(expectedResponse);
		Mockito
			.when(this.apiResponder.respond(expectedResponse,
					ReimbursementTestDataFactory.Messages.REIMBURSEMENT_CREATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.CREATED))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.createReimbursement(timesheetId, requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetReimbursementService).createReimbursement(timesheetId, requestDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedResponse, ReimbursementTestDataFactory.Messages.REIMBURSEMENT_CREATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.CREATED);
	}

	@Test
	@DisplayName("Reopen reimbursement successfully")
	void testReopenReimbursementValidRequestReopensReimbursement() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		ReopenReimbursementRequestBodyDto requestDto = ReimbursementTestDataFactory.createReopenReimbursementRequest();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.createReimbursementResponse();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.updateReimbursementSuccessResponse(expectedResponse);

		Mockito.when(this.timesheetReimbursementService.reopenReimbursement(timesheetId, reimbursementId, requestDto))
			.thenReturn(expectedResponse);
		Mockito
			.when(this.apiResponder.respond(expectedResponse,
					ReimbursementTestDataFactory.Messages.REIMBURSEMENT_REOPENED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.reopenReimbursement(timesheetId,
				reimbursementId, requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetReimbursementService)
			.reopenReimbursement(timesheetId, reimbursementId, requestDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedResponse, ReimbursementTestDataFactory.Messages.REIMBURSEMENT_REOPENED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Update payable billable successfully")
	void testUpdatePayableBillableValidRequestUpdatesFlags() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto requestDto = ReimbursementTestDataFactory
			.createUpdatePayableBillableRequest();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createApprovedReimbursementResponse();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.updateReimbursementSuccessResponse(expectedResponse);

		Mockito.when(this.timesheetReimbursementService.updatePayableBillable(timesheetId, reimbursementId, requestDto))
			.thenReturn(expectedResponse);
		Mockito
			.when(this.apiResponder.respond(expectedResponse,
					ReimbursementTestDataFactory.Messages.REIMBURSEMENT_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.updatePayableBillable(timesheetId,
				reimbursementId, requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetReimbursementService)
			.updatePayableBillable(timesheetId, reimbursementId, requestDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedResponse, ReimbursementTestDataFactory.Messages.REIMBURSEMENT_UPDATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Update share with client successfully")
	void testUpdateShareWithClientValidRequestUpdatesShareStatus() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateShareWithClientRequestBodyDto requestDto = ReimbursementTestDataFactory.createShareWithClientOffRequest();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.updateShareWithClientResponse(ReimbursementTestDataFactory.getNotSharedWithClient());
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.updateReimbursementSuccessResponse(expectedResponse);

		Mockito.when(this.timesheetReimbursementService.updateShareWithClient(timesheetId, reimbursementId, requestDto))
			.thenReturn(expectedResponse);
		Mockito
			.when(this.apiResponder.respond(expectedResponse,
					ReimbursementTestDataFactory.Messages.REIMBURSEMENT_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.updateShareWithClient(timesheetId,
				reimbursementId, requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetReimbursementService)
			.updateShareWithClient(timesheetId, reimbursementId, requestDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedResponse, ReimbursementTestDataFactory.Messages.REIMBURSEMENT_UPDATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("List reimbursements successfully")
	void testListReimbursementsValidRequestReturnsList() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		List<ReimbursementListItemResponseBodyDto> expectedResponse = ReimbursementTestDataFactory
			.createReimbursementListItemResponseList();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.createReimbursementListItemSuccessResponse(expectedResponse);

		Mockito.when(this.timesheetReimbursementService.listReimbursements(timesheetId)).thenReturn(expectedResponse);
		Mockito
			.when(this.apiResponder.respond(expectedResponse,
					ReimbursementTestDataFactory.Messages.REIMBURSEMENTS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.listReimbursements(timesheetId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetReimbursementService).listReimbursements(timesheetId);
		Mockito.verify(this.apiResponder)
			.respond(expectedResponse, ReimbursementTestDataFactory.Messages.REIMBURSEMENTS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("List reimbursements returns empty list when no reimbursements")
	void testListReimbursementsNoReimbursementsReturnsEmptyList() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		List<ReimbursementListItemResponseBodyDto> expectedResponse = ReimbursementTestDataFactory
			.createEmptyReimbursementListItemResponseList();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.createReimbursementListItemSuccessResponse(expectedResponse);

		Mockito.when(this.timesheetReimbursementService.listReimbursements(timesheetId)).thenReturn(expectedResponse);
		Mockito
			.when(this.apiResponder.respond(expectedResponse,
					ReimbursementTestDataFactory.Messages.REIMBURSEMENTS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.listReimbursements(timesheetId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		assertThat(expectedResponse).isEmpty();
		Mockito.verify(this.timesheetReimbursementService).listReimbursements(timesheetId);
	}

	@Test
	@DisplayName("Update reimbursement successfully")
	void testUpdateReimbursementValidRequestUpdatesReimbursement() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto requestDto = ReimbursementTestDataFactory.updateReimbursementRequest();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.updateReimbursementResponse();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.updateReimbursementSuccessResponse(expectedResponse);

		Mockito.when(this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, requestDto))
			.thenReturn(expectedResponse);
		Mockito
			.when(this.apiResponder.respond(expectedResponse,
					ReimbursementTestDataFactory.Messages.REIMBURSEMENT_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.updateReimbursement(timesheetId,
				reimbursementId, requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetReimbursementService)
			.updateReimbursement(timesheetId, reimbursementId, requestDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedResponse, ReimbursementTestDataFactory.Messages.REIMBURSEMENT_UPDATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Update reimbursement with partial fields successfully")
	void testUpdateReimbursementPartialFieldsReturnsSuccess() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto requestDto = ReimbursementTestDataFactory.updateReimbursementRequestPartial();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.updateReimbursementResponse();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.updateReimbursementSuccessResponse(expectedResponse);

		Mockito.when(this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, requestDto))
			.thenReturn(expectedResponse);
		Mockito
			.when(this.apiResponder.respond(expectedResponse,
					ReimbursementTestDataFactory.Messages.REIMBURSEMENT_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.updateReimbursement(timesheetId,
				reimbursementId, requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetReimbursementService)
			.updateReimbursement(timesheetId, reimbursementId, requestDto);
	}

	@Test
	@DisplayName("Delete reimbursement successfully")
	void testDeleteReimbursementValidRequestDeletesReimbursement() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory.createVoidSuccessResponse();

		Mockito.doNothing().when(this.timesheetReimbursementService).deleteReimbursement(timesheetId, reimbursementId);
		Mockito
			.when(this.apiResponder.respond(null,
					ReimbursementTestDataFactory.Messages.REIMBURSEMENT_DELETED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.deleteReimbursement(timesheetId,
				reimbursementId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetReimbursementService).deleteReimbursement(timesheetId, reimbursementId);
		Mockito.verify(this.apiResponder)
			.respond(null, ReimbursementTestDataFactory.Messages.REIMBURSEMENT_DELETED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Update reimbursement status successfully")
	void testUpdateReimbursementStatusValidRequestUpdatesStatus() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementStatusRequestBodyDto requestDto = new UpdateReimbursementStatusRequestBodyDto(
				ReimbursementTestDataFactory.getStatusApproved(), "Approved by manager");
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createApprovedReimbursementResponse();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.updateReimbursementSuccessResponse(expectedResponse);

		Mockito
			.when(this.timesheetReimbursementService.updateReimbursementStatus(timesheetId, reimbursementId,
					requestDto))
			.thenReturn(expectedResponse);
		Mockito
			.when(this.apiResponder.respond(expectedResponse, "Reimbursement status updated successfully",
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.updateReimbursementStatus(timesheetId,
				reimbursementId, requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetReimbursementService)
			.updateReimbursementStatus(timesheetId, reimbursementId, requestDto);
	}

	@Test
	@DisplayName("Get reimbursement status history successfully")
	void testGetReimbursementStatusHistoryValidRequestReturnsHistory() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		List<ReimbursementStatusHistoryResponseBodyDto> expectedResponse = ReimbursementTestDataFactory
			.createStatusHistoryResponseList();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.createStatusHistoryListSuccessResponse(expectedResponse);

		Mockito.when(this.timesheetReimbursementService.getReimbursementStatusHistory(timesheetId, reimbursementId))
			.thenReturn(expectedResponse);
		Mockito
			.when(this.apiResponder.respond(expectedResponse, "Status history fetched successfully",
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.getReimbursementStatusHistory(timesheetId,
				reimbursementId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetReimbursementService).getReimbursementStatusHistory(timesheetId, reimbursementId);
	}

	@Test
	@DisplayName("Get reimbursement count successfully")
	void testGetReimbursementCountValidRequestReturnsCount() {
		// Arrange
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer expectedResponse = ReimbursementTestDataFactory.getDefaultReimbursementCount();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.createReimbursementCountSuccessResponse(expectedResponse);

		Mockito.when(this.timesheetReimbursementService.getReimbursementCount(timesheetId))
			.thenReturn(expectedResponse);
		Mockito.when(this.apiResponder.respond(expectedResponse,
				ReimbursementTestDataFactory.Messages.REIMBURSEMENT_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetReimbursementController.getReimbursementCount(timesheetId);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetReimbursementService).getReimbursementCount(timesheetId);
		Mockito.verify(this.apiResponder)
			.respond(expectedResponse, ReimbursementTestDataFactory.Messages.REIMBURSEMENT_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
