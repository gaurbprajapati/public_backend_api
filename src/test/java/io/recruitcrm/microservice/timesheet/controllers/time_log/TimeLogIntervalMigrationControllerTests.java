package io.recruitcrm.microservice.timesheet.controllers.time_log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.responses.APIErrorResponse;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.time_log.ITimeLogIntervalMigrationService;
import io.recruitcrm.microservice.timesheet.testdata.TimeLogIntervalMigrationTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimeLogIntervalMigrationController Tests")
class TimeLogIntervalMigrationControllerTests {

	@Mock
	private ITimeLogIntervalMigrationService timeLogIntervalMigrationService;

	@Mock
	private APIResponder apiResponder;

	@Mock
	private AuthHolder auth;

	private TimeLogIntervalMigrationController createController(String env) {
		return new TimeLogIntervalMigrationController(this.timeLogIntervalMigrationService, this.apiResponder,
				this.auth, env);
	}

	@Test
	@DisplayName("migrateTimeLogsToIntervalTable should succeed when env is not local or production")
	void testMigrateTimeLogsToIntervalTableNonRestrictedEnvReturnsSuccess() {
		// Given
		TimeLogIntervalMigrationController controller = this
			.createController(TimeLogIntervalMigrationTestDataFactory.ENV_DEV);
		TimeLogIntervalMigrationRequestBodyDto request = TimeLogIntervalMigrationTestDataFactory
			.createMigrationRequest();
		TimeLogIntervalMigrationResponseBodyDto result = TimeLogIntervalMigrationTestDataFactory
			.createMigrationResponse();
		ResponseEntity<APINormalResponse<TimeLogIntervalMigrationResponseBodyDto>> expectedResponseEntity = new ResponseEntity<>(
				new APINormalResponse<>(result), HttpStatus.OK);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.timeLogIntervalMigrationService.migrateTimeLogsToIntervalTable(request)).willReturn(result);
		given(this.apiResponder.respond(result, TimeLogIntervalMigrationTestDataFactory.MESSAGE_MIGRATION_SUCCESS,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = controller.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.timeLogIntervalMigrationService).should().migrateTimeLogsToIntervalTable(request);
		then(this.apiResponder).should()
			.respond(result, TimeLogIntervalMigrationTestDataFactory.MESSAGE_MIGRATION_SUCCESS, APIResponseType.SUCCESS,
					HttpStatus.OK);
	}

	@Test
	@DisplayName("migrateTimeLogsToIntervalTable should succeed in local env for authorized account and user")
	void testMigrateTimeLogsToIntervalTableLocalEnvAuthorizedReturnsSuccess() {
		// Given
		TimeLogIntervalMigrationController controller = this
			.createController(TimeLogIntervalMigrationTestDataFactory.ENV_LOCAL);
		TimeLogIntervalMigrationRequestBodyDto request = TimeLogIntervalMigrationTestDataFactory
			.createMigrationRequest();
		TimeLogIntervalMigrationResponseBodyDto result = TimeLogIntervalMigrationTestDataFactory
			.createMigrationResponse();
		ResponseEntity<APINormalResponse<TimeLogIntervalMigrationResponseBodyDto>> expectedResponseEntity = new ResponseEntity<>(
				new APINormalResponse<>(result), HttpStatus.OK);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(TimeLogIntervalMigrationTestDataFactory.AUTHORIZED_ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(TimeLogIntervalMigrationTestDataFactory.AUTHORIZED_USER_ID);
		given(this.timeLogIntervalMigrationService.migrateTimeLogsToIntervalTable(request)).willReturn(result);
		given(this.apiResponder.respond(result, TimeLogIntervalMigrationTestDataFactory.MESSAGE_MIGRATION_SUCCESS,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = controller.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.timeLogIntervalMigrationService).should().migrateTimeLogsToIntervalTable(request);
		then(this.apiResponder).should()
			.respond(result, TimeLogIntervalMigrationTestDataFactory.MESSAGE_MIGRATION_SUCCESS, APIResponseType.SUCCESS,
					HttpStatus.OK);
	}

	@Test
	@DisplayName("migrateTimeLogsToIntervalTable should succeed in production env for authorized account and user")
	void testMigrateTimeLogsToIntervalTableProductionEnvAuthorizedReturnsSuccess() {
		// Given
		TimeLogIntervalMigrationController controller = this
			.createController(TimeLogIntervalMigrationTestDataFactory.ENV_PRODUCTION);
		TimeLogIntervalMigrationRequestBodyDto request = TimeLogIntervalMigrationTestDataFactory
			.createMigrationRequest();
		TimeLogIntervalMigrationResponseBodyDto result = TimeLogIntervalMigrationTestDataFactory
			.createMigrationResponse();
		ResponseEntity<APINormalResponse<TimeLogIntervalMigrationResponseBodyDto>> expectedResponseEntity = new ResponseEntity<>(
				new APINormalResponse<>(result), HttpStatus.OK);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(TimeLogIntervalMigrationTestDataFactory.AUTHORIZED_ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(TimeLogIntervalMigrationTestDataFactory.AUTHORIZED_USER_ID);
		given(this.timeLogIntervalMigrationService.migrateTimeLogsToIntervalTable(request)).willReturn(result);
		given(this.apiResponder.respond(result, TimeLogIntervalMigrationTestDataFactory.MESSAGE_MIGRATION_SUCCESS,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = controller.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.timeLogIntervalMigrationService).should().migrateTimeLogsToIntervalTable(request);
		then(this.apiResponder).should()
			.respond(result, TimeLogIntervalMigrationTestDataFactory.MESSAGE_MIGRATION_SUCCESS, APIResponseType.SUCCESS,
					HttpStatus.OK);
	}

	@Test
	@DisplayName("migrateTimeLogsToIntervalTable should return error in local env when caller is not authorized")
	void testMigrateTimeLogsToIntervalTableLocalEnvUnauthorizedReturnsError() {
		// Given
		TimeLogIntervalMigrationController controller = this
			.createController(TimeLogIntervalMigrationTestDataFactory.ENV_LOCAL);
		TimeLogIntervalMigrationRequestBodyDto request = TimeLogIntervalMigrationTestDataFactory
			.createMigrationRequest();
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(TimeLogIntervalMigrationTestDataFactory.AUTHORIZED_USER_ID);
		ResponseEntity<APIErrorResponse> expectedErrorEntity = new ResponseEntity<>(
				new APIErrorResponse(new UnauthorizedAccessException(
						TimeLogIntervalMigrationTestDataFactory.MESSAGE_UNAUTHORIZED_MIGRATION)),
				HttpStatus.INTERNAL_SERVER_ERROR);
		given(this.apiResponder.respondWithError(isA(UnauthorizedAccessException.class), eq(APIResponseType.ERROR),
				eq(HttpStatus.INTERNAL_SERVER_ERROR)))
			.willReturn(expectedErrorEntity);

		// When
		ResponseEntity<?> response = controller.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(response).isEqualTo(expectedErrorEntity);
		then(this.timeLogIntervalMigrationService).shouldHaveNoInteractions();
		then(this.apiResponder).should()
			.respondWithError(isA(UnauthorizedAccessException.class), eq(APIResponseType.ERROR),
					eq(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Test
	@DisplayName("migrateTimeLogsToIntervalTable should return error in local env when user id is not authorized")
	void testMigrateTimeLogsToIntervalTableLocalEnvWrongUserReturnsError() {
		// Given
		TimeLogIntervalMigrationController controller = this
			.createController(TimeLogIntervalMigrationTestDataFactory.ENV_LOCAL);
		TimeLogIntervalMigrationRequestBodyDto request = TimeLogIntervalMigrationTestDataFactory
			.createMigrationRequest();
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(TimeLogIntervalMigrationTestDataFactory.AUTHORIZED_ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(TimeLogIntervalMigrationTestDataFactory.WRONG_USER_ID);
		ResponseEntity<APIErrorResponse> expectedErrorEntity = new ResponseEntity<>(
				new APIErrorResponse(new UnauthorizedAccessException(
						TimeLogIntervalMigrationTestDataFactory.MESSAGE_UNAUTHORIZED_MIGRATION)),
				HttpStatus.INTERNAL_SERVER_ERROR);
		given(this.apiResponder.respondWithError(isA(UnauthorizedAccessException.class), eq(APIResponseType.ERROR),
				eq(HttpStatus.INTERNAL_SERVER_ERROR)))
			.willReturn(expectedErrorEntity);

		// When
		ResponseEntity<?> response = controller.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(response).isEqualTo(expectedErrorEntity);
		then(this.timeLogIntervalMigrationService).shouldHaveNoInteractions();
		then(this.apiResponder).should()
			.respondWithError(isA(UnauthorizedAccessException.class), eq(APIResponseType.ERROR),
					eq(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Test
	@DisplayName("migrateTimeLogsToIntervalTable should propagate service failure through APIResponder error response")
	void testMigrateTimeLogsToIntervalTableServiceThrowsReturnsError() {
		// Given
		TimeLogIntervalMigrationController controller = this
			.createController(TimeLogIntervalMigrationTestDataFactory.ENV_DEV);
		TimeLogIntervalMigrationRequestBodyDto request = TimeLogIntervalMigrationTestDataFactory
			.createMigrationRequest();
		RuntimeException ex = new RuntimeException("Migration failed");
		ResponseEntity<APIErrorResponse> expectedErrorEntity = new ResponseEntity<>(new APIErrorResponse(ex),
				HttpStatus.INTERNAL_SERVER_ERROR);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.timeLogIntervalMigrationService.migrateTimeLogsToIntervalTable(request)).willThrow(ex);
		given(this.apiResponder.respondWithError(ex, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR))
			.willReturn(expectedErrorEntity);

		// When
		ResponseEntity<?> response = controller.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(response).isEqualTo(expectedErrorEntity);
		then(this.timeLogIntervalMigrationService).should().migrateTimeLogsToIntervalTable(request);
		then(this.apiResponder).should().respondWithError(ex, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
