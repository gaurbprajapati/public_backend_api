package io.recruitcrm.microservice.timesheet.dao.deal;

import io.recruitcrm.entity.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DealJpaRepository extends JpaRepository<Deal, Integer> {

	// Method to find a deal by its ID and account ID
	Optional<Deal> findByIdAndAccountId(Integer id, Integer accountId);

	// fetch all

}
