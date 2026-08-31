package io.recruitcrm.microservice.timesheet.dto.timesheet;

import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.microservice.timesheet.dto.approver.ApproverRequestResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogTotalPayBillResponseBodyDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetResponseBodyDto {

	@NotNull(message = "Timesheet id cannot be null")
	private Integer timesheetId;

	private Integer workLogType;

	private Integer timesheetFrequency;

	private Integer timesheetStartDay;

	private Boolean calculateBreakTime;

	private Integer isRemarkMandatory;

	private Integer breakTimeThreshold;

	@NotNull(message = "Approval status cannot be null")
	private Integer approvalStatusId;

	private Integer payStatusId;

	private Integer payoutPaidOn;

	private String payoutNumber;

	private Integer billStatusId;

	private Integer invoiceCreatedOn;

	private String invoiceNumber;

	private String remark;

	private Integer createdOn;

	private String payCurrencySymbol;

	private String payCurrencyCode;

	private String billCurrencySymbol;

	private String billCurrencyCode;

	private String approvedBy;

	private Integer approvedByUserTypeId;

	private Integer approvedByUserId;

	private ApproverRequestResponseBodyDto approvers;

	private List<TimeLogResponseBodyDto> timeLogs;

	private List<TemplateWorkDay> templateWorkDays;

	private Boolean isWeeklyEnabled;

	private TimeLogTotalPayBillResponseBodyDto timesheetTotalPayBill;

	private Integer invoiceStatusId;

	private Integer isUnplannedHoursPayEnabled;

}