/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.entity.model.Account;
import io.recruitcrm.entity.model.User;
import io.recruitcrm.microservice.timesheet.configuration.auth.IAuthenticationPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal;

/**
 * Test data factory for AuthHolder-related test objects. Provides factory methods to
 * create consistent test data across all AuthHolder tests.
 */
public final class AuthHolderTestDataFactory {

	private static final Integer DEFAULT_USER_ID = 1;

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	private static final Integer DEFAULT_ROLE_ID = 10;

	private static final String DEFAULT_USERNAME = "testuser";

	private static final String DEFAULT_EMAIL = "testuser@test.com";

	private static final String DEFAULT_FIRSTNAME = "Test";

	private static final String DEFAULT_LASTNAME = "User";

	/**
	 * Private constructor to prevent instantiation.
	 */
	private AuthHolderTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// ========== User Entity Factory Methods ==========

	/**
	 * Creates a User entity with default test data.
	 * @return User entity with default values
	 */
	public static User createDefaultUser() {
		User user = new User();
		user.setId(DEFAULT_USER_ID);
		user.setUsername(DEFAULT_USERNAME);
		user.setEmail(DEFAULT_EMAIL);
		user.setFirstname(DEFAULT_FIRSTNAME);
		user.setLastname(DEFAULT_LASTNAME);
		user.setRoleId(DEFAULT_ROLE_ID);
		user.setAccount(createDefaultAccount());
		return user;
	}

	/**
	 * Creates a User entity with custom ID.
	 * @param userId the user ID to set
	 * @return User entity with custom ID
	 */
	public static User createUserWithId(Integer userId) {
		User user = createDefaultUser();
		user.setId(userId);
		return user;
	}

	/**
	 * Creates a User entity without account.
	 * @return User entity without account
	 */
	public static User createUserWithoutAccount() {
		User user = createDefaultUser();
		user.setAccount(null);
		return user;
	}

	/**
	 * Creates a User entity without username.
	 * @return User entity without username
	 */
	public static User createUserWithoutUsername() {
		User user = createDefaultUser();
		user.setUsername(null);
		return user;
	}

	/**
	 * Creates a User entity with empty username.
	 * @return User entity with empty username
	 */
	public static User createUserWithEmptyUsername() {
		User user = createDefaultUser();
		user.setUsername("");
		return user;
	}

	/**
	 * Creates a User entity without firstname and lastname.
	 * @return User entity without names
	 */
	public static User createUserWithoutNames() {
		User user = createDefaultUser();
		user.setUsername(null);
		user.setFirstname(null);
		user.setLastname(null);
		return user;
	}

	// ========== Account Entity Factory Methods ==========

	/**
	 * Creates a default Account entity.
	 * @return Account entity with default values
	 */
	public static Account createDefaultAccount() {
		Account account = new Account();
		account.setId(DEFAULT_ACCOUNT_ID);
		return account;
	}

	// ========== Authentication Principal Factory Methods ==========

	/**
	 * Creates a mock IAuthenticationPrincipal with valid identifiers.
	 * @return IAuthenticationPrincipal mock with valid data
	 */
	public static IAuthenticationPrincipal<Integer, User> createValidAuthenticationPrincipal() {
		User user = createDefaultUser();
		return new TestAuthenticationPrincipal(DEFAULT_USER_ID, DEFAULT_ACCOUNT_ID, DEFAULT_ROLE_ID, DEFAULT_USERNAME,
				"Admin", user);
	}

	/**
	 * Creates a mock IAuthenticationPrincipal with null unique identifier.
	 * @return IAuthenticationPrincipal mock with null unique identifier
	 */
	public static IAuthenticationPrincipal<Integer, User> createPrincipalWithNullUniqueId() {
		User user = createDefaultUser();
		return new TestAuthenticationPrincipal(null, DEFAULT_ACCOUNT_ID, DEFAULT_ROLE_ID, DEFAULT_USERNAME, "Admin",
				user);
	}

	/**
	 * Creates a mock IAuthenticationPrincipal with null organization identifier.
	 * @return IAuthenticationPrincipal mock with null organization identifier
	 */
	public static IAuthenticationPrincipal<Integer, User> createPrincipalWithNullOrgId() {
		User user = createDefaultUser();
		return new TestAuthenticationPrincipal(DEFAULT_USER_ID, null, DEFAULT_ROLE_ID, DEFAULT_USERNAME, "Admin", user);
	}

	// ========== Unified Principal Factory Methods ==========

	/**
	 * Creates a valid UserPrincipal.
	 * @return UserPrincipal with valid data
	 */
	public static UserPrincipal createValidUserPrincipal() {
		return new UserPrincipal(createDefaultUser());
	}

	/**
	 * Creates a UserPrincipal with custom user.
	 * @param user the user to wrap
	 * @return UserPrincipal with the provided user
	 */
	public static UserPrincipal createUserPrincipal(User user) {
		return new UserPrincipal(user);
	}

	/**
	 * Creates a mock AuthPrincipal with null unique identifier.
	 * @return AuthPrincipal mock with null unique identifier
	 */
	public static AuthPrincipal createPrincipalWithNullUniqueIdentifier() {
		return new TestAuthPrincipal(null, DEFAULT_ACCOUNT_ID, DEFAULT_ROLE_ID, PrincipalType.USER);
	}

	/**
	 * Creates a mock AuthPrincipal with null organization identifier.
	 * @return AuthPrincipal mock with null organization identifier
	 */
	public static AuthPrincipal createPrincipalWithNullOrganizationId() {
		return new TestAuthPrincipal(DEFAULT_USER_ID, null, DEFAULT_ROLE_ID, PrincipalType.USER);
	}

	/**
	 * Creates a mock AuthPrincipal for contractor type.
	 * @return AuthPrincipal mock for contractor
	 */
	public static AuthPrincipal createContractorPrincipal() {
		return new TestAuthPrincipal(DEFAULT_USER_ID, DEFAULT_ACCOUNT_ID, DEFAULT_ROLE_ID, PrincipalType.CONTRACTOR);
	}

	/**
	 * Creates a mock AuthPrincipal for contact type.
	 * @return AuthPrincipal mock for contact
	 */
	public static AuthPrincipal createContactPrincipal() {
		return new TestAuthPrincipal(DEFAULT_USER_ID, DEFAULT_ACCOUNT_ID, DEFAULT_ROLE_ID, PrincipalType.CONTACT);
	}

	// ========== Default ID Getters ==========

	/**
	 * Gets the default user ID for testing.
	 * @return default user ID
	 */
	public static Integer getDefaultUserId() {
		return DEFAULT_USER_ID;
	}

	/**
	 * Gets the default account ID for testing.
	 * @return default account ID
	 */
	public static Integer getDefaultAccountId() {
		return DEFAULT_ACCOUNT_ID;
	}

	/**
	 * Gets the default role ID for testing.
	 * @return default role ID
	 */
	public static Integer getDefaultRoleId() {
		return DEFAULT_ROLE_ID;
	}

	// ========== Inner Test Implementation Classes ==========

	/**
	 * Test implementation of IAuthenticationPrincipal for testing purposes.
	 */
	public static final class TestAuthenticationPrincipal implements IAuthenticationPrincipal<Integer, User> {

		private final Integer uniqueIdentifier;

		private final Integer organizationIdentifier;

		private final Integer roleIdentifier;

		private final String userName;

		private final String roleLabel;

		private User user;

		/**
		 * Constructor for TestAuthenticationPrincipal.
		 * @param uniqueIdentifier the unique identifier
		 * @param organizationIdentifier the organization identifier
		 * @param roleIdentifier the role identifier
		 * @param userName the username
		 * @param roleLabel the role label
		 * @param user the user entity
		 */
		public TestAuthenticationPrincipal(Integer uniqueIdentifier, Integer organizationIdentifier,
				Integer roleIdentifier, String userName, String roleLabel, User user) {
			this.uniqueIdentifier = uniqueIdentifier;
			this.organizationIdentifier = organizationIdentifier;
			this.roleIdentifier = roleIdentifier;
			this.userName = userName;
			this.roleLabel = roleLabel;
			this.user = user;
		}

		@Override
		public Integer getUniqueIdentifier() {
			return this.uniqueIdentifier;
		}

		@Override
		public String getUserName() {
			return this.userName;
		}

		@Override
		public Integer getRoleIdentifier() {
			return this.roleIdentifier;
		}

		@Override
		public String getRoleIdentifierLabel() {
			return this.roleLabel;
		}

		@Override
		public Integer getOrganizationIdentifier() {
			return this.organizationIdentifier;
		}

		@Override
		public User getUser() {
			return this.user;
		}

		@Override
		public void setUser(User user) {
			this.user = user;
		}

	}

	/**
	 * Test implementation of AuthPrincipal for testing purposes.
	 */
	public static final class TestAuthPrincipal implements AuthPrincipal {

		private final Integer uniqueIdentifier;

		private final Integer organizationIdentifier;

		private final Integer roleIdentifier;

		private final PrincipalType principalType;

		/**
		 * Constructor for TestAuthPrincipal.
		 * @param uniqueIdentifier the unique identifier
		 * @param organizationIdentifier the organization identifier
		 * @param roleIdentifier the role identifier
		 * @param principalType the principal type
		 */
		public TestAuthPrincipal(Integer uniqueIdentifier, Integer organizationIdentifier, Integer roleIdentifier,
				PrincipalType principalType) {
			this.uniqueIdentifier = uniqueIdentifier;
			this.organizationIdentifier = organizationIdentifier;
			this.roleIdentifier = roleIdentifier;
			this.principalType = principalType;
		}

		@Override
		public PrincipalType getPrincipalType() {
			return this.principalType;
		}

		@Override
		public Integer getUniqueIdentifier() {
			return this.uniqueIdentifier;
		}

		@Override
		public Integer getOrganizationIdentifier() {
			return this.organizationIdentifier;
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
			return this.roleIdentifier;
		}

	}

	// ===== Message Constants (Inner Types Must Be Last) =====

	/**
	 * Message constants for test assertions. Note: Inner types must be placed at the end
	 * to comply with InnerTypeLast checkstyle rule.
	 */
	public static final class Messages {

		public static final String AUTHENTICATION_PRINCIPAL_NOT_FOUND = "Authentication principal not found.";

		public static final String UNIFIED_PRINCIPAL_NOT_FOUND = "Unified authentication principal not found.";

		public static final String PRINCIPAL_CANNOT_BE_NULL = "Principal cannot be null";

		public static final String PRINCIPAL_MUST_HAVE_VALID_IDENTIFIERS = "Principal must have valid unique and organization identifiers";

		/**
		 * Private constructor to prevent instantiation.
		 */
		private Messages() {
			// Messages class - prevent instantiation
		}

	}

}
