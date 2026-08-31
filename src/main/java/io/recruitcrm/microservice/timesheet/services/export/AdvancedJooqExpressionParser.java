package io.recruitcrm.microservice.timesheet.services.export;

import io.recruitcrm.microservice.search.models.jooq.tables.CandidateLastActivitiesT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetApprovalT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetApproverT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetInvoiceT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbluser;

import java.util.Locale;

import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

/**
 * Simplified JOOQ expression parser that directly parses expressions from JSON
 * configuration. No more complex pattern matching or template systems - just direct
 * expression parsing.
 */
@Component
public class AdvancedJooqExpressionParser {

	private final JooqFieldResolver fieldResolver;

	// Constants for duplicated literals
	private static final String FROM_UNIXTIME = "FROM_UNIXTIME";

	private static final String INVOICE_ID_PREFIX_FIELD = "invoice.invoice_id_prefix";

	private static final String DATE_FORMAT_MDY = "%m/%d/%Y";

	private static final String DATE_FORMAT_BDY = "%b %d %Y";

	private static final String DSL_CASE_EXPR = "DSL.case_";

	private static final String DSL_CONCAT_EXPR = "DSL.concat";

	private static final String CUST_COLUMN_PREFIX = "custcolumn";

	// Common table references
	private static final CstTimesheetT TS = CstTimesheetT.CST_TIMESHEET_T.as("ts");

	private static final Tblcandidate CANDIDATE = Tblcandidate.TBLCANDIDATE.as("c");

	private static final CstTimesheetSettingT TSS = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tss");

	private static final CstTimesheetApprovalT TA = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T.as("ta");

	private static final Tbluser TU = Tbluser.TBLUSER.as("tu");

	private static final Tbluser TU_ADDED = Tbluser.TBLUSER.as("tu_added");

	private static final Tbluser TU_UPDATED = Tbluser.TBLUSER.as("tu_updated");

	private static final CstTimesheetApproverT TAP = CstTimesheetApproverT.CST_TIMESHEET_APPROVER_T.as("tap");

	private static final Tbluser TU_APPROVER = Tbluser.TBLUSER.as("tu_approver");

	private static final CandidateLastActivitiesT CLA = CandidateLastActivitiesT.CANDIDATE_LAST_ACTIVITIES_T.as("cla");

	private static final CstTimesheetInvoiceT TI = CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T.as("ti");

	public AdvancedJooqExpressionParser(JooqFieldResolver fieldResolver) {
		this.fieldResolver = fieldResolver;
	}

	/**
	 * Main entry point for parsing JOOQ expressions directly from JSON configuration
	 */
	public Field<?> parseExpression(String expression, String alias) {
		try {
			// Strategy 1: Simple field resolution (TABLE.FIELD)
			if (this.fieldResolver.canResolve(expression)) {
				return this.fieldResolver.resolveSimpleField(expression, alias);
			}

			// Strategy 2: DSL.val() expressions
			if (expression.startsWith("DSL.val(")) {
				return parseDslValue(expression, alias);
			}

			// Strategy 3: Direct JOOQ expression parsing
			return parseJooqExpression(expression, alias);

		}
		catch (Exception exception) {
			return DSL.val("").as(alias);
		}
	}

	/**
	 * Parse JOOQ expressions directly from JSON configuration
	 */
	private Field<?> parseJooqExpression(String expression, String alias) {
		// Candidate-related expressions
		Field<?> candidateField = this.parseCandidateExpressions(expression, alias);
		if (candidateField != null) {
			return candidateField;
		}

		// Timesheet-related expressions
		Field<?> timesheetField = this.parseTimesheetExpressions(expression, alias);
		if (timesheetField != null) {
			return timesheetField;
		}

		// User-related expressions
		Field<?> userField = this.parseUserExpressions(expression, alias);
		if (userField != null) {
			return userField;
		}

		// Invoice / timesheet invoice expressions
		Field<?> invoiceField = this.parseInvoiceExpressions(expression, alias);
		if (invoiceField != null) {
			return invoiceField;
		}

		// Special expressions
		Field<?> specialField = this.parseSpecialExpressions(expression, alias);
		if (specialField != null) {
			return specialField;
		}

		// Fallback for unknown expressions
		return DSL.val("").as(alias);
	}

	/**
	 * Parse candidate-related expressions
	 */
	private Field<?> parseCandidateExpressions(String expression, String alias) {
		// Handle candidate name expression
		if (expression.contains("DSL.when") && expression.contains("C.LASTNAME")) {
			return DSL.when(CANDIDATE.LASTNAME.isNull().or(CANDIDATE.LASTNAME.eq("")), CANDIDATE.FIRSTNAME)
				.otherwise(DSL.concat(CANDIDATE.FIRSTNAME, DSL.val(" "), CANDIDATE.LASTNAME))
				.as(alias);
		}

		// Handle candidate age expression
		if (expression.contains("DSL.when") && expression.contains("C.CANDIDATEDOB")
				&& expression.contains("TIMESTAMPDIFF")) {
			return DSL.when(CANDIDATE.CANDIDATEDOB.ne(1).and(CANDIDATE.CANDIDATEDOB.ne(0)), DSL
				.field("TIMESTAMPDIFF(YEAR, " + FROM_UNIXTIME + "({0}), NOW())", Integer.class, CANDIDATE.CANDIDATEDOB))
				.otherwise(0)
				.as(alias);
		}

		// Handle salary type label
		if (expression.contains(DSL_CASE_EXPR) && expression.contains("C.SALARYTYPE")) {
			return this.createSalaryTypeCaseExpression(alias);
		}

		// Handle candidate date fields
		Field<?> candidateDateField = this.parseCandidateDateFields(expression, alias);
		if (candidateDateField != null) {
			return candidateDateField;
		}

		// Handle candidate opt-out fields
		Field<?> optOutField = this.parseCandidateOptOutFields(expression, alias);
		if (optOutField != null) {
			return optOutField;
		}

		return null;
	}

	/**
	 * Parse timesheet-related expressions
	 */
	private Field<?> parseTimesheetExpressions(String expression, String alias) {
		// Handle timesheet period expression
		if (expression.contains(DSL_CONCAT_EXPR) && expression.contains("TS.PERIOD_START")
				&& expression.contains("TS.PERIOD_END")) {
			return DSL.concat(DSL.function(FROM_UNIXTIME, String.class, TS.PERIOD_START, DSL.val(DATE_FORMAT_MDY)),
					DSL.val(" - "), DSL.function(FROM_UNIXTIME, String.class, TS.PERIOD_END, DSL.val(DATE_FORMAT_MDY)))
				.as(alias);
		}

		// Handle job duration expression
		if (expression.contains(DSL_CONCAT_EXPR) && expression.contains("TSS.JOB_START_DATE")
				&& expression.contains("TSS.JOB_END_DATE")) {
			return DSL
				.concat(DSL.function(FROM_UNIXTIME, String.class, TSS.JOB_START_DATE, DSL.val(DATE_FORMAT_MDY)),
						DSL.val(" - "),
						DSL.function(FROM_UNIXTIME, String.class, TSS.JOB_END_DATE, DSL.val(DATE_FORMAT_MDY)))
				.as(alias);
		}

		// Handle timesheet frequency CASE expression
		if (expression.contains("TSS.TIMESHEET_FREQUENCY") && expression.contains(DSL_CASE_EXPR)) {
			return this.createTimesheetFrequencyCaseExpression(alias);
		}

		// Handle timesheet status CASE expression
		if (expression.contains("TA.TIMESHEET_APPROVAL_STATUS_TYPE_ID") && expression.contains(DSL_CASE_EXPR)) {
			return this.createTimesheetStatusCaseExpression(alias);
		}

		// Handle timesheet date fields
		Field<?> timesheetDateField = this.parseTimesheetDateFields(expression, alias);
		if (timesheetDateField != null) {
			return timesheetDateField;
		}

		// Handle formatted timesheet ID
		if (expression.contains("C.SRNO") && expression.contains("TS.ID") && expression.contains(DSL_CONCAT_EXPR)
				&& expression.contains("TS-")) {
			return DSL.concat(DSL.val("TS-"), CANDIDATE.SRNO, DSL.val("-"), TS.ID).as(alias);
		}

		return null;
	}

	/**
	 * Parse user-related expressions
	 */
	private Field<?> parseUserExpressions(String expression, String alias) {
		// Handle timesheet approved by expression
		if (expression.contains("TU.FIRST_NAME") && expression.contains("TU.LAST_NAME")
				&& expression.contains(DSL_CONCAT_EXPR)) {
			return DSL.case_()
				.when(TA.TIMESHEET_APPROVAL_STATUS_TYPE_ID.eq(4),
						DSL.concat(DSL.coalesce(TU.FIRSTNAME, DSL.val("")), DSL.val(" "),
								DSL.coalesce(TU.LASTNAME, DSL.val(""))))
				.otherwise(DSL.val(""))
				.as(alias);
		}

		// Handle timesheet added by expression
		if (expression.contains("TU_ADDED.FIRSTNAME") && expression.contains("TU_ADDED.LASTNAME")
				&& expression.contains(DSL_CONCAT_EXPR)) {
			return DSL
				.concat(DSL.coalesce(TU_ADDED.FIRSTNAME, DSL.val("")), DSL.val(" "),
						DSL.coalesce(TU_ADDED.LASTNAME, DSL.val("")))
				.as(alias);
		}

		// Handle timesheet updated by expression
		if (expression.contains("TU_UPDATED.FIRSTNAME") && expression.contains("TU_UPDATED.LASTNAME")
				&& expression.contains(DSL_CONCAT_EXPR)) {
			return DSL
				.concat(DSL.coalesce(TU_UPDATED.FIRSTNAME, DSL.val("")), DSL.val(" "),
						DSL.coalesce(TU_UPDATED.LASTNAME, DSL.val("")))
				.as(alias);
		}

		return null;
	}

	/**
	 * Parse invoice and timesheet invoice related expressions
	 */
	private Field<?> parseInvoiceExpressions(String expression, String alias) {
		if ("PAY_STATUS_CASE".equals(expression)) {
			return DSL.case_()
				.when(TI.CST_TIMESHEET_PAY_STATUS_TYPE_ID.eq(1), DSL.val("Paid"))
				.when(TI.CST_TIMESHEET_PAY_STATUS_TYPE_ID.eq(2), DSL.val("Unpaid"))
				.otherwise(DSL.val(""))
				.as(alias);
		}

		if ("BILL_STATUS_CASE".equals(expression)) {
			return DSL.case_()
				.when(TI.CST_TIMESHEET_BILL_STATUS_TYPE_ID.eq(1), DSL.val("Billed"))
				.when(TI.CST_TIMESHEET_BILL_STATUS_TYPE_ID.eq(2), DSL.val("Unbilled"))
				.when(TI.CST_TIMESHEET_BILL_STATUS_TYPE_ID.eq(3), DSL.val("Collected"))
				.otherwise(DSL.val(""))
				.as(alias);
		}

		if ("PAYOUT_PAID_ON_EXPR".equals(expression)) {
			return DSL.function(FROM_UNIXTIME, String.class, TI.PAYMENT_PAID_ON, DSL.val(DATE_FORMAT_MDY)).as(alias);
		}

		if ("INVOICE_CREATED_ON_EXPR".equals(expression)) {
			return DSL
				.function(FROM_UNIXTIME, String.class, DSL.field("invoice.created_on", Integer.class),
						DSL.val(DATE_FORMAT_MDY))
				.as(alias);
		}

		if ("INVOICE_NUMBER_EXPR".equals(expression)) {
			return DSL
				.when(DSL.field(INVOICE_ID_PREFIX_FIELD, String.class)
					.isNull()
					.or(DSL.field(INVOICE_ID_PREFIX_FIELD, String.class).eq("")),
						DSL.field("invoice.invoice_id_number", String.class))
				.otherwise(DSL.concat(DSL.field(INVOICE_ID_PREFIX_FIELD, String.class), DSL.val("-"),
						DSL.field("invoice.invoice_id_number", String.class)))
				.as(alias);
		}

		if ("PAY_CURRENCY_CODE".equals(expression)) {
			return DSL
				.concat(DSL.field("pay_curr.symbol", String.class), DSL.val(" "),
						DSL.field("pay_curr.code", String.class))
				.as(alias);
		}

		if ("BILL_CURRENCY_CODE".equals(expression)) {
			return DSL
				.concat(DSL.field("bill_curr.symbol", String.class), DSL.val(" "),
						DSL.field("bill_curr.code", String.class))
				.as(alias);
		}

		return null;
	}

	/**
	 * Parse special expressions
	 */
	private Field<?> parseSpecialExpressions(String expression, String alias) {
		// Handle timesheet approvers subquery
		if (expression.equals("SUBQUERY_GROUP_CONCAT_APPROVERS")) {
			return this.createApproversSubquery(alias);
		}

		// Handle deal_name field
		if (expression.contains("DSL.field") && expression.contains("deals.deal_name")) {
			return DSL.field("deals.deal_name", String.class).as(alias);
		}

		// Handle date format expressions
		if (expression.contains("DSL.function") && expression.contains("DATE_FORMAT")) {
			return DSL.function("DATE_FORMAT", String.class, TS.ID, DSL.val("%d %b %Y")).as(alias);
		}

		// Handle dynamic custom column fields
		if (expression.startsWith(CUST_COLUMN_PREFIX)) {
			return this.parseCustomColumnField(expression, alias);
		}

		return null;
	}

	/**
	 * Parse candidate date fields
	 */
	private Field<?> parseCandidateDateFields(String expression, String alias) {
		if (!expression.contains(FROM_UNIXTIME)) {
			return null;
		}

		if (expression.contains("C.CANDIDATEDOB") && expression.contains(DATE_FORMAT_BDY)) {
			return DSL.function(FROM_UNIXTIME, String.class, CANDIDATE.CANDIDATEDOB, DSL.val(DATE_FORMAT_BDY))
				.as(alias);
		}

		if (expression.contains("C.AVAILABLEFROM")) {
			return DSL.function(FROM_UNIXTIME, String.class, CANDIDATE.AVAILABLEFROM, DSL.val(DATE_FORMAT_BDY))
				.as(alias);
		}

		if (expression.contains("C.CREATEDON")) {
			return DSL.function(FROM_UNIXTIME, String.class, CANDIDATE.CREATEDON, DSL.val(DATE_FORMAT_BDY)).as(alias);
		}

		if (expression.contains("C.UPDATEDON")) {
			return DSL.function(FROM_UNIXTIME, String.class, CANDIDATE.UPDATEDON, DSL.val(DATE_FORMAT_BDY)).as(alias);
		}

		if (expression.contains("C.RESUMEADDEDON")) {
			return DSL.function(FROM_UNIXTIME, String.class, CANDIDATE.RESUMEADDEDON, DSL.val(DATE_FORMAT_BDY))
				.as(alias);
		}

		if (expression.contains("C.RESUMEUPDATEDON")) {
			return DSL.function(FROM_UNIXTIME, String.class, CANDIDATE.RESUMEUPDATEDON, DSL.val(DATE_FORMAT_BDY))
				.as(alias);
		}

		if (expression.contains("C.RESUMEUPDATEREQUESTEDON")) {
			return DSL
				.function(FROM_UNIXTIME, String.class, CANDIDATE.RESUMEUPDATEREQUESTEDON, DSL.val(DATE_FORMAT_BDY))
				.as(alias);
		}

		// Handle candidate last activity date fields
		return this.parseCandidateLastActivityDateFields(expression, alias);
	}

	/**
	 * Parse candidate last activity date fields
	 */
	private Field<?> parseCandidateLastActivityDateFields(String expression, String alias) {
		if (expression.contains("CLA.CALLLOG_CREATED_ON")) {
			return DSL.function(FROM_UNIXTIME, String.class, CLA.CALLLOG_CREATED_ON, DSL.val(DATE_FORMAT_BDY))
				.as(alias);
		}

		if (expression.contains("CLA.SMS_SENT_ON")) {
			return DSL.function(FROM_UNIXTIME, String.class, CLA.SMS_SENT_ON, DSL.val(DATE_FORMAT_BDY)).as(alias);
		}

		if (expression.contains("CLA.EMAIL_SENT_ON")) {
			return DSL.function(FROM_UNIXTIME, String.class, CLA.EMAIL_SENT_ON, DSL.val(DATE_FORMAT_BDY)).as(alias);
		}

		if (expression.contains("CLA.LAST_COMMUNICATION_TIMESTAMP")) {
			return DSL.function(FROM_UNIXTIME, String.class, CLA.LAST_COMMUNICATION_TIMESTAMP, DSL.val(DATE_FORMAT_BDY))
				.as(alias);
		}

		if (expression.contains("CLA.MEETING_CREATED_ON")) {
			return DSL.function(FROM_UNIXTIME, String.class, CLA.MEETING_CREATED_ON, DSL.val(DATE_FORMAT_BDY))
				.as(alias);
		}

		if (expression.contains("CLA.MESSAGE_SENT_ON")) {
			return DSL.function(FROM_UNIXTIME, String.class, CLA.MESSAGE_SENT_ON, DSL.val(DATE_FORMAT_BDY)).as(alias);
		}

		if (expression.contains("CLA.NOTE_CREATED_ON")) {
			return DSL.function(FROM_UNIXTIME, String.class, CLA.NOTE_CREATED_ON, DSL.val(DATE_FORMAT_BDY)).as(alias);
		}

		return null;
	}

	/**
	 * Parse candidate opt-out fields
	 */
	private Field<?> parseCandidateOptOutFields(String expression, String alias) {
		if (expression.contains(DSL_CASE_EXPR) && expression.contains("C.EMAIL_OPT_OUT")) {
			return DSL.case_()
				.when(CANDIDATE.EMAIL_OPT_OUT.eq((byte) 0), DSL.val("No"))
				.otherwise(DSL.val("Yes"))
				.as(alias);
		}

		if (expression.contains(DSL_CASE_EXPR) && expression.contains("C.SMS_OPT_OUT")) {
			return DSL.case_()
				.when(CANDIDATE.SMS_OPT_OUT.eq((byte) 0), DSL.val("No"))
				.otherwise(DSL.val("Yes"))
				.as(alias);
		}

		return null;
	}

	/**
	 * Parse timesheet date fields
	 */
	private Field<?> parseTimesheetDateFields(String expression, String alias) {
		if (!expression.contains(FROM_UNIXTIME)) {
			return null;
		}

		if (expression.contains("TS.ADDED_ON")) {
			return DSL.function(FROM_UNIXTIME, String.class, TS.ADDED_ON, DSL.val(DATE_FORMAT_MDY)).as(alias);
		}

		if (expression.contains("TS.UPDATED_ON")) {
			return DSL.function(FROM_UNIXTIME, String.class, TS.UPDATED_ON, DSL.val(DATE_FORMAT_MDY)).as(alias);
		}

		return null;
	}

	/**
	 * Create salary type case expression
	 */
	private Field<?> createSalaryTypeCaseExpression(String alias) {
		return DSL.case_()
			.when(CANDIDATE.SALARYTYPE.eq("1"), "Monthly Salary")
			.when(CANDIDATE.SALARYTYPE.eq("2"), "Annual Salary")
			.when(CANDIDATE.SALARYTYPE.eq("3"), "Weekly Salary")
			.when(CANDIDATE.SALARYTYPE.eq("4"), "Daily Salary")
			.when(CANDIDATE.SALARYTYPE.eq("5"), "Hourly Salary")
			.otherwise("")
			.as(alias);
	}

	/**
	 * Create timesheet frequency case expression
	 */
	private Field<?> createTimesheetFrequencyCaseExpression(String alias) {
		return DSL.case_()
			.when(TSS.TIMESHEET_FREQUENCY.eq(1), DSL.val("Custom"))
			.when(TSS.TIMESHEET_FREQUENCY.eq(2), DSL.val("Weekly"))
			.when(TSS.TIMESHEET_FREQUENCY.eq(3), DSL.val("Biweekly"))
			.when(TSS.TIMESHEET_FREQUENCY.eq(4), DSL.val("Monthly"))
			.otherwise(DSL.val("Unknown"))
			.as(alias);
	}

	/**
	 * Create timesheet status case expression
	 */
	private Field<?> createTimesheetStatusCaseExpression(String alias) {
		return DSL.case_()
			.when(TA.TIMESHEET_APPROVAL_STATUS_TYPE_ID.eq(1), DSL.val("Open"))
			.when(TA.TIMESHEET_APPROVAL_STATUS_TYPE_ID.eq(2), DSL.val("Submitted"))
			.when(TA.TIMESHEET_APPROVAL_STATUS_TYPE_ID.eq(3), DSL.val("Rejected"))
			.when(TA.TIMESHEET_APPROVAL_STATUS_TYPE_ID.eq(4), DSL.val("Approved"))
			.otherwise(DSL.val("UNKNOWN"))
			.as(alias);
	}

	/**
	 * Create approvers subquery
	 */
	private Field<?> createApproversSubquery(String alias) {
		return DSL
			.select(DSL.field("GROUP_CONCAT(DISTINCT CONCAT(COALESCE({0}, ''), ' ', COALESCE({1}, '')) SEPARATOR ', ')",
					String.class, TU_APPROVER.FIRSTNAME, TU_APPROVER.LASTNAME))
			.from(TAP)
			.leftJoin(TU_APPROVER)
			.on(TAP.ENTITY_ID.eq(TU_APPROVER.ID))
			.where(TAP.TIMESHEET_SETTING_ID.eq(TSS.ID))
			.and(DSL.trim(DSL.concat(DSL.coalesce(TU_APPROVER.FIRSTNAME, DSL.val("")), DSL.val(" "),
					DSL.coalesce(TU_APPROVER.LASTNAME, DSL.val(""))))
				.ne(""))
			.asField(alias);
	}

	/**
	 * Parse custom column field
	 */
	private Field<?> parseCustomColumnField(String expression, String alias) {
		try {
			String columnNumber = expression.substring(CUST_COLUMN_PREFIX.length());
			int columnIndex = Integer.parseInt(columnNumber);

			if (columnIndex >= 1 && columnIndex <= 150) {
				String dbColumnName = CUST_COLUMN_PREFIX + columnIndex;
				Field<?> baseField = DSL.field("ccf." + dbColumnName.toLowerCase(Locale.ROOT));
				return baseField.as(alias);
			}
		}
		catch (NumberFormatException ex) {
			// Invalid column number, fall through to default
		}

		return null;
	}

	/**
	 * Parse DSL.val() expressions dynamically
	 */
	private Field<?> parseDslValue(String expression, String alias) {
		// Extract value from DSL.val(value) handling nested parentheses
		String prefix = "DSL.val(";
		String suffix = ")";

		if (!expression.startsWith(prefix) || !expression.endsWith(suffix)) {
			return DSL.val("").as(alias);
		}

		// Extract content between outermost parentheses
		String value = expression.substring(prefix.length(), expression.length() - suffix.length());

		// Handle different value types
		if (value.equals("\"\"")) {
			return DSL.val("").as(alias);
		}
		else if (value.startsWith("\"") && value.endsWith("\"")) {
			// String value
			String stringValue = value.substring(1, value.length() - 1);
			return DSL.val(stringValue).as(alias);
		}
		else if (value.matches("\\d+")) {
			// Integer value
			return DSL.val(Integer.parseInt(value)).as(alias);
		}
		else if (value.matches("\\d+\\.\\d+")) {
			// Decimal value
			return DSL.val(Double.parseDouble(value)).as(alias);
		}

		return DSL.val(value).as(alias);
	}

}
