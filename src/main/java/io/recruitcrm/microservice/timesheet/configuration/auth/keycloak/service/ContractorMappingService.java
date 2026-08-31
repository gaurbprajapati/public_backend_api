/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.dao.candidate.CandidateJpaRepository;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Service to map Keycloak JWT tokens to Candidate (Contractor) entities.
 *
 * This service is used when a contractor authenticates via Keycloak. It looks up the
 * contractor in the RCRM database using: 1. entityId claim (when entityType = 3) 2. email
 * address as fallback
 */
@Service
public class ContractorMappingService {

	private final CandidateJpaRepository candidateRepository;

	private final KeycloakPersonaDetector personaDetector;

	private final Logger logger;

	public ContractorMappingService(CandidateJpaRepository candidateRepository, KeycloakPersonaDetector personaDetector,
			@Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) Logger logger) {
		this.candidateRepository = candidateRepository;
		this.personaDetector = personaDetector;
		this.logger = logger;
	}

	/**
	 * Map Keycloak JWT to Candidate entity from RCRM database
	 * @param jwt Validated Keycloak JWT token (expects entityType=3,
	 * entityId=contractor_id)
	 * @param accountId Account ID for data isolation
	 * @return Candidate entity
	 * @throws UsernameNotFoundException if contractor not found
	 */
	public Candidate mapToCandidate(Jwt jwt, Integer accountId) {
		// Try to get entityId from JWT (for entityType = 3, this is the contractor ID)
		Integer contractorId = this.personaDetector.extractEntityId(jwt);

		if (contractorId != null && contractorId > 0) {
			// Query by ID and account ID
			return this.candidateRepository.findByIdAndAccountId(contractorId, accountId).orElseThrow(() -> {
				this.logger.logWarn("Contractor not found with ID: " + contractorId + " for account: " + accountId);
				return new UsernameNotFoundException(
						"Contractor not found with ID: " + contractorId + " for account: " + accountId);
			});
		}

		// Fallback: Try to find by email
		String email = this.personaDetector.extractEmail(jwt);
		if (email == null || email.isEmpty()) {
			this.logger.logError("No entityId or email found in Keycloak JWT for contractor (entityType=3)");
			throw new UnauthorizedAccessException("Authentication failed");
		}

		return this.candidateRepository.findByEmailIdAndAccountId(email, accountId).orElseThrow(() -> {
			this.logger.logWarn("Contractor not found with email: " + email + " for account: " + accountId);
			return new UsernameNotFoundException(
					"Contractor not found with email: " + email + " for account: " + accountId);
		});
	}

	/**
	 * Map Keycloak JWT to Candidate entity (without account ID constraint) This is useful
	 * when account ID is not known upfront.
	 * @param jwt Validated Keycloak JWT token (expects entityType=3,
	 * entityId=contractor_id)
	 * @return Candidate entity
	 * @throws UsernameNotFoundException if contractor not found
	 */
	public Candidate mapToCandidate(Jwt jwt) {
		// Try to get entityId from JWT (for entityType = 3, this is the contractor ID)
		Integer contractorId = this.personaDetector.extractEntityId(jwt);

		if (contractorId != null && contractorId > 0) {
			// Query by ID only (less secure, but useful for initial lookup)
			return this.candidateRepository.findById(contractorId).orElseThrow(() -> {
				this.logger.logWarn("Contractor not found with ID: " + contractorId);
				return new UsernameNotFoundException("Contractor not found with ID: " + contractorId);
			});
		}

		// If no entityId, we can't safely query without account context
		this.logger.logError("entityId is required when account context is not provided (entityType=3 for contractor)");
		throw new UnauthorizedAccessException("Authentication failed");
	}

}
