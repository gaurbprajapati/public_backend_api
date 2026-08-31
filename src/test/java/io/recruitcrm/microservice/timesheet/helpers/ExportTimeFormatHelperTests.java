package io.recruitcrm.microservice.timesheet.helpers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ExportTimeFormatHelper}.
 */
class ExportTimeFormatHelperTests {

	@Test
	@DisplayName("convertTo12HourFormat returns empty string for null")
	void testConvertTo12HourFormatNullReturnsEmptyString() {
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat(null)).isEmpty();
	}

	@Test
	@DisplayName("convertTo12HourFormat returns value for blank string")
	void testConvertTo12HourFormatBlankReturnsBlank() {
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("")).isEmpty();
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("   ")).isEqualTo("   ");
	}

	@Test
	@DisplayName("convertTo12HourFormat leaves decimal hours unchanged")
	void testConvertTo12HourFormatDecimalHoursUnchanged() {
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("8.00")).isEqualTo("8.00");
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("0.50")).isEqualTo("0.50");
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("10.5")).isEqualTo("10.5");
	}

	@Test
	@DisplayName("convertTo12HourFormat converts single time to 12-hour")
	void testConvertTo12HourFormatSingleTimeConvertsTo12Hour() {
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("09:00")).isEqualTo("09:00 AM");
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("12:00")).isEqualTo("12:00 PM");
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("13:30")).isEqualTo("01:30 PM");
	}

	@Test
	@DisplayName("convertTo12HourFormat converts time range to 12-hour")
	void testConvertTo12HourFormatRangeConvertsTo12Hour() {
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("09:00-12:00")).isEqualTo("09:00 AM-12:00 PM");
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("07:00-19:00")).isEqualTo("07:00 AM-07:00 PM");
	}

	@Test
	@DisplayName("convertTo12HourFormat handles comma-separated ranges")
	void testConvertTo12HourFormatCommaSeparatedRanges() {
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("09:00-12:00, 14:00-17:00"))
			.isEqualTo("09:00 AM-12:00 PM, 02:00 PM-05:00 PM");
	}

	@Test
	@DisplayName("convertTo12HourFormat returns original when part does not match time pattern")
	void testConvertTo12HourFormatInvalidPatternReturnsOriginal() {
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("invalid")).isEqualTo("invalid");
		assertThat(ExportTimeFormatHelper.convertTo12HourFormat("20:00-21:00")).isEqualTo("08:00 PM-09:00 PM");
	}

	@Test
	@DisplayName("convertTo12HourFormat range with only start returns start when end empty")
	void testConvertTo12HourFormatRangeOnlyStartReturnsStart() {
		String result = ExportTimeFormatHelper.convertTo12HourFormat("09:00-");
		assertThat(result).isEqualTo("09:00 AM");
	}

	@Test
	@DisplayName("convertTo12HourFormat range with invalid end returns only start converted")
	void testConvertTo12HourFormatRangeInvalidEndReturnsStartOnly() {
		String result = ExportTimeFormatHelper.convertTo12HourFormat("09:00-xx:xx");
		assertThat(result).isEqualTo("09:00 AM");
	}

	@Test
	@DisplayName("applyTimeFormat returns value when use12HourFormat is false")
	void testApplyTimeFormatFalseReturnsValueUnchanged() {
		assertThat(ExportTimeFormatHelper.applyTimeFormat("09:00-12:00", false)).isEqualTo("09:00-12:00");
		assertThat(ExportTimeFormatHelper.applyTimeFormat(null, false)).isEmpty();
		assertThat(ExportTimeFormatHelper.applyTimeFormat("", false)).isEmpty();
	}

	@Test
	@DisplayName("applyTimeFormat converts to 12-hour when use12HourFormat is true")
	void testApplyTimeFormatTrueConvertsTo12Hour() {
		assertThat(ExportTimeFormatHelper.applyTimeFormat("09:00-12:00", true)).isEqualTo("09:00 AM-12:00 PM");
		assertThat(ExportTimeFormatHelper.applyTimeFormat(null, true)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = { "9:00", "09:00", "0:00", "23:59" })
	@DisplayName("convertTo12HourFormat accepts HH:MM and H:MM patterns")
	void testConvertTo12HourFormatAcceptsHourPatterns(String timeStr) {
		String result = ExportTimeFormatHelper.convertTo12HourFormat(timeStr);
		assertThat(result).isNotEmpty().containsAnyOf("AM", "PM");
	}

	@Test
	@DisplayName("convertTo12HourFormat returns original part when time parses with exception")
	void testConvertTo12HourFormatParseExceptionReturnsOriginalPart() {
		String result = ExportTimeFormatHelper.convertTo12HourFormat("25:00");
		assertThat(result).isEqualTo("25:00");
	}

	@Test
	@DisplayName("convertTo12HourFormat keeps empty part for leading comma separated value")
	void testConvertTo12HourFormatEmptyPartReturnsEmpty() {
		String result = ExportTimeFormatHelper.convertTo12HourFormat(", 09:00");
		assertThat(result).isEqualTo(", 09:00 AM");
	}

	@Test
	@DisplayName("convertTo12HourFormat returns original part when range start and end both invalid")
	void testConvertTo12HourFormatRangeBothInvalidReturnsPart() {
		String result = ExportTimeFormatHelper.convertTo12HourFormat("xx:xx-yy:yy");
		assertThat(result).isEqualTo("xx:xx-yy:yy");
	}

	@Test
	@DisplayName("convertTo12HourFormat converts both endpoints of a valid range")
	void testConvertTo12HourFormatRangeBothValidConverts() {
		String result = ExportTimeFormatHelper.convertTo12HourFormat("13:00-15:30");
		assertThat(result).isEqualTo("01:00 PM-03:30 PM");
	}

}
