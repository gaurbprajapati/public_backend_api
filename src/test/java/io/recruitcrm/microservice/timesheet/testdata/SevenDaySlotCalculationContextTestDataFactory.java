package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.helpers.record.SevenDaySlotCalculationContext;

import java.time.LocalDateTime;

/**
 * Test data factory for {@link SevenDaySlotCalculationContext}.
 */
public final class SevenDaySlotCalculationContextTestDataFactory {

	public static final int DEFAULT_ADJUSTED_START = 1710000000;

	public static final LocalDateTime DEFAULT_ADJUSTED_START_DATE_TIME = LocalDateTime.of(2024, 3, 9, 9, 30);

	public static final int DEFAULT_ADJUSTED_START_DAY_OF_WEEK = 6;

	public static final int DEFAULT_FIRST_START_DAY_EPOCH = 1709856000;

	public static final int DEFAULT_TIMESHEET_START_DAY = 1;

	private SevenDaySlotCalculationContextTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Creates a default SevenDaySlotCalculationContext.
	 * @return default record instance
	 */
	public static SevenDaySlotCalculationContext createContext() {
		return new SevenDaySlotCalculationContext(DEFAULT_ADJUSTED_START, DEFAULT_ADJUSTED_START_DATE_TIME,
				DEFAULT_ADJUSTED_START_DAY_OF_WEEK, DEFAULT_FIRST_START_DAY_EPOCH, DEFAULT_TIMESHEET_START_DAY);
	}

	/**
	 * Creates a custom SevenDaySlotCalculationContext.
	 * @param adjustedStart adjusted start epoch
	 * @param adjustedStartDateTime adjusted start date-time
	 * @param adjustedStartDayOfWeek adjusted day of week
	 * @param firstStartDayEpoch first start day epoch
	 * @param timesheetStartDay timesheet start day
	 * @return custom record instance
	 */
	public static SevenDaySlotCalculationContext createContext(int adjustedStart, LocalDateTime adjustedStartDateTime,
			int adjustedStartDayOfWeek, int firstStartDayEpoch, int timesheetStartDay) {
		return new SevenDaySlotCalculationContext(adjustedStart, adjustedStartDateTime, adjustedStartDayOfWeek,
				firstStartDayEpoch, timesheetStartDay);
	}

}
