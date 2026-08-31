/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

/**
 * JPQL fixtures for
 * {@link io.recruitcrm.microservice.timesheet.repositories.portals.ContractorTimesheetRepository}
 * unit tests. Strings must match the repository text blocks exactly so
 * {@code createQuery} stubs align.
 */
public final class ContractorTimesheetRepositoryTestDataFactory {

	private ContractorTimesheetRepositoryTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Epoch seconds used for date-window queries in portal tests.
	 */
	public static Long getDefaultPortalCurrentEpoch() {
		return 1_700_000_000L;
	}

	public static String jpqlCountTimesheetEnabledForContractor() {
		return """
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
	}

	public static String jpqlCountTimesheetEnabledForContractorWithoutDateCheck() {
		return """
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
	}

	public static String jpqlCountContractJobsForContractor() {
		return """
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
	}

}
