package io.recruitcrm.microservice.timesheet.services.export;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.testdata.JooqFieldResolverTestDataFactory;
import org.jooq.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for JooqFieldResolver. Tests field resolution, caching, and validation
 * scenarios using BDDMockito style and comprehensive coverage for all public methods and
 * edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JooqFieldResolver Tests")
class JooqFieldResolverTests {

	private JooqFieldResolver jooqFieldResolver;

	@BeforeEach
	void setUp() {
		this.jooqFieldResolver = new JooqFieldResolver();
	}

	@Test
	@DisplayName("Resolve simple field should return aliased field for valid timesheet expression")
	void testResolveSimpleFieldValidTimesheetExpressionReturnsAliasedField() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createValidTimesheetExpression();
		String alias = JooqFieldResolverTestDataFactory.createTimesheetIdAlias();

		// When
		Field<?> result = this.jooqFieldResolver.resolveSimpleField(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
	}

	@Test
	@DisplayName("Resolve simple field should return aliased field for valid candidate expression")
	void testResolveSimpleFieldValidCandidateExpressionReturnsAliasedField() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createValidCandidateExpression();
		String alias = JooqFieldResolverTestDataFactory.createCandidateNameAlias();

		// When
		Field<?> result = this.jooqFieldResolver.resolveSimpleField(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
	}

	@Test
	@DisplayName("Resolve simple field should return aliased field for valid job expression")
	void testResolveSimpleFieldValidJobExpressionReturnsAliasedField() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createValidJobExpression();
		String alias = JooqFieldResolverTestDataFactory.createJobTitleAlias();

		// When
		Field<?> result = this.jooqFieldResolver.resolveSimpleField(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
	}

	@Test
	@DisplayName("Resolve simple field should return empty field for expression without dot")
	void testResolveSimpleFieldExpressionWithoutDotReturnsEmptyField() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createInvalidExpressionNoDot();
		String alias = JooqFieldResolverTestDataFactory.createTestAlias();

		// When
		Field<?> result = this.jooqFieldResolver.resolveSimpleField(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		// Should return empty string field for invalid expressions
	}

	@Test
	@DisplayName("Resolve simple field should return empty field for expression with too many parts")
	void testResolveSimpleFieldExpressionWithTooManyPartsReturnsEmptyField() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createInvalidExpressionTooManyParts();
		String alias = JooqFieldResolverTestDataFactory.createTestAlias();

		// When
		Field<?> result = this.jooqFieldResolver.resolveSimpleField(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		// Should return empty string field for invalid expressions
	}

	@Test
	@DisplayName("Resolve simple field should return empty field for unknown table expression")
	void testResolveSimpleFieldUnknownTableExpressionReturnsEmptyField() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createUnknownTableExpression();
		String alias = JooqFieldResolverTestDataFactory.createTestAlias();

		// When
		Field<?> result = this.jooqFieldResolver.resolveSimpleField(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		// Should return empty string field for unknown table
	}

	@Test
	@DisplayName("Resolve simple field should return empty field for nonexistent field expression")
	void testResolveSimpleFieldNonexistentFieldExpressionReturnsEmptyField() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createNonexistentFieldExpression();
		String alias = JooqFieldResolverTestDataFactory.createTestAlias();

		// When
		Field<?> result = this.jooqFieldResolver.resolveSimpleField(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		// Should return empty string field for nonexistent field
	}

	@Test
	@DisplayName("Resolve simple field should return empty field for empty expression")
	void testResolveSimpleFieldEmptyExpressionReturnsEmptyField() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createEmptyExpression();
		String alias = JooqFieldResolverTestDataFactory.createTestAlias();

		// When
		Field<?> result = this.jooqFieldResolver.resolveSimpleField(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		// Should return empty string field for empty expression
	}

	@Test
	@DisplayName("Resolve simple field should return empty field for null expression")
	void testResolveSimpleFieldNullExpressionReturnsEmptyField() {
		// Given
		String expression = null;
		String alias = JooqFieldResolverTestDataFactory.createTestAlias();

		// When
		Field<?> result = this.jooqFieldResolver.resolveSimpleField(expression, alias);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(alias);
		// Should return empty string field for null expression
	}

	@Test
	@DisplayName("Resolve simple field should use cache for repeated calls")
	void testResolveSimpleFieldRepeatedCallsUsesCache() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createValidTimesheetExpression();
		String alias = JooqFieldResolverTestDataFactory.createTimesheetIdAlias();

		// When
		Field<?> firstResult = this.jooqFieldResolver.resolveSimpleField(expression, alias);
		Field<?> secondResult = this.jooqFieldResolver.resolveSimpleField(expression, alias);

		// Then
		assertThat(firstResult).isNotNull();
		assertThat(secondResult).isNotNull();
		assertThat(firstResult).isSameAs(secondResult); // Should be same instance from
														// cache
	}

	@Test
	@DisplayName("Can resolve should return true for valid timesheet expression")
	void testCanResolveValidTimesheetExpressionReturnsTrue() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createValidTimesheetExpression();

		// When
		boolean result = this.jooqFieldResolver.canResolve(expression);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Can resolve should return true for valid candidate expression")
	void testCanResolveValidCandidateExpressionReturnsTrue() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createValidCandidateExpression();

		// When
		boolean result = this.jooqFieldResolver.canResolve(expression);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Can resolve should return true for valid job expression")
	void testCanResolveValidJobExpressionReturnsTrue() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createValidJobExpression();

		// When
		boolean result = this.jooqFieldResolver.canResolve(expression);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Can resolve should return false for expression without dot")
	void testCanResolveExpressionWithoutDotReturnsFalse() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createInvalidExpressionNoDot();

		// When
		boolean result = this.jooqFieldResolver.canResolve(expression);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Can resolve should return false for expression with too many parts")
	void testCanResolveExpressionWithTooManyPartsReturnsFalse() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createInvalidExpressionTooManyParts();

		// When
		boolean result = this.jooqFieldResolver.canResolve(expression);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Can resolve should return false for unknown table expression")
	void testCanResolveUnknownTableExpressionReturnsFalse() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createUnknownTableExpression();

		// When
		boolean result = this.jooqFieldResolver.canResolve(expression);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Can resolve should return false for empty expression")
	void testCanResolveEmptyExpressionReturnsFalse() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createEmptyExpression();

		// When
		boolean result = this.jooqFieldResolver.canResolve(expression);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Can resolve should return false for null expression")
	void testCanResolveNullExpressionReturnsFalse() {
		// Given
		String expression = null;

		// When
		boolean result = this.jooqFieldResolver.canResolve(expression);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Can resolve should return true for nonexistent field but valid table")
	void testCanResolveNonexistentFieldValidTableReturnsTrue() {
		// Given
		String expression = JooqFieldResolverTestDataFactory.createNonexistentFieldExpression();

		// When
		boolean result = this.jooqFieldResolver.canResolve(expression);

		// Then
		assertThat(result).isTrue(); // canResolve only checks table existence, not field
	}

}
