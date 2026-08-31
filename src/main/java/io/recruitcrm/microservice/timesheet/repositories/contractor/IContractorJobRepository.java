package io.recruitcrm.microservice.timesheet.repositories.contractor;

import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobResponseBodyDto;

import java.util.List;

public interface IContractorJobRepository {

	List<ContractorJobResponseBodyDto> findJobsByContractorId(Integer contractorId, Integer accountId);

}
