/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineRequestBodyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IRuleEngineControllerTests {

	@Test
	@DisplayName("Interface - Has evaluateRules method")
	void testInterfaceHasEvaluateRulesMethod() {
		// This test verifies that the interface has the expected method
		assertThat(IRuleEngineController.class.getMethods())
			.anyMatch((method) -> method.getName().equals("evaluateRules") && method.getParameterCount() == 1
					&& method.getParameterTypes()[0] == RuleEngineRequestBodyDto.class);
	}

	@Test
	@DisplayName("Interface - Has validateRules method")
	void testInterfaceHasValidateRulesMethod() {
		// This test verifies that the interface has the expected method
		assertThat(IRuleEngineController.class.getMethods())
			.anyMatch((method) -> method.getName().equals("validateRules") && method.getParameterCount() == 1
					&& method.getParameterTypes()[0] == RuleEngineRequestBodyDto.class);
	}

	@Test
	@DisplayName("Interface - evaluateRules method signature")
	void testInterfaceEvaluateRulesMethodSignature() {
		// This test verifies the method signature
		try {
			var method = IRuleEngineController.class.getMethod("evaluateRules", RuleEngineRequestBodyDto.class);
			assertThat(method.getReturnType()).isEqualTo(ResponseEntity.class);
		}
		catch (Exception ex) {
			throw new AssertionError("evaluateRules method not found in IRuleEngineController interface");
		}
	}

	@Test
	@DisplayName("Interface - validateRules method signature")
	void testInterfaceValidateRulesMethodSignature() {
		// This test verifies the method signature
		try {
			var method = IRuleEngineController.class.getMethod("validateRules", RuleEngineRequestBodyDto.class);
			assertThat(method.getReturnType()).isEqualTo(ResponseEntity.class);
		}
		catch (Exception ex) {
			throw new AssertionError("validateRules method not found in IRuleEngineController interface");
		}
	}

	@Test
	@DisplayName("Interface - Can be implemented")
	void testInterfaceCanBeImplemented() {
		// This test verifies that the interface can be implemented
		IRuleEngineController implementation = new IRuleEngineController() {
			@Override
			public ResponseEntity<?> evaluateRules(RuleEngineRequestBodyDto requestBodyDto) {
				return null;
			}

			@Override
			public ResponseEntity<?> validateRules(RuleEngineRequestBodyDto requestBodyDto) {
				return null;
			}

			@Override
			public ResponseEntity<?> evaluateRulesOnDemand(
					io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto requestBodyDto) {
				return null;
			}
		};

		assertThat(implementation).isNotNull().isInstanceOf(IRuleEngineController.class);
	}

	@Test
	@DisplayName("Interface - Method contracts")
	void testInterfaceMethodContracts() {
		// This test verifies the method contracts through reflection
		var methods = IRuleEngineController.class.getMethods();

		// Find the specific methods we're looking for
		var evaluateRulesMethod = findMethodByName(methods, "evaluateRules");
		var validateRulesMethod = findMethodByName(methods, "validateRules");

		assertThat(evaluateRulesMethod).isNotNull().satisfies((method) -> {
			assertThat(method.getParameterCount()).isEqualTo(1);
			assertThat(method.getParameterTypes()[0]).isEqualTo(RuleEngineRequestBodyDto.class);
			assertThat(method.getReturnType()).isEqualTo(ResponseEntity.class);
		});

		assertThat(validateRulesMethod).isNotNull().satisfies((method) -> {
			assertThat(method.getParameterCount()).isEqualTo(1);
			assertThat(method.getParameterTypes()[0]).isEqualTo(RuleEngineRequestBodyDto.class);
			assertThat(method.getReturnType()).isEqualTo(ResponseEntity.class);
		});
	}

	private java.lang.reflect.Method findMethodByName(java.lang.reflect.Method[] methods, String name) {
		for (var method : methods) {
			if (method.getName().equals(name)) {
				return method;
			}
		}
		return null;
	}

	@Test
	@DisplayName("Interface - Has PostMapping annotations")
	void testInterfaceHasPostMappingAnnotations() {
		// This test verifies that the interface has the expected annotations
		var methods = IRuleEngineController.class.getMethods();

		for (var method : methods) {
			assertThat(method.getAnnotation(org.springframework.web.bind.annotation.PostMapping.class))
				.as("@PostMapping on method " + method.getName())
				.isNotNull();
		}
	}

	@Test
	@DisplayName("Interface - Has RequestBody annotations")
	void testInterfaceHasRequestBodyAnnotations() {
		// This test verifies that the interface has the expected annotations
		var methods = IRuleEngineController.class.getMethods();

		for (var method : methods) {
			var parameters = method.getParameters();
			assertThat(parameters).hasSize(1).satisfies((params) -> {
				var requestBody = params[0].getAnnotation(org.springframework.web.bind.annotation.RequestBody.class);
				assertThat(requestBody).isNotNull();
			});
		}
	}

}