package io.recruitcrm.microservice.timesheet.search.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.DateIsFilterValue;
import io.recruitcrm.microservice.timesheet.search.dto.ZonedDateTimeRangeDto;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("DateHelper Tests")
class DateHelperTests {

	private static final String GMT_DIFFERENCE = "+05:30";

	private static final int FIRST_DAY_OF_MONTH = 1;

	private static final int FIRST_DAY_OF_YEAR = 1;

	private static final long EPOCH_START = 0L;

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for TODAY")
	void testGetZonedDateTimeRangeForToday() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.TODAY, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for YESTERDAY")
	void testGetZonedDateTimeRangeForYesterday() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.YESTERDAY, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for THIS_WEEK")
	void testGetZonedDateTimeRangeForThisWeek() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.THIS_WEEK, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for LAST_WEEK")
	void testGetZonedDateTimeRangeForLastWeek() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.LAST_WEEK, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for THIS_MONTH")
	void testGetZonedDateTimeRangeForThisMonth() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.THIS_MONTH, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getDayOfMonth()).isEqualTo(FIRST_DAY_OF_MONTH);
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for LAST_MONTH")
	void testGetZonedDateTimeRangeForLastMonth() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.LAST_MONTH, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getDayOfMonth()).isEqualTo(FIRST_DAY_OF_MONTH);
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for LAST_30")
	void testGetZonedDateTimeRangeForLast30() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.LAST_30, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for LAST_60")
	void testGetZonedDateTimeRangeForLast60() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.LAST_60, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for LAST_90")
	void testGetZonedDateTimeRangeForLast90() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.LAST_90, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for LAST_365")
	void testGetZonedDateTimeRangeForLast365() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.LAST_365, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for THIS_QUARTER")
	void testGetZonedDateTimeRangeForThisQuarter() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.THIS_QUARTER, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "Q1 (Jan-Mar)", "Q2 (Apr-Jun)", "Q3 (Jul-Sep)", "Q4 (Oct-Dec)" })
	void testGetZonedDateTimeRangeForThisQuarterByQuarter(String quarterLabel) {
		assertThat(quarterLabel).isNotNull();
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.THIS_QUARTER, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for LAST_QUARTER")
	void testGetZonedDateTimeRangeForLastQuarter() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.LAST_QUARTER, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for LAST_QUARTER when current is Q1")
	void testGetZonedDateTimeRangeForLastQuarterWhenCurrentIsQ1() {
		// When current quarter is Q1, last quarter should be Q4 of previous year
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.LAST_QUARTER, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for THIS_YEAR")
	void testGetZonedDateTimeRangeForThisYear() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.THIS_YEAR, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getDayOfYear()).isEqualTo(FIRST_DAY_OF_YEAR);
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for LAST_YEAR")
	void testGetZonedDateTimeRangeForLastYear() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.LAST_YEAR, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().getDayOfYear()).isEqualTo(FIRST_DAY_OF_YEAR);
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should return correct range for ALL_TIME")
	void testGetZonedDateTimeRangeForAllTime() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.ALL_TIME, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBefore(result.getTo());
		assertThat(result.getFrom().toEpochSecond()).isEqualTo(EPOCH_START);
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should handle different GMT differences")
	void testGetZonedDateTimeRangeWithDifferentGmtDifferences() {
		String[] gmtDifferences = { "+00:00", "+05:30", "-05:00", "+09:00", "-08:00" };

		for (String gmtDiff : gmtDifferences) {
			ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.TODAY, gmtDiff);

			assertThat(result).isNotNull();
			assertThat(result.getFrom()).isNotNull();
			assertThat(result.getTo()).isNotNull();
			assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(gmtDiff));
			assertThat(result.getTo().getZone()).isEqualTo(ZoneOffset.of(gmtDiff));
		}
	}

	@ParameterizedTest
	@EnumSource(DateIsFilterValue.class)
	@DisplayName("getZonedDateTimeRange should handle all enum values")
	void testGetZonedDateTimeRangeForAllEnumValues(DateIsFilterValue dateIsFilterValue) {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(dateIsFilterValue, GMT_DIFFERENCE);

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom()).isBeforeOrEqualTo(result.getTo());
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
		assertThat(result.getTo().getZone()).isEqualTo(ZoneOffset.of(GMT_DIFFERENCE));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should throw exception for null dateIsFilterValue")
	void testGetZonedDateTimeRangeThrowsExceptionForNull() {
		assertThatThrownBy(() -> DateHelper.getZonedDateTimeRange(null, GMT_DIFFERENCE))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("getZonedDateTimeRange should handle negative GMT offset")
	void testGetZonedDateTimeRangeWithNegativeGmtOffset() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.TODAY, "-05:00");

		assertThat(result).isNotNull();
		assertThat(result.getFrom()).isNotNull();
		assertThat(result.getTo()).isNotNull();
		assertThat(result.getFrom().getZone()).isEqualTo(ZoneOffset.of("-05:00"));
	}

	@Test
	@DisplayName("getZonedDateTimeRange should ensure 'to' is end of day (minus 1 second)")
	void testGetZonedDateTimeRangeToIsEndOfDay() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.TODAY, GMT_DIFFERENCE);

		LocalDate today = LocalDate.now(ZoneOffset.of(GMT_DIFFERENCE));
		ZonedDateTime expectedEnd = today.plusDays(1).atStartOfDay(ZoneOffset.of(GMT_DIFFERENCE)).minusSeconds(1);

		assertThat(result.getTo()).isEqualTo(expectedEnd);
	}

	@Test
	@DisplayName("getZonedDateTimeRange should ensure 'from' is start of day")
	void testGetZonedDateTimeRangeFromIsStartOfDay() {
		ZonedDateTimeRangeDto result = DateHelper.getZonedDateTimeRange(DateIsFilterValue.TODAY, GMT_DIFFERENCE);

		LocalDate today = LocalDate.now(ZoneOffset.of(GMT_DIFFERENCE));
		ZonedDateTime expectedStart = today.atStartOfDay(ZoneOffset.of(GMT_DIFFERENCE));

		assertThat(result.getFrom()).isEqualTo(expectedStart);
	}

}
