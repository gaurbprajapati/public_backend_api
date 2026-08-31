package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAccessControlResponseBodyDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface JobTimesheetAccessMapper {

	JobTimesheetAccessMapper INSTANCE = Mappers.getMapper(JobTimesheetAccessMapper.class);

	@Mapping(source = "canCreate", target = "canCreate")
	@Mapping(source = "canEdit", target = "canEdit")
	@Mapping(source = "canDelete", target = "canDelete")
	TimesheetJobAccessControlResponseBodyDto toResponseDto(JobTimesheetAccess access);

}
