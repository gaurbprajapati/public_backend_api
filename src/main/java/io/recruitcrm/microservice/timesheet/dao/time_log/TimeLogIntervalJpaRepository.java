package io.recruitcrm.microservice.timesheet.dao.time_log;

import io.recruitcrm.contract_staffing.entity.model.TimeLogInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeLogIntervalJpaRepository extends JpaRepository<TimeLogInterval, Integer> {

	List<TimeLogInterval> findByTimeLogIdIn(List<Integer> timeLogIds);

	List<TimeLogInterval> findByTimeLogId(Integer timeLogId);

}
