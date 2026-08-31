package io.recruitcrm.microservice.timesheet.rule_engine.constants;

public final class RuleTemplateConstants {

	private RuleTemplateConstants() {
		// prevent instantiation
	}

	public static final String CLONE_PREFIX = "(Clone) ";

	public static final String ELLIPSIS = "...";

	public static final int MAX_TEMPLATE_NAME_LENGTH = 200;

	public static final String TEMPLATE_NAME_COLUMN = "template_name";

	public static final String TEMPLATE_NAME_CANNOT_BE_NULL = "cannot be null";

	public static final String TEMPLATE_NAME_REQUIRED_MESSAGE = "Template name is required";

	public static final String UNIQUE_TEMPLATE_NAME_CONSTRAINT = "uk_rule_template_name";

	public static final String DATA_TRUNCATION = "Data truncation";

	public static final String DATA_TOO_LONG = "Data too long";

}