package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period;

import java.time.ZoneOffset;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for timesheet period IS_AFTER filter - matches timesheets where the entire
 * period (period_start) is after the specified epoch timestamp (in seconds).
 */
public class IsAfterFilterNode extends TimesheetPeriodFieldBaseFilterNode {

	private Field<Integer> periodStartField;

	private Field<Integer> periodEndField;

	public IsAfterFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.periodStartField = this.getPeriodStartField();
		this.periodEndField = this.getPeriodEndField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		Long filterValueLong = this.parseFilterValue();

		if (filterValueLong == null) {
			// If no valid filter value provided, return false condition (matches nothing)
			return List.of(DSL.falseCondition());
		}

		// Apply GMT difference to convert filter value from user's local timezone to UTC
		String gmtDifference = (this.filterNodeContext.getGmtDifference() != null)
				? this.filterNodeContext.getGmtDifference() : "+00:00";
		ZoneOffset offset = ZoneOffset.of(gmtDifference);
		int offsetSeconds = offset.getTotalSeconds();

		// Add offset to convert from user's local date interpretation to UTC
		Integer filterValue = Math.toIntExact(filterValueLong + offsetSeconds);

		// Return timesheets where period_start > filterValue OR period_end > filterValue
		Condition periodAfterValue = this.periodStartField.gt(filterValue).or(this.periodEndField.gt(filterValue));

		return List.of(periodAfterValue);
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

	/**
	 * Parses filterValue as a single epoch timestamp in seconds. Can be: 1. Single
	 * integer string: "1764527400" 2. JSON number: 1764527400
	 * @return Epoch timestamp in seconds or null if invalid
	 */
	private Long parseFilterValue() {
		String filterValue = this.filterNodeContext.getFilterDto().getFilterValue();
		if (filterValue == null || filterValue.trim().isEmpty()) {
			return null;
		}

		try {
			// Try parsing as JSON number first
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(filterValue);
			if (jsonNode.isInt()) {
				return (long) jsonNode.asInt();
			}
			else if (jsonNode.isLong()) {
				return jsonNode.asLong();
			}
			else if (jsonNode.isTextual()) {
				return Long.parseLong(jsonNode.asText().trim());
			}
		}
		catch (Exception ex) {
			// Not JSON, try parsing as long string
		}

		// Parse as long string
		try {
			return Long.parseLong(filterValue.trim());
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

}
