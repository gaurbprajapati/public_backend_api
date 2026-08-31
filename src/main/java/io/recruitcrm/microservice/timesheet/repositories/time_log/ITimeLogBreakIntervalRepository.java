package io.recruitcrm.microservice.timesheet.repositories.time_log;

import io.recruitcrm.contract_staffing.entity.model.TimeLogBreakInterval;

import java.util.List;

public interface ITimeLogBreakIntervalRepository {

	List<TimeLogBreakInterval> saveBreakIntervals(List<TimeLogBreakInterval> breakIntervals);

	List<TimeLogBreakInterval> findBreakIntervalsByTimeLogIdIn(List<Integer> timeLogIds);

	void deleteAll(List<TimeLogBreakInterval> breakIntervals);

	/**
	 * Batch delete break intervals by time log IDs using JOOQ bulk delete
	 * @param timeLogIds List of time log IDs to delete break intervals for
	 */
	void deleteByTimeLogIdIn(List<Integer> timeLogIds);

}