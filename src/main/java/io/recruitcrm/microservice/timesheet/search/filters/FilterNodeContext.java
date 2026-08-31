package io.recruitcrm.microservice.timesheet.search.filters;

import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import lombok.Data;

@Data
public class FilterNodeContext {

	private FilterDto filterDto;

	private Integer accountId;

	private String gmtDifference;

}
