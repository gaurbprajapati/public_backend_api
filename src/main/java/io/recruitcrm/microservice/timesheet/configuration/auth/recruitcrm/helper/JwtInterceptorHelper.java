/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.security.SignatureException;
import io.recruitcrm.entity.model.User;
import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.core.context.sync.SyncLogContext;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.AccessControlConfigHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service.ContactMappingService;
import io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service.ContractorMappingService;
import io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service.KeycloakJwtService;
import io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service.KeycloakPersonaDetector;
import io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.service.KeycloakUserMappingService;
import io.recruitcrm.microservice.timesheet.configuration.auth.keycloak.util.TokenTypeDetector;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.SimpleContactPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.SimpleContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal;
import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Contact;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.AuthenticationPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.UserRepository;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.service.JwtService;
import io.recruitcrm.microservice.timesheet.responses.APIErrorResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.MessageFormat;

@Component
public class JwtInterceptorHelper implements IJwtInterceptorHelper {

	private final JwtService jwtService;

	private final KeycloakJwtService keycloakJwtService;

	private final KeycloakUserMappingService keycloakUserMappingService;

	private final TokenTypeDetector tokenTypeDetector;

	private final KeycloakPersonaDetector keycloakPersonaDetector;

	private final ContractorMappingService contractorMappingService;

	private final ContactMappingService contactMappingService;

	private final UserRepository userRepository;

	private final AuthHolder auth;

	private final SyncLogContext logContext;

	private final Logger logger;

	private final AccessControlConfigHolder accessControlConfigHolder;

	@Value("${security.keycloak.detection-strategy:auto}")
	private String detectionStrategy;

	public JwtInterceptorHelper(JwtService jwtService, KeycloakJwtService keycloakJwtService,
			KeycloakUserMappingService keycloakUserMappingService, TokenTypeDetector tokenTypeDetector,
			KeycloakPersonaDetector keycloakPersonaDetector, ContractorMappingService contractorMappingService,
			ContactMappingService contactMappingService,
			@Qualifier(UserRepository.BEAN_NAME) UserRepository userRepository, AuthHolder auth,
			SyncLogContext logContext, @Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) Logger logger,
			AccessControlConfigHolder accessControlConfigHolder) {
		this.jwtService = jwtService;
		this.keycloakJwtService = keycloakJwtService;
		this.keycloakUserMappingService = keycloakUserMappingService;
		this.tokenTypeDetector = tokenTypeDetector;
		this.keycloakPersonaDetector = keycloakPersonaDetector;
		this.contractorMappingService = contractorMappingService;
		this.contactMappingService = contactMappingService;
		this.userRepository = userRepository;
		this.auth = auth;
		this.logContext = logContext;
		this.logger = logger;
		this.accessControlConfigHolder = accessControlConfigHolder;
	}

	@Override
	public Boolean handleWithBearerToken(HttpServletResponse response, String authHeader) throws IOException {
		// Delegate to the new method with null request for backward compatibility
		return handleWithBearerToken(null, response, authHeader);
	}

	@Override
	public Boolean handleWithBearerToken(HttpServletRequest request, HttpServletResponse response, String authHeader)
			throws IOException {
		try {
			final String jwt = authHeader.substring(7);

			// Check for explicit auth type header (optional, for performance)
			String authTypeHeader = (request != null) ? request.getHeader("X-Auth-Type") : null;

			if ("keycloak".equalsIgnoreCase(authTypeHeader)) {
				// Explicitly requested Keycloak
				this.logger.logInfo("Processing Keycloak token (explicit header)");
				return handleKeycloakToken(jwt);
			}
			else if ("legacy".equalsIgnoreCase(authTypeHeader)) {
				// Explicitly requested legacy
				this.logger.logInfo("Processing legacy token (explicit header)");
				return handleLegacyToken(jwt);
			}

			// No header or auto-detection mode
			if (this.keycloakJwtService.isKeycloakEnabled() && "auto".equalsIgnoreCase(this.detectionStrategy)) {
				// Auto-detect based on token structure
				TokenTypeDetector.TokenType detectedType = this.tokenTypeDetector.detectTokenType(jwt);

				return switch (detectedType) {
					case KEYCLOAK -> {
						this.logger.logInfo("Processing Keycloak token (auto-detected)");
						yield handleKeycloakToken(jwt);
					}
					case LEGACY -> {
						this.logger.logInfo("Processing legacy token (auto-detected)");
						yield handleLegacyToken(jwt);
					}
					case UNKNOWN -> {
						// Unknown type, try legacy as fallback
						this.logger.logWarn("Unknown token type, attempting legacy validation");
						yield handleLegacyToken(jwt);
					}
				};
			}
			else {
				// Keycloak disabled or not in auto mode, use legacy only
				this.logger.logInfo("Processing legacy token (Keycloak disabled)");
				return handleLegacyToken(jwt);
			}

		}
		catch (SignatureException ex) {
			this.logger.logWarn("Invalid token signature: " + ex.getMessage());
			sendErrorResponse(response, "Invalid token");
		}
		catch (UsernameNotFoundException ex) {
			this.logger.logWarn("User not found: " + ex.getMessage());
			sendErrorResponse(response, "User not found");
		}
		catch (Exception ex) {
			this.logger.logError(MessageFormat.format("Internal Server Error: {0}", ex));
			sendErrorResponse(response, "Internal Server Error");
		}
		return false;
	}

	/**
	 * Handle Keycloak JWT token validation and user authentication with multi-persona
	 * support. Detects persona type (USER, CONTRACTOR, CONTACT) from JWT claims and
	 * creates appropriate principal.
	 * @param token JWT token string
	 * @return true if authentication successful, false otherwise
	 */
	private Boolean handleKeycloakToken(String token) {
		try {
			// Validate Keycloak JWT
			Jwt jwt = this.keycloakJwtService.validateToken(token);

			// Detect persona type from JWT
			PrincipalType personaType = this.keycloakPersonaDetector.detectPersonaType(jwt);
			this.logger.logInfo("Detected persona type: " + personaType);

			// Handle based on persona type
			switch (personaType) {
				case USER:
					return handleKeycloakUserPrincipal(jwt);
				case CONTRACTOR:
					return handleKeycloakContractorPrincipal(jwt);
				case CONTACT:
					return handleKeycloakContactPrincipal(jwt);
				default:
					this.logger.logWarn("Unknown persona type, defaulting to USER");
					return handleKeycloakUserPrincipal(jwt);
			}
		}
		catch (JwtException ex) {
			this.logger.logError("Keycloak token validation failed: " + ex.getMessage());
			throw new JwtException("Keycloak token validation failed", ex);
		}
		catch (Exception ex) {
			this.logger.logError("Keycloak authentication error: " + ex.getMessage());
			throw new IllegalStateException("Keycloak authentication failed", ex);
		}
	}

	/**
	 * Handle Keycloak token for RCRM User
	 * @param jwt Validated Keycloak JWT
	 * @return true if successful
	 */
	private Boolean handleKeycloakUserPrincipal(Jwt jwt) {
		// Map Keycloak JWT to User entity
		User user = this.keycloakUserMappingService.mapKeycloakUserToLocalUser(jwt);

		// Create UserPrincipal
		UserPrincipal userPrincipal = new UserPrincipal(user);

		// Set unified principal in AuthHolder
		this.auth.setUnifiedPrincipal(userPrincipal);

		// Also set legacy principal for backward compatibility
		setAuthenticationContext(user);

		this.logger
			.logInfo("Keycloak USER authentication successful for: " + user.getEmail() + " (ID: " + user.getId() + ")");
		return true;
	}

	/**
	 * Handle Keycloak token for Contractor
	 * @param jwt Validated Keycloak JWT
	 * @return true if successful
	 */
	private Boolean handleKeycloakContractorPrincipal(Jwt jwt) {
		// Map to Candidate entity (query RCRM DB using contractor ID from JWT or email
		// fallback)
		Candidate candidate = this.contractorMappingService.mapToCandidate(jwt);

		// Create ContractorPrincipal (uses entity but doesn't access lazy relationships)
		ContractorPrincipal contractorPrincipal = new ContractorPrincipal(candidate);

		// Set unified principal in AuthHolder
		this.auth.setUnifiedPrincipal(contractorPrincipal);

		// Set Spring Security context (for authorization)
		setContractorSecurityContext(candidate);

		// Set logging context
		this.logContext.setUserId(candidate.getId());
		this.logContext.setUserEmail(candidate.getEmailId());
		this.logContext.setUserAccountId(candidate.getAccountId());

		this.logger.logInfo("Keycloak CONTRACTOR authentication successful for: " + candidate.getEmailId() + " (ID: "
				+ candidate.getId() + ")");
		return true;
	}

	/**
	 * Handle Keycloak token for Contact
	 * @param jwt Validated Keycloak JWT
	 * @return true if successful
	 */
	private Boolean handleKeycloakContactPrincipal(Jwt jwt) {
		// Map to Contact entity (query RCRM DB using contact ID from JWT or email
		// fallback)
		Contact contact = this.contactMappingService.mapToContact(jwt);

		// Use the email from the Keycloak JWT, not the one stored on the RCRM contact.
		// If the client changed their email in the portal, Keycloak holds the new email
		// while RCRM may still have the old one. Downstream lookups (e.g. timesheet
		// sharing) key on email, so we must use the authoritative Keycloak value.
		String jwtEmail = this.keycloakPersonaDetector.extractEmail(jwt);

		// Create ContactPrincipal — jwtEmail overrides contact.getEmail() in getEmail()
		ContactPrincipal contactPrincipal = new ContactPrincipal(contact, jwtEmail);

		// Set unified principal in AuthHolder
		this.auth.setUnifiedPrincipal(contactPrincipal);

		// Set Spring Security context (for authorization) — use jwtEmail so the
		// Security context reflects the authoritative Keycloak email, not the stale RCRM
		// value
		setContactSecurityContext(contact, jwtEmail);

		// Set logging context
		this.logContext.setUserId(contact.getId());
		this.logContext.setUserEmail((jwtEmail != null) ? jwtEmail : contact.getEmail());
		this.logContext.setUserAccountId(contact.getAccountId());

		this.logger.logInfo("Keycloak CONTACT authentication successful for: " + contactPrincipal.getEmail() + " (ID: "
				+ contact.getId() + ")");
		return true;
	}

	/**
	 * Set Spring Security context for Contractor (without traditional User entity) Uses
	 * simple POJO to avoid LazyInitializationException
	 * @param candidate Candidate entity
	 */
	private void setContractorSecurityContext(Candidate candidate) {
		// Create simple POJO with only needed data (no Hibernate proxy issues)
		SimpleContractorPrincipal simplePrincipal = new SimpleContractorPrincipal(candidate.getId(),
				candidate.getEmailId(), candidate.getAccountId(), candidate.getFirstName(), candidate.getLastName());

		// Store POJO in SecurityContext instead of Hibernate entity
		Authentication authentication = new UsernamePasswordAuthenticationToken(simplePrincipal, null,
				java.util.Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Note: We don't call accessControlConfigHolder.initAccessControlConfig
		// because contractors have their own permission model
	}

	/**
	 * Set Spring Security context for Contact (without traditional User entity) Uses
	 * simple POJO to avoid LazyInitializationException
	 * @param contact Contact entity
	 * @param jwtEmail email from the Keycloak JWT — authoritative over the RCRM-stored
	 * email
	 */
	private void setContactSecurityContext(Contact contact, String jwtEmail) {
		// Create simple POJO with only needed data (no Hibernate proxy issues).
		// Use jwtEmail so the Security context always reflects the current Keycloak
		// email.
		SimpleContactPrincipal simplePrincipal = new SimpleContactPrincipal(contact.getId(), jwtEmail,
				contact.getAccountId(), contact.getCompanyId(), contact.getFirstName(), contact.getLastName());

		// Store POJO in SecurityContext instead of Hibernate entity
		Authentication authentication = new UsernamePasswordAuthenticationToken(simplePrincipal, null,
				java.util.Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Note: We don't call accessControlConfigHolder.initAccessControlConfig
		// because contacts have their own permission model
	}

	/**
	 * Handle legacy JWT token validation and user authentication.
	 * @param token JWT token string
	 * @return true if authentication successful, false otherwise
	 */
	private Boolean handleLegacyToken(String token) {
		final String userId = this.jwtService.extractUsername(token);
		return handleWithUserId(userId);
	}

	/**
	 * Set authentication context for the user. Sets BOTH legacy and unified principals
	 * for backward compatibility.
	 * @param userDetails User entity
	 */
	private void setAuthenticationContext(User userDetails) {
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Set legacy authentication principal (for backward compatibility)
		AuthenticationPrincipal authenticationPrincipal = new AuthenticationPrincipal();
		authenticationPrincipal.setUser(userDetails);
		this.accessControlConfigHolder.initAccessControlConfig(userDetails.getRoleId());
		this.auth.setAuthenticationPrincipal(authenticationPrincipal);

		// ALSO set unified principal (for new multi-persona system)
		UserPrincipal userPrincipal = new UserPrincipal(userDetails);
		this.auth.setUnifiedPrincipal(userPrincipal);

		this.logContext.setUserId(userDetails.getId());
		this.logContext.setUserEmail(userDetails.getEmail());
		this.logContext.setUserAccountId(userDetails.getAccount().getId());
		this.logContext.setUserAccountName(userDetails.getAccount().getTitle());
	}

	@Override
	public Boolean handleWithUserId(String userId) {
		if (userId != null) {
			User userDetails = this.userRepository.findById(Integer.parseInt(userId))
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			// Use the shared authentication context setup
			setAuthenticationContext(userDetails);

			return true;
		}
		return false;
	}

	@Override
	public void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
		APIErrorResponse msg = new APIErrorResponse(errorMessage, "Unauthorised access", APIResponseType.WARNING,
				HttpStatus.UNAUTHORIZED);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		// Convert objects to JSON strings
		String apiErrorResponseJson = objectMapper.writeValueAsString(msg);
		// Set response status and content type
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		// Write JSON strings to response
		response.getWriter().write(apiErrorResponseJson);
	}

}
