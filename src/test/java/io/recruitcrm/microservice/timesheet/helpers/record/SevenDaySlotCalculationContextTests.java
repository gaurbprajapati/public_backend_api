package io.recruitcrm.microservice.timesheet.helpers.record;

import io.recruitcrm.microservice.timesheet.testdata.SevenDaySlotCalculationContextTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SevenDaySlotCalculationContext Tests")
class SevenDaySlotCalculationContextTests {

	private static final int CUSTOM_ADJUSTED_START = 1710100000;

	private static final LocalDateTime CUSTOM_ADJUSTED_START_DATE_TIME = LocalDateTime.of(2024, 3, 10, 8, 15);

	private static final int CUSTOM_ADJUSTED_START_DAY_OF_WEEK = 7;

	private static final int CUSTOM_FIRST_START_DAY_EPOCH = 1709942400;

	private static final int CUSTOM_TIMESHEET_START_DAY = 2;

	@Test
	@DisplayName("record should expose all values from default test data factory")
	void testRecordAccessorsWithDefaultFactoryData() {
		// Given
		SevenDaySlotCalculationContext context = SevenDaySlotCalculationContextTestDataFactory.createContext();

		// When and Then
		assertThat(context.adjustedStart())
			.isEqualTo(SevenDaySlotCalculationContextTestDataFactory.DEFAULT_ADJUSTED_START);
		assertThat(context.adjustedStartDateTime())
			.isEqualTo(SevenDaySlotCalculationContextTestDataFactory.DEFAULT_ADJUSTED_START_DATE_TIME);
		assertThat(context.adjustedStartDayOfWeek())
			.isEqualTo(SevenDaySlotCalculationContextTestDataFactory.DEFAULT_ADJUSTED_START_DAY_OF_WEEK);
		assertThat(context.firstStartDayEpoch())
			.isEqualTo(SevenDaySlotCalculationContextTestDataFactory.DEFAULT_FIRST_START_DAY_EPOCH);
		assertThat(context.timesheetStartDay())
			.isEqualTo(SevenDaySlotCalculationContextTestDataFactory.DEFAULT_TIMESHEET_START_DAY);
	}

	@Test
	@DisplayName("record should support value equality and consistent hash code")
	void testRecordEqualityAndHashCode() {
		// Given
		SevenDaySlotCalculationContext left = SevenDaySlotCalculationContextTestDataFactory.createContext(
				CUSTOM_ADJUSTED_START, CUSTOM_ADJUSTED_START_DATE_TIME, CUSTOM_ADJUSTED_START_DAY_OF_WEEK,
				CUSTOM_FIRST_START_DAY_EPOCH, CUSTOM_TIMESHEET_START_DAY);
		SevenDaySlotCalculationContext right = SevenDaySlotCalculationContextTestDataFactory.createContext(
				CUSTOM_ADJUSTED_START, CUSTOM_ADJUSTED_START_DATE_TIME, CUSTOM_ADJUSTED_START_DAY_OF_WEEK,
				CUSTOM_FIRST_START_DAY_EPOCH, CUSTOM_TIMESHEET_START_DAY);

		// When and Then
		assertThat(left).isEqualTo(right).hasSameHashCodeAs(right);
	}

	@Test
	@DisplayName("record toString should include component names and values")
	void testRecordToStringContainsComponents() {
		// Given
		SevenDaySlotCalculationContext context = SevenDaySlotCalculationContextTestDataFactory.createContext(
				CUSTOM_ADJUSTED_START, CUSTOM_ADJUSTED_START_DATE_TIME, CUSTOM_ADJUSTED_START_DAY_OF_WEEK,
				CUSTOM_FIRST_START_DAY_EPOCH, CUSTOM_TIMESHEET_START_DAY);

		// When
		String result = context.toString();

		// Then
		assertThat(result).contains("adjustedStart=" + CUSTOM_ADJUSTED_START)
			.contains("adjustedStartDateTime=" + CUSTOM_ADJUSTED_START_DATE_TIME)
			.contains("adjustedStartDayOfWeek=" + CUSTOM_ADJUSTED_START_DAY_OF_WEEK)
			.contains("firstStartDayEpoch=" + CUSTOM_FIRST_START_DAY_EPOCH)
			.contains("timesheetStartDay=" + CUSTOM_TIMESHEET_START_DAY);
	}

}
