package io.recruitcrm.microservice.timesheet.repositories.time_log;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.TimeLogBreakInterval;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimeLogBreakIntervalT;
import io.recruitcrm.microservice.timesheet.dao.time_log.TimeLogBreakIntervalJpaRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Repository
public class TimeLogBreakIntervalRepository implements ITimeLogBreakIntervalRepository {

	private final TimeLogBreakIntervalJpaRepository timeLogBreakIntervalJpaRepository;

	private final DSLContext auroraDbDSLContext;

	public TimeLogBreakIntervalRepository(TimeLogBreakIntervalJpaRepository timeLogBreakIntervalJpaRepository,
			DSLContext auroraDbDSLContext) {
		this.timeLogBreakIntervalJpaRepository = timeLogBreakIntervalJpaRepository;
		this.auroraDbDSLContext = auroraDbDSLContext;
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public List<TimeLogBreakInterval> saveBreakIntervals(List<TimeLogBreakInterval> breakIntervals) {
		return this.timeLogBreakIntervalJpaRepository.saveAll(breakIntervals);
	}

	@Override
	public List<TimeLogBreakInterval> findBreakIntervalsByTimeLogIdIn(List<Integer> timeLogIds) {
		return this.timeLogBreakIntervalJpaRepository.findBreakIntervalsByTimeLogIdIn(timeLogIds);
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAll(List<TimeLogBreakInterval> breakIntervals) {
		if (breakIntervals == null || breakIntervals.isEmpty()) {
			return;
		}

		// Extract IDs from entities for batch delete
		List<Integer> ids = breakIntervals.stream()
			.map(TimeLogBreakInterval::getId)
			.filter(Objects::nonNull) // Filter out null IDs (safety check)
			.toList();

		if (ids.isEmpty()) {
			return; // No valid IDs to delete
		}

		// Batch delete using JOOQ - single SQL statement: DELETE FROM table WHERE id IN
		// (...)
		// This is much more efficient than JPA's deleteAll() which executes N individual
		// DELETE statements
		var table = CstTimeLogBreakIntervalT.CST_TIME_LOG_BREAK_INTERVAL_T;
		this.auroraDbDSLContext.deleteFrom(table).where(table.ID.in(ids)).execute();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteByTimeLogIdIn(List<Integer> timeLogIds) {
		if (timeLogIds == null || timeLogIds.isEmpty()) {
			return;
		}
		var table = CstTimeLogBreakIntervalT.CST_TIME_LOG_BREAK_INTERVAL_T;
		this.auroraDbDSLContext.deleteFrom(table).where(table.TIME_LOG_ID.in(timeLogIds)).execute();
	}

}