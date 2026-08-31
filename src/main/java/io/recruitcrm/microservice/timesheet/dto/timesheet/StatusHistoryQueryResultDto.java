package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryQueryResultDto {

	@NotNull(message = "Id cannot be null")
	private Integer id;

	@NotNull(message = "Status cannot be null")
	private Integer status;

	private String remark;

	private Integer updatedByUserTypeId;

	@NotNull(message = "Updated on cannot be null")
	private Integer updatedOn;

	@NotNull(message = "Updated on cannot be null")
	private Integer updatedById;

}
