package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePayableBillableRequestBodyDto {

	private Integer isPayable;

	private Integer isBillable;

}
