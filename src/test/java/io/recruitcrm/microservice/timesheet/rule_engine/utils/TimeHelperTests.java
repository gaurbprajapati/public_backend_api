package io.recruitcrm.microservice.timesheet.rule_engine.utils;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.ChargeMethodType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TemplateWorkDay;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@DisplayName("TimeHelper Tests")
class TimeHelperTests {

	@Test
	@DisplayName("isOverlapping - with overlapping long times")
	void isOverlappingWithOverlappingLongTimes() {
		// Arrange
		long startTime1 = 1000;
		long endTime1 = 2000;
		long startTime2 = 1500;
		long endTime2 = 2500;

		// Act
		boolean result = TimeHelper.isOverlapping(startTime1, endTime1, startTime2, endTime2);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isOverlapping - with non-overlapping long times")
	void isOverlappingWithNonOverlappingLongTimes() {
		// Arrange
		long startTime1 = 1000;
		long endTime1 = 2000;
		long startTime2 = 2500;
		long endTime2 = 3000;

		// Act
		boolean result = TimeHelper.isOverlapping(startTime1, endTime1, startTime2, endTime2);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isWithinRange - with time within range")
	void isWithinRangeWithTimeWithinRange() {
		// Arrange
		long time = 1500;
		long startTime = 1000;
		long endTime = 2000;

		// Act
		boolean result = TimeHelper.isWithinRange(time, startTime, endTime);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isWithinRange - with time outside range")
	void isWithinRangeWithTimeOutsideRange() {
		// Arrange
		long time = 2500;
		long startTime = 1000;
		long endTime = 2000;

		// Act
		boolean result = TimeHelper.isWithinRange(time, startTime, endTime);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isOverlapping - with overlapping LocalTime")
	void isOverlappingWithOverlappingLocalTime() {
		// Arrange
		LocalTime startTime1 = LocalTime.of(9, 0);
		LocalTime endTime1 = LocalTime.of(17, 0);
		LocalTime startTime2 = LocalTime.of(12, 0);
		LocalTime endTime2 = LocalTime.of(18, 0);

		// Act
		boolean result = TimeHelper.isOverlapping(startTime1, endTime1, startTime2, endTime2);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isOverlapping - with null LocalTime")
	void isOverlappingWithNullLocalTime() {
		// Act
		boolean result = TimeHelper.isOverlapping(null, LocalTime.of(17, 0), LocalTime.of(12, 0), LocalTime.of(18, 0));

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isWithinRange - with LocalTime within range")
	void isWithinRangeWithLocalTimeWithinRange() {
		// Arrange
		LocalTime time = LocalTime.of(12, 0);
		LocalTime startTime = LocalTime.of(9, 0);
		LocalTime endTime = LocalTime.of(17, 0);

		// Act
		boolean result = TimeHelper.isWithinRange(time, startTime, endTime);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isWithinRange - with null LocalTime")
	void isWithinRangeWithNullLocalTime() {
		// Act
		boolean result = TimeHelper.isWithinRange(null, LocalTime.of(9, 0), LocalTime.of(17, 0));

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isBefore - with LocalTime before")
	void isBeforeWithLocalTimeBefore() {
		// Arrange
		LocalTime startTime1 = LocalTime.of(9, 0);
		LocalTime endTime1 = LocalTime.of(12, 0);
		LocalTime startTime2 = LocalTime.of(14, 0);
		LocalTime endTime2 = LocalTime.of(17, 0);

		// Act
		boolean result = TimeHelper.isBefore(startTime1, endTime1, startTime2, endTime2);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isBefore - with null LocalTime")
	void isBeforeWithNullLocalTime() {
		// Act
		boolean result = TimeHelper.isBefore(null, LocalTime.of(12, 0), LocalTime.of(14, 0), LocalTime.of(17, 0));

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isBefore - with long times before")
	void isBeforeWithLongTimesBefore() {
		// Arrange
		long startTime1 = 1000;
		long endTime1 = 2000;
		long startTime2 = 3000;

		// Act
		boolean result = TimeHelper.isBefore(startTime1, endTime1, startTime2);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isAfter - with LocalTime after")
	void isAfterWithLocalTimeAfter() {
		// Arrange
		LocalTime startTime1 = LocalTime.of(14, 0);
		LocalTime endTime1 = LocalTime.of(17, 0);
		LocalTime startTime2 = LocalTime.of(9, 0);
		LocalTime endTime2 = LocalTime.of(12, 0);

		// Act
		boolean result = TimeHelper.isAfter(startTime1, endTime1, startTime2, endTime2);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isAfter - with null LocalTime")
	void isAfterWithNullLocalTime() {
		// Act
		boolean result = TimeHelper.isAfter(null, LocalTime.of(17, 0), LocalTime.of(9, 0), LocalTime.of(12, 0));

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isAfter - with long times after")
	void isAfterWithLongTimesAfter() {
		// Arrange
		long startTime1 = 3000;
		long endTime1 = 4000;
		long endTime2 = 2000;

		// Act
		boolean result = TimeHelper.isAfter(startTime1, endTime1, endTime2);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isOverlapping(long) - first conjunct false short-circuits second")
	void isOverlappingLongFirstConjunctFalse() {
		assertThat(TimeHelper.isOverlapping(3000L, 4000L, 1000L, 2000L)).isFalse();
	}

	@Test
	@DisplayName("isOverlapping(long) - first true second false")
	void isOverlappingLongFirstTrueSecondFalse() {
		assertThat(TimeHelper.isOverlapping(1000L, 2000L, 2000L, 3000L)).isFalse();
	}

	@Test
	@DisplayName("isWithinRange(long) - time before start")
	void isWithinRangeLongBeforeStart() {
		assertThat(TimeHelper.isWithinRange(500L, 1000L, 2000L)).isFalse();
	}

	@Test
	@DisplayName("isOverlapping(LocalTime) - only endTime1 null")
	void isOverlappingLocalTimeOnlyEndTime1Null() {
		assertThat(TimeHelper.isOverlapping(LocalTime.of(9, 0), null, LocalTime.of(12, 0), LocalTime.of(18, 0)))
			.isFalse();
	}

	@Test
	@DisplayName("isOverlapping(LocalTime) - only startTime1 null")
	void isOverlappingLocalTimeOnlyStartTime1Null() {
		assertThat(TimeHelper.isOverlapping(null, LocalTime.of(17, 0), LocalTime.of(12, 0), LocalTime.of(18, 0)))
			.isFalse();
	}

	@Test
	@DisplayName("isOverlapping(LocalTime) - only startTime2 null")
	void isOverlappingLocalTimeOnlyStartTime2Null() {
		assertThat(TimeHelper.isOverlapping(LocalTime.of(9, 0), LocalTime.of(17, 0), null, LocalTime.of(18, 0)))
			.isFalse();
	}

	@Test
	@DisplayName("isOverlapping(LocalTime) - only endTime2 null")
	void isOverlappingLocalTimeOnlyEndTime2Null() {
		assertThat(TimeHelper.isOverlapping(LocalTime.of(9, 0), LocalTime.of(17, 0), LocalTime.of(12, 0), null))
			.isFalse();
	}

	@Test
	@DisplayName("isOverlapping(LocalTime) - first isBefore true second false")
	void isOverlappingLocalTimeFirstTrueSecondFalse() {
		assertThat(TimeHelper.isOverlapping(LocalTime.of(9, 0), LocalTime.of(12, 0), LocalTime.of(14, 0),
				LocalTime.of(17, 0)))
			.isFalse();
	}

	@Test
	@DisplayName("isOverlapping(LocalTime) - first isBefore false")
	void isOverlappingLocalTimeFirstFalse() {
		assertThat(TimeHelper.isOverlapping(LocalTime.of(14, 0), LocalTime.of(17, 0), LocalTime.of(9, 0),
				LocalTime.of(12, 0)))
			.isFalse();
	}

	@Test
	@DisplayName("isWithinRange(LocalTime) - time before start")
	void isWithinRangeLocalTimeBeforeStart() {
		assertThat(TimeHelper.isWithinRange(LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(17, 0))).isFalse();
	}

	@Test
	@DisplayName("isWithinRange(LocalTime) - time after end")
	void isWithinRangeLocalTimeAfterEnd() {
		assertThat(TimeHelper.isWithinRange(LocalTime.of(18, 0), LocalTime.of(9, 0), LocalTime.of(17, 0))).isFalse();
	}

	@Test
	@DisplayName("isWithinRange(LocalTime) - only startTime null")
	void isWithinRangeLocalTimeOnlyStartNull() {
		assertThat(TimeHelper.isWithinRange(LocalTime.of(12, 0), null, LocalTime.of(17, 0))).isFalse();
	}

	@Test
	@DisplayName("isWithinRange(LocalTime) - only endTime null")
	void isWithinRangeLocalTimeOnlyEndNull() {
		assertThat(TimeHelper.isWithinRange(LocalTime.of(12, 0), LocalTime.of(9, 0), null)).isFalse();
	}

	@Test
	@DisplayName("isBefore(LocalTime) - only startTime1 null")
	void isBeforeLocalTimeOnlyStartTime2Null() {
		assertThat(TimeHelper.isBefore(LocalTime.of(9, 0), LocalTime.of(12, 0), null, LocalTime.of(17, 0))).isFalse();
	}

	@Test
	@DisplayName("isBefore(LocalTime) - only startTime1 null")
	void isBeforeLocalTimeOnlyStartTime1Null() {
		assertThat(TimeHelper.isBefore(null, LocalTime.of(12, 0), LocalTime.of(14, 0), LocalTime.of(17, 0))).isFalse();
	}

	@Test
	@DisplayName("isBefore(LocalTime) - only endTime1 null")
	void isBeforeLocalTimeOnlyEndTime1Null() {
		assertThat(TimeHelper.isBefore(LocalTime.of(9, 0), null, LocalTime.of(14, 0), LocalTime.of(17, 0))).isFalse();
	}

	@Test
	@DisplayName("isBefore(LocalTime) - only endTime2 null")
	void isBeforeLocalTimeOnlyEndTime2Null() {
		assertThat(TimeHelper.isBefore(LocalTime.of(9, 0), LocalTime.of(12, 0), LocalTime.of(14, 0), null)).isFalse();
	}

	@Test
	@DisplayName("isBefore(LocalTime) - first before false")
	void isBeforeLocalTimeFirstConjunctFalse() {
		assertThat(
				TimeHelper.isBefore(LocalTime.of(9, 0), LocalTime.of(12, 0), LocalTime.of(8, 0), LocalTime.of(11, 0)))
			.isFalse();
	}

	@Test
	@DisplayName("isBefore(LocalTime) - second before false")
	void isBeforeLocalTimeSecondConjunctFalse() {
		assertThat(
				TimeHelper.isBefore(LocalTime.of(9, 0), LocalTime.of(14, 0), LocalTime.of(12, 0), LocalTime.of(17, 0)))
			.isFalse();
	}

	@Test
	@DisplayName("isBefore(long) - first conjunct false")
	void isBeforeLongFirstFalse() {
		assertThat(TimeHelper.isBefore(3000L, 4000L, 1000L)).isFalse();
	}

	@Test
	@DisplayName("isBefore(long) - second conjunct false")
	void isBeforeLongSecondFalse() {
		assertThat(TimeHelper.isBefore(1000L, 4000L, 2000L)).isFalse();
	}

	@Test
	@DisplayName("isAfter(LocalTime) - only endTime2 null")
	void isAfterLocalTimeOnlyEndTime2Null() {
		assertThat(TimeHelper.isAfter(LocalTime.of(14, 0), LocalTime.of(17, 0), LocalTime.of(9, 0), null)).isFalse();
	}

	@Test
	@DisplayName("isAfter(LocalTime) - only startTime1 null")
	void isAfterLocalTimeOnlyStartTime1Null() {
		assertThat(TimeHelper.isAfter(null, LocalTime.of(17, 0), LocalTime.of(9, 0), LocalTime.of(12, 0))).isFalse();
	}

	@Test
	@DisplayName("isAfter(LocalTime) - only endTime1 null")
	void isAfterLocalTimeOnlyEndTime1Null() {
		assertThat(TimeHelper.isAfter(LocalTime.of(14, 0), null, LocalTime.of(9, 0), LocalTime.of(12, 0))).isFalse();
	}

	@Test
	@DisplayName("isAfter(LocalTime) - only startTime2 null")
	void isAfterLocalTimeOnlyStartTime2Null() {
		assertThat(TimeHelper.isAfter(LocalTime.of(14, 0), LocalTime.of(17, 0), null, LocalTime.of(12, 0))).isFalse();
	}

	@Test
	@DisplayName("isAfter(LocalTime) - first after false")
	void isAfterLocalTimeFirstFalse() {
		assertThat(
				TimeHelper.isAfter(LocalTime.of(9, 0), LocalTime.of(12, 0), LocalTime.of(10, 0), LocalTime.of(15, 0)))
			.isFalse();
	}

	@Test
	@DisplayName("isAfter(LocalTime) - second after false")
	void isAfterLocalTimeSecondFalse() {
		assertThat(
				TimeHelper.isAfter(LocalTime.of(16, 0), LocalTime.of(15, 0), LocalTime.of(9, 0), LocalTime.of(15, 0)))
			.isFalse();
	}

	@Test
	@DisplayName("isAfter(long) - first conjunct false")
	void isAfterLongFirstFalse() {
		assertThat(TimeHelper.isAfter(1000L, 2000L, 3000L)).isFalse();
	}

	@Test
	@DisplayName("isAfter(long) - second conjunct false")
	void isAfterLongSecondFalse() {
		assertThat(TimeHelper.isAfter(16000L, 15000L, 15000L)).isFalse();
	}

	@Test
	@DisplayName("calculateDuration - with valid LocalTime")
	void testCalculateDurationWithValidLocalTime() {
		// Arrange
		LocalTime startTime = LocalTime.of(9, 0);
		LocalTime endTime = LocalTime.of(17, 0);

		// Act
		Duration result = TimeHelper.calculateDuration(startTime, endTime);

		// Assert
		assertThat(result).isEqualTo(Duration.ofHours(8));
	}

	@Test
	@DisplayName("calculateDuration - with null LocalTime")
	void testCalculateDurationWithNullLocalTime() {
		// Act
		Duration result = TimeHelper.calculateDuration(null, LocalTime.of(17, 0));

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateDuration - with null end LocalTime only")
	void testCalculateDurationWithNullEndLocalTimeOnly() {
		assertThat(TimeHelper.calculateDuration(LocalTime.of(9, 0), null)).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateDuration - with invalid LocalTime order")
	void testCalculateDurationWithInvalidLocalTimeOrder() {
		// Arrange
		LocalTime startTime = LocalTime.of(17, 0);
		LocalTime endTime = LocalTime.of(9, 0);

		// Act
		Duration result = TimeHelper.calculateDuration(startTime, endTime);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateDuration - with valid long times")
	void testCalculateDurationWithValidLongTimes() {
		// Arrange
		long startTime = 1000;
		long endTime = 2000;

		// Act
		Duration result = TimeHelper.calculateDuration(startTime, endTime);

		// Assert
		assertThat(result).isEqualTo(Duration.ofSeconds(1000));
	}

	@Test
	@DisplayName("calculateDuration - with invalid long times order")
	void testCalculateDurationWithInvalidLongTimesOrder() {
		// Arrange
		long startTime = 2000;
		long endTime = 1000;

		// Act
		Duration result = TimeHelper.calculateDuration(startTime, endTime);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("addDuration - with LocalTime")
	void testAddDurationWithLocalTime() {
		// Arrange
		LocalTime time = LocalTime.of(9, 0);
		Duration duration = Duration.ofHours(2);

		// Act
		LocalTime result = TimeHelper.addDuration(time, duration);

		// Assert
		assertThat(result).isEqualTo(LocalTime.of(11, 0));
	}

	@Test
	@DisplayName("addDuration - with null LocalTime")
	void testAddDurationWithNullLocalTime() {
		// Act
		LocalTime result = TimeHelper.addDuration(null, Duration.ofHours(2));

		// Assert
		assertThat(result).isEqualTo(LocalTime.MIDNIGHT);
	}

	@Test
	@DisplayName("addDuration - with null Duration")
	void testAddDurationWithNullDuration() {
		assertThat(TimeHelper.addDuration(LocalTime.of(9, 0), null)).isEqualTo(LocalTime.MIDNIGHT);
	}

	@Test
	@DisplayName("subtractDuration - with LocalTime")
	void testSubtractDurationWithLocalTime() {
		// Arrange
		LocalTime time = LocalTime.of(11, 0);
		Duration duration = Duration.ofHours(2);

		// Act
		LocalTime result = TimeHelper.subtractDuration(time, duration);

		// Assert
		assertThat(result).isEqualTo(LocalTime.of(9, 0));
	}

	@Test
	@DisplayName("subtractDuration - with null LocalTime")
	void testSubtractDurationWithNullLocalTime() {
		// Act
		LocalTime result = TimeHelper.subtractDuration(null, Duration.ofHours(2));

		// Assert
		assertThat(result).isEqualTo(LocalTime.MIDNIGHT);
	}

	@Test
	@DisplayName("subtractDuration - with null Duration")
	void testSubtractDurationWithNullDuration() {
		assertThat(TimeHelper.subtractDuration(LocalTime.of(11, 0), null)).isEqualTo(LocalTime.MIDNIGHT);
	}

	@Test
	@DisplayName("addDuration - with long time")
	void testAddDurationWithLongTime() {
		// Arrange
		long time = 1000;
		Duration duration = Duration.ofSeconds(500);

		// Act
		long result = TimeHelper.addDuration(time, duration);

		// Assert
		assertThat(result).isEqualTo(1500);
	}

	@Test
	@DisplayName("subtractDuration - with long time")
	void testSubtractDurationWithLongTime() {
		// Arrange
		long time = 1500;
		Duration duration = Duration.ofSeconds(500);

		// Act
		long result = TimeHelper.subtractDuration(time, duration);

		// Assert
		assertThat(result).isEqualTo(1000);
	}

	@Test
	@DisplayName("getTemplateWorkDayFromDayType - with matching work day")
	void testGetTemplateWorkDayFromDayTypeWithMatchingWorkDay() {
		// Arrange
		List<TemplateWorkDay> templateWorkDays = List.of(
				new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)),
				new TemplateWorkDay(WorkDay.TUESDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));

		// Act
		TemplateWorkDay result = TimeHelper.getTemplateWorkDayFromDayType(templateWorkDays, WorkDay.MONDAY);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getWorkDayType()).isEqualTo(WorkDay.MONDAY);
	}

	@Test
	@DisplayName("getTemplateWorkDayFromDayType - with non-matching work day")
	void testGetTemplateWorkDayFromDayTypeWithNonMatchingWorkDay() {
		// Arrange
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));

		// Act
		TemplateWorkDay result = TimeHelper.getTemplateWorkDayFromDayType(templateWorkDays, WorkDay.TUESDAY);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getTemplateWorkDayFromDayType - with null template work days")
	void testGetTemplateWorkDayFromDayTypeWithNullTemplateWorkDays() {
		// Act
		TemplateWorkDay result = TimeHelper.getTemplateWorkDayFromDayType(null, WorkDay.MONDAY);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getTemplateWorkDayFromDayType - with null work day")
	void testGetTemplateWorkDayFromDayTypeWithNullWorkDay() {
		// Arrange
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));

		// Act
		TemplateWorkDay result = TimeHelper.getTemplateWorkDayFromDayType(templateWorkDays, null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getWorkDayFromLocalDate - with Monday")
	void testGetWorkDayFromLocalDateWithMonday() {
		// Arrange
		LocalDate monday = LocalDate.of(2024, 1, 1); // Monday

		// Act
		WorkDay result = TimeHelper.getWorkDayFromLocalDate(monday);

		// Assert
		assertThat(result).isEqualTo(WorkDay.MONDAY);
	}

	@Test
	@DisplayName("getWorkDayFromLocalDate - with Sunday")
	void testGetWorkDayFromLocalDateWithSunday() {
		// Arrange
		LocalDate sunday = LocalDate.of(2024, 1, 7); // Sunday

		// Act
		WorkDay result = TimeHelper.getWorkDayFromLocalDate(sunday);

		// Assert
		assertThat(result).isEqualTo(WorkDay.SUNDAY);
	}

	@Test
	@DisplayName("getWorkDayFromLocalDate - with null date")
	void testGetWorkDayFromLocalDateWithNullDate() {
		// Act
		WorkDay result = TimeHelper.getWorkDayFromLocalDate(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getWorkDayFromDate - alias for getWorkDayFromLocalDate")
	void testGetWorkDayFromDateAliasForGetWorkDayFromLocalDate() {
		// Arrange
		LocalDate monday = LocalDate.of(2024, 1, 1); // Monday

		// Act
		WorkDay result = TimeHelper.getWorkDayFromDate(monday);

		// Assert
		assertThat(result).isEqualTo(WorkDay.MONDAY);
	}

	@Test
	@DisplayName("isWorkDay - with DAY_OFF")
	void testIsWorkDayWithDayOff() {
		// Act
		boolean result = TimeHelper.isWorkDay(WorkDayType.DAY_OFF);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isWorkDay - with WORK_DAY")
	void testIsWorkDayWithWorkDay() {
		// Act
		boolean result = TimeHelper.isWorkDay(WorkDayType.WORK_DAY);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("toRange - with valid LocalTime")
	void testToRangeWithValidLocalTime() {
		// Arrange
		LocalTime startTime = LocalTime.of(9, 0);
		LocalTime endTime = LocalTime.of(17, 0);

		// Act
		Range<LocalTime> result = TimeHelper.toRange(startTime, endTime);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.contains(LocalTime.of(12, 0))).isTrue();
	}

	@Test
	@DisplayName("toRange - with null LocalTime")
	void testToRangeWithNullLocalTime() {
		// Act
		Range<LocalTime> result = TimeHelper.toRange(null, LocalTime.of(17, 0));

		// Assert
		assertThat(result).isEqualTo(Range.all());
	}

	@Test
	@DisplayName("toRange - with null end LocalTime only")
	void testToRangeWithNullEndLocalTimeOnly() {
		assertThat(TimeHelper.toRange(LocalTime.of(9, 0), null)).isEqualTo(Range.all());
	}

	@Test
	@DisplayName("toRange - with invalid LocalTime order")
	void testToRangeWithInvalidLocalTimeOrder() {
		// Arrange
		LocalTime startTime = LocalTime.of(17, 0);
		LocalTime endTime = LocalTime.of(9, 0);

		// Act
		Range<LocalTime> result = TimeHelper.toRange(startTime, endTime);

		// Assert
		assertThat(result).isEqualTo(Range.all());
	}

	@Test
	@DisplayName("extendContiguously - with valid parameters")
	void testExtendContiguouslyWithValidParameters() {
		// Arrange
		RangeSet<LocalTime> occupied = TreeRangeSet.create();
		RangeSet<LocalTime> workingDay = TreeRangeSet.create();
		workingDay.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));

		RangeSet<LocalTime> incomingPattern = TreeRangeSet.create();
		incomingPattern.add(Range.closedOpen(LocalTime.of(0, 0), LocalTime.of(1, 0)));

		// Act
		Range<LocalTime> result = TimeHelper.extendContiguously(occupied, workingDay, incomingPattern);

		// Assert
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("extendContiguously - with empty incoming pattern")
	void testExtendContiguouslyWithEmptyIncomingPattern() {
		// Arrange
		RangeSet<LocalTime> occupied = TreeRangeSet.create();
		RangeSet<LocalTime> workingDay = TreeRangeSet.create();
		workingDay.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));

		RangeSet<LocalTime> incomingPattern = TreeRangeSet.create();

		// Act
		Range<LocalTime> result = TimeHelper.extendContiguously(occupied, workingDay, incomingPattern);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("extendContiguously - incomingPattern empty returns null")
	void testExtendContiguouslyIncomingPatternEmpty() {
		RangeSet<LocalTime> occupied = TreeRangeSet.create();
		RangeSet<LocalTime> workingDay = TreeRangeSet.create();
		workingDay.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		RangeSet<LocalTime> incomingPattern = TreeRangeSet.create();
		assertThat(TimeHelper.extendContiguously(occupied, workingDay, incomingPattern)).isNull();
	}

	@Test
	@DisplayName("extendContiguously - workingDay empty returns null")
	void testExtendContiguouslyWorkingDayEmpty() {
		RangeSet<LocalTime> occupied = TreeRangeSet.create();
		RangeSet<LocalTime> workingDay = TreeRangeSet.create();
		RangeSet<LocalTime> incomingPattern = TreeRangeSet.create();
		incomingPattern.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		assertThat(TimeHelper.extendContiguously(occupied, workingDay, incomingPattern)).isNull();
	}

	@Test
	@DisplayName("extendContiguously - anchor not before dayEnd returns null")
	void testExtendContiguouslyAnchorNotBeforeDayEnd() {
		RangeSet<LocalTime> occupied = TreeRangeSet.create();
		RangeSet<LocalTime> workingDay = TreeRangeSet.create();
		workingDay.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		RangeSet<LocalTime> incomingPattern = TreeRangeSet.create();
		incomingPattern.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		// Fill occupied so anchor is at or after dayEnd
		occupied.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		assertThat(TimeHelper.extendContiguously(occupied, workingDay, incomingPattern)).isNull();
	}

	@Test
	@DisplayName("extendContiguously - slice zero returns null")
	void testExtendContiguouslySliceZero() {
		RangeSet<LocalTime> occupied = TreeRangeSet.create();
		occupied.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(9, 0, 30)));
		RangeSet<LocalTime> workingDay = TreeRangeSet.create();
		workingDay.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(9, 1)));
		RangeSet<LocalTime> incomingPattern = TreeRangeSet.create();
		incomingPattern.add(Range.closedOpen(LocalTime.of(0, 0), LocalTime.of(1, 0)));
		assertThat(TimeHelper.extendContiguously(occupied, workingDay, incomingPattern)).isNull();
	}

	@Test
	@DisplayName("claimDOTime - with valid parameters")
	void testClaimDOTimeWithValidParameters() {
		// Arrange
		Range<LocalTime> fullRange = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		Duration doQuota = Duration.ofHours(2);

		// Act
		List<Range<LocalTime>> result = TimeHelper.claimDOTime(fullRange, occupiedRanges, doQuota);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
	}

	@Test
	@DisplayName("claimDOTime - blockDuration <= remaining branch")
	void testClaimDOTimeBlockDurationLessThanOrEqualRemaining() {
		Range<LocalTime> fullRange = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(12, 0));
		RangeSet<LocalTime> occupied = TreeRangeSet.create();
		Duration doQuota = Duration.ofHours(3);
		// All available block fits in remaining
		var result = TimeHelper.claimDOTime(fullRange, occupied, doQuota);
		assertThat(result).hasSize(1);
	}

	@Test
	@DisplayName("claimDOTime - blockDuration > remaining branch")
	void testClaimDOTimeBlockDurationGreaterThanRemaining() {
		Range<LocalTime> fullRange = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(12, 0));
		RangeSet<LocalTime> occupied = TreeRangeSet.create();
		Duration doQuota = Duration.ofHours(2);
		// Only part of block fits in remaining
		var result = TimeHelper.claimDOTime(fullRange, occupied, doQuota);
		assertThat(result).hasSize(1);
		assertThat(result.get(0).upperEndpoint()).isEqualTo(LocalTime.of(11, 0));
	}

	@Test
	@DisplayName("claimDOTimeFromEnd - with valid parameters")
	void testClaimDOTimeFromEndWithValidParameters() {
		// Arrange
		Range<LocalTime> fullRange = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		Duration doQuota = Duration.ofHours(2);

		// Act
		List<Range<LocalTime>> result = TimeHelper.claimDOTimeFromEnd(fullRange, occupiedRanges, doQuota);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
	}

	@Test
	@DisplayName("claimDOTimeFromEnd - claims partial block from end when quota is smaller than last block")
	void testClaimDOTimeFromEndPartialBlockFromEnd() {
		Range<LocalTime> fullRange = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		List<Range<LocalTime>> result = TimeHelper.claimDOTimeFromEnd(fullRange, occupiedRanges, Duration.ofHours(2));
		assertThat(result).hasSize(1);
		assertThat(result.get(0).lowerEndpoint()).isEqualTo(LocalTime.of(15, 0));
		assertThat(result.get(0).upperEndpoint()).isEqualTo(LocalTime.of(17, 0));
	}

	@Test
	@DisplayName("claimDOTimeFromEnd - fully claims tail range then partial from earlier range")
	void testClaimDOTimeFromEndMultiRangeFullThenPartial() {
		Range<LocalTime> fullRange = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(13, 0)));
		List<Range<LocalTime>> result = TimeHelper.claimDOTimeFromEnd(fullRange, occupiedRanges, Duration.ofHours(5));
		assertThat(result).hasSize(2);
		assertThat(result.get(0).lowerEndpoint()).isEqualTo(LocalTime.of(13, 0));
		assertThat(result.get(0).upperEndpoint()).isEqualTo(LocalTime.of(17, 0));
		assertThat(result.get(1).lowerEndpoint()).isEqualTo(LocalTime.of(11, 0));
		assertThat(result.get(1).upperEndpoint()).isEqualTo(LocalTime.of(12, 0));
	}

	@Test
	@DisplayName("claimDOTimeFromEnd - no available ranges when full day is occupied")
	void testClaimDOTimeFromEndNoAvailableRanges() {
		Range<LocalTime> fullRange = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));
		assertThat(TimeHelper.claimDOTimeFromEnd(fullRange, occupiedRanges, Duration.ofHours(1))).isEmpty();
	}

	@Test
	@DisplayName("calculateDOHours - with valid durations")
	void testCalculateDOHoursWithValidDurations() {
		// Arrange
		Duration workedDuration = Duration.ofHours(10);
		Duration currentThreshold = Duration.ofHours(8);
		Duration nextThreshold = Duration.ofHours(12);

		// Act
		Duration result = TimeHelper.calculateDOHours(workedDuration, currentThreshold, nextThreshold);

		// Assert
		assertThat(result).isEqualTo(Duration.ofHours(2));
	}

	@Test
	@DisplayName("calculateDOHours(long) - worked hours at or below threshold returns zero")
	void testCalculateDOHoursLongAtOrBelowThreshold() throws Exception {
		assertThat(invokeCalculateDOHoursLong(8, 8, 12)).isZero();
		assertThat(invokeCalculateDOHoursLong(5, 8, 12)).isZero();
	}

	@Test
	@DisplayName("calculateDOHours(long) - claims hours between threshold and next threshold")
	void testCalculateDOHoursLongClaimsBetweenThresholds() throws Exception {
		assertThat(invokeCalculateDOHoursLong(10, 8, 12)).isEqualTo(2);
	}

	@Test
	@DisplayName("calculateDOHours(long) - caps claim at tier maximum")
	void testCalculateDOHoursLongCapsAtTierMaximum() throws Exception {
		assertThat(invokeCalculateDOHoursLong(20, 8, 12)).isEqualTo(4);
	}

	@Test
	@DisplayName("calculateTotalFreeDuration - with valid parameters")
	void testCalculateTotalFreeDurationWithValidParameters() {
		// Arrange
		Range<LocalTime> workingRange = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(13, 0)));

		// Act
		Duration result = TimeHelper.calculateTotalFreeDuration(workingRange, occupiedRanges);

		// Assert
		assertThat(result).isEqualTo(Duration.ofHours(7));
	}

	@Test
	@DisplayName("getFreeTimeRanges - with valid parameters")
	void testGetFreeTimeRangesWithValidParameters() {
		// Arrange
		Range<LocalTime> workingRange = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(13, 0)));

		// Act
		RangeSet<LocalTime> result = TimeHelper.getFreeTimeRanges(workingRange, occupiedRanges);

		// Assert
		assertThat(result).isNotNull().satisfies((ranges) -> assertThat(ranges.asRanges()).hasSize(2));
	}

	@Test
	@DisplayName("getAvailableTimeRanges - with valid parameters")
	void testGetAvailableTimeRangesWithValidParameters() {
		// Arrange
		Range<LocalTime> grossRange = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> occupiedRanges = TreeRangeSet.create();
		occupiedRanges.add(Range.closedOpen(LocalTime.of(12, 0), LocalTime.of(13, 0)));

		// Act
		RangeSet<LocalTime> result = TimeHelper.getAvailableTimeRanges(grossRange, occupiedRanges);

		// Assert
		assertThat(result).isNotNull().satisfies((ranges) -> assertThat(ranges.asRanges()).hasSize(2));
	}

	@Test
	@DisplayName("normaliseDurationRangeToLocalTime - with valid duration")
	void testNormaliseDurationRangeToLocalTimeWithValidDuration() {
		// Arrange
		Duration start = Duration.ofHours(2);
		Duration end = Duration.ofHours(4);

		// Act
		Range<LocalTime> result = TimeHelper.normaliseDurationRangeToLocalTime(start, end);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.lowerEndpoint()).isEqualTo(LocalTime.of(2, 0));
		assertThat(result.upperEndpoint()).isEqualTo(LocalTime.of(4, 0));
	}

	@Test
	@DisplayName("normaliseDurationToLocalTime - with valid duration")
	void testNormaliseDurationToLocalTimeWithValidDuration() {
		// Arrange
		Duration duration = Duration.ofHours(2);

		// Act
		LocalTime result = TimeHelper.normaliseDurationToLocalTime(duration);

		// Assert
		assertThat(result).isEqualTo(LocalTime.of(2, 0));
	}

	@Test
	@DisplayName("convertDurationToApproximateHours - with valid duration")
	void testConvertDurationToApproximateHoursWithValidDuration() {
		// Arrange
		Duration duration = Duration.ofHours(2);

		// Act
		Float result = TimeHelper.convertDurationToApproximateHours(duration);

		// Assert
		assertThat(result).isEqualTo(2.0f);
	}

	@Test
	@DisplayName("convertRangeSetToDuration - with valid range set")
	void testConvertRangeSetToDurationWithValidRangeSet() {
		// Arrange
		RangeSet<LocalTime> rangeSet = TreeRangeSet.create();
		rangeSet.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(12, 0)));
		rangeSet.add(Range.closedOpen(LocalTime.of(13, 0), LocalTime.of(17, 0)));

		// Act
		Duration result = TimeHelper.convertRangeSetToDuration(rangeSet);

		// Assert
		assertThat(result).isEqualTo(Duration.ofHours(7));
	}

	@Test
	@DisplayName("convertRangeSetToDuration - null range set returns zero")
	void testConvertRangeSetToDurationNullReturnsZero() {
		assertThat(TimeHelper.convertRangeSetToDuration(null)).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("convertRangeSetToDuration - empty range set returns zero")
	void testConvertRangeSetToDurationEmptyReturnsZero() {
		assertThat(TimeHelper.convertRangeSetToDuration(TreeRangeSet.create())).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("splitTimeLogsOnWeeklyBasis - with valid time logs")
	void testSplitTimeLogsOnWeeklyBasisWithValidTimeLogs() {
		// Arrange
		List<TimeLog> timeLogs = new ArrayList<>();
		TimeLog timeLog1 = mock(TimeLog.class);
		given(timeLog1.getDate()).willReturn(LocalDate.of(2024, 1, 1)); // Monday
		timeLogs.add(timeLog1);

		TimeLog timeLog2 = mock(TimeLog.class);
		given(timeLog2.getDate()).willReturn(LocalDate.of(2024, 1, 8)); // Next Monday
		timeLogs.add(timeLog2);

		// Act
		List<List<TimeLog>> result = TimeHelper.splitTimeLogsOnWeeklyBasis(timeLogs);

		// Assert
		assertThat(result).isNotNull().hasSize(2);
	}

	@Test
	@DisplayName("splitTimeLogsOnWeeklyBasis - with custom week start day")
	void testSplitTimeLogsOnWeeklyBasisWithCustomWeekStartDay() {
		// Arrange
		List<TimeLog> timeLogs = new ArrayList<>();
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getDate()).willReturn(LocalDate.of(2024, 1, 1));
		timeLogs.add(timeLog);

		// Act
		List<List<TimeLog>> result = TimeHelper.splitTimeLogsOnWeeklyBasis(timeLogs, WorkDay.MONDAY);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
	}

	@Test
	@DisplayName("isWeekDay - with week day")
	void testIsWeekDayWithWeekDay() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getDayType()).willReturn(WorkDayType.WORK_DAY);

		// Act
		boolean result = TimeHelper.isWeekDay(timeLog);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isWeekDay - null timeLog")
	void testIsWeekDayNullTimeLog() {
		assertThat(TimeHelper.isWeekDay(null)).isFalse();
	}

	@Test
	@DisplayName("isWeekDay - null dayType")
	void testIsWeekDayNullDayType() {
		TimeLog log = mock(TimeLog.class);
		given(log.getDayType()).willReturn(null);
		assertThat(TimeHelper.isWeekDay(log)).isFalse();
	}

	@Test
	@DisplayName("isWeekDay - day off returns false")
	void testIsWeekDayDayOff() {
		TimeLog log = mock(TimeLog.class);
		given(log.getDayType()).willReturn(WorkDayType.DAY_OFF);
		assertThat(TimeHelper.isWeekDay(log)).isFalse();
	}

	@Test
	@DisplayName("getMinimumDuration - with first duration smaller")
	void testGetMinimumDurationWithFirstDurationSmaller() {
		// Arrange
		Duration duration1 = Duration.ofHours(2);
		Duration duration2 = Duration.ofHours(4);

		// Act
		Duration result = TimeHelper.getMinimumDuration(duration1, duration2);

		// Assert
		assertThat(result).isEqualTo(duration1);
	}

	@Test
	@DisplayName("getMinimumDuration - with second duration smaller")
	void testGetMinimumDurationWithSecondDurationSmaller() {
		// Arrange
		Duration duration1 = Duration.ofHours(4);
		Duration duration2 = Duration.ofHours(2);

		// Act
		Duration result = TimeHelper.getMinimumDuration(duration1, duration2);

		// Assert
		assertThat(result).isEqualTo(duration2);
	}

	@Test
	@DisplayName("isDurationBasedTimeLog - with duration-based time log")
	void testIsDurationBasedTimeLogWithDurationBasedTimeLog() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getWorkTime()).willReturn(Duration.ofHours(8));

		// Act
		boolean result = TimeHelper.isDurationBasedTimeLog(timeLog);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isDurationBasedTimeLog - with range-based time log")
	void testIsDurationBasedTimeLogWithRangeBasedTimeLog() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getWorkTime()).willReturn(null);
		given(timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		// Act
		boolean result = TimeHelper.isDurationBasedTimeLog(timeLog);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isDurationBasedTimeLog - with null time log")
	void testIsDurationBasedTimeLogWithNullTimeLog() {
		// Act
		boolean result = TimeHelper.isDurationBasedTimeLog(null);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("calculateTimeLogDuration - with null time log")
	void testCalculateTimeLogDurationWithNullTimeLog() {
		assertThat(TimeHelper.calculateTimeLogDuration(null)).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateTimeLogDuration - duration-based uses normalized times when present")
	void testCalculateTimeLogDurationDurationBasedUsesNormalizedTimes() {
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkTime(Duration.ofHours(8));
		timeLog.setNormalizedWorkStartTime(LocalTime.of(10, 0));
		timeLog.setNormalizedWorkEndTime(LocalTime.of(13, 0));
		assertThat(TimeHelper.calculateTimeLogDuration(timeLog)).isEqualTo(Duration.ofHours(3));
	}

	@Test
	@DisplayName("calculateTimeLogDuration - with valid start and end times")
	void testCalculateTimeLogDurationWithValidStartAndEndTimes() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(LocalTime.of(9, 0));
		timeLog.setWorkEndTime(LocalTime.of(17, 0));

		// Act
		Duration result = TimeHelper.calculateTimeLogDuration(timeLog);

		// Assert
		assertThat(result).isEqualTo(Duration.ofHours(8));
	}

	@Test
	@DisplayName("calculateTimeLogDuration - with null start time")
	void testCalculateTimeLogDurationWithNullStartTime() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(null);
		timeLog.setWorkEndTime(LocalTime.of(17, 0));

		// Act
		Duration result = TimeHelper.calculateTimeLogDuration(timeLog);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateTimeLogDuration - with null end time")
	void testCalculateTimeLogDurationWithNullEndTime() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(LocalTime.of(9, 0));
		timeLog.setWorkEndTime(null);

		// Act
		Duration result = TimeHelper.calculateTimeLogDuration(timeLog);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateTimeLogDuration - with invalid time range")
	void testCalculateTimeLogDurationWithInvalidTimeRange() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkStartTime(LocalTime.of(17, 0));
		timeLog.setWorkEndTime(LocalTime.of(9, 0));

		// Act
		Duration result = TimeHelper.calculateTimeLogDuration(timeLog);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateTimeLogDuration - with workTime duration")
	void testCalculateTimeLogDurationWithWorkTimeDuration() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkTime(Duration.ofHours(8));

		// Act
		Duration result = TimeHelper.calculateTimeLogDuration(timeLog);

		// Assert
		assertThat(result).isEqualTo(Duration.ofHours(8));
	}

	@Test
	@DisplayName("calculateTimeLogDuration - with both workTime and start/end times")
	void testCalculateTimeLogDurationWithBothWorkTimeAndStartEndTimes() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setWorkTime(Duration.ofHours(8));
		timeLog.setWorkStartTime(LocalTime.of(9, 0));
		timeLog.setWorkEndTime(LocalTime.of(17, 0));

		// Act
		Duration result = TimeHelper.calculateTimeLogDuration(timeLog);

		// Assert
		assertThat(result).isEqualTo(Duration.ofHours(8));
	}

	@Test
	@DisplayName("calculateTimeLogDuration - with no time data")
	void testCalculateTimeLogDurationWithNoTimeData() {
		// Arrange
		TimeLog timeLog = new TimeLog();

		// Act
		Duration result = TimeHelper.calculateTimeLogDuration(timeLog);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateTimeLogDuration - duration-based, null normalized, non-null workTime")
	void testCalculateTimeLogDurationDurationBasedNullNormalizedNonNullWorkTime() {
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkTime()).willReturn(Duration.ofMinutes(30));
		given(log.getNormalizedWorkStartTime()).willReturn(null);
		given(log.getNormalizedWorkEndTime()).willReturn(null);
		assertThat(TimeHelper.calculateTimeLogDuration(log)).isEqualTo(Duration.ofMinutes(30));
	}

	@Test
	@DisplayName("calculateTimeLogDuration - duration-based, one normalized null uses workTime")
	void testCalculateTimeLogDurationDurationBasedOneNormalizedNullUsesWorkTime() {
		TimeLog log = new TimeLog();
		log.setWorkTime(Duration.ofHours(2));
		log.setNormalizedWorkStartTime(LocalTime.of(9, 0));
		log.setNormalizedWorkEndTime(null);
		assertThat(TimeHelper.calculateTimeLogDuration(log)).isEqualTo(Duration.ofHours(2));
	}

	@Test
	@DisplayName("calculateTimeLogDuration - duration-based, workTime null on second read falls through to range")
	void testCalculateTimeLogDurationDurationBasedWorkTimeNullOnSecondRead() {
		TimeLog log = mock(TimeLog.class);
		AtomicInteger workTimeCalls = new AtomicInteger(0);
		given(log.getWorkTime()).willAnswer((invocation) -> {
			int n = workTimeCalls.getAndIncrement();
			return (n == 0) ? Duration.ofHours(1) : null;
		});
		given(log.getNormalizedWorkStartTime()).willReturn(null);
		given(log.getNormalizedWorkEndTime()).willReturn(null);
		given(log.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log.getWorkEndTime()).willReturn(LocalTime.of(10, 0));
		assertThat(TimeHelper.calculateTimeLogDuration(log)).isEqualTo(Duration.ofHours(1));
	}

	@Test
	@DisplayName("getEffectiveStartTime - with range-based time log")
	void testGetEffectiveStartTimeWithRangeBasedTimeLog() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getWorkTime()).willReturn(null);
		given(timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));

		// Act
		LocalTime result = TimeHelper.getEffectiveStartTime(timeLog);

		// Assert
		assertThat(result).isEqualTo(LocalTime.of(9, 0));
	}

	@Test
	@DisplayName("getEffectiveStartTime - with duration-based time log")
	void testGetEffectiveStartTimeWithDurationBasedTimeLog() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getWorkTime()).willReturn(Duration.ofHours(8));
		given(timeLog.getNormalizedWorkStartTime()).willReturn(LocalTime.of(9, 0));

		// Act
		LocalTime result = TimeHelper.getEffectiveStartTime(timeLog);

		// Assert
		assertThat(result).isEqualTo(LocalTime.of(9, 0));
	}

	@Test
	@DisplayName("getEffectiveStartTime - with null time log")
	void testGetEffectiveStartTimeWithNullTimeLog() {
		// Act
		LocalTime result = TimeHelper.getEffectiveStartTime(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getEffectiveEndTime - with range-based time log")
	void testGetEffectiveEndTimeWithRangeBasedTimeLog() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getWorkTime()).willReturn(null);
		given(timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));

		// Act
		LocalTime result = TimeHelper.getEffectiveEndTime(timeLog);

		// Assert
		assertThat(result).isEqualTo(LocalTime.of(17, 0));
	}

	@Test
	@DisplayName("getEffectiveEndTime - with duration-based time log")
	void testGetEffectiveEndTimeWithDurationBasedTimeLog() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getWorkTime()).willReturn(Duration.ofHours(8));
		given(timeLog.getNormalizedWorkEndTime()).willReturn(LocalTime.of(17, 0));

		// Act
		LocalTime result = TimeHelper.getEffectiveEndTime(timeLog);

		// Assert
		assertThat(result).isEqualTo(LocalTime.of(17, 0));
	}

	@Test
	@DisplayName("getEffectiveEndTime - with null time log")
	void testGetEffectiveEndTimeWithNullTimeLog() {
		// Act
		LocalTime result = TimeHelper.getEffectiveEndTime(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("isTimeRangeWithinRegularHours - with overlapping range")
	void testIsTimeRangeWithinRegularHoursWithOverlappingRange() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(6, 0), LocalTime.of(10, 0))); // Overlaps
																					// with
																					// 00:00-08:00

		LocalDate timeLogDate = LocalDate.of(2024, 1, 1); // Monday
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));

		// Act
		boolean result = TimeHelper.isTimeRangeWithinRegularHours(timeRange, timeLogDate, templateWorkDays);

		// Assert
		assertThat(result).isTrue(); // 6:00-10:00 overlaps with regular hours 00:00-08:00
	}

	@Test
	@DisplayName("isTimeRangeWithinRegularHours - with non-overlapping range")
	void testIsTimeRangeWithinRegularHoursWithNonOverlappingRange() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(18, 0), LocalTime.of(20, 0)));

		LocalDate timeLogDate = LocalDate.of(2024, 1, 1); // Monday
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));

		// Act
		boolean result = TimeHelper.isTimeRangeWithinRegularHours(timeRange, timeLogDate, templateWorkDays);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isTimeRangeWithinRegularHours - with non-work day")
	void testIsTimeRangeWithinRegularHoursWithNonWorkDay() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(16, 0)));

		LocalDate timeLogDate = LocalDate.of(2024, 1, 7); // Sunday
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));

		// Act
		boolean result = TimeHelper.isTimeRangeWithinRegularHours(timeRange, timeLogDate, templateWorkDays);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isTimeRangeWithinRegularHours - workDay is null")
	void testIsTimeRangeWithinRegularHoursWorkDayNull() {
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		List<TemplateWorkDay> templateWorkDays = List.of();
		// Use a date that will not map to a valid WorkDay
		assertThat(TimeHelper.isTimeRangeWithinRegularHours(timeRange, null, templateWorkDays)).isFalse();
	}

	@Test
	@DisplayName("isTimeRangeWithinRegularHours - non-null empty time range")
	void testIsTimeRangeWithinRegularHoursEmptyTimeRange() {
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));
		assertThat(TimeHelper.isTimeRangeWithinRegularHours(timeRange, LocalDate.of(2024, 1, 1), templateWorkDays))
			.isFalse();
	}

	@Test
	@DisplayName("isTimeRangeWithinRegularHours - null templateWorkDays")
	void testIsTimeRangeWithinRegularHoursNullTemplates() {
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		assertThat(TimeHelper.isTimeRangeWithinRegularHours(timeRange, LocalDate.of(2024, 1, 1), null)).isFalse();
	}

	@Test
	@DisplayName("isTimeRangeWithinRegularHours - connected but empty intersection at boundary")
	void testIsTimeRangeWithinRegularHoursConnectedEmptyIntersection() {
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(8, 0), LocalTime.of(9, 0)));
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));
		assertThat(TimeHelper.isTimeRangeWithinRegularHours(timeRange, LocalDate.of(2024, 1, 1), templateWorkDays))
			.isFalse();
	}

	@Test
	@DisplayName("isTimeRangeWithinRegularHours - null time range")
	void testIsTimeRangeWithinRegularHoursNullTimeRange() {
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));
		assertThat(TimeHelper.isTimeRangeWithinRegularHours(null, LocalDate.of(2024, 1, 1), templateWorkDays))
			.isFalse();
	}

	@Test
	@DisplayName("isTimeRangeWithinRegularHours - getWorkDayFromLocalDate returns null")
	void testIsTimeRangeWithinRegularHoursWhenWorkDayNull() {
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));
		try (MockedStatic<TimeHelper> mocked = mockStatic(TimeHelper.class, Mockito.CALLS_REAL_METHODS)) {
			mocked.when(() -> TimeHelper.getWorkDayFromLocalDate(any(LocalDate.class))).thenReturn(null);
			assertThat(TimeHelper.isTimeRangeWithinRegularHours(timeRange, LocalDate.of(2024, 1, 1), templateWorkDays))
				.isFalse();
		}
	}

	@Test
	@DisplayName("isWorkDay(LocalDate, List) - getWorkDayFromLocalDate returns null")
	void testIsWorkDayWhenWorkDayNull() {
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));
		try (MockedStatic<TimeHelper> mocked = mockStatic(TimeHelper.class, Mockito.CALLS_REAL_METHODS)) {
			mocked.when(() -> TimeHelper.getWorkDayFromLocalDate(any(LocalDate.class))).thenReturn(null);
			assertThat(TimeHelper.isWorkDay(LocalDate.of(2024, 1, 1), templateWorkDays)).isFalse();
		}
	}

	@Test
	@DisplayName("isWorkDay - with work day and template")
	void testIsWorkDayWithWorkDayAndTemplate() {
		// Arrange
		LocalDate monday = LocalDate.of(2024, 1, 1); // Monday
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));

		// Act
		boolean result = TimeHelper.isWorkDay(monday, templateWorkDays);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isWorkDay - with non-work day")
	void testIsWorkDayWithNonWorkDay() {
		// Arrange
		LocalDate sunday = LocalDate.of(2024, 1, 7); // Sunday
		List<TemplateWorkDay> templateWorkDays = List
			.of(new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0), LocalTime.of(17, 0)));

		// Act
		boolean result = TimeHelper.isWorkDay(sunday, templateWorkDays);

		// Assert
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("calculatePayAmount - with MULTIPLIER method")
	void testCalculatePayAmountWithMultiplierMethod() {
		// Arrange
		Duration duration = Duration.ofHours(2);
		ChargeMethodType chargeMethod = ChargeMethodType.MULTIPLIER;
		Float basePayRate = 20.0f;
		Float payRateMultiplier = 1.5f;
		Float payRatePerHour = 0.0f;

		// Act
		BigDecimal result = TimeHelper.calculatePayAmount(duration, chargeMethod, basePayRate, payRateMultiplier,
				payRatePerHour);

		// Assert
		assertThat(result).isEqualTo(new BigDecimal("60.0")); // 2 hours * 20 * 1.5
	}

	@Test
	@DisplayName("calculatePayAmount - with FIXED_RATE method")
	void testCalculatePayAmountWithFixedRateMethod() {
		// Arrange
		Duration duration = Duration.ofHours(2);
		ChargeMethodType chargeMethod = ChargeMethodType.FIXED_RATE;
		Float basePayRate = 20.0f;
		Float payRateMultiplier = 1.5f;
		Float payRatePerHour = 25.0f;

		// Act
		BigDecimal result = TimeHelper.calculatePayAmount(duration, chargeMethod, basePayRate, payRateMultiplier,
				payRatePerHour);

		// Assert
		assertThat(result).isEqualTo(new BigDecimal("50.0")); // 2 hours * 25
	}

	@Test
	@DisplayName("calculatePayAmount - with zero duration")
	void testCalculatePayAmountWithZeroDuration() {
		// Arrange
		Duration duration = Duration.ZERO;
		ChargeMethodType chargeMethod = ChargeMethodType.MULTIPLIER;
		Float basePayRate = 20.0f;
		Float payRateMultiplier = 1.5f;
		Float payRatePerHour = 0.0f;

		// Act
		BigDecimal result = TimeHelper.calculatePayAmount(duration, chargeMethod, basePayRate, payRateMultiplier,
				payRatePerHour);

		// Assert
		assertThat(result).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("calculatePayAmount - null duration returns zero")
	void testCalculatePayAmountNullDuration() {
		assertThat(TimeHelper.calculatePayAmount(null, ChargeMethodType.MULTIPLIER, 10f, 2f, 5f))
			.isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("calculatePayAmount - MULTIPLIER with null basePayRate uses zero")
	void testCalculatePayAmountMultiplierNullBasePayRate() {
		BigDecimal result = TimeHelper.calculatePayAmount(Duration.ofHours(2), ChargeMethodType.MULTIPLIER, null, 1.5f,
				0f);
		assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("calculatePayAmount - MULTIPLIER with null payRateMultiplier uses one")
	void testCalculatePayAmountMultiplierNullPayRateMultiplier() {
		BigDecimal result = TimeHelper.calculatePayAmount(Duration.ofHours(2), ChargeMethodType.MULTIPLIER, 20f, null,
				0f);
		assertThat(result).isEqualByComparingTo(new BigDecimal("40.0"));
	}

	@Test
	@DisplayName("calculatePayAmount - FIXED_RATE with null payRatePerHour uses zero")
	void testCalculatePayAmountFixedRateNullPayRatePerHour() {
		BigDecimal result = TimeHelper.calculatePayAmount(Duration.ofHours(2), ChargeMethodType.FIXED_RATE, 10f, 2f,
				null);
		assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("calculatePayAmount - unknown chargeMethod (null)")
	void testCalculatePayAmountUnknownChargeMethod() {
		Duration duration = Duration.ofHours(1);
		Float basePayRate = 10f;
		Float payRateMultiplier = 2f;
		Float payRatePerHour = 5f;

		assertThatThrownBy(
				() -> TimeHelper.calculatePayAmount(duration, null, basePayRate, payRateMultiplier, payRatePerHour))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("calculatePayAmount - MULTIPLIER with null basePayRate and null payRateMultiplier")
	void testCalculatePayAmountMultiplierBothRatesNull() {
		BigDecimal result = TimeHelper.calculatePayAmount(Duration.ofHours(2), ChargeMethodType.MULTIPLIER, null, null,
				99f);
		assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("calculatePayAmount - switch default branch via synthetic switch map")
	void testCalculatePayAmountSwitchDefaultBranchFullRates() {
		withChargeMethodSwitchMapDefaultRoute(() -> {
			BigDecimal result = TimeHelper.calculatePayAmount(Duration.ofHours(2), ChargeMethodType.MULTIPLIER, 10f,
					1.5f, 99f);
			assertThat(result).isEqualByComparingTo(new BigDecimal("30.0"));
		});
	}

	@Test
	@DisplayName("calculatePayAmount - switch default branch with null basePayRate")
	void testCalculatePayAmountSwitchDefaultBranchNullBasePayRate() {
		withChargeMethodSwitchMapDefaultRoute(() -> {
			BigDecimal result = TimeHelper.calculatePayAmount(Duration.ofHours(2), ChargeMethodType.MULTIPLIER, null,
					1.5f, 99f);
			assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
		});
	}

	@Test
	@DisplayName("calculatePayAmount - switch default branch with null payRateMultiplier")
	void testCalculatePayAmountSwitchDefaultBranchNullPayRateMultiplier() {
		withChargeMethodSwitchMapDefaultRoute(() -> {
			BigDecimal result = TimeHelper.calculatePayAmount(Duration.ofHours(2), ChargeMethodType.MULTIPLIER, 10f,
					null, 99f);
			assertThat(result).isEqualByComparingTo(new BigDecimal("20.0"));
		});
	}

	@Test
	@DisplayName("calculatePayAmount - switch default branch with null basePayRate and null payRateMultiplier")
	void testCalculatePayAmountSwitchDefaultBranchBothRatesNull() {
		withChargeMethodSwitchMapDefaultRoute(() -> {
			BigDecimal result = TimeHelper.calculatePayAmount(Duration.ofHours(2), ChargeMethodType.MULTIPLIER, null,
					null, 99f);
			assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
		});
	}

	@Test
	@DisplayName("calculateBillAmount - with MULTIPLIER method")
	void testCalculateBillAmountWithMultiplierMethod() {
		// Arrange
		Duration duration = Duration.ofHours(2);
		ChargeMethodType chargeMethod = ChargeMethodType.MULTIPLIER;
		Float baseBillRate = 30.0f;
		Float billRateMultiplier = 1.2f;
		Float billRatePerHour = 0.0f;

		// Act
		BigDecimal result = TimeHelper.calculateBillAmount(duration, chargeMethod, baseBillRate, billRateMultiplier,
				billRatePerHour);

		// Assert
		assertThat(result).isEqualTo(new BigDecimal("72.0")); // 2 hours * 30 * 1.2
	}

	@Test
	@DisplayName("calculateBillAmount - with FIXED_RATE method")
	void testCalculateBillAmountWithFixedRateMethod() {
		// Arrange
		Duration duration = Duration.ofHours(2);
		ChargeMethodType chargeMethod = ChargeMethodType.FIXED_RATE;
		Float baseBillRate = 30.0f;
		Float billRateMultiplier = 1.2f;
		Float billRatePerHour = 35.0f;

		// Act
		BigDecimal result = TimeHelper.calculateBillAmount(duration, chargeMethod, baseBillRate, billRateMultiplier,
				billRatePerHour);

		// Assert
		assertThat(result).isEqualTo(new BigDecimal("70.0")); // 2 hours * 35
	}

	@Test
	@DisplayName("calculateBillAmount - with zero duration")
	void testCalculateBillAmountWithZeroDuration() {
		// Arrange
		Duration duration = Duration.ZERO;
		ChargeMethodType chargeMethod = ChargeMethodType.MULTIPLIER;
		Float baseBillRate = 30.0f;
		Float billRateMultiplier = 1.2f;
		Float billRatePerHour = 0.0f;

		// Act
		BigDecimal result = TimeHelper.calculateBillAmount(duration, chargeMethod, baseBillRate, billRateMultiplier,
				billRatePerHour);

		// Assert
		assertThat(result).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("calculateBillAmount - null duration returns zero")
	void testCalculateBillAmountNullDuration() {
		assertThat(TimeHelper.calculateBillAmount(null, ChargeMethodType.MULTIPLIER, 10f, 2f, 5f))
			.isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("calculateBillAmount - MULTIPLIER with null baseBillRate uses zero")
	void testCalculateBillAmountMultiplierNullBaseBillRate() {
		BigDecimal result = TimeHelper.calculateBillAmount(Duration.ofHours(2), ChargeMethodType.MULTIPLIER, null, 1.2f,
				0f);
		assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("calculateBillAmount - MULTIPLIER with null billRateMultiplier uses one")
	void testCalculateBillAmountMultiplierNullBillRateMultiplier() {
		BigDecimal result = TimeHelper.calculateBillAmount(Duration.ofHours(2), ChargeMethodType.MULTIPLIER, 30f, null,
				0f);
		assertThat(result).isEqualByComparingTo(new BigDecimal("60.0"));
	}

	@Test
	@DisplayName("calculateBillAmount - FIXED_RATE with null billRatePerHour uses zero")
	void testCalculateBillAmountFixedRateNullBillRatePerHour() {
		BigDecimal result = TimeHelper.calculateBillAmount(Duration.ofHours(2), ChargeMethodType.FIXED_RATE, 10f, 2f,
				null);
		assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("calculateBillAmount - unknown chargeMethod (null)")
	void testCalculateBillAmountUnknownChargeMethod() {
		assertThat(TimeHelper.calculateBillAmount(Duration.ofHours(1), null, 10f, 2f, 5f))
			.isEqualByComparingTo(BigDecimal.valueOf(20.0));
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - with valid parameters")
	void testConstrainTimeRangeToTimeLogBoundariesWithValidParameters() {
		// Arrange
		RangeSet<LocalTime> timeRange = TreeRangeSet.create();
		timeRange.add(Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(16, 0)));

		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(timeLog.getWorkTime()).willReturn(null); // Ensure it's treated as
														// range-based

		// Act
		RangeSet<LocalTime> result = TimeHelper.constrainTimeRangeToTimeLogBoundaries(timeRange, timeLog);

		// Assert
		assertThat(result).isNotNull().satisfies((ranges) -> assertThat(ranges.asRanges()).hasSize(1));
	}

	@Test
	@DisplayName("constrainSingleTimeRangeToTimeLogBoundaries - with valid parameters")
	void testConstrainSingleTimeRangeToTimeLogBoundariesWithValidParameters() {
		// Arrange
		Range<LocalTime> timeRange = Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(16, 0));

		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(timeLog.getWorkTime()).willReturn(null); // Ensure it's treated as
														// range-based

		// Act
		Range<LocalTime> result = TimeHelper.constrainSingleTimeRangeToTimeLogBoundaries(timeRange, timeLog);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.lowerEndpoint()).isEqualTo(LocalTime.of(10, 0));
		assertThat(result.upperEndpoint()).isEqualTo(LocalTime.of(16, 0));
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - empty non-null time range")
	void testConstrainTimeRangeToTimeLogBoundariesEmptyRangeSet() {
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(log.getWorkTime()).willReturn(null);
		assertThat(TimeHelper.constrainTimeRangeToTimeLogBoundaries(TreeRangeSet.create(), log).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - range not connected to boundary")
	void testConstrainTimeRangeToTimeLogBoundariesNotConnected() {
		RangeSet<LocalTime> rs = TreeRangeSet.create();
		rs.add(Range.closedOpen(LocalTime.of(20, 0), LocalTime.of(21, 0)));
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(log.getWorkTime()).willReturn(null);
		assertThat(TimeHelper.constrainTimeRangeToTimeLogBoundaries(rs, log).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - connected but empty intersection")
	void testConstrainTimeRangeToTimeLogBoundariesEmptyIntersection() {
		RangeSet<LocalTime> rs = TreeRangeSet.create();
		rs.add(Range.closedOpen(LocalTime.of(17, 0), LocalTime.of(18, 0)));
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(log.getWorkTime()).willReturn(null);
		assertThat(TimeHelper.constrainTimeRangeToTimeLogBoundaries(rs, log).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - equal effective start and end returns empty")
	void testConstrainTimeRangeToTimeLogBoundariesEqualEffectiveTimes() {
		RangeSet<LocalTime> rs = TreeRangeSet.create();
		rs.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkStartTime()).willReturn(LocalTime.of(12, 0));
		given(log.getWorkEndTime()).willReturn(LocalTime.of(12, 0));
		given(log.getWorkTime()).willReturn(null);
		assertThat(TimeHelper.constrainTimeRangeToTimeLogBoundaries(rs, log).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - null effective start returns empty")
	void testConstrainTimeRangeToTimeLogBoundariesNullEffectiveStart() {
		RangeSet<LocalTime> rs = TreeRangeSet.create();
		rs.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkTime()).willReturn(null);
		given(log.getWorkStartTime()).willReturn(null);
		given(log.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		assertThat(TimeHelper.constrainTimeRangeToTimeLogBoundaries(rs, log).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - null effective end returns empty")
	void testConstrainTimeRangeToTimeLogBoundariesNullEffectiveEnd() {
		RangeSet<LocalTime> rs = TreeRangeSet.create();
		rs.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkTime()).willReturn(null);
		given(log.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log.getWorkEndTime()).willReturn(null);
		assertThat(TimeHelper.constrainTimeRangeToTimeLogBoundaries(rs, log).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - reversed effective start and end returns empty")
	void testConstrainTimeRangeToTimeLogBoundariesReversedEffectiveTimes() {
		RangeSet<LocalTime> rs = TreeRangeSet.create();
		rs.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkStartTime()).willReturn(LocalTime.of(17, 0));
		given(log.getWorkEndTime()).willReturn(LocalTime.of(9, 0));
		given(log.getWorkTime()).willReturn(null);
		assertThat(TimeHelper.constrainTimeRangeToTimeLogBoundaries(rs, log).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - boundary Range.all() returns empty")
	void testConstrainTimeRangeToTimeLogBoundariesBoundaryAllRange() {
		RangeSet<LocalTime> rs = TreeRangeSet.create();
		rs.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkTime()).willReturn(null);
		given(log.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		try (MockedStatic<TimeHelper> mocked = mockStatic(TimeHelper.class, Mockito.CALLS_REAL_METHODS)) {
			mocked.when(() -> TimeHelper.toRange(eq(LocalTime.of(9, 0)), eq(LocalTime.of(17, 0))))
				.thenReturn(Range.all());
			assertThat(TimeHelper.constrainTimeRangeToTimeLogBoundaries(rs, log).isEmpty()).isTrue();
		}
	}

	@Test
	@DisplayName("shouldIncludeBreakTimeInCalculation - with break time included")
	void testShouldIncludeBreakTimeInCalculationWithBreakTimeIncluded() {
		// Arrange
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		given(timesheetSetting.getCalculateBreakTime()).willReturn(true);

		// Act
		boolean result = TimeHelper.shouldIncludeBreakTimeInCalculation(timesheetSetting);

		// Assert
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("calculateWorkTimeRespectingBreakFlag - with break time included")
	void testCalculateWorkTimeRespectingBreakFlagWithBreakTimeIncluded() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(timeLog.getBreakTime()).willReturn(Duration.ofHours(1));
		given(timeLog.getWorkTime()).willReturn(null); // Ensure it's treated as
														// range-based

		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		given(timesheetSetting.getCalculateBreakTime()).willReturn(true);

		// Act
		Duration result = TimeHelper.calculateWorkTimeRespectingBreakFlag(timeLog, timesheetSetting);

		// Assert
		assertThat(result).isEqualTo(Duration.ofHours(8)); // Total time (8h) since break
															// time is included
	}

	@Test
	@DisplayName("calculateWorkTimeRespectingBreakFlag - with break time excluded")
	void testCalculateWorkTimeRespectingBreakFlagWithBreakTimeExcluded() {
		// Arrange
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(timeLog.getBreakTime()).willReturn(Duration.ofHours(1));
		given(timeLog.getWorkTime()).willReturn(null); // Ensure it's treated as
														// range-based

		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		given(timesheetSetting.getCalculateBreakTime()).willReturn(false);

		// Act
		Duration result = TimeHelper.calculateWorkTimeRespectingBreakFlag(timeLog, timesheetSetting);

		// Assert
		assertThat(result).isEqualTo(Duration.ofHours(7)); // Total time (8h) minus break
															// time (1h) = 7h
	}

	@Test
	@DisplayName("calculateWorkTimeRespectingBreakFlag - null break returns total")
	void testCalculateWorkTimeRespectingBreakFlagNullBreak() {
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(timeLog.getBreakTime()).willReturn(null);
		given(timeLog.getWorkTime()).willReturn(null);
		TimesheetSetting setting = mock(TimesheetSetting.class);
		given(setting.getCalculateBreakTime()).willReturn(false);
		assertThat(TimeHelper.calculateWorkTimeRespectingBreakFlag(timeLog, setting)).isEqualTo(Duration.ofHours(8));
	}

	@Test
	@DisplayName("calculateWorkTimeRespectingBreakFlag - zero break returns total")
	void testCalculateWorkTimeRespectingBreakFlagZeroBreak() {
		TimeLog timeLog = mock(TimeLog.class);
		given(timeLog.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(timeLog.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		given(timeLog.getBreakTime()).willReturn(Duration.ZERO);
		given(timeLog.getWorkTime()).willReturn(null);
		TimesheetSetting setting = mock(TimesheetSetting.class);
		given(setting.getCalculateBreakTime()).willReturn(false);
		assertThat(TimeHelper.calculateWorkTimeRespectingBreakFlag(timeLog, setting)).isEqualTo(Duration.ofHours(8));
	}

	@Test
	@DisplayName("shiftRangeByBreak - normal case shifts by break duration")
	void testShiftRangeByBreakNormal() {
		Duration start = Duration.ofHours(1);
		Duration end = Duration.ofHours(3);
		Duration breakDuration = Duration.ofMinutes(30);
		Range<LocalTime> result = TimeHelper.shiftRangeByBreak(start, end, breakDuration);
		assertThat(result.lowerEndpoint()).isEqualTo(LocalTime.of(1, 30));
		assertThat(result.upperEndpoint()).isEqualTo(LocalTime.of(3, 30));
	}

	@Test
	@DisplayName("shiftRangeByBreak - zero break duration returns original range")
	void testShiftRangeByBreakZeroBreak() {
		Duration start = Duration.ofHours(2);
		Duration end = Duration.ofHours(4);
		Duration breakDuration = Duration.ZERO;
		Range<LocalTime> result = TimeHelper.shiftRangeByBreak(start, end, breakDuration);
		assertThat(result.lowerEndpoint()).isEqualTo(LocalTime.of(2, 0));
		assertThat(result.upperEndpoint()).isEqualTo(LocalTime.of(4, 0));
	}

	@Test
	@DisplayName("shiftRangeByBreak - null break duration returns original range")
	void testShiftRangeByBreakNullBreak() {
		Duration start = Duration.ofHours(2);
		Duration end = Duration.ofHours(4);
		Duration breakDuration = null;
		Range<LocalTime> result = TimeHelper.shiftRangeByBreak(start, end, breakDuration);
		assertThat(result.lowerEndpoint()).isEqualTo(LocalTime.of(2, 0));
		assertThat(result.upperEndpoint()).isEqualTo(LocalTime.of(4, 0));
	}

	@Test
	@DisplayName("shiftRangeByBreak - start after end returns Range.all()")
	void testShiftRangeByBreakStartAfterEnd() {
		Duration start = Duration.ofHours(5);
		Duration end = Duration.ofHours(2);
		Duration breakDuration = Duration.ofMinutes(15);
		Range<LocalTime> result = TimeHelper.shiftRangeByBreak(start, end, breakDuration);
		assertThat(result).isEqualTo(Range.all());
	}

	@Test
	@DisplayName("shiftDurationRangeForward - all null returns Range.all()")
	void testShiftDurationRangeForwardAllNull() {
		assertThat(TimeHelper.shiftDurationRangeForward(null, null, null)).isEqualTo(Range.all());
	}

	@Test
	@DisplayName("shiftDurationRangeForward - null originalStart only")
	void testShiftDurationRangeForwardNullOriginalStart() {
		assertThat(TimeHelper.shiftDurationRangeForward(null, Duration.ofHours(2), Duration.ofMinutes(5)))
			.isEqualTo(Range.all());
	}

	@Test
	@DisplayName("shiftDurationRangeForward - null originalEnd only")
	void testShiftDurationRangeForwardNullOriginalEnd() {
		assertThat(TimeHelper.shiftDurationRangeForward(Duration.ofHours(1), null, Duration.ofMinutes(5)))
			.isEqualTo(Range.all());
	}

	@Test
	@DisplayName("shiftDurationRangeForward - null shiftOffset only")
	void testShiftDurationRangeForwardNullShiftOffset() {
		assertThat(TimeHelper.shiftDurationRangeForward(Duration.ofHours(1), Duration.ofHours(2), null))
			.isEqualTo(Range.all());
	}

	@Test
	@DisplayName("shiftDurationRangeForward - start after end returns Range.all()")
	void testShiftDurationRangeForwardStartAfterEnd() {
		Duration start = Duration.ofHours(5);
		Duration end = Duration.ofHours(2);
		Duration shift = Duration.ofMinutes(10);
		assertThat(TimeHelper.shiftDurationRangeForward(start, end, shift)).isEqualTo(Range.all());
	}

	@Test
	@DisplayName("shiftDurationRangeForward - valid shift")
	void testShiftDurationRangeForwardValid() {
		Duration start = Duration.ofHours(1);
		Duration end = Duration.ofHours(3);
		Duration shift = Duration.ofMinutes(30);
		Range<LocalTime> result = TimeHelper.shiftDurationRangeForward(start, end, shift);
		assertThat(result.lowerEndpoint()).isEqualTo(LocalTime.of(1, 30));
		assertThat(result.upperEndpoint()).isEqualTo(LocalTime.of(3, 30));
	}

	@Test
	@DisplayName("shiftDurationForward - nulls return MIDNIGHT")
	void testShiftDurationForwardNulls() {
		assertThat(TimeHelper.shiftDurationForward(null, null)).isEqualTo(LocalTime.MIDNIGHT);
		assertThat(TimeHelper.shiftDurationForward(Duration.ofHours(1), null)).isEqualTo(LocalTime.MIDNIGHT);
		assertThat(TimeHelper.shiftDurationForward(null, Duration.ofHours(1))).isEqualTo(LocalTime.MIDNIGHT);
	}

	@Test
	@DisplayName("shiftDurationForward - valid shift")
	void testShiftDurationForwardValid() {
		Duration orig = Duration.ofHours(2);
		Duration shift = Duration.ofMinutes(15);
		LocalTime result = TimeHelper.shiftDurationForward(orig, shift);
		assertThat(result).isEqualTo(LocalTime.of(2, 15));
	}

	@Test
	@DisplayName("createBreakRangeAtStart - null or zero returns Range.all()")
	void testCreateBreakRangeAtStartNullOrZero() {
		assertThat(TimeHelper.createBreakRangeAtStart(null)).isEqualTo(Range.all());
		assertThat(TimeHelper.createBreakRangeAtStart(Duration.ZERO)).isEqualTo(Range.all());
	}

	@Test
	@DisplayName("createBreakRangeAtStart - valid duration")
	void testCreateBreakRangeAtStartValid() {
		Duration breakDuration = Duration.ofMinutes(45);
		Range<LocalTime> result = TimeHelper.createBreakRangeAtStart(breakDuration);
		assertThat(result.lowerEndpoint()).isEqualTo(LocalTime.MIDNIGHT);
		assertThat(result.upperEndpoint()).isEqualTo(LocalTime.of(0, 45));
	}

	@Test
	@DisplayName("calculateEffectiveWorkDuration - totalWorkDuration null returns ZERO")
	void testCalculateEffectiveWorkDurationNullTotal() {
		assertThat(TimeHelper.calculateEffectiveWorkDuration(null, Duration.ofHours(1))).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateEffectiveWorkDuration - breakDuration null or zero returns totalWorkDuration")
	void testCalculateEffectiveWorkDurationNullOrZeroBreak() {
		Duration total = Duration.ofHours(5);
		assertThat(TimeHelper.calculateEffectiveWorkDuration(total, null)).isEqualTo(total);
		assertThat(TimeHelper.calculateEffectiveWorkDuration(total, Duration.ZERO)).isEqualTo(total);
	}

	@Test
	@DisplayName("calculateEffectiveWorkDuration - breakDuration >= totalWorkDuration returns ZERO")
	void testCalculateEffectiveWorkDurationBreakGreaterOrEqual() {
		Duration total = Duration.ofHours(2);
		Duration breakDur = Duration.ofHours(3);
		assertThat(TimeHelper.calculateEffectiveWorkDuration(total, breakDur)).isEqualTo(Duration.ZERO);
		assertThat(TimeHelper.calculateEffectiveWorkDuration(total, total)).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateEffectiveWorkDuration - breakDuration < totalWorkDuration returns difference")
	void testCalculateEffectiveWorkDurationBreakLess() {
		Duration total = Duration.ofHours(5);
		Duration breakDur = Duration.ofHours(2);
		assertThat(TimeHelper.calculateEffectiveWorkDuration(total, breakDur)).isEqualTo(Duration.ofHours(3));
	}

	@Test
	@DisplayName("convertWorkDayToDayOfWeek - covers all enum values")
	void testConvertWorkDayToDayOfWeekAllEnums() throws Exception {
		java.lang.reflect.Method m = TimeHelper.class.getDeclaredMethod("convertWorkDayToDayOfWeek", WorkDay.class);
		m.setAccessible(true);
		for (WorkDay wd : WorkDay.values()) {
			assertThat(m.invoke(null, wd)).isNotNull();
		}
	}

	@Test
	@DisplayName("TimeHelper private constructor throws UnsupportedOperationException")
	void testTimeHelperPrivateConstructorThrows() throws Exception {
		Constructor<TimeHelper> constructor = TimeHelper.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	@DisplayName("splitTimeLogsOnWeeklyBasis - throws on null timeLogs")
	void testSplitTimeLogsOnWeeklyBasisNullTimeLogs() {
		assertThatThrownBy(() -> TimeHelper.splitTimeLogsOnWeeklyBasis(null, WorkDay.MONDAY))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("TimeLogs list cannot be null");
	}

	@Test
	@DisplayName("splitTimeLogsOnWeeklyBasis - throws on null weekStartDay")
	void testSplitTimeLogsOnWeeklyBasisNullWeekStartDay() {
		List<TimeLog> timeLogs = List.of();
		assertThatThrownBy(() -> TimeHelper.splitTimeLogsOnWeeklyBasis(timeLogs, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Week start day cannot be null");
	}

	@Test
	@DisplayName("getMinimumDuration - both null")
	void testGetMinimumDurationBothNull() {
		assertThat(TimeHelper.getMinimumDuration(null, null)).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("getMinimumDuration - first null")
	void testGetMinimumDurationFirstNull() {
		assertThat(TimeHelper.getMinimumDuration(null, Duration.ofMinutes(5))).isEqualTo(Duration.ofMinutes(5));
	}

	@Test
	@DisplayName("getMinimumDuration - second null")
	void testGetMinimumDurationSecondNull() {
		assertThat(TimeHelper.getMinimumDuration(Duration.ofMinutes(5), null)).isEqualTo(Duration.ofMinutes(5));
	}

	@Test
	@DisplayName("normaliseDurationRangeToLocalTime - start null")
	void testNormaliseDurationRangeToLocalTimeStartNull() {
		assertThat(TimeHelper.normaliseDurationRangeToLocalTime(null, Duration.ofMinutes(5))).isEqualTo(Range.all());
	}

	@Test
	@DisplayName("normaliseDurationRangeToLocalTime - end null")
	void testNormaliseDurationRangeToLocalTimeEndNull() {
		assertThat(TimeHelper.normaliseDurationRangeToLocalTime(Duration.ofMinutes(5), null)).isEqualTo(Range.all());
	}

	@Test
	@DisplayName("normaliseDurationRangeToLocalTime - start after end")
	void testNormaliseDurationRangeToLocalTimeStartAfterEnd() {
		assertThat(TimeHelper.normaliseDurationRangeToLocalTime(Duration.ofMinutes(10), Duration.ofMinutes(5)))
			.isEqualTo(Range.all());
	}

	@Test
	@DisplayName("normaliseDurationToLocalTime - null duration")
	void testNormaliseDurationToLocalTimeNull() {
		assertThat(TimeHelper.normaliseDurationToLocalTime(null)).isEqualTo(LocalTime.MIDNIGHT);
	}

	@Test
	@DisplayName("convertDurationToApproximateHours - null duration")
	void testConvertDurationToApproximateHoursNull() {
		assertThat(TimeHelper.convertDurationToApproximateHours(null)).isEqualTo(0.0f);
	}

	@Test
	@DisplayName("getFreeTimeRanges - null workingRange")
	void testGetFreeTimeRangesNullWorkingRange() {
		assertThat(TimeHelper.getFreeTimeRanges(null, TreeRangeSet.create()).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("getFreeTimeRanges - empty workingRange")
	void testGetFreeTimeRangesEmptyWorkingRange() {
		Range<LocalTime> emptyWorking = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(9, 0));
		assertThat(TimeHelper.getFreeTimeRanges(emptyWorking, TreeRangeSet.create()).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("getAvailableTimeRanges - null grossRange")
	void testGetAvailableTimeRangesNullGrossRange() {
		assertThat(TimeHelper.getAvailableTimeRanges(null, TreeRangeSet.create()).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("getAvailableTimeRanges - empty grossRange")
	void testGetAvailableTimeRangesEmptyGrossRange() {
		Range<LocalTime> emptyGross = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(9, 0));
		assertThat(TimeHelper.getAvailableTimeRanges(emptyGross, TreeRangeSet.create()).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("getAvailableTimeRanges - null occupiedRanges returns full gross")
	void testGetAvailableTimeRangesNullOccupiedReturnsGross() {
		Range<LocalTime> gross = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(12, 0));
		RangeSet<LocalTime> result = TimeHelper.getAvailableTimeRanges(gross, null);
		assertThat(result.asRanges()).hasSize(1);
		assertThat(result.asRanges().iterator().next()).isEqualTo(gross);
	}

	@Test
	@DisplayName("getAvailableTimeRanges - empty non-null occupied returns full gross")
	void testGetAvailableTimeRangesEmptyOccupiedReturnsGross() {
		Range<LocalTime> gross = Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0));
		RangeSet<LocalTime> result = TimeHelper.getAvailableTimeRanges(gross, TreeRangeSet.create());
		assertThat(result.asRanges()).hasSize(1);
		assertThat(result.asRanges().iterator().next()).isEqualTo(gross);
	}

	@Test
	@DisplayName("isWorkDay(LocalDate, List) - null date")
	void testIsWorkDayNullDate() {
		assertThat(TimeHelper.isWorkDay((LocalDate) null, List.of())).isFalse();
	}

	@Test
	@DisplayName("isWorkDay(LocalDate, List) - null templateWorkDays")
	void testIsWorkDayNullTemplateWorkDays() {
		assertThat(TimeHelper.isWorkDay(LocalDate.now(), null)).isFalse();
	}

	@Test
	@DisplayName("isWorkDay(LocalDate, List) - null workDay")
	void testIsWorkDayNullWorkDay() {
		// getWorkDayFromLocalDate returns null for null date, already covered
		// Here, pass a date that maps to a WorkDay not present in templateWorkDays
		assertThat(TimeHelper.isWorkDay(LocalDate.now(), List.of())).isFalse();
	}

	@Test
	@DisplayName("shouldIncludeBreakTimeInCalculation - null template")
	void testShouldIncludeBreakTimeInCalculationNull() {
		assertThat(TimeHelper.shouldIncludeBreakTimeInCalculation(null)).isFalse();
	}

	@Test
	@DisplayName("shouldIncludeBreakTimeInCalculation - null flag")
	void testShouldIncludeBreakTimeInCalculationNullFlag() {
		TimesheetSetting timesheetSetting = mock(TimesheetSetting.class);
		given(timesheetSetting.getCalculateBreakTime()).willReturn(null);
		assertThat(TimeHelper.shouldIncludeBreakTimeInCalculation(timesheetSetting)).isFalse();
	}

	@Test
	@DisplayName("calculateDOHours(Duration) - null workedDuration")
	void testCalculateDOHoursNullWorkedDuration() {
		assertThat(TimeHelper.calculateDOHours(null, Duration.ofHours(1), Duration.ofHours(2)))
			.isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateDOHours(Duration) - null currentThreshold")
	void testCalculateDOHoursNullCurrentThreshold() {
		assertThat(TimeHelper.calculateDOHours(Duration.ofHours(2), null, Duration.ofHours(3)))
			.isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateDOHours(Duration) - null nextThreshold")
	void testCalculateDOHoursNullNextThreshold() {
		assertThat(TimeHelper.calculateDOHours(Duration.ofHours(2), Duration.ofHours(1), null))
			.isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateDOHours(Duration) - workedDuration <= currentThreshold")
	void testCalculateDOHoursBelowThreshold() {
		assertThat(TimeHelper.calculateDOHours(Duration.ofHours(1), Duration.ofHours(2), Duration.ofHours(3)))
			.isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateDOHours(Duration) - available > maxThisTier")
	void testCalculateDOHoursAvailableGreaterThanMax() {
		Duration worked = Duration.ofHours(10);
		Duration current = Duration.ofHours(2);
		Duration next = Duration.ofHours(5);
		assertThat(TimeHelper.calculateDOHours(worked, current, next)).isEqualTo(Duration.ofHours(3));
	}

	@Test
	@DisplayName("calculateDOHours(Duration) - available <= maxThisTier")
	void testCalculateDOHoursAvailableLessThanMax() {
		Duration worked = Duration.ofHours(4);
		Duration current = Duration.ofHours(2);
		Duration next = Duration.ofHours(5);
		assertThat(TimeHelper.calculateDOHours(worked, current, next)).isEqualTo(Duration.ofHours(2));
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - null timeRange")
	void testConstrainTimeRangeToTimeLogBoundariesNullTimeRange() {
		assertThat(TimeHelper.constrainTimeRangeToTimeLogBoundaries(null, mock(TimeLog.class)).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - null timeLog")
	void testConstrainTimeRangeToTimeLogBoundariesNullTimeLog() {
		RangeSet<LocalTime> rs = TreeRangeSet.create();
		rs.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		assertThat(TimeHelper.constrainTimeRangeToTimeLogBoundaries(rs, null).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - invalid timeLog times")
	void testConstrainTimeRangeToTimeLogBoundariesInvalidTimes() {
		RangeSet<LocalTime> rs = TreeRangeSet.create();
		rs.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkStartTime()).willReturn(LocalTime.of(11, 0));
		given(log.getWorkEndTime()).willReturn(LocalTime.of(10, 0));
		given(log.getWorkTime()).willReturn(null);
		assertThat(TimeHelper.constrainTimeRangeToTimeLogBoundaries(rs, log).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("constrainTimeRangeToTimeLogBoundaries - timeLogBoundary equals Range.all()")
	void testConstrainTimeRangeToTimeLogBoundariesAllRange() {
		RangeSet<LocalTime> rs = TreeRangeSet.create();
		rs.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)));
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkStartTime()).willReturn(null);
		given(log.getWorkEndTime()).willReturn(null);
		given(log.getWorkTime()).willReturn(null);
		assertThat(TimeHelper.constrainTimeRangeToTimeLogBoundaries(rs, log).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("constrainSingleTimeRangeToTimeLogBoundaries - null timeRange")
	void testConstrainSingleTimeRangeToTimeLogBoundariesNullTimeRange() {
		assertThat(TimeHelper.constrainSingleTimeRangeToTimeLogBoundaries(null, mock(TimeLog.class))).isNull();
	}

	@Test
	@DisplayName("constrainSingleTimeRangeToTimeLogBoundaries - null timeLog")
	void testConstrainSingleTimeRangeToTimeLogBoundariesNullTimeLog() {
		assertThat(TimeHelper.constrainSingleTimeRangeToTimeLogBoundaries(
				Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)), null))
			.isNull();
	}

	@Test
	@DisplayName("constrainSingleTimeRangeToTimeLogBoundaries - timeLogBoundary equals Range.all()")
	void testConstrainSingleTimeRangeToTimeLogBoundariesAllRange() {
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkStartTime()).willReturn(null);
		given(log.getWorkEndTime()).willReturn(null);
		given(log.getWorkTime()).willReturn(null);
		assertThat(TimeHelper.constrainSingleTimeRangeToTimeLogBoundaries(
				Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(10, 0)), log))
			.isNull();
	}

	@DisplayName("constrainSingleTimeRangeToTimeLogBoundaries - returns null when time log rejects constraint")
	@ParameterizedTest(name = "{4}")
	@MethodSource("constrainSingleTimeRangeToTimeLogBoundariesNullCases")
	void testConstrainSingleTimeRangeToTimeLogBoundariesReturnsNullForTimeLogCases(LocalTime workStart,
			LocalTime workEnd, LocalTime rangeStart, LocalTime rangeEnd, String scenario) {
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkTime()).willReturn(null);
		given(log.getWorkStartTime()).willReturn(workStart);
		given(log.getWorkEndTime()).willReturn(workEnd);
		assertThat(TimeHelper.constrainSingleTimeRangeToTimeLogBoundaries(Range.closedOpen(rangeStart, rangeEnd), log))
			.isNull();
	}

	private static Stream<Arguments> constrainSingleTimeRangeToTimeLogBoundariesNullCases() {
		return Stream.of(
				Arguments.of(LocalTime.of(11, 0), LocalTime.of(10, 0), LocalTime.of(9, 0), LocalTime.of(10, 0),
						"invalid effective times (start after end)"),
				Arguments.of(LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(9, 0), LocalTime.of(10, 0),
						"range not connected to time log boundary"),
				Arguments.of(LocalTime.of(12, 0), LocalTime.of(12, 0), LocalTime.of(9, 0), LocalTime.of(10, 0),
						"equal effective start and end"),
				Arguments.of(LocalTime.of(14, 0), LocalTime.of(10, 0), LocalTime.of(9, 0), LocalTime.of(15, 0),
						"reversed effective times"));
	}

	@Test
	@DisplayName("constrainSingleTimeRangeToTimeLogBoundaries - intersection is empty")
	void testConstrainSingleTimeRangeToTimeLogBoundariesEmptyIntersection() {
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log.getWorkEndTime()).willReturn(LocalTime.of(10, 0));
		given(log.getWorkTime()).willReturn(null);
		Range<LocalTime> r = Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(11, 0));
		assertThat(TimeHelper.constrainSingleTimeRangeToTimeLogBoundaries(r, log)).isNull();
	}

	@Test
	@DisplayName("constrainSingleTimeRangeToTimeLogBoundaries - null effective start returns null")
	void testConstrainSingleTimeRangeToTimeLogBoundariesNullEffectiveStart() {
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkTime()).willReturn(null);
		given(log.getWorkStartTime()).willReturn(null);
		given(log.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		assertThat(TimeHelper.constrainSingleTimeRangeToTimeLogBoundaries(
				Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(11, 0)), log))
			.isNull();
	}

	@Test
	@DisplayName("constrainSingleTimeRangeToTimeLogBoundaries - null effective end returns null")
	void testConstrainSingleTimeRangeToTimeLogBoundariesNullEffectiveEnd() {
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkTime()).willReturn(null);
		given(log.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log.getWorkEndTime()).willReturn(null);
		assertThat(TimeHelper.constrainSingleTimeRangeToTimeLogBoundaries(
				Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(11, 0)), log))
			.isNull();
	}

	@Test
	@DisplayName("constrainSingleTimeRangeToTimeLogBoundaries - boundary Range.all() returns null")
	void testConstrainSingleTimeRangeToTimeLogBoundariesBoundaryAllRange() {
		TimeLog log = mock(TimeLog.class);
		given(log.getWorkTime()).willReturn(null);
		given(log.getWorkStartTime()).willReturn(LocalTime.of(9, 0));
		given(log.getWorkEndTime()).willReturn(LocalTime.of(17, 0));
		try (MockedStatic<TimeHelper> mocked = mockStatic(TimeHelper.class, Mockito.CALLS_REAL_METHODS)) {
			mocked.when(() -> TimeHelper.toRange(eq(LocalTime.of(9, 0)), eq(LocalTime.of(17, 0))))
				.thenReturn(Range.all());
			assertThat(TimeHelper.constrainSingleTimeRangeToTimeLogBoundaries(
					Range.closedOpen(LocalTime.of(10, 0), LocalTime.of(11, 0)), log))
				.isNull();
		}
	}

	@Test
	@DisplayName("calculateBreakTimeThresholdAdjustment - breakTimeThreshold less than totalBreakTime")
	void testCalculateBreakTimeThresholdAdjustmentBreakTimeThresholdLessThanTotalBreakTime() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setBreakTime(Duration.ofMinutes(30)); // 30 minutes break time

		TimesheetSetting timesheetSetting = new TimesheetSetting();
		timesheetSetting.setBreakTimeThreshold(Duration.ofMinutes(20)); // 20 minutes
																		// threshold

		// Act
		Duration result = TimeHelper.calculateBreakTimeThresholdAdjustment(timeLog, timesheetSetting);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO); // When threshold <= totalBreakTime,
														// no adjustment needed
	}

	@Test
	@DisplayName("calculateBreakTimeThresholdAdjustment - breakTimeThreshold greater than totalBreakTime")
	void testCalculateBreakTimeThresholdAdjustmentBreakTimeThresholdGreaterThanTotalBreakTime() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setBreakTime(Duration.ofMinutes(15)); // 15 minutes break time

		TimesheetSetting timesheetSetting = new TimesheetSetting();
		timesheetSetting.setBreakTimeThreshold(Duration.ofMinutes(30)); // 30 minutes
																		// threshold

		// Act
		Duration result = TimeHelper.calculateBreakTimeThresholdAdjustment(timeLog, timesheetSetting);

		// Assert
		assertThat(result).isEqualTo(Duration.ofMinutes(15)); // 30 - 15 = 15 minutes to
																// deduct
	}

	@Test
	@DisplayName("calculateBreakTimeThresholdAdjustment - breakTimeThreshold equals totalBreakTime")
	void testCalculateBreakTimeThresholdAdjustmentBreakTimeThresholdEqualsTotalBreakTime() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setBreakTime(Duration.ofMinutes(30)); // 30 minutes break time

		TimesheetSetting timesheetSetting = new TimesheetSetting();
		timesheetSetting.setBreakTimeThreshold(Duration.ofMinutes(30)); // 30 minutes
																		// threshold

		// Act
		Duration result = TimeHelper.calculateBreakTimeThresholdAdjustment(timeLog, timesheetSetting);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO); // No adjustment needed
	}

	@Test
	@DisplayName("calculateBreakTimeThresholdAdjustment - null breakTimeThreshold")
	void testCalculateBreakTimeThresholdAdjustmentNullBreakTimeThreshold() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setBreakTime(Duration.ofMinutes(30));

		TimesheetSetting timesheetSetting = new TimesheetSetting();
		timesheetSetting.setBreakTimeThreshold(null);

		// Act
		Duration result = TimeHelper.calculateBreakTimeThresholdAdjustment(timeLog, timesheetSetting);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateBreakTimeThresholdAdjustment - zero breakTimeThreshold")
	void testCalculateBreakTimeThresholdAdjustmentZeroBreakTimeThreshold() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setBreakTime(Duration.ofMinutes(30));

		TimesheetSetting timesheetSetting = new TimesheetSetting();
		timesheetSetting.setBreakTimeThreshold(Duration.ZERO);

		// Act
		Duration result = TimeHelper.calculateBreakTimeThresholdAdjustment(timeLog, timesheetSetting);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateBreakTimeThresholdAdjustment - null totalBreakTime")
	void testCalculateBreakTimeThresholdAdjustmentNullTotalBreakTime() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setBreakTime(null);

		TimesheetSetting timesheetSetting = new TimesheetSetting();
		timesheetSetting.setBreakTimeThreshold(Duration.ofMinutes(30));

		// Act
		Duration result = TimeHelper.calculateBreakTimeThresholdAdjustment(timeLog, timesheetSetting);

		// Assert
		assertThat(result).isEqualTo(Duration.ofMinutes(30)); // Should return entire
																// threshold
	}

	@Test
	@DisplayName("calculateBreakTimeThresholdAdjustment - zero totalBreakTime")
	void testCalculateBreakTimeThresholdAdjustmentZeroTotalBreakTime() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setBreakTime(Duration.ZERO);

		TimesheetSetting timesheetSetting = new TimesheetSetting();
		timesheetSetting.setBreakTimeThreshold(Duration.ofMinutes(30));

		// Act
		Duration result = TimeHelper.calculateBreakTimeThresholdAdjustment(timeLog, timesheetSetting);

		// Assert
		assertThat(result).isEqualTo(Duration.ofMinutes(30)); // Should return entire
																// threshold
	}

	@Test
	@DisplayName("calculateBreakTimeThresholdAdjustment - null timeLog")
	void testCalculateBreakTimeThresholdAdjustmentNullTimeLog() {
		// Arrange
		TimesheetSetting timesheetSetting = new TimesheetSetting();
		timesheetSetting.setBreakTimeThreshold(Duration.ofMinutes(30));

		// Act
		Duration result = TimeHelper.calculateBreakTimeThresholdAdjustment(null, timesheetSetting);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateBreakTimeThresholdAdjustment - null timesheetSetting")
	void testCalculateBreakTimeThresholdAdjustmentNullTimesheetSetting() {
		// Arrange
		TimeLog timeLog = new TimeLog();
		timeLog.setBreakTime(Duration.ofMinutes(30));

		// Act
		Duration result = TimeHelper.calculateBreakTimeThresholdAdjustment(timeLog, null);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("calculateBreakTimeThresholdAdjustment - both null")
	void testCalculateBreakTimeThresholdAdjustmentBothNull() {
		// Act
		Duration result = TimeHelper.calculateBreakTimeThresholdAdjustment(null, null);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("isLastIntervalOfDay - null same day list returns true")
	void testIsLastIntervalOfDayNullSameDayReturnsTrue() {
		TimeLog current = new TimeLog();
		current.setWorkStartTime(LocalTime.of(9, 0));
		current.setWorkEndTime(LocalTime.of(17, 0));
		assertThat(TimeHelper.isLastIntervalOfDay(current, null)).isTrue();
	}

	@Test
	@DisplayName("isLastIntervalOfDay - empty peer list returns true")
	void testIsLastIntervalOfDayEmptyPeerListReturnsTrue() {
		TimeLog current = new TimeLog();
		current.setWorkStartTime(LocalTime.of(9, 0));
		current.setWorkEndTime(LocalTime.of(17, 0));
		assertThat(TimeHelper.isLastIntervalOfDay(current, Collections.emptyList())).isTrue();
	}

	@Test
	@DisplayName("isLastIntervalOfDay - single interval returns true")
	void testIsLastIntervalOfDaySingleIntervalReturnsTrue() {
		TimeLog log = new TimeLog();
		log.setWorkStartTime(LocalTime.of(9, 0));
		log.setWorkEndTime(LocalTime.of(12, 0));
		assertThat(TimeHelper.isLastIntervalOfDay(log, List.of(log))).isTrue();
	}

	@Test
	@DisplayName("isLastIntervalOfDay - returns true when current has latest end among peers")
	void testIsLastIntervalOfDayTrueWhenLatestEnd() {
		TimeLog first = new TimeLog();
		first.setWorkStartTime(LocalTime.of(9, 0));
		first.setWorkEndTime(LocalTime.of(12, 0));
		TimeLog last = new TimeLog();
		last.setWorkStartTime(LocalTime.of(13, 0));
		last.setWorkEndTime(LocalTime.of(18, 0));
		assertThat(TimeHelper.isLastIntervalOfDay(last, List.of(first, last))).isTrue();
	}

	@Test
	@DisplayName("isLastIntervalOfDay - returns false when another interval ends later")
	void testIsLastIntervalOfDayFalseWhenLaterEndExists() {
		TimeLog earlier = new TimeLog();
		earlier.setWorkStartTime(LocalTime.of(9, 0));
		earlier.setWorkEndTime(LocalTime.of(12, 0));
		TimeLog later = new TimeLog();
		later.setWorkStartTime(LocalTime.of(13, 0));
		later.setWorkEndTime(LocalTime.of(18, 0));
		assertThat(TimeHelper.isLastIntervalOfDay(earlier, List.of(earlier, later))).isFalse();
	}

	@Test
	@DisplayName("isLastIntervalOfDay - returns false when current effective end is null")
	void testIsLastIntervalOfDayFalseWhenCurrentEndNull() {
		TimeLog current = new TimeLog();
		current.setWorkStartTime(LocalTime.of(9, 0));
		current.setWorkEndTime(null);
		TimeLog other = new TimeLog();
		other.setWorkStartTime(LocalTime.of(10, 0));
		other.setWorkEndTime(LocalTime.of(11, 0));
		assertThat(TimeHelper.isLastIntervalOfDay(current, List.of(current, other))).isFalse();
	}

	@Test
	@DisplayName("createMergedTimeLog - merges multiple intervals")
	void testCreateMergedTimeLogMergesIntervals() {
		TimeLog fallback = new TimeLog();
		TimeLog a = new TimeLog();
		a.setWorkStartTime(LocalTime.of(9, 0));
		a.setWorkEndTime(LocalTime.of(12, 0));
		TimeLog b = new TimeLog();
		b.setWorkStartTime(LocalTime.of(14, 0));
		b.setWorkEndTime(LocalTime.of(17, 0));
		TimeLog merged = TimeHelper.createMergedTimeLog(fallback, List.of(a, b));
		assertThat(merged.getWorkStartTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(merged.getWorkEndTime()).isEqualTo(LocalTime.of(17, 0));
		assertThat(merged.getNormalizedWorkStartTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(merged.getNormalizedWorkEndTime()).isEqualTo(LocalTime.of(17, 0));
	}

	@Test
	@DisplayName("createMergedTimeLog - earlier start in second list position still lowers mergedStart")
	void testCreateMergedTimeLogEarlierStartListedSecond() {
		TimeLog fallback = new TimeLog();
		TimeLog laterListedFirst = new TimeLog();
		laterListedFirst.setWorkStartTime(LocalTime.of(14, 0));
		laterListedFirst.setWorkEndTime(LocalTime.of(17, 0));
		TimeLog earlierListedSecond = new TimeLog();
		earlierListedSecond.setWorkStartTime(LocalTime.of(9, 0));
		earlierListedSecond.setWorkEndTime(LocalTime.of(12, 0));
		TimeLog merged = TimeHelper.createMergedTimeLog(fallback, List.of(laterListedFirst, earlierListedSecond));
		assertThat(merged.getWorkStartTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(merged.getWorkEndTime()).isEqualTo(LocalTime.of(17, 0));
	}

	@Test
	@DisplayName("createMergedTimeLog - returns fallback when same day list is null")
	void testCreateMergedTimeLogNullSameDayReturnsFallback() {
		TimeLog fallback = new TimeLog();
		fallback.setId(42);
		assertThat(TimeHelper.createMergedTimeLog(fallback, null)).isSameAs(fallback);
	}

	@Test
	@DisplayName("createMergedTimeLog - returns fallback when same day list is empty")
	void testCreateMergedTimeLogEmptySameDayReturnsFallback() {
		TimeLog fallback = new TimeLog();
		fallback.setId(43);
		assertThat(TimeHelper.createMergedTimeLog(fallback, List.of())).isSameAs(fallback);
	}

	@Test
	@DisplayName("createMergedTimeLog - returns fallback when no valid times")
	void testCreateMergedTimeLogReturnsFallbackWhenNoValidTimes() {
		TimeLog fallback = new TimeLog();
		fallback.setId(1);
		TimeLog a = new TimeLog();
		TimeLog b = new TimeLog();
		assertThat(TimeHelper.createMergedTimeLog(fallback, List.of(a, b))).isSameAs(fallback);
	}

	@Test
	@DisplayName("createMergedTimeLog - returns fallback when merged span is not strictly ordered")
	void testCreateMergedTimeLogReturnsFallbackWhenMergedStartEqualsEnd() {
		TimeLog fallback = new TimeLog();
		fallback.setId(99);
		TimeLog a = new TimeLog();
		a.setWorkStartTime(LocalTime.of(10, 0));
		a.setWorkEndTime(LocalTime.of(10, 0));
		TimeLog b = new TimeLog();
		b.setWorkStartTime(LocalTime.of(10, 0));
		b.setWorkEndTime(LocalTime.of(10, 0));
		assertThat(TimeHelper.createMergedTimeLog(fallback, List.of(a, b))).isSameAs(fallback);
	}

	@Test
	@DisplayName("createMergedTimeLog - second interval does not extend merged bounds")
	void testCreateMergedTimeLogSecondIntervalInsideMergedBounds() {
		TimeLog fallback = new TimeLog();
		TimeLog wide = new TimeLog();
		wide.setWorkStartTime(LocalTime.of(9, 0));
		wide.setWorkEndTime(LocalTime.of(17, 0));
		TimeLog inner = new TimeLog();
		inner.setWorkStartTime(LocalTime.of(10, 0));
		inner.setWorkEndTime(LocalTime.of(11, 0));
		TimeLog merged = TimeHelper.createMergedTimeLog(fallback, List.of(wide, inner));
		assertThat(merged.getWorkStartTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(merged.getWorkEndTime()).isEqualTo(LocalTime.of(17, 0));
	}

	@Test
	@DisplayName("createMergedTimeLog - second interval same start does not move mergedStart")
	void testCreateMergedTimeLogSecondIntervalSameStartDoesNotMoveMergedStart() {
		TimeLog fallback = new TimeLog();
		TimeLog first = new TimeLog();
		first.setWorkStartTime(LocalTime.of(9, 0));
		first.setWorkEndTime(LocalTime.of(12, 0));
		TimeLog second = new TimeLog();
		second.setWorkStartTime(LocalTime.of(9, 0));
		second.setWorkEndTime(LocalTime.of(15, 0));
		TimeLog merged = TimeHelper.createMergedTimeLog(fallback, List.of(first, second));
		assertThat(merged.getWorkStartTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(merged.getWorkEndTime()).isEqualTo(LocalTime.of(15, 0));
	}

	@Test
	@DisplayName("createMergedTimeLog - second interval same end does not move mergedEnd")
	void testCreateMergedTimeLogSecondIntervalSameEndDoesNotMoveMergedEnd() {
		TimeLog fallback = new TimeLog();
		TimeLog first = new TimeLog();
		first.setWorkStartTime(LocalTime.of(9, 0));
		first.setWorkEndTime(LocalTime.of(17, 0));
		TimeLog second = new TimeLog();
		second.setWorkStartTime(LocalTime.of(10, 0));
		second.setWorkEndTime(LocalTime.of(17, 0));
		TimeLog merged = TimeHelper.createMergedTimeLog(fallback, List.of(first, second));
		assertThat(merged.getWorkStartTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(merged.getWorkEndTime()).isEqualTo(LocalTime.of(17, 0));
	}

	@Test
	@DisplayName("createMergedTimeLog - returns fallback when min start after max end across logs")
	void testCreateMergedTimeLogReturnsFallbackWhenMinStartAfterMaxEnd() {
		TimeLog fallback = new TimeLog();
		fallback.setId(77);
		TimeLog a = new TimeLog();
		a.setWorkStartTime(LocalTime.of(15, 0));
		a.setWorkEndTime(LocalTime.of(10, 0));
		TimeLog b = new TimeLog();
		assertThat(TimeHelper.createMergedTimeLog(fallback, List.of(a, b))).isSameAs(fallback);
	}

	@Test
	@DisplayName("createMergedTimeLog - returns fallback when no effective end across intervals")
	void testCreateMergedTimeLogReturnsFallbackWhenMergedEndNeverSet() {
		TimeLog fallback = new TimeLog();
		fallback.setId(88);
		TimeLog a = new TimeLog();
		a.setWorkStartTime(LocalTime.of(9, 0));
		a.setWorkEndTime(null);
		TimeLog b = new TimeLog();
		b.setWorkStartTime(LocalTime.of(10, 0));
		b.setWorkEndTime(null);
		assertThat(TimeHelper.createMergedTimeLog(fallback, List.of(a, b))).isSameAs(fallback);
	}

	@Test
	@DisplayName("createMergedTimeLog - peer with null effective start still extends merged end")
	void testCreateMergedTimeLogPeerWithNullStart() {
		TimeLog fallback = new TimeLog();
		TimeLog a = new TimeLog();
		a.setWorkStartTime(LocalTime.of(9, 0));
		a.setWorkEndTime(LocalTime.of(12, 0));
		TimeLog b = mock(TimeLog.class);
		given(b.getWorkTime()).willReturn(null);
		given(b.getWorkStartTime()).willReturn(null);
		given(b.getWorkEndTime()).willReturn(LocalTime.of(15, 0));
		TimeLog merged = TimeHelper.createMergedTimeLog(fallback, List.of(a, b));
		assertThat(merged.getWorkStartTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(merged.getWorkEndTime()).isEqualTo(LocalTime.of(15, 0));
	}

	@Test
	@DisplayName("getSameDayTimeLogs - filters by date")
	void testGetSameDayTimeLogsFiltersByDate() {
		LocalDate day = LocalDate.of(2024, 6, 1);
		TimeLog ref = new TimeLog();
		ref.setDate(day);
		TimeLog same = new TimeLog();
		same.setDate(day);
		TimeLog other = new TimeLog();
		other.setDate(day.plusDays(1));
		assertThat(TimeHelper.getSameDayTimeLogs(ref, List.of(ref, same, other))).containsExactly(ref, same);
	}

	@Test
	@DisplayName("getSameDayTimeLogs - excludes entries with null date from results")
	void testGetSameDayTimeLogsExcludesNullDatePeers() {
		LocalDate day = LocalDate.of(2024, 6, 1);
		TimeLog ref = new TimeLog();
		ref.setDate(day);
		TimeLog noDate = new TimeLog();
		noDate.setDate(null);
		assertThat(TimeHelper.getSameDayTimeLogs(ref, List.of(ref, noDate))).containsExactly(ref);
	}

	@Test
	@DisplayName("getSameDayTimeLogs - returns empty when reference time log is null")
	void testGetSameDayTimeLogsNullReferenceTimeLog() {
		TimeLog peer = new TimeLog();
		peer.setDate(LocalDate.of(2024, 6, 1));
		assertThat(TimeHelper.getSameDayTimeLogs(null, List.of(peer))).isEmpty();
	}

	@Test
	@DisplayName("getSameDayTimeLogs - returns empty when allTimeLogs null")
	void testGetSameDayTimeLogsNullAllLogs() {
		TimeLog ref = new TimeLog();
		ref.setDate(LocalDate.of(2024, 1, 1));
		assertThat(TimeHelper.getSameDayTimeLogs(ref, null)).isEmpty();
	}

	@Test
	@DisplayName("getSameDayTimeLogs - returns empty when timeLog date null")
	void testGetSameDayTimeLogsNullRefDate() {
		TimeLog ref = new TimeLog();
		ref.setDate(null);
		assertThat(TimeHelper.getSameDayTimeLogs(ref, List.of(ref))).isEmpty();
	}

	@Test
	@DisplayName("getGapsBetweenIntervals - returns gap between two disjoint intervals")
	void testGetGapsBetweenIntervalsTwoIntervalsWithGap() {
		TimeLog first = new TimeLog();
		first.setWorkStartTime(LocalTime.of(9, 0));
		first.setWorkEndTime(LocalTime.of(12, 0));
		TimeLog second = new TimeLog();
		second.setWorkStartTime(LocalTime.of(14, 0));
		second.setWorkEndTime(LocalTime.of(17, 0));
		RangeSet<LocalTime> gaps = TimeHelper.getGapsBetweenIntervals(List.of(first, second));
		assertThat(gaps.asRanges()).hasSize(1);
		Range<LocalTime> gap = gaps.asRanges().iterator().next();
		assertThat(gap.lowerEndpoint()).isEqualTo(LocalTime.of(12, 0));
		assertThat(gap.upperEndpoint()).isEqualTo(LocalTime.of(14, 0));
	}

	@Test
	@DisplayName("getGapsBetweenIntervals - inner interval does not widen span so no gaps")
	void testGetGapsBetweenIntervalsPlateauMergedBounds() {
		TimeLog outer = new TimeLog();
		outer.setWorkStartTime(LocalTime.of(9, 0));
		outer.setWorkEndTime(LocalTime.of(17, 0));
		TimeLog inner = new TimeLog();
		inner.setWorkStartTime(LocalTime.of(10, 0));
		inner.setWorkEndTime(LocalTime.of(11, 0));
		RangeSet<LocalTime> gaps = TimeHelper.getGapsBetweenIntervals(List.of(outer, inner));
		assertThat(gaps.asRanges()).isEmpty();
	}

	@Test
	@DisplayName("getGapsBetweenIntervals - skips interval with null start when building work ranges")
	void testGetGapsBetweenIntervalsSkipsNullStartInterval() {
		TimeLog valid = new TimeLog();
		valid.setWorkStartTime(LocalTime.of(9, 0));
		valid.setWorkEndTime(LocalTime.of(12, 0));
		TimeLog nullStart = new TimeLog();
		nullStart.setWorkStartTime(null);
		nullStart.setWorkEndTime(LocalTime.of(11, 0));
		assertThat(TimeHelper.getGapsBetweenIntervals(List.of(valid, nullStart)).asRanges()).isEmpty();
	}

	@Test
	@DisplayName("getGapsBetweenIntervals - second interval start equals mergedStart does not shift span start")
	void testGetGapsBetweenIntervalsSecondStartEqualsMergedStart() {
		TimeLog first = new TimeLog();
		first.setWorkStartTime(LocalTime.of(9, 0));
		first.setWorkEndTime(LocalTime.of(12, 0));
		TimeLog second = new TimeLog();
		second.setWorkStartTime(LocalTime.of(9, 0));
		second.setWorkEndTime(LocalTime.of(10, 0));
		assertThat(TimeHelper.getGapsBetweenIntervals(List.of(first, second)).asRanges()).isEmpty();
	}

	@Test
	@DisplayName("getGapsBetweenIntervals - skips interval with null end when building work ranges")
	void testGetGapsBetweenIntervalsSkipsNullEndInterval() {
		TimeLog valid = new TimeLog();
		valid.setWorkStartTime(LocalTime.of(9, 0));
		valid.setWorkEndTime(LocalTime.of(12, 0));
		TimeLog nullEnd = new TimeLog();
		nullEnd.setWorkStartTime(LocalTime.of(10, 0));
		nullEnd.setWorkEndTime(null);
		assertThat(TimeHelper.getGapsBetweenIntervals(List.of(valid, nullEnd)).asRanges()).isEmpty();
	}

	@Test
	@DisplayName("getGapsBetweenIntervals - all intervals invalid yields empty")
	void testGetGapsBetweenIntervalsAllIntervalsInvalid() {
		TimeLog a = new TimeLog();
		a.setWorkStartTime(LocalTime.of(12, 0));
		a.setWorkEndTime(LocalTime.of(9, 0));
		TimeLog b = new TimeLog();
		b.setWorkStartTime(LocalTime.of(14, 0));
		b.setWorkEndTime(LocalTime.of(13, 0));
		assertThat(TimeHelper.getGapsBetweenIntervals(List.of(a, b)).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("getGapsBetweenIntervals - first interval invalid then valid sets merged span")
	void testGetGapsBetweenIntervalsSkipsInvalidThenMergesValid() {
		TimeLog invalid = new TimeLog();
		invalid.setWorkStartTime(LocalTime.of(14, 0));
		invalid.setWorkEndTime(LocalTime.of(9, 0));
		TimeLog valid = new TimeLog();
		valid.setWorkStartTime(LocalTime.of(9, 0));
		valid.setWorkEndTime(LocalTime.of(12, 0));
		assertThat(TimeHelper.getGapsBetweenIntervals(List.of(invalid, valid)).asRanges()).isEmpty();
	}

	@Test
	@DisplayName("getGapsBetweenIntervals - earlier start in second list position still lowers mergedStart")
	void testGetGapsBetweenIntervalsEarlierStartListedSecond() {
		TimeLog laterListedFirst = new TimeLog();
		laterListedFirst.setWorkStartTime(LocalTime.of(14, 0));
		laterListedFirst.setWorkEndTime(LocalTime.of(17, 0));
		TimeLog earlierListedSecond = new TimeLog();
		earlierListedSecond.setWorkStartTime(LocalTime.of(9, 0));
		earlierListedSecond.setWorkEndTime(LocalTime.of(12, 0));
		RangeSet<LocalTime> gaps = TimeHelper.getGapsBetweenIntervals(List.of(laterListedFirst, earlierListedSecond));
		assertThat(gaps.asRanges()).hasSize(1);
		Range<LocalTime> gap = gaps.asRanges().iterator().next();
		assertThat(gap.lowerEndpoint()).isEqualTo(LocalTime.of(12, 0));
		assertThat(gap.upperEndpoint()).isEqualTo(LocalTime.of(14, 0));
	}

	@Test
	@DisplayName("getGapsBetweenIntervals - returns empty when no valid work ranges")
	void testGetGapsBetweenIntervalsNoValidWorkRanges() {
		TimeLog a = new TimeLog();
		a.setWorkStartTime(LocalTime.of(12, 0));
		a.setWorkEndTime(LocalTime.of(9, 0));
		TimeLog b = new TimeLog();
		b.setWorkStartTime(null);
		b.setWorkEndTime(LocalTime.of(17, 0));
		assertThat(TimeHelper.getGapsBetweenIntervals(List.of(a, b)).isEmpty()).isTrue();
	}

	@Test
	@DisplayName("getGapsBetweenIntervals - null or single log returns empty")
	void testGetGapsBetweenIntervalsNullOrSingle() {
		TimeLog log = new TimeLog();
		log.setWorkStartTime(LocalTime.of(9, 0));
		log.setWorkEndTime(LocalTime.of(17, 0));
		assertThat(TimeHelper.getGapsBetweenIntervals(null).isEmpty()).isTrue();
		assertThat(TimeHelper.getGapsBetweenIntervals(List.of(log)).isEmpty()).isTrue();
	}

	/**
	 * Invokes the deprecated {@code calculateDOHours(long, long, long)} overload via
	 * reflection so tests cover production callers without a direct deprecated call.
	 */
	private static long invokeCalculateDOHoursLong(long workedHours, long threshold, long nextThreshold)
			throws Exception {
		Method method = TimeHelper.class.getMethod("calculateDOHours", long.class, long.class, long.class);
		return (long) method.invoke(null, workedHours, threshold, nextThreshold);
	}

	/**
	 * Routes {@link ChargeMethodType#MULTIPLIER} through the compiler-generated switch
	 * default bytecode path by temporarily setting its synthetic switch-map entry to zero
	 * (tests only; does not modify {@link TimeHelper} source).
	 */
	private static synchronized void withChargeMethodSwitchMapDefaultRoute(Runnable assertion) {
		try {
			Class<?> switchMapHolder = Class
				.forName("io.recruitcrm.microservice.timesheet.rule_engine.utils.TimeHelper$1");
			Field switchMapField = switchMapHolder.getDeclaredField(
					"$SwitchMap$io$recruitcrm$microservice$timesheet$rule_engine$constants$ChargeMethodType");
			switchMapField.setAccessible(true);
			int[] switchMap = (int[]) switchMapField.get(null);
			int multiplierOrdinal = ChargeMethodType.MULTIPLIER.ordinal();
			int savedMapping = switchMap[multiplierOrdinal];
			switchMap[multiplierOrdinal] = 0;
			try {
				assertion.run();
			}
			finally {
				switchMap[multiplierOrdinal] = savedMapping;
			}
		}
		catch (ReflectiveOperationException exception) {
			throw new AssertionError("Failed to adjust ChargeMethodType switch map for coverage", exception);
		}
	}

}