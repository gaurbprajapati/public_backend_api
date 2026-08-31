/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for {@link ClientPortalUrlHelper}.
 */
class ClientPortalUrlHelperTests {

	@Test
	@DisplayName("Resolve portal base URL returns production URL when env is null")
	void testResolvePortalBaseUrlNullEnvReturnsProductionUrl() {
		assertThat(ClientPortalUrlHelper.resolvePortalBaseUrl(null)).isEqualTo("https://portal.recruitcrm.io");
	}

	@ParameterizedTest
	@CsvSource({ "production,https://portal.recruitcrm.io", "prod,https://portal.recruitcrm.io",
			"canada,https://portal.recruitcrm.io", "hmp3,https://vms-test3.recruitcrm.io",
			"hmp2,https://vms-test2.recruitcrm.io", "hmp,https://vms-test.recruitcrm.io",
			"unknown,https://portal.recruitcrm.io" })
	@DisplayName("Resolve portal base URL returns expected URL for each environment")
	void testResolvePortalBaseUrlReturnsExpectedUrlForEnvironment(String env, String expectedUrl) {
		assertThat(ClientPortalUrlHelper.resolvePortalBaseUrl(env)).isEqualTo(expectedUrl);
	}

	@Test
	@DisplayName("Resolve signup portal URL appends client signup path with query parameters")
	void testResolveSignupPortalUrlAppendsClientSignupPathWithQueryParameters() {
		assertThat(ClientPortalUrlHelper.resolveSignupPortalUrl("hmp3", "jane.doe@acmecorp.com", "Acme Corp", 67_890,
				12_345, 11_111))
			.isEqualTo("https://vms-test3.recruitcrm.io/signup?email=jane.doe@acmecorp.com"
					+ "&companyName=Acme%20Corp&accountId=67890&rcrmCompanyId=12345&rcrmContactId=11111");
	}

	@Test
	@DisplayName("Resolve signup portal URL omits blank and null query parameters")
	void testResolveSignupPortalUrlOmitsBlankAndNullQueryParameters() {
		assertThat(ClientPortalUrlHelper.resolveSignupPortalUrl("production", "jane@example.com", null, 100, 78, null))
			.isEqualTo(
					"https://portal.recruitcrm.io/signup?email=jane@example.com&accountId=100" + "&rcrmCompanyId=78");
	}

	@Test
	@DisplayName("Resolve client login portal URL appends client login path")
	void testResolveClientLoginPortalUrlAppendsClientLoginPath() {
		assertThat(ClientPortalUrlHelper.resolveClientLoginPortalUrl("production"))
			.isEqualTo("https://portal.recruitcrm.io/client/login");
	}

	@Test
	@DisplayName("Resolve hiring manager name combines first and last name")
	void testResolveHiringManagerNameCombinesFirstAndLastName() {
		assertThat(ClientPortalUrlHelper.resolveHiringManagerName("Shivang", "Saini")).isEqualTo("Shivang Saini");
	}

	@Test
	@DisplayName("Resolve hiring manager name returns first name when last name missing")
	void testResolveHiringManagerNameReturnsFirstNameWhenLastNameMissing() {
		assertThat(ClientPortalUrlHelper.resolveHiringManagerName("Shivang", null)).isEqualTo("Shivang");
	}

	@Test
	@DisplayName("Resolve hiring manager name returns last name when first name missing")
	void testResolveHiringManagerNameReturnsLastNameWhenFirstNameMissing() {
		assertThat(ClientPortalUrlHelper.resolveHiringManagerName(null, "Saini")).isEqualTo("Saini");
	}

	@Test
	@DisplayName("Resolve hiring manager name returns empty string when both names missing")
	void testResolveHiringManagerNameReturnsEmptyStringWhenBothNamesMissing() {
		assertThat(ClientPortalUrlHelper.resolveHiringManagerName(null, null)).isEmpty();
	}

	@Test
	@DisplayName("Resolve contact first name trims and returns empty string when missing")
	void testResolveContactFirstNameTrimsAndReturnsEmptyStringWhenMissing() {
		assertThat(ClientPortalUrlHelper.resolveContactFirstName("  Shivang  ")).isEqualTo("Shivang");
		assertThat(ClientPortalUrlHelper.resolveContactFirstName(null)).isEmpty();
	}

	@Test
	@DisplayName("Resolve agency name trims and returns empty string when missing")
	void testResolveAgencyNameTrimsAndReturnsEmptyStringWhenMissing() {
		assertThat(ClientPortalUrlHelper.resolveAgencyName("  RecruitCRM  ")).isEqualTo("RecruitCRM");
		assertThat(ClientPortalUrlHelper.resolveAgencyName(null)).isEmpty();
	}

	@Test
	@DisplayName("Resolve signup portal URL omits blank email and company name")
	void testResolveSignupPortalUrlOmitsBlankEmailAndCompanyName() {
		assertThat(ClientPortalUrlHelper.resolveSignupPortalUrl("hmp", "   ", "   ", null, null, 222))
			.isEqualTo("https://vms-test.recruitcrm.io/signup?rcrmContactId=222");
	}

	@Test
	@DisplayName("Resolve signup portal URL returns base signup path when all optional params are null")
	void testResolveSignupPortalUrlAllOptionalParamsNullReturnsBaseSignupPath() {
		assertThat(ClientPortalUrlHelper.resolveSignupPortalUrl("hmp2", null, null, null, null, null))
			.isEqualTo("https://vms-test2.recruitcrm.io/signup");
	}

	@Test
	@DisplayName("Resolve contact first name returns empty string when value is blank")
	void testResolveContactFirstNameBlankReturnsEmptyString() {
		assertThat(ClientPortalUrlHelper.resolveContactFirstName("   ")).isEmpty();
	}

	@Test
	@DisplayName("Resolve agency name returns empty string when value is blank")
	void testResolveAgencyNameBlankReturnsEmptyString() {
		assertThat(ClientPortalUrlHelper.resolveAgencyName("   ")).isEmpty();
	}

	@Test
	@DisplayName("Resolve agency user name trims value and returns empty string when missing or blank")
	void testResolveAgencyUserNameTrimsValueAndReturnsEmptyStringWhenMissingOrBlank() {
		assertThat(ClientPortalUrlHelper.resolveAgencyUserName("  Shivang Saini  ")).isEqualTo("Shivang Saini");
		assertThat(ClientPortalUrlHelper.resolveAgencyUserName(null)).isEmpty();
		assertThat(ClientPortalUrlHelper.resolveAgencyUserName("   ")).isEmpty();
	}

	@Test
	@DisplayName("Resolve hiring manager name returns empty string when both names are blank")
	void testResolveHiringManagerNameBlankNamesReturnsEmptyString() {
		assertThat(ClientPortalUrlHelper.resolveHiringManagerName("   ", "   ")).isEmpty();
	}

}
