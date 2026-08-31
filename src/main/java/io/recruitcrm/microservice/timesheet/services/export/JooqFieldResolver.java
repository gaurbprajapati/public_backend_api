package io.recruitcrm.microservice.timesheet.services.export;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetApprovalT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetInvoiceT;
import io.recruitcrm.microservice.search.models.jooq.tables.CandidateCustomFieldsT;
import io.recruitcrm.microservice.search.models.jooq.tables.CandidateLastActivitiesT;
import io.recruitcrm.microservice.search.models.jooq.tables.EntityOffLimitT;
import io.recruitcrm.microservice.search.models.jooq.tables.OffLimitStatusT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcompany;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldealjobs;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblgender;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbljob;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced JOOQ field resolver that uses reflection and caching for scalable field
 * resolution. This approach eliminates the need for hardcoded if-statements for every
 * field.
 */
@Component
public class JooqFieldResolver {

	// Table aliases for consistent usage
	private static final CstTimesheetT TS = CstTimesheetT.CST_TIMESHEET_T.as("ts");

	private static final CstTimesheetSettingT TSS = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tss");

	private static final CstTimesheetSettingAssociationT TSA = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T
		.as("tsa");

	private static final Tblcandidate CANDIDATE = Tblcandidate.TBLCANDIDATE.as("c");

	private static final Tbljob JOB = Tbljob.TBLJOB.as("j");

	private static final Tblcompany COMPANY = Tblcompany.TBLCOMPANY.as("comp");

	private static final Tbldealjobs DEALJOBS = Tbldealjobs.TBLDEALJOBS.as("dealJobs");

	private static final CstTimesheetApprovalT TIMESHEET_APPROVAL_T = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T
		.as("ta");

	private static final CstTimesheetInvoiceT TIMESHEET_INVOICE_T = CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T
		.as("ti");

	private static final Tblgender GENDER = Tblgender.TBLGENDER.as("gen");

	private static final CandidateLastActivitiesT CANDIDATE_LAST_ACTIVITIES_T = CandidateLastActivitiesT.CANDIDATE_LAST_ACTIVITIES_T
		.as("cla");

	// Table registry for dynamic field resolution
	private final Map<String, Table<?>> tableRegistry;

	// Field cache to avoid repeated reflection calls
	private final Map<String, Field<?>> fieldCache;

	public JooqFieldResolver() {
		this.tableRegistry = initializeTableRegistry();
		this.fieldCache = new ConcurrentHashMap<>();
	}

	/**
	 * Initialize table registry with aliases
	 */
	private Map<String, Table<?>> initializeTableRegistry() {
		Map<String, Table<?>> tables = new HashMap<>();
		tables.put("TS", TS);
		tables.put("TSS", TSS);
		tables.put("TSA", TSA);
		tables.put("C", CANDIDATE);
		tables.put("J", JOB);
		tables.put("COMP", COMPANY);
		tables.put("DEALJOBS", DEALJOBS);
		tables.put("TIMESHEET_APPROVAL_T", TIMESHEET_APPROVAL_T);
		tables.put("TI", TIMESHEET_INVOICE_T);
		tables.put("GEN", GENDER);
		tables.put("CLA", CANDIDATE_LAST_ACTIVITIES_T);
		tables.put("CCF", CandidateCustomFieldsT.CANDIDATE_CUSTOM_FIELDS_T);
		// Off-limit tables (now using proper JOOQ-generated table classes)
		tables.put("EOL", EntityOffLimitT.ENTITY_OFF_LIMIT_T.as("eol"));
		tables.put("OLS", OffLimitStatusT.OFF_LIMIT_STATUS_T.as("ols"));
		// Deal subquery table (referenced as virtual table from JOIN)
		tables.put("DEALS", org.jooq.impl.DSL.table("deals"));

		return tables;
	}

	/**
	 * Resolve a simple field reference like "CANDIDATE.ID" or "TS.PERIOD_START"
	 */
	public Field<?> resolveSimpleField(String expression, String alias) {

		// Check cache first
		String cacheKey = expression + ":" + alias;
		if (this.fieldCache.containsKey(cacheKey)) {
			return this.fieldCache.get(cacheKey);
		}

		try {
			// Parse table.field expression
			String[] parts = expression.split("\\.");
			if (parts.length != 2) {

				return DSL.val("").as(alias);
			}

			String tableName = parts[0];
			String fieldName = parts[1];

			// Get table from registry
			Table<?> table = this.tableRegistry.get(tableName);
			if (table == null) {

				return DSL.val("").as(alias);
			}

			// Use reflection to get the field
			Field<?> field = getFieldFromTable(table, fieldName);
			if (field == null) {

				return DSL.val("").as(alias);
			}

			// Apply alias and cache
			Field<?> aliasedField = field.as(alias);
			this.fieldCache.put(cacheKey, aliasedField);

			return aliasedField;

		}
		catch (Exception exception) {

			return DSL.val("").as(alias);
		}
	}

	/**
	 * Get field from table using reflection
	 */
	private Field<?> getFieldFromTable(Table<?> table, String fieldName) {
		try {
			// Try to get the field using reflection
			java.lang.reflect.Field reflectionField = table.getClass().getField(fieldName);
			Object fieldValue = reflectionField.get(table);

			if (fieldValue instanceof Field<?>) {
				return (Field<?>) fieldValue;
			}

			return null;
		}
		catch (NoSuchFieldException | IllegalAccessException exception) {

			return null;
		}
	}

	/**
	 * Check if a field expression can be resolved
	 */
	public boolean canResolve(String expression) {
		if (expression == null || !expression.contains(".")) {
			return false;
		}

		String[] parts = expression.split("\\.");
		return parts.length == 2 && this.tableRegistry.containsKey(parts[0]);
	}

}
