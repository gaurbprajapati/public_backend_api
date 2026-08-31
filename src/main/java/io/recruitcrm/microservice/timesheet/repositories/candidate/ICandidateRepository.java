/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.candidate;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto;

import java.util.Map;
import java.util.Set;

public interface ICandidateRepository {

	Map<Integer, ContractorNamePhotoQueryResultDto> getContractorQueryResultMap(Set<Integer> ids);

	Candidate getCandidate(Integer id, Integer accountId);

}
