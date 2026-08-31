package io.recruitcrm.microservice.timesheet.services.job;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.dto.job.TimesheetEnabledAssignedCandidateQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.job.TimesheetEnabledAssignedCandidateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ForbiddenAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityTypeEnum;
import io.recruitcrm.microservice.timesheet.repositories.job.JobContractorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobContractorService implements IJobContractorService {

	private final JobContractorRepository jobContractorRepository;

	private final AuthHolder auth;

	public JobContractorService(JobContractorRepository jobContractorRepository, AuthHolder auth) {
		this.jobContractorRepository = jobContractorRepository;
		this.auth = auth;
	}

	@Override
	public List<TimesheetEnabledAssignedCandidateResponseBodyDto> getTimesheetEnabledAssignedCandidates(
			List<Integer> jobIds) {
		if (this.auth.getPrincipalType() == PrincipalType.CONTRACTOR) {
			throw new ForbiddenAccessException(
					"Contractors are not allowed to access timesheet enabled assigned candidates.");
		}

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		List<Integer> ownedJobIds = this.jobContractorRepository.getOwnedJobIds(jobIds, accountId);
		if (ownedJobIds.isEmpty()) {
			throw new ResourceNotFoundException("No accessible jobs found for the provided job IDs.");
		}

		List<TimesheetEnabledAssignedCandidateQueryResultDto> queryResults = this.jobContractorRepository
			.getTimesheetEnabledAssignedCandidates(ownedJobIds, accountId);

		return queryResults.stream().map((queryResult) -> {
			String fullName = this.buildFullName(queryResult.getFirstName(), queryResult.getLastName());

			return TimesheetEnabledAssignedCandidateResponseBodyDto.builder()
				.email(queryResult.getEmailId())
				.entityType(EntityTypeEnum.CANDIDATE.getIdAsString())
				.id(queryResult.getId())
				.photo(queryResult.getProfilePic())
				.slug(queryResult.getSlug())
				.srno(queryResult.getSrno())
				.title(fullName)
				.jobId(queryResult.getJobId())
				.jobStartDate(queryResult.getJobStartDate())
				.jobEndDate(queryResult.getJobEndDate())
				.timesheetFrequency(queryResult.getTimesheetFrequency())
				.timesheetStartDay(queryResult.getTimesheetStartDay())
				.build();
		}).toList();
	}

	private String buildFullName(String firstName, String lastName) {
		if (lastName == null || lastName.trim().isEmpty()) {
			return (firstName != null) ? firstName : "";
		}
		return ((firstName != null) ? firstName : "") + " " + lastName;
	}

}
