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
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAndContractorListQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.ITimesheetRepository;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetSearchTestDataFactory;
import org.jooq.impl.DSL;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TimesheetSearchService Tests")
class TimesheetSearchServiceTests {

	private static final List<Integer> SAMPLE_TIMESHEET_IDS = List.of(1, 2, 3);

	@InjectMocks
	private TimesheetSearchService timesheetSearchService;

	@Mock
	private ITimesheetRepository timesheetRepository;

	@Mock
	private DSLContext auroraDbDSLContext;

	@Mock
	private AccessControlHelper accessControlHelper;

	private Integer accountId;

	private String gmtDifference;

	private Pageable pageable;

	@BeforeEach
	void setUp() {
		this.accountId = TimesheetSearchTestDataFactory.getDefaultAccountId();
		this.gmtDifference = TimesheetSearchTestDataFactory.getDefaultGmtDifference();
		this.pageable = TimesheetSearchTestDataFactory.createDefaultPageable();
		given(this.accessControlHelper.buildCandidatesAccessControlCondition(any())).willReturn(DSL.trueCondition());
	}

	@Test
	@DisplayName("searchTimesheets should return empty list when no timesheet IDs found")
	void testSearchTimesheetsNoIdsFoundReturnsEmptyList() {
		// Given
		FilterSearchListDto filterSearchListDto = TimesheetSearchTestDataFactory.createFilterSearchListDto();
		List<SortPriorityRequestBodyDto> sortPriorityList = Collections.emptyList();
		given(this.timesheetRepository.getTimesheetIdsPage(any(), anyString(), anyList(), any(Pageable.class)))
			.willReturn(Collections.emptyList());

		// When
		List<TimesheetJobAndContractorListQueryResultDto> result = this.timesheetSearchService
			.searchTimesheets(filterSearchListDto, sortPriorityList, this.accountId, this.gmtDifference, this.pageable);

		// Then
		assertThat(result).isNotNull().isEmpty();
		then(this.timesheetRepository).should(never())
			.getTimesheetsListByIds(anyList(), anyList(), any(Pageable.class));
	}

	@Test
	@DisplayName("searchTimesheets should hydrate the resolved page of timesheet IDs")
	void testSearchTimesheetsIdsFoundCallsRepositoryWithIds() {
		// Given
		FilterSearchListDto filterSearchListDto = TimesheetSearchTestDataFactory.createFilterSearchListDto();
		List<SortPriorityRequestBodyDto> sortPriorityList = Collections.emptyList();
		given(this.timesheetRepository.getTimesheetIdsPage(any(), anyString(), anyList(), any(Pageable.class)))
			.willReturn(SAMPLE_TIMESHEET_IDS);
		given(this.timesheetRepository.getTimesheetsListByIds(anyList(), anyList(), any(Pageable.class)))
			.willReturn(Collections.emptyList());

		// When
		this.timesheetSearchService.searchTimesheets(filterSearchListDto, sortPriorityList, this.accountId,
				this.gmtDifference, this.pageable);

		// Then: the page of IDs is hydrated with the same sort
		then(this.timesheetRepository).should()
			.getTimesheetsListByIds(SAMPLE_TIMESHEET_IDS, sortPriorityList, this.pageable);
	}

	@Test
	@DisplayName("searchTimesheets should return repository results when IDs are resolved")
	void testSearchTimesheetsReturnsRepositoryResults() {
		// Given
		FilterSearchListDto filterSearchListDto = TimesheetSearchTestDataFactory.createFilterSearchListDto();
		List<SortPriorityRequestBodyDto> sortPriorityList = Collections.emptyList();
		given(this.timesheetRepository.getTimesheetIdsPage(any(), anyString(), anyList(), any(Pageable.class)))
			.willReturn(SAMPLE_TIMESHEET_IDS);
		TimesheetJobAndContractorListQueryResultDto resultDto = new TimesheetJobAndContractorListQueryResultDto();
		List<TimesheetJobAndContractorListQueryResultDto> expectedResults = List.of(resultDto);
		given(this.timesheetRepository.getTimesheetsListByIds(anyList(), anyList(), any(Pageable.class)))
			.willReturn(expectedResults);

		// When
		List<TimesheetJobAndContractorListQueryResultDto> result = this.timesheetSearchService
			.searchTimesheets(filterSearchListDto, sortPriorityList, this.accountId, this.gmtDifference, this.pageable);

		// Then
		assertThat(result).isNotNull().isEqualTo(expectedResults);
	}

	@Test
	@DisplayName("searchTimesheets should pass sortPriorityList and pageable to the id-page query")
	void testSearchTimesheetsPassesSortAndPageableToIdPageQuery() {
		// Given
		FilterSearchListDto filterSearchListDto = TimesheetSearchTestDataFactory.createFilterSearchListDto();
		SortPriorityRequestBodyDto sortPriorityDto = new SortPriorityRequestBodyDto();
		List<SortPriorityRequestBodyDto> sortPriorityList = List.of(sortPriorityDto);
		Pageable customPageable = PageRequest.of(1, 20);
		given(this.timesheetRepository.getTimesheetIdsPage(any(), anyString(), anyList(), any(Pageable.class)))
			.willReturn(SAMPLE_TIMESHEET_IDS);
		given(this.timesheetRepository.getTimesheetsListByIds(anyList(), anyList(), any(Pageable.class)))
			.willReturn(Collections.emptyList());

		// When
		this.timesheetSearchService.searchTimesheets(filterSearchListDto, sortPriorityList, this.accountId,
				this.gmtDifference, customPageable);

		// Then: sort + pagination are resolved together in the id-page query, and the
		// same
		// sort is threaded to the hydration query.
		then(this.timesheetRepository).should()
			.getTimesheetIdsPage(any(), anyString(), eq(sortPriorityList), eq(customPageable));
		then(this.timesheetRepository).should().getTimesheetsListByIds(anyList(), eq(sortPriorityList), any());
	}

	@Test
	@DisplayName("getTimesheetsCount should return count from database when present")
	void testGetTimesheetsCountReturnsCountWhenPresent() {
		// Given
		FilterSearchListDto filterSearchListDto = TimesheetSearchTestDataFactory.createFilterSearchListDto();
		Record countRecord = mock(Record.class);
		given(countRecord.get(0, Long.class)).willReturn(Long.valueOf(42L));
		given(this.auroraDbDSLContext.fetchOne(any(ResultQuery.class))).willReturn(countRecord);

		// When
		Long result = this.timesheetSearchService.getTimesheetsCount(filterSearchListDto, this.accountId,
				this.gmtDifference);

		// Then
		assertThat(result).isEqualTo(Long.valueOf(42L));
		then(this.accessControlHelper).should().buildCandidatesAccessControlCondition(any());
		then(this.auroraDbDSLContext).should().fetchOne(any(ResultQuery.class));
	}

	@Test
	@DisplayName("getTimesheetsCount should return zero when count field is null")
	void testGetTimesheetsCountNullCountReturnsZero() {
		// Given
		FilterSearchListDto filterSearchListDto = TimesheetSearchTestDataFactory.createFilterSearchListDto();
		Record countRecord = mock(Record.class);
		given(countRecord.get(0, Long.class)).willReturn(null);
		given(this.auroraDbDSLContext.fetchOne(any(ResultQuery.class))).willReturn(countRecord);

		// When
		Long result = this.timesheetSearchService.getTimesheetsCount(filterSearchListDto, this.accountId,
				this.gmtDifference);

		// Then
		assertThat(result).isZero();
		then(this.auroraDbDSLContext).should().fetchOne(any(ResultQuery.class));
	}

	@Test
	@DisplayName("getTimesheetsCount should throw when fetchOne returns null record")
	void testGetTimesheetsCountNullRecordThrowsException() {
		// Given
		FilterSearchListDto filterSearchListDto = TimesheetSearchTestDataFactory.createFilterSearchListDto();
		given(this.auroraDbDSLContext.fetchOne(any(ResultQuery.class))).willReturn(null);

		// When and Then
		assertThatThrownBy(() -> this.timesheetSearchService.getTimesheetsCount(filterSearchListDto, this.accountId,
				this.gmtDifference))
			.isInstanceOf(NullPointerException.class);
	}

}
