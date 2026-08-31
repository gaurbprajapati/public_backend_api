package io.recruitcrm.microservice.timesheet.rule_engine.utils;

import com.google.common.collect.RangeSet;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TimeHelper Multi-Interval Day Utility Tests")
class TimeHelperMultiIntervalTests {

	private TimeLog createTimeLog(LocalTime start, LocalTime end) {
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(start);
		timeLog.setWorkEndTime(end);
		return timeLog;
	}

	private TimeLog createTimeLogWithDate(LocalTime start, LocalTime end, LocalDate date) {
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(start);
		timeLog.setWorkEndTime(end);
		timeLog.setDate(date);
		return timeLog;
	}

	@Nested
	@DisplayName("isLastIntervalOfDay Tests")
	class IsLastIntervalOfDayTests {

		@Test
		@DisplayName("Returns true for null sameDayTimeLogs")
		void testIsLastIntervalNullList() {
			TimeLog timeLog = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));

			boolean result = TimeHelper.isLastIntervalOfDay(timeLog, null);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Returns true for single entry list")
		void testIsLastIntervalSingleEntry() {
			TimeLog timeLog = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));

			boolean result = TimeHelper.isLastIntervalOfDay(timeLog, List.of(timeLog));

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Returns true for empty list")
		void testIsLastIntervalEmptyList() {
			TimeLog timeLog = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));

			boolean result = TimeHelper.isLastIntervalOfDay(timeLog, new ArrayList<>());

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Returns true for last interval")
		void testIsLastIntervalForLastInterval() {
			TimeLog early = createTimeLog(LocalTime.of(7, 0), LocalTime.of(12, 0));
			TimeLog late = createTimeLog(LocalTime.of(13, 0), LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(early, late);

			boolean result = TimeHelper.isLastIntervalOfDay(late, sameDayLogs);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Returns false for non-last interval")
		void testIsLastIntervalForFirstInterval() {
			TimeLog early = createTimeLog(LocalTime.of(7, 0), LocalTime.of(12, 0));
			TimeLog late = createTimeLog(LocalTime.of(13, 0), LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(early, late);

			boolean result = TimeHelper.isLastIntervalOfDay(early, sameDayLogs);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Returns false when current end time is null")
		void testIsLastIntervalNullCurrentEndTime() {
			TimeLog timeLog = createTimeLog(LocalTime.of(9, 0), null);
			TimeLog other = createTimeLog(LocalTime.of(13, 0), LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(timeLog, other);

			boolean result = TimeHelper.isLastIntervalOfDay(timeLog, sameDayLogs);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Returns true when all other end times are null")
		void testIsLastIntervalOtherEndTimesNull() {
			TimeLog current = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
			TimeLog other = createTimeLog(LocalTime.of(7, 0), null);
			List<TimeLog> sameDayLogs = List.of(current, other);

			boolean result = TimeHelper.isLastIntervalOfDay(current, sameDayLogs);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Returns true when intervals have equal end times")
		void testIsLastIntervalEqualEndTimes() {
			TimeLog first = createTimeLog(LocalTime.of(7, 0), LocalTime.of(17, 0));
			TimeLog second = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
			List<TimeLog> sameDayLogs = List.of(first, second);

			boolean result = TimeHelper.isLastIntervalOfDay(first, sameDayLogs);

			assertThat(result).isTrue();
		}

	}

	@Nested
	@DisplayName("createMergedTimeLog Tests")
	class CreateMergedTimeLogTests {

		@Test
		@DisplayName("Returns fallback for null sameDayTimeLogs")
		void testCreateMergedNullList() {
			TimeLog fallback = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));

			TimeLog result = TimeHelper.createMergedTimeLog(fallback, null);

			assertThat(result).isSameAs(fallback);
		}

		@Test
		@DisplayName("Returns fallback for single entry list")
		void testCreateMergedSingleEntry() {
			TimeLog fallback = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));

			TimeLog result = TimeHelper.createMergedTimeLog(fallback, List.of(fallback));

			assertThat(result).isSameAs(fallback);
		}

		@Test
		@DisplayName("Merges two valid intervals into one spanning range")
		void testCreateMergedTwoValidIntervals() {
			TimeLog tl1 = createTimeLog(LocalTime.of(7, 0), LocalTime.of(12, 0));
			TimeLog tl2 = createTimeLog(LocalTime.of(13, 0), LocalTime.of(17, 0));

			TimeLog result = TimeHelper.createMergedTimeLog(tl1, List.of(tl1, tl2));

			assertThat(result.getWorkStartTime()).isEqualTo(LocalTime.of(7, 0));
			assertThat(result.getWorkEndTime()).isEqualTo(LocalTime.of(17, 0));
			assertThat(result.getNormalizedWorkStartTime()).isEqualTo(LocalTime.of(7, 0));
			assertThat(result.getNormalizedWorkEndTime()).isEqualTo(LocalTime.of(17, 0));
		}

		@Test
		@DisplayName("Returns fallback when all times are null")
		void testCreateMergedAllNullTimes() {
			TimeLog fallback = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
			TimeLog tl1 = createTimeLog(null, null);
			TimeLog tl2 = createTimeLog(null, null);

			TimeLog result = TimeHelper.createMergedTimeLog(fallback, List.of(tl1, tl2));

			assertThat(result).isSameAs(fallback);
		}

		@Test
		@DisplayName("Returns fallback when mergedStart equals mergedEnd")
		void testCreateMergedStartEqualsEnd() {
			TimeLog fallback = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
			TimeLog tl1 = createTimeLog(LocalTime.of(9, 0), LocalTime.of(9, 0));
			TimeLog tl2 = createTimeLog(LocalTime.of(9, 0), LocalTime.of(9, 0));

			TimeLog result = TimeHelper.createMergedTimeLog(fallback, List.of(tl1, tl2));

			assertThat(result).isSameAs(fallback);
		}

		@Test
		@DisplayName("Returns fallback when mergedEnd is null")
		void testCreateMergedEndNull() {
			TimeLog fallback = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
			TimeLog tl1 = createTimeLog(LocalTime.of(9, 0), null);
			TimeLog tl2 = createTimeLog(LocalTime.of(13, 0), null);

			TimeLog result = TimeHelper.createMergedTimeLog(fallback, List.of(tl1, tl2));

			assertThat(result).isSameAs(fallback);
		}

		@Test
		@DisplayName("Returns fallback when mergedStart is null")
		void testCreateMergedStartNull() {
			TimeLog fallback = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));
			TimeLog tl1 = createTimeLog(null, LocalTime.of(12, 0));
			TimeLog tl2 = createTimeLog(null, LocalTime.of(17, 0));

			TimeLog result = TimeHelper.createMergedTimeLog(fallback, List.of(tl1, tl2));

			assertThat(result).isSameAs(fallback);
		}

		@Test
		@DisplayName("Merges three intervals correctly")
		void testCreateMergedThreeIntervals() {
			TimeLog tl1 = createTimeLog(LocalTime.of(5, 0), LocalTime.of(7, 0));
			TimeLog tl2 = createTimeLog(LocalTime.of(9, 0), LocalTime.of(12, 0));
			TimeLog tl3 = createTimeLog(LocalTime.of(13, 0), LocalTime.of(21, 0));

			TimeLog result = TimeHelper.createMergedTimeLog(tl1, List.of(tl1, tl2, tl3));

			assertThat(result.getWorkStartTime()).isEqualTo(LocalTime.of(5, 0));
			assertThat(result.getWorkEndTime()).isEqualTo(LocalTime.of(21, 0));
		}

	}

	@Nested
	@DisplayName("getSameDayTimeLogs Tests")
	class GetSameDayTimeLogsTests {

		@Test
		@DisplayName("Returns empty list for null allTimeLogs")
		void testGetSameDayNullAllTimeLogs() {
			TimeLog timeLog = createTimeLogWithDate(LocalTime.of(9, 0), LocalTime.of(17, 0), LocalDate.of(2025, 1, 6));

			List<TimeLog> result = TimeHelper.getSameDayTimeLogs(timeLog, null);

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Returns empty list for null timeLog")
		void testGetSameDayNullTimeLog() {
			List<TimeLog> result = TimeHelper.getSameDayTimeLogs(null, new ArrayList<>());

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Returns empty list for timeLog with null date")
		void testGetSameDayNullTimeLogDate() {
			TimeLog timeLog = createTimeLogWithDate(LocalTime.of(9, 0), LocalTime.of(17, 0), null);

			List<TimeLog> result = TimeHelper.getSameDayTimeLogs(timeLog, new ArrayList<>());

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("Returns only same-day time logs")
		void testGetSameDayFiltersSameDay() {
			LocalDate targetDate = LocalDate.of(2025, 1, 6);
			TimeLog timeLog = createTimeLogWithDate(LocalTime.of(9, 0), LocalTime.of(17, 0), targetDate);
			TimeLog sameDay = createTimeLogWithDate(LocalTime.of(7, 0), LocalTime.of(9, 0), targetDate);
			TimeLog differentDay = createTimeLogWithDate(LocalTime.of(9, 0), LocalTime.of(17, 0),
					LocalDate.of(2025, 1, 7));

			List<TimeLog> result = TimeHelper.getSameDayTimeLogs(timeLog, List.of(timeLog, sameDay, differentDay));

			assertThat(result).hasSize(2);
		}

		@Test
		@DisplayName("Filters out time logs with null date")
		void testGetSameDayFiltersNullDates() {
			LocalDate targetDate = LocalDate.of(2025, 1, 6);
			TimeLog timeLog = createTimeLogWithDate(LocalTime.of(9, 0), LocalTime.of(17, 0), targetDate);
			TimeLog nullDateLog = createTimeLogWithDate(LocalTime.of(7, 0), LocalTime.of(9, 0), null);

			List<TimeLog> result = TimeHelper.getSameDayTimeLogs(timeLog, List.of(timeLog, nullDateLog));

			assertThat(result).hasSize(1);
		}

	}

	@Nested
	@DisplayName("getGapsBetweenIntervals Tests")
	class GetGapsBetweenIntervalsTests {

		@Test
		@DisplayName("Returns empty for null list")
		void testGetGapsNullList() {
			RangeSet<LocalTime> result = TimeHelper.getGapsBetweenIntervals(null);

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Returns empty for single interval")
		void testGetGapsSingleInterval() {
			TimeLog timeLog = createTimeLog(LocalTime.of(9, 0), LocalTime.of(17, 0));

			RangeSet<LocalTime> result = TimeHelper.getGapsBetweenIntervals(List.of(timeLog));

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Returns empty for empty list")
		void testGetGapsEmptyList() {
			RangeSet<LocalTime> result = TimeHelper.getGapsBetweenIntervals(new ArrayList<>());

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Returns gap between two non-adjacent intervals")
		void testGetGapsBetweenTwoIntervals() {
			TimeLog tl1 = createTimeLog(LocalTime.of(9, 0), LocalTime.of(12, 0));
			TimeLog tl2 = createTimeLog(LocalTime.of(13, 0), LocalTime.of(17, 0));

			RangeSet<LocalTime> result = TimeHelper.getGapsBetweenIntervals(List.of(tl1, tl2));

			assertThat(result.isEmpty()).isFalse();
			assertThat(result.asRanges()).hasSize(1);
			assertThat(result.encloses(TimeHelper.toRange(LocalTime.of(12, 0), LocalTime.of(13, 0)))).isTrue();
		}

		@Test
		@DisplayName("Returns empty for adjacent intervals with no gap")
		void testGetGapsAdjacentIntervalsNoGap() {
			TimeLog tl1 = createTimeLog(LocalTime.of(9, 0), LocalTime.of(12, 0));
			TimeLog tl2 = createTimeLog(LocalTime.of(12, 0), LocalTime.of(17, 0));

			RangeSet<LocalTime> result = TimeHelper.getGapsBetweenIntervals(List.of(tl1, tl2));

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Returns multiple gaps between three intervals")
		void testGetGapsMultipleGaps() {
			TimeLog tl1 = createTimeLog(LocalTime.of(5, 0), LocalTime.of(7, 0));
			TimeLog tl2 = createTimeLog(LocalTime.of(9, 0), LocalTime.of(12, 0));
			TimeLog tl3 = createTimeLog(LocalTime.of(14, 0), LocalTime.of(17, 0));

			RangeSet<LocalTime> result = TimeHelper.getGapsBetweenIntervals(List.of(tl1, tl2, tl3));

			assertThat(result.isEmpty()).isFalse();
			assertThat(result.asRanges()).hasSize(2);
		}

		@Test
		@DisplayName("Skips intervals with null times")
		void testGetGapsSkipsNullTimes() {
			TimeLog tl1 = createTimeLog(LocalTime.of(9, 0), LocalTime.of(12, 0));
			TimeLog tl2 = createTimeLog(null, null);

			RangeSet<LocalTime> result = TimeHelper.getGapsBetweenIntervals(List.of(tl1, tl2));

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Returns empty when all intervals have null times")
		void testGetGapsAllNullTimes() {
			TimeLog tl1 = createTimeLog(null, null);
			TimeLog tl2 = createTimeLog(null, null);

			RangeSet<LocalTime> result = TimeHelper.getGapsBetweenIntervals(List.of(tl1, tl2));

			assertThat(result.isEmpty()).isTrue();
		}

		@Test
		@DisplayName("Skips intervals where start is after end")
		void testGetGapsSkipsInvalidIntervals() {
			TimeLog valid = createTimeLog(LocalTime.of(9, 0), LocalTime.of(12, 0));
			TimeLog invalid = createTimeLog(LocalTime.of(17, 0), LocalTime.of(13, 0));

			RangeSet<LocalTime> result = TimeHelper.getGapsBetweenIntervals(List.of(valid, invalid));

			assertThat(result.isEmpty()).isTrue();
		}

	}

}
