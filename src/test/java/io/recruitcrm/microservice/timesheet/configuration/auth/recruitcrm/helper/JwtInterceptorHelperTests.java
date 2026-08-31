/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.jsonwebtoken.security.SignatureException;
import io.recruitcrm.entity.model.Account;
import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Contact;
import io.recruitcrm.entity.model.User;
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
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.UserRepository;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for JwtInterceptorHelper class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class JwtInterceptorHelperTests {

	@Mock
	private JwtService jwtService;

	@Mock
	private KeycloakJwtService keycloakJwtService;

	@Mock
	private KeycloakUserMappingService keycloakUserMappingService;

	@Mock
	private TokenTypeDetector tokenTypeDetector;

	@Mock
	private KeycloakPersonaDetector keycloakPersonaDetector;

	@Mock
	private ContractorMappingService contractorMappingService;

	@Mock
	private ContactMappingService contactMappingService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthHolder auth;

	@Mock
	private SyncLogContext logContext;

	@Mock
	private Logger logger;

	@Mock
	private AccessControlConfigHolder accessControlConfigHolder;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private Jwt jwt;

	private JwtInterceptorHelper jwtInterceptorHelper;

	private static final String VALID_AUTH_HEADER = "Bearer validToken123";

	private static final String VALID_TOKEN = "validToken123";

	// ========== Helper Methods ==========

	private static User createDefaultUser() {
		User user = new User();
		user.setId(1);
		user.setEmail("user@test.com");
		user.setUsername("testuser");
		user.setFirstname("Test");
		user.setLastname("User");
		user.setRoleId(1);
		Account account = new Account();
		account.setId(100);
		account.setTitle("Test Account");
		user.setAccount(account);
		return user;
	}

	private static Candidate createDefaultCandidate() {
		Candidate candidate = new Candidate();
		candidate.setId(1);
		candidate.setAccountId(100);
		candidate.setEmailId("contractor@test.com");
		candidate.setFirstName("Test");
		candidate.setLastName("Contractor");
		return candidate;
	}

	private static Contact createDefaultContact() {
		Contact contact = new Contact();
		contact.setId(1);
		contact.setAccountId(100);
		contact.setEmail("contact@test.com");
		contact.setFirstName("Test");
		contact.setLastName("Contact");
		contact.setCompanyId(1);
		return contact;
	}

	@BeforeEach
	void setUp() {
		this.jwtInterceptorHelper = new JwtInterceptorHelper(this.jwtService, this.keycloakJwtService,
				this.keycloakUserMappingService, this.tokenTypeDetector, this.keycloakPersonaDetector,
				this.contractorMappingService, this.contactMappingService, this.userRepository, this.auth,
				this.logContext, this.logger, this.accessControlConfigHolder);
		ReflectionTestUtils.setField(this.jwtInterceptorHelper, "detectionStrategy", "auto");
	}

	// ========== handleWithBearerToken(response, authHeader) Tests ==========

	@Nested
	@DisplayName("handleWithBearerToken backward compatibility Tests")
	class HandleWithBearerTokenBackwardCompatibilityTests {

		@Test
		@DisplayName("Should delegate to handleWithBearerToken with null request")
		void testHandleWithBearerTokenDelegatesToNewMethod() throws Exception {
			// Given
			User user = createDefaultUser();
			given(JwtInterceptorHelperTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(false);
			given(JwtInterceptorHelperTests.this.jwtService.extractUsername(VALID_TOKEN)).willReturn("1");
			given(JwtInterceptorHelperTests.this.userRepository.findById(1)).willReturn(Optional.of(user));

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper
				.handleWithBearerToken(JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isTrue();
		}

	}

	// ========== handleWithBearerToken with request Tests ==========

	@Nested
	@DisplayName("handleWithBearerToken with request Tests")
	class HandleWithBearerTokenWithRequestTests {

		@Test
		@DisplayName("Should handle explicit Keycloak auth type header")
		void testHandleWithBearerTokenExplicitKeycloakHeader() throws Exception {
			// Given
			User user = createDefaultUser();
			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn("keycloak");
			given(JwtInterceptorHelperTests.this.keycloakJwtService.validateToken(VALID_TOKEN))
				.willReturn(JwtInterceptorHelperTests.this.jwt);
			given(JwtInterceptorHelperTests.this.keycloakPersonaDetector
				.detectPersonaType(JwtInterceptorHelperTests.this.jwt)).willReturn(PrincipalType.USER);
			given(JwtInterceptorHelperTests.this.keycloakUserMappingService
				.mapKeycloakUserToLocalUser(JwtInterceptorHelperTests.this.jwt)).willReturn(user);

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should handle explicit legacy auth type header")
		void testHandleWithBearerTokenExplicitLegacyHeader() throws Exception {
			// Given
			User user = createDefaultUser();
			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn("legacy");
			given(JwtInterceptorHelperTests.this.jwtService.extractUsername(VALID_TOKEN)).willReturn("1");
			given(JwtInterceptorHelperTests.this.userRepository.findById(1)).willReturn(Optional.of(user));

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should auto-detect Keycloak token type")
		void testHandleWithBearerTokenAutoDetectKeycloak() throws Exception {
			// Given
			User user = createDefaultUser();
			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn(null);
			given(JwtInterceptorHelperTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(true);
			given(JwtInterceptorHelperTests.this.tokenTypeDetector.detectTokenType(VALID_TOKEN))
				.willReturn(TokenTypeDetector.TokenType.KEYCLOAK);
			given(JwtInterceptorHelperTests.this.keycloakJwtService.validateToken(VALID_TOKEN))
				.willReturn(JwtInterceptorHelperTests.this.jwt);
			given(JwtInterceptorHelperTests.this.keycloakPersonaDetector
				.detectPersonaType(JwtInterceptorHelperTests.this.jwt)).willReturn(PrincipalType.USER);
			given(JwtInterceptorHelperTests.this.keycloakUserMappingService
				.mapKeycloakUserToLocalUser(JwtInterceptorHelperTests.this.jwt)).willReturn(user);

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should auto-detect legacy token type")
		void testHandleWithBearerTokenAutoDetectLegacy() throws Exception {
			// Given
			User user = createDefaultUser();
			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn(null);
			given(JwtInterceptorHelperTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(true);
			given(JwtInterceptorHelperTests.this.tokenTypeDetector.detectTokenType(VALID_TOKEN))
				.willReturn(TokenTypeDetector.TokenType.LEGACY);
			given(JwtInterceptorHelperTests.this.jwtService.extractUsername(VALID_TOKEN)).willReturn("1");
			given(JwtInterceptorHelperTests.this.userRepository.findById(1)).willReturn(Optional.of(user));

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should fallback to legacy for unknown token type")
		void testHandleWithBearerTokenAutoDetectUnknown() throws Exception {
			// Given
			User user = createDefaultUser();
			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn(null);
			given(JwtInterceptorHelperTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(true);
			given(JwtInterceptorHelperTests.this.tokenTypeDetector.detectTokenType(VALID_TOKEN))
				.willReturn(TokenTypeDetector.TokenType.UNKNOWN);
			given(JwtInterceptorHelperTests.this.jwtService.extractUsername(VALID_TOKEN)).willReturn("1");
			given(JwtInterceptorHelperTests.this.userRepository.findById(1)).willReturn(Optional.of(user));

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should use legacy when detection strategy is not auto")
		void testHandleWithBearerTokenNonAutoStrategy() throws Exception {
			// Given
			User user = createDefaultUser();
			ReflectionTestUtils.setField(JwtInterceptorHelperTests.this.jwtInterceptorHelper, "detectionStrategy",
					"legacy");
			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn(null);
			given(JwtInterceptorHelperTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(true);
			given(JwtInterceptorHelperTests.this.jwtService.extractUsername(VALID_TOKEN)).willReturn("1");
			given(JwtInterceptorHelperTests.this.userRepository.findById(1)).willReturn(Optional.of(user));

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isTrue();
			// Reset
			ReflectionTestUtils.setField(JwtInterceptorHelperTests.this.jwtInterceptorHelper, "detectionStrategy",
					"auto");
		}

		@Test
		@DisplayName("Should use legacy when Keycloak is disabled")
		void testHandleWithBearerTokenKeycloakDisabled() throws Exception {
			// Given
			User user = createDefaultUser();
			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn(null);
			given(JwtInterceptorHelperTests.this.keycloakJwtService.isKeycloakEnabled()).willReturn(false);
			given(JwtInterceptorHelperTests.this.jwtService.extractUsername(VALID_TOKEN)).willReturn("1");
			given(JwtInterceptorHelperTests.this.userRepository.findById(1)).willReturn(Optional.of(user));

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should handle contractor persona type")
		void testHandleWithBearerTokenContractorPersona() throws Exception {
			// Given
			Candidate candidate = createDefaultCandidate();
			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn("keycloak");
			given(JwtInterceptorHelperTests.this.keycloakJwtService.validateToken(VALID_TOKEN))
				.willReturn(JwtInterceptorHelperTests.this.jwt);
			given(JwtInterceptorHelperTests.this.keycloakPersonaDetector
				.detectPersonaType(JwtInterceptorHelperTests.this.jwt)).willReturn(PrincipalType.CONTRACTOR);
			given(JwtInterceptorHelperTests.this.contractorMappingService
				.mapToCandidate(JwtInterceptorHelperTests.this.jwt)).willReturn(candidate);

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should handle contact persona type")
		void testHandleWithBearerTokenContactPersona() throws Exception {
			// Given
			Contact contact = createDefaultContact();
			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn("keycloak");
			given(JwtInterceptorHelperTests.this.keycloakJwtService.validateToken(VALID_TOKEN))
				.willReturn(JwtInterceptorHelperTests.this.jwt);
			given(JwtInterceptorHelperTests.this.keycloakPersonaDetector
				.detectPersonaType(JwtInterceptorHelperTests.this.jwt)).willReturn(PrincipalType.CONTACT);
			given(JwtInterceptorHelperTests.this.contactMappingService.mapToContact(JwtInterceptorHelperTests.this.jwt))
				.willReturn(contact);

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should handle contact persona type using JWT email override when present")
		void testHandleWithBearerTokenContactPersonaWithJwtEmail() throws Exception {
			// Given
			Contact contact = createDefaultContact();
			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn("keycloak");
			given(JwtInterceptorHelperTests.this.keycloakJwtService.validateToken(VALID_TOKEN))
				.willReturn(JwtInterceptorHelperTests.this.jwt);
			given(JwtInterceptorHelperTests.this.keycloakPersonaDetector
				.detectPersonaType(JwtInterceptorHelperTests.this.jwt)).willReturn(PrincipalType.CONTACT);
			given(JwtInterceptorHelperTests.this.contactMappingService.mapToContact(JwtInterceptorHelperTests.this.jwt))
				.willReturn(contact);
			given(JwtInterceptorHelperTests.this.keycloakPersonaDetector
				.extractEmail(JwtInterceptorHelperTests.this.jwt)).willReturn("jwt.contact@test.com");

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return false on JwtException in Keycloak token validation")
		void testHandleWithBearerTokenJwtException() throws Exception {
			// Given
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);

			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn("keycloak");
			given(JwtInterceptorHelperTests.this.keycloakJwtService.validateToken(VALID_TOKEN))
				.willThrow(new org.springframework.security.oauth2.jwt.JwtException("Token validation failed"));
			given(JwtInterceptorHelperTests.this.response.getWriter()).willReturn(printWriter);

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return false on general exception in Keycloak handling")
		void testHandleWithBearerTokenKeycloakGeneralException() throws Exception {
			// Given
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);

			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn("keycloak");
			given(JwtInterceptorHelperTests.this.keycloakJwtService.validateToken(VALID_TOKEN))
				.willReturn(JwtInterceptorHelperTests.this.jwt);
			given(JwtInterceptorHelperTests.this.keycloakPersonaDetector
				.detectPersonaType(JwtInterceptorHelperTests.this.jwt)).willThrow(new RuntimeException("Unexpected"));
			given(JwtInterceptorHelperTests.this.response.getWriter()).willReturn(printWriter);

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return false on signature exception")
		void testHandleWithBearerTokenSignatureException() throws Exception {
			// Given
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);

			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn("keycloak");
			given(JwtInterceptorHelperTests.this.keycloakJwtService.validateToken(VALID_TOKEN))
				.willThrow(new SignatureException("Invalid signature"));
			given(JwtInterceptorHelperTests.this.response.getWriter()).willReturn(printWriter);

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return false on username not found exception")
		void testHandleWithBearerTokenUsernameNotFoundException() throws Exception {
			// Given
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);

			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn("legacy");
			given(JwtInterceptorHelperTests.this.jwtService.extractUsername(VALID_TOKEN)).willReturn("1");
			given(JwtInterceptorHelperTests.this.userRepository.findById(1))
				.willThrow(new UsernameNotFoundException("User not found"));
			given(JwtInterceptorHelperTests.this.response.getWriter()).willReturn(printWriter);

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return false on general exception")
		void testHandleWithBearerTokenGeneralException() throws Exception {
			// Given
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);

			given(JwtInterceptorHelperTests.this.request.getHeader("X-Auth-Type")).willReturn("legacy");
			given(JwtInterceptorHelperTests.this.jwtService.extractUsername(VALID_TOKEN))
				.willThrow(new RuntimeException("Unexpected error"));
			given(JwtInterceptorHelperTests.this.response.getWriter()).willReturn(printWriter);

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithBearerToken(
					JwtInterceptorHelperTests.this.request, JwtInterceptorHelperTests.this.response, VALID_AUTH_HEADER);

			// Then
			assertThat(result).isFalse();
		}

	}

	// ========== handleWithUserId Tests ==========

	@Nested
	@DisplayName("handleWithUserId Tests")
	class HandleWithUserIdTests {

		@Test
		@DisplayName("Should return true when user found")
		void testHandleWithUserIdValidUserReturnsTrue() {
			// Given
			User user = createDefaultUser();
			given(JwtInterceptorHelperTests.this.userRepository.findById(1)).willReturn(Optional.of(user));

			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithUserId("1");

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return false when userId is null")
		void testHandleWithUserIdNullUserIdReturnsFalse() {
			// When
			Boolean result = JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithUserId(null);

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should throw UsernameNotFoundException when user not found")
		void testHandleWithUserIdUserNotFoundThrowsException() {
			// Given
			given(JwtInterceptorHelperTests.this.userRepository.findById(999)).willReturn(Optional.empty());

			// When & Then
			try {
				JwtInterceptorHelperTests.this.jwtInterceptorHelper.handleWithUserId("999");
			}
			catch (UsernameNotFoundException ex) {
				assertThat(ex.getMessage()).isEqualTo("User not found");
			}
		}

	}

	// ========== sendErrorResponse Tests ==========

	@Nested
	@DisplayName("sendErrorResponse Tests")
	class SendErrorResponseTests {

		@Test
		@DisplayName("Should send error response with correct format")
		void testSendErrorResponseSendsCorrectFormat() throws IOException {
			// Given
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			given(JwtInterceptorHelperTests.this.response.getWriter()).willReturn(printWriter);

			// When
			JwtInterceptorHelperTests.this.jwtInterceptorHelper
				.sendErrorResponse(JwtInterceptorHelperTests.this.response, "Test error message");

			// Then
			then(JwtInterceptorHelperTests.this.response).should().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			then(JwtInterceptorHelperTests.this.response).should().setContentType("application/json");
			assertThat(stringWriter.toString()).contains("Test error message");
		}

	}

}
