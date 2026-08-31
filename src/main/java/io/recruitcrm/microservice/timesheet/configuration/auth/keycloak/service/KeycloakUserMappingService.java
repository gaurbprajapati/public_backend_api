/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service;

import io.recruitcrm.entity.model.User;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.UserRepository;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Service for mapping Keycloak JWT tokens to local User entities. Uses email as the
 * common identifier between Keycloak and local database.
 */
@Service
public class KeycloakUserMappingService {

	private final UserRepository userRepository;

	public KeycloakUserMappingService(@Qualifier(UserRepository.BEAN_NAME) UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Maps Keycloak JWT to local User entity. Strategy: Use email from Keycloak JWT to
	 * find user in local database.
	 * @param jwt Decoded Keycloak JWT token
	 * @return Local User entity
	 * @throws UsernameNotFoundException if user not found by email
	 */
	public User mapKeycloakUserToLocalUser(Jwt jwt) {
		String email = jwt.getClaimAsString("email");

		if (email == null || email.isEmpty()) {
			throw new UnauthorizedAccessException("Authentication failed");
		}

		// Find user by email in local database
		return this.userRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("User not found"));

	}

	/**
	 * Maps Keycloak JWT to local User entity by user ID. This is an alternative mapping
	 * strategy if Keycloak user IDs are stored in the local database.
	 * @param jwt Decoded Keycloak JWT token
	 * @return Local User entity
	 * @throws UsernameNotFoundException if user not found by Keycloak ID
	 */
	public User mapKeycloakUserByKeycloakId(Jwt jwt) {
		String keycloakUserId = jwt.getSubject();

		if (keycloakUserId == null || keycloakUserId.isEmpty()) {
			throw new UnauthorizedAccessException("Authentication failed");
		}

		throw new UnsupportedOperationException(
				"Keycloak ID mapping not implemented. Use email-based mapping instead.");
	}

}
