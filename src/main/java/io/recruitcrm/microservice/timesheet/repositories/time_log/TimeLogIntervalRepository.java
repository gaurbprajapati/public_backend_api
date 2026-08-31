package io.recruitcrm.microservice.timesheet.repositories.time_log;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.TimeLogInterval;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimeLogIntervalT;
import io.recruitcrm.microservice.timesheet.dao.time_log.TimeLogIntervalJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.time_log_interval.TimeLogIntervalDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TimeLogIntervalRepository implements ITimeLogIntervalRepository {

	private static final int BATCH_SIZE = 2000;

	private static final int DELETE_BATCH_SIZE = 2000;

	private final TimeLogIntervalJpaRepository timeLogIntervalJpaRepository;

	private final DSLContext auroraDbDSLContext;

	@PersistenceContext
	private EntityManager entityManager;

	public TimeLogIntervalRepository(TimeLogIntervalJpaRepository timeLogIntervalJpaRepository,
			DSLContext auroraDbDSLContext) {
		this.timeLogIntervalJpaRepository = timeLogIntervalJpaRepository;
		this.auroraDbDSLContext = auroraDbDSLContext;
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public List<TimeLogInterval> saveTimeLogIntervals(List<TimeLogInterval> timeLogIntervals) {
		return this.timeLogIntervalJpaRepository.saveAll(timeLogIntervals);
	}

	@Override
	public List<TimeLogInterval> findByTimeLogIdIn(List<Integer> timeLogIds) {
		return this.timeLogIntervalJpaRepository.findByTimeLogIdIn(timeLogIds);
	}

	@Override
	public List<TimeLogInterval> findByTimeLogId(Integer timeLogId) {
		return this.timeLogIntervalJpaRepository.findByTimeLogId(timeLogId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Integer> findIntervalIdsByTimeLogIdIn(List<Integer> timeLogIds) {
		if (timeLogIds == null || timeLogIds.isEmpty()) {
			return Collections.emptyList();
		}

		// Use native SQL to fetch only IDs - much faster than fetching full entities
		String sql = "SELECT id FROM cst_time_log_interval_t WHERE time_log_id IN (:timeLogIds)";
		Query query = this.entityManager.createNativeQuery(sql);
		query.setParameter("timeLogIds", timeLogIds);

		@SuppressWarnings("unchecked")
		List<Number> result = query.getResultList();
		return result.stream().map(Number::intValue).toList();
	}

	@Override
	@WriterRoute
	@Transactional(readOnly = true)
	public List<Integer> findTimeLogIdsWithExistingIntervals(List<Integer> timeLogIds) {
		if (timeLogIds == null || timeLogIds.isEmpty()) {
			return Collections.emptyList();
		}

		String sql = "SELECT DISTINCT time_log_id FROM cst_time_log_interval_t WHERE time_log_id IN (:timeLogIds)";
		Query query = this.entityManager.createNativeQuery(sql);
		query.setParameter("timeLogIds", timeLogIds);

		@SuppressWarnings("unchecked")
		List<Number> result = query.getResultList();
		return result.stream().map(Number::intValue).toList();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAll(List<TimeLogInterval> timeLogIntervals) {
		if (timeLogIntervals == null || timeLogIntervals.isEmpty()) {
			return;
		}
		this.timeLogIntervalJpaRepository.deleteAll(timeLogIntervals);
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteByTimeLogIdIn(List<Integer> timeLogIds) {
		if (timeLogIds == null || timeLogIds.isEmpty()) {
			return;
		}
		List<TimeLogInterval> intervalsToDelete = this.timeLogIntervalJpaRepository.findByTimeLogIdIn(timeLogIds);
		if (!intervalsToDelete.isEmpty()) {
			this.timeLogIntervalJpaRepository.deleteAll(intervalsToDelete);
		}
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteByTimeLogIntervalIdIn(List<Integer> timeLogIds) {
		if (timeLogIds == null || timeLogIds.isEmpty()) {
			return;
		}
		var table = CstTimeLogIntervalT.CST_TIME_LOG_INTERVAL_T;
		this.auroraDbDSLContext.deleteFrom(table).where(table.TIME_LOG_ID.in(timeLogIds)).execute();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteByIdIn(List<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return;
		}

		int totalBatches = (int) Math.ceil((double) ids.size() / DELETE_BATCH_SIZE);

		// Process in batches to avoid max_allowed_packet limits
		for (int batchIdx = 0; batchIdx < totalBatches; batchIdx++) {
			int startIdx = batchIdx * DELETE_BATCH_SIZE;
			int endIdx = Math.min(startIdx + DELETE_BATCH_SIZE, ids.size());
			List<Integer> batch = ids.subList(startIdx, endIdx);

			// Use native SQL DELETE WHERE id IN (...) - single statement, much faster
			// than JPA deleteAll()
			String sql = "DELETE FROM cst_time_log_interval_t WHERE id IN (:ids)";
			Query query = this.entityManager.createNativeQuery(sql);
			query.setParameter("ids", batch);

			query.executeUpdate();

		}
	}

	/**
	 * Batch insert using: INSERT INTO cst_time_log_interval_t (...) VALUES (...), (...),
	 * (...) Implements batching: splits large datasets into chunks of BATCH_SIZE (1000)
	 * For 30k records: executes 30 queries of 1000 records each
	 */
	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public int batchInsert(List<Object[]> values) {
		if (values == null || values.isEmpty()) {
			return 0;
		}

		int totalInserted = 0;
		int totalBatches = (int) Math.ceil((double) values.size() / BATCH_SIZE);

		// Process in batches of BATCH_SIZE
		for (int batchIdx = 0; batchIdx < totalBatches; batchIdx++) {
			int startIdx = batchIdx * BATCH_SIZE;
			int endIdx = Math.min(startIdx + BATCH_SIZE, values.size());
			List<Object[]> batch = values.subList(startIdx, endIdx);

			// Build INSERT INTO ... VALUES (...), (...), (...)
			StringBuilder sql = new StringBuilder("INSERT INTO `cst_time_log_interval_t` "
					+ "(`time_log_id`, `work_start_time`, `work_end_time`, `range_based_remark`, `break_interval`) "
					+ "VALUES ");

			// Add placeholders: (?, ?, ?, ?, CAST(? AS JSON)), ...
			for (int i = 0; i < batch.size(); i++) {
				if (i > 0) {
					sql.append(", ");
				}
				sql.append("(?, ?, ?, ?, CAST(? AS JSON))");
			}

			Query query = this.entityManager.createNativeQuery(sql.toString());

			// Set parameters
			int paramIndex = 1;
			for (Object[] row : batch) {
				query.setParameter(paramIndex++, row[0]); // time_log_id
				query.setParameter(paramIndex++, row[1]); // work_start_time
				query.setParameter(paramIndex++, row[2]); // work_end_time
				query.setParameter(paramIndex++, row[3]); // range_based_remark
				query.setParameter(paramIndex++, row[4]); // break_interval JSON
			}

			int inserted = query.executeUpdate();
			totalInserted += inserted;

		}

		return totalInserted;
	}

	/**
	 * Batch upsert using: INSERT INTO ... VALUES ... ON DUPLICATE KEY UPDATE ...
	 * Implements batching: splits large datasets into chunks of BATCH_SIZE (1000) Handles
	 * both insert (when ID is null/new) and update (when ID exists)
	 */
	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public int batchUpsert(List<io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalUpsertDto> values) {
		if (values == null || values.isEmpty()) {
			return 0;
		}

		int totalAffected = 0;
		int totalBatches = (int) Math.ceil((double) values.size() / BATCH_SIZE);

		// Process in batches of BATCH_SIZE
		for (int batchIdx = 0; batchIdx < totalBatches; batchIdx++) {
			int startIdx = batchIdx * BATCH_SIZE;
			int endIdx = Math.min(startIdx + BATCH_SIZE, values.size());
			List<io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalUpsertDto> batch = values
				.subList(startIdx, endIdx);

			// Build INSERT INTO ... VALUES (...) ON DUPLICATE KEY UPDATE ...
			StringBuilder sql = new StringBuilder("INSERT INTO `cst_time_log_interval_t` "
					+ "(`id`, `time_log_id`, `work_start_time`, `work_end_time`, `range_based_remark`, `break_interval`) "
					+ "VALUES ");

			// Add placeholders: (?, ?, ?, ?, ?, CAST(? AS JSON)), ...
			for (int i = 0; i < batch.size(); i++) {
				if (i > 0) {
					sql.append(", ");
				}
				sql.append("(?, ?, ?, ?, ?, CAST(? AS JSON))");
			}

			// ON DUPLICATE KEY UPDATE clause
			sql.append(" ON DUPLICATE KEY UPDATE " + "`time_log_id` = VALUES(`time_log_id`), "
					+ "`work_start_time` = VALUES(`work_start_time`), " + "`work_end_time` = VALUES(`work_end_time`), "
					+ "`range_based_remark` = VALUES(`range_based_remark`), "
					+ "`break_interval` = VALUES(`break_interval`)");

			Query query = this.entityManager.createNativeQuery(sql.toString());

			// Set parameters
			int paramIndex = 1;
			for (io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalUpsertDto dto : batch) {
				query.setParameter(paramIndex++, dto.getId());
				query.setParameter(paramIndex++, dto.getTimeLogId());
				query.setParameter(paramIndex++, dto.getWorkStartTime());
				query.setParameter(paramIndex++, dto.getWorkEndTime());
				query.setParameter(paramIndex++, dto.getRangeBasedRemark());
				query.setParameter(paramIndex++, dto.getBreakIntervalJson());
			}

			int affected = query.executeUpdate();
			totalAffected += affected;

		}

		return totalAffected;
	}

	/**
	 * Fetches work intervals from cst_time_log_interval_t grouped by parent timeLogId.
	 * Used by rule engine to expand each time log into its individual work periods.
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<Integer, List<TimeLogIntervalDto>> findIntervalsByTimeLogIds(List<Integer> timeLogIds) {
		if (timeLogIds == null || timeLogIds.isEmpty()) {
			return new HashMap<>();
		}

		// Fetch all intervals for the given time log IDs from cst_time_log_interval_t
		CstTimeLogIntervalT table = CstTimeLogIntervalT.CST_TIME_LOG_INTERVAL_T;
		var records = this.auroraDbDSLContext
			.select(table.ID, table.TIME_LOG_ID, table.WORK_START_TIME, table.WORK_END_TIME, table.RANGE_BASED_REMARK,
					table.BREAK_INTERVAL)
			.from(table)
			.where(table.TIME_LOG_ID.in(timeLogIds))
			.orderBy(table.TIME_LOG_ID, table.WORK_START_TIME)
			.fetch();

		// Group intervals by time log ID
		Map<Integer, List<TimeLogIntervalDto>> intervalsByTimeLogId = new HashMap<>();

		for (var rec : records) {
			Integer timeLogId = rec.get(table.TIME_LOG_ID);
			Integer workStartTime = rec.get(table.WORK_START_TIME);
			Integer workEndTime = rec.get(table.WORK_END_TIME);

			TimeLogIntervalDto intervalDto = TimeLogIntervalDto.builder()
				.id(rec.get(table.ID))
				.timeLogId(timeLogId)
				.workStartTime(workStartTime)
				.workEndTime(workEndTime)
				.rangeBasedRemark(rec.get(table.RANGE_BASED_REMARK))
				.breakInterval(
						(rec.get(table.BREAK_INTERVAL) != null) ? rec.get(table.BREAK_INTERVAL).toString() : null)
				.build();

			intervalsByTimeLogId.computeIfAbsent(timeLogId, (k) -> new ArrayList<>()).add(intervalDto);
		}

		return intervalsByTimeLogId;
	}

}
