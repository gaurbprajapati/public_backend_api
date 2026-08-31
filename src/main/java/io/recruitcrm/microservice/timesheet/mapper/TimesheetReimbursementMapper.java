package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursement;
import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursementStatusHistory;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.CreateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementListItemResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementRequestBodyDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimesheetReimbursementMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "timesheetId", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "isPayable", ignore = true)
	@Mapping(target = "isBillable", ignore = true)
	@Mapping(target = "isSharedWithClient", ignore = true)
	@Mapping(target = "accountId", ignore = true)
	@Mapping(target = "addedBy", ignore = true)
	@Mapping(target = "addedOn", ignore = true)
	@Mapping(target = "addedByUserTypeId", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "updatedOn", ignore = true)
	@Mapping(target = "updatedByUserTypeId", ignore = true)
	TimesheetReimbursement toEntity(CreateReimbursementRequestBodyDto dto);

	@Mapping(source = "reimbursement.id", target = "id")
	@Mapping(source = "reimbursement.timesheetId", target = "timesheetId")
	@Mapping(source = "reimbursement.description", target = "description")
	@Mapping(source = "reimbursement.amount", target = "amount")
	@Mapping(source = "reimbursement.documentToken", target = "documentToken")
	@Mapping(source = "reimbursement.fileName", target = "fileName")
	@Mapping(source = "reimbursement.status", target = "status")
	@Mapping(source = "reimbursement.currencyId", target = "currencyId")
	@Mapping(source = "reimbursement.addedBy", target = "addedBy")
	@Mapping(source = "reimbursement.addedOn", target = "addedOn")
	@Mapping(source = "reimbursement.updatedBy", target = "updatedBy")
	@Mapping(source = "reimbursement.updatedOn", target = "updatedOn")
	@Mapping(source = "reimbursement.isPayable", target = "isPayable")
	@Mapping(source = "reimbursement.isBillable", target = "isBillable")
	@Mapping(source = "reimbursement.isSharedWithClient", target = "isSharedWithClient")
	@Mapping(source = "statusLabel", target = "statusLabel")
	ReimbursementResponseBodyDto toResponseDto(TimesheetReimbursement reimbursement, String statusLabel);

	@Mapping(source = "reimbursement.id", target = "id")
	@Mapping(source = "reimbursement.timesheetId", target = "timesheetId")
	@Mapping(source = "reimbursement.description", target = "description")
	@Mapping(source = "reimbursement.amount", target = "amount")
	@Mapping(source = "reimbursement.documentToken", target = "documentToken")
	@Mapping(source = "reimbursement.fileName", target = "fileName")
	@Mapping(source = "reimbursement.status", target = "status")
	@Mapping(source = "reimbursement.currencyId", target = "currencyId")
	@Mapping(source = "reimbursement.addedOn", target = "addedOn")
	@Mapping(source = "reimbursement.updatedOn", target = "updatedOn")
	@Mapping(source = "reimbursement.isPayable", target = "isPayable")
	@Mapping(source = "reimbursement.isBillable", target = "isBillable")
	@Mapping(source = "reimbursement.isSharedWithClient", target = "isSharedWithClient")
	@Mapping(source = "statusLabel", target = "statusLabel")
	@Mapping(target = "addedBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	ReimbursementListItemResponseBodyDto toListItemResponseDto(TimesheetReimbursement reimbursement,
			String statusLabel);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "timesheetId", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "isPayable", ignore = true)
	@Mapping(target = "isBillable", ignore = true)
	@Mapping(target = "isSharedWithClient", ignore = true)
	@Mapping(target = "accountId", ignore = true)
	@Mapping(target = "addedBy", ignore = true)
	@Mapping(target = "addedOn", ignore = true)
	@Mapping(target = "addedByUserTypeId", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "updatedOn", ignore = true)
	@Mapping(target = "updatedByUserTypeId", ignore = true)
	@Mapping(target = "documentToken", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
	@Mapping(target = "fileName", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
	void updateEntityFromDto(UpdateReimbursementRequestBodyDto dto, @MappingTarget TimesheetReimbursement entity);

	@Mapping(source = "history.id", target = "id")
	@Mapping(source = "history.reimbursementStatusTypeId", target = "status")
	@Mapping(source = "history.remark", target = "remark")
	@Mapping(source = "history.createdOn", target = "createdOn")
	@Mapping(source = "statusLabel", target = "statusLabel")
	@Mapping(target = "createdBy", ignore = true)
	ReimbursementStatusHistoryResponseBodyDto toStatusHistoryResponseDto(TimesheetReimbursementStatusHistory history,
			String statusLabel);

}
