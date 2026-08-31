package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.entity.model.Invoice;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetDealListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAndContractorListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetResponseBodyDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimesheetMapper {

	TimesheetMapper INSTANCE = Mappers.getMapper(TimesheetMapper.class);

	@Mapping(source = "timesheet.id", target = "timesheetId")
	@Mapping(source = "timesheetApproval.timesheetApprovalStatusTypeId", target = "approvalStatusId")
	@Mapping(source = "timesheetInvoice.paymentStatusId", target = "payStatusId")
	@Mapping(source = "timesheetInvoice.paymentPaidOn", target = "payoutPaidOn")
	@Mapping(source = "timesheetInvoice.payoutNumber", target = "payoutNumber")
	@Mapping(source = "timesheetInvoice.billingStatusId", target = "billStatusId")
	@Mapping(source = "invoice.createdOn", target = "invoiceCreatedOn")
	@Mapping(source = "invoice.invoiceIdNumber", target = "invoiceNumber")
	@Mapping(source = "timesheetInvoice.remark", target = "remark")
	@Mapping(source = "timesheetApproval.createdOn", target = "createdOn")
	TimesheetResponseBodyDto toDto(Timesheet timesheet, TimesheetApproval timesheetApproval,
			TimesheetInvoice timesheetInvoice, Invoice invoice);

	@Named("toStringValue")
	default String toStringValue(Object value) {
		return (value != null) ? String.valueOf(value) : null;
	}

	List<TimesheetListResponseBodyDto> listTimeSheetRequestToResponseBodyDto(
			List<TimesheetDealListQueryResultDto> projection);

	List<TimesheetListResponseBodyDto> listTimeSheetJobAndContractorRequestToResponseBodyDto(
			List<TimesheetJobAndContractorListQueryResultDto> projection);

}