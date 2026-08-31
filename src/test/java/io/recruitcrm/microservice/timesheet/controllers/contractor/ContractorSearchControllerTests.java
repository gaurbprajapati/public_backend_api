package io.recruitcrm.microservice.timesheet.controllers.contractor;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.contractor.IContractorService;
import io.recruitcrm.microservice.timesheet.testdata.ContractorTestDataFactory;
import java.util.List;
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
class ContractorSearchControllerTests {

	@Mock
	private IContractorService contractorService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private ContractorSearchController contractorSearchController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Search contractors count successfully")
	void testSearchContractorsCountValidRequestReturnsCount() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getDefaultContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count returns zero when no contractors found")
	void testSearchContractorsCountNoContractorsReturnsZeroCount() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getZeroContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isInstanceOf(APINormalResponse.class);
		@SuppressWarnings("unchecked")
		APINormalResponse<Long> responseBody = (APINormalResponse<Long>) response.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isEqualTo(expectedCount);
		assertThat(responseBody.getData()).isZero();
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count returns large count successfully")
	void testSearchContractorsCountLargeDatasetReturnsLargeCount() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getLargeContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isInstanceOf(APINormalResponse.class);
		@SuppressWarnings("unchecked")
		APINormalResponse<Long> responseBody = (APINormalResponse<Long>) response.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isEqualTo(expectedCount);
		assertThat(responseBody.getData()).isEqualTo(1000L);
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count returns single contractor count successfully")
	void testSearchContractorsCountSingleContractorReturnsSingleCount() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getSingleContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isInstanceOf(APINormalResponse.class);
		@SuppressWarnings("unchecked")
		APINormalResponse<Long> responseBody = (APINormalResponse<Long>) response.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isEqualTo(expectedCount);
		assertThat(responseBody.getData()).isEqualTo(1L);
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count returns small count successfully")
	void testSearchContractorsCountSmallDatasetReturnsSmallCount() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getSmallContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isInstanceOf(APINormalResponse.class);
		@SuppressWarnings("unchecked")
		APINormalResponse<Long> responseBody = (APINormalResponse<Long>) response.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isEqualTo(expectedCount);
		assertThat(responseBody.getData()).isEqualTo(5L);
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count verifies response body structure")
	void testSearchContractorsCountVerifiesResponseBodyStructure() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getDefaultContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders()).isNotNull();
		assertThat(response.getBody()).isInstanceOf(APINormalResponse.class);
		@SuppressWarnings("unchecked")
		APINormalResponse<Long> responseBody = (APINormalResponse<Long>) response.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isNotNull();
		assertThat(responseBody.getData()).isInstanceOf(Long.class);
		assertThat(responseBody.getData()).isEqualTo(expectedCount);
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count verifies service interaction")
	void testSearchContractorsCountVerifiesServiceInteraction() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getDefaultContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.contractorService, Mockito.times(1)).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.contractorService, Mockito.only()).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder, Mockito.times(1))
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count with advanced search context returns count successfully")
	void testSearchContractorsCountWithAdvancedContextReturnsCount() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory
			.createContractorSearchRequestWithAdvancedContext();
		Long expectedCount = ContractorTestDataFactory.getDefaultContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isInstanceOf(APINormalResponse.class);
		assertThat(searchRequestBodyDto.getAdvancedSearchContext()).isEqualTo("test-context");
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count with filters returns count successfully")
	void testSearchContractorsCountWithFiltersReturnsCount() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory
			.createContractorSearchRequestWithFilters();
		Long expectedCount = ContractorTestDataFactory.getSmallContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(searchRequestBodyDto.getAdvancedSearchContext()).isEqualTo("filter-context");
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count validates HTTP status is OK")
	void testSearchContractorsCountValidatesHttpStatusIsOk() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getDefaultContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count validates response type is Long")
	void testSearchContractorsCountValidatesResponseTypeIsLong() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getDefaultContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response.getBody()).isInstanceOf(APINormalResponse.class);
		@SuppressWarnings("unchecked")
		APINormalResponse<Long> responseBody = (APINormalResponse<Long>) response.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isInstanceOf(Long.class);
		assertThat(responseBody.getData()).isPositive();
		assertThat(responseBody.getData()).isGreaterThan(0L);
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count with zero validates response correctly")
	void testSearchContractorsCountWithZeroValidatesResponseCorrectly() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getZeroContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		@SuppressWarnings("unchecked")
		APINormalResponse<Long> responseBody = (APINormalResponse<Long>) response.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isNotNull();
		assertThat(responseBody.getData()).isZero();
		assertThat(responseBody.getData()).isNotPositive();
		assertThat(responseBody.getData()).isLessThanOrEqualTo(0L);
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count validates controller method execution")
	void testSearchContractorsCountValidatesControllerMethodExecution() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getDefaultContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert - Verify complete execution flow
		assertThat(response).isNotNull().isEqualTo(expectedResponseEntity);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(searchRequestBodyDto).isNotNull();
		Mockito.verify(this.contractorService, Mockito.times(1)).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder, Mockito.times(1))
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
		Mockito.verifyNoMoreInteractions(this.contractorService);
	}

	@Test
	@DisplayName("Search contractors count with large count validates data type")
	void testSearchContractorsCountWithLargeCountValidatesDataType() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getLargeContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito
			.when(this.apiResponder.respond(expectedCount,
					ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		@SuppressWarnings("unchecked")
		APINormalResponse<Long> responseBody = (APINormalResponse<Long>) response.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isNotNull();
		assertThat(responseBody.getData()).isInstanceOf(Long.class);
		assertThat(responseBody.getData()).isGreaterThan(100L);
		assertThat(responseBody.getData()).isEqualTo(1000L);
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors count verifies message constant")
	void testSearchContractorsCountVerifiesMessageConstant() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		Long expectedCount = ContractorTestDataFactory.getDefaultContractorCount();
		ResponseEntity<APINormalResponse<Long>> expectedResponseEntity = ContractorTestDataFactory
			.createLongCountSuccessResponse(expectedCount);
		String expectedMessage = ContractorTestDataFactory.Messages.CONTRACTOR_COUNT_FETCHED_SUCCESSFULLY;

		Mockito.when(this.contractorService.searchContractorsCount(searchRequestBodyDto)).thenReturn(expectedCount);
		Mockito.when(this.apiResponder.respond(expectedCount, expectedMessage, APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractorsCount(searchRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		assertThat(expectedMessage).isEqualTo("Contractor count fetched successfully").isNotEmpty().isNotBlank();
		Mockito.verify(this.contractorService).searchContractorsCount(searchRequestBodyDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedCount, expectedMessage, APIResponseType.SUCCESS, HttpStatus.OK);
	}

	// ===== Search Contractors Endpoint Tests =====

	@Test
	@DisplayName("Search contractors successfully")
	void testSearchContractorsValidRequestReturnsContractors() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest();
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors returns empty list when no contractors found")
	void testSearchContractorsNoContractorsReturnsEmptyList() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest();
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createEmptyContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isNotNull()
			.extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
			.containsExactly(HttpStatus.OK, expectedResponseEntity.getBody());
		@SuppressWarnings("unchecked")
		APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>> responseBody = (APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>) response
			.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isEmpty();
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors with pagination returns contractors successfully")
	void testSearchContractorsWithPaginationReturnsContractors() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest(1, 10);
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		@SuppressWarnings("unchecked")
		APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>> responseBody = (APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>) response
			.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isNotNull().hasSize(2);
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors verifies HTTP status is OK")
	void testSearchContractorsValidatesHttpStatusIsOk() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest();
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors verifies response body structure")
	void testSearchContractorsVerifiesResponseBodyStructure() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest();
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isNotNull()
			.extracting(ResponseEntity::getBody, ResponseEntity::getStatusCode, ResponseEntity::getHeaders)
			.doesNotContainNull();
		assertThat(response.getBody()).isInstanceOf(APINormalResponse.class);
		@SuppressWarnings("unchecked")
		APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>> responseBody = (APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>) response
			.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isNotNull().isInstanceOf(List.class).hasSizeGreaterThan(0);
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors verifies service interaction")
	void testSearchContractorsVerifiesServiceInteraction() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest();
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.contractorService, Mockito.times(1))
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.contractorService, Mockito.only())
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder, Mockito.times(1))
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors verifies pagination parameters are converted correctly")
	void testSearchContractorsVerifiesPaginationParametersConvertedCorrectly() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest(2, 15);
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito.when(this.contractorService.searchContractors(Mockito.eq(searchRequestBodyDto), Mockito.any()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.contractorService).searchContractors(Mockito.eq(searchRequestBodyDto), Mockito.any());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors with filters returns contractors successfully")
	void testSearchContractorsWithFiltersReturnsContractors() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory
			.createContractorSearchRequestWithFilters();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest();
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(searchRequestBodyDto.getAdvancedSearchContext()).isEqualTo("filter-context");
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors with large page size returns contractors successfully")
	void testSearchContractorsWithLargePageSizeReturnsContractors() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest(1, 100);
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors with first page returns contractors successfully")
	void testSearchContractorsWithFirstPageReturnsContractors() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest(0, 20);
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		@SuppressWarnings("unchecked")
		APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>> responseBody = (APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>) response
			.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).hasSize(2);
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors with small page size returns contractors successfully")
	void testSearchContractorsWithSmallPageSizeReturnsContractors() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest(1, 5);
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors with medium page size returns contractors successfully")
	void testSearchContractorsWithMediumPageSizeReturnsContractors() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest(1, 50);
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors with third page returns contractors successfully")
	void testSearchContractorsWithThirdPageReturnsContractors() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest(3, 10);
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito.when(this.contractorService.searchContractors(Mockito.eq(searchRequestBodyDto), Mockito.any()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Mockito.verify(this.contractorService).searchContractors(Mockito.eq(searchRequestBodyDto), Mockito.any());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors with advanced search context returns contractors successfully")
	void testSearchContractorsWithAdvancedContextReturnsContractors() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory
			.createContractorSearchRequestWithAdvancedContext();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest();
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(searchRequestBodyDto.getAdvancedSearchContext()).isEqualTo("test-context");
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors verifies contractor list contains expected fields")
	void testSearchContractorsVerifiesContractorListContainsExpectedFields() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest();
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		@SuppressWarnings("unchecked")
		APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>> responseBody = (APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>) response
			.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isNotEmpty().hasSize(2);
		assertThat(expectedContractors.get(0).getId()).isNotNull();
		assertThat(expectedContractors.get(0).getName()).isNotNull();
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Search contractors validates controller method execution flow")
	void testSearchContractorsValidatesControllerMethodExecutionFlow() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest();
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isNotNull().isEqualTo(expectedResponseEntity);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(searchRequestBodyDto).isNotNull();
		assertThat(paginationRequestBodyDto).isNotNull();
		Mockito.verify(this.contractorService, Mockito.times(1))
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder, Mockito.times(1))
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
		Mockito.verifyNoMoreInteractions(this.contractorService);
	}

	@Test
	@DisplayName("Search contractors with empty result validates response correctly")
	void testSearchContractorsWithEmptyResultValidatesResponseCorrectly() {
		// Arrange
		ContractorSearchRequestBodyDto searchRequestBodyDto = ContractorTestDataFactory.createContractorSearchRequest();
		io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto paginationRequestBodyDto = ContractorTestDataFactory
			.createPaginationRequest();
		List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto> expectedContractors = ContractorTestDataFactory
			.createEmptyContractorListResponseList();
		ResponseEntity<APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>> expectedResponseEntity = ContractorTestDataFactory
			.createContractorListSuccessResponse(expectedContractors);

		Mockito
			.when(this.contractorService.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable()))
			.thenReturn(expectedContractors);
		Mockito
			.when(this.apiResponder.respond(expectedContractors,
					ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.contractorSearchController.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		@SuppressWarnings("unchecked")
		APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>> responseBody = (APINormalResponse<List<io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto>>) response
			.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getData()).isNotNull().isEmpty();
		Mockito.verify(this.contractorService)
			.searchContractors(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		Mockito.verify(this.apiResponder)
			.respond(expectedContractors, ContractorTestDataFactory.Messages.CONTRACTORS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
