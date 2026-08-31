package io.recruitcrm.microservice.timesheet.dto.entity_columns.locale;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabelResponseBodyDto {

	private String label;

	private String longlabel;

}