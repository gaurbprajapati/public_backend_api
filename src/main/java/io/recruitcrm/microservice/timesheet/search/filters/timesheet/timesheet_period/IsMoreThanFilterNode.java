package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for timesheet period IS_MORE_THAN filter - matches timesheets where the
 * period_end is before (today - N days). For example, if today is Dec 29 and filterValue
 * is 10, it matches timesheets where period_end < Dec 19 (GMT adjusted), i.e., all
 * timesheets from Jan 1, 1970 to Dec 19 (GMT adjusted). Filter value format: number of
 * days as integer (e.g., "10").
 */
public class IsMoreThanFilterNode extends TimesheetPeriodFieldBaseFilterNode {

	private Field<Integer> periodEndField;

	public IsMoreThanFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.periodEndField = this.getPeriodEndField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		Integer days = this.parseFilterValue();

		if (days == null || days < 0) {
			// If no valid days value provided, return false condition (matches nothing)
			return List.of(DSL.falseCondition());
		}

		// Get GMT difference for offset calculation
		String gmtDifference = (this.filterNodeContext.getGmtDifference() != null)
				? this.filterNodeContext.getGmtDifference() : "+00:00";
		ZoneId zoneId = ZoneOffset.of(gmtDifference);
		ZoneOffset offset = ZoneOffset.of(gmtDifference);
		int offsetSeconds = offset.getTotalSeconds();

		// Calculate end date: today - N days
		LocalDate today = LocalDate.now(zoneId);
		LocalDate endDate = today.minusDays(days);

		// Convert to epoch seconds at start of day
		Instant endInstant = endDate.atStartOfDay(zoneId).toInstant();
		Long endEpochLong = endInstant.getEpochSecond();

		// Apply GMT offset to convert from user's local timezone to UTC (same as
		// isBetween)
		Integer endEpoch = Math.toIntExact(endEpochLong + offsetSeconds);

		// Return timesheets where period_end < endEpoch (from epoch 0 to endEpoch)
		Condition periodBeforeDate = this.periodEndField.lt(endEpoch);

		return List.of(periodBeforeDate);
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

	/**
	 * Parses filterValue as number of days. Can be: 1. Single integer string: "10" 2.
	 * JSON number: 10
	 * @return Number of days or null if invalid
	 */
	private Integer parseFilterValue() {
		String filterValue = this.filterNodeContext.getFilterDto().getFilterValue();
		if (filterValue == null || filterValue.trim().isEmpty()) {
			return null;
		}

		try {
			// Try parsing as JSON number first
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(filterValue);
			if (jsonNode.isInt()) {
				return jsonNode.asInt();
			}
			else if (jsonNode.isLong()) {
				return Math.toIntExact(jsonNode.asLong());
			}
			else if (jsonNode.isTextual()) {
				return Integer.parseInt(jsonNode.asText().trim());
			}
		}
		catch (Exception ex) {
			// Not JSON, try parsing as integer string
		}

		// Parse as integer string
		try {
			return Integer.parseInt(filterValue.trim());
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

}
