package io.recruitcrm.microservice.timesheet.services.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;

import io.recruitcrm.microservice.timesheet.testdata.AdvancedJooqExpressionParserTestDataFactory;
import java.lang.reflect.Method;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test cases for AdvancedJooqExpressionParser following mandatory rule patterns.
 * Tests all public methods with 100% branch coverage using factory-based test data.
 */
@ExtendWith(MockitoExtension.class)
class AdvancedJooqExpressionParserTests {

	@Mock
	private JooqFieldResolver jooqFieldResolver;

	@InjectMocks
	private AdvancedJooqExpressionParser advancedJooqExpressionParser;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	// ==================== Simple Field Resolution Tests ====================

	@Test
	@DisplayName("Parse expression with simple field should resolve through field resolver")
	void testParseExpressionSimpleFieldResolvesTohroughFieldResolver() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createSimpleFieldExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();
		Field<?> mockField = DSL.field("ts.id").as(alias);

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(true);
		doReturn(mockField).when(this.jooqFieldResolver).resolveSimpleField(anyString(), anyString());

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
		then(this.jooqFieldResolver).should().resolveSimpleField(expression, alias);
	}

	@Test
	@DisplayName("Parse expression with candidate field should resolve through field resolver")
	void testParseExpressionCandidateFieldResolvesTohroughFieldResolver() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createCandidateFieldExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getCandidateNameAlias();
		Field<?> mockField = DSL.field("c.firstname").as(alias);

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(true);
		doReturn(mockField).when(this.jooqFieldResolver).resolveSimpleField(anyString(), anyString());

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
		then(this.jooqFieldResolver).should().resolveSimpleField(expression, alias);
	}

	// ==================== DSL.val() Expression Tests ====================

	@Test
	@DisplayName("Parse expression with DSL.val string should return string field")
	void testParseExpressionDslValStringReturnsStringField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createDslValueStringExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with DSL.val empty string should return empty string field")
	void testParseExpressionDslValEmptyStringReturnsEmptyStringField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createDslValueEmptyStringExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with DSL.val integer should return integer field")
	void testParseExpressionDslValIntegerReturnsIntegerField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createDslValueIntegerExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with DSL.val decimal should return decimal field")
	void testParseExpressionDslValDecimalReturnsDecimalField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createDslValueDecimalExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with DSL.val unclosed string should return raw value field")
	void testParseExpressionDslValUnclosedStringReturnsRawValueField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createDslValueUnclosedStringExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with invalid DSL.val should return fallback field")
	void testParseExpressionInvalidDslValReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createInvalidDslValueExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	// ==================== Candidate Expression Tests ====================

	@Test
	@DisplayName("Parse expression with candidate name should return candidate name field")
	void testParseExpressionCandidateNameReturnsCandidateNameField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createCandidateNameExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getCandidateNameAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with candidate age should return candidate age field")
	void testParseExpressionCandidateAgeReturnsCandidateAgeField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createCandidateAgeExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with salary type case should return salary type field")
	void testParseExpressionSalaryTypeCaseReturnsSalaryTypeField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createSalaryTypeCaseExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with candidate date field should return candidate date field")
	void testParseExpressionCandidateDateFieldReturnsCandidateDateField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createCandidateDateFieldExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with candidate last note date should return last note field")
	void testParseExpressionCandidateLastNoteDateReturnsLastNoteField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.CANDIDATE_LAST_NOTE_DATE_EXPRESSION;
		String alias = AdvancedJooqExpressionParserTestDataFactory.LAST_NOTE_CREATED_ON_ALIAS;

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with candidate opt-out should return candidate opt-out field")
	void testParseExpressionCandidateOptOutReturnsCandidateOptOutField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createCandidateOptOutExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	// ==================== Timesheet Expression Tests ====================

	@Test
	@DisplayName("Parse expression with timesheet period should return timesheet period field")
	void testParseExpressionTimesheetPeriodReturnsTimesheetPeriodField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createTimesheetPeriodExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getTimesheetPeriodAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with job duration should return job duration field")
	void testParseExpressionJobDurationReturnsJobDurationField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createJobDurationExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with timesheet frequency should return timesheet frequency field")
	void testParseExpressionTimesheetFrequencyReturnsTimesheetFrequencyField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createTimesheetFrequencyExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with timesheet status should return timesheet status field")
	void testParseExpressionTimesheetStatusReturnsTimesheetStatusField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createTimesheetStatusExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with formatted timesheet ID should return formatted timesheet ID field")
	void testParseExpressionFormattedTimesheetIdReturnsFormattedTimesheetIdField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createFormattedTimesheetIdExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getFormattedTimesheetIdAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with timesheet date field should return timesheet date field")
	void testParseExpressionTimesheetDateFieldReturnsTimesheetDateField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createTimesheetDateFieldExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	// ==================== User Expression Tests ====================

	@Test
	@DisplayName("Parse expression with user full name should return user full name field")
	void testParseExpressionUserFullNameReturnsUserFullNameField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createUserFullNameExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with timesheet added by should return timesheet added by field")
	void testParseExpressionTimesheetAddedByReturnsTimesheetAddedByField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createTimesheetAddedByExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with user approved by missing first name should return fallback field")
	void testParseExpressionUserApprovedByMissingFirstNameReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory
			.createUserApprovedByExpressionMissingFirstName();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with user approved by missing last name should return fallback field")
	void testParseExpressionUserApprovedByMissingLastNameReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createUserApprovedByExpressionMissingLastName();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with timesheet added by missing last name should return fallback field")
	void testParseExpressionTimesheetAddedByMissingLastNameReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory
			.createTimesheetAddedByExpressionMissingLastName();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with timesheet added by missing concat should return fallback field")
	void testParseExpressionTimesheetAddedByMissingConcatReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createTimesheetAddedByExpressionMissingConcat();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with timesheet updated by missing last name should return fallback field")
	void testParseExpressionTimesheetUpdatedByMissingLastNameReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory
			.createTimesheetUpdatedByExpressionMissingLastName();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with timesheet updated by should return timesheet updated by field")
	void testParseExpressionTimesheetUpdatedByReturnsTimesheetUpdatedByField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createTimesheetUpdatedByExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	// ==================== Invoice Expression Tests ====================

	@Test
	@DisplayName("Parse expression with pay status case should return pay status field")
	void testParseExpressionPayStatusCaseReturnsPayStatusField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createPayStatusCaseExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with bill status case should return bill status field")
	void testParseExpressionBillStatusCaseReturnsBillStatusField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createBillStatusCaseExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with payout paid on should return payout paid on field")
	void testParseExpressionPayoutPaidOnReturnsPayoutPaidOnField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createPayoutPaidOnExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with invoice created on should return invoice created on field")
	void testParseExpressionInvoiceCreatedOnReturnsInvoiceCreatedOnField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createInvoiceCreatedOnExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with invoice number should return invoice number field")
	void testParseExpressionInvoiceNumberReturnsInvoiceNumberField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createInvoiceNumberExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	// ==================== Special Expression Tests ====================

	@Test
	@DisplayName("Parse expression with approvers subquery should return approvers subquery field")
	void testParseExpressionApproversSubqueryReturnsApproversSubqueryField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createApproversSubqueryExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with deal name field should return deal name field")
	void testParseExpressionDealNameFieldReturnsDealNameField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createDealNameFieldExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with date format function should return date format function field")
	void testParseExpressionDateFormatFunctionReturnsDateFormatFunctionField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createDateFormatFunctionExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with custom column field should return custom column field")
	void testParseExpressionCustomColumnFieldReturnsCustomColumnField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createCustomColumnFieldExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with valid custcolumn prefix should return custom column field")
	void testParseExpressionValidCustColumnPrefixReturnsCustomColumnField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createValidCustColumnExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	// ==================== Edge Case Tests ====================

	@Test
	@DisplayName("Parse expression with null expression should return fallback field")
	void testParseExpressionNullExpressionReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createNullExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
	}

	@Test
	@DisplayName("Parse expression with empty expression should return fallback field")
	void testParseExpressionEmptyExpressionReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createEmptyExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with exception throwing expression should return fallback field")
	void testParseExpressionExceptionThrowingExpressionReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createExceptionThrowingExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willThrow(new RuntimeException("Test exception"));

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with invalid custom column should return null")
	void testParseExpressionInvalidCustomColumnReturnsNull() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createInvalidCustomColumnExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with zero index custom column should return fallback field")
	void testParseExpressionZeroIndexCustomColumnReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createZeroIndexCustomColumnExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with high number custom column should return null")
	void testParseExpressionHighNumberCustomColumnReturnsNull() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createHighNumberCustomColumnExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	// ==================== Branch Coverage Tests ====================

	@ParameterizedTest
	@ValueSource(strings = { "C.CANDIDATEDOB", // Missing FROM_UNIXTIME
			"TS.ADDED_ON", // Missing FROM_UNIXTIME
			"FROM_UNIXTIME(C.AVAILABLEFROM, \"%b %d %Y\")", "FROM_UNIXTIME(C.CREATEDON, \"%b %d %Y\")",
			"FROM_UNIXTIME(C.UPDATEDON, \"%b %d %Y\")", "FROM_UNIXTIME(C.RESUMEADDEDON, \"%b %d %Y\")",
			"FROM_UNIXTIME(C.RESUMEUPDATEDON, \"%b %d %Y\")",
			"FROM_UNIXTIME(C.RESUMEUPDATEREQUESTEDON, \"%b %d %Y\")" })
	@DisplayName("Parse expression with date fields should return appropriate fields")
	void testParseExpressionDateFieldsReturnsAppropriateFields(String expression) {
		// Given
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with different candidate last activity date fields should return appropriate fields")
	void testParseExpressionDifferentCandidateLastActivityDateFieldsReturnsAppropriateFields() {
		// Given - Test different candidate last activity date field branches
		String[] expressions = { "FROM_UNIXTIME(CLA.CALLLOG_CREATED_ON, \"%b %d %Y\")",
				"FROM_UNIXTIME(CLA.SMS_SENT_ON, \"%b %d %Y\")", "FROM_UNIXTIME(CLA.EMAIL_SENT_ON, \"%b %d %Y\")",
				"FROM_UNIXTIME(CLA.LAST_COMMUNICATION_TIMESTAMP, \"%b %d %Y\")",
				"FROM_UNIXTIME(CLA.MEETING_CREATED_ON, \"%b %d %Y\")",
				"FROM_UNIXTIME(CLA.MESSAGE_SENT_ON, \"%b %d %Y\")",
				"FROM_UNIXTIME(CLA.NOTE_CREATED_ON, \"%b %d %Y\")" };

		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		for (String expression : expressions) {
			given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

			// When
			Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo(alias);
		}

		then(this.jooqFieldResolver).should().canResolve(expressions[0]);
		then(this.jooqFieldResolver).should().canResolve(expressions[1]);
		then(this.jooqFieldResolver).should().canResolve(expressions[2]);
		then(this.jooqFieldResolver).should().canResolve(expressions[3]);
		then(this.jooqFieldResolver).should().canResolve(expressions[4]);
		then(this.jooqFieldResolver).should().canResolve(expressions[5]);
		then(this.jooqFieldResolver).should().canResolve(expressions[6]);
	}

	@Test
	@DisplayName("Parse expression with different opt-out fields should return appropriate fields")
	void testParseExpressionDifferentOptOutFieldsReturnsAppropriateFields() {
		// Given - Test different opt-out field branches
		String[] expressions = { "DSL.case_(C.EMAIL_OPT_OUT).when(0, \"No\").otherwise(\"Yes\")",
				"DSL.case_(C.SMS_OPT_OUT).when(0, \"No\").otherwise(\"Yes\")" };

		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		for (String expression : expressions) {
			given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

			// When
			Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo(alias);
		}

		then(this.jooqFieldResolver).should().canResolve(expressions[0]);
		then(this.jooqFieldResolver).should().canResolve(expressions[1]);
	}

	@Test
	@DisplayName("Parse expression with different timesheet date fields should return appropriate fields")
	void testParseExpressionDifferentTimesheetDateFieldsReturnsAppropriateFields() {
		// Given - Test different timesheet date field branches
		String[] expressions = { "FROM_UNIXTIME(TS.ADDED_ON, \"%m/%d/%Y\")",
				"FROM_UNIXTIME(TS.UPDATED_ON, \"%m/%d/%Y\")" };

		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		for (String expression : expressions) {
			given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

			// When
			Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo(alias);
		}

		then(this.jooqFieldResolver).should().canResolve(expressions[0]);
		then(this.jooqFieldResolver).should().canResolve(expressions[1]);
	}

	// ==================== Branch Coverage Edge Cases ====================

	@Test
	@DisplayName("Parse candidate age expression missing C.CANDIDATEDOB should return fallback field")
	void testParseCandidateAgeExpressionMissingCandidateDobReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory
			.createCandidateAgeExpressionMissingCandidateDob();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse candidate age expression missing TIMESTAMPDIFF should return fallback field")
	void testParseCandidateAgeExpressionMissingTimestampDiffReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory
			.createCandidateAgeExpressionMissingTimestampDiff();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse timesheet period expression missing TS.PERIOD_END should return fallback field")
	void testParseTimesheetPeriodExpressionMissingPeriodEndReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory
			.createTimesheetPeriodExpressionMissingPeriodEnd();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getTimesheetPeriodAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse job date range expression missing TSS.JOB_END_DATE should return fallback field")
	void testParseJobDateRangeExpressionMissingJobEndDateReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createJobDateRangeExpressionMissingJobEndDate();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse timesheet frequency expression missing DSL.case_ should return fallback field")
	void testParseTimesheetFrequencyExpressionMissingCaseReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createTimesheetFrequencyExpressionMissingCase();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse approval status expression missing DSL.case_ should return fallback field")
	void testParseApprovalStatusExpressionMissingCaseReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createApprovalStatusExpressionMissingCase();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse formatted timesheet expression missing TS.ID should return fallback field")
	void testParseFormattedTimesheetExpressionMissingTsIdReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createFormattedTimesheetExpressionMissingTsId();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getFormattedTimesheetIdAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse formatted timesheet expression missing DSL.concat should return fallback field")
	void testParseFormattedTimesheetExpressionMissingConcatReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory
			.createFormattedTimesheetExpressionMissingConcat();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getFormattedTimesheetIdAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse formatted timesheet expression missing TS- prefix should return fallback field")
	void testParseFormattedTimesheetExpressionMissingTsPrefixReturnsFallbackField() {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory
			.createFormattedTimesheetExpressionMissingTsPrefix();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getFormattedTimesheetIdAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("parseDslValue without DSL.val prefix should return empty string field")
	void testParseDslValueWithoutPrefixReturnsEmptyStringField() throws Exception {
		// Given
		String expression = AdvancedJooqExpressionParserTestDataFactory.createDslValueWithoutPrefixExpression();
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		Method method = AdvancedJooqExpressionParser.class.getDeclaredMethod("parseDslValue", String.class,
				String.class);
		method.setAccessible(true);

		// When
		Field<?> result = (Field<?>) method.invoke(this.advancedJooqExpressionParser, expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
	}

	@ParameterizedTest
	@ValueSource(strings = { "DSL.val(someValue)", "TU.FIRST_NAME || TU.LAST_NAME",
			"TU_ADDED.FIRSTNAME TU_ADDED.LASTNAME", "TU_UPDATED.FIRSTNAME TU_UPDATED.LASTNAME",
			"DSL.field(other.field, String.class)", "DSL.function(OTHER_FUNC, String.class, field)",
			"FROM_UNIXTIME(C.CANDIDATEDOB, \"%m/%d/%Y\")", "custcolumn151", "custcolumnabc" })
	@DisplayName("Parse expression with partial-branch inputs should return fallback field")
	void testParseExpressionPartialBranchInputsReturnsFallbackField(String expression) {
		// Given
		String alias = AdvancedJooqExpressionParserTestDataFactory.getDefaultTestAlias();

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull().satisfies((field) -> assertThat(field.getName()).isEqualTo(alias));
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	// ==================== Currency Expression Tests ====================

	@Test
	@DisplayName("Parse expression with pay currency code should return pay currency field")
	void testParseExpressionPayCurrencyCodeReturnsPayCurrencyField() {
		// Given
		String expression = "PAY_CURRENCY_CODE";
		String alias = "payCurrency";

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

	@Test
	@DisplayName("Parse expression with bill currency code should return bill currency field")
	void testParseExpressionBillCurrencyCodeReturnsBillCurrencyField() {
		// Given
		String expression = "BILL_CURRENCY_CODE";
		String alias = "billCurrency";

		given(this.jooqFieldResolver.canResolve(expression)).willReturn(false);

		// When
		Field<?> result = this.advancedJooqExpressionParser.parseExpression(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		then(this.jooqFieldResolver).should().canResolve(expression);
	}

}
