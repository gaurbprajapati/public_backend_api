package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.contract_staffing.entity.configuration.Generated;
import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.contract_staffing.entity.model.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.dto.approver.ApproverRequestResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogResponseBodyDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Generated
public interface TimeLogMapper {

	TimeLogMapper INSTANCE = Mappers.getMapper(TimeLogMapper.class);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "date", source = "date")
	@Mapping(target = "dayTypeId", source = "dayTypeId")
	@Mapping(target = "workTime", source = "workTime")
	@Mapping(target = "workStartTime", source = "workStartTime")
	@Mapping(target = "workEndTime", source = "workEndTime")
	@Mapping(target = "overTime", source = "overTime")
	@Mapping(target = "breakTime", source = "breakTime")
	@Mapping(target = "remark", source = "remark")
	@Mapping(target = "totalTime", source = "totalTime")
	TimeLogResponseBodyDto toDto(TimeLog timeLog);

	/**
	 * Maps a list of TimeLog entities to a list of TimeLogResponseBodyDto objects.
	 * @param timeLogs the list of TimeLog entities to map
	 * @return the list of mapped TimeLogResponseBodyDto objects
	 */
	List<TimeLogResponseBodyDto> getTimeLogToDto(List<TimeLog> timeLogs);

	List<TimelogResponseBodyDto> timeLogQueryResultDtoToResponseBodyDto(List<TimelogQueryResultDto> projection);

	// need to remove tushar

	@Named("mapApprovers")
	default ApproverRequestResponseBodyDto mapApprovers(List<TimesheetApprover> approvers) {
		if (approvers == null || approvers.isEmpty()) {
			return null;
		}

		ApproverRequestResponseBodyDto dto = new ApproverRequestResponseBodyDto();
		List<Integer> agencyIds = new ArrayList<>();
		List<Integer> clientIds = new ArrayList<>();

		for (TimesheetApprover approver : approvers) {
			if (approver.getUserTypeId() != null
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

}
