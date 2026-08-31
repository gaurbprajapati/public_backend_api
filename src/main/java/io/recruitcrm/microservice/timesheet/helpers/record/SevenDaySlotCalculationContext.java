package io.recruitcrm.microservice.timesheet.helpers.record;

import java.time.LocalDateTime;

/**
 * Context record for seven-day slot calculation containing adjusted start times and
 * day-of-week calculations for weekly timesheet frequency.
 */
public record SevenDaySlotCalculationContext(int adjustedStart, LocalDateTime adjustedStartDateTime,
		int adjustedStartDayOfWeek, int firstStartDayEpoch, int timesheetStartDay) {
}