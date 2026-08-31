package io.recruitcrm.microservice.timesheet.dao.job;

import io.recruitcrm.entity.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobJpaRepository extends JpaRepository<Job, Integer> {

}
