package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.entity.model.Invoice;
import io.recruitcrm.microservice.timesheet.dto.invoice.BillDetailsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.BulkInvoiceValidationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.BulkInvoiceValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPayBillHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

/**
 * Test data factory for Invoice-related test objects.
 */
public final class InvoiceTestDataFactory {

	private InvoiceTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// ===== Request DTOs =====

	public static BulkInvoiceValidationRequestBodyDto createBulkInvoiceValidationRequest() {
		BulkInvoiceValidationRequestBodyDto request = new BulkInvoiceValidationRequestBodyDto();
		request.setTimesheetIds(Arrays.asList(1, 2, 3));
		return request;
	}

	// ===== Response DTOs =====

	public static TimesheetPayBillHistoryResponseBodyDto createTimesheetPayBillHistoryResponse() {
		TimesheetPayBillHistoryResponseBodyDto response = new TimesheetPayBillHistoryResponseBodyDto();
		response.setTimesheetId(getDefaultTimesheetId());
		return response;
	}

	public static BillDetailsResponseBodyDto createBillDetailsResponse() {
		BillDetailsResponseBodyDto response = new BillDetailsResponseBodyDto();
		response.setTimesheetId(getDefaultTimesheetId());
		return response;
	}

	public static BulkInvoiceValidationResponseBodyDto createBulkInvoiceValidationResponse() {
		return new BulkInvoiceValidationResponseBodyDto();
	}

	// ===== API Response Entities =====

	public static ResponseEntity<APINormalResponse<TimesheetPayBillHistoryResponseBodyDto>> createTimesheetPayBillHistorySuccessResponse(
			TimesheetPayBillHistoryResponseBodyDto data) {
		APINormalResponse<TimesheetPayBillHistoryResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APINormalResponse<BillDetailsResponseBodyDto>> createBillDetailsSuccessResponse(
			BillDetailsResponseBodyDto data) {
		APINormalResponse<BillDetailsResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<APINormalResponse<BulkInvoiceValidationResponseBodyDto>> createBulkInvoiceValidationSuccessResponse(
			BulkInvoiceValidationResponseBodyDto data) {
		APINormalResponse<BulkInvoiceValidationResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ===== Entity Objects =====

	public static TimesheetInvoice createTimesheetInvoiceWithBothStatuses() {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(getDefaultTimesheetId());
		invoice.setBillingStatusId(1);
		invoice.setPaymentStatusId(2);
		invoice.setPaymentPaidOn(1704153600);
		invoice.setManualInvoiceNumber("INV-001");
		invoice.setInvoiceId(1);
		return invoice;
	}

	public static TimesheetInvoice createTimesheetInvoiceWithBillingStatusOnly() {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(getDefaultTimesheetId());
		invoice.setBillingStatusId(1);
		invoice.setPaymentStatusId(null);
		invoice.setPaymentPaidOn(null);
		invoice.setManualInvoiceNumber("INV-001");
		invoice.setInvoiceId(1);
		return invoice;
	}

	public static TimesheetInvoice createTimesheetInvoiceWithPaymentStatusOnly() {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(getDefaultTimesheetId());
		invoice.setBillingStatusId(null);
		invoice.setPaymentStatusId(2);
		invoice.setPaymentPaidOn(1704153600);
		invoice.setManualInvoiceNumber(null);
		invoice.setInvoiceId(1);
		return invoice;
	}

	public static TimesheetInvoice createTimesheetInvoiceWithNoStatuses() {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(getDefaultTimesheetId());
		invoice.setBillingStatusId(null);
		invoice.setPaymentStatusId(null);
		invoice.setPaymentPaidOn(null);
		invoice.setManualInvoiceNumber(null);
		invoice.setInvoiceId(1);
		return invoice;
	}

	public static TimesheetInvoice createTimesheetInvoiceWithNullDates() {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(getDefaultTimesheetId());
		invoice.setBillingStatusId(1);
		invoice.setPaymentStatusId(2);
		invoice.setPaymentPaidOn(null);
		invoice.setManualInvoiceNumber("INV-001");
		invoice.setInvoiceId(1);
		return invoice;
	}

	public static Invoice createInvoice() {
		Invoice invoice = new Invoice();
		invoice.setId(1);
		invoice.setInvoiceIdNumber("INV-001");
		invoice.setCreatedOn(1704067200);
		return invoice;
	}

	// ===== Test IDs and Constants =====

	public static Integer getDefaultTimesheetId() {
		return 1;
	}

	public static Integer getDefaultUserId() {
		return 3181197;
	}

	public static String getDefaultUserName() {
		return "Vivek CST";
	}

	public static Integer getDefaultUserTypeId() {
		return 2;
	}

	public static final class Messages {

		public static final String TIMESHEET_PAY_BILL_HISTORY_FETCHED_SUCCESSFULLY = "Timesheet pay bill history fetched successfully";

		public static final String TIMESHEET_BILL_DETAILS_FETCHED_SUCCESSFULLY = "Timesheet bill details fetched successfully";

		public static final String TIMESHEET_VALIDATION_COMPLETED_SUCCESSFULLY = "Timesheet validation completed successfully";

	}

}