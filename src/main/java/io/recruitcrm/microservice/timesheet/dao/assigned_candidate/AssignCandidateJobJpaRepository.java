package io.recruitcrm.microservice.timesheet.dao.assigned_candidate;

import io.recruitcrm.entity.model.AssignCandidateJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignCandidateJobJpaRepository extends JpaRepository<AssignCandidateJob, Integer> {

	AssignCandidateJob findByJobIdAndCandidateIdAndAccountId(Integer jobId, Integer candidateId, Integer accountId);

	@Query("SELECT acj FROM AssignCandidateJob acj WHERE acj.jobId = :jobId AND acj.candidateId IN :contractorIds AND acj.accountId = :accountId")
	List<AssignCandidateJob> findByJobIdAndCandidateIdsAndAccountId(@Param("jobId") Integer jobId,
			@Param("contractorIds") List<Integer> contractorIds, @Param("accountId") Integer accountId);

}
