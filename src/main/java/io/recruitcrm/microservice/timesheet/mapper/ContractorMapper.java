package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorAddedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorDealQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorOwnerResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorUpdatedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DealResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobResultBodyDto;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractorMapper {

	public List<ContractorListResponseBodyDto> mapToResponseDtos(List<ContractorQueryResultDto> queryResults) {
		return queryResults.stream().map(this::mapToResponseDto).toList();
	}

	private ContractorListResponseBodyDto mapToResponseDto(ContractorQueryResultDto projection) {
		ContractorListResponseBodyDto dto = new ContractorListResponseBodyDto();
		dto.setId(projection.getId());
		dto.setName(projection.getName());
		dto.setEmail(projection.getEmail());
		dto.setPhone(projection.getPhone());
		dto.setCountry(projection.getCountry());
		dto.setAddedOn(projection.getAddedOn());
		dto.setUpdatedOn(projection.getUpdatedOn());

		// Set owner
		if (projection.getOwnerId() != null) {
			dto.setOwner(new ContractorOwnerResponseBodyDto(projection.getOwnerId(), projection.getOwnerName()));
		}

		// Set added by
		if (projection.getAddedById() != null) {
			dto.setAddedBy(
					new ContractorAddedByResponseBodyDto(projection.getAddedById(), projection.getAddedByName()));
		}

		// Set updated by
		if (projection.getUpdatedById() != null) {
			dto.setUpdatedBy(
					new ContractorUpdatedByResponseBodyDto(projection.getUpdatedById(), projection.getUpdatedByName()));
		}

		// Set additional fields
		dto.setTitle(projection.getTitle());
		dto.setCity(projection.getCity());
		dto.setState(projection.getState());
		dto.setPostalCode(projection.getPostalCode());
		dto.setLocality(projection.getLocality());
		dto.setFullAddress(projection.getFullAddress());
		dto.setBirthDate(projection.getBirthDate());
		dto.setCurrentOrganization(projection.getCurrentOrganization());
		dto.setSkills(projection.getSkills());
		dto.setProfilefacebook(projection.getProfilefacebook());
		dto.setProfilegithub(projection.getProfilegithub());
		dto.setProfilelinkedin(projection.getProfilelinkedin());
		dto.setProfiletwitter(projection.getProfiletwitter());
		dto.setProfilexing(projection.getProfilexing());
		dto.setSource(projection.getSource());
		dto.setGender(projection.getGender());

		// Set contractor DTO (like in timesheets)
		io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorQueryResultDto contractorDto = getContractorQueryResultDto(
				projection);
		// Note: assignmentId is not set for contractors as they are always assigned to
		// jobs
		dto.setContractor(contractorDto);

		return dto;
	}

	@NotNull
	private static io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorQueryResultDto getContractorQueryResultDto(
			ContractorQueryResultDto projection) {
		io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorQueryResultDto contractorDto = new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorQueryResultDto();
		contractorDto.setId(projection.getId());
		contractorDto.setName(projection.getName());
		contractorDto.setPhoto(projection.getPhoto());
		contractorDto.setSlug(projection.getSlug());
		contractorDto.setPosition(projection.getPosition());
		contractorDto.setOwnerId((projection.getOwnerId() != null) ? projection.getOwnerId().toString() : null);
		contractorDto.setOffLimitStatusId(projection.getContractorOffLimitStatusId());
		contractorDto.setOffLimitReason(projection.getContractorOffLimitReason());
		contractorDto.setOffLimitEndDate(projection.getContractorOffLimitEndDate());
		contractorDto.setOffLimitStartDate(projection.getContractorOffLimitStartDate());
		contractorDto.setStatusLabel(projection.getContractorStatusLabel());
		contractorDto.setBackgroundColorHex(projection.getContractorBackgroundColorHex());
		contractorDto.setTextColorHex(projection.getContractorTextColorHex());
		contractorDto.setMarkedByName(projection.getContractorMarkedByName());
		return contractorDto;
	}

	public List<JobResultBodyDto> mapJobsToResponseDtos(List<ContractorJobQueryResultDto> contractorJobs) {
		return contractorJobs.stream().map(this::mapJobToResponseDto).toList();
	}

	private JobResultBodyDto mapJobToResponseDto(ContractorJobQueryResultDto job) {
		JobResultBodyDto jobDto = new JobResultBodyDto();
		jobDto.setId(job.getJobId());
		jobDto.setName(job.getJobName());
		jobDto.setSlug(job.getJobSlug());
		jobDto.setCompanyName(job.getCompanyName());
		jobDto.setCompanySlug(job.getCompanySlug());
		jobDto.setStatus(job.getJobStatus());
		jobDto.setSrno(job.getJobSrno());
		return jobDto;
	}

	public List<DealResponseBodyDto> mapDealsToResponseDtos(List<ContractorDealQueryResultDto> contractorDeals) {
		return contractorDeals.stream().map(this::mapDealToResponseDto).toList();
	}

	private DealResponseBodyDto mapDealToResponseDto(ContractorDealQueryResultDto deal) {
		return new DealResponseBodyDto(deal.getDealId(), deal.getDealName(), deal.getOwnerName(),
				deal.getSerialNumber(), deal.getSlug(), deal.getStatus());
	}

}
