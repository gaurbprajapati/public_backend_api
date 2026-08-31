package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetValidatorQueryResultDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ValidatorMapper {

	ValidatorMapper INSTANCE = Mappers.getMapper(ValidatorMapper.class);

	@Mapping(source = "workLogType", target = "workLogType")
	List<TimesheetAndSettingValidatorResponseBodyDto> timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(
			List<TimesheetAndSettingValidatorQueryResultDto> projection);

	@Mapping(source = "workLogType", target = "workLogType")
	TimesheetAndSettingValidatorResponseBodyDto timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(
			TimesheetAndSettingValidatorQueryResultDto queryResult);

	List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorQueryResultToResponseBodyDto(
			List<ContractorTimesheetValidatorQueryResultDto> queryResults);

	ContractorTimesheetAndSettingValidatorResponseBodyDto contractorQueryResultToResponseBodyDto(
			ContractorTimesheetValidatorQueryResultDto queryResult);

}
