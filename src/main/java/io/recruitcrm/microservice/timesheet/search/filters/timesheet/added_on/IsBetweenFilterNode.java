package io.recruitcrm.microservice.timesheet.search.filters.timesheet.added_on;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.helpers.FieldConditionSpecifications;
import io.recruitcrm.microservice.timesheet.search.dto.ZonedDateTimeRangeDto;

public class IsBetweenFilterNode extends AddedOnFieldBaseFilterNode {

	private Field<Integer> searchField;

	public IsBetweenFilterNode(FilterNodeContext filterNodeContext) {
		super(filterNodeContext);
		this.searchField = this.getSearchField(Integer.class);
	}

	@Override
	public List<Condition> getFilterConditions() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(this.filterNodeContext.getFilterDto().getFilterValue());
			Long startEpoch = jsonNode.get("start").asLong();
			Long endEpoch = jsonNode.get("end").asLong();

			String gmtDifference = (this.filterNodeContext.getGmtDifference() != null)
					? this.filterNodeContext.getGmtDifference() : "+00:00";
			ZoneId zoneId = ZoneOffset.of(gmtDifference);
			ZonedDateTime from = Instant.ofEpochSecond(startEpoch).atZone(zoneId);
			ZonedDateTime to = Instant.ofEpochSecond(endEpoch).atZone(zoneId);

			ZonedDateTimeRangeDto dateRange = new ZonedDateTimeRangeDto(from, to);
			return List.of(FieldConditionSpecifications.isBetween(this.searchField, dateRange));
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("Invalid date range format for IS_BETWEEN filter", ex);
		}
	}

}
