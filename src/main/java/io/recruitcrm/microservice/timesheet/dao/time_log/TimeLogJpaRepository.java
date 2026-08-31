package io.recruitcrm.microservice.timesheet.dao.time_log;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogWorkSummaryQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetWorkSummaryQueryResultDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeLogJpaRepository extends JpaRepository<TimeLog, Integer> {

	List<TimeLog> findByTimesheetId(Integer timesheetId);

	List<TimeLog> findByTimesheetIdIn(List<Integer> timesheetIds);

	Optional<TimeLog> findByIdAndTimesheetId(Integer id, Integer timesheetId);

	@Transactional(propagation = Propagation.MANDATORY)
	@WriterRoute
	void deleteByTimesheetId(Integer timesheetId);

	@Transactional(propagation = Propagation.MANDATORY)
	@WriterRoute
	void deleteByTimesheetIdIn(List<Integer> timesheetIds);

	@Query("SELECT new io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetWorkSummaryQueryResultDto("
			+ "t.timesheetId, SUM(t.workTime), SUM(t.overTime), SUM(t.payData), SUM(t.billData), SUM(t.totalTime)) "
			+ "FROM TimeLog t WHERE t.timesheetId IN :timesheetIds GROUP BY t.timesheetId")
	List<TimesheetWorkSummaryQueryResultDto> getTimesheetWorkSummaries(
			@Param("timesheetIds") List<Integer> timesheetIds);

	@Query("SELECT new io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetWorkSummaryQueryResultDto("
			+ "t.timesheetId, SUM(CASE WHEN t.workEndTime IS NOT NULL AND t.workStartTime IS NOT NULL THEN (t.workEndTime - t.workStartTime) ELSE 0 END), SUM(t.overTime), SUM(t.payData), SUM(t.billData),  SUM(t.totalTime)) "
			+ "FROM TimeLog t WHERE t.timesheetId IN :timesheetIds GROUP BY t.timesheetId")
	List<TimesheetWorkSummaryQueryResultDto> getTimesheetWorkDurationSummaries(
			@Param("timesheetIds") List<Integer> timesheetIds);

	@Query("SELECT new io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogWorkSummaryQueryResultDto("
			+ "t.timesheetId, SUM(t.workTime), SUM(t.breakTime), SUM(t.overTime), SUM(t.totalTime), SUM(t.payData), SUM(t.billData)) "
			+ "FROM TimeLog t WHERE t.timesheetId IN :timesheetIds GROUP BY t.timesheetId")
	List<TimeLogWorkSummaryQueryResultDto> getTimeLogWorkSummaries(@Param("timesheetIds") List<Integer> timesheetIds);

	@Query("SELECT new io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogWorkSummaryQueryResultDto("
			+ "t.timesheetId, SUM(CASE WHEN t.workEndTime IS NOT NULL AND t.workStartTime IS NOT NULL THEN (t.workEndTime - t.workStartTime) ELSE 0 END), SUM(t.breakTime), SUM(t.overTime), SUM(t.totalTime), SUM(t.payData), SUM(t.billData)) "
			+ "FROM TimeLog t WHERE t.timesheetId IN :timesheetIds GROUP BY t.timesheetId")
	List<TimeLogWorkSummaryQueryResultDto> getTimeLogWorkDurationSummaries(
			@Param("timesheetIds") List<Integer> timesheetIds);

	long countByDateAfter(Integer date);

	long countByDateBefore(Integer date);

	@Query("FROM TimeLog t WHERE t.timesheetId IN :timesheetIds")
	List<TimeLog> findByTimesheetIds(@Param("timesheetIds") List<Integer> timesheetIds);

}