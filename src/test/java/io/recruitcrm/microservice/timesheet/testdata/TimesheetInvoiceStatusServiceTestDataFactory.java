package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.BillStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.PaymentStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.PayBillTypeEnum;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetPayBillStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.services.timesheet_status.TimesheetInvoiceStatusService}
 * unit tests.
 */
public final class TimesheetInvoiceStatusServiceTestDataFactory {

	private TimesheetInvoiceStatusServiceTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getDefaultTimesheetId() {
		return 1;
	}

	public static Integer getDefaultAccountId() {
		return 123;
	}

	public static Integer getDefaultUserId() {
		return 456;
	}

	public static Integer getDefaultUserTypeId() {
		return AccountUserEnum.USERTYPEID.getId();
	}

	public static Timesheet createTimesheet(Integer timesheetId, Integer accountId) {
		Timesheet timesheet = new Timesheet();
		timesheet.setId(timesheetId);
		timesheet.setAccountId(accountId);
		return timesheet;
	}

	public static TimesheetInvoice createTimesheetInvoiceWithTimesheetId(Integer timesheetId) {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(timesheetId);
		return invoice;
	}

	public static TimesheetInvoice createTimesheetInvoiceForPayUpdate(Integer timesheetId) {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(timesheetId);
		invoice.setPaymentStatusId(PaymentStatusEnum.UN_PAID.getId());
		invoice.setBillingStatusId(BillStatusEnum.UN_BILLED.getId());
		return invoice;
	}

	public static UpdateTimesheetStatusRequestBodyDto createUpdateTimesheetStatusRequest(Integer approvalStatus,
			String remark) {
		UpdateTimesheetStatusRequestBodyDto dto = new UpdateTimesheetStatusRequestBodyDto();
		dto.setApprovalStatus(approvalStatus);
		dto.setRemark(remark);
		return dto;
	}

	public static UpdateTimesheetPayBillStatusRequestBodyDto createPayBillPayPaidRequest(Integer payoutPaidOn,
			String payoutNumber) {
		UpdateTimesheetPayBillStatusRequestBodyDto dto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		dto.setPayBillType(PayBillTypeEnum.PAY.getId());
		dto.setPayStatusId(PaymentStatusEnum.PAID.getId());
		dto.setPayoutPaidOn(payoutPaidOn);
		dto.setPayoutNumber(payoutNumber);
		return dto;
	}

	public static UpdateTimesheetPayBillStatusRequestBodyDto createPayBillPayUnpaidRequest() {
		UpdateTimesheetPayBillStatusRequestBodyDto dto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		dto.setPayBillType(PayBillTypeEnum.PAY.getId());
		dto.setPayStatusId(PaymentStatusEnum.UN_PAID.getId());
		return dto;
	}

	public static UpdateTimesheetPayBillStatusRequestBodyDto createPayBillBillUnpaidRequest() {
		UpdateTimesheetPayBillStatusRequestBodyDto dto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		dto.setPayBillType(PayBillTypeEnum.BILL.getId());
		dto.setPayStatusId(PaymentStatusEnum.UN_PAID.getId());
		return dto;
	}

}
