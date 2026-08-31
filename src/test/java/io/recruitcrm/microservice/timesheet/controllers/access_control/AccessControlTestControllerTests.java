package io.recruitcrm.microservice.timesheet.controllers.access_control;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.constants.AccessControlMessageConstants;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckResult;
import io.recruitcrm.microservice.timesheet.responses.APIErrorResponse;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccessControlTestController Tests")
class AccessControlTestControllerTests {

	@Mock
	private AccessControlChecker accessControlChecker;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private AccessControlTestController accessControlTestController;

	@Test
	@DisplayName("testTimesheetBulkPermissions - Success")
	void testTimesheetBulkPermissionsSuccess() {
		// Arrange
		List<Integer> timesheetIds = List.of(1, 2, 3);
		AccessControlTestController.TimesheetBulkTestRequest request = new AccessControlTestController.TimesheetBulkTestRequest(
				timesheetIds);

		BulkPermissionCheckResult expectedResult = BulkPermissionCheckResult.builder().results(List.of()).build();
		given(this.accessControlChecker.allowsBulk(any())).willReturn(expectedResult);

		ResponseEntity<APINormalResponse<BulkPermissionCheckResult>> expectedResponse = ResponseEntity.ok().build();
		given(this.apiResponder.respond(any(), any(), any(), any())).willReturn((ResponseEntity) expectedResponse);

		// Act
		ResponseEntity<?> result = this.accessControlTestController.testTimesheetBulkPermissions(request);

		// Assert
		assertThat(result).isEqualTo(expectedResponse);
		then(this.accessControlChecker).should().allowsBulk(any());
		then(this.apiResponder).should().respond(any(), any(), any(), any());
	}

	@Test
	@DisplayName("testTimesheetBulkPermissions - Exception")
	void testTimesheetBulkPermissionsException() {
		// Arrange
		List<Integer> timesheetIds = List.of(1, 2, 3);
		AccessControlTestController.TimesheetBulkTestRequest request = new AccessControlTestController.TimesheetBulkTestRequest(
				timesheetIds);

		RuntimeException exception = new RuntimeException("Test exception");
		given(this.accessControlChecker.allowsBulk(any())).willThrow(exception);

		ResponseEntity<APIErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.build();
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR))
			.willReturn(expectedResponse);

		// Act
		ResponseEntity<?> result = this.accessControlTestController.testTimesheetBulkPermissions(request);

		// Assert
		assertThat(result).isEqualTo(expectedResponse);
		then(this.apiResponder).should()
			.respondWithError(exception, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	@DisplayName("testCandidateBulkPermissions - Success")
	void testCandidateBulkPermissionsSuccess() {
		// Arrange
		List<Integer> candidateIds = List.of(1, 2, 3);
		AccessControlTestController.CandidateBulkTestRequest request = new AccessControlTestController.CandidateBulkTestRequest(
				candidateIds);

		BulkPermissionCheckResult expectedResult = BulkPermissionCheckResult.builder().results(List.of()).build();
		given(this.accessControlChecker.allowsBulk(any())).willReturn(expectedResult);

		ResponseEntity<APINormalResponse<BulkPermissionCheckResult>> expectedResponse = ResponseEntity.ok().build();
		given(this.apiResponder.respond(any(), any(), any(), any())).willReturn((ResponseEntity) expectedResponse);

		// Act
		ResponseEntity<?> result = this.accessControlTestController.testCandidateBulkPermissions(request);

		// Assert
		assertThat(result).isEqualTo(expectedResponse);
		then(this.accessControlChecker).should().allowsBulk(any());
		then(this.apiResponder).should().respond(any(), any(), any(), any());
	}

	@Test
	@DisplayName("testCandidateBulkPermissions - Exception")
	void testCandidateBulkPermissionsException() {
		// Arrange
		List<Integer> candidateIds = List.of(1, 2, 3);
		AccessControlTestController.CandidateBulkTestRequest request = new AccessControlTestController.CandidateBulkTestRequest(
				candidateIds);

		RuntimeException exception = new RuntimeException("Test exception");
		given(this.accessControlChecker.allowsBulk(any())).willThrow(exception);

		ResponseEntity<APIErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.build();
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR))
			.willReturn(expectedResponse);

		// Act
		ResponseEntity<?> result = this.accessControlTestController.testCandidateBulkPermissions(request);

		// Assert
		assertThat(result).isEqualTo(expectedResponse);
		then(this.apiResponder).should()
			.respondWithError(exception, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	@DisplayName("testJobBulkPermissions - Success")
	void testJobBulkPermissionsSuccess() {
		// Arrange
		List<Integer> jobIds = List.of(1, 2, 3);
		AccessControlTestController.JobBulkTestRequest request = new AccessControlTestController.JobBulkTestRequest(
				jobIds);

		BulkPermissionCheckResult expectedResult = BulkPermissionCheckResult.builder().results(List.of()).build();
		given(this.accessControlChecker.allowsBulk(any())).willReturn(expectedResult);

		ResponseEntity<APINormalResponse<BulkPermissionCheckResult>> expectedResponse = ResponseEntity.ok().build();
		given(this.apiResponder.respond(any(), any(), any(), any())).willReturn((ResponseEntity) expectedResponse);

		// Act
		ResponseEntity<?> result = this.accessControlTestController.testJobBulkPermissions(request);

		// Assert
		assertThat(result).isEqualTo(expectedResponse);
		then(this.accessControlChecker).should().allowsBulk(any());
		then(this.apiResponder).should().respond(any(), any(), any(), any());
	}

	@Test
	@DisplayName("testJobBulkPermissions - Exception")
	void testJobBulkPermissionsException() {
		// Arrange
		List<Integer> jobIds = List.of(1, 2, 3);
		AccessControlTestController.JobBulkTestRequest request = new AccessControlTestController.JobBulkTestRequest(
				jobIds);

		RuntimeException exception = new RuntimeException("Test exception");
		given(this.accessControlChecker.allowsBulk(any())).willThrow(exception);

		ResponseEntity<APIErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.build();
		given(this.apiResponder.respondWithError(exception, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR))
			.willReturn(expectedResponse);

		// Act
		ResponseEntity<?> result = this.accessControlTestController.testJobBulkPermissions(request);

		// Assert
		assertThat(result).isEqualTo(expectedResponse);
		then(this.apiResponder).should()
			.respondWithError(exception, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	@DisplayName("TimesheetBulkTestRequest - with valid timesheet IDs")
	void testTimesheetBulkTestRequestWithValidTimesheetIds() {
		// Arrange & Act
		List<Integer> timesheetIds = List.of(1, 2, 3);
		AccessControlTestController.TimesheetBulkTestRequest request = new AccessControlTestController.TimesheetBulkTestRequest(
				timesheetIds);

		// Assert
		assertThat(request.timesheetIds()).isEqualTo(timesheetIds);
	}

	@Test
	@DisplayName("TimesheetBulkTestRequest - with null timesheet IDs")
	void testTimesheetBulkTestRequestWithNullTimesheetIds() {
		// Act & Assert
		assertThatThrownBy(() -> new AccessControlTestController.TimesheetBulkTestRequest(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Timesheet IDs list cannot be null or empty");
	}

	@Test
	@DisplayName("TimesheetBulkTestRequest - with empty timesheet IDs")
	void testTimesheetBulkTestRequestWithEmptyTimesheetIds() {
		// Arrange
		List<Integer> emptyTimesheetIds = List.of();

		// Act & Assert
		assertThatThrownBy(() -> new AccessControlTestController.TimesheetBulkTestRequest(emptyTimesheetIds))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Timesheet IDs list cannot be null or empty");
	}

	@Test
	@DisplayName("CandidateBulkTestRequest - with valid candidate IDs")
	void testCandidateBulkTestRequestWithValidCandidateIds() {
		// Arrange & Act
		List<Integer> candidateIds = List.of(1, 2, 3);
		AccessControlTestController.CandidateBulkTestRequest request = new AccessControlTestController.CandidateBulkTestRequest(
				candidateIds);

		// Assert
		assertThat(request.candidateIds()).isEqualTo(candidateIds);
	}

	@Test
	@DisplayName("CandidateBulkTestRequest - with null candidate IDs")
	void testCandidateBulkTestRequestWithNullCandidateIds() {
		// Act & Assert
		assertThatThrownBy(() -> new AccessControlTestController.CandidateBulkTestRequest(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Candidate IDs list cannot be null or empty");
	}

	@Test
	@DisplayName("CandidateBulkTestRequest - with empty candidate IDs")
	void testCandidateBulkTestRequestWithEmptyCandidateIds() {
		// Arrange
		List<Integer> emptyCandidateIds = List.of();

		// Act & Assert
		assertThatThrownBy(() -> new AccessControlTestController.CandidateBulkTestRequest(emptyCandidateIds))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Candidate IDs list cannot be null or empty");
	}

	@Test
	@DisplayName("JobBulkTestRequest - with valid job IDs")
	void testJobBulkTestRequestWithValidJobIds() {
		// Arrange & Act
		List<Integer> jobIds = List.of(1, 2, 3);
		AccessControlTestController.JobBulkTestRequest request = new AccessControlTestController.JobBulkTestRequest(
				jobIds);

		// Assert
		assertThat(request.jobIds()).isEqualTo(jobIds);
	}

	@Test
	@DisplayName("JobBulkTestRequest - with null job IDs")
	void testJobBulkTestRequestWithNullJobIds() {
		// Act & Assert
		assertThatThrownBy(() -> new AccessControlTestController.JobBulkTestRequest(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Job IDs list cannot be null or empty");
	}

	@Test
	@DisplayName("JobBulkTestRequest - with empty job IDs")
	void testJobBulkTestRequestWithEmptyJobIds() {
		// Arrange
		List<Integer> emptyJobIds = List.of();

		// Act & Assert
		assertThatThrownBy(() -> new AccessControlTestController.JobBulkTestRequest(emptyJobIds))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Job IDs list cannot be null or empty");
	}

	@Test
	@DisplayName("testTimesheetBulkPermissions - with single timesheet ID")
	void testTimesheetBulkPermissionsWithSingleTimesheetId() {
		// Arrange
		List<Integer> timesheetIds = List.of(1);
		AccessControlTestController.TimesheetBulkTestRequest request = new AccessControlTestController.TimesheetBulkTestRequest(
				timesheetIds);

		BulkPermissionCheckResult expectedResult = BulkPermissionCheckResult.builder().results(List.of()).build();
		given(this.accessControlChecker.allowsBulk(any())).willReturn(expectedResult);

		// Mock the APIResponder to return a valid ResponseEntity
		APINormalResponse<BulkPermissionCheckResult> apiResponse = new APINormalResponse<>(expectedResult,
				AccessControlMessageConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX + "1 timesheets");
		ResponseEntity<APINormalResponse<BulkPermissionCheckResult>> mockResponse = ResponseEntity.ok(apiResponse);
		given(this.apiResponder.respond(any(), any(), any(), any())).willReturn((ResponseEntity) mockResponse);

		// Act
		ResponseEntity<?> result = this.accessControlTestController.testTimesheetBulkPermissions(request);

		// Assert
		assertThat(result).isNotNull();
		verify(this.accessControlChecker).allowsBulk(any());
		verify(this.apiResponder).respond(expectedResult,
				AccessControlMessageConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX + "1 timesheets",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("testCandidateBulkPermissions - with single candidate ID")
	void testCandidateBulkPermissionsWithSingleCandidateId() {
		// Arrange
		List<Integer> candidateIds = List.of(1);
		AccessControlTestController.CandidateBulkTestRequest request = new AccessControlTestController.CandidateBulkTestRequest(
				candidateIds);

		BulkPermissionCheckResult expectedResult = BulkPermissionCheckResult.builder().results(List.of()).build();
		given(this.accessControlChecker.allowsBulk(any())).willReturn(expectedResult);

		// Mock the APIResponder to return a valid ResponseEntity
		APINormalResponse<BulkPermissionCheckResult> apiResponse = new APINormalResponse<>(expectedResult,
				AccessControlMessageConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX + "1 candidates");
		ResponseEntity<APINormalResponse<BulkPermissionCheckResult>> mockResponse = ResponseEntity.ok(apiResponse);
		given(this.apiResponder.respond(any(), any(), any(), any())).willReturn((ResponseEntity) mockResponse);

		// Act
		ResponseEntity<?> result = this.accessControlTestController.testCandidateBulkPermissions(request);

		// Assert
		assertThat(result).isNotNull();
		verify(this.accessControlChecker).allowsBulk(any());
		verify(this.apiResponder).respond(expectedResult,
				AccessControlMessageConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX + "1 candidates",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("testJobBulkPermissions - with single job ID")
	void testJobBulkPermissionsWithSingleJobId() {
		// Arrange
		List<Integer> jobIds = List.of(1);
		AccessControlTestController.JobBulkTestRequest request = new AccessControlTestController.JobBulkTestRequest(
				jobIds);

		BulkPermissionCheckResult expectedResult = BulkPermissionCheckResult.builder().results(List.of()).build();
		given(this.accessControlChecker.allowsBulk(any())).willReturn(expectedResult);

		// Mock the APIResponder to return a valid ResponseEntity
		APINormalResponse<BulkPermissionCheckResult> apiResponse = new APINormalResponse<>(expectedResult,
				AccessControlMessageConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX + "1 jobs");
		ResponseEntity<APINormalResponse<BulkPermissionCheckResult>> mockResponse = ResponseEntity.ok(apiResponse);
		given(this.apiResponder.respond(any(), any(), any(), any())).willReturn((ResponseEntity) mockResponse);

		// Act
		ResponseEntity<?> result = this.accessControlTestController.testJobBulkPermissions(request);

		// Assert
		assertThat(result).isNotNull();
		verify(this.accessControlChecker).allowsBulk(any());
		verify(this.apiResponder).respond(expectedResult,
				AccessControlMessageConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX + "1 jobs",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

}