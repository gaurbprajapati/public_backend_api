package io.recruitcrm.microservice.timesheet.services.contractor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorDealQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DealResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobResultBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.ContractorStatus;
import io.recruitcrm.microservice.timesheet.mapper.ContractorMapper;
import io.recruitcrm.microservice.timesheet.repositories.contractor.IContractorRepository;
import io.recruitcrm.microservice.timesheet.repositories.portals.IContractorTimesheetRepository;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import io.recruitcrm.microservice.timesheet.services.search.ContractorSearchService;
import io.recruitcrm.microservice.timesheet.services.user.IUserTimezoneService;

import java.time.Instant;

@Service
public class ContractorService implements IContractorService {

	private final IContractorRepository contractorRepository;

	private final ContractorSearchService contractorSearchService;

	private final AuthHolder auth;

	private final IContractorTimesheetRepository contractorTimesheetRepository;

	private final IUserTimezoneService userTimezoneService;

	private final ContractorMapper contractorMapper;

	public ContractorService(IContractorRepository contractorRepository,
			ContractorSearchService contractorSearchService, AuthHolder auth,
			IContractorTimesheetRepository contractorTimesheetRepository, IUserTimezoneService userTimezoneService,
			ContractorMapper contractorMapper) {
		this.contractorRepository = contractorRepository;
		this.contractorSearchService = contractorSearchService;
		this.auth = auth;
		this.contractorTimesheetRepository = contractorTimesheetRepository;
		this.userTimezoneService = userTimezoneService;
		this.contractorMapper = contractorMapper;
	}

	@Override
	public List<ContractorListResponseBodyDto> searchContractors(ContractorSearchRequestBodyDto searchRequestBodyDto,
			Pageable pageable) {

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		String gmtDifference = this.userTimezoneService.getCurrentUserGmtDifference();

		FilterSearchListDto filterSearchListDto = searchRequestBodyDto.getFilterSearchList();
		List<io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto> sortPriorityList = searchRequestBodyDto
			.getSortPriorityList();

		// If filterSearchListDto is null, return all contractors with sorting applied if
		// any,
		// otherwise return contractors without filters (sorted by updated_on desc by
		// default)
		List<ContractorQueryResultDto> queryResults;
		if (filterSearchListDto == null) {
			queryResults = this.contractorRepository.getAllContractorsByAccountId(accountId, sortPriorityList,
					pageable);
		}
		else {
			queryResults = this.contractorSearchService.searchContractors(filterSearchListDto, sortPriorityList,
					accountId, gmtDifference, pageable);
		}

		if (queryResults == null || queryResults.isEmpty()) {
			return Collections.emptyList();
		}

		List<ContractorListResponseBodyDto> responseDtos = this.contractorMapper.mapToResponseDtos(queryResults);

		// Enrich with jobs and deals
		List<Integer> contractorIds = queryResults.stream().map(ContractorQueryResultDto::getId).toList();
		this.enrichWithStatus(responseDtos, accountId);
		this.enrichWithJobs(responseDtos, accountId);
		this.enrichWithDeals(responseDtos, contractorIds, accountId);

		return responseDtos;
	}

	private void enrichWithJobs(List<ContractorListResponseBodyDto> responseDtos, Integer accountId) {
		Long currentEpoch = Instant.now().getEpochSecond();

		// Only fetch jobs for ASSIGNED contractors
		List<Integer> assignedContractorIds = responseDtos.stream()
			.filter((dto) -> ContractorStatus.ASSIGNED.getValue().equals(dto.getStatus()))
			.map(ContractorListResponseBodyDto::getId)
			.toList();

		if (assignedContractorIds.isEmpty()) {
			// No assigned contractors, set empty jobs for all
			for (ContractorListResponseBodyDto dto : responseDtos) {
				dto.setAssignedJobs(Collections.emptyList());
			}
			return;
		}

		List<ContractorJobQueryResultDto> jobs = this.contractorRepository.getJobsByContractorIds(assignedContractorIds,
				accountId);

		// Group jobs by contractor ID
		Map<Integer, List<ContractorJobQueryResultDto>> jobsByContractor = jobs.stream()
			.collect(Collectors.groupingBy(ContractorJobQueryResultDto::getContractorId));

		// Map to response DTOs
		for (ContractorListResponseBodyDto dto : responseDtos) {
			// For AVAILABLE contractors, don't return any jobs
			if (ContractorStatus.AVAILABLE.getValue().equals(dto.getStatus())) {
				dto.setAssignedJobs(Collections.emptyList());
				continue;
			}

			// For ASSIGNED contractors, filter jobs where current date is between start
			// and end
			List<ContractorJobQueryResultDto> contractorJobs = jobsByContractor.get(dto.getId());
			if (contractorJobs != null && !contractorJobs.isEmpty()) {
				// Filter jobs where current date is between jobStartDate and jobEndDate
				List<ContractorJobQueryResultDto> activeJobs = contractorJobs.stream()
					.filter((job) -> job.getJobStartDate() != null && job.getJobEndDate() != null
							&& currentEpoch.intValue() >= job.getJobStartDate()
							&& currentEpoch.intValue() <= job.getJobEndDate())
					.toList();

				if (!activeJobs.isEmpty()) {
					List<JobResultBodyDto> jobDtos = this.contractorMapper.mapJobsToResponseDtos(activeJobs);
					dto.setAssignedJobs(jobDtos);
				}
				else {
					dto.setAssignedJobs(Collections.emptyList());
				}
			}
			else {
				dto.setAssignedJobs(Collections.emptyList());
			}
		}
	}

	private void enrichWithDeals(List<ContractorListResponseBodyDto> responseDtos, List<Integer> contractorIds,
			Integer accountId) {
		List<ContractorDealQueryResultDto> deals = this.contractorRepository.getDealsByContractorIds(contractorIds,
				accountId);

		// Group deals by contractor ID
		Map<Integer, List<ContractorDealQueryResultDto>> dealsByContractor = deals.stream()
			.collect(Collectors.groupingBy(ContractorDealQueryResultDto::getContractorId));

		// Map to response DTOs
		for (ContractorListResponseBodyDto dto : responseDtos) {
			List<ContractorDealQueryResultDto> contractorDeals = dealsByContractor.get(dto.getId());
			if (contractorDeals != null && !contractorDeals.isEmpty()) {
				List<DealResponseBodyDto> dealDtos = this.contractorMapper.mapDealsToResponseDtos(contractorDeals);
				dto.setDeals(dealDtos);
			}
			else {
				dto.setDeals(Collections.emptyList());
			}
		}
	}

	private void enrichWithStatus(List<ContractorListResponseBodyDto> responseDtos, Integer accountId) {
		Long currentEpoch = Instant.now().getEpochSecond();

		for (ContractorListResponseBodyDto dto : responseDtos) {
			Integer contractorId = dto.getId();

			// Check if contractor has active timesheet settings (current date between
			// start/end)
			Long activeTimesheetCount = this.contractorTimesheetRepository
				.countTimesheetEnabledForContractor(contractorId, currentEpoch, accountId);

			if (activeTimesheetCount != null && activeTimesheetCount > 0) {
				// Timesheet enabled AND current date is between start/end dates
				dto.setStatus(ContractorStatus.ASSIGNED.getValue());
			}
			else {
				dto.setStatus(ContractorStatus.AVAILABLE.getValue());
			}
		}
	}

	@Override
	public Long searchContractorsCount(ContractorSearchRequestBodyDto searchRequestBodyDto) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		String gmtDifference = this.userTimezoneService.getCurrentUserGmtDifference();

		FilterSearchListDto filterSearchListDto = searchRequestBodyDto.getFilterSearchList();

		// If filterSearchListDto is null, return count of all contractors
		if (filterSearchListDto == null) {
			return this.contractorRepository.getAllContractorsCountByAccountId(accountId);
		}

		// Otherwise, get filtered count using CTE
		return this.contractorSearchService.getContractorsCount(filterSearchListDto, accountId, gmtDifference);
	}

}
