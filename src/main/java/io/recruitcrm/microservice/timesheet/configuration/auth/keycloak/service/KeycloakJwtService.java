/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Service for validating Keycloak JWT tokens. Supports both public key and JWKS endpoint
 * validation.
 */
@Service
public class KeycloakJwtService {

	@Value("${security.keycloak.public-key:#{null}}")
	private String keycloakPublicKey;

	@Value("${security.keycloak.issuer-uri}")
	private String issuerUri;

	@Value("${security.keycloak.jwk-set-uri:#{null}}")
	private String jwkSetUri;

	@Value("${security.keycloak.enabled:false}")
	private boolean keycloakEnabled;

	private JwtDecoder jwtDecoder;

	/**
	 * Check if Keycloak authentication is enabled.
	 * @return true if Keycloak is enabled
	 */
	public boolean isKeycloakEnabled() {
		return this.keycloakEnabled;
	}

	/**
	 * Get the issuer URI for Keycloak.
	 * @return issuer URI
	 */
	public String getIssuerUri() {
		return this.issuerUri;
	}

	/**
	 * Get or create the JWT decoder for Keycloak tokens.
	 * @return JwtDecoder instance
	 */
	public JwtDecoder getJwtDecoder() {
		if (this.jwtDecoder == null) {
			NimbusJwtDecoder decoder;

			if (this.jwkSetUri != null && !this.jwkSetUri.isEmpty()) {
				// Use JWKS endpoint (recommended for production)
				decoder = NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
			}
			else if (this.keycloakPublicKey != null && !this.keycloakPublicKey.isEmpty()) {
				// Use public key
				try {
					RSAPublicKey publicKey = parsePublicKey(this.keycloakPublicKey);
					decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
				}
				catch (java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException ex) {
					throw new IllegalStateException("Failed to parse Keycloak public key", ex);
				}
			}
			else {
				throw new IllegalStateException(
						"Keycloak JWT decoder configuration missing. Provide either public-key or jwk-set-uri.");
			}

			// Add issuer validation
			decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(this.issuerUri));
			this.jwtDecoder = decoder;
		}
		return this.jwtDecoder;
	}

	/**
	 * Validate a Keycloak JWT token.
	 * @param token JWT token string
	 * @return Decoded JWT
	 */
	public Jwt validateToken(String token) {
		try {
			return getJwtDecoder().decode(token);
		}
		catch (JwtException ex) {
			throw new JwtException("Keycloak token validation failed: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Extract user ID from Keycloak JWT.
	 * @param jwt Decoded JWT
	 * @return User ID (subject claim)
	 */
	public String extractUserId(Jwt jwt) {
		return jwt.getSubject();
	}

	/**
	 * Extract email from Keycloak JWT.
	 * @param jwt Decoded JWT
	 * @return Email address
	 */
	public String extractEmail(Jwt jwt) {
		return jwt.getClaimAsString("email");
	}

	/**
	 * Extract preferred username from Keycloak JWT.
	 * @param jwt Decoded JWT
	 * @return Preferred username
	 */
	public String extractPreferredUsername(Jwt jwt) {
		return jwt.getClaimAsString("preferred_username");
	}

	/**
	 * Extract realm roles from Keycloak JWT.
	 * @param jwt Decoded JWT
	 * @return Realm roles
	 */
	@SuppressWarnings("unchecked")
	public java.util.List<String> extractRealmRoles(Jwt jwt) {
		var realmAccess = jwt.getClaimAsMap("realm_access");
		if (realmAccess != null && realmAccess.containsKey("roles")) {
			return (java.util.List<String>) realmAccess.get("roles");
		}
		return java.util.Collections.emptyList();
	}

	/**
	 * Parse RSA public key from PEM format.
	 * @param publicKeyPEM Public key in PEM format
	 * @return RSAPublicKey instance
	 * @throws IllegalArgumentException if key parsing fails
	 * @throws java.security.NoSuchAlgorithmException if RSA algorithm is not available
	 * @throws java.security.spec.InvalidKeySpecException if key spec is invalid
	 */
	private RSAPublicKey parsePublicKey(String publicKeyPEM)
			throws java.security.NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException {
		String publicKeyContent = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "")
			.replace("-----END PUBLIC KEY-----", "")
			.replaceAll("\\s+", "");

		byte[] decoded = Base64.getDecoder().decode(publicKeyContent);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return (RSAPublicKey) kf.generatePublic(spec);
	}

}
