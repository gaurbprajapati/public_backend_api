package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementResponseBodyDto {

	private Integer id;

	private Integer timesheetId;

	private String description;

	private BigDecimal amount;

	private String documentToken;

	private String fileName;

	private Integer status;

	private String statusLabel;

	private Integer isPayable;

	private Integer isBillable;

	private Integer currencyId;

	private Integer addedBy;

	private Integer addedOn;

	private Integer updatedBy;

	private Integer updatedOn;

	private Integer isSharedWithClient;

}
