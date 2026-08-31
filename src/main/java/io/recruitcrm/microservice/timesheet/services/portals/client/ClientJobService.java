package io.recruitcrm.microservice.timesheet.services.portals.client;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.dto.portal.client.ClientJobResponseBodyDto;
import io.recruitcrm.microservice.timesheet.repositories.portals.client.IClientJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClientJobService implements IClientJobService {

	private final IClientJobRepository clientJobRepository;

	private final AuthHolder auth;

	public ClientJobService(IClientJobRepository clientJobRepository, AuthHolder auth) {
		this.clientJobRepository = clientJobRepository;
		this.auth = auth;
	}

	@Override
	public List<ClientJobResponseBodyDto> fetchClientJobs() {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		AuthPrincipal principal = this.auth.getUnifiedPrincipal();
		String email = principal.getEmail();
		if (email == null || email.isEmpty()) {
			return List.of();
		}
		return this.clientJobRepository.findJobsByEmail(email, accountId);
	}

}
