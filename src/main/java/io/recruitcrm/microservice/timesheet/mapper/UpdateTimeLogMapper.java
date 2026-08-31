package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogRequestBodyDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UpdateTimeLogMapper {

	@Mapping(target = "totalTime", expression = "java(calculateTotalTime(dto))")
	void updateTimeLogFromDto(TimeLogRequestBodyDto dto, @MappingTarget TimeLog timeLog);

	default Integer calculateTotalTime(TimeLogRequestBodyDto dto) {
		return dto.getWorkTime() + dto.getOverTime();
	}

}