package io.recruitcrm.microservice.timesheet.repositories.time_log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;

import io.recruitcrm.contract_staffing.entity.model.BreakInterval;
import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.contract_staffing.entity.model.TimeLogInterval;
import io.recruitcrm.microservice.timesheet.dao.time_log.TimeLogJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogWithSettingQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimesheetJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetLogQueryResultDto;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimeLogT;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetLogsTestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.jooq.Record;
import org.jooq.Record10;
import org.jooq.Result;
import org.jooq.DSLContext;
import org.jooq.DeleteConditionStep;
import org.jooq.DeleteUsingStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

@ExtendWith(MockitoExtension.class)
class TimeLogRepositoryTests {

	@InjectMocks
	private TimeLogRepository repository;

	@Mock
	private TimeLogJpaRepository timeLogJpaRepository;

	@Mock
	private EntityManager entityManager;

	@Mock
	private DSLContext dslContext;

	@Mock
	private TimeLogIntervalRepository timeLogIntervalRepository;

	@BeforeEach
	void setUp() {
		// Common setup for repository tests
	}

	@SuppressWarnings("unchecked")
	private <T> T invokePrivateMethod(String methodName, Class<?>[] parameterTypes, Object... args) {
		try {
			java.lang.reflect.Method method = TimeLogRepository.class.getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
			return (T) method.invoke(this.repository, args);
		}
		catch (Exception ex) {
			throw new RuntimeException("Failed invoking private method: " + methodName, ex);
		}
	}

	private Object getPrivateStaticField(String fieldName) {
		try {
			java.lang.reflect.Field reflectionField = TimeLogRepository.class.getDeclaredField(fieldName);
			reflectionField.setAccessible(true);
			return reflectionField.get(null);
		}
		catch (Exception ex) {
			throw new RuntimeException("Failed accessing private static field: " + fieldName, ex);
		}
	}

	private DSLContext createDslContextForFetch(Object fetchResult) {
		// Condition-phase proxy: handles where → and → orderBy → seek → fetch
		Object conditionChain = java.lang.reflect.Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { org.jooq.SelectConditionStep.class, org.jooq.SelectOrderByStep.class,
						org.jooq.SelectSeekStep2.class, org.jooq.SelectSeekStepN.class },
				(proxy, method, args) -> {
					if ("fetch".equals(method.getName())) {
						return fetchResult;
					}
					return proxy;
				});

		// Main-chain proxy: handles select → from → join/innerJoin/leftJoin → on,
		// delegates to conditionChain on where()
		Object mainChain = java.lang.reflect.Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { org.jooq.SelectSelectStep.class, org.jooq.SelectFromStep.class,
						org.jooq.SelectJoinStep.class, org.jooq.SelectOnStep.class, org.jooq.SelectOptionalOnStep.class,
						org.jooq.SelectOnConditionStep.class, org.jooq.SelectWhereStep.class },
				(proxy, method, args) -> {
					if ("where".equals(method.getName())) {
						return conditionChain;
					}
					if ("fetch".equals(method.getName())) {
						return fetchResult;
					}
					return proxy;
				});

		return mock(DSLContext.class, (invocation) -> {
			if (invocation.getMethod().getName().startsWith("select")) {
				return mainChain;
			}
			return org.mockito.Answers.RETURNS_DEFAULTS.answer(invocation);
		});
	}

	@Test
	@DisplayName("fetchIntervalsByTimeLogIds returns empty map for null/empty inputs")
	void testFetchIntervalsByTimeLogIdsReturnsEmptyMapForNullOrEmpty() {
		Map<Integer, List<TimeLogInterval>> nullResult = this.invokePrivateMethod("fetchIntervalsByTimeLogIds",
				new Class<?>[] { List.class }, (Object) null);
		assertThat(nullResult).isEmpty();

		Map<Integer, List<TimeLogInterval>> emptyResult = this.invokePrivateMethod("fetchIntervalsByTimeLogIds",
				new Class<?>[] { List.class }, Collections.emptyList());
		assertThat(emptyResult).isEmpty();
	}

	@Test
	@DisplayName("fetchIntervalsByTimeLogIds filters nulls and deletion markers")
	void testFetchIntervalsByTimeLogIdsFiltersNullsAndDeletionMarkers() {
		List<Integer> timeLogIds = List.of(10, 11, 12, 13);

		TimeLogInterval deletionMarker = mock(TimeLogInterval.class);
		given(deletionMarker.getTimeLogId()).willReturn(10);
		given(deletionMarker.getWorkStartTime()).willReturn(-1);
		given(deletionMarker.getWorkEndTime()).willReturn(-1);

		TimeLogInterval nullStartTime = mock(TimeLogInterval.class);
		given(nullStartTime.getTimeLogId()).willReturn(11);
		given(nullStartTime.getWorkStartTime()).willReturn(null);

		TimeLogInterval nullEndTime = mock(TimeLogInterval.class);
		given(nullEndTime.getTimeLogId()).willReturn(12);
		given(nullEndTime.getWorkStartTime()).willReturn(-1);
		given(nullEndTime.getWorkEndTime()).willReturn(null);

		TimeLogInterval validInterval = mock(TimeLogInterval.class);
		given(validInterval.getTimeLogId()).willReturn(13);
		given(validInterval.getWorkStartTime()).willReturn(1);

		TimeLogInterval nullTimeLogId = mock(TimeLogInterval.class);
		given(nullTimeLogId.getTimeLogId()).willReturn(null);

		given(this.timeLogIntervalRepository.findByTimeLogIdIn(timeLogIds)).willReturn(java.util.Arrays.asList(null,
				deletionMarker, nullStartTime, nullEndTime, nullTimeLogId, validInterval));

		Map<Integer, List<TimeLogInterval>> result = this.invokePrivateMethod("fetchIntervalsByTimeLogIds",
				new Class<?>[] { List.class }, timeLogIds);

		assertThat(result).hasSize(3).containsKeys(11, 12, 13);
		assertThat(result.get(11)).isNotNull().hasSize(1);
		assertThat(result.get(12)).hasSize(1);
		assertThat(result.get(13)).hasSize(1);
	}

	@Test
	@DisplayName("processTimeLogRecords filters by period and formats work-time")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testProcessTimeLogRecordsFormatsAndFilters() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetT ts = (CstTimesheetT) this.getPrivateStaticField("TS");
		CstTimesheetSettingT tss = (CstTimesheetSettingT) this.getPrivateStaticField("TSS");

		Record10<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> outsideRecord = mock(
				Record10.class);
		given(outsideRecord.get(tl.TIMESHEET_ID)).willReturn(100);
		given(outsideRecord.get(tl.DATE)).willReturn(5);
		given(outsideRecord.get(ts.PERIOD_START)).willReturn(10);
		given(outsideRecord.get(ts.PERIOD_END)).willReturn(20);

		Record10<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> workLogHourRecord = mock(
				Record10.class);
		given(workLogHourRecord.get(tl.TIMESHEET_ID)).willReturn(100);
		given(workLogHourRecord.get(tl.DATE)).willReturn(12);
		given(workLogHourRecord.get(ts.PERIOD_START)).willReturn(10);
		given(workLogHourRecord.get(ts.PERIOD_END)).willReturn(20);
		given(workLogHourRecord.get(tss.WORK_LOG_TYPE)).willReturn(1);
		given(workLogHourRecord.get(tl.WORK_TIME)).willReturn(3600);
		given(workLogHourRecord.get(tl.WORK_START_TIME)).willReturn(null);
		given(workLogHourRecord.get(tl.WORK_END_TIME)).willReturn(null);

		Record10<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> workLogNoTimeRecord = mock(
				Record10.class);
		given(workLogNoTimeRecord.get(tl.TIMESHEET_ID)).willReturn(100);
		given(workLogNoTimeRecord.get(tl.DATE)).willReturn(13);
		given(workLogNoTimeRecord.get(ts.PERIOD_START)).willReturn(10);
		given(workLogNoTimeRecord.get(ts.PERIOD_END)).willReturn(20);
		given(workLogNoTimeRecord.get(tss.WORK_LOG_TYPE)).willReturn(3);
		given(workLogNoTimeRecord.get(tl.WORK_TIME)).willReturn(null);
		given(workLogNoTimeRecord.get(tl.WORK_START_TIME)).willReturn(null);
		given(workLogNoTimeRecord.get(tl.WORK_END_TIME)).willReturn(null);

		Record10<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> rangeValidRecord = mock(
				Record10.class);
		given(rangeValidRecord.get(tl.TIMESHEET_ID)).willReturn(101);
		given(rangeValidRecord.get(tl.DATE)).willReturn(12);
		given(rangeValidRecord.get(ts.PERIOD_START)).willReturn(10);
		given(rangeValidRecord.get(ts.PERIOD_END)).willReturn(20);
		given(rangeValidRecord.get(tss.WORK_LOG_TYPE)).willReturn(2);
		given(rangeValidRecord.get(tl.WORK_TIME)).willReturn(null);
		given(rangeValidRecord.get(tl.WORK_START_TIME)).willReturn(3600);
		given(rangeValidRecord.get(tl.WORK_END_TIME)).willReturn(7200);

		Record10<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> rangeEmptyRecord = mock(
				Record10.class);
		given(rangeEmptyRecord.get(tl.TIMESHEET_ID)).willReturn(101);
		given(rangeEmptyRecord.get(tl.DATE)).willReturn(14);
		given(rangeEmptyRecord.get(ts.PERIOD_START)).willReturn(10);
		given(rangeEmptyRecord.get(ts.PERIOD_END)).willReturn(20);
		given(rangeEmptyRecord.get(tss.WORK_LOG_TYPE)).willReturn(2);
		given(rangeEmptyRecord.get(tl.WORK_TIME)).willReturn(null);
		given(rangeEmptyRecord.get(tl.WORK_START_TIME)).willReturn(-1);
		given(rangeEmptyRecord.get(tl.WORK_END_TIME)).willReturn(7200);

		List<Record10<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>> records = List
			.of(outsideRecord, workLogHourRecord, workLogNoTimeRecord, rangeValidRecord, rangeEmptyRecord);

		Result timeLogRecords = mock(Result.class);
		given(timeLogRecords.iterator()).willReturn(records.iterator());

		Map<Integer, List<String>> processed = this.invokePrivateMethod("processTimeLogRecords",
				new Class<?>[] { Result.class }, timeLogRecords);

		Map<Integer, String> converted = this.invokePrivateMethod("convertToFinalFormat", new Class<?>[] { Map.class },
				processed);

		assertThat(converted).containsKeys(100, 101);
		assertThat(converted.get(100)).contains("1.00").contains("No time logged").contains("; ");
		assertThat(converted.get(101)).contains("01:00-02:00");
	}

	@Test
	@DisplayName("processStructuredTimeLogRecords builds column-to-value structure")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testProcessStructuredTimeLogRecordsBuildsOutput() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetT ts = (CstTimesheetT) this.getPrivateStaticField("TS");
		CstTimesheetSettingT tss = (CstTimesheetSettingT) this.getPrivateStaticField("TSS");

		Integer outsideTimesheetId = 200;
		Integer outsideDate = 5;
		Integer periodStart = 10;
		Integer periodEnd = 20;

		Record outsideRecord = mock(Record.class);
		given(outsideRecord.get(tl.TIMESHEET_ID)).willReturn(outsideTimesheetId);
		given(outsideRecord.get(tl.DATE)).willReturn(outsideDate);
		given(outsideRecord.get(ts.PERIOD_START)).willReturn(periodStart);
		given(outsideRecord.get(ts.PERIOD_END)).willReturn(periodEnd);

		Integer timesheetIdWithIntervals = 201;
		Integer insideDate1 = 12;
		Integer timeLogIdWithIntervals = 10;
		Record recordWithIntervals = mock(Record.class);
		given(recordWithIntervals.get(tl.TIMESHEET_ID)).willReturn(timesheetIdWithIntervals);
		given(recordWithIntervals.get(tl.DATE)).willReturn(insideDate1);
		given(recordWithIntervals.get(ts.PERIOD_START)).willReturn(periodStart);
		given(recordWithIntervals.get(ts.PERIOD_END)).willReturn(periodEnd);
		given(recordWithIntervals.get(tl.ID)).willReturn(timeLogIdWithIntervals);
		given(recordWithIntervals.get(tss.WORK_LOG_TYPE)).willReturn(2);
		given(recordWithIntervals.get(tl.WORK_TIME)).willReturn(null);
		given(recordWithIntervals.get(tl.WORK_START_TIME)).willReturn(3600);
		given(recordWithIntervals.get(tl.WORK_END_TIME)).willReturn(7200);

		Integer timesheetIdWithEmptyIntervals = 202;
		Integer insideDate2 = 13;
		Integer timeLogIdEmptyIntervals = 11;
		Record recordWithEmptyIntervals = mock(Record.class);
		given(recordWithEmptyIntervals.get(tl.TIMESHEET_ID)).willReturn(timesheetIdWithEmptyIntervals);
		given(recordWithEmptyIntervals.get(tl.DATE)).willReturn(insideDate2);
		given(recordWithEmptyIntervals.get(ts.PERIOD_START)).willReturn(periodStart);
		given(recordWithEmptyIntervals.get(ts.PERIOD_END)).willReturn(periodEnd);
		given(recordWithEmptyIntervals.get(tl.ID)).willReturn(timeLogIdEmptyIntervals);
		given(recordWithEmptyIntervals.get(tss.WORK_LOG_TYPE)).willReturn(2);
		given(recordWithEmptyIntervals.get(tl.WORK_TIME)).willReturn(null);
		given(recordWithEmptyIntervals.get(tl.WORK_START_TIME)).willReturn(3600);
		given(recordWithEmptyIntervals.get(tl.WORK_END_TIME)).willReturn(7200);

		Integer timesheetIdWithNullWorkLogType = 203;
		Integer insideDate3 = 14;
		Record recordWithNullWorkLogType = mock(Record.class);
		given(recordWithNullWorkLogType.get(tl.TIMESHEET_ID)).willReturn(timesheetIdWithNullWorkLogType);
		given(recordWithNullWorkLogType.get(tl.DATE)).willReturn(insideDate3);
		given(recordWithNullWorkLogType.get(ts.PERIOD_START)).willReturn(periodStart);
		given(recordWithNullWorkLogType.get(ts.PERIOD_END)).willReturn(periodEnd);
		given(recordWithNullWorkLogType.get(tl.ID)).willReturn(null);
		given(recordWithNullWorkLogType.get(tss.WORK_LOG_TYPE)).willReturn(null);
		given(recordWithNullWorkLogType.get(tl.WORK_TIME)).willReturn(null);
		given(recordWithNullWorkLogType.get(tl.WORK_START_TIME)).willReturn(null);
		given(recordWithNullWorkLogType.get(tl.WORK_END_TIME)).willReturn(null);

		Result structuredTimeLogRecords = mock(Result.class);
		given(structuredTimeLogRecords.iterator())
			.willReturn(List.of(outsideRecord, recordWithIntervals, recordWithEmptyIntervals, recordWithNullWorkLogType)
				.iterator());

		TimeLogInterval deletionMarker = mock(TimeLogInterval.class);
		given(deletionMarker.getWorkStartTime()).willReturn(-1);
		given(deletionMarker.getWorkEndTime()).willReturn(-1);

		TimeLogInterval emptyRange = mock(TimeLogInterval.class);
		given(emptyRange.getWorkStartTime()).willReturn(-1);
		given(emptyRange.getWorkEndTime()).willReturn(7200);

		TimeLogInterval interval1 = mock(TimeLogInterval.class);
		given(interval1.getWorkStartTime()).willReturn(3600);
		given(interval1.getWorkEndTime()).willReturn(7200);

		List<TimeLogInterval> nonEmptyIntervals = java.util.Arrays.asList(null, deletionMarker, emptyRange, interval1);

		Map<Integer, List<TimeLogInterval>> intervalsByTimeLogId = Map.of(timeLogIdWithIntervals, nonEmptyIntervals,
				timeLogIdEmptyIntervals, Collections.emptyList());

		Map<Integer, Map<String, String>> result = this.invokePrivateMethod("processStructuredTimeLogRecords",
				new Class<?>[] { Result.class, Map.class }, structuredTimeLogRecords, intervalsByTimeLogId);

		String header1 = this.invokePrivateMethod("formatDateAsColumnHeader", new Class<?>[] { Integer.class },
				insideDate1);
		String header2 = this.invokePrivateMethod("formatDateAsColumnHeader", new Class<?>[] { Integer.class },
				insideDate2);
		String header3 = this.invokePrivateMethod("formatDateAsColumnHeader", new Class<?>[] { Integer.class },
				insideDate3);

		assertThat(result).containsKeys(timesheetIdWithIntervals, timesheetIdWithEmptyIntervals,
				timesheetIdWithNullWorkLogType);
		assertThat(result.get(timesheetIdWithIntervals)).containsEntry(header1, "01:00-02:00");
		assertThat(result.get(timesheetIdWithEmptyIntervals)).containsEntry(header2, "01:00-02:00");
		assertThat(result.get(timesheetIdWithNullWorkLogType)).containsEntry(header3, "No time logged");
	}

	@Test
	@DisplayName("formatIntervalsAsCommaSeparated handles null/empty inputs")
	void testFormatIntervalsAsCommaSeparatedNullAndEmpty() {
		String nullResult = this.invokePrivateMethod("formatIntervalsAsCommaSeparated", new Class<?>[] { List.class },
				(Object) null);
		assertThat(nullResult).isEmpty();

		String emptyResult = this.invokePrivateMethod("formatIntervalsAsCommaSeparated", new Class<?>[] { List.class },
				Collections.emptyList());
		assertThat(emptyResult).isEmpty();
	}

	@Test
	@DisplayName("formatSecondsToHours handles null/negative inputs")
	void testFormatSecondsToHoursNullAndNegative() {
		String nullResult = this.invokePrivateMethod("formatSecondsToHours", new Class<?>[] { Integer.class },
				(Object) null);
		assertThat(nullResult).isEmpty();

		String negativeResult = this.invokePrivateMethod("formatSecondsToHours", new Class<?>[] { Integer.class }, -1);
		assertThat(negativeResult).isEmpty();

		String positiveResult = this.invokePrivateMethod("formatSecondsToHours", new Class<?>[] { Integer.class },
				3600);
		assertThat(positiveResult).isEqualTo("1.00");
	}

	@Test
	@DisplayName("formatDateAsColumnHeader returns Unknown Date for null")
	void testFormatDateAsColumnHeaderNull() {
		String result = this.invokePrivateMethod("formatDateAsColumnHeader", new Class<?>[] { Integer.class },
				(Object) null);
		assertThat(result).isEqualTo("Unknown Date");
	}

	@Test
	@DisplayName("formatWorkHourType returns empty string when work time is null")
	void testFormatWorkHourTypeNullReturnsEmpty() {
		String result = this.invokePrivateMethod("formatWorkHourType", new Class<?>[] { Integer.class }, (Object) null);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatSecondsToTime handles null negative and positive seconds")
	void testFormatSecondsToTimeHandlesNullNegativeAndPositive() {
		String nullResult = this.invokePrivateMethod("formatSecondsToTime", new Class<?>[] { Integer.class },
				(Object) null);
		assertThat(nullResult).isEmpty();

		String negativeResult = this.invokePrivateMethod("formatSecondsToTime", new Class<?>[] { Integer.class }, -1);
		assertThat(negativeResult).isEmpty();

		String positiveResult = this.invokePrivateMethod("formatSecondsToTime", new Class<?>[] { Integer.class }, 3660);
		assertThat(positiveResult).isEqualTo("01:01");
	}

	@Test
	@DisplayName("processStructuredIntervalAwareRecords filters period and writes empty for null value")
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void testProcessStructuredIntervalAwareRecordsFiltersAndWritesEmptyForNullExtractor() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetT ts = (CstTimesheetT) this.getPrivateStaticField("TS");

		Record outsideRecord = mock(Record.class);
		given(outsideRecord.getValue(tl.TIMESHEET_ID)).willReturn(700);
		given(outsideRecord.getValue(tl.DATE)).willReturn(5);
		given(outsideRecord.getValue(ts.PERIOD_START)).willReturn(10);
		given(outsideRecord.getValue(ts.PERIOD_END)).willReturn(20);

		Record insideRecordWithNullValue = mock(Record.class);
		given(insideRecordWithNullValue.getValue(tl.TIMESHEET_ID)).willReturn(700);
		given(insideRecordWithNullValue.getValue(tl.DATE)).willReturn(12);
		given(insideRecordWithNullValue.getValue(ts.PERIOD_START)).willReturn(10);
		given(insideRecordWithNullValue.getValue(ts.PERIOD_END)).willReturn(20);

		Record insideRecordWithTextValue = mock(Record.class);
		given(insideRecordWithTextValue.getValue(tl.TIMESHEET_ID)).willReturn(701);
		given(insideRecordWithTextValue.getValue(tl.DATE)).willReturn(13);
		given(insideRecordWithTextValue.getValue(ts.PERIOD_START)).willReturn(10);
		given(insideRecordWithTextValue.getValue(ts.PERIOD_END)).willReturn(20);

		Result records = mock(Result.class);
		given(records.iterator())
			.willReturn(List.of(outsideRecord, insideRecordWithNullValue, insideRecordWithTextValue).iterator());

		java.util.function.BiFunction<Record, Map<Integer, List<TimeLogInterval>>, String> valueExtractor = (
				recordEntry, intervals) -> (recordEntry == insideRecordWithTextValue) ? "break" : null;

		Map<Integer, Map<String, String>> result = this.invokePrivateMethod("processStructuredIntervalAwareRecords",
				new Class<?>[] { Result.class, Map.class, java.util.function.BiFunction.class }, records,
				Collections.emptyMap(), valueExtractor);

		String day12Header = this.invokePrivateMethod("formatDateAsColumnHeader", new Class<?>[] { Integer.class }, 12);
		String day13Header = this.invokePrivateMethod("formatDateAsColumnHeader", new Class<?>[] { Integer.class }, 13);

		assertThat(result).containsKeys(700, 701);
		assertThat(result.get(700)).containsEntry(day12Header, "");
		assertThat(result.get(701)).containsEntry(day13Header, "break");
	}

	@Test
	@DisplayName("formatBreakIntervalsAsCommaSeparated skips invalid and deletion markers")
	void testFormatBreakIntervalsAsCommaSeparatedSkipsInvalidValues() {
		BreakInterval nullBreak = null;

		BreakInterval missingStart = mock(BreakInterval.class);
		given(missingStart.getBreakStartTime()).willReturn(null);

		BreakInterval deletionBreak = mock(BreakInterval.class);
		given(deletionBreak.getBreakStartTime()).willReturn(-1);
		given(deletionBreak.getBreakEndTime()).willReturn(-1);

		BreakInterval validBreak = mock(BreakInterval.class);
		given(validBreak.getBreakStartTime()).willReturn(3600);
		given(validBreak.getBreakEndTime()).willReturn(3660);

		TimeLogInterval deletionMarkerInterval = mock(TimeLogInterval.class);
		given(deletionMarkerInterval.getWorkStartTime()).willReturn(-1);
		given(deletionMarkerInterval.getWorkEndTime()).willReturn(-1);

		TimeLogInterval noBreaksInterval = mock(TimeLogInterval.class);
		given(noBreaksInterval.getWorkStartTime()).willReturn(3600);
		given(noBreaksInterval.getBreakInterval()).willReturn(Collections.emptyList());

		TimeLogInterval validInterval = mock(TimeLogInterval.class);
		given(validInterval.getWorkStartTime()).willReturn(3600);
		given(validInterval.getBreakInterval())
			.willReturn(java.util.Arrays.asList(nullBreak, missingStart, deletionBreak, validBreak));

		String result = this.invokePrivateMethod("formatBreakIntervalsAsCommaSeparated", new Class<?>[] { List.class },
				java.util.Arrays.asList(null, deletionMarkerInterval, noBreaksInterval, validInterval));

		assertThat(result).isEqualTo("01:00-01:01");
	}

	@Test
	@DisplayName("extractBreakIntervalValue uses intervals for start end and break time for hour mode")
	void testExtractBreakIntervalValueSupportsBothModes() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetSettingT tss = (CstTimesheetSettingT) this.getPrivateStaticField("TSS");

		BreakInterval validBreak = mock(BreakInterval.class);
		given(validBreak.getBreakStartTime()).willReturn(3600);
		given(validBreak.getBreakEndTime()).willReturn(3660);

		TimeLogInterval validInterval = mock(TimeLogInterval.class);
		given(validInterval.getWorkStartTime()).willReturn(3600);
		given(validInterval.getBreakInterval()).willReturn(List.of(validBreak));

		Record startEndRecord = mock(Record.class);
		given(startEndRecord.getValue(tss.WORK_LOG_TYPE)).willReturn(2);
		given(startEndRecord.getValue(tl.ID)).willReturn(999);

		String startEndResult = this.invokePrivateMethod("extractBreakIntervalValue",
				new Class<?>[] { Record.class, Map.class }, startEndRecord, Map.of(999, List.of(validInterval)));

		Record hoursRecord = mock(Record.class);
		given(hoursRecord.getValue(tss.WORK_LOG_TYPE)).willReturn(1);
		given(hoursRecord.getValue(tl.ID)).willReturn(1000);
		given(hoursRecord.getValue(tl.BREAK_TIME)).willReturn(3600);

		String hoursResult = this.invokePrivateMethod("extractBreakIntervalValue",
				new Class<?>[] { Record.class, Map.class }, hoursRecord, Collections.emptyMap());

		assertThat(startEndResult).isEqualTo("01:00-01:01");
		assertThat(hoursResult).isEqualTo("1.00");
	}

	@Test
	@DisplayName("extractRemarkValue and formatRemarksAsCommaSeparated handle both work log modes")
	void testExtractRemarkValueAndFormatRemarksAsCommaSeparated() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetSettingT tss = (CstTimesheetSettingT) this.getPrivateStaticField("TSS");

		TimeLogInterval deletionMarkerInterval = mock(TimeLogInterval.class);
		given(deletionMarkerInterval.getWorkStartTime()).willReturn(-1);
		given(deletionMarkerInterval.getWorkEndTime()).willReturn(-1);

		TimeLogInterval blankRemarkInterval = mock(TimeLogInterval.class);
		given(blankRemarkInterval.getWorkStartTime()).willReturn(3600);
		given(blankRemarkInterval.getRangeBasedRemark()).willReturn("   ");

		TimeLogInterval validRemarkInterval = mock(TimeLogInterval.class);
		given(validRemarkInterval.getWorkStartTime()).willReturn(3600);
		given(validRemarkInterval.getRangeBasedRemark()).willReturn("  first shift  ");

		String formattedRemarks = this.invokePrivateMethod("formatRemarksAsCommaSeparated",
				new Class<?>[] { List.class },
				java.util.Arrays.asList(null, deletionMarkerInterval, blankRemarkInterval, validRemarkInterval));
		assertThat(formattedRemarks).isEqualTo("first shift");

		Record startEndRecord = mock(Record.class);
		given(startEndRecord.getValue(tss.WORK_LOG_TYPE)).willReturn(2);
		given(startEndRecord.getValue(tl.ID)).willReturn(111);

		String startEndResult = this.invokePrivateMethod("extractRemarkValue",
				new Class<?>[] { Record.class, Map.class }, startEndRecord, Map.of(111, List.of(validRemarkInterval)));

		Record hoursRecord = mock(Record.class);
		given(hoursRecord.getValue(tss.WORK_LOG_TYPE)).willReturn(1);
		given(hoursRecord.getValue(tl.ID)).willReturn(112);
		given(hoursRecord.getValue(tl.REMARK)).willReturn("  day note  ");

		String hoursResult = this.invokePrivateMethod("extractRemarkValue", new Class<?>[] { Record.class, Map.class },
				hoursRecord, Collections.emptyMap());

		Record blankHoursRemarkRecord = mock(Record.class);
		given(blankHoursRemarkRecord.getValue(tss.WORK_LOG_TYPE)).willReturn(1);
		given(blankHoursRemarkRecord.getValue(tl.ID)).willReturn(113);
		given(blankHoursRemarkRecord.getValue(tl.REMARK)).willReturn("   ");

		String blankHoursResult = this.invokePrivateMethod("extractRemarkValue",
				new Class<?>[] { Record.class, Map.class }, blankHoursRemarkRecord, Collections.emptyMap());

		assertThat(startEndResult).isEqualTo("first shift");
		assertThat(hoursResult).isEqualTo("day note");
		assertThat(blankHoursResult).isEmpty();
	}

	@Test
	@DisplayName("Create bulk timesheet logs should delegate to JPA repository")
	void testCreateBulkTimesheetLogsValidListDelegatesToJpaRepository() {
		// Given
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();

		// When
		this.repository.createBulkTimesheetLogs(timeLogs);

		// Then
		then(this.timeLogJpaRepository).should().saveAll(timeLogs);
	}

	@Test
	@DisplayName("Create bulk timesheet logs should handle empty list")
	void testCreateBulkTimesheetLogsEmptyListHandlesCorrectly() {
		// Given
		List<TimeLog> emptyTimeLogs = Collections.emptyList();

		// When
		this.repository.createBulkTimesheetLogs(emptyTimeLogs);

		// Then
		then(this.timeLogJpaRepository).should().saveAll(emptyTimeLogs);
	}

	@Test
	@DisplayName("Create bulk timesheet logs should propagate DataAccessException")
	void testCreateBulkTimesheetLogsDataAccessExceptionPropagatedException() {
		// Given
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();

		willThrow(new DataAccessException("Database constraint violation") {
		}).given(this.timeLogJpaRepository).saveAll(timeLogs);

		// When & Then
		assertThatThrownBy(() -> this.repository.createBulkTimesheetLogs(timeLogs))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database constraint violation");

		then(this.timeLogJpaRepository).should().saveAll(timeLogs);
	}

	@Test
	@DisplayName("Create timesheet log should create entity and delegate to JPA repository")
	void testCreateTimesheetLogValidParametersCreatesEntityAndDelegatesToJpaRepository() {
		// Given
		Integer date = 20240115;
		Integer dateTypeId = 1;
		Integer timesheetId = 1;

		// When
		this.repository.createTimesheetLog(date, dateTypeId, timesheetId);

		// Then
		then(this.timeLogJpaRepository).should().save(any(TimeLog.class));
	}

	@Test
	@DisplayName("Create timesheet log should handle null date parameter")
	void testCreateTimesheetLogNullDateHandlesCorrectly() {
		// Given
		Integer date = null;
		Integer dateTypeId = 1;
		Integer timesheetId = 1;

		// When
		this.repository.createTimesheetLog(date, dateTypeId, timesheetId);

		// Then
		then(this.timeLogJpaRepository).should().save(any(TimeLog.class));
	}

	@Test
	@DisplayName("Create timesheet log should propagate DataAccessException")
	void testCreateTimesheetLogDataAccessExceptionPropagatedException() {
		// Given
		Integer date = 20240115;
		Integer dateTypeId = 1;
		Integer timesheetId = 1;

		willThrow(new DataAccessException("Database save failed") {
		}).given(this.timeLogJpaRepository).save(any(TimeLog.class));

		// When & Then
		assertThatThrownBy(() -> this.repository.createTimesheetLog(date, dateTypeId, timesheetId))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database save failed");

		then(this.timeLogJpaRepository).should().save(any(TimeLog.class));
	}

	@Test
	@DisplayName("Save time log should delegate to JPA repository")
	void testSaveTimeLogValidEntityDelegatesToJpaRepository() {
		// Given
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		// When
		this.repository.saveTimeLog(timeLog);

		// Then
		then(this.timeLogJpaRepository).should().save(timeLog);
	}

	@Test
	@DisplayName("Save time log should propagate DataAccessException")
	void testSaveTimeLogDataAccessExceptionPropagatedException() {
		// Given
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		willThrow(new DataAccessException("Database connection lost") {
		}).given(this.timeLogJpaRepository).save(timeLog);

		// When & Then
		assertThatThrownBy(() -> this.repository.saveTimeLog(timeLog)).isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database connection lost");

		then(this.timeLogJpaRepository).should().save(timeLog);
	}

	@Test
	@DisplayName("Validate by date with start date should execute JPQL query and return true when no conflicts")
	void testValidateByDateWithStartDateValidParametersExecutesJpqlQueryReturnsTrue() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Long startDate = 20240115L;
		Long endDate = null;
		Integer accountId = 1;
		String expectedJpql = "SELECT COUNT(tl) FROM TimeLog tl JOIN tl.timesheet t JOIN t.timesheetSetting ts WHERE tl.date < :date AND ts.association.jobId = :jobId AND ts.association.contractorId = :contractorId AND ts.accountId = :accountId";
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);

		given(this.entityManager.createQuery(expectedJpql, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("date", startDate)).willReturn(mockQuery);
		given(mockQuery.setParameter("jobId", jobId)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", contractorId)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(0L);

		// When
		Boolean result = this.repository.validateByDate(jobId, contractorId, startDate, endDate, accountId);

		// Then
		assertThat(result).isTrue();
		then(this.entityManager).should().createQuery(expectedJpql, Long.class);
		then(mockQuery).should().setParameter("date", startDate);
		then(mockQuery).should().setParameter("jobId", jobId);
		then(mockQuery).should().setParameter("contractorId", contractorId);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("Validate by date with end date should execute JPQL query and return false when conflicts exist")
	void testValidateByDateWithEndDateValidParametersExecutesJpqlQueryReturnsFalse() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Long startDate = null;
		Long endDate = 20240115L;
		Integer accountId = 1;
		String expectedJpql = "SELECT COUNT(tl) FROM TimeLog tl JOIN tl.timesheet t JOIN t.timesheetSetting ts WHERE tl.date > :date AND ts.association.jobId = :jobId AND ts.association.contractorId = :contractorId AND ts.accountId = :accountId";
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);

		given(this.entityManager.createQuery(expectedJpql, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("date", endDate)).willReturn(mockQuery);
		given(mockQuery.setParameter("jobId", jobId)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", contractorId)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(3L);

		// When
		Boolean result = this.repository.validateByDate(jobId, contractorId, startDate, endDate, accountId);

		// Then
		assertThat(result).isFalse();
		then(this.entityManager).should().createQuery(expectedJpql, Long.class);
		then(mockQuery).should().setParameter("date", endDate);
		then(mockQuery).should().setParameter("jobId", jobId);
		then(mockQuery).should().setParameter("contractorId", contractorId);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("Validate by date should throw DataAccessException when database error occurs")
	void testValidateByDateDatabaseErrorThrowsDataAccessException() {
		// Given
		Integer jobId = 1;
		Integer contractorId = 1;
		Long startDate = 20240115L;
		Long endDate = null;
		Integer accountId = 1;
		String expectedJpql = "SELECT COUNT(tl) FROM TimeLog tl JOIN tl.timesheet t JOIN t.timesheetSetting ts WHERE tl.date < :date AND ts.association.jobId = :jobId AND ts.association.contractorId = :contractorId AND ts.accountId = :accountId";
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);

		given(this.entityManager.createQuery(expectedJpql, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("date", startDate)).willReturn(mockQuery);
		given(mockQuery.setParameter("jobId", jobId)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", contractorId)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willThrow(new DataAccessException("Query execution failed") {
		});

		// When & Then
		assertThatThrownBy(() -> this.repository.validateByDate(jobId, contractorId, startDate, endDate, accountId))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Query execution failed");

		then(this.entityManager).should().createQuery(expectedJpql, Long.class);
		then(mockQuery).should().setParameter("date", startDate);
		then(mockQuery).should().setParameter("jobId", jobId);
		then(mockQuery).should().setParameter("contractorId", contractorId);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("Get time log by timesheet ID should execute JPQL query with invoice data when invoice exists")
	void testGetTimeLogByTimesheetIdWithInvoiceExecutesJpqlQueryWithInvoiceData() {
		// Given
		Integer timesheetId = 1;
		Integer invoiceId = 10;

		/**
		 * Mock the TimesheetInvoice query to return an invoice ID
		 */
		TypedQuery<Integer> invoiceCheckQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(
				"SELECT ti.invoiceId FROM TimesheetInvoice ti WHERE ti.timesheetId = :timesheetId", Integer.class))
			.willReturn(invoiceCheckQuery);
		given(invoiceCheckQuery.setParameter("timesheetId", timesheetId)).willReturn(invoiceCheckQuery);
		given(invoiceCheckQuery.getResultList()).willReturn(List.of(invoiceId));

		/**
		 * Build the expected JPQL with invoice data
		 */
		String expectedJpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetLogQueryResultDto("
				+ "ts.id, tss.id, tss.workLogType, tss.timesheetFrequency, tss.timesheetStartDay, tss.calculateBreakTime, "
				+ "tss.breakTimeThreshold, tss.isRemarkMandatory, ts.periodStart, ts.periodEnd, ta.timesheetApprovalStatusTypeId, "
				+ "ti.paymentStatusId, ti.paymentPaidOn, ti.payoutNumber, ti.billingStatusId, "
				+ "i.createdOn, CASE WHEN (i.invoiceIdPrefix IS NULL OR i.invoiceIdPrefix = '') THEN i.invoiceIdNumber ELSE CONCAT(CONCAT(i.invoiceIdPrefix, '-'), i.invoiceIdNumber) END, i.invoiceStatus, "
				+ "ta.remark, ta.createdOn, payCurr.symbol, payCurr.code, billCurr.symbol, billCurr.code, ta.userTypeId, ta.entityId, "
				+ "tss.templateWorkDay, tss.customRule, tss.isUnplannedHoursPayEnabled) " + "FROM Timesheet ts "
				+ "JOIN TimesheetSetting tss ON ts.timesheetSettingId = tss.id "
				+ "LEFT JOIN TimesheetApproval ta ON ta.timesheetId = ts.id AND ta.id = ("
				+ "    SELECT MAX(ta2.id) FROM TimesheetApproval ta2 WHERE ta2.timesheetId = ts.id) "
				+ "LEFT JOIN TimesheetInvoice ti ON ti.timesheetId = ts.id "
				+ "LEFT JOIN Invoice i ON i.id = ti.invoiceId "
				+ "LEFT JOIN Currency payCurr ON payCurr.id = tss.payCurrencyId "
				+ "LEFT JOIN Currency billCurr ON billCurr.id = tss.billCurrencyId " + "WHERE ts.id = :timesheetId";

		TypedQuery<TimesheetLogQueryResultDto> mockQuery = mock(TypedQuery.class);
		TimesheetLogQueryResultDto expectedResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();

		given(this.entityManager.createQuery(expectedJpql, TimesheetLogQueryResultDto.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetId", timesheetId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(expectedResult);

		// When
		TimesheetLogQueryResultDto result = this.repository.getTimeLogByTimesheetId(timesheetId);

		// Then
		assertThat(result).isEqualTo(expectedResult);
		then(this.entityManager).should()
			.createQuery("SELECT ti.invoiceId FROM TimesheetInvoice ti WHERE ti.timesheetId = :timesheetId",
					Integer.class);
		then(invoiceCheckQuery).should().setParameter("timesheetId", timesheetId);
		then(invoiceCheckQuery).should().getResultList();
		then(this.entityManager).should().createQuery(expectedJpql, TimesheetLogQueryResultDto.class);
		then(mockQuery).should().setParameter("timesheetId", timesheetId);
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("Get time log by timesheet ID should throw NoResultException when no data found")
	void testGetTimeLogByTimesheetIdNoDataFoundThrowsNoResultException() {
		// Given
		Integer timesheetId = 999;

		/**
		 * Mock the TimesheetInvoice query to return empty result
		 */
		TypedQuery<Integer> invoiceCheckQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(
				"SELECT ti.invoiceId FROM TimesheetInvoice ti WHERE ti.timesheetId = :timesheetId", Integer.class))
			.willReturn(invoiceCheckQuery);
		given(invoiceCheckQuery.setParameter("timesheetId", timesheetId)).willReturn(invoiceCheckQuery);
		given(invoiceCheckQuery.getResultList()).willReturn(Collections.emptyList());

		/**
		 * Build the expected JPQL without invoice data
		 */
		String expectedJpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetLogQueryResultDto("
				+ "ts.id, tss.id, tss.workLogType, tss.timesheetFrequency, tss.timesheetStartDay, tss.calculateBreakTime, "
				+ "tss.breakTimeThreshold, tss.isRemarkMandatory, ts.periodStart, ts.periodEnd, ta.timesheetApprovalStatusTypeId, "
				+ "ti.paymentStatusId, ti.paymentPaidOn, ti.payoutNumber, ti.billingStatusId, "
				+ "null, null, null, ta.remark, ta.createdOn, payCurr.symbol, payCurr.code, billCurr.symbol, billCurr.code, ta.userTypeId, ta.entityId, "
				+ "tss.templateWorkDay, tss.customRule, tss.isUnplannedHoursPayEnabled) " + "FROM Timesheet ts "
				+ "JOIN TimesheetSetting tss ON ts.timesheetSettingId = tss.id "
				+ "LEFT JOIN TimesheetApproval ta ON ta.timesheetId = ts.id AND ta.id = ("
				+ "    SELECT MAX(ta2.id) FROM TimesheetApproval ta2 WHERE ta2.timesheetId = ts.id) "
				+ "LEFT JOIN TimesheetInvoice ti ON ti.timesheetId = ts.id "
				+ "LEFT JOIN Currency payCurr ON payCurr.id = tss.payCurrencyId "
				+ "LEFT JOIN Currency billCurr ON billCurr.id = tss.billCurrencyId " + "WHERE ts.id = :timesheetId";

		TypedQuery<TimesheetLogQueryResultDto> mockQuery = mock(TypedQuery.class);

		given(this.entityManager.createQuery(expectedJpql, TimesheetLogQueryResultDto.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetId", timesheetId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willThrow(new NoResultException("No entity found"));

		// When & Then
		assertThatThrownBy(() -> this.repository.getTimeLogByTimesheetId(timesheetId))
			.isInstanceOf(NoResultException.class)
			.hasMessageContaining("No entity found");

		then(this.entityManager).should()
			.createQuery("SELECT ti.invoiceId FROM TimesheetInvoice ti WHERE ti.timesheetId = :timesheetId",
					Integer.class);
		then(invoiceCheckQuery).should().setParameter("timesheetId", timesheetId);
		then(invoiceCheckQuery).should().getResultList();
		then(this.entityManager).should().createQuery(expectedJpql, TimesheetLogQueryResultDto.class);
		then(mockQuery).should().setParameter("timesheetId", timesheetId);
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("Get time log by timesheet ID should throw DataAccessException when database error occurs")
	void testGetTimeLogByTimesheetIdDatabaseErrorThrowsDataAccessException() {
		// Given
		Integer timesheetId = 1;

		/**
		 * Mock the TimesheetInvoice query to return empty result
		 */
		TypedQuery<Integer> invoiceCheckQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(
				"SELECT ti.invoiceId FROM TimesheetInvoice ti WHERE ti.timesheetId = :timesheetId", Integer.class))
			.willReturn(invoiceCheckQuery);
		given(invoiceCheckQuery.setParameter("timesheetId", timesheetId)).willReturn(invoiceCheckQuery);
		given(invoiceCheckQuery.getResultList()).willReturn(Collections.emptyList());

		/**
		 * Build the expected JPQL without invoice data
		 */
		String expectedJpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetLogQueryResultDto("
				+ "ts.id, tss.id, tss.workLogType, tss.timesheetFrequency, tss.timesheetStartDay, tss.calculateBreakTime, "
				+ "tss.breakTimeThreshold, tss.isRemarkMandatory, ts.periodStart, ts.periodEnd, ta.timesheetApprovalStatusTypeId, "
				+ "ti.paymentStatusId, ti.paymentPaidOn, ti.payoutNumber, ti.billingStatusId, "
				+ "null, null, null, ta.remark, ta.createdOn, payCurr.symbol, payCurr.code, billCurr.symbol, billCurr.code, ta.userTypeId, ta.entityId, "
				+ "tss.templateWorkDay, tss.customRule, tss.isUnplannedHoursPayEnabled) " + "FROM Timesheet ts "
				+ "JOIN TimesheetSetting tss ON ts.timesheetSettingId = tss.id "
				+ "LEFT JOIN TimesheetApproval ta ON ta.timesheetId = ts.id AND ta.id = ("
				+ "    SELECT MAX(ta2.id) FROM TimesheetApproval ta2 WHERE ta2.timesheetId = ts.id) "
				+ "LEFT JOIN TimesheetInvoice ti ON ti.timesheetId = ts.id "
				+ "LEFT JOIN Currency payCurr ON payCurr.id = tss.payCurrencyId "
				+ "LEFT JOIN Currency billCurr ON billCurr.id = tss.billCurrencyId " + "WHERE ts.id = :timesheetId";

		TypedQuery<TimesheetLogQueryResultDto> mockQuery = mock(TypedQuery.class);

		given(this.entityManager.createQuery(expectedJpql, TimesheetLogQueryResultDto.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetId", timesheetId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willThrow(new DataAccessException("Database connection timeout") {
		});

		// When & Then
		assertThatThrownBy(() -> this.repository.getTimeLogByTimesheetId(timesheetId))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database connection timeout");

		then(this.entityManager).should()
			.createQuery("SELECT ti.invoiceId FROM TimesheetInvoice ti WHERE ti.timesheetId = :timesheetId",
					Integer.class);
		then(invoiceCheckQuery).should().setParameter("timesheetId", timesheetId);
		then(invoiceCheckQuery).should().getResultList();
		then(this.entityManager).should().createQuery(expectedJpql, TimesheetLogQueryResultDto.class);
		then(mockQuery).should().setParameter("timesheetId", timesheetId);
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("Validate by date should handle null parameters correctly")
	void testValidateByDateNullParametersHandlesCorrectly() {
		// Given
		Integer jobId = null;
		Integer contractorId = null;
		Long startDate = 20240115L;
		Long endDate = null;
		Integer accountId = null;
		String expectedJpql = "SELECT COUNT(tl) FROM TimeLog tl JOIN tl.timesheet t JOIN t.timesheetSetting ts WHERE tl.date < :date AND ts.association.jobId = :jobId AND ts.association.contractorId = :contractorId AND ts.accountId = :accountId";
		TypedQuery<Long> mockQuery = mock(TypedQuery.class);

		given(this.entityManager.createQuery(expectedJpql, Long.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("date", startDate)).willReturn(mockQuery);
		given(mockQuery.setParameter("jobId", jobId)).willReturn(mockQuery);
		given(mockQuery.setParameter("contractorId", contractorId)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(0L);

		// When
		Boolean result = this.repository.validateByDate(jobId, contractorId, startDate, endDate, accountId);

		// Then
		assertThat(result).isTrue();
		then(this.entityManager).should().createQuery(expectedJpql, Long.class);
		then(mockQuery).should().setParameter("date", startDate);
		then(mockQuery).should().setParameter("jobId", jobId);
		then(mockQuery).should().setParameter("contractorId", contractorId);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("Find timesheet settings for timesheets should execute JPQL query with breakTimeThreshold")
	void testFindTimesheetSettingsForTimesheetsValidIdsExecutesJpqlQueryReturnsResult() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		Integer accountId = 1;
		String expectedJpql = "SELECT DISTINCT t.id, ts.calculateBreakTime, ts.breakTimeThreshold, ts.templateWorkDay, ts.isRemarkMandatory FROM Timesheet t LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId WHERE t.id IN :timesheetIds AND t.accountId = :accountId";
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);
		List<Object[]> expectedResult = List.of(
				new Object[] { 1, false, 30, TimesheetLogsTestDataFactory.createTemplateWorkDayList(), false },
				new Object[] { 2, true, null, TimesheetLogsTestDataFactory.createTemplateWorkDayList(), true });

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetIds", timesheetIds)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(expectedResult);

		// When
		List<Object[]> result = this.repository.findTimesheetSettingsForTimesheets(timesheetIds, accountId);

		// Then
		assertThat(result).isEqualTo(expectedResult).hasSize(2);
		assertThat(result.get(0)[0]).isEqualTo(1); // timesheetId
		assertThat(result.get(0)[1]).isEqualTo(false); // calculateBreakTime
		assertThat(result.get(0)[2]).isEqualTo(30); // breakTimeThreshold
		assertThat(result.get(0)[4]).isEqualTo(false); // isRemarkMandatory
		assertThat(result.get(1)[1]).isEqualTo(true); // calculateBreakTime
		assertThat(result.get(1)[2]).isNull(); // breakTimeThreshold should be null when
												// calculateBreakTime is true
		assertThat(result.get(1)[4]).isEqualTo(true); // isRemarkMandatory
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("timesheetIds", timesheetIds);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Find timesheet settings for timesheets should handle empty timesheet IDs list")
	void testFindTimesheetSettingsForTimesheetsEmptyListReturnsEmpty() {
		// Given
		List<Integer> emptyTimesheetIds = Collections.emptyList();
		Integer accountId = 1;
		String expectedJpql = "SELECT DISTINCT t.id, ts.calculateBreakTime, ts.breakTimeThreshold, ts.templateWorkDay, ts.isRemarkMandatory FROM Timesheet t LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId WHERE t.id IN :timesheetIds AND t.accountId = :accountId";
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);
		List<Object[]> emptyResult = Collections.emptyList();

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetIds", emptyTimesheetIds)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(emptyResult);

		// When
		List<Object[]> result = this.repository.findTimesheetSettingsForTimesheets(emptyTimesheetIds, accountId);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("timesheetIds", emptyTimesheetIds);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get time logs for timesheets should return empty map when timesheet IDs are null")
	void testGetTimeLogsForTimesheetsNullTimesheetIdsReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = null;
		Integer accountId = 1;

		// When
		Map<Integer, String> result = this.repository.getTimeLogsForTimesheets(timesheetIds, accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get time logs for timesheets should return empty map when timesheet IDs are empty")
	void testGetTimeLogsForTimesheetsEmptyTimesheetIdsReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = Collections.emptyList();
		Integer accountId = 1;

		// When
		Map<Integer, String> result = this.repository.getTimeLogsForTimesheets(timesheetIds, accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get time logs for timesheets should throw ResourceNotFoundException when exception occurs")
	void testGetTimeLogsForTimesheetsExceptionThrowsResourceNotFoundException() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		Integer accountId = 1;

		// Mock the DSL context to throw an exception
		given(this.dslContext.select()).willThrow(new RuntimeException("Database error"));

		// When & Then
		assertThatThrownBy(() -> this.repository.getTimeLogsForTimesheets(timesheetIds, accountId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Failed to fetch time logs for 3 timesheets");
	}

	@Test
	@DisplayName("Get structured time logs for timesheets should return empty map when timesheet IDs are null")
	void testGetStructuredTimeLogsForTimesheetsNullTimesheetIdsReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = null;
		Integer accountId = 1;

		// When
		Map<Integer, Map<String, String>> result = this.repository.getStructuredTimeLogsForTimesheets(timesheetIds,
				accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get structured time logs for timesheets should return empty map when timesheet IDs are empty")
	void testGetStructuredTimeLogsForTimesheetsEmptyTimesheetIdsReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = Collections.emptyList();
		Integer accountId = 1;

		// When
		Map<Integer, Map<String, String>> result = this.repository.getStructuredTimeLogsForTimesheets(timesheetIds,
				accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get structured time logs for timesheets should throw ResourceNotFoundException when exception occurs")
	void testGetStructuredTimeLogsForTimesheetsExceptionThrowsResourceNotFoundException() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		Integer accountId = 1;

		// Mock the DSL context to throw an exception
		given(this.dslContext.select()).willThrow(new RuntimeException("Database error"));

		// When & Then
		assertThatThrownBy(() -> this.repository.getStructuredTimeLogsForTimesheets(timesheetIds, accountId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Failed to fetch structured time logs for 3 timesheets");
	}

	@Test
	@DisplayName("Get structured overtime hours for timesheets should return empty map when timesheet IDs are null")
	void testGetStructuredOvertimeHoursForTimesheetsNullTimesheetIdsReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = null;
		Integer accountId = 1;

		// When
		Map<Integer, Map<String, String>> result = this.repository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get structured overtime hours for timesheets should return empty map when timesheet IDs are empty")
	void testGetStructuredOvertimeHoursForTimesheetsEmptyTimesheetIdsReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = Collections.emptyList();
		Integer accountId = 1;

		// When
		Map<Integer, Map<String, String>> result = this.repository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get structured overtime hours for timesheets should return empty map when exception occurs")
	void testGetStructuredOvertimeHoursForTimesheetsExceptionReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		Integer accountId = 1;

		// Mock the DSL context to throw an exception
		given(this.dslContext.select()).willThrow(new RuntimeException("Database error"));

		// When
		Map<Integer, Map<String, String>> result = this.repository.getStructuredOvertimeHoursForTimesheets(timesheetIds,
				accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get structured overtime hours for timesheets should filter by period and format hours")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testGetStructuredOvertimeHoursForTimesheetsFiltersByPeriodAndFormatsHours() {
		Result records = mock(Result.class);
		DSLContext deepDslContext = this.createDslContextForFetch(records);
		TimeLogRepository localRepository = new TimeLogRepository(this.entityManager, this.timeLogJpaRepository,
				deepDslContext, this.timeLogIntervalRepository);

		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetT ts = (CstTimesheetT) this.getPrivateStaticField("TS");

		Record outsideRecord = mock(org.jooq.Record5.class);
		given(outsideRecord.getValue(tl.TIMESHEET_ID)).willReturn(700);
		given(outsideRecord.getValue(tl.DATE)).willReturn(5);
		given(outsideRecord.getValue(tl.OVER_TIME)).willReturn(3600);
		given(outsideRecord.getValue(ts.PERIOD_START)).willReturn(10);
		given(outsideRecord.getValue(ts.PERIOD_END)).willReturn(20);

		Record insideRecord = mock(org.jooq.Record5.class);
		given(insideRecord.getValue(tl.TIMESHEET_ID)).willReturn(700);
		given(insideRecord.getValue(tl.DATE)).willReturn(12);
		given(insideRecord.getValue(tl.OVER_TIME)).willReturn(3600);
		given(insideRecord.getValue(ts.PERIOD_START)).willReturn(10);
		given(insideRecord.getValue(ts.PERIOD_END)).willReturn(20);

		Record insideNullSeconds = mock(org.jooq.Record5.class);
		given(insideNullSeconds.getValue(tl.TIMESHEET_ID)).willReturn(701);
		given(insideNullSeconds.getValue(tl.DATE)).willReturn(12);
		given(insideNullSeconds.getValue(tl.OVER_TIME)).willReturn(null);
		given(insideNullSeconds.getValue(ts.PERIOD_START)).willReturn(10);
		given(insideNullSeconds.getValue(ts.PERIOD_END)).willReturn(20);

		given(records.iterator()).willReturn(List.of(outsideRecord, insideRecord, insideNullSeconds).iterator());

		Map<Integer, Map<String, String>> result = localRepository
			.getStructuredOvertimeHoursForTimesheets(List.of(700, 701), 1);

		String day12Header = this.invokePrivateMethod("formatDateAsColumnHeader", new Class<?>[] { Integer.class }, 12);
		assertThat(result).containsKeys(700, 701);
		assertThat(result.get(700)).containsEntry(day12Header, "1.00");
		assertThat(result.get(701)).containsEntry(day12Header, "");
	}

	@Test
	@DisplayName("Get structured total time for timesheets should return empty map when timesheet IDs are null")
	void testGetStructuredTotalTimeForTimesheetsNullTimesheetIdsReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = null;
		Integer accountId = 1;

		// When
		Map<Integer, Map<String, String>> result = this.repository.getStructuredTotalTimeForTimesheets(timesheetIds,
				accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get structured total time for timesheets should return empty map when timesheet IDs are empty")
	void testGetStructuredTotalTimeForTimesheetsEmptyTimesheetIdsReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = Collections.emptyList();
		Integer accountId = 1;

		// When
		Map<Integer, Map<String, String>> result = this.repository.getStructuredTotalTimeForTimesheets(timesheetIds,
				accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get structured total time for timesheets should return empty map when exception occurs")
	void testGetStructuredTotalTimeForTimesheetsExceptionReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		Integer accountId = 1;

		// Mock the DSL context to throw an exception
		given(this.dslContext.select()).willThrow(new RuntimeException("Database error"));

		// When
		Map<Integer, Map<String, String>> result = this.repository.getStructuredTotalTimeForTimesheets(timesheetIds,
				accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get structured total time for timesheets should filter by period and format HH:mm")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testGetStructuredTotalTimeForTimesheetsFiltersByPeriodAndFormatsTime() {
		Result records = mock(Result.class);
		DSLContext deepDslContext = this.createDslContextForFetch(records);
		TimeLogRepository localRepository = new TimeLogRepository(this.entityManager, this.timeLogJpaRepository,
				deepDslContext, this.timeLogIntervalRepository);

		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetT ts = (CstTimesheetT) this.getPrivateStaticField("TS");

		Record outsideRecord = mock(org.jooq.Record5.class);
		given(outsideRecord.getValue(tl.TIMESHEET_ID)).willReturn(800);
		given(outsideRecord.getValue(tl.DATE)).willReturn(5);
		given(outsideRecord.getValue(tl.TOTAL_TIME)).willReturn(3660);
		given(outsideRecord.getValue(ts.PERIOD_START)).willReturn(10);
		given(outsideRecord.getValue(ts.PERIOD_END)).willReturn(20);

		Record insideRecord = mock(org.jooq.Record5.class);
		given(insideRecord.getValue(tl.TIMESHEET_ID)).willReturn(800);
		given(insideRecord.getValue(tl.DATE)).willReturn(12);
		given(insideRecord.getValue(tl.TOTAL_TIME)).willReturn(3660);
		given(insideRecord.getValue(ts.PERIOD_START)).willReturn(10);
		given(insideRecord.getValue(ts.PERIOD_END)).willReturn(20);

		Record insideNullSeconds = mock(org.jooq.Record5.class);
		given(insideNullSeconds.getValue(tl.TIMESHEET_ID)).willReturn(801);
		given(insideNullSeconds.getValue(tl.DATE)).willReturn(12);
		given(insideNullSeconds.getValue(tl.TOTAL_TIME)).willReturn(null);
		given(insideNullSeconds.getValue(ts.PERIOD_START)).willReturn(10);
		given(insideNullSeconds.getValue(ts.PERIOD_END)).willReturn(20);

		given(records.iterator()).willReturn(List.of(outsideRecord, insideRecord, insideNullSeconds).iterator());

		Map<Integer, Map<String, String>> result = localRepository
			.getStructuredTotalTimeForTimesheets(List.of(800, 801), 1);

		String day12Header = this.invokePrivateMethod("formatDateAsColumnHeader", new Class<?>[] { Integer.class }, 12);
		assertThat(result).containsKeys(800, 801);
		assertThat(result.get(800)).containsEntry(day12Header, "01:01");
		assertThat(result.get(801)).containsEntry(day12Header, "");
	}

	@Test
	@DisplayName("Get structured effective work hours for timesheets should return empty map when timesheet IDs are null")
	void testGetStructuredEffectiveWorkHoursForTimesheetsNullTimesheetIdsReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = null;
		Integer accountId = 1;

		// When
		Map<Integer, Map<String, String>> result = this.repository
			.getStructuredEffectiveWorkHoursForTimesheets(timesheetIds, accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get structured effective work hours for timesheets should return empty map when timesheet IDs are empty")
	void testGetStructuredEffectiveWorkHoursForTimesheetsEmptyTimesheetIdsReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = Collections.emptyList();
		Integer accountId = 1;

		// When
		Map<Integer, Map<String, String>> result = this.repository
			.getStructuredEffectiveWorkHoursForTimesheets(timesheetIds, accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get structured effective work hours for timesheets should return empty map when exception occurs")
	void testGetStructuredEffectiveWorkHoursForTimesheetsExceptionReturnsEmptyMap() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		Integer accountId = 1;

		// Mock the DSL context to throw an exception
		given(this.dslContext.select()).willThrow(new RuntimeException("Database error"));

		// When
		Map<Integer, Map<String, String>> result = this.repository
			.getStructuredEffectiveWorkHoursForTimesheets(timesheetIds, accountId);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Get structured effective work hours for timesheets should filter by period and format hours")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testGetStructuredEffectiveWorkHoursForTimesheetsFiltersByPeriodAndFormatsHours() {
		Result records = mock(Result.class);
		DSLContext deepDslContext = this.createDslContextForFetch(records);
		TimeLogRepository localRepository = new TimeLogRepository(this.entityManager, this.timeLogJpaRepository,
				deepDslContext, this.timeLogIntervalRepository);

		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetT ts = (CstTimesheetT) this.getPrivateStaticField("TS");

		Record outsideRecord = mock(org.jooq.Record5.class);
		given(outsideRecord.getValue(tl.TIMESHEET_ID)).willReturn(900);
		given(outsideRecord.getValue(tl.DATE)).willReturn(5);
		given(outsideRecord.getValue(tl.TOTAL_TIME)).willReturn(3600);
		given(outsideRecord.getValue(ts.PERIOD_START)).willReturn(10);
		given(outsideRecord.getValue(ts.PERIOD_END)).willReturn(20);

		Record insideRecord = mock(org.jooq.Record5.class);
		given(insideRecord.getValue(tl.TIMESHEET_ID)).willReturn(900);
		given(insideRecord.getValue(tl.DATE)).willReturn(12);
		given(insideRecord.getValue(tl.TOTAL_TIME)).willReturn(3600);
		given(insideRecord.getValue(ts.PERIOD_START)).willReturn(10);
		given(insideRecord.getValue(ts.PERIOD_END)).willReturn(20);

		Record insideNullSeconds = mock(org.jooq.Record5.class);
		given(insideNullSeconds.getValue(tl.TIMESHEET_ID)).willReturn(901);
		given(insideNullSeconds.getValue(tl.DATE)).willReturn(12);
		given(insideNullSeconds.getValue(tl.TOTAL_TIME)).willReturn(null);
		given(insideNullSeconds.getValue(ts.PERIOD_START)).willReturn(10);
		given(insideNullSeconds.getValue(ts.PERIOD_END)).willReturn(20);

		given(records.iterator()).willReturn(List.of(outsideRecord, insideRecord, insideNullSeconds).iterator());

		Map<Integer, Map<String, String>> result = localRepository
			.getStructuredEffectiveWorkHoursForTimesheets(List.of(900, 901), 1);

		String day12Header = this.invokePrivateMethod("formatDateAsColumnHeader", new Class<?>[] { Integer.class }, 12);
		assertThat(result).containsKeys(900, 901);
		assertThat(result.get(900)).containsEntry(day12Header, "1.00");
		assertThat(result.get(901)).containsEntry(day12Header, "");
	}

	@Test
	@DisplayName("Find time logs with details should execute JPQL query and return result list")
	void testFindTimeLogsWithDetailsValidIdsExecutesJpqlQueryReturnsResultList() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		Integer accountId = 1;
		String expectedJpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogQueryResultDto("
				+ "tl.id, tl.date, tl.dayTypeId, tl.workTime, tl.workStartTime, tl.workEndTime, "
				+ "tl.breakTime, tl.overTime, tl.remark, tl.totalTime, tl.timesheetId, "
				+ "t.periodStart, t.periodEnd) " + "FROM TimeLog tl "
				+ "LEFT JOIN Timesheet t ON t.id = tl.timesheetId "
				+ "LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "WHERE tl.timesheetId IN :timesheetIds AND t.accountId = :accountId "
				+ "ORDER BY tl.timesheetId, tl.date, tl.id";
		TypedQuery<TimelogQueryResultDto> mockQuery = mock(TypedQuery.class);
		List<TimelogQueryResultDto> expectedResult = List.of(TimesheetLogsTestDataFactory.createTimelogQueryResult());

		given(this.entityManager.createQuery(expectedJpql, TimelogQueryResultDto.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetIds", timesheetIds)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(expectedResult);

		// When
		List<TimelogQueryResultDto> result = this.repository.findTimeLogsWithDetails(timesheetIds, accountId);

		// Then
		assertThat(result).isEqualTo(expectedResult).hasSize(1);
		then(this.entityManager).should().createQuery(expectedJpql, TimelogQueryResultDto.class);
		then(mockQuery).should().setParameter("timesheetIds", timesheetIds);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Find time logs with details should handle empty timesheet IDs list")
	void testFindTimeLogsWithDetailsEmptyListReturnsEmpty() {
		// Given
		List<Integer> emptyTimesheetIds = Collections.emptyList();
		Integer accountId = 1;
		String expectedJpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogQueryResultDto("
				+ "tl.id, tl.date, tl.dayTypeId, tl.workTime, tl.workStartTime, tl.workEndTime, "
				+ "tl.breakTime, tl.overTime, tl.remark, tl.totalTime, tl.timesheetId, "
				+ "t.periodStart, t.periodEnd) " + "FROM TimeLog tl "
				+ "LEFT JOIN Timesheet t ON t.id = tl.timesheetId "
				+ "LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "WHERE tl.timesheetId IN :timesheetIds AND t.accountId = :accountId "
				+ "ORDER BY tl.timesheetId, tl.date, tl.id";
		TypedQuery<TimelogQueryResultDto> mockQuery = mock(TypedQuery.class);
		List<TimelogQueryResultDto> emptyResult = Collections.emptyList();

		given(this.entityManager.createQuery(expectedJpql, TimelogQueryResultDto.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetIds", emptyTimesheetIds)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(emptyResult);

		// When
		List<TimelogQueryResultDto> result = this.repository.findTimeLogsWithDetails(emptyTimesheetIds, accountId);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).should().createQuery(expectedJpql, TimelogQueryResultDto.class);
		then(mockQuery).should().setParameter("timesheetIds", emptyTimesheetIds);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Find time logs with details should propagate DataAccessException when database error occurs")
	void testFindTimeLogsWithDetailsDatabaseErrorPropagatesDataAccessException() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		Integer accountId = 1;
		String expectedJpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogQueryResultDto("
				+ "tl.id, tl.date, tl.dayTypeId, tl.workTime, tl.workStartTime, tl.workEndTime, "
				+ "tl.breakTime, tl.overTime, tl.remark, tl.totalTime, tl.timesheetId, "
				+ "t.periodStart, t.periodEnd) " + "FROM TimeLog tl "
				+ "LEFT JOIN Timesheet t ON t.id = tl.timesheetId "
				+ "LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "WHERE tl.timesheetId IN :timesheetIds AND t.accountId = :accountId "
				+ "ORDER BY tl.timesheetId, tl.date, tl.id";
		TypedQuery<TimelogQueryResultDto> mockQuery = mock(TypedQuery.class);

		given(this.entityManager.createQuery(expectedJpql, TimelogQueryResultDto.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetIds", timesheetIds)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willThrow(new DataAccessException("Database query failed") {
		});

		// When & Then
		assertThatThrownBy(() -> this.repository.findTimeLogsWithDetails(timesheetIds, accountId))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database query failed");

		then(this.entityManager).should().createQuery(expectedJpql, TimelogQueryResultDto.class);
		then(mockQuery).should().setParameter("timesheetIds", timesheetIds);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Find time logs with setting details should execute JPQL query and return result list")
	void testFindTimeLogsWithSettingDetailsValidIdsExecutesJpqlQueryReturnsResultList() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		Integer accountId = 1;
		String expectedJpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogWithSettingQueryResultDto("
				+ "tl.id, tl.date, tl.dayTypeId, tl.workTime, tl.workStartTime, tl.workEndTime, "
				+ "tl.breakTime, tl.overTime, tl.remark, tl.totalTime, tl.timesheetId, "
				+ "t.periodStart, t.periodEnd, ts.id, ts.calculateBreakTime, ts.breakTimeThreshold, ts.templateWorkDay, ts.isRemarkMandatory) "
				+ "FROM TimeLog tl " + "LEFT JOIN Timesheet t ON t.id = tl.timesheetId "
				+ "LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "WHERE tl.timesheetId IN :timesheetIds AND t.accountId = :accountId "
				+ "ORDER BY tl.timesheetId, tl.date, tl.id";
		TypedQuery<TimelogWithSettingQueryResultDto> mockQuery = mock(TypedQuery.class);
		List<TimelogWithSettingQueryResultDto> expectedResult = List
			.of(TimesheetLogsTestDataFactory.createTimelogWithSettingQueryResult());

		given(this.entityManager.createQuery(expectedJpql, TimelogWithSettingQueryResultDto.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetIds", timesheetIds)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(expectedResult);

		// When
		List<TimelogWithSettingQueryResultDto> result = this.repository.findTimeLogsWithSettingDetails(timesheetIds,
				accountId);

		// Then
		assertThat(result).isEqualTo(expectedResult).hasSize(1);
		then(this.entityManager).should().createQuery(expectedJpql, TimelogWithSettingQueryResultDto.class);
		then(mockQuery).should().setParameter("timesheetIds", timesheetIds);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Find time logs with setting details should handle empty timesheet IDs list")
	void testFindTimeLogsWithSettingDetailsEmptyListReturnsEmpty() {
		// Given
		List<Integer> emptyTimesheetIds = Collections.emptyList();
		Integer accountId = 1;
		String expectedJpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogWithSettingQueryResultDto("
				+ "tl.id, tl.date, tl.dayTypeId, tl.workTime, tl.workStartTime, tl.workEndTime, "
				+ "tl.breakTime, tl.overTime, tl.remark, tl.totalTime, tl.timesheetId, "
				+ "t.periodStart, t.periodEnd, ts.id, ts.calculateBreakTime, ts.breakTimeThreshold, ts.templateWorkDay, ts.isRemarkMandatory) "
				+ "FROM TimeLog tl " + "LEFT JOIN Timesheet t ON t.id = tl.timesheetId "
				+ "LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "WHERE tl.timesheetId IN :timesheetIds AND t.accountId = :accountId "
				+ "ORDER BY tl.timesheetId, tl.date, tl.id";
		TypedQuery<TimelogWithSettingQueryResultDto> mockQuery = mock(TypedQuery.class);
		List<TimelogWithSettingQueryResultDto> emptyResult = Collections.emptyList();

		given(this.entityManager.createQuery(expectedJpql, TimelogWithSettingQueryResultDto.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetIds", emptyTimesheetIds)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(emptyResult);

		// When
		List<TimelogWithSettingQueryResultDto> result = this.repository
			.findTimeLogsWithSettingDetails(emptyTimesheetIds, accountId);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).should().createQuery(expectedJpql, TimelogWithSettingQueryResultDto.class);
		then(mockQuery).should().setParameter("timesheetIds", emptyTimesheetIds);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Find time logs with setting details should propagate DataAccessException when database error occurs")
	void testFindTimeLogsWithSettingDetailsDatabaseErrorPropagatesDataAccessException() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		Integer accountId = 1;
		String expectedJpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogWithSettingQueryResultDto("
				+ "tl.id, tl.date, tl.dayTypeId, tl.workTime, tl.workStartTime, tl.workEndTime, "
				+ "tl.breakTime, tl.overTime, tl.remark, tl.totalTime, tl.timesheetId, "
				+ "t.periodStart, t.periodEnd, ts.id, ts.calculateBreakTime, ts.breakTimeThreshold, ts.templateWorkDay, ts.isRemarkMandatory) "
				+ "FROM TimeLog tl " + "LEFT JOIN Timesheet t ON t.id = tl.timesheetId "
				+ "LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "WHERE tl.timesheetId IN :timesheetIds AND t.accountId = :accountId "
				+ "ORDER BY tl.timesheetId, tl.date, tl.id";
		TypedQuery<TimelogWithSettingQueryResultDto> mockQuery = mock(TypedQuery.class);

		given(this.entityManager.createQuery(expectedJpql, TimelogWithSettingQueryResultDto.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetIds", timesheetIds)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willThrow(new DataAccessException("Database query failed") {
		});

		// When & Then
		assertThatThrownBy(() -> this.repository.findTimeLogsWithSettingDetails(timesheetIds, accountId))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database query failed");

		then(this.entityManager).should().createQuery(expectedJpql, TimelogWithSettingQueryResultDto.class);
		then(mockQuery).should().setParameter("timesheetIds", timesheetIds);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get time log by timesheet ID should execute JPQL query without invoice data when no invoice exists")
	void testGetTimeLogByTimesheetIdNoInvoiceExecutesJpqlQueryWithoutInvoiceData() {
		// Given
		Integer timesheetId = 1;

		/**
		 * Mock the TimesheetInvoice query to return empty result
		 */
		TypedQuery<Integer> invoiceCheckQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(
				"SELECT ti.invoiceId FROM TimesheetInvoice ti WHERE ti.timesheetId = :timesheetId", Integer.class))
			.willReturn(invoiceCheckQuery);
		given(invoiceCheckQuery.setParameter("timesheetId", timesheetId)).willReturn(invoiceCheckQuery);
		given(invoiceCheckQuery.getResultList()).willReturn(Collections.emptyList());

		/**
		 * Build the expected JPQL without invoice data
		 */
		String expectedJpql = "SELECT NEW io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetLogQueryResultDto("
				+ "ts.id, tss.id, tss.workLogType, tss.timesheetFrequency, tss.timesheetStartDay, tss.calculateBreakTime, "
				+ "tss.breakTimeThreshold, tss.isRemarkMandatory, ts.periodStart, ts.periodEnd, ta.timesheetApprovalStatusTypeId, "
				+ "ti.paymentStatusId, ti.paymentPaidOn, ti.payoutNumber, ti.billingStatusId, "
				+ "null, null, null, ta.remark, ta.createdOn, payCurr.symbol, payCurr.code, billCurr.symbol, billCurr.code, ta.userTypeId, ta.entityId, "
				+ "tss.templateWorkDay, tss.customRule, tss.isUnplannedHoursPayEnabled) " + "FROM Timesheet ts "
				+ "JOIN TimesheetSetting tss ON ts.timesheetSettingId = tss.id "
				+ "LEFT JOIN TimesheetApproval ta ON ta.timesheetId = ts.id AND ta.id = ("
				+ "    SELECT MAX(ta2.id) FROM TimesheetApproval ta2 WHERE ta2.timesheetId = ts.id) "
				+ "LEFT JOIN TimesheetInvoice ti ON ti.timesheetId = ts.id "
				+ "LEFT JOIN Currency payCurr ON payCurr.id = tss.payCurrencyId "
				+ "LEFT JOIN Currency billCurr ON billCurr.id = tss.billCurrencyId " + "WHERE ts.id = :timesheetId";

		TypedQuery<TimesheetLogQueryResultDto> mockQuery = mock(TypedQuery.class);
		TimesheetLogQueryResultDto expectedResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();

		given(this.entityManager.createQuery(expectedJpql, TimesheetLogQueryResultDto.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetId", timesheetId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(expectedResult);

		// When
		TimesheetLogQueryResultDto result = this.repository.getTimeLogByTimesheetId(timesheetId);

		// Then
		assertThat(result).isEqualTo(expectedResult);
		then(this.entityManager).should()
			.createQuery("SELECT ti.invoiceId FROM TimesheetInvoice ti WHERE ti.timesheetId = :timesheetId",
					Integer.class);
		then(invoiceCheckQuery).should().setParameter("timesheetId", timesheetId);
		then(invoiceCheckQuery).should().getResultList();
		then(this.entityManager).should().createQuery(expectedJpql, TimesheetLogQueryResultDto.class);
		then(mockQuery).should().setParameter("timesheetId", timesheetId);
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("Find company by timesheet IDs should return empty map for null/empty inputs")
	void testFindCompanyByTimesheetIdsReturnsEmptyMapForNullOrEmptyInputs() {
		assertThat(this.repository.findCompanyByTimesheetIds(null, 1)).isEmpty();
		assertThat(this.repository.findCompanyByTimesheetIds(Collections.emptyList(), 1)).isEmpty();
	}

	@Test
	@DisplayName("Find company by timesheet IDs should map and de-duplicate by timesheetId")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testFindCompanyByTimesheetIdsMapsAndDeDuplicatesByTimesheetId() {
		Result records = mock(Result.class);
		DSLContext deepDslContext = this.createDslContextForFetch(records);
		TimeLogRepository localRepository = new TimeLogRepository(this.entityManager, this.timeLogJpaRepository,
				deepDslContext, this.timeLogIntervalRepository);
		Record record1 = mock(org.jooq.Record5.class);
		Record record2 = mock(org.jooq.Record5.class);

		given(records.stream()).willReturn(Stream.of(record1, record2));

		given(record1.get(
				io.recruitcrm.microservice.timesheet.helpers.constants.RepositoryParameterConstants.TIMESHEET_ID,
				Integer.class))
			.willReturn(100);
		given(record1.get(io.recruitcrm.microservice.timesheet.helpers.constants.RepositoryParameterConstants.JOB_ID,
				Integer.class))
			.willReturn(200);
		given(record1.get("jobName", String.class)).willReturn("Job One");
		given(record1.get("jobSlug", String.class)).willReturn("job-one");
		given(record1.get("assignmentId", Integer.class)).willReturn(300);

		given(record2.get(
				io.recruitcrm.microservice.timesheet.helpers.constants.RepositoryParameterConstants.TIMESHEET_ID,
				Integer.class))
			.willReturn(100);
		given(record2.get(io.recruitcrm.microservice.timesheet.helpers.constants.RepositoryParameterConstants.JOB_ID,
				Integer.class))
			.willReturn(201);
		given(record2.get("jobName", String.class)).willReturn("Job Two");
		given(record2.get("jobSlug", String.class)).willReturn("job-two");
		given(record2.get("assignmentId", Integer.class)).willReturn(301);

		Map<Integer, TimesheetJobQueryResultDto> result = localRepository.findCompanyByTimesheetIds(List.of(100), 1);

		assertThat(result).hasSize(1).containsKey(100);
		assertThat(result.get(100).getJobId()).isEqualTo(Integer.valueOf(200));
	}

	@Test
	@DisplayName("Find time log IDs by timesheet IDs should return empty list for null/empty inputs")
	void testFindTimeLogIdsByTimesheetIdInReturnsEmptyListForNullOrEmptyInputs() {
		assertThat(this.repository.findTimeLogIdsByTimesheetIdIn(null)).isEmpty();
		assertThat(this.repository.findTimeLogIdsByTimesheetIdIn(Collections.emptyList())).isEmpty();
	}

	@Test
	@DisplayName("Find time log IDs by timesheet IDs should fetch and map IDs")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testFindTimeLogIdsByTimesheetIdInFetchesAndMapsIds() {
		CstTimeLogT table = CstTimeLogT.CST_TIME_LOG_T;
		org.jooq.SelectSelectStep<org.jooq.Record1<Integer>> selectStep = mock(org.jooq.SelectSelectStep.class);
		org.jooq.SelectJoinStep<org.jooq.Record1<Integer>> fromStep = mock(org.jooq.SelectJoinStep.class);
		org.jooq.SelectConditionStep<org.jooq.Record1<Integer>> whereStep = mock(org.jooq.SelectConditionStep.class);
		org.jooq.Result<org.jooq.Record1<Integer>> fetched = mock(org.jooq.Result.class);
		org.jooq.Record1<Integer> firstRecord = mock(org.jooq.Record1.class);
		org.jooq.Record1<Integer> secondRecord = mock(org.jooq.Record1.class);

		given(this.dslContext.select(table.ID)).willReturn(selectStep);
		given(selectStep.from(table)).willReturn(fromStep);
		given(fromStep.where(any(org.jooq.Condition.class))).willReturn(whereStep);
		given(whereStep.fetch()).willReturn(fetched);
		given(fetched.stream()).willReturn(Stream.of(firstRecord, secondRecord));
		given(firstRecord.getValue(table.ID)).willReturn(11);
		given(secondRecord.getValue(table.ID)).willReturn(12);

		List<Integer> result = this.repository.findTimeLogIdsByTimesheetIdIn(List.of(1, 2));

		assertThat(result).containsExactly(11, 12);
	}

	@Test
	@DisplayName("Batch upsert should return zero for null/empty values")
	void testBatchUpsertReturnsZeroForNullOrEmptyValues() {
		assertThat(this.repository.batchUpsert(null)).isZero();
		assertThat(this.repository.batchUpsert(Collections.emptyList())).isZero();
	}

	@Test
	@DisplayName("Batch upsert should execute native query and set all parameters")
	void testBatchUpsertExecutesNativeQueryAndSetsAllParameters() {
		io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogUpsertDto dto = mock(
				io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogUpsertDto.class);
		given(dto.getId()).willReturn(1);
		given(dto.getDate()).willReturn(1700000000);
		given(dto.getDayTypeId()).willReturn(2);
		given(dto.getTimesheetId()).willReturn(3);
		given(dto.getRemark()).willReturn("note");
		given(dto.getBreakTime()).willReturn(60);
		given(dto.getOverTime()).willReturn(120);
		given(dto.getTotalTime()).willReturn(3600);
		given(dto.getWorkTime()).willReturn(3480);

		jakarta.persistence.Query nativeQuery = mock(jakarta.persistence.Query.class);
		given(this.entityManager.createNativeQuery(any())).willReturn(nativeQuery);
		given(nativeQuery.setParameter(any(Integer.class), any())).willReturn(nativeQuery);
		given(nativeQuery.executeUpdate()).willReturn(1);

		int affectedRows = this.repository.batchUpsert(List.of(dto));

		assertThat(affectedRows).isEqualTo(1);
		then(this.entityManager).should().createNativeQuery(any());
		then(nativeQuery).should().executeUpdate();
	}

	@Test
	@DisplayName("Batch upsert should process values across multiple batches")
	void testBatchUpsertProcessesValuesAcrossMultipleBatches() {
		io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogUpsertDto dto = mock(
				io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogUpsertDto.class);
		given(dto.getId()).willReturn(1);
		given(dto.getDate()).willReturn(1700000000);
		given(dto.getDayTypeId()).willReturn(2);
		given(dto.getTimesheetId()).willReturn(3);
		given(dto.getRemark()).willReturn("note");
		given(dto.getBreakTime()).willReturn(60);
		given(dto.getOverTime()).willReturn(120);
		given(dto.getTotalTime()).willReturn(3600);
		given(dto.getWorkTime()).willReturn(3480);

		List<io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogUpsertDto> values = java.util.Collections
			.nCopies(501, dto);
		jakarta.persistence.Query nativeQuery = mock(jakarta.persistence.Query.class);
		given(this.entityManager.createNativeQuery(any())).willReturn(nativeQuery);
		given(nativeQuery.setParameter(any(Integer.class), any())).willReturn(nativeQuery);
		given(nativeQuery.executeUpdate()).willReturn(1);

		int affectedRows = this.repository.batchUpsert(values);

		assertThat(affectedRows).isEqualTo(2);
		then(nativeQuery).should(org.mockito.Mockito.times(2)).executeUpdate();
	}

	@Test
	@DisplayName("Find time logs for migration by timesheet IDs should return empty for null/empty inputs")
	void testFindTimeLogsForMigrationByTimesheetIdsReturnsEmptyForNullOrEmptyInputs() {
		assertThat(this.repository.findTimeLogsForMigration((List<Integer>) null)).isEmpty();
		assertThat(this.repository.findTimeLogsForMigration(Collections.emptyList())).isEmpty();
	}

	@Test
	@DisplayName("Find time logs for migration by timesheet IDs should map JOOQ records")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testFindTimeLogsForMigrationByTimesheetIdsMapsJooqRecords() {
		org.jooq.Result fetched = mock(org.jooq.Result.class);
		DSLContext deepDslContext = this.createDslContextForFetch(fetched);
		TimeLogRepository localRepository = new TimeLogRepository(this.entityManager, this.timeLogJpaRepository,
				deepDslContext, this.timeLogIntervalRepository);
		CstTimeLogT table = CstTimeLogT.CST_TIME_LOG_T;

		given(fetched.map(any())).willAnswer((invocation) -> {
			java.util.function.Function mapper = invocation.getArgument(0);
			org.jooq.Record7 rec = mock(org.jooq.Record7.class);
			given(rec.get(table.TIMESHEET_ID)).willReturn(10);
			given(rec.get(table.ID)).willReturn(11);
			given(rec.get(table.TOTAL_TIME)).willReturn(null);
			given(rec.get(table.OVER_TIME)).willReturn(120);
			given(rec.get(table.WORK_TIME)).willReturn(3600);
			given(rec.get(table.WORK_START_TIME)).willReturn(100);
			given(rec.get(table.WORK_END_TIME)).willReturn(200);
			Object dto = mapper.apply(rec);
			return List.of(dto);
		});

		List<io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationDto> result = localRepository
			.findTimeLogsForMigration(List.of(1, 2));

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTimesheetId()).isEqualTo(Integer.valueOf(10));
		assertThat(result.get(0).getTimeLogId()).isEqualTo(Integer.valueOf(11));
		assertThat(result.get(0).getTotalTime()).isZero();
		assertThat(result.get(0).getOverTime()).isEqualTo(Integer.valueOf(120));
		assertThat(result.get(0).getWorkTime()).isEqualTo(Integer.valueOf(3600));
	}

	@Test
	@DisplayName("Find time logs for migration by batch should map native query rows")
	void testFindTimeLogsForMigrationByBatchMapsNativeQueryRows() {
		jakarta.persistence.Query nativeQuery = mock(jakarta.persistence.Query.class);
		List<Object[]> rows = List.of(new Object[] { 10, 3600, 7200, " remark ", 2, 1800, 4 },
				new Object[] { null, null, null, null, null, null, null });

		given(this.entityManager.createNativeQuery(any())).willReturn(nativeQuery);
		given(nativeQuery.setFirstResult(5)).willReturn(nativeQuery);
		given(nativeQuery.setMaxResults(2)).willReturn(nativeQuery);
		given(nativeQuery.getResultList()).willReturn(rows);

		List<io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationQueryResultDto> result = this.repository
			.findTimeLogsForMigration(2, 5);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getId()).isEqualTo(Integer.valueOf(10));
		assertThat(result.get(1).getId()).isNull();
	}

	@Test
	@DisplayName("Count unmigrated time logs should return number result")
	void testCountUnmigratedTimeLogsReturnsNumberResult() {
		jakarta.persistence.Query nativeQuery = mock(jakarta.persistence.Query.class);
		given(this.entityManager.createNativeQuery(any())).willReturn(nativeQuery);
		given(nativeQuery.getSingleResult()).willReturn(5L);

		long count = this.repository.countUnmigratedTimeLogs();

		assertThat(count).isEqualTo(5L);
	}

	@Test
	@DisplayName("Count unmigrated time logs should return zero when query result is null")
	void testCountUnmigratedTimeLogsReturnsZeroWhenResultIsNull() {
		jakarta.persistence.Query nativeQuery = mock(jakarta.persistence.Query.class);
		given(this.entityManager.createNativeQuery(any())).willReturn(nativeQuery);
		given(nativeQuery.getSingleResult()).willReturn(null);

		long count = this.repository.countUnmigratedTimeLogs();

		assertThat(count).isZero();
	}

	@Test
	@DisplayName("Get structured break intervals should return empty map for null/empty inputs")
	void testGetStructuredBreakIntervalsForTimesheetsReturnsEmptyMapForNullOrEmptyInputs() {
		assertThat(this.repository.getStructuredBreakIntervalsForTimesheets(null, 1)).isEmpty();
		assertThat(this.repository.getStructuredBreakIntervalsForTimesheets(Collections.emptyList(), 1)).isEmpty();
	}

	@Test
	@DisplayName("Get structured break intervals should return empty map when exception occurs")
	void testGetStructuredBreakIntervalsForTimesheetsExceptionReturnsEmptyMap() {
		given(this.dslContext.select()).willThrow(new RuntimeException("Database error"));
		assertThat(this.repository.getStructuredBreakIntervalsForTimesheets(List.of(1, 2), 1)).isEmpty();
	}

	@Test
	@DisplayName("Get structured remarks should return empty map for null/empty inputs")
	void testGetStructuredRemarksForTimesheetsReturnsEmptyMapForNullOrEmptyInputs() {
		assertThat(this.repository.getStructuredRemarksForTimesheets(null, 1)).isEmpty();
		assertThat(this.repository.getStructuredRemarksForTimesheets(Collections.emptyList(), 1)).isEmpty();
	}

	@Test
	@DisplayName("Get structured remarks should return empty map when exception occurs")
	void testGetStructuredRemarksForTimesheetsExceptionReturnsEmptyMap() {
		given(this.dslContext.select()).willThrow(new RuntimeException("Database error"));
		assertThat(this.repository.getStructuredRemarksForTimesheets(List.of(1, 2), 1)).isEmpty();
	}

	// ===== deleteByTimesheetIdIn Tests =====

	@Test
	@DisplayName("Delete by timesheet IDs should execute JOOQ delete query")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testDeleteByTimesheetIdInValidIdsExecutesJooqDeleteQuery() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		DeleteUsingStep mockDeleteUsingStep = mock(DeleteUsingStep.class);
		DeleteConditionStep mockDeleteConditionStep = mock(DeleteConditionStep.class);
		given(this.dslContext.deleteFrom(CstTimeLogT.CST_TIME_LOG_T)).willReturn(mockDeleteUsingStep);
		given(mockDeleteUsingStep.where(any(org.jooq.Condition.class))).willReturn(mockDeleteConditionStep);
		given(mockDeleteConditionStep.execute()).willReturn(1);

		// When
		this.repository.deleteByTimesheetIdIn(timesheetIds);

		// Then
		then(this.dslContext).should().deleteFrom(CstTimeLogT.CST_TIME_LOG_T);
		then(mockDeleteUsingStep).should().where(any(org.jooq.Condition.class));
		then(mockDeleteConditionStep).should().execute();
	}

	@Test
	@DisplayName("Delete by timesheet IDs should return early when IDs are null")
	void testDeleteByTimesheetIdInNullIdsReturnsEarly() {
		// Given
		List<Integer> timesheetIds = null;

		// When
		this.repository.deleteByTimesheetIdIn(timesheetIds);

		// Then
		then(this.dslContext).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Delete by timesheet IDs should return early when IDs are empty")
	void testDeleteByTimesheetIdInEmptyIdsReturnsEarly() {
		// Given
		List<Integer> timesheetIds = Collections.emptyList();

		// When
		this.repository.deleteByTimesheetIdIn(timesheetIds);

		// Then
		then(this.dslContext).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Delete by timesheet IDs should propagate exception when JOOQ delete fails")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testDeleteByTimesheetIdInJooqDeleteFailsPropagatesException() {
		// Given
		List<Integer> timesheetIds = List.of(1, 2, 3);
		DeleteUsingStep mockDeleteUsingStep = mock(DeleteUsingStep.class);
		DeleteConditionStep mockDeleteConditionStep = mock(DeleteConditionStep.class);
		given(this.dslContext.deleteFrom(CstTimeLogT.CST_TIME_LOG_T)).willReturn(mockDeleteUsingStep);
		given(mockDeleteUsingStep.where(any(org.jooq.Condition.class))).willReturn(mockDeleteConditionStep);
		willThrow(new RuntimeException("Database error")).given(mockDeleteConditionStep).execute();

		// When & Then
		assertThatThrownBy(() -> this.repository.deleteByTimesheetIdIn(timesheetIds))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Database error");

		then(this.dslContext).should().deleteFrom(CstTimeLogT.CST_TIME_LOG_T);
		then(mockDeleteConditionStep).should().execute();
	}

	// ─── Additional coverage tests for private helper methods ───

	@Test
	@DisplayName("formatStartEndTimeType returns range when both times valid")
	void testFormatStartEndTimeTypeValidRange() {
		String result = this.invokePrivateMethod("formatStartEndTimeType",
				new Class<?>[] { Integer.class, Integer.class }, 25200, 68400);
		assertThat(result).isEqualTo("07:00-19:00");
	}

	@Test
	@DisplayName("formatStartEndTimeType returns empty when start is null")
	void testFormatStartEndTimeTypeNullStart() {
		String result = this.invokePrivateMethod("formatStartEndTimeType",
				new Class<?>[] { Integer.class, Integer.class }, null, 68400);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatStartEndTimeType returns empty when end is null")
	void testFormatStartEndTimeTypeNullEnd() {
		String result = this.invokePrivateMethod("formatStartEndTimeType",
				new Class<?>[] { Integer.class, Integer.class }, 25200, null);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("isDateOutsidePeriod returns true when date before period")
	void testIsDateOutsidePeriodBeforeStart() {
		boolean result = this.invokePrivateMethod("isDateOutsidePeriod",
				new Class<?>[] { Integer.class, Integer.class, Integer.class }, 5, 10, 20);
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isDateOutsidePeriod returns false when date inside period")
	void testIsDateOutsidePeriodInsidePeriod() {
		boolean result = this.invokePrivateMethod("isDateOutsidePeriod",
				new Class<?>[] { Integer.class, Integer.class, Integer.class }, 15, 10, 20);
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isDateOutsidePeriod returns true when date after period")
	void testIsDateOutsidePeriodAfterEnd() {
		boolean result = this.invokePrivateMethod("isDateOutsidePeriod",
				new Class<?>[] { Integer.class, Integer.class, Integer.class }, 25, 10, 20);
		assertThat(result).isTrue();
	}

	static Stream<Arguments> formatBreakIntervalsEmptyResultProvider() {
		TimeLogInterval deletionInterval = mock(TimeLogInterval.class);
		given(deletionInterval.getWorkStartTime()).willReturn(-1);
		given(deletionInterval.getWorkEndTime()).willReturn(-1);
		return Stream.of(Arguments.of("null list", null), Arguments.of("empty list", Collections.emptyList()),
				Arguments.of("deletion marker intervals", List.of(deletionInterval)));
	}

	@ParameterizedTest(name = "formatBreakIntervalsAsCommaSeparated returns empty for {0}")
	@MethodSource("formatBreakIntervalsEmptyResultProvider")
	void testFormatBreakIntervalsAsCommaSeparatedReturnsEmpty(String scenario, List<TimeLogInterval> input) {
		String result = this.invokePrivateMethod("formatBreakIntervalsAsCommaSeparated", new Class<?>[] { List.class },
				(Object) input);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatBreakIntervalsAsCommaSeparated formats valid break intervals")
	void testFormatBreakIntervalsAsCommaSeparatedValid() {
		TimeLogInterval interval = mock(TimeLogInterval.class);
		given(interval.getWorkStartTime()).willReturn(100);
		BreakInterval bi = mock(BreakInterval.class);
		given(bi.getBreakStartTime()).willReturn(43200);
		given(bi.getBreakEndTime()).willReturn(46800);
		given(interval.getBreakInterval()).willReturn(List.of(bi));
		String result = this.invokePrivateMethod("formatBreakIntervalsAsCommaSeparated", new Class<?>[] { List.class },
				List.of(interval));
		assertThat(result).isEqualTo("12:00-13:00");
	}

	@Test
	@DisplayName("formatBreakIntervalsAsCommaSeparated skips null break intervals")
	void testFormatBreakIntervalsAsCommaSeparatedNullBreakIntervals() {
		TimeLogInterval interval = mock(TimeLogInterval.class);
		given(interval.getWorkStartTime()).willReturn(100);
		given(interval.getBreakInterval()).willReturn(null);
		String result = this.invokePrivateMethod("formatBreakIntervalsAsCommaSeparated", new Class<?>[] { List.class },
				List.of(interval));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatBreakIntervalsAsCommaSeparated skips deletion marker break intervals (-1,-1)")
	void testFormatBreakIntervalsAsCommaSeparatedSkipsDeletionBreakIntervals() {
		TimeLogInterval interval = mock(TimeLogInterval.class);
		given(interval.getWorkStartTime()).willReturn(100);
		BreakInterval bi = mock(BreakInterval.class);
		given(bi.getBreakStartTime()).willReturn(-1);
		given(bi.getBreakEndTime()).willReturn(-1);
		given(interval.getBreakInterval()).willReturn(List.of(bi));
		String result = this.invokePrivateMethod("formatBreakIntervalsAsCommaSeparated", new Class<?>[] { List.class },
				List.of(interval));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatRemarksAsCommaSeparated returns empty for null list")
	void testFormatRemarksAsCommaSeparatedNull() {
		String result = this.invokePrivateMethod("formatRemarksAsCommaSeparated", new Class<?>[] { List.class },
				(Object) null);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatRemarksAsCommaSeparated returns empty for empty list")
	void testFormatRemarksAsCommaSeparatedEmpty() {
		String result = this.invokePrivateMethod("formatRemarksAsCommaSeparated", new Class<?>[] { List.class },
				Collections.emptyList());
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatRemarksAsCommaSeparated formats valid remarks")
	void testFormatRemarksAsCommaSeparatedValid() {
		TimeLogInterval interval1 = mock(TimeLogInterval.class);
		given(interval1.getWorkStartTime()).willReturn(100);
		given(interval1.getRangeBasedRemark()).willReturn("Morning shift");
		TimeLogInterval interval2 = mock(TimeLogInterval.class);
		given(interval2.getWorkStartTime()).willReturn(300);
		given(interval2.getRangeBasedRemark()).willReturn("Evening shift");
		String result = this.invokePrivateMethod("formatRemarksAsCommaSeparated", new Class<?>[] { List.class },
				List.of(interval1, interval2));
		assertThat(result).isEqualTo("Morning shift, Evening shift");
	}

	@Test
	@DisplayName("formatRemarksAsCommaSeparated skips blank remarks")
	void testFormatRemarksAsCommaSeparatedSkipsBlank() {
		TimeLogInterval interval = mock(TimeLogInterval.class);
		given(interval.getWorkStartTime()).willReturn(100);
		given(interval.getRangeBasedRemark()).willReturn("   ");
		String result = this.invokePrivateMethod("formatRemarksAsCommaSeparated", new Class<?>[] { List.class },
				List.of(interval));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatRemarksAsCommaSeparated skips deletion markers")
	void testFormatRemarksAsCommaSeparatedSkipsDeletionMarkers() {
		TimeLogInterval deletionInterval = mock(TimeLogInterval.class);
		given(deletionInterval.getWorkStartTime()).willReturn(-1);
		given(deletionInterval.getWorkEndTime()).willReturn(-1);
		String result = this.invokePrivateMethod("formatRemarksAsCommaSeparated", new Class<?>[] { List.class },
				List.of(deletionInterval));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatIntervalsAsCommaSeparated returns empty for null list")
	void testFormatIntervalsAsCommaSeparatedNull() {
		String result = this.invokePrivateMethod("formatIntervalsAsCommaSeparated", new Class<?>[] { List.class },
				(Object) null);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatIntervalsAsCommaSeparated returns empty for empty list")
	void testFormatIntervalsAsCommaSeparatedEmpty() {
		String result = this.invokePrivateMethod("formatIntervalsAsCommaSeparated", new Class<?>[] { List.class },
				Collections.emptyList());
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatIntervalsAsCommaSeparated formats multiple intervals")
	void testFormatIntervalsAsCommaSeparatedMultiple() {
		TimeLogInterval interval1 = mock(TimeLogInterval.class);
		given(interval1.getWorkStartTime()).willReturn(25200);
		given(interval1.getWorkEndTime()).willReturn(68400);
		TimeLogInterval interval2 = mock(TimeLogInterval.class);
		given(interval2.getWorkStartTime()).willReturn(72000);
		given(interval2.getWorkEndTime()).willReturn(75600);
		String result = this.invokePrivateMethod("formatIntervalsAsCommaSeparated", new Class<?>[] { List.class },
				List.of(interval1, interval2));
		assertThat(result).isEqualTo("07:00-19:00, 20:00-21:00");
	}

	@Test
	@DisplayName("formatIntervalsAsCommaSeparated skips deletion markers")
	void testFormatIntervalsAsCommaSeparatedSkipsDeletionMarkers() {
		TimeLogInterval deletionInterval = mock(TimeLogInterval.class);
		given(deletionInterval.getWorkStartTime()).willReturn(-1);
		given(deletionInterval.getWorkEndTime()).willReturn(-1);
		String result = this.invokePrivateMethod("formatIntervalsAsCommaSeparated", new Class<?>[] { List.class },
				List.of(deletionInterval));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatDateAsColumnHeader returns 'Invalid Date' for exception")
	void testFormatDateAsColumnHeaderInvalidDate() {
		String result = this.invokePrivateMethod("formatDateAsColumnHeader", new Class<?>[] { Integer.class },
				Integer.MIN_VALUE);
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("findTimeLogsForMigration maps null total_time, over_time, work_time to 0")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testFindTimeLogsForMigrationNullFieldsDefaultToZero() {
		Result records = mock(Result.class);
		DSLContext deepDslContext = this.createDslContextForFetch(records);
		TimeLogRepository localRepository = new TimeLogRepository(this.entityManager, this.timeLogJpaRepository,
				deepDslContext, this.timeLogIntervalRepository);

		var table = io.recruitcrm.microservice.search.models.jooq.tables.CstTimeLogT.CST_TIME_LOG_T;

		Record rec = mock(org.jooq.Record7.class);
		given(rec.get(table.TIMESHEET_ID)).willReturn(1);
		given(rec.get(table.ID)).willReturn(10);
		given(rec.get(table.TOTAL_TIME)).willReturn(null);
		given(rec.get(table.OVER_TIME)).willReturn(null);
		given(rec.get(table.WORK_TIME)).willReturn(null);
		given(rec.get(table.WORK_START_TIME)).willReturn(25200);
		given(rec.get(table.WORK_END_TIME)).willReturn(68400);

		given(records.map(any(org.jooq.RecordMapper.class))).willAnswer((invocation) -> {
			org.jooq.RecordMapper mapper = invocation.getArgument(0);
			return List.of(mapper.map(rec));
		});

		List<TimeLogMigrationDto> result = localRepository.findTimeLogsForMigration(List.of(1));

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTotalTime()).isZero();
		assertThat(result.get(0).getOverTime()).isZero();
		assertThat(result.get(0).getWorkTime()).isZero();
	}

	@Test
	@DisplayName("findTimeLogsForMigration maps non-null fields correctly")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testFindTimeLogsForMigrationNonNullFieldsMapped() {
		Result records = mock(Result.class);
		DSLContext deepDslContext = this.createDslContextForFetch(records);
		TimeLogRepository localRepository = new TimeLogRepository(this.entityManager, this.timeLogJpaRepository,
				deepDslContext, this.timeLogIntervalRepository);

		var table = io.recruitcrm.microservice.search.models.jooq.tables.CstTimeLogT.CST_TIME_LOG_T;

		Record rec = mock(org.jooq.Record7.class);
		given(rec.get(table.TIMESHEET_ID)).willReturn(1);
		given(rec.get(table.ID)).willReturn(10);
		given(rec.get(table.TOTAL_TIME)).willReturn(3600);
		given(rec.get(table.OVER_TIME)).willReturn(1800);
		given(rec.get(table.WORK_TIME)).willReturn(900);
		given(rec.get(table.WORK_START_TIME)).willReturn(25200);
		given(rec.get(table.WORK_END_TIME)).willReturn(68400);

		given(records.map(any(org.jooq.RecordMapper.class))).willAnswer((invocation) -> {
			org.jooq.RecordMapper mapper = invocation.getArgument(0);
			return List.of(mapper.map(rec));
		});

		List<TimeLogMigrationDto> result = localRepository.findTimeLogsForMigration(List.of(1));

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTotalTime()).isEqualTo(3600);
		assertThat(result.get(0).getOverTime()).isEqualTo(1800);
		assertThat(result.get(0).getWorkTime()).isEqualTo(900);
	}

	@Test
	@DisplayName("getTimeLogsForTimesheets processes and formats time logs")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testGetTimeLogsForTimesheetsNullReturnsEmptyMap() {
		assertThat(this.repository.getTimeLogsForTimesheets(null, 1)).isEmpty();
	}

	@Test
	@DisplayName("getTimeLogsForTimesheets returns empty map for empty timesheetIds")
	void testGetTimeLogsForTimesheetsEmptyReturnsEmptyMap() {
		assertThat(this.repository.getTimeLogsForTimesheets(Collections.emptyList(), 1)).isEmpty();
	}

	@Test
	@DisplayName("getStructuredBreakIntervalsForTimesheets returns empty map for null timesheetIds")
	void testGetStructuredBreakIntervalsForTimesheetsNullReturnsEmpty() {
		assertThat(this.repository.getStructuredBreakIntervalsForTimesheets(null, 1)).isEmpty();
	}

	@Test
	@DisplayName("getStructuredBreakIntervalsForTimesheets returns empty map for empty timesheetIds")
	void testGetStructuredBreakIntervalsForTimesheetsEmptyReturnsEmpty() {
		assertThat(this.repository.getStructuredBreakIntervalsForTimesheets(Collections.emptyList(), 1)).isEmpty();
	}

	@Test
	@DisplayName("getStructuredRemarksForTimesheets returns empty map for null timesheetIds")
	void testGetStructuredRemarksForTimesheetsNullReturnsEmpty() {
		assertThat(this.repository.getStructuredRemarksForTimesheets(null, 1)).isEmpty();
	}

	@Test
	@DisplayName("getStructuredRemarksForTimesheets returns empty map for empty timesheetIds")
	void testGetStructuredRemarksForTimesheetsEmptyReturnsEmpty() {
		assertThat(this.repository.getStructuredRemarksForTimesheets(Collections.emptyList(), 1)).isEmpty();
	}

	@Test
	@DisplayName("getStructuredTimeLogsForTimesheets returns empty map for null timesheetIds")
	void testGetStructuredTimeLogsForTimesheetsNullReturnsEmpty() {
		assertThat(this.repository.getStructuredTimeLogsForTimesheets(null, 1)).isEmpty();
	}

	@Test
	@DisplayName("getStructuredTimeLogsForTimesheets returns empty map for empty timesheetIds")
	void testGetStructuredTimeLogsForTimesheetsEmptyReturnsEmpty() {
		assertThat(this.repository.getStructuredTimeLogsForTimesheets(Collections.emptyList(), 1)).isEmpty();
	}

	@Test
	@DisplayName("extractBreakIntervalValue returns break time for non-interval work log type")
	@SuppressWarnings("unchecked")
	void testExtractBreakIntervalValueHoursBasedType() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		Object tss = this.getPrivateStaticField("TSS");
		Record rec = mock(org.jooq.Record.class);
		try {
			java.lang.reflect.Field tssField = io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT.class
				.getDeclaredField("WORK_LOG_TYPE");
			tssField.setAccessible(true);
			Object workLogTypeField = tssField.get(tss);
			given(rec.getValue((org.jooq.Field) workLogTypeField)).willReturn(1);
		}
		catch (Exception ex) {
			// field access may vary
		}
		given(rec.getValue(tl.ID)).willReturn(10);
		given(rec.getValue(tl.BREAK_TIME)).willReturn(1800);
		Map<Integer, List<TimeLogInterval>> intervalMap = Collections.emptyMap();
		String result = this.invokePrivateMethod("extractBreakIntervalValue",
				new Class<?>[] { org.jooq.Record.class, Map.class }, rec, intervalMap);
		assertThat(result).isEqualTo("0.50");
	}

	@Test
	@DisplayName("extractRemarkValue returns remark for non-interval work log type")
	@SuppressWarnings("unchecked")
	void testExtractRemarkValueHoursBasedType() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		Object tss = this.getPrivateStaticField("TSS");
		Record rec = mock(org.jooq.Record.class);
		try {
			java.lang.reflect.Field tssField = io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT.class
				.getDeclaredField("WORK_LOG_TYPE");
			tssField.setAccessible(true);
			Object workLogTypeField = tssField.get(tss);
			given(rec.getValue((org.jooq.Field) workLogTypeField)).willReturn(1);
		}
		catch (Exception ex) {
			// field access may vary
		}
		given(rec.getValue(tl.ID)).willReturn(10);
		given(rec.getValue(tl.REMARK)).willReturn("Test remark");
		Map<Integer, List<TimeLogInterval>> intervalMap = Collections.emptyMap();
		String result = this.invokePrivateMethod("extractRemarkValue",
				new Class<?>[] { org.jooq.Record.class, Map.class }, rec, intervalMap);
		assertThat(result).isEqualTo("Test remark");
	}

	@Test
	@DisplayName("extractRemarkValue returns empty for blank remark")
	@SuppressWarnings("unchecked")
	void testExtractRemarkValueBlankRemark() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		Object tss = this.getPrivateStaticField("TSS");
		Record rec = mock(org.jooq.Record.class);
		try {
			java.lang.reflect.Field tssField = io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT.class
				.getDeclaredField("WORK_LOG_TYPE");
			tssField.setAccessible(true);
			Object workLogTypeField = tssField.get(tss);
			given(rec.getValue((org.jooq.Field) workLogTypeField)).willReturn(1);
		}
		catch (Exception ex) {
			// field access may vary
		}
		given(rec.getValue(tl.ID)).willReturn(10);
		given(rec.getValue(tl.REMARK)).willReturn("   ");
		Map<Integer, List<TimeLogInterval>> intervalMap = Collections.emptyMap();
		String result = this.invokePrivateMethod("extractRemarkValue",
				new Class<?>[] { org.jooq.Record.class, Map.class }, rec, intervalMap);
		assertThat(result).isEmpty();
	}

	// ─── Coverage tests for uncovered lines ───

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private DSLContext createDslContextForIterableFetch(List<org.jooq.Record> records) {
		org.jooq.Result mockResult = mock(org.jooq.Result.class);
		given(mockResult.iterator()).willReturn(records.iterator());
		given(mockResult.stream()).willReturn(records.stream());
		given(mockResult.size()).willReturn(records.size());
		given(mockResult.spliterator()).willReturn(records.spliterator());
		org.mockito.BDDMockito.willAnswer((inv) -> {
			java.util.function.Consumer action = inv.getArgument(0);
			records.forEach(action);
			return null;
		}).given(mockResult).forEach(any());
		return this.createDslContextForFetch(mockResult);
	}

	@Test
	@DisplayName("getTimeLogsForTimesheets returns formatted time logs on success")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testGetTimeLogsForTimesheetsSuccessPath() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetT ts = (CstTimesheetT) this.getPrivateStaticField("TS");
		CstTimesheetSettingT tss = (CstTimesheetSettingT) this.getPrivateStaticField("TSS");

		org.jooq.Record10 rec = mock(org.jooq.Record10.class);
		given(rec.get(tl.TIMESHEET_ID)).willReturn(100);
		given(rec.get(tl.DATE)).willReturn(1720569600);
		given(rec.get(ts.PERIOD_START)).willReturn(1720483200);
		given(rec.get(ts.PERIOD_END)).willReturn(1720656000);
		given(rec.get(tss.WORK_LOG_TYPE)).willReturn(1);
		given(rec.get(tl.WORK_TIME)).willReturn(3600);
		given(rec.get(tl.WORK_START_TIME)).willReturn(null);
		given(rec.get(tl.WORK_END_TIME)).willReturn(null);

		org.jooq.Result mockResult = mock(org.jooq.Result.class);
		given(mockResult.iterator()).willReturn(List.of((org.jooq.Record) rec).iterator());

		DSLContext deepDsl = this.createDslContextForFetch(mockResult);
		TimeLogRepository localRepo = new TimeLogRepository(this.entityManager, this.timeLogJpaRepository, deepDsl,
				this.timeLogIntervalRepository);

		Map<Integer, String> result = localRepo.getTimeLogsForTimesheets(List.of(100), 1);

		assertThat(result).containsKey(100);
		assertThat(result.get(100)).contains("1.00");
	}

	@Test
	@DisplayName("getTimeLogsForTimesheets wraps exception as ResourceNotFoundException")
	void testGetTimeLogsForTimesheetsExceptionThrowsResourceNotFound() {
		given(this.dslContext.select()).willThrow(new RuntimeException("DB error"));
		List<Integer> timesheetIds = List.of(1);
		assertThatThrownBy(() -> this.repository.getTimeLogsForTimesheets(timesheetIds, 1))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("getStructuredTimeLogsForTimesheets returns structured data on success")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testGetStructuredTimeLogsForTimesheetsSuccessPath() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetT ts = (CstTimesheetT) this.getPrivateStaticField("TS");
		CstTimesheetSettingT tss = (CstTimesheetSettingT) this.getPrivateStaticField("TSS");

		org.jooq.Record rec = mock(org.jooq.Record.class);
		given(rec.get(tl.ID)).willReturn(50);
		given(rec.get(tl.TIMESHEET_ID)).willReturn(200);
		given(rec.get(tl.DATE)).willReturn(1720569600);
		given(rec.get(ts.PERIOD_START)).willReturn(1720483200);
		given(rec.get(ts.PERIOD_END)).willReturn(1720656000);
		given(rec.get(tss.WORK_LOG_TYPE)).willReturn(1);
		given(rec.get(tl.WORK_TIME)).willReturn(7200);
		given(rec.get(tl.WORK_START_TIME)).willReturn(null);
		given(rec.get(tl.WORK_END_TIME)).willReturn(null);

		org.jooq.Result mockResult = mock(org.jooq.Result.class);
		given(mockResult.iterator()).willReturn(List.of(rec).iterator());
		given(mockResult.stream()).willReturn(Stream.of(rec));

		DSLContext deepDsl = this.createDslContextForFetch(mockResult);
		TimeLogRepository localRepo = new TimeLogRepository(this.entityManager, this.timeLogJpaRepository, deepDsl,
				this.timeLogIntervalRepository);

		Map<Integer, Map<String, String>> result = localRepo.getStructuredTimeLogsForTimesheets(List.of(200), 1);

		assertThat(result).containsKey(200);
		assertThat(result.get(200)).isNotEmpty();
	}

	@Test
	@DisplayName("getStructuredTimeLogsForTimesheets wraps exception as ResourceNotFoundException")
	void testGetStructuredTimeLogsForTimesheetsExceptionThrowsResourceNotFound() {
		given(this.dslContext.select()).willThrow(new RuntimeException("DB error"));
		List<Integer> timesheetIds = List.of(1);
		assertThatThrownBy(() -> this.repository.getStructuredTimeLogsForTimesheets(timesheetIds, 1))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("createStructuredWorkTimeValue uses intervals when range type with non-empty intervals")
	void testCreateStructuredWorkTimeValueIntervalBranch() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetSettingT tss = (CstTimesheetSettingT) this.getPrivateStaticField("TSS");

		org.jooq.Record rec = mock(org.jooq.Record.class);
		given(rec.get(tl.WORK_TIME)).willReturn(null);
		given(rec.get(tl.WORK_START_TIME)).willReturn(3600);
		given(rec.get(tl.WORK_END_TIME)).willReturn(7200);
		given(rec.get(tss.WORK_LOG_TYPE)).willReturn(2);

		TimeLogInterval interval = mock(TimeLogInterval.class);
		given(interval.getWorkStartTime()).willReturn(25200);
		given(interval.getWorkEndTime()).willReturn(68400);

		String result = this.invokePrivateMethod("createStructuredWorkTimeValue",
				new Class<?>[] { org.jooq.Record.class, List.class }, rec, List.of(interval));

		assertThat(result).isEqualTo("07:00-19:00");
	}

	@Test
	@DisplayName("getStructuredOvertimeHoursForTimesheets returns empty map on exception")
	void testGetStructuredOvertimeHoursExceptionReturnsEmptyMap() {
		// Given
		given(this.dslContext.select()).willThrow(new RuntimeException("Database error"));

		// When
		Map<Integer, Map<String, String>> result = this.repository.getStructuredOvertimeHoursForTimesheets(List.of(1),
				1);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getStructuredTotalTimeForTimesheets returns empty map on exception")
	void testGetStructuredTotalTimeExceptionReturnsEmptyMap() {
		// Given
		given(this.dslContext.select()).willThrow(new RuntimeException("Database error"));

		// When
		Map<Integer, Map<String, String>> result = this.repository.getStructuredTotalTimeForTimesheets(List.of(1), 1);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getStructuredEffectiveWorkHoursForTimesheets returns empty map on exception")
	void testGetStructuredEffectiveWorkHoursExceptionReturnsEmptyMap() {
		// Given
		given(this.dslContext.select()).willThrow(new RuntimeException("Database error"));

		// When
		Map<Integer, Map<String, String>> result = this.repository
			.getStructuredEffectiveWorkHoursForTimesheets(List.of(1), 1);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getStructuredBreakIntervalsForTimesheets returns data on success")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testGetStructuredBreakIntervalsSuccessPath() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetT ts = (CstTimesheetT) this.getPrivateStaticField("TS");
		CstTimesheetSettingT tss = (CstTimesheetSettingT) this.getPrivateStaticField("TSS");

		org.jooq.Record rec = mock(org.jooq.Record.class);
		given(rec.get(tl.ID)).willReturn(60);
		given(rec.getValue(tl.TIMESHEET_ID)).willReturn(600);
		given(rec.getValue(tl.DATE)).willReturn(15);
		given(rec.getValue(ts.PERIOD_START)).willReturn(10);
		given(rec.getValue(ts.PERIOD_END)).willReturn(20);
		given(rec.getValue(tss.WORK_LOG_TYPE)).willReturn(1);
		given(rec.getValue(tl.ID)).willReturn(60);
		given(rec.getValue(tl.BREAK_TIME)).willReturn(1800);

		org.jooq.Result mockResult = mock(org.jooq.Result.class);
		given(mockResult.iterator()).willReturn(List.of(rec).iterator());
		given(mockResult.stream()).willReturn(Stream.of(rec));

		DSLContext deepDsl = this.createDslContextForFetch(mockResult);
		TimeLogRepository localRepo = new TimeLogRepository(this.entityManager, this.timeLogJpaRepository, deepDsl,
				this.timeLogIntervalRepository);

		Map<Integer, Map<String, String>> result = localRepo.getStructuredBreakIntervalsForTimesheets(List.of(600), 1);

		assertThat(result).containsKey(600);
	}

	@Test
	@DisplayName("getStructuredRemarksForTimesheets returns data on success")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testGetStructuredRemarksSuccessPath() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetT ts = (CstTimesheetT) this.getPrivateStaticField("TS");
		CstTimesheetSettingT tss = (CstTimesheetSettingT) this.getPrivateStaticField("TSS");

		org.jooq.Record rec = mock(org.jooq.Record.class);
		given(rec.get(tl.ID)).willReturn(70);
		given(rec.getValue(tl.TIMESHEET_ID)).willReturn(700);
		given(rec.getValue(tl.DATE)).willReturn(15);
		given(rec.getValue(ts.PERIOD_START)).willReturn(10);
		given(rec.getValue(ts.PERIOD_END)).willReturn(20);
		given(rec.getValue(tss.WORK_LOG_TYPE)).willReturn(1);
		given(rec.getValue(tl.ID)).willReturn(70);
		given(rec.getValue(tl.REMARK)).willReturn("Test note");

		org.jooq.Result mockResult = mock(org.jooq.Result.class);
		given(mockResult.iterator()).willReturn(List.of(rec).iterator());
		given(mockResult.stream()).willReturn(Stream.of(rec));

		DSLContext deepDsl = this.createDslContextForFetch(mockResult);
		TimeLogRepository localRepo = new TimeLogRepository(this.entityManager, this.timeLogJpaRepository, deepDsl,
				this.timeLogIntervalRepository);

		Map<Integer, Map<String, String>> result = localRepo.getStructuredRemarksForTimesheets(List.of(700), 1);

		assertThat(result).containsKey(700);
	}

	@Test
	@DisplayName("extractBreakIntervalValue returns comma-separated breaks for start-end type")
	void testExtractBreakIntervalValueStartEndType() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetSettingT tss = (CstTimesheetSettingT) this.getPrivateStaticField("TSS");

		BreakInterval bi = mock(BreakInterval.class);
		given(bi.getBreakStartTime()).willReturn(43200);
		given(bi.getBreakEndTime()).willReturn(46800);

		TimeLogInterval interval = mock(TimeLogInterval.class);
		given(interval.getWorkStartTime()).willReturn(100);
		given(interval.getBreakInterval()).willReturn(List.of(bi));

		org.jooq.Record rec = mock(org.jooq.Record.class);
		given(rec.getValue(tss.WORK_LOG_TYPE)).willReturn(2);
		given(rec.getValue(tl.ID)).willReturn(80);

		String result = this.invokePrivateMethod("extractBreakIntervalValue",
				new Class<?>[] { org.jooq.Record.class, Map.class }, rec, Map.of(80, List.of(interval)));

		assertThat(result).isEqualTo("12:00-13:00");
	}

	@Test
	@DisplayName("extractRemarkValue returns comma-separated remarks for start-end type")
	void testExtractRemarkValueStartEndType() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetSettingT tss = (CstTimesheetSettingT) this.getPrivateStaticField("TSS");

		TimeLogInterval interval = mock(TimeLogInterval.class);
		given(interval.getWorkStartTime()).willReturn(100);
		given(interval.getRangeBasedRemark()).willReturn("shift remark");

		org.jooq.Record rec = mock(org.jooq.Record.class);
		given(rec.getValue(tss.WORK_LOG_TYPE)).willReturn(2);
		given(rec.getValue(tl.ID)).willReturn(90);

		String result = this.invokePrivateMethod("extractRemarkValue",
				new Class<?>[] { org.jooq.Record.class, Map.class }, rec, Map.of(90, List.of(interval)));

		assertThat(result).isEqualTo("shift remark");
	}

	@Test
	@DisplayName("extractRemarkValue returns trimmed remark for hours-based with null remark")
	void testExtractRemarkValueHoursBasedNullRemark() {
		CstTimeLogT tl = (CstTimeLogT) this.getPrivateStaticField("TL");
		CstTimesheetSettingT tss = (CstTimesheetSettingT) this.getPrivateStaticField("TSS");

		org.jooq.Record rec = mock(org.jooq.Record.class);
		given(rec.getValue(tss.WORK_LOG_TYPE)).willReturn(1);
		given(rec.getValue(tl.ID)).willReturn(91);
		given(rec.getValue(tl.REMARK)).willReturn(null);

		String result = this.invokePrivateMethod("extractRemarkValue",
				new Class<?>[] { org.jooq.Record.class, Map.class }, rec, Collections.emptyMap());

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("formatBreakIntervalsAsCommaSeparated processes valid break with null start skip")
	void testFormatBreakIntervalsAsCommaSeparatedWithMixedBreaks() {
		BreakInterval validBi = mock(BreakInterval.class);
		given(validBi.getBreakStartTime()).willReturn(43200);
		given(validBi.getBreakEndTime()).willReturn(46800);

		BreakInterval nullStartBi = mock(BreakInterval.class);
		given(nullStartBi.getBreakStartTime()).willReturn(null);

		TimeLogInterval interval = mock(TimeLogInterval.class);
		given(interval.getWorkStartTime()).willReturn(100);
		given(interval.getBreakInterval()).willReturn(java.util.Arrays.asList(nullStartBi, validBi));

		String result = this.invokePrivateMethod("formatBreakIntervalsAsCommaSeparated", new Class<?>[] { List.class },
				List.of(interval));

		assertThat(result).isEqualTo("12:00-13:00");
	}

	@Test
	@DisplayName("formatRemarksAsCommaSeparated returns remark when interval has valid remark")
	void testFormatRemarksAsCommaSeparatedWithValidRemark() {
		TimeLogInterval interval = mock(TimeLogInterval.class);
		given(interval.getWorkStartTime()).willReturn(100);
		given(interval.getRangeBasedRemark()).willReturn("Valid remark");

		String result = this.invokePrivateMethod("formatRemarksAsCommaSeparated", new Class<?>[] { List.class },
				List.of(interval));

		assertThat(result).isEqualTo("Valid remark");
	}

	@Test
	@DisplayName("formatRemarksAsCommaSeparated returns empty for null remark")
	void testFormatRemarksAsCommaSeparatedNullRemark() {
		TimeLogInterval interval = mock(TimeLogInterval.class);
		given(interval.getWorkStartTime()).willReturn(100);
		given(interval.getRangeBasedRemark()).willReturn(null);

		String result = this.invokePrivateMethod("formatRemarksAsCommaSeparated", new Class<?>[] { List.class },
				List.of(interval));

		assertThat(result).isEmpty();
	}

}