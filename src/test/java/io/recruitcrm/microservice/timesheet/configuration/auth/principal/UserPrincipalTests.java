/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.principal;

import io.recruitcrm.entity.model.Account;
import io.recruitcrm.entity.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for UserPrincipal class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class UserPrincipalTests {

	@Mock
	private User user;

	@Mock
	private Account account;

	private UserPrincipal userPrincipal;

	@BeforeEach
	void setUp() {
		// Setup will be done in each test
	}

	@Nested
	@DisplayName("Constructor Tests")
	class ConstructorTests {

		@Test
		@DisplayName("Should throw IllegalArgumentException when user is null")
		void testConstructorNullUserThrowsIllegalArgumentException() {
			// When & Then
			assertThatThrownBy(() -> new UserPrincipal(null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("User cannot be null");
		}

		@Test
		@DisplayName("Should create UserPrincipal when user is not null")
		void testConstructorValidUserCreatesUserPrincipal() {
			// When
			UserPrincipal result = new UserPrincipal(UserPrincipalTests.this.user);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getUser()).isEqualTo(UserPrincipalTests.this.user);
		}

	}

	@Nested
	@DisplayName("getPrincipalType Tests")
	class GetPrincipalTypeTests {

		@Test
		@DisplayName("Should return USER principal type")
		void testGetPrincipalTypeReturnsUser() {
			// Given
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);

			// When
			PrincipalType result = UserPrincipalTests.this.userPrincipal.getPrincipalType();

			// Then
			assertThat(result).isEqualTo(PrincipalType.USER);
		}

	}

	@Nested
	@DisplayName("getUniqueIdentifier Tests")
	class GetUniqueIdentifierTests {

		@Test
		@DisplayName("Should return user ID")
		void testGetUniqueIdentifierReturnsUserId() {
			// Given
			Integer userId = 1;
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getId()).willReturn(userId);

			// When
			Integer result = UserPrincipalTests.this.userPrincipal.getUniqueIdentifier();

			// Then
			assertThat(result).isEqualTo(userId);
		}

	}

	@Nested
	@DisplayName("getOrganizationIdentifier Tests")
	class GetOrganizationIdentifierTests {

		@Test
		@DisplayName("Should return account ID when account is not null")
		void testGetOrganizationIdentifierAccountNotNullReturnsAccountId() {
			// Given
			Integer accountId = 100;
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getAccount())
				.willReturn(UserPrincipalTests.this.account);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.account.getId()).willReturn(accountId);

			// When
			Integer result = UserPrincipalTests.this.userPrincipal.getOrganizationIdentifier();

			// Then
			assertThat(result).isEqualTo(accountId);
		}

		@Test
		@DisplayName("Should return null when account is null")
		void testGetOrganizationIdentifierAccountNullReturnsNull() {
			// Given
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getAccount()).willReturn(null);

			// When
			Integer result = UserPrincipalTests.this.userPrincipal.getOrganizationIdentifier();

			// Then
			assertThat(result).isNull();
		}

	}

	@Nested
	@DisplayName("getEmail Tests")
	class GetEmailTests {

		@Test
		@DisplayName("Should return user email")
		void testGetEmailReturnsUserEmail() {
			// Given
			String email = "test@example.com";
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getEmail()).willReturn(email);

			// When
			String result = UserPrincipalTests.this.userPrincipal.getEmail();

			// Then
			assertThat(result).isEqualTo(email);
		}

	}

	@Nested
	@DisplayName("getDisplayName Tests")
	class GetDisplayNameTests {

		@Test
		@DisplayName("Should return username when username is not null and not empty")
		void testGetDisplayNameUsernameNotNullReturnsUsername() {
			// Given
			String username = "testuser";
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getUsername()).willReturn(username);

			// When
			String result = UserPrincipalTests.this.userPrincipal.getDisplayName();

			// Then
			assertThat(result).isEqualTo(username);
		}

		@Test
		@DisplayName("Should return full name when username is null")
		void testGetDisplayNameUsernameNullReturnsFullName() {
			// Given
			String firstName = "John";
			String lastName = "Doe";
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getUsername()).willReturn(null);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getFirstname()).willReturn(firstName);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getLastname()).willReturn(lastName);

			// When
			String result = UserPrincipalTests.this.userPrincipal.getDisplayName();

			// Then
			assertThat(result).isEqualTo("John Doe");
		}

		@Test
		@DisplayName("Should return full name when username is empty")
		void testGetDisplayNameUsernameEmptyReturnsFullName() {
			// Given
			String firstName = "John";
			String lastName = "Doe";
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getUsername()).willReturn("");
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getFirstname()).willReturn(firstName);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getLastname()).willReturn(lastName);

			// When
			String result = UserPrincipalTests.this.userPrincipal.getDisplayName();

			// Then
			assertThat(result).isEqualTo("John Doe");
		}

		@Test
		@DisplayName("Should return email when full name is empty")
		void testGetDisplayNameFullNameEmptyReturnsEmail() {
			// Given
			String email = "test@example.com";
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getUsername()).willReturn(null);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getFirstname()).willReturn(null);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getLastname()).willReturn(null);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getEmail()).willReturn(email);

			// When
			String result = UserPrincipalTests.this.userPrincipal.getDisplayName();

			// Then
			assertThat(result).isEqualTo(email);
		}

		@Test
		@DisplayName("Should return email when full name is whitespace only")
		void testGetDisplayNameFullNameWhitespaceReturnsEmail() {
			// Given
			String email = "test@example.com";
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getUsername()).willReturn(null);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getFirstname()).willReturn("");
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getLastname()).willReturn("");
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getEmail()).willReturn(email);

			// When
			String result = UserPrincipalTests.this.userPrincipal.getDisplayName();

			// Then
			assertThat(result).isEqualTo(email);
		}

		@Test
		@DisplayName("Should return full name when only firstname is present")
		void testGetDisplayNameOnlyFirstnameReturnsFullName() {
			// Given
			String firstName = "John";
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getUsername()).willReturn(null);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getFirstname()).willReturn(firstName);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getLastname()).willReturn(null);

			// When
			String result = UserPrincipalTests.this.userPrincipal.getDisplayName();

			// Then
			assertThat(result).isEqualTo("John");
		}

		@Test
		@DisplayName("Should return full name when only lastname is present")
		void testGetDisplayNameOnlyLastnameReturnsFullName() {
			// Given
			String lastName = "Doe";
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getUsername()).willReturn(null);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getFirstname()).willReturn(null);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getLastname()).willReturn(lastName);

			// When
			String result = UserPrincipalTests.this.userPrincipal.getDisplayName();

			// Then
			assertThat(result).isEqualTo("Doe");
		}

	}

	@Nested
	@DisplayName("getFullName Tests")
	class GetFullNameTests {

		@Test
		@DisplayName("Should return first and last name while display name prefers username")
		void testGetFullNameIgnoresUsernameReturnsFirstLast() {
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getUsername()).willReturn("loginuser");
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getFirstname()).willReturn("John");
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getLastname()).willReturn("Doe");

			assertThat(UserPrincipalTests.this.userPrincipal.getDisplayName()).isEqualTo("loginuser");
			assertThat(UserPrincipalTests.this.userPrincipal.getFullName()).isEqualTo("John Doe");
		}

		@Test
		@DisplayName("Should return email when trimmed first and last names are empty")
		void testGetFullNameEmptyNamesReturnsEmail() {
			String email = "test@example.com";
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getFirstname()).willReturn(null);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getLastname()).willReturn(null);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getEmail()).willReturn(email);

			String result = UserPrincipalTests.this.userPrincipal.getFullName();

			assertThat(result).isEqualTo(email);
		}

		@Test
		@DisplayName("Should return concatenated name when first and last are present")
		void testGetFullNameBothNamesReturnsConcatenated() {
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getFirstname()).willReturn("Jane");
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getLastname()).willReturn("Smith");

			String result = UserPrincipalTests.this.userPrincipal.getFullName();

			assertThat(result).isEqualTo("Jane Smith");
		}

	}

	@Nested
	@DisplayName("getRoleIdentifier Tests")
	class GetRoleIdentifierTests {

		@Test
		@DisplayName("Should return user role ID")
		void testGetRoleIdentifierReturnsRoleId() {
			// Given
			Integer roleId = 10;
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getRoleId()).willReturn(roleId);

			// When
			Integer result = UserPrincipalTests.this.userPrincipal.getRoleIdentifier();

			// Then
			assertThat(result).isEqualTo(roleId);
		}

	}

	@Nested
	@DisplayName("getUser Tests")
	class GetUserTests {

		@Test
		@DisplayName("Should return user entity")
		void testGetUserReturnsUser() {
			// Given
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);

			// When
			User result = UserPrincipalTests.this.userPrincipal.getUser();

			// Then
			assertThat(result).isEqualTo(UserPrincipalTests.this.user);
		}

	}

	@Nested
	@DisplayName("getUsername Tests")
	class GetUsernameTests {

		@Test
		@DisplayName("Should return user username")
		void testGetUsernameReturnsUsername() {
			// Given
			String username = "testuser";
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);
			org.mockito.BDDMockito.given(UserPrincipalTests.this.user.getUsername()).willReturn(username);

			// When
			String result = UserPrincipalTests.this.userPrincipal.getUsername();

			// Then
			assertThat(result).isEqualTo(username);
		}

	}

	@Nested
	@DisplayName("getRoleLabel Tests")
	class GetRoleLabelTests {

		@Test
		@DisplayName("Should return User role label")
		void testGetRoleLabelReturnsUser() {
			// Given
			UserPrincipalTests.this.userPrincipal = new UserPrincipal(UserPrincipalTests.this.user);

			// When
			String result = UserPrincipalTests.this.userPrincipal.getRoleLabel();

			// Then
			assertThat(result).isEqualTo("User");
		}

	}

}
