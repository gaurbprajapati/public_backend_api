package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.BillStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.PaymentStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;

/**
 * Test data factory for TimesheetInvoice-related test objects.
 */
public final class TimesheetInvoiceTestDataFactory {

	private TimesheetInvoiceTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// ===== Test IDs and Constants =====

	public static Integer getDefaultTimesheetId() {
		return 1;
	}

	public static Integer getDefaultUserId() {
		return 1;
	}

	public static Integer getDefaultAccountId() {
		return 1;
	}

	public static Integer getDefaultUserTypeId() {
		return AccountUserEnum.USERTYPEID.getId();
	}

	// ===== Entity Objects =====

	public static TimesheetInvoice createTimesheetInvoice() {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(getDefaultTimesheetId());
		invoice.setAccountId(getDefaultAccountId());
		invoice.setUpdatedBy(getDefaultUserId());
		invoice.setUpdatedOn(getCurrentUnixTimestamp());
		invoice.setUserTypeId(getDefaultUserTypeId());
		invoice.setPaymentStatusId(PaymentStatusEnum.UN_PAID.getId());
		invoice.setBillingStatusId(BillStatusEnum.UN_BILLED.getId());
		return invoice;
	}

	public static TimesheetInvoice createTimesheetInvoiceWithCustomValues(Integer timesheetId, Integer userId,
			Integer userTypeId) {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(timesheetId);
		invoice.setAccountId(getDefaultAccountId());
		invoice.setUpdatedBy(userId);
		invoice.setUpdatedOn(getCurrentUnixTimestamp());
		invoice.setUserTypeId(userTypeId);
		invoice.setPaymentStatusId(PaymentStatusEnum.UN_PAID.getId());
		invoice.setBillingStatusId(BillStatusEnum.UN_BILLED.getId());
		return invoice;
	}

	// ===== Helper Methods =====

	private static Integer getCurrentUnixTimestamp() {
		return Math.toIntExact(System.currentTimeMillis() / 1000);
	}

}
