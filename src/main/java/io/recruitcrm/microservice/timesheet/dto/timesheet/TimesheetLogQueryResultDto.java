package io.recruitcrm.microservice.timesheet.dto.timesheet;

import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.entity.model.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetLogQueryResultDto {

	private Integer timesheetId;

	private Integer timesheetSettingId;

	private Integer workLogType;

	private Integer timesheetFrequency;

	private Integer timesheetStartDay;

	private Boolean calculateBreakTime;

	private Integer breakTimeThreshold;

	private Integer isRemarkMandatory;

	private Integer startDate;

	private Integer endDate;

	private Integer approvalStatusId;

	private Integer paymentStatusId;

	private Integer paymentPaidOn;

	private String payoutNumber;

	private Integer billingStatusId;

	private Integer billingDate;

	private String invoiceNumber;

	private InvoiceStatus invoiceStatus;

	private String remark;

	private Integer updatedOn;

	private String payCurrencySymbol;

	private String payCurrencyCode;

	private String billCurrencySymbol;

	private String billCurrencyCode;

	private Integer userTypeId;

	private Integer entityId;

	private List<TemplateWorkDay> templateWorkDays;

	private List<CustomRule> customRules;

	private Integer isUnplannedHoursPayEnabled;

}
