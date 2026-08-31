package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.microservice.timesheet.dto.company.CompanyOffLimitResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobDurationQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetDealListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAndContractorListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPeriodResponseBodyDto;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomTimeSheetMapper {

	public List<TimesheetListResponseBodyDto> listTimeSheetJobAndContractorRequestToResponseBodyDto(
			List<TimesheetJobAndContractorListQueryResultDto> projections) {
		return projections.stream().map(this::listTimeSheetJobAndContractorMapToResponseDto).toList();
	}

	public List<TimesheetListResponseBodyDto> listTimeSheetRequestToResponseBodyDto(
			List<TimesheetDealListQueryResultDto> projections) {
		return projections.stream().map(this::listTimeSheetMapToResponseDto).toList();
	}

	private TimesheetListResponseBodyDto listTimeSheetMapToResponseDto(TimesheetDealListQueryResultDto projection) {
		TimesheetListResponseBodyDto dto = new TimesheetListResponseBodyDto();

		dto.setId(projection.getId());

		dto.setTimesheetPeriod(new TimesheetPeriodResponseBodyDto(projection.getTimesheetPeriodStartDate(),
				projection.getTimesheetPeriodEndDate()));

		dto.setJobDuration(new JobDurationQueryResultDto(projection.getJobDurationStartDate(),
				projection.getJobDurationEndDate()));

		ContractorQueryResultDto contractorDto = new ContractorQueryResultDto();
		contractorDto.setId(projection.getContractorId());
		contractorDto.setName(projection.getContractorName());
		contractorDto.setPhoto(projection.getContractorPhoto());
		contractorDto.setSlug(projection.getContractorSlug());
		contractorDto.setPosition(projection.getContractorPosition());
		contractorDto.setOwnerId(projection.getContractorOwnerId());
		contractorDto.setAssignmentId(projection.getContractorAssignmentId());
		// Set contractor off-limit fields
		contractorDto.setOffLimitStatusId(projection.getContractorOffLimitStatusId());
		contractorDto.setStatusLabel(projection.getContractorStatusLabel());
		contractorDto.setBackgroundColorHex(projection.getContractorBackgroundColorHex());
		contractorDto.setTextColorHex(projection.getContractorTextColorHex());
		contractorDto.setOffLimitReason(projection.getContractorOffLimitReason());
		contractorDto.setMarkedByName(projection.getContractorMarkedByName());
		contractorDto.setOffLimitStartDate(projection.getContractorOffLimitStartDate());
		contractorDto.setOffLimitEndDate(projection.getContractorOffLimitEndDate());
		dto.setContractor(contractorDto);

		JobResultBodyDto jobDto = new JobResultBodyDto();
		jobDto.setId(projection.getJobId());
		jobDto.setName(projection.getJobName());
		jobDto.setSlug(projection.getJobSlug());
		jobDto.setIsUnassigned(false);
		jobDto.setCompanyName(projection.getCompanyName());
		jobDto.setCompanySlug(null); // Not available in deal-based query
		jobDto.setStatus(null); // Not available in deal-based query
		jobDto.setJobType(null); // Not available in deal-based query
		dto.setJob(jobDto);

		dto.setPayRate(projection.getPayRate());
		dto.setBillRate(projection.getBillRate());
		dto.setPayData(projection.getPayData());
		dto.setBillData(projection.getBillData());
		dto.setAddedOn(projection.getAddedOn());
		dto.setUpdatedOn(projection.getUpdatedOn());

		dto.setPayStatusId(projection.getPayStatusId());
		dto.setPayoutPaidOn(projection.getPayoutPaidOn());
		dto.setPayoutNumber(projection.getPayoutNumber());
		dto.setPayoutFile(projection.getPayoutFile());
		dto.setBillStatusId(projection.getBillStatusId());
		dto.setInvoiceNumber(projection.getInvoiceNumber());
		dto.setInvoiceCreatedOn(projection.getInvoiceCreatedOn());
		dto.setPayCurrencySymbol(projection.getPayCurrencySymbol());
		dto.setBillCurrencySymbol(projection.getBillCurrencySymbol());
		dto.setPayCurrencyCode(projection.getPayCurrencyCode());
		dto.setBillCurrencyCode(projection.getBillCurrencyCode());
		dto.setSerialNumber(projection.getSerialNumber());
		dto.setCompanyName(projection.getCompanyName());
		dto.setInvoiceStatusId(projection.getInvoiceStatusId());
		dto.setIsReimbursementEnabled(
				(projection.getIsReimbursementEnabled() != null) ? projection.getIsReimbursementEnabled() : 0);
		dto.setReimbursementCount(
				(projection.getReimbursementCount() != null) ? projection.getReimbursementCount() : 0);
		return dto;
	}

	private TimesheetListResponseBodyDto listTimeSheetJobAndContractorMapToResponseDto(
			TimesheetJobAndContractorListQueryResultDto projection) {

		TimesheetListResponseBodyDto timesheetListResponseBodyDto = new TimesheetListResponseBodyDto();

		timesheetListResponseBodyDto.setAddedOn(projection.getAddedOn());
		timesheetListResponseBodyDto.setBillData(projection.getBillData());
		timesheetListResponseBodyDto.setBillRate(projection.getBillRate());
		timesheetListResponseBodyDto.setBillStatusId(projection.getBillStatusId());
		timesheetListResponseBodyDto.setPayCurrencySymbol(projection.getPayCurrencySymbol());
		timesheetListResponseBodyDto.setBillCurrencySymbol(projection.getBillCurrencySymbol());
		timesheetListResponseBodyDto.setPayCurrencyCode(projection.getPayCurrencyCode());
		timesheetListResponseBodyDto.setBillCurrencyCode(projection.getBillCurrencyCode());
		timesheetListResponseBodyDto.setId(projection.getId());
		timesheetListResponseBodyDto.setInvoiceNumber(projection.getInvoiceNumber());
		timesheetListResponseBodyDto.setPayData(projection.getPayData());
		timesheetListResponseBodyDto.setPayRate(projection.getPayRate());
		timesheetListResponseBodyDto.setPayoutPaidOn(projection.getPayoutPaidOn());
		timesheetListResponseBodyDto.setPayStatusId(projection.getPayStatusId());
		timesheetListResponseBodyDto.setPayoutNumber(projection.getPayoutNumber());
		timesheetListResponseBodyDto.setPayoutFile(projection.getPayoutFile());
		timesheetListResponseBodyDto.setUpdatedOn(projection.getUpdatedOn());
		timesheetListResponseBodyDto.setJobDuration(new JobDurationQueryResultDto(projection.getJobDurationStartDate(),
				projection.getJobDurationEndDate()));
		timesheetListResponseBodyDto.setTimesheetPeriod(new TimesheetPeriodResponseBodyDto(
				projection.getTimesheetPeriodStartDate(), projection.getTimesheetPeriodEndDate()));
		timesheetListResponseBodyDto.setSerialNumber(projection.getSerialNumber());
		timesheetListResponseBodyDto.setInvoiceCreatedOn(projection.getInvoiceCreatedOn());
		timesheetListResponseBodyDto.setInvoiceStatusId(projection.getInvoiceStatusId());
		timesheetListResponseBodyDto.setCanCreate(projection.getCanCreate());
		timesheetListResponseBodyDto.setCanEdit(projection.getCanEdit());
		timesheetListResponseBodyDto.setCanDelete(projection.getCanDelete());
		timesheetListResponseBodyDto.setCompanyLogo(projection.getCompanyLogo());
		timesheetListResponseBodyDto.setJobName(projection.getJobName());
		timesheetListResponseBodyDto.setContractorName(projection.getContractorName());
		timesheetListResponseBodyDto
			.setTotalTime((projection.getTotalTime() != null) ? projection.getTotalTime().longValue() : 0L);
		timesheetListResponseBodyDto
			.setTotalWorkTime((projection.getTotalWorkTime() != null) ? projection.getTotalWorkTime().longValue() : 0L);
		timesheetListResponseBodyDto
			.setTotalOvertime((projection.getTotalOvertime() != null) ? projection.getTotalOvertime().longValue() : 0L);
		// Set company off-limit data
		CompanyOffLimitResponseBodyDto companyOffLimitDto = getCompanyOffLimitResponseBodyDto(projection);
		timesheetListResponseBodyDto.setCompany(companyOffLimitDto);

		// Set contractor details
		if (projection.getContractorId() != null) {
			ContractorQueryResultDto contractorDto = getContractorQueryResultDto(projection);
			timesheetListResponseBodyDto.setContractor(contractorDto);
		}

		// Set job details
		if (projection.getJobId() != null) {
			JobResultBodyDto jobDto = new JobResultBodyDto();
			jobDto.setId(projection.getJobId());
			jobDto.setName(projection.getJobName());
			jobDto.setSlug(projection.getJobSlug());
			jobDto.setIsUnassigned(false);
			jobDto.setCompanyName(projection.getCompanyName());
			jobDto.setCompanySlug(projection.getCompanySlug());
			jobDto.setStatus(projection.getJobStatus());
			jobDto.setJobType(projection.getJobType());
			timesheetListResponseBodyDto.setJob(jobDto);
		}
		else {
			timesheetListResponseBodyDto.setJob(null);
		}

		// Set company name
		timesheetListResponseBodyDto.setCompanyName(projection.getCompanyName());

		timesheetListResponseBodyDto.setReimbursementCount(
				(projection.getReimbursementCount() != null) ? projection.getReimbursementCount() : 0);

		timesheetListResponseBodyDto.setIsReimbursementEnabled(
				(projection.getIsReimbursementEnabled() != null) ? projection.getIsReimbursementEnabled() : 0);

		return timesheetListResponseBodyDto;
	}

	@NotNull
	private static CompanyOffLimitResponseBodyDto getCompanyOffLimitResponseBodyDto(
			TimesheetJobAndContractorListQueryResultDto projection) {
		CompanyOffLimitResponseBodyDto companyOffLimitDto = new CompanyOffLimitResponseBodyDto();
		companyOffLimitDto.setOffLimitStatusId(projection.getCompanyOffLimitStatusId());
		companyOffLimitDto.setOffLimitReason(projection.getCompanyOffLimitReason());
		companyOffLimitDto.setOffLimitEndDate(projection.getCompanyOffLimitEndDate());
		companyOffLimitDto.setOffLimitStartDate(projection.getCompanyOffLimitStartDate());
		companyOffLimitDto.setStatusLabel(projection.getCompanyStatusLabel());
		companyOffLimitDto.setBackgroundColorHex(projection.getCompanyBackgroundColorHex());
		companyOffLimitDto.setTextColorHex(projection.getCompanyTextColorHex());
		companyOffLimitDto.setMarkedByName(projection.getCompanyMarkedByName());
		companyOffLimitDto.setName(projection.getCompanyName());
		companyOffLimitDto.setProfilePic(projection.getCompanyLogo());
		companyOffLimitDto.setSlug(projection.getCompanySlug());
		return companyOffLimitDto;
	}

	@NotNull
	private static ContractorQueryResultDto getContractorQueryResultDto(
			TimesheetJobAndContractorListQueryResultDto projection) {
		ContractorQueryResultDto contractorDto = new ContractorQueryResultDto();
		// Set contractor basic fields
		contractorDto.setId(projection.getContractorId());
		contractorDto.setName(projection.getContractorName());
		contractorDto.setPhoto(projection.getContractorPhoto());
		contractorDto.setSlug(projection.getContractorSlug());
		contractorDto.setPosition(projection.getContractorPosition());
		contractorDto.setOwnerId(projection.getContractorOwnerId());
		// Set contractor off-limit fields
		contractorDto.setOffLimitStatusId(projection.getContractorOffLimitStatusId());
		contractorDto.setStatusLabel(projection.getContractorStatusLabel());
		contractorDto.setBackgroundColorHex(projection.getContractorBackgroundColorHex());
		contractorDto.setTextColorHex(projection.getContractorTextColorHex());
		// Set contractor assignment to job ID (null if not assigned)
		contractorDto.setAssignmentId(projection.getContractorAssignmentId());
		return contractorDto;
	}

}
