/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service.KeycloakJwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Utility class for detecting JWT token types (Keycloak vs Legacy). Inspects token
 * payload without full validation to determine authentication strategy.
 */
@Component
public class TokenTypeDetector {

	private static final Logger LOGGER = LoggerFactory.getLogger(TokenTypeDetector.class);

	private final ObjectMapper objectMapper;

	private final KeycloakJwtService keycloakJwtService;

	public TokenTypeDetector(ObjectMapper objectMapper, KeycloakJwtService keycloakJwtService) {
		this.objectMapper = objectMapper;
		this.keycloakJwtService = keycloakJwtService;
	}

	/**
	 * Token type enumeration.
	 */
	public enum TokenType {

		KEYCLOAK, LEGACY, UNKNOWN

	}

	/**
	 * Detects token type by inspecting JWT payload without full validation. Checks for
	 * Keycloak-specific claims and issuer patterns.
	 * @param token JWT token string
	 * @return TokenType (KEYCLOAK, LEGACY, or UNKNOWN)
	 */
	public TokenType detectTokenType(String token) {
		try {
			// Decode JWT payload without validation (just base64 decode)
			String[] parts = token.split("\\.");
			if (parts.length < 2) {
				LOGGER.warn("Invalid JWT format: insufficient parts");
				return TokenType.UNKNOWN;
			}

			// Decode the payload (second part)
			String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
			JsonNode claims = this.objectMapper.readTree(payload);

			// Check for Keycloak issuer
			if (claims.has("iss")) {
				String issuer = claims.get("iss").asText();

				// Keycloak issuer contains '/realms/' pattern
				if (issuer.contains("/realms/")) {
					LOGGER.debug("Detected Keycloak token by issuer pattern: {}", issuer);
					return TokenType.KEYCLOAK;
				}

				// Check if issuer matches configured Keycloak issuer
				if (this.keycloakJwtService.isKeycloakEnabled()
						&& issuer.equals(this.keycloakJwtService.getIssuerUri())) {
					LOGGER.debug("Detected Keycloak token by matching issuer URI");
					return TokenType.KEYCLOAK;
				}
			}

			// Check for Keycloak-specific claims
			if (claims.has("realm_access")) {
				LOGGER.debug("Detected Keycloak token by realm_access claim");
				return TokenType.KEYCLOAK;
			}

			if (claims.has("preferred_username")) {
				LOGGER.debug("Detected Keycloak token by preferred_username claim");
				return TokenType.KEYCLOAK;
			}

			if (claims.has("resource_access")) {
				LOGGER.debug("Detected Keycloak token by resource_access claim");
				return TokenType.KEYCLOAK;
			}

			// If no Keycloak indicators, assume legacy
			LOGGER.debug("No Keycloak indicators found, treating as legacy token");
			return TokenType.LEGACY;

		}
		catch (Exception ex) {
			LOGGER.error("Failed to detect token type: {}", ex.getMessage(), ex);
			return TokenType.UNKNOWN;
		}
	}

	/**
	 * Check if token is a Keycloak token.
	 * @param token JWT token string
	 * @return true if token is a Keycloak token
	 */
	public boolean isKeycloakToken(String token) {
		return detectTokenType(token) == TokenType.KEYCLOAK;
	}

	/**
	 * Check if token is a legacy token.
	 * @param token JWT token string
	 * @return true if token is a legacy token
	 */
	public boolean isLegacyToken(String token) {
		return detectTokenType(token) == TokenType.LEGACY;
	}

}
