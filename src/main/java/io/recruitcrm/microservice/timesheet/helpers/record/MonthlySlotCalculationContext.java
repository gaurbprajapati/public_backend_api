package io.recruitcrm.microservice.timesheet.helpers.record;

import java.time.LocalDateTime;

/**
 * Context record for monthly slot calculation containing month-based calculations and
 * epoch time conversions for monthly timesheet frequency.
 */
public record MonthlySlotCalculationContext(int currentStartEpoch, LocalDateTime oneMonthFromCurrentStart,
		int relativeOneMonthFromCurrentStartEpoch) {
}