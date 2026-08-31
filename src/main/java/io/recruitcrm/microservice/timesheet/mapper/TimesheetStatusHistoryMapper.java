package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.microservice.timesheet.dto.timesheet.StatusHistoryQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.StatusHistoryResponseBodyDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TimesheetStatusHistoryMapper {

	TimesheetStatusHistoryMapper INSTANCE = Mappers.getMapper(TimesheetStatusHistoryMapper.class);

	List<StatusHistoryResponseBodyDto> toStatusHistoryDtoList(List<TimesheetApproval> approvals);

	List<StatusHistoryResponseBodyDto> toTimesheetStatusResultBodyDto(List<StatusHistoryQueryResultDto> projection);

}