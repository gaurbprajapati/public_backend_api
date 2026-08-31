/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.recruitcrm.entity.model.User;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.testdata.AuthHolderTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for AuthHolder class. Tests all methods for 100% line and branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class AuthHolderTests {

	private AuthHolder authHolder;

	@BeforeEach
	void setUp() {
		this.authHolder = new AuthHolder();
	}

	// ========== setAuthenticationPrincipal Tests ==========

	@Nested
	@DisplayName("setAuthenticationPrincipal Tests")
	class SetAuthenticationPrincipalTests {

		@Test
		@DisplayName("Should set authentication principal when valid principal is provided")
		void testSetAuthenticationPrincipalValidPrincipalSetsPrincipal() {
			// Given
			IAuthenticationPrincipal<Integer, User> principal = AuthHolderTestDataFactory
				.createValidAuthenticationPrincipal();

			// When
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(principal);

			// Then
			IAuthenticationPrincipal<Integer, User> result = AuthHolderTests.this.authHolder
				.getAuthenticationPrincipal();
			assertThat(result).isEqualTo(principal);
		}

		@Test
		@DisplayName("Should not set principal when unique identifier is null")
		void testSetAuthenticationPrincipalNullUniqueIdDoesNotSetPrincipal() {
			// Given
			IAuthenticationPrincipal<Integer, User> principalWithNullId = AuthHolderTestDataFactory
				.createPrincipalWithNullUniqueId();

			// When
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(principalWithNullId);

			// Then
			assertThatThrownBy(() -> AuthHolderTests.this.authHolder.getAuthenticationPrincipal())
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage(AuthHolderTestDataFactory.Messages.AUTHENTICATION_PRINCIPAL_NOT_FOUND);
		}

		@Test
		@DisplayName("Should not set principal when organization identifier is null")
		void testSetAuthenticationPrincipalNullOrgIdDoesNotSetPrincipal() {
			// Given
			IAuthenticationPrincipal<Integer, User> principalWithNullOrgId = AuthHolderTestDataFactory
				.createPrincipalWithNullOrgId();

			// When
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(principalWithNullOrgId);

			// Then
			assertThatThrownBy(() -> AuthHolderTests.this.authHolder.getAuthenticationPrincipal())
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage(AuthHolderTestDataFactory.Messages.AUTHENTICATION_PRINCIPAL_NOT_FOUND);
		}

	}

	// ========== getAuthenticationPrincipal Tests ==========

	@Nested
	@DisplayName("getAuthenticationPrincipal Tests")
	class GetAuthenticationPrincipalTests {

		@Test
		@DisplayName("Should return authentication principal when principal is set")
		void testGetAuthenticationPrincipalPrincipalSetReturnsPrincipal() {
			// Given
			IAuthenticationPrincipal<Integer, User> principal = AuthHolderTestDataFactory
				.createValidAuthenticationPrincipal();
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(principal);

			// When
			IAuthenticationPrincipal<Integer, User> result = AuthHolderTests.this.authHolder
				.getAuthenticationPrincipal();

			// Then
			assertThat(result).isEqualTo(principal);
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when principal is not set")
		void testGetAuthenticationPrincipalPrincipalNotSetThrowsException() {
			// Given - authHolder has no principal set

			// When & Then
			assertThatThrownBy(() -> AuthHolderTests.this.authHolder.getAuthenticationPrincipal())
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage(AuthHolderTestDataFactory.Messages.AUTHENTICATION_PRINCIPAL_NOT_FOUND);
		}

	}

	// ========== getAuthenticationPrincipalUniqueIdentifier Tests ==========

	@Nested
	@DisplayName("getAuthenticationPrincipalUniqueIdentifier Tests")
	class GetAuthenticationPrincipalUniqueIdentifierTests {

		@Test
		@DisplayName("Should return unique identifier from unified principal when set")
		void testGetUniqueIdUnifiedPrincipalSetReturnsFromUnifiedPrincipal() {
			// Given
			UserPrincipal unifiedPrincipal = AuthHolderTestDataFactory.createValidUserPrincipal();
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(unifiedPrincipal);

			// When
			Integer result = AuthHolderTests.this.authHolder.getAuthenticationPrincipalUniqueIdentifier();

			// Then
			assertThat(result).isEqualTo(AuthHolderTestDataFactory.getDefaultUserId());
		}

		@Test
		@DisplayName("Should return unique identifier from legacy principal when unified not set")
		void testGetUniqueIdUnifiedNotSetLegacySetReturnsFromLegacy() {
			// Given
			IAuthenticationPrincipal<Integer, User> legacyPrincipal = AuthHolderTestDataFactory
				.createValidAuthenticationPrincipal();
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(legacyPrincipal);

			// When
			Integer result = AuthHolderTests.this.authHolder.getAuthenticationPrincipalUniqueIdentifier();

			// Then
			assertThat(result).isEqualTo(AuthHolderTestDataFactory.getDefaultUserId());
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when no principal is set")
		void testGetUniqueIdNoPrincipalSetThrowsException() {
			// Given - no principal set

			// When & Then
			assertThatThrownBy(() -> AuthHolderTests.this.authHolder.getAuthenticationPrincipalUniqueIdentifier())
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage(AuthHolderTestDataFactory.Messages.AUTHENTICATION_PRINCIPAL_NOT_FOUND);
		}

	}

	// ========== getAuthenticationPrincipalRoleIdentifier Tests ==========

	@Nested
	@DisplayName("getAuthenticationPrincipalRoleIdentifier Tests")
	class GetAuthenticationPrincipalRoleIdentifierTests {

		@Test
		@DisplayName("Should return role identifier from unified principal when set")
		void testGetRoleIdUnifiedPrincipalSetReturnsFromUnifiedPrincipal() {
			// Given
			UserPrincipal unifiedPrincipal = AuthHolderTestDataFactory.createValidUserPrincipal();
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(unifiedPrincipal);

			// When
			Integer result = AuthHolderTests.this.authHolder.getAuthenticationPrincipalRoleIdentifier();

			// Then
			assertThat(result).isEqualTo(AuthHolderTestDataFactory.getDefaultRoleId());
		}

		@Test
		@DisplayName("Should return role identifier from legacy principal when unified not set")
		void testGetRoleIdUnifiedNotSetLegacySetReturnsFromLegacy() {
			// Given
			IAuthenticationPrincipal<Integer, User> legacyPrincipal = AuthHolderTestDataFactory
				.createValidAuthenticationPrincipal();
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(legacyPrincipal);

			// When
			Integer result = AuthHolderTests.this.authHolder.getAuthenticationPrincipalRoleIdentifier();

			// Then
			assertThat(result).isEqualTo(AuthHolderTestDataFactory.getDefaultRoleId());
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when no principal is set")
		void testGetRoleIdNoPrincipalSetThrowsException() {
			// Given - no principal set

			// When & Then
			assertThatThrownBy(() -> AuthHolderTests.this.authHolder.getAuthenticationPrincipalRoleIdentifier())
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage(AuthHolderTestDataFactory.Messages.AUTHENTICATION_PRINCIPAL_NOT_FOUND);
		}

	}

	// ========== getAuthenticationPrincipalOrganizationIdentifier Tests ==========

	@Nested
	@DisplayName("getAuthenticationPrincipalOrganizationIdentifier Tests")
	class GetAuthenticationPrincipalOrganizationIdentifierTests {

		@Test
		@DisplayName("Should return organization identifier from unified principal when set")
		void testGetOrgIdUnifiedPrincipalSetReturnsFromUnifiedPrincipal() {
			// Given
			UserPrincipal unifiedPrincipal = AuthHolderTestDataFactory.createValidUserPrincipal();
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(unifiedPrincipal);

			// When
			Integer result = AuthHolderTests.this.authHolder.getAuthenticationPrincipalOrganizationIdentifier();

			// Then
			assertThat(result).isEqualTo(AuthHolderTestDataFactory.getDefaultAccountId());
		}

		@Test
		@DisplayName("Should return organization identifier from legacy principal when unified not set")
		void testGetOrgIdUnifiedNotSetLegacySetReturnsFromLegacy() {
			// Given
			IAuthenticationPrincipal<Integer, User> legacyPrincipal = AuthHolderTestDataFactory
				.createValidAuthenticationPrincipal();
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(legacyPrincipal);

			// When
			Integer result = AuthHolderTests.this.authHolder.getAuthenticationPrincipalOrganizationIdentifier();

			// Then
			assertThat(result).isEqualTo(AuthHolderTestDataFactory.getDefaultAccountId());
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when no principal is set")
		void testGetOrgIdNoPrincipalSetThrowsException() {
			// Given - no principal set

			// When & Then
			assertThatThrownBy(() -> AuthHolderTests.this.authHolder.getAuthenticationPrincipalOrganizationIdentifier())
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage(AuthHolderTestDataFactory.Messages.AUTHENTICATION_PRINCIPAL_NOT_FOUND);
		}

	}

	// ========== setUnifiedPrincipal Tests ==========

	@Nested
	@DisplayName("setUnifiedPrincipal Tests")
	class SetUnifiedPrincipalTests {

		@Test
		@DisplayName("Should set unified principal when valid principal is provided")
		void testSetUnifiedPrincipalValidPrincipalSetsPrincipal() {
			// Given
			UserPrincipal principal = AuthHolderTestDataFactory.createValidUserPrincipal();

			// When
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(principal);

			// Then
			AuthPrincipal result = AuthHolderTests.this.authHolder.getUnifiedPrincipal();
			assertThat(result).isEqualTo(principal);
		}

		@Test
		@DisplayName("Should throw IllegalArgumentException when principal is null")
		void testSetUnifiedPrincipalNullPrincipalThrowsException() {
			// Given
			AuthPrincipal nullPrincipal = null;

			// When & Then
			assertThatThrownBy(() -> AuthHolderTests.this.authHolder.setUnifiedPrincipal(nullPrincipal))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(AuthHolderTestDataFactory.Messages.PRINCIPAL_CANNOT_BE_NULL);
		}

		@Test
		@DisplayName("Should throw IllegalArgumentException when unique identifier is null")
		void testSetUnifiedPrincipalNullUniqueIdThrowsException() {
			// Given
			AuthPrincipal principalWithNullId = AuthHolderTestDataFactory.createPrincipalWithNullUniqueIdentifier();

			// When & Then
			assertThatThrownBy(() -> AuthHolderTests.this.authHolder.setUnifiedPrincipal(principalWithNullId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(AuthHolderTestDataFactory.Messages.PRINCIPAL_MUST_HAVE_VALID_IDENTIFIERS);
		}

		@Test
		@DisplayName("Should throw IllegalArgumentException when organization identifier is null")
		void testSetUnifiedPrincipalNullOrgIdThrowsException() {
			// Given
			AuthPrincipal principalWithNullOrgId = AuthHolderTestDataFactory.createPrincipalWithNullOrganizationId();

			// When & Then
			assertThatThrownBy(() -> AuthHolderTests.this.authHolder.setUnifiedPrincipal(principalWithNullOrgId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(AuthHolderTestDataFactory.Messages.PRINCIPAL_MUST_HAVE_VALID_IDENTIFIERS);
		}

	}

	// ========== getUnifiedPrincipal Tests ==========

	@Nested
	@DisplayName("getUnifiedPrincipal Tests")
	class GetUnifiedPrincipalTests {

		@Test
		@DisplayName("Should return unified principal when set")
		void testGetUnifiedPrincipalUnifiedSetReturnsUnifiedPrincipal() {
			// Given
			UserPrincipal principal = AuthHolderTestDataFactory.createValidUserPrincipal();
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(principal);

			// When
			AuthPrincipal result = AuthHolderTests.this.authHolder.getUnifiedPrincipal();

			// Then
			assertThat(result).isEqualTo(principal);
		}

		@Test
		@DisplayName("Should return converted UserPrincipal when only legacy principal is set")
		void testGetUnifiedPrincipalOnlyLegacySetReturnsConvertedUserPrincipal() {
			// Given
			IAuthenticationPrincipal<Integer, User> legacyPrincipal = AuthHolderTestDataFactory
				.createValidAuthenticationPrincipal();
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(legacyPrincipal);

			// When
			AuthPrincipal result = AuthHolderTests.this.authHolder.getUnifiedPrincipal();

			// Then
			assertThat(result).isInstanceOf(UserPrincipal.class);
			assertThat(result.getPrincipalType()).isEqualTo(PrincipalType.USER);
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when no principal is set")
		void testGetUnifiedPrincipalNoPrincipalSetThrowsException() {
			// Given - no principal set

			// When & Then
			assertThatThrownBy(() -> AuthHolderTests.this.authHolder.getUnifiedPrincipal())
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage(AuthHolderTestDataFactory.Messages.UNIFIED_PRINCIPAL_NOT_FOUND);
		}

	}

	// ========== getPrincipalType Tests ==========

	@Nested
	@DisplayName("getPrincipalType Tests")
	class GetPrincipalTypeTests {

		@Test
		@DisplayName("Should return principal type from unified principal when set")
		void testGetPrincipalTypeUnifiedSetReturnsPrincipalType() {
			// Given
			UserPrincipal principal = AuthHolderTestDataFactory.createValidUserPrincipal();
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(principal);

			// When
			PrincipalType result = AuthHolderTests.this.authHolder.getPrincipalType();

			// Then
			assertThat(result).isEqualTo(PrincipalType.USER);
		}

		@Test
		@DisplayName("Should return USER type when only legacy principal is set")
		void testGetPrincipalTypeOnlyLegacySetReturnsUserType() {
			// Given
			IAuthenticationPrincipal<Integer, User> legacyPrincipal = AuthHolderTestDataFactory
				.createValidAuthenticationPrincipal();
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(legacyPrincipal);

			// When
			PrincipalType result = AuthHolderTests.this.authHolder.getPrincipalType();

			// Then
			assertThat(result).isEqualTo(PrincipalType.USER);
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when no principal is set")
		void testGetPrincipalTypeNoPrincipalSetThrowsException() {
			// Given - no principal set

			// When & Then
			assertThatThrownBy(() -> AuthHolderTests.this.authHolder.getPrincipalType())
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage(AuthHolderTestDataFactory.Messages.AUTHENTICATION_PRINCIPAL_NOT_FOUND);
		}

		@Test
		@DisplayName("Should return CONTRACTOR type when contractor principal is set")
		void testGetPrincipalTypeContractorPrincipalReturnsContractorType() {
			// Given
			AuthPrincipal contractorPrincipal = AuthHolderTestDataFactory.createContractorPrincipal();
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(contractorPrincipal);

			// When
			PrincipalType result = AuthHolderTests.this.authHolder.getPrincipalType();

			// Then
			assertThat(result).isEqualTo(PrincipalType.CONTRACTOR);
		}

		@Test
		@DisplayName("Should return CONTACT type when contact principal is set")
		void testGetPrincipalTypeContactPrincipalReturnsContactType() {
			// Given
			AuthPrincipal contactPrincipal = AuthHolderTestDataFactory.createContactPrincipal();
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(contactPrincipal);

			// When
			PrincipalType result = AuthHolderTests.this.authHolder.getPrincipalType();

			// Then
			assertThat(result).isEqualTo(PrincipalType.CONTACT);
		}

	}

	// ========== hasUnifiedPrincipal Tests ==========

	@Nested
	@DisplayName("hasUnifiedPrincipal Tests")
	class HasUnifiedPrincipalTests {

		@Test
		@DisplayName("Should return true when unified principal is set")
		void testHasUnifiedPrincipalUnifiedSetReturnsTrue() {
			// Given
			UserPrincipal principal = AuthHolderTestDataFactory.createValidUserPrincipal();
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(principal);

			// When
			boolean result = AuthHolderTests.this.authHolder.hasUnifiedPrincipal();

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return false when unified principal is not set")
		void testHasUnifiedPrincipalUnifiedNotSetReturnsFalse() {
			// Given - no unified principal set

			// When
			boolean result = AuthHolderTests.this.authHolder.hasUnifiedPrincipal();

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return false when only legacy principal is set")
		void testHasUnifiedPrincipalOnlyLegacySetReturnsFalse() {
			// Given
			IAuthenticationPrincipal<Integer, User> legacyPrincipal = AuthHolderTestDataFactory
				.createValidAuthenticationPrincipal();
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(legacyPrincipal);

			// When
			boolean result = AuthHolderTests.this.authHolder.hasUnifiedPrincipal();

			// Then
			assertThat(result).isFalse();
		}

	}

	// ========== hasPrincipal Tests ==========

	@Nested
	@DisplayName("hasPrincipal Tests")
	class HasPrincipalTests {

		@Test
		@DisplayName("Should return true when unified principal is set")
		void testHasPrincipalUnifiedSetReturnsTrue() {
			// Given
			UserPrincipal principal = AuthHolderTestDataFactory.createValidUserPrincipal();
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(principal);

			// When
			boolean result = AuthHolderTests.this.authHolder.hasPrincipal();

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return true when legacy principal is set")
		void testHasPrincipalLegacySetReturnsTrue() {
			// Given
			IAuthenticationPrincipal<Integer, User> legacyPrincipal = AuthHolderTestDataFactory
				.createValidAuthenticationPrincipal();
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(legacyPrincipal);

			// When
			boolean result = AuthHolderTests.this.authHolder.hasPrincipal();

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return true when both principals are set")
		void testHasPrincipalBothSetReturnsTrue() {
			// Given
			IAuthenticationPrincipal<Integer, User> legacyPrincipal = AuthHolderTestDataFactory
				.createValidAuthenticationPrincipal();
			UserPrincipal unifiedPrincipal = AuthHolderTestDataFactory.createValidUserPrincipal();
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(legacyPrincipal);
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(unifiedPrincipal);

			// When
			boolean result = AuthHolderTests.this.authHolder.hasPrincipal();

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return false when no principal is set")
		void testHasPrincipalNoPrincipalSetReturnsFalse() {
			// Given - no principal set

			// When
			boolean result = AuthHolderTests.this.authHolder.hasPrincipal();

			// Then
			assertThat(result).isFalse();
		}

	}

	// ========== isSystemUserPrincipal Tests ==========

	@Nested
	@DisplayName("isSystemUserPrincipal Tests")
	class IsSystemUserPrincipalTests {

		@Test
		@DisplayName("Should return true when unified principal is system user")
		void testIsSystemUserUnifiedUserPrincipalReturnsTrue() {
			// Given
			UserPrincipal principal = AuthHolderTestDataFactory.createValidUserPrincipal();
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(principal);

			// When
			boolean result = AuthHolderTests.this.authHolder.isSystemUserPrincipal();

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return false when unified principal is contractor")
		void testIsSystemUserContractorPrincipalReturnsFalse() {
			// Given
			AuthPrincipal contractorPrincipal = AuthHolderTestDataFactory.createContractorPrincipal();
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(contractorPrincipal);

			// When
			boolean result = AuthHolderTests.this.authHolder.isSystemUserPrincipal();

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return false when unified principal is contact")
		void testIsSystemUserContactPrincipalReturnsFalse() {
			// Given
			AuthPrincipal contactPrincipal = AuthHolderTestDataFactory.createContactPrincipal();
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(contactPrincipal);

			// When
			boolean result = AuthHolderTests.this.authHolder.isSystemUserPrincipal();

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should return true when only legacy principal is set")
		void testIsSystemUserOnlyLegacySetReturnsTrue() {
			// Given
			IAuthenticationPrincipal<Integer, User> legacyPrincipal = AuthHolderTestDataFactory
				.createValidAuthenticationPrincipal();
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(legacyPrincipal);

			// When
			boolean result = AuthHolderTests.this.authHolder.isSystemUserPrincipal();

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should return false when no principal is set")
		void testIsSystemUserNoPrincipalSetReturnsFalse() {
			// Given - no principal set

			// When
			boolean result = AuthHolderTests.this.authHolder.isSystemUserPrincipal();

			// Then
			assertThat(result).isFalse();
		}

	}

	// ========== clear Tests ==========

	@Nested
	@DisplayName("clear Tests")
	class ClearTests {

		@Test
		@DisplayName("Should clear all authentication state")
		void testClearClearsAllState() {
			// Given
			IAuthenticationPrincipal<Integer, User> legacyPrincipal = AuthHolderTestDataFactory
				.createValidAuthenticationPrincipal();
			UserPrincipal unifiedPrincipal = AuthHolderTestDataFactory.createValidUserPrincipal();
			AuthHolderTests.this.authHolder.setAuthenticationPrincipal(legacyPrincipal);
			AuthHolderTests.this.authHolder.setUnifiedPrincipal(unifiedPrincipal);

			// When
			AuthHolderTests.this.authHolder.clear();

			// Then
			assertThat(AuthHolderTests.this.authHolder.hasPrincipal()).isFalse();
			assertThat(AuthHolderTests.this.authHolder.hasUnifiedPrincipal()).isFalse();
		}

		@Test
		@DisplayName("Should not throw when clearing empty state")
		void testClearEmptyStateDoesNotThrow() {
			// Given - no principal set

			// When & Then - should not throw
			AuthHolderTests.this.authHolder.clear();
			assertThat(AuthHolderTests.this.authHolder.hasPrincipal()).isFalse();
		}

	}

	// ========== Bean Name Constant Test ==========

	@Nested
	@DisplayName("Bean Name Constant Tests")
	class BeanNameConstantTests {

		@Test
		@DisplayName("Should have correct bean name constant")
		void testBeanNameConstantHasCorrectValue() {
			// Given & When & Then
			assertThat(AuthHolder.BEAN_NAME).isEqualTo("recruitcrmAuthHolder");
		}

	}

}
