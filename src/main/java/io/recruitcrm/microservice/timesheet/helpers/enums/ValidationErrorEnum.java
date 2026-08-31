package io.recruitcrm.microservice.timesheet.helpers.enums;

import lombok.Getter;

@Getter
public enum ValidationErrorEnum {

	TIMESHEET_APPROVED("approved"), TIMESHEET_DIFFERENT_SETTINGS("different_setting"),
	TIMESHEET_DIFFERENT_PERIOD("different_period"), TIMESHEET_ANOTHER_JOB("another_job"),
	NO_EDIT_ACCESS("no_edit_access"), NOT_APPROVED("not_approved"), DIFFERENT_COMPANY("different_company"),
	DIFFERENT_CURRENCY("different_currency"), ALREADY_BILLED("already_billed"), ALREADY_COLLECTED("already_collected"),
	UNBILLED_AND_INVOICE_ATTACHED("unbilled_and_invoice_attached");

	private final String message;

	ValidationErrorEnum(String message) {
		this.message = message;
	}

}
