package io.recruitcrm.microservice.timesheet.repositories.contractor;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectLimitStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.EntityOffLimitT;
import io.recruitcrm.microservice.search.models.jooq.tables.OffLimitStatusT;
import io.recruitcrm.microservice.search.models.jooq.tables.OffLimitStatusColourT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblassignjobcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldealcandidates;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldeals;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcompany;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblgender;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbljob;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbljobstatus;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbldealpipelinestages;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbluser;
import io.recruitcrm.microservice.timesheet.helpers.enums.ContractorStatus;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityTypeEnum;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorDealQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.repositories.SortingQueryBuilder;

@Repository
public class ContractorRepository implements IContractorRepository {

	private static final String OWNER_ALIAS = "owner";

	private static final String PHONE_ALIAS = "phone";

	private static final String EMAIL_ALIAS = "email";

	private static final String COUNTRY_ALIAS = "country";

	private static final String OWNER_ID_ALIAS = "ownerId";

	private static final String OWNER_NAME_ALIAS = "ownerName";

	private static final String ADDED_BY_ID_ALIAS = "addedById";

	private static final String ADDED_ON_ALIAS = "addedOn";

	private static final String ADDED_BY_NAME_ALIAS = "addedByName";

	private static final String UPDATED_ON_ALIAS = "updatedOn";

	private static final String UPDATED_BY_ID_ALIAS = "updatedById";

	private static final String UPDATED_BY_NAME_ALIAS = "updatedByName";

	private static final String JOB_TYPE_CONTRACT = "contract";

	private static final String JOB_TYPE_CONTRACT_TO_PERMANENT = "contracttopermanent";

	private static final String DEAL_NAME_ALIAS = "dealName";

	private static final String STATUS_ALIAS = "status";

	private static final String GENDER_SORT_FIELD = "gender";

	private static final String GENDER_ID_ALIAS = "genderid";

	private static final String ADDED_BY_TABLE_ALIAS = "addedBy";

	private static final String UPDATED_BY_TABLE_ALIAS = "updatedBy";

	private static final String PHOTO_ALIAS = "photo";

	private static final String POSITION_ALIAS = "position";

	private static final String TITLE_ALIAS = "title";

	private static final String STATE_ALIAS = "state";

	private static final String POSTAL_CODE_ALIAS = "postalCode";

	private static final String LOCALITY_ALIAS = "locality";

	private static final String FULL_ADDRESS_ALIAS = "fullAddress";

	private static final String BIRTH_DATE_ALIAS = "birthDate";

	private static final String CURRENT_ORGANIZATION_ALIAS = "currentOrganization";

	private static final String SKILLS_ALIAS = "skills";

	private static final String PROFILE_FACEBOOK_ALIAS = "profilefacebook";

	private static final String PROFILE_GITHUB_ALIAS = "profilegithub";

	private static final String PROFILE_LINKEDIN_ALIAS = "profilelinkedin";

	private static final String PROFILE_TWITTER_ALIAS = "profiletwitter";

	private static final String PROFILE_XING_ALIAS = "profilexing";

	private static final String SOURCE_ALIAS = "source";

	private final DSLContext auroraDbDSLContext;

	private final SortingQueryBuilder sortingQueryBuilder;

	private final AccessControlHelper accessControlHelper;

	public ContractorRepository(DSLContext auroraDbDSLContext, SortingQueryBuilder sortingQueryBuilder,
			AccessControlHelper accessControlHelper) {
		this.auroraDbDSLContext = auroraDbDSLContext;
		this.sortingQueryBuilder = sortingQueryBuilder;
		this.accessControlHelper = accessControlHelper;
	}

	@Override
	public List<ContractorQueryResultDto> getContractorsListByIds(List<Integer> contractorIds,
			List<SortPriorityRequestBodyDto> sortPriorityList, Integer accountId) {
		if (contractorIds == null || contractorIds.isEmpty()) {
			return List.of();
		}

		var candidate = Tblcandidate.TBLCANDIDATE;
		var owner = Tbluser.TBLUSER.as(OWNER_ALIAS);
		var addedBy = Tbluser.TBLUSER.as(ADDED_BY_TABLE_ALIAS);
		var updatedBy = Tbluser.TBLUSER.as(UPDATED_BY_TABLE_ALIAS);
		var gender = Tblgender.TBLGENDER;

		// Current epoch (seconds) used by the Status / Job Name sort keys to mirror the
		// "active assignment" check in ContractorService (current date within job dates),
		// so the filtered list sorts by the same values the grid displays.
		int currentEpoch = (int) Instant.now().getEpochSecond();

		// Off-limit tables for contractor
		var contractorEntityOffLimitT = EntityOffLimitT.ENTITY_OFF_LIMIT_T;
		var contractorOffLimitStatusT = OffLimitStatusT.OFF_LIMIT_STATUS_T;
		var contractorOffLimitStatusColourT = OffLimitStatusColourT.OFF_LIMIT_STATUS_COLOUR_T
			.as("contractorOffLimitStatusColourT");
		var contractorOffLimitMarkedByUser = Tbluser.TBLUSER.as("contractorOffLimitMarkedByUser");

		SelectConditionStep<?> baseQuery = DSL.select(
				// Contractor fields
				candidate.ID.as("id"),
				// Active-assignment job name / first deal name — sort keys only (ignored
				// by
				// DTO mapping; jobName/dealName are populated separately). jobName
				// mirrors
				// the "active job" the grid shows (ContractorService.enrichWithJobs) and
				// the
				// deal name is account-scoped, so the filtered list sorts by the
				// displayed
				// value — identical to the no-filter path (getAllContractorsByAccountId).
				this.buildActiveJobNameSortField(accountId, currentEpoch), this.buildDealNameSortField(accountId),
				this.buildFullNameField(candidate.FIRSTNAME, candidate.LASTNAME, "name"),
				candidate.PROFILEPIC.as(PHOTO_ALIAS), candidate.SLUG.as("slug"), candidate.POSITION.as(POSITION_ALIAS),
				candidate.EMAILID.as(EMAIL_ALIAS), candidate.CONTACTNUMBER.as(PHONE_ALIAS),
				candidate.COUNTRY.as(COUNTRY_ALIAS),

				// Owner fields
				candidate.OWNERID.as(OWNER_ID_ALIAS),
				this.buildFullNameField(owner.FIRSTNAME, owner.LASTNAME, OWNER_NAME_ALIAS),

				// Added fields
				candidate.CREATEDON.as(ADDED_ON_ALIAS), candidate.CREATEDBY.as(ADDED_BY_ID_ALIAS),
				this.buildFullNameField(addedBy.FIRSTNAME, addedBy.LASTNAME, ADDED_BY_NAME_ALIAS),

				// Updated fields
				candidate.UPDATEDON.as(UPDATED_ON_ALIAS), candidate.UPDATEDBY.as(UPDATED_BY_ID_ALIAS),
				this.buildFullNameField(updatedBy.FIRSTNAME, updatedBy.LASTNAME, UPDATED_BY_NAME_ALIAS),

				// Additional contractor fields
				candidate.POSITION.as(TITLE_ALIAS), candidate.CITY.as("city"), candidate.STATE.as(STATE_ALIAS),
				candidate.POSTAL_CODE.as(POSTAL_CODE_ALIAS), candidate.LOCALITY.as(LOCALITY_ALIAS),
				candidate.ADDRESS.as(FULL_ADDRESS_ALIAS), candidate.CANDIDATEDOB.as(BIRTH_DATE_ALIAS),
				candidate.LASTORGANISATION.as(CURRENT_ORGANIZATION_ALIAS), candidate.SKILL.as(SKILLS_ALIAS),
				candidate.PROFILEFACEBOOK.as(PROFILE_FACEBOOK_ALIAS), candidate.PROFILEGITHUB.as(PROFILE_GITHUB_ALIAS),
				candidate.PROFILELINKEDIN.as(PROFILE_LINKEDIN_ALIAS),
				candidate.PROFILETWITTER.as(PROFILE_TWITTER_ALIAS), candidate.PROFILEXING.as(PROFILE_XING_ALIAS),
				candidate.SOURCE.as(SOURCE_ALIAS), gender.LABEL.as(GENDER_SORT_FIELD),
				// Gender sort key — mirrors the candidate list page, which sorts the
				// Gender column by the underlying gender id (Not Available, Male, Female,
				// Non Binary, Prefer not to say) rather than alphabetically by label. The
				// "gender" sort field is remapped onto this alias (see remapGenderSort).
				candidate.GENDERID.as(GENDER_ID_ALIAS),

				// Contractor off-limit fields
				contractorEntityOffLimitT.OFF_LIMIT_STATUS_ID.as("contractorOffLimitStatusId"),
				// Status sort key (assigned=1 / available=0), mirroring
				// ContractorService.enrichWithStatus so the Status column sorts by the
				// badge
				// shown rather than by the unrelated off-limit status id — identical to
				// the
				// no-filter path (getAllContractorsByAccountId).
				DSL.when(this.buildActiveContractAssignmentCondition(accountId, currentEpoch),
						DSL.inline(ContractorStatus.ASSIGNED.getValue()))
					.otherwise(DSL.inline(ContractorStatus.AVAILABLE.getValue()))
					.as(STATUS_ALIAS),
				contractorEntityOffLimitT.OFF_LIMIT_REASON.as("contractorOffLimitReason"),
				contractorEntityOffLimitT.OFF_LIMIT_END_DATE.as("contractorOffLimitEndDate"),
				contractorEntityOffLimitT.CREATED_ON.as("contractorOffLimitStartDate"),
				contractorOffLimitStatusT.STATUS_LABEL.as("contractorStatusLabel"),
				contractorOffLimitStatusColourT.BACKGROUND_COLOR_HEX.as("contractorBackgroundColorHex"),
				contractorOffLimitStatusColourT.TEXT_COLOR_HEX.as("contractorTextColorHex"), DSL
					.when(contractorOffLimitMarkedByUser.LASTNAME.isNull()
						.or(contractorOffLimitMarkedByUser.LASTNAME.eq("")), contractorOffLimitMarkedByUser.FIRSTNAME)
					.otherwise(DSL.concat(contractorOffLimitMarkedByUser.FIRSTNAME, DSL.val(" "),
							contractorOffLimitMarkedByUser.LASTNAME))
					.as("contractorMarkedByName"))
			.from(candidate)
			.leftJoin(owner)
			.on(owner.ID.eq(candidate.OWNERID))
			.leftJoin(addedBy)
			.on(addedBy.ID.eq(candidate.CREATEDBY))
			.leftJoin(updatedBy)
			.on(updatedBy.ID.eq(candidate.UPDATEDBY))
			.leftJoin(gender)
			.on(candidate.GENDERID.eq(gender.ID))
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
			.where(candidate.ID.in(contractorIds));

		SelectLimitStep<?> finalQuery = this.buildSortedQuery(baseQuery, sortPriorityList, candidate, owner, addedBy,
				updatedBy);

		return this.auroraDbDSLContext.fetch(finalQuery).into(ContractorQueryResultDto.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getContractorIdsPage(CommonTableExpression<?> cte, String cteName,
			List<SortPriorityRequestBodyDto> sortPriorityList, Integer accountId, Pageable pageable) {
		var candidate = Tblcandidate.TBLCANDIDATE;
		var owner = Tbluser.TBLUSER.as(OWNER_ALIAS);
		var addedBy = Tbluser.TBLUSER.as(ADDED_BY_TABLE_ALIAS);
		var updatedBy = Tbluser.TBLUSER.as(UPDATED_BY_TABLE_ALIAS);

		// Same active-assignment "now" the Status / Job Name sort keys mirror in the data
		// query (getContractorsListByIds).
		int currentEpoch = (int) Instant.now().getEpochSecond();

		Field<Integer> cteIdField = DSL.field(DSL.name(cteName, "id"), Integer.class);
		Condition accessControlCondition = this.accessControlHelper
			.buildCandidatesAccessControlCondition(candidate.OWNERID);

		SelectConditionStep<?> baseQuery = DSL.with(cte)
			.select(candidate.ID.as("id"), this.buildActiveJobNameSortField(accountId, currentEpoch),
					this.buildDealNameSortField(accountId),
					this.buildFullNameField(candidate.FIRSTNAME, candidate.LASTNAME, "name"),
					candidate.PROFILEPIC.as(PHOTO_ALIAS), candidate.SLUG.as("slug"),
					candidate.POSITION.as(POSITION_ALIAS), candidate.EMAILID.as(EMAIL_ALIAS),
					candidate.CONTACTNUMBER.as(PHONE_ALIAS), candidate.COUNTRY.as(COUNTRY_ALIAS),
					candidate.OWNERID.as(OWNER_ID_ALIAS),
					this.buildFullNameField(owner.FIRSTNAME, owner.LASTNAME, OWNER_NAME_ALIAS),
					candidate.CREATEDON.as(ADDED_ON_ALIAS), candidate.CREATEDBY.as(ADDED_BY_ID_ALIAS),
					this.buildFullNameField(addedBy.FIRSTNAME, addedBy.LASTNAME, ADDED_BY_NAME_ALIAS),
					candidate.UPDATEDON.as(UPDATED_ON_ALIAS), candidate.UPDATEDBY.as(UPDATED_BY_ID_ALIAS),
					this.buildFullNameField(updatedBy.FIRSTNAME, updatedBy.LASTNAME, UPDATED_BY_NAME_ALIAS),
					candidate.POSITION.as(TITLE_ALIAS), candidate.CITY.as("city"), candidate.STATE.as(STATE_ALIAS),
					candidate.POSTAL_CODE.as(POSTAL_CODE_ALIAS), candidate.LOCALITY.as(LOCALITY_ALIAS),
					candidate.ADDRESS.as(FULL_ADDRESS_ALIAS), candidate.CANDIDATEDOB.as(BIRTH_DATE_ALIAS),
					candidate.LASTORGANISATION.as(CURRENT_ORGANIZATION_ALIAS), candidate.SKILL.as(SKILLS_ALIAS),
					candidate.PROFILEFACEBOOK.as(PROFILE_FACEBOOK_ALIAS),
					candidate.PROFILEGITHUB.as(PROFILE_GITHUB_ALIAS),
					candidate.PROFILELINKEDIN.as(PROFILE_LINKEDIN_ALIAS),
					candidate.PROFILETWITTER.as(PROFILE_TWITTER_ALIAS), candidate.PROFILEXING.as(PROFILE_XING_ALIAS),
					candidate.SOURCE.as(SOURCE_ALIAS), candidate.GENDERID.as(GENDER_ID_ALIAS),
					DSL.when(this.buildActiveContractAssignmentCondition(accountId, currentEpoch),
							DSL.inline(ContractorStatus.ASSIGNED.getValue()))
						.otherwise(DSL.inline(ContractorStatus.AVAILABLE.getValue()))
						.as(STATUS_ALIAS))
			.from(DSL.name(cteName))
			.innerJoin(candidate)
			.on(candidate.ID.eq(cteIdField))
			.leftJoin(owner)
			.on(owner.ID.eq(candidate.OWNERID))
			.leftJoin(addedBy)
			.on(addedBy.ID.eq(candidate.CREATEDBY))
			.leftJoin(updatedBy)
			.on(updatedBy.ID.eq(candidate.UPDATEDBY))
			.where(accessControlCondition);

		SelectLimitStep<?> finalQuery = this.buildSortedQuery(baseQuery, sortPriorityList, candidate, owner, addedBy,
				updatedBy);

		Result<Record> result = (Result<Record>) this.auroraDbDSLContext
			.fetch(finalQuery.offset(pageable.getPageNumber() * pageable.getPageSize()).limit(pageable.getPageSize()));

		return result.stream().map((rec) -> rec.get("id", Integer.class)).toList();
	}

	@Override
	public List<ContractorJobQueryResultDto> getJobsByContractorIds(List<Integer> contractorIds, Integer accountId) {
		if (contractorIds == null || contractorIds.isEmpty()) {
			return List.of();
		}

		var candidate = Tblcandidate.TBLCANDIDATE;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;
		var job = Tbljob.TBLJOB;
		var company = Tblcompany.TBLCOMPANY;
		var jobStatus = Tbljobstatus.TBLJOBSTATUS;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingInner = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tsSettingInner");
		var tsSettingAssocInner = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T
			.as("tsSettingAssocInner");

		var query = DSL
			.select(candidate.ID.as("contractorId"), job.ID.as("jobId"), job.NAME.as("jobName"), job.SLUG.as("jobSlug"),
					company.COMPANYNAME.as("companyName"), company.SLUG.as("companySlug"),
					tsSetting.JOB_START_DATE.as("jobStartDate"), tsSetting.JOB_END_DATE.as("jobEndDate"),
					jobStatus.LABEL.as("jobStatus"), job.SRNO.as("jobSrno"))
			.from(candidate)
			.innerJoin(assignJobCandidate)
			.on(assignJobCandidate.CANDIDATEID.eq(candidate.ID))
			.innerJoin(job)
			.on(job.ID.eq(assignJobCandidate.JOBID))
			.leftJoin(jobStatus)
			.on(job.JOBSTATUS.eq(jobStatus.ID))
			.innerJoin(tsSettingAssoc)
			.on(tsSettingAssoc.JOB_ID.eq(job.ID).and(tsSettingAssoc.CONTRACTOR_ID.eq(candidate.ID)))
			.innerJoin(tsSetting)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(company)
			.on(company.ID.eq(job.COMPANYID))
			.where(candidate.ID.in(contractorIds))
			.and(job.ACCOUNTID.eq(accountId))
			.and(job.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT))
			.and(tsSetting.ID.eq(DSL.select(DSL.max(tsSettingInner.ID))
				.from(tsSettingInner)
				.innerJoin(tsSettingAssocInner)
				.on(tsSettingInner.ASSOCIATION_ID.eq(tsSettingAssocInner.ID))
				.where(tsSettingAssocInner.JOB_ID.eq(job.ID).and(tsSettingAssocInner.CONTRACTOR_ID.eq(candidate.ID)))))
			.orderBy(candidate.ID.asc(), assignJobCandidate.ID.asc());

		return this.auroraDbDSLContext.fetch(query).into(ContractorJobQueryResultDto.class);
	}

	@Override
	public List<ContractorDealQueryResultDto> getDealsByContractorIds(List<Integer> contractorIds, Integer accountId) {
		if (contractorIds == null || contractorIds.isEmpty()) {
			return List.of();
		}

		var candidate = Tblcandidate.TBLCANDIDATE;
		var dealCandidates = Tbldealcandidates.TBLDEALCANDIDATES;
		var deals = Tbldeals.TBLDEALS;
		var owner = Tbluser.TBLUSER.as(OWNER_ALIAS);
		var dealPipelineStages = Tbldealpipelinestages.TBLDEALPIPELINESTAGES;

		var query = DSL
			.select(candidate.ID.as("contractorId"), deals.ID.as("dealId"), deals.NAME.as(DEAL_NAME_ALIAS),
					DSL.when(owner.LASTNAME.isNull().or(owner.LASTNAME.eq("")), owner.FIRSTNAME)
						.otherwise(DSL.concat(owner.FIRSTNAME, DSL.val(" "), owner.LASTNAME))
						.as(OWNER_NAME_ALIAS),
					deals.SRNO.as("serialNumber"), deals.SLUG.as("slug"), dealPipelineStages.LABEL.as(STATUS_ALIAS))
			.from(candidate)
			.innerJoin(dealCandidates)
			.on(dealCandidates.CANDIDATEID.eq(candidate.ID))
			.innerJoin(deals)
			.on(deals.ID.eq(dealCandidates.DEALID))
			.leftJoin(owner)
			.on(owner.ID.eq(deals.OWNERID))
			.leftJoin(dealPipelineStages)
			.on(deals.DEALSTAGE.eq(dealPipelineStages.ID))
			.where(candidate.ID.in(contractorIds))
			.and(deals.ACCOUNTID.eq(accountId))
			.orderBy(candidate.ID.asc(), dealCandidates.ID.asc());

		return this.auroraDbDSLContext.fetch(query).into(ContractorDealQueryResultDto.class);
	}

	@Override
	public List<ContractorQueryResultDto> getAllContractorsByAccountId(Integer accountId,
			List<SortPriorityRequestBodyDto> sortPriorityList, Pageable pageable) {
		var candidate = Tblcandidate.TBLCANDIDATE;
		var owner = Tbluser.TBLUSER.as(OWNER_ALIAS);
		var addedBy = Tbluser.TBLUSER.as(ADDED_BY_TABLE_ALIAS);
		var updatedBy = Tbluser.TBLUSER.as(UPDATED_BY_TABLE_ALIAS);
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;
		var job = Tbljob.TBLJOB;
		var gender = Tblgender.TBLGENDER;

		// Current epoch (seconds) used by the Status / Job Name sort keys to mirror the
		// "active assignment" check in ContractorService (current date within job dates).
		int currentEpoch = (int) Instant.now().getEpochSecond();

		// Aliased tables used only by the dealName sort sub-select below.
		var dcForName = Tbldealcandidates.TBLDEALCANDIDATES.as("dcForName");
		var dealForName = Tbldeals.TBLDEALS.as("dealForName");

		// Timesheet setting association table
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;

		// Off-limit tables for contractor
		var contractorEntityOffLimitT = EntityOffLimitT.ENTITY_OFF_LIMIT_T;
		var contractorOffLimitStatusT = OffLimitStatusT.OFF_LIMIT_STATUS_T;
		var contractorOffLimitStatusColourT = OffLimitStatusColourT.OFF_LIMIT_STATUS_COLOUR_T
			.as("contractorOffLimitStatusColourT");
		var contractorOffLimitMarkedByUser = Tbluser.TBLUSER.as("contractorOffLimitMarkedByUser");

		Condition candidatesAccessControlCondition = this.accessControlHelper
			.buildCandidatesAccessControlCondition(candidate.OWNERID);

		SelectConditionStep<?> baseQuery = DSL.selectDistinct(
				// Contractor fields
				candidate.ID.as("id"),
				// Active-assignment job name / first deal name — sort keys only (ignored
				// by
				// DTO mapping; jobName/dealName are populated separately). jobName
				// mirrors the
				// "active job" the grid shows (see ContractorService.enrichWithJobs).
				this.buildActiveJobNameSortField(accountId, currentEpoch),
				DSL.field(DSL.select(dealForName.NAME)
					.from(dcForName)
					.innerJoin(dealForName)
					.on(dealForName.ID.eq(dcForName.DEALID))
					.where(dcForName.CANDIDATEID.eq(candidate.ID))
					.and(dealForName.ACCOUNTID.eq(accountId))
					.orderBy(dcForName.ID.asc())
					.limit(1)).as(DEAL_NAME_ALIAS),
				DSL.when(candidate.LASTNAME.isNull().or(candidate.LASTNAME.eq("")), candidate.FIRSTNAME)
					.otherwise(DSL.concat(candidate.FIRSTNAME, DSL.val(" "), candidate.LASTNAME))
					.as("name"),
				candidate.PROFILEPIC.as(PHOTO_ALIAS), candidate.SLUG.as("slug"), candidate.POSITION.as(POSITION_ALIAS),
				candidate.EMAILID.as(EMAIL_ALIAS), candidate.CONTACTNUMBER.as(PHONE_ALIAS),
				candidate.COUNTRY.as(COUNTRY_ALIAS),

				// Owner fields
				candidate.OWNERID.as(OWNER_ID_ALIAS),
				DSL.when(owner.LASTNAME.isNull().or(owner.LASTNAME.eq("")), owner.FIRSTNAME)
					.otherwise(DSL.concat(owner.FIRSTNAME, DSL.val(" "), owner.LASTNAME))
					.as(OWNER_NAME_ALIAS),

				// Added fields
				candidate.CREATEDON.as(ADDED_ON_ALIAS), candidate.CREATEDBY.as(ADDED_BY_ID_ALIAS),
				DSL.when(addedBy.LASTNAME.isNull().or(addedBy.LASTNAME.eq("")), addedBy.FIRSTNAME)
					.otherwise(DSL.concat(addedBy.FIRSTNAME, DSL.val(" "), addedBy.LASTNAME))
					.as(ADDED_BY_NAME_ALIAS),

				// Updated fields
				candidate.UPDATEDON.as(UPDATED_ON_ALIAS), candidate.UPDATEDBY.as(UPDATED_BY_ID_ALIAS),
				DSL.when(updatedBy.LASTNAME.isNull().or(updatedBy.LASTNAME.eq("")), updatedBy.FIRSTNAME)
					.otherwise(DSL.concat(updatedBy.FIRSTNAME, DSL.val(" "), updatedBy.LASTNAME))
					.as(UPDATED_BY_NAME_ALIAS),

				// Additional contractor fields
				candidate.POSITION.as(TITLE_ALIAS), candidate.CITY.as("city"), candidate.STATE.as(STATE_ALIAS),
				candidate.POSTAL_CODE.as(POSTAL_CODE_ALIAS), candidate.LOCALITY.as(LOCALITY_ALIAS),
				candidate.ADDRESS.as(FULL_ADDRESS_ALIAS), candidate.CANDIDATEDOB.as(BIRTH_DATE_ALIAS),
				candidate.LASTORGANISATION.as(CURRENT_ORGANIZATION_ALIAS), candidate.SKILL.as(SKILLS_ALIAS),
				candidate.PROFILEFACEBOOK.as(PROFILE_FACEBOOK_ALIAS), candidate.PROFILEGITHUB.as(PROFILE_GITHUB_ALIAS),
				candidate.PROFILELINKEDIN.as(PROFILE_LINKEDIN_ALIAS),
				candidate.PROFILETWITTER.as(PROFILE_TWITTER_ALIAS), candidate.PROFILEXING.as(PROFILE_XING_ALIAS),
				candidate.SOURCE.as(SOURCE_ALIAS), gender.LABEL.as(GENDER_SORT_FIELD),
				// Gender sort key — mirrors the candidate list page, which sorts the
				// Gender column by the underlying gender id (Not Available, Male, Female,
				// Non Binary, Prefer not to say) rather than alphabetically by label. The
				// "gender" sort field is remapped onto this alias (see remapGenderSort).
				candidate.GENDERID.as(GENDER_ID_ALIAS),

				// Contractor off-limit fields
				contractorEntityOffLimitT.OFF_LIMIT_STATUS_ID.as("contractorOffLimitStatusId"),
				// Status sort key (assigned=1 / available=0), mirroring
				// ContractorService.enrichWithStatus so the column sorts by the badge
				// shown
				// rather than by the unrelated off-limit status id.
				DSL.when(this.buildActiveContractAssignmentCondition(accountId, currentEpoch),
						DSL.inline(ContractorStatus.ASSIGNED.getValue()))
					.otherwise(DSL.inline(ContractorStatus.AVAILABLE.getValue()))
					.as(STATUS_ALIAS),
				contractorEntityOffLimitT.OFF_LIMIT_REASON.as("contractorOffLimitReason"),
				contractorEntityOffLimitT.OFF_LIMIT_END_DATE.as("contractorOffLimitEndDate"),
				contractorEntityOffLimitT.CREATED_ON.as("contractorOffLimitStartDate"),
				contractorOffLimitStatusT.STATUS_LABEL.as("contractorStatusLabel"),
				contractorOffLimitStatusColourT.BACKGROUND_COLOR_HEX.as("contractorBackgroundColorHex"),
				contractorOffLimitStatusColourT.TEXT_COLOR_HEX.as("contractorTextColorHex"), DSL
					.when(contractorOffLimitMarkedByUser.LASTNAME.isNull()
						.or(contractorOffLimitMarkedByUser.LASTNAME.eq("")), contractorOffLimitMarkedByUser.FIRSTNAME)
					.otherwise(DSL.concat(contractorOffLimitMarkedByUser.FIRSTNAME, DSL.val(" "),
							contractorOffLimitMarkedByUser.LASTNAME))
					.as("contractorMarkedByName"))
			.from(candidate)
			.innerJoin(assignJobCandidate)
			.on(assignJobCandidate.CANDIDATEID.eq(candidate.ID))
			.innerJoin(job)
			.on(job.ID.eq(assignJobCandidate.JOBID))
			.innerJoin(tsSettingAssoc)
			.on(tsSettingAssoc.JOB_ID.eq(job.ID).and(tsSettingAssoc.CONTRACTOR_ID.eq(candidate.ID)))
			.leftJoin(owner)
			.on(owner.ID.eq(candidate.OWNERID))
			.leftJoin(addedBy)
			.on(addedBy.ID.eq(candidate.CREATEDBY))
			.leftJoin(updatedBy)
			.on(updatedBy.ID.eq(candidate.UPDATEDBY))
			.leftJoin(gender)
			.on(candidate.GENDERID.eq(gender.ID))
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
			.where(candidate.ACCOUNTID.eq(accountId))
			.and(job.ACCOUNTID.eq(accountId))
			.and(job.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT))
			.and(candidatesAccessControlCondition);

		SelectLimitStep<?> finalQuery = this.buildSortedQuery(baseQuery, sortPriorityList, candidate, owner, addedBy,
				updatedBy);

		// Apply pagination
		finalQuery = (SelectLimitStep<?>) finalQuery.offset(pageable.getPageNumber() * pageable.getPageSize())
			.limit(pageable.getPageSize());

		return this.auroraDbDSLContext.fetch(finalQuery).into(ContractorQueryResultDto.class);
	}

	/**
	 * Mirror the candidate list page: the Gender column sorts by the underlying gender id
	 * (Not Available, Male, Female, Non Binary, Prefer not to say) rather than
	 * alphabetically by label. Any incoming {@code gender} sort priority is remapped onto
	 * the projected {@link #GENDER_ID_ALIAS} sort key; all other priorities pass through
	 * unchanged. The requested sort direction is preserved.
	 */
	private List<SortPriorityRequestBodyDto> remapGenderSort(List<SortPriorityRequestBodyDto> sortPriorityList) {
		if (sortPriorityList == null) {
			return List.of();
		}
		return sortPriorityList.stream()
			.map((sort) -> GENDER_SORT_FIELD.equalsIgnoreCase(sort.getField())
					? new SortPriorityRequestBodyDto(GENDER_ID_ALIAS, sort.getOrder()) : sort)
			.toList();
	}

	private Condition buildActiveContractAssignmentCondition(Integer accountId, int currentEpoch) {
		var candidate = Tblcandidate.TBLCANDIDATE;
		var acj = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE.as("statusAcj");
		var jobT = Tbljob.TBLJOB.as("statusJob");
		var tsa = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T.as("statusTsa");
		var ts = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("statusTs");
		var tsInner = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("statusTsInner");
		var tsaInner = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T.as("statusTsaInner");
		return DSL.exists(DSL.selectOne()
			.from(acj)
			.innerJoin(jobT)
			.on(jobT.ID.eq(acj.JOBID))
			.innerJoin(tsa)
			.on(tsa.JOB_ID.eq(jobT.ID).and(tsa.CONTRACTOR_ID.eq(acj.CANDIDATEID)))
			.innerJoin(ts)
			.on(ts.ASSOCIATION_ID.eq(tsa.ID))
			.where(acj.CANDIDATEID.eq(candidate.ID))
			.and(jobT.ACCOUNTID.eq(accountId))
			.and(jobT.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT))
			.and(DSL.val(currentEpoch).between(ts.JOB_START_DATE, ts.JOB_END_DATE))
			.and(ts.ID.eq(DSL.select(DSL.max(tsInner.ID))
				.from(tsInner)
				.innerJoin(tsaInner)
				.on(tsInner.ASSOCIATION_ID.eq(tsaInner.ID))
				.where(tsaInner.JOB_ID.eq(jobT.ID).and(tsaInner.CONTRACTOR_ID.eq(candidate.ID))))));
	}

	private Field<String> buildActiveJobNameSortField(Integer accountId, int currentEpoch) {
		var candidate = Tblcandidate.TBLCANDIDATE;
		var acj = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE.as("jobNameAcj");
		var jobT = Tbljob.TBLJOB.as("jobNameJob");
		var tsa = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T.as("jobNameTsa");
		var ts = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("jobNameTs");
		var tsInner = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("jobNameTsInner");
		var tsaInner = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T.as("jobNameTsaInner");
		return DSL
			.field(DSL.select(jobT.NAME)
				.from(acj)
				.innerJoin(jobT)
				.on(jobT.ID.eq(acj.JOBID))
				.innerJoin(tsa)
				.on(tsa.JOB_ID.eq(jobT.ID).and(tsa.CONTRACTOR_ID.eq(acj.CANDIDATEID)))
				.innerJoin(ts)
				.on(ts.ASSOCIATION_ID.eq(tsa.ID))
				.where(acj.CANDIDATEID.eq(candidate.ID))
				.and(jobT.ACCOUNTID.eq(accountId))
				.and(jobT.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT))
				.and(DSL.val(currentEpoch).between(ts.JOB_START_DATE, ts.JOB_END_DATE))
				.and(ts.ID.eq(DSL.select(DSL.max(tsInner.ID))
					.from(tsInner)
					.innerJoin(tsaInner)
					.on(tsInner.ASSOCIATION_ID.eq(tsaInner.ID))
					.where(tsaInner.JOB_ID.eq(jobT.ID).and(tsaInner.CONTRACTOR_ID.eq(candidate.ID)))))
				.orderBy(acj.ID.asc())
				.limit(1))
			.as("jobName");
	}

	private Field<String> buildDealNameSortField(Integer accountId) {
		var candidate = Tblcandidate.TBLCANDIDATE;
		var dcForName = Tbldealcandidates.TBLDEALCANDIDATES.as("dcForName");
		var dealForName = Tbldeals.TBLDEALS.as("dealForName");
		return DSL
			.field(DSL.select(dealForName.NAME)
				.from(dcForName)
				.innerJoin(dealForName)
				.on(dealForName.ID.eq(dcForName.DEALID))
				.where(dcForName.CANDIDATEID.eq(candidate.ID))
				.and(dealForName.ACCOUNTID.eq(accountId))
				.orderBy(dcForName.ID.asc())
				.limit(1))
			.as(DEAL_NAME_ALIAS);
	}

	private Field<String> buildFullNameField(Field<String> firstName, Field<String> lastName, String alias) {
		return DSL.when(lastName.isNull().or(lastName.eq("")), firstName)
			.otherwise(DSL.concat(firstName, DSL.val(" "), lastName))
			.as(alias);
	}

	/**
	 * Build the final, ordered query shared by the contractor list / id-page queries.
	 * Remaps any {@code gender} sort onto the gender-id sort key, then applies
	 * multi-table sorting when valid sort fields are present; otherwise falls back to the
	 * default updatedOn/id descending order. Pagination (if any) is applied by the
	 * caller.
	 */
	private SelectLimitStep<?> buildSortedQuery(SelectConditionStep<?> baseQuery,
			List<SortPriorityRequestBodyDto> sortPriorityList, Tblcandidate candidate, Table<?> owner, Table<?> addedBy,
			Table<?> updatedBy) {
		Map<String, Table<?>> fieldTableMapping = this.createFieldTableMapping(candidate, owner, addedBy, updatedBy);

		// Remap a "gender" sort onto the gender-id sort key so the column orders by
		// gender
		// id (NA, Male, Female, Non Binary, Prefer not to say) like the candidate list
		// page, rather than alphabetically by label.
		List<SortPriorityRequestBodyDto> resolvedSortPriorityList = this.remapGenderSort(sortPriorityList);

		if (resolvedSortPriorityList != null && !resolvedSortPriorityList.isEmpty()) {
			// Check if there are any valid (non-null, non-empty) sort fields
			boolean hasValidSortFields = resolvedSortPriorityList.stream()
				.anyMatch((sort) -> sort.getField() != null && !sort.getField().trim().isEmpty());

			if (hasValidSortFields) {
				return this.sortingQueryBuilder.addSortingQuery(baseQuery, resolvedSortPriorityList, fieldTableMapping,
						candidate);
			}
		}

		// No sort priorities, or all sort fields invalid: fall back to default sorting.
		return baseQuery.orderBy(candidate.UPDATEDON.desc(), candidate.ID.desc());
	}

	private Map<String, Table<?>> createFieldTableMapping(Tblcandidate candidate, Table<?> owner, Table<?> addedBy,
			Table<?> updatedBy) {
		Map<String, Table<?>> fieldTableMapping = new HashMap<>();
		// Add candidate table fields
		fieldTableMapping.put("id", candidate);
		fieldTableMapping.put("name", candidate);
		fieldTableMapping.put(EMAIL_ALIAS, candidate);
		fieldTableMapping.put(PHONE_ALIAS, candidate);
		fieldTableMapping.put(COUNTRY_ALIAS, candidate);
		fieldTableMapping.put(ADDED_ON_ALIAS, candidate);
		fieldTableMapping.put(UPDATED_ON_ALIAS, candidate);
		fieldTableMapping.put("createdon", candidate);
		fieldTableMapping.put("updatedon", candidate);
		// Add owner table fields
		fieldTableMapping.put(OWNER_ID_ALIAS, owner);
		fieldTableMapping.put("ownerid", owner);
		fieldTableMapping.put(OWNER_NAME_ALIAS, owner);
		fieldTableMapping.put("ownername", owner);
		// Add addedBy table fields
		fieldTableMapping.put(ADDED_BY_ID_ALIAS, addedBy);
		fieldTableMapping.put("addedbyid", addedBy);
		fieldTableMapping.put(ADDED_BY_NAME_ALIAS, addedBy);
		fieldTableMapping.put("addedbyname", addedBy);
		fieldTableMapping.put("createdby", addedBy);
		// Add updatedBy table fields
		fieldTableMapping.put(UPDATED_BY_ID_ALIAS, updatedBy);
		fieldTableMapping.put("updatedbyid", updatedBy);
		fieldTableMapping.put(UPDATED_BY_NAME_ALIAS, updatedBy);
		fieldTableMapping.put("updatedbyname", updatedBy);
		fieldTableMapping.put("updatedby", updatedBy);
		return fieldTableMapping;
	}

	@Override
	public Long getAllContractorsCountByAccountId(Integer accountId) {
		var candidate = Tblcandidate.TBLCANDIDATE;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;
		var job = Tbljob.TBLJOB;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;

		Condition candidatesAccessControlCondition = this.accessControlHelper
			.buildCandidatesAccessControlCondition(candidate.OWNERID);

		Long count = this.auroraDbDSLContext.select(DSL.countDistinct(candidate.ID))
			.from(candidate)
			.innerJoin(assignJobCandidate)
			.on(assignJobCandidate.CANDIDATEID.eq(candidate.ID))
			.innerJoin(job)
			.on(job.ID.eq(assignJobCandidate.JOBID))
			.innerJoin(tsSettingAssoc)
			.on(tsSettingAssoc.JOB_ID.eq(job.ID).and(tsSettingAssoc.CONTRACTOR_ID.eq(candidate.ID)))
			.where(candidate.ACCOUNTID.eq(accountId))
			.and(job.ACCOUNTID.eq(accountId))
			.and(job.JOB_TYPE.in(JOB_TYPE_CONTRACT, JOB_TYPE_CONTRACT_TO_PERMANENT))
			.and(candidatesAccessControlCondition)
			.fetchOne(0, Long.class);

		return (count != null) ? count : 0L;
	}

}
