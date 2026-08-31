package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.contract_staffing.entity.configuration.Generated;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.dto.approver.ApproverRequestResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingResponseBodyDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Generated
public interface TimesheetSettingMapper {

	TimesheetSettingMapper INSTANCE = Mappers.getMapper(TimesheetSettingMapper.class);

	@Mapping(target = "id", ignore = true)
	@Mapping(source = "jobId", target = "association.jobId")
	@Mapping(source = "request.jobStartDate", target = "jobStartDate")
	@Mapping(source = "request.jobEndDate", target = "jobEndDate")
	@Mapping(source = "request.timesheetFrequency", target = "timesheetFrequency")
	@Mapping(source = "request.payCurrencyId", target = "payCurrencyId")
	@Mapping(source = "request.payRate", target = "payRate")
	@Mapping(source = "request.billCurrencyId", target = "billCurrencyId")
	@Mapping(source = "request.billRate", target = "billRate")
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "createdOn", ignore = true)
	@Mapping(source = "request.workLogType", target = "workLogType")
	@Mapping(source = "request.calculateBreakTime", target = "calculateBreakTime")
	@Mapping(source = "request.isRemarkMandatory", target = "isRemarkMandatory")
	@Mapping(source = "request.isReimbursementEnabled", target = "isReimbursementEnabled")
	@Mapping(source = "request.isClientExpenseSharingEnabled", target = "isClientExpenseSharingEnabled")
	@Mapping(target = "customRule", ignore = true)
	TimesheetSetting toEntity(Integer jobId, TimesheetSettingRequestBodyDto request);

	@Mapping(source = "timesheetSetting", target = ".")
	@Mapping(source = "approvers", target = "approvers", qualifiedByName = "mapApprovers")
	@Mapping(source = "timesheetSetting.createdOn", target = "updatedOn")
	@Mapping(source = "timesheetSetting.createdBy", target = "updatedBy")
	@Mapping(source = "timesheetSetting.isReimbursementEnabled", target = "isReimbursementEnabled")
	@Mapping(source = "timesheetSetting.isClientExpenseSharingEnabled", target = "isClientExpenseSharingEnabled")
	@Mapping(target = "templateWorkDays", ignore = true)
	@Mapping(target = "customRules", ignore = true)
	TimesheetSettingResponseBodyDto timesheetSettingToDtoWithApprovers(TimesheetSetting timesheetSetting,
			List<TimesheetApprover> approvers);

	@Named("mapApprovers")
	default ApproverRequestResponseBodyDto mapApprovers(List<TimesheetApprover> approvers) {
		if (approvers == null || approvers.isEmpty()) {
			return null;
		}

		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		List<Integer> agencyIds = new ArrayList<>();
		List<Integer> clientIds = new ArrayList<>();

		for (TimesheetApprover approver : approvers) {
			if (approver.getUserType() != null
					&& approver.getUserTypeId().equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
				agencyIds.add(approver.getEntityId());
			}
			else {
				clientIds.add(approver.getEntityId());
			}
		}

		dto.setAgencyIds(agencyIds);
		dto.setClientIds(clientIds);
		return dto;
	}

	@Mapping(source = "jobStartDate", target = "jobStartDate")
	@Mapping(source = "jobEndDate", target = "jobEndDate")
	@Mapping(source = "timesheetFrequency", target = "timesheetFrequency")
	@Mapping(source = "timesheetStartDay", target = "timesheetStartDay")
	@Mapping(source = "approvers", target = "approvers")
	@Mapping(source = "payCurrencyId", target = "payCurrencyId")
	@Mapping(source = "payRate", target = "payRate")
	@Mapping(source = "billCurrencyId", target = "billCurrencyId")
	@Mapping(source = "billRate", target = "billRate")
	@Mapping(source = "workDayIds", target = "workDayIds")
	@Mapping(source = "workLogType", target = "workLogType")
	@Mapping(source = "calculateBreakTime", target = "calculateBreakTime")
	@Mapping(source = "isRemarkMandatory", target = "isRemarkMandatory")
	@Mapping(source = "isUnplannedHoursPayEnabled", target = "isUnplannedHoursPayEnabled")
	@Mapping(source = "customRules", target = "customRules")
	@Mapping(source = "isReimbursementEnabled", target = "isReimbursementEnabled")
	@Mapping(source = "isClientExpenseSharingEnabled", target = "isClientExpenseSharingEnabled")
	TimesheetSettingRequestBodyDto toRequestDto(TimesheetSettingBulkRequestBodyDto bulkRequest);

	@Mapping(target = "id", ignore = true)
	@Mapping(source = "source.jobStartDate", target = "jobStartDate")
	@Mapping(source = "source.jobEndDate", target = "jobEndDate")
	@Mapping(source = "source.timesheetFrequency", target = "timesheetFrequency")
	@Mapping(source = "source.timesheetStartDay", target = "timesheetStartDay")
	@Mapping(source = "source.accountId", target = "accountId")
	@Mapping(source = "source.workLogType", target = "workLogType")
	@Mapping(source = "source.calculateBreakTime", target = "calculateBreakTime")
	@Mapping(source = "source.isRemarkMandatory", target = "isRemarkMandatory")
	@Mapping(source = "source.isUnplannedHoursPayEnabled", target = "isUnplannedHoursPayEnabled")
	@Mapping(source = "source.payCurrencyId", target = "payCurrencyId")
	@Mapping(source = "source.billCurrencyId", target = "billCurrencyId")
	@Mapping(source = "source.payRate", target = "payRate")
	@Mapping(source = "source.billRate", target = "billRate")
	@Mapping(source = "source.templateWorkDay", target = "templateWorkDay")
	@Mapping(source = "source.customRule", target = "customRule")
	@Mapping(source = "source.createdOn", target = "createdOn")
	@Mapping(source = "source.createdBy", target = "createdBy")
	@Mapping(source = "source.createdByUserTypeId", target = "createdByUserTypeId")
	@Mapping(source = "source.isReimbursementEnabled", target = "isReimbursementEnabled")
	@Mapping(source = "source.isClientExpenseSharingEnabled", target = "isClientExpenseSharingEnabled")
	TimesheetSetting copyTimesheetSetting(TimesheetSetting source);

}