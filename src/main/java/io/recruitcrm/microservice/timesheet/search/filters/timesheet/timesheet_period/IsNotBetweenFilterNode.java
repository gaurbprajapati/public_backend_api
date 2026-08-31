package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period;

import java.time.ZoneOffset;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;

/**
 * Filter node for timesheet period IS_NOT_BETWEEN filter - matches timesheets where the
 * timesheet period does NOT overlap with the specified date range. Filter value format:
 * {"start": 1764527400, "end": 1765996199} where values are epoch timestamps in seconds.
 */
public class IsNotBetweenFilterNode extends TimesheetPeriodFieldBaseFilterNode {

	private Field<Integer> periodStartField;

	private Field<Integer> periodEndField;

	public IsNotBetweenFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.periodStartField = this.getPeriodStartField();
		this.periodEndField = this.getPeriodEndField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		try {
			String filterValue = this.filterNodeContext.getFilterDto().getFilterValue();
			if (filterValue == null || filterValue.trim().isEmpty()) {
				return List.of(DSL.falseCondition());
			}

			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(filterValue);
			Long startEpochLong = jsonNode.get("start").asLong();
			Long endEpochLong = jsonNode.get("end").asLong();

			// Apply GMT difference to convert filter values from user's local timezone to
			// UTC
			String gmtDifference = (this.filterNodeContext.getGmtDifference() != null)
					? this.filterNodeContext.getGmtDifference() : "+00:00";
			ZoneOffset offset = ZoneOffset.of(gmtDifference);
			int offsetSeconds = offset.getTotalSeconds();

			// Add offset to convert from user's local date interpretation to UTC
			Integer startEpoch = Math.toIntExact(startEpochLong + offsetSeconds);
			Integer endEpoch = Math.toIntExact(endEpochLong + offsetSeconds);

			/**
			 * Filter timesheets where the requested date range does NOT overlap with the
			 * timesheet period. A timesheet does NOT match if: - The timesheet period
			 * ends before the requested range starts (period_end < startEpoch) OR - The
			 * timesheet period starts after the requested range ends (period_start >
			 * endEpoch) This is the opposite of IS_BETWEEN filter.
			 */
			Condition periodNotOverlap = this.periodEndField.lt(startEpoch).or(this.periodStartField.gt(endEpoch));

			return List.of(periodNotOverlap);
		}
		catch (Exception ex) {
			// If parsing fails, return false condition (matches nothing)
			return List.of(DSL.falseCondition());
		}
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
