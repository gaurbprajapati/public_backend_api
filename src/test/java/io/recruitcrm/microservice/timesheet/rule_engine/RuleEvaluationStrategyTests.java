/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.WeeklyRuleEvaluatorResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RuleEvaluationStrategyTests {

	@Test
	@DisplayName("Interface - Has evaluateRules method")
	void testInterfaceHasEvaluateRulesMethod() {
		// This test verifies that the interface has the expected method
		assertThat(RuleEvaluationStrategy.class.getMethods())
			.anyMatch((method) -> method.getName().equals("evaluateRules") && method.getParameterCount() == 1
					&& method.getParameterTypes()[0] == Timesheet.class);
	}

	@Test
	@DisplayName("Interface - Has canHandle method")
	void testInterfaceHasCanHandleMethod() {
		// This test verifies that the interface has the expected method
		assertThat(RuleEvaluationStrategy.class.getMethods()).anyMatch((method) -> method.getName().equals("canHandle")
				&& method.getParameterCount() == 1 && method.getParameterTypes()[0] == Integer.class);
	}

	@Test
	@DisplayName("Interface - evaluateRules method signature")
	void testInterfaceEvaluateRulesMethodSignature() {
		// This test verifies the method signature
		try {
			var method = RuleEvaluationStrategy.class.getMethod("evaluateRules", Timesheet.class);
			assertThat(method.getReturnType()).isEqualTo(WeeklyRuleEvaluatorResult.class);
		}
		catch (Exception ex) {
			throw new AssertionError("evaluateRules method not found in RuleEvaluationStrategy interface");
		}
	}

	@Test
	@DisplayName("Interface - canHandle method signature")
	void testInterfaceCanHandleMethodSignature() {
		// This test verifies the method signature
		try {
			var method = RuleEvaluationStrategy.class.getMethod("canHandle", Integer.class);
			assertThat(method.getReturnType()).isEqualTo(boolean.class);
		}
		catch (Exception ex) {
			throw new AssertionError("canHandle method not found in RuleEvaluationStrategy interface");
		}
	}

	@Test
	@DisplayName("Interface - Can be implemented")
	void testInterfaceCanBeImplemented() {
		// This test verifies that the interface can be implemented
		RuleEvaluationStrategy implementation = new RuleEvaluationStrategy() {
			@Override
			public WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet) {
				return null;
			}

			@Override
			public boolean canHandle(Integer workLogType) {
				return false;
			}

			@Override
			public WeeklyRuleEvaluatorResult evaluateRulesWithTimeLogs(Timesheet timesheet,
					java.util.List<io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog> timeLogs) {
				return null;
			}
		};

		assertThat(implementation).isNotNull().isInstanceOf(RuleEvaluationStrategy.class);
	}

	@Test
	@DisplayName("Interface - Method contracts")
	void testInterfaceMethodContracts() {
		// This test verifies the method contracts through reflection
		var methods = RuleEvaluationStrategy.class.getMethods();

		// Find the specific methods we're looking for
		var evaluateRulesMethod = findMethodByName(methods, "evaluateRules");
		var canHandleMethod = findMethodByName(methods, "canHandle");

		assertThat(evaluateRulesMethod).isNotNull();
		assertThat(canHandleMethod).isNotNull();

		assertThat(evaluateRulesMethod.getParameterCount()).isEqualTo(1);
		assertThat(evaluateRulesMethod.getParameterTypes()[0]).isEqualTo(Timesheet.class);
		assertThat(evaluateRulesMethod.getReturnType()).isEqualTo(WeeklyRuleEvaluatorResult.class);

		assertThat(canHandleMethod.getParameterCount()).isEqualTo(1);
		assertThat(canHandleMethod.getParameterTypes()[0]).isEqualTo(Integer.class);
		assertThat(canHandleMethod.getReturnType()).isEqualTo(boolean.class);
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
	@DisplayName("Interface - evaluateRules method exceptions")
	void testInterfaceEvaluateRulesMethodExceptions() {
		// This test verifies the method exception declarations
		try {
			var method = RuleEvaluationStrategy.class.getMethod("evaluateRules", Timesheet.class);
			var exceptions = method.getExceptionTypes();

			// Interface methods don't declare exceptions, implementations do
			assertThat(exceptions).isEmpty();
		}
		catch (Exception ex) {
			throw new AssertionError("evaluateRules method not found in RuleEvaluationStrategy interface");
		}
	}

	@Test
	@DisplayName("Interface - canHandle method exceptions")
	void testInterfaceCanHandleMethodExceptions() {
		// This test verifies the method exception declarations
		try {
			var method = RuleEvaluationStrategy.class.getMethod("canHandle", Integer.class);
			var exceptions = method.getExceptionTypes();

			// canHandle should not declare any exceptions
			assertThat(exceptions).isEmpty();
		}
		catch (Exception ex) {
			throw new AssertionError("canHandle method not found in RuleEvaluationStrategy interface");
		}
	}

	@Test
	@DisplayName("Interface - evaluateRules method exists")
	void testInterfaceEvaluateRulesMethodExists() {
		// This test verifies that the method exists
		try {
			var method = RuleEvaluationStrategy.class.getMethod("evaluateRules", Timesheet.class);
			assertThat(method).isNotNull();
		}
		catch (Exception ex) {
			throw new AssertionError("evaluateRules method not found in RuleEvaluationStrategy interface");
		}
	}

	@Test
	@DisplayName("Interface - canHandle method exists")
	void testInterfaceCanHandleMethodExists() {
		// This test verifies that the method exists
		try {
			var method = RuleEvaluationStrategy.class.getMethod("canHandle", Integer.class);
			assertThat(method).isNotNull();
		}
		catch (Exception ex) {
			throw new AssertionError("canHandle method not found in RuleEvaluationStrategy interface");
		}
	}

}