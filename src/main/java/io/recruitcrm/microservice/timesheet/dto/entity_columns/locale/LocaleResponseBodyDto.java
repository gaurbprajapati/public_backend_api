package io.recruitcrm.microservice.timesheet.dto.entity_columns.locale;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocaleResponseBodyDto {

	@JsonProperty("timesheet")
	private Map<String, LabelResponseBodyDto> timesheet;

}