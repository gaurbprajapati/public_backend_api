/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.portals.client;

import io.recruitcrm.contract_staffing.entity.model.ClientPortalStatus;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusQueryResultDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository for client portal status lookups in {@code client_portal_status_t}.
 */
public interface IClientPortalStatusRepository {

	/**
	 * Finds a client portal status row by HMP user email and RCRM account ID.
	 * @param email normalized contact email address
	 * @param accountId RCRM tenant account ID
	 * @return matching portal status row, if present
	 */
	Optional<ClientPortalStatusQueryResultDto> findByVmsUserEmailAndAccountId(String email, Integer accountId);

	/**
	 * Finds a client portal status entity by email and account ID.
	 * @param email normalized contact email address
	 * @param accountId RCRM tenant account ID
	 * @return matching entity, if present
	 */
	Optional<ClientPortalStatus> findEntityByVmsUserEmailAndAccountId(String email, Integer accountId);

	/**
	 * Persists a client portal status entity.
	 * @param entity entity to save
	 * @return saved entity
	 */
	ClientPortalStatus save(ClientPortalStatus entity);

	/**
	 * Persists multiple client portal status entities in a single batch operation.
	 * @param entities entities to save
	 * @return saved entities
	 */
	List<ClientPortalStatus> saveAll(Collection<ClientPortalStatus> entities);

	/**
	 * Returns true when a portal status record exists for the email under a different
	 * account.
	 * @param email normalized contact email address
	 * @param accountId current RCRM tenant account ID
	 * @return true when another account has a portal status record for this email
	 */
	boolean existsPortalStatusUnderDifferentAccount(String email, Integer accountId);

	/**
	 * Finds client portal status entities for the given emails within an account.
	 * @param emails normalized contact email addresses
	 * @param accountId RCRM tenant account ID
	 * @return matching entities for the provided emails
	 */
	List<ClientPortalStatus> findEntitiesByVmsUserEmailInAndAccountId(Collection<String> emails, Integer accountId);

	/**
	 * Returns emails that are portal-enabled under a different account.
	 * @param emails normalized contact email addresses
	 * @param accountId current RCRM tenant account ID
	 * @return emails active on HMP under another agency
	 */
	List<String> findEmailsPortalEnabledUnderDifferentAccount(Collection<String> emails, Integer accountId);

}
