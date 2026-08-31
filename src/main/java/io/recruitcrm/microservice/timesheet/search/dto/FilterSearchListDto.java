package io.recruitcrm.microservice.timesheet.search.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterSearchListDto {

	private List<GroupFilterListDto> groupFilterList;

	private String groupJoinOperator;

}
