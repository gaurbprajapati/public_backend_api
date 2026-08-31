package io.recruitcrm.microservice.timesheet.dto.timesheet_setting;

import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import io.recruitcrm.microservice.timesheet.dto.approver.ApproverRequestResponseBodyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Data
public class TimesheetSettingRequestBodyDto {

	@NotNull(message = "Job start date cannot be null")
	private Integer jobStartDate;

	@NotNull(message = "Job end date cannot be null")
	private Integer jobEndDate;

	@NotNull(message = "Timesheet frequency cannot be null")
	private Integer timesheetFrequency;

	private Integer timesheetStartDay;

	@Valid
	@NotNull(message = "Approver's cannot be null")
	private ApproverRequestResponseBodyDto approvers;

	@NotNull(message = "Pay currency id cannot be null")
	private Integer payCurrencyId;

	@NotNull(message = "Pay rate cannot be null")
	private float payRate;

	@NotNull(message = "Bill currency id cannot be null")
	private Integer billCurrencyId;

	@NotNull(message = "Bill rate cannot be null")
	private float billRate;

	@NotNull(message = "Work days cannot be null")
	private List<Integer> workDayIds;

	@NotNull(message = "Work time cannot be null")
	private Integer workLogType;

	@NotNull(message = "Timesheet setting preference cannot be null")
	private Integer isPreferencesModified;

	@NotNull(message = "calculateBreakTime is required and can be either true or false")
	private Boolean calculateBreakTime;

	private Integer breakTimeThreshold;

	@Min(value = 0, message = "isRemarkMandatory must be 0 or 1")
	@Max(value = 1, message = "isRemarkMandatory must be 0 or 1")
	private Integer isRemarkMandatory;

	@Min(value = 0, message = "isUnplannedHoursPayEnabled must be 0 or 1")
	@Max(value = 1, message = "isUnplannedHoursPayEnabled must be 0 or 1")
	private Integer isUnplannedHoursPayEnabled;

	private List<Integer> workTime;

	private List<Integer> workStartTime;

	private List<Integer> workEndTime;

	private List<CustomRule> customRules;

	private Integer isReimbursementEnabled;

	private Integer isClientExpenseSharingEnabled;

}