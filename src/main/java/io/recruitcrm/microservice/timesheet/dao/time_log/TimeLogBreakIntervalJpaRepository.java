package io.recruitcrm.microservice.timesheet.dao.time_log;

import io.recruitcrm.contract_staffing.entity.model.TimeLogBreakInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeLogBreakIntervalJpaRepository extends JpaRepository<TimeLogBreakInterval, Integer> {

	List<TimeLogBreakInterval> findBreakIntervalsByTimeLogIdIn(List<Integer> timeLogIds);

}