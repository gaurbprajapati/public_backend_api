package io.recruitcrm.microservice.timesheet.search.cte;

import org.jooq.CommonTableExpression;

import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;

public interface IFilterSearchCteProvider {

	CommonTableExpression<?> getCte(FilterSearchListDto filterSearchListDto, Integer accountId, String gmtDifference);

	String getCteName();

}
