package io.recruitcrm.microservice.timesheet.search.dto;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterDto {

	@NotNull
	private String groupType;

	private String dbField;

	private String filterValue;

	private FilterTypes filterType;

	private String fieldType;

	private String subGroupType;

	private String filterBarLabel;

	private String filterName;

	private Boolean isCrossEntity;

}
