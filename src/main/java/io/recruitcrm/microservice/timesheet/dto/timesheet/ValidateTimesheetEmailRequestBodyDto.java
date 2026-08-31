package io.recruitcrm.microservice.timesheet.dto.timesheet;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTimesheetEmailRequestBodyDto {

	@NotEmpty(message = "atleast one timesheetIds is required")
	@JsonDeserialize(using = IntegerListDeserializer.class)
	private List<Integer> timesheetIds;

	@NotNull(message = "entity_type_id is required")
	@JsonProperty("entity_type_id")
	@JsonAlias({ "entityTypeId" })
	private Integer entityTypeId;

}
