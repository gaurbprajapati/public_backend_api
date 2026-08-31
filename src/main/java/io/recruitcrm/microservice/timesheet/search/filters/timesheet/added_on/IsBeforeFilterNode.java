package io.recruitcrm.microservice.timesheet.search.filters.timesheet.added_on;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.helpers.FieldConditionSpecifications;

public class IsBeforeFilterNode extends AddedOnFieldBaseFilterNode {

	private Field<Integer> searchField;

	public IsBeforeFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.searchField = this.getSearchField(Integer.class);
	}

	@Override
	public List<Condition> getFilterConditions() {
		// Parse filterValue as epoch seconds
		Long epochSeconds = Long.parseLong(this.filterNodeContext.getFilterDto().getFilterValue());
		String gmtDifference = (this.filterNodeContext.getGmtDifference() != null)
				? this.filterNodeContext.getGmtDifference() : "+00:00";
		ZoneId zoneId = ZoneOffset.of(gmtDifference);
		ZonedDateTime zonedDateTime = Instant.ofEpochSecond(epochSeconds).atZone(zoneId);
		return List.of(FieldConditionSpecifications.isBefore(this.searchField, zonedDateTime));
	}

}
