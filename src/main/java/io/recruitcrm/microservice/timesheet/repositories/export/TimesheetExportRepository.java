package io.recruitcrm.microservice.timesheet.repositories.export;

import io.recruitcrm.microservice.search.models.jooq.tables.CandidateCustomFieldsT;
import io.recruitcrm.microservice.search.models.jooq.tables.CandidateLastActivitiesT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetInvoiceT;
import io.recruitcrm.microservice.search.models.jooq.tables.EntityOffLimitT;
import io.recruitcrm.microservice.search.models.jooq.tables.OffLimitStatusT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcurrency;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldeals;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldealcandidates;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldealjobs;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityTypeEnum;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetApprovalT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcompany;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblgender;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbluser;

import io.recruitcrm.microservice.search.models.jooq.tables.Tbljob;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ExportFieldDefinition;
import io.recruitcrm.microservice.timesheet.dto.export.PeriodGroupedExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.TimesheetTotalsQueryResultDto;
import io.recruitcrm.microservice.timesheet.services.export.ExportFieldRegistry;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dynamic export repository using JOOQ with DSLContext. Builds queries dynamically based
 * on requested fields using JOOQ's fluent API.
 */
@Repository
@Transactional(readOnly = true)
public class TimesheetExportRepository implements ITimesheetExportRepository {

	private final DSLContext dslContext;

	private final ExportFieldRegistry fieldRegistry;

	// Table aliases for consistent usage
	private static final CstTimesheetT TS = CstTimesheetT.CST_TIMESHEET_T.as("ts");

	private static final CstTimesheetSettingT TSS = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tss");

	private static final CstTimesheetSettingAssociationT TSA = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T
		.as("tsa");

	private static final Tblcandidate CANDIDATE = Tblcandidate.TBLCANDIDATE.as("c");

	private static final Tbljob JOB = Tbljob.TBLJOB.as("j");

	private static final Tblcompany COMPANY = Tblcompany.TBLCOMPANY.as("comp");

	private static final CstTimesheetApprovalT TA = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T.as("ta");

	private static final Tbluser TU = Tbluser.TBLUSER.as("tu");

	private static final Tbluser TU_ADDED = Tbluser.TBLUSER.as("tu_added");

	private static final Tbluser TU_UPDATED = Tbluser.TBLUSER.as("tu_updated");

	private static final Tbluser USER = Tbluser.TBLUSER.as("usr");

	private static final Tbluser USER_UPDATED = Tbluser.TBLUSER.as("usr_upd");

	private static final Tbluser USER_OWNER = Tbluser.TBLUSER.as("usr_own");

	private static final Tblgender GENDER = Tblgender.TBLGENDER.as("gen");

	private static final CandidateCustomFieldsT CANDIDATE_CUSTOM_FIELDS = CandidateCustomFieldsT.CANDIDATE_CUSTOM_FIELDS_T
		.as("ccf");

	private static final CandidateLastActivitiesT CANDIDATE_LAST_ACTIVITIES = CandidateLastActivitiesT.CANDIDATE_LAST_ACTIVITIES_T
		.as("cla");

	// Off-limit tables (now using proper JOOQ-generated table classes)
	private static final EntityOffLimitT ENTITY_OFF_LIMIT = EntityOffLimitT.ENTITY_OFF_LIMIT_T.as("eol");

	private static final OffLimitStatusT OFF_LIMIT_STATUS = OffLimitStatusT.OFF_LIMIT_STATUS_T.as("ols");

	// Deal tables for complex deal_name subquery JOIN
	private static final Tbldeals DEALS = Tbldeals.TBLDEALS.as("deals_tbl");

	private static final Tbldealcandidates DEAL_CANDIDATES = Tbldealcandidates.TBLDEALCANDIDATES.as("dc");

	private static final Tbldealjobs DEAL_JOBS = Tbldealjobs.TBLDEALJOBS.as("dj");

	// Timesheet invoice table (for pay status, bill status, payout, invoice fields)
	private static final CstTimesheetInvoiceT TI = CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T.as("ti");

	// Currency tables (for pay and bill currency codes)
	private static final Tblcurrency PAY_CURRENCY = Tblcurrency.TBLCURRENCY.as("pay_curr");

	private static final Tblcurrency BILL_CURRENCY = Tblcurrency.TBLCURRENCY.as("bill_curr");

	private static final String INVOICE_ENTITY = "invoice";

	private static final String PERIOD_START_EPOCH = "period_start_epoch";

	private static final String PERIOD_END_EPOCH = "period_end_epoch";

	public TimesheetExportRepository(DSLContext dslContext, ExportFieldRegistry fieldRegistry) {
		this.dslContext = dslContext;
		this.fieldRegistry = fieldRegistry;
	}

	@Override
	public TimesheetTotalsQueryResultDto getTimesheetTotals(List<Integer> timesheetIds, Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return new TimesheetTotalsQueryResultDto(new HashMap<>(), new HashMap<>(), new HashMap<>(),
					new HashMap<>());
		}

		var records = this.dslContext
			.select(TS.ID, TS.TOTAL_TIME, TS.TOTAL_WORK_TIME, TS.TOTAL_OVERTIME, TS.TOTAL_REGULAR_HOUR)
			.from(TS)
			.where(TS.ID.in(timesheetIds))
			.and(TS.ACCOUNT_ID.eq(accountId))
			.fetch();

		Map<Integer, String> totalTime = new HashMap<>();
		Map<Integer, String> totalWorkTime = new HashMap<>();
		Map<Integer, String> totalOvertime = new HashMap<>();
		Map<Integer, String> totalRegularHours = new HashMap<>();

		for (var rec : records) {
			Integer id = rec.get(TS.ID);
			if (id == null) {
				continue;
			}
			totalTime.put(id, formatSecondsToHours(rec.get(TS.TOTAL_TIME)));
			totalWorkTime.put(id, formatSecondsToHours(rec.get(TS.TOTAL_WORK_TIME)));
			totalOvertime.put(id, formatSecondsToHours(rec.get(TS.TOTAL_OVERTIME)));
			totalRegularHours.put(id, formatSecondsToHours(rec.get(TS.TOTAL_REGULAR_HOUR)));
		}

		return new TimesheetTotalsQueryResultDto(totalTime, totalWorkTime, totalOvertime, totalRegularHours);
	}

	private static String formatSecondsToHours(Integer seconds) {
		if (seconds == null || seconds < 0) {
			return "0.00";
		}
		double hours = seconds / 3600.0;
		return String.format("%.2f", hours);
	}

	/**
	 * Get export data using dynamic JOOQ query building
	 */
	public List<DynamicExportResponseBodyDto> getExportData(DynamicExportRequestBodyDto request, Integer accountId) {

		// 1. Get field definitions for requested fields
		List<ExportFieldDefinition> fieldDefinitions = this.fieldRegistry
			.getFieldDefinitions(request.getSelectedFields());

		// 2. Build dynamic JOOQ select fields
		List<Field<?>> selectFields = this.buildSelectFields(fieldDefinitions);

		// 3. Build base query with dynamic joins
		SelectJoinStep<?> baseQuery = this.buildBaseQuery(selectFields, fieldDefinitions);

		// 4. Add WHERE conditions
		SelectConditionStep<?> conditionedQuery = this.addWhereConditions(baseQuery, request, accountId);

		// 5. Execute query and get results
		var results = this.dslContext.fetch(conditionedQuery);

		// 6. Convert to DynamicExportResponseBodyDto
		return this.convertToDynamicExportData(results, request.getSelectedFields());
	}

	/**
	 * Get export data grouped by timesheet periods (converted to UTC)
	 */
	public List<PeriodGroupedExportResponseBodyDto> getExportDataGroupedByPeriods(DynamicExportRequestBodyDto request,
			Integer accountId) {

		// When specific timesheetIds are provided, we should NOT use SQL GROUP BY
		// as it can cause records with same period dates to be grouped together,
		// losing individual timesheet records. Instead, get individual records and group
		// in memory.
		if (request.getTimesheetIds() != null && !request.getTimesheetIds().isEmpty()) {
			return this.getExportDataGroupedByPeriodsFromIndividualRecords(request, accountId);
		}

		// 1. Fetch all timesheets in a single query (avoids N+1 round-trips)
		var flatResults = this.fetchAllTimesheetsForPeriodGrouping(request, accountId);

		// 2. Group in memory and convert to PeriodGroupedExportResponseBodyDto
		return this.convertToGroupedPeriodData(flatResults, request.getSelectedFields());
	}

	/**
	 * Get export data grouped by periods from individual records (used when specific
	 * timesheetIds are provided) This avoids SQL GROUP BY which can lose individual
	 * records with same period dates.
	 */
	protected List<PeriodGroupedExportResponseBodyDto> getExportDataGroupedByPeriodsFromIndividualRecords(
			DynamicExportRequestBodyDto request, Integer accountId) {

		// 1. Get individual timesheet records without GROUP BY
		List<DynamicExportResponseBodyDto> individualTimesheetRecords = this.getExportData(request, accountId);

		// 2. Group records by period in memory
		Map<String, List<DynamicExportResponseBodyDto>> recordsByPeriod = new LinkedHashMap<>();

		for (DynamicExportResponseBodyDto individualTimesheet : individualTimesheetRecords) {
			// Extract period information from the record data
			String timesheetPeriod = (String) individualTimesheet.getData().get("timesheetPeriod");

			if (timesheetPeriod != null) {
				recordsByPeriod.computeIfAbsent(timesheetPeriod, (k) -> new ArrayList<>()).add(individualTimesheet);
			}
		}

		// 3. Convert to PeriodGroupedExportResponseBodyDto format
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();

		for (Map.Entry<String, List<DynamicExportResponseBodyDto>> entry : recordsByPeriod.entrySet()) {
			String periodRange = entry.getKey();
			List<DynamicExportResponseBodyDto> periodRecords = entry.getValue();

			// Parse period range to extract start and end dates
			// Format: "01/06/2025 - 05/20/2025"
			String[] periodParts = periodRange.split(" - ");
			String periodStartDisplay = (periodParts.length > 0) ? periodParts[0] : "";
			String periodEndDisplay = (periodParts.length > 1) ? periodParts[1] : "";

			PeriodGroupedExportResponseBodyDto groupedPeriod = new PeriodGroupedExportResponseBodyDto(
					periodStartDisplay, periodEndDisplay, periodRange, periodRecords, periodRecords.size(),
					this.parseDateToEpoch(periodStartDisplay));

			groupedData.add(groupedPeriod);
		}

		sortGroupDataByPeriodStartDate(groupedData);

		return groupedData;
	}

	/**
	 * Build JOOQ select fields from field definitions
	 */
	protected List<Field<?>> buildSelectFields(List<ExportFieldDefinition> fieldDefinitions) {
		List<Field<?>> fields = new ArrayList<>();
		for (ExportFieldDefinition fieldDefinition : fieldDefinitions) {
			fields.add(fieldDefinition.getJooqField());
		}
		return fields;
	}

	/**
	 * Build base JOOQ query with dynamic joins
	 */
	protected SelectJoinStep<?> buildBaseQuery(List<Field<?>> selectFields,
			List<ExportFieldDefinition> fieldDefinitions) {

		// Get all required entities
		Set<String> requiredEntities = fieldDefinitions.stream()
			.flatMap((def) -> def.getRequiredEntities().stream())
			.collect(Collectors.toSet());

		// Start with base query
		SelectJoinStep<?> query = this.dslContext.select(selectFields).from(TS);

		// Apply joins in logical groups to reduce complexity
		query = this.addCoreTimesheetJoins(query, requiredEntities);
		query = this.addCandidateRelatedJoins(query, requiredEntities);
		query = this.addUserRelatedJoins(query, requiredEntities);
		query = this.addJobAndCompanyJoins(query, requiredEntities);
		query = this.addDealJoins(query, requiredEntities);
		query = this.addApprovalJoins(query, requiredEntities);
		query = this.addInvoiceJoins(query, requiredEntities);
		query = this.addCurrencyJoins(query, requiredEntities);

		return query;
	}

	/**
	 * Add core timesheet-related joins (timesheet_setting, association, candidate)
	 */
	protected SelectJoinStep<?> addCoreTimesheetJoins(SelectJoinStep<?> query, Set<String> requiredEntities) {
		if (requiredEntities.contains("tss")) {
			// Join timesheet -> timesheet_setting
			query = query.leftJoin(TSS).on(TS.TIMESHEET_SETTING_ID.eq(TSS.ID));
		}

		if (requiredEntities.contains("tsa")) {
			// Join timesheet_setting -> association
			query = query.leftJoin(TSA).on(TSS.ASSOCIATION_ID.eq(TSA.ID));
		}

		if (requiredEntities.contains("c")) {
			// Join association -> candidate
			query = query.leftJoin(CANDIDATE).on(TSA.CONTRACTOR_ID.eq(CANDIDATE.ID));
		}

		return query;
	}

	/**
	 * Add candidate-related joins (custom fields, gender, activities, off-limits)
	 */
	protected SelectJoinStep<?> addCandidateRelatedJoins(SelectJoinStep<?> query, Set<String> requiredEntities) {
		if (requiredEntities.contains("ccf")) {
			query = query.leftJoin(CANDIDATE_CUSTOM_FIELDS).on(CANDIDATE.ID.eq(CANDIDATE_CUSTOM_FIELDS.CANDIDATE_ID));
		}

		if (requiredEntities.contains("gen")) {
			query = query.leftJoin(GENDER).on(CANDIDATE.GENDERID.eq(GENDER.ID));
		}

		if (requiredEntities.contains("cla")) {
			query = query.leftJoin(CANDIDATE_LAST_ACTIVITIES)
				.on(CANDIDATE.ID.eq(CANDIDATE_LAST_ACTIVITIES.CANDIDATE_ID));
		}

		if (requiredEntities.contains("eol")) {
			EntityTypeEnum entityType = this.determineEntityTypeForExport(requiredEntities);
			query = query.leftJoin(ENTITY_OFF_LIMIT)
				.on(CANDIDATE.ID.eq(ENTITY_OFF_LIMIT.ENTITY_ID)
					.and(ENTITY_OFF_LIMIT.ENTITY_TYPE.eq(entityType.getId())));
		}

		if (requiredEntities.contains("ols")) {
			query = query.leftJoin(OFF_LIMIT_STATUS).on(ENTITY_OFF_LIMIT.OFF_LIMIT_STATUS_ID.eq(OFF_LIMIT_STATUS.ID));
		}

		return query;
	}

	/**
	 * Add user-related joins (creator, updater, owner)
	 */
	protected SelectJoinStep<?> addUserRelatedJoins(SelectJoinStep<?> query, Set<String> requiredEntities) {
		if (requiredEntities.contains("usr")) {
			query = query.leftJoin(USER).on(CANDIDATE.CREATEDBY.eq(USER.ID));
		}

		if (requiredEntities.contains("usr_upd")) {
			query = query.leftJoin(USER_UPDATED).on(CANDIDATE.UPDATEDBY.eq(USER_UPDATED.ID));
		}

		if (requiredEntities.contains("usr_own")) {
			query = query.leftJoin(USER_OWNER).on(CANDIDATE.OWNERID.eq(USER_OWNER.ID));
		}

		return query;
	}

	/**
	 * Add job and company joins
	 */
	protected SelectJoinStep<?> addJobAndCompanyJoins(SelectJoinStep<?> query, Set<String> requiredEntities) {
		if (requiredEntities.contains("j")) {
			query = query.leftJoin(JOB).on(TSA.JOB_ID.eq(JOB.ID));
		}

		if (requiredEntities.contains("comp")) {
			query = query.leftJoin(COMPANY).on(JOB.COMPANYID.eq(COMPANY.ID));
		}

		return query;
	}

	/**
	 * Add timesheet invoice and invoice table joins (for pay status, bill status, payout
	 * number, invoice number, invoice created on)
	 *
	 * Uses a pre-aggregated derived table for latest invoice per timesheet instead of a
	 * correlated subquery, enabling hash/merge joins and reducing CPU load on large
	 * datasets.
	 */
	protected SelectJoinStep<?> addInvoiceJoins(SelectJoinStep<?> query, Set<String> requiredEntities) {
		boolean needsTi = requiredEntities.contains("ti") || requiredEntities.contains(INVOICE_ENTITY);
		if (needsTi) {
			// Pre-aggregate max invoice ID per timesheet (computed once, not per row)
			var latestInvoiceDerived = DSL
				.select(CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T.CST_TIMESHEET_ID,
						DSL.max(CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T.ID).as("latest_invoice_id"))
				.from(CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T)
				.groupBy(CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T.CST_TIMESHEET_ID)
				.asTable("latest_invoice");

			query = query.leftJoin(latestInvoiceDerived)
				.on(DSL.field("latest_invoice.cst_timesheet_id", Integer.class).eq(TS.ID))
				.leftJoin(TI)
				.on(TI.ID.eq(DSL.field("latest_invoice.latest_invoice_id", Integer.class)));
		}

		if (requiredEntities.contains(INVOICE_ENTITY)) {
			var invoiceTable = DSL.table("invoice_t").as(INVOICE_ENTITY);
			query = query.leftJoin(invoiceTable).on(DSL.field(INVOICE_ENTITY + ".id", Integer.class).eq(TI.INVOICE_ID));
		}

		return query;
	}

	/**
	 * Add currency-related joins (pay currency and bill currency from tblcurrency)
	 */
	protected SelectJoinStep<?> addCurrencyJoins(SelectJoinStep<?> query, Set<String> requiredEntities) {
		if (requiredEntities.contains("pay_curr")) {
			query = query.leftJoin(PAY_CURRENCY).on(TSS.PAY_CURRENCY_ID.eq(PAY_CURRENCY.ID));
		}

		if (requiredEntities.contains("bill_curr")) {
			query = query.leftJoin(BILL_CURRENCY).on(TSS.BILL_CURRENCY_ID.eq(BILL_CURRENCY.ID));
		}

		return query;
	}

	/**
	 * Add deal-related joins with subquery
	 */
	protected SelectJoinStep<?> addDealJoins(SelectJoinStep<?> query, Set<String> requiredEntities) {
		if (requiredEntities.contains("deals")) {
			// Create subquery for deal names grouped by candidate and job
			var dealSubquery = DSL
				.select(DSL.groupConcat(DEALS.NAME).separator(", ").as("deal_name"),
						DEAL_CANDIDATES.CANDIDATEID.as("candidateid"), DEAL_JOBS.JOBID.as("jobid"))
				.from(DEALS)
				.innerJoin(DEAL_CANDIDATES)
				.on(DEAL_CANDIDATES.DEALID.eq(DEALS.ID))
				.innerJoin(DEAL_JOBS)
				.on(DEAL_JOBS.DEALID.eq(DEALS.ID))
				.groupBy(DEAL_CANDIDATES.CANDIDATEID, DEAL_JOBS.JOBID)
				.asTable("deals");

			// LEFT JOIN the subquery on candidate and job
			query = query.leftJoin(dealSubquery)
				.on(TSA.CONTRACTOR_ID.eq(DSL.field("deals.candidateid", Integer.class))
					.and(TSA.JOB_ID.eq(DSL.field("deals.jobid", Integer.class))));
		}

		return query;
	}

	/**
	 * Add approval-related joins (timesheet approval and users)
	 *
	 * Uses a pre-aggregated derived table for latest approval per timesheet instead of a
	 * correlated subquery, enabling hash/merge joins and reducing CPU load on large
	 * datasets.
	 */
	protected SelectJoinStep<?> addApprovalJoins(SelectJoinStep<?> query, Set<String> requiredEntities) {
		if (requiredEntities.contains("ta")) {
			// Pre-aggregate max approval ID per timesheet (computed once, not per row)
			var latestApprovalDerived = DSL
				.select(CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T.TIMESHEET_ID,
						DSL.max(CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T.ID).as("latest_approval_id"))
				.from(CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T)
				.groupBy(CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T.TIMESHEET_ID)
				.asTable("latest_approval");

			query = query.leftJoin(latestApprovalDerived)
				.on(DSL.field("latest_approval.timesheet_id", Integer.class).eq(TS.ID))
				.leftJoin(TA)
				.on(TA.ID.eq(DSL.field("latest_approval.latest_approval_id", Integer.class)));
		}

		if (requiredEntities.contains("tu")) {
			// Join approval -> user (for approved_by name)
			query = query.leftJoin(TU).on(TA.ENTITY_ID.eq(TU.ID));
		}

		if (requiredEntities.contains("tu_added")) {
			// Join timesheet -> user (for added_by name)
			query = query.leftJoin(TU_ADDED).on(TS.ADDED_BY.eq(TU_ADDED.ID));
		}

		if (requiredEntities.contains("tu_updated")) {
			// Join timesheet -> user (for updated_by name)
			query = query.leftJoin(TU_UPDATED).on(TS.UPDATED_BY.eq(TU_UPDATED.ID));
		}

		return query;
	}

	/**
	 * Determine the appropriate entity type for the current export context. This method
	 * analyzes the required entities to determine what type of entity we're primarily
	 * exporting from.
	 * @param requiredEntities The set of required entities for the export
	 * @return The appropriate EntityType for the export context
	 */
	protected EntityTypeEnum determineEntityTypeForExport(Set<String> requiredEntities) {
		// For timesheet exports, we're primarily dealing with candidate data
		// since the export flows through: timesheet -> timesheet_setting -> association
		// -> candidate
		if (requiredEntities.contains("c")) {
			return EntityTypeEnum.CANDIDATE;
		}

		// Default to candidate for timesheet exports
		return EntityTypeEnum.CANDIDATE;
	}

	/**
	 * Add WHERE conditions to JOOQ query
	 */
	protected SelectConditionStep<?> addWhereConditions(SelectJoinStep<?> query, DynamicExportRequestBodyDto request,
			Integer accountId) {

		Condition whereCondition = TS.ACCOUNT_ID.eq(accountId);

		// Add timesheet ID filter if specified
		whereCondition = this.addTimesheetIdFilter(whereCondition, request);

		// Add custom filters if specified
		whereCondition = this.addCustomFilters(whereCondition, request);

		return query.where(whereCondition);
	}

	/**
	 * Add timesheet ID filter to the where condition
	 */
	protected Condition addTimesheetIdFilter(Condition whereCondition, DynamicExportRequestBodyDto request) {
		if (request.getTimesheetIds() != null && !request.getTimesheetIds().isEmpty()) {
			whereCondition = whereCondition.and(TS.ID.in(request.getTimesheetIds()));
			// Note: When timesheetIds are provided, other filters still apply but
			// timesheet IDs are the primary filter
		}
		return whereCondition;
	}

	/**
	 * Add custom filters to the where condition
	 */
	protected Condition addCustomFilters(Condition whereCondition, DynamicExportRequestBodyDto request) {
		if (request.getFilters() == null || request.getFilters().isEmpty()) {
			return whereCondition;
		}

		for (Map.Entry<String, Object> filter : request.getFilters().entrySet()) {
			whereCondition = this.applyIndividualFilter(whereCondition, filter.getKey(), filter.getValue());
		}

		return whereCondition;
	}

	/**
	 * Apply individual filter based on filter key and value
	 */
	protected Condition applyIndividualFilter(Condition whereCondition, String filterKey, Object filterValue) {
		switch (filterKey) {
			case "candidateId":
				return this.addCandidateIdFilter(whereCondition, filterValue);
			case "periodStartAfter":
				return this.addPeriodStartAfterFilter(whereCondition, filterValue);
			case "periodEndBefore":
				return this.addPeriodEndBeforeFilter(whereCondition, filterValue);
			default:
				// Unknown filter key - return condition unchanged
				return whereCondition;
		}
	}

	/**
	 * Add candidate ID filter
	 */
	protected Condition addCandidateIdFilter(Condition whereCondition, Object filterValue) {
		if (filterValue instanceof Integer integer) {
			return whereCondition.and(TSA.CONTRACTOR_ID.eq(integer));
		}
		return whereCondition;
	}

	/**
	 * Add period start after filter
	 */
	protected Condition addPeriodStartAfterFilter(Condition whereCondition, Object filterValue) {
		if (filterValue instanceof Integer integer) {
			return whereCondition.and(TS.PERIOD_START.ge(integer));
		}
		return whereCondition;
	}

	/**
	 * Add period end before filter
	 */
	protected Condition addPeriodEndBeforeFilter(Condition whereCondition, Object filterValue) {
		if (filterValue instanceof Integer integer) {
			return whereCondition.and(TS.PERIOD_END.le(integer));
		}
		return whereCondition;
	}

	/**
	 * Convert JOOQ query results to DynamicExportResponseBodyDto
	 */
	protected List<DynamicExportResponseBodyDto> convertToDynamicExportData(
			org.jooq.Result<? extends org.jooq.Record> results, List<String> selectedFields) {

		return results.stream().map((result) -> {
			Map<String, Object> data = new HashMap<>();

			// Map each result field to its corresponding field name
			for (String fieldName : selectedFields) {
				Object value = result.get(fieldName);
				data.put(fieldName, value);
			}

			// Create a new independent copy of selectedFields to avoid shared reference
			// issues
			return new DynamicExportResponseBodyDto(data, new ArrayList<>(selectedFields));
		}).toList();
	}

	/**
	 * Build select fields including period conversion fields for grouping
	 */
	protected List<Field<?>> buildSelectFieldsWithPeriods(List<ExportFieldDefinition> fieldDefinitions) {
		List<Field<?>> selectFields = this.buildSelectFields(fieldDefinitions);

		// Add period conversion fields for grouping
		selectFields.add(DSL
			.function("DATE_FORMAT", String.class,
					DSL.function("FROM_UNIXTIME", java.sql.Timestamp.class, TS.PERIOD_START), DSL.val("%d %M"))
			.as("period_start_display"));
		selectFields.add(DSL
			.function("DATE_FORMAT", String.class,
					DSL.function("FROM_UNIXTIME", java.sql.Timestamp.class, TS.PERIOD_END), DSL.val("%d %M"))
			.as("period_end_display"));
		selectFields.add(TS.PERIOD_START.as(PERIOD_START_EPOCH));
		selectFields.add(TS.PERIOD_END.as(PERIOD_END_EPOCH));

		return selectFields;
	}

	/**
	 * Fetch all timesheets for period grouping in a single batched query. Uses the same
	 * filters as getExportData but includes period fields for in-memory grouping. Avoids
	 * N+1 queries when exporting many periods.
	 */
	protected org.jooq.Result<? extends org.jooq.Record> fetchAllTimesheetsForPeriodGrouping(
			DynamicExportRequestBodyDto request, Integer accountId) {

		List<ExportFieldDefinition> fieldDefinitions = this.fieldRegistry
			.getFieldDefinitions(request.getSelectedFields());

		List<Field<?>> selectFields = this.buildSelectFieldsWithPeriods(fieldDefinitions);
		SelectJoinStep<?> baseQuery = this.buildBaseQuery(selectFields, fieldDefinitions);
		SelectConditionStep<?> conditionedQuery = this.addWhereConditions(baseQuery, request, accountId);

		return this.dslContext.fetch(conditionedQuery.orderBy(TS.PERIOD_START.asc(), TS.ID.desc()));
	}

	/**
	 * Convert flat query results to PeriodGroupedExportResponseBodyDto by grouping
	 * in-memory by (period_start_epoch, period_end_epoch). Each row contains full
	 * timesheet data; rows are grouped into periods without additional database queries.
	 */
	protected List<PeriodGroupedExportResponseBodyDto> convertToGroupedPeriodData(
			org.jooq.Result<? extends org.jooq.Record> flatResults, List<String> selectedFields) {

		if (flatResults == null || flatResults.isEmpty()) {
			return new ArrayList<>();
		}

		// Group rows by (period_start_epoch, period_end_epoch) preserving insertion order
		Map<String, List<org.jooq.Record>> rowsByPeriod = new LinkedHashMap<>();

		for (org.jooq.Record rec : flatResults) {
			Integer periodStartEpoch = (Integer) rec.get(PERIOD_START_EPOCH);
			Integer periodEndEpoch = (Integer) rec.get(PERIOD_END_EPOCH);

			String periodKey = periodStartEpoch + "_" + periodEndEpoch;
			rowsByPeriod.computeIfAbsent(periodKey, (k) -> new ArrayList<>()).add(rec);
		}

		// Convert each group to PeriodGroupedExportResponseBodyDto
		List<PeriodGroupedExportResponseBodyDto> groupedData = new ArrayList<>();

		for (List<org.jooq.Record> periodRows : rowsByPeriod.values()) {
			org.jooq.Record firstRow = periodRows.get(0);
			String periodStartDisplay = (String) firstRow.get("period_start_display");
			String periodEndDisplay = (String) firstRow.get("period_end_display");
			Integer periodStartEpoch = (Integer) firstRow.get(PERIOD_START_EPOCH);

			String periodDisplayName = ((periodStartDisplay != null) ? periodStartDisplay : "") + " - "
					+ ((periodEndDisplay != null) ? periodEndDisplay : "");

			List<DynamicExportResponseBodyDto> timesheetsInPeriod = periodRows.stream().map((row) -> {
				Map<String, Object> data = new HashMap<>();
				for (String fieldName : selectedFields) {
					Object value = row.get(fieldName);
					data.put(fieldName, value);
				}
				return new DynamicExportResponseBodyDto(data, new ArrayList<>(selectedFields));
			}).toList();

			groupedData.add(new PeriodGroupedExportResponseBodyDto(periodStartDisplay, periodEndDisplay,
					periodDisplayName, timesheetsInPeriod, timesheetsInPeriod.size(), periodStartEpoch));
		}

		// Sort by period start date
		sortGroupDataByPeriodStartDate(groupedData);

		return groupedData;
	}

	private void sortGroupDataByPeriodStartDate(List<PeriodGroupedExportResponseBodyDto> groupedData) {
		groupedData.sort((a, b) -> {
			Integer periodStartA = a.getPeriodStart();
			Integer periodStartB = b.getPeriodStart();

			if (periodStartA == null && periodStartB == null) {
				return 0;
			}
			if (periodStartA == null) {
				return 1;
			}
			if (periodStartB == null) {
				return -1;
			}
			return periodStartA.compareTo(periodStartB);
		});
	}

	/**
	 * Get all timesheets within a specific period
	 */
	protected List<DynamicExportResponseBodyDto> getTimesheetsInPeriod(DynamicExportRequestBodyDto request,
			Integer accountId, Integer periodStart, Integer periodEnd) {

		// Create a new request for this specific period
		DynamicExportRequestBodyDto periodRequest = DynamicExportRequestBodyDto.builder()
			.timesheetFields(request.getTimesheetFields())
			.fileFormat(request.getFileFormat())
			.maxRecords(request.getMaxRecords())
			.filters(request.getFilters())
			.timesheetIds(request.getTimesheetIds())
			.exportEachDay(false) // Disable grouping for individual period query
			.build();

		// Get field definitions
		List<ExportFieldDefinition> fieldDefinitions = this.fieldRegistry
			.getFieldDefinitions(request.getSelectedFields());

		// Build query for this specific period
		List<Field<?>> selectFields = this.buildSelectFields(fieldDefinitions);
		SelectJoinStep<?> baseQuery = this.buildBaseQuery(selectFields, fieldDefinitions);
		SelectConditionStep<?> conditionedQuery = this.addWhereConditions(baseQuery, periodRequest, accountId);

		// Add period-specific filters
		var periodFilteredQuery = conditionedQuery.and(TS.PERIOD_START.eq(periodStart))
			.and(TS.PERIOD_END.eq(periodEnd))
			.orderBy(TS.ID.desc());

		// Execute and convert
		var results = this.dslContext.fetch(periodFilteredQuery);
		return this.convertToDynamicExportData(results, request.getSelectedFields());
	}

	/**
	 * Parse date string in MM/dd/yyyy format to Unix timestamp for sorting. Returns null
	 * if parsing fails.
	 */
	protected Integer parseDateToEpoch(String dateString) {
		if (dateString == null || dateString.trim().isEmpty()) {
			return null;
		}

		try {
			// Parse MM/dd/yyyy format
			java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy");
			java.time.LocalDate localDate = java.time.LocalDate.parse(dateString.trim(), formatter);

			// Convert to Unix timestamp (start of day in UTC)
			java.time.ZonedDateTime zonedDateTime = localDate.atStartOfDay(java.time.ZoneId.of("UTC"));
			return (int) zonedDateTime.toEpochSecond();
		}
		catch (Exception ex) {
			// If parsing fails, return null (will be sorted to end)
			return null;
		}
	}

}
