package io.recruitcrm.microservice.timesheet.search.filters.timesheet.added_on;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.constants.DateIsFilterValue;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.helpers.DateHelper;
import io.recruitcrm.microservice.timesheet.search.helpers.FieldConditionSpecifications;
import io.recruitcrm.microservice.timesheet.search.dto.ZonedDateTimeRangeDto;

public class IsNotFilterNode extends AddedOnFieldBaseFilterNode {

	private Field<Integer> searchField;

	private DateIsFilterValue dateIsFilterValue;

	public IsNotFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.dateIsFilterValue = DateIsFilterValue.fromValue(this.filterNodeContext.getFilterDto().getFilterValue());
		this.searchField = this.getSearchField(Integer.class);
	}

	@Override
	public List<Condition> getFilterConditions() {
		String gmtDifference = (this.filterNodeContext.getGmtDifference() != null)
				? this.filterNodeContext.getGmtDifference() : "+00:00";
		ZonedDateTimeRangeDto dateRange = DateHelper.getZonedDateTimeRange(this.dateIsFilterValue, gmtDifference);
		return List.of(FieldConditionSpecifications.isNotBetween(this.searchField, dateRange));
	}

}
