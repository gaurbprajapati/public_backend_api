package io.recruitcrm.microservice.timesheet.services.contractor;

import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobResponseBodyDto;

import java.util.List;

public interface IContractorJobService {

	List<ContractorJobResponseBodyDto> getContractorJobs();

}
