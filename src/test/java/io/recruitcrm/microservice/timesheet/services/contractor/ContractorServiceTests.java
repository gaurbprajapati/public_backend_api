package io.recruitcrm.microservice.timesheet.services.contractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
import io.recruitcrm.microservice.timesheet.testdata.ContractorTestDataFactory;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetSearchTestDataFactory;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractorService Tests")
class ContractorServiceTests {

	@InjectMocks
	private ContractorService contractorService;

	@Mock
	private IContractorRepository contractorRepository;

	@Mock
	private ContractorSearchService contractorSearchService;

	@Mock
	private AuthHolder auth;

	@Mock
	private IContractorTimesheetRepository contractorTimesheetRepository;

	@Mock
	private IUserTimezoneService userTimezoneService;

	@Mock
	private ContractorMapper contractorMapper;

	private Integer accountId;

	private String gmtDifference;

	private Pageable pageable;

	@BeforeEach
	void setUp() {
		this.accountId = ContractorTestDataFactory.getDefaultAccountId();
		this.gmtDifference = TimesheetSearchTestDataFactory.getDefaultGmtDifference();
		this.pageable = PageRequest.of(0, 10);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(this.accountId);
		given(this.userTimezoneService.getCurrentUserGmtDifference()).willReturn(this.gmtDifference);
	}

	@Nested
	@DisplayName("searchContractors Tests")
	class SearchContractorsTests {

		@Test
		@DisplayName("Should use repository getAll when filterSearchList is null")
		void testSearchContractorsNullFilterUsesRepositoryGetAll() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			List<ContractorQueryResultDto> queryResults = ContractorTestDataFactory
				.createContractorQueryResultDtoList();
			List<ContractorListResponseBodyDto> responseDtos = ContractorTestDataFactory
				.createContractorListResponseList();
			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(
					ContractorServiceTests.this.accountId, null, ContractorServiceTests.this.pageable))
				.willReturn(queryResults);
			given(ContractorServiceTests.this.contractorMapper.mapToResponseDtos(queryResults))
				.willReturn(responseDtos);
			this.stubEnrichmentForAllAvailable();

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result).isEqualTo(responseDtos);
			then(ContractorServiceTests.this.contractorRepository).should()
				.getAllContractorsByAccountId(ContractorServiceTests.this.accountId, null,
						ContractorServiceTests.this.pageable);
			then(ContractorServiceTests.this.contractorSearchService).should(never())
				.searchContractors(any(FilterSearchListDto.class), any(), anyInt(), any(), any(Pageable.class));
		}

		@Test
		@DisplayName("Should use search service when filterSearchList is present")
		void testSearchContractorsNonNullFilterUsesSearchService() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithFilterSearchList();
			FilterSearchListDto filterSearchListDto = searchRequest.getFilterSearchList();
			List<ContractorQueryResultDto> queryResults = ContractorTestDataFactory
				.createContractorQueryResultDtoList();
			List<ContractorListResponseBodyDto> responseDtos = ContractorTestDataFactory
				.createContractorListResponseList();
			given(ContractorServiceTests.this.contractorSearchService.searchContractors(filterSearchListDto, null,
					ContractorServiceTests.this.accountId, ContractorServiceTests.this.gmtDifference,
					ContractorServiceTests.this.pageable))
				.willReturn(queryResults);
			given(ContractorServiceTests.this.contractorMapper.mapToResponseDtos(queryResults))
				.willReturn(responseDtos);
			this.stubEnrichmentForAllAvailable();

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result).isEqualTo(responseDtos);
			then(ContractorServiceTests.this.contractorSearchService).should()
				.searchContractors(filterSearchListDto, null, ContractorServiceTests.this.accountId,
						ContractorServiceTests.this.gmtDifference, ContractorServiceTests.this.pageable);
			then(ContractorServiceTests.this.contractorRepository).should(never())
				.getAllContractorsByAccountId(anyInt(), any(), any(Pageable.class));
		}

		@Test
		@DisplayName("Should return empty list when query results are null")
		void testSearchContractorsNullResultsReturnsEmptyList() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(anyInt(), any(),
					any(Pageable.class)))
				.willReturn(null);

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result).isEmpty();
			then(ContractorServiceTests.this.contractorMapper).should(never()).mapToResponseDtos(anyList());
		}

		@Test
		@DisplayName("Should return empty list when query results are empty")
		void testSearchContractorsEmptyResultsReturnsEmptyList() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(anyInt(), any(),
					any(Pageable.class)))
				.willReturn(Collections.emptyList());

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result).isEmpty();
			then(ContractorServiceTests.this.contractorMapper).should(never()).mapToResponseDtos(anyList());
		}

		@Test
		@DisplayName("Should enrich assigned contractor with active jobs and deals")
		void testSearchContractorsAssignedWithActiveJobsAndDealsEnrichesResponse() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			Integer assignedContractorId = ContractorTestDataFactory.getDefaultContractorId();
			List<ContractorQueryResultDto> queryResults = List
				.of(ContractorTestDataFactory.createContractorQueryResultDto());
			ContractorListResponseBodyDto responseDto = ContractorTestDataFactory.createContractorListResponse();
			responseDto.setId(assignedContractorId);
			List<ContractorListResponseBodyDto> responseDtos = List.of(responseDto);

			ContractorJobQueryResultDto activeJob = ContractorTestDataFactory
				.createActiveContractorJobQueryResultDto(assignedContractorId);
			ContractorDealQueryResultDto deal = ContractorTestDataFactory
				.createContractorDealQueryResultDto(assignedContractorId);
			JobResultBodyDto jobDto = new JobResultBodyDto();
			jobDto.setId(ContractorTestDataFactory.getDefaultJobId());
			DealResponseBodyDto dealDto = new DealResponseBodyDto(Integer.valueOf(10), "Test Deal", "Deal Owner",
					Integer.valueOf(100), "test-deal", "Open");

			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(anyInt(), any(),
					any(Pageable.class)))
				.willReturn(queryResults);
			given(ContractorServiceTests.this.contractorMapper.mapToResponseDtos(queryResults))
				.willReturn(responseDtos);
			given(ContractorServiceTests.this.contractorTimesheetRepository.countTimesheetEnabledForContractor(
					eq(assignedContractorId), anyLong(), eq(ContractorServiceTests.this.accountId)))
				.willReturn(Long.valueOf(1L));
			given(ContractorServiceTests.this.contractorRepository.getJobsByContractorIds(List.of(assignedContractorId),
					ContractorServiceTests.this.accountId))
				.willReturn(List.of(activeJob));
			given(ContractorServiceTests.this.contractorMapper.mapJobsToResponseDtos(List.of(activeJob)))
				.willReturn(List.of(jobDto));
			given(ContractorServiceTests.this.contractorRepository
				.getDealsByContractorIds(List.of(assignedContractorId), ContractorServiceTests.this.accountId))
				.willReturn(List.of(deal));
			given(ContractorServiceTests.this.contractorMapper.mapDealsToResponseDtos(List.of(deal)))
				.willReturn(List.of(dealDto));

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result).hasSize(1).first().satisfies((contractor) -> {
				assertThat(contractor.getStatus()).isEqualTo(ContractorStatus.ASSIGNED.getValue());
				assertThat(contractor.getAssignedJobs()).containsExactly(jobDto);
				assertThat(contractor.getDeals()).containsExactly(dealDto);
			});
		}

		@Test
		@DisplayName("Should set empty jobs when assigned contractor has only inactive jobs")
		void testSearchContractorsAssignedWithInactiveJobsReturnsEmptyJobs() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			Integer assignedContractorId = ContractorTestDataFactory.getDefaultContractorId();
			List<ContractorQueryResultDto> queryResults = List
				.of(ContractorTestDataFactory.createContractorQueryResultDto());
			ContractorListResponseBodyDto responseDto = ContractorTestDataFactory.createContractorListResponse();
			responseDto.setId(assignedContractorId);
			List<ContractorListResponseBodyDto> responseDtos = List.of(responseDto);

			ContractorJobQueryResultDto inactiveJob = ContractorTestDataFactory
				.createInactiveContractorJobQueryResultDto(assignedContractorId);

			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(anyInt(), any(),
					any(Pageable.class)))
				.willReturn(queryResults);
			given(ContractorServiceTests.this.contractorMapper.mapToResponseDtos(queryResults))
				.willReturn(responseDtos);
			given(ContractorServiceTests.this.contractorTimesheetRepository.countTimesheetEnabledForContractor(
					eq(assignedContractorId), anyLong(), eq(ContractorServiceTests.this.accountId)))
				.willReturn(Long.valueOf(2L));
			given(ContractorServiceTests.this.contractorRepository.getJobsByContractorIds(List.of(assignedContractorId),
					ContractorServiceTests.this.accountId))
				.willReturn(List.of(inactiveJob));
			given(ContractorServiceTests.this.contractorRepository.getDealsByContractorIds(anyList(), anyInt()))
				.willReturn(Collections.emptyList());

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result).hasSize(1).first().satisfies((contractor) -> {
				assertThat(contractor.getStatus()).isEqualTo(ContractorStatus.ASSIGNED.getValue());
				assertThat(contractor.getAssignedJobs()).isEmpty();
			});
			then(ContractorServiceTests.this.contractorMapper).should(never()).mapJobsToResponseDtos(anyList());
		}

		@Test
		@DisplayName("Should set available status and skip job fetch when no contractors are assigned")
		void testSearchContractorsAllAvailableSetsStatusAndSkipsJobFetch() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			List<ContractorQueryResultDto> queryResults = ContractorTestDataFactory
				.createContractorQueryResultDtoList();
			List<ContractorListResponseBodyDto> responseDtos = ContractorTestDataFactory
				.createContractorListResponseList();
			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(anyInt(), any(),
					any(Pageable.class)))
				.willReturn(queryResults);
			given(ContractorServiceTests.this.contractorMapper.mapToResponseDtos(queryResults))
				.willReturn(responseDtos);
			given(ContractorServiceTests.this.contractorTimesheetRepository.countTimesheetEnabledForContractor(anyInt(),
					anyLong(), anyInt()))
				.willReturn(null);
			given(ContractorServiceTests.this.contractorRepository.getDealsByContractorIds(anyList(), anyInt()))
				.willReturn(Collections.emptyList());

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result).isNotEmpty()
				.allMatch((dto) -> ContractorStatus.AVAILABLE.getValue().equals(dto.getStatus()))
				.allMatch((dto) -> dto.getAssignedJobs().isEmpty())
				.allMatch((dto) -> dto.getDeals().isEmpty());
			then(ContractorServiceTests.this.contractorRepository).should(never())
				.getJobsByContractorIds(anyList(), anyInt());
		}

		@Test
		@DisplayName("Should set empty jobs when assigned contractor has no jobs in repository result")
		void testSearchContractorsAssignedWithNoJobsInMapReturnsEmptyJobs() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			Integer assignedContractorId = ContractorTestDataFactory.getDefaultContractorId();
			List<ContractorQueryResultDto> queryResults = List
				.of(ContractorTestDataFactory.createContractorQueryResultDto());
			ContractorListResponseBodyDto responseDto = ContractorTestDataFactory.createContractorListResponse();
			responseDto.setId(assignedContractorId);
			List<ContractorListResponseBodyDto> responseDtos = List.of(responseDto);

			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(anyInt(), any(),
					any(Pageable.class)))
				.willReturn(queryResults);
			given(ContractorServiceTests.this.contractorMapper.mapToResponseDtos(queryResults))
				.willReturn(responseDtos);
			given(ContractorServiceTests.this.contractorTimesheetRepository.countTimesheetEnabledForContractor(
					eq(assignedContractorId), anyLong(), eq(ContractorServiceTests.this.accountId)))
				.willReturn(Long.valueOf(1L));
			given(ContractorServiceTests.this.contractorRepository.getJobsByContractorIds(List.of(assignedContractorId),
					ContractorServiceTests.this.accountId))
				.willReturn(Collections.emptyList());
			given(ContractorServiceTests.this.contractorRepository.getDealsByContractorIds(anyList(), anyInt()))
				.willReturn(Collections.emptyList());

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result).hasSize(1).first().satisfies((contractor) -> {
				assertThat(contractor.getStatus()).isEqualTo(ContractorStatus.ASSIGNED.getValue());
				assertThat(contractor.getAssignedJobs()).isEmpty();
			});
		}

		@Test
		@DisplayName("Should set empty jobs when assigned contractor has expired jobs")
		void testSearchContractorsAssignedWithExpiredJobsReturnsEmptyJobs() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			Integer assignedContractorId = ContractorTestDataFactory.getDefaultContractorId();
			List<ContractorQueryResultDto> queryResults = List
				.of(ContractorTestDataFactory.createContractorQueryResultDto());
			ContractorListResponseBodyDto responseDto = ContractorTestDataFactory.createContractorListResponse();
			responseDto.setId(assignedContractorId);
			List<ContractorListResponseBodyDto> responseDtos = List.of(responseDto);

			ContractorJobQueryResultDto expiredJob = ContractorTestDataFactory
				.createExpiredContractorJobQueryResultDto(assignedContractorId);

			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(anyInt(), any(),
					any(Pageable.class)))
				.willReturn(queryResults);
			given(ContractorServiceTests.this.contractorMapper.mapToResponseDtos(queryResults))
				.willReturn(responseDtos);
			given(ContractorServiceTests.this.contractorTimesheetRepository.countTimesheetEnabledForContractor(
					eq(assignedContractorId), anyLong(), eq(ContractorServiceTests.this.accountId)))
				.willReturn(Long.valueOf(1L));
			given(ContractorServiceTests.this.contractorRepository.getJobsByContractorIds(List.of(assignedContractorId),
					ContractorServiceTests.this.accountId))
				.willReturn(List.of(expiredJob));
			given(ContractorServiceTests.this.contractorRepository.getDealsByContractorIds(anyList(), anyInt()))
				.willReturn(Collections.emptyList());

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result.get(0).getAssignedJobs()).isEmpty();
			then(ContractorServiceTests.this.contractorMapper).should(never()).mapJobsToResponseDtos(anyList());
		}

		@Test
		@DisplayName("Should set empty jobs when assigned contractor has jobs with null end date")
		void testSearchContractorsAssignedWithNullEndDateJobsReturnsEmptyJobs() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			Integer assignedContractorId = ContractorTestDataFactory.getDefaultContractorId();
			List<ContractorQueryResultDto> queryResults = List
				.of(ContractorTestDataFactory.createContractorQueryResultDto());
			ContractorListResponseBodyDto responseDto = ContractorTestDataFactory.createContractorListResponse();
			responseDto.setId(assignedContractorId);
			List<ContractorListResponseBodyDto> responseDtos = List.of(responseDto);

			ContractorJobQueryResultDto nullEndDateJob = ContractorTestDataFactory
				.createContractorJobQueryResultDtoWithNullEndDate(assignedContractorId);

			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(anyInt(), any(),
					any(Pageable.class)))
				.willReturn(queryResults);
			given(ContractorServiceTests.this.contractorMapper.mapToResponseDtos(queryResults))
				.willReturn(responseDtos);
			given(ContractorServiceTests.this.contractorTimesheetRepository.countTimesheetEnabledForContractor(
					eq(assignedContractorId), anyLong(), eq(ContractorServiceTests.this.accountId)))
				.willReturn(Long.valueOf(1L));
			given(ContractorServiceTests.this.contractorRepository.getJobsByContractorIds(List.of(assignedContractorId),
					ContractorServiceTests.this.accountId))
				.willReturn(List.of(nullEndDateJob));
			given(ContractorServiceTests.this.contractorRepository.getDealsByContractorIds(anyList(), anyInt()))
				.willReturn(Collections.emptyList());

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result.get(0).getAssignedJobs()).isEmpty();
		}

		@Test
		@DisplayName("Should set empty jobs when assigned contractor has jobs with null dates")
		void testSearchContractorsAssignedWithNullDateJobsReturnsEmptyJobs() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			Integer assignedContractorId = ContractorTestDataFactory.getDefaultContractorId();
			List<ContractorQueryResultDto> queryResults = List
				.of(ContractorTestDataFactory.createContractorQueryResultDto());
			ContractorListResponseBodyDto responseDto = ContractorTestDataFactory.createContractorListResponse();
			responseDto.setId(assignedContractorId);
			List<ContractorListResponseBodyDto> responseDtos = List.of(responseDto);

			ContractorJobQueryResultDto nullDateJob = ContractorTestDataFactory
				.createContractorJobQueryResultDtoWithNullDates(assignedContractorId);

			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(anyInt(), any(),
					any(Pageable.class)))
				.willReturn(queryResults);
			given(ContractorServiceTests.this.contractorMapper.mapToResponseDtos(queryResults))
				.willReturn(responseDtos);
			given(ContractorServiceTests.this.contractorTimesheetRepository.countTimesheetEnabledForContractor(
					eq(assignedContractorId), anyLong(), eq(ContractorServiceTests.this.accountId)))
				.willReturn(Long.valueOf(1L));
			given(ContractorServiceTests.this.contractorRepository.getJobsByContractorIds(List.of(assignedContractorId),
					ContractorServiceTests.this.accountId))
				.willReturn(List.of(nullDateJob));
			given(ContractorServiceTests.this.contractorRepository.getDealsByContractorIds(anyList(), anyInt()))
				.willReturn(Collections.emptyList());

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result.get(0).getAssignedJobs()).isEmpty();
			then(ContractorServiceTests.this.contractorMapper).should(never()).mapJobsToResponseDtos(anyList());
		}

		@Test
		@DisplayName("Should set empty deals when contractor has no matching deals in map")
		void testSearchContractorsNoMatchingDealsReturnsEmptyDeals() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			Integer contractorId = ContractorTestDataFactory.getDefaultContractorId();
			Integer otherContractorId = ContractorTestDataFactory.getDefaultSecondaryContractorId();
			List<ContractorQueryResultDto> queryResults = List
				.of(ContractorTestDataFactory.createContractorQueryResultDto());
			ContractorListResponseBodyDto responseDto = ContractorTestDataFactory.createContractorListResponse();
			responseDto.setId(contractorId);
			List<ContractorListResponseBodyDto> responseDtos = List.of(responseDto);

			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(anyInt(), any(),
					any(Pageable.class)))
				.willReturn(queryResults);
			given(ContractorServiceTests.this.contractorMapper.mapToResponseDtos(queryResults))
				.willReturn(responseDtos);
			given(ContractorServiceTests.this.contractorTimesheetRepository.countTimesheetEnabledForContractor(
					eq(contractorId), anyLong(), eq(ContractorServiceTests.this.accountId)))
				.willReturn(Long.valueOf(0L));
			given(ContractorServiceTests.this.contractorRepository.getDealsByContractorIds(anyList(), anyInt()))
				.willReturn(List.of(ContractorTestDataFactory.createContractorDealQueryResultDto(otherContractorId)));

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result.get(0).getDeals()).isEmpty();
			then(ContractorServiceTests.this.contractorMapper).should(never()).mapDealsToResponseDtos(anyList());
		}

		@Test
		@DisplayName("Should set empty deals when contractor has no deals")
		void testSearchContractorsNoDealsReturnsEmptyDeals() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			Integer contractorId = ContractorTestDataFactory.getDefaultContractorId();
			List<ContractorQueryResultDto> queryResults = List
				.of(ContractorTestDataFactory.createContractorQueryResultDto());
			ContractorListResponseBodyDto responseDto = ContractorTestDataFactory.createContractorListResponse();
			responseDto.setId(contractorId);
			List<ContractorListResponseBodyDto> responseDtos = List.of(responseDto);

			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(anyInt(), any(),
					any(Pageable.class)))
				.willReturn(queryResults);
			given(ContractorServiceTests.this.contractorMapper.mapToResponseDtos(queryResults))
				.willReturn(responseDtos);
			given(ContractorServiceTests.this.contractorTimesheetRepository.countTimesheetEnabledForContractor(
					eq(contractorId), anyLong(), eq(ContractorServiceTests.this.accountId)))
				.willReturn(Long.valueOf(0L));
			given(ContractorServiceTests.this.contractorRepository.getDealsByContractorIds(anyList(), anyInt()))
				.willReturn(Collections.emptyList());

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result).hasSize(1).first().satisfies((contractor) -> {
				assertThat(contractor.getStatus()).isEqualTo(ContractorStatus.AVAILABLE.getValue());
				assertThat(contractor.getDeals()).isEmpty();
			});
			then(ContractorServiceTests.this.contractorMapper).should(never()).mapDealsToResponseDtos(anyList());
		}

		@Test
		@DisplayName("Should enrich mixed assigned and available contractors correctly")
		void testSearchContractorsMixedStatusEnrichesJobsOnlyForAssigned() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			Integer assignedContractorId = ContractorTestDataFactory.getDefaultContractorId();
			Integer availableContractorId = ContractorTestDataFactory.getDefaultSecondaryContractorId();
			List<ContractorQueryResultDto> queryResults = ContractorTestDataFactory
				.createContractorQueryResultDtoList();

			ContractorListResponseBodyDto assignedDto = ContractorTestDataFactory.createContractorListResponse();
			assignedDto.setId(assignedContractorId);
			ContractorListResponseBodyDto availableDto = ContractorTestDataFactory.createSecondContractorListResponse();
			availableDto.setId(availableContractorId);
			List<ContractorListResponseBodyDto> responseDtos = Arrays.asList(assignedDto, availableDto);

			ContractorJobQueryResultDto activeJob = ContractorTestDataFactory
				.createActiveContractorJobQueryResultDto(assignedContractorId);
			JobResultBodyDto jobDto = new JobResultBodyDto();
			jobDto.setId(ContractorTestDataFactory.getDefaultJobId());

			given(ContractorServiceTests.this.contractorRepository.getAllContractorsByAccountId(anyInt(), any(),
					any(Pageable.class)))
				.willReturn(queryResults);
			given(ContractorServiceTests.this.contractorMapper.mapToResponseDtos(queryResults))
				.willReturn(responseDtos);
			given(ContractorServiceTests.this.contractorTimesheetRepository.countTimesheetEnabledForContractor(
					eq(assignedContractorId), anyLong(), eq(ContractorServiceTests.this.accountId)))
				.willReturn(Long.valueOf(1L));
			given(ContractorServiceTests.this.contractorTimesheetRepository.countTimesheetEnabledForContractor(
					eq(availableContractorId), anyLong(), eq(ContractorServiceTests.this.accountId)))
				.willReturn(Long.valueOf(0L));
			given(ContractorServiceTests.this.contractorRepository.getJobsByContractorIds(List.of(assignedContractorId),
					ContractorServiceTests.this.accountId))
				.willReturn(List.of(activeJob));
			given(ContractorServiceTests.this.contractorMapper.mapJobsToResponseDtos(List.of(activeJob)))
				.willReturn(List.of(jobDto));
			given(ContractorServiceTests.this.contractorRepository.getDealsByContractorIds(anyList(), anyInt()))
				.willReturn(Collections.emptyList());

			// When
			List<ContractorListResponseBodyDto> result = ContractorServiceTests.this.contractorService
				.searchContractors(searchRequest, ContractorServiceTests.this.pageable);

			// Then
			assertThat(result).hasSize(2).satisfiesExactly((assignedContractor) -> {
				assertThat(assignedContractor.getStatus()).isEqualTo(ContractorStatus.ASSIGNED.getValue());
				assertThat(assignedContractor.getAssignedJobs()).containsExactly(jobDto);
			}, (availableContractor) -> {
				assertThat(availableContractor.getStatus()).isEqualTo(ContractorStatus.AVAILABLE.getValue());
				assertThat(availableContractor.getAssignedJobs()).isEmpty();
			});
		}

		private void stubEnrichmentForAllAvailable() {
			given(ContractorServiceTests.this.contractorTimesheetRepository.countTimesheetEnabledForContractor(anyInt(),
					anyLong(), anyInt()))
				.willReturn(Long.valueOf(0L));
			given(ContractorServiceTests.this.contractorRepository.getDealsByContractorIds(anyList(), anyInt()))
				.willReturn(Collections.emptyList());
		}

	}

	@Nested
	@DisplayName("searchContractorsCount Tests")
	class SearchContractorsCountTests {

		@Test
		@DisplayName("Should use repository count when filterSearchList is null")
		void testSearchContractorsCountNullFilterUsesRepositoryCount() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithoutFilter();
			given(ContractorServiceTests.this.contractorRepository
				.getAllContractorsCountByAccountId(ContractorServiceTests.this.accountId))
				.willReturn(ContractorTestDataFactory.getDefaultContractorCount());

			// When
			Long result = ContractorServiceTests.this.contractorService.searchContractorsCount(searchRequest);

			// Then
			assertThat(result).isEqualTo(ContractorTestDataFactory.getDefaultContractorCount());
			then(ContractorServiceTests.this.contractorRepository).should()
				.getAllContractorsCountByAccountId(ContractorServiceTests.this.accountId);
			then(ContractorServiceTests.this.contractorSearchService).should(never())
				.getContractorsCount(any(FilterSearchListDto.class), anyInt(), any());
		}

		@Test
		@DisplayName("Should use search service count when filterSearchList is present")
		void testSearchContractorsCountFilteredUsesSearchServiceCount() {
			// Given
			ContractorSearchRequestBodyDto searchRequest = ContractorTestDataFactory
				.createContractorSearchRequestWithFilterSearchList();
			FilterSearchListDto filterSearchListDto = searchRequest.getFilterSearchList();
			given(ContractorServiceTests.this.contractorSearchService.getContractorsCount(filterSearchListDto,
					ContractorServiceTests.this.accountId, ContractorServiceTests.this.gmtDifference))
				.willReturn(ContractorTestDataFactory.getSmallContractorCount());

			// When
			Long result = ContractorServiceTests.this.contractorService.searchContractorsCount(searchRequest);

			// Then
			assertThat(result).isEqualTo(ContractorTestDataFactory.getSmallContractorCount());
			then(ContractorServiceTests.this.contractorSearchService).should()
				.getContractorsCount(filterSearchListDto, ContractorServiceTests.this.accountId,
						ContractorServiceTests.this.gmtDifference);
			then(ContractorServiceTests.this.contractorRepository).should(never())
				.getAllContractorsCountByAccountId(anyInt());
		}

	}

}
