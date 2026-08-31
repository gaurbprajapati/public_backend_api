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
 * Filter node for timesheet period IS_BETWEEN filter - matches timesheets where the
 * timesheet period overlaps with the specified date range. Filter value format: {"start":
 * 1764527400, "end": 1765996199} where values are epoch timestamps in seconds.
 */
public class IsBetweenFilterNode extends TimesheetPeriodFieldBaseFilterNode {

	private Field<Integer> periodStartField;

	private Field<Integer> periodEndField;

	public IsBetweenFilterNode(FilterNodeContext filterNodeContext) {
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
			 * Filter timesheets where the requested date range overlaps with the
			 * timesheet period. A timesheet matches if: - The timesheet period_start
			 * falls within the requested range (startEpoch <= period_start <= endEpoch)
			 * OR - The timesheet period_end falls within the requested range (startEpoch
			 * <= period_end <= endEpoch) OR - The timesheet period fully contains the
			 * requested range (period_start <= startEpoch AND period_end >= endEpoch)
			 */
			Condition periodOverlap = this.periodStartField.between(startEpoch, endEpoch)
				.or(this.periodEndField.between(startEpoch, endEpoch))
				.or(this.periodStartField.le(startEpoch).and(this.periodEndField.ge(endEpoch)));

			return List.of(periodOverlap);
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
