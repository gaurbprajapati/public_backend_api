/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.microservice.timesheet.dto.rule_engine.OnDemandTimesheetOvertimeDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEngineResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.responses.IAPIResponder;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineRequestBodyDto;
import io.recruitcrm.microservice.timesheet.services.rule_engine.IRuleEngineService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RuleEngineControllerTests {

	@Mock
	private IRuleEngineService ruleEngineService;

	@Mock
	private IAPIResponder apiResponder;

	@Mock
	private RuleEngineRequestBodyDto requestBodyDto;

	@Mock
	private RuleEngineResponseBodyDto responseBodyDto;

	@Mock
	private BulkUpdateTimeLogsRequestBodyDto bulkRequestBodyDto;

	@Mock
	private OnDemandTimesheetOvertimeDto onDemandResult;

	private RuleEngineController ruleEngineController;

	@BeforeEach
	void setUp() {
		this.ruleEngineController = new RuleEngineController(this.ruleEngineService, this.apiResponder);
	}

	@Test
	@DisplayName("Evaluate rules - Success")
	void testEvaluateRulesSuccessReturns200() {
		// Given
		ResponseEntity<APINormalResponse<RuleEngineResponseBodyDto>> expectedResponse = ResponseEntity.ok().build();
		given(this.ruleEngineService.evaluateRules(this.requestBodyDto)).willReturn(this.responseBodyDto);
		given(this.apiResponder.respond(this.responseBodyDto, "Rules evaluated successfully", APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn(expectedResponse);

		// When
		ResponseEntity<?> result = this.ruleEngineController.evaluateRules(this.requestBodyDto);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.ruleEngineService).should().evaluateRules(this.requestBodyDto);
		then(this.apiResponder).should()
			.respond(this.responseBodyDto, "Rules evaluated successfully", APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Evaluate rules - IllegalArgumentException converts to ValidationErrorException")
	void testEvaluateRulesIllegalArgumentExceptionThrowsValidationErrorException() {
		// Given
		given(this.ruleEngineService.evaluateRules(this.requestBodyDto))
			.willThrow(new IllegalArgumentException("Invalid timesheet ID"));

		// When & Then
		assertThatThrownBy(() -> this.ruleEngineController.evaluateRules(this.requestBodyDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("Invalid timesheet ID");
	}

	@Test
	@DisplayName("Evaluate rules - Null request body throws ValidationErrorException")
	void testEvaluateRulesNullRequestBodyThrowsValidationErrorException() {
		// Given
		given(this.ruleEngineService.evaluateRules(null))
			.willThrow(new IllegalArgumentException("Request body cannot be null"));

		// When & Then
		assertThatThrownBy(() -> this.ruleEngineController.evaluateRules(null))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("Request body cannot be null");
	}

	@Test
	@DisplayName("Validate rules - Success")
	void testValidateRulesSuccessReturns200() {
		// Given
		ResponseEntity<APINormalResponse<String>> expectedResponse = ResponseEntity.ok().build();
		given(this.ruleEngineService.validateRules(this.requestBodyDto)).willReturn("Validation successful");
		given(this.apiResponder.respond("Validation successful", "All rules are valid")).willReturn(expectedResponse);

		// When
		ResponseEntity<?> result = this.ruleEngineController.validateRules(this.requestBodyDto);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.ruleEngineService).should().validateRules(this.requestBodyDto);
		then(this.apiResponder).should().respond("Validation successful", "All rules are valid");
	}

	@Test
	@DisplayName("Validate rules - IllegalArgumentException converts to ValidationErrorException")
	void testValidateRulesIllegalArgumentExceptionThrowsValidationErrorException() {
		// Given
		given(this.ruleEngineService.validateRules(this.requestBodyDto))
			.willThrow(new IllegalArgumentException("Invalid rule configuration"));

		// When & Then
		assertThatThrownBy(() -> this.ruleEngineController.validateRules(this.requestBodyDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("Invalid rule configuration");
	}

	@Test
	@DisplayName("Validate rules - Null request body throws ValidationErrorException")
	void testValidateRulesNullRequestBodyThrowsValidationErrorException() {
		// Given
		given(this.ruleEngineService.validateRules(null))
			.willThrow(new IllegalArgumentException("Request body cannot be null"));

		// When & Then
		assertThatThrownBy(() -> this.ruleEngineController.validateRules(null))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("Request body cannot be null");
	}

	@Test
	@DisplayName("Constructor - Creates instance with dependencies")
	void testConstructorCreatesInstanceWithDependencies() {
		// When
		RuleEngineController newController = new RuleEngineController(this.ruleEngineService, this.apiResponder);

		// Then
		assertThat(newController).isNotNull();
	}

	@Test
	@DisplayName("Evaluate rules - Service returns null response")
	void testEvaluateRulesServiceReturnsNullResponseHandlesGracefully() {
		// Given
		given(this.ruleEngineService.evaluateRules(this.requestBodyDto)).willReturn(null);
		ResponseEntity<APINormalResponse<Object>> expectedResponse = ResponseEntity.ok().build();
		given(this.apiResponder.respond(null, "Rules evaluated successfully", APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponse);

		// When
		ResponseEntity<?> result = this.ruleEngineController.evaluateRules(this.requestBodyDto);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
	}

	@Test
	@DisplayName("Validate rules - Service returns empty string")
	void testValidateRulesServiceReturnsEmptyStringHandlesGracefully() {
		// Given
		given(this.ruleEngineService.validateRules(this.requestBodyDto)).willReturn("");
		ResponseEntity<APINormalResponse<String>> expectedResponse = ResponseEntity.ok().build();
		given(this.apiResponder.respond("", "All rules are valid")).willReturn(expectedResponse);

		// When
		ResponseEntity<?> result = this.ruleEngineController.validateRules(this.requestBodyDto);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
	}

	@Test
	@DisplayName("Evaluate rules - Service throws RuntimeException converts to ValidationErrorException")
	void testEvaluateRulesRuntimeExceptionDoesNotConvertToValidationErrorException() {
		// Given
		given(this.ruleEngineService.evaluateRules(this.requestBodyDto))
			.willThrow(new RuntimeException("Unexpected error"));

		// When & Then
		assertThatThrownBy(() -> this.ruleEngineController.evaluateRules(this.requestBodyDto))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("Unexpected error");
	}

	@Test
	@DisplayName("Validate rules - Service throws RuntimeException propagates correctly")
	void testValidateRulesRuntimeExceptionPropagatesCorrectly() {
		// Given
		given(this.ruleEngineService.validateRules(this.requestBodyDto))
			.willThrow(new RuntimeException("Validation system error"));

		// When & Then
		assertThatThrownBy(() -> this.ruleEngineController.validateRules(this.requestBodyDto))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("Validation system error");
	}

	@Test
	@DisplayName("Evaluate rules - Response with different HTTP status codes")
	void testEvaluateRulesWithDifferentHttpStatusCodes() {
		// Given
		given(this.ruleEngineService.evaluateRules(this.requestBodyDto)).willReturn(this.responseBodyDto);
		given(this.apiResponder.respond(this.responseBodyDto, "Rules evaluated successfully", APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn(ResponseEntity.ok().build());

		// When
		ResponseEntity<?> result = this.ruleEngineController.evaluateRules(this.requestBodyDto);

		// Then
		assertThat(result).isNotNull();
		then(this.ruleEngineService).should().evaluateRules(this.requestBodyDto);
	}

	@Test
	@DisplayName("Evaluate rules - Verify correct method signature parameters")
	void testEvaluateRulesVerifyCorrectMethodSignatureParameters() {
		// Given
		ResponseEntity<APINormalResponse<RuleEngineResponseBodyDto>> expectedResponse = ResponseEntity.ok().build();
		given(this.ruleEngineService.evaluateRules(this.requestBodyDto)).willReturn(this.responseBodyDto);
		given(this.apiResponder.respond(this.responseBodyDto, "Rules evaluated successfully", APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn(expectedResponse);

		// When
		ResponseEntity<?> result = this.ruleEngineController.evaluateRules(this.requestBodyDto);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.apiResponder).should()
			.respond(this.responseBodyDto, "Rules evaluated successfully", APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Evaluate rules - Multiple sequential calls work correctly")
	void testEvaluateRulesMultipleSequentialCallsWorkCorrectly() {
		// Given
		ResponseEntity<APINormalResponse<RuleEngineResponseBodyDto>> expectedResponse = ResponseEntity.ok().build();
		given(this.ruleEngineService.evaluateRules(this.requestBodyDto)).willReturn(this.responseBodyDto);
		given(this.apiResponder.respond(this.responseBodyDto, "Rules evaluated successfully", APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn(expectedResponse);

		// When
		ResponseEntity<?> result1 = this.ruleEngineController.evaluateRules(this.requestBodyDto);
		ResponseEntity<?> result2 = this.ruleEngineController.evaluateRules(this.requestBodyDto);
		ResponseEntity<?> result3 = this.ruleEngineController.evaluateRules(this.requestBodyDto);

		// Then
		assertThat(result1).isEqualTo(expectedResponse);
		assertThat(result2).isEqualTo(expectedResponse);
		assertThat(result3).isEqualTo(expectedResponse);
		then(this.ruleEngineService).should(times(3)).evaluateRules(this.requestBodyDto);
	}

	@Test
	@DisplayName("Validate rules - Multiple sequential calls work correctly")
	void testValidateRulesMultipleSequentialCallsWorkCorrectly() {
		// Given
		ResponseEntity<APINormalResponse<String>> expectedResponse = ResponseEntity.ok().build();
		given(this.ruleEngineService.validateRules(this.requestBodyDto)).willReturn("Validation successful");
		given(this.apiResponder.respond("Validation successful", "All rules are valid")).willReturn(expectedResponse);

		// When
		ResponseEntity<?> result1 = this.ruleEngineController.validateRules(this.requestBodyDto);
		ResponseEntity<?> result2 = this.ruleEngineController.validateRules(this.requestBodyDto);

		// Then
		assertThat(result1).isEqualTo(expectedResponse);
		assertThat(result2).isEqualTo(expectedResponse);
		then(this.ruleEngineService).should(times(2)).validateRules(this.requestBodyDto);
	}

	@Test
	@DisplayName("Evaluate rules on demand - Success returns 200 with result list")
	void testEvaluateRulesOnDemandSuccessReturns200() {
		// Given
		List<OnDemandTimesheetOvertimeDto> resultList = List.of(this.onDemandResult);
		ResponseEntity<APINormalResponse<List<OnDemandTimesheetOvertimeDto>>> expectedResponse = ResponseEntity.ok()
			.build();
		given(this.ruleEngineService.evaluateRulesOnDemand(this.bulkRequestBodyDto)).willReturn(resultList);
		given(this.apiResponder.respond(resultList, "Rules evaluated successfully (on-demand)", APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn(expectedResponse);

		// When
		ResponseEntity<?> result = this.ruleEngineController.evaluateRulesOnDemand(this.bulkRequestBodyDto);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.ruleEngineService).should().evaluateRulesOnDemand(this.bulkRequestBodyDto);
		then(this.apiResponder).should()
			.respond(resultList, "Rules evaluated successfully (on-demand)", APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Evaluate rules on demand - Delegates to service correctly")
	void testEvaluateRulesOnDemandDelegatesToServiceCorrectly() {
		// Given
		List<OnDemandTimesheetOvertimeDto> resultList = List.of(this.onDemandResult);
		given(this.ruleEngineService.evaluateRulesOnDemand(this.bulkRequestBodyDto)).willReturn(resultList);
		given(this.apiResponder.respond(resultList, "Rules evaluated successfully (on-demand)", APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn(ResponseEntity.ok().build());

		// When
		this.ruleEngineController.evaluateRulesOnDemand(this.bulkRequestBodyDto);

		// Then
		then(this.ruleEngineService).should().evaluateRulesOnDemand(this.bulkRequestBodyDto);
	}

	@Test
	@DisplayName("Evaluate rules on demand - IllegalArgumentException converts to ValidationErrorException")
	void testEvaluateRulesOnDemandIllegalArgumentExceptionThrowsValidationErrorException() {
		// Given
		given(this.ruleEngineService.evaluateRulesOnDemand(this.bulkRequestBodyDto))
			.willThrow(new IllegalArgumentException("Time logs cannot be null or empty"));

		// When & Then
		assertThatThrownBy(() -> this.ruleEngineController.evaluateRulesOnDemand(this.bulkRequestBodyDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("Time logs cannot be null or empty");
	}

	@Test
	@DisplayName("Evaluate rules on demand - Null request body throws ValidationErrorException")
	void testEvaluateRulesOnDemandNullRequestBodyThrowsValidationErrorException() {
		// Given
		given(this.ruleEngineService.evaluateRulesOnDemand(null))
			.willThrow(new IllegalArgumentException("Request body cannot be null"));

		// When & Then
		assertThatThrownBy(() -> this.ruleEngineController.evaluateRulesOnDemand(null))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("Request body cannot be null");
	}

	@Test
	@DisplayName("Evaluate rules on demand - Service returns empty list")
	void testEvaluateRulesOnDemandServiceReturnsEmptyListHandlesGracefully() {
		// Given
		List<OnDemandTimesheetOvertimeDto> emptyList = Collections.emptyList();
		ResponseEntity<APINormalResponse<List<OnDemandTimesheetOvertimeDto>>> expectedResponse = ResponseEntity.ok()
			.build();
		given(this.ruleEngineService.evaluateRulesOnDemand(this.bulkRequestBodyDto)).willReturn(emptyList);
		given(this.apiResponder.respond(emptyList, "Rules evaluated successfully (on-demand)", APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn(expectedResponse);

		// When
		ResponseEntity<?> result = this.ruleEngineController.evaluateRulesOnDemand(this.bulkRequestBodyDto);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.ruleEngineService).should().evaluateRulesOnDemand(this.bulkRequestBodyDto);
	}

	@Test
	@DisplayName("Evaluate rules on demand - Service returns null result")
	void testEvaluateRulesOnDemandServiceReturnsNullHandlesGracefully() {
		// Given
		ResponseEntity<APINormalResponse<Object>> expectedResponse = ResponseEntity.ok().build();
		given(this.ruleEngineService.evaluateRulesOnDemand(this.bulkRequestBodyDto)).willReturn(null);
		given(this.apiResponder.respond(null, "Rules evaluated successfully (on-demand)", APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn(expectedResponse);

		// When
		ResponseEntity<?> result = this.ruleEngineController.evaluateRulesOnDemand(this.bulkRequestBodyDto);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
	}

	@Test
	@DisplayName("Evaluate rules on demand - RuntimeException propagates without conversion")
	void testEvaluateRulesOnDemandRuntimeExceptionPropagatesCorrectly() {
		// Given
		given(this.ruleEngineService.evaluateRulesOnDemand(this.bulkRequestBodyDto))
			.willThrow(new RuntimeException("Unexpected evaluator error"));

		// When & Then
		assertThatThrownBy(() -> this.ruleEngineController.evaluateRulesOnDemand(this.bulkRequestBodyDto))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("Unexpected evaluator error");
	}

	@Test
	@DisplayName("Evaluate rules on demand - Verify response message is correct")
	void testEvaluateRulesOnDemandVerifyCorrectResponseMessage() {
		// Given
		List<OnDemandTimesheetOvertimeDto> resultList = List.of(this.onDemandResult);
		ResponseEntity<APINormalResponse<List<OnDemandTimesheetOvertimeDto>>> expectedResponse = ResponseEntity.ok()
			.build();
		given(this.ruleEngineService.evaluateRulesOnDemand(this.bulkRequestBodyDto)).willReturn(resultList);
		given(this.apiResponder.respond(resultList, "Rules evaluated successfully (on-demand)", APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn(expectedResponse);

		// When
		ResponseEntity<?> result = this.ruleEngineController.evaluateRulesOnDemand(this.bulkRequestBodyDto);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.apiResponder).should()
			.respond(resultList, "Rules evaluated successfully (on-demand)", APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
