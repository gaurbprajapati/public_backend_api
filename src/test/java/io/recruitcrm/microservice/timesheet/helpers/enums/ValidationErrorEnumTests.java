package io.recruitcrm.microservice.timesheet.helpers.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Unit tests for ValidationErrorEnum enum. Tests all enum values, getters, and
 * properties. This test class ensures 100% code coverage for ValidationErrorEnum.
 */
class ValidationErrorEnumTests {

	@Test
	@DisplayName("Should have correct TIMESHEET_APPROVED enum values")
	void shouldHaveCorrectTimesheetApprovedEnumValues() {
		// When
		ValidationErrorEnum timesheetApproved = ValidationErrorEnum.TIMESHEET_APPROVED;

		// Then
		assertThat(timesheetApproved).isNotNull();
		assertThat(timesheetApproved.getMessage()).isEqualTo("approved");
	}

	@Test
	@DisplayName("Should have correct TIMESHEET_DIFFERENT_SETTINGS enum values")
	void shouldHaveCorrectTimesheetDifferentSettingsEnumValues() {
		// When
		ValidationErrorEnum differentSettings = ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS;

		// Then
		assertThat(differentSettings).isNotNull();
		assertThat(differentSettings.getMessage()).isEqualTo("different_setting");
	}

	@Test
	@DisplayName("Should have correct TIMESHEET_DIFFERENT_PERIOD enum values")
	void shouldHaveCorrectTimesheetDifferentPeriodEnumValues() {
		// When
		ValidationErrorEnum differentPeriod = ValidationErrorEnum.TIMESHEET_DIFFERENT_PERIOD;

		// Then
		assertThat(differentPeriod).isNotNull();
		assertThat(differentPeriod.getMessage()).isEqualTo("different_period");
	}

	@Test
	@DisplayName("Should have correct TIMESHEET_ANOTHER_JOB enum values")
	void shouldHaveCorrectTimesheetAnotherJobEnumValues() {
		// When
		ValidationErrorEnum anotherJob = ValidationErrorEnum.TIMESHEET_ANOTHER_JOB;

		// Then
		assertThat(anotherJob).isNotNull();
		assertThat(anotherJob.getMessage()).isEqualTo("another_job");
	}

	@Test
	@DisplayName("Should have correct NO_EDIT_ACCESS enum values")
	void shouldHaveCorrectNoEditAccessEnumValues() {
		// When
		ValidationErrorEnum noEditAccess = ValidationErrorEnum.NO_EDIT_ACCESS;

		// Then
		assertThat(noEditAccess).isNotNull();
		assertThat(noEditAccess.getMessage()).isEqualTo("no_edit_access");
	}

	@Test
	@DisplayName("Should have correct NOT_APPROVED enum values")
	void shouldHaveCorrectNotApprovedEnumValues() {
		// When
		ValidationErrorEnum notApproved = ValidationErrorEnum.NOT_APPROVED;

		// Then
		assertThat(notApproved).isNotNull();
		assertThat(notApproved.getMessage()).isEqualTo("not_approved");
	}

	@Test
	@DisplayName("Should have correct DIFFERENT_COMPANY enum values")
	void shouldHaveCorrectDifferentCompanyEnumValues() {
		// When
		ValidationErrorEnum differentCompany = ValidationErrorEnum.DIFFERENT_COMPANY;

		// Then
		assertThat(differentCompany).isNotNull();
		assertThat(differentCompany.getMessage()).isEqualTo("different_company");
	}

	@Test
	@DisplayName("Should have correct DIFFERENT_CURRENCY enum values")
	void shouldHaveCorrectDifferentCurrencyEnumValues() {
		// When
		ValidationErrorEnum differentCurrency = ValidationErrorEnum.DIFFERENT_CURRENCY;

		// Then
		assertThat(differentCurrency).isNotNull();
		assertThat(differentCurrency.getMessage()).isEqualTo("different_currency");
	}

	@Test
	@DisplayName("Should have correct ALREADY_BILLED enum values")
	void shouldHaveCorrectAlreadyBilledEnumValues() {
		// When
		ValidationErrorEnum alreadyBilled = ValidationErrorEnum.ALREADY_BILLED;

		// Then
		assertThat(alreadyBilled).isNotNull();
		assertThat(alreadyBilled.getMessage()).isEqualTo("already_billed");
	}

	@Test
	@DisplayName("Should have correct ALREADY_COLLECTED enum values")
	void shouldHaveCorrectAlreadyCollectedEnumValues() {
		// When
		ValidationErrorEnum alreadyCollected = ValidationErrorEnum.ALREADY_COLLECTED;

		// Then
		assertThat(alreadyCollected).isNotNull();
		assertThat(alreadyCollected.getMessage()).isEqualTo("already_collected");
	}

	@Test
	@DisplayName("Should have correct UNBILLED_AND_INVOICE_ATTACHED enum values")
	void shouldHaveCorrectUnbilledAndInvoiceAttachedEnumValues() {
		ValidationErrorEnum unbilled = ValidationErrorEnum.UNBILLED_AND_INVOICE_ATTACHED;
		assertThat(unbilled).isNotNull();
		assertThat(unbilled.getMessage()).isEqualTo("unbilled_and_invoice_attached");
	}

	@Test
	@DisplayName("Should have exactly eleven enum values")
	void shouldHaveExactlyElevenEnumValues() {
		// When
		ValidationErrorEnum[] values = ValidationErrorEnum.values();

		// Then
		assertThat(values).hasSize(11)
			.containsExactlyInAnyOrder(ValidationErrorEnum.TIMESHEET_APPROVED,
					ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS, ValidationErrorEnum.TIMESHEET_DIFFERENT_PERIOD,
					ValidationErrorEnum.TIMESHEET_ANOTHER_JOB, ValidationErrorEnum.NO_EDIT_ACCESS,
					ValidationErrorEnum.NOT_APPROVED, ValidationErrorEnum.DIFFERENT_COMPANY,
					ValidationErrorEnum.DIFFERENT_CURRENCY, ValidationErrorEnum.ALREADY_BILLED,
					ValidationErrorEnum.ALREADY_COLLECTED, ValidationErrorEnum.UNBILLED_AND_INVOICE_ATTACHED);
	}

	@ParameterizedTest
	@EnumSource(ValidationErrorEnum.class)
	@DisplayName("Should have non-null message for all enum values")
	void shouldHaveNonNullMessageForAllEnumValues(ValidationErrorEnum errorEnum) {
		// When & Then
		assertThat(errorEnum.getMessage()).isNotNull();
		assertThat(errorEnum.getMessage()).isNotBlank();
	}

	@ParameterizedTest
	@EnumSource(ValidationErrorEnum.class)
	@DisplayName("Should have meaningful message for all enum values")
	void shouldHaveMeaningfulMessageForAllEnumValues(ValidationErrorEnum errorEnum) {
		// When & Then
		assertThat(errorEnum.getMessage()).isNotEmpty();
		assertThat(errorEnum.getMessage().length()).isGreaterThan(0);
	}

	@Test
	@DisplayName("Should support valueOf method")
	void shouldSupportValueOfMethod() {
		// When & Then
		assertThat(ValidationErrorEnum.valueOf("TIMESHEET_APPROVED")).isEqualTo(ValidationErrorEnum.TIMESHEET_APPROVED);
		assertThat(ValidationErrorEnum.valueOf("TIMESHEET_DIFFERENT_SETTINGS"))
			.isEqualTo(ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS);
		assertThat(ValidationErrorEnum.valueOf("TIMESHEET_DIFFERENT_PERIOD"))
			.isEqualTo(ValidationErrorEnum.TIMESHEET_DIFFERENT_PERIOD);
		assertThat(ValidationErrorEnum.valueOf("TIMESHEET_ANOTHER_JOB"))
			.isEqualTo(ValidationErrorEnum.TIMESHEET_ANOTHER_JOB);
		assertThat(ValidationErrorEnum.valueOf("NO_EDIT_ACCESS")).isEqualTo(ValidationErrorEnum.NO_EDIT_ACCESS);
		assertThat(ValidationErrorEnum.valueOf("NOT_APPROVED")).isEqualTo(ValidationErrorEnum.NOT_APPROVED);
		assertThat(ValidationErrorEnum.valueOf("DIFFERENT_COMPANY")).isEqualTo(ValidationErrorEnum.DIFFERENT_COMPANY);
		assertThat(ValidationErrorEnum.valueOf("DIFFERENT_CURRENCY")).isEqualTo(ValidationErrorEnum.DIFFERENT_CURRENCY);
		assertThat(ValidationErrorEnum.valueOf("ALREADY_BILLED")).isEqualTo(ValidationErrorEnum.ALREADY_BILLED);
		assertThat(ValidationErrorEnum.valueOf("ALREADY_COLLECTED")).isEqualTo(ValidationErrorEnum.ALREADY_COLLECTED);
		assertThat(ValidationErrorEnum.valueOf("UNBILLED_AND_INVOICE_ATTACHED"))
			.isEqualTo(ValidationErrorEnum.UNBILLED_AND_INVOICE_ATTACHED);
	}

	@Test
	@DisplayName("Should support name method")
	void shouldSupportNameMethod() {
		// When & Then
		assertThat(ValidationErrorEnum.TIMESHEET_APPROVED.name()).isEqualTo("TIMESHEET_APPROVED");
		assertThat(ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS.name()).isEqualTo("TIMESHEET_DIFFERENT_SETTINGS");
		assertThat(ValidationErrorEnum.TIMESHEET_DIFFERENT_PERIOD.name()).isEqualTo("TIMESHEET_DIFFERENT_PERIOD");
		assertThat(ValidationErrorEnum.TIMESHEET_ANOTHER_JOB.name()).isEqualTo("TIMESHEET_ANOTHER_JOB");
		assertThat(ValidationErrorEnum.NO_EDIT_ACCESS.name()).isEqualTo("NO_EDIT_ACCESS");
		assertThat(ValidationErrorEnum.NOT_APPROVED.name()).isEqualTo("NOT_APPROVED");
		assertThat(ValidationErrorEnum.DIFFERENT_COMPANY.name()).isEqualTo("DIFFERENT_COMPANY");
		assertThat(ValidationErrorEnum.DIFFERENT_CURRENCY.name()).isEqualTo("DIFFERENT_CURRENCY");
		assertThat(ValidationErrorEnum.ALREADY_BILLED.name()).isEqualTo("ALREADY_BILLED");
		assertThat(ValidationErrorEnum.ALREADY_COLLECTED.name()).isEqualTo("ALREADY_COLLECTED");
		assertThat(ValidationErrorEnum.UNBILLED_AND_INVOICE_ATTACHED.name()).isEqualTo("UNBILLED_AND_INVOICE_ATTACHED");
	}

	@Test
	@DisplayName("Should support ordinal method")
	void shouldSupportOrdinalMethod() {
		// When & Then
		assertThat(ValidationErrorEnum.TIMESHEET_APPROVED.ordinal()).isZero();
		assertThat(ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS.ordinal()).isEqualTo(1);
		assertThat(ValidationErrorEnum.TIMESHEET_DIFFERENT_PERIOD.ordinal()).isEqualTo(2);
		assertThat(ValidationErrorEnum.TIMESHEET_ANOTHER_JOB.ordinal()).isEqualTo(3);
		assertThat(ValidationErrorEnum.NO_EDIT_ACCESS.ordinal()).isEqualTo(4);
		assertThat(ValidationErrorEnum.NOT_APPROVED.ordinal()).isEqualTo(5);
		assertThat(ValidationErrorEnum.DIFFERENT_COMPANY.ordinal()).isEqualTo(6);
		assertThat(ValidationErrorEnum.DIFFERENT_CURRENCY.ordinal()).isEqualTo(7);
		assertThat(ValidationErrorEnum.ALREADY_BILLED.ordinal()).isEqualTo(8);
		assertThat(ValidationErrorEnum.ALREADY_COLLECTED.ordinal()).isEqualTo(9);
		assertThat(ValidationErrorEnum.UNBILLED_AND_INVOICE_ATTACHED.ordinal()).isEqualTo(10);
	}

	@Test
	@DisplayName("Should support equals and hashCode")
	void shouldSupportEqualsAndHashCode() {
		// When & Then
		assertThat(ValidationErrorEnum.TIMESHEET_APPROVED).isEqualTo(ValidationErrorEnum.TIMESHEET_APPROVED)
			.isNotEqualTo(ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS);
		assertThat(ValidationErrorEnum.TIMESHEET_APPROVED.hashCode())
			.isEqualTo(ValidationErrorEnum.TIMESHEET_APPROVED.hashCode())
			.isNotEqualTo(ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS.hashCode());
	}

	@Test
	@DisplayName("Should support toString method")
	void shouldSupportToStringMethod() {
		// When & Then
		assertThat(ValidationErrorEnum.TIMESHEET_APPROVED).hasToString("TIMESHEET_APPROVED");
		assertThat(ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS).hasToString("TIMESHEET_DIFFERENT_SETTINGS");
		assertThat(ValidationErrorEnum.TIMESHEET_DIFFERENT_PERIOD).hasToString("TIMESHEET_DIFFERENT_PERIOD");
		assertThat(ValidationErrorEnum.TIMESHEET_ANOTHER_JOB).hasToString("TIMESHEET_ANOTHER_JOB");
		assertThat(ValidationErrorEnum.NO_EDIT_ACCESS).hasToString("NO_EDIT_ACCESS");
		assertThat(ValidationErrorEnum.NOT_APPROVED).hasToString("NOT_APPROVED");
		assertThat(ValidationErrorEnum.DIFFERENT_COMPANY).hasToString("DIFFERENT_COMPANY");
		assertThat(ValidationErrorEnum.DIFFERENT_CURRENCY).hasToString("DIFFERENT_CURRENCY");
		assertThat(ValidationErrorEnum.ALREADY_BILLED).hasToString("ALREADY_BILLED");
		assertThat(ValidationErrorEnum.ALREADY_COLLECTED).hasToString("ALREADY_COLLECTED");
		assertThat(ValidationErrorEnum.UNBILLED_AND_INVOICE_ATTACHED).hasToString("UNBILLED_AND_INVOICE_ATTACHED");
	}

	@Test
	@DisplayName("Should have unique message values")
	void shouldHaveUniqueMessageValues() {
		// When
		ValidationErrorEnum[] values = ValidationErrorEnum.values();

		// Then
		for (int i = 0; i < values.length; i++) {
			for (int j = i + 1; j < values.length; j++) {
				assertThat(values[i].getMessage()).isNotEqualTo(values[j].getMessage());
			}
		}
	}

	@Test
	@DisplayName("Should have consistent message format")
	void shouldHaveConsistentMessageFormat() {
		// When & Then
		// All messages should be lowercase with underscores
		assertThat(ValidationErrorEnum.TIMESHEET_APPROVED.getMessage()).matches("^[a-z_]+$");
		assertThat(ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS.getMessage()).matches("^[a-z_]+$");
		assertThat(ValidationErrorEnum.TIMESHEET_DIFFERENT_PERIOD.getMessage()).matches("^[a-z_]+$");
		assertThat(ValidationErrorEnum.TIMESHEET_ANOTHER_JOB.getMessage()).matches("^[a-z_]+$");
		assertThat(ValidationErrorEnum.NO_EDIT_ACCESS.getMessage()).matches("^[a-z_]+$");
		assertThat(ValidationErrorEnum.NOT_APPROVED.getMessage()).matches("^[a-z_]+$");
		assertThat(ValidationErrorEnum.DIFFERENT_COMPANY.getMessage()).matches("^[a-z_]+$");
		assertThat(ValidationErrorEnum.DIFFERENT_CURRENCY.getMessage()).matches("^[a-z_]+$");
		assertThat(ValidationErrorEnum.ALREADY_BILLED.getMessage()).matches("^[a-z_]+$");
		assertThat(ValidationErrorEnum.ALREADY_COLLECTED.getMessage()).matches("^[a-z_]+$");
		assertThat(ValidationErrorEnum.UNBILLED_AND_INVOICE_ATTACHED.getMessage()).matches("^[a-z_]+$");
	}

	@Test
	@DisplayName("Should be serializable")
	void shouldBeSerializable() {
		// When & Then
		// Enum should be serializable by default
		assertThat(java.io.Serializable.class).isAssignableFrom(ValidationErrorEnum.class);
	}

	@Test
	@DisplayName("Should support compareTo method")
	void shouldSupportCompareToMethod() {
		// When & Then
		assertThat(ValidationErrorEnum.TIMESHEET_APPROVED.compareTo(ValidationErrorEnum.TIMESHEET_APPROVED))
			.isEqualByComparingTo(0);
		assertThat(ValidationErrorEnum.TIMESHEET_APPROVED.compareTo(ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS))
			.isLessThan(0);
		assertThat(ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS.compareTo(ValidationErrorEnum.TIMESHEET_APPROVED))
			.isGreaterThan(0);
	}

	@Test
	@DisplayName("Should support getDeclaringClass method")
	void shouldSupportGetDeclaringClassMethod() {
		// When & Then
		assertThat(ValidationErrorEnum.TIMESHEET_APPROVED.getDeclaringClass()).isEqualTo(ValidationErrorEnum.class);
		assertThat(ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS.getDeclaringClass())
			.isEqualTo(ValidationErrorEnum.class);
		assertThat(ValidationErrorEnum.TIMESHEET_DIFFERENT_PERIOD.getDeclaringClass())
			.isEqualTo(ValidationErrorEnum.class);
		assertThat(ValidationErrorEnum.TIMESHEET_ANOTHER_JOB.getDeclaringClass()).isEqualTo(ValidationErrorEnum.class);
		assertThat(ValidationErrorEnum.NO_EDIT_ACCESS.getDeclaringClass()).isEqualTo(ValidationErrorEnum.class);
		assertThat(ValidationErrorEnum.NOT_APPROVED.getDeclaringClass()).isEqualTo(ValidationErrorEnum.class);
		assertThat(ValidationErrorEnum.DIFFERENT_COMPANY.getDeclaringClass()).isEqualTo(ValidationErrorEnum.class);
		assertThat(ValidationErrorEnum.DIFFERENT_CURRENCY.getDeclaringClass()).isEqualTo(ValidationErrorEnum.class);
		assertThat(ValidationErrorEnum.ALREADY_BILLED.getDeclaringClass()).isEqualTo(ValidationErrorEnum.class);
		assertThat(ValidationErrorEnum.ALREADY_COLLECTED.getDeclaringClass()).isEqualTo(ValidationErrorEnum.class);
		assertThat(ValidationErrorEnum.UNBILLED_AND_INVOICE_ATTACHED.getDeclaringClass())
			.isEqualTo(ValidationErrorEnum.class);
	}

	@Test
	@DisplayName("Should have proper enum constants order")
	void shouldHaveProperEnumConstantsOrder() {
		// When
		ValidationErrorEnum[] values = ValidationErrorEnum.values();

		// Then
		assertThat(values[0]).isEqualTo(ValidationErrorEnum.TIMESHEET_APPROVED);
		assertThat(values[1]).isEqualTo(ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS);
		assertThat(values[2]).isEqualTo(ValidationErrorEnum.TIMESHEET_DIFFERENT_PERIOD);
		assertThat(values[3]).isEqualTo(ValidationErrorEnum.TIMESHEET_ANOTHER_JOB);
		assertThat(values[4]).isEqualTo(ValidationErrorEnum.NO_EDIT_ACCESS);
		assertThat(values[5]).isEqualTo(ValidationErrorEnum.NOT_APPROVED);
		assertThat(values[6]).isEqualTo(ValidationErrorEnum.DIFFERENT_COMPANY);
		assertThat(values[7]).isEqualTo(ValidationErrorEnum.DIFFERENT_CURRENCY);
		assertThat(values[8]).isEqualTo(ValidationErrorEnum.ALREADY_BILLED);
		assertThat(values[9]).isEqualTo(ValidationErrorEnum.ALREADY_COLLECTED);
		assertThat(values[10]).isEqualTo(ValidationErrorEnum.UNBILLED_AND_INVOICE_ATTACHED);
	}

}
