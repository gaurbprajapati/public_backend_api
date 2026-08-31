package io.recruitcrm.microservice.timesheet.repositories.contractor;

import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobResponseBodyDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ContractorJobRepository implements IContractorJobRepository {

	private final EntityManager entityManager;

	public ContractorJobRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<ContractorJobResponseBodyDto> findJobsByContractorId(Integer contractorId, Integer accountId) {
		String jpql = """
				SELECT new io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobResponseBodyDto(
				    j.id,
				    j.name,
				    c.companyName
				)
				FROM AssignCandidateJob acj
				JOIN acj.job j
				LEFT JOIN j.company c
				JOIN TimesheetSettingAssociation tsa
				    ON acj.candidateId = tsa.contractorId
				    AND acj.jobId = tsa.jobId
				WHERE tsa.contractorId = :contractorId
				  AND j.jobType IN ('contract', 'contracttopermanent')
				  AND j.accountId = :accountId
				""";

		TypedQuery<ContractorJobResponseBodyDto> query = this.entityManager.createQuery(jpql,
				ContractorJobResponseBodyDto.class);
		query.setParameter("contractorId", contractorId);
		query.setParameter("accountId", accountId);

		return query.getResultList();
	}

}