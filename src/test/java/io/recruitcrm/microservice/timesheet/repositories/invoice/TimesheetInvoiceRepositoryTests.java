package io.recruitcrm.microservice.timesheet.repositories.invoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;

import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.microservice.TimesheetApplication;
import io.recruitcrm.microservice.timesheet.dao.invoice.InvoiceJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.invoice.InvoiceValidationQueryResultDto;
import io.recruitcrm.microservice.timesheet.testdata.InvoiceTestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TimesheetApplication.class)
class TimesheetInvoiceRepositoryTests {

	@InjectMocks
	private TimesheetInvoiceRepository timesheetInvoiceRepository;

	@Mock
	private InvoiceJpaRepository invoiceJpaRepository;

	@Mock
	private EntityManager entityManager;

	@Mock
	private TypedQuery<InvoiceValidationQueryResultDto> validationQuery;

	@Mock
	private TypedQuery<Integer> integerQuery;

	@Test
	@DisplayName("Find by timesheet ID should return timesheet invoice")
	void testFindByTimesheetIdValidIdReturnsTimesheetInvoice() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 123;
		TimesheetInvoice expectedInvoice = InvoiceTestDataFactory.createTimesheetInvoiceWithBothStatuses();

		given(this.invoiceJpaRepository.findByTimesheetIdAndAccountId(timesheetId, accountId))
			.willReturn(expectedInvoice);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getBillingStatusId()).isEqualTo(expectedInvoice.getBillingStatusId());
		assertThat(result.getPaymentStatusId()).isEqualTo(expectedInvoice.getPaymentStatusId());

		then(this.invoiceJpaRepository).should().findByTimesheetIdAndAccountId(timesheetId, accountId);
	}

	@Test
	@DisplayName("Find by timesheet ID should return null when not found")
	void testFindByTimesheetIdNotFoundReturnsNull() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 123;

		given(this.invoiceJpaRepository.findByTimesheetIdAndAccountId(timesheetId, accountId)).willReturn(null);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId);

		// Then
		assertThat(result).isNull();

		then(this.invoiceJpaRepository).should().findByTimesheetIdAndAccountId(timesheetId, accountId);
	}

	@Test
	@DisplayName("Find by timesheet IDs should return list of timesheet invoices")
	void testFindByTimesheetIdInValidIdsReturnsTimesheetInvoices() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1, 2, 3);
		Integer accountId = 123;
		TimesheetInvoice invoice1 = InvoiceTestDataFactory.createTimesheetInvoiceWithBothStatuses();
		TimesheetInvoice invoice2 = InvoiceTestDataFactory.createTimesheetInvoiceWithBillingStatusOnly();
		TimesheetInvoice invoice3 = InvoiceTestDataFactory.createTimesheetInvoiceWithPaymentStatusOnly();
		List<TimesheetInvoice> expectedInvoices = Arrays.asList(invoice1, invoice2, invoice3);

		given(this.invoiceJpaRepository.findByTimesheetIdIn(timesheetIds, accountId)).willReturn(expectedInvoices);

		// When
		List<TimesheetInvoice> result = this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId);

		// Then
		assertThat(result).containsExactlyInAnyOrderElementsOf(expectedInvoices);

		then(this.invoiceJpaRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Find by timesheet IDs should return empty list when no invoices found")
	void testFindByTimesheetIdInNoInvoicesFoundReturnsEmptyList() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1, 2, 3);
		Integer accountId = 123;

		given(this.invoiceJpaRepository.findByTimesheetIdIn(timesheetIds, accountId))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetInvoice> result = this.timesheetInvoiceRepository.findByTimesheetIdIn(timesheetIds, accountId);

		// Then
		assertThat(result).isEmpty();

		then(this.invoiceJpaRepository).should().findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Save invoice should persist and return saved invoice")
	void testSaveInvoiceValidInvoiceReturnsSavedInvoice() {
		// Given
		TimesheetInvoice invoice = InvoiceTestDataFactory.createTimesheetInvoiceWithBothStatuses();
		TimesheetInvoice savedInvoice = InvoiceTestDataFactory.createTimesheetInvoiceWithBothStatuses();
		savedInvoice.setId(1);

		given(this.invoiceJpaRepository.save(invoice)).willReturn(savedInvoice);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository.saveInvoice(invoice);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1);
		assertThat(result.getTimesheetId()).isEqualTo(invoice.getTimesheetId());

		then(this.invoiceJpaRepository).should().save(invoice);
	}

	@Test
	@DisplayName("Find invoice with status history by timesheet ID should return invoice")
	void testFindInvoiceWithStatusHistoryByTimesheetIdValidIdReturnsInvoice() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 123;
		TimesheetInvoice expectedInvoice = InvoiceTestDataFactory.createTimesheetInvoiceWithBothStatuses();

		given(this.invoiceJpaRepository.findByTimesheetIdAndAccountId(timesheetId, accountId))
			.willReturn(expectedInvoice);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository.findInvoiceWithStatusHistoryByTimesheetId(timesheetId,
				accountId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getBillingStatusId()).isEqualTo(expectedInvoice.getBillingStatusId());
		assertThat(result.getPaymentStatusId()).isEqualTo(expectedInvoice.getPaymentStatusId());

		then(this.invoiceJpaRepository).should().findByTimesheetIdAndAccountId(timesheetId, accountId);
	}

	@Test
	@DisplayName("Find bill details by timesheet ID should return invoice")
	void testFindBillDetailsByTimesheetIdValidIdReturnsInvoice() {
		// Given
		Integer timesheetId = InvoiceTestDataFactory.getDefaultTimesheetId();
		Integer accountId = 123;
		TimesheetInvoice expectedInvoice = InvoiceTestDataFactory.createTimesheetInvoiceWithBothStatuses();

		given(this.invoiceJpaRepository.findByTimesheetIdAndAccountId(timesheetId, accountId))
			.willReturn(expectedInvoice);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository.findBillDetailsByTimesheetId(timesheetId, accountId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getBillingStatusId()).isEqualTo(expectedInvoice.getBillingStatusId());

		then(this.invoiceJpaRepository).should().findByTimesheetIdAndAccountId(timesheetId, accountId);
	}

	@Test
	@DisplayName("Validate timesheets for invoice should return validation results")
	@SuppressWarnings("unchecked")
	void testValidateTimesheetsForInvoiceValidIdsReturnsValidationResults() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1, 2, 3);
		Integer accountId = 1;
		List<InvoiceValidationQueryResultDto> expectedResults = this.createValidationResults();

		given(this.entityManager.createQuery(any(String.class), any(Class.class))).willReturn(this.validationQuery);
		given(this.validationQuery.setParameter("timesheetIds", timesheetIds)).willReturn(this.validationQuery);
		given(this.validationQuery.setParameter("accountId", accountId)).willReturn(this.validationQuery);
		given(this.validationQuery.getResultList()).willReturn(expectedResults);

		// When
		List<InvoiceValidationQueryResultDto> result = this.timesheetInvoiceRepository
			.validateTimesheetsForInvoice(timesheetIds, accountId);

		// Then
		assertThat(result).containsExactlyInAnyOrderElementsOf(expectedResults);

		then(this.entityManager).should().createQuery(any(String.class), any(Class.class));
	}

	@Test
	@DisplayName("Validate timesheets for invoice should return empty list when no timesheets found")
	@SuppressWarnings("unchecked")
	void testValidateTimesheetsForInvoiceNoTimesheetsFoundReturnsEmptyList() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1, 2, 3);
		Integer accountId = 1;

		given(this.entityManager.createQuery(any(String.class), any(Class.class))).willReturn(this.validationQuery);
		given(this.validationQuery.setParameter("timesheetIds", timesheetIds)).willReturn(this.validationQuery);
		given(this.validationQuery.setParameter("accountId", accountId)).willReturn(this.validationQuery);
		given(this.validationQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		List<InvoiceValidationQueryResultDto> result = this.timesheetInvoiceRepository
			.validateTimesheetsForInvoice(timesheetIds, accountId);

		// Then
		assertThat(result).isEmpty();

		then(this.entityManager).should().createQuery(any(String.class), any(Class.class));
	}

	@Test
	@DisplayName("Get timesheet IDs should return set of timesheet IDs with invoices")
	@SuppressWarnings("unchecked")
	void testGetTimesheetIdsValidIdsReturnsTimesheetIds() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1, 2, 3);
		Integer accountId = 1;
		List<Integer> timesheetIdsWithInvoices = Arrays.asList(1, 3);

		given(this.entityManager.createQuery(any(String.class), any(Class.class))).willReturn(this.integerQuery);
		given(this.integerQuery.setParameter("timesheetIds", timesheetIds)).willReturn(this.integerQuery);
		given(this.integerQuery.setParameter("accountId", accountId)).willReturn(this.integerQuery);
		given(this.integerQuery.getResultList()).willReturn(timesheetIdsWithInvoices);

		// When
		Set<Integer> result = this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId);

		// Then
		assertThat(result).containsExactlyInAnyOrder(1, 3);

		then(this.entityManager).should().createQuery(any(String.class), any(Class.class));
	}

	@Test
	@DisplayName("Get timesheet IDs should return empty set when no invoices found")
	@SuppressWarnings("unchecked")
	void testGetTimesheetIdsNoInvoicesFoundReturnsEmptySet() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1, 2, 3);
		Integer accountId = 1;

		given(this.entityManager.createQuery(any(String.class), any(Class.class))).willReturn(this.integerQuery);
		given(this.integerQuery.setParameter("timesheetIds", timesheetIds)).willReturn(this.integerQuery);
		given(this.integerQuery.setParameter("accountId", accountId)).willReturn(this.integerQuery);
		given(this.integerQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		Set<Integer> result = this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId);

		// Then
		assertThat(result).isEmpty();

		then(this.entityManager).should().createQuery(any(String.class), any(Class.class));
	}

	@Test
	@DisplayName("Get timesheet IDs should handle duplicate IDs correctly")
	@SuppressWarnings("unchecked")
	void testGetTimesheetIdsDuplicateIdsReturnsUniqueSet() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(1, 2, 3);
		Integer accountId = 1;
		List<Integer> duplicateTimesheetIds = Arrays.asList(1, 1, 2, 2, 3);

		given(this.entityManager.createQuery(any(String.class), any(Class.class))).willReturn(this.integerQuery);
		given(this.integerQuery.setParameter("timesheetIds", timesheetIds)).willReturn(this.integerQuery);
		given(this.integerQuery.setParameter("accountId", accountId)).willReturn(this.integerQuery);
		given(this.integerQuery.getResultList()).willReturn(duplicateTimesheetIds);

		// When
		Set<Integer> result = this.timesheetInvoiceRepository.getTimesheetIds(timesheetIds, accountId);

		// Then
		assertThat(result).containsExactlyInAnyOrder(1, 2, 3);

		then(this.entityManager).should().createQuery(any(String.class), any(Class.class));
	}

	/**
	 * Helper method to create validation results for testing
	 */
	private List<InvoiceValidationQueryResultDto> createValidationResults() {
		InvoiceValidationQueryResultDto result1 = new InvoiceValidationQueryResultDto();
		result1.setTimesheetId(1);
		result1.setTimesheetApprovalStatusTypeId(4);
		result1.setCompanyName("Test Company 1");
		result1.setCurrencyId(1);
		result1.setBillAmount(100.0);

		InvoiceValidationQueryResultDto result2 = new InvoiceValidationQueryResultDto();
		result2.setTimesheetId(2);
		result2.setTimesheetApprovalStatusTypeId(4);
		result2.setCompanyName("Test Company 2");
		result2.setCurrencyId(1);
		result2.setBillAmount(200.0);

		InvoiceValidationQueryResultDto result3 = new InvoiceValidationQueryResultDto();
		result3.setTimesheetId(3);
		result3.setTimesheetApprovalStatusTypeId(4);
		result3.setCompanyName("Test Company 3");
		result3.setCurrencyId(1);
		result3.setBillAmount(300.0);

		return Arrays.asList(result1, result2, result3);
	}

}
