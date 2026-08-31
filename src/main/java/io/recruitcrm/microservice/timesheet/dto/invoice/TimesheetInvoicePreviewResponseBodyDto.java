package io.recruitcrm.microservice.timesheet.dto.invoice;

import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPeriodResponseBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetInvoicePreviewResponseBodyDto {

	private Integer timesheetId;

	private TimesheetPeriodResponseBodyDto timesheetPeriod;

	private Integer currencyId;

	private Double billAmount;

	private String billCurrencySymbol;

	private String billCurrencyCode;

	private String errorKey;

	private String contractorName;

	private Integer contractorOwnerId;

	private String contractorSlug;

	private String jobSlug;

	private String contractorProfilePicUrl;

	private Integer contractorSerialNumber;

	private Integer timesheetApprovalStatusTypeId;

	private Integer contractorJobAssignmentId;

	private Integer contractorId;

	private Integer jobId;

	private AssociationsResponseBodyDto associations;

	private String payCurrencyCode;

	private String payCurrencySymbol;

	private Integer isReimbursementEnabled;

}
