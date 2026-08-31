/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.principal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for AuthPrincipal interface default methods. Tests all default methods for
 * 100% line and branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class AuthPrincipalTests {

	// ========== isSystemUser Tests ==========

	@Nested
	@DisplayName("isSystemUser Tests")
	class IsSystemUserTests {

		@Test
		@DisplayName("Should return true when principal type is USER")
		void testIsSystemUserPrincipalTypeUserReturnsTrue() {
			// Given
			AuthPrincipal principal = new TestAuthPrincipal(PrincipalType.USER);

			// When
			boolean result = principal.isSystemUser();

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return false when principal type is CONTRACTOR")
		void testIsSystemUserPrincipalTypeContractorReturnsFalse() {
			// Given
			AuthPrincipal principal = new TestAuthPrincipal(PrincipalType.CONTRACTOR);

			// When
			boolean result = principal.isSystemUser();

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return false when principal type is CONTACT")
		void testIsSystemUserPrincipalTypeContactReturnsFalse() {
			// Given
			AuthPrincipal principal = new TestAuthPrincipal(PrincipalType.CONTACT);

			// When
			boolean result = principal.isSystemUser();

			// Then
			assertThat(result).isFalse();
		}

	}

	// ========== isContractor Tests ==========

	@Nested
	@DisplayName("isContractor Tests")
	class IsContractorTests {

		@Test
		@DisplayName("Should return true when principal type is CONTRACTOR")
		void testIsContractorPrincipalTypeContractorReturnsTrue() {
			// Given
			AuthPrincipal principal = new TestAuthPrincipal(PrincipalType.CONTRACTOR);

			// When
			boolean result = principal.isContractor();

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return false when principal type is USER")
		void testIsContractorPrincipalTypeUserReturnsFalse() {
			// Given
			AuthPrincipal principal = new TestAuthPrincipal(PrincipalType.USER);

			// When
			boolean result = principal.isContractor();

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return false when principal type is CONTACT")
		void testIsContractorPrincipalTypeContactReturnsFalse() {
			// Given
			AuthPrincipal principal = new TestAuthPrincipal(PrincipalType.CONTACT);

			// When
			boolean result = principal.isContractor();

			// Then
			assertThat(result).isFalse();
		}

	}

	// ========== isContact Tests ==========

	@Nested
	@DisplayName("isContact Tests")
	class IsContactTests {

		@Test
		@DisplayName("Should return true when principal type is CONTACT")
		void testIsContactPrincipalTypeContactReturnsTrue() {
			// Given
			AuthPrincipal principal = new TestAuthPrincipal(PrincipalType.CONTACT);

			// When
			boolean result = principal.isContact();

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return false when principal type is USER")
		void testIsContactPrincipalTypeUserReturnsFalse() {
			// Given
			AuthPrincipal principal = new TestAuthPrincipal(PrincipalType.USER);

			// When
			boolean result = principal.isContact();

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return false when principal type is CONTRACTOR")
		void testIsContactPrincipalTypeContractorReturnsFalse() {
			// Given
			AuthPrincipal principal = new TestAuthPrincipal(PrincipalType.CONTRACTOR);

			// When
			boolean result = principal.isContact();

			// Then
			assertThat(result).isFalse();
		}

	}

	// ========== Test Helper Implementation ==========

	/**
	 * Test implementation of AuthPrincipal for testing default methods.
	 */
	private static class TestAuthPrincipal implements AuthPrincipal {

		private final PrincipalType principalType;

		TestAuthPrincipal(PrincipalType principalType) {
			this.principalType = principalType;
		}

		@Override
		public PrincipalType getPrincipalType() {
			return this.principalType;
		}

		@Override
		public Integer getUniqueIdentifier() {
			return 1;
		}

		@Override
		public Integer getOrganizationIdentifier() {
			return 100;
		}

		@Override
		public String getEmail() {
			return "test@test.com";
		}

		@Override
		public String getDisplayName() {
			return "Test Principal";
		}

		@Override
		public String getFullName() {
			return "Test Principal";
		}

		@Override
		public Integer getRoleIdentifier() {
			return 10;
		}

	}

}
