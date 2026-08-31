package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.BillStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.PaymentStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.microservice.timesheet.dto.invoice.InvoiceValidationQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public final class InvoiceRepositoryTestDataFactory {

	private InvoiceRepositoryTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// Default values
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

	public static Integer getDefaultCurrentUnixTimestamp() {
		return Math.toIntExact(Instant.now().getEpochSecond());
	}

	// Entities
	public static TimesheetInvoice createTimesheetInvoice() {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setId(1);
		invoice.setTimesheetId(getDefaultTimesheetId());
		invoice.setAccountId(getDefaultAccountId());
		invoice.setUpdatedBy(getDefaultUserId());
		invoice.setUpdatedOn(getDefaultCurrentUnixTimestamp());
		invoice.setUserTypeId(getDefaultUserTypeId());
		invoice.setPaymentStatusId(PaymentStatusEnum.UN_PAID.getId());
		invoice.setBillingStatusId(BillStatusEnum.UN_BILLED.getId());
		return invoice;
	}

	public static TimesheetInvoice createTimesheetInvoiceWithId(Integer id) {
		TimesheetInvoice invoice = createTimesheetInvoice();
		invoice.setId(id);
		return invoice;
	}

	public static TimesheetInvoice createTimesheetInvoiceWithTimesheetId(Integer timesheetId) {
		TimesheetInvoice invoice = createTimesheetInvoice();
		invoice.setTimesheetId(timesheetId);
		return invoice;
	}

	// DTOs
	public static InvoiceValidationQueryResultDto createInvoiceValidationQueryResultDto() {
		InvoiceValidationQueryResultDto dto = new InvoiceValidationQueryResultDto();
		dto.setTimesheetId(getDefaultTimesheetId());
		dto.setTimesheetApprovalStatusTypeId(1);
		dto.setCompanyName("Test Company");
		dto.setPeriodStart(1633046400);
		dto.setPeriodEnd(1635724800);
		dto.setCurrencyId(1);
		dto.setBillAmount(1000.0);
		dto.setCurrencySymbol("USD");
		dto.setContractorName("John Doe");
		dto.setContractorProfilePicUrl("profile.jpg");
		dto.setContractorSerialNumber(12345);
		dto.setCompanyId(1);
		dto.setJobId(1);
		dto.setContractorId(2);
		dto.setDealId(1);
		return dto;
	}

	public static InvoiceValidationQueryResultDto createInvoiceValidationQueryResultDtoWithTimesheetId(
			Integer timesheetId) {
		InvoiceValidationQueryResultDto dto = createInvoiceValidationQueryResultDto();
		dto.setTimesheetId(timesheetId);
		return dto;
	}

	// Lists
	public static List<TimesheetInvoice> createTimesheetInvoices() {
		return Arrays.asList(createTimesheetInvoiceWithId(1), createTimesheetInvoiceWithId(2),
				createTimesheetInvoiceWithId(3));
	}

	public static List<TimesheetInvoice> createTimesheetInvoicesWithTimesheetIds(List<Integer> timesheetIds) {
		return timesheetIds.stream()
			.map(InvoiceRepositoryTestDataFactory::createTimesheetInvoiceWithTimesheetId)
			.toList();
	}

	public static List<Integer> createTimesheetIds() {
		return Arrays.asList(1, 2, 3, 4, 5);
	}

	public static List<InvoiceValidationQueryResultDto> createInvoiceValidationQueryResultDtos() {
		return Arrays.asList(createInvoiceValidationQueryResultDtoWithTimesheetId(1),
				createInvoiceValidationQueryResultDtoWithTimesheetId(2),
				createInvoiceValidationQueryResultDtoWithTimesheetId(3));
	}

	// Edge cases
	public static TimesheetInvoice createNullTimesheetInvoice() {
		return null;
	}

	public static List<TimesheetInvoice> createEmptyTimesheetInvoices() {
		return Arrays.asList();
	}

	public static List<InvoiceValidationQueryResultDto> createEmptyInvoiceValidationQueryResultDtos() {
		return Arrays.asList();
	}

}
