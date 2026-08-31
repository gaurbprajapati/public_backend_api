/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SimpleContactPrincipal class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class SimpleContactPrincipalTests {

	private static final Integer DEFAULT_ID = 1;

	private static final String DEFAULT_EMAIL = "contact@example.com";

	private static final Integer DEFAULT_ACCOUNT_ID = 100;

	private static final Integer DEFAULT_COMPANY_ID = 200;

	private static final String DEFAULT_FIRST_NAME = "John";

	private static final String DEFAULT_LAST_NAME = "Doe";

	private SimpleContactPrincipal simpleContactPrincipal;

	@BeforeEach
	void setUp() {
		this.simpleContactPrincipal = new SimpleContactPrincipal(DEFAULT_ID, DEFAULT_EMAIL, DEFAULT_ACCOUNT_ID,
				DEFAULT_COMPANY_ID, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME);
	}

	@Test
	@DisplayName("GetId should return id")
	void testGetIdReturnsId() {
		// When
		Integer result = this.simpleContactPrincipal.getId();

		// Then
		assertThat(result).isEqualTo(DEFAULT_ID);
	}

	@Test
	@DisplayName("GetEmail should return email")
	void testGetEmailReturnsEmail() {
		// When
		String result = this.simpleContactPrincipal.getEmail();

		// Then
		assertThat(result).isEqualTo(DEFAULT_EMAIL);
	}

	@Test
	@DisplayName("GetAccountId should return accountId")
	void testGetAccountIdReturnsAccountId() {
		// When
		Integer result = this.simpleContactPrincipal.getAccountId();

		// Then
		assertThat(result).isEqualTo(DEFAULT_ACCOUNT_ID);
	}

	@Test
	@DisplayName("GetCompanyId should return companyId")
	void testGetCompanyIdReturnsCompanyId() {
		// When
		Integer result = this.simpleContactPrincipal.getCompanyId();

		// Then
		assertThat(result).isEqualTo(DEFAULT_COMPANY_ID);
	}

	@Test
	@DisplayName("GetFirstName should return firstName")
	void testGetFirstNameReturnsFirstName() {
		// When
		String result = this.simpleContactPrincipal.getFirstName();

		// Then
		assertThat(result).isEqualTo(DEFAULT_FIRST_NAME);
	}

	@Test
	@DisplayName("GetLastName should return lastName")
	void testGetLastNameReturnsLastName() {
		// When
		String result = this.simpleContactPrincipal.getLastName();

		// Then
		assertThat(result).isEqualTo(DEFAULT_LAST_NAME);
	}

	@Test
	@DisplayName("ToString should return formatted string")
	void testToStringReturnsFormattedString() {
		// When
		String result = this.simpleContactPrincipal.toString();

		// Then
		assertThat(result).isEqualTo("Contact(" + DEFAULT_EMAIL + ")");
	}

}
