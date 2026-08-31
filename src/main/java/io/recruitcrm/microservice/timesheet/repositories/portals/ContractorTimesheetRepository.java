package io.recruitcrm.microservice.timesheet.repositories.portals;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

@Repository
public class ContractorTimesheetRepository implements IContractorTimesheetRepository {

	private static final String CONTRACTOR_ID_PARAM = "contractorId";

	private static final String ACCOUNT_ID_PARAM = "accountId";

	private final EntityManager entityManager;

	public ContractorTimesheetRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public Long countTimesheetEnabledForContractor(Integer contractorId, Long currentEpoch, Integer accountId) {
		String jpql = """
				SELECT CASE
				    WHEN COUNT(ts.id) > 0 THEN CAST(1 AS long)
				    ELSE CAST(0 AS long)
				END
				FROM TimesheetSetting ts
				INNER JOIN ts.association tsa
				INNER JOIN Job j
				    ON j.id = tsa.jobId
				INNER JOIN AssignCandidateJob acj
				    ON acj.jobId = tsa.jobId
				    AND acj.candidateId = tsa.contractorId
				WHERE tsa.contractorId = :contractorId
				    AND ts.accountId = :accountId
				    AND acj.accountId = :accountId
				    AND j.jobType IN ('contract', 'contracttopermanent')
				    AND :currentEpoch BETWEEN ts.jobStartDate AND ts.jobEndDate
				    AND ts.id IN (
				        SELECT MAX(innerTs.id)
				        FROM TimesheetSetting innerTs
				        INNER JOIN innerTs.association innerTsa
				        INNER JOIN AssignCandidateJob innerAcj
				            ON innerAcj.jobId = innerTsa.jobId
				            AND innerAcj.candidateId = innerTsa.contractorId
				        WHERE innerTsa.contractorId = :contractorId
				            AND innerTs.accountId = :accountId
				            AND innerAcj.accountId = :accountId
				        GROUP BY innerTsa.jobId
				    )
				""";
		TypedQuery<Long> query = this.entityManager.createQuery(jpql, Long.class);
		query.setParameter(CONTRACTOR_ID_PARAM, contractorId);
		query.setParameter("currentEpoch", currentEpoch.intValue());
		query.setParameter(ACCOUNT_ID_PARAM, accountId);

		return query.getSingleResult();
	}

	@Override
	public Long countTimesheetEnabledForContractorWithoutDateCheck(Integer contractorId, Integer accountId) {
		String jpql = """
				SELECT CASE
				    WHEN COUNT(ts.id) > 0 THEN CAST(1 AS long)
				    ELSE CAST(0 AS long)
				END
				FROM TimesheetSetting ts
				INNER JOIN ts.association tsa
				INNER JOIN Job j
				    ON j.id = tsa.jobId
				INNER JOIN AssignCandidateJob acj
				    ON acj.jobId = tsa.jobId
				    AND acj.candidateId = tsa.contractorId
				WHERE tsa.contractorId = :contractorId
				    AND ts.accountId = :accountId
				    AND j.jobType IN ('contract', 'contracttopermanent')
				    AND ts.id IN (
				        SELECT MAX(innerTs.id)
				        FROM TimesheetSetting innerTs
				        INNER JOIN innerTs.association innerTsa
				        INNER JOIN AssignCandidateJob innerAcj
				            ON innerAcj.jobId = innerTsa.jobId
				            AND innerAcj.candidateId = innerTsa.contractorId
				        WHERE innerTsa.contractorId = :contractorId
				            AND innerTs.accountId = :accountId
				        GROUP BY innerTsa.jobId
				    )
				""";
		TypedQuery<Long> query = this.entityManager.createQuery(jpql, Long.class);
		query.setParameter(CONTRACTOR_ID_PARAM, contractorId);
		query.setParameter(ACCOUNT_ID_PARAM, accountId);

		return query.getSingleResult();
	}

	@Override
	public Long countContractJobsForContractor(Integer contractorId, Integer accountId) {
		String jpql = """
				SELECT COUNT(DISTINCT acj.jobId)
				FROM AssignCandidateJob acj
				INNER JOIN TimesheetSettingAssociation tsa
				 ON tsa.contractorId = acj.candidateId
				 AND tsa.jobId = acj.jobId
				INNER JOIN Job j
				     ON j.id = acj.jobId
				WHERE acj.candidateId = :contractorId
				    AND acj.accountId = :accountId
				    AND j.jobType IN ('contract', 'contracttopermanent')
				""";
		TypedQuery<Long> query = this.entityManager.createQuery(jpql, Long.class);
		query.setParameter(CONTRACTOR_ID_PARAM, contractorId);
		query.setParameter(ACCOUNT_ID_PARAM, accountId);

		return query.getSingleResult();
	}

}
