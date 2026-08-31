/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine;

import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.ICustomRuleTimeRangeResolver;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.IRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IRuleFactoryTests {

	@Mock
	private Logger logger;

	@Test
	@DisplayName("Interface - Has createRule method")
	void testInterfaceHasCreateRuleMethod() {
		// This test verifies that the interface has the expected method
		boolean hasCreateRuleMethod = java.util.Arrays.stream(IRuleFactory.class.getMethods())
			.anyMatch((method) -> method.getName().equals("createRule") && method.getParameterCount() == 2
					&& method.getParameterTypes()[0] == RuleType.class
					&& method.getParameterTypes()[1] == Logger.class);

		assertThat(hasCreateRuleMethod)
			.withFailMessage(
					"createRule method with RuleType and Logger parameters not found in IRuleFactory interface")
			.isTrue();
	}

	@Test
	@DisplayName("Interface - Has createTimeRangeResolver method")
	void testInterfaceHasCreateTimeRangeResolverMethod() {
		// This test verifies that the interface has the expected method
		boolean hasCreateTimeRangeResolverMethod = java.util.Arrays.stream(IRuleFactory.class.getMethods())
			.anyMatch((method) -> method.getName().equals("createTimeRangeResolver") && method.getParameterCount() == 1
					&& method.getParameterTypes()[0] == RuleType.class);

		assertThat(hasCreateTimeRangeResolverMethod)
			.withFailMessage(
					"createTimeRangeResolver method with RuleType parameter not found in IRuleFactory interface")
			.isTrue();
	}

	@Test
	@DisplayName("Interface - createRule method signature")
	void testInterfaceCreateRuleMethodSignature() {
		// This test verifies the method signature
		try {
			var method = IRuleFactory.class.getMethod("createRule", RuleType.class, Logger.class);
			assertThat(method).isNotNull()
				.satisfies((m) -> assertThat(m.getReturnType()).isEqualTo(IRule.class))
				.satisfies((m) -> assertThat(m.getName()).isEqualTo("createRule"))
				.satisfies((m) -> assertThat(m.getParameterCount()).isEqualTo(2))
				.satisfies((m) -> assertThat(m.getParameterTypes()[0]).isEqualTo(RuleType.class))
				.satisfies((m) -> assertThat(m.getParameterTypes()[1]).isEqualTo(Logger.class));
		}
		catch (Exception ex) {
			throw new AssertionError("createRule method not found in IRuleFactory interface");
		}
	}

	@Test
	@DisplayName("Interface - createTimeRangeResolver method signature")
	void testInterfaceCreateTimeRangeResolverMethodSignature() {
		// This test verifies the method signature
		try {
			var method = IRuleFactory.class.getMethod("createTimeRangeResolver", RuleType.class);
			assertThat(method).isNotNull()
				.satisfies((m) -> assertThat(m.getReturnType()).isEqualTo(ICustomRuleTimeRangeResolver.class))
				.satisfies((m) -> assertThat(m.getName()).isEqualTo("createTimeRangeResolver"))
				.satisfies((m) -> assertThat(m.getParameterCount()).isEqualTo(1))
				.satisfies((m) -> assertThat(m.getParameterTypes()[0]).isEqualTo(RuleType.class));
		}
		catch (Exception ex) {
			throw new AssertionError("createTimeRangeResolver method not found in IRuleFactory interface");
		}
	}

	@Test
	@DisplayName("Interface - Can be implemented")
	void testInterfaceCanBeImplemented() {
		// This test verifies that the interface can be implemented
		IRuleFactory implementation = new IRuleFactory() {
			@Override
			public IRule createRule(RuleType ruleType, Logger logger) {
				return null;
			}

			@Override
			public ICustomRuleTimeRangeResolver createTimeRangeResolver(RuleType ruleType) {
				return null;
			}
		};

		assertThat(implementation).isNotNull()
			.isInstanceOf(IRuleFactory.class)
			.satisfies((impl) -> assertThat(impl.createRule(RuleType.RANGE_BASED_DAILY_OVERTIME, this.logger)).isNull())
			.satisfies(
					(impl) -> assertThat(impl.createTimeRangeResolver(RuleType.RANGE_BASED_DAILY_OVERTIME)).isNull());
	}

	@Test
	@DisplayName("Interface - Method contracts")
	void testInterfaceMethodContracts() {
		// This test verifies the method contracts through reflection
		var methods = IRuleFactory.class.getMethods();

		// Find the specific methods we're looking for
		var createRuleMethod = findMethodByName(methods, "createRule");
		var createTimeRangeResolverMethod = findMethodByName(methods, "createTimeRangeResolver");

		assertThat(createRuleMethod).isNotNull()
			.satisfies((method) -> assertThat(method.getParameterCount()).isEqualTo(2))
			.satisfies((method) -> assertThat(method.getParameterTypes()[0]).isEqualTo(RuleType.class))
			.satisfies((method) -> assertThat(method.getParameterTypes()[1]).isEqualTo(Logger.class))
			.satisfies((method) -> assertThat(method.getReturnType()).isEqualTo(IRule.class))
			.satisfies((method) -> assertThat(method.getName()).isEqualTo("createRule"));

		assertThat(createTimeRangeResolverMethod).isNotNull()
			.satisfies((method) -> assertThat(method.getParameterCount()).isEqualTo(1))
			.satisfies((method) -> assertThat(method.getParameterTypes()[0]).isEqualTo(RuleType.class))
			.satisfies((method) -> assertThat(method.getReturnType()).isEqualTo(ICustomRuleTimeRangeResolver.class))
			.satisfies((method) -> assertThat(method.getName()).isEqualTo("createTimeRangeResolver"));
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
	@DisplayName("Interface - createRule method exceptions")
	void testInterfaceCreateRuleMethodExceptions() {
		// This test verifies the method exception declarations
		try {
			var method = IRuleFactory.class.getMethod("createRule", RuleType.class, Logger.class);
			var exceptions = method.getExceptionTypes();

			// Interface methods don't declare exceptions, implementations do
			assertThat(exceptions).isEmpty();
		}
		catch (Exception ex) {
			throw new AssertionError("createRule method not found in IRuleFactory interface");
		}
	}

	@Test
	@DisplayName("Interface - createTimeRangeResolver method exceptions")
	void testInterfaceCreateTimeRangeResolverMethodExceptions() {
		// This test verifies the method exception declarations
		try {
			var method = IRuleFactory.class.getMethod("createTimeRangeResolver", RuleType.class);
			var exceptions = method.getExceptionTypes();

			// Interface methods don't declare exceptions, implementations do
			assertThat(exceptions).isEmpty();
		}
		catch (Exception ex) {
			throw new AssertionError("createTimeRangeResolver method not found in IRuleFactory interface");
		}
	}

}