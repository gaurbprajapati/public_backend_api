package io.recruitcrm.microservice.timesheet.services.invoice;

import io.recruitcrm.contract_staffing.entity.model.BillStatusEnum;
import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.PaymentStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.entity.model.Invoice;
import io.recruitcrm.microservice.timesheet.dao.invoice.InvoiceEntityJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.invoice.InvoicesJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.invoice.BillDetailsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.AssociationsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.BulkInvoiceValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.InvoiceValidationQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.TimesheetInvoicePreviewResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPeriodResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPayBillHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.helpers.enums.ValidationErrorEnum;
import io.recruitcrm.microservice.timesheet.repositories.invoice.TimesheetInvoiceRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TimesheetInvoiceService implements ITimesheetInvoiceService {

	final TimesheetJpaRepository timesheetJpaRepository;

	final TimesheetInvoiceRepository timesheetInvoiceRepository;

	final AuthHolder auth;

	final InvoiceEntityJpaRepository invoiceEntityJpaRepository;

	final InvoicesJpaRepository invoicesJpaRepository;

	public TimesheetInvoiceService(TimesheetJpaRepository timesheetJpaRepository,
			TimesheetInvoiceRepository timesheetInvoiceRepository, AuthHolder auth,
			InvoiceEntityJpaRepository invoiceEntityJpaRepository, InvoicesJpaRepository invoicesJpaRepository) {
		this.timesheetJpaRepository = timesheetJpaRepository;
		this.timesheetInvoiceRepository = timesheetInvoiceRepository;
		this.auth = auth;
		this.invoiceEntityJpaRepository = invoiceEntityJpaRepository;
		this.invoicesJpaRepository = invoicesJpaRepository;
	}

	@Override
	public TimesheetPayBillHistoryResponseBodyDto getTimesheetPayBillHistory(Integer timesheetId) {
		// will be developed in phase 2
		return null;
	}

	@Override
	public BillDetailsResponseBodyDto getBillDetailsByTimesheetId(Integer timesheetId) {
		// Get account ID for authorization
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		// Verify timesheet exists
		if (!this.timesheetJpaRepository.existsById(timesheetId)) {
			throw new ResourceNotFoundException("Timesheet", timesheetId);
		}

		// Get invoice data with account ID check
		TimesheetInvoice timesheetInvoice = this.timesheetInvoiceRepository.findBillDetailsByTimesheetId(timesheetId,
				accountId);

		if (timesheetInvoice == null) {
			throw new ResourceNotFoundException("Invoice for timesheet", timesheetId);
		}

		Invoice invoice = this.invoicesJpaRepository.findById(timesheetInvoice.getInvoiceId())
			.orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

		// Create response DTO
		BillDetailsResponseBodyDto responseDto = new BillDetailsResponseBodyDto();
		responseDto.setTimesheetId(timesheetId);
		responseDto.setBillStatusId(timesheetInvoice.getBillingStatusId());
		responseDto.setInvoiceNumber(invoice.getInvoiceIdNumber());
		responseDto.setInvoiceCreatedOn(invoice.getCreatedOn());
		responseDto.setInvoiceFile(null); // Field not available in entity
		responseDto.setRemark(null);

		return responseDto;
	}

	@Override
	@WriterRoute
	public Invoice createInvoice(Invoice invoice) {
		return this.invoiceEntityJpaRepository.save(invoice);
	}

	@Override
	public BulkInvoiceValidationResponseBodyDto validateTimesheetsForInvoice(List<Integer> timesheetIds) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		Set<Integer> timesheetIdsInvoiceCreated = this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds,
				accountId);

		// Fetch timesheet validation data
		List<InvoiceValidationQueryResultDto> validationResults = this.timesheetInvoiceRepository
			.validateTimesheetsForInvoice(timesheetIds, accountId);

		// Fetch TimesheetInvoice records to check bill status
		// This can be optimized, we can save a db call here
		List<TimesheetInvoice> timesheetInvoices = this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds,
				accountId);
		Map<Integer, TimesheetInvoice> invoiceMap = timesheetInvoices.stream()
			.collect(Collectors.toMap(TimesheetInvoice::getTimesheetId, (invoice) -> invoice));

		// Get primary timesheet (first in the list)
		Integer primaryTimesheetId = timesheetIds.getFirst();
		InvoiceValidationQueryResultDto primaryTimesheet = validationResults.stream()
			.filter((result) -> result.getTimesheetId().equals(primaryTimesheetId))
			.findFirst()
			.orElse(null);

		// Group validation results by timesheet ID to handle multiple deals per job
		Map<Integer, List<InvoiceValidationQueryResultDto>> groupedResults = validationResults.stream()
			.collect(Collectors.groupingBy(InvoiceValidationQueryResultDto::getTimesheetId));

		List<TimesheetInvoicePreviewResponseBodyDto> timesheetInvoicePreviewData = new ArrayList<>();

		Integer errorCount = 0;
		// Process each timesheet group and create preview data
		for (Map.Entry<Integer, List<InvoiceValidationQueryResultDto>> entry : groupedResults.entrySet()) {
			List<InvoiceValidationQueryResultDto> timesheetResults = entry.getValue();
			InvoiceValidationQueryResultDto firstResult = timesheetResults.get(0);

			TimesheetInvoicePreviewResponseBodyDto previewData = new TimesheetInvoicePreviewResponseBodyDto();

			// Set basic timesheet data
			previewData.setTimesheetId(firstResult.getTimesheetId());
			previewData.setTimesheetPeriod(
					new TimesheetPeriodResponseBodyDto(firstResult.getPeriodStart(), firstResult.getPeriodEnd()));
			previewData.setCurrencyId(firstResult.getCurrencyId());
			previewData.setBillCurrencyCode(firstResult.getCurrencyCode());
			previewData.setBillAmount(firstResult.getBillAmount());
			previewData.setBillCurrencySymbol(firstResult.getCurrencySymbol());
			previewData.setContractorName(firstResult.getContractorName());
			previewData.setContractorProfilePicUrl(firstResult.getContractorProfilePicUrl());
			previewData.setTimesheetApprovalStatusTypeId(firstResult.getTimesheetApprovalStatusTypeId());
			previewData.setContractorSerialNumber(firstResult.getContractorSerialNumber());
			previewData.setContractorOwnerId(firstResult.getContractorOwnerId());
			previewData.setContractorSlug(firstResult.getContractorSlug());
			previewData.setJobSlug(firstResult.getJobSlug());
			previewData.setContractorJobAssignmentId(firstResult.getContractorJobAssignmentId());
			previewData.setJobId(firstResult.getJobId());
			previewData.setContractorId(firstResult.getContractorId());
			previewData.setPayCurrencyCode(firstResult.getPayCurrencyCode());
			previewData.setPayCurrencySymbol(firstResult.getPayCurrencySymbol());
			previewData.setIsReimbursementEnabled(firstResult.getIsReimbursementEnabled());

			// Collect all deal IDs for this timesheet
			List<Integer> dealIds = timesheetResults.stream()
				.map(InvoiceValidationQueryResultDto::getDealId)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

			// Create associations object with aggregated deal IDs
			AssociationsResponseBodyDto associations = new AssociationsResponseBodyDto(firstResult.getJobContactId(),
					firstResult.getCompanyId(), firstResult.getJobId(), firstResult.getContractorId(), dealIds);
			previewData.setAssociations((associations));

			// Get TimesheetInvoice for bill status validation
			TimesheetInvoice timesheetInvoice = invoiceMap.get(firstResult.getTimesheetId());

			String errorStatus = getErrorStatus(firstResult, primaryTimesheet, timesheetInvoice,
					timesheetIdsInvoiceCreated);
			if (!errorStatus.isEmpty()) {
				errorCount++;
			}
			previewData.setErrorKey(errorStatus);
			timesheetInvoicePreviewData.add(previewData);
		}

		return new BulkInvoiceValidationResponseBodyDto(timesheetInvoicePreviewData, errorCount);
	}

	@NotNull
	private static String getErrorStatus(InvoiceValidationQueryResultDto result,
			InvoiceValidationQueryResultDto primaryTimesheet, TimesheetInvoice timesheetInvoice,
			Set<Integer> timesheetIdsInvoiceCreated) {
		String errorStatus = "";

		// Priority 1: Check company validation (only if timesheet is approved and no
		// previous errors)
		if (primaryTimesheet != null && !Objects.equals(result.getCompanyName(), primaryTimesheet.getCompanyName())) {
			return ValidationErrorEnum.DIFFERENT_COMPANY.getMessage();
		}

		// Priority 2: Check approval status (4 = APPROVED) - only if no bill status error
		if ((result.getTimesheetApprovalStatusTypeId() == null
				|| !Objects.equals(result.getTimesheetApprovalStatusTypeId(), 4))) {
			return ValidationErrorEnum.NOT_APPROVED.getMessage();
		}

		// Priority 3: Check bill status validation (highest priority)
		if (timesheetInvoice != null) {
			if (timesheetInvoice.getBillingStatusId() != null
					&& timesheetInvoice.getBillingStatusId().equals(BillStatusEnum.BILLED.getId())) {
				return ValidationErrorEnum.ALREADY_BILLED.getMessage();
			}
			else if (timesheetInvoice.getBillingStatusId() != null
					&& timesheetInvoice.getBillingStatusId().equals(BillStatusEnum.COLLECTED.getId())) {
				return ValidationErrorEnum.ALREADY_COLLECTED.getMessage();
			}
		}

		// Priority 4: Check if an invoice is created for the timesheet
		if (timesheetInvoice != null && timesheetIdsInvoiceCreated.contains(timesheetInvoice.getTimesheetId())) {
			return ValidationErrorEnum.UNBILLED_AND_INVOICE_ATTACHED.getMessage();
		}

		// Priority 5: Check bill currency validation (only if timesheet is approved, same
		// company, and no previous errors)
		if (primaryTimesheet != null && result.getCurrencyId() != null
				&& !Objects.equals(result.getCurrencyId(), primaryTimesheet.getCurrencyId())) {
			return ValidationErrorEnum.DIFFERENT_CURRENCY.getMessage();
		}
		return errorStatus;
	}

	@Override
	public void createTimesheetInvoice(Integer timesheetId, Integer userId, Integer userTypeId) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer currentUNIXTimestamp = Math.toIntExact(Instant.now().getEpochSecond());

		// Check if invoice already exists for this timesheet
		TimesheetInvoice existingInvoice = this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId);

		TimesheetInvoice invoice;
		if (existingInvoice != null) {
			// Update existing invoice
			invoice = existingInvoice;
			invoice.setUpdatedBy(userId);
			invoice.setUpdatedOn(currentUNIXTimestamp);
			invoice.setUserTypeId(userTypeId);
			// Only set statuses if they are null to preserve existing status values
			if (invoice.getPaymentStatusId() == null) {
				invoice.setPaymentStatusId(PaymentStatusEnum.UN_PAID.getId());
			}
			if (invoice.getBillingStatusId() == null) {
				invoice.setBillingStatusId(BillStatusEnum.UN_BILLED.getId());
			}
		}
		else {
			// Create new timesheet invoice
			invoice = new TimesheetInvoice();
			invoice.setTimesheetId(timesheetId);
			invoice.setAccountId(accountId);
			invoice.setUpdatedBy(userId);
			invoice.setUpdatedOn(currentUNIXTimestamp);
			invoice.setUserTypeId(userTypeId);
			invoice.setPaymentStatusId(PaymentStatusEnum.UN_PAID.getId());
			invoice.setBillingStatusId(BillStatusEnum.UN_BILLED.getId());
		}

		// Save or update the invoice
		this.timesheetInvoiceRepository.saveInvoice(invoice);
	}

}
