package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDealListQueryResultDto {

	// From CST_TIMESHEET_T
	@NotNull
	private Integer id;

	@NotNull
	private Integer workLogType;

	@NotNull
	private Integer timesheetPeriodStartDate;

	@NotNull
	private Integer timesheetPeriodEndDate;

	@NotNull
	private Integer addedById;

	@NotNull
	private Integer updatedById;

	@NotNull
	private Integer addedByUserTypeId;

	@NotNull
	private Integer updatedByUserTypeId;

	@NotNull
	private Integer addedOn;

	@NotNull
	private Integer updatedOn;

	// From CST_TIMESHEET_SETTING_T
	@NotNull
	private Integer jobDurationStartDate;

	@NotNull
	private Integer jobDurationEndDate;

	private String payCurrencySymbol;

	private String billCurrencySymbol;

	private String payCurrencyCode;

	private String billCurrencyCode;

	@NotNull
	private Float payRate;

	@NotNull
	private Float billRate;

	private Float payData;

	private Float billData;

	private Integer totalTime;

	private Integer totalWorkTime;

	private Integer totalOvertime;

	// From TBLCANDIDATE
	@NotNull
	private Integer contractorId;

	@NotNull
	private String contractorName;

	private String contractorPhoto;

	private String contractorSlug;

	private String contractorPosition;

	private String contractorOwnerId;

	// From TBLASSIGNJOBCANDIDATE
	private Integer contractorAssignmentId;

	// From TBLJOB
	@NotNull
	private Integer jobId;

	@NotNull
	private String jobName;

	@NotNull
	private String jobSlug;

	// From CST_TIMESHEET_INVOICE_T
	private Integer payStatusId;

	private Integer payoutPaidOn;

	private String payoutNumber;

	private String payoutFile;

	private Integer billStatusId;

	private Integer billingDate;

	private String invoiceNumber;

	private String serialNumber;

	private String companyName;

	private Integer invoiceCreatedOn;

	private Integer invoiceStatusId;

	private Integer isReimbursementEnabled;

	private Integer reimbursementCount;

	// Contractor off-limit fields
	private Integer contractorOffLimitStatusId;

	private String contractorStatusLabel;

	private String contractorBackgroundColorHex;

	private String contractorTextColorHex;

	private String contractorOffLimitReason;

	private String contractorMarkedByName;

	private Integer contractorOffLimitStartDate;

	private Integer contractorOffLimitEndDate;

}
