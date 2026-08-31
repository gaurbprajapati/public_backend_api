package io.recruitcrm.microservice.timesheet.helpers;

import org.springframework.web.util.UriComponentsBuilder;

/**
 * Resolves client portal URLs based on deployment environment.
 */
public final class ClientPortalUrlHelper {

	private static final String PRODUCTION_PORTAL_BASE_URL = "https://portal.recruitcrm.io";

	private static final String HMP3_PORTAL_BASE_URL = "https://vms-test3.recruitcrm.io";

	private static final String HMP2_PORTAL_BASE_URL = "https://vms-test2.recruitcrm.io";

	private static final String HMP_PORTAL_BASE_URL = "https://vms-test.recruitcrm.io";

	private static final String CLIENT_SIGNUP_PATH = "/signup";

	private static final String CLIENT_LOGIN_PATH = "/client/login";

	private ClientPortalUrlHelper() {
	}

	public static String resolvePortalBaseUrl(String env) {
		if (env == null) {
			return PRODUCTION_PORTAL_BASE_URL;
		}
		return switch (env.toLowerCase()) {
			case "prod", "production", "canada" -> PRODUCTION_PORTAL_BASE_URL;
			case "hmp3" -> HMP3_PORTAL_BASE_URL;
			case "hmp2" -> HMP2_PORTAL_BASE_URL;
			case "hmp" -> HMP_PORTAL_BASE_URL;
			default -> PRODUCTION_PORTAL_BASE_URL;
		};
	}

	public static String resolveSignupPortalUrl(String env, String email, String companyName, Integer accountId,
			Integer rcrmCompanyId, Integer rcrmContactId) {
		UriComponentsBuilder builder = UriComponentsBuilder
			.fromUriString(resolvePortalBaseUrl(env) + CLIENT_SIGNUP_PATH);
		if ((email != null) && !email.isBlank()) {
			builder.queryParam("email", email.trim());
		}
		if ((companyName != null) && !companyName.isBlank()) {
			builder.queryParam("companyName", companyName.trim());
		}
		if (accountId != null) {
			builder.queryParam("accountId", accountId);
		}
		if (rcrmCompanyId != null) {
			builder.queryParam("rcrmCompanyId", rcrmCompanyId);
		}
		if (rcrmContactId != null) {
			builder.queryParam("rcrmContactId", rcrmContactId);
		}
		return builder.build().encode().toUriString();
	}

	public static String resolveClientLoginPortalUrl(String env) {
		return resolvePortalBaseUrl(env) + CLIENT_LOGIN_PATH;
	}

	public static String resolveHiringManagerName(String firstName, String lastName) {
		boolean hasFirstName = (firstName != null) && !firstName.isBlank();
		boolean hasLastName = (lastName != null) && !lastName.isBlank();
		if (hasFirstName && hasLastName) {
			return firstName.trim() + " " + lastName.trim();
		}
		if (hasFirstName) {
			return firstName.trim();
		}
		if (hasLastName) {
			return lastName.trim();
		}
		return "";
	}

	public static String resolveContactFirstName(String firstName) {
		return ((firstName != null) && !firstName.isBlank()) ? firstName.trim() : "";
	}

	public static String resolveAgencyName(String agencyName) {
		return ((agencyName != null) && !agencyName.isBlank()) ? agencyName.trim() : "";
	}

	public static String resolveAgencyUserName(String recruiterName) {
		return ((recruiterName != null) && !recruiterName.isBlank()) ? recruiterName.trim() : "";
	}

}
