package io.recruitcrm.microservice.timesheet.controllers.entity_columns;

import io.recruitcrm.microservice.timesheet.dto.entity_columns.AccountViewColumnResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.entity_columns.EntityColumnService;
import io.recruitcrm.microservice.timesheet.testdata.EntityColumnTestDataFactory;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EntityColumnControllerTests {

	@Mock
	private EntityColumnService entityColumnService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private EntityColumnController entityColumnController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Get entity columns successfully")
	void testGetEntityColumnsValidEntityReturnsColumns() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getDefaultEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getEntityColumns(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get entity columns for all timesheet page successfully")
	void testGetEntityColumnsAllTimesheetPageValidEntityReturnsColumns() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getAllTimesheetPageEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getEntityColumns(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get entity columns for all contractor page successfully")
	void testGetEntityColumnsAllContractorPageValidEntityReturnsColumns() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getAllContractorPageEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getEntityColumns(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get entity columns for timesheet contractor successfully")
	void testGetEntityColumnsTimesheetContractorValidEntityReturnsColumns() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getTimesheetContractorEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getEntityColumns(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get entity columns for timesheet deal successfully")
	void testGetEntityColumnsTimesheetDealValidEntityReturnsColumns() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getTimesheetDealEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getEntityColumns(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get entity columns for contractor portal successfully")
	void testGetEntityColumnsContractorPortalValidEntityReturnsColumns() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getContractorPortalEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getEntityColumns(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get entity columns for client portal successfully")
	void testGetEntityColumnsClientPortalValidEntityReturnsColumns() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getClientPortalEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getEntityColumns(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get entity columns verifies response structure with columns key")
	void testGetEntityColumnsValidEntityVerifiesResponseStructure() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getDefaultEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Mockito.verify(this.entityColumnService).getEntityColumns(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get entity columns verifies service is called with exact entity parameter")
	void testGetEntityColumnsValidEntityVerifiesServiceCalledWithExactParameter() {
		// Arrange
		String entity = "custom_entity_type";
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getEntityColumns("custom_entity_type");
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get entity columns with lowercase entity name returns columns")
	void testGetEntityColumnsLowercaseEntityNameReturnsColumns() {
		// Arrange
		String entity = "timesheet";
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Mockito.verify(this.entityColumnService).getEntityColumns(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get entity columns with underscore entity name returns columns")
	void testGetEntityColumnsUnderscoreEntityNameReturnsColumns() {
		// Arrange
		String entity = "timesheet_contractor";
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Mockito.verify(this.entityColumnService).getEntityColumns(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get entity columns verifies response has correct HTTP status")
	void testGetEntityColumnsValidEntityVerifiesHttpStatusOk() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getDefaultEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getEntityColumns(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get entity columns verifies API responder called with correct message")
	void testGetEntityColumnsValidEntityVerifiesApiResponderMessage() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getDefaultEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getEntityColumns(entity)).thenReturn(expectedColumns);
		Mockito
			.when(this.apiResponder.respond(expectedData,
					EntityColumnTestDataFactory.Messages.ENTITY_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getEntityColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getEntityColumns(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, "Entity columns fetched successfully", APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get account view columns for all contractors page successfully")
	void testGetAccountViewColumnsAllContractorPageReturnsColumns() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getAllContractorPageEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createAccountViewColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getAccountViewColumnsByEntity(entity)).thenReturn(expectedColumns);
		Mockito.when(this.apiResponder.respond(expectedData,
				EntityColumnTestDataFactory.Messages.ACCOUNT_VIEW_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getAccountViewColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getAccountViewColumnsByEntity(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ACCOUNT_VIEW_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get account view columns for all timesheets page successfully")
	void testGetAccountViewColumnsAllTimesheetPageReturnsColumns() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getAllTimesheetPageEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createAccountViewColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getAccountViewColumnsByEntity(entity)).thenReturn(expectedColumns);
		Mockito.when(this.apiResponder.respond(expectedData,
				EntityColumnTestDataFactory.Messages.ACCOUNT_VIEW_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getAccountViewColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getAccountViewColumnsByEntity(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ACCOUNT_VIEW_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get account view columns for timesheet contractor successfully")
	void testGetAccountViewColumnsTimesheetContractorReturnsColumns() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getTimesheetContractorEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createAccountViewColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getAccountViewColumnsByEntity(entity)).thenReturn(expectedColumns);
		Mockito.when(this.apiResponder.respond(expectedData,
				EntityColumnTestDataFactory.Messages.ACCOUNT_VIEW_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getAccountViewColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.entityColumnService).getAccountViewColumnsByEntity(entity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ACCOUNT_VIEW_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get account view columns verifies response structure with accountViewColumns key")
	void testGetAccountViewColumnsVerifiesResponseStructure() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getAllContractorPageEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createAccountViewColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getAccountViewColumnsByEntity(entity)).thenReturn(expectedColumns);
		Mockito.when(this.apiResponder.respond(expectedData,
				EntityColumnTestDataFactory.Messages.ACCOUNT_VIEW_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getAccountViewColumns(entity);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Mockito.verify(this.entityColumnService).getAccountViewColumnsByEntity(entity);
	}

	@Test
	@DisplayName("Get account view columns verifies API responder called with correct message")
	void testGetAccountViewColumnsVerifiesApiResponderMessage() {
		// Arrange
		String entity = EntityColumnTestDataFactory.getAllContractorPageEntity();
		AccountViewColumnResponseBodyDto expectedColumns = EntityColumnTestDataFactory
			.createAccountViewColumnResponse();
		List<Map<String, AccountViewColumnResponseBodyDto>> expectedData = EntityColumnTestDataFactory
			.createAccountViewColumnsList();
		ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> expectedResponseEntity = EntityColumnTestDataFactory
			.createEntityColumnsSuccessResponse(expectedData);

		Mockito.when(this.entityColumnService.getAccountViewColumnsByEntity(entity)).thenReturn(expectedColumns);
		Mockito.when(this.apiResponder.respond(expectedData,
				EntityColumnTestDataFactory.Messages.ACCOUNT_VIEW_COLUMNS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.entityColumnController.getAccountViewColumns(entity);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.apiResponder)
			.respond(expectedData, EntityColumnTestDataFactory.Messages.ACCOUNT_VIEW_COLUMNS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

}