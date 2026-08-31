package io.recruitcrm.microservice.timesheet.services.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursement;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcurrency;
import io.recruitcrm.microservice.timesheet.dao.reimbursement.TimesheetReimbursementJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ReimbursementExportRowDto;
import io.recruitcrm.microservice.timesheet.helpers.constants.ReimbursementConstants;
import io.recruitcrm.microservice.timesheet.testdata.ReimbursementExportTestDataFactory;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;

/**
 * Unit tests for ReimbursementExportService covering row building, currency symbol
 * resolution, context field resolution, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReimbursementExportService Tests")
class ReimbursementExportServiceTests {

	@InjectMocks
	private ReimbursementExportService reimbursementExportService;

	@Mock
	private TimesheetReimbursementJpaRepository reimbursementJpaRepository;

	@Mock
	private DSLContext dslContext;

	@Mock
	private SelectSelectStep<?> selectStep;

	@Mock
	private SelectJoinStep<?> joinStep;

	@Mock
	private SelectConditionStep<?> conditionStep;

	@Mock
	private Result<?> jooqResult;

	// ==================== Row Building Tests ====================

	@Test
	@DisplayName("Should build reimbursement rows with correct context fields")
	void testBuildReimbursementExportRowsWithContext() {
		// Given
		List<Integer> timesheetIds = ReimbursementExportTestDataFactory.createTimesheetIds();
		List<TimesheetReimbursement> reimbursements = ReimbursementExportTestDataFactory
			.createTwoApprovedReimbursements();
		Map<Integer, DynamicExportResponseBodyDto> contextMap = ReimbursementExportTestDataFactory
			.createTimesheetContextMap();

		given(this.reimbursementJpaRepository.findAllByTimesheetIdInAndAccountIdAndStatusOrderByTimesheetIdAscIdAsc(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementConstants.STATUS_APPROVED))
			.willReturn(reimbursements);

		stubCurrencyQuery(Map.of(ReimbursementExportTestDataFactory.TEST_CURRENCY_ID_USD,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_SYMBOL_USD,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_ID_EUR,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_SYMBOL_EUR));

		// When
		List<ReimbursementExportRowDto> rows = this.reimbursementExportService
			.buildReimbursementExportRows(timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID, contextMap);

		// Then
		assertThat(rows).hasSize(2);

		ReimbursementExportRowDto firstRow = rows.get(0);
		assertThat(firstRow.getTimesheetId())
			.isEqualTo(String.valueOf(ReimbursementExportTestDataFactory.TEST_TIMESHEET_ID_1));
		assertThat(firstRow.getTimesheetPeriod()).isEqualTo(ReimbursementExportTestDataFactory.TEST_PERIOD);
		assertThat(firstRow.getContractorName()).isEqualTo(ReimbursementExportTestDataFactory.TEST_CONTRACTOR);
		assertThat(firstRow.getJobName()).isEqualTo(ReimbursementExportTestDataFactory.TEST_JOB);
		assertThat(firstRow.getCompanyName()).isEqualTo(ReimbursementExportTestDataFactory.TEST_COMPANY);
		assertThat(firstRow.getJobDuration()).isEqualTo(ReimbursementExportTestDataFactory.TEST_DURATION);
		assertThat(firstRow.getReimbursementDescription())
			.isEqualTo(ReimbursementExportTestDataFactory.TEST_DESCRIPTION_1);
		assertThat(firstRow.getAmount()).isEqualByComparingTo(ReimbursementExportTestDataFactory.TEST_AMOUNT_1);
		assertThat(firstRow.getCurrencySymbol()).isEqualTo(ReimbursementExportTestDataFactory.TEST_CURRENCY_SYMBOL_USD);
		assertThat(firstRow.getPayable()).isEqualTo("Yes");
		assertThat(firstRow.getBillable()).isEqualTo("No");

		then(this.reimbursementJpaRepository).should()
			.findAllByTimesheetIdInAndAccountIdAndStatusOrderByTimesheetIdAscIdAsc(timesheetIds,
					ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID, ReimbursementConstants.STATUS_APPROVED);
	}

	@Test
	@DisplayName("Should return empty list when no reimbursements found")
	void testBuildReimbursementExportRowsNoReimbursements() {
		// Given
		List<Integer> timesheetIds = ReimbursementExportTestDataFactory.createTimesheetIds();
		given(this.reimbursementJpaRepository.findAllByTimesheetIdInAndAccountIdAndStatusOrderByTimesheetIdAscIdAsc(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementConstants.STATUS_APPROVED))
			.willReturn(new ArrayList<>());

		// When
		List<ReimbursementExportRowDto> rows = this.reimbursementExportService.buildReimbursementExportRows(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID, new HashMap<>());

		// Then
		assertThat(rows).isEmpty();
	}

	// ==================== Context Resolution Tests ====================

	@Test
	@DisplayName("Should return empty strings when context map is empty")
	void testBuildReimbursementExportRowsEmptyContextMap() {
		// Given
		List<Integer> timesheetIds = ReimbursementExportTestDataFactory.createTimesheetIds();
		List<TimesheetReimbursement> reimbursements = List.of(ReimbursementExportTestDataFactory.createReimbursement(
				ReimbursementExportTestDataFactory.TEST_TIMESHEET_ID_1,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_ID_USD,
				ReimbursementExportTestDataFactory.TEST_DESCRIPTION_1, ReimbursementExportTestDataFactory.TEST_AMOUNT_1,
				1, 1, 2));

		given(this.reimbursementJpaRepository.findAllByTimesheetIdInAndAccountIdAndStatusOrderByTimesheetIdAscIdAsc(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementConstants.STATUS_APPROVED))
			.willReturn(reimbursements);

		stubCurrencyQuery(Map.of(ReimbursementExportTestDataFactory.TEST_CURRENCY_ID_USD,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_SYMBOL_USD));

		// When
		List<ReimbursementExportRowDto> rows = this.reimbursementExportService.buildReimbursementExportRows(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementExportTestDataFactory.createEmptyContextMap());

		// Then
		assertThat(rows).hasSize(1);
		ReimbursementExportRowDto row = rows.get(0);
		assertThat(row.getTimesheetPeriod()).isEmpty();
		assertThat(row.getContractorName()).isEmpty();
		assertThat(row.getJobName()).isEmpty();
		assertThat(row.getCompanyName()).isEmpty();
		assertThat(row.getJobDuration()).isEmpty();
		assertThat(row.getReimbursementDescription()).isEqualTo(ReimbursementExportTestDataFactory.TEST_DESCRIPTION_1);
	}

	@Test
	@DisplayName("Should return empty strings when context dto has null data map")
	void testBuildReimbursementExportRowsContextWithNullDataMap() {
		// Given
		List<Integer> timesheetIds = List.of(ReimbursementExportTestDataFactory.TEST_TIMESHEET_ID_1);
		List<TimesheetReimbursement> reimbursements = List.of(ReimbursementExportTestDataFactory.createReimbursement(
				ReimbursementExportTestDataFactory.TEST_TIMESHEET_ID_1,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_ID_USD,
				ReimbursementExportTestDataFactory.TEST_DESCRIPTION_1, ReimbursementExportTestDataFactory.TEST_AMOUNT_1,
				0, 0, 2));

		given(this.reimbursementJpaRepository.findAllByTimesheetIdInAndAccountIdAndStatusOrderByTimesheetIdAscIdAsc(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementConstants.STATUS_APPROVED))
			.willReturn(reimbursements);

		stubCurrencyQuery(Map.of(ReimbursementExportTestDataFactory.TEST_CURRENCY_ID_USD,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_SYMBOL_USD));

		DynamicExportResponseBodyDto nullDataDto = new DynamicExportResponseBodyDto();
		nullDataDto.setData(null);
		Map<Integer, DynamicExportResponseBodyDto> contextMap = new HashMap<>();
		contextMap.put(ReimbursementExportTestDataFactory.TEST_TIMESHEET_ID_1, nullDataDto);

		// When
		List<ReimbursementExportRowDto> rows = this.reimbursementExportService
			.buildReimbursementExportRows(timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID, contextMap);

		// Then
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getTimesheetPeriod()).isEmpty();
		assertThat(rows.get(0).getContractorName()).isEmpty();
	}

	// ==================== Payable / Billable Tests ====================

	@Test
	@DisplayName("Should map isPayable=1 to Yes and isPayable=0 to No")
	void testPayableBillableMapping() {
		// Given
		List<Integer> timesheetIds = ReimbursementExportTestDataFactory.createTimesheetIds();
		List<TimesheetReimbursement> reimbursements = ReimbursementExportTestDataFactory
			.createTwoApprovedReimbursements();

		given(this.reimbursementJpaRepository.findAllByTimesheetIdInAndAccountIdAndStatusOrderByTimesheetIdAscIdAsc(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementConstants.STATUS_APPROVED))
			.willReturn(reimbursements);

		stubCurrencyQuery(Map.of(ReimbursementExportTestDataFactory.TEST_CURRENCY_ID_USD,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_SYMBOL_USD,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_ID_EUR,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_SYMBOL_EUR));

		// When
		List<ReimbursementExportRowDto> rows = this.reimbursementExportService.buildReimbursementExportRows(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementExportTestDataFactory.createTimesheetContextMap());

		// Then
		assertThat(rows.get(0).getPayable()).isEqualTo("Yes");
		assertThat(rows.get(0).getBillable()).isEqualTo("No");
		assertThat(rows.get(1).getPayable()).isEqualTo("No");
		assertThat(rows.get(1).getBillable()).isEqualTo("Yes");
	}

	@Test
	@DisplayName("Should map null isPayable and isBillable to No")
	void testNullPayableBillableMapping() {
		// Given
		List<Integer> timesheetIds = List.of(ReimbursementExportTestDataFactory.TEST_TIMESHEET_ID_1);
		TimesheetReimbursement reimbursement = ReimbursementExportTestDataFactory.createReimbursement(
				ReimbursementExportTestDataFactory.TEST_TIMESHEET_ID_1,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_ID_USD,
				ReimbursementExportTestDataFactory.TEST_DESCRIPTION_1, ReimbursementExportTestDataFactory.TEST_AMOUNT_1,
				0, 0, 2);
		reimbursement.setIsPayable(null);
		reimbursement.setIsBillable(null);

		given(this.reimbursementJpaRepository.findAllByTimesheetIdInAndAccountIdAndStatusOrderByTimesheetIdAscIdAsc(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementConstants.STATUS_APPROVED))
			.willReturn(List.of(reimbursement));

		stubCurrencyQuery(Map.of(ReimbursementExportTestDataFactory.TEST_CURRENCY_ID_USD,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_SYMBOL_USD));

		// When
		List<ReimbursementExportRowDto> rows = this.reimbursementExportService.buildReimbursementExportRows(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementExportTestDataFactory.createEmptyContextMap());

		// Then
		assertThat(rows.get(0).getPayable()).isEqualTo("No");
		assertThat(rows.get(0).getBillable()).isEqualTo("No");
	}

	// ==================== Currency Symbol Tests ====================

	@Test
	@DisplayName("Should resolve currency symbols from different currencies")
	void testCurrencySymbolResolutionMultipleCurrencies() {
		// Given
		List<Integer> timesheetIds = ReimbursementExportTestDataFactory.createTimesheetIds();
		List<TimesheetReimbursement> reimbursements = ReimbursementExportTestDataFactory
			.createTwoApprovedReimbursements();

		given(this.reimbursementJpaRepository.findAllByTimesheetIdInAndAccountIdAndStatusOrderByTimesheetIdAscIdAsc(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementConstants.STATUS_APPROVED))
			.willReturn(reimbursements);

		stubCurrencyQuery(Map.of(ReimbursementExportTestDataFactory.TEST_CURRENCY_ID_USD,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_SYMBOL_USD,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_ID_EUR,
				ReimbursementExportTestDataFactory.TEST_CURRENCY_SYMBOL_EUR));

		// When
		List<ReimbursementExportRowDto> rows = this.reimbursementExportService.buildReimbursementExportRows(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementExportTestDataFactory.createTimesheetContextMap());

		// Then
		assertThat(rows.get(0).getCurrencySymbol())
			.isEqualTo(ReimbursementExportTestDataFactory.TEST_CURRENCY_SYMBOL_USD);
		assertThat(rows.get(1).getCurrencySymbol())
			.isEqualTo(ReimbursementExportTestDataFactory.TEST_CURRENCY_SYMBOL_EUR);
	}

	@Test
	@DisplayName("Should return empty currency symbol when currency ID is not found")
	void testCurrencySymbolFallbackForUnknownCurrency() {
		// Given
		List<Integer> timesheetIds = List.of(ReimbursementExportTestDataFactory.TEST_TIMESHEET_ID_1);
		TimesheetReimbursement reimbursement = ReimbursementExportTestDataFactory.createReimbursement(
				ReimbursementExportTestDataFactory.TEST_TIMESHEET_ID_1, 999, "Unknown currency expense",
				ReimbursementExportTestDataFactory.TEST_AMOUNT_1, 1, 0, 2);

		given(this.reimbursementJpaRepository.findAllByTimesheetIdInAndAccountIdAndStatusOrderByTimesheetIdAscIdAsc(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementConstants.STATUS_APPROVED))
			.willReturn(List.of(reimbursement));

		stubCurrencyQuery(Map.of());

		// When
		List<ReimbursementExportRowDto> rows = this.reimbursementExportService.buildReimbursementExportRows(
				timesheetIds, ReimbursementExportTestDataFactory.TEST_ACCOUNT_ID,
				ReimbursementExportTestDataFactory.createEmptyContextMap());

		// Then
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getCurrencySymbol()).isEmpty();
	}

	// ==================== Helper ====================

	@SuppressWarnings("unchecked")
	private void stubCurrencyQuery(Map<Integer, String> currencyMap) {
		doReturn(this.selectStep).when(this.dslContext).select(any(), any());
		doReturn(this.joinStep).when(this.selectStep).from(any(Tblcurrency.class));
		doReturn(this.conditionStep).when(this.joinStep).where(any(org.jooq.Condition.class));
		doReturn(this.jooqResult).when(this.conditionStep).fetch();
		willAnswer((invocation) -> {
			Consumer<org.jooq.Record2<Integer, String>> consumer = invocation.getArgument(0);
			currencyMap.forEach((id, symbol) -> {
				@SuppressWarnings("unchecked")
				org.jooq.Record2<Integer, String> rec = mock(org.jooq.Record2.class);
				willAnswer((fieldInv) -> {
					org.jooq.Field<?> field = fieldInv.getArgument(0);
					if ("id".equals(field.getName())) {
						return id;
					}
					return symbol;
				}).given(rec).get(any(org.jooq.Field.class));
				consumer.accept(rec);
			});
			return null;
		}).given(this.jooqResult).forEach(any());
	}

}
