package io.recruitcrm.microservice.timesheet.dto.timesheet_setting;

import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import io.recruitcrm.contract_staffing.entity.model.TemplateWorkDay;
import io.recruitcrm.microservice.timesheet.dto.approver.ApproverRequestResponseBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetSettingResponseBodyDto {

	private Integer id;

	private Integer jobStartDate;

	private Integer jobEndDate;

	private Integer timesheetFrequency;

	private Integer timesheetStartDay;

	private List<Integer> workDayIds;

	private ApproverRequestResponseBodyDto approvers;

	private Integer payCurrencyId;

	private Integer billCurrencyId;

	private float billRate;

	private float payRate;

	private Integer workLogType;

	private Boolean calculateBreakTime;

	private Integer breakTimeThreshold;

	private Integer isRemarkMandatory;

	private Integer isUnplannedHoursPayEnabled;

	private List<TemplateWorkDay> templateWorkDays;

	private List<CustomRule> customRules;

	private Integer updatedOn;

	private Integer updatedBy;

	private Integer updatedByUserTypeId;

	private Integer enabledOn;

	private Integer enabledBy;

	private Integer enabledByUserTypeId;

	private Integer isReimbursementEnabled;

	private Integer isClientExpenseSharingEnabled;

}