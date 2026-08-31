package io.recruitcrm.microservice.timesheet.services.contractor;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.repositories.contractor.ContractorJobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractorJobService implements IContractorJobService {

	private final ContractorJobRepository contractorJobRepository;

	private final AuthHolder auth;

	public ContractorJobService(ContractorJobRepository contractorJobRepository, AuthHolder auth) {
		this.contractorJobRepository = contractorJobRepository;
		this.auth = auth;
	}

	@Override
	public List<ContractorJobResponseBodyDto> getContractorJobs() {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		// Extract entityType and entityId from the authenticated principal (derived from
		// JWT token)
		PrincipalType principalType = principal.getPrincipalType();
		if (!principalType.equals(PrincipalType.CONTRACTOR)) {
			throw new UnauthorizedAccessException("Unauthorized access");
		}
		ContractorPrincipal contractorPrincipal = (ContractorPrincipal) principal;
		Integer contractorId = contractorPrincipal.getCandidateId();

		return this.contractorJobRepository.findJobsByContractorId(contractorId, accountId);
	}

}
