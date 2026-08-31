package io.recruitcrm.microservice.timesheet.repositories.invoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.microservice.timesheet.dao.invoice.InvoiceJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.invoice.InvoiceValidationQueryResultDto;
import io.recruitcrm.microservice.timesheet.testdata.InvoiceRepositoryTestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class InvoiceRepositoryTests {

	@InjectMocks
	private TimesheetInvoiceRepository timesheetInvoiceRepository;

	@Mock
	private InvoiceJpaRepository invoiceJpaRepository;

	@Mock
	private EntityManager entityManager;

	@Mock
	private TypedQuery<InvoiceValidationQueryResultDto> query;

	private static final Integer TIMESHEET_ID = InvoiceRepositoryTestDataFactory.getDefaultTimesheetId();

	private static final Integer ACCOUNT_ID = InvoiceRepositoryTestDataFactory.getDefaultAccountId();

	private static final List<Integer> TIMESHEET_IDS = InvoiceRepositoryTestDataFactory.createTimesheetIds();

	@BeforeEach
	void setUp() {
		// Common setup if needed
	}

	@Test
	@DisplayName("Find by timesheet ID - Success")
	void testFindByTimesheetIdSuccessfully() {
		// Given
		TimesheetInvoice expectedInvoice = InvoiceRepositoryTestDataFactory.createTimesheetInvoice();
		given(this.invoiceJpaRepository.findByTimesheetIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(expectedInvoice);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository.findByTimesheetId(TIMESHEET_ID, ACCOUNT_ID);

		// Then
		assertThat(result).isNotNull().isEqualTo(expectedInvoice);
		then(this.invoiceJpaRepository).should().findByTimesheetIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID);
	}

	@Test
	@DisplayName("Find by timesheet ID - Not found")
	void testFindByTimesheetIdNotFound() {
		// Given
		given(this.invoiceJpaRepository.findByTimesheetIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(null);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository.findByTimesheetId(TIMESHEET_ID, ACCOUNT_ID);

		// Then
		assertThat(result).isNull();
		then(this.invoiceJpaRepository).should().findByTimesheetIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID);
	}

	@Test
	@DisplayName("Find by timesheet ID in - Success")
	void testFindByTimesheetIdInSuccessfully() {
		// Given
		List<TimesheetInvoice> expectedInvoices = InvoiceRepositoryTestDataFactory.createTimesheetInvoices();
		given(this.invoiceJpaRepository.findByTimesheetIdIn(TIMESHEET_IDS, ACCOUNT_ID)).willReturn(expectedInvoices);

		// When
		List<TimesheetInvoice> result = this.timesheetInvoiceRepository.findByTimesheetIdIn(TIMESHEET_IDS, ACCOUNT_ID);

		// Then
		assertThat(result).isNotNull().hasSize(expectedInvoices.size()).isEqualTo(expectedInvoices);
		then(this.invoiceJpaRepository).should().findByTimesheetIdIn(TIMESHEET_IDS, ACCOUNT_ID);
	}

	@Test
	@DisplayName("Find by timesheet ID in - Empty result")
	void testFindByTimesheetIdInEmptyResult() {
		// Given
		given(this.invoiceJpaRepository.findByTimesheetIdIn(TIMESHEET_IDS, ACCOUNT_ID))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetInvoice> result = this.timesheetInvoiceRepository.findByTimesheetIdIn(TIMESHEET_IDS, ACCOUNT_ID);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.invoiceJpaRepository).should().findByTimesheetIdIn(TIMESHEET_IDS, ACCOUNT_ID);
	}

	@Test
	@DisplayName("Find by timesheet ID in - Null result")
	void testFindByTimesheetIdInNullResult() {
		// Given
		given(this.invoiceJpaRepository.findByTimesheetIdIn(TIMESHEET_IDS, ACCOUNT_ID)).willReturn(null);

		// When
		List<TimesheetInvoice> result = this.timesheetInvoiceRepository.findByTimesheetIdIn(TIMESHEET_IDS, ACCOUNT_ID);

		// Then
		assertThat(result).isNull();
		then(this.invoiceJpaRepository).should().findByTimesheetIdIn(TIMESHEET_IDS, ACCOUNT_ID);
	}

	@Test
	@DisplayName("Save invoice - Success")
	void testSaveInvoiceSuccessfully() {
		// Given
		TimesheetInvoice invoiceToSave = InvoiceRepositoryTestDataFactory.createTimesheetInvoice();
		TimesheetInvoice savedInvoice = InvoiceRepositoryTestDataFactory.createTimesheetInvoiceWithId(1);
		given(this.invoiceJpaRepository.save(invoiceToSave)).willReturn(savedInvoice);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository.saveInvoice(invoiceToSave);

		// Then
		assertThat(result).isNotNull().isEqualTo(savedInvoice);
		then(this.invoiceJpaRepository).should().save(invoiceToSave);
	}

	@Test
	@DisplayName("Save invoice - Null input")
	void testSaveInvoiceWithNullInput() {
		// Given
		given(this.invoiceJpaRepository.save(null)).willReturn(null);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository.saveInvoice(null);

		// Then
		assertThat(result).isNull();
		then(this.invoiceJpaRepository).should().save(null);
	}

	@Test
	@DisplayName("Find invoice with status history by timesheet ID - Success")
	void testFindInvoiceWithStatusHistoryByTimesheetIdSuccessfully() {
		// Given
		TimesheetInvoice expectedInvoice = InvoiceRepositoryTestDataFactory.createTimesheetInvoice();
		given(this.invoiceJpaRepository.findByTimesheetIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(expectedInvoice);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository
			.findInvoiceWithStatusHistoryByTimesheetId(TIMESHEET_ID, ACCOUNT_ID);

		// Then
		assertThat(result).isNotNull().isEqualTo(expectedInvoice);
		then(this.invoiceJpaRepository).should().findByTimesheetIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID);
	}

	@Test
	@DisplayName("Find invoice with status history by timesheet ID - Not found")
	void testFindInvoiceWithStatusHistoryByTimesheetIdNotFound() {
		// Given
		given(this.invoiceJpaRepository.findByTimesheetIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(null);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository
			.findInvoiceWithStatusHistoryByTimesheetId(TIMESHEET_ID, ACCOUNT_ID);

		// Then
		assertThat(result).isNull();
		then(this.invoiceJpaRepository).should().findByTimesheetIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID);
	}

	@Test
	@DisplayName("Find bill details by timesheet ID - Success")
	void testFindBillDetailsByTimesheetIdSuccessfully() {
		// Given
		TimesheetInvoice expectedInvoice = InvoiceRepositoryTestDataFactory.createTimesheetInvoice();
		given(this.invoiceJpaRepository.findByTimesheetIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(expectedInvoice);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository.findBillDetailsByTimesheetId(TIMESHEET_ID,
				ACCOUNT_ID);

		// Then
		assertThat(result).isNotNull().isEqualTo(expectedInvoice);
		then(this.invoiceJpaRepository).should().findByTimesheetIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID);
	}

	@Test
	@DisplayName("Find bill details by timesheet ID - Not found")
	void testFindBillDetailsByTimesheetIdNotFound() {
		// Given
		given(this.invoiceJpaRepository.findByTimesheetIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(null);

		// When
		TimesheetInvoice result = this.timesheetInvoiceRepository.findBillDetailsByTimesheetId(TIMESHEET_ID,
				ACCOUNT_ID);

		// Then
		assertThat(result).isNull();
		then(this.invoiceJpaRepository).should().findByTimesheetIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID);
	}

	@Test
	@DisplayName("Validate timesheets for invoice - Success")
	void testValidateTimesheetsForInvoiceSuccessfully() {
		// Given
		List<InvoiceValidationQueryResultDto> expectedResults = InvoiceRepositoryTestDataFactory
			.createInvoiceValidationQueryResultDtos();
		given(this.entityManager.createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class)))
			.willReturn(this.query);
		given(this.query.setParameter("timesheetIds", TIMESHEET_IDS)).willReturn(this.query);
		given(this.query.setParameter("accountId", ACCOUNT_ID)).willReturn(this.query);
		given(this.query.getResultList()).willReturn(expectedResults);

		// When
		List<InvoiceValidationQueryResultDto> result = this.timesheetInvoiceRepository
			.validateTimesheetsForInvoice(TIMESHEET_IDS, ACCOUNT_ID);

		// Then
		assertThat(result).isNotNull().hasSize(expectedResults.size()).isEqualTo(expectedResults);
		then(this.entityManager).should().createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class));
		then(this.query).should().setParameter("timesheetIds", TIMESHEET_IDS);
		then(this.query).should().setParameter("accountId", ACCOUNT_ID);
		then(this.query).should().getResultList();
	}

	@Test
	@DisplayName("Validate timesheets for invoice - Empty result")
	void testValidateTimesheetsForInvoiceEmptyResult() {
		// Given
		given(this.entityManager.createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class)))
			.willReturn(this.query);
		given(this.query.setParameter("timesheetIds", TIMESHEET_IDS)).willReturn(this.query);
		given(this.query.setParameter("accountId", ACCOUNT_ID)).willReturn(this.query);
		given(this.query.getResultList()).willReturn(Collections.emptyList());

		// When
		List<InvoiceValidationQueryResultDto> result = this.timesheetInvoiceRepository
			.validateTimesheetsForInvoice(TIMESHEET_IDS, ACCOUNT_ID);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.entityManager).should().createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class));
		then(this.query).should().setParameter("timesheetIds", TIMESHEET_IDS);
		then(this.query).should().setParameter("accountId", ACCOUNT_ID);
		then(this.query).should().getResultList();
	}

	@Test
	@DisplayName("Validate timesheets for invoice - Null result")
	void testValidateTimesheetsForInvoiceNullResult() {
		// Given
		given(this.entityManager.createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class)))
			.willReturn(this.query);
		given(this.query.setParameter("timesheetIds", TIMESHEET_IDS)).willReturn(this.query);
		given(this.query.setParameter("accountId", ACCOUNT_ID)).willReturn(this.query);
		given(this.query.getResultList()).willReturn(null);

		// When
		List<InvoiceValidationQueryResultDto> result = this.timesheetInvoiceRepository
			.validateTimesheetsForInvoice(TIMESHEET_IDS, ACCOUNT_ID);

		// Then
		assertThat(result).isNull();
		then(this.entityManager).should().createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class));
		then(this.query).should().setParameter("timesheetIds", TIMESHEET_IDS);
		then(this.query).should().setParameter("accountId", ACCOUNT_ID);
		then(this.query).should().getResultList();
	}

	@Test
	@DisplayName("Validate timesheets for invoice - Single timesheet ID")
	void testValidateTimesheetsForInvoiceSingleTimesheetId() {
		// Given
		List<Integer> singleTimesheetId = Collections.singletonList(TIMESHEET_ID);
		List<InvoiceValidationQueryResultDto> expectedResults = Collections
			.singletonList(InvoiceRepositoryTestDataFactory.createInvoiceValidationQueryResultDto());
		given(this.entityManager.createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class)))
			.willReturn(this.query);
		given(this.query.setParameter("timesheetIds", singleTimesheetId)).willReturn(this.query);
		given(this.query.setParameter("accountId", ACCOUNT_ID)).willReturn(this.query);
		given(this.query.getResultList()).willReturn(expectedResults);

		// When
		List<InvoiceValidationQueryResultDto> result = this.timesheetInvoiceRepository
			.validateTimesheetsForInvoice(singleTimesheetId, ACCOUNT_ID);

		// Then
		assertThat(result).isNotNull().hasSize(1).isEqualTo(expectedResults);
		then(this.entityManager).should().createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class));
		then(this.query).should().setParameter("timesheetIds", singleTimesheetId);
		then(this.query).should().setParameter("accountId", ACCOUNT_ID);
		then(this.query).should().getResultList();
	}

	@Test
	@DisplayName("Validate timesheets for invoice - Empty timesheet IDs list")
	void testValidateTimesheetsForInvoiceEmptyTimesheetIdsList() {
		// Given
		List<Integer> emptyTimesheetIds = Collections.emptyList();
		given(this.entityManager.createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class)))
			.willReturn(this.query);
		given(this.query.setParameter("timesheetIds", emptyTimesheetIds)).willReturn(this.query);
		given(this.query.setParameter("accountId", ACCOUNT_ID)).willReturn(this.query);
		given(this.query.getResultList()).willReturn(Collections.emptyList());

		// When
		List<InvoiceValidationQueryResultDto> result = this.timesheetInvoiceRepository
			.validateTimesheetsForInvoice(emptyTimesheetIds, ACCOUNT_ID);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.entityManager).should().createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class));
		then(this.query).should().setParameter("timesheetIds", emptyTimesheetIds);
		then(this.query).should().setParameter("accountId", ACCOUNT_ID);
		then(this.query).should().getResultList();
	}

	@Test
	@DisplayName("Validate timesheets for invoice - Null timesheet IDs list")
	void testValidateTimesheetsForInvoiceNullTimesheetIdsList() {
		// Given
		given(this.entityManager.createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class)))
			.willReturn(this.query);
		given(this.query.setParameter("timesheetIds", null)).willReturn(this.query);
		given(this.query.setParameter("accountId", ACCOUNT_ID)).willReturn(this.query);
		given(this.query.getResultList()).willReturn(Collections.emptyList());

		// When
		List<InvoiceValidationQueryResultDto> result = this.timesheetInvoiceRepository
			.validateTimesheetsForInvoice(null, ACCOUNT_ID);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.entityManager).should().createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class));
		then(this.query).should().setParameter("timesheetIds", null);
		then(this.query).should().setParameter("accountId", ACCOUNT_ID);
		then(this.query).should().getResultList();
	}

	@Test
	@DisplayName("Validate timesheets for invoice - Null account ID")
	void testValidateTimesheetsForInvoiceNullAccountId() {
		// Given
		given(this.entityManager.createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class)))
			.willReturn(this.query);
		given(this.query.setParameter("timesheetIds", TIMESHEET_IDS)).willReturn(this.query);
		given(this.query.setParameter("accountId", null)).willReturn(this.query);
		given(this.query.getResultList()).willReturn(Collections.emptyList());

		// When
		List<InvoiceValidationQueryResultDto> result = this.timesheetInvoiceRepository
			.validateTimesheetsForInvoice(TIMESHEET_IDS, null);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.entityManager).should().createQuery(anyString(), eq(InvoiceValidationQueryResultDto.class));
		then(this.query).should().setParameter("timesheetIds", TIMESHEET_IDS);
		then(this.query).should().setParameter("accountId", null);
		then(this.query).should().getResultList();
	}

}