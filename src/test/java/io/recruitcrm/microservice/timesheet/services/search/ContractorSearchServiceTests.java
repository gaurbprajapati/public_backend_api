package io.recruitcrm.microservice.timesheet.services.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.util.Collections;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.repositories.contractor.IContractorRepository;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import io.recruitcrm.microservice.timesheet.testdata.ContractorSearchTestDataFactory;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ContractorSearchService Tests")
class ContractorSearchServiceTests {

	private static final List<Integer> SAMPLE_CONTRACTOR_IDS = List.of(1, 2, 3);

	@InjectMocks
	private ContractorSearchService contractorSearchService;

	@Mock
	private IContractorRepository contractorRepository;

	@Mock
	private DSLContext auroraDbDSLContext;

	@Mock
	private AccessControlHelper accessControlHelper;

	private Integer accountId;

	private String gmtDifference;

	private Pageable pageable;

	@BeforeEach
	void setUp() {
		this.accountId = ContractorSearchTestDataFactory.getDefaultAccountId();
		this.gmtDifference = ContractorSearchTestDataFactory.getDefaultGmtDifference();
		this.pageable = ContractorSearchTestDataFactory.createDefaultPageable();
		given(this.accessControlHelper.buildCandidatesAccessControlCondition(any())).willReturn(DSL.trueCondition());
	}

	@Test
	@DisplayName("searchContractors should return empty list when no contractor IDs found")
	void testSearchContractorsNoIdsFoundReturnsEmptyList() {
		// Given
		FilterSearchListDto filterSearchListDto = ContractorSearchTestDataFactory.createFilterSearchListDto();
		List<SortPriorityRequestBodyDto> sortPriorityList = Collections.emptyList();
		given(this.contractorRepository.getContractorIdsPage(any(), anyString(), anyList(), any(Integer.class),
				any(Pageable.class)))
			.willReturn(Collections.emptyList());

		// When
		List<ContractorQueryResultDto> result = this.contractorSearchService.searchContractors(filterSearchListDto,
				sortPriorityList, this.accountId, this.gmtDifference, this.pageable);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.contractorRepository).should(never())
			.getContractorsListByIds(anyList(), anyList(), any(Integer.class));
	}

	@Test
	@DisplayName("searchContractors should hydrate the resolved page of contractor IDs")
	void testSearchContractorsIdsFoundCallsRepositoryWithIds() {
		// Given
		FilterSearchListDto filterSearchListDto = ContractorSearchTestDataFactory.createFilterSearchListDto();
		List<SortPriorityRequestBodyDto> sortPriorityList = Collections.emptyList();
		given(this.contractorRepository.getContractorIdsPage(any(), anyString(), anyList(), any(Integer.class),
				any(Pageable.class)))
			.willReturn(SAMPLE_CONTRACTOR_IDS);
		given(this.contractorRepository.getContractorsListByIds(anyList(), anyList(), any(Integer.class)))
			.willReturn(Collections.emptyList());

		// When
		this.contractorSearchService.searchContractors(filterSearchListDto, sortPriorityList, this.accountId,
				this.gmtDifference, this.pageable);

		// Then: the page of IDs is hydrated with the same sort, without re-paginating
		then(this.contractorRepository).should()
			.getContractorsListByIds(SAMPLE_CONTRACTOR_IDS, sortPriorityList, this.accountId);
	}

	@Test
	@DisplayName("searchContractors should return repository results when IDs are resolved")
	void testSearchContractorsReturnsRepositoryResults() {
		// Given
		FilterSearchListDto filterSearchListDto = ContractorSearchTestDataFactory.createFilterSearchListDto();
		List<SortPriorityRequestBodyDto> sortPriorityList = Collections.emptyList();
		given(this.contractorRepository.getContractorIdsPage(any(), anyString(), anyList(), any(Integer.class),
				any(Pageable.class)))
			.willReturn(SAMPLE_CONTRACTOR_IDS);
		ContractorQueryResultDto resultDto = new ContractorQueryResultDto();
		List<ContractorQueryResultDto> expectedResults = List.of(resultDto);
		given(this.contractorRepository.getContractorsListByIds(anyList(), anyList(), any(Integer.class)))
			.willReturn(expectedResults);

		// When
		List<ContractorQueryResultDto> result = this.contractorSearchService.searchContractors(filterSearchListDto,
				sortPriorityList, this.accountId, this.gmtDifference, this.pageable);

		// Then
		assertThat(result).isNotNull().isEqualTo(expectedResults);
	}

	@Test
	@DisplayName("searchContractors should pass sortPriorityList and pageable to the id-page query")
	void testSearchContractorsPassesSortAndPageableToIdPageQuery() {
		// Given
		FilterSearchListDto filterSearchListDto = ContractorSearchTestDataFactory.createFilterSearchListDto();
		SortPriorityRequestBodyDto sortPriorityDto = new SortPriorityRequestBodyDto();
		List<SortPriorityRequestBodyDto> sortPriorityList = List.of(sortPriorityDto);
		Pageable customPageable = PageRequest.of(1, 20);
		given(this.contractorRepository.getContractorIdsPage(any(), anyString(), anyList(), any(Integer.class),
				any(Pageable.class)))
			.willReturn(SAMPLE_CONTRACTOR_IDS);
		given(this.contractorRepository.getContractorsListByIds(anyList(), anyList(), any(Integer.class)))
			.willReturn(Collections.emptyList());

		// When
		this.contractorSearchService.searchContractors(filterSearchListDto, sortPriorityList, this.accountId,
				this.gmtDifference, customPageable);

		// Then: sort + pagination are resolved together in the id-page query, and the
		// same
		// sort is threaded to the hydration query (which no longer paginates).
		then(this.contractorRepository).should()
			.getContractorIdsPage(any(), anyString(), eq(sortPriorityList), eq(this.accountId), eq(customPageable));
		then(this.contractorRepository).should()
			.getContractorsListByIds(anyList(), eq(sortPriorityList), eq(this.accountId));
	}

	@Test
	@DisplayName("getContractorsCount should return count from database when present")
	void testGetContractorsCountReturnsCountWhenPresent() {
		// Given
		FilterSearchListDto filterSearchListDto = ContractorSearchTestDataFactory.createFilterSearchListDto();
		Record countRecord = mock(Record.class);
		given(countRecord.get(0, Long.class)).willReturn(Long.valueOf(42L));
		given(this.auroraDbDSLContext.fetchOne(any(ResultQuery.class))).willReturn(countRecord);

		// When
		Long result = this.contractorSearchService.getContractorsCount(filterSearchListDto, this.accountId,
				this.gmtDifference);

		// Then
		assertThat(result).isEqualTo(Long.valueOf(42L));
		then(this.accessControlHelper).should().buildCandidatesAccessControlCondition(any());
		then(this.auroraDbDSLContext).should().fetchOne(any(ResultQuery.class));
	}

	@Test
	@DisplayName("getContractorsCount should return zero when count field is null")
	void testGetContractorsCountNullCountReturnsZero() {
		// Given
		FilterSearchListDto filterSearchListDto = ContractorSearchTestDataFactory.createFilterSearchListDto();
		Record countRecord = mock(Record.class);
		given(countRecord.get(0, Long.class)).willReturn(null);
		given(this.auroraDbDSLContext.fetchOne(any(ResultQuery.class))).willReturn(countRecord);

		// When
		Long result = this.contractorSearchService.getContractorsCount(filterSearchListDto, this.accountId,
				this.gmtDifference);

		// Then
		assertThat(result).isZero();
		then(this.auroraDbDSLContext).should().fetchOne(any(ResultQuery.class));
	}

	@Test
	@DisplayName("getContractorsCount should throw when fetchOne returns null record")
	void testGetContractorsCountNullRecordThrowsException() {
		// Given
		FilterSearchListDto filterSearchListDto = ContractorSearchTestDataFactory.createFilterSearchListDto();
		given(this.auroraDbDSLContext.fetchOne(any(ResultQuery.class))).willReturn(null);

		// When and Then
		assertThatThrownBy(() -> this.contractorSearchService.getContractorsCount(filterSearchListDto, this.accountId,
				this.gmtDifference))
			.isInstanceOf(NullPointerException.class);
	}

}
