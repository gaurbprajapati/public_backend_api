/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service;

import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Service to detect the persona type from a Keycloak JWT token.
 *
 * This detector examines custom claims in the Keycloak JWT to determine if the
 * authenticated entity is a: - RCRM User (default) - Contractor (candidate accessing via
 * VMS) - Contact (client/hiring manager accessing via VMS)
 *
 * Expected Custom Claims in Keycloak JWT: - entityType: Integer (3 = contractor, 1 =
 * contact/client) - entityId: Integer (the ID of the contractor or contact) - email:
 * String (always required for mapping)
 */
@Component
public class KeycloakPersonaDetector {

	// Custom claim keys in Keycloak JWT
	private static final String CLAIM_ENTITY_TYPE = "entityType";

	private static final String CLAIM_ENTITY_ID = "entityId";

	// Entity type constants
	private static final Integer ENTITY_TYPE_CONTACT = 1;

	private static final Integer ENTITY_TYPE_CONTRACTOR = 3;

	/**
	 * Detect the persona type from Keycloak JWT claims
	 * @param jwt Validated Keycloak JWT token
	 * @return PrincipalType enum value
	 */
	public PrincipalType detectPersonaType(Jwt jwt) {
		if (jwt == null) {
			throw new IllegalArgumentException("JWT cannot be null");
		}

		// Get entityType claim
		Integer entityType = extractEntityType(jwt);

		if (entityType == null) {
			// No entityType claim - default to USER
			return PrincipalType.USER;
		}

		// Check for contractor (entityType = 5)
		if (ENTITY_TYPE_CONTRACTOR.equals(entityType)) {
			// Validate entityId exists
			Integer entityId = extractEntityId(jwt);
			if (entityId != null && entityId > 0) {
				return PrincipalType.CONTRACTOR;
			}
			// Fallback to USER if entityId is missing
			return PrincipalType.USER;
		}

		// Check for contact (entityType = 2)
		if (ENTITY_TYPE_CONTACT.equals(entityType)) {
			// Validate entityId exists
			Integer entityId = extractEntityId(jwt);
			if (entityId != null && entityId > 0) {
				return PrincipalType.CONTACT;
			}
			// Fallback to USER if entityId is missing
			return PrincipalType.USER;
		}

		// Default to USER for any other entityType value
		return PrincipalType.USER;
	}

	/**
	 * Extract entity type from Keycloak JWT
	 * @param jwt Keycloak JWT token
	 * @return entity type (1 = contact, 3 = contractor) or null if not present
	 */
	public Integer extractEntityType(Jwt jwt) {
		try {
			Object claim = jwt.getClaim(CLAIM_ENTITY_TYPE);
			if (claim == null) {
				return null;
			}
			if (claim instanceof Integer integer) {
				return integer;
			}
			if (claim instanceof Number number) {
				return number.intValue();
			}
			if (claim instanceof String string) {
				return Integer.parseInt(string);
			}
			return null;
		}
		catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Extract entity ID from Keycloak JWT
	 * @param jwt Keycloak JWT token
	 * @return entity ID (contractor ID or contact ID) or null if not present
	 */
	public Integer extractEntityId(Jwt jwt) {
		try {
			Object claim = jwt.getClaim(CLAIM_ENTITY_ID);
			if (claim == null) {
				return null;
			}
			if (claim instanceof Integer integer) {
				return integer;
			}
			if (claim instanceof Number number) {
				return number.intValue();
			}
			if (claim instanceof String string) {
				return Integer.parseInt(string);
			}
			return null;
		}
		catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Extract contractor ID from Keycloak JWT (for backward compatibility)
	 * @param jwt Keycloak JWT token
	 * @return contractor ID or null if not present
	 * @deprecated Use extractEntityId() instead
	 */
	@Deprecated(since = "1.0", forRemoval = true)
	public Integer extractContractorId(Jwt jwt) {
		// Check if this is the new format (entityType = 5)
		Integer entityType = extractEntityType(jwt);
		if (entityType != null && ENTITY_TYPE_CONTRACTOR.equals(entityType)) {
			return extractEntityId(jwt);
		}
		return null;
	}

	/**
	 * Extract contact ID from Keycloak JWT (for backward compatibility)
	 * @param jwt Keycloak JWT token
	 * @return contact ID or null if not present
	 * @deprecated Use extractEntityId() instead
	 */
	@Deprecated(since = "1.0", forRemoval = true)
	public Integer extractContactId(Jwt jwt) {
		// Check if this is the new format (entityType = 2)
		Integer entityType = extractEntityType(jwt);
		if (entityType != null && ENTITY_TYPE_CONTACT.equals(entityType)) {
			return extractEntityId(jwt);
		}
		return null;
	}

	/**
	 * Extract email from Keycloak JWT (common to all persona types)
	 * @param jwt Keycloak JWT token
	 * @return email address or null
	 */
	public String extractEmail(Jwt jwt) {
		// Try standard email claim
		String email = jwt.getClaimAsString("email");
		if (email != null && !email.isEmpty()) {
			return email;
		}

		// Try preferred_username as fallback
		String username = jwt.getClaimAsString("preferred_username");
		if (username != null && username.contains("@")) {
			return username;
		}

		return null;
	}

}
