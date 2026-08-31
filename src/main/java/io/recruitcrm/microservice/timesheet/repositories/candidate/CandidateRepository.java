package io.recruitcrm.microservice.timesheet.repositories.candidate;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.microservice.timesheet.dao.candidate.CandidateJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class CandidateRepository implements ICandidateRepository {

	private final EntityManager entityManager;

	private final CandidateJpaRepository candidateJpaRepository;

	public CandidateRepository(EntityManager entityManager, CandidateJpaRepository candidateJpaRepository) {
		this.entityManager = entityManager;
		this.candidateJpaRepository = candidateJpaRepository;
	}

	@Override
	public Map<Integer, ContractorNamePhotoQueryResultDto> getContractorQueryResultMap(Set<Integer> ids) {
		String jpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.profilePic, c.slug) "
				+ "FROM Candidate c WHERE c.id IN :ids";
		TypedQuery<Object[]> query = this.entityManager.createQuery(jpql, Object[].class);
		query.setParameter("ids", ids);

		// Transform the result into Map<Integer, ContractorNamePhotoQueryResultDto>
		return query.getResultList()
			.stream()
			.collect(Collectors.toMap((result) -> (Integer) result[0],
					(result) -> (ContractorNamePhotoQueryResultDto) result[1]));
	}

	@Override
	public Candidate getCandidate(Integer id, Integer accountId) {
		return this.candidateJpaRepository.getByIdAndAccountId(id, accountId);
	}

}
