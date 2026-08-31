package io.recruitcrm.microservice.timesheet.dto.contractor;

import java.util.List;

import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractorSearchRequestBodyDto {

	private String advancedSearchContext;

	private FilterSearchListDto defaultFilterList;

	private FilterSearchListDto filterSearchList;

	private FilterSearchListDto booleanSearchList;

	private List<SortPriorityRequestBodyDto> sortPriorityList;

}
