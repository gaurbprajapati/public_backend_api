package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import io.recruitcrm.microservice.timesheet.dto.timesheet.AddedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdatedByResponseBodyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementListItemResponseBodyDto {

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

	private AddedByResponseBodyDto addedBy;

	private Integer addedOn;

	private UpdatedByResponseBodyDto updatedBy;

	private Integer updatedOn;

	private Integer isSharedWithClient;

}
