package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursement;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ReimbursementExportRowDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test data factory for reimbursement export tests.
 */
public final class ReimbursementExportTestDataFactory {

	private ReimbursementExportTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static final Integer TEST_ACCOUNT_ID = 1;

	public static final Integer TEST_TIMESHEET_ID_1 = 100;

	public static final Integer TEST_TIMESHEET_ID_2 = 200;

	public static final Integer TEST_CURRENCY_ID_USD = 1;

	public static final Integer TEST_CURRENCY_ID_EUR = 2;

	public static final String TEST_CURRENCY_SYMBOL_USD = "$";

	public static final String TEST_CURRENCY_SYMBOL_EUR = "€";

	public static final String TEST_PERIOD = "01/01/2024 - 01/15/2024";

	public static final String TEST_CONTRACTOR = "John Doe";

	public static final String TEST_JOB = "Software Engineer";

	public static final String TEST_COMPANY = "Acme Corp";

	public static final String TEST_DURATION = "6 months";

	public static final String TEST_DESCRIPTION_1 = "Travel expense";

	public static final String TEST_DESCRIPTION_2 = "Office supplies";

	public static final BigDecimal TEST_AMOUNT_1 = new BigDecimal("120.50");

	public static final BigDecimal TEST_AMOUNT_2 = new BigDecimal("45.00");

	public static TimesheetReimbursement createReimbursement(Integer timesheetId, Integer currencyId,
			String description, BigDecimal amount, int isPayable, int isBillable, int status) {
		TimesheetReimbursement r = new TimesheetReimbursement();
		r.setId(1);
		r.setTimesheetId(timesheetId);
		r.setCurrencyId(currencyId);
		r.setDescription(description);
		r.setAmount(amount);
		r.setIsPayable(isPayable);
		r.setIsBillable(isBillable);
		r.setStatus(status);
		r.setAccountId(TEST_ACCOUNT_ID);
		r.setAddedBy(1);
		r.setAddedByUserTypeId(1);
		r.setAddedOn(1700000000);
		r.setUpdatedBy(1);
		r.setUpdatedByUserTypeId(1);
		r.setUpdatedOn(1700000000);
		return r;
	}

	public static List<TimesheetReimbursement> createTwoApprovedReimbursements() {
		List<TimesheetReimbursement> list = new ArrayList<>();
		list.add(createReimbursement(TEST_TIMESHEET_ID_1, TEST_CURRENCY_ID_USD, TEST_DESCRIPTION_1, TEST_AMOUNT_1, 1, 0,
				2));
		list.add(createReimbursement(TEST_TIMESHEET_ID_2, TEST_CURRENCY_ID_EUR, TEST_DESCRIPTION_2, TEST_AMOUNT_2, 0, 1,
				2));
		return list;
	}

	public static Map<Integer, DynamicExportResponseBodyDto> createTimesheetContextMap() {
		Map<Integer, DynamicExportResponseBodyDto> map = new HashMap<>();
		map.put(TEST_TIMESHEET_ID_1, createContextDto(TEST_PERIOD, TEST_CONTRACTOR, TEST_JOB, TEST_COMPANY,
				TEST_DURATION, TEST_TIMESHEET_ID_1));
		map.put(TEST_TIMESHEET_ID_2, createContextDto(TEST_PERIOD, "Jane Smith", "QA Engineer", "Beta Inc", "3 months",
				TEST_TIMESHEET_ID_2));
		return map;
	}

	public static Map<Integer, DynamicExportResponseBodyDto> createEmptyContextMap() {
		return new HashMap<>();
	}

	public static DynamicExportResponseBodyDto createContextDto(String period, String contractor, String job,
			String company, String duration, Integer timesheetId) {
		Map<String, Object> data = new HashMap<>();
		data.put("timesheetPeriod", period);
		data.put("candidatename", contractor);
		data.put("jobName", job);
		data.put("timesheetCompany", company);
		data.put("jobDuration", duration);
		data.put("timesheet", timesheetId);
		return new DynamicExportResponseBodyDto(data, new ArrayList<>(data.keySet()));
	}

	public static List<Integer> createTimesheetIds() {
		return List.of(TEST_TIMESHEET_ID_1, TEST_TIMESHEET_ID_2);
	}

	public static List<ReimbursementExportRowDto> createReimbursementExportRows() {
		List<ReimbursementExportRowDto> rows = new ArrayList<>();
		rows.add(ReimbursementExportRowDto.builder()
			.timesheetId(String.valueOf(TEST_TIMESHEET_ID_1))
			.timesheetPeriod(TEST_PERIOD)
			.contractorName(TEST_CONTRACTOR)
			.jobName(TEST_JOB)
			.companyName(TEST_COMPANY)
			.jobDuration(TEST_DURATION)
			.reimbursementDescription(TEST_DESCRIPTION_1)
			.amount(TEST_AMOUNT_1)
			.currencySymbol(TEST_CURRENCY_SYMBOL_USD)
			.payable("Yes")
			.billable("No")
			.status("Approved")
			.build());
		rows.add(ReimbursementExportRowDto.builder()
			.timesheetId(String.valueOf(TEST_TIMESHEET_ID_2))
			.timesheetPeriod(TEST_PERIOD)
			.contractorName("Jane Smith")
			.jobName("QA Engineer")
			.companyName("Beta Inc")
			.jobDuration("3 months")
			.reimbursementDescription(TEST_DESCRIPTION_2)
			.amount(TEST_AMOUNT_2)
			.currencySymbol(TEST_CURRENCY_SYMBOL_EUR)
			.payable("No")
			.billable("Yes")
			.status("Approved")
			.build());
		return rows;
	}

	public static ReimbursementExportRowDto createSingleExportRow() {
		return ReimbursementExportRowDto.builder()
			.timesheetId(String.valueOf(TEST_TIMESHEET_ID_1))
			.timesheetPeriod(TEST_PERIOD)
			.contractorName(TEST_CONTRACTOR)
			.jobName(TEST_JOB)
			.companyName(TEST_COMPANY)
			.jobDuration(TEST_DURATION)
			.reimbursementDescription(TEST_DESCRIPTION_1)
			.amount(TEST_AMOUNT_1)
			.currencySymbol(TEST_CURRENCY_SYMBOL_USD)
			.payable("Yes")
			.billable("No")
			.status("Approved")
			.build();
	}

	public static ReimbursementExportRowDto createExportRowWithNullAmount() {
		return ReimbursementExportRowDto.builder()
			.timesheetId(String.valueOf(TEST_TIMESHEET_ID_1))
			.timesheetPeriod(TEST_PERIOD)
			.contractorName(TEST_CONTRACTOR)
			.jobName(TEST_JOB)
			.companyName(TEST_COMPANY)
			.jobDuration(TEST_DURATION)
			.reimbursementDescription(TEST_DESCRIPTION_1)
			.amount(null)
			.currencySymbol(null)
			.payable("Yes")
			.billable("No")
			.status("Approved")
			.build();
	}

	public static ReimbursementExportRowDto createExportRowWithEmptyCurrencySymbol() {
		return ReimbursementExportRowDto.builder()
			.timesheetId(String.valueOf(TEST_TIMESHEET_ID_1))
			.reimbursementDescription(TEST_DESCRIPTION_1)
			.amount(TEST_AMOUNT_1)
			.currencySymbol("")
			.payable("No")
			.billable("No")
			.status("Pending")
			.build();
	}

}
