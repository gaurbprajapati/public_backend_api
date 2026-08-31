package io.recruitcrm.microservice.timesheet.repositories.timesheet_reimbursement;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetReimbursementClientShareHistoryT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetReimbursementStatusHistoryT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetReimbursementT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbljob;
import io.recruitcrm.microservice.timesheet.dto.jobs.ReimbursementSubmissionReminderWindowRowDto;
import io.recruitcrm.microservice.timesheet.helpers.constants.BooleanFlagEnum;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.ITimesheetRepository;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TimesheetReimbursementRepository implements ITimesheetReimbursementRepository {

	private static final String CREATED_ON_FIELD = "created_on";

	private final DSLContext auroraDbDSLContext;

	private final ITimesheetRepository timesheetRepository;

	public TimesheetReimbursementRepository(DSLContext auroraDbDSLContext, ITimesheetRepository timesheetRepository) {
		this.auroraDbDSLContext = auroraDbDSLContext;
		this.timesheetRepository = timesheetRepository;
	}

	@Override
	public Integer getReimbursementCountByTimesheetIdAndEntity(Integer timesheetId, Integer entityType,
			Integer entityId, Integer accountId) {
		var reimbursement = CstTimesheetReimbursementT.CST_TIMESHEET_REIMBURSEMENT_T;
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var job = Tbljob.TBLJOB;

		Condition entityCondition = this.buildEntityCondition(entityType, entityId, accountId, tsSettingAssoc,
				tsSetting, job);

		Condition whereCondition = reimbursement.CST_TIMESHEET_ID.eq(timesheetId)
			.and(reimbursement.ACCOUNT_ID.eq(accountId))
			.and(entityCondition);

		/**
		 * For a client/contact viewer, only count expense claims explicitly shared with
		 * the client (is_shared_with_client = 1), matching the client-facing list view.
		 * Agency/contractor callers count all claims in scope. The jOOQ metamodel is
		 * stale for this column, so it is referenced by qualified name.
		 */
		if (UserTypeEnum.COMPANY_CONTACT.getId().equals(entityType)) {
			whereCondition = whereCondition
				.and(DSL.field(DSL.name(reimbursement.getName(), "is_shared_with_client"), Integer.class)
					.eq(BooleanFlagEnum.TRUE.getValue()));
		}

		Integer count = this.auroraDbDSLContext.selectCount()
			.from(reimbursement)
			.join(ts)
			.on(ts.ID.eq(reimbursement.CST_TIMESHEET_ID))
			.join(tsSetting)
			.on(tsSetting.ID.eq(ts.TIMESHEET_SETTING_ID))
			.join(tsSettingAssoc)
			.on(tsSettingAssoc.ID.eq(tsSetting.ASSOCIATION_ID))
			.join(job)
			.on(job.ID.eq(tsSettingAssoc.JOB_ID))
			.where(whereCondition)
			.fetchOne(0, Integer.class);

		return (count != null) ? count : 0;
	}

	@Override
	public List<ReimbursementSubmissionReminderWindowRowDto> findReimbursementsWhereTransitionedToSubmittedInWindow(
			long fromEpoch, long toEpoch, int submittedStatusId, int rejectedStatusId, int approvedStatusId) {
		int fromEpochSeconds = Math.toIntExact(fromEpoch);
		int toEpochSeconds = Math.toIntExact(toEpoch);

		CstTimesheetReimbursementStatusHistoryT history = CstTimesheetReimbursementStatusHistoryT.CST_TIMESHEET_REIMBURSEMENT_STATUS_HISTORY_T;
		CstTimesheetReimbursementT reimbursement = CstTimesheetReimbursementT.CST_TIMESHEET_REIMBURSEMENT_T;

		Field<Integer> prevStatus = DSL.lag(history.CST_REIMBURSEMENT_STATUS_TYPE_ID)
			.over(DSL.partitionBy(history.CST_TIMESHEET_REIMBURSEMENT_ID).orderBy(history.ID))
			.as("prev_status");

		Field<Integer> createdOnField = DSL.field(DSL.name(history.getName(), CREATED_ON_FIELD), Integer.class);

		Table<?> reimbursementStatusHistory = this.auroraDbDSLContext
			.select(history.CST_TIMESHEET_REIMBURSEMENT_ID,
					history.CST_REIMBURSEMENT_STATUS_TYPE_ID.as("current_status"), createdOnField.as(CREATED_ON_FIELD),
					prevStatus)
			.from(history)
			.asTable("reimbursement_status_history");

		Field<Integer> reimbursementId = reimbursementStatusHistory
			.field(history.CST_TIMESHEET_REIMBURSEMENT_ID.getName(), Integer.class);
		Field<Integer> currentStatus = reimbursementStatusHistory.field("current_status", Integer.class);
		Field<Integer> createdOn = reimbursementStatusHistory.field(CREATED_ON_FIELD, Integer.class);
		Field<Integer> prev = reimbursementStatusHistory.field("prev_status", Integer.class);

		var statusTransitionQuery = this.auroraDbDSLContext
			.selectDistinct(reimbursement.ID, reimbursement.CST_TIMESHEET_ID)
			.from(reimbursementStatusHistory)
			.join(reimbursement)
			.on(reimbursement.ID.eq(reimbursementId))
			.where(currentStatus.eq(submittedStatusId))
			.and(createdOn.ge(fromEpochSeconds))
			.and(createdOn.lt(toEpochSeconds))
			.and(prev.isNull().or(prev.eq(rejectedStatusId)).or(prev.eq(approvedStatusId)))
			.and(reimbursement.STATUS.eq(submittedStatusId));

		CstTimesheetReimbursementClientShareHistoryT clientShareHistory = CstTimesheetReimbursementClientShareHistoryT.CST_TIMESHEET_REIMBURSEMENT_CLIENT_SHARE_HISTORY_T;
		byte sharedWithClientTrue = BooleanFlagEnum.TRUE.getValue().byteValue();

		var clientShareTransitionQuery = this.auroraDbDSLContext
			.selectDistinct(reimbursement.ID, reimbursement.CST_TIMESHEET_ID)
			.from(clientShareHistory)
			.join(reimbursement)
			.on(reimbursement.ID.eq(clientShareHistory.CST_TIMESHEET_REIMBURSEMENT_ID))
			.where(clientShareHistory.IS_SHARED_WITH_CLIENT.eq(sharedWithClientTrue))
			.and(clientShareHistory.CREATED_ON.ge(fromEpochSeconds))
			.and(clientShareHistory.CREATED_ON.lt(toEpochSeconds))
			.and(reimbursement.STATUS.eq(submittedStatusId));

		return statusTransitionQuery.union(clientShareTransitionQuery)
			.fetch((row) -> new ReimbursementSubmissionReminderWindowRowDto(row.get(reimbursement.ID),
					row.get(reimbursement.CST_TIMESHEET_ID)));
	}

	private Condition buildEntityCondition(Integer entityType, Integer entityId, Integer accountId,
			CstTimesheetSettingAssociationT tsSettingAssoc, CstTimesheetSettingT tsSetting, Tbljob job) {
		if (entityType == null) {
			return tsSetting.ACCOUNT_ID.eq(accountId);
		}

		if (UserTypeEnum.CONTRACTOR.getId().equals(entityType)) {
			return tsSettingAssoc.CONTRACTOR_ID.eq(entityId)
				.and(tsSetting.ACCOUNT_ID.eq(accountId))
				.and(job.ACCOUNTID.eq(accountId));
		}

		if (UserTypeEnum.COMPANY_CONTACT.getId().equals(entityType)) {
			var jobContractorPairs = this.timesheetRepository.getJobContractorPairsByContactId(entityId, accountId);

			if (jobContractorPairs == null || jobContractorPairs.isEmpty()) {
				return DSL.falseCondition();
			}

			Condition rowCondition = DSL.row(tsSettingAssoc.JOB_ID, tsSettingAssoc.CONTRACTOR_ID)
				.in(jobContractorPairs.stream()
					.map((pair) -> DSL.row(pair.getJobId(), pair.getContractorId()))
					.toList());

			return rowCondition.and(tsSetting.ACCOUNT_ID.eq(accountId));
		}

		return tsSetting.ACCOUNT_ID.eq(accountId);
	}

}
