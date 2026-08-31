package io.recruitcrm.microservice.timesheet.services.portals;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.repositories.portals.IContractorTimesheetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class ContractorTimesheetService implements IContractorTimesheetService {

	private final IContractorTimesheetRepository contractorTimesheetRepository;

	private final AuthHolder auth;

	public ContractorTimesheetService(IContractorTimesheetRepository contractorTimesheetRepository, AuthHolder auth) {
		this.contractorTimesheetRepository = contractorTimesheetRepository;
		this.auth = auth;
	}

	@Override
	public Integer isTimesheetEnabled(Integer contractorId) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		// First check if contractor has any contract or contracttopermanent jobs
		Long contractJobCount = this.contractorTimesheetRepository.countContractJobsForContractor(contractorId,
				accountId);

		// If no contract or contracttopermanent jobs, return null
		if (contractJobCount == 0) {
			return null;
		}

		// Check if timesheet is enabled for the contractor
		Long currentEpoch = Instant.now().getEpochSecond();
		Long timesheetEnabledCount = this.contractorTimesheetRepository.countTimesheetEnabledForContractor(contractorId,
				currentEpoch, accountId);

		// Return 1 if count > 0, else return 0
		return (timesheetEnabledCount > 0) ? 1 : 0;
	}

}
