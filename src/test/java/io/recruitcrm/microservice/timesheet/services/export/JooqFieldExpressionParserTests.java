package io.recruitcrm.microservice.timesheet.services.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.anyString;

import io.recruitcrm.microservice.timesheet.testdata.JooqFieldExpressionParserTestDataFactory;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for JooqFieldExpressionParser
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JooqFieldExpressionParser Tests")
class JooqFieldExpressionParserTests {

	@Mock
	private AdvancedJooqExpressionParser advancedParser;

	@InjectMocks
	private JooqFieldExpressionParser jooqFieldExpressionParser;

	@BeforeEach
	void setUp() {
		// Setup is handled by @InjectMocks
	}

	@Test
	@DisplayName("Parse expression should return field when advanced parser succeeds")
	void testParseExpressionValidExpressionReturnsField() {
		// Given
		String expression = JooqFieldExpressionParserTestDataFactory.VALID_EXPRESSION;
		String alias = JooqFieldExpressionParserTestDataFactory.TEST_ALIAS;

		willAnswer((invocation) -> DSL.field("CANDIDATE.ID").as(alias)).given(this.advancedParser)
			.parseExpression(expression, alias);

		// When
		Field<?> result = this.jooqFieldExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
	}

	@Test
	@DisplayName("Parse expression should return field when advanced parser handles complex expression")
	void testParseExpressionComplexExpressionReturnsField() {
		// Given
		String expression = JooqFieldExpressionParserTestDataFactory.COMPLEX_EXPRESSION;
		String alias = JooqFieldExpressionParserTestDataFactory.CANDIDATE_NAME_ALIAS;

		willAnswer((invocation) -> DSL.concat(DSL.val("John"), DSL.val(" "), DSL.val("Doe")).as(alias))
			.given(this.advancedParser)
			.parseExpression(expression, alias);

		// When
		Field<?> result = this.jooqFieldExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
	}

	@Test
	@DisplayName("Parse expression should return fallback field when advanced parser throws exception")
	void testParseExpressionAdvancedParserExceptionReturnsFallbackField() {
		// Given
		String expression = JooqFieldExpressionParserTestDataFactory.INVALID_EXPRESSION;
		String alias = JooqFieldExpressionParserTestDataFactory.TEST_ALIAS;

		given(this.advancedParser.parseExpression(expression, alias)).willThrow(new RuntimeException("Parsing failed"));

		// When
		Field<?> result = this.jooqFieldExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		// Verify it's a fallback field (empty string with alias)
		assertThat(result.toString()).contains(alias);
	}

	@Test
	@DisplayName("Parse expression should return fallback field when advanced parser throws IllegalArgumentException")
	void testParseExpressionAdvancedParserIllegalArgumentExceptionReturnsFallbackField() {
		// Given
		String expression = JooqFieldExpressionParserTestDataFactory.INVALID_EXPRESSION;
		String alias = JooqFieldExpressionParserTestDataFactory.TEST_ALIAS;

		given(this.advancedParser.parseExpression(expression, alias))
			.willThrow(new IllegalArgumentException("Invalid expression"));

		// When
		Field<?> result = this.jooqFieldExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
	}

	@Test
	@DisplayName("Parse expression should return fallback field when advanced parser throws NullPointerException")
	void testParseExpressionAdvancedParserNullPointerExceptionReturnsFallbackField() {
		// Given
		String expression = JooqFieldExpressionParserTestDataFactory.NULL_EXPRESSION;
		String alias = JooqFieldExpressionParserTestDataFactory.TEST_ALIAS;

		given(this.advancedParser.parseExpression(expression, alias))
			.willThrow(new NullPointerException("Null expression"));

		// When
		Field<?> result = this.jooqFieldExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
	}

	@Test
	@DisplayName("Parse expression should handle empty expression gracefully")
	void testParseExpressionEmptyExpressionHandledGracefully() {
		// Given
		String expression = JooqFieldExpressionParserTestDataFactory.EMPTY_EXPRESSION;
		String alias = JooqFieldExpressionParserTestDataFactory.TEST_ALIAS;

		given(this.advancedParser.parseExpression(expression, alias))
			.willThrow(new IllegalArgumentException("Empty expression"));

		// When
		Field<?> result = this.jooqFieldExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
	}

	@Test
	@DisplayName("Parse expression should work with different alias")
	void testParseExpressionDifferentAliasWorksCorrectly() {
		// Given
		String expression = JooqFieldExpressionParserTestDataFactory.VALID_EXPRESSION;
		String alias = JooqFieldExpressionParserTestDataFactory.TIMESHEET_ID_ALIAS;

		willAnswer((invocation) -> DSL.field("TIMESHEET.ID").as(alias)).given(this.advancedParser)
			.parseExpression(expression, alias);

		// When
		Field<?> result = this.jooqFieldExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
	}

	@Test
	@DisplayName("Parse expression should return fallback field with correct alias when exception occurs")
	void testParseExpressionExceptionReturnsFallbackFieldWithCorrectAlias() {
		// Given
		String expression = JooqFieldExpressionParserTestDataFactory.INVALID_EXPRESSION;
		String customAlias = "custom_alias";

		given(this.advancedParser.parseExpression(expression, customAlias))
			.willThrow(new RuntimeException("Parsing error"));

		// When
		Field<?> result = this.jooqFieldExpressionParser.parseExpression(expression, customAlias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(customAlias);
	}

	@Test
	@DisplayName("Parse expression should return fallback field when any exception occurs")
	void testParseExpressionAnyExceptionReturnsFallbackField() {
		// Given
		String expression = "INVALID_FIELD";
		String alias = "test_field";

		given(this.advancedParser.parseExpression(anyString(), anyString()))
			.willThrow(new RuntimeException("Generic parsing error"));

		// When
		Field<?> result = this.jooqFieldExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
	}

}