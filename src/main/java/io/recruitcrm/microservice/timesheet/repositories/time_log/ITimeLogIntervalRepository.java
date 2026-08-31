package io.recruitcrm.microservice.timesheet.repositories.time_log;

import io.recruitcrm.contract_staffing.entity.model.TimeLogInterval;
import io.recruitcrm.microservice.timesheet.dto.time_log_interval.TimeLogIntervalDto;

import java.util.List;
import java.util.Map;

public interface ITimeLogIntervalRepository {

	/**
	 * Finds all intervals for the given time log IDs.
	 * @param timeLogIds list of time log IDs to fetch intervals for
	 * @return map of time log ID to list of interval DTOs, ordered by work_start_time
	 */
	Map<Integer, List<TimeLogIntervalDto>> findIntervalsByTimeLogIds(List<Integer> timeLogIds);

	List<TimeLogInterval> saveTimeLogIntervals(List<TimeLogInterval> timeLogIntervals);

	List<TimeLogInterval> findByTimeLogIdIn(List<Integer> timeLogIds);

	List<TimeLogInterval> findByTimeLogId(Integer timeLogId);

	/**
	 * Fetch only interval IDs by time log IDs (optimized - returns only IDs, not full
	 * entities)
	 * @param timeLogIds List of time log IDs
	 * @return List of interval IDs
	 */
	List<Integer> findIntervalIdsByTimeLogIdIn(List<Integer> timeLogIds);

	void deleteAll(List<TimeLogInterval> timeLogIntervals);

	void deleteByTimeLogIdIn(List<Integer> timeLogIds);

	/**
	 * Batch delete intervals by time log IDs using JOOQ Much more efficient than JPA
	 * deleteAll() which requires fetching entities first
	 * @param timeLogIds List of time log IDs whose intervals should be deleted
	 */
	void deleteByTimeLogIntervalIdIn(List<Integer> timeLogIds);

	/**
	 * Batch delete intervals by IDs using native SQL DELETE WHERE id IN (...) Much more
	 * efficient than JPA deleteAll() which executes N individual DELETE statements
	 * @param ids List of interval IDs to delete
	 */
	void deleteByIdIn(List<Integer> ids);

	/**
	 * Batch insert time log intervals using native SQL INSERT INTO ... VALUES
	 * @param values List of value arrays: [timeLogId, workStartTime, workEndTime,
	 * rangeBasedRemark, breakIntervalJson]
	 * @return Number of rows inserted
	 */
	int batchInsert(List<Object[]> values);

	/**
	 * Batch upsert time log intervals using native SQL INSERT ... ON DUPLICATE KEY UPDATE
	 * @param values List of TimeLogIntervalUpsertDto objects containing interval data
	 * @return Number of rows affected (inserted + updated)
	 */
	int batchUpsert(List<io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalUpsertDto> values);

	/**
	 * Find time log IDs that already have entries in cst_time_log_interval_t.
	 * @param timeLogIds List of time log IDs to check
	 * @return Subset of timeLogIds that have existing interval entries
	 */
	List<Integer> findTimeLogIdsWithExistingIntervals(List<Integer> timeLogIds);

}
