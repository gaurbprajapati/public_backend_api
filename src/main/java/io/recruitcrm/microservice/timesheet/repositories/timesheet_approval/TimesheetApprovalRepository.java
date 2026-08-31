package io.recruitcrm.microservice.timesheet.repositories.timesheet_approval;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetApprovalT;
import io.recruitcrm.microservice.timesheet.dao.timesheet_approval.TimesheetApprovalJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public class TimesheetApprovalRepository implements ITimesheetApprovalRepository {

	final TimesheetApprovalJpaRepository timesheetApprovalJpaRepository;

	private final DSLContext auroraDbDSLContext;

	private final EntityManager entityManager;

	public TimesheetApprovalRepository(TimesheetApprovalJpaRepository timesheetApprovalJpaRepository,
			DSLContext auroraDbDSLContext, EntityManager entityManager) {
		this.timesheetApprovalJpaRepository = timesheetApprovalJpaRepository;
		this.auroraDbDSLContext = auroraDbDSLContext;
		this.entityManager = entityManager;
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void createTimesheetApproval(Integer timesheetId, Integer userId, Integer userTypeId,
			Integer approvalStatusTypeId, String remark) {
		TimesheetApproval timesheetApproval = new TimesheetApproval();
		Integer currentUNIXTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		timesheetApproval.setTimesheetId(timesheetId);
		timesheetApproval.setUserTypeId(userTypeId);
		timesheetApproval.setEntityId(userId);
		timesheetApproval.setTimesheetApprovalStatusTypeId(approvalStatusTypeId);
		timesheetApproval.setCreatedOn(currentUNIXTimestamp);
		timesheetApproval.setRemark(remark);

		this.timesheetApprovalJpaRepository.save(timesheetApproval);
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void createBulkTimesheetApprovals(List<TimesheetApproval> timesheetApprovals) {
		this.timesheetApprovalJpaRepository.saveAll(timesheetApprovals);
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteByTimesheetIdIn(List<Integer> timesheetIds) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return;
		}

		// Batch delete using JOOQ - single SQL statement: DELETE FROM table WHERE
		// timesheet_id IN (...)
		// This is much more efficient than JPA's deleteByTimesheetIdIn() which executes
		// N individual DELETE statements
		var table = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T;
		this.auroraDbDSLContext.deleteFrom(table).where(table.TIMESHEET_ID.in(timesheetIds)).execute();
	}

	@Override
	public List<TimesheetApproval> findLatestApprovalEntitiesByTimesheetIds(List<Integer> timesheetIds) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return List.of();
		}

		String jpql = "SELECT t1 FROM TimesheetApproval t1 "
				+ "WHERE t1.id = (SELECT MAX(t2.id) FROM TimesheetApproval t2 "
				+ "WHERE t2.timesheetId = t1.timesheetId) " + "AND t1.timesheetId IN :timesheetIds "
				+ "ORDER BY t1.id DESC";
		TypedQuery<TimesheetApproval> query = this.entityManager.createQuery(jpql, TimesheetApproval.class);
		query.setParameter("timesheetIds", timesheetIds);
		return query.getResultList();
	}

	@Override
	public List<Integer> findTimesheetIdsWhereTransitionedToSubmittedInWindow(long fromEpoch, long toEpoch,
			int submittedStatusId, int openStatusId, int rejectedStatusId) {

		int fromEpochSeconds = Math.toIntExact(fromEpoch);
		int toEpochSeconds = Math.toIntExact(toEpoch);

		CstTimesheetApprovalT approval = CstTimesheetApprovalT.CST_TIMESHEET_APPROVAL_T.as("approval");

		// ── Step 1: Build approval history with previous and latest status ────────
		Field<Integer> prevStatus = DSL.lag(approval.TIMESHEET_APPROVAL_STATUS_TYPE_ID)
			.over(DSL.partitionBy(approval.TIMESHEET_ID).orderBy(approval.ID))
			.as("prev_status");

		Field<Integer> latestStatus = DSL.firstValue(approval.TIMESHEET_APPROVAL_STATUS_TYPE_ID)
			.over(DSL.partitionBy(approval.TIMESHEET_ID).orderBy(approval.ID.desc()))
			.as("latest_status");

		Table<?> approvalStatusHistory = this.auroraDbDSLContext
			.select(approval.TIMESHEET_ID.as("timesheet_id"),
					approval.TIMESHEET_APPROVAL_STATUS_TYPE_ID.as("current_status"),
					approval.CREATED_ON.as("created_on"), prevStatus, latestStatus)
			.from(approval)
			.asTable("approval_status_history");

		Field<Integer> timesheetId = approvalStatusHistory.field("timesheet_id", Integer.class);
		Field<Integer> currentStatus = approvalStatusHistory.field("current_status", Integer.class);
		Field<Integer> createdOn = approvalStatusHistory.field("created_on", Integer.class);
		Field<Integer> prev = approvalStatusHistory.field("prev_status", Integer.class);
		Field<Integer> latest = approvalStatusHistory.field("latest_status", Integer.class);

		// ── Step 2: Find timesheets that transitioned to SUBMITTED in window ──────
		return this.auroraDbDSLContext.selectDistinct(timesheetId)
			.from(approvalStatusHistory)
			// current row is SUBMITTED and within window
			.where(currentStatus.eq(submittedStatusId))
			.and(createdOn.ge(fromEpochSeconds))
			.and(createdOn.lt(toEpochSeconds))
			// immediately previous status was OPEN or REJECTED
			.and(prev.in(openStatusId, rejectedStatusId))
			// latest status is still SUBMITTED
			.and(latest.eq(submittedStatusId))
			.fetch(timesheetId);
	}

}
