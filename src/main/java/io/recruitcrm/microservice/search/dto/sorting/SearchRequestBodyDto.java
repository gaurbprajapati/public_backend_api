package io.recruitcrm.microservice.search.dto.sorting;

import io.recruitcrm.microservice.search.dto.filter.TimesheetPeriodRequestBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestBodyDto {

	private TimesheetPeriodRequestBodyDto timesheetPeriodRequestBodyDto;

	private List<SortPriorityRequestBodyDto> sortPriorityList;

	private List<Integer> timesheetIds;

	private Boolean isSubmitted;

	private Boolean isReimbursement;

}