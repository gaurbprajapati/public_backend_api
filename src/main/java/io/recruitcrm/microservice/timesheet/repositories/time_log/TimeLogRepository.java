package io.recruitcrm.microservice.timesheet.repositories.time_log;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.BreakInterval;
import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.contract_staffing.entity.model.TimeLogInterval;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimeLogT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblassignjobcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbljob;
import io.recruitcrm.microservice.timesheet.dao.time_log.TimeLogJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimesheetJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogWithSettingQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetLogQueryResultDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.helpers.constants.RepositoryParameterConstants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Repository
public class TimeLogRepository implements ITimeLogRepository {

	/**
	 * Batch size: 500 records per query
	 */
	private static final int TIME_LOG_BATCH_SIZE = 500;

	private final TimeLogJpaRepository timeLogJpaRepository;

	private final EntityManager entityManager;

	private final DSLContext dslContext;

	private final TimeLogIntervalRepository timeLogIntervalRepository;

	private static final int WORK_LOG_TYPE_ENTER_START_END_TIME = 2;

	// Table aliases for consistent usage
	private static final CstTimeLogT TL = CstTimeLogT.CST_TIME_LOG_T.as("tl");

	private static final CstTimesheetT TS = CstTimesheetT.CST_TIMESHEET_T.as("ts");

	private static final CstTimesheetSettingT TSS = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T.as("tss");

	public TimeLogRepository(EntityManager entityManager, TimeLogJpaRepository timeLogJpaRepository,
			DSLContext dslContext, TimeLogIntervalRepository timeLogIntervalRepository) {
		this.timeLogJpaRepository = timeLogJpaRepository;
		this.entityManager = entityManager;
		this.dslContext = dslContext;
		this.timeLogIntervalRepository = timeLogIntervalRepository;
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void createBulkTimesheetLogs(List<TimeLog> timeLogs) {
		this.timeLogJpaRepository.saveAll(timeLogs);
	}

	@Override
	@WriterRoute
	@Transactional
	public void createTimesheetLog(Integer date, Integer dateTypeId, Integer timesheetId) {
		TimeLog timesheetLog = new TimeLog();
		timesheetLog.setTimesheetId(timesheetId);
		timesheetLog.setDate(date);
		timesheetLog.setDayTypeId(dateTypeId);
		this.timeLogJpaRepository.save(timesheetLog);
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveTimeLog(TimeLog timeLog) {
		this.timeLogJpaRepository.save(timeLog);
	}

	private static final String VALIDATE_BY_START_DATE_JPQL = "SELECT COUNT(tl) FROM TimeLog tl "
			+ "JOIN tl.timesheet t JOIN t.timesheetSetting ts " + "WHERE tl.date < :date "
			+ "AND ts.association.jobId = :jobId AND ts.association.contractorId = :contractorId "
			+ "AND ts.accountId = :accountId";

	private static final String VALIDATE_BY_END_DATE_JPQL = "SELECT COUNT(tl) FROM TimeLog tl "
			+ "JOIN tl.timesheet t JOIN t.timesheetSetting ts " + "WHERE tl.date > :date "
			+ "AND ts.association.jobId = :jobId AND ts.association.contractorId = :contractorId "
			+ "AND ts.accountId = :accountId";

	@Override
	public Boolean validateByDate(Integer jobId, Integer contractorId, Long startDate, Long endDate,
			Integer accountId) {
		Long date = (startDate != null) ? startDate : endDate;
		String jpql = (startDate != null) ? VALIDATE_BY_START_DATE_JPQL : VALIDATE_BY_END_DATE_JPQL;

		TypedQuery<Long> query = this.entityManager.createQuery(jpql, Long.class);
		query.setParameter("date", date);
		query.setParameter("jobId", jobId);
		query.setParameter("contractorId", contractorId);
		query.setParameter(RepositoryParameterConstants.ACCOUNT_ID, accountId);

		return query.getSingleResult() == 0;
	}

	private static final String TIMESHEET_LOG_WITH_INVOICE_JPQL = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetLogQueryResultDto("
			+ "ts.id, tss.id, tss.workLogType, tss.timesheetFrequency, tss.timesheetStartDay, tss.calculateBreakTime, "
			+ "tss.breakTimeThreshold, tss.isRemarkMandatory, ts.periodStart, ts.periodEnd, ta.timesheetApprovalStatusTypeId, "
			+ "ti.paymentStatusId, ti.paymentPaidOn, ti.payoutNumber, ti.billingStatusId, "
			+ "i.createdOn, CASE WHEN (i.invoiceIdPrefix IS NULL OR i.invoiceIdPrefix = '') THEN i.invoiceIdNumber ELSE CONCAT(CONCAT(i.invoiceIdPrefix, '-'), i.invoiceIdNumber) END, i.invoiceStatus, "
			+ "ta.remark, ta.createdOn, payCurr.symbol, payCurr.code, billCurr.symbol, billCurr.code, ta.userTypeId, ta.entityId, "
			+ "tss.templateWorkDay, tss.customRule, tss.isUnplannedHoursPayEnabled) " + "FROM Timesheet ts "
			+ "JOIN TimesheetSetting tss ON ts.timesheetSettingId = tss.id "
			+ "LEFT JOIN TimesheetApproval ta ON ta.timesheetId = ts.id AND ta.id = ("
			+ "    SELECT MAX(ta2.id) FROM TimesheetApproval ta2 WHERE ta2.timesheetId = ts.id) "
			+ "LEFT JOIN TimesheetInvoice ti ON ti.timesheetId = ts.id " + "LEFT JOIN Invoice i ON i.id = ti.invoiceId "
			+ "LEFT JOIN Currency payCurr ON payCurr.id = tss.payCurrencyId "
			+ "LEFT JOIN Currency billCurr ON billCurr.id = tss.billCurrencyId " + "WHERE ts.id = :timesheetId";

	private static final String TIMESHEET_LOG_WITHOUT_INVOICE_JPQL = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetLogQueryResultDto("
			+ "ts.id, tss.id, tss.workLogType, tss.timesheetFrequency, tss.timesheetStartDay, tss.calculateBreakTime, "
			+ "tss.breakTimeThreshold, tss.isRemarkMandatory, ts.periodStart, ts.periodEnd, ta.timesheetApprovalStatusTypeId, "
			+ "ti.paymentStatusId, ti.paymentPaidOn, ti.payoutNumber, ti.billingStatusId, " + "null, null, null, "
			+ "ta.remark, ta.createdOn, payCurr.symbol, payCurr.code, billCurr.symbol, billCurr.code, ta.userTypeId, ta.entityId, "
			+ "tss.templateWorkDay, tss.customRule, tss.isUnplannedHoursPayEnabled) " + "FROM Timesheet ts "
			+ "JOIN TimesheetSetting tss ON ts.timesheetSettingId = tss.id "
			+ "LEFT JOIN TimesheetApproval ta ON ta.timesheetId = ts.id AND ta.id = ("
			+ "    SELECT MAX(ta2.id) FROM TimesheetApproval ta2 WHERE ta2.timesheetId = ts.id) "
			+ "LEFT JOIN TimesheetInvoice ti ON ti.timesheetId = ts.id "
			+ "LEFT JOIN Currency payCurr ON payCurr.id = tss.payCurrencyId "
			+ "LEFT JOIN Currency billCurr ON billCurr.id = tss.billCurrencyId " + "WHERE ts.id = :timesheetId";

	public TimesheetLogQueryResultDto getTimeLogByTimesheetId(Integer timesheetId) {

		List<Integer> result = this.entityManager
			.createQuery("SELECT ti.invoiceId FROM TimesheetInvoice ti WHERE ti.timesheetId = :timesheetId",
					Integer.class)
			.setParameter("timesheetId", timesheetId)
			.getResultList();

		Integer invoiceId = result.isEmpty() ? null : result.get(0);

		String jpql = (invoiceId != null) ? TIMESHEET_LOG_WITH_INVOICE_JPQL : TIMESHEET_LOG_WITHOUT_INVOICE_JPQL;

		TypedQuery<TimesheetLogQueryResultDto> query = this.entityManager.createQuery(jpql,
				TimesheetLogQueryResultDto.class);
		query.setParameter(RepositoryParameterConstants.TIMESHEET_ID, timesheetId);
		return query.getSingleResult();

	}

	public List<TimelogQueryResultDto> findTimeLogsWithDetails(List<Integer> timesheetIds, Integer accountId) {
		String jpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogQueryResultDto("
				+ "tl.id, tl.date, tl.dayTypeId, tl.workTime, tl.workStartTime, tl.workEndTime, "
				+ "tl.breakTime, tl.overTime, tl.remark, tl.totalTime, tl.timesheetId, "
				+ "t.periodStart, t.periodEnd) " + "FROM TimeLog tl "
				+ "LEFT JOIN Timesheet t ON t.id = tl.timesheetId "
				+ "LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "WHERE tl.timesheetId IN :timesheetIds AND t.accountId = :accountId "
				+ "ORDER BY tl.timesheetId, tl.date, tl.id";

		TypedQuery<TimelogQueryResultDto> query = this.entityManager.createQuery(jpql, TimelogQueryResultDto.class);
		query.setParameter(RepositoryParameterConstants.TIMESHEET_IDS, timesheetIds);
		query.setParameter(RepositoryParameterConstants.ACCOUNT_ID, accountId);

		return query.getResultList();
	}

	public List<TimelogWithSettingQueryResultDto> findTimeLogsWithSettingDetails(List<Integer> timesheetIds,
			Integer accountId) {
		String jpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogWithSettingQueryResultDto("
				+ "tl.id, tl.date, tl.dayTypeId, tl.workTime, tl.workStartTime, tl.workEndTime, "
				+ "tl.breakTime, tl.overTime, tl.remark, tl.totalTime, tl.timesheetId, "
				+ "t.periodStart, t.periodEnd, ts.id, ts.calculateBreakTime, ts.breakTimeThreshold, ts.templateWorkDay, ts.isRemarkMandatory) "
				+ "FROM TimeLog tl " + "LEFT JOIN Timesheet t ON t.id = tl.timesheetId "
				+ "LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "WHERE tl.timesheetId IN :timesheetIds AND t.accountId = :accountId "
				+ "ORDER BY tl.timesheetId, tl.date, tl.id";

		TypedQuery<TimelogWithSettingQueryResultDto> query = this.entityManager.createQuery(jpql,
				TimelogWithSettingQueryResultDto.class);
		query.setParameter(RepositoryParameterConstants.TIMESHEET_IDS, timesheetIds);
		query.setParameter(RepositoryParameterConstants.ACCOUNT_ID, accountId);

		return query.getResultList();
	}

	public List<Object[]> findTimesheetSettingsForTimesheets(List<Integer> timesheetIds, Integer accountId) {
		String jpql = "SELECT DISTINCT t.id, ts.calculateBreakTime, ts.breakTimeThreshold, ts.templateWorkDay, ts.isRemarkMandatory "
				+ "FROM Timesheet t " + "LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "WHERE t.id IN :timesheetIds AND t.accountId = :accountId";

		TypedQuery<Object[]> query = this.entityManager.createQuery(jpql, Object[].class);
		query.setParameter(RepositoryParameterConstants.TIMESHEET_IDS, timesheetIds);
		query.setParameter(RepositoryParameterConstants.ACCOUNT_ID, accountId);

		return query.getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Integer, TimesheetJobQueryResultDto> findCompanyByTimesheetIds(List<Integer> timesheetIds,
			Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return Collections.emptyMap();
		}

		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tss = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsa = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var job = Tbljob.TBLJOB;
		var assignJobCandidate = Tblassignjobcandidate.TBLASSIGNJOBCANDIDATE;

		var records = this.dslContext
			.select(ts.ID.as(RepositoryParameterConstants.TIMESHEET_ID), job.ID.as(RepositoryParameterConstants.JOB_ID),
					job.NAME.as("jobName"), job.SLUG.as("jobSlug"), assignJobCandidate.ID.as("assignmentId"))
			.from(ts)
			.innerJoin(tss)
			.on(ts.TIMESHEET_SETTING_ID.eq(tss.ID))
			.innerJoin(tsa)
			.on(tss.ASSOCIATION_ID.eq(tsa.ID))
			.innerJoin(job)
			.on(tsa.JOB_ID.eq(job.ID))
			.leftJoin(assignJobCandidate)
			.on(assignJobCandidate.JOBID.eq(job.ID).and(assignJobCandidate.CANDIDATEID.eq(tsa.CONTRACTOR_ID)))
			.where(ts.ID.in(timesheetIds))
			.and(ts.ACCOUNT_ID.eq(accountId))
			.fetch();

		return records.stream()
			.collect(Collectors.toMap((entry) -> entry.get(RepositoryParameterConstants.TIMESHEET_ID, Integer.class),
					(entry) -> new TimesheetJobQueryResultDto(
							entry.get(RepositoryParameterConstants.TIMESHEET_ID, Integer.class),
							entry.get(RepositoryParameterConstants.JOB_ID, Integer.class),
							entry.get("jobName", String.class), entry.get("jobSlug", String.class),
							entry.get("assignmentId", Integer.class)),
					(existing, replacement) -> existing));
	}

	/**
	 * Batch fetch formatted time logs for multiple timesheets. This method replaces N
	 * individual queries with 1 optimized batch query.
	 *
	 * Performance: Reduces database calls from N+1 to 2 (base query + this batch query)
	 * Approach: Fetch individual records and group/format in Java to avoid JOOQ
	 * GROUP_CONCAT issues
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<Integer, String> getTimeLogsForTimesheets(List<Integer> timesheetIds, Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return Collections.emptyMap();
		}

		try {
			var timeLogRecords = this.fetchTimeLogRecords(timesheetIds, accountId);
			Map<Integer, List<String>> timeLogsByTimesheet = this.processTimeLogRecords(timeLogRecords);
			return this.convertToFinalFormat(timeLogsByTimesheet);
		}
		catch (Exception ex) {
			throw new ResourceNotFoundException("Failed to fetch time logs for " + timesheetIds.size() + " timesheets");
		}
	}

	/**
	 * Fetch time log records from database (includes TL.ID for interval lookup)
	 */
	private org.jooq.Result<org.jooq.Record10<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> fetchTimeLogRecords(
			List<Integer> timesheetIds, Integer accountId) {
		return this.dslContext
			.select(TL.TIMESHEET_ID, TL.DATE, TL.WORK_TIME, TL.WORK_START_TIME, TL.WORK_END_TIME, TL.OVER_TIME,
					TL.TOTAL_TIME, TSS.WORK_LOG_TYPE, TS.PERIOD_START, TS.PERIOD_END)
			.from(TL)
			.join(TS)
			.on(TL.TIMESHEET_ID.eq(TS.ID))
			.join(TSS)
			.on(TS.TIMESHEET_SETTING_ID.eq(TSS.ID))
			.where(TL.TIMESHEET_ID.in(timesheetIds))
			.and(TS.ACCOUNT_ID.eq(accountId))
			.orderBy(TL.TIMESHEET_ID, TL.DATE)
			.fetch();
	}

	/**
	 * Fetch time log records for structured export (includes TL.ID for multiple interval
	 * support)
	 */
	private org.jooq.Result<? extends org.jooq.Record> fetchTimeLogRecordsForStructuredExport(
			List<Integer> timesheetIds, Integer accountId) {
		return this.dslContext
			.select(TL.ID, TL.TIMESHEET_ID, TL.DATE, TL.WORK_TIME, TL.WORK_START_TIME, TL.WORK_END_TIME, TL.OVER_TIME,
					TL.TOTAL_TIME, TSS.WORK_LOG_TYPE, TS.PERIOD_START, TS.PERIOD_END)
			.from(TL)
			.join(TS)
			.on(TL.TIMESHEET_ID.eq(TS.ID))
			.join(TSS)
			.on(TS.TIMESHEET_SETTING_ID.eq(TSS.ID))
			.where(TL.TIMESHEET_ID.in(timesheetIds))
			.and(TS.ACCOUNT_ID.eq(accountId))
			.orderBy(TL.TIMESHEET_ID, TL.DATE)
			.fetch();
	}

	/**
	 * Batch fetch intervals grouped by time log ID for comma-separated work hours display
	 */
	private Map<Integer, List<TimeLogInterval>> fetchIntervalsByTimeLogIds(List<Integer> timeLogIds) {
		if (timeLogIds == null || timeLogIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<TimeLogInterval> intervals = this.timeLogIntervalRepository.findByTimeLogIdIn(timeLogIds);
		Map<Integer, List<TimeLogInterval>> byTimeLogId = new HashMap<>();
		for (TimeLogInterval interval : intervals) {
			if (interval != null && interval.getTimeLogId() != null && !this.isDeletionMarker(interval)) {
				byTimeLogId.computeIfAbsent(interval.getTimeLogId(), (k) -> new ArrayList<>()).add(interval);
			}
		}
		return byTimeLogId;
	}

	private boolean isDeletionMarker(TimeLogInterval interval) {
		return interval.getWorkStartTime() != null && interval.getWorkStartTime() == -1
				&& interval.getWorkEndTime() != null && interval.getWorkEndTime() == -1;
	}

	/**
	 * Process time log records and group by timesheet
	 */
	private Map<Integer, List<String>> processTimeLogRecords(
			org.jooq.Result<org.jooq.Record10<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> timeLogRecords) {
		Map<Integer, List<String>> timeLogsByTimesheet = new HashMap<>();

		for (var timeLogRecord : timeLogRecords) {
			Integer timesheetId = timeLogRecord.get(TL.TIMESHEET_ID);
			Integer date = timeLogRecord.get(TL.DATE);
			Integer periodStart = timeLogRecord.get(TS.PERIOD_START);
			Integer periodEnd = timeLogRecord.get(TS.PERIOD_END);

			// Filter time logs to only include dates within THIS timesheet's specific
			// period
			if (this.isDateOutsidePeriod(date, periodStart, periodEnd)) {
				continue;
			}

			String timeLogEntry = this.createTimeLogEntry(timeLogRecord);
			timeLogsByTimesheet.computeIfAbsent(timesheetId, (k) -> new ArrayList<>()).add(timeLogEntry);
		}

		return timeLogsByTimesheet;
	}

	/**
	 * Check if date is outside the timesheet period
	 */
	private boolean isDateOutsidePeriod(Integer date, Integer periodStart, Integer periodEnd) {
		return date < periodStart || date > periodEnd;
	}

	/**
	 * Create a formatted time log entry from a record
	 */
	private String createTimeLogEntry(
			org.jooq.Record10<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> timeLogRecord) {
		Integer date = timeLogRecord.get(TL.DATE);
		Integer workTime = timeLogRecord.get(TL.WORK_TIME);
		Integer workStartTime = timeLogRecord.get(TL.WORK_START_TIME);
		Integer workEndTime = timeLogRecord.get(TL.WORK_END_TIME);
		Integer workLogType = timeLogRecord.get(TSS.WORK_LOG_TYPE);

		String formattedDate = this.formatUnixTimestampToDate(date);
		String formattedWorkTime = this.formatWorkTimeByType(workLogType, workTime, workStartTime, workEndTime);

		return formattedDate + ": " + formattedWorkTime;
	}

	/**
	 * Format Unix timestamp to readable date
	 */
	private String formatUnixTimestampToDate(Integer date) {
		return java.time.Instant.ofEpochSecond(date)
			.atZone(java.time.ZoneId.of("UTC"))
			.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
	}

	/**
	 * Format work time based on work log type
	 */
	private String formatWorkTimeByType(Integer workLogType, Integer workTime, Integer workStartTime,
			Integer workEndTime) {
		if (workLogType != null && workLogType == 1) {
			return this.formatWorkHourType(workTime);
		}
		else if (workLogType != null && workLogType == 2) {
			return this.formatStartEndTimeType(workStartTime, workEndTime);
		}
		else {
			return "No time logged";
		}
	}

	/**
	 * Format work hour type (decimal hours)
	 */
	private String formatWorkHourType(Integer workTime) {
		if (workTime != null) {
			double hours = workTime / 3600.0;
			return String.format("%.2f", hours);
		}
		return "";
	}

	/**
	 * Format start and end time type (time range)
	 */
	private String formatStartEndTimeType(Integer workStartTime, Integer workEndTime) {
		String startTime = this.formatSecondsToTime(workStartTime);
		String endTime = this.formatSecondsToTime(workEndTime);

		if (!startTime.isEmpty() && !endTime.isEmpty()) {
			return startTime + "-" + endTime;
		}
		return "";
	}

	/**
	 * Convert grouped time logs to final format
	 */
	private Map<Integer, String> convertToFinalFormat(Map<Integer, List<String>> timeLogsByTimesheet) {
		return timeLogsByTimesheet.entrySet()
			.stream()
			.collect(Collectors.toMap(Map.Entry::getKey, (entry) -> String.join("; ", entry.getValue())));
	}

	/**
	 * Batch fetch structured time logs for dynamic column expansion. Each date becomes a
	 * separate column with day name + date as header.
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<Integer, Map<String, String>> getStructuredTimeLogsForTimesheets(List<Integer> timesheetIds,
			Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return Collections.emptyMap();
		}

		try {
			org.jooq.Result<? extends org.jooq.Record> structuredTimeLogRecords = this
				.fetchTimeLogRecordsForStructuredExport(timesheetIds, accountId);
			List<Integer> timeLogIds = structuredTimeLogRecords.stream()
				.map((r) -> r.get(TL.ID))
				.filter(Objects::nonNull)
				.distinct()
				.toList();
			Map<Integer, List<TimeLogInterval>> intervalsByTimeLogId = this.fetchIntervalsByTimeLogIds(timeLogIds);
			return this.processStructuredTimeLogRecords(structuredTimeLogRecords, intervalsByTimeLogId);
		}
		catch (Exception ex) {
			throw new ResourceNotFoundException(
					"Failed to fetch structured time logs for " + timesheetIds.size() + " timesheets");
		}
	}

	/**
	 * Process structured time log records and group by timesheet and date. For
	 * ENTER_START_END_TIME work log type, uses intervals from cst_time_log_interval_t
	 * when present to support multiple comma-separated time ranges per day.
	 */
	private Map<Integer, Map<String, String>> processStructuredTimeLogRecords(
			org.jooq.Result<? extends org.jooq.Record> structuredTimeLogRecords,
			Map<Integer, List<TimeLogInterval>> intervalsByTimeLogId) {
		Map<Integer, Map<String, String>> structuredTimeLogs = new HashMap<>();

		for (var timeLogRecord : structuredTimeLogRecords) {
			Integer timesheetId = timeLogRecord.get(TL.TIMESHEET_ID);
			Integer date = timeLogRecord.get(TL.DATE);
			Integer periodStart = timeLogRecord.get(TS.PERIOD_START);
			Integer periodEnd = timeLogRecord.get(TS.PERIOD_END);

			// Filter time logs to only include dates within THIS timesheet's specific
			// period
			if (this.isDateOutsidePeriod(date, periodStart, periodEnd)) {
				continue;
			}

			String columnHeader = this.formatDateAsColumnHeader(date);
			Integer timeLogId = timeLogRecord.get(TL.ID);
			List<TimeLogInterval> intervals = (timeLogId != null) ? intervalsByTimeLogId.get(timeLogId) : null;
			String workTimeValue = this.createStructuredWorkTimeValue(timeLogRecord, intervals);

			structuredTimeLogs.computeIfAbsent(timesheetId, (k) -> new HashMap<>()).put(columnHeader, workTimeValue);
		}

		return structuredTimeLogs;
	}

	/**
	 * Create structured work time value from a record. For ENTER_START_END_TIME type,
	 * uses intervals when present to format multiple ranges as comma-separated (e.g.
	 * "07:00-19:00, 20:00-21:00").
	 */
	private String createStructuredWorkTimeValue(org.jooq.Record timeLogRecord, List<TimeLogInterval> intervals) {
		Integer workTime = timeLogRecord.get(TL.WORK_TIME);
		Integer workStartTime = timeLogRecord.get(TL.WORK_START_TIME);
		Integer workEndTime = timeLogRecord.get(TL.WORK_END_TIME);
		Integer workLogType = timeLogRecord.get(TSS.WORK_LOG_TYPE);

		// For range-based work log type, use intervals when available (multiple per day)
		if (workLogType != null && workLogType == WORK_LOG_TYPE_ENTER_START_END_TIME && intervals != null
				&& !intervals.isEmpty()) {
			return this.formatIntervalsAsCommaSeparated(intervals);
		}

		return this.formatWorkTimeByType(workLogType, workTime, workStartTime, workEndTime);
	}

	/**
	 * Format multiple time log intervals as comma-separated "HH:MM-HH:MM" strings
	 */
	private String formatIntervalsAsCommaSeparated(List<TimeLogInterval> intervals) {
		if (intervals == null || intervals.isEmpty()) {
			return "";
		}
		return intervals.stream()
			.filter((i) -> i != null && !this.isDeletionMarker(i))
			.map((i) -> this.formatStartEndTimeType(i.getWorkStartTime(), i.getWorkEndTime()))
			.filter((s) -> !s.isEmpty())
			.collect(Collectors.joining(", "));
	}

	/**
	 * Helper method to format seconds to HH:mm format
	 */
	private String formatSecondsToTime(Integer seconds) {
		if (seconds == null || seconds < 0) {
			return "";
		}

		int hours = seconds / 3600;
		int minutes = (seconds % 3600) / 60;
		return String.format("%02d:%02d", hours, minutes);
	}

	/**
	 * Helper method to format seconds to decimal hours format
	 */
	private String formatSecondsToHours(Integer seconds) {
		if (seconds == null || seconds < 0) {
			return "";
		}

		double hours = seconds / 3600.0;
		return String.format("%.2f", hours);
	}

	/**
	 * Helper method to format Unix timestamp as column header with day name in UTC
	 * Example: 1720569600 -> "Thursday, 10 Jul 2025"
	 */
	private String formatDateAsColumnHeader(Integer unixTimestamp) {
		if (unixTimestamp == null) {
			return "Unknown Date";
		}

		try {
			return java.time.Instant.ofEpochSecond(unixTimestamp)
				.atZone(java.time.ZoneId.of("UTC"))
				.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", java.util.Locale.ENGLISH));
		}
		catch (Exception ex) {
			return "Invalid Date";
		}
	}

	/**
	 * Batch fetch structured overtime hours data for dynamic column expansion.
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<Integer, Map<String, String>> getStructuredOvertimeHoursForTimesheets(List<Integer> timesheetIds,
			Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return Collections.emptyMap();
		}

		try {
			// Fetch time log records with timesheet period info for proper Java-based
			// filtering
			var records = this.dslContext.select(TL.TIMESHEET_ID, TL.DATE, TL.OVER_TIME, TS.PERIOD_START, TS.PERIOD_END)
				.from(TL)
				.join(TS)
				.on(TL.TIMESHEET_ID.eq(TS.ID))
				.where(TL.TIMESHEET_ID.in(timesheetIds))
				.and(TS.ACCOUNT_ID.eq(accountId))
				.orderBy(TL.TIMESHEET_ID, TL.DATE)
				.fetch();

			// Group by timesheet, then by date column name with proper period filtering
			Map<Integer, Map<String, String>> structuredOvertime = new HashMap<>();

			for (var overtimeRecord : records) {
				Integer timesheetId = overtimeRecord.getValue(TL.TIMESHEET_ID);
				Integer date = overtimeRecord.getValue(TL.DATE);
				Integer overtimeSeconds = overtimeRecord.getValue(TL.OVER_TIME);
				Integer periodStart = overtimeRecord.getValue(TS.PERIOD_START);
				Integer periodEnd = overtimeRecord.getValue(TS.PERIOD_END);

				// Java-based period filtering - ensure time log date falls within
				// timesheet period
				if (date < periodStart || date > periodEnd) {
					continue;
				}

				String dateColumnHeader = this.formatDateAsColumnHeader(date);
				String formattedOvertime = this.formatSecondsToHours(overtimeSeconds);

				structuredOvertime.computeIfAbsent(timesheetId, (k) -> new HashMap<>())
					.put(dateColumnHeader, formattedOvertime);
			}

			return structuredOvertime;
		}
		catch (Exception ex) {
			return Collections.emptyMap();
		}
	}

	/**
	 * Batch fetch structured total time data for dynamic column expansion.
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<Integer, Map<String, String>> getStructuredTotalTimeForTimesheets(List<Integer> timesheetIds,
			Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return Collections.emptyMap();
		}

		try {
			// Fetch time log records with timesheet period info for proper Java-based
			// filtering
			var records = this.dslContext
				.select(TL.TIMESHEET_ID, TL.DATE, TL.TOTAL_TIME, TS.PERIOD_START, TS.PERIOD_END)
				.from(TL)
				.join(TS)
				.on(TL.TIMESHEET_ID.eq(TS.ID))
				.where(TL.TIMESHEET_ID.in(timesheetIds))
				.and(TS.ACCOUNT_ID.eq(accountId))
				.orderBy(TL.TIMESHEET_ID, TL.DATE)
				.fetch();

			// Group by timesheet, then by date column name with proper period filtering
			Map<Integer, Map<String, String>> structuredTotalTime = new HashMap<>();

			for (var totalTimeRecord : records) {
				Integer timesheetId = totalTimeRecord.getValue(TL.TIMESHEET_ID);
				Integer date = totalTimeRecord.getValue(TL.DATE);
				Integer totalTimeSeconds = totalTimeRecord.getValue(TL.TOTAL_TIME);
				Integer periodStart = totalTimeRecord.getValue(TS.PERIOD_START);
				Integer periodEnd = totalTimeRecord.getValue(TS.PERIOD_END);

				// Java-based period filtering - ensure time log date falls within
				// timesheet period
				if (date < periodStart || date > periodEnd) {
					continue;
				}

				String dateColumnHeader = this.formatDateAsColumnHeader(date);
				String formattedTotalTime = this.formatSecondsToTime(totalTimeSeconds);

				structuredTotalTime.computeIfAbsent(timesheetId, (k) -> new HashMap<>())
					.put(dateColumnHeader, formattedTotalTime);
			}

			return structuredTotalTime;
		}
		catch (Exception ex) {
			return Collections.emptyMap();
		}
	}

	/**
	 * Batch fetch structured effective work hours data for dynamic column expansion.
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<Integer, Map<String, String>> getStructuredEffectiveWorkHoursForTimesheets(List<Integer> timesheetIds,
			Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return Collections.emptyMap();
		}

		try {
			// Fetch time log records with timesheet period info for proper Java-based
			// filtering
			var records = this.dslContext
				.select(TL.TIMESHEET_ID, TL.DATE, TL.TOTAL_TIME, TS.PERIOD_START, TS.PERIOD_END)
				.from(TL)
				.join(TS)
				.on(TL.TIMESHEET_ID.eq(TS.ID))
				.where(TL.TIMESHEET_ID.in(timesheetIds))
				.and(TS.ACCOUNT_ID.eq(accountId))
				.orderBy(TL.TIMESHEET_ID, TL.DATE)
				.fetch();

			// Group by timesheet, then by date column name with proper period filtering
			Map<Integer, Map<String, String>> structuredEffectiveWorkHours = new HashMap<>();

			for (var effectiveWorkRecord : records) {
				Integer timesheetId = effectiveWorkRecord.getValue(TL.TIMESHEET_ID);
				Integer date = effectiveWorkRecord.getValue(TL.DATE);
				Integer totalTimeSeconds = effectiveWorkRecord.getValue(TL.TOTAL_TIME);
				Integer periodStart = effectiveWorkRecord.getValue(TS.PERIOD_START);
				Integer periodEnd = effectiveWorkRecord.getValue(TS.PERIOD_END);

				// Java-based period filtering - ensure time log date falls within
				// timesheet period
				if (date < periodStart || date > periodEnd) {
					continue;
				}

				String dateColumnHeader = this.formatDateAsColumnHeader(date);
				String formattedEffectiveWorkHours = this.formatSecondsToHours(totalTimeSeconds);

				structuredEffectiveWorkHours.computeIfAbsent(timesheetId, (k) -> new HashMap<>())
					.put(dateColumnHeader, formattedEffectiveWorkHours);
			}

			return structuredEffectiveWorkHours;
		}
		catch (Exception ex) {
			return Collections.emptyMap();
		}
	}

	/**
	 * Fetches time log records for interval-aware structured export (break intervals,
	 * remarks). Includes TL.ID for interval lookup, TL.BREAK_TIME, TL.REMARK, and
	 * TSS.WORK_LOG_TYPE for value extraction.
	 */
	private org.jooq.Result<? extends org.jooq.Record> fetchTimeLogRecordsForIntervalAwareExport(
			List<Integer> timesheetIds, Integer accountId) {
		return this.dslContext
			.select(TL.ID, TL.TIMESHEET_ID, TL.DATE, TL.BREAK_TIME, TL.REMARK, TSS.WORK_LOG_TYPE, TS.PERIOD_START,
					TS.PERIOD_END)
			.from(TL)
			.join(TS)
			.on(TL.TIMESHEET_ID.eq(TS.ID))
			.join(TSS)
			.on(TS.TIMESHEET_SETTING_ID.eq(TSS.ID))
			.where(TL.TIMESHEET_ID.in(timesheetIds))
			.and(TS.ACCOUNT_ID.eq(accountId))
			.orderBy(TL.TIMESHEET_ID, TL.DATE)
			.fetch();
	}

	/**
	 * Processes interval-aware records into timesheet-by-date structure. Applies period
	 * filtering and uses the given value extractor to produce the cell value per record.
	 */
	private Map<Integer, Map<String, String>> processStructuredIntervalAwareRecords(
			org.jooq.Result<? extends org.jooq.Record> records,
			Map<Integer, List<TimeLogInterval>> intervalsByTimeLogId,
			BiFunction<org.jooq.Record, Map<Integer, List<TimeLogInterval>>, String> valueExtractor) {
		Map<Integer, Map<String, String>> result = new HashMap<>();

		for (var rec : records) {
			Integer timesheetId = rec.getValue(TL.TIMESHEET_ID);
			Integer date = rec.getValue(TL.DATE);
			Integer periodStart = rec.getValue(TS.PERIOD_START);
			Integer periodEnd = rec.getValue(TS.PERIOD_END);

			if (this.isDateOutsidePeriod(date, periodStart, periodEnd)) {
				continue;
			}

			String dateColumnHeader = this.formatDateAsColumnHeader(date);
			String value = valueExtractor.apply(rec, intervalsByTimeLogId);
			result.computeIfAbsent(timesheetId, (k) -> new HashMap<>())
				.put(dateColumnHeader, (value != null) ? value : "");
		}

		return result;
	}

	/**
	 * Batch fetch structured break intervals for dynamic column expansion. Time-interval
	 * logging: comma-separated start-end ranges (e.g. "09:00-09:15, 12:00-13:00").
	 * Hours-based logging: break hours as decimal (e.g. "0.50").
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<Integer, Map<String, String>> getStructuredBreakIntervalsForTimesheets(List<Integer> timesheetIds,
			Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return Collections.emptyMap();
		}

		try {
			var records = this.fetchTimeLogRecordsForIntervalAwareExport(timesheetIds, accountId);
			List<Integer> timeLogIds = records.stream()
				.map((r) -> r.get(TL.ID))
				.filter(Objects::nonNull)
				.distinct()
				.toList();
			Map<Integer, List<TimeLogInterval>> intervalsByTimeLogId = this.fetchIntervalsByTimeLogIds(timeLogIds);
			return this.processStructuredIntervalAwareRecords(records, intervalsByTimeLogId,
					this::extractBreakIntervalValue);
		}
		catch (Exception ex) {
			return Collections.emptyMap();
		}
	}

	/**
	 * Extracts break interval value from a record. Time-interval: comma-separated break
	 * ranges from intervals. Hours-based: break_time as decimal hours.
	 */
	private String extractBreakIntervalValue(org.jooq.Record rec,
			Map<Integer, List<TimeLogInterval>> intervalsByTimeLogId) {
		Integer workLogType = rec.getValue(TSS.WORK_LOG_TYPE);
		Integer timeLogId = rec.getValue(TL.ID);

		if (workLogType != null && workLogType == WORK_LOG_TYPE_ENTER_START_END_TIME) {
			List<TimeLogInterval> intervals = intervalsByTimeLogId.getOrDefault(timeLogId, List.of());
			return this.formatBreakIntervalsAsCommaSeparated(intervals);
		}
		Integer breakTimeSeconds = rec.getValue(TL.BREAK_TIME);
		return this.formatSecondsToHours(breakTimeSeconds);
	}

	/**
	 * Format break intervals from time log intervals as comma-separated "HH:MM-HH:MM"
	 * strings
	 */
	private String formatBreakIntervalsAsCommaSeparated(List<TimeLogInterval> intervals) {
		if (intervals == null || intervals.isEmpty()) {
			return "";
		}
		List<String> parts = new ArrayList<>();
		for (TimeLogInterval interval : intervals) {
			List<BreakInterval> breakIntervals = null;
			if (interval != null && !this.isDeletionMarker(interval)) {
				breakIntervals = interval.getBreakInterval();
			}
			if (breakIntervals == null || breakIntervals.isEmpty()) {
				continue;
			}
			for (BreakInterval bi : breakIntervals) {
				if (bi != null && bi.getBreakStartTime() != null && bi.getBreakEndTime() != null) {
					// Skip deletion markers (-1, -1)
					if (bi.getBreakStartTime() == -1 && bi.getBreakEndTime() == -1) {
						continue;
					}
					String part = this.formatStartEndTimeType(bi.getBreakStartTime(), bi.getBreakEndTime());
					if (!part.isEmpty()) {
						parts.add(part);
					}
				}
			}
		}
		return String.join(", ", parts);
	}

	/**
	 * Batch fetch structured remarks for dynamic column expansion. Time-interval logging:
	 * comma-separated remarks per time range. Hours-based logging: single remark.
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<Integer, Map<String, String>> getStructuredRemarksForTimesheets(List<Integer> timesheetIds,
			Integer accountId) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return Collections.emptyMap();
		}

		try {
			var records = this.fetchTimeLogRecordsForIntervalAwareExport(timesheetIds, accountId);
			List<Integer> timeLogIds = records.stream()
				.map((r) -> r.get(TL.ID))
				.filter(Objects::nonNull)
				.distinct()
				.toList();
			Map<Integer, List<TimeLogInterval>> intervalsByTimeLogId = this.fetchIntervalsByTimeLogIds(timeLogIds);
			return this.processStructuredIntervalAwareRecords(records, intervalsByTimeLogId, this::extractRemarkValue);
		}
		catch (Exception ex) {
			return Collections.emptyMap();
		}
	}

	/**
	 * Extracts remark value from a record. Time-interval: comma-separated remarks per
	 * range. Hours-based: single remark.
	 */
	private String extractRemarkValue(org.jooq.Record rec, Map<Integer, List<TimeLogInterval>> intervalsByTimeLogId) {
		Integer workLogType = rec.getValue(TSS.WORK_LOG_TYPE);
		Integer timeLogId = rec.getValue(TL.ID);

		if (workLogType != null && workLogType == WORK_LOG_TYPE_ENTER_START_END_TIME) {
			List<TimeLogInterval> intervals = intervalsByTimeLogId.getOrDefault(timeLogId, List.of());
			return this.formatRemarksAsCommaSeparated(intervals);
		}
		String remark = rec.getValue(TL.REMARK);
		return (remark != null && !remark.isBlank()) ? remark.trim() : "";
	}

	/**
	 * Format remarks from time log intervals as comma-separated strings. One remark per
	 * time range (interval).
	 */
	private String formatRemarksAsCommaSeparated(List<TimeLogInterval> intervals) {
		if (intervals == null || intervals.isEmpty()) {
			return "";
		}
		List<String> parts = new ArrayList<>();
		for (TimeLogInterval interval : intervals) {
			if (interval == null || this.isDeletionMarker(interval)) {
				continue;
			}
			String remark = interval.getRangeBasedRemark();
			if (remark != null && !remark.isBlank()) {
				parts.add(remark.trim());
			}
		}
		return String.join(", ", parts);
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteByTimesheetIdIn(List<Integer> timesheetIds) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return;
		}

		var table = CstTimeLogT.CST_TIME_LOG_T;
		this.dslContext.deleteFrom(table).where(table.TIMESHEET_ID.in(timesheetIds)).execute();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Integer> findTimeLogIdsByTimesheetIdIn(List<Integer> timesheetIds) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return Collections.emptyList();
		}

		var table = CstTimeLogT.CST_TIME_LOG_T;
		return this.dslContext.select(table.ID)
			.from(table)
			.where(table.TIMESHEET_ID.in(timesheetIds))
			.fetch()
			.stream()
			.map((rec) -> rec.getValue(table.ID))
			.toList();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public int batchUpsert(List<io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogUpsertDto> values) {
		if (values == null || values.isEmpty()) {
			return 0;
		}

		int totalRowsAffected = 0;

		// Process in batches to avoid max_allowed_packet limits
		for (int i = 0; i < values.size(); i += TIME_LOG_BATCH_SIZE) {
			int endIndex = Math.min(i + TIME_LOG_BATCH_SIZE, values.size());
			List<io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogUpsertDto> batch = values.subList(i,
					endIndex);

			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO `cst_time_log_t` ");
			sql.append(
					"(`id`, `date`, `day_type_id`, `timesheet_id`, `remark`, `break_time`, `over_time`, `total_time`, `work_time`) ");
			sql.append("VALUES ");

			// Build VALUES clause
			for (int j = 0; j < batch.size(); j++) {
				if (j > 0) {
					sql.append(", ");
				}
				sql.append("(?, ?, ?, ?, ?, ?, ?, ?, ?)");
			}

			// Add ON DUPLICATE KEY UPDATE clause
			sql.append(" ON DUPLICATE KEY UPDATE ");
			sql.append("`remark` = VALUES(`remark`), ");
			sql.append("`break_time` = VALUES(`break_time`), ");
			sql.append("`over_time` = VALUES(`over_time`), ");
			sql.append("`total_time` = VALUES(`total_time`), ");
			sql.append("`work_time` = VALUES(`work_time`)");

			Query query = this.entityManager.createNativeQuery(sql.toString());

			// Set parameters
			int paramIndex = 1;
			for (io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogUpsertDto dto : batch) {
				query.setParameter(paramIndex++, dto.getId());
				query.setParameter(paramIndex++, dto.getDate());
				query.setParameter(paramIndex++, dto.getDayTypeId());
				query.setParameter(paramIndex++, dto.getTimesheetId());
				query.setParameter(paramIndex++, dto.getRemark());
				query.setParameter(paramIndex++, dto.getBreakTime());
				query.setParameter(paramIndex++, dto.getOverTime());
				query.setParameter(paramIndex++, dto.getTotalTime());
				query.setParameter(paramIndex++, dto.getWorkTime());
			}

			int rowsAffected = query.executeUpdate();
			totalRowsAffected += rowsAffected;

		}

		return totalRowsAffected;
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimeLogMigrationDto> findTimeLogsForMigration(List<Integer> timesheetIds) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return Collections.emptyList();
		}

		var table = CstTimeLogT.CST_TIME_LOG_T;
		return this.dslContext
			.select(table.TIMESHEET_ID, table.ID, table.TOTAL_TIME, table.OVER_TIME, table.WORK_TIME,
					table.WORK_START_TIME, table.WORK_END_TIME)
			.from(table)
			.where(table.TIMESHEET_ID.in(timesheetIds))
			.fetch()
			.map((rec) -> new TimeLogMigrationDto(rec.get(table.TIMESHEET_ID), rec.get(table.ID),
					(rec.get(table.TOTAL_TIME) != null) ? rec.get(table.TOTAL_TIME) : 0,
					(rec.get(table.OVER_TIME) != null) ? rec.get(table.OVER_TIME) : 0,
					(rec.get(table.WORK_TIME) != null) ? rec.get(table.WORK_TIME) : 0, rec.get(table.WORK_START_TIME),
					rec.get(table.WORK_END_TIME)));
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimeLogMigrationQueryResultDto> findTimeLogsForMigration(int batchSize, int offset) {
		String sql = "SELECT tl.id, tl.work_start_time, tl.work_end_time, tl.remark, tss.work_log_type, "
				+ "tl.work_time, ta.timesheet_approval_status_type_id " + "FROM cst_time_log_t tl "
				+ "LEFT JOIN cst_timesheet_t ts ON ts.id = tl.timesheet_id "
				+ "LEFT JOIN cst_timesheet_setting_t tss ON tss.id = ts.timesheet_setting_id "
				+ "LEFT JOIN cst_timesheet_approval_t ta ON ta.timesheet_id = ts.id "
				+ "AND ta.id = (SELECT MAX(ta2.id) FROM cst_timesheet_approval_t ta2 WHERE ta2.timesheet_id = ts.id) "
				+ "ORDER BY tl.id";

		Query query = this.entityManager.createNativeQuery(sql);
		query.setFirstResult(offset);
		query.setMaxResults(batchSize);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = query.getResultList();

		List<TimeLogMigrationQueryResultDto> result = new ArrayList<>();
		for (Object[] row : rows) {
			TimeLogMigrationQueryResultDto dto = new TimeLogMigrationQueryResultDto();
			dto.setId((row[0] != null) ? ((Number) row[0]).intValue() : null);
			dto.setWorkStartTime((row[1] != null) ? ((Number) row[1]).intValue() : null);
			dto.setWorkEndTime((row[2] != null) ? ((Number) row[2]).intValue() : null);
			dto.setRemark((row[3] != null) ? row[3].toString() : null);
			dto.setWorkLogType((row[4] != null) ? ((Number) row[4]).intValue() : null);
			dto.setWorkTime((row[5] != null) ? ((Number) row[5]).intValue() : null);
			dto.setTimesheetApprovalStatusTypeId((row[6] != null) ? ((Number) row[6]).intValue() : null);
			result.add(dto);
		}
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public long countUnmigratedTimeLogs() {
		String sql = "SELECT COUNT(tl.id) " + "FROM cst_time_log_t tl "
				+ "LEFT JOIN cst_time_log_interval_t tli ON tli.time_log_id = tl.id " + "WHERE tli.id IS NULL";
		Query query = this.entityManager.createNativeQuery(sql);
		Object result = query.getSingleResult();
		return (result != null) ? ((Number) result).longValue() : 0L;
	}

}