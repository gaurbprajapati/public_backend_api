/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service;

import io.recruitcrm.entity.model.Contact;
import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.dao.contact.ContactJpaRepository;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Service to map Keycloak JWT tokens to Contact entities.
 *
 * This service is used when a contact (client/hiring manager) authenticates via Keycloak.
 * It looks up the contact in the RCRM database using: 1. entityId claim (when entityType
 * = 1) 2. email address as fallback
 */
@Service
public class ContactMappingService {

	private final ContactJpaRepository contactRepository;

	private final KeycloakPersonaDetector personaDetector;

	private final Logger logger;

	public ContactMappingService(ContactJpaRepository contactRepository, KeycloakPersonaDetector personaDetector,
			@Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) Logger logger) {
		this.contactRepository = contactRepository;
		this.personaDetector = personaDetector;
		this.logger = logger;
	}

	/**
	 * Map Keycloak JWT to Contact entity from RCRM database
	 * @param jwt Validated Keycloak JWT token (expects entityType=1, entityId=contact_id)
	 * @param accountId Account ID for data isolation
	 * @return Contact entity
	 * @throws UsernameNotFoundException if contact not found
	 */
	public Contact mapToContact(Jwt jwt, Integer accountId) {
		// Try to get entityId from JWT (for entityType = 1, this is the contact ID)
		Integer contactId = this.personaDetector.extractEntityId(jwt);

		if (contactId != null && contactId > 0) {
			// Query by ID and account ID
			return this.contactRepository.findByIdAndAccountId(contactId, accountId).orElseThrow(() -> {
				this.logger.logWarn("Contact not found with ID: " + contactId + " for account: " + accountId);
				return new UsernameNotFoundException(
						"Contact not found with ID: " + contactId + " for account: " + accountId);
			});
		}

		// Fallback: Try to find by email
		String email = this.personaDetector.extractEmail(jwt);
		if (email == null || email.isEmpty()) {
			this.logger.logError("No entityId or email found in Keycloak JWT for contact (entityType=1)");
			throw new UnauthorizedAccessException("Authentication failed");
		}

		return this.contactRepository.findByEmailAndAccountId(email, accountId).orElseThrow(() -> {
			this.logger.logWarn("Contact not found with email: " + email + " for account: " + accountId);
			return new UsernameNotFoundException(
					"Contact not found with email: " + email + " for account: " + accountId);
		});
	}

	/**
	 * Map Keycloak JWT to Contact entity (without account ID constraint) This is useful
	 * when account ID is not known upfront.
	 * @param jwt Validated Keycloak JWT token (expects entityType=1, entityId=contact_id)
	 * @return Contact entity
	 * @throws UsernameNotFoundException if contact not found
	 */
	public Contact mapToContact(Jwt jwt) {
		// Try to get entityId from JWT (for entityType = 1, this is the contact ID)
		Integer contactId = this.personaDetector.extractEntityId(jwt);

		if (contactId != null && contactId > 0) {
			// Query by ID only (less secure, but useful for initial lookup)
			return this.contactRepository.findById(contactId).orElseThrow(() -> {
				this.logger.logWarn("Contact not found with ID: " + contactId);
				return new UsernameNotFoundException("Contact not found with ID: " + contactId);
			});
		}

		// If no entityId, we can't safely query without account context
		this.logger.logError("entityId is required when account context is not provided (entityType=1 for contact)");
		throw new UnauthorizedAccessException("Authentication failed");
	}

}
