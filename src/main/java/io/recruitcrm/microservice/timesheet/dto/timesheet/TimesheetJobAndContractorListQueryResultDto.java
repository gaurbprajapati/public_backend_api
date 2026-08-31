package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetJobAndContractorListQueryResultDto {

	@NotNull(message = "Timesheet id cannot be null")
	private Integer id;

	private Integer timesheetSettingId;

	@NotNull(message = "Work log type cannot be null")
	private Integer workLogType;

	@NotNull(message = "Start date cannot be null")
	private Integer timesheetPeriodStartDate;

	@NotNull(message = "End date cannot be null")
	private Integer timesheetPeriodEndDate;

	private Integer jobDurationStartDate;

	private Integer jobDurationEndDate;

	private String payCurrencySymbol;

	private String billCurrencySymbol;

	private String payCurrencyCode;

	private String billCurrencyCode;

	private Float payRate;

	private Float billRate;

	private Float payData;

	private Float billData;

	private Integer rateTypeId;

	private Integer addedOn;

	private Integer updatedOn;

	private Integer addedById;

	private Integer updatedById;

	private Integer payStatusId;

	private Integer payoutPaidOn;

	private String payoutNumber;

	private String payoutFile;

	private Integer billStatusId;

	private Integer invoiceCreatedOn;

	private String invoiceNumber;

	private Integer invoiceStatusId;

	private Integer addedByUserTypeId;

	private Integer updatedByUserTypeId;

	private String serialNumber;

	private Byte canCreate;

	private Byte canEdit;

	private Byte canDelete;

	// Contractor fields
	private Integer contractorId;

	private String contractorName;

	private String contractorPhoto;

	private String contractorSlug;

	private String contractorPosition;

	private String contractorOwnerId;

	// Job fields
	private Integer jobId;

	private String jobName;

	private String jobSlug;

	private String companyName;

	private String companyLogo;

	// Company off-limit fields
	private Integer companyOffLimitStatusId;

	private String companyOffLimitReason;

	private Integer companyOffLimitEndDate;

	private Integer companyOffLimitStartDate;

	private String companyStatusLabel;

	private String companyBackgroundColorHex;

	private String companyTextColorHex;

	private String companyMarkedByName;

	// Contractor off-limit fields
	private Integer contractorOffLimitStatusId;

	private String contractorStatusLabel;

	private String contractorBackgroundColorHex;

	private String contractorTextColorHex;

	private String companySlug;

	// Contractor assignment to job ID (null if not assigned)
	private Integer contractorAssignmentId;

	// Job status
	private String jobStatus;

	// Job type
	private String jobType;

	// Total time columns from cst_timesheet_t (in seconds)
	private Integer totalTime;

	private Integer totalWorkTime;

	private Integer totalOvertime;

	private Integer reimbursementCount;

	private Integer isReimbursementEnabled;

}
