package io.recruitcrm.microservice.timesheet.services.export;

import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

/**
 * Enhanced JOOQ expression parser that delegates to the advanced parser for scalability.
 * This class serves as a compatibility layer for the existing API while providing much
 * better maintainability and extensibility.
 */
@Component
public class JooqFieldExpressionParser {

	private final AdvancedJooqExpressionParser advancedParser;

	public JooqFieldExpressionParser(AdvancedJooqExpressionParser advancedParser) {
		this.advancedParser = advancedParser;
	}

	/**
	 * Parse a string expression into a JOOQ Field object using the advanced parser. This
	 * method maintains compatibility with the existing API while using the new scalable
	 * architecture underneath.
	 * @param expression The JOOQ expression as a string (e.g., "CANDIDATE.ID" or complex
	 * expressions)
	 * @param frontendName The alias to apply to the field
	 * @return A JOOQ Field<?> object representing the expression
	 */
	public Field<?> parseExpression(String expression, String frontendName) {
		try {

			return this.advancedParser.parseExpression(expression, frontendName);
		}
		catch (Exception exception) {

			return DSL.val("").as(frontendName);
		}
	}

}