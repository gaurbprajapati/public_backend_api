package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayBillHistoryResponseBodyDto {

	@NotNull(message = "Id cannot be null")
	private Integer id;

	@NotNull(message = "Status id cannot be null")
	private Integer statusId;

	private String remark;

	@NotNull(message = "Updated on cannot be null")
	private Integer updatedOn;

	private UpdatedByResponseBodyDto updatedBy;

}
