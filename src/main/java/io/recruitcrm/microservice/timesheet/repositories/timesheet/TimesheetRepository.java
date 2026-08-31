package io.recruitcrm.microservice.timesheet.repositories.timesheet;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.BillStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.PaymentStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Job;
import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
import io.recruitcrm.microservice.search.models.jooq.tables.CstJobTimesheetAccessT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetApprovalT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetApproverT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetInvoiceT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetReimbursementT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.search.models.jooq.tables.EntityOffLimitT;
import io.recruitcrm.microservice.search.models.jooq.tables.JobSecondaryContactsT;
import io.recruitcrm.microservice.search.models.jooq.tables.OffLimitStatusColourT;
import io.recruitcrm.microservice.search.models.jooq.tables.OffLimitStatusT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblassignjobcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcompany;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcontact;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcurrency;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldealcandidates;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldealjobs;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldealpipelinestages;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldeals;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbljob;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbljobstatus;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbluser;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityTypeEnum;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DealQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetDealListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAndContractorListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPermissionDataDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobSearchQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CompanySearchQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeDetailSummaryDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DealSearchQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetForMigrationDto;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.helpers.constants.BooleanFlagEnum;
import io.recruitcrm.microservice.timesheet.helpers.constants.ReimbursementConstants;
import io.recruitcrm.microservice.timesheet.helpers.constants.RepositoryParameterConstants;
import io.recruitcrm.microservice.timesheet.repositories.SortingQueryBuilder;
import jakarta.persistence.EntityManager;
import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectJoinStep;
import org.jooq.SelectLimitStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.SelectSelectStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TimesheetRepository implements ITimesheetRepository {

	// String literal constants for field names and aliases
	// Currency table aliases
	private static final String PAY_CURRENCY_ALIAS = "pay_currency";

	private static final String BILL_CURRENCY_ALIAS = "bill_currency";

	// Invoice table and field names
	private static final String INVOICE_TABLE = "invoice";

	private static final String INVOICE_TABLE_ALIAS = "invoice_t";

	private static final String INVOICE_ID_FIELD = "invoice.id";

	private static final String INVOICE_CREATED_ON_FIELD = "invoice.created_on";

	private static final String INVOICE_ID_PREFIX_FIELD = "invoice.invoice_id_prefix";

	private static final String INVOICE_ID_NUMBER_FIELD = "invoice.invoice_id_number";

	private static final String INVOICE_STATUS_ID_FIELD = "invoice.invoice_status_id";

	private static final String INVOICE_UPDATED_ON_ALIAS = "invoiceUpdatedOn";

	// Result DTO field aliases - Company
	private static final String COMPANY_NAME_ALIAS = "companyName";

	// Result DTO field aliases - Contractor
	private static final String CONTRACTOR_ID_ALIAS = "contractorId";

	private static final String CONTRACTOR_NAME_ALIAS = "contractorName";

	private static final String CONTRACTOR_PHOTO_ALIAS = "contractorPhoto";

	private static final String CONTRACTOR_ASSIGNMENT_ID_ALIAS = "contractorAssignmentId";

	private static final String CONTRACTOR_SLUG_ALIAS = "contractorSlug";

	private static final String CONTRACTOR_POSITION_ALIAS = "contractorPosition";

	private static final String CONTRACTOR_OWNER_ID_ALIAS = "contractorOwnerId";

	// Contractor off-limit table and field aliases
	private static final String CONTRACTOR_ENTITY_OFF_LIMIT_T_ALIAS = "contractorEntityOffLimitT";

	private static final String CONTRACTOR_OFF_LIMIT_STATUS_T_ALIAS = "contractorOffLimitStatusT";

	private static final String CONTRACTOR_OFF_LIMIT_STATUS_COLOUR_T_ALIAS = "contractorOffLimitStatusColourT";

	private static final String CONTRACTOR_OFF_LIMIT_STATUS_ID_ALIAS = "contractorOffLimitStatusId";

	private static final String CONTRACTOR_STATUS_LABEL_ALIAS = "contractorStatusLabel";

	private static final String CONTRACTOR_BACKGROUND_COLOR_HEX_ALIAS = "contractorBackgroundColorHex";

	private static final String CONTRACTOR_TEXT_COLOR_HEX_ALIAS = "contractorTextColorHex";

	private static final String CONTRACTOR_OFF_LIMIT_REASON_ALIAS = "contractorOffLimitReason";

	private static final String CONTRACTOR_MARKED_BY_NAME_ALIAS = "contractorMarkedByName";

	private static final String CONTRACTOR_OFF_LIMIT_START_DATE_ALIAS = "contractorOffLimitStartDate";

	private static final String CONTRACTOR_OFF_LIMIT_END_DATE_ALIAS = "contractorOffLimitEndDate";

	private static final String CONTRACTOR_OFF_LIMIT_MARKED_BY_USER_ALIAS = "contractorOffLimitMarkedByUser";

	private static final String OWNER_ALIAS = "owner";

	private static final String SERIAL_NUMBER_ALIAS = "serialNumber";

	private static final String PAYOUT_NUMBER_ALIAS = "payoutNumber";

	private static final String ADDED_BY_USER_TYPE_ID_ALIAS = "addedByUserTypeId";

	private static final String UPDATED_BY_USER_TYPE_ID_ALIAS = "updatedByUserTypeId";

	private static final String TIMESHEET_SETTING_ID_ALIAS = "timesheetSettingId";

	private static final String CAN_CREATE_ALIAS = "canCreate";

	private static final String CAN_EDIT_ALIAS = "canEdit";

	private static final String CAN_DELETE_ALIAS = "canDelete";

	private static final String COMPANY_LOGO_ALIAS = "companyLogo";

	private static final String JOB_STATUS_ALIAS = "jobStatus";

	private static final String JOB_TYPE_ALIAS = "jobType";

	// Result DTO field aliases - Job
	private static final String JOB_ID_ALIAS = "jobId";

	private static final String JOB_NAME_ALIAS = "jobName";

	private static final String JOB_SLUG_ALIAS = "jobSlug";

	private static final String JOB_DURATION_START_DATE_ALIAS = "jobDurationStartDate";

	private static final String JOB_DURATION_END_DATE_ALIAS = "jobDurationEndDate";

	// Result DTO field aliases - Timesheet period
	private static final String TIMESHEET_PERIOD_START_DATE_ALIAS = "timesheetPeriodStartDate";

	private static final String TIMESHEET_PERIOD_END_DATE_ALIAS = "timesheetPeriodEndDate";

	private static final String WORK_LOG_TYPE_ALIAS = "workLogType";

	// Result DTO field aliases - Audit fields
	private static final String ADDED_BY_ID_ALIAS = "addedById";

	private static final String UPDATED_BY_ID_ALIAS = "updatedById";

	private static final String ADDED_ON_ALIAS = "addedOn";

	private static final String UPDATED_ON_ALIAS = "updatedOn";

	// Result DTO field aliases - Payment and billing
	private static final String PAY_RATE_ALIAS = "payRate";

	private static final String BILL_RATE_ALIAS = "billRate";

	private static final String PAY_DATA_ALIAS = "payData";

	private static final String BILL_DATA_ALIAS = "billData";

	private static final String TOTAL_TIME_ALIAS = "totalTime";

	private static final String TOTAL_WORK_TIME_ALIAS = "totalWorkTime";

	private static final String TOTAL_OVERTIME_ALIAS = "totalOvertime";

	private static final String WHEN_TSID_PARAM_PREFIX = "WHEN :tsId";

	private static final String SQL_THEN_PARAM_PREFIX = " THEN :";

	private static final String PAY_CURRENCY_SYMBOL_ALIAS = "payCurrencySymbol";

	private static final String BILL_CURRENCY_SYMBOL_ALIAS = "billCurrencySymbol";

	private static final String PAY_CURRENCY_CODE_ALIAS = "payCurrencyCode";

	private static final String BILL_CURRENCY_CODE_ALIAS = "billCurrencyCode";

	private static final String PAY_STATUS_ID_ALIAS = "payStatusId";

	private static final String BILL_STATUS_ID_ALIAS = "billStatusId";

	private static final String PAYOUT_PAID_ON_ALIAS = "payoutPaidOn";

	private static final String PAYOUT_FILE_ALIAS = "payoutFile";

	// Result DTO field aliases - Invoice
	private static final String INVOICE_NUMBER_ALIAS = "invoiceNumber";

	private static final String INVOICE_CREATED_ON_ALIAS = "invoiceCreatedOn";

	private static final String INVOICE_STATUS_ID_ALIAS = "invoiceStatusId";

	private static final String REIMBURSEMENT_COUNT_ALIAS = "reimbursementCount";

	private static final String IS_REIMBURSEMENT_ENABLED_ALIAS = "isReimbursementEnabled";

	// Sort key and projection aliases
	private static final String TIMESHEET_ID_ALIAS = "timesheetId";

	private static final String DEAL_NAME_ALIAS = "dealName";

	private static final String TIMESHEET_STATUS_ID_ALIAS = "timesheetStatusId";

	private static final String APPROVED_BY_ALIAS = "approvedBy";

	private static final String ADDED_BY_ALIAS = "addedBy";

	private static final String UPDATED_BY_ALIAS = "updatedBy";

	// JOOQ table aliases for sort sub-queries
	private static final String TS_STATUS_FOR_SORT_ALIAS = "tsStatusForSort";

	private static final String TS_APPROVED_BY_FOR_SORT_ALIAS = "tsApprovedByForSort";

	private static final String TS_APPROVED_BY_LATEST_ALIAS = "tsApprovedByLatest";

	private static final String APPROVER_USER_FOR_SORT_ALIAS = "approverUserForSort";

	private static final String ADDED_BY_USER_FOR_SORT_ALIAS = "addedByUserForSort";

	private static final String UPDATED_BY_USER_FOR_SORT_ALIAS = "updatedByUserForSort";

	// Job type values for contract staffing filtering
	private static final String JOB_TYPE_CONTRACT = "contract";

	private static final String JOB_TYPE_CONTRACT_TO_PERMANENT = "contracttopermanent";

	final EntityManager entityManager;

	private final TimesheetJpaRepository timesheetJpaRepository;

	private final SortingQueryBuilder sortingQueryBuilder;

	private final DSLContext auroraDbDSLContext;

	private final AccessControlHelper accessControlHelper;

	public TimesheetRepository(EntityManager entityManager, TimesheetJpaRepository timesheetJpaRepository,
			SortingQueryBuilder sortingQueryBuilder, DSLContext auroraDbDSLContext,
			AccessControlHelper accessControlHelper) {
		this.entityManager = entityManager;
		this.timesheetJpaRepository = timesheetJpaRepository;
		this.sortingQueryBuilder = sortingQueryBuilder;
		this.auroraDbDSLContext = auroraDbDSLContext;
		this.accessControlHelper = accessControlHelper;
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Timesheet> createTimesheets(List<Timesheet> timesheets) {
		return this.timesheetJpaRepository.saveAll(timesheets);
	}

	@Override
	public List<ContractorJobQueryResultDto> getCommonCandidatesByDealId(Integer dealId) {
		String jpql = "SELECT new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobQueryResultDto(ajc.candidateId, ajc.jobId) "
				+ "FROM DealJob dj " + "LEFT JOIN AssignCandidateJob ajc ON ajc.jobId = dj.jobId "
				+ "INNER JOIN DealCandidate dc ON dc.candidate.id = ajc.candidateId "
				+ "INNER JOIN Job j ON j.id = ajc.jobId "
				+ "WHERE dj.deal.id = :dealId AND dc.deal.id = :dealId AND j.jobType IN ('contract', 'contracttopermanent')";
		return this.entityManager.createQuery(jpql, ContractorJobQueryResultDto.class)
			.setParameter("dealId", dealId)
			.getResultList();
	}

	@Override
	public List<TimesheetDealListQueryResultDto> getTimesheetsListByDealId(
			List<ContractorJobQueryResultDto> contractorJobs, Integer accountId,
			SearchRequestBodyDto searchRequestBodyDto, Pageable pageable) {

		// Aliases for tables
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var candidate = Tblcandidate.TBLCANDIDATE;
		var job = Tbljob.TBLJOB;
		var timesheetInvoice = CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;
		var reimbursement = CstTimesheetReimbursementT.CST_TIMESHEET_REIMBURSEMENT_T;

		// Currency aliases
		var payCurrency = Tblcurrency.TBLCURRENCY.as(PAY_CURRENCY_ALIAS);
		var billCurrency = Tblcurrency.TBLCURRENCY.as(BILL_CURRENCY_ALIAS);

		// Build WHERE condition for contractor-job pairs
		Condition whereCondition = DSL.falseCondition();
		for (ContractorJobQueryResultDto contractorJob : contractorJobs) {
			whereCondition = whereCondition.or(tsSettingAssoc.CONTRACTOR_ID.eq(contractorJob.getContractorId())
				.and(tsSettingAssoc.JOB_ID.eq(contractorJob.getJobId())));
		}

		// Create company table reference
		var company = DSL.table("tblcompany").as("company");
		var companyName = DSL.field("company.companyname", String.class).as(COMPANY_NAME_ALIAS);

		// Off-limit tables for contractor (candidate)
		var contractorOffLimit = this.buildContractorOffLimitTables();
		var contractorEntityOffLimitT = contractorOffLimit.entityOffLimitT();
		var contractorOffLimitStatusT = contractorOffLimit.offLimitStatusT();
		var contractorOffLimitStatusColourT = contractorOffLimit.offLimitStatusColourT();
		var contractorOffLimitMarkedByUser = Tbluser.TBLUSER.as(CONTRACTOR_OFF_LIMIT_MARKED_BY_USER_ALIAS);

		// Invoice table reference
		var invoice = DSL.table(INVOICE_TABLE_ALIAS).as(INVOICE_TABLE);
		var invoiceCreatedOn = DSL.field(INVOICE_CREATED_ON_FIELD, Integer.class).as(INVOICE_CREATED_ON_ALIAS);
		final String invoiceIdPrefixField = INVOICE_ID_PREFIX_FIELD;
		final String invoiceIdNumberField = INVOICE_ID_NUMBER_FIELD;
		final String invoiceStatusIdField = INVOICE_STATUS_ID_FIELD;
		var invoiceNumber = DSL
			.when(DSL.field(invoiceIdPrefixField, String.class)
				.isNull()
				.or(DSL.field(invoiceIdPrefixField, String.class).eq("")),
					DSL.field(invoiceIdNumberField, String.class))
			.otherwise(DSL.concat(DSL.field(invoiceIdPrefixField, String.class), DSL.value("-"),
					DSL.field(invoiceIdNumberField, String.class)))
			.as(INVOICE_NUMBER_ALIAS);
		var invoiceStatusId = DSL.field(invoiceStatusIdField, Integer.class).as(INVOICE_STATUS_ID_ALIAS);

		var reimbursementCountField = DSL
			.field(DSL.selectCount()
				.from(reimbursement)
				.where(reimbursement.CST_TIMESHEET_ID.eq(ts.ID).and(reimbursement.ACCOUNT_ID.eq(accountId))))
			.as(REIMBURSEMENT_COUNT_ALIAS);

		var baseQuery = DSL
			.select(ts.ID, tsSetting.WORK_LOG_TYPE.as(WORK_LOG_TYPE_ALIAS),
					ts.PERIOD_START.as(TIMESHEET_PERIOD_START_DATE_ALIAS),
					ts.PERIOD_END.as(TIMESHEET_PERIOD_END_DATE_ALIAS), ts.ADDED_BY.as(ADDED_BY_ID_ALIAS),
					ts.UPDATED_BY.as(UPDATED_BY_ID_ALIAS), ts.ADDED_BY_USER_TYPE_ID, ts.UPDATED_BY_USER_TYPE_ID,
					ts.ADDED_ON.as(ADDED_ON_ALIAS), ts.UPDATED_ON.as(UPDATED_ON_ALIAS),
					tsSetting.JOB_START_DATE.as(JOB_DURATION_START_DATE_ALIAS),
					tsSetting.JOB_END_DATE.as(JOB_DURATION_END_DATE_ALIAS), tsSetting.PAY_RATE.as(PAY_RATE_ALIAS),
					tsSetting.BILL_RATE.as(BILL_RATE_ALIAS), ts.PAY_DATA.as(PAY_DATA_ALIAS),
					ts.BILL_DATA.as(BILL_DATA_ALIAS), ts.TOTAL_TIME.as(TOTAL_TIME_ALIAS),
					ts.TOTAL_WORK_TIME.as(TOTAL_WORK_TIME_ALIAS), ts.TOTAL_OVERTIME.as(TOTAL_OVERTIME_ALIAS),
					payCurrency.SYMBOL.as(PAY_CURRENCY_SYMBOL_ALIAS),
					billCurrency.SYMBOL.as(BILL_CURRENCY_SYMBOL_ALIAS), payCurrency.CODE.as(PAY_CURRENCY_CODE_ALIAS),
					billCurrency.CODE.as(BILL_CURRENCY_CODE_ALIAS),
					tsSettingAssoc.CONTRACTOR_ID.as(CONTRACTOR_ID_ALIAS), tsSettingAssoc.JOB_ID.as(JOB_ID_ALIAS),
					candidate.ID.as(CONTRACTOR_ID_ALIAS), candidate.SRNO.as(SERIAL_NUMBER_ALIAS),
					candidate.OWNERID.as(CONTRACTOR_OWNER_ID_ALIAS),
					DSL.when(candidate.LASTNAME.isNull().or(candidate.LASTNAME.eq("")), candidate.FIRSTNAME)
						.otherwise(DSL.concat(candidate.FIRSTNAME, DSL.val(" "), candidate.LASTNAME))
						.as(CONTRACTOR_NAME_ALIAS),
					candidate.PROFILEPIC.as(CONTRACTOR_PHOTO_ALIAS), candidate.SLUG.as(CONTRACTOR_SLUG_ALIAS),
					candidate.POSITION.as(CONTRACTOR_POSITION_ALIAS), job.ID.as(JOB_ID_ALIAS),
					job.NAME.as(JOB_NAME_ALIAS), job.SLUG.as(JOB_SLUG_ALIAS), companyName,
					timesheetInvoice.CST_TIMESHEET_PAY_STATUS_TYPE_ID.as(PAY_STATUS_ID_ALIAS),
					timesheetInvoice.PAYMENT_PAID_ON.as(PAYOUT_PAID_ON_ALIAS), timesheetInvoice.PAYOUT_NUMBER,
					timesheetInvoice.PAYOUT_FILE.as(PAYOUT_FILE_ALIAS),
					timesheetInvoice.CST_TIMESHEET_BILL_STATUS_TYPE_ID.as(BILL_STATUS_ID_ALIAS),
					timesheetInvoice.UPDATED_ON.as(INVOICE_UPDATED_ON_ALIAS), invoiceNumber, invoiceCreatedOn,
					invoiceStatusId, assignJobCandidate.ID.as(CONTRACTOR_ASSIGNMENT_ID_ALIAS),
					// Contractor off-limit fields
					contractorEntityOffLimitT.OFF_LIMIT_STATUS_ID.as(CONTRACTOR_OFF_LIMIT_STATUS_ID_ALIAS),
					contractorOffLimitStatusT.STATUS_LABEL.as(CONTRACTOR_STATUS_LABEL_ALIAS),
					contractorOffLimitStatusColourT.BACKGROUND_COLOR_HEX.as(CONTRACTOR_BACKGROUND_COLOR_HEX_ALIAS),
					contractorOffLimitStatusColourT.TEXT_COLOR_HEX.as(CONTRACTOR_TEXT_COLOR_HEX_ALIAS),
					contractorEntityOffLimitT.OFF_LIMIT_REASON.as(CONTRACTOR_OFF_LIMIT_REASON_ALIAS),
					contractorEntityOffLimitT.CREATED_ON.as(CONTRACTOR_OFF_LIMIT_START_DATE_ALIAS),
					contractorEntityOffLimitT.OFF_LIMIT_END_DATE.as(CONTRACTOR_OFF_LIMIT_END_DATE_ALIAS),
					DSL.when(
							contractorOffLimitMarkedByUser.LASTNAME.isNull()
								.or(contractorOffLimitMarkedByUser.LASTNAME.eq("")),
							contractorOffLimitMarkedByUser.FIRSTNAME)
						.otherwise(DSL.concat(contractorOffLimitMarkedByUser.FIRSTNAME, DSL.val(" "),
								contractorOffLimitMarkedByUser.LASTNAME))
						.as(CONTRACTOR_MARKED_BY_NAME_ALIAS),
					tsSetting.IS_REIMBURSEMENT_ENABLED.cast(Integer.class).as(IS_REIMBURSEMENT_ENABLED_ALIAS),
					reimbursementCountField)
			.from(tsSetting)
			.join(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.leftJoin(contractorEntityOffLimitT)
			.on(contractorEntityOffLimitT.ENTITY_ID.eq(candidate.ID)
				.and(contractorEntityOffLimitT.ENTITY_TYPE.eq(EntityTypeEnum.CANDIDATE.getId())))
			.leftJoin(contractorOffLimitStatusT)
			.on(contractorEntityOffLimitT.OFF_LIMIT_STATUS_ID.eq(contractorOffLimitStatusT.ID))
			.leftJoin(contractorOffLimitStatusColourT)
			.on(contractorOffLimitStatusT.STATUS_COLOUR_ID
				.eq(contractorOffLimitStatusColourT.OFFLIMIT_STATUS_COLOUR_ID))
			.leftJoin(contractorOffLimitMarkedByUser)
			.on(contractorOffLimitMarkedByUser.ID.eq(contractorEntityOffLimitT.CREATED_BY))
			.leftJoin(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID))
			.leftJoin(company)
			.on(DSL.field("company.id", Integer.class).eq(job.COMPANYID))
			.leftJoin(assignJobCandidate)
			.on(assignJobCandidate.JOBID.eq(job.ID).and(assignJobCandidate.CANDIDATEID.eq(candidate.ID)))
			.join(ts)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(timesheetInvoice)
			.on(timesheetInvoice.CST_TIMESHEET_ID.eq(ts.ID))
			.leftJoin(invoice)
			.on(DSL.field(INVOICE_ID_FIELD, Integer.class).eq(timesheetInvoice.INVOICE_ID))
			.leftJoin(payCurrency)
			.on(payCurrency.ID.eq(tsSetting.PAY_CURRENCY_ID))
			.leftJoin(billCurrency)
			.on(billCurrency.ID.eq(tsSetting.BILL_CURRENCY_ID))
			.where(whereCondition);

		Select<?> finalQuery;
		if (searchRequestBodyDto.getSortPriorityList() != null
				&& !searchRequestBodyDto.getSortPriorityList().isEmpty()) {
			// Check if there are any valid (non-null, non-empty) sort fields
			boolean hasValidSortFields = searchRequestBodyDto.getSortPriorityList()
				.stream()
				.anyMatch((sort) -> sort.getField() != null && !sort.getField().trim().isEmpty());

			if (hasValidSortFields) {
				finalQuery = this.sortingQueryBuilder.addSortingQuery(baseQuery,
						this.expandSortTiebreakers(searchRequestBodyDto.getSortPriorityList()), ts);
			}
			else {
				// All sort fields are invalid, fall back to default sorting
				finalQuery = baseQuery.orderBy(ts.UPDATED_ON.desc(), ts.ID.desc());
			}
		}
		else {
			finalQuery = baseQuery.orderBy(ts.UPDATED_ON.desc(), ts.ID.desc());
		}

		finalQuery = ((SelectLimitStep<?>) finalQuery).offset(pageable.getPageNumber() * pageable.getPageSize())
			.limit(pageable.getPageSize());

		return this.auroraDbDSLContext.fetch(finalQuery).into(TimesheetDealListQueryResultDto.class);
	}

	@Override
	public List<TimesheetJobAndContractorListQueryResultDto> getTimesheetsListByJobAndContractorId(Integer jobId,
			Integer contractorId, Integer accountId, SearchRequestBodyDto searchRequestBodyDto, Pageable pageable) {

		// Aliases for tables
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var timesheetInvoice = CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T;
		final var payCurrency = Tblcurrency.TBLCURRENCY.as(PAY_CURRENCY_ALIAS);
		final var billCurrency = Tblcurrency.TBLCURRENCY.as(BILL_CURRENCY_ALIAS);
		var candidate = Tblcandidate.TBLCANDIDATE;
		var job = Tbljob.TBLJOB;
		var reimbursement = CstTimesheetReimbursementT.CST_TIMESHEET_REIMBURSEMENT_T;
		var sortTables = TimesheetListSortTables.create();

		// Off-limit tables for contractor (candidate)
		var contractorOffLimit = this.buildContractorOffLimitTables();
		var contractorEntityOffLimitT = contractorOffLimit.entityOffLimitT();
		var contractorOffLimitStatusT = contractorOffLimit.offLimitStatusT();
		var contractorOffLimitStatusColourT = contractorOffLimit.offLimitStatusColourT();

		// Create company table reference
		var company = DSL.table("tblcompany").as("company");
		var companyName = DSL.field("company.companyname", String.class).as(COMPANY_NAME_ALIAS);

		var invoice = DSL.table(INVOICE_TABLE_ALIAS).as(INVOICE_TABLE);
		final String invoiceIdPrefixField = INVOICE_ID_PREFIX_FIELD;
		final String invoiceIdNumberField = INVOICE_ID_NUMBER_FIELD;
		final String invoiceStatusIdField = INVOICE_STATUS_ID_FIELD;
		var invoiceNumber = DSL
			.when(DSL.field(invoiceIdPrefixField, String.class)
				.isNull()
				.or(DSL.field(invoiceIdPrefixField, String.class).eq("")),
					DSL.field(invoiceIdNumberField, String.class))
			.otherwise(DSL.concat(DSL.field(invoiceIdPrefixField, String.class), DSL.value("-"),
					DSL.field(invoiceIdNumberField, String.class)))
			.as(INVOICE_NUMBER_ALIAS);
		var invoiceCreatedOn = DSL.field(INVOICE_CREATED_ON_FIELD, Integer.class).as(INVOICE_CREATED_ON_ALIAS);
		var invoiceStatusId = DSL.field(invoiceStatusIdField, Integer.class).as(INVOICE_STATUS_ID_ALIAS);

		var reimbursementCountField = DSL
			.field(DSL.selectCount()
				.from(reimbursement)
				.where(reimbursement.CST_TIMESHEET_ID.eq(ts.ID).and(reimbursement.ACCOUNT_ID.eq(accountId))))
			.as(REIMBURSEMENT_COUNT_ALIAS);

		SelectConditionStep<?> baseQuery = DSL.select(
				// Timesheet fields
				ts.ID.as("id"), ts.PERIOD_START.as(TIMESHEET_PERIOD_START_DATE_ALIAS),
				ts.PERIOD_END.as(TIMESHEET_PERIOD_END_DATE_ALIAS),

				// TimesheetSetting fields
				tsSetting.WORK_LOG_TYPE.as(WORK_LOG_TYPE_ALIAS),
				tsSetting.JOB_START_DATE.as(JOB_DURATION_START_DATE_ALIAS),
				tsSetting.JOB_END_DATE.as(JOB_DURATION_END_DATE_ALIAS),

				// Rates
				tsSetting.PAY_RATE.as(PAY_RATE_ALIAS), tsSetting.BILL_RATE.as(BILL_RATE_ALIAS),

				// Currency symbols
				payCurrency.SYMBOL.as(PAY_CURRENCY_SYMBOL_ALIAS), billCurrency.SYMBOL.as(BILL_CURRENCY_SYMBOL_ALIAS),

				// Currency codes
				payCurrency.CODE.as(PAY_CURRENCY_CODE_ALIAS), billCurrency.CODE.as(BILL_CURRENCY_CODE_ALIAS),

				// Total Pay/Bill data from Timesheet table
				ts.PAY_DATA.as(PAY_DATA_ALIAS), ts.BILL_DATA.as(BILL_DATA_ALIAS),

				// Total time columns from Timesheet table (seconds)
				ts.TOTAL_TIME.as(TOTAL_TIME_ALIAS), ts.TOTAL_WORK_TIME.as(TOTAL_WORK_TIME_ALIAS),
				ts.TOTAL_OVERTIME.as(TOTAL_OVERTIME_ALIAS),

				// Audit timestamps
				ts.ADDED_ON.as(ADDED_ON_ALIAS), ts.UPDATED_ON.as(UPDATED_ON_ALIAS),

				// Added/Updated by
				ts.ADDED_BY.as(ADDED_BY_ID_ALIAS), ts.UPDATED_BY.as(UPDATED_BY_ID_ALIAS),

				// Timesheet Invoice table fields
				timesheetInvoice.CST_TIMESHEET_PAY_STATUS_TYPE_ID.as(PAY_STATUS_ID_ALIAS),
				timesheetInvoice.PAYMENT_PAID_ON.as(PAYOUT_PAID_ON_ALIAS),
				timesheetInvoice.PAYOUT_NUMBER.as(PAYOUT_NUMBER_ALIAS),
				timesheetInvoice.PAYOUT_FILE.as(PAYOUT_FILE_ALIAS),
				timesheetInvoice.CST_TIMESHEET_BILL_STATUS_TYPE_ID.as(BILL_STATUS_ID_ALIAS),

				// Invoice table fields
				invoiceNumber, invoiceCreatedOn, invoiceStatusId,

				// Contractor Serial Number
				candidate.SRNO.as(SERIAL_NUMBER_ALIAS),

				// User types
				ts.ADDED_BY_USER_TYPE_ID.as(ADDED_BY_USER_TYPE_ID_ALIAS),
				ts.UPDATED_BY_USER_TYPE_ID.as(UPDATED_BY_USER_TYPE_ID_ALIAS),

				// Contractor fields
				candidate.ID.as(CONTRACTOR_ID_ALIAS),
				DSL.when(candidate.LASTNAME.isNull().or(candidate.LASTNAME.eq("")), candidate.FIRSTNAME)
					.otherwise(DSL.concat(candidate.FIRSTNAME, DSL.val(" "), candidate.LASTNAME))
					.as(CONTRACTOR_NAME_ALIAS),
				candidate.PROFILEPIC.as(CONTRACTOR_PHOTO_ALIAS), candidate.SLUG.as(CONTRACTOR_SLUG_ALIAS),
				candidate.POSITION.as(CONTRACTOR_POSITION_ALIAS), candidate.OWNERID.as(CONTRACTOR_OWNER_ID_ALIAS),

				// Contractor off-limit fields
				contractorEntityOffLimitT.OFF_LIMIT_STATUS_ID.as(CONTRACTOR_OFF_LIMIT_STATUS_ID_ALIAS),
				contractorOffLimitStatusT.STATUS_LABEL.as(CONTRACTOR_STATUS_LABEL_ALIAS),
				contractorOffLimitStatusColourT.BACKGROUND_COLOR_HEX.as(CONTRACTOR_BACKGROUND_COLOR_HEX_ALIAS),
				contractorOffLimitStatusColourT.TEXT_COLOR_HEX.as(CONTRACTOR_TEXT_COLOR_HEX_ALIAS),

				// Job fields
				job.ID.as(JOB_ID_ALIAS), job.NAME.as(JOB_NAME_ALIAS), job.SLUG.as(JOB_SLUG_ALIAS), companyName,
				buildTimesheetStatusIdSortField(ts, sortTables.tsStatusForSort()),
				buildApprovedBySortField(ts, sortTables), ts.ID.as(TIMESHEET_ID_ALIAS), buildPayStatusSortField(),
				buildBillStatusSortField(), buildExpenseClaimSortField(),
				buildAgencyRecruiterDisplayNameSortField(sortTables.addedByUserForSort(), ts.ADDED_BY,
						ts.ADDED_BY_USER_TYPE_ID, ADDED_BY_ALIAS),
				buildAgencyRecruiterDisplayNameSortField(sortTables.updatedByUserForSort(), ts.UPDATED_BY,
						ts.UPDATED_BY_USER_TYPE_ID, UPDATED_BY_ALIAS),
				reimbursementCountField,
				tsSetting.IS_REIMBURSEMENT_ENABLED.cast(Integer.class).as(IS_REIMBURSEMENT_ENABLED_ALIAS))
			.from(ts)
			.leftJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.leftJoin(contractorEntityOffLimitT)
			.on(contractorEntityOffLimitT.ENTITY_ID.eq(candidate.ID)
				.and(contractorEntityOffLimitT.ENTITY_TYPE.eq(EntityTypeEnum.CANDIDATE.getId())))
			.leftJoin(contractorOffLimitStatusT)
			.on(contractorEntityOffLimitT.OFF_LIMIT_STATUS_ID.eq(contractorOffLimitStatusT.ID))
			.leftJoin(contractorOffLimitStatusColourT)
			.on(contractorOffLimitStatusT.STATUS_COLOUR_ID
				.eq(contractorOffLimitStatusColourT.OFFLIMIT_STATUS_COLOUR_ID))
			.leftJoin(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID))
			.leftJoin(company)
			.on(DSL.field("company.id", Integer.class).eq(job.COMPANYID))
			.leftJoin(payCurrency)
			.on(payCurrency.ID.eq(tsSetting.PAY_CURRENCY_ID))
			.leftJoin(billCurrency)
			.on(billCurrency.ID.eq(tsSetting.BILL_CURRENCY_ID))
			.leftJoin(timesheetInvoice)
			.on(timesheetInvoice.CST_TIMESHEET_ID.eq(ts.ID))
			.leftJoin(invoice)
			.on(DSL.field(INVOICE_ID_FIELD, Integer.class).eq(timesheetInvoice.INVOICE_ID))
			.where(tsSettingAssoc.JOB_ID.eq(jobId).and(tsSettingAssoc.CONTRACTOR_ID.eq(contractorId)));

		SelectLimitStep<?> finalQuery;

		if (searchRequestBodyDto.getSortPriorityList() != null
				&& !searchRequestBodyDto.getSortPriorityList().isEmpty()) {
			// Check if there are any valid (non-null, non-empty) sort fields
			boolean hasValidSortFields = searchRequestBodyDto.getSortPriorityList()
				.stream()
				.anyMatch((sort) -> sort.getField() != null && !sort.getField().trim().isEmpty());

			if (hasValidSortFields) {
				finalQuery = this.sortingQueryBuilder.addSortingQuery(baseQuery,
						this.expandSortTiebreakers(searchRequestBodyDto.getSortPriorityList()), ts);
			}
			else {
				// All sort fields are invalid, fall back to default sorting
				finalQuery = baseQuery.orderBy(ts.UPDATED_ON.desc(), ts.ID.desc());
			}
		}
		else {
			finalQuery = baseQuery.orderBy(ts.UPDATED_ON.desc(), ts.ID.desc());
		}

		// Apply pagination
		finalQuery = (SelectLimitStep<?>) finalQuery.offset(pageable.getPageNumber() * pageable.getPageSize())
			.limit(pageable.getPageSize());

		return this.auroraDbDSLContext.fetch(finalQuery).into(TimesheetJobAndContractorListQueryResultDto.class);
	}

	@Override
	public List<Integer> findContactIdsByEmail(String email, Integer accountId) {
		var contact = Tblcontact.TBLCONTACT;
		return this.auroraDbDSLContext.select(contact.ID)
			.from(contact)
			.where(contact.EMAIL.eq(email))
			.and(contact.ACCOUNTID.eq(accountId))
			.and(contact.DELETED.eq((byte) 0))
			.fetchInto(Integer.class);
	}

	@Override
	public List<TimesheetJobAndContractorListQueryResultDto> getTimesheetsListByEntityId(Integer entityType,
			Integer entityId, List<Integer> contactIds, Integer accountId, SearchRequestBodyDto searchRequestBodyDto,
			Pageable pageable) {

		// Aliases for tables
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var timesheetInvoice = CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T;
		final var payCurrency = Tblcurrency.TBLCURRENCY.as(PAY_CURRENCY_ALIAS);
		final var billCurrency = Tblcurrency.TBLCURRENCY.as(BILL_CURRENCY_ALIAS);
		var candidate = Tblcandidate.TBLCANDIDATE;
		var job = Tbljob.TBLJOB;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;
		var jobTimesheetAccess = CstJobTimesheetAccessT.CST_JOB_TIMESHEET_ACCESS_T;
		var sortTables = TimesheetListSortTables.create();

		var company = Tblcompany.TBLCOMPANY;
		var companyName = Tblcompany.TBLCOMPANY.COMPANYNAME.as(COMPANY_NAME_ALIAS);

		var invoice = DSL.table(INVOICE_TABLE_ALIAS).as(INVOICE_TABLE);
		final String invoiceIdPrefixField = INVOICE_ID_PREFIX_FIELD;
		final String invoiceIdNumberField = INVOICE_ID_NUMBER_FIELD;
		final String invoiceStatusIdField = INVOICE_STATUS_ID_FIELD;
		var invoiceNumber = DSL
			.when(DSL.field(invoiceIdPrefixField, String.class)
				.isNull()
				.or(DSL.field(invoiceIdPrefixField, String.class).eq("")),
					DSL.field(invoiceIdNumberField, String.class))
			.otherwise(DSL.concat(DSL.field(invoiceIdPrefixField, String.class), DSL.value("-"),
					DSL.field(invoiceIdNumberField, String.class)))
			.as(INVOICE_NUMBER_ALIAS);
		var invoiceCreatedOn = DSL.field(INVOICE_CREATED_ON_FIELD, Integer.class).as(INVOICE_CREATED_ON_ALIAS);
		var invoiceStatusId = DSL.field(invoiceStatusIdField, Integer.class).as(INVOICE_STATUS_ID_ALIAS);

		var reimbursement = CstTimesheetReimbursementT.CST_TIMESHEET_REIMBURSEMENT_T;

		Condition reimbursementCountCondition = reimbursement.CST_TIMESHEET_ID.eq(ts.ID)
			.and(reimbursement.ACCOUNT_ID.eq(accountId));

		/**
		 * For the client portal (contact viewer), the reimbursement count must reflect
		 * only expense claims shared with the client; unshared claims are invisible to
		 * them. Contractor/agency views count every claim. The jOOQ metamodel is stale
		 * for this column, so it is referenced by qualified name.
		 */
		if (UserTypeEnum.COMPANY_CONTACT.getId().equals(entityType)) {
			reimbursementCountCondition = reimbursementCountCondition
				.and(DSL.field(DSL.name(reimbursement.getName(), "is_shared_with_client"), Integer.class)
					.eq(BooleanFlagEnum.TRUE.getValue()));
		}

		var reimbursementCountField = DSL
			.field(DSL.selectCount().from(reimbursement).where(reimbursementCountCondition))
			.as(REIMBURSEMENT_COUNT_ALIAS);

		SelectOnConditionStep<?> baseQueryBuilder = DSL.select(
				// Timesheet fields
				ts.ID.as("id"), ts.TIMESHEET_SETTING_ID.as(TIMESHEET_SETTING_ID_ALIAS),
				ts.PERIOD_START.as(TIMESHEET_PERIOD_START_DATE_ALIAS),
				ts.PERIOD_END.as(TIMESHEET_PERIOD_END_DATE_ALIAS),

				// TimesheetSetting fields
				tsSetting.WORK_LOG_TYPE.as(WORK_LOG_TYPE_ALIAS),
				tsSetting.JOB_START_DATE.as(JOB_DURATION_START_DATE_ALIAS),
				tsSetting.JOB_END_DATE.as(JOB_DURATION_END_DATE_ALIAS),

				// Rates
				tsSetting.PAY_RATE.as(PAY_RATE_ALIAS), tsSetting.BILL_RATE.as(BILL_RATE_ALIAS),

				// Currency symbols
				payCurrency.SYMBOL.as(PAY_CURRENCY_SYMBOL_ALIAS), billCurrency.SYMBOL.as(BILL_CURRENCY_SYMBOL_ALIAS),

				// Currency codes
				payCurrency.CODE.as(PAY_CURRENCY_CODE_ALIAS), billCurrency.CODE.as(BILL_CURRENCY_CODE_ALIAS),

				// Total Pay/Bill data from Timesheet table
				ts.PAY_DATA.as(PAY_DATA_ALIAS), ts.BILL_DATA.as(BILL_DATA_ALIAS),

				// Total time columns from Timesheet table (seconds)
				ts.TOTAL_TIME.as(TOTAL_TIME_ALIAS), ts.TOTAL_WORK_TIME.as(TOTAL_WORK_TIME_ALIAS),
				ts.TOTAL_OVERTIME.as(TOTAL_OVERTIME_ALIAS),

				// Audit timestamps
				ts.ADDED_ON.as(ADDED_ON_ALIAS), ts.UPDATED_ON.as(UPDATED_ON_ALIAS),

				// Added/Updated by
				ts.ADDED_BY.as(ADDED_BY_ID_ALIAS), ts.UPDATED_BY.as(UPDATED_BY_ID_ALIAS),

				// Timesheet Invoice table fields
				timesheetInvoice.CST_TIMESHEET_PAY_STATUS_TYPE_ID.as(PAY_STATUS_ID_ALIAS),
				timesheetInvoice.PAYMENT_PAID_ON.as(PAYOUT_PAID_ON_ALIAS),
				timesheetInvoice.PAYOUT_NUMBER.as(PAYOUT_NUMBER_ALIAS),
				timesheetInvoice.PAYOUT_FILE.as(PAYOUT_FILE_ALIAS),
				timesheetInvoice.CST_TIMESHEET_BILL_STATUS_TYPE_ID.as(BILL_STATUS_ID_ALIAS),

				// Invoice table fields
				invoiceNumber, invoiceCreatedOn, invoiceStatusId,

				// Contractor Serial Number
				candidate.SRNO.as(SERIAL_NUMBER_ALIAS),

				// User types
				ts.ADDED_BY_USER_TYPE_ID.as(ADDED_BY_USER_TYPE_ID_ALIAS),
				ts.UPDATED_BY_USER_TYPE_ID.as(UPDATED_BY_USER_TYPE_ID_ALIAS),

				// Job Timesheet Access Control
				jobTimesheetAccess.CAN_CREATE.as(CAN_CREATE_ALIAS), jobTimesheetAccess.CAN_EDIT.as(CAN_EDIT_ALIAS),
				jobTimesheetAccess.CAN_DELETE.as(CAN_DELETE_ALIAS),

				// Contractor fields
				candidate.ID.as(CONTRACTOR_ID_ALIAS),
				DSL.when(candidate.LASTNAME.isNull().or(candidate.LASTNAME.eq("")), candidate.FIRSTNAME)
					.otherwise(DSL.concat(candidate.FIRSTNAME, DSL.val(" "), candidate.LASTNAME))
					.as(CONTRACTOR_NAME_ALIAS),
				candidate.PROFILEPIC.as(CONTRACTOR_PHOTO_ALIAS), candidate.SLUG.as(CONTRACTOR_SLUG_ALIAS),
				candidate.POSITION.as(CONTRACTOR_POSITION_ALIAS), candidate.OWNERID.as(CONTRACTOR_OWNER_ID_ALIAS),

				// Job fields
				job.ID.as(JOB_ID_ALIAS), job.NAME.as(JOB_NAME_ALIAS), job.SLUG.as(JOB_SLUG_ALIAS), companyName,
				company.LOGO.as(COMPANY_LOGO_ALIAS), buildTimesheetStatusIdSortField(ts, sortTables.tsStatusForSort()),
				buildApprovedBySortField(ts, sortTables), ts.ID.as(TIMESHEET_ID_ALIAS), buildPayStatusSortField(),
				buildBillStatusSortField(), buildExpenseClaimSortField(),
				buildAgencyRecruiterDisplayNameSortField(sortTables.addedByUserForSort(), ts.ADDED_BY,
						ts.ADDED_BY_USER_TYPE_ID, ADDED_BY_ALIAS),
				buildAgencyRecruiterDisplayNameSortField(sortTables.updatedByUserForSort(), ts.UPDATED_BY,
						ts.UPDATED_BY_USER_TYPE_ID, UPDATED_BY_ALIAS),
				reimbursementCountField,
				tsSetting.IS_REIMBURSEMENT_ENABLED.cast(Integer.class).as(IS_REIMBURSEMENT_ENABLED_ALIAS))
			.from(ts)
			.leftJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.leftJoin(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID));

		// Conditionally add assignJobCandidate join only for Contractor (entityType 3)
		// For Contact/Client (entityType 1), getJobContractorPairsByContactId already
		// filters by assignments, so the join is redundant
		SelectOnConditionStep<?> queryWithAssignJoin = (entityType == 3) ? baseQueryBuilder.join(assignJobCandidate)
			.on(assignJobCandidate.JOBID.eq(job.ID)
				.and(assignJobCandidate.CANDIDATEID.eq(tsSettingAssoc.CONTRACTOR_ID))) : baseQueryBuilder;

		SelectConditionStep<?> finalBaseQuery = queryWithAssignJoin.leftJoin(company)
			.on(company.ID.eq(job.COMPANYID))
			.leftJoin(jobTimesheetAccess)
			.on(jobTimesheetAccess.JOB_ID.cast(Integer.class).eq(job.ID))
			.leftJoin(payCurrency)
			.on(payCurrency.ID.eq(tsSetting.PAY_CURRENCY_ID))
			.leftJoin(billCurrency)
			.on(billCurrency.ID.eq(tsSetting.BILL_CURRENCY_ID))
			.leftJoin(timesheetInvoice)
			.on(timesheetInvoice.CST_TIMESHEET_ID.eq(ts.ID))
			.leftJoin(invoice)
			.on(DSL.field(INVOICE_ID_FIELD, Integer.class).eq(timesheetInvoice.INVOICE_ID))
			.where(buildEntityCondition(entityType, entityId, contactIds, accountId, tsSettingAssoc, tsSetting, job));

		// Apply timesheet period filter if provided
		SelectConditionStep<?> queryWithFilters = finalBaseQuery;
		if (searchRequestBodyDto.getTimesheetPeriodRequestBodyDto() != null) {
			Integer startDate = searchRequestBodyDto.getTimesheetPeriodRequestBodyDto().getStartDate();
			Integer endDate = searchRequestBodyDto.getTimesheetPeriodRequestBodyDto().getEndDate();

			if (startDate != null && endDate != null) {
				/**
				 * Filter timesheets where the requested date range overlaps with the
				 * timesheet period. A timesheet matches if: - The timesheet period_start
				 * falls within the requested range (startDate <= period_start <= endDate)
				 * OR - The timesheet period_end falls within the requested range
				 * (startDate <= period_end <= endDate) OR - The timesheet period fully
				 * contains the requested range (period_start <= startDate AND period_end
				 * >= endDate)
				 *
				 * OPTIMIZATION: Using between() for more readable SQL and better query
				 * plan
				 */
				Condition periodOverlap = ts.PERIOD_START.between(startDate, endDate)
					.or(ts.PERIOD_END.between(startDate, endDate))
					.or(ts.PERIOD_START.le(startDate).and(ts.PERIOD_END.ge(endDate)));

				queryWithFilters = queryWithFilters.and(periodOverlap);
			}
		}

		// Apply timesheetIds filter if provided
		if (searchRequestBodyDto.getTimesheetIds() != null && !searchRequestBodyDto.getTimesheetIds().isEmpty()) {
			queryWithFilters = queryWithFilters.and(ts.ID.in(searchRequestBodyDto.getTimesheetIds()));
		}

		/**
		 * Apply isSubmitted filter: entity must be a configured approver AND the
		 * timesheet's latest approval status must be submitted (status type id = 2).
		 */
		if (Boolean.TRUE.equals(searchRequestBodyDto.getIsSubmitted())) {
			var tsApprover = CstTimesheetApproverT.CST_TIMESHEET_APPROVER_T;
			var tsApproval = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T;

			queryWithFilters = queryWithFilters
				.and(ts.TIMESHEET_SETTING_ID.in(DSL.select(tsApprover.TIMESHEET_SETTING_ID)
					.from(tsApprover)
					.where(tsApprover.ENTITY_ID.eq(entityId))))
				.and(DSL.exists(DSL.selectOne()
					.from(tsApproval)
					.where(tsApproval.TIMESHEET_ID.eq(ts.ID))
					.and(tsApproval.ID.eq(DSL.select(DSL.max(tsApproval.ID))
						.from(tsApproval)
						.where(tsApproval.TIMESHEET_ID.eq(ts.ID))))
					.and(tsApproval.TIMESHEET_APPROVAL_STATUS_TYPE_ID.eq(2))));
		}

		if (Boolean.TRUE.equals(searchRequestBodyDto.getIsReimbursement())) {
			var reimbursementFilter = CstTimesheetReimbursementT.CST_TIMESHEET_REIMBURSEMENT_T;
			queryWithFilters = queryWithFilters
				.and(this.buildPendingReimbursementExistsCondition(ts, reimbursementFilter, accountId, entityType));
		}

		// Create field-to-table mapping for multi-table sorting
		Map<String, Table<?>> fieldTableMapping = this.createFieldTableMapping(ts, tsSetting, job, company, candidate);

		SelectLimitStep<?> finalQuery;

		if (searchRequestBodyDto.getSortPriorityList() != null
				&& !searchRequestBodyDto.getSortPriorityList().isEmpty()) {
			// Check if there are any valid (non-null, non-empty) sort fields
			boolean hasValidSortFields = searchRequestBodyDto.getSortPriorityList()
				.stream()
				.anyMatch((sort) -> sort.getField() != null && !sort.getField().trim().isEmpty());

			if (hasValidSortFields) {
				finalQuery = this.sortingQueryBuilder.addSortingQuery(queryWithFilters,
						this.expandSortTiebreakers(searchRequestBodyDto.getSortPriorityList()), fieldTableMapping, ts);
			}
			else {
				// All sort fields are invalid, fall back to default sorting
				finalQuery = queryWithFilters.orderBy(ts.UPDATED_ON.desc(), ts.ID.desc());
			}
		}
		else {
			finalQuery = queryWithFilters.orderBy(ts.UPDATED_ON.desc(), ts.ID.desc());
		}

		// Apply pagination
		finalQuery = (SelectLimitStep<?>) finalQuery.offset(pageable.getPageNumber() * pageable.getPageSize())
			.limit(pageable.getPageSize());

		return this.auroraDbDSLContext.fetch(finalQuery).into(TimesheetJobAndContractorListQueryResultDto.class);
	}

	/**
	 * Maps a primary sort field to the secondary tiebreaker field that must follow it in
	 * the same direction. The timesheet-period and job-duration columns are rendered from
	 * a (start date, end date) pair, so sorting on the start date must fall back to the
	 * end date when two rows share the same start date.
	 */
	private static final Map<String, String> SORT_TIEBREAKERS = Map.of(TIMESHEET_PERIOD_START_DATE_ALIAS,
			TIMESHEET_PERIOD_END_DATE_ALIAS, JOB_DURATION_START_DATE_ALIAS, JOB_DURATION_END_DATE_ALIAS);

	/**
	 * Expand a requested sort-priority list so the (start date, end date) pair columns
	 * fall back to their end date when two rows share the same start date. For every
	 * entry whose field has a {@link #SORT_TIEBREAKERS} mapping, the matching end-date
	 * entry is inserted immediately after it in the same direction. The original list is
	 * left untouched. If the end-date alias is not part of the query projection,
	 * {@link SortingQueryBuilder} silently drops it from the ORDER BY.
	 * @param sortPriorityList the sort priority list requested by the caller
	 * @return a new list with end-date tiebreakers inserted after their start-date
	 * entries
	 */
	private List<SortPriorityRequestBodyDto> expandSortTiebreakers(List<SortPriorityRequestBodyDto> sortPriorityList) {
		List<SortPriorityRequestBodyDto> expanded = new ArrayList<>();
		for (SortPriorityRequestBodyDto sort : sortPriorityList) {
			expanded.add(sort);
			String tiebreakerField = SORT_TIEBREAKERS.get(sort.getField());
			if (tiebreakerField != null) {
				expanded.add(new SortPriorityRequestBodyDto(tiebreakerField, sort.getOrder()));
			}
		}
		return expanded;
	}

	/**
	 * Create a mapping of field names to their corresponding JOOQ table references. This
	 * mapping is used for multi-table sorting to ensure each field is properly qualified
	 * with its table reference in SQL queries.
	 * @param ts Timesheet table reference
	 * @param tsSetting TimesheetSetting table reference
	 * @param job Job table reference
	 * @param company Company table reference
	 * @return Map of field names to their corresponding table references
	 */
	private Map<String, Table<?>> createFieldTableMapping(CstTimesheetT ts, CstTimesheetSettingT tsSetting, Tbljob job,
			org.jooq.Table<?> company, Table<?> candidate) {
		Map<String, Table<?>> fieldTableMapping = new HashMap<>();

		// Timesheet table fields
		fieldTableMapping.put("period_start", ts);
		fieldTableMapping.put("added_on", ts);
		fieldTableMapping.put("updated_on", ts);

		// TimesheetSetting table fields
		fieldTableMapping.put("pay_rate", tsSetting);
		fieldTableMapping.put("job_start_date", tsSetting);

		fieldTableMapping.put("firstname", candidate);
		// Job table fields
		fieldTableMapping.put("name", job);

		// Company table fields
		fieldTableMapping.put("companyname", company);

		return fieldTableMapping;
	}

	private ContractorOffLimitTables buildContractorOffLimitTables() {
		return new ContractorOffLimitTables(EntityOffLimitT.ENTITY_OFF_LIMIT_T.as(CONTRACTOR_ENTITY_OFF_LIMIT_T_ALIAS),
				OffLimitStatusT.OFF_LIMIT_STATUS_T.as(CONTRACTOR_OFF_LIMIT_STATUS_T_ALIAS),
				OffLimitStatusColourT.OFF_LIMIT_STATUS_COLOUR_T.as(CONTRACTOR_OFF_LIMIT_STATUS_COLOUR_T_ALIAS));
	}

	/** Latest-approval status id that means the timesheet is approved. */
	private static final int APPROVED_TIMESHEET_STATUS_ID = 4;

	/** Default latest-approval status id (OPEN) when a timesheet has no approval row. */
	private static final int OPEN_TIMESHEET_STATUS_ID = 1;

	/**
	 * Latest timesheet approval-status id for the current timesheet row, mirroring the
	 * {@code timesheetStatusId} projection
	 * ({@code COALESCE(latest approval status, OPEN)}). Pay/Bill status are shown in the
	 * grid only once a timesheet is approved; before then the cell renders "Not
	 * available" regardless of the stored invoice status, so the sort keys below reuse
	 * this to keep their order consistent with what the user sees. A distinct table alias
	 * is passed in to avoid colliding with the query's own approval sub-selects.
	 */
	private static Field<Integer> latestApprovalStatusForSort(String alias) {
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var approval = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T.as(alias);
		return DSL.coalesce(DSL.field(DSL.select(approval.TIMESHEET_APPROVAL_STATUS_TYPE_ID)
			.from(approval)
			.where(approval.TIMESHEET_ID.eq(ts.ID))
			.orderBy(approval.ID.desc())
			.limit(1)), DSL.val(OPEN_TIMESHEET_STATUS_ID));
	}

	/**
	 * Sort key for the "Pay Status" column. The cell shows "Not available" until the
	 * timesheet is approved; once approved it shows the pay status, defaulting to UNPAID
	 * when no invoice row exists ({@link PaymentStatusEnum} PAID=1, UN_PAID=2). This
	 * emits a CASE rank so {@code ORDER BY "payStatus"} ascending produces the displayed
	 * order: not available (not approved) &rarr; unpaid &rarr; paid. Aliased "payStatus"
	 * (a sort key only, ignored by the DTO mapping).
	 */
	static Field<Integer> buildPayStatusSortField() {
		var payStatus = CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T.CST_TIMESHEET_PAY_STATUS_TYPE_ID;
		var approvalStatus = latestApprovalStatusForSort("payStatusApprovalForSort");
		return DSL.when(approvalStatus.ne(APPROVED_TIMESHEET_STATUS_ID), DSL.inline(0))
			.when(payStatus.isNull(), DSL.inline(1))
			.when(payStatus.eq(PaymentStatusEnum.UN_PAID.getId()), DSL.inline(1))
			.when(payStatus.eq(PaymentStatusEnum.PAID.getId()), DSL.inline(2))
			.otherwise(DSL.inline(3))
			.as("payStatus");
	}

	/**
	 * Sort key for the "Bill Status" column. The cell shows "Not available" until the
	 * timesheet is approved; once approved it shows the bill status, defaulting to
	 * UNBILLED when no invoice row exists ({@link BillStatusEnum} BILLED=1, UN_BILLED=2,
	 * COLLECTED=3). This emits a CASE rank so {@code ORDER BY "billStatus"} ascending
	 * produces the displayed order: not available (not approved) &rarr; unbilled &rarr;
	 * billed &rarr; collected. Aliased "billStatus" (a sort key only, ignored by the DTO
	 * mapping).
	 */
	static Field<Integer> buildBillStatusSortField() {
		var billStatus = CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T.CST_TIMESHEET_BILL_STATUS_TYPE_ID;
		var approvalStatus = latestApprovalStatusForSort("billStatusApprovalForSort");
		return DSL.when(approvalStatus.ne(APPROVED_TIMESHEET_STATUS_ID), DSL.inline(0))
			.when(billStatus.isNull(), DSL.inline(1))
			.when(billStatus.eq(BillStatusEnum.UN_BILLED.getId()), DSL.inline(1))
			.when(billStatus.eq(BillStatusEnum.BILLED.getId()), DSL.inline(2))
			.when(billStatus.eq(BillStatusEnum.COLLECTED.getId()), DSL.inline(3))
			.otherwise(DSL.inline(4))
			.as("billStatus");
	}

	/**
	 * Sort key for the "Expense Claim" column. The cell shows "Not available" when
	 * reimbursement is not enabled for the timesheet setting, otherwise the reimbursement
	 * count (0, 1, 2 &hellip;). There is no single stored column to sort on, so this
	 * emits a rank that places "not available" rows (rank -1) before any enabled row and
	 * orders the rest by their reimbursement count. {@code ORDER BY "expenseClaim"}
	 * ascending therefore yields: not available &rarr; fewest claims &rarr; most claims.
	 * Aliased "expenseClaim" (a sort key only, ignored by the DTO mapping).
	 */
	static Field<Integer> buildExpenseClaimSortField() {
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var reimbursement = CstTimesheetReimbursementT.CST_TIMESHEET_REIMBURSEMENT_T;
		Field<Integer> claimCount = DSL.field(DSL.selectCount()
			.from(reimbursement)
			.where(reimbursement.CST_TIMESHEET_ID.eq(ts.ID).and(reimbursement.ACCOUNT_ID.eq(tsSetting.ACCOUNT_ID))));
		return DSL.when(tsSetting.IS_REIMBURSEMENT_ENABLED.cast(Integer.class).eq(1), claimCount)
			.otherwise(DSL.inline(-1))
			.as("expenseClaim");
	}

	private static final String DC_FOR_NAME_ALIAS = "dcForName";

	private static final String DJ_FOR_NAME_ALIAS = "djForName";

	private static final String DEAL_FOR_NAME_ALIAS = "dealForName";

	/**
	 * Sort key for the deal name shown against a timesheet row. The grid renders the
	 * deal(s) returned by {@link #getDealsByTimesheetIds}, which scopes a deal to the
	 * timesheet's contractor (via deal-candidate), its job (via deal-job) and the
	 * account. This sort key MUST mirror that scope, otherwise rows order by a deal the
	 * grid never shows (e.g. a deal on a different job or account) and the column looks
	 * unsorted. We expose the first such deal by serial number as the scalar alias
	 * {@link #DEAL_NAME_ALIAS} (ignored by DTO mapping) so {@code ORDER BY "dealName"}
	 * can resolve it.
	 */
	private static Field<String> buildDealNameSortField(Tblcandidate candidate, Tbljob job,
			CstTimesheetSettingT tsSetting) {
		var dcForName = Tbldealcandidates.TBLDEALCANDIDATES.as(DC_FOR_NAME_ALIAS);
		var djForName = Tbldealjobs.TBLDEALJOBS.as(DJ_FOR_NAME_ALIAS);
		var dealForName = Tbldeals.TBLDEALS.as(DEAL_FOR_NAME_ALIAS);
		return DSL
			.field(DSL.select(dealForName.NAME)
				.from(dcForName)
				.innerJoin(djForName)
				.on(djForName.DEALID.eq(dcForName.DEALID).and(djForName.JOBID.eq(job.ID)))
				.innerJoin(dealForName)
				.on(dealForName.ID.eq(dcForName.DEALID))
				.where(dcForName.CANDIDATEID.eq(candidate.ID))
				.and(dealForName.ACCOUNTID.eq(tsSetting.ACCOUNT_ID))
				.orderBy(dcForName.ID.asc())
				.limit(1))
			.as(DEAL_NAME_ALIAS);
	}

	/**
	 * Sort key for latest-approval status enum id ({@link #OPEN_TIMESHEET_STATUS_ID}
	 * default). Aliased {@link #TIMESHEET_STATUS_ID_ALIAS} (ignored by DTO mapping).
	 */
	private static Field<Integer> buildTimesheetStatusIdSortField(CstTimesheetT ts,
			CstTimesheetApprovalT tsStatusForSort) {
		return DSL.coalesce(DSL.field(DSL.select(tsStatusForSort.TIMESHEET_APPROVAL_STATUS_TYPE_ID)
			.from(tsStatusForSort)
			.where(tsStatusForSort.TIMESHEET_ID.eq(ts.ID))
			.orderBy(tsStatusForSort.ID.desc())
			.limit(1)), DSL.val(OPEN_TIMESHEET_STATUS_ID)).as(TIMESHEET_STATUS_ID_ALIAS);
	}

	/**
	 * Sort key for latest-approval agency approver display name when status is
	 * {@link #APPROVED_TIMESHEET_STATUS_ID}. Aliased {@link #APPROVED_BY_ALIAS} (ignored
	 * by DTO mapping).
	 */
	private static Field<String> buildApprovedBySortField(CstTimesheetT ts, TimesheetListSortTables sortTables) {
		return DSL
			.field(DSL
				.select(DSL
					.when(sortTables.approverUserForSort().LASTNAME.isNull()
						.or(sortTables.approverUserForSort().LASTNAME.eq("")),
							sortTables.approverUserForSort().FIRSTNAME)
					.otherwise(DSL.concat(sortTables.approverUserForSort().FIRSTNAME, DSL.val(" "),
							sortTables.approverUserForSort().LASTNAME)))
				.from(sortTables.tsApprovedByForSort())
				.leftJoin(sortTables.approverUserForSort())
				.on(sortTables.approverUserForSort().ID.eq(sortTables.tsApprovedByForSort().ENTITY_ID)
					.and(sortTables.tsApprovedByForSort().USER_TYPE_ID.eq(UserTypeEnum.AGENCY_RECRUITER.getId())))
				.where(sortTables.tsApprovedByForSort().TIMESHEET_ID.eq(ts.ID)
					.and(sortTables.tsApprovedByForSort().TIMESHEET_APPROVAL_STATUS_TYPE_ID
						.eq(APPROVED_TIMESHEET_STATUS_ID))
					.and(sortTables.tsApprovedByForSort().ID.eq(DSL.select(DSL.max(sortTables.tsApprovedByLatest().ID))
						.from(sortTables.tsApprovedByLatest())
						.where(sortTables.tsApprovedByLatest().TIMESHEET_ID.eq(ts.ID)))))
				.limit(1))
			.as(APPROVED_BY_ALIAS);
	}

	/**
	 * Sort key for agency recruiter display name on addedBy/updatedBy columns. Aliased
	 * per caller (ignored by DTO mapping).
	 */
	private static Field<String> buildAgencyRecruiterDisplayNameSortField(Tbluser userForSort,
			Field<Integer> userIdField, Field<Integer> userTypeIdField, String alias) {
		return DSL.field(DSL
			.select(DSL.when(userForSort.LASTNAME.isNull().or(userForSort.LASTNAME.eq("")), userForSort.FIRSTNAME)
				.otherwise(DSL.concat(userForSort.FIRSTNAME, DSL.val(" "), userForSort.LASTNAME)))
			.from(userForSort)
			.where(userForSort.ID.eq(userIdField).and(userTypeIdField.eq(UserTypeEnum.AGENCY_RECRUITER.getId())))
			.limit(1)).as(alias);
	}

	/**
	 * Build WHERE condition based on entity type Entity type 3 = Contractor - filter by
	 * contractor ID, account ID, job conditions (ENABLE_VMS_LINK, JOB_TYPE), and ensure
	 * contractor is assigned to job via tblassignjobcandidate Entity type 1 =
	 * Contact/Client - filter by jobs where contactid = contactId AND enable_vms_link = 1
	 * AND accountId, using optimized row value expressions for better query performance
	 */
	private List<ContractorJobQueryResultDto> getJobContractorPairsByContactIds(List<Integer> contactIds,
			Integer accountId) {
		var job = Tbljob.TBLJOB;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var jobSecondaryContacts = JobSecondaryContactsT.JOB_SECONDARY_CONTACTS_T;

		return this.auroraDbDSLContext
			.select(tsSettingAssoc.CONTRACTOR_ID.as(CONTRACTOR_ID_ALIAS), tsSettingAssoc.JOB_ID.as(JOB_ID_ALIAS))
			.from(tsSettingAssoc)
			.join(tsSetting)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.join(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID))
			.leftJoin(jobSecondaryContacts)
			.on(jobSecondaryContacts.JOB_ID.eq(job.ID))
			.where(job.ACCOUNTID.eq(accountId))
			.and(job.AUTHID.isNotNull().and(job.AUTHID.ne("")))
			.and(job.JOB_TYPE.in(JOB_TYPE_CONTRACT_TO_PERMANENT, JOB_TYPE_CONTRACT))
			.and(job.CONTACTID.in(contactIds).or(jobSecondaryContacts.CONTACT_ID.in(contactIds)))
			.fetchInto(ContractorJobQueryResultDto.class);
	}

	private Condition buildEntityCondition(Integer entityType, Integer entityId, List<Integer> contactIds,
			Integer accountId, CstTimesheetSettingAssociationT tsSettingAssoc, CstTimesheetSettingT tsSetting,
			Tbljob job) {
		return switch (entityType) {
			case 3 -> tsSettingAssoc.CONTRACTOR_ID.eq(entityId)
				.and(tsSetting.ACCOUNT_ID.eq(accountId))
				.and(job.ACCOUNTID.eq(accountId))
				.and(job.JOB_TYPE.in(JOB_TYPE_CONTRACT_TO_PERMANENT, JOB_TYPE_CONTRACT));
			case 1 -> {
				// Contact/Client:
				// Get job-contractor pairs for this contact/client from jobs where:
				// - contactid = contactId (or email-resolved contactIds)
				// - enable_vms_link = 1
				// - job_type IN ('contracttopermanent', 'contract')

				// Step 1: Get job-contractor pairs — use email-based contactIds when
				// available, fall back to single entityId for backward compatibility
				List<ContractorJobQueryResultDto> jobContractorPairs = contactIds.isEmpty()
						? this.getJobContractorPairsByContactId(null, accountId)
						: this.getJobContractorPairsByContactIds(contactIds, accountId);

				if (jobContractorPairs == null || jobContractorPairs.isEmpty()) {
					// If no pairs found, return false condition (no results)
					yield DSL.falseCondition();
				}

				// Step 2: Build optimized condition using row value expressions
				// OPTIMIZATION: Use (job_id, contractor_id) IN ((val1, val2), (val3,
				// val4), ...)
				// This is more efficient than multiple OR conditions as it:
				// - Removes the "false OR" prefix from the generated SQL
				// - Uses a single IN clause instead of multiple OR conditions
				// - Allows better index utilization on composite (job_id, contractor_id)
				// indexes
				// - Simplifies the query execution plan for the database optimizer
				Condition rowCondition = DSL.row(tsSettingAssoc.JOB_ID, tsSettingAssoc.CONTRACTOR_ID)
					.in(jobContractorPairs.stream()
						.map((pair) -> DSL.row(pair.getJobId(), pair.getContractorId()))
						.toList());

				// Step 3: Combine with account ID check for multi-tenant isolation
				yield rowCondition.and(tsSetting.ACCOUNT_ID.eq(accountId));
			}
			default -> DSL.falseCondition();
		};
	}

	@Override
	public Boolean validateTimesheetsExist(List<Integer> timeLogDates, Integer accountId, Integer jobId,
			List<Integer> contractorIds) {
		// Step 1: Fetch count of TimeLog entries based on Timesheet and TimesheetSetting
		// conditions
		Long count = this.entityManager
			.createQuery("SELECT COUNT(tl.id) " + "FROM TimeLog tl " + "JOIN tl.timesheet t "
					+ "JOIN t.timesheetSetting ts " + "JOIN ts.association a " + "WHERE a.jobId = :jobId "
					+ "AND a.contractorId IN :contractorIds " + "AND ts.accountId = :accountId "
					+ "AND tl.date IN :dates", Long.class)
			.setParameter(RepositoryParameterConstants.JOB_ID, jobId)
			.setParameter("contractorIds", contractorIds)
			.setParameter(RepositoryParameterConstants.ACCOUNT_ID, accountId)
			.setParameter("dates", timeLogDates)
			.getSingleResult();

		// Step 2: Convert to boolean based on count
		return count > 0;
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateTimesheetLastModified(Integer timesheetId, Integer userId, Integer userTypeId,
			Integer currentTimestamp) {
		this.entityManager.createQuery(
				"UPDATE Timesheet t SET t.updatedBy = :userId, t.updatedByUserTypeId = :userTypeId, t.updatedOn = :currentTimestamp WHERE t.id = :timesheetId")
			.setParameter(RepositoryParameterConstants.TIMESHEET_ID, timesheetId)
			.setParameter("userId", userId)
			.setParameter("userTypeId", userTypeId)
			.setParameter("currentTimestamp", currentTimestamp)
			.executeUpdate();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateTimesheetTimeDetails(Integer timesheetId, Integer totalTime, Integer totalWorkTime) {
		this.entityManager
			.createNativeQuery("UPDATE cst_timesheet_t SET total_time = :totalTime, total_work_time = :totalWorkTime"
					+ " WHERE id = :timesheetId")
			.setParameter(RepositoryParameterConstants.TIMESHEET_ID, timesheetId)
			.setParameter(TOTAL_TIME_ALIAS, totalTime)
			.setParameter(TOTAL_WORK_TIME_ALIAS, totalWorkTime)
			.executeUpdate();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void batchUpdateTimesheetLastModifiedWithTimeDetails(List<Integer> timesheetIds, Integer userId,
			Integer userTypeId, Integer currentTimestamp, List<TimeDetailSummaryDto> timeDetails) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return;
		}

		Map<Integer, TimeDetailSummaryDto> timeDetailsByTimesheetId = new HashMap<>();
		prepareTimeDetailsByTimesheetId(timeDetails, timeDetailsByTimesheetId);

		boolean hasTimeDetails = !timeDetailsByTimesheetId.isEmpty();

		StringBuilder sql = new StringBuilder(
				"UPDATE cst_timesheet_t SET updated_by = :userId, updated_by_user_type_id = :userTypeId, "
						+ "updated_on = :currentTimestamp");

		prepareSqlQueryForBatchUpdateTimesheetLastModifiedWithTimeDetails(timesheetIds, timeDetailsByTimesheetId,
				hasTimeDetails, sql);

		jakarta.persistence.Query query = this.entityManager.createNativeQuery(sql.toString());
		query.setParameter("userId", userId);
		query.setParameter("userTypeId", userTypeId);
		query.setParameter("currentTimestamp", currentTimestamp);

		for (int i = 0; i < timesheetIds.size(); i++) {
			Integer tsId = timesheetIds.get(i);
			query.setParameter("tsId" + i, tsId);
			if (hasTimeDetails && timeDetailsByTimesheetId.containsKey(tsId)) {
				TimeDetailSummaryDto d = timeDetailsByTimesheetId.get(tsId);
				query.setParameter(TOTAL_TIME_ALIAS + i, d.getTotalTime());
				query.setParameter(TOTAL_WORK_TIME_ALIAS + i, d.getTotalWorkTime());
			}
		}

		query.executeUpdate();
	}

	private void prepareSqlQueryForBatchUpdateTimesheetLastModifiedWithTimeDetails(List<Integer> timesheetIds,
			Map<Integer, TimeDetailSummaryDto> timeDetailsByTimesheetId, boolean hasTimeDetails, StringBuilder sql) {
		if (hasTimeDetails) {
			sql.append(", total_time = CASE id ");
			for (int i = 0; i < timesheetIds.size(); i++) {
				Integer tsId = timesheetIds.get(i);
				if (timeDetailsByTimesheetId.containsKey(tsId)) {
					sql.append(WHEN_TSID_PARAM_PREFIX)
						.append(i)
						.append(SQL_THEN_PARAM_PREFIX)
						.append(TOTAL_TIME_ALIAS)
						.append(i)
						.append(" ");
				}
			}
			sql.append("ELSE total_time END");

			sql.append(", total_work_time = CASE id ");
			for (int i = 0; i < timesheetIds.size(); i++) {
				Integer tsId = timesheetIds.get(i);
				if (timeDetailsByTimesheetId.containsKey(tsId)) {
					sql.append(WHEN_TSID_PARAM_PREFIX)
						.append(i)
						.append(SQL_THEN_PARAM_PREFIX)
						.append(TOTAL_WORK_TIME_ALIAS)
						.append(i)
						.append(" ");
				}
			}
			sql.append("ELSE total_work_time END");
		}

		sql.append(" WHERE id IN (");
		for (int i = 0; i < timesheetIds.size(); i++) {
			if (i > 0) {
				sql.append(", ");
			}
			sql.append(":tsId").append(i);
		}
		sql.append(")");
	}

	private void prepareTimeDetailsByTimesheetId(List<TimeDetailSummaryDto> timeDetails,
			Map<Integer, TimeDetailSummaryDto> timeDetailsByTimesheetId) {
		if (timeDetails != null) {
			for (TimeDetailSummaryDto d : timeDetails) {
				if (d != null && d.getTimesheetId() != null) {
					timeDetailsByTimesheetId.put(d.getTimesheetId(), d);
				}
			}
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Boolean validateIsApprover(Integer timesheetId, Integer userId, Integer userTypeId) {
		Long count = this.entityManager.createQuery("""
				SELECT COUNT(timesheetApprover.id) FROM TimesheetApprover AS timesheetApprover
				JOIN TimesheetSetting AS timesheetSetting ON timesheetSetting.id = timesheetApprover.timesheetSettingId
				JOIN Timesheet AS timesheet ON timesheet.timesheetSettingId = timesheetSetting.id
				WHERE timesheetApprover.entityId=:userId
				AND timesheetApprover.userTypeId=:userTypeId
				AND timesheet.id=:timesheetId
				""", Long.class)
			.setParameter(RepositoryParameterConstants.USER_ID, userId)
			.setParameter(RepositoryParameterConstants.USER_TYPE_ID, userTypeId)
			.setParameter(RepositoryParameterConstants.TIMESHEET_ID, timesheetId)
			.getSingleResult();

		return count > 0;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Candidate getCandidateLinkedToTimesheet(Integer timesheetId, Integer accountId) {
		List<Candidate> results = this.entityManager
			.createQuery(
					"SELECT c FROM Candidate c " + "JOIN TimesheetSettingAssociation tsa ON tsa.contractorId = c.id "
							+ "JOIN TimesheetSetting ts ON ts.association.id = tsa.id "
							+ "JOIN Timesheet t ON t.timesheetSettingId = ts.id "
							+ "WHERE t.id = :timesheetId AND ts.accountId = :accountId",
					Candidate.class)
			.setParameter(RepositoryParameterConstants.ACCOUNT_ID, accountId)
			.setParameter(RepositoryParameterConstants.TIMESHEET_ID, timesheetId)
			.getResultList();

		if (results.isEmpty()) {
			return null;
		}

		return results.getFirst();
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public Job getJobLinkedToTimesheet(Integer timesheetId, Integer accountId) {
		List<Job> results = this.entityManager
			.createQuery("SELECT j FROM Job j " + "JOIN TimesheetSettingAssociation tsa ON tsa.jobId = j.id "
					+ "JOIN TimesheetSetting ts ON ts.association.id = tsa.id "
					+ "JOIN Timesheet t ON t.timesheetSettingId = ts.id "
					+ "WHERE t.id = :timesheetId AND ts.accountId = :accountId", Job.class)
			.setParameter(RepositoryParameterConstants.ACCOUNT_ID, accountId)
			.setParameter(RepositoryParameterConstants.TIMESHEET_ID, timesheetId)
			.getResultList();

		if (results.isEmpty()) {
			return null;
		}

		return results.getFirst();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Integer getCompanyIdLinkedToTimesheet(Integer timesheetId, Integer accountId) {
		List<Integer> results = this.entityManager
			.createQuery("SELECT tsa.companyId FROM TimesheetSettingAssociation tsa "
					+ "JOIN TimesheetSetting ts ON ts.association.id = tsa.id "
					+ "JOIN Timesheet t ON t.timesheetSettingId = ts.id "
					+ "WHERE t.id = :timesheetId AND ts.accountId = :accountId", Integer.class)
			.setParameter(RepositoryParameterConstants.ACCOUNT_ID, accountId)
			.setParameter(RepositoryParameterConstants.TIMESHEET_ID, timesheetId)
			.getResultList();

		if (results.isEmpty()) {
			return null;
		}

		return results.getFirst();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<TimesheetPermissionDataDto> getTimesheetPermissionDataBulk(List<Integer> timesheetIds,
			Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return List.of();
		}

		String jpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPermissionDataDto("
				+ "t.id, " + "t.timesheetSettingId, " + "tsa.contractorId, " + "tsa.jobId, " + "c, " + "j) "
				+ "FROM Timesheet t " + "JOIN TimesheetSetting ts ON t.timesheetSettingId = ts.id "
				+ "JOIN TimesheetSettingAssociation tsa ON ts.association.id = tsa.id "
				+ "JOIN Candidate c ON tsa.contractorId = c.id " + "JOIN Job j ON tsa.jobId = j.id "
				+ "WHERE t.id IN :timesheetIds AND ts.accountId = :accountId";

		return this.entityManager.createQuery(jpql, TimesheetPermissionDataDto.class)
			.setParameter("timesheetIds", timesheetIds)
			.setParameter(RepositoryParameterConstants.ACCOUNT_ID, accountId)
			.getResultList();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<TimesheetPermissionDataDto> getCandidatePermissionDataBulk(List<Integer> candidateIds,
			Integer accountId) {
		if (candidateIds == null || candidateIds.isEmpty()) {
			return List.of();
		}

		String jpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPermissionDataDto("
				+ "null, " + "null, " + "c.id, " + "null, " + "c, " + "null) " + "FROM Candidate c "
				+ "WHERE c.id IN :candidateIds AND c.accountId = :accountId";

		return this.entityManager.createQuery(jpql, TimesheetPermissionDataDto.class)
			.setParameter("candidateIds", candidateIds)
			.setParameter(RepositoryParameterConstants.ACCOUNT_ID, accountId)
			.getResultList();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<TimesheetPermissionDataDto> getJobPermissionDataBulk(List<Integer> jobIds, Integer accountId) {
		if (jobIds == null || jobIds.isEmpty()) {
			return List.of();
		}

		String jpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPermissionDataDto("
				+ "null, " + "null, " + "null, " + "j.id, " + "null, " + "j) " + "FROM Job j "
				+ "WHERE j.id IN :jobIds AND j.accountId = :accountId";

		return this.entityManager.createQuery(jpql, TimesheetPermissionDataDto.class)
			.setParameter("jobIds", jobIds)
			.setParameter(RepositoryParameterConstants.ACCOUNT_ID, accountId)
			.getResultList();
	}

	@Override
	public List<Integer> getJobIdsByContactId(Integer contactId, Integer accountId) {
		var job = Tbljob.TBLJOB;

		return this.auroraDbDSLContext.selectDistinct(job.ID)
			.from(job)
			.where(job.CONTACTID.eq(contactId)
				.and(job.ENABLE_VMS_LINK.eq((byte) 1))
				.and(job.ACCOUNTID.eq(accountId))
				.and(job.JOB_TYPE.in(JOB_TYPE_CONTRACT_TO_PERMANENT, JOB_TYPE_CONTRACT)))
			.fetchInto(Integer.class);
	}

	@Override
	public List<ContractorJobQueryResultDto> getJobContractorPairsByContactId(Integer contactId, Integer accountId) {
		var job = Tbljob.TBLJOB;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var jobSecondaryContacts = JobSecondaryContactsT.JOB_SECONDARY_CONTACTS_T;

		// Get pairs from timesheet associations (includes deleted candidates)
		return this.auroraDbDSLContext
			.select(tsSettingAssoc.CONTRACTOR_ID.as(CONTRACTOR_ID_ALIAS), tsSettingAssoc.JOB_ID.as(JOB_ID_ALIAS))
			.from(tsSettingAssoc)
			.join(tsSetting)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.join(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID))
			.leftJoin(jobSecondaryContacts)
			.on(jobSecondaryContacts.JOB_ID.eq(job.ID))
			.where(job.ACCOUNTID.eq(accountId))
			.and(job.AUTHID.isNotNull().and(job.AUTHID.ne("")))
			.and(job.JOB_TYPE.in(JOB_TYPE_CONTRACT_TO_PERMANENT, JOB_TYPE_CONTRACT))
			.and(job.CONTACTID.eq(contactId).or(jobSecondaryContacts.CONTACT_ID.eq(contactId)))
			.fetchInto(ContractorJobQueryResultDto.class);
	}

	@Override
	public Long getTimesheetsCountByEntityId(Integer entityType, Integer entityId, List<Integer> contactIds,
			Integer accountId) {
		// Aliases for tables
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var job = Tbljob.TBLJOB;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;

		// Build count query with only entity condition (no filters)
		var baseCountQuery = this.auroraDbDSLContext.selectCount()
			.from(ts)
			.leftJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID));

		// Conditionally add assignJobCandidate join only for Contractor (entityType 3)
		var countQueryWithJoin = (entityType == 3) ? baseCountQuery.join(assignJobCandidate)
			.on(assignJobCandidate.JOBID.eq(job.ID)
				.and(assignJobCandidate.CANDIDATEID.eq(tsSettingAssoc.CONTRACTOR_ID))) : baseCountQuery;

		Long count = countQueryWithJoin
			.where(this.buildEntityCondition(entityType, entityId, contactIds, accountId, tsSettingAssoc, tsSetting,
					job))
			.fetchOne(0, Long.class);

		return (count != null) ? count : 0L;
	}

	@Override
	public Long getTimesheetsCountByEntityIdWithFilters(Integer entityType, Integer entityId, List<Integer> contactIds,
			Integer accountId, SearchRequestBodyDto searchRequestBodyDto) {
		// Aliases for tables
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var job = Tbljob.TBLJOB;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;

		// Build base count query with entity condition
		var baseCountQueryBuilder = this.auroraDbDSLContext.selectCount()
			.from(ts)
			.leftJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID));

		// Conditionally add assignJobCandidate join only for Contractor (entityType 3)
		SelectConditionStep<Record1<Integer>> baseCountQuery = (entityType == 3)
				? baseCountQueryBuilder.join(assignJobCandidate)
					.on(assignJobCandidate.JOBID.eq(job.ID)
						.and(assignJobCandidate.CANDIDATEID.eq(tsSettingAssoc.CONTRACTOR_ID)))
					.where(this.buildEntityCondition(entityType, entityId, contactIds, accountId, tsSettingAssoc,
							tsSetting, job))
				: baseCountQueryBuilder.where(this.buildEntityCondition(entityType, entityId, contactIds, accountId,
						tsSettingAssoc, tsSetting, job));

		// Apply timesheet period filter if provided
		SelectConditionStep<Record1<Integer>> queryWithFilters = baseCountQuery;
		if (searchRequestBodyDto.getTimesheetPeriodRequestBodyDto() != null) {
			Integer startDate = searchRequestBodyDto.getTimesheetPeriodRequestBodyDto().getStartDate();
			Integer endDate = searchRequestBodyDto.getTimesheetPeriodRequestBodyDto().getEndDate();

			if (startDate != null && endDate != null) {
				/**
				 * Filter timesheets where the requested date range overlaps with the
				 * timesheet period. A timesheet matches if: - The timesheet period_start
				 * falls within the requested range (startDate <= period_start <= endDate)
				 * OR - The timesheet period_end falls within the requested range
				 * (startDate <= period_end <= endDate) OR - The timesheet period fully
				 * contains the requested range (period_start <= startDate AND period_end
				 * >= endDate)
				 *
				 * OPTIMIZATION: Using between() for more readable SQL and better query
				 * plan
				 */
				Condition periodOverlap = ts.PERIOD_START.between(startDate, endDate)
					.or(ts.PERIOD_END.between(startDate, endDate))
					.or(ts.PERIOD_START.le(startDate).and(ts.PERIOD_END.ge(endDate)));

				queryWithFilters = queryWithFilters.and(periodOverlap);
			}
		}

		// Apply timesheetIds filter if provided
		if (searchRequestBodyDto.getTimesheetIds() != null && !searchRequestBodyDto.getTimesheetIds().isEmpty()) {
			queryWithFilters = queryWithFilters.and(ts.ID.in(searchRequestBodyDto.getTimesheetIds()));
		}

		/**
		 * Apply isSubmitted filter: entity must be a configured approver AND the
		 * timesheet's latest approval status must be submitted (status type id = 2).
		 */
		if (Boolean.TRUE.equals(searchRequestBodyDto.getIsSubmitted())) {
			var tsApprover = CstTimesheetApproverT.CST_TIMESHEET_APPROVER_T;
			var tsApproval = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T;

			queryWithFilters = queryWithFilters
				.and(ts.TIMESHEET_SETTING_ID.in(DSL.select(tsApprover.TIMESHEET_SETTING_ID)
					.from(tsApprover)
					.where(tsApprover.ENTITY_ID.eq(entityId))))
				.and(DSL.exists(DSL.selectOne()
					.from(tsApproval)
					.where(tsApproval.TIMESHEET_ID.eq(ts.ID))
					.and(tsApproval.ID.eq(DSL.select(DSL.max(tsApproval.ID))
						.from(tsApproval)
						.where(tsApproval.TIMESHEET_ID.eq(ts.ID))))
					.and(tsApproval.TIMESHEET_APPROVAL_STATUS_TYPE_ID.eq(2))));
		}

		if (Boolean.TRUE.equals(searchRequestBodyDto.getIsReimbursement())) {
			var reimbursementFilter = CstTimesheetReimbursementT.CST_TIMESHEET_REIMBURSEMENT_T;
			queryWithFilters = queryWithFilters
				.and(this.buildPendingReimbursementExistsCondition(ts, reimbursementFilter, accountId, entityType));
		}

		Long count = queryWithFilters.fetchOne(0, Long.class);
		return (count != null) ? count : 0L;
	}

	@Override
	public List<TimesheetJobAndContractorListQueryResultDto> getTimesheetsListByIds(List<Integer> timesheetIds,
			List<SortPriorityRequestBodyDto> sortPriorityList, Pageable pageable) {

		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return List.of();
		}

		SelectLimitStep<?> finalQuery = this.buildTimesheetsSortedQuery(timesheetIds, null, null, sortPriorityList);

		return this.auroraDbDSLContext.fetch(finalQuery).into(TimesheetJobAndContractorListQueryResultDto.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getTimesheetIdsPage(CommonTableExpression<?> cte, String cteName,
			List<SortPriorityRequestBodyDto> sortPriorityList, Pageable pageable) {

		SelectLimitStep<?> finalQuery = this.buildTimesheetsSortedQuery(null, cte, cteName, sortPriorityList);

		Result<Record> result = (Result<Record>) this.auroraDbDSLContext
			.fetch(finalQuery.offset(pageable.getPageNumber() * pageable.getPageSize()).limit(pageable.getPageSize()));

		return result.stream().map((r) -> r.get("id", Integer.class)).toList();
	}

	/**
	 * Builds the full, sorted (but un-paginated) "timesheets" query shared by
	 * {@link #getTimesheetsListByIds} (hydration over an {@code IN (ids)} set) and
	 * {@link #getTimesheetIdsPage} (sorted page selection over the filtered CTE). Exactly
	 * one source is supplied: pass {@code timesheetIds} for the IN variant, or
	 * {@code cte} + {@code cteName} for the CTE variant (which additionally applies
	 * candidate access control). Sharing one field list + join graph guarantees both
	 * callers order by byte-for-byte identical sort keys. Assumes the supplied source is
	 * non-empty.
	 */
	private SelectLimitStep<?> buildTimesheetsSortedQuery(List<Integer> timesheetIds, CommonTableExpression<?> cte,
			String cteName, List<SortPriorityRequestBodyDto> sortPriorityList) {
		// Aliases for tables
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var timesheetInvoice = CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T;
		final var payCurrency = Tblcurrency.TBLCURRENCY.as(PAY_CURRENCY_ALIAS);
		final var billCurrency = Tblcurrency.TBLCURRENCY.as(BILL_CURRENCY_ALIAS);
		var candidate = Tblcandidate.TBLCANDIDATE;
		var job = Tbljob.TBLJOB;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;
		var jobTimesheetAccess = CstJobTimesheetAccessT.CST_JOB_TIMESHEET_ACCESS_T;
		var jobStatus = Tbljobstatus.TBLJOBSTATUS;
		var sortTables = TimesheetListSortTables.create();

		var company = Tblcompany.TBLCOMPANY;
		var companyName = Tblcompany.TBLCOMPANY.COMPANYNAME.as(COMPANY_NAME_ALIAS);

		// Off-limit tables for company
		var entityOffLimitT = EntityOffLimitT.ENTITY_OFF_LIMIT_T;
		var offLimitStatusT = OffLimitStatusT.OFF_LIMIT_STATUS_T;
		var offLimitStatusColourT = OffLimitStatusColourT.OFF_LIMIT_STATUS_COLOUR_T;
		var offLimitMarkedByUser = Tbluser.TBLUSER.as("offLimitMarkedByUser");

		// Off-limit tables for contractor (candidate)
		var contractorEntityOffLimitT = EntityOffLimitT.ENTITY_OFF_LIMIT_T.as(CONTRACTOR_ENTITY_OFF_LIMIT_T_ALIAS);
		var contractorOffLimitStatusT = OffLimitStatusT.OFF_LIMIT_STATUS_T.as(CONTRACTOR_OFF_LIMIT_STATUS_T_ALIAS);
		var contractorOffLimitStatusColourT = OffLimitStatusColourT.OFF_LIMIT_STATUS_COLOUR_T
			.as(CONTRACTOR_OFF_LIMIT_STATUS_COLOUR_T_ALIAS);

		var invoice = DSL.table(INVOICE_TABLE_ALIAS).as(INVOICE_TABLE);
		final String invoiceIdPrefixField = INVOICE_ID_PREFIX_FIELD;
		final String invoiceIdNumberField = INVOICE_ID_NUMBER_FIELD;
		final String invoiceStatusIdField = INVOICE_STATUS_ID_FIELD;
		var invoiceNumber = DSL
			.when(DSL.field(invoiceIdPrefixField, String.class)
				.isNull()
				.or(DSL.field(invoiceIdPrefixField, String.class).eq("")),
					DSL.field(invoiceIdNumberField, String.class))
			.otherwise(DSL.concat(DSL.field(invoiceIdPrefixField, String.class), DSL.value("-"),
					DSL.field(invoiceIdNumberField, String.class)))
			.as(INVOICE_NUMBER_ALIAS);
		var invoiceCreatedOn = DSL.field(INVOICE_CREATED_ON_FIELD, Integer.class).as(INVOICE_CREATED_ON_ALIAS);
		var invoiceStatusId = DSL.field(invoiceStatusIdField, Integer.class).as(INVOICE_STATUS_ID_ALIAS);

		var reimbursement = CstTimesheetReimbursementT.CST_TIMESHEET_REIMBURSEMENT_T;
		var reimbursementCountField = DSL
			.field(DSL.selectCount()
				.from(reimbursement)
				.where(reimbursement.CST_TIMESHEET_ID.eq(ts.ID).and(reimbursement.ACCOUNT_ID.eq(tsSetting.ACCOUNT_ID))))
			.as(REIMBURSEMENT_COUNT_ALIAS);

		List<SelectFieldOrAsterisk> selectFields = Arrays.asList(
				// Timesheet fields
				ts.ID.as("id"), ts.TIMESHEET_SETTING_ID.as(TIMESHEET_SETTING_ID_ALIAS),
				ts.PERIOD_START.as(TIMESHEET_PERIOD_START_DATE_ALIAS),
				ts.PERIOD_END.as(TIMESHEET_PERIOD_END_DATE_ALIAS),

				// TimesheetSetting fields
				tsSetting.WORK_LOG_TYPE.as(WORK_LOG_TYPE_ALIAS),
				tsSetting.JOB_START_DATE.as(JOB_DURATION_START_DATE_ALIAS),
				tsSetting.JOB_END_DATE.as(JOB_DURATION_END_DATE_ALIAS),

				// Rates
				tsSetting.PAY_RATE.as(PAY_RATE_ALIAS), tsSetting.BILL_RATE.as(BILL_RATE_ALIAS),

				// Currency symbols
				payCurrency.SYMBOL.as(PAY_CURRENCY_SYMBOL_ALIAS), billCurrency.SYMBOL.as(BILL_CURRENCY_SYMBOL_ALIAS),

				// Currency codes
				payCurrency.CODE.as(PAY_CURRENCY_CODE_ALIAS), billCurrency.CODE.as(BILL_CURRENCY_CODE_ALIAS),

				// Total Pay/Bill data from Timesheet table
				ts.PAY_DATA.as(PAY_DATA_ALIAS), ts.BILL_DATA.as(BILL_DATA_ALIAS),

				// Total time columns from Timesheet table (seconds)
				ts.TOTAL_TIME.as(TOTAL_TIME_ALIAS), ts.TOTAL_WORK_TIME.as(TOTAL_WORK_TIME_ALIAS),
				ts.TOTAL_OVERTIME.as(TOTAL_OVERTIME_ALIAS),

				// Audit timestamps
				ts.ADDED_ON.as(ADDED_ON_ALIAS), ts.UPDATED_ON.as(UPDATED_ON_ALIAS),

				// Added/Updated by
				ts.ADDED_BY.as(ADDED_BY_ID_ALIAS), ts.UPDATED_BY.as(UPDATED_BY_ID_ALIAS),

				// Timesheet Invoice table fields
				timesheetInvoice.CST_TIMESHEET_PAY_STATUS_TYPE_ID.as(PAY_STATUS_ID_ALIAS),
				timesheetInvoice.PAYMENT_PAID_ON.as(PAYOUT_PAID_ON_ALIAS),
				timesheetInvoice.PAYOUT_NUMBER.as(PAYOUT_NUMBER_ALIAS),
				timesheetInvoice.PAYOUT_FILE.as(PAYOUT_FILE_ALIAS),
				timesheetInvoice.CST_TIMESHEET_BILL_STATUS_TYPE_ID.as(BILL_STATUS_ID_ALIAS),

				// Invoice table fields
				invoiceNumber, invoiceCreatedOn, invoiceStatusId,

				// Contractor Serial Number
				candidate.SRNO.as(SERIAL_NUMBER_ALIAS),

				// User types
				ts.ADDED_BY_USER_TYPE_ID.as(ADDED_BY_USER_TYPE_ID_ALIAS),
				ts.UPDATED_BY_USER_TYPE_ID.as(UPDATED_BY_USER_TYPE_ID_ALIAS),

				// Job Timesheet Access Control
				jobTimesheetAccess.CAN_CREATE.as(CAN_CREATE_ALIAS), jobTimesheetAccess.CAN_EDIT.as(CAN_EDIT_ALIAS),
				jobTimesheetAccess.CAN_DELETE.as(CAN_DELETE_ALIAS),

				// Contractor fields
				candidate.ID.as(CONTRACTOR_ID_ALIAS),
				DSL.when(candidate.LASTNAME.isNull().or(candidate.LASTNAME.eq("")), candidate.FIRSTNAME)
					.otherwise(DSL.concat(candidate.FIRSTNAME, DSL.val(" "), candidate.LASTNAME))
					.as(CONTRACTOR_NAME_ALIAS),
				candidate.PROFILEPIC.as(CONTRACTOR_PHOTO_ALIAS), candidate.SLUG.as(CONTRACTOR_SLUG_ALIAS),
				candidate.POSITION.as(CONTRACTOR_POSITION_ALIAS), candidate.OWNERID.as(CONTRACTOR_OWNER_ID_ALIAS),

				// Job fields
				job.ID.as(JOB_ID_ALIAS), job.NAME.as(JOB_NAME_ALIAS), job.SLUG.as(JOB_SLUG_ALIAS), companyName,
				company.LOGO.as(COMPANY_LOGO_ALIAS), jobStatus.LABEL.as(JOB_STATUS_ALIAS),
				job.JOB_TYPE.as(JOB_TYPE_ALIAS),

				// Company off-limit fields
				entityOffLimitT.OFF_LIMIT_STATUS_ID.as("companyOffLimitStatusId"),
				entityOffLimitT.OFF_LIMIT_REASON.as("companyOffLimitReason"),
				entityOffLimitT.OFF_LIMIT_END_DATE.as("companyOffLimitEndDate"),
				entityOffLimitT.CREATED_ON.as("companyOffLimitStartDate"),
				offLimitStatusT.STATUS_LABEL.as("companyStatusLabel"),
				offLimitStatusColourT.BACKGROUND_COLOR_HEX.as("companyBackgroundColorHex"),
				offLimitStatusColourT.TEXT_COLOR_HEX.as("companyTextColorHex"),
				DSL.when(offLimitMarkedByUser.LASTNAME.isNull().or(offLimitMarkedByUser.LASTNAME.eq("")),
						offLimitMarkedByUser.FIRSTNAME)
					.otherwise(DSL.concat(offLimitMarkedByUser.FIRSTNAME, DSL.val(" "), offLimitMarkedByUser.LASTNAME))
					.as("companyMarkedByName"),

				// Contractor off-limit fields
				contractorEntityOffLimitT.OFF_LIMIT_STATUS_ID.as(CONTRACTOR_OFF_LIMIT_STATUS_ID_ALIAS),
				contractorOffLimitStatusT.STATUS_LABEL.as(CONTRACTOR_STATUS_LABEL_ALIAS),
				contractorOffLimitStatusColourT.BACKGROUND_COLOR_HEX.as(CONTRACTOR_BACKGROUND_COLOR_HEX_ALIAS),
				contractorOffLimitStatusColourT.TEXT_COLOR_HEX.as(CONTRACTOR_TEXT_COLOR_HEX_ALIAS),
				assignJobCandidate.ID.as(CONTRACTOR_ASSIGNMENT_ID_ALIAS), company.SLUG.as("companySlug"),
				buildDealNameSortField(candidate, job, tsSetting),
				buildTimesheetStatusIdSortField(ts, sortTables.tsStatusForSort()),
				buildApprovedBySortField(ts, sortTables), ts.ID.as(TIMESHEET_ID_ALIAS), buildPayStatusSortField(),
				buildBillStatusSortField(), buildExpenseClaimSortField(),
				buildAgencyRecruiterDisplayNameSortField(sortTables.addedByUserForSort(), ts.ADDED_BY,
						ts.ADDED_BY_USER_TYPE_ID, ADDED_BY_ALIAS),
				buildAgencyRecruiterDisplayNameSortField(sortTables.updatedByUserForSort(), ts.UPDATED_BY,
						ts.UPDATED_BY_USER_TYPE_ID, UPDATED_BY_ALIAS),
				reimbursementCountField,
				tsSetting.IS_REIMBURSEMENT_ENABLED.cast(Integer.class).as(IS_REIMBURSEMENT_ENABLED_ALIAS));

		boolean fromCte = cte != null;
		Field<Integer> cteIdField = fromCte ? DSL.field(DSL.name(cteName, "id"), Integer.class) : null;

		SelectSelectStep<Record> selectStep = fromCte ? DSL.with(cte).select(selectFields) : DSL.select(selectFields);
		SelectJoinStep<Record> fromStep = fromCte
				? selectStep.from(DSL.name(cteName)).innerJoin(ts).on(ts.ID.eq(cteIdField)) : selectStep.from(ts);

		SelectConditionStep<?> baseQuery = fromStep.leftJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.leftJoin(contractorEntityOffLimitT)
			.on(contractorEntityOffLimitT.ENTITY_ID.eq(candidate.ID)
				.and(contractorEntityOffLimitT.ENTITY_TYPE.eq(EntityTypeEnum.CANDIDATE.getId())))
			.leftJoin(contractorOffLimitStatusT)
			.on(contractorEntityOffLimitT.OFF_LIMIT_STATUS_ID.eq(contractorOffLimitStatusT.ID))
			.leftJoin(contractorOffLimitStatusColourT)
			.on(contractorOffLimitStatusT.STATUS_COLOUR_ID
				.eq(contractorOffLimitStatusColourT.OFFLIMIT_STATUS_COLOUR_ID))
			.leftJoin(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID))
			.leftJoin(jobStatus)
			.on(job.JOBSTATUS.eq(jobStatus.ID))
			.leftJoin(assignJobCandidate)
			.on(assignJobCandidate.JOBID.eq(job.ID).and(assignJobCandidate.CANDIDATEID.eq(candidate.ID)))
			.leftJoin(company)
			.on(company.ID.eq(job.COMPANYID))
			.leftJoin(entityOffLimitT)
			.on(entityOffLimitT.ENTITY_ID.eq(company.ID)
				.and(entityOffLimitT.ENTITY_TYPE.eq(EntityTypeEnum.COMPANY.getId())))
			.leftJoin(offLimitStatusT)
			.on(entityOffLimitT.OFF_LIMIT_STATUS_ID.eq(offLimitStatusT.ID))
			.leftJoin(offLimitStatusColourT)
			.on(offLimitStatusT.STATUS_COLOUR_ID.eq(offLimitStatusColourT.OFFLIMIT_STATUS_COLOUR_ID))
			.leftJoin(offLimitMarkedByUser)
			.on(offLimitMarkedByUser.ID.eq(entityOffLimitT.CREATED_BY))
			.leftJoin(jobTimesheetAccess)
			.on(jobTimesheetAccess.JOB_ID.cast(Integer.class).eq(job.ID))
			.leftJoin(payCurrency)
			.on(payCurrency.ID.eq(tsSetting.PAY_CURRENCY_ID))
			.leftJoin(billCurrency)
			.on(billCurrency.ID.eq(tsSetting.BILL_CURRENCY_ID))
			.leftJoin(timesheetInvoice)
			.on(timesheetInvoice.CST_TIMESHEET_ID.eq(ts.ID)
				.and(timesheetInvoice.ID.eq(DSL.select(DSL.max(CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T.ID))
					.from(CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T)
					.where(CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T.CST_TIMESHEET_ID.eq(ts.ID)))))
			.leftJoin(invoice)
			.on(DSL.field(INVOICE_ID_FIELD, Integer.class).eq(timesheetInvoice.INVOICE_ID))
			.where(fromCte ? this.accessControlHelper.buildCandidatesAccessControlCondition(candidate.OWNERID)
					: ts.ID.in(timesheetIds));

		// Create field-to-table mapping for multi-table sorting
		Map<String, Table<?>> fieldTableMapping = this.createFieldTableMapping(ts, tsSetting, job, company, candidate);

		SelectLimitStep<?> finalQuery;

		if (sortPriorityList != null && !sortPriorityList.isEmpty()) {
			// Check if there are any valid (non-null, non-empty) sort fields
			boolean hasValidSortFields = sortPriorityList.stream()
				.anyMatch((sort) -> sort.getField() != null && !sort.getField().trim().isEmpty());

			if (hasValidSortFields) {
				finalQuery = this.sortingQueryBuilder.addSortingQuery(baseQuery,
						this.expandSortTiebreakers(sortPriorityList), fieldTableMapping, ts);
			}
			else {
				// All sort fields are invalid, fall back to default sorting
				// Default sorting: Timesheet period start (desc), Timesheet period end
				// (desc), Company Name (asc), Job ID (asc)
				finalQuery = baseQuery.orderBy(ts.PERIOD_START.desc(), ts.PERIOD_END.desc(), company.COMPANYNAME.asc(),
						job.ID.asc(), ts.ID.desc());
			}
		}
		else {
			// Default sorting: Timesheet period start (desc), Timesheet period end
			// (desc),
			// Company Name (asc), Job ID (asc)
			finalQuery = baseQuery.orderBy(ts.PERIOD_START.desc(), ts.PERIOD_END.desc(), company.COMPANYNAME.asc(),
					job.ID.asc(), ts.ID.desc());
		}

		return finalQuery;
	}

	@Override
	public List<DealQueryResultDto> getDealsByTimesheetIds(List<Integer> timesheetIds, Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return List.of();
		}

		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var deals = Tbldeals.TBLDEALS;
		var dealCandidates = Tbldealcandidates.TBLDEALCANDIDATES;
		var dealJobs = Tbldealjobs.TBLDEALJOBS;
		var owner = Tbluser.TBLUSER.as(OWNER_ALIAS);
		var dealPipelineStages = Tbldealpipelinestages.TBLDEALPIPELINESTAGES;

		var query = DSL
			.select(ts.ID.as(TIMESHEET_ID_ALIAS), tsSettingAssoc.CONTRACTOR_ID.as(CONTRACTOR_ID_ALIAS),
					tsSettingAssoc.JOB_ID.as(JOB_ID_ALIAS), deals.ID.as("dealId"), deals.NAME.as(DEAL_NAME_ALIAS),
					DSL.when(owner.LASTNAME.isNull().or(owner.LASTNAME.eq("")), owner.FIRSTNAME)
						.otherwise(DSL.concat(owner.FIRSTNAME, DSL.val(" "), owner.LASTNAME))
						.as("ownerName"),
					deals.SRNO.as(SERIAL_NUMBER_ALIAS), deals.SLUG.as("slug"), dealPipelineStages.LABEL.as("status"))
			.from(ts)
			.innerJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.innerJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.innerJoin(dealCandidates)
			.on(dealCandidates.CANDIDATEID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.innerJoin(dealJobs)
			.on(dealJobs.JOBID.eq(tsSettingAssoc.JOB_ID).and(dealJobs.DEALID.eq(dealCandidates.DEALID)))
			.innerJoin(deals)
			.on(deals.ID.eq(dealCandidates.DEALID))
			.leftJoin(owner)
			.on(owner.ID.eq(deals.OWNERID))
			.leftJoin(dealPipelineStages)
			.on(deals.DEALSTAGE.eq(dealPipelineStages.ID))
			.where(ts.ID.in(timesheetIds))
			.and(deals.ACCOUNTID.eq(accountId))
			.orderBy(ts.ID.asc(), dealCandidates.ID.asc());

		return this.auroraDbDSLContext.fetch(query).into(DealQueryResultDto.class);
	}

	@Override
	public List<TimesheetJobAndContractorListQueryResultDto> getAllTimesheetsByAccountId(Integer accountId,
			List<SortPriorityRequestBodyDto> sortPriorityList, Pageable pageable) {

		// Aliases for tables
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var timesheetInvoice = CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T;
		final var payCurrency = Tblcurrency.TBLCURRENCY.as(PAY_CURRENCY_ALIAS);
		final var billCurrency = Tblcurrency.TBLCURRENCY.as(BILL_CURRENCY_ALIAS);
		var candidate = Tblcandidate.TBLCANDIDATE;
		var job = Tbljob.TBLJOB;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;
		var jobTimesheetAccess = CstJobTimesheetAccessT.CST_JOB_TIMESHEET_ACCESS_T;
		var jobStatus = Tbljobstatus.TBLJOBSTATUS;
		var sortTables = TimesheetListSortTables.create();

		var company = Tblcompany.TBLCOMPANY;
		var companyName = Tblcompany.TBLCOMPANY.COMPANYNAME.as(COMPANY_NAME_ALIAS);

		// Off-limit tables for company
		var entityOffLimitT = EntityOffLimitT.ENTITY_OFF_LIMIT_T;
		var offLimitStatusT = OffLimitStatusT.OFF_LIMIT_STATUS_T;
		var offLimitStatusColourT = OffLimitStatusColourT.OFF_LIMIT_STATUS_COLOUR_T;
		var offLimitMarkedByUser = Tbluser.TBLUSER.as("offLimitMarkedByUser");

		// Off-limit tables for contractor (candidate)
		var contractorOffLimit = this.buildContractorOffLimitTables();
		var contractorEntityOffLimitT = contractorOffLimit.entityOffLimitT();
		var contractorOffLimitStatusT = contractorOffLimit.offLimitStatusT();
		var contractorOffLimitStatusColourT = contractorOffLimit.offLimitStatusColourT();

		var invoice = DSL.table(INVOICE_TABLE_ALIAS).as(INVOICE_TABLE);
		final String invoiceIdPrefixField = INVOICE_ID_PREFIX_FIELD;
		final String invoiceIdNumberField = INVOICE_ID_NUMBER_FIELD;
		final String invoiceStatusIdField = INVOICE_STATUS_ID_FIELD;
		var invoiceNumber = DSL
			.when(DSL.field(invoiceIdPrefixField, String.class)
				.isNull()
				.or(DSL.field(invoiceIdPrefixField, String.class).eq("")),
					DSL.field(invoiceIdNumberField, String.class))
			.otherwise(DSL.concat(DSL.field(invoiceIdPrefixField, String.class), DSL.value("-"),
					DSL.field(invoiceIdNumberField, String.class)))
			.as(INVOICE_NUMBER_ALIAS);
		var invoiceCreatedOn = DSL.field(INVOICE_CREATED_ON_FIELD, Integer.class).as(INVOICE_CREATED_ON_ALIAS);
		var invoiceStatusId = DSL.field(invoiceStatusIdField, Integer.class).as(INVOICE_STATUS_ID_ALIAS);
		Condition candidatesAccessControlCondition = this.accessControlHelper
			.buildCandidatesAccessControlCondition(candidate.OWNERID);

		var reimbursement = CstTimesheetReimbursementT.CST_TIMESHEET_REIMBURSEMENT_T;
		var reimbursementCountField = DSL
			.field(DSL.selectCount()
				.from(reimbursement)
				.where(reimbursement.CST_TIMESHEET_ID.eq(ts.ID).and(reimbursement.ACCOUNT_ID.eq(accountId))))
			.as(REIMBURSEMENT_COUNT_ALIAS);

		SelectConditionStep<?> baseQuery = DSL.select(
				// Timesheet fields
				ts.ID.as("id"), ts.TIMESHEET_SETTING_ID.as(TIMESHEET_SETTING_ID_ALIAS),
				ts.PERIOD_START.as(TIMESHEET_PERIOD_START_DATE_ALIAS),
				ts.PERIOD_END.as(TIMESHEET_PERIOD_END_DATE_ALIAS),

				// TimesheetSetting fields
				tsSetting.WORK_LOG_TYPE.as(WORK_LOG_TYPE_ALIAS),
				tsSetting.JOB_START_DATE.as(JOB_DURATION_START_DATE_ALIAS),
				tsSetting.JOB_END_DATE.as(JOB_DURATION_END_DATE_ALIAS),

				// Rates
				tsSetting.PAY_RATE.as(PAY_RATE_ALIAS), tsSetting.BILL_RATE.as(BILL_RATE_ALIAS),

				// Currency symbols
				payCurrency.SYMBOL.as(PAY_CURRENCY_SYMBOL_ALIAS), billCurrency.SYMBOL.as(BILL_CURRENCY_SYMBOL_ALIAS),

				// Currency codes
				payCurrency.CODE.as(PAY_CURRENCY_CODE_ALIAS), billCurrency.CODE.as(BILL_CURRENCY_CODE_ALIAS),

				// Total Pay/Bill data from Timesheet table
				ts.PAY_DATA.as(PAY_DATA_ALIAS), ts.BILL_DATA.as(BILL_DATA_ALIAS),

				// Total time columns from Timesheet table (seconds)
				ts.TOTAL_TIME.as(TOTAL_TIME_ALIAS), ts.TOTAL_WORK_TIME.as(TOTAL_WORK_TIME_ALIAS),
				ts.TOTAL_OVERTIME.as(TOTAL_OVERTIME_ALIAS),

				// Audit timestamps
				ts.ADDED_ON.as(ADDED_ON_ALIAS), ts.UPDATED_ON.as(UPDATED_ON_ALIAS),

				// Added/Updated by
				ts.ADDED_BY.as(ADDED_BY_ID_ALIAS), ts.UPDATED_BY.as(UPDATED_BY_ID_ALIAS),

				// Timesheet Invoice table fields
				timesheetInvoice.CST_TIMESHEET_PAY_STATUS_TYPE_ID.as(PAY_STATUS_ID_ALIAS),
				timesheetInvoice.PAYMENT_PAID_ON.as(PAYOUT_PAID_ON_ALIAS),
				timesheetInvoice.PAYOUT_NUMBER.as(PAYOUT_NUMBER_ALIAS),
				timesheetInvoice.PAYOUT_FILE.as(PAYOUT_FILE_ALIAS),
				timesheetInvoice.CST_TIMESHEET_BILL_STATUS_TYPE_ID.as(BILL_STATUS_ID_ALIAS),

				// Invoice table fields
				invoiceNumber, invoiceCreatedOn, invoiceStatusId,

				// Contractor Serial Number
				candidate.SRNO.as(SERIAL_NUMBER_ALIAS),

				// User types
				ts.ADDED_BY_USER_TYPE_ID.as(ADDED_BY_USER_TYPE_ID_ALIAS),
				ts.UPDATED_BY_USER_TYPE_ID.as(UPDATED_BY_USER_TYPE_ID_ALIAS),

				// Job Timesheet Access Control
				jobTimesheetAccess.CAN_CREATE.as(CAN_CREATE_ALIAS), jobTimesheetAccess.CAN_EDIT.as(CAN_EDIT_ALIAS),
				jobTimesheetAccess.CAN_DELETE.as(CAN_DELETE_ALIAS),

				// Contractor fields
				candidate.ID.as(CONTRACTOR_ID_ALIAS),
				DSL.when(candidate.LASTNAME.isNull().or(candidate.LASTNAME.eq("")), candidate.FIRSTNAME)
					.otherwise(DSL.concat(candidate.FIRSTNAME, DSL.val(" "), candidate.LASTNAME))
					.as(CONTRACTOR_NAME_ALIAS),
				candidate.PROFILEPIC.as(CONTRACTOR_PHOTO_ALIAS), candidate.SLUG.as(CONTRACTOR_SLUG_ALIAS),
				candidate.POSITION.as(CONTRACTOR_POSITION_ALIAS), candidate.OWNERID.as(CONTRACTOR_OWNER_ID_ALIAS),

				// Job fields
				job.ID.as(JOB_ID_ALIAS), job.NAME.as(JOB_NAME_ALIAS), job.SLUG.as(JOB_SLUG_ALIAS), companyName,
				company.LOGO.as(COMPANY_LOGO_ALIAS), jobStatus.LABEL.as(JOB_STATUS_ALIAS),
				job.JOB_TYPE.as(JOB_TYPE_ALIAS),

				// Company off-limit fields
				entityOffLimitT.OFF_LIMIT_STATUS_ID.as("companyOffLimitStatusId"),
				entityOffLimitT.OFF_LIMIT_REASON.as("companyOffLimitReason"),
				entityOffLimitT.OFF_LIMIT_END_DATE.as("companyOffLimitEndDate"),
				entityOffLimitT.CREATED_ON.as("companyOffLimitStartDate"),
				offLimitStatusT.STATUS_LABEL.as("companyStatusLabel"),
				offLimitStatusColourT.BACKGROUND_COLOR_HEX.as("companyBackgroundColorHex"),
				offLimitStatusColourT.TEXT_COLOR_HEX.as("companyTextColorHex"),
				DSL.when(offLimitMarkedByUser.LASTNAME.isNull().or(offLimitMarkedByUser.LASTNAME.eq("")),
						offLimitMarkedByUser.FIRSTNAME)
					.otherwise(DSL.concat(offLimitMarkedByUser.FIRSTNAME, DSL.val(" "), offLimitMarkedByUser.LASTNAME))
					.as("companyMarkedByName"),

				// Contractor off-limit fields
				contractorEntityOffLimitT.OFF_LIMIT_STATUS_ID.as(CONTRACTOR_OFF_LIMIT_STATUS_ID_ALIAS),
				contractorOffLimitStatusT.STATUS_LABEL.as(CONTRACTOR_STATUS_LABEL_ALIAS),
				contractorOffLimitStatusColourT.BACKGROUND_COLOR_HEX.as(CONTRACTOR_BACKGROUND_COLOR_HEX_ALIAS),
				contractorOffLimitStatusColourT.TEXT_COLOR_HEX.as(CONTRACTOR_TEXT_COLOR_HEX_ALIAS),
				assignJobCandidate.ID.as(CONTRACTOR_ASSIGNMENT_ID_ALIAS), company.SLUG.as("companySlug"),
				buildDealNameSortField(candidate, job, tsSetting),
				buildTimesheetStatusIdSortField(ts, sortTables.tsStatusForSort()),
				buildApprovedBySortField(ts, sortTables), ts.ID.as(TIMESHEET_ID_ALIAS), buildPayStatusSortField(),
				buildBillStatusSortField(), buildExpenseClaimSortField(),
				buildAgencyRecruiterDisplayNameSortField(sortTables.addedByUserForSort(), ts.ADDED_BY,
						ts.ADDED_BY_USER_TYPE_ID, ADDED_BY_ALIAS),
				buildAgencyRecruiterDisplayNameSortField(sortTables.updatedByUserForSort(), ts.UPDATED_BY,
						ts.UPDATED_BY_USER_TYPE_ID, UPDATED_BY_ALIAS),
				reimbursementCountField,
				tsSetting.IS_REIMBURSEMENT_ENABLED.cast(Integer.class).as(IS_REIMBURSEMENT_ENABLED_ALIAS))
			.from(ts)
			.leftJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.leftJoin(contractorEntityOffLimitT)
			.on(contractorEntityOffLimitT.ENTITY_ID.eq(candidate.ID)
				.and(contractorEntityOffLimitT.ENTITY_TYPE.eq(EntityTypeEnum.CANDIDATE.getId())))
			.leftJoin(contractorOffLimitStatusT)
			.on(contractorEntityOffLimitT.OFF_LIMIT_STATUS_ID.eq(contractorOffLimitStatusT.ID))
			.leftJoin(contractorOffLimitStatusColourT)
			.on(contractorOffLimitStatusT.STATUS_COLOUR_ID
				.eq(contractorOffLimitStatusColourT.OFFLIMIT_STATUS_COLOUR_ID))
			.leftJoin(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID))
			.leftJoin(jobStatus)
			.on(job.JOBSTATUS.eq(jobStatus.ID))
			.leftJoin(assignJobCandidate)
			.on(assignJobCandidate.JOBID.eq(job.ID).and(assignJobCandidate.CANDIDATEID.eq(candidate.ID)))
			.leftJoin(company)
			.on(company.ID.eq(job.COMPANYID))
			.leftJoin(entityOffLimitT)
			.on(entityOffLimitT.ENTITY_ID.eq(company.ID)
				.and(entityOffLimitT.ENTITY_TYPE.eq(EntityTypeEnum.COMPANY.getId())))
			.leftJoin(offLimitStatusT)
			.on(entityOffLimitT.OFF_LIMIT_STATUS_ID.eq(offLimitStatusT.ID))
			.leftJoin(offLimitStatusColourT)
			.on(offLimitStatusT.STATUS_COLOUR_ID.eq(offLimitStatusColourT.OFFLIMIT_STATUS_COLOUR_ID))
			.leftJoin(offLimitMarkedByUser)
			.on(offLimitMarkedByUser.ID.eq(entityOffLimitT.CREATED_BY))
			.leftJoin(jobTimesheetAccess)
			.on(jobTimesheetAccess.JOB_ID.cast(Integer.class).eq(job.ID))
			.leftJoin(payCurrency)
			.on(payCurrency.ID.eq(tsSetting.PAY_CURRENCY_ID))
			.leftJoin(billCurrency)
			.on(billCurrency.ID.eq(tsSetting.BILL_CURRENCY_ID))
			.leftJoin(timesheetInvoice)
			.on(timesheetInvoice.CST_TIMESHEET_ID.eq(ts.ID)
				.and(timesheetInvoice.ID.eq(DSL.select(DSL.max(CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T.ID))
					.from(CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T)
					.where(CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T.CST_TIMESHEET_ID.eq(ts.ID)))))
			.leftJoin(invoice)
			.on(DSL.field(INVOICE_ID_FIELD, Integer.class).eq(timesheetInvoice.INVOICE_ID))
			.where(tsSetting.ACCOUNT_ID.eq(accountId).and(candidatesAccessControlCondition));

		// Create field-to-table mapping for multi-table sorting
		Map<String, Table<?>> fieldTableMapping = this.createFieldTableMapping(ts, tsSetting, job, company, candidate);

		SelectLimitStep<?> finalQuery;

		if (sortPriorityList != null && !sortPriorityList.isEmpty()) {
			// Check if there are any valid (non-null, non-empty) sort fields
			boolean hasValidSortFields = sortPriorityList.stream()
				.anyMatch((sort) -> sort.getField() != null && !sort.getField().trim().isEmpty());

			if (hasValidSortFields) {
				finalQuery = this.sortingQueryBuilder.addSortingQuery(baseQuery,
						this.expandSortTiebreakers(sortPriorityList), fieldTableMapping, ts);
			}
			else {
				// All sort fields are invalid, fall back to default sorting
				// Default sorting: Timesheet period start (desc), Timesheet period end
				// (desc), Company Name (asc), Job ID (asc)
				finalQuery = baseQuery.orderBy(ts.PERIOD_START.desc(), ts.PERIOD_END.desc(), company.COMPANYNAME.asc(),
						job.ID.asc(), ts.ID.desc());
			}
		}
		else {
			// Default sorting: Timesheet period start (desc), Timesheet period end
			// (desc),
			// Company Name (asc), Job ID (asc)
			finalQuery = baseQuery.orderBy(ts.PERIOD_START.desc(), ts.PERIOD_END.desc(), company.COMPANYNAME.asc(),
					job.ID.asc(), ts.ID.desc());
		}

		// Apply pagination
		finalQuery = (SelectLimitStep<?>) finalQuery.offset(pageable.getPageNumber() * pageable.getPageSize())
			.limit(pageable.getPageSize());

		return this.auroraDbDSLContext.fetch(finalQuery).into(TimesheetJobAndContractorListQueryResultDto.class);
	}

	@Override
	public List<JobSearchQueryResultDto> searchJobs(Integer accountId, String searchKeyword,
			Boolean fromContractorsListPage) {
		var job = Tbljob.TBLJOB;
		var company = Tblcompany.TBLCOMPANY;

		Condition baseCondition = job.ACCOUNTID.eq(accountId);
		if (Boolean.TRUE.equals(fromContractorsListPage)) {
			baseCondition = baseCondition.and(job.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT));
		}

		Condition searchCondition = buildJobSearchCondition(job, searchKeyword);

		Condition accessControlCondition = this.accessControlHelper.buildJobsAccessControlCondition(job.OWNERID,
				job.ID);

		return this.auroraDbDSLContext
			.select(job.ID, job.NAME.as("name"), job.SLUG, job.SRNO, company.COMPANYNAME.as("companyname"),
					company.SLUG.as("companyslug"), job.CITY.as("location"))
			.from(job)
			.leftJoin(company)
			.on(company.ID.eq(job.COMPANYID))
			.where(baseCondition.and(searchCondition).and(accessControlCondition))
			.limit(6)
			.fetch()
			.into(JobSearchQueryResultDto.class);
	}

	/**
	 * Builds the search condition for job search by keyword. Matches job name, job srno
	 * (when keyword is numeric), job city, and job type using case-insensitive like.
	 */
	private Condition buildJobSearchCondition(Tbljob job, String searchKeyword) {
		if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
			return DSL.trueCondition();
		}
		String trimmed = searchKeyword.trim();
		String pattern = "%" + trimmed + "%";
		Condition searchCondition = job.NAME.likeIgnoreCase(pattern).or(job.CITY.likeIgnoreCase(pattern));
		try {
			int jobSrno = Integer.parseInt(trimmed);
			searchCondition = searchCondition.or(job.SRNO.eq(jobSrno));
		}
		catch (NumberFormatException ex) {
			// Keyword is not numeric, job SRNO match not applied
		}
		return searchCondition;
	}

	/**
	 * Builds the search condition for company search by keyword. Matches company name,
	 * company srno (when keyword is numeric), and company city using case-insensitive
	 * like.
	 */
	private Condition buildCompanySearchCondition(Tblcompany company, String searchKeyword) {
		if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
			return DSL.trueCondition();
		}
		String trimmed = searchKeyword.trim();
		String pattern = "%" + trimmed + "%";
		Condition searchCondition = company.COMPANYNAME.likeIgnoreCase(pattern)
			.or(company.CITY.likeIgnoreCase(pattern));
		try {
			int companySrno = Integer.parseInt(trimmed);
			searchCondition = searchCondition.or(company.SRNO.eq(companySrno));
		}
		catch (NumberFormatException ex) {
			// Keyword is not numeric, company SRNO match not applied
		}
		return searchCondition;
	}

	/**
	 * Builds the search condition for deal search by keyword. Matches Name (deal name),
	 * Owner Name (concatenated first and last name when owner table is joined), and Stage
	 * (pipeline stage label when stage table is joined). Also matches deal srno when
	 * keyword is numeric.
	 */
	private Condition buildDealSearchCondition(Tbldeals deals, String searchKeyword, Tbluser owner,
			Tbldealpipelinestages dealPipelineStages) {
		if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
			return DSL.trueCondition();
		}
		String trimmed = searchKeyword.trim();
		String pattern = "%" + trimmed + "%";
		Condition searchCondition = deals.NAME.likeIgnoreCase(pattern);
		if (owner != null) {
			var ownerFullName = DSL.when(owner.LASTNAME.isNull().or(owner.LASTNAME.eq("")), owner.FIRSTNAME)
				.otherwise(DSL.concat(owner.FIRSTNAME, DSL.val(" "), owner.LASTNAME));
			searchCondition = searchCondition.or(ownerFullName.likeIgnoreCase(pattern));
		}
		if (dealPipelineStages != null) {
			searchCondition = searchCondition.or(dealPipelineStages.LABEL.likeIgnoreCase(pattern));
		}
		try {
			int dealSrno = Integer.parseInt(trimmed);
			searchCondition = searchCondition.or(deals.SRNO.eq(dealSrno));
		}
		catch (NumberFormatException ex) {
			// Keyword is not numeric, deal SRNO match not applied
		}
		return searchCondition;
	}

	@Override
	public List<CompanySearchQueryResultDto> searchCompanies(Integer accountId, String searchKeyword) {
		var company = Tblcompany.TBLCOMPANY;
		var job = Tbljob.TBLJOB;

		Condition baseCondition = company.ACCOUNTID.eq(accountId)
			.and(job.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT));

		Condition searchCondition = this.buildCompanySearchCondition(company, searchKeyword);

		Condition accessControlCondition = this.accessControlHelper
			.buildCompaniesAccessControlCondition(company.OWNERID);

		return this.auroraDbDSLContext
			.selectDistinct(company.ID, company.COMPANYNAME.as("name"), company.SLUG, company.SRNO, company.ADDRESS,
					company.CITY, company.HASCHILDREN, company.INDUSTRYID, company.OWNERID, company.LOGO,
					company.WEBSITE)
			.from(company)
			.innerJoin(job)
			.on(job.COMPANYID.eq(company.ID))
			.where(baseCondition.and(searchCondition).and(accessControlCondition))
			.limit(6)
			.fetch()
			.into(CompanySearchQueryResultDto.class);
	}

	@Override
	public List<DealSearchQueryResultDto> searchDeals(Integer accountId, String searchKeyword,
			Boolean fromContractorsListPage) {
		var deals = Tbldeals.TBLDEALS;
		var dealPipelineStages = Tbldealpipelinestages.TBLDEALPIPELINESTAGES;
		var owner = Tbluser.TBLUSER.as(OWNER_ALIAS);

		Condition baseCondition = deals.ACCOUNTID.eq(accountId);

		Condition searchCondition = this.buildDealSearchCondition(deals, searchKeyword, owner, dealPipelineStages);

		Condition accessControlCondition = this.accessControlHelper.buildDealsAccessControlCondition(deals.OWNERID);

		SelectJoinStep<?> query = this.auroraDbDSLContext
			.selectDistinct(deals.ID, deals.NAME, deals.SLUG, deals.SRNO, deals.OWNERID.as(OWNER_ALIAS),
					deals.DEALSTAGE, dealPipelineStages.LABEL.as("stagename"))
			.from(deals);

		if (Boolean.TRUE.equals(fromContractorsListPage)) {
			var dealCandidates = Tbldealcandidates.TBLDEALCANDIDATES;
			query = query.innerJoin(dealCandidates).on(dealCandidates.DEALID.eq(deals.ID));
		}
		else {
			var dealJobs = Tbldealjobs.TBLDEALJOBS;
			var job = Tbljob.TBLJOB;
			baseCondition = baseCondition.and(job.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT));
			query = query.innerJoin(dealJobs)
				.on(dealJobs.DEALID.eq(deals.ID))
				.innerJoin(job)
				.on(job.ID.eq(dealJobs.JOBID));
		}

		query = query.leftJoin(dealPipelineStages)
			.on(deals.DEALSTAGE.eq(dealPipelineStages.ID))
			.leftJoin(owner)
			.on(owner.ID.eq(deals.OWNERID));

		return query.where(baseCondition.and(searchCondition).and(accessControlCondition))
			.limit(6)
			.fetch()
			.into(DealSearchQueryResultDto.class);
	}

	@Override
	public Long getAllTimesheetsCountByAccountId(Integer accountId) {
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var candidate = Tblcandidate.TBLCANDIDATE;

		Condition candidatesAccessControlCondition = this.accessControlHelper
			.buildCandidatesAccessControlCondition(candidate.OWNERID);

		Long count = this.auroraDbDSLContext.selectCount()
			.from(ts)
			.leftJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.where(tsSetting.ACCOUNT_ID.eq(accountId).and(candidatesAccessControlCondition))
			.fetchOne(0, Long.class);

		return (count != null) ? count : 0L;
	}

	@Override
	public Long countTimesheetsByIdsAndAccountId(List<Integer> timesheetIds, Integer accountId) {
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;

		Long count = this.auroraDbDSLContext.selectCount()
			.from(ts)
			.leftJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.where(ts.ID.in(timesheetIds).and(tsSetting.ACCOUNT_ID.eq(accountId)))
			.fetchOne(0, Long.class);

		return (count != null) ? count : 0L;
	}

	@Override
	public List<Integer> filterTimesheetIdsByAccountAndCandidateAccess(List<Integer> timesheetIds, Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return List.of();
		}

		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var candidate = Tblcandidate.TBLCANDIDATE;
		var job = Tbljob.TBLJOB;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;

		Condition candidatesAccessControlCondition = this.accessControlHelper
			.buildCandidatesAccessControlCondition(candidate.OWNERID);

		return this.auroraDbDSLContext.select(ts.ID)
			.from(ts)
			.leftJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.leftJoin(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID))
			.leftJoin(assignJobCandidate)
			.on(assignJobCandidate.JOBID.eq(job.ID).and(assignJobCandidate.CANDIDATEID.eq(candidate.ID)))
			.where(tsSetting.ACCOUNT_ID.eq(accountId).and(ts.ID.in(timesheetIds)).and(candidatesAccessControlCondition))
			.fetchInto(Integer.class);
	}

	@Override
	public Long getTimesheetsCountByApproverUserId(Integer userId, Integer accountId) {
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var tsApprover = CstTimesheetApproverT.CST_TIMESHEET_APPROVER_T;
		var tsApproval = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T;
		var candidate = Tblcandidate.TBLCANDIDATE;
		var job = Tbljob.TBLJOB;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;

		Condition candidatesAccessControlCondition = this.accessControlHelper
			.buildCandidatesAccessControlCondition(candidate.OWNERID);

		Select<Record1<Integer>> approverSettingIdsSubquery = DSL.select(tsApprover.TIMESHEET_SETTING_ID)
			.from(tsApprover)
			.where(tsApprover.ENTITY_ID.eq(userId));

		Select<Record1<Integer>> latestApprovalSubquery = DSL.select(DSL.max(tsApproval.ID))
			.from(tsApproval)
			.where(tsApproval.TIMESHEET_ID.eq(ts.ID));

		Long count = this.auroraDbDSLContext.selectCount()
			.from(ts)
			.innerJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.leftJoin(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID))
			.leftJoin(assignJobCandidate)
			.on(assignJobCandidate.JOBID.eq(job.ID).and(assignJobCandidate.CANDIDATEID.eq(candidate.ID)))
			.innerJoin(tsApproval)
			.on(tsApproval.TIMESHEET_ID.eq(ts.ID).and(tsApproval.ID.eq(latestApprovalSubquery)))
			.where(tsSetting.ACCOUNT_ID.eq(accountId))
			.and(ts.TIMESHEET_SETTING_ID.in(approverSettingIdsSubquery))
			.and(tsApproval.TIMESHEET_APPROVAL_STATUS_TYPE_ID.eq(2))
			.and(candidatesAccessControlCondition)
			.fetchOne(0, Long.class);

		return (count != null) ? count : 0L;
	}

	@Override
	public List<Integer> getTimesheetIdsByApproverUserId(Integer userId, Integer accountId, Pageable pageable) {
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var tsApprover = CstTimesheetApproverT.CST_TIMESHEET_APPROVER_T;
		var tsApproval = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T;
		var candidate = Tblcandidate.TBLCANDIDATE;
		var job = Tbljob.TBLJOB;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;

		Condition candidatesAccessControlCondition = this.accessControlHelper
			.buildCandidatesAccessControlCondition(candidate.OWNERID);

		Select<Record1<Integer>> approverSettingIdsSubquery = DSL.select(tsApprover.TIMESHEET_SETTING_ID)
			.from(tsApprover)
			.where(tsApprover.ENTITY_ID.eq(userId));

		Select<Record1<Integer>> latestApprovalSubquery = DSL.select(DSL.max(tsApproval.ID))
			.from(tsApproval)
			.where(tsApproval.TIMESHEET_ID.eq(ts.ID));

		return this.auroraDbDSLContext.select(ts.ID)
			.from(ts)
			.innerJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.leftJoin(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID))
			.leftJoin(assignJobCandidate)
			.on(assignJobCandidate.JOBID.eq(job.ID).and(assignJobCandidate.CANDIDATEID.eq(candidate.ID)))
			.innerJoin(tsApproval)
			.on(tsApproval.TIMESHEET_ID.eq(ts.ID).and(tsApproval.ID.eq(latestApprovalSubquery)))
			.where(tsSetting.ACCOUNT_ID.eq(accountId))
			.and(ts.TIMESHEET_SETTING_ID.in(approverSettingIdsSubquery))
			.and(tsApproval.TIMESHEET_APPROVAL_STATUS_TYPE_ID.eq(2))
			.and(candidatesAccessControlCondition)
			.orderBy(ts.PERIOD_START.desc(), ts.PERIOD_END.desc(), ts.ID.desc())
			.offset(pageable.getPageNumber() * pageable.getPageSize())
			.limit(pageable.getPageSize())
			.fetchInto(Integer.class);
	}

	@Override
	public Long getTimesheetsCountWithPendingReimbursements(Integer accountId) {
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var reimbursement = CstTimesheetReimbursementT.CST_TIMESHEET_REIMBURSEMENT_T;
		var candidate = Tblcandidate.TBLCANDIDATE;
		var job = Tbljob.TBLJOB;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;

		Condition candidatesAccessControlCondition = this.accessControlHelper
			.buildCandidatesAccessControlCondition(candidate.OWNERID);

		Condition pendingReimbursementExists = this.buildPendingReimbursementExistsCondition(ts, reimbursement,
				accountId);

		Long count = this.auroraDbDSLContext.selectCount()
			.from(ts)
			.innerJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.leftJoin(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID))
			.leftJoin(assignJobCandidate)
			.on(assignJobCandidate.JOBID.eq(job.ID).and(assignJobCandidate.CANDIDATEID.eq(candidate.ID)))
			.where(tsSetting.ACCOUNT_ID.eq(accountId))
			.and(pendingReimbursementExists)
			.and(candidatesAccessControlCondition)
			.fetchOne(0, Long.class);

		return (count != null) ? count : 0L;
	}

	@Override
	public List<Integer> getTimesheetIdsWithPendingReimbursements(Integer accountId, Pageable pageable) {
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var reimbursement = CstTimesheetReimbursementT.CST_TIMESHEET_REIMBURSEMENT_T;
		var candidate = Tblcandidate.TBLCANDIDATE;
		var job = Tbljob.TBLJOB;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;

		Condition candidatesAccessControlCondition = this.accessControlHelper
			.buildCandidatesAccessControlCondition(candidate.OWNERID);

		Condition pendingReimbursementExists = this.buildPendingReimbursementExistsCondition(ts, reimbursement,
				accountId);

		return this.auroraDbDSLContext.select(ts.ID)
			.from(ts)
			.innerJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.leftJoin(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID))
			.leftJoin(assignJobCandidate)
			.on(assignJobCandidate.JOBID.eq(job.ID).and(assignJobCandidate.CANDIDATEID.eq(candidate.ID)))
			.where(tsSetting.ACCOUNT_ID.eq(accountId))
			.and(pendingReimbursementExists)
			.and(candidatesAccessControlCondition)
			.orderBy(ts.PERIOD_START.desc(), ts.PERIOD_END.desc(), ts.ID.desc())
			.offset(pageable.getPageNumber() * pageable.getPageSize())
			.limit(pageable.getPageSize())
			.fetchInto(Integer.class);
	}

	/**
	 * Account-scoped variant (agency dashboards / reminders) — matches any pending
	 * expense claim regardless of client-sharing state. No client visibility filter.
	 */
	private Condition buildPendingReimbursementExistsCondition(CstTimesheetT ts,
			CstTimesheetReimbursementT reimbursement, Integer accountId) {
		return this.buildPendingReimbursementExistsCondition(ts, reimbursement, accountId, null);
	}

	private Condition buildPendingReimbursementExistsCondition(CstTimesheetT ts,
			CstTimesheetReimbursementT reimbursement, Integer accountId, Integer entityType) {
		SelectConditionStep<Record1<Integer>> pendingReimbursement = DSL.selectOne()
			.from(reimbursement)
			.where(reimbursement.CST_TIMESHEET_ID.eq(ts.ID))
			.and(reimbursement.ACCOUNT_ID.eq(accountId))
			.and(reimbursement.STATUS.eq(ReimbursementConstants.STATUS_SUBMITTED));

		/**
		 * For a client/contact viewer, only surface timesheets that have at least one
		 * PENDING expense claim explicitly shared with the client. If every pending
		 * expense claim on the timesheet is unshared (is_shared_with_client = 0), the
		 * timesheet is withheld from the client. Agency/contractor viewers are
		 * unaffected. The jOOQ metamodel is stale for this column, so it is referenced by
		 * qualified name.
		 */
		if (UserTypeEnum.COMPANY_CONTACT.getId().equals(entityType)) {
			pendingReimbursement = pendingReimbursement
				.and(DSL.field(DSL.name(reimbursement.getName(), "is_shared_with_client"), Integer.class)
					.eq(BooleanFlagEnum.TRUE.getValue()));
		}

		return DSL.exists(pendingReimbursement);
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteByIdInAndAccountId(List<Integer> ids, Integer accountId) {
		if (ids == null || ids.isEmpty() || accountId == null) {
			return;
		}

		// Batch delete using JOOQ - single SQL statement: DELETE FROM table WHERE id IN
		// (...) AND account_id = ?
		// This is much more efficient than JPA's deleteByIdInAndAccountId() which
		// executes N individual DELETE statements
		var table = CstTimesheetT.CST_TIMESHEET_T;
		this.auroraDbDSLContext.deleteFrom(table).where(table.ID.in(ids)).and(table.ACCOUNT_ID.eq(accountId)).execute();
	}

	@Override
	public List<Timesheet> findByIdInAndAccountId(List<Integer> ids, Integer accountId) {
		if (ids == null || ids.isEmpty() || accountId == null) {
			return List.of();
		}

		String jpql = "SELECT t FROM Timesheet t WHERE t.id IN :ids AND t.accountId = :accountId";
		return this.entityManager.createQuery(jpql, Timesheet.class)
			.setParameter("ids", ids)
			.setParameter("accountId", accountId)
			.getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimesheetForMigrationDto> findTimesheetsForMigration(int limit, int offset) {
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		return this.auroraDbDSLContext.select(ts.ID, ts.TIMESHEET_SETTING_ID)
			.from(ts)
			.orderBy(ts.ID)
			.limit(limit)
			.offset(offset)
			.fetch()
			.map((rec) -> new TimesheetForMigrationDto(rec.get(ts.ID), rec.get(ts.TIMESHEET_SETTING_ID)));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<TimesheetForMigrationDto> findTimesheetForMigrationById(Integer timesheetId) {
		if (timesheetId == null) {
			return Optional.empty();
		}
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var rec = this.auroraDbDSLContext.select(ts.ID, ts.TIMESHEET_SETTING_ID)
			.from(ts)
			.where(ts.ID.eq(timesheetId))
			.fetchOne();
		return (rec != null)
				? Optional.of(new TimesheetForMigrationDto(rec.get(ts.ID), rec.get(ts.TIMESHEET_SETTING_ID)))
				: Optional.empty();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateTimesheetTotalColumns(Integer timesheetId, int totalTime, int totalWorkTime, int totalOvertime) {
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		this.auroraDbDSLContext.update(ts)
			.set(ts.TOTAL_TIME, totalTime)
			.set(ts.TOTAL_WORK_TIME, totalWorkTime)
			.set(ts.TOTAL_OVERTIME, totalOvertime)
			.where(ts.ID.eq(timesheetId))
			.execute();
	}

	/**
	 * Aliased JOOQ tables referenced only by timesheet list sort sub-selects (status,
	 * approvedBy, addedBy, updatedBy).
	 */
	private record TimesheetListSortTables(CstTimesheetApprovalT tsStatusForSort,
			CstTimesheetApprovalT tsApprovedByForSort, CstTimesheetApprovalT tsApprovedByLatest,
			Tbluser approverUserForSort, Tbluser addedByUserForSort, Tbluser updatedByUserForSort) {

		private static TimesheetListSortTables create() {
			return new TimesheetListSortTables(
					CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T.as(TS_STATUS_FOR_SORT_ALIAS),
					CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T.as(TS_APPROVED_BY_FOR_SORT_ALIAS),
					CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T.as(TS_APPROVED_BY_LATEST_ALIAS),
					Tbluser.TBLUSER.as(APPROVER_USER_FOR_SORT_ALIAS), Tbluser.TBLUSER.as(ADDED_BY_USER_FOR_SORT_ALIAS),
					Tbluser.TBLUSER.as(UPDATED_BY_USER_FOR_SORT_ALIAS));
		}

	}

	/**
	 * Holder for the contractor off-limit jOOQ table aliases.
	 */
	private record ContractorOffLimitTables(EntityOffLimitT entityOffLimitT, OffLimitStatusT offLimitStatusT,
			OffLimitStatusColourT offLimitStatusColourT) {
	}

}
