package io.recruitcrm.microservice.timesheet.search.helpers;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import io.recruitcrm.microservice.timesheet.search.constants.DateIsFilterValue;
import io.recruitcrm.microservice.timesheet.search.dto.ZonedDateTimeRangeDto;

public final class DateHelper {

	private DateHelper() {
		// Utility class
	}

	public static ZonedDateTimeRangeDto getZonedDateTimeRange(DateIsFilterValue dateIsFilterValue,
			String gmtDifference) {
		ZoneId zoneId = ZoneOffset.of(gmtDifference);
		LocalDate now = LocalDate.now(zoneId);

		return switch (dateIsFilterValue) {
			case TODAY -> getToday(now, zoneId);
			case YESTERDAY -> getYesterday(now, zoneId);
			case THIS_WEEK -> getThisWeek(now, zoneId);
			case LAST_WEEK -> getLastWeek(now, zoneId);
			case THIS_MONTH -> getThisMonth(now, zoneId);
			case LAST_MONTH -> getLastMonth(now, zoneId);
			case LAST_30 -> getLast30Days(now, zoneId);
			case LAST_60 -> getLast60Days(now, zoneId);
			case LAST_90 -> getLast90Days(now, zoneId);
			case LAST_365 -> getLast365Days(now, zoneId);
			case THIS_QUARTER -> getThisQuarter(now, zoneId);
			case LAST_QUARTER -> getLastQuarter(now, zoneId);
			case THIS_YEAR -> getThisYear(now, zoneId);
			case LAST_YEAR -> getLastYear(now, zoneId);
			case ALL_TIME -> getAllTime(now, zoneId);
			default -> throw new IllegalArgumentException("Invalid date filter value: " + dateIsFilterValue);
		};
	}

	private static ZonedDateTimeRangeDto getToday(LocalDate now, ZoneId zoneId) {
		ZonedDateTime from = now.atStartOfDay(zoneId);
		ZonedDateTime to = now.plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getYesterday(LocalDate now, ZoneId zoneId) {
		LocalDate yesterday = now.minusDays(1);
		ZonedDateTime from = yesterday.atStartOfDay(zoneId);
		ZonedDateTime to = yesterday.plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getThisWeek(LocalDate now, ZoneId zoneId) {
		ZonedDateTime from = now.with(DayOfWeek.MONDAY).atStartOfDay(zoneId);
		ZonedDateTime to = now.with(DayOfWeek.SUNDAY).atStartOfDay(zoneId).plusDays(1).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getLastWeek(LocalDate now, ZoneId zoneId) {
		LocalDate lastWeekStart = now.with(DayOfWeek.MONDAY).minusWeeks(1);
		LocalDate lastWeekEnd = now.with(DayOfWeek.SUNDAY).minusWeeks(1);
		ZonedDateTime from = lastWeekStart.atStartOfDay(zoneId);
		ZonedDateTime to = lastWeekEnd.plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getThisMonth(LocalDate now, ZoneId zoneId) {
		ZonedDateTime from = now.withDayOfMonth(1).atStartOfDay(zoneId);
		ZonedDateTime to = now.withDayOfMonth(now.lengthOfMonth()).plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getLastMonth(LocalDate now, ZoneId zoneId) {
		LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
		LocalDate lastMonthEnd = lastMonthStart.withDayOfMonth(lastMonthStart.lengthOfMonth());
		ZonedDateTime from = lastMonthStart.atStartOfDay(zoneId);
		ZonedDateTime to = lastMonthEnd.plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getLast30Days(LocalDate now, ZoneId zoneId) {
		LocalDate startDate = now.minusDays(29);
		ZonedDateTime from = startDate.atStartOfDay(zoneId);
		ZonedDateTime to = now.plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getLast60Days(LocalDate now, ZoneId zoneId) {
		LocalDate startDate = now.minusDays(59);
		ZonedDateTime from = startDate.atStartOfDay(zoneId);
		ZonedDateTime to = now.plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getLast90Days(LocalDate now, ZoneId zoneId) {
		LocalDate startDate = now.minusDays(89);
		ZonedDateTime from = startDate.atStartOfDay(zoneId);
		ZonedDateTime to = now.plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getLast365Days(LocalDate now, ZoneId zoneId) {
		LocalDate startDate = now.minusDays(364);
		ZonedDateTime from = startDate.atStartOfDay(zoneId);
		ZonedDateTime to = now.plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getThisQuarter(LocalDate now, ZoneId zoneId) {
		Month currentMonthEnum = now.getMonth();
		Month quarterStartMonth;
		Month quarterEndMonth;
		if (currentMonthEnum.compareTo(Month.MARCH) <= 0) {
			quarterStartMonth = Month.JANUARY;
			quarterEndMonth = Month.MARCH;
		}
		else if (currentMonthEnum.compareTo(Month.JUNE) <= 0) {
			quarterStartMonth = Month.APRIL;
			quarterEndMonth = Month.JUNE;
		}
		else if (currentMonthEnum.compareTo(Month.SEPTEMBER) <= 0) {
			quarterStartMonth = Month.JULY;
			quarterEndMonth = Month.SEPTEMBER;
		}
		else {
			quarterStartMonth = Month.OCTOBER;
			quarterEndMonth = Month.DECEMBER;
		}

		LocalDate quarterStart = now.with(quarterStartMonth).withDayOfMonth(1);
		LocalDate quarterEnd = now.with(quarterEndMonth).withDayOfMonth(1);
		quarterEnd = quarterEnd.withDayOfMonth(quarterEnd.lengthOfMonth());

		ZonedDateTime from = quarterStart.atStartOfDay(zoneId);
		ZonedDateTime to = quarterEnd.plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getLastQuarter(LocalDate now, ZoneId zoneId) {
		Month currentMonthEnum = now.getMonth();
		Month lastQuarterStartMonth;
		Month lastQuarterEndMonth;
		int yearOffset = 0;

		if (currentMonthEnum.compareTo(Month.MARCH) <= 0) {
			lastQuarterStartMonth = Month.OCTOBER;
			lastQuarterEndMonth = Month.DECEMBER;
			yearOffset = -1;
		}
		else if (currentMonthEnum.compareTo(Month.JUNE) <= 0) {
			lastQuarterStartMonth = Month.JANUARY;
			lastQuarterEndMonth = Month.MARCH;
		}
		else if (currentMonthEnum.compareTo(Month.SEPTEMBER) <= 0) {
			lastQuarterStartMonth = Month.APRIL;
			lastQuarterEndMonth = Month.JUNE;
		}
		else {
			lastQuarterStartMonth = Month.JULY;
			lastQuarterEndMonth = Month.SEPTEMBER;
		}

		LocalDate lastQuarterStart = now.plusYears(yearOffset).with(lastQuarterStartMonth).withDayOfMonth(1);
		LocalDate lastQuarterEnd = now.plusYears(yearOffset).with(lastQuarterEndMonth).withDayOfMonth(1);
		lastQuarterEnd = lastQuarterEnd.withDayOfMonth(lastQuarterEnd.lengthOfMonth());

		ZonedDateTime from = lastQuarterStart.atStartOfDay(zoneId);
		ZonedDateTime to = lastQuarterEnd.plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getThisYear(LocalDate now, ZoneId zoneId) {
		ZonedDateTime from = now.withDayOfYear(1).atStartOfDay(zoneId);
		ZonedDateTime to = now.withDayOfYear(now.lengthOfYear()).plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getLastYear(LocalDate now, ZoneId zoneId) {
		LocalDate lastYearStart = now.minusYears(1).withDayOfYear(1);
		LocalDate lastYearEnd = lastYearStart.withDayOfYear(lastYearStart.lengthOfYear());
		ZonedDateTime from = lastYearStart.atStartOfDay(zoneId);
		ZonedDateTime to = lastYearEnd.plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

	private static ZonedDateTimeRangeDto getAllTime(LocalDate now, ZoneId zoneId) {
		ZonedDateTime from = Instant.EPOCH.atZone(zoneId);
		ZonedDateTime to = now.plusDays(1).atStartOfDay(zoneId).minusSeconds(1);
		return new ZonedDateTimeRangeDto(from, to);
	}

}
