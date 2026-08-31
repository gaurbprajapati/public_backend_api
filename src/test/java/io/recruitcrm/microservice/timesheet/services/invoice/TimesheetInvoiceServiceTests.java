package io.recruitcrm.microservice.timesheet.services.invoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.contract_staffing.entity.model.BillStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.PaymentStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.entity.model.Invoice;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dao.invoice.InvoiceEntityJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.invoice.InvoicesJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.invoice.BillDetailsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.BulkInvoiceValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.InvoiceValidationQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.invoice.TimesheetInvoicePreviewResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPeriodResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.helpers.enums.ValidationErrorEnum;
import io.recruitcrm.microservice.timesheet.repositories.invoice.TimesheetInvoiceRepository;
import io.recruitcrm.microservice.timesheet.testdata.InvoiceTestDataFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TimesheetInvoiceService Tests")
class TimesheetInvoiceServiceTests {

	@InjectMocks
	private TimesheetInvoiceService timesheetInvoiceService;

	@Mock
	private TimesheetJpaRepository timesheetJpaRepository;

	@Mock
	private TimesheetInvoiceRepository timesheetInvoiceRepository;

	@Mock
	private InvoiceEntityJpaRepository invoiceEntityJpaRepository;

	@Mock
	private InvoicesJpaRepository invoicesJpaRepository;

	@Mock
	private AuthHolder auth;

	@BeforeEach
	void setUp() {
		// Given - Common setup for invoice service tests
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(1);
	}

	@Test
	@DisplayName("Get timesheet pay bill history should return null")
	void testGetTimesheetPayBillHistoryReturnsNull() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();

		// When
		var result = this.timesheetInvoiceService.getTimesheetPayBillHistory(timesheetId);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Get bill details should return success response when invoice exists")
	void testGetBillDetailsByTimesheetIdValidTimesheetReturnsSuccess() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 1;
		TimesheetInvoice timesheetInvoice = InvoiceTestDataFactory.createTimesheetInvoiceWithBothStatuses();
		Invoice invoice = InvoiceTestDataFactory.createInvoice();

		given(this.timesheetJpaRepository.existsById(timesheetId)).willReturn(true);
		given(this.timesheetInvoiceRepository.findBillDetailsByTimesheetId(timesheetId, accountId))
			.willReturn(timesheetInvoice);
		given(this.invoicesJpaRepository.findById(timesheetInvoice.getInvoiceId())).willReturn(Optional.of(invoice));

		// When
		BillDetailsResponseBodyDto result = this.timesheetInvoiceService.getBillDetailsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getBillStatusId()).isEqualTo(timesheetInvoice.getBillingStatusId());
		assertThat(result.getInvoiceNumber()).isEqualTo(invoice.getInvoiceIdNumber());
		assertThat(result.getInvoiceCreatedOn()).isEqualTo(invoice.getCreatedOn());
		assertThat(result.getInvoiceFile()).isNull();
		assertThat(result.getRemark()).isNull();

		then(this.timesheetJpaRepository).should().existsById(timesheetId);
		then(this.timesheetInvoiceRepository).should().findBillDetailsByTimesheetId(timesheetId, accountId);
		then(this.invoicesJpaRepository).should().findById(timesheetInvoice.getInvoiceId());
	}

	@Test
	@DisplayName("Get bill details should throw ResourceNotFoundException when timesheet not found")
	void testGetBillDetailsByTimesheetIdTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();

		given(this.timesheetJpaRepository.existsById(timesheetId)).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceService.getBillDetailsByTimesheetId(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet id " + timesheetId + " not found.");

		then(this.timesheetJpaRepository).should().existsById(timesheetId);
	}

	@Test
	@DisplayName("Get bill details should throw ResourceNotFoundException when timesheet invoice not found")
	void testGetBillDetailsByTimesheetIdTimesheetInvoiceNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 1;

		given(this.timesheetJpaRepository.existsById(timesheetId)).willReturn(true);
		given(this.timesheetInvoiceRepository.findBillDetailsByTimesheetId(timesheetId, accountId)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceService.getBillDetailsByTimesheetId(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Invoice for timesheet id " + timesheetId + " not found.");

		then(this.timesheetJpaRepository).should().existsById(timesheetId);
		then(this.timesheetInvoiceRepository).should().findBillDetailsByTimesheetId(timesheetId, accountId);
	}

	@Test
	@DisplayName("Get bill details should throw ResourceNotFoundException when invoice entity not found")
	void testGetBillDetailsByTimesheetIdInvoiceEntityNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 1;
		TimesheetInvoice timesheetInvoice = InvoiceTestDataFactory.createTimesheetInvoiceWithBothStatuses();

		given(this.timesheetJpaRepository.existsById(timesheetId)).willReturn(true);
		given(this.timesheetInvoiceRepository.findBillDetailsByTimesheetId(timesheetId, accountId))
			.willReturn(timesheetInvoice);
		given(this.invoicesJpaRepository.findById(timesheetInvoice.getInvoiceId())).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceService.getBillDetailsByTimesheetId(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Invoice not found");

		then(this.timesheetJpaRepository).should().existsById(timesheetId);
		then(this.timesheetInvoiceRepository).should().findBillDetailsByTimesheetId(timesheetId, accountId);
		then(this.invoicesJpaRepository).should().findById(timesheetInvoice.getInvoiceId());
	}

	@Test
	@DisplayName("Create invoice should return saved invoice successfully")
	void testCreateInvoiceValidInvoiceReturnsSavedInvoice() {
		// Given
		Invoice invoice = InvoiceTestDataFactory.createInvoice();
		Invoice savedInvoice = InvoiceTestDataFactory.createInvoice();
		savedInvoice.setId(1);

		given(this.invoiceEntityJpaRepository.save(invoice)).willReturn(savedInvoice);

		// When
		Invoice result = this.timesheetInvoiceService.createInvoice(invoice);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1);
		then(this.invoiceEntityJpaRepository).should().save(invoice);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should return success with no errors for approved timesheets")
	void testValidateTimesheetsForInvoiceApprovedTimesheetsReturnsSuccess() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1, 2, 3);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createApprovedInvoiceValidationResults();
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors();
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(3);
		assertThat(result.getErrorCount()).isZero();

		TimesheetInvoicePreviewResponseBodyDto firstPreview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(firstPreview.getTimesheetId()).isEqualTo(1);
		assertThat(firstPreview.getErrorKey()).isEmpty();

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should return error for unbilled with invoice attached")
	void testValidateTimesheetsForInvoiceUnbilledWithInvoiceAttachedReturnsError() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createApprovedInvoiceValidationResults()
			.subList(0, 1);
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors().subList(0, 1);
		Set<Integer> invoiceCreatedSet = new HashSet<>(Arrays.asList(1));

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(invoiceCreatedSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(result.getErrorCount()).isOne();

		TimesheetInvoicePreviewResponseBodyDto preview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(preview.getErrorKey()).isEqualTo(ValidationErrorEnum.UNBILLED_AND_INVOICE_ATTACHED.getMessage());

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should allow approved timesheet when timesheet invoice is missing")
	void testValidateTimesheetsForInvoiceMissingTimesheetInvoiceReturnsNoError() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createApprovedInvoiceValidationResults()
			.subList(0, 1);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(Collections.emptyList());

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(result.getErrorCount()).isZero();
		assertThat(result.getTimesheetInvoicePreviewData().get(0).getErrorKey()).isEmpty();

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should return error for already billed timesheet")
	void testValidateTimesheetsForInvoiceAlreadyBilledReturnsError() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createApprovedInvoiceValidationResults()
			.subList(0, 1);
		List<TimesheetInvoice> timesheetInvoices = Arrays
			.asList(this.createTimesheetInvoiceWithBillingStatus(1, BillStatusEnum.BILLED.getId()));
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(result.getErrorCount()).isOne();

		TimesheetInvoicePreviewResponseBodyDto preview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(preview.getErrorKey()).isEqualTo(ValidationErrorEnum.ALREADY_BILLED.getMessage());

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should return error for already collected timesheet")
	void testValidateTimesheetsForInvoiceAlreadyCollectedReturnsError() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createApprovedInvoiceValidationResults()
			.subList(0, 1);
		List<TimesheetInvoice> timesheetInvoices = Arrays
			.asList(this.createTimesheetInvoiceWithBillingStatus(1, BillStatusEnum.COLLECTED.getId()));
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(result.getErrorCount()).isOne();

		TimesheetInvoicePreviewResponseBodyDto preview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(preview.getErrorKey()).isEqualTo(ValidationErrorEnum.ALREADY_COLLECTED.getMessage());

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should return error for unapproved timesheet")
	void testValidateTimesheetsForInvoiceUnapprovedTimesheetReturnsError() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createUnapprovedInvoiceValidationResults();
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors().subList(0, 1);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(result.getErrorCount()).isOne();

		TimesheetInvoicePreviewResponseBodyDto preview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(preview.getErrorKey()).isEqualTo(ValidationErrorEnum.NOT_APPROVED.getMessage());

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should return error for null approval status")
	void testValidateTimesheetsForInvoiceNullApprovalStatusReturnsError() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createNullApprovalStatusValidationResults();
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors().subList(0, 1);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(result.getErrorCount()).isOne();

		TimesheetInvoicePreviewResponseBodyDto preview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(preview.getErrorKey()).isEqualTo(ValidationErrorEnum.NOT_APPROVED.getMessage());

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should return error for different company")
	void testValidateTimesheetsForInvoiceDifferentCompanyReturnsError() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1, 2);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createDifferentCompanyValidationResults();
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors().subList(0, 2);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(2);
		assertThat(result.getErrorCount()).isOne();

		TimesheetInvoicePreviewResponseBodyDto firstPreview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(firstPreview.getErrorKey()).isEmpty();

		TimesheetInvoicePreviewResponseBodyDto secondPreview = result.getTimesheetInvoicePreviewData().get(1);
		assertThat(secondPreview.getErrorKey()).isEqualTo(ValidationErrorEnum.DIFFERENT_COMPANY.getMessage());

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should return error for different currency")
	void testValidateTimesheetsForInvoiceDifferentCurrencyReturnsError() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1, 2);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createDifferentCurrencyValidationResults();
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors().subList(0, 2);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(2);
		assertThat(result.getErrorCount()).isOne();

		TimesheetInvoicePreviewResponseBodyDto firstPreview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(firstPreview.getErrorKey()).isEmpty();

		TimesheetInvoicePreviewResponseBodyDto secondPreview = result.getTimesheetInvoicePreviewData().get(1);
		assertThat(secondPreview.getErrorKey()).isEqualTo(ValidationErrorEnum.DIFFERENT_CURRENCY.getMessage());

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should handle multiple deals per timesheet")
	void testValidateTimesheetsForInvoiceMultipleDealsHandlesCorrectly() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createMultipleDealValidationResults();
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors().subList(0, 1);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(result.getErrorCount()).isZero();

		TimesheetInvoicePreviewResponseBodyDto preview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(preview.getAssociations().getAssociations().get(11)).hasSize(2);
		assertThat(preview.getAssociations().getAssociations().get(11)).containsExactlyInAnyOrder(1, 2);

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should handle null deal IDs")
	void testValidateTimesheetsForInvoiceNullDealIdsHandlesCorrectly() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createNullDealValidationResults();
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors().subList(0, 1);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(result.getErrorCount()).isZero();

		TimesheetInvoicePreviewResponseBodyDto preview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(preview.getAssociations().getAssociations().get(11)).isEmpty();

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should handle null billing status")
	void testValidateTimesheetsForInvoiceNullBillingStatusHandlesCorrectly() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createApprovedInvoiceValidationResults()
			.subList(0, 1);
		List<TimesheetInvoice> timesheetInvoices = Arrays.asList(this.createTimesheetInvoiceWithNullBillingStatus(1));
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(result.getErrorCount()).isZero();

		TimesheetInvoicePreviewResponseBodyDto preview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(preview.getErrorKey()).isEmpty();

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should handle non-equal approval status not matching 4")
	void testValidateTimesheetsForInvoiceNonEqualApprovalStatusHandlesCorrectly() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;
		InvoiceValidationQueryResultDto result = this.createApprovedInvoiceValidationResults().get(0);
		result.setTimesheetApprovalStatusTypeId(3); // Not equal to 4
		List<InvoiceValidationQueryResultDto> validationResults = Collections.singletonList(result);
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors().subList(0, 1);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto response = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(response.getErrorCount()).isOne();

		TimesheetInvoicePreviewResponseBodyDto preview = response.getTimesheetInvoicePreviewData().get(0);
		assertThat(preview.getErrorKey()).isEqualTo(ValidationErrorEnum.NOT_APPROVED.getMessage());

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Create timesheet invoice should create new invoice when invoice does not exist")
	void testCreateTimesheetInvoiceInvoiceDoesNotExistCreatesNewInvoice() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 1;
		Integer userId = InvoiceTestDataFactory.getDefaultUserId();
		Integer userTypeId = 1;
		TimesheetInvoice[] savedInvoice = new TimesheetInvoice[1];

		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId)).willReturn(null);
		given(this.timesheetInvoiceRepository.saveInvoice(any(TimesheetInvoice.class))).willAnswer((invocation) -> {
			savedInvoice[0] = invocation.getArgument(0);
			return savedInvoice[0];
		});

		// When
		this.timesheetInvoiceService.createTimesheetInvoice(timesheetId, userId, userTypeId);

		// Then
		assertThat(savedInvoice[0]).isNotNull();
		assertThat(savedInvoice[0].getTimesheetId()).isEqualTo(timesheetId);
		assertThat(savedInvoice[0].getAccountId()).isEqualTo(accountId);
		assertThat(savedInvoice[0].getUpdatedBy()).isEqualTo(userId);
		assertThat(savedInvoice[0].getUserTypeId()).isEqualTo(userTypeId);
		assertThat(savedInvoice[0].getPaymentStatusId()).isEqualTo(PaymentStatusEnum.UN_PAID.getId());
		assertThat(savedInvoice[0].getBillingStatusId()).isEqualTo(BillStatusEnum.UN_BILLED.getId());
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, accountId);
		then(this.timesheetInvoiceRepository).should().saveInvoice(any(TimesheetInvoice.class));
	}

	@Test
	@DisplayName("Create timesheet invoice should update existing invoice when invoice exists")
	void testCreateTimesheetInvoiceInvoiceExistsUpdatesExistingInvoice() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 1;
		Integer userId = InvoiceTestDataFactory.getDefaultUserId();
		Integer userTypeId = 1;
		TimesheetInvoice existingInvoice = InvoiceTestDataFactory.createTimesheetInvoiceWithNoStatuses();
		existingInvoice.setId(1);
		existingInvoice.setTimesheetId(timesheetId);
		existingInvoice.setAccountId(accountId);
		existingInvoice.setUpdatedBy(999);
		existingInvoice.setUpdatedOn(1000);

		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId)).willReturn(existingInvoice);
		given(this.timesheetInvoiceRepository.saveInvoice(any(TimesheetInvoice.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		this.timesheetInvoiceService.createTimesheetInvoice(timesheetId, userId, userTypeId);

		// Then
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, accountId);
		then(this.timesheetInvoiceRepository).should().saveInvoice(any(TimesheetInvoice.class));
	}

	@Test
	@DisplayName("Create timesheet invoice should set default statuses when existing invoice has null statuses")
	void testCreateTimesheetInvoiceExistingInvoiceWithNullStatusesSetsDefaultStatuses() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 1;
		Integer userId = InvoiceTestDataFactory.getDefaultUserId();
		Integer userTypeId = 1;
		TimesheetInvoice existingInvoice = InvoiceTestDataFactory.createTimesheetInvoiceWithNoStatuses();
		existingInvoice.setId(1);
		existingInvoice.setTimesheetId(timesheetId);
		existingInvoice.setAccountId(accountId);
		existingInvoice.setPaymentStatusId(null);
		existingInvoice.setBillingStatusId(null);

		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId)).willReturn(existingInvoice);
		given(this.timesheetInvoiceRepository.saveInvoice(any(TimesheetInvoice.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		this.timesheetInvoiceService.createTimesheetInvoice(timesheetId, userId, userTypeId);

		// Then
		assertThat(existingInvoice.getPaymentStatusId()).isEqualTo(PaymentStatusEnum.UN_PAID.getId());
		assertThat(existingInvoice.getBillingStatusId()).isEqualTo(BillStatusEnum.UN_BILLED.getId());
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, accountId);
		then(this.timesheetInvoiceRepository).should().saveInvoice(any(TimesheetInvoice.class));
	}

	@Test
	@DisplayName("Create timesheet invoice should preserve existing statuses when updating existing invoice")
	void testCreateTimesheetInvoiceExistingInvoicePreservesExistingStatuses() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 1;
		Integer userId = InvoiceTestDataFactory.getDefaultUserId();
		Integer userTypeId = 1;
		TimesheetInvoice existingInvoice = InvoiceTestDataFactory.createTimesheetInvoiceWithBothStatuses();
		existingInvoice.setId(1);
		existingInvoice.setTimesheetId(timesheetId);
		existingInvoice.setAccountId(accountId);
		Integer existingPaymentStatusId = existingInvoice.getPaymentStatusId();
		Integer existingBillingStatusId = existingInvoice.getBillingStatusId();

		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId)).willReturn(existingInvoice);
		given(this.timesheetInvoiceRepository.saveInvoice(any(TimesheetInvoice.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		this.timesheetInvoiceService.createTimesheetInvoice(timesheetId, userId, userTypeId);

		// Then
		assertThat(existingInvoice.getPaymentStatusId()).isEqualTo(existingPaymentStatusId);
		assertThat(existingInvoice.getBillingStatusId()).isEqualTo(existingBillingStatusId);
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, accountId);
		then(this.timesheetInvoiceRepository).should().saveInvoice(any(TimesheetInvoice.class));
	}

	@Test
	@DisplayName("Create timesheet invoice should update user fields when updating existing invoice")
	void testCreateTimesheetInvoiceExistingInvoiceUpdatesUserFields() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 1;
		Integer userId = InvoiceTestDataFactory.getDefaultUserId();
		Integer userTypeId = 1;
		TimesheetInvoice existingInvoice = InvoiceTestDataFactory.createTimesheetInvoiceWithNoStatuses();
		existingInvoice.setId(1);
		existingInvoice.setTimesheetId(timesheetId);
		existingInvoice.setAccountId(accountId);
		existingInvoice.setUpdatedBy(999);
		existingInvoice.setUpdatedOn(1000);
		existingInvoice.setUserTypeId(999);

		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId)).willReturn(existingInvoice);
		given(this.timesheetInvoiceRepository.saveInvoice(any(TimesheetInvoice.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		this.timesheetInvoiceService.createTimesheetInvoice(timesheetId, userId, userTypeId);

		// Then
		assertThat(existingInvoice.getUpdatedBy()).isEqualTo(userId);
		assertThat(existingInvoice.getUserTypeId()).isEqualTo(userTypeId);
		assertThat(existingInvoice.getUpdatedOn()).isNotNull();
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, accountId);
		then(this.timesheetInvoiceRepository).should().saveInvoice(any(TimesheetInvoice.class));
	}

	@Test
	@DisplayName("Validate timesheets for invoice should throw when empty timesheet list provided")
	void testValidateTimesheetsForInvoiceEmptyTimesheetListThrowsNoSuchElement() {
		// Given
		List<Integer> timesheetIds = Collections.emptyList();
		Integer accountId = 1;
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(Collections.emptyList());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(Collections.emptyList());

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceService.validateTimesheetsForInvoice(timesheetIds))
			.isInstanceOf(NoSuchElementException.class);

		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should throw when null timesheet list provided")
	void testValidateTimesheetsForInvoiceNullTimesheetListThrowsNullPointerException() {
		// Given
		List<Integer> timesheetIds = null;
		Integer accountId = 1;
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(Collections.emptyList());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(Collections.emptyList());

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceService.validateTimesheetsForInvoice(timesheetIds))
			.isInstanceOf(NullPointerException.class);

		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should succeed when no TimesheetInvoice row exists for approved timesheet")
	void testValidateTimesheetsForInvoiceNoTimesheetInvoiceRecordReturnsNoError() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createApprovedInvoiceValidationResults()
			.subList(0, 1);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(Collections.emptyList());

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(result.getErrorCount()).isZero();
		assertThat(result.getTimesheetInvoicePreviewData().get(0).getErrorKey()).isEmpty();

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should handle mixed valid and invalid timesheets")
	void testValidateTimesheetsForInvoiceMixedValidAndInvalidTimesheetsReturnsPartialErrors() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1, 2);
		Integer accountId = 1;

		// First timesheet is approved, second is not approved
		InvoiceValidationQueryResultDto approvedResult = new InvoiceValidationQueryResultDto();
		approvedResult.setTimesheetId(1);
		approvedResult.setTimesheetApprovalStatusTypeId(4); // APPROVED
		approvedResult.setCompanyName("Test Company");
		approvedResult.setCompanyId(1);
		approvedResult.setPeriodStart(1704067200);
		approvedResult.setPeriodEnd(1704153600);
		approvedResult.setCurrencyId(1);
		approvedResult.setCurrencyCode("USD");
		approvedResult.setCurrencySymbol("$");
		approvedResult.setBillAmount(100.0);
		approvedResult.setContractorName("Test Contractor");
		approvedResult.setContractorProfilePicUrl("http://example.com/photo.jpg");
		approvedResult.setContractorSerialNumber(123);
		approvedResult.setContractorOwnerId(1);
		approvedResult.setContractorSlug("test-contractor");
		approvedResult.setJobSlug("test-job");
		approvedResult.setJobId(1);
		approvedResult.setContractorId(1);
		approvedResult.setContractorJobAssignmentId(1);
		approvedResult.setDealId(1);

		InvoiceValidationQueryResultDto unapprovedResult = new InvoiceValidationQueryResultDto();
		unapprovedResult.setTimesheetId(2);
		unapprovedResult.setTimesheetApprovalStatusTypeId(1); // SUBMITTED - not approved
		unapprovedResult.setCompanyName("Test Company");
		unapprovedResult.setCompanyId(1);
		unapprovedResult.setPeriodStart(1704067200);
		unapprovedResult.setPeriodEnd(1704153600);
		unapprovedResult.setCurrencyId(1);
		unapprovedResult.setCurrencyCode("USD");
		unapprovedResult.setCurrencySymbol("$");
		unapprovedResult.setBillAmount(200.0);
		unapprovedResult.setContractorName("Test Contractor 2");
		unapprovedResult.setContractorProfilePicUrl("http://example.com/photo2.jpg");
		unapprovedResult.setContractorSerialNumber(124);
		unapprovedResult.setContractorOwnerId(1);
		unapprovedResult.setContractorSlug("test-contractor-2");
		unapprovedResult.setJobSlug("test-job");
		unapprovedResult.setJobId(1);
		unapprovedResult.setContractorId(2);
		unapprovedResult.setContractorJobAssignmentId(2);
		unapprovedResult.setDealId(1);

		List<InvoiceValidationQueryResultDto> validationResults = Arrays.asList(approvedResult, unapprovedResult);
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors().subList(0, 2);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(2);
		assertThat(result.getErrorCount()).isOne();

		// Verify first timesheet has no error
		TimesheetInvoicePreviewResponseBodyDto firstPreview = result.getTimesheetInvoicePreviewData()
			.stream()
			.filter((preview) -> preview.getTimesheetId().equals(1))
			.findFirst()
			.orElse(null);
		assertThat(firstPreview).isNotNull();
		assertThat(firstPreview.getErrorKey()).isEmpty();

		// Verify second timesheet has NOT_APPROVED error
		TimesheetInvoicePreviewResponseBodyDto secondPreview = result.getTimesheetInvoicePreviewData()
			.stream()
			.filter((preview) -> preview.getTimesheetId().equals(2))
			.findFirst()
			.orElse(null);
		assertThat(secondPreview).isNotNull();
		assertThat(secondPreview.getErrorKey()).isEqualTo(ValidationErrorEnum.NOT_APPROVED.getMessage());

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should prioritize different company error over not approved")
	void testValidateTimesheetsForInvoiceDifferentCompanyPriorityOverNotApproved() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1, 2);
		Integer accountId = 1;

		InvoiceValidationQueryResultDto approvedResult = new InvoiceValidationQueryResultDto();
		approvedResult.setTimesheetId(1);
		approvedResult.setTimesheetApprovalStatusTypeId(4); // APPROVED
		approvedResult.setCompanyName("Test Company");
		approvedResult.setCompanyId(1);
		approvedResult.setPeriodStart(1704067200);
		approvedResult.setPeriodEnd(1704153600);
		approvedResult.setCurrencyId(1);
		approvedResult.setCurrencyCode("USD");
		approvedResult.setCurrencySymbol("$");
		approvedResult.setBillAmount(100.0);
		approvedResult.setContractorName("Test Contractor");
		approvedResult.setContractorProfilePicUrl("http://example.com/photo.jpg");
		approvedResult.setContractorSerialNumber(123);
		approvedResult.setContractorOwnerId(1);
		approvedResult.setContractorSlug("test-contractor");
		approvedResult.setJobSlug("test-job");
		approvedResult.setJobId(1);
		approvedResult.setContractorId(1);
		approvedResult.setContractorJobAssignmentId(1);
		approvedResult.setDealId(1);

		// Second timesheet has different company AND is not approved
		InvoiceValidationQueryResultDto differentCompanyResult = new InvoiceValidationQueryResultDto();
		differentCompanyResult.setTimesheetId(2);
		differentCompanyResult.setTimesheetApprovalStatusTypeId(4); // APPROVED but
																	// different company
		differentCompanyResult.setCompanyName("Different Company"); // Different company
		differentCompanyResult.setCompanyId(2);
		differentCompanyResult.setPeriodStart(1704067200);
		differentCompanyResult.setPeriodEnd(1704153600);
		differentCompanyResult.setCurrencyId(1);
		differentCompanyResult.setCurrencyCode("USD");
		differentCompanyResult.setCurrencySymbol("$");
		differentCompanyResult.setBillAmount(200.0);
		differentCompanyResult.setContractorName("Test Contractor 2");
		differentCompanyResult.setContractorProfilePicUrl("http://example.com/photo2.jpg");
		differentCompanyResult.setContractorSerialNumber(124);
		differentCompanyResult.setContractorOwnerId(1);
		differentCompanyResult.setContractorSlug("test-contractor-2");
		differentCompanyResult.setJobSlug("test-job");
		differentCompanyResult.setJobId(1);
		differentCompanyResult.setContractorId(2);
		differentCompanyResult.setContractorJobAssignmentId(2);
		differentCompanyResult.setDealId(1);

		List<InvoiceValidationQueryResultDto> validationResults = Arrays.asList(approvedResult, differentCompanyResult);
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors().subList(0, 2);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(2);
		assertThat(result.getErrorCount()).isOne();

		// Verify second timesheet has DIFFERENT_COMPANY error (priority 1)
		TimesheetInvoicePreviewResponseBodyDto secondPreview = result.getTimesheetInvoicePreviewData()
			.stream()
			.filter((preview) -> preview.getTimesheetId().equals(2))
			.findFirst()
			.orElse(null);
		assertThat(secondPreview).isNotNull();
		assertThat(secondPreview.getErrorKey()).isEqualTo(ValidationErrorEnum.DIFFERENT_COMPANY.getMessage());

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should handle null primary timesheet gracefully")
	void testValidateTimesheetsForInvoiceNullPrimaryTimesheetHandlesGracefully() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(999); // Non-existent timesheet ID
		Integer accountId = 1;
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(Collections.emptyList());
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(Collections.emptyList());

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).isEmpty();
		assertThat(result.getErrorCount()).isZero();

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should skip company and currency checks when primary timesheet is null")
	void testValidateTimesheetsForInvoicePrimaryTimesheetNullWithResultsSkipsPrimaryChecks() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(999);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> validationResults = this.createApprovedInvoiceValidationResults()
			.subList(0, 1);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(Collections.emptyList());

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(result.getErrorCount()).isZero();
		assertThat(result.getTimesheetInvoicePreviewData().get(0).getErrorKey()).isEmpty();

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().validateTimesheetsForInvoice(timesheetIds, accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should handle null currency ID in timesheet")
	void testValidateTimesheetsForInvoiceNullCurrencyIdHandlesGracefully() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;

		InvoiceValidationQueryResultDto resultWithNullCurrency = new InvoiceValidationQueryResultDto();
		resultWithNullCurrency.setTimesheetId(1);
		resultWithNullCurrency.setTimesheetApprovalStatusTypeId(4); // APPROVED
		resultWithNullCurrency.setCompanyName("Test Company");
		resultWithNullCurrency.setCompanyId(1);
		resultWithNullCurrency.setPeriodStart(1704067200);
		resultWithNullCurrency.setPeriodEnd(1704153600);
		resultWithNullCurrency.setCurrencyId(null); // Null currency
		resultWithNullCurrency.setCurrencyCode(null);
		resultWithNullCurrency.setCurrencySymbol(null);
		resultWithNullCurrency.setBillAmount(100.0);
		resultWithNullCurrency.setContractorName("Test Contractor");
		resultWithNullCurrency.setContractorProfilePicUrl("http://example.com/photo.jpg");
		resultWithNullCurrency.setContractorSerialNumber(123);
		resultWithNullCurrency.setContractorOwnerId(1);
		resultWithNullCurrency.setContractorSlug("test-contractor");
		resultWithNullCurrency.setJobSlug("test-job");
		resultWithNullCurrency.setJobId(1);
		resultWithNullCurrency.setContractorId(1);
		resultWithNullCurrency.setContractorJobAssignmentId(1);
		resultWithNullCurrency.setDealId(1);

		List<InvoiceValidationQueryResultDto> validationResults = Arrays.asList(resultWithNullCurrency);
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors().subList(0, 1);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);
		assertThat(result.getErrorCount()).isZero();

		TimesheetInvoicePreviewResponseBodyDto preview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(preview.getCurrencyId()).isNull();
		assertThat(preview.getBillCurrencyCode()).isNull();
		assertThat(preview.getErrorKey()).isEmpty();

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Create timesheet invoice should set correct timestamp")
	void testCreateTimesheetInvoiceSetsCorrectTimestamp() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 1;
		Integer userId = InvoiceTestDataFactory.getDefaultUserId();
		Integer userTypeId = 1;
		Integer beforeTimestamp = Math.toIntExact(java.time.Instant.now().getEpochSecond());

		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId)).willReturn(null);
		given(this.timesheetInvoiceRepository.saveInvoice(any(TimesheetInvoice.class)))
			.willAnswer((invocation) -> invocation.getArgument(0));

		// When
		this.timesheetInvoiceService.createTimesheetInvoice(timesheetId, userId, userTypeId);

		// Then
		then(this.timesheetInvoiceRepository).should().saveInvoice(argThat((invoice) -> {
			Integer afterTimestamp = Math.toIntExact(java.time.Instant.now().getEpochSecond());
			return invoice.getUpdatedOn() >= beforeTimestamp && invoice.getUpdatedOn() <= afterTimestamp;
		}));
	}

	@Test
	@DisplayName("Validate timesheets should set all preview fields correctly")
	void testValidateTimesheetsForInvoiceSetsAllPreviewFieldsCorrectly() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1);
		Integer accountId = 1;

		InvoiceValidationQueryResultDto validationResult = new InvoiceValidationQueryResultDto();
		validationResult.setTimesheetId(1);
		validationResult.setTimesheetApprovalStatusTypeId(4);
		validationResult.setCompanyName("Test Company");
		validationResult.setCompanyId(1);
		validationResult.setPeriodStart(1704067200);
		validationResult.setPeriodEnd(1704153600);
		validationResult.setCurrencyId(1);
		validationResult.setCurrencyCode("USD");
		validationResult.setCurrencySymbol("$");
		validationResult.setBillAmount(100.0);
		validationResult.setContractorName("Test Contractor");
		validationResult.setContractorProfilePicUrl("http://example.com/photo.jpg");
		validationResult.setContractorSerialNumber(123);
		validationResult.setContractorOwnerId(1);
		validationResult.setContractorSlug("test-contractor");
		validationResult.setJobSlug("test-job");
		validationResult.setJobId(1);
		validationResult.setContractorId(1);
		validationResult.setContractorJobAssignmentId(1);
		validationResult.setDealId(1);
		validationResult.setJobContactId(5);
		validationResult.setPayCurrencyCode("GBP");
		validationResult.setPayCurrencySymbol("£");
		validationResult.setIsReimbursementEnabled(1);

		List<InvoiceValidationQueryResultDto> validationResults = Arrays.asList(validationResult);
		List<TimesheetInvoice> timesheetInvoices = this.createTimesheetInvoicesWithNoErrors().subList(0, 1);
		Set<Integer> emptyInvoiceSet = new HashSet<>();

		given(this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId)).willReturn(emptyInvoiceSet);
		given(this.timesheetInvoiceRepository.validateTimesheetsForInvoice(timesheetIds, accountId))
			.willReturn(validationResults);
		given(this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(timesheetInvoices);

		// When
		BulkInvoiceValidationResponseBodyDto result = this.timesheetInvoiceService
			.validateTimesheetsForInvoice(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetInvoicePreviewData()).hasSize(1);

		TimesheetInvoicePreviewResponseBodyDto preview = result.getTimesheetInvoicePreviewData().get(0);
		assertThat(preview)
			.extracting(TimesheetInvoicePreviewResponseBodyDto::getTimesheetId,
					TimesheetInvoicePreviewResponseBodyDto::getCurrencyId,
					TimesheetInvoicePreviewResponseBodyDto::getBillCurrencyCode,
					TimesheetInvoicePreviewResponseBodyDto::getBillCurrencySymbol,
					TimesheetInvoicePreviewResponseBodyDto::getBillAmount,
					TimesheetInvoicePreviewResponseBodyDto::getContractorName,
					TimesheetInvoicePreviewResponseBodyDto::getContractorProfilePicUrl,
					TimesheetInvoicePreviewResponseBodyDto::getTimesheetApprovalStatusTypeId,
					TimesheetInvoicePreviewResponseBodyDto::getContractorSerialNumber,
					TimesheetInvoicePreviewResponseBodyDto::getContractorOwnerId,
					TimesheetInvoicePreviewResponseBodyDto::getContractorSlug,
					TimesheetInvoicePreviewResponseBodyDto::getJobSlug,
					TimesheetInvoicePreviewResponseBodyDto::getContractorJobAssignmentId,
					TimesheetInvoicePreviewResponseBodyDto::getJobId,
					TimesheetInvoicePreviewResponseBodyDto::getContractorId,
					TimesheetInvoicePreviewResponseBodyDto::getPayCurrencyCode,
					TimesheetInvoicePreviewResponseBodyDto::getPayCurrencySymbol,
					TimesheetInvoicePreviewResponseBodyDto::getIsReimbursementEnabled)
			.containsExactly(1, 1, "USD", "$", 100.0, "Test Contractor", "http://example.com/photo.jpg", 4, 123, 1,
					"test-contractor", "test-job", 1, 1, 1, "GBP", "£", 1);
		assertThat(preview.getTimesheetPeriod()).isNotNull()
			.extracting(TimesheetPeriodResponseBodyDto::getTimesheetStartDate,
					TimesheetPeriodResponseBodyDto::getTimesheetEndDate)
			.containsExactly(1704067200, 1704153600);
		assertThat(preview.getAssociations()).isNotNull();
		assertThat(preview.getAssociations().getAssociations().get(11)).containsExactly(1);

		then(this.timesheetInvoiceRepository).should().getTimesheetIds(timesheetIds, accountId);
	}

	/**
	 * Helper method to create approved invoice validation results
	 */
	private List<InvoiceValidationQueryResultDto> createApprovedInvoiceValidationResults() {
		InvoiceValidationQueryResultDto result1 = new InvoiceValidationQueryResultDto();
		result1.setTimesheetId(1);
		result1.setTimesheetApprovalStatusTypeId(4); // APPROVED
		result1.setCompanyName("Test Company");
		result1.setCompanyId(1);
		result1.setPeriodStart(1704067200);
		result1.setPeriodEnd(1704153600);
		result1.setCurrencyId(1);
		result1.setCurrencyCode("USD");
		result1.setCurrencySymbol("$");
		result1.setBillAmount(100.0);
		result1.setContractorName("Test Contractor");
		result1.setContractorProfilePicUrl("http://example.com/photo.jpg");
		result1.setContractorSerialNumber(123);
		result1.setContractorOwnerId(1);
		result1.setContractorSlug("test-contractor");
		result1.setJobSlug("test-job");
		result1.setJobId(1);
		result1.setContractorId(1);
		result1.setContractorJobAssignmentId(1);
		result1.setDealId(1);

		InvoiceValidationQueryResultDto result2 = new InvoiceValidationQueryResultDto();
		result2.setTimesheetId(2);
		result2.setTimesheetApprovalStatusTypeId(4); // APPROVED
		result2.setCompanyName("Test Company");
		result2.setCompanyId(1);
		result2.setPeriodStart(1704067200);
		result2.setPeriodEnd(1704153600);
		result2.setCurrencyId(1);
		result2.setCurrencyCode("USD");
		result2.setCurrencySymbol("$");
		result2.setBillAmount(200.0);
		result2.setContractorName("Test Contractor 2");
		result2.setContractorProfilePicUrl("http://example.com/photo2.jpg");
		result2.setContractorSerialNumber(124);
		result2.setContractorOwnerId(1);
		result2.setContractorSlug("test-contractor-2");
		result2.setJobSlug("test-job");
		result2.setJobId(1);
		result2.setContractorId(2);
		result2.setContractorJobAssignmentId(2);
		result2.setDealId(1);

		InvoiceValidationQueryResultDto result3 = new InvoiceValidationQueryResultDto();
		result3.setTimesheetId(3);
		result3.setTimesheetApprovalStatusTypeId(4); // APPROVED
		result3.setCompanyName("Test Company");
		result3.setCompanyId(1);
		result3.setPeriodStart(1704067200);
		result3.setPeriodEnd(1704153600);
		result3.setCurrencyId(1);
		result3.setCurrencyCode("USD");
		result3.setCurrencySymbol("$");
		result3.setBillAmount(300.0);
		result3.setContractorName("Test Contractor 3");
		result3.setContractorProfilePicUrl("http://example.com/photo3.jpg");
		result3.setContractorSerialNumber(125);
		result3.setContractorOwnerId(1);
		result3.setContractorSlug("test-contractor-3");
		result3.setJobSlug("test-job");
		result3.setJobId(1);
		result3.setContractorId(3);
		result3.setContractorJobAssignmentId(3);
		result3.setDealId(1);

		return Arrays.asList(result1, result2, result3);
	}

	/**
	 * Helper method to create unapproved invoice validation results
	 */
	private List<InvoiceValidationQueryResultDto> createUnapprovedInvoiceValidationResults() {
		InvoiceValidationQueryResultDto result = new InvoiceValidationQueryResultDto();
		result.setTimesheetId(1);
		result.setTimesheetApprovalStatusTypeId(1); // NOT APPROVED
		result.setCompanyName("Test Company");
		result.setCompanyId(1);
		result.setPeriodStart(1704067200);
		result.setPeriodEnd(1704153600);
		result.setCurrencyId(1);
		result.setCurrencyCode("USD");
		result.setCurrencySymbol("$");
		result.setBillAmount(100.0);
		result.setContractorName("Test Contractor");
		result.setContractorProfilePicUrl("http://example.com/photo.jpg");
		result.setContractorSerialNumber(123);
		result.setContractorOwnerId(1);
		result.setContractorSlug("test-contractor");
		result.setJobSlug("test-job");
		result.setJobId(1);
		result.setContractorId(1);
		result.setContractorJobAssignmentId(1);
		result.setDealId(1);

		return Collections.singletonList(result);
	}

	/**
	 * Helper method to create null approval status validation results
	 */
	private List<InvoiceValidationQueryResultDto> createNullApprovalStatusValidationResults() {
		InvoiceValidationQueryResultDto result = new InvoiceValidationQueryResultDto();
		result.setTimesheetId(1);
		result.setTimesheetApprovalStatusTypeId(null); // NULL APPROVAL STATUS
		result.setCompanyName("Test Company");
		result.setCompanyId(1);
		result.setPeriodStart(1704067200);
		result.setPeriodEnd(1704153600);
		result.setCurrencyId(1);
		result.setCurrencyCode("USD");
		result.setCurrencySymbol("$");
		result.setBillAmount(100.0);
		result.setContractorName("Test Contractor");
		result.setContractorProfilePicUrl("http://example.com/photo.jpg");
		result.setContractorSerialNumber(123);
		result.setContractorOwnerId(1);
		result.setContractorSlug("test-contractor");
		result.setJobSlug("test-job");
		result.setJobId(1);
		result.setContractorId(1);
		result.setContractorJobAssignmentId(1);
		result.setDealId(1);

		return Collections.singletonList(result);
	}

	/**
	 * Helper method to create different company validation results
	 */
	private List<InvoiceValidationQueryResultDto> createDifferentCompanyValidationResults() {
		InvoiceValidationQueryResultDto result1 = new InvoiceValidationQueryResultDto();
		result1.setTimesheetId(1);
		result1.setTimesheetApprovalStatusTypeId(4); // APPROVED
		result1.setCompanyName("Test Company");
		result1.setCompanyId(1);
		result1.setPeriodStart(1704067200);
		result1.setPeriodEnd(1704153600);
		result1.setCurrencyId(1);
		result1.setCurrencyCode("USD");
		result1.setCurrencySymbol("$");
		result1.setBillAmount(100.0);
		result1.setContractorName("Test Contractor");
		result1.setContractorProfilePicUrl("http://example.com/photo.jpg");
		result1.setContractorSerialNumber(123);
		result1.setContractorOwnerId(1);
		result1.setContractorSlug("test-contractor");
		result1.setJobSlug("test-job");
		result1.setJobId(1);
		result1.setContractorId(1);
		result1.setContractorJobAssignmentId(1);
		result1.setDealId(1);

		InvoiceValidationQueryResultDto result2 = new InvoiceValidationQueryResultDto();
		result2.setTimesheetId(2);
		result2.setTimesheetApprovalStatusTypeId(4); // APPROVED
		result2.setCompanyName("Different Company"); // Different company
		result2.setCompanyId(2);
		result2.setPeriodStart(1704067200);
		result2.setPeriodEnd(1704153600);
		result2.setCurrencyId(1);
		result2.setCurrencyCode("USD");
		result2.setCurrencySymbol("$");
		result2.setBillAmount(200.0);
		result2.setContractorName("Test Contractor 2");
		result2.setContractorProfilePicUrl("http://example.com/photo2.jpg");
		result2.setContractorSerialNumber(124);
		result2.setContractorOwnerId(1);
		result2.setContractorSlug("test-contractor-2");
		result2.setJobSlug("test-job");
		result2.setJobId(1);
		result2.setContractorId(2);
		result2.setContractorJobAssignmentId(2);
		result2.setDealId(1);

		return Arrays.asList(result1, result2);
	}

	/**
	 * Helper method to create different currency validation results
	 */
	private List<InvoiceValidationQueryResultDto> createDifferentCurrencyValidationResults() {
		InvoiceValidationQueryResultDto result1 = new InvoiceValidationQueryResultDto();
		result1.setTimesheetId(1);
		result1.setTimesheetApprovalStatusTypeId(4); // APPROVED
		result1.setCompanyName("Test Company");
		result1.setCompanyId(1);
		result1.setPeriodStart(1704067200);
		result1.setPeriodEnd(1704153600);
		result1.setCurrencyId(1);
		result1.setCurrencyCode("USD");
		result1.setCurrencySymbol("$");
		result1.setBillAmount(100.0);
		result1.setContractorName("Test Contractor");
		result1.setContractorProfilePicUrl("http://example.com/photo.jpg");
		result1.setContractorSerialNumber(123);
		result1.setContractorOwnerId(1);
		result1.setContractorSlug("test-contractor");
		result1.setJobSlug("test-job");
		result1.setJobId(1);
		result1.setContractorId(1);
		result1.setContractorJobAssignmentId(1);
		result1.setDealId(1);

		InvoiceValidationQueryResultDto result2 = new InvoiceValidationQueryResultDto();
		result2.setTimesheetId(2);
		result2.setTimesheetApprovalStatusTypeId(4); // APPROVED
		result2.setCompanyName("Test Company");
		result2.setCompanyId(1);
		result2.setPeriodStart(1704067200);
		result2.setPeriodEnd(1704153600);
		result2.setCurrencyId(2); // Different currency
		result2.setCurrencyCode("EUR");
		result2.setCurrencySymbol("€");
		result2.setBillAmount(200.0);
		result2.setContractorName("Test Contractor 2");
		result2.setContractorProfilePicUrl("http://example.com/photo2.jpg");
		result2.setContractorSerialNumber(124);
		result2.setContractorOwnerId(1);
		result2.setContractorSlug("test-contractor-2");
		result2.setJobSlug("test-job");
		result2.setJobId(1);
		result2.setContractorId(2);
		result2.setContractorJobAssignmentId(2);
		result2.setDealId(1);

		return Arrays.asList(result1, result2);
	}

	/**
	 * Helper method to create multiple deal validation results
	 */
	private List<InvoiceValidationQueryResultDto> createMultipleDealValidationResults() {
		InvoiceValidationQueryResultDto result1 = new InvoiceValidationQueryResultDto();
		result1.setTimesheetId(1);
		result1.setTimesheetApprovalStatusTypeId(4); // APPROVED
		result1.setCompanyName("Test Company");
		result1.setCompanyId(1);
		result1.setPeriodStart(1704067200);
		result1.setPeriodEnd(1704153600);
		result1.setCurrencyId(1);
		result1.setCurrencyCode("USD");
		result1.setCurrencySymbol("$");
		result1.setBillAmount(100.0);
		result1.setContractorName("Test Contractor");
		result1.setContractorProfilePicUrl("http://example.com/photo.jpg");
		result1.setContractorSerialNumber(123);
		result1.setContractorOwnerId(1);
		result1.setContractorSlug("test-contractor");
		result1.setJobSlug("test-job");
		result1.setJobId(1);
		result1.setContractorId(1);
		result1.setContractorJobAssignmentId(1);
		result1.setDealId(1); // First deal

		InvoiceValidationQueryResultDto result2 = new InvoiceValidationQueryResultDto();
		result2.setTimesheetId(1); // Same timesheet
		result2.setTimesheetApprovalStatusTypeId(4); // APPROVED
		result2.setCompanyName("Test Company");
		result2.setCompanyId(1);
		result2.setPeriodStart(1704067200);
		result2.setPeriodEnd(1704153600);
		result2.setCurrencyId(1);
		result2.setCurrencyCode("USD");
		result2.setCurrencySymbol("$");
		result2.setBillAmount(100.0);
		result2.setContractorName("Test Contractor");
		result2.setContractorProfilePicUrl("http://example.com/photo.jpg");
		result2.setContractorSerialNumber(123);
		result2.setContractorOwnerId(1);
		result2.setContractorSlug("test-contractor");
		result2.setJobSlug("test-job");
		result2.setJobId(1);
		result2.setContractorId(1);
		result2.setContractorJobAssignmentId(1);
		result2.setDealId(2); // Second deal

		return Arrays.asList(result1, result2);
	}

	/**
	 * Helper method to create null deal validation results
	 */
	private List<InvoiceValidationQueryResultDto> createNullDealValidationResults() {
		InvoiceValidationQueryResultDto result = new InvoiceValidationQueryResultDto();
		result.setTimesheetId(1);
		result.setTimesheetApprovalStatusTypeId(4); // APPROVED
		result.setCompanyName("Test Company");
		result.setCompanyId(1);
		result.setPeriodStart(1704067200);
		result.setPeriodEnd(1704153600);
		result.setCurrencyId(1);
		result.setCurrencyCode("USD");
		result.setCurrencySymbol("$");
		result.setBillAmount(100.0);
		result.setContractorName("Test Contractor");
		result.setContractorProfilePicUrl("http://example.com/photo.jpg");
		result.setContractorSerialNumber(123);
		result.setContractorOwnerId(1);
		result.setContractorSlug("test-contractor");
		result.setJobSlug("test-job");
		result.setJobId(1);
		result.setContractorId(1);
		result.setContractorJobAssignmentId(1);
		result.setDealId(null); // Null deal

		return Collections.singletonList(result);
	}

	/**
	 * Helper method to create timesheet invoices with no errors
	 */
	private List<TimesheetInvoice> createTimesheetInvoicesWithNoErrors() {
		TimesheetInvoice invoice1 = new TimesheetInvoice();
		invoice1.setTimesheetId(1);
		invoice1.setBillingStatusId(BillStatusEnum.UN_BILLED.getId());

		TimesheetInvoice invoice2 = new TimesheetInvoice();
		invoice2.setTimesheetId(2);
		invoice2.setBillingStatusId(BillStatusEnum.UN_BILLED.getId());

		TimesheetInvoice invoice3 = new TimesheetInvoice();
		invoice3.setTimesheetId(3);
		invoice3.setBillingStatusId(BillStatusEnum.UN_BILLED.getId());

		return Arrays.asList(invoice1, invoice2, invoice3);
	}

	/**
	 * Helper method to create timesheet invoice with specific billing status
	 */
	private TimesheetInvoice createTimesheetInvoiceWithBillingStatus(Integer timesheetId, Integer billingStatusId) {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(timesheetId);
		invoice.setBillingStatusId(billingStatusId);
		return invoice;
	}

	/**
	 * Helper method to create timesheet invoice with null billing status
	 */
	private TimesheetInvoice createTimesheetInvoiceWithNullBillingStatus(Integer timesheetId) {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(timesheetId);
		invoice.setBillingStatusId(null);
		return invoice;
	}

}
