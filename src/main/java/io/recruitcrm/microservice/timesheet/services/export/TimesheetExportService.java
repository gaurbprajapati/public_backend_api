package io.recruitcrm.microservice.timesheet.services.export;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ExportResult;
import io.recruitcrm.microservice.timesheet.dto.export.FileFormat;
import io.recruitcrm.microservice.timesheet.dto.export.ReimbursementExportRowDto;
import io.recruitcrm.microservice.timesheet.dto.export.TimesheetTotalsQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.export.PeriodGroupedExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;

import io.recruitcrm.microservice.timesheet.helpers.ExportTimeFormatHelper;
import io.recruitcrm.microservice.timesheet.repositories.export.ITimesheetExportRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.ITimeLogRepository;
import io.recruitcrm.microservice.timesheet.repositories.user.UserRepository;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Optimized timesheet export service that avoids duplicate database calls by returning
 * both file content and suggested filename in a single operation.
 */
@Service
@Transactional(readOnly = true)
public class TimesheetExportService implements ITimesheetExportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimesheetExportService.class);

	// Constants for duplicated literals
	private static final String WORK_HOURS_SUFFIX = ", Work Hours";

	private static final String BREAK_INTERVALS_SUFFIX = ", Break Intervals";

	private static final String REMARKS_SUFFIX = ", Remarks";

	private static final String OVERTIME_HOURS_SUFFIX = ", Overtime Hours";

	private static final String FIELD_TIMESHEET_ID = "timesheetId";

	private static final String FIELD_CANDIDATE_NAME = "candidatename";

	private static final String FIELD_TIMESHEET_PERIOD = "timesheetPeriod";

	private static final String FIELD_BREAK_INTERVALS = "breakIntervals";

	private static final String FIELD_TIME_LOG_REMARKS = "timeLogRemarks";

	private static final String FIELD_TOTAL_OVERTIME = "totalOvertime";

	private static final String FIELD_TOTAL_WORK_TIME = "totalWorkTime";

	private static final String FIELD_TOTAL_REGULAR_HOURS = "totalRegularHours";

	private static final String FIELD_TOTAL_TIME = "totalTime";

	private static final String FIELD_WORK_DAYS = "workDays";

	private static final String FIELD_RESOURCE_URL = "resource_url";

	private static final String FIELD_OWNER_ID = "ownerid";

	private static final String FIELD_CREATED_BY = "createdby";

	private static final String FIELD_UPDATED_BY = "updatedby";

	private static final String FIELD_WORK_HOURS = "workHours";

	private static final String FIELD_OVERTIME_HOURS = "overtimeHours";

	private static final String FIELD_EFFECTIVE_WORK_HOURS = "effectiveWorkHours";

	private static final String FIELD_TIMESHEET = "timesheet";

	private static final String FIELD_CONTRACTOR = "contractor";

	private static final String FIELD_JOB_NAME = "jobName";

	private static final String FIELD_COMPANY_NAME = "timesheetCompany";

	private static final String FIELD_JOB_DURATION = "jobDuration";

	/**
	 * Canonical export sequence for the whole-timesheet aggregate total columns. When
	 * more than one of these is selected they are rendered as a contiguous block in this
	 * order: Total Work Hours -> Total Regular Hours -> Total Overtime Hours -> Total
	 * Hours.
	 */
	private static final List<String> TOTALS_CANONICAL_ORDER = List.of(FIELD_TOTAL_WORK_TIME, FIELD_TOTAL_REGULAR_HOURS,
			FIELD_TOTAL_OVERTIME, FIELD_TOTAL_TIME);

	private static final List<String> REIMBURSEMENT_CONTEXT_TS_FIELDS = List.of(FIELD_TIMESHEET_ID,
			FIELD_TIMESHEET_PERIOD, FIELD_JOB_NAME, FIELD_COMPANY_NAME, FIELD_JOB_DURATION);

	private static final List<String> REIMBURSEMENT_CONTEXT_CANDIDATE_FIELDS = List.of(FIELD_CANDIDATE_NAME);

	private final ITimesheetExportRepository timesheetExportRepository;

	private final ITimeLogRepository timeLogRepository;

	private final ExportFieldRegistry fieldRegistry;

	private final ITimesheetFileGeneratorService timesheetFileGeneratorService;

	private final IReimbursementExportService reimbursementExportService;

	private final AuthHolder authHolder;

	private final WorkDaysConverter workDaysConverter;

	private final UserRepository userRepository;

	private final CustomColumnTypeService customColumnTypeService;

	@Value("${application.env}")
	private String applicationEnv;

	// Cache for user details to avoid repeated database calls
	private final Map<Integer, String> userNameCache = new ConcurrentHashMap<>();

	public TimesheetExportService(ITimesheetExportRepository timesheetExportRepository,
			ITimeLogRepository timeLogRepository, ExportFieldRegistry fieldRegistry,
			ITimesheetFileGeneratorService timesheetFileGeneratorService,
			IReimbursementExportService reimbursementExportService, AuthHolder authHolder,
			WorkDaysConverter workDaysConverter, UserRepository userRepository,
			CustomColumnTypeService customColumnTypeService) {
		this.timesheetExportRepository = timesheetExportRepository;
		this.timeLogRepository = timeLogRepository;
		this.fieldRegistry = fieldRegistry;
		this.timesheetFileGeneratorService = timesheetFileGeneratorService;
		this.reimbursementExportService = reimbursementExportService;
		this.authHolder = authHolder;
		this.workDaysConverter = workDaysConverter;
		this.userRepository = userRepository;
		this.customColumnTypeService = customColumnTypeService;
	}

	/**
	 * Export data and return both file resource and suggested filename in a single
	 * operation. This avoids duplicate database calls for filename generation.
	 */
	public ExportResult exportDataWithFilename(DynamicExportRequestBodyDto request) {

		this.validateMandatoryExportColumns(request);

		// 1. Validate requested fields
		this.fieldRegistry.validateFields(request.getSelectedFields());

		// 2. Get account ID from auth context
		Integer accountId = this.authHolder.getAuthenticationPrincipalOrganizationIdentifier();

		// 3. Resolve user time format preference (0=12h AM/PM, 1=24h, null=default 24h)
		boolean use12HourFormat = false;
		try {
			Integer userId = this.authHolder.getAuthenticationPrincipalUniqueIdentifier();
			Integer timeFormatType = (userId != null) ? this.userRepository.getTimeFormatTypeByUserId(userId) : null;
			use12HourFormat = (timeFormatType != null && timeFormatType == 0);
		}
		catch (Exception ex) {
			// Default to 24-hour if user not found or not a system user
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			LOGGER.error("exception occurred inside exportDataWithFilename() method :: {}", sw);
		}

		// 4. Export data and generate filename based on request type
		if (request.isExportEachDay()) {
			return this.exportPeriodGroupedData(request, accountId, use12HourFormat);
		}
		else {
			return this.exportRegularData(request, accountId);
		}
	}

	/**
	 * Export data grouped by periods and generate period-based filename
	 */
	private ExportResult exportPeriodGroupedData(DynamicExportRequestBodyDto request, Integer accountId,
			boolean use12HourFormat) {

		// Get grouped data from database (single call)
		List<PeriodGroupedExportResponseBodyDto> groupedData = this.timesheetExportRepository
			.getExportDataGroupedByPeriods(request, accountId);

		if (groupedData.isEmpty()) {
			throw new ResourceNotFoundException("No data found for the specified criteria");
		}

		// Apply post-processing to all timesheets in grouped data
		for (PeriodGroupedExportResponseBodyDto period : groupedData) {
			this.applyPostProcessing(period.getTimesheetsInPeriod(), request.getSelectedFields());
		}

		// Batch fetch all enhancement data for the entire export range in single queries,
		// then apply in-memory per period to avoid N+1 database roundtrips
		List<Integer> allTimesheetIds = this.extractAllTimesheetIdsFromGroupedData(groupedData);

		if (!allTimesheetIds.isEmpty()) {
			// Enhance with aggregate totals if requested (single batch fetch)
			this.enhanceGroupedDataWithAggregateTotalsBatch(groupedData, request.getSelectedFields(), accountId,
					allTimesheetIds);

			// Enhance with work hours if requested (single batch fetch)
			if (request.getSelectedFields().contains(FIELD_WORK_HOURS)) {
				this.enhanceGroupedDataWithWorkHoursBatch(groupedData, allTimesheetIds, accountId, use12HourFormat);
			}

			// Enhance with overtime hours data if requested (single batch fetch)
			if (request.getSelectedFields().contains(FIELD_OVERTIME_HOURS)) {
				this.enhanceGroupedDataWithOvertimeHoursBatch(groupedData, allTimesheetIds, accountId);
			}

			// Enhance with effective work hours data if requested (single batch fetch)
			if (request.getSelectedFields().contains(FIELD_EFFECTIVE_WORK_HOURS)) {
				this.enhanceGroupedDataWithEffectiveWorkHoursBatch(groupedData, allTimesheetIds, accountId);
			}

			// Enhance with break intervals if requested (single batch fetch)
			if (request.getSelectedFields().contains(FIELD_BREAK_INTERVALS)) {
				this.enhanceGroupedDataWithBreakIntervalsBatch(groupedData, allTimesheetIds, accountId,
						use12HourFormat);
			}

			// Enhance with time log remarks if requested (single batch fetch)
			if (request.getSelectedFields().contains(FIELD_TIME_LOG_REMARKS)) {
				this.enhanceGroupedDataWithRemarksBatch(groupedData, allTimesheetIds, accountId);
			}
		}

		// Enforce canonical sequence for the aggregate total columns before per-day
		// ordering so the reordered totals feed into the fixed column ordering
		for (PeriodGroupedExportResponseBodyDto period : groupedData) {
			this.applyTotalsColumnOrdering(period.getTimesheetsInPeriod());
		}

		// Apply proper column ordering for time-based fields when exportEachDay is
		// true
		this.applyTimeFieldColumnOrdering(groupedData, request.getSelectedFields());

		// Extract filename from the first period (already loaded data)
		String suggestedFilename = this.sanitizeFilename(groupedData.get(0).getPeriodDisplayName());

		// Calculate total record count
		long totalRecords = groupedData.stream().mapToLong(PeriodGroupedExportResponseBodyDto::getTimesheetCount).sum();

		return this.buildExportResultWithReimbursements(request, accountId, null, groupedData, allTimesheetIds,
				new ExportMetadata(suggestedFilename, totalRecords, true));
	}

	/**
	 * Export regular data without grouping
	 */
	private ExportResult exportRegularData(DynamicExportRequestBodyDto request, Integer accountId) {

		// Get regular export data from database (single call)
		List<DynamicExportResponseBodyDto> exportData = this.timesheetExportRepository.getExportData(request,
				accountId);

		if (exportData.isEmpty()) {
			throw new ResourceNotFoundException("No data found for the specified criteria");
		}

		// Apply post-processing to fields that require it (e.g., work_days conversion)
		this.applyPostProcessing(exportData, request.getSelectedFields());

		// Enhance with aggregate totals if requested (works for both exportEachDay
		// true/false)
		this.enhanceWithAggregateTotals(exportData, request.getSelectedFields(), accountId);

		// Enforce canonical sequence for the aggregate total columns
		this.applyTotalsColumnOrdering(exportData);

		List<Integer> timesheetIds = this.extractTimesheetIds(exportData);
		String suggestedFilename = "export_data";

		return this.buildExportResultWithReimbursements(request, accountId, exportData, null, timesheetIds,
				new ExportMetadata(suggestedFilename, exportData.size(), false));
	}

	/**
	 * Decides whether to include reimbursement data in the export based on the request's
	 * includeReimbursements flag. When true and approved reimbursements exist: for Excel,
	 * adds a "Reimbursements" sheet; for CSV, produces a ZIP with timesheets CSV(s) +
	 * reimbursements.csv. Empty reimbursement list = no sheet added.
	 */
	private ExportResult buildExportResultWithReimbursements(DynamicExportRequestBodyDto request, Integer accountId,
			List<DynamicExportResponseBodyDto> flatData, List<PeriodGroupedExportResponseBodyDto> groupedData,
			List<Integer> timesheetIds, ExportMetadata metadata) {

		boolean isGrouped = (groupedData != null);

		if (!request.isIncludeReimbursements()) {
			ByteArrayResource resource = isGrouped
					? this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request)
					: this.timesheetFileGeneratorService.generateFile(flatData, request);
			return new ExportResult(resource, metadata.suggestedFilename(), metadata.recordCount(),
					metadata.periodGrouped());
		}

		Map<Integer, DynamicExportResponseBodyDto> contextMap = this.fetchReimbursementContextMap(timesheetIds,
				accountId);
		List<ReimbursementExportRowDto> reimbursementRows = this.reimbursementExportService
			.buildReimbursementExportRows(timesheetIds, accountId, contextMap);

		ByteArrayResource resource = isGrouped
				? this.generateGroupedWithReimbursements(groupedData, request, reimbursementRows)
				: this.generateFlatWithReimbursements(flatData, request, reimbursementRows);

		return new ExportResult(resource, metadata.suggestedFilename(), metadata.recordCount(),
				metadata.periodGrouped());
	}

	private ByteArrayResource generateFlatWithReimbursements(List<DynamicExportResponseBodyDto> data,
			DynamicExportRequestBodyDto request, List<ReimbursementExportRowDto> reimbursements) {

		return switch (request.getFileFormat()) {
			case EXCEL ->
				this.timesheetFileGeneratorService.generateExcelWithReimbursements(data, request, reimbursements);
			case CSV ->
				this.timesheetFileGeneratorService.generateCsvWithReimbursementsZip(data, request, reimbursements);
		};
	}

	private ByteArrayResource generateGroupedWithReimbursements(List<PeriodGroupedExportResponseBodyDto> groupedData,
			DynamicExportRequestBodyDto request, List<ReimbursementExportRowDto> reimbursements) {

		return switch (request.getFileFormat()) {
			case EXCEL -> this.timesheetFileGeneratorService.generateGroupedExcelWithReimbursements(groupedData,
					request, reimbursements);
			case CSV -> this.timesheetFileGeneratorService.generateGroupedCsvWithReimbursementsZip(groupedData, request,
					reimbursements);
		};
	}

	/**
	 * Fetches the context fields needed for the reimbursement sheet directly from the DB.
	 * This ensures period, contractor, job, company, and duration are always populated
	 * regardless of which fields the user selected for the main export.
	 */
	private Map<Integer, DynamicExportResponseBodyDto> fetchReimbursementContextMap(List<Integer> timesheetIds,
			Integer accountId) {

		DynamicExportRequestBodyDto contextRequest = DynamicExportRequestBodyDto.builder()
			.timesheetFields(REIMBURSEMENT_CONTEXT_TS_FIELDS)
			.candidateFields(REIMBURSEMENT_CONTEXT_CANDIDATE_FIELDS)
			.timesheetIds(timesheetIds)
			.fileFormat(FileFormat.EXCEL)
			.build();

		List<DynamicExportResponseBodyDto> contextRows = this.timesheetExportRepository.getExportData(contextRequest,
				accountId);

		Map<Integer, DynamicExportResponseBodyDto> map = new HashMap<>();
		for (DynamicExportResponseBodyDto row : contextRows) {
			Integer tsId = (Integer) row.getData().get(FIELD_TIMESHEET);
			if (tsId != null) {
				map.putIfAbsent(tsId, row);
			}
		}
		return map;
	}

	/**
	 * Enhance grouped export data with work hours using a single batch fetch for all
	 * timesheets, then group by period in memory. Avoids N+1 database roundtrips.
	 */
	private void enhanceGroupedDataWithWorkHoursBatch(List<PeriodGroupedExportResponseBodyDto> groupedData,
			List<Integer> allTimesheetIds, Integer accountId, boolean use12HourFormat) {
		Map<Integer, Map<String, String>> allTimeLogsMap = this.timeLogRepository
			.getStructuredTimeLogsForTimesheets(allTimesheetIds, accountId);

		for (PeriodGroupedExportResponseBodyDto period : groupedData) {
			this.expandWorkHoursIntoColumns(period.getTimesheetsInPeriod(), allTimeLogsMap, use12HourFormat);
		}
	}

	/**
	 * Enhance grouped export data with break intervals using a single batch fetch. Time-
	 * interval logging: comma-separated ranges (e.g. "09:00-09:15, 12:00-13:00"). Hours-
	 * based logging: break hours as decimal (e.g. "0.50").
	 */
	private void enhanceGroupedDataWithBreakIntervalsBatch(List<PeriodGroupedExportResponseBodyDto> groupedData,
			List<Integer> allTimesheetIds, Integer accountId, boolean use12HourFormat) {
		Map<Integer, Map<String, String>> allBreakIntervalsMap = this.timeLogRepository
			.getStructuredBreakIntervalsForTimesheets(allTimesheetIds, accountId);

		for (PeriodGroupedExportResponseBodyDto period : groupedData) {
			this.expandBreakIntervalsIntoColumns(period.getTimesheetsInPeriod(), allBreakIntervalsMap, use12HourFormat);
		}
	}

	/**
	 * Enhance grouped export data with time log remarks using a single batch fetch.
	 * Time-interval: comma-separated remarks per range. Hours-based: single remark.
	 */
	private void enhanceGroupedDataWithRemarksBatch(List<PeriodGroupedExportResponseBodyDto> groupedData,
			List<Integer> allTimesheetIds, Integer accountId) {
		Map<Integer, Map<String, String>> allRemarksMap = this.timeLogRepository
			.getStructuredRemarksForTimesheets(allTimesheetIds, accountId);

		for (PeriodGroupedExportResponseBodyDto period : groupedData) {
			this.expandRemarksIntoColumns(period.getTimesheetsInPeriod(), allRemarksMap);
		}
	}

	/**
	 * Enhance grouped export data with overtime hours using a single batch fetch for all
	 * timesheets.
	 */
	private void enhanceGroupedDataWithOvertimeHoursBatch(List<PeriodGroupedExportResponseBodyDto> groupedData,
			List<Integer> allTimesheetIds, Integer accountId) {
		Map<Integer, Map<String, String>> allOvertimeMap = this.timeLogRepository
			.getStructuredOvertimeHoursForTimesheets(allTimesheetIds, accountId);

		for (PeriodGroupedExportResponseBodyDto period : groupedData) {
			this.expandOvertimeHoursIntoColumns(period.getTimesheetsInPeriod(), allOvertimeMap);
		}
	}

	/**
	 * Enhance grouped export data with effective work hours using a single batch fetch
	 * for all timesheets.
	 */
	private void enhanceGroupedDataWithEffectiveWorkHoursBatch(List<PeriodGroupedExportResponseBodyDto> groupedData,
			List<Integer> allTimesheetIds, Integer accountId) {
		Map<Integer, Map<String, String>> allEffectiveWorkHoursMap = this.timeLogRepository
			.getStructuredEffectiveWorkHoursForTimesheets(allTimesheetIds, accountId);

		for (PeriodGroupedExportResponseBodyDto period : groupedData) {
			this.expandEffectiveWorkHoursIntoColumns(period.getTimesheetsInPeriod(), allEffectiveWorkHoursMap);
		}
	}

	/**
	 * Enhance grouped export data with aggregate totals using a single batch fetch for
	 * all timesheets. Avoids N+1 database roundtrips when processing multiple periods.
	 */
	private void enhanceGroupedDataWithAggregateTotalsBatch(List<PeriodGroupedExportResponseBodyDto> groupedData,
			List<String> selectedFields, Integer accountId, List<Integer> allTimesheetIds) {
		Map<Integer, String> aggregateTotals = this.fetchAggregateTotals(selectedFields, allTimesheetIds, accountId);

		for (PeriodGroupedExportResponseBodyDto period : groupedData) {
			for (DynamicExportResponseBodyDto data : period.getTimesheetsInPeriod()) {
				Integer timesheetId = (Integer) data.getData().get(FIELD_TIMESHEET);
				if (timesheetId != null) {
					this.populateAggregateFields(data, selectedFields, aggregateTotals, timesheetId);
				}
			}
		}
	}

	/**
	 * Enhance export data with aggregate totals (total overtime hours, total work hours,
	 * total hours). These are regular columns that work for both exportEachDay
	 * true/false.
	 */
	private void enhanceWithAggregateTotals(List<DynamicExportResponseBodyDto> exportData, List<String> selectedFields,
			Integer accountId) {
		// Extract timesheet IDs for batch processing
		List<Integer> timesheetIds = this.extractTimesheetIds(exportData);
		if (timesheetIds.isEmpty()) {
			return;
		}

		// Fetch aggregate data if requested
		Map<Integer, String> aggregateTotals = this.fetchAggregateTotals(selectedFields, timesheetIds, accountId);

		// Populate the fields in the export data
		for (DynamicExportResponseBodyDto data : exportData) {
			Integer timesheetId = (Integer) data.getData().get(FIELD_TIMESHEET);
			if (timesheetId != null) {
				this.populateAggregateFields(data, selectedFields, aggregateTotals, timesheetId);
			}
		}
	}

	/**
	 * Fetch aggregate totals from cst_timesheet_t (total_time, total_work_time,
	 * total_overtime) based on selected fields.
	 */
	private Map<Integer, String> fetchAggregateTotals(List<String> selectedFields, List<Integer> timesheetIds,
			Integer accountId) {
		Map<Integer, String> aggregateTotals = new HashMap<>();

		boolean needsTotals = selectedFields.contains(FIELD_TOTAL_OVERTIME)
				|| selectedFields.contains(FIELD_TOTAL_WORK_TIME) || selectedFields.contains(FIELD_TOTAL_REGULAR_HOURS)
				|| selectedFields.contains(FIELD_TOTAL_TIME);
		if (!needsTotals) {
			return aggregateTotals;
		}

		TimesheetTotalsQueryResultDto totals = this.timesheetExportRepository.getTimesheetTotals(timesheetIds,
				accountId);

		prepareAggregateTotalMapFromTimesheetId(selectedFields, timesheetIds, aggregateTotals, totals);

		return aggregateTotals;
	}

	private void prepareAggregateTotalMapFromTimesheetId(List<String> selectedFields, List<Integer> timesheetIds,
			Map<Integer, String> aggregateTotals, TimesheetTotalsQueryResultDto totals) {
		for (Integer id : timesheetIds) {
			StringBuilder parts = new StringBuilder();
			if (selectedFields.contains(FIELD_TOTAL_OVERTIME)) {
				String value = totals.totalOvertime().getOrDefault(id, "0.00");
				parts.append(FIELD_TOTAL_OVERTIME).append(":").append(value);
			}
			if (selectedFields.contains(FIELD_TOTAL_WORK_TIME)) {
				if (parts.length() > 0) {
					parts.append("|");
				}
				String value = totals.totalWorkTime().getOrDefault(id, "0.00");
				parts.append(FIELD_TOTAL_WORK_TIME).append(":").append(value);
			}
			if (selectedFields.contains(FIELD_TOTAL_REGULAR_HOURS)) {
				if (parts.length() > 0) {
					parts.append("|");
				}
				String value = totals.totalRegularHours().getOrDefault(id, "0.00");
				parts.append(FIELD_TOTAL_REGULAR_HOURS).append(":").append(value);
			}
			if (selectedFields.contains(FIELD_TOTAL_TIME)) {
				if (parts.length() > 0) {
					parts.append("|");
				}
				String value = totals.totalTime().getOrDefault(id, "0.00");
				parts.append(FIELD_TOTAL_TIME).append(":").append(value);
			}
			if (parts.length() > 0) {
				aggregateTotals.put(id, parts.toString());
			}
		}
	}

	/**
	 * Populate aggregate fields in export data
	 */
	private void populateAggregateFields(DynamicExportResponseBodyDto data, List<String> selectedFields,
			Map<Integer, String> aggregateTotals, Integer timesheetId) {
		String aggregateData = aggregateTotals.get(timesheetId);
		if (aggregateData == null) {
			return;
		}

		String[] parts = aggregateData.split("\\|");
		for (String part : parts) {
			this.processAggregatePart(part, selectedFields, data);
		}
	}

	/**
	 * Process a single aggregate part and update data if applicable
	 */
	private void processAggregatePart(String part, List<String> selectedFields, DynamicExportResponseBodyDto data) {
		String totalOvertimePrefix = FIELD_TOTAL_OVERTIME + ":";
		String totalWorkTimePrefix = FIELD_TOTAL_WORK_TIME + ":";
		String totalRegularHoursPrefix = FIELD_TOTAL_REGULAR_HOURS + ":";
		String totalTimePrefix = FIELD_TOTAL_TIME + ":";
		if (part.startsWith(totalOvertimePrefix) && selectedFields.contains(FIELD_TOTAL_OVERTIME)) {
			String value = part.substring(totalOvertimePrefix.length());
			data.getData().put(FIELD_TOTAL_OVERTIME, value.isEmpty() ? "0.00" : value);
		}
		else if (part.startsWith(totalRegularHoursPrefix) && selectedFields.contains(FIELD_TOTAL_REGULAR_HOURS)) {
			String value = part.substring(totalRegularHoursPrefix.length());
			data.getData().put(FIELD_TOTAL_REGULAR_HOURS, value.isEmpty() ? "0.00" : value);
		}
		else if (part.startsWith(totalWorkTimePrefix) && selectedFields.contains(FIELD_TOTAL_WORK_TIME)) {
			String value = part.substring(totalWorkTimePrefix.length());
			data.getData().put(FIELD_TOTAL_WORK_TIME, value.isEmpty() ? "0.00" : value);
		}
		else if (part.startsWith(totalTimePrefix) && selectedFields.contains(FIELD_TOTAL_TIME)) {
			String value = part.substring(totalTimePrefix.length());
			data.getData().put(FIELD_TOTAL_TIME, value.isEmpty() ? "0.00" : value);
		}
	}

	/**
	 * Enforce the canonical export sequence for the aggregate total columns on every row:
	 * Total Work Hours -> Total Regular Hours -> Total Overtime Hours -> Total Hours. The
	 * block is anchored at the earliest position any of these columns currently occupies,
	 * so the surrounding columns keep their relative order. Runs before per-day column
	 * ordering (exportEachDay: true) so the reordered totals feed into the fixed
	 * ordering.
	 */
	private void applyTotalsColumnOrdering(List<DynamicExportResponseBodyDto> exportData) {
		if (exportData == null) {
			return;
		}
		for (DynamicExportResponseBodyDto data : exportData) {
			this.reorderTotalsColumns(data);
		}
	}

	private void reorderTotalsColumns(DynamicExportResponseBodyDto data) {
		List<String> columnOrder = data.getColumnOrder();
		if (columnOrder == null) {
			return;
		}

		List<String> presentTotals = TOTALS_CANONICAL_ORDER.stream().filter(columnOrder::contains).toList();
		// Nothing to reorder unless at least two of the total columns are present.
		if (presentTotals.size() < 2) {
			return;
		}

		int anchorIndex = presentTotals.stream().mapToInt(columnOrder::indexOf).min().orElse(-1);
		Set<String> totalsSet = new HashSet<>(presentTotals);

		List<String> newColumnOrder = new ArrayList<>(columnOrder.size());
		for (int i = 0; i < columnOrder.size(); i++) {
			if (i == anchorIndex) {
				newColumnOrder.addAll(presentTotals);
			}
			String column = columnOrder.get(i);
			if (!totalsSet.contains(column)) {
				newColumnOrder.add(column);
			}
		}

		data.setColumnOrder(newColumnOrder);
	}

	/**
	 * Extract timesheet IDs from export data for batch processing. Assumes timesheetId is
	 * always present in the selected fields.
	 */
	private List<Integer> extractTimesheetIds(List<DynamicExportResponseBodyDto> exportData) {
		return exportData.stream()
			.map((data) -> (Integer) data.getData().get(FIELD_TIMESHEET))
			.filter(Objects::nonNull)
			.distinct()
			.toList();
	}

	/**
	 * Extract all timesheet IDs from grouped export data for batch processing across all
	 * periods.
	 */
	private List<Integer> extractAllTimesheetIdsFromGroupedData(List<PeriodGroupedExportResponseBodyDto> groupedData) {
		return groupedData.stream()
			.flatMap((period) -> period.getTimesheetsInPeriod().stream())
			.map((data) -> (Integer) data.getData().get(FIELD_TIMESHEET))
			.filter(Objects::nonNull)
			.distinct()
			.toList();
	}

	/**
	 * Expand work hours into separate date columns for dynamic export. Only adds date
	 * columns that are relevant to each specific timesheet.
	 */
	private void expandWorkHoursIntoColumns(List<DynamicExportResponseBodyDto> exportData,
			Map<Integer, Map<String, String>> structuredTimeLogsMap, boolean use12HourFormat) {

		// Collect and sort work hours columns
		List<String> sortedWorkHoursColumns = this.collectAndSortWorkHoursColumns(exportData, structuredTimeLogsMap);

		// Expand work hours for each timesheet
		for (DynamicExportResponseBodyDto data : exportData) {
			this.expandWorkHoursForSingleTimesheet(data, structuredTimeLogsMap, sortedWorkHoursColumns,
					use12HourFormat);
		}
	}

	/**
	 * Collect all unique work hours columns and sort them chronologically
	 */
	private List<String> collectAndSortWorkHoursColumns(List<DynamicExportResponseBodyDto> exportData,
			Map<Integer, Map<String, String>> structuredTimeLogsMap) {
		Set<String> allWorkHoursColumns = new HashSet<>();

		for (DynamicExportResponseBodyDto data : exportData) {
			Integer timesheetId = (Integer) data.getData().get(FIELD_TIMESHEET);
			if (timesheetId != null && structuredTimeLogsMap.containsKey(timesheetId)) {
				Map<String, String> timeLogColumns = structuredTimeLogsMap.get(timesheetId);
				this.collectWorkHoursColumnsFromTimesheet(timeLogColumns, allWorkHoursColumns);
			}
		}

		return allWorkHoursColumns.stream().sorted(this::compareWorkHoursColumns).toList();
	}

	/**
	 * Collect work hours columns from a single timesheet's time log columns
	 */
	private void collectWorkHoursColumnsFromTimesheet(Map<String, String> timeLogColumns,
			Set<String> allWorkHoursColumns) {
		for (String dateColumn : timeLogColumns.keySet()) {
			String workHoursColumn = this.transformToWorkHoursColumn(dateColumn);
			allWorkHoursColumns.add(workHoursColumn);
		}
	}

	/**
	 * Expand work hours for a single timesheet
	 */
	private void expandWorkHoursForSingleTimesheet(DynamicExportResponseBodyDto data,
			Map<Integer, Map<String, String>> structuredTimeLogsMap, List<String> sortedWorkHoursColumns,
			boolean use12HourFormat) {
		Integer timesheetId = (Integer) data.getData().get(FIELD_TIMESHEET);

		// Remove the original workHours field since we're expanding it
		data.getData().remove(FIELD_WORK_HOURS);

		if (timesheetId != null && structuredTimeLogsMap.containsKey(timesheetId)) {
			Map<String, String> timeLogColumns = structuredTimeLogsMap.get(timesheetId);
			this.addWorkHoursColumnsWithData(data, timeLogColumns, sortedWorkHoursColumns, use12HourFormat);
		}
		else {
			this.addEmptyWorkHoursColumns(data, sortedWorkHoursColumns);
		}

		this.updateColumnOrderForWorkHours(data, sortedWorkHoursColumns);
	}

	/**
	 * Add work hours columns with actual data
	 */
	private void addWorkHoursColumnsWithData(DynamicExportResponseBodyDto data, Map<String, String> timeLogColumns,
			List<String> sortedWorkHoursColumns, boolean use12HourFormat) {
		for (String workHoursColumn : sortedWorkHoursColumns) {
			String originalDateColumn = this.findOriginalDateColumn(workHoursColumn, timeLogColumns.keySet());
			String timeValue = (originalDateColumn != null) ? timeLogColumns.getOrDefault(originalDateColumn, "") : "";
			timeValue = ExportTimeFormatHelper.applyTimeFormat(timeValue, use12HourFormat);
			data.getData().put(workHoursColumn, timeValue);
		}
	}

	/**
	 * Add empty work hours columns for timesheets without time log data
	 */
	private void addEmptyWorkHoursColumns(DynamicExportResponseBodyDto data, List<String> sortedWorkHoursColumns) {
		for (String workHoursColumn : sortedWorkHoursColumns) {
			data.getData().put(workHoursColumn, "");
		}
	}

	/**
	 * Update column order to include new work hours columns
	 */
	private void updateColumnOrderForWorkHours(DynamicExportResponseBodyDto data, List<String> sortedWorkHoursColumns) {
		if (data.getColumnOrder() != null) {
			List<String> newColumnOrder = new ArrayList<>(data.getColumnOrder());
			newColumnOrder.remove(FIELD_WORK_HOURS);
			newColumnOrder.addAll(sortedWorkHoursColumns);
			data.setColumnOrder(newColumnOrder);
		}
	}

	/**
	 * Expand break intervals into separate date columns for dynamic export.
	 * Time-interval: comma-separated ranges (e.g. "09:00-09:15, 12:00-13:00").
	 * Hours-based: decimal hours.
	 */
	private void expandBreakIntervalsIntoColumns(List<DynamicExportResponseBodyDto> exportData,
			Map<Integer, Map<String, String>> structuredBreakIntervalsMap, boolean use12HourFormat) {
		List<String> sortedBreakIntervalsColumns = this.collectAndSortBreakIntervalsColumns(exportData,
				structuredBreakIntervalsMap);

		for (DynamicExportResponseBodyDto data : exportData) {
			this.expandBreakIntervalsForSingleTimesheet(data, structuredBreakIntervalsMap, sortedBreakIntervalsColumns,
					use12HourFormat);
		}
	}

	private List<String> collectAndSortBreakIntervalsColumns(List<DynamicExportResponseBodyDto> exportData,
			Map<Integer, Map<String, String>> structuredBreakIntervalsMap) {
		Set<String> allBreakIntervalsColumns = new HashSet<>();

		for (DynamicExportResponseBodyDto data : exportData) {
			Integer timesheetId = (Integer) data.getData().get(FIELD_TIMESHEET);
			if (timesheetId != null && structuredBreakIntervalsMap.containsKey(timesheetId)) {
				Map<String, String> breakIntervalsColumns = structuredBreakIntervalsMap.get(timesheetId);
				this.collectBreakIntervalsColumnsFromTimesheet(breakIntervalsColumns, allBreakIntervalsColumns);
			}
		}

		return allBreakIntervalsColumns.stream().sorted(this::compareBreakIntervalsColumns).toList();
	}

	private void collectBreakIntervalsColumnsFromTimesheet(Map<String, String> breakIntervalsColumns,
			Set<String> allBreakIntervalsColumns) {
		for (String dateColumn : breakIntervalsColumns.keySet()) {
			String breakIntervalsColumn = this.transformToBreakIntervalsColumn(dateColumn);
			allBreakIntervalsColumns.add(breakIntervalsColumn);
		}
	}

	private void expandBreakIntervalsForSingleTimesheet(DynamicExportResponseBodyDto data,
			Map<Integer, Map<String, String>> structuredBreakIntervalsMap, List<String> sortedBreakIntervalsColumns,
			boolean use12HourFormat) {
		Integer timesheetId = (Integer) data.getData().get(FIELD_TIMESHEET);

		data.getData().remove(FIELD_BREAK_INTERVALS);

		if (timesheetId != null && structuredBreakIntervalsMap.containsKey(timesheetId)) {
			Map<String, String> breakIntervalsColumns = structuredBreakIntervalsMap.get(timesheetId);
			this.addBreakIntervalsColumnsWithData(data, breakIntervalsColumns, sortedBreakIntervalsColumns,
					use12HourFormat);
		}
		else {
			this.addEmptyBreakIntervalsColumns(data, sortedBreakIntervalsColumns);
		}

		this.updateColumnOrderForBreakIntervals(data, sortedBreakIntervalsColumns);
	}

	private void addBreakIntervalsColumnsWithData(DynamicExportResponseBodyDto data,
			Map<String, String> breakIntervalsColumns, List<String> sortedBreakIntervalsColumns,
			boolean use12HourFormat) {
		for (String breakIntervalsColumn : sortedBreakIntervalsColumns) {
			String originalDateColumn = this.findOriginalDateColumnForBreakIntervals(breakIntervalsColumn,
					breakIntervalsColumns.keySet());
			String breakValue = (originalDateColumn != null)
					? breakIntervalsColumns.getOrDefault(originalDateColumn, "") : "";
			breakValue = ExportTimeFormatHelper.applyTimeFormat(breakValue, use12HourFormat);
			data.getData().put(breakIntervalsColumn, breakValue);
		}
	}

	private void addEmptyBreakIntervalsColumns(DynamicExportResponseBodyDto data,
			List<String> sortedBreakIntervalsColumns) {
		for (String breakIntervalsColumn : sortedBreakIntervalsColumns) {
			data.getData().put(breakIntervalsColumn, "");
		}
	}

	private void updateColumnOrderForBreakIntervals(DynamicExportResponseBodyDto data,
			List<String> sortedBreakIntervalsColumns) {
		if (data.getColumnOrder() != null) {
			List<String> newColumnOrder = new ArrayList<>(data.getColumnOrder());
			newColumnOrder.remove(FIELD_BREAK_INTERVALS);
			newColumnOrder.addAll(sortedBreakIntervalsColumns);
			data.setColumnOrder(newColumnOrder);
		}
	}

	private String transformToBreakIntervalsColumn(String dateColumn) {
		if (dateColumn != null && dateColumn.contains(",")) {
			return dateColumn + BREAK_INTERVALS_SUFFIX;
		}
		return dateColumn + BREAK_INTERVALS_SUFFIX;
	}

	private String findOriginalDateColumnForBreakIntervals(String breakIntervalsColumn,
			Set<String> availableDateColumns) {
		if (breakIntervalsColumn == null || !breakIntervalsColumn.contains(BREAK_INTERVALS_SUFFIX)) {
			return null;
		}

		String dateColumn = breakIntervalsColumn.replace(BREAK_INTERVALS_SUFFIX, "").trim();

		if (availableDateColumns.contains(dateColumn)) {
			return dateColumn;
		}

		return null;
	}

	private int compareBreakIntervalsColumns(String col1, String col2) {
		String dateCol1 = col1.replace(BREAK_INTERVALS_SUFFIX, "");
		String dateCol2 = col2.replace(BREAK_INTERVALS_SUFFIX, "");

		return this.compareDateColumns(dateCol1, dateCol2);
	}

	/**
	 * Expand time log remarks into separate date columns for dynamic export.
	 * Time-interval: comma-separated per range. Hours-based: single remark.
	 */
	private void expandRemarksIntoColumns(List<DynamicExportResponseBodyDto> exportData,
			Map<Integer, Map<String, String>> structuredRemarksMap) {
		List<String> sortedRemarksColumns = this.collectAndSortRemarksColumns(exportData, structuredRemarksMap);

		for (DynamicExportResponseBodyDto data : exportData) {
			this.expandRemarksForSingleTimesheet(data, structuredRemarksMap, sortedRemarksColumns);
		}
	}

	private List<String> collectAndSortRemarksColumns(List<DynamicExportResponseBodyDto> exportData,
			Map<Integer, Map<String, String>> structuredRemarksMap) {
		Set<String> allRemarksColumns = new HashSet<>();

		for (DynamicExportResponseBodyDto data : exportData) {
			Integer timesheetId = (Integer) data.getData().get(FIELD_TIMESHEET);
			if (timesheetId != null && structuredRemarksMap.containsKey(timesheetId)) {
				Map<String, String> remarksColumns = structuredRemarksMap.get(timesheetId);
				this.collectRemarksColumnsFromTimesheet(remarksColumns, allRemarksColumns);
			}
		}

		return allRemarksColumns.stream().sorted(this::compareRemarksColumns).toList();
	}

	private void collectRemarksColumnsFromTimesheet(Map<String, String> remarksColumns, Set<String> allRemarksColumns) {
		for (String dateColumn : remarksColumns.keySet()) {
			String remarksColumn = this.transformToRemarksColumn(dateColumn);
			allRemarksColumns.add(remarksColumn);
		}
	}

	private void expandRemarksForSingleTimesheet(DynamicExportResponseBodyDto data,
			Map<Integer, Map<String, String>> structuredRemarksMap, List<String> sortedRemarksColumns) {
		Integer timesheetId = (Integer) data.getData().get(FIELD_TIMESHEET);

		data.getData().remove(FIELD_TIME_LOG_REMARKS);

		if (timesheetId != null && structuredRemarksMap.containsKey(timesheetId)) {
			Map<String, String> remarksColumns = structuredRemarksMap.get(timesheetId);
			this.addRemarksColumnsWithData(data, remarksColumns, sortedRemarksColumns);
		}
		else {
			this.addEmptyRemarksColumns(data, sortedRemarksColumns);
		}

		this.updateColumnOrderForRemarks(data, sortedRemarksColumns);
	}

	private void addRemarksColumnsWithData(DynamicExportResponseBodyDto data, Map<String, String> remarksColumns,
			List<String> sortedRemarksColumns) {
		for (String remarksColumn : sortedRemarksColumns) {
			String originalDateColumn = this.findOriginalDateColumnForRemarks(remarksColumn, remarksColumns.keySet());
			String remarkValue = (originalDateColumn != null) ? remarksColumns.getOrDefault(originalDateColumn, "")
					: "";
			data.getData().put(remarksColumn, remarkValue);
		}
	}

	private void addEmptyRemarksColumns(DynamicExportResponseBodyDto data, List<String> sortedRemarksColumns) {
		for (String remarksColumn : sortedRemarksColumns) {
			data.getData().put(remarksColumn, "");
		}
	}

	private void updateColumnOrderForRemarks(DynamicExportResponseBodyDto data, List<String> sortedRemarksColumns) {
		if (data.getColumnOrder() != null) {
			List<String> newColumnOrder = new ArrayList<>(data.getColumnOrder());
			newColumnOrder.remove(FIELD_TIME_LOG_REMARKS);
			newColumnOrder.addAll(sortedRemarksColumns);
			data.setColumnOrder(newColumnOrder);
		}
	}

	private String transformToRemarksColumn(String dateColumn) {
		if (dateColumn != null && dateColumn.contains(",")) {
			return dateColumn + REMARKS_SUFFIX;
		}
		return dateColumn + REMARKS_SUFFIX;
	}

	private String findOriginalDateColumnForRemarks(String remarksColumn, Set<String> availableDateColumns) {
		if (remarksColumn == null || !remarksColumn.contains(REMARKS_SUFFIX)) {
			return null;
		}

		String dateColumn = remarksColumn.replace(REMARKS_SUFFIX, "").trim();

		if (availableDateColumns.contains(dateColumn)) {
			return dateColumn;
		}

		return null;
	}

	private int compareRemarksColumns(String col1, String col2) {
		String dateCol1 = col1.replace(REMARKS_SUFFIX, "");
		String dateCol2 = col2.replace(REMARKS_SUFFIX, "");

		return this.compareDateColumns(dateCol1, dateCol2);
	}

	/**
	 * Expand overtime hours into separate date columns for dynamic export. Uses "Day,
	 * Overtime Hours" format to avoid collision with other time fields.
	 */
	private void expandOvertimeHoursIntoColumns(List<DynamicExportResponseBodyDto> exportData,
			Map<Integer, Map<String, String>> structuredOvertimeMap) {

		// Collect and sort overtime hours columns
		List<String> sortedOvertimeHoursColumns = this.collectAndSortOvertimeHoursColumns(exportData,
				structuredOvertimeMap);

		// Expand overtime hours for each timesheet
		for (DynamicExportResponseBodyDto data : exportData) {
			this.expandOvertimeHoursForSingleTimesheet(data, structuredOvertimeMap, sortedOvertimeHoursColumns);
		}
	}

	/**
	 * Collect all unique overtime hours columns and sort them chronologically
	 */
	private List<String> collectAndSortOvertimeHoursColumns(List<DynamicExportResponseBodyDto> exportData,
			Map<Integer, Map<String, String>> structuredOvertimeMap) {
		Set<String> allOvertimeHoursColumns = new HashSet<>();

		for (DynamicExportResponseBodyDto data : exportData) {
			Integer timesheetId = (Integer) data.getData().get(FIELD_TIMESHEET);
			if (timesheetId != null && structuredOvertimeMap.containsKey(timesheetId)) {
				Map<String, String> overtimeColumns = structuredOvertimeMap.get(timesheetId);
				this.collectOvertimeHoursColumnsFromTimesheet(overtimeColumns, allOvertimeHoursColumns);
			}
		}

		return allOvertimeHoursColumns.stream().sorted(this::compareOvertimeHoursColumns).toList();
	}

	/**
	 * Collect overtime hours columns from a single timesheet's overtime columns
	 */
	private void collectOvertimeHoursColumnsFromTimesheet(Map<String, String> overtimeColumns,
			Set<String> allOvertimeHoursColumns) {
		for (String dateColumn : overtimeColumns.keySet()) {
			String overtimeHoursColumn = this.transformToOvertimeHoursColumn(dateColumn);
			allOvertimeHoursColumns.add(overtimeHoursColumn);
		}
	}

	/**
	 * Expand overtime hours for a single timesheet
	 */
	private void expandOvertimeHoursForSingleTimesheet(DynamicExportResponseBodyDto data,
			Map<Integer, Map<String, String>> structuredOvertimeMap, List<String> sortedOvertimeHoursColumns) {
		Integer timesheetId = (Integer) data.getData().get(FIELD_TIMESHEET);

		// Remove the original overtimeHours field since we're expanding it
		data.getData().remove(FIELD_OVERTIME_HOURS);

		if (timesheetId != null && structuredOvertimeMap.containsKey(timesheetId)) {
			Map<String, String> overtimeColumns = structuredOvertimeMap.get(timesheetId);
			this.addOvertimeHoursColumnsWithData(data, overtimeColumns, sortedOvertimeHoursColumns);
		}
		else {
			this.addEmptyOvertimeHoursColumns(data, sortedOvertimeHoursColumns);
		}

		this.updateColumnOrderForOvertimeHours(data, sortedOvertimeHoursColumns);
	}

	/**
	 * Add overtime hours columns with actual data
	 */
	private void addOvertimeHoursColumnsWithData(DynamicExportResponseBodyDto data, Map<String, String> overtimeColumns,
			List<String> sortedOvertimeHoursColumns) {
		for (String overtimeHoursColumn : sortedOvertimeHoursColumns) {
			String originalDateColumn = this.findOriginalDateColumnForOvertime(overtimeHoursColumn,
					overtimeColumns.keySet());
			String overtimeValue = (originalDateColumn != null) ? overtimeColumns.getOrDefault(originalDateColumn, "")
					: "";
			data.getData().put(overtimeHoursColumn, overtimeValue);
		}
	}

	/**
	 * Add empty overtime hours columns for timesheets without overtime data
	 */
	private void addEmptyOvertimeHoursColumns(DynamicExportResponseBodyDto data,
			List<String> sortedOvertimeHoursColumns) {
		for (String overtimeHoursColumn : sortedOvertimeHoursColumns) {
			data.getData().put(overtimeHoursColumn, "");
		}
	}

	/**
	 * Update column order to include new overtime hours columns
	 */
	private void updateColumnOrderForOvertimeHours(DynamicExportResponseBodyDto data,
			List<String> sortedOvertimeHoursColumns) {
		if (data.getColumnOrder() != null) {
			List<String> newColumnOrder = new ArrayList<>(data.getColumnOrder());
			newColumnOrder.remove(FIELD_OVERTIME_HOURS);
			newColumnOrder.addAll(sortedOvertimeHoursColumns);
			data.setColumnOrder(newColumnOrder);
		}
	}

	/**
	 * Compare date column headers chronologically. Parses date from format "Thursday, 10
	 * Jul 2025" and sorts by actual date.
	 */
	private int compareDateColumns(String dateColumn1, String dateColumn2) {
		try {
			// Extract date part after the comma and day name
			// Format: "Thursday, 10 Jul 2025" -> "10 Jul 2025"
			String datePart1 = dateColumn1.substring(dateColumn1.indexOf(',') + 2);
			String datePart2 = dateColumn2.substring(dateColumn2.indexOf(',') + 2);

			// Parse dates for comparison
			java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy",
					java.util.Locale.ENGLISH);
			java.time.LocalDate date1 = java.time.LocalDate.parse(datePart1, formatter);
			java.time.LocalDate date2 = java.time.LocalDate.parse(datePart2, formatter);

			return date1.compareTo(date2);
		}
		catch (Exception ex) {
			// Fallback to string comparison if date parsing fails
			return dateColumn1.compareTo(dateColumn2);
		}
	}

	/**
	 * Sanitize filename for file system compatibility
	 */
	private String sanitizeFilename(String filename) {
		// Remove invalid file system characters
		return filename.replaceAll("[<>:\"/\\\\|?*]", "-");
	}

	/**
	 * Expand effective work hours into separate date columns for dynamic export.
	 */
	private void expandEffectiveWorkHoursIntoColumns(List<DynamicExportResponseBodyDto> exportData,
			Map<Integer, Map<String, String>> structuredEffectiveWorkHoursMap) {

		// Collect and sort effective work hours columns
		List<String> sortedRelevantDateColumns = this.collectAndSortEffectiveWorkHoursColumns(exportData,
				structuredEffectiveWorkHoursMap);

		// Expand effective work hours for each timesheet
		for (DynamicExportResponseBodyDto data : exportData) {
			this.expandEffectiveWorkHoursForSingleTimesheet(data, structuredEffectiveWorkHoursMap,
					sortedRelevantDateColumns);
		}
	}

	/**
	 * Collect all unique effective work hours date columns and sort them chronologically
	 */
	private List<String> collectAndSortEffectiveWorkHoursColumns(List<DynamicExportResponseBodyDto> exportData,
			Map<Integer, Map<String, String>> structuredEffectiveWorkHoursMap) {
		Set<String> allRelevantDateColumns = new HashSet<>();

		for (DynamicExportResponseBodyDto data : exportData) {
			Integer timesheetId = (Integer) data.getData().get(FIELD_TIMESHEET);
			if (timesheetId != null && structuredEffectiveWorkHoursMap.containsKey(timesheetId)) {
				Map<String, String> effectiveWorkHoursColumns = structuredEffectiveWorkHoursMap.get(timesheetId);
				allRelevantDateColumns.addAll(effectiveWorkHoursColumns.keySet());
			}
		}

		return allRelevantDateColumns.stream().sorted(this::compareDateColumns).toList();
	}

	/**
	 * Expand effective work hours for a single timesheet
	 */
	private void expandEffectiveWorkHoursForSingleTimesheet(DynamicExportResponseBodyDto data,
			Map<Integer, Map<String, String>> structuredEffectiveWorkHoursMap, List<String> sortedRelevantDateColumns) {
		Integer timesheetId = (Integer) data.getData().get(FIELD_TIMESHEET);

		// Remove the original effectiveWorkHours field since we're expanding it
		data.getData().remove(FIELD_EFFECTIVE_WORK_HOURS);

		if (timesheetId != null && structuredEffectiveWorkHoursMap.containsKey(timesheetId)) {
			Map<String, String> effectiveWorkHoursColumns = structuredEffectiveWorkHoursMap.get(timesheetId);
			this.addEffectiveWorkHoursColumnsWithData(data, effectiveWorkHoursColumns, sortedRelevantDateColumns);
		}
		else {
			this.addEmptyEffectiveWorkHoursColumns(data, sortedRelevantDateColumns);
		}

		this.updateColumnOrderForEffectiveWorkHours(data, sortedRelevantDateColumns);
	}

	/**
	 * Add effective work hours columns with actual data
	 */
	private void addEffectiveWorkHoursColumnsWithData(DynamicExportResponseBodyDto data,
			Map<String, String> effectiveWorkHoursColumns, List<String> sortedRelevantDateColumns) {
		for (String dateColumn : sortedRelevantDateColumns) {
			String effectiveWorkHoursValue = effectiveWorkHoursColumns.getOrDefault(dateColumn, "");
			data.getData().put(dateColumn, effectiveWorkHoursValue);
		}
	}

	/**
	 * Add empty effective work hours columns for timesheets without data
	 */
	private void addEmptyEffectiveWorkHoursColumns(DynamicExportResponseBodyDto data,
			List<String> sortedRelevantDateColumns) {
		for (String dateColumn : sortedRelevantDateColumns) {
			data.getData().put(dateColumn, "");
		}
	}

	/**
	 * Update column order to include new effective work hours date columns
	 */
	private void updateColumnOrderForEffectiveWorkHours(DynamicExportResponseBodyDto data,
			List<String> sortedRelevantDateColumns) {
		if (data.getColumnOrder() != null) {
			List<String> newColumnOrder = new ArrayList<>(data.getColumnOrder());
			newColumnOrder.remove(FIELD_EFFECTIVE_WORK_HOURS);
			newColumnOrder.addAll(sortedRelevantDateColumns);
			data.setColumnOrder(newColumnOrder);
		}
	}

	/**
	 * Validates that mandatory export columns are present in the request.
	 */
	private void validateMandatoryExportColumns(DynamicExportRequestBodyDto request) {
		List<String> timesheetFields = request.getOriginalTimesheetFields();
		List<String> candidateFields = request.getCandidateFields();

		Boolean hasTimesheetId = timesheetFields != null && timesheetFields.contains(FIELD_TIMESHEET_ID);
		Boolean hasCandidateName = candidateFields != null && candidateFields.contains(FIELD_CANDIDATE_NAME);
		Boolean hasTimesheetPeriod = timesheetFields != null && timesheetFields.contains(FIELD_TIMESHEET_PERIOD);

		if (!Boolean.TRUE.equals(hasTimesheetId)) {
			throw new ResourceNotFoundException(FIELD_TIMESHEET_ID + " is a mandatory field for timesheet export");
		}

		if (!Boolean.TRUE.equals(hasTimesheetPeriod)) {
			throw new ResourceNotFoundException(FIELD_TIMESHEET_PERIOD + " is a mandatory field for timesheet export");
		}

		if (!Boolean.TRUE.equals(hasCandidateName)) {
			throw new ResourceNotFoundException(FIELD_CANDIDATE_NAME + " is a mandatory field for timesheet export");
		}
	}

	/**
	 * Apply post-processing to export data for fields that require special handling.
	 * Currently handles workDays conversion from JSON to comma-separated day names,
	 * resource_url construction with dynamic slug placeholders, and user field
	 * transformations.
	 */
	private void applyPostProcessing(List<DynamicExportResponseBodyDto> exportData, List<String> selectedFields) {
		// Determine what processing is needed
		PostProcessingRequirements requirements = this.determinePostProcessingRequirements(selectedFields);

		if (!requirements.hasAnyProcessing()) {
			return;
		}

		// Collect user IDs if needed
		Set<Integer> userIds = requirements.needsUserFieldProcessing() ? this.collectUserIds(exportData, selectedFields)
				: new HashSet<>();

		// Process each data row
		for (DynamicExportResponseBodyDto data : exportData) {
			this.processDataRow(data, requirements, userIds, selectedFields);
		}

		// Apply custom column type conversions if needed
		if (requirements.needsCustomColumnProcessing()) {
			this.processCustomColumnConversions(exportData, selectedFields);
		}
	}

	/**
	 * Determine what post-processing is required based on selected fields
	 */
	private PostProcessingRequirements determinePostProcessingRequirements(List<String> selectedFields) {
		boolean needsWorkDaysProcessing = selectedFields.contains(FIELD_WORK_DAYS);
		boolean needsResourceUrlProcessing = selectedFields.contains(FIELD_RESOURCE_URL);
		boolean needsUserFieldProcessing = selectedFields.contains(FIELD_OWNER_ID)
				|| selectedFields.contains(FIELD_CREATED_BY) || selectedFields.contains(FIELD_UPDATED_BY);
		boolean needsCustomColumnProcessing = selectedFields.stream()
			.anyMatch((field) -> field.startsWith("custcolumn"));

		return new PostProcessingRequirements(needsWorkDaysProcessing, needsResourceUrlProcessing,
				needsUserFieldProcessing, needsCustomColumnProcessing);
	}

	/**
	 * Process a single data row with the required transformations
	 */
	private void processDataRow(DynamicExportResponseBodyDto data, PostProcessingRequirements requirements,
			Set<Integer> userIds, List<String> selectedFields) {
		if (requirements.needsWorkDaysProcessing()) {
			this.processWorkDaysField(data);
		}

		if (requirements.needsResourceUrlProcessing()) {
			this.processResourceUrlField(data);
		}

		if (requirements.needsUserFieldProcessing()) {
			this.transformUserFields(data, selectedFields, userIds);
		}
	}

	/**
	 * Process workDays field conversion
	 */
	private void processWorkDaysField(DynamicExportResponseBodyDto data) {
		Object workDaysValue = data.getData().get(FIELD_WORK_DAYS);
		if (workDaysValue != null) {
			String workDaysJson = workDaysValue.toString();
			String convertedWorkDays = this.workDaysConverter.convertWorkDaysToNames(workDaysJson);
			data.getData().put(FIELD_WORK_DAYS, convertedWorkDays);
		}
	}

	/**
	 * Process resource_url field construction
	 */
	private void processResourceUrlField(DynamicExportResponseBodyDto data) {
		Object slugValue = data.getData().get(FIELD_RESOURCE_URL);
		if (slugValue != null && !slugValue.toString().trim().isEmpty()) {
			String baseUrl = this.buildBaseUrl();
			String constructedUrl = baseUrl + "v1/candidate/" + slugValue.toString();
			data.getData().put(FIELD_RESOURCE_URL, constructedUrl);
		}
	}

	/**
	 * Process custom column type conversions
	 */
	private void processCustomColumnConversions(List<DynamicExportResponseBodyDto> exportData,
			List<String> selectedFields) {
		List<Map<String, Object>> dataList = exportData.stream().map(DynamicExportResponseBodyDto::getData).toList();
		this.applyCustomColumnTypeConversions(dataList, selectedFields);
	}

	/**
	 * Collect all user IDs from the export data for batch processing
	 */
	private Set<Integer> collectUserIds(List<DynamicExportResponseBodyDto> exportData, List<String> selectedFields) {
		Set<Integer> userIds = new HashSet<>();

		for (DynamicExportResponseBodyDto data : exportData) {
			if (selectedFields.contains(FIELD_OWNER_ID)) {
				this.addUserIdIfValid(userIds, data.getData().get(FIELD_OWNER_ID));
			}
			if (selectedFields.contains(FIELD_CREATED_BY)) {
				this.addUserIdIfValid(userIds, data.getData().get(FIELD_CREATED_BY));
			}
			if (selectedFields.contains(FIELD_UPDATED_BY)) {
				this.addUserIdIfValid(userIds, data.getData().get(FIELD_UPDATED_BY));
			}
		}

		return userIds;
	}

	/**
	 * Add user ID to set if it's a valid integer
	 */
	private void addUserIdIfValid(Set<Integer> userIds, Object userIdValue) {
		if (userIdValue != null) {
			try {
				Integer userId = Integer.valueOf(userIdValue.toString());
				if (userId > 0) {
					userIds.add(userId);
				}
			}
			catch (NumberFormatException ex) {
				// Ignore invalid user IDs
			}
		}
	}

	/**
	 * Transform user fields from IDs to names using cached user data
	 */
	private void transformUserFields(DynamicExportResponseBodyDto data, List<String> selectedFields,
			Set<Integer> userIds) {
		// Fetch user data in batch if we haven't cached it yet
		Map<Integer, UserDetailsQueryResultDto> userDetailsMap = this.getUserDetailsWithCaching(userIds);

		if (selectedFields.contains(FIELD_OWNER_ID)) {
			this.transformUserField(data, FIELD_OWNER_ID, userDetailsMap);
		}
		if (selectedFields.contains(FIELD_CREATED_BY)) {
			this.transformUserField(data, FIELD_CREATED_BY, userDetailsMap);
		}
		if (selectedFields.contains(FIELD_UPDATED_BY)) {
			this.transformUserField(data, FIELD_UPDATED_BY, userDetailsMap);
		}
	}

	/**
	 * Transform a single user field from ID to name
	 */
	private void transformUserField(DynamicExportResponseBodyDto data, String fieldName,
			Map<Integer, UserDetailsQueryResultDto> userDetailsMap) {
		Object userIdValue = data.getData().get(fieldName);
		if (userIdValue != null) {
			try {
				Integer userId = Integer.valueOf(userIdValue.toString());
				String userName = this.userNameCache.computeIfAbsent(userId, (id) -> {
					UserDetailsQueryResultDto userDetails = userDetailsMap.get(id);
					return (userDetails != null && userDetails.getName() != null) ? userDetails.getName()
							: "Unknown User";
				});
				data.getData().put(fieldName, userName);
			}
			catch (NumberFormatException ex) {
				// Keep original value if not a valid integer
			}
		}
	}

	/**
	 * Get user details with caching to avoid repeated database calls
	 */
	private Map<Integer, UserDetailsQueryResultDto> getUserDetailsWithCaching(Set<Integer> userIds) {
		// Filter out user IDs that are already cached
		Set<Integer> uncachedUserIds = userIds.stream()
			.filter((userId) -> !this.userNameCache.containsKey(userId))
			.collect(Collectors.toSet());

		// Fetch uncached user details in batch
		if (!uncachedUserIds.isEmpty()) {
			try {
				return this.userRepository.getUserDetailsMap(uncachedUserIds);
			}
			catch (Exception ex) {
				// Return empty map if database call fails
				return new java.util.HashMap<>();
			}
		}

		return new java.util.HashMap<>();
	}

	/**
	 * Build the base URL for candidate resource links based on the application
	 * environment. For local environment: http://localhost:9000/ For other environments:
	 * https://{environment}.recruitcrm.io/
	 */
	private String buildBaseUrl() {
		if ("local".equalsIgnoreCase(this.applicationEnv)) {
			return "http://localhost:9000/";
		}
		return "https://" + this.applicationEnv + ".recruitcrm.io/";
	}

	/**
	 * Transform date column header to work hours column header. For biweekly/monthly
	 * periods, preserves full date to avoid collisions. "Thursday, 10 Jul 2025" ->
	 * "Thursday, 10 Jul 2025, Work Hours"
	 */
	private String transformToWorkHoursColumn(String dateColumn) {
		if (dateColumn != null && dateColumn.contains(",")) {
			// Keep the full date to handle biweekly/monthly periods with duplicate day
			// names
			return dateColumn + WORK_HOURS_SUFFIX;
		}
		return dateColumn + WORK_HOURS_SUFFIX;
	}

	/**
	 * Find the original date column that corresponds to a work hours column. "Thursday,
	 * 10 Jul 2025, Work Hours" -> "Thursday, 10 Jul 2025"
	 */
	private String findOriginalDateColumn(String workHoursColumn, Set<String> availableDateColumns) {
		if (workHoursColumn == null || !workHoursColumn.contains(WORK_HOURS_SUFFIX)) {
			return null;
		}

		// Extract the date part from work hours column
		// "Thursday, 10 Jul 2025, Work Hours" -> "Thursday, 10 Jul 2025"
		String dateColumn = workHoursColumn.replace(WORK_HOURS_SUFFIX, "").trim();

		// Check if this exact date column exists in available columns
		if (availableDateColumns.contains(dateColumn)) {
			return dateColumn;
		}

		return null;
	}

	/**
	 * Compare work hours columns for chronological sorting. Extracts date part and
	 * compares using existing date comparison logic to handle biweekly/monthly periods.
	 */
	private int compareWorkHoursColumns(String col1, String col2) {
		// Convert work hours columns back to comparable date format for sorting
		// "Thursday, 10 Jul 2025, Work Hours" -> "Thursday, 10 Jul 2025"
		String dateCol1 = col1.replace(WORK_HOURS_SUFFIX, "");
		String dateCol2 = col2.replace(WORK_HOURS_SUFFIX, "");

		// Use existing date comparison logic for proper chronological sorting
		return this.compareDateColumns(dateCol1, dateCol2);
	}

	/**
	 * Transform date column header to overtime hours column header. For biweekly/monthly
	 * periods, preserves full date to avoid collisions. "Thursday, 10 Jul 2025" ->
	 * "Thursday, 10 Jul 2025, Overtime Hours"
	 */
	private String transformToOvertimeHoursColumn(String dateColumn) {
		if (dateColumn != null && dateColumn.contains(",")) {
			// Keep the full date to handle biweekly/monthly periods with duplicate day
			// names
			return dateColumn + OVERTIME_HOURS_SUFFIX;
		}
		return dateColumn + OVERTIME_HOURS_SUFFIX;
	}

	/**
	 * Find the original date column that corresponds to an overtime hours column.
	 * "Thursday, 10 Jul 2025, Overtime Hours" -> "Thursday, 10 Jul 2025"
	 */
	private String findOriginalDateColumnForOvertime(String overtimeHoursColumn, Set<String> availableDateColumns) {
		if (overtimeHoursColumn == null || !overtimeHoursColumn.contains(OVERTIME_HOURS_SUFFIX)) {
			return null;
		}

		// Extract the date part from overtime hours column
		// "Thursday, 10 Jul 2025, Overtime Hours" -> "Thursday, 10 Jul 2025"
		String dateColumn = overtimeHoursColumn.replace(OVERTIME_HOURS_SUFFIX, "").trim();

		// Check if this exact date column exists in available columns
		if (availableDateColumns.contains(dateColumn)) {
			return dateColumn;
		}

		return null;
	}

	/**
	 * Compare overtime hours columns for chronological sorting. Extracts date part and
	 * compares using existing date comparison logic to handle biweekly/monthly periods.
	 */
	private int compareOvertimeHoursColumns(String col1, String col2) {
		// Convert overtime hours columns back to comparable date format for sorting
		// "Thursday, 10 Jul 2025, Overtime Hours" -> "Thursday, 10 Jul 2025"
		String dateCol1 = col1.replace(OVERTIME_HOURS_SUFFIX, "");
		String dateCol2 = col2.replace(OVERTIME_HOURS_SUFFIX, "");

		// Use existing date comparison logic for proper chronological sorting
		return this.compareDateColumns(dateCol1, dateCol2);
	}

	/**
	 * Apply proper column ordering for time-based fields when exportEachDay is true.
	 * Precedence per day: Date -> Work Hours -> Overtime Hours Example: "Monday, 7 Jul
	 * 2025" -> "Monday, Work Hours" -> "Monday, Overtime Hours"
	 *
	 * Caches sorted date column order once per period since all timesheets in a period
	 * share the same set of dates, avoiding redundant parsing and sorting.
	 */
	private void applyTimeFieldColumnOrdering(List<PeriodGroupedExportResponseBodyDto> groupedData,
			List<String> selectedFields) {
		boolean hasWorkHours = selectedFields.contains(FIELD_WORK_HOURS);
		boolean hasOvertimeHours = selectedFields.contains(FIELD_OVERTIME_HOURS);
		boolean hasEffectiveWorkHours = selectedFields.contains(FIELD_EFFECTIVE_WORK_HOURS);
		boolean hasBreakIntervals = selectedFields.contains(FIELD_BREAK_INTERVALS);
		boolean hasTimeLogRemarks = selectedFields.contains(FIELD_TIME_LOG_REMARKS);

		// Only apply if we have time-based fields
		if (!hasWorkHours && !hasOvertimeHours && !hasEffectiveWorkHours && !hasBreakIntervals && !hasTimeLogRemarks) {
			return;
		}

		for (PeriodGroupedExportResponseBodyDto period : groupedData) {
			List<String> sortedDatesForPeriod = this.computeSortedDateKeysForPeriod(period.getTimesheetsInPeriod());

			for (DynamicExportResponseBodyDto data : period.getTimesheetsInPeriod()) {
				this.reorderTimeFieldColumns(data, hasWorkHours, hasOvertimeHours, hasEffectiveWorkHours,
						hasBreakIntervals, hasTimeLogRemarks, sortedDatesForPeriod);
			}
		}
	}

	/**
	 * Compute sorted date keys once per period. All timesheets in a period share the same
	 * dates, so parsing and sorting is done once and reused.
	 */
	private List<String> computeSortedDateKeysForPeriod(List<DynamicExportResponseBodyDto> timesheetsInPeriod) {
		Set<String> allDateKeys = new HashSet<>();

		for (DynamicExportResponseBodyDto data : timesheetsInPeriod) {
			if (data.getColumnOrder() == null) {
				continue;
			}

			for (String column : data.getColumnOrder()) {
				if (this.isTimeBasedColumn(column)) {
					String dateKey = this.extractDateKeyFromColumn(column);
					allDateKeys.add(dateKey);
				}
			}
		}

		if (allDateKeys.isEmpty()) {
			return List.of();
		}

		Map<String, List<String>> dateKeysMap = new HashMap<>();
		for (String key : allDateKeys) {
			dateKeysMap.put(key, List.of());
		}

		return this.sortDatesByActualDate(dateKeysMap);
	}

	/**
	 * Reorder time field columns for a single export data object. Groups columns by full
	 * date to handle biweekly/monthly periods with duplicate day names, and applies
	 * precedence: Date -> Work Hours -> Overtime Hours
	 *
	 * For exportEachDay: true, enforces fixed ordering: 1. Timesheet ID, Contractor Name,
	 * Timesheet Period (fixed first 3) 2. Time log fields (effective hours, work hours,
	 * overtime hours) 3. Remaining user-selected fields
	 * @param sortedDatesForPeriod pre-computed sorted date keys for the period (avoids
	 * redundant parsing/sorting per timesheet)
	 */
	private void reorderTimeFieldColumns(DynamicExportResponseBodyDto data, boolean hasWorkHours,
			boolean hasOvertimeHours, boolean hasEffectiveWorkHours, boolean hasBreakIntervals,
			boolean hasTimeLogRemarks, List<String> sortedDatesForPeriod) {

		if (data.getColumnOrder() == null) {
			return;
		}

		List<String> currentColumnOrder = data.getColumnOrder();
		List<String> newColumnOrder = new ArrayList<>();

		// Separate time-based columns from regular columns
		List<String> regularColumns = new ArrayList<>();
		Map<String, List<String>> timeColumnsByDate = new HashMap<>();

		for (String column : currentColumnOrder) {
			if (this.isTimeBasedColumn(column)) {
				String dateKey = this.extractDateKeyFromColumn(column);
				timeColumnsByDate.computeIfAbsent(dateKey, (k) -> new ArrayList<>()).add(column);
			}
			else {
				regularColumns.add(column);
			}
		}

		// Apply fixed ordering for exportEachDay: true (uses cached sorted dates)
		this.applyFixedColumnOrdering(FixedColumnOrderingRequest.builder()
			.newColumnOrder(newColumnOrder)
			.regularColumns(regularColumns)
			.timeColumnsByDate(timeColumnsByDate)
			.hasWorkHours(hasWorkHours)
			.hasOvertimeHours(hasOvertimeHours)
			.hasEffectiveWorkHours(hasEffectiveWorkHours)
			.hasBreakIntervals(hasBreakIntervals)
			.hasTimeLogRemarks(hasTimeLogRemarks)
			.sortedDatesForPeriod(sortedDatesForPeriod)
			.build());

		// Update the column order
		data.setColumnOrder(newColumnOrder);
	}

	/**
	 * Apply fixed column ordering for exportEachDay: true Order: Fixed first 3 fields ->
	 * Time log fields -> Remaining fields
	 * @param sortedDatesForPeriod pre-computed sorted date keys for the period, or empty
	 * to compute inline
	 */
	private void applyFixedColumnOrdering(FixedColumnOrderingRequest fixedColumnOrderingRequest) {

		// Define the fixed first 3 fields (in order)
		List<String> fixedFirstFields = List.of(FIELD_TIMESHEET_ID, FIELD_CONTRACTOR, FIELD_TIMESHEET_PERIOD);

		// 1. Add fixed first 3 fields if they exist in regular columns
		for (String fixedField : fixedFirstFields) {
			if (fixedColumnOrderingRequest.getRegularColumns().contains(fixedField)) {
				fixedColumnOrderingRequest.getNewColumnOrder().add(fixedField);
			}
		}

		// 2. Add time log fields (sorted chronologically by date) - use cached order when
		// available
		List<String> sortedDates = fixedColumnOrderingRequest.getSortedDatesForPeriod().isEmpty()
				? this.sortDatesByActualDate(fixedColumnOrderingRequest.getTimeColumnsByDate())
				: fixedColumnOrderingRequest.getSortedDatesForPeriod();

		for (String dateKey : sortedDates) {
			List<String> dateColumns = fixedColumnOrderingRequest.getTimeColumnsByDate().get(dateKey);
			if (dateColumns == null) {
				continue;
			}

			String dayName = this.extractDayFromColumn(dateKey);

			// Apply precedence: Effective Work Hours -> Work Hours -> Break Intervals ->
			// Remarks -> Overtime Hours
			this.addColumnsInPrecedenceOrder(fixedColumnOrderingRequest, dateColumns, dayName);
		}

		// 3. Add remaining regular columns (excluding the fixed first 3)
		for (String column : fixedColumnOrderingRequest.getRegularColumns()) {
			if (!fixedFirstFields.contains(column)
					&& !fixedColumnOrderingRequest.getNewColumnOrder().contains(column)) {
				fixedColumnOrderingRequest.getNewColumnOrder().add(column);
			}
		}
	}

	/**
	 * Add time columns for a specific day in precedence order. Handles new format where
	 * work/overtime hours include full date.
	 */
	private void addColumnsInPrecedenceOrder(FixedColumnOrderingRequest fixedColumnOrderingRequest,
			List<String> dayColumns, String day) {

		// 1. First: Effective work hours (Date format: "Monday, 7 Jul 2025")
		addEffectiveWorkHours(fixedColumnOrderingRequest, dayColumns, day);

		// 2. Second: Work hours (New format: "Monday, 7 Jul 2025, Work Hours")
		addWorkHours(fixedColumnOrderingRequest, dayColumns, day);

		// 3. Third: Break intervals (New format: "Monday, 7 Jul 2025, Break Intervals")
		addBreakIntervals(fixedColumnOrderingRequest, dayColumns, day);

		// 4. Fourth: Remarks (New format: "Monday, 7 Jul 2025, Remarks")
		addRemarks(fixedColumnOrderingRequest, dayColumns, day);

		// 5. Fifth: Overtime hours (New format: "Monday, 7 Jul 2025, Overtime Hours")
		addOvertimeHours(fixedColumnOrderingRequest, dayColumns, day);
	}

	private void addOvertimeHours(FixedColumnOrderingRequest fixedColumnOrderingRequest, List<String> dayColumns,
			String day) {
		if (fixedColumnOrderingRequest.isHasOvertimeHours()) {
			String overtimeHoursColumn = dayColumns.stream()
				.filter((col) -> col.contains(OVERTIME_HOURS_SUFFIX) && col.startsWith(day + ","))
				.findFirst()
				.orElse(null);
			if (overtimeHoursColumn != null) {
				fixedColumnOrderingRequest.getNewColumnOrder().add(overtimeHoursColumn);
			}
		}
	}

	private void addRemarks(FixedColumnOrderingRequest fixedColumnOrderingRequest, List<String> dayColumns,
			String day) {
		if (fixedColumnOrderingRequest.isHasTimeLogRemarks()) {
			String remarksColumn = dayColumns.stream()
				.filter((col) -> col.contains(REMARKS_SUFFIX) && col.startsWith(day + ","))
				.findFirst()
				.orElse(null);
			if (remarksColumn != null) {
				fixedColumnOrderingRequest.getNewColumnOrder().add(remarksColumn);
			}
		}
	}

	private void addBreakIntervals(FixedColumnOrderingRequest fixedColumnOrderingRequest, List<String> dayColumns,
			String day) {
		if (fixedColumnOrderingRequest.isHasBreakIntervals()) {
			String breakIntervalsColumn = dayColumns.stream()
				.filter((col) -> col.contains(BREAK_INTERVALS_SUFFIX) && col.startsWith(day + ","))
				.findFirst()
				.orElse(null);
			if (breakIntervalsColumn != null) {
				fixedColumnOrderingRequest.getNewColumnOrder().add(breakIntervalsColumn);
			}
		}
	}

	private void addWorkHours(FixedColumnOrderingRequest fixedColumnOrderingRequest, List<String> dayColumns,
			String day) {
		if (fixedColumnOrderingRequest.isHasWorkHours()) {
			String workHoursColumn = dayColumns.stream()
				.filter((col) -> col.contains(WORK_HOURS_SUFFIX) && col.startsWith(day + ","))
				.findFirst()
				.orElse(null);
			if (workHoursColumn != null) {
				fixedColumnOrderingRequest.getNewColumnOrder().add(workHoursColumn);
			}
		}
	}

	private void addEffectiveWorkHours(FixedColumnOrderingRequest fixedColumnOrderingRequest, List<String> dayColumns,
			String day) {
		if (fixedColumnOrderingRequest.isHasEffectiveWorkHours()) {
			String effectiveWorkHoursColumn = this.findColumnByPattern(dayColumns, day + ",", "Work Hours",
					"Overtime Hours", "Break Intervals", "Remarks");
			if (effectiveWorkHoursColumn != null) {
				fixedColumnOrderingRequest.getNewColumnOrder().add(effectiveWorkHoursColumn);
			}
		}
	}

	/**
	 * Check if a column is time-based (contains date, work hours, or overtime hours).
	 * Handles both old format ("Monday, Work Hours") and new format ("Monday, 7 Jul 2025,
	 * Work Hours").
	 */
	private boolean isTimeBasedColumn(String column) {
		return column.contains(WORK_HOURS_SUFFIX) || column.contains(OVERTIME_HOURS_SUFFIX)
				|| column.contains(BREAK_INTERVALS_SUFFIX) || column.contains(REMARKS_SUFFIX)
				|| (column.contains(",") && column.matches(".*\\b\\d{1,2}\\s+\\w{3}\\s+\\d{4}\\b.*"));
	}

	/**
	 * Extract day name from a time-based column. Handles both old format ("Monday, Work
	 * Hours") and new format ("Monday, 7 Jul 2025, Work Hours").
	 */
	private String extractDayFromColumn(String column) {
		if (column.contains(",")) {
			return column.split(",")[0].trim();
		}
		return column;
	}

	/**
	 * Extract a unique date key from a time-based column to handle biweekly/monthly
	 * periods. For date columns like "Monday, 7 Jul 2025", returns the full string. For
	 * work/overtime columns like "Monday, 7 Jul 2025, Work Hours", extracts the date
	 * part.
	 */
	private String extractDateKeyFromColumn(String column) {
		// If it's already a date column (contains date pattern), use it as is
		if (column.matches(".*\\b\\d{1,2}\\s+\\w{3}\\s+\\d{4}\\b.*") && !column.contains(WORK_HOURS_SUFFIX)
				&& !column.contains(OVERTIME_HOURS_SUFFIX) && !column.contains(BREAK_INTERVALS_SUFFIX)
				&& !column.contains(REMARKS_SUFFIX)) {
			return column;
		}

		// If it's a work hours column, extract the date part
		if (column.contains(WORK_HOURS_SUFFIX)) {
			return column.replace(WORK_HOURS_SUFFIX, "").trim();
		}

		// If it's a break intervals column, extract the date part
		if (column.contains(BREAK_INTERVALS_SUFFIX)) {
			return column.replace(BREAK_INTERVALS_SUFFIX, "").trim();
		}

		// If it's a remarks column, extract the date part
		if (column.contains(REMARKS_SUFFIX)) {
			return column.replace(REMARKS_SUFFIX, "").trim();
		}

		// If it's an overtime hours column, extract the date part
		if (column.contains(OVERTIME_HOURS_SUFFIX)) {
			return column.replace(OVERTIME_HOURS_SUFFIX, "").trim();
		}

		// Fallback: use the original column as key
		return column;
	}

	/**
	 * Find column that starts with pattern but doesn't contain exclusions.
	 */
	private String findColumnByPattern(List<String> columns, String startsWith, String... exclusions) {
		return columns.stream().filter((col) -> col.startsWith(startsWith)).filter((col) -> {
			for (String exclusion : exclusions) {
				if (col.contains(exclusion)) {
					return false;
				}
			}
			return true;
		}).findFirst().orElse(null);
	}

	/**
	 * Sort date keys by actual chronological date instead of alphabetical day name.
	 * Handles biweekly/monthly periods with multiple dates per day name.
	 */
	private List<String> sortDatesByActualDate(Map<String, List<String>> timeColumnsByDate) {
		// Create a map of date key to actual timestamp for sorting
		Map<String, Integer> dateToUnixTimestamp = new HashMap<>();

		for (Map.Entry<String, List<String>> entry : timeColumnsByDate.entrySet()) {
			String dateKey = entry.getKey();
			List<String> dateColumns = entry.getValue();

			// Extract timestamp from the date key (which should be a full date string)
			Integer unixTimestamp = this.extractUnixTimestampFromDateColumn(dateKey);
			if (unixTimestamp != null) {
				dateToUnixTimestamp.put(dateKey, unixTimestamp);
			}
			else {
				// If dateKey is not a date column, try to find one in the columns
				String dateColumn = this.findDateColumnForDay(dateColumns, this.extractDayFromColumn(dateKey));
				if (dateColumn != null) {
					unixTimestamp = this.extractUnixTimestampFromDateColumn(dateColumn);
					if (unixTimestamp != null) {
						dateToUnixTimestamp.put(dateKey, unixTimestamp);
					}
				}
			}
		}

		// Sort date keys by their actual dates (unix timestamps)
		return timeColumnsByDate.keySet().stream().sorted((date1, date2) -> {
			Integer timestamp1 = dateToUnixTimestamp.get(date1);
			Integer timestamp2 = dateToUnixTimestamp.get(date2);

			// If we have timestamps for both dates, compare them
			if (timestamp1 != null && timestamp2 != null) {
				return timestamp1.compareTo(timestamp2);
			}

			// Fallback to alphabetical if we can't determine dates
			return date1.compareTo(date2);
		}).toList();
	}

	/**
	 * Find a date column (effective work hours format) for a specific day. Looks for
	 * columns like "Monday, 18 Aug 2025" that contain actual dates.
	 */
	private String findDateColumnForDay(List<String> dayColumns, String dayName) {
		return dayColumns.stream()
			.filter((col) -> col.startsWith(dayName + ","))
			.filter((col) -> !col.contains(WORK_HOURS_SUFFIX))
			.filter((col) -> !col.contains(OVERTIME_HOURS_SUFFIX))
			.filter((col) -> !col.contains(BREAK_INTERVALS_SUFFIX))
			.filter((col) -> !col.contains(REMARKS_SUFFIX))
			.filter((col) -> col.matches(".*\\b\\d{1,2}\\s+\\w{3}\\s+\\d{4}\\b.*"))
			.findFirst()
			.orElse(null);
	}

	/**
	 * Extract unix timestamp from a date column header. Parses "Monday, 18 Aug 2025"
	 * format and converts to unix timestamp.
	 */
	private Integer extractUnixTimestampFromDateColumn(String dateColumn) {
		try {
			// Extract date part from "Monday, 18 Aug 2025"
			if (dateColumn.contains(",")) {
				String datePart = dateColumn.split(",", 2)[1].trim();

				// Parse the date format "18 Aug 2025"
				java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
					.ofPattern("d MMM yyyy", java.util.Locale.ENGLISH);
				java.time.LocalDate localDate = java.time.LocalDate.parse(datePart, formatter);

				// Convert to unix timestamp (start of day in UTC)
				java.time.ZonedDateTime zonedDateTime = localDate.atStartOfDay(java.time.ZoneId.of("UTC"));
				return (int) zonedDateTime.toEpochSecond();
			}
		}
		catch (Exception ex) {
			// If parsing fails, return null and fall back to alphabetical sorting
		}

		return null;
	}

	/**
	 * Apply custom column type conversions to timesheets data. This method handles field
	 * type conversions for custom columns based on their configuration in Tblextrafields.
	 */
	private void applyCustomColumnTypeConversions(List<Map<String, Object>> timesheetsData,
			List<String> selectedFields) {
		if (timesheetsData.isEmpty()) {
			return;
		}

		// Extract custom column fields from selected fields
		List<String> customColumnFields = selectedFields.stream()
			.filter((field) -> field.startsWith("custcolumn"))
			.toList();

		if (customColumnFields.isEmpty()) {
			return; // No custom columns to process
		}

		// Extract column IDs and get their field types
		List<Integer> columnIds = customColumnFields.stream().map((field) -> {
			try {
				return Integer.parseInt(field.substring("custcolumn".length()));
			}
			catch (NumberFormatException ex) {
				return null;
			}
		}).filter((id) -> id != null && id >= 1 && id <= 150).toList();

		if (columnIds.isEmpty()) {
			return; // No valid custom column IDs
		}

		// Get field types for all custom columns in one batch call
		Map<Integer, String> fieldTypes = this.customColumnTypeService.getFieldTypes(columnIds);

		// Process each timesheet record
		for (Map<String, Object> timesheet : timesheetsData) {
			for (String customColumnField : customColumnFields) {
				try {
					// Extract column ID from field name
					int columnId = Integer.parseInt(customColumnField.substring("custcolumn".length()));
					String fieldType = fieldTypes.get(columnId);

					if (fieldType != null) {
						// Get raw value from timesheet data
						Object rawValue = timesheet.get(customColumnField);

						// Convert value based on field type
						String convertedValue = this.customColumnTypeService.convertValue(rawValue, fieldType);

						// Update timesheet data with converted value
						timesheet.put(customColumnField, convertedValue);
					}
				}
				catch (NumberFormatException ex) {
					// Skip invalid custom column field names
				}
			}
		}
	}

	private record ExportMetadata(String suggestedFilename, long recordCount, boolean periodGrouped) {
	}

}