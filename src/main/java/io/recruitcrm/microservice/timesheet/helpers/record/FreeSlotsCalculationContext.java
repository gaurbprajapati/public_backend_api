package io.recruitcrm.microservice.timesheet.helpers.record;

import java.util.List;

/**
 * Context record for free slots calculation containing all necessary parameters for
 * determining available time slots between occupied periods.
 */
public record FreeSlotsCalculationContext(Integer globalStart, Integer globalEnd, Integer timesheetStartDay,
		Integer timesheetFrequencyId, List<List<Integer>> allOccupiedSlots, boolean endMatchedSlots,
		boolean startMatchedSlots) {
}