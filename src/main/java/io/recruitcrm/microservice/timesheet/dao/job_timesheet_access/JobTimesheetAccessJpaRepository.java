package io.recruitcrm.microservice.timesheet.dao.job_timesheet_access;

import io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobTimesheetAccessJpaRepository extends JpaRepository<JobTimesheetAccess, Integer> {

	Optional<JobTimesheetAccess> findByJobId(Integer jobId);

	@Query("Select j from JobTimesheetAccess j where j.jobId = :jobId and j.accountId = :accountId")
	Optional<JobTimesheetAccess> findByJobIdAndAccountId(@Param("jobId") Integer jobId,
			@Param("accountId") Integer accountId);

	@Query("Select j from JobTimesheetAccess j where j.jobId IN :jobIds and j.accountId = :accountId")
	List<JobTimesheetAccess> findByJobIdsAndAccountId(@Param("jobIds") List<Integer> jobIds,
			@Param("accountId") Integer accountId);

}
