package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("IRule Tests")
class IRuleTests {

	@Test
	@DisplayName("IRule implementation - basic functionality")
	void testIRuleImplementationBasicFunctionality() {
		// Arrange
		IRule rule = createMockRule();

		// Act
		String name = rule.getName();
		rule.validate();
		RuleEvaluationResult result = rule.evaluate(new RuleEvaluationContext());

		// Assert
		assertThat(name).isEqualTo("Test Rule");
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("IRule implementation - with null context")
	void testIRuleImplementationWithNullContext() {
		// Arrange
		IRule rule = createMockRule();

		// Act & Assert
		assertThatThrownBy(() -> rule.evaluate(null)).isInstanceOf(IllegalArgumentException.class);
	}

	private IRule createMockRule() {
		return new IRule() {
			@Override
			public RuleEvaluationResult evaluate(RuleEvaluationContext ruleEvaluationContext) {
				if (ruleEvaluationContext == null) {
					throw new IllegalArgumentException("Context cannot be null");
				}
				return new RuleEvaluationResult();
			}

			@Override
			public void validate() {
				// Mock validation - no exceptions
			}

			@Override
			public String getName() {
				return "Test Rule";
			}
		};
	}

}