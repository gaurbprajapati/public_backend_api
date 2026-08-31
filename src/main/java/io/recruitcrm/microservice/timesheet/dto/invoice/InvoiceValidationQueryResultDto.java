package io.recruitcrm.microservice.timesheet.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceValidationQueryResultDto {

	private Integer timesheetId;

	private Integer timesheetApprovalStatusTypeId;

	private String companyName;

	private Integer periodStart;

	private Integer periodEnd;

	private Integer currencyId;

	private Double billAmount;

	private String currencySymbol;

	private String currencyCode;

	private String contractorName;

	private String contractorProfilePicUrl;

	private Integer contractorSerialNumber;

	private Integer companyId;

	private Integer jobId;

	private String jobSlug;

	private Integer jobContactId;

	private Integer contractorId;

	private Integer contractorOwnerId;

	private String contractorSlug;

	private Integer dealId;

	private Integer contractorJobAssignmentId;

	private String payCurrencyCode;

	private String payCurrencySymbol;

	private Integer isReimbursementEnabled;

}
