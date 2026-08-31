package io.recruitcrm.microservice.timesheet.dao.deal;

import io.recruitcrm.entity.model.DealCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealCandidateJpaRepository extends JpaRepository<DealCandidate, Integer> {

	// fetch all candidate for given deal id
	List<DealCandidate> findByDealId(Integer dealId);

}
