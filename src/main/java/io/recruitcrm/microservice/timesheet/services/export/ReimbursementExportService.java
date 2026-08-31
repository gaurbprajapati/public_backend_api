package io.recruitcrm.microservice.timesheet.services.export;

import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursement;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcurrency;
import io.recruitcrm.microservice.timesheet.dao.reimbursement.TimesheetReimbursementJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ReimbursementExportRowDto;
import io.recruitcrm.microservice.timesheet.helpers.constants.ReimbursementConstants;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Assembles reimbursement rows for the "Reimbursements" sheet in the export spreadsheet.
 * Only APPROVED reimbursements (status = 2) are included, ordered by timesheetId then
 * reimbursementId. Enriches each row with timesheet-level context and resolves currency
 * symbols for the amount column.
 */
@Service
public class ReimbursementExportService implements IReimbursementExportService {

	private static final String FIELD_TIMESHEET_ID = "timesheetId";

	private static final String FIELD_TIMESHEET_PERIOD = "timesheetPeriod";

	private static final String FIELD_CANDIDATE_NAME = "candidatename";

	private static final String FIELD_JOB_NAME = "jobName";

	private static final String FIELD_COMPANY_NAME = "timesheetCompany";

	private static final String FIELD_JOB_DURATION = "jobDuration";

	private static final Tblcurrency CURRENCY = Tblcurrency.TBLCURRENCY;

	private final TimesheetReimbursementJpaRepository reimbursementJpaRepository;

	private final DSLContext dslContext;

	public ReimbursementExportService(TimesheetReimbursementJpaRepository reimbursementJpaRepository,
			DSLContext dslContext) {
		this.reimbursementJpaRepository = reimbursementJpaRepository;
		this.dslContext = dslContext;
	}

	@Override
	public List<ReimbursementExportRowDto> buildReimbursementExportRows(List<Integer> timesheetIds, Integer accountId,
			Map<Integer, DynamicExportResponseBodyDto> timesheetContextMap) {

		List<TimesheetReimbursement> reimbursements = this.reimbursementJpaRepository
			.findAllByTimesheetIdInAndAccountIdAndStatusOrderByTimesheetIdAscIdAsc(timesheetIds, accountId,
					ReimbursementConstants.STATUS_APPROVED);

		Map<Integer, String> currencySymbols = this.fetchCurrencySymbols(reimbursements);

		List<ReimbursementExportRowDto> rows = new ArrayList<>(reimbursements.size());

		for (TimesheetReimbursement r : reimbursements) {
			DynamicExportResponseBodyDto tsContext = timesheetContextMap.get(r.getTimesheetId());

			rows.add(ReimbursementExportRowDto.builder()
				.timesheetId(resolveContextField(tsContext, FIELD_TIMESHEET_ID))
				.timesheetPeriod(resolveContextField(tsContext, FIELD_TIMESHEET_PERIOD))
				.contractorName(resolveContextField(tsContext, FIELD_CANDIDATE_NAME))
				.jobName(resolveContextField(tsContext, FIELD_JOB_NAME))
				.companyName(resolveContextField(tsContext, FIELD_COMPANY_NAME))
				.jobDuration(resolveContextField(tsContext, FIELD_JOB_DURATION))
				.reimbursementDescription(r.getDescription())
				.amount(r.getAmount())
				.currencySymbol(currencySymbols.getOrDefault(r.getCurrencyId(), ""))
				.payable((r.getIsPayable() != null && r.getIsPayable() == 1) ? "Yes" : "No")
				.billable((r.getIsBillable() != null && r.getIsBillable() == 1) ? "Yes" : "No")
				.status(ReimbursementConstants.getStatusLabel(r.getStatus()))
				.build());
		}

		return rows;
	}

	/**
	 * Batch-fetches currency symbols for all distinct currency IDs found in the
	 * reimbursements, avoiding N+1 queries.
	 */
	private Map<Integer, String> fetchCurrencySymbols(List<TimesheetReimbursement> reimbursements) {
		Set<Integer> currencyIds = reimbursements.stream()
			.map(TimesheetReimbursement::getCurrencyId)
			.collect(Collectors.toSet());

		if (currencyIds.isEmpty()) {
			return Map.of();
		}

		Map<Integer, String> symbolMap = new HashMap<>();
		this.dslContext.select(CURRENCY.ID, CURRENCY.SYMBOL)
			.from(CURRENCY)
			.where(CURRENCY.ID.in(currencyIds))
			.fetch()
			.forEach((row) -> symbolMap.put(row.get(CURRENCY.ID), row.get(CURRENCY.SYMBOL)));

		return symbolMap;
	}

	private static String resolveContextField(DynamicExportResponseBodyDto context, String fieldName) {
		if (context == null || context.getData() == null) {
			return "";
		}
		Object value = context.getData().get(fieldName);
		return (value != null) ? value.toString() : "";
	}

}
