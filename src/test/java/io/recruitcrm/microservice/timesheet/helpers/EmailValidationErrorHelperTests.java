package io.recruitcrm.microservice.timesheet.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.testdata.EmailValidationErrorHelperTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EmailValidationErrorHelper Tests")
class EmailValidationErrorHelperTests {

	private EmailValidationErrorHelper emailValidationErrorHelper;

	@BeforeEach
	void setUp() {
		this.emailValidationErrorHelper = new EmailValidationErrorHelper();
	}

	@Test
	@DisplayName("isTimesheetSubmitted should return true when status is submitted")
	void testIsTimesheetSubmittedSubmittedStatusReturnsTrue() {
		// Given
		Integer statusId = EmailValidationErrorHelperTestDataFactory.getSubmittedStatusId();

		// When
		boolean result = this.emailValidationErrorHelper.isTimesheetSubmitted(statusId);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isTimesheetSubmitted should return false when status is not submitted")
	void testIsTimesheetSubmittedNonSubmittedStatusReturnsFalse() {
		// Given
		Integer statusId = EmailValidationErrorHelperTestDataFactory.getOpenStatusId();

		// When
		boolean result = this.emailValidationErrorHelper.isTimesheetSubmitted(statusId);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isTimesheetApproved should return true when status is approved")
	void testIsTimesheetApprovedApprovedStatusReturnsTrue() {
		// Given
		Integer statusId = EmailValidationErrorHelperTestDataFactory.getApprovedStatusId();

		// When
		boolean result = this.emailValidationErrorHelper.isTimesheetApproved(statusId);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isTimesheetApproved should return false when status is not approved")
	void testIsTimesheetApprovedNonApprovedStatusReturnsFalse() {
		// Given
		Integer statusId = EmailValidationErrorHelperTestDataFactory.getRejectedStatusId();

		// When
		boolean result = this.emailValidationErrorHelper.isTimesheetApproved(statusId);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isTimesheetOpen should return true when status is open")
	void testIsTimesheetOpenOpenStatusReturnsTrue() {
		// Given
		Integer statusId = EmailValidationErrorHelperTestDataFactory.getOpenStatusId();

		// When
		boolean result = this.emailValidationErrorHelper.isTimesheetOpen(statusId);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isTimesheetOpen should return false when status is not open")
	void testIsTimesheetOpenNonOpenStatusReturnsFalse() {
		// Given
		Integer statusId = EmailValidationErrorHelperTestDataFactory.getUnknownStatusId();

		// When
		boolean result = this.emailValidationErrorHelper.isTimesheetOpen(statusId);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isTimesheetRejected should return true when status is rejected")
	void testIsTimesheetRejectedRejectedStatusReturnsTrue() {
		// Given
		Integer statusId = EmailValidationErrorHelperTestDataFactory.getRejectedStatusId();

		// When
		boolean result = this.emailValidationErrorHelper.isTimesheetRejected(statusId);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isTimesheetRejected should return false when status is not rejected")
	void testIsTimesheetRejectedNonRejectedStatusReturnsFalse() {
		// Given
		Integer statusId = EmailValidationErrorHelperTestDataFactory.getApprovedStatusId();

		// When
		boolean result = this.emailValidationErrorHelper.isTimesheetRejected(statusId);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isEmailMissing should return true when email is null")
	void testIsEmailMissingNullEmailReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper.isEmailMissing(null);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isEmailMissing should return true when email is blank")
	void testIsEmailMissingBlankEmailReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isEmailMissing(EmailValidationErrorHelperTestDataFactory.getBlankEmail());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isEmailMissing should return false when email is present")
	void testIsEmailMissingValidEmailReturnsFalse() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isEmailMissing(EmailValidationErrorHelperTestDataFactory.getValidEmail());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isOptedOutOfEmail should return true when opt-out flag is one")
	void testIsOptedOutOfEmailOptOutFlagReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isOptedOutOfEmail(EmailValidationErrorHelperTestDataFactory.getOptedOutFlag());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isOptedOutOfEmail should return false when opt-out flag is null")
	void testIsOptedOutOfEmailNullFlagReturnsFalse() {
		// When
		boolean result = this.emailValidationErrorHelper.isOptedOutOfEmail(null);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isOptedOutOfEmail should return false when opt-out flag is zero")
	void testIsOptedOutOfEmailZeroFlagReturnsFalse() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isOptedOutOfEmail(EmailValidationErrorHelperTestDataFactory.getNotOptedOutFlag());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isRecordDeleted should return true when deleted flag is one")
	void testIsRecordDeletedDeletedFlagReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isRecordDeleted(EmailValidationErrorHelperTestDataFactory.getDeletedFlag());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isRecordDeleted should return false when deleted flag is null")
	void testIsRecordDeletedNullFlagReturnsFalse() {
		// When
		boolean result = this.emailValidationErrorHelper.isRecordDeleted(null);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isRecordDeleted should return false when deleted flag is zero")
	void testIsRecordDeletedZeroFlagReturnsFalse() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isRecordDeleted(EmailValidationErrorHelperTestDataFactory.getNotDeletedFlag());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isContractorUnassigned should return true when assignment id is null")
	void testIsContractorUnassignedNullAssignmentIdReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper.isContractorUnassigned(null);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isContractorUnassigned should return false when assignment id is present")
	void testIsContractorUnassignedPresentAssignmentIdReturnsFalse() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isContractorUnassigned(EmailValidationErrorHelperTestDataFactory.getDefaultAssignmentId());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isPortalNotExist should return true when portal status id is invitation not sent")
	void testIsPortalNotExistInvitationNotSentStatusReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isPortalNotExist(EmailValidationErrorHelperTestDataFactory.getPortalInvitationNotSentStatusId());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isPortalNotExist should return true when portal status id is invitation sent")
	void testIsPortalNotExistInvitationSentStatusReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isPortalNotExist(EmailValidationErrorHelperTestDataFactory.getPortalInvitationSentStatusId());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isPortalNotExist should return false when portal status id is portal enabled")
	void testIsPortalNotExistPortalEnabledStatusReturnsFalse() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isPortalNotExist(EmailValidationErrorHelperTestDataFactory.getPortalEnabledStatusId());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isPortalNotExist should return false when portal status id is portal disabled")
	void testIsPortalNotExistPortalDisabledStatusReturnsFalse() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isPortalNotExist(EmailValidationErrorHelperTestDataFactory.getPortalDisabledStatusId());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isPortalNotExist should return true when portal status id is null")
	void testIsPortalNotExistNullPortalStatusIdReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper.isPortalNotExist(null);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isPortalDisabled should return true when portal status id is disabled")
	void testIsPortalDisabledDisabledStatusReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isPortalDisabled(EmailValidationErrorHelperTestDataFactory.getPortalDisabledStatusId());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isPortalDisabled should return false when portal status id is null")
	void testIsPortalDisabledNullStatusReturnsFalse() {
		// When
		boolean result = this.emailValidationErrorHelper.isPortalDisabled(null);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isPortalDisabled should return false when portal status id is not disabled")
	void testIsPortalDisabledEnabledStatusReturnsFalse() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isPortalDisabled(EmailValidationErrorHelperTestDataFactory.getPortalEnabledStatusId());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isTimesheetNotSharedWithClient should return true when shared flag is null")
	void testIsTimesheetNotSharedWithClientNullFlagReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper.isTimesheetNotSharedWithClient(null);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isTimesheetNotSharedWithClient should return true when shared flag is zero")
	void testIsTimesheetNotSharedWithClientZeroFlagReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isTimesheetNotSharedWithClient(EmailValidationErrorHelperTestDataFactory.getNotSharedWithClientFlag());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isTimesheetNotSharedWithClient should return false when shared flag is one")
	void testIsTimesheetNotSharedWithClientSharedFlagReturnsFalse() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isTimesheetNotSharedWithClient(EmailValidationErrorHelperTestDataFactory.getSharedWithClientFlag());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isTimesheetNotSharedWithContact should return true when shared flag is null")
	void testIsTimesheetNotSharedWithContactNullFlagReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper.isTimesheetNotSharedWithContact(null);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isTimesheetNotSharedWithContact should return true when shared flag is zero")
	void testIsTimesheetNotSharedWithContactZeroFlagReturnsTrue() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isTimesheetNotSharedWithContact(EmailValidationErrorHelperTestDataFactory.getNotSharedWithContactFlag());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isTimesheetNotSharedWithContact should return false when shared flag is one")
	void testIsTimesheetNotSharedWithContactSharedFlagReturnsFalse() {
		// When
		boolean result = this.emailValidationErrorHelper
			.isTimesheetNotSharedWithContact(EmailValidationErrorHelperTestDataFactory.getSharedWithContactFlag());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("validateEmailFormat should return null for valid email")
	void testValidateEmailFormatValidEmailReturnsNull() {
		// When
		String result = this.emailValidationErrorHelper
			.validateEmailFormat(EmailValidationErrorHelperTestDataFactory.getValidEmail());

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("validateEmailFormat should return error code for invalid email")
	void testValidateEmailFormatInvalidEmailReturnsErrorCode() {
		// When
		String result = this.emailValidationErrorHelper
			.validateEmailFormat(EmailValidationErrorHelperTestDataFactory.getInvalidEmail());

		// Then
		assertThat(result).isEqualTo("incorrect_email");
	}

	@Test
	@DisplayName("buildFullName should return first name only when last name is empty")
	void testBuildFullNameEmptyLastNameReturnsFirstNameOnly() {
		// When
		String result = this.emailValidationErrorHelper
			.buildFullName(EmailValidationErrorHelperTestDataFactory.getFirstName(), "");

		// Then
		assertThat(result).isEqualTo(EmailValidationErrorHelperTestDataFactory.getFirstName());
	}

	@Test
	@DisplayName("buildFullName should return trimmed full name when both names are present")
	void testBuildFullNameBothNamesPresentReturnsFullName() {
		// When
		String result = this.emailValidationErrorHelper.buildFullName(
				EmailValidationErrorHelperTestDataFactory.getWhitespaceFirstName(),
				EmailValidationErrorHelperTestDataFactory.getWhitespaceLastName());

		// Then
		assertThat(result).isEqualTo("Jane Doe");
	}

	@Test
	@DisplayName("buildFullName should handle null first and last names")
	void testBuildFullNameNullNamesReturnsEmptyString() {
		// When
		String result = this.emailValidationErrorHelper.buildFullName(null, null);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("validateClientEmailFormat should return required message when email is null")
	void testValidateClientEmailFormatNullEmailReturnsRequiredMessage() {
		// When
		String result = this.emailValidationErrorHelper.validateClientEmailFormat(null);

		// Then
		assertThat(result).isEqualTo("email is required");
	}

	@Test
	@DisplayName("validateClientEmailFormat should return required message when email is blank")
	void testValidateClientEmailFormatBlankEmailReturnsRequiredMessage() {
		// When
		String result = this.emailValidationErrorHelper.validateClientEmailFormat("   ");

		// Then
		assertThat(result).isEqualTo("email is required");
	}

	@Test
	@DisplayName("validateClientEmailFormat should return invalid message when email format is incorrect")
	void testValidateClientEmailFormatInvalidEmailReturnsInvalidMessage() {
		// When
		String result = this.emailValidationErrorHelper.validateClientEmailFormat("not-an-email");

		// Then
		assertThat(result).isEqualTo("email must be a valid email address");
	}

	@Test
	@DisplayName("validateClientEmailFormat should return null when email is valid")
	void testValidateClientEmailFormatValidEmailReturnsNull() {
		// When
		String result = this.emailValidationErrorHelper.validateClientEmailFormat("  jane.doe@example.com  ");

		// Then
		assertThat(result).isNull();
	}

}
