package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetPayBillStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test data factory for TimesheetStatus-related test objects.
 */
public final class TimesheetStatusTestDataFactory {

	private TimesheetStatusTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// ===== Request DTOs =====

	public static UpdateTimesheetStatusRequestBodyDto createUpdateTimesheetStatusRequest() {
		UpdateTimesheetStatusRequestBodyDto request = new UpdateTimesheetStatusRequestBodyDto();
		request.setApprovalStatus(getDefaultApprovalStatus());
		request.setRemark(getDefaultRemark());
		return request;
	}

	public static UpdateTimesheetPayBillStatusRequestBodyDto createUpdateTimesheetPayBillStatusRequest() {
		UpdateTimesheetPayBillStatusRequestBodyDto request = new UpdateTimesheetPayBillStatusRequestBodyDto();
		request.setPayStatusId(getDefaultPaymentStatusId());
		request.setPayoutPaidOn(getDefaultPaymentDate());
		request.setPayoutNumber(getDefaultPayoutNumber());
		request.setBillStatusId(getDefaultBillingStatusId());
		request.setInvoiceCreatedOn(getDefaultBillingDate());
		request.setInvoiceNumber(getDefaultInvoiceNumber());
		return request;
	}

	// ===== API Response Entities =====

	public static ResponseEntity<APINormalResponse<Void>> createVoidSuccessResponse() {
		APINormalResponse<Void> response = new APINormalResponse<>(null);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ===== Test IDs and Constants =====

	public static Integer getDefaultTimesheetId() {
		return 1;
	}

	public static Integer getDefaultApprovalStatus() {
		return 1;
	}

	public static String getDefaultRemark() {
		return "Test remark";
	}

	public static Integer getDefaultPaymentStatusId() {
		return 1;
	}

	public static Integer getDefaultPaymentDate() {
		return 20240115;
	}

	public static String getDefaultPayoutNumber() {
		return "PAY-001";
	}

	public static Integer getDefaultBillingStatusId() {
		return 1;
	}

	public static Integer getDefaultBillingDate() {
		return 20240115;
	}

	public static String getDefaultInvoiceNumber() {
		return "INV-001";
	}

	public static final class Messages {

		public static final String TIMESHEET_STATUS_UPDATED_SUCCESSFULLY = "Timesheet status updated successfully";

		public static final String TIMESHEET_UPDATED_SUCCESSFULLY = "Timesheet updated successfully";

	}

}