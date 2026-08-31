package io.recruitcrm.microservice.timesheet.dao.candidate;

import io.recruitcrm.entity.model.Candidate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateJpaRepository extends JpaRepository<Candidate, Integer> {

	Candidate getByIdAndAccountId(Integer id, Integer accountId);

	@EntityGraph(attributePaths = { "account", "owner", "createdBy", "updatedBy" })
	Optional<Candidate> findByEmailIdAndAccountId(String emailId, Integer accountId);

	@EntityGraph(attributePaths = { "account", "owner", "createdBy", "updatedBy" })
	Optional<Candidate> findByIdAndAccountId(Integer id, Integer accountId);

	@EntityGraph(attributePaths = { "account", "owner", "createdBy", "updatedBy" })
	@Override
	Optional<Candidate> findById(Integer id);

}
