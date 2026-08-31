package io.recruitcrm.microservice.timesheet.dto.timesheet;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetSearchRequestBodyDto {

	private String advancedSearchContext;

	private FilterSearchListDto defaultFilterList;

	private FilterSearchListDto filterSearchList;

	private FilterSearchListDto booleanSearchList;

	private List<SortPriorityRequestBodyDto> sortPriorityList;

	@JsonDeserialize(using = IntegerListDeserializer.class)
	private List<Integer> timesheetIds;

	private Boolean isSubmitted;

	private Boolean isReimbursement;

}
