package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period;

import java.time.ZoneOffset;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.constants.DateIsFilterValue;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.helpers.DateHelper;
import io.recruitcrm.microservice.timesheet.search.dto.ZonedDateTimeRangeDto;

public class IsFilterNode extends TimesheetPeriodFieldBaseFilterNode {

	private Field<Integer> periodStartField;

	private Field<Integer> periodEndField;

	private DateIsFilterValue dateIsFilterValue;

	public IsFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		String filterValue = this.filterNodeContext.getFilterDto().getFilterValue();
		if (filterValue != null && !filterValue.trim().isEmpty()) {
			this.dateIsFilterValue = DateIsFilterValue.fromValue(filterValue);
		}
		this.periodStartField = this.getPeriodStartField();
		this.periodEndField = this.getPeriodEndField();
	}

	@Override
	public List<Condition> getFilterConditions() {
		// Parse filterValue as DateIsFilterValue enum (e.g., "TODAY", "YESTERDAY",
		// "THIS_MONTH")
		if (this.dateIsFilterValue == null) {
			// If no valid filter value provided, return false condition (matches nothing)
			return List.of(org.jooq.impl.DSL.falseCondition());
		}

		String gmtDifference = (this.filterNodeContext.getGmtDifference() != null)
				? this.filterNodeContext.getGmtDifference() : "+00:00";
		ZonedDateTimeRangeDto dateRange = DateHelper.getZonedDateTimeRange(this.dateIsFilterValue, gmtDifference);

		// Convert ZonedDateTime to epoch seconds
		Long startEpochLong = dateRange.getFrom().toEpochSecond();
		Long endEpochLong = dateRange.getTo().toEpochSecond();

		// Apply GMT difference to convert filter values from user's local timezone to UTC
		// (same as IsBetweenFilterNode)
		ZoneOffset offset = ZoneOffset.of(gmtDifference);
		int offsetSeconds = offset.getTotalSeconds();

		// Add offset to convert from user's local date interpretation to UTC
		Integer startEpoch = Math.toIntExact(startEpochLong + offsetSeconds);
		Integer endEpoch = Math.toIntExact(endEpochLong + offsetSeconds);

		/**
		 * Filter timesheets where the requested date range overlaps with the timesheet
		 * period. A timesheet matches if: - The timesheet period_start falls within the
		 * requested range (startEpoch <= period_start <= endEpoch) OR - The timesheet
		 * period_end falls within the requested range (startEpoch <= period_end <=
		 * endEpoch) OR - The timesheet period fully contains the requested range
		 * (period_start <= startEpoch AND period_end >= endEpoch)
		 */
		org.jooq.Condition periodOverlap = this.periodStartField.between(startEpoch, endEpoch)
			.or(this.periodEndField.between(startEpoch, endEpoch))
			.or(this.periodStartField.le(startEpoch).and(this.periodEndField.ge(endEpoch)));

		return List.of(periodOverlap);
	}

	@Override
	public Boolean isSelectDistinct() {
		// Use DISTINCT to avoid duplicate timesheet IDs
		return true;
	}

}
