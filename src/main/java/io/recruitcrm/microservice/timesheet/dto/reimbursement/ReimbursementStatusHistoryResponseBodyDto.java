package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import io.recruitcrm.microservice.timesheet.dto.timesheet.AddedByResponseBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementStatusHistoryResponseBodyDto {

	private Integer id;

	private Integer status;

	private String statusLabel;

	private String remark;

	private AddedByResponseBodyDto createdBy;

	private Integer createdOn;

}
