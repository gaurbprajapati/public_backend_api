/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.portals.client;

import io.recruitcrm.contract_staffing.entity.model.ClientPortalStatus;
import io.recruitcrm.microservice.timesheet.dao.portals.client.ClientPortalStatusJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.constants.ClientPortalStatusConstants;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@code client_portal_status_t} email-based portal status lookups.
 */
@Repository
public class ClientPortalStatusRepository implements IClientPortalStatusRepository {

	private final ClientPortalStatusJpaRepository clientPortalStatusJpaRepository;

	public ClientPortalStatusRepository(ClientPortalStatusJpaRepository clientPortalStatusJpaRepository) {
		this.clientPortalStatusJpaRepository = clientPortalStatusJpaRepository;
	}

	@Override
	public Optional<ClientPortalStatusQueryResultDto> findByVmsUserEmailAndAccountId(String email, Integer accountId) {
		return this.clientPortalStatusJpaRepository.findByVmsUserEmailAndAccountId(email, accountId)
			.map(this::toQueryResultDto);
	}

	@Override
	public Optional<ClientPortalStatus> findEntityByVmsUserEmailAndAccountId(String email, Integer accountId) {
		return this.clientPortalStatusJpaRepository.findByVmsUserEmailAndAccountId(email, accountId);
	}

	@Override
	public ClientPortalStatus save(ClientPortalStatus entity) {
		return this.clientPortalStatusJpaRepository.save(entity);
	}

	@Override
	public List<ClientPortalStatus> saveAll(Collection<ClientPortalStatus> entities) {
		if ((entities == null) || entities.isEmpty()) {
			return List.of();
		}
		return this.clientPortalStatusJpaRepository.saveAll(entities);
	}

	@Override
	public boolean existsPortalStatusUnderDifferentAccount(String email, Integer accountId) {
		return this.clientPortalStatusJpaRepository.existsByVmsUserEmailAndAccountIdNot(email, accountId);
	}

	@Override
	public List<ClientPortalStatus> findEntitiesByVmsUserEmailInAndAccountId(Collection<String> emails,
			Integer accountId) {
		if ((emails == null) || emails.isEmpty()) {
			return List.of();
		}
		return this.clientPortalStatusJpaRepository.findByVmsUserEmailInAndAccountId(emails, accountId);
	}

	@Override
	public List<String> findEmailsPortalEnabledUnderDifferentAccount(Collection<String> emails, Integer accountId) {
		if ((emails == null) || emails.isEmpty()) {
			return List.of();
		}
		return this.clientPortalStatusJpaRepository.findVmsUserEmailsPortalEnabledUnderDifferentAccount(emails,
				ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_ENABLED, accountId);
	}

	private ClientPortalStatusQueryResultDto toQueryResultDto(ClientPortalStatus clientPortalStatus) {
		return new ClientPortalStatusQueryResultDto(clientPortalStatus.getPortalStatusId(),
				clientPortalStatus.getCompanyId(), clientPortalStatus.getVmsUserEmail(),
				clientPortalStatus.getVmsUserId(), clientPortalStatus.getInviteCount(),
				clientPortalStatus.getInviteSentOn(), clientPortalStatus.getInviteSentByUserId(),
				clientPortalStatus.getUpdatedOn());
	}

}
