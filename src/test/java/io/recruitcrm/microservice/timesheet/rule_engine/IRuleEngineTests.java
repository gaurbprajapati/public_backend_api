/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IRuleEngineTests {

	@Mock
	private IRuleEngine ruleEngine;

	@Mock
	private Timesheet timesheet;

	@Mock
	private WeeklyRuleEvaluatorResult expectedResult;

	@BeforeEach
	void setUp() {
		// Create a concrete implementation for testing the interface
		this.ruleEngine = new RuleEngine(
				new io.recruitcrm.microservice.timesheet.rule_engine.rules.RuleEvaluator(List.of()));
	}

	@Test
	@DisplayName("Interface - Has evaluateRules method")
	void testInterfaceHasEvaluateRulesMethod() {
		// This test verifies that the interface has the expected method
		boolean hasEvaluateRulesMethod = java.util.Arrays.stream(IRuleEngine.class.getMethods())
			.anyMatch((method) -> method.getName().equals("evaluateRules") && method.getParameterCount() == 1
					&& method.getParameterTypes()[0] == Timesheet.class);

		assertThat(hasEvaluateRulesMethod)
			.withFailMessage("evaluateRules method with Timesheet parameter not found in IRuleEngine interface")
			.isTrue();
	}

	@Test
	@DisplayName("Interface - Has validateRules method")
	void testInterfaceHasValidateRulesMethod() {
		// This test verifies that the interface has the expected method
		boolean hasValidateRulesMethod = java.util.Arrays.stream(IRuleEngine.class.getMethods())
			.anyMatch((method) -> method.getName().equals("validateRules") && method.getParameterCount() == 0);

		assertThat(hasValidateRulesMethod)
			.withFailMessage("validateRules method with no parameters not found in IRuleEngine interface")
			.isTrue();
	}

	@Test
	@DisplayName("Interface - evaluateRules method signature")
	void testInterfaceEvaluateRulesMethodSignature() {
		// This test verifies the method signature
		try {
			var method = IRuleEngine.class.getMethod("evaluateRules", Timesheet.class);
			assertThat(method).isNotNull()
				.satisfies((m) -> assertThat(m.getName()).isEqualTo("evaluateRules"))
				.satisfies((m) -> assertThat(m.getParameterCount()).isEqualTo(1))
				.satisfies((m) -> assertThat(m.getParameterTypes()[0]).isEqualTo(Timesheet.class));
		}
		catch (Exception ex) {
			throw new AssertionError("evaluateRules method not found in IRuleEngine interface");
		}
	}

	@Test
	@DisplayName("Interface - validateRules method signature")
	void testInterfaceValidateRulesMethodSignature() {
		// This test verifies the method signature
		try {
			var method = IRuleEngine.class.getMethod("validateRules");
			assertThat(method).isNotNull()
				.satisfies((m) -> assertThat(m.getName()).isEqualTo("validateRules"))
				.satisfies((m) -> assertThat(m.getParameterCount()).isZero());
		}
		catch (Exception ex) {
			throw new AssertionError("validateRules method not found in IRuleEngine interface");
		}
	}

	@Test
	@DisplayName("Interface - evaluateRules return type")
	void testInterfaceEvaluateRulesReturnType() {
		// This test verifies the return type
		try {
			var method = IRuleEngine.class.getMethod("evaluateRules", Timesheet.class);
			assertThat(method).isNotNull()
				.satisfies((m) -> assertThat(m.getReturnType()).isEqualTo(WeeklyRuleEvaluatorResult.class))
				.satisfies((m) -> assertThat(m.getParameterCount()).isEqualTo(1))
				.satisfies((m) -> assertThat(m.getParameterTypes()[0]).isEqualTo(Timesheet.class));
		}
		catch (Exception ex) {
			throw new AssertionError("evaluateRules method not found in IRuleEngine interface");
		}
	}

	@Test
	@DisplayName("Interface - validateRules return type")
	void testInterfaceValidateRulesReturnType() {
		// This test verifies the return type
		try {
			var method = IRuleEngine.class.getMethod("validateRules");
			assertThat(method).isNotNull()
				.satisfies((m) -> assertThat(m.getReturnType()).isEqualTo(void.class))
				.satisfies((m) -> assertThat(m.getParameterCount()).isZero())
				.satisfies((m) -> assertThat(m.getName()).isEqualTo("validateRules"));
		}
		catch (Exception ex) {
			throw new AssertionError("validateRules method not found in IRuleEngine interface");
		}
	}

	@Test
	@DisplayName("Interface - Can be implemented")
	void testInterfaceCanBeImplemented() {
		// This test verifies that the interface can be implemented
		IRuleEngine implementation = new IRuleEngine() {
			@Override
			public WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet) {
				return null;
			}

			@Override
			public WeeklyRuleEvaluatorResult evaluateRulesOnDemand(Timesheet timesheet,
					java.util.List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> timeLogs) {
				return null;
			}

			@Override
			public void validateRules() {
				// Implementation
			}
		};

		assertThat(implementation).isNotNull()
			.isInstanceOf(IRuleEngine.class)
			.satisfies((impl) -> assertThat(impl.evaluateRules(this.timesheet)).isNull());
	}

	@Test
	@DisplayName("Interface - Method contracts")
	void testInterfaceMethodContracts() {
		// This test verifies the method contracts through reflection
		var methods = IRuleEngine.class.getMethods();

		// Find the specific methods we're looking for
		var evaluateRulesMethod = findMethodByName(methods, "evaluateRules");
		var validateRulesMethod = findMethodByName(methods, "validateRules");

		assertThat(evaluateRulesMethod).isNotNull()
			.satisfies((method) -> assertThat(method.getParameterCount()).isEqualTo(1))
			.satisfies((method) -> assertThat(method.getParameterTypes()[0]).isEqualTo(Timesheet.class))
			.satisfies((method) -> assertThat(method.getReturnType()).isEqualTo(WeeklyRuleEvaluatorResult.class))
			.satisfies((method) -> assertThat(method.getName()).isEqualTo("evaluateRules"));

		assertThat(validateRulesMethod).isNotNull()
			.satisfies((method) -> assertThat(method.getParameterCount()).isZero())
			.satisfies((method) -> assertThat(method.getReturnType()).isEqualTo(void.class))
			.satisfies((method) -> assertThat(method.getName()).isEqualTo("validateRules"));
	}

	private java.lang.reflect.Method findMethodByName(java.lang.reflect.Method[] methods, String name) {
		for (var method : methods) {
			if (method.getName().equals(name)) {
				return method;
			}
		}
		return null;
	}

}