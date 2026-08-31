package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryResponseBodyDto {

	@NotNull(message = "Id cannot be null")
	private Integer id;

	@NotNull(message = "Status cannot be null")
	private Integer status;

	private String remark;

	@NotNull(message = "Updated on cannot be null")
	private Integer updatedOn;

	private UpdatedByResponseBodyDto updatedBy;

}
