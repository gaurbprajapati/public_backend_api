package io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Select;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.filters.TimesheetGroupBaseFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.deal.DealFilterConditions;

/**
 * Filter node for exact match logic - matches timesheets for contractor-job pairs that
 * have exactly the specified deals (all and only those deals). Uses contractor-job pair
 * filtering approach to ensure both contractor and job are associated with the deals.
 */
public class IsFilterNode extends DistinctDealFilterNode {

	public IsFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Start with minimal joins (for account_id filter)
		List<TableJoinSpecification> joins = this.getMinimalJoins();

		// Add association join (needed for job ID)
		joins.add(new TableJoinSpecification(TableJoinType.INNER, TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC,
				TimesheetGroupBaseFilterNode.TS_SETTING.ASSOCIATION_ID
					.eq(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.ID)));

		// No deal-related joins needed - we filter by job ID using subquery
		return joins;
	}

	@Override
	public List<Condition> getFilterConditions() {
		List<Integer> dealIds = this.parseIntegerFilterValue();

		if (dealIds.isEmpty()) {
			return DealFilterConditions.emptyDealIdsMatchesNothing();
		}

		int expectedCount = dealIds.size();

		// Step 1: Find contractor-job pairs that have all the specified deals
		// A deal is associated with a contractor-job pair when:
		// - The contractor is in tbldealcandidates for that deal
		// - The job is in tbldealjobs for that deal
		// - AND the deal IDs match between tbldealcandidates and tbldealjobs
		// SELECT candidateid, jobid FROM tbldealcandidates dc
		// INNER JOIN tbldealjobs dj ON dj.dealid = dc.dealid
		// WHERE dc.dealid IN (dealIds)
		// GROUP BY candidateid, jobid
		// HAVING count(*) = expectedCount
		Field<Integer> candidateIdField = AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES.CANDIDATEID;
		Field<Integer> jobIdField = AssociatedDealFieldBaseFilterNode.DEAL_JOBS.JOBID;
		Select<?> contractorJobPairsWithDeals = DSL.select(candidateIdField, jobIdField)
			.from(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES)
			.innerJoin(AssociatedDealFieldBaseFilterNode.DEAL_JOBS)
			.on(AssociatedDealFieldBaseFilterNode.DEAL_JOBS.DEALID
				.eq(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES.DEALID))
			.where(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES.DEALID.in(dealIds))
			.groupBy(candidateIdField, jobIdField)
			.having(DSL.count().eq(expectedCount));

		// Step 2: Filter out contractor-job pairs that have deals outside the specified
		// list
		// Using NOT EXISTS to ensure no deals outside the filter list
		org.jooq.Table<?> pairTable = contractorJobPairsWithDeals.asTable("pair");
		Field<Integer> pairCandidateIdField = pairTable.field(candidateIdField.getName(), Integer.class);
		Field<Integer> pairJobIdField = pairTable.field(jobIdField.getName(), Integer.class);

		// Filter timesheets where both contractor_id and job_id match the contractor-job
		// pairs
		// We need to check both conditions together using a row value expression
		return List.of(DSL
			.row(TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.CONTRACTOR_ID,
					TimesheetGroupBaseFilterNode.TS_SETTING_ASSOC.JOB_ID)
			.in(DSL.select(pairCandidateIdField, pairJobIdField)
				.from(pairTable)
				.where(DSL.notExists(DSL.selectOne()
					.from(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES)
					.innerJoin(AssociatedDealFieldBaseFilterNode.DEAL_JOBS)
					.on(AssociatedDealFieldBaseFilterNode.DEAL_JOBS.DEALID
						.eq(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES.DEALID))
					.where(pairCandidateIdField.eq(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES.CANDIDATEID))
					.and(pairJobIdField.eq(AssociatedDealFieldBaseFilterNode.DEAL_JOBS.JOBID))
					.and(AssociatedDealFieldBaseFilterNode.DEAL_CANDIDATES.DEALID.notIn(dealIds))))));
	}

}
