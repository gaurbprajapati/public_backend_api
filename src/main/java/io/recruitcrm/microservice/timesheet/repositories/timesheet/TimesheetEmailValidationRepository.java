package io.recruitcrm.microservice.timesheet.repositories.timesheet;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetApprovalT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetApproverT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblassignjobcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.ContractorPortalStatusT;
import io.recruitcrm.microservice.search.models.jooq.tables.JobSecondaryContactsT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbljob;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbluser;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetApproverEmailQueryRowDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCandidateEmailQueryResultDto;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class TimesheetEmailValidationRepository {

	private static final int ENTITY_TYPE_CONTRACTOR = 3;

	private static final int USER_TYPE_COMPANY_CONTACT = 1;

	private static final int USER_TYPE_AGENCY_RECRUITER = 2;

	private static final String FIELD_TIMESHEET_ID = "timesheet_id";

	private static final String FIELD_DELETED = "deleted";

	private static final String FIELD_FIRST_NAME = "firstName";

	private static final String FIELD_LAST_NAME = "lastName";

	private static final String FIELD_EMAIL_ID = "emailId";

	private static final String FIELD_EMAIL_OPT_OUT = "emailOptOut";

	private static final String FIELD_TIMESHEET_ID_CAMEL = "timesheetId";

	private static final String TABLE_LATEST_APPROVAL = "latest_approval";

	private static final String FIELD_OWNER_ID = "ownerId";

	private static final String JOB_TYPE_CONTRACT = "contract";

	private static final String JOB_TYPE_CONTRACT_TO_PERMANENT = "contracttopermanent";

	private static final String TABLE_CLIENT_PORTAL_STATUS = "client_portal_status_t";

	private static final String FIELD_VMS_USER_EMAIL = "vms_user_email";

	private static final String FIELD_PORTAL_STATUS_ID = "portal_status_id";

	private final DSLContext auroraDbDSLContext;

	public TimesheetEmailValidationRepository(DSLContext auroraDbDSLContext) {
		this.auroraDbDSLContext = auroraDbDSLContext;
	}

	/**
	 * Single consolidated query that fetches candidate details, latest approval status,
	 * and assignment info in one database roundtrip.
	 */
	public List<TimesheetCandidateEmailQueryResultDto> getTimesheetValidationData(List<Integer> timesheetIds,
			Integer accountId, Integer entityTypeId) {
		var timesheet = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsAssociation = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var candidate = Tblcandidate.TBLCANDIDATE;
		var approval = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T;

		Field<Integer> latestApprovalStatus = DSL.select(approval.TIMESHEET_APPROVAL_STATUS_TYPE_ID)
			.from(approval)
			.where(approval.TIMESHEET_ID.eq(timesheet.ID))
			.orderBy(approval.ID.desc())
			.limit(1)
			.asField("latestApprovalStatusId");

		if (entityTypeId != null && entityTypeId == ENTITY_TYPE_CONTRACTOR) {
			return this.fetchWithAssignment(timesheetIds, accountId, timesheet, tsSetting, tsAssociation, candidate,
					latestApprovalStatus);
		}

		return this.fetchWithoutAssignment(timesheetIds, accountId, timesheet, tsSetting, tsAssociation, candidate,
				latestApprovalStatus);
	}

	/**
	 * Fetches one row per timesheet approver (same source as time-logs:
	 * {@code cst_timesheet_approver_t} via timesheet setting), with contact or agency
	 * user details resolved from {@code tblcontact} / {@code tbluser}. Timesheets with no
	 * approvers still produce one row with null approver columns.
	 */
	public List<TimesheetApproverEmailQueryRowDto> getApproverEmailValidationRows(List<Integer> timesheetIds,
			Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return Collections.emptyList();
		}
		var timesheet = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsAssociation = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var approval = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T;
		var tap = CstTimesheetApproverT.CST_TIMESHEET_APPROVER_T;
		var tu = Tbluser.TBLUSER.as("tu");
		var job = Tbljob.TBLJOB;
		var jsc = JobSecondaryContactsT.JOB_SECONDARY_CONTACTS_T;
		Table<?> contact = DSL.table(DSL.name("tblcontact")).as("tc");

		Table<?> latestApproval = buildLatestApprovalDerivedTable(approval, timesheetIds);
		Field<Integer> laTimesheetId = DSL.field(DSL.name("la", FIELD_TIMESHEET_ID), Integer.class);
		Field<Integer> laStatusId = DSL.field(DSL.name("la", "latest_status_id"), Integer.class);

		Field<Integer> tcId = DSL.field(DSL.name("tc", "id"), Integer.class);
		Field<String> tcFirst = DSL.field(DSL.name("tc", "firstname"), String.class);
		Field<String> tcLast = DSL.field(DSL.name("tc", "lastname"), String.class);
		Field<String> tcEmail = DSL.field(DSL.name("tc", "email"), String.class);
		Field<String> tcSlug = DSL.field(DSL.name("tc", "slug"), String.class);
		Field<Byte> tcEmailOptOut = DSL.field(DSL.name("tc", "email_opt_out"), Byte.class);
		Field<Byte> tcDeleted = DSL.field(DSL.name("tc", FIELD_DELETED), Byte.class);

		Field<String> firstNameField = DSL.when(tap.ID.isNull(), DSL.inline((String) null))
			.when(tap.USER_TYPE_ID.eq(USER_TYPE_COMPANY_CONTACT), tcFirst)
			.when(tap.USER_TYPE_ID.eq(USER_TYPE_AGENCY_RECRUITER), tu.FIRSTNAME)
			.otherwise(DSL.inline((String) null))
			.as(FIELD_FIRST_NAME);

		Field<String> lastNameField = DSL.when(tap.ID.isNull(), DSL.inline((String) null))
			.when(tap.USER_TYPE_ID.eq(USER_TYPE_COMPANY_CONTACT), tcLast)
			.when(tap.USER_TYPE_ID.eq(USER_TYPE_AGENCY_RECRUITER), tu.LASTNAME)
			.otherwise(DSL.inline((String) null))
			.as(FIELD_LAST_NAME);

		Field<String> emailIdField = DSL.when(tap.ID.isNull(), DSL.inline((String) null))
			.when(tap.USER_TYPE_ID.eq(USER_TYPE_COMPANY_CONTACT), tcEmail)
			.when(tap.USER_TYPE_ID.eq(USER_TYPE_AGENCY_RECRUITER), tu.EMAIL)
			.otherwise(DSL.inline((String) null))
			.as(FIELD_EMAIL_ID);

		Field<String> slugField = DSL.when(tap.USER_TYPE_ID.eq(USER_TYPE_COMPANY_CONTACT), tcSlug)
			.otherwise(DSL.inline((String) null))
			.as("slug");

		Field<Byte> emailOptOutField = DSL.when(tap.ID.isNull(), DSL.inline((byte) 0))
			.when(tap.USER_TYPE_ID.eq(USER_TYPE_COMPANY_CONTACT), DSL.coalesce(tcEmailOptOut, DSL.inline((byte) 0)))
			.when(tap.USER_TYPE_ID.eq(USER_TYPE_AGENCY_RECRUITER), DSL.inline((byte) 0))
			.otherwise(DSL.inline((byte) 0))
			.as(FIELD_EMAIL_OPT_OUT);

		Field<Byte> deletedField = DSL.when(tap.ID.isNull(), DSL.inline((byte) 0))
			.when(tap.USER_TYPE_ID.eq(USER_TYPE_COMPANY_CONTACT), DSL.coalesce(tcDeleted, DSL.inline((byte) 0)))
			.when(tap.USER_TYPE_ID.eq(USER_TYPE_AGENCY_RECRUITER),
					DSL.when(tu.USERSTATUS.eq((byte) 1), DSL.inline((byte) 1)).otherwise(DSL.inline((byte) 0)))
			.otherwise(DSL.inline((byte) 0))
			.as(FIELD_DELETED);

		Field<Byte> sharedWithContactField = DSL
			.when(tap.USER_TYPE_ID.ne(USER_TYPE_COMPANY_CONTACT), DSL.inline((byte) 1))
			.when(job.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT)
				.and(tap.ENTITY_ID.eq(job.CONTACTID)
					.or(DSL.exists(DSL.selectOne()
						.from(jsc)
						.where(jsc.JOB_ID.eq(job.ID).and(jsc.CONTACT_ID.eq(tap.ENTITY_ID)))))),
					DSL.inline((byte) 1))
			.otherwise(DSL.inline((byte) 0))
			.as("sharedWithContact");

		Field<Byte> sharedWithClientField = DSL
			.when(tap.USER_TYPE_ID.ne(USER_TYPE_COMPANY_CONTACT), DSL.inline((byte) 1))
			.when(job.AUTHID.isNotNull().and(job.AUTHID.ne("")), DSL.inline((byte) 1))
			.otherwise(DSL.inline((byte) 0))
			.as("sharedWithClient");

		Field<Integer> tcOwnerId = DSL.field(DSL.name("tc", "ownerid"), Integer.class);
		Field<Integer> ownerIdField = DSL.when(tap.USER_TYPE_ID.eq(USER_TYPE_COMPANY_CONTACT), tcOwnerId)
			.otherwise(DSL.inline((Integer) null))
			.as(FIELD_OWNER_ID);

		return this.auroraDbDSLContext
			.select(timesheet.ID.as(FIELD_TIMESHEET_ID_CAMEL), tap.ID.as("timesheetApproverId"),
					tap.USER_TYPE_ID.as("userTypeId"), tap.ENTITY_ID.as("entityId"), firstNameField, lastNameField,
					emailIdField, slugField, emailOptOutField, deletedField, laStatusId.as("latestApprovalStatusId"),
					job.ID.as("jobId"), sharedWithContactField, sharedWithClientField, ownerIdField)
			.from(timesheet)
			.innerJoin(tsSetting)
			.on(tsSetting.ID.eq(timesheet.TIMESHEET_SETTING_ID))
			.innerJoin(tsAssociation)
			.on(tsAssociation.ID.eq(tsSetting.ASSOCIATION_ID))
			.leftJoin(job)
			.on(job.ID.eq(tsAssociation.JOB_ID))
			.leftJoin(tap)
			.on(tap.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(contact)
			.on(tap.USER_TYPE_ID.eq(USER_TYPE_COMPANY_CONTACT).and(tcId.eq(tap.ENTITY_ID)))
			.leftJoin(tu)
			.on(tap.USER_TYPE_ID.eq(USER_TYPE_AGENCY_RECRUITER)
				.and(tu.ID.eq(tap.ENTITY_ID))
				.and(tu.ACCOUNTID.eq(accountId)))
			.leftJoin(latestApproval)
			.on(laTimesheetId.eq(timesheet.ID))
			.where(timesheet.ID.in(timesheetIds).and(timesheet.ACCOUNT_ID.eq(accountId)))
			.orderBy(timesheet.ID.asc(), tap.ID.asc())
			.fetch()
			.into(TimesheetApproverEmailQueryRowDto.class);
	}

	/**
	 * Builds a derived table that computes the latest approval status per timesheet in a
	 * single pass, replacing the correlated scalar subquery that MySQL would execute once
	 * per row.
	 */
	private Table<?> buildLatestApprovalDerivedTable(CstTimesheetApprovalT approval, List<Integer> timesheetIds) {
		Field<Integer> maxId = DSL.max(approval.ID).as("max_id");
		Table<?> latest = this.auroraDbDSLContext.select(approval.TIMESHEET_ID, maxId)
			.from(approval)
			.where(approval.TIMESHEET_ID.in(timesheetIds))
			.groupBy(approval.TIMESHEET_ID)
			.asTable(TABLE_LATEST_APPROVAL);

		Field<Integer> latestTimesheetId = DSL.field(DSL.name(TABLE_LATEST_APPROVAL, FIELD_TIMESHEET_ID),
				Integer.class);
		Field<Integer> latestMaxId = DSL.field(DSL.name(TABLE_LATEST_APPROVAL, "max_id"), Integer.class);

		return this.auroraDbDSLContext
			.select(latestTimesheetId.as(FIELD_TIMESHEET_ID),
					approval.TIMESHEET_APPROVAL_STATUS_TYPE_ID.as("latest_status_id"))
			.from(latest)
			.innerJoin(approval)
			.on(approval.ID.eq(latestMaxId))
			.asTable("la");
	}

	private List<TimesheetCandidateEmailQueryResultDto> fetchWithAssignment(List<Integer> timesheetIds,
			Integer accountId, CstTimesheetT timesheet, CstTimesheetSettingT tsSetting,
			CstTimesheetSettingAssociationT tsAssociation, Tblcandidate candidate,
			Field<Integer> latestApprovalStatus) {
		var job = Tbljob.TBLJOB;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;
		var portalStatus = ContractorPortalStatusT.CONTRACTOR_PORTAL_STATUS_T;

		return this.auroraDbDSLContext
			.select(timesheet.ID.as(FIELD_TIMESHEET_ID_CAMEL), candidate.ID.as("candidateId"),
					candidate.FIRSTNAME.as(FIELD_FIRST_NAME), candidate.LASTNAME.as(FIELD_LAST_NAME),
					candidate.SRNO.as("srno"), candidate.SLUG.as("slug"), candidate.EMAILID.as(FIELD_EMAIL_ID),
					candidate.EMAIL_OPT_OUT.as(FIELD_EMAIL_OPT_OUT), candidate.DELETED.as(FIELD_DELETED),
					latestApprovalStatus, assignJobCandidate.ID.as("assignmentId"),
					portalStatus.STATUS_ID.cast(Integer.class).as("portalStatusId"),
					candidate.OWNERID.as(FIELD_OWNER_ID))
			.from(timesheet)
			.innerJoin(tsSetting)
			.on(tsSetting.ID.eq(timesheet.TIMESHEET_SETTING_ID))
			.innerJoin(tsAssociation)
			.on(tsAssociation.ID.eq(tsSetting.ASSOCIATION_ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsAssociation.CONTRACTOR_ID))
			.leftJoin(job)
			.on(tsAssociation.JOB_ID.eq(job.ID))
			.leftJoin(assignJobCandidate)
			.on(assignJobCandidate.JOBID.eq(job.ID).and(assignJobCandidate.CANDIDATEID.eq(tsAssociation.CONTRACTOR_ID)))
			.leftJoin(portalStatus)
			.on(portalStatus.CONTRACTOR_ID.cast(Integer.class).eq(candidate.ID))
			.where(timesheet.ID.in(timesheetIds).and(timesheet.ACCOUNT_ID.eq(accountId)))
			.fetch()
			.into(TimesheetCandidateEmailQueryResultDto.class);
	}

	private List<TimesheetCandidateEmailQueryResultDto> fetchWithoutAssignment(List<Integer> timesheetIds,
			Integer accountId, CstTimesheetT timesheet, CstTimesheetSettingT tsSetting,
			CstTimesheetSettingAssociationT tsAssociation, Tblcandidate candidate,
			Field<Integer> latestApprovalStatus) {
		return this.auroraDbDSLContext
			.select(timesheet.ID.as(FIELD_TIMESHEET_ID_CAMEL), candidate.ID.as("candidateId"),
					candidate.FIRSTNAME.as(FIELD_FIRST_NAME), candidate.LASTNAME.as(FIELD_LAST_NAME),
					candidate.SRNO.as("srno"), candidate.SLUG.as("slug"), candidate.EMAILID.as(FIELD_EMAIL_ID),
					candidate.EMAIL_OPT_OUT.as(FIELD_EMAIL_OPT_OUT), candidate.DELETED.as(FIELD_DELETED),
					latestApprovalStatus, candidate.OWNERID.as(FIELD_OWNER_ID))
			.from(timesheet)
			.innerJoin(tsSetting)
			.on(tsSetting.ID.eq(timesheet.TIMESHEET_SETTING_ID))
			.innerJoin(tsAssociation)
			.on(tsAssociation.ID.eq(tsSetting.ASSOCIATION_ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsAssociation.CONTRACTOR_ID))
			.where(timesheet.ID.in(timesheetIds).and(timesheet.ACCOUNT_ID.eq(accountId)))
			.fetch()
			.into(TimesheetCandidateEmailQueryResultDto.class);
	}

	/**
	 * Loads client portal status ids from {@code client_portal_status_t} keyed by
	 * {@code vms_user_email} for the given account.
	 */
	public Map<String, Integer> getClientPortalStatusByEmails(List<String> emails, Integer accountId) {
		if ((emails == null) || emails.isEmpty()) {
			return Collections.emptyMap();
		}
		Table<?> clientPortalStatus = DSL.table(DSL.name(TABLE_CLIENT_PORTAL_STATUS));
		Field<String> vmsUserEmail = DSL.field(DSL.name(TABLE_CLIENT_PORTAL_STATUS, FIELD_VMS_USER_EMAIL),
				String.class);
		Field<Integer> portalAccountId = DSL.field(DSL.name(TABLE_CLIENT_PORTAL_STATUS, "account_id"), Integer.class);
		Field<Integer> portalStatusId = DSL.field(DSL.name(TABLE_CLIENT_PORTAL_STATUS, FIELD_PORTAL_STATUS_ID),
				Integer.class);

		return this.auroraDbDSLContext.select(vmsUserEmail, portalStatusId)
			.from(clientPortalStatus)
			.where(vmsUserEmail.in(emails).and(portalAccountId.eq(accountId)))
			.fetchMap(vmsUserEmail, portalStatusId);
	}

}
