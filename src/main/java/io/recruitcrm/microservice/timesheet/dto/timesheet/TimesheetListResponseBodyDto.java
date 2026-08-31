package io.recruitcrm.microservice.timesheet.dto.timesheet;

import io.recruitcrm.microservice.timesheet.dto.approver.TimesheetApproversResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.company.CompanyOffLimitResponseBodyDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetListResponseBodyDto {

	@NotNull(message = "Id cannot be null")
	private Integer id;

	@NotNull(message = "Timesheet period cannot be null")
	private TimesheetPeriodResponseBodyDto timesheetPeriod;

	@NotNull(message = "Approval status id cannot be null")
	private Integer timesheetStatusId;

	@NotNull(message = "Job duration cannot be null")
	private JobDurationQueryResultDto jobDuration;

	@NotNull(message = "Pay currency symbol cannot be null")
	private String payCurrencySymbol;

	@NotNull(message = "Bill currency symbol cannot be null")
	private String billCurrencySymbol;

	@NotNull(message = "Pay currency code cannot be null")
	private String payCurrencyCode;

	@NotNull(message = "Bill currency code cannot be null")
	private String billCurrencyCode;

	@NotNull(message = "Pay rate cannot be null")
	private Float payRate;

	@NotNull(message = "Bill rate cannot be null")
	private Float billRate;

	@NotNull(message = "Pay data cannot be null")
	private Float payData;

	@NotNull(message = "Bill data cannot be null")
	private Float billData;

	@NotNull(message = "Approver cannot be null")
	private ApproverResultBodyDto approvedBy;

	@NotNull(message = "Added on date cannot be null")
	private Integer addedOn;

	@NotNull(message = "Updated on date cannot be null")
	private Integer updatedOn;

	@NotNull(message = "Contractor cannot be null")
	private ContractorQueryResultDto contractor;

	// Added since its a requirement for timesheet list page
	private String contractorName;

	@NotNull(message = "Job cannot be null")
	private JobResultBodyDto job;

	// Added since its a requirement for timesheet list page
	private String jobName;

	private Integer payStatusId;

	private Integer payoutPaidOn;

	private String payoutNumber;

	private String payoutFile;

	private Integer billStatusId;

	private String invoiceNumber;

	private Integer invoiceCreatedOn;

	@NotNull(message = "Added by details cannot be null")
	private AddedByResponseBodyDto addedBy;

	@NotNull(message = "Updated by details cannot be null")
	private UpdatedByResponseBodyDto updatedBy;

	private Long totalWorkTime;

	private Long totalOvertime;

	private Long totalTime;

	private String serialNumber;

	private String companyName;

	private String companyLogo;

	private CompanyOffLimitResponseBodyDto company;

	private Integer invoiceStatusId;

	private Byte canCreate;

	private Byte canEdit;

	private Byte canDelete;

	private TimesheetApproversResponseBodyDto approvers;

	private List<DealResponseBodyDto> deals;

	private Integer reimbursementCount;

	private Integer isReimbursementEnabled;

	private Integer isInvoiceCreated;

}