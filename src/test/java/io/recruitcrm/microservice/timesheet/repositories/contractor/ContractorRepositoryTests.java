package io.recruitcrm.microservice.timesheet.repositories.contractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectField;
import org.jooq.SelectLimitAfterOffsetStep;
import org.jooq.SelectLimitPercentAfterOffsetStep;
import org.jooq.SelectLimitStep;
import org.jooq.SelectOrderByStep;
import org.jooq.Table;
import org.jooq.TableLike;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorDealQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.repositories.SortingQueryBuilder;
import io.recruitcrm.microservice.timesheet.testdata.ContractorRepositoryTestDataFactory;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ContractorRepository Tests")
class ContractorRepositoryTests {

	@InjectMocks
	private ContractorRepository contractorRepository;

	@Mock
	private SortingQueryBuilder sortingQueryBuilder;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private DSLContext auroraDbDSLContext;

	@Mock
	private AccessControlHelper accessControlHelper;

	@SuppressWarnings("rawtypes")
	private SelectOrderByStep jooqOrderByStep;

	@SuppressWarnings("rawtypes")
	private SelectLimitAfterOffsetStep jooqAfterOffsetStep;

	@SuppressWarnings("rawtypes")
	private SelectLimitPercentAfterOffsetStep jooqAfterPaginationStep;

	@BeforeEach
	void setUp() {
		this.jooqOrderByStep = mock(SelectOrderByStep.class);
		this.jooqAfterOffsetStep = mock(SelectLimitAfterOffsetStep.class);
		this.jooqAfterPaginationStep = mock(SelectLimitPercentAfterOffsetStep.class,
				withSettings().extraInterfaces(SelectLimitStep.class));
		lenient().when(this.jooqOrderByStep.offset(any(Number.class))).thenReturn(this.jooqAfterOffsetStep);
		lenient().when(this.jooqAfterOffsetStep.limit(any(Number.class))).thenReturn(this.jooqAfterPaginationStep);

		lenient().doReturn(this.jooqOrderByStep)
			.when(this.sortingQueryBuilder)
			.addSortingQuery(any(SelectConditionStep.class), any(List.class), any(java.util.Map.class),
					any(Table.class));

		lenient().when(this.accessControlHelper.buildCandidatesAccessControlCondition(any()))
			.thenReturn(DSL.trueCondition());

		Result<Record> defaultFetchResult = mock(Result.class);
		lenient().when(defaultFetchResult.into(any(Class.class))).thenReturn(Collections.emptyList());
		lenient().when(this.auroraDbDSLContext.fetch(any(ResultQuery.class))).thenReturn(defaultFetchResult);
	}

	@Nested
	@DisplayName("getContractorsListByIds Tests")
	class GetContractorsListByIdsTests {

		@Test
		@DisplayName("Should return empty list when contractor IDs is null")
		void testGetContractorsListByIdsNullIdsReturnsEmptyList() {
			// When
			List<ContractorQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getContractorsListByIds(null, null, ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEmpty();
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should(never()).fetch(any(ResultQuery.class));
		}

		@Test
		@DisplayName("Should return empty list when contractor IDs is empty")
		void testGetContractorsListByIdsEmptyIdsReturnsEmptyList() {
			// When
			List<ContractorQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getContractorsListByIds(Collections.emptyList(), null,
						ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEmpty();
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should(never()).fetch(any(ResultQuery.class));
		}

		@Test
		@DisplayName("Should execute JOOQ query and return contractors when IDs are valid")
		void testGetContractorsListByIdsValidIdsExecutesJooqQueryReturnsContractors() {
			// Given
			List<Integer> contractorIds = Arrays.asList(1, 2, 3);
			List<ContractorQueryResultDto> expectedList = ContractorRepositoryTestDataFactory
				.createContractorQueryResultList();

			Result<Record> mockResult = mock(Result.class);
			given(mockResult.into(ContractorQueryResultDto.class)).willReturn(expectedList);
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<ContractorQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getContractorsListByIds(contractorIds, null,
						ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEqualTo(expectedList);
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should().fetch(any(ResultQuery.class));
		}

		@Test
		@DisplayName("Should use sorting query builder when sort fields are valid")
		void testGetContractorsListByIdsValidSortUsesSortingQueryBuilder() {
			// Given
			List<Integer> contractorIds = Arrays.asList(1, 2);
			List<SortPriorityRequestBodyDto> sortPriorityList = Arrays
				.asList(new SortPriorityRequestBodyDto("updatedOn", "desc"));
			List<ContractorQueryResultDto> expectedList = ContractorRepositoryTestDataFactory
				.createContractorQueryResultList();

			Result<Record> mockResult = mock(Result.class);
			given(mockResult.into(ContractorQueryResultDto.class)).willReturn(expectedList);
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<ContractorQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getContractorsListByIds(contractorIds, sortPriorityList,
						ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEqualTo(expectedList);
			then(ContractorRepositoryTests.this.sortingQueryBuilder).should()
				.addSortingQuery(any(SelectConditionStep.class), any(List.class), any(java.util.Map.class),
						any(Table.class));
		}

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName("Should remap gender sort onto gender id so the column orders by gender id, not label")
		void testGetContractorsListByIdsGenderSortRemappedToGenderId() {
			// Given
			List<Integer> contractorIds = Arrays.asList(1, 2);
			List<SortPriorityRequestBodyDto> sortPriorityList = Arrays
				.asList(new SortPriorityRequestBodyDto("gender", "asc"));

			// When
			ContractorRepositoryTests.this.contractorRepository.getContractorsListByIds(contractorIds, sortPriorityList,
					ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			ArgumentCaptor<List<SortPriorityRequestBodyDto>> sortCaptor = ArgumentCaptor.forClass(List.class);
			then(ContractorRepositoryTests.this.sortingQueryBuilder).should()
				.addSortingQuery(any(SelectConditionStep.class), sortCaptor.capture(), any(java.util.Map.class),
						any(Table.class));
			assertThat(sortCaptor.getValue()).hasSize(1);
			assertThat(sortCaptor.getValue().get(0).getField()).isEqualTo("genderid");
			assertThat(sortCaptor.getValue().get(0).getOrder()).isEqualTo("asc");
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@DisplayName("Job Name sort key should select the first-assigned active job (assignment order), not lowest job serial number")
		void testGetContractorsListByIdsJobNameSortKeyUsesAssignmentOrder() {
			// Given
			List<Integer> contractorIds = Arrays.asList(1, 2);
			List<SortPriorityRequestBodyDto> sortPriorityList = Arrays
				.asList(new SortPriorityRequestBodyDto("jobName", "desc"));

			// When
			ContractorRepositoryTests.this.contractorRepository.getContractorsListByIds(contractorIds, sortPriorityList,
					ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			ArgumentCaptor<SelectConditionStep> baseQueryCaptor = ArgumentCaptor.forClass(SelectConditionStep.class);
			then(ContractorRepositoryTests.this.sortingQueryBuilder).should()
				.addSortingQuery(baseQueryCaptor.capture(), any(List.class), any(java.util.Map.class),
						any(Table.class));
			String sql = baseQueryCaptor.getValue()
				.toString()
				.toLowerCase(Locale.ROOT)
				.replace("\"", "")
				.replace("`", "");
			// The Job Name sort key must mirror the displayed first badge — the
			// first-assigned active job (jobNameAcj.id ascending) — not the lowest job
			// serial number (srno).
			assertThat(sql).doesNotContain("srno").contains("jobnameacj.id");
		}

		@Test
		@DisplayName("Should use default order by when sort field is null")
		void testGetContractorsListByIdsNullSortFieldUsesDefaultOrderBy() {
			// Given
			List<Integer> contractorIds = Arrays.asList(10);
			List<SortPriorityRequestBodyDto> sortPriorityList = Arrays
				.asList(new SortPriorityRequestBodyDto(null, "asc"));
			List<ContractorQueryResultDto> expectedList = ContractorRepositoryTestDataFactory
				.createContractorQueryResultList();

			Result<Record> mockResult = mock(Result.class);
			given(mockResult.into(ContractorQueryResultDto.class)).willReturn(expectedList);
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<ContractorQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getContractorsListByIds(contractorIds, sortPriorityList,
						ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEqualTo(expectedList);
			then(ContractorRepositoryTests.this.sortingQueryBuilder).should(never())
				.addSortingQuery(any(SelectConditionStep.class), any(List.class), any(java.util.Map.class),
						any(Table.class));
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should().fetch(any(ResultQuery.class));
		}

		@Test
		@DisplayName("Should use default order by when sort fields are invalid")
		void testGetContractorsListByIdsInvalidSortFieldsUsesDefaultOrderBy() {
			// Given
			List<Integer> contractorIds = Arrays.asList(10);
			List<SortPriorityRequestBodyDto> sortPriorityList = Arrays
				.asList(new SortPriorityRequestBodyDto("", "asc"));
			List<ContractorQueryResultDto> expectedList = ContractorRepositoryTestDataFactory
				.createContractorQueryResultList();

			Result<Record> mockResult = mock(Result.class);
			given(mockResult.into(ContractorQueryResultDto.class)).willReturn(expectedList);
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<ContractorQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getContractorsListByIds(contractorIds, sortPriorityList,
						ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEqualTo(expectedList);
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should().fetch(any(ResultQuery.class));
		}

		@Test
		@DisplayName("Should use default order by when sort list is empty")
		void testGetContractorsListByIdsEmptySortListUsesDefaultOrderBy() {
			// Given
			List<Integer> contractorIds = Arrays.asList(7);
			List<SortPriorityRequestBodyDto> sortPriorityList = Collections.emptyList();
			List<ContractorQueryResultDto> expectedList = ContractorRepositoryTestDataFactory
				.createContractorQueryResultList();

			Result<Record> mockResult = mock(Result.class);
			given(mockResult.into(ContractorQueryResultDto.class)).willReturn(expectedList);
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<ContractorQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getContractorsListByIds(contractorIds, sortPriorityList,
						ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEqualTo(expectedList);
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should().fetch(any(ResultQuery.class));
		}

	}

	@Nested
	@DisplayName("getContractorIdsPage Tests")
	class GetContractorIdsPageTests {

		private static final String CTE_NAME = "contractorCte";

		private CommonTableExpression<?> buildTestCte() {
			return DSL.name(CTE_NAME)
				.fields("id")
				.as(DSL.select(Tblcandidate.TBLCANDIDATE.ID).from(Tblcandidate.TBLCANDIDATE));
		}

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName("Should sort, paginate and return only the page of contractor IDs")
		void testGetContractorIdsPageSortsPaginatesAndReturnsIds() {
			// Given
			CommonTableExpression<?> cte = buildTestCte();
			List<SortPriorityRequestBodyDto> sortPriorityList = Arrays
				.asList(new SortPriorityRequestBodyDto("updatedOn", "desc"));
			Pageable pageable = PageRequest.of(2, 20);

			Record record1 = mock(Record.class);
			Record record2 = mock(Record.class);
			given(record1.get("id", Integer.class)).willReturn(Integer.valueOf(11));
			given(record2.get("id", Integer.class)).willReturn(Integer.valueOf(22));
			Result<Record> mockResult = mock(Result.class);
			given(mockResult.stream()).willReturn(List.of(record1, record2).stream());
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<Integer> ids = ContractorRepositoryTests.this.contractorRepository.getContractorIdsPage(cte, CTE_NAME,
					sortPriorityList, ContractorRepositoryTestDataFactory.getDefaultAccountId(), pageable);

			// Then: the requested sort is applied and only the paginated page of IDs is
			// returned (that the mapped IDs come back proves the offset/limit chain ran).
			assertThat(ids).containsExactly(11, 22);
			then(ContractorRepositoryTests.this.sortingQueryBuilder).should()
				.addSortingQuery(any(SelectConditionStep.class), any(List.class), any(java.util.Map.class),
						any(Table.class));
		}

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName("Should use default order by and still paginate when sort list is empty")
		void testGetContractorIdsPageEmptySortUsesDefaultOrderBy() {
			// Given
			CommonTableExpression<?> cte = buildTestCte();
			Pageable pageable = PageRequest.of(0, 10);
			Result<Record> mockResult = mock(Result.class);
			given(mockResult.stream()).willReturn(Collections.<Record>emptyList().stream());
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<Integer> ids = ContractorRepositoryTests.this.contractorRepository.getContractorIdsPage(cte, CTE_NAME,
					Collections.emptyList(), ContractorRepositoryTestDataFactory.getDefaultAccountId(), pageable);

			// Then
			assertThat(ids).isEmpty();
			then(ContractorRepositoryTests.this.sortingQueryBuilder).should(never())
				.addSortingQuery(any(SelectConditionStep.class), any(List.class), any(java.util.Map.class),
						any(Table.class));
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should().fetch(any(ResultQuery.class));
		}

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName("Should use default order by and still paginate when sort fields are invalid")
		void testGetContractorIdsPageInvalidSortFieldsUsesDefaultOrderBy() {
			// Given
			CommonTableExpression<?> cte = buildTestCte();
			List<SortPriorityRequestBodyDto> sortPriorityList = Arrays
				.asList(new SortPriorityRequestBodyDto("", "asc"));
			Pageable pageable = PageRequest.of(0, 10);
			Result<Record> mockResult = mock(Result.class);
			given(mockResult.stream()).willReturn(Collections.<Record>emptyList().stream());
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<Integer> ids = ContractorRepositoryTests.this.contractorRepository.getContractorIdsPage(cte, CTE_NAME,
					sortPriorityList, ContractorRepositoryTestDataFactory.getDefaultAccountId(), pageable);

			// Then
			assertThat(ids).isEmpty();
			then(ContractorRepositoryTests.this.sortingQueryBuilder).should(never())
				.addSortingQuery(any(SelectConditionStep.class), any(List.class), any(java.util.Map.class),
						any(Table.class));
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should().fetch(any(ResultQuery.class));
		}

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName("Should remap gender sort onto gender id in the id-page query")
		void testGetContractorIdsPageGenderSortRemappedToGenderId() {
			// Given
			CommonTableExpression<?> cte = buildTestCte();
			List<SortPriorityRequestBodyDto> sortPriorityList = Arrays
				.asList(new SortPriorityRequestBodyDto("gender", "asc"));
			Pageable pageable = PageRequest.of(0, 10);
			Result<Record> mockResult = mock(Result.class);
			given(mockResult.stream()).willReturn(Collections.<Record>emptyList().stream());
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			ContractorRepositoryTests.this.contractorRepository.getContractorIdsPage(cte, CTE_NAME, sortPriorityList,
					ContractorRepositoryTestDataFactory.getDefaultAccountId(), pageable);

			// Then: the id-page query must remap gender the same way the data query does.
			ArgumentCaptor<List<SortPriorityRequestBodyDto>> sortCaptor = ArgumentCaptor.forClass(List.class);
			then(ContractorRepositoryTests.this.sortingQueryBuilder).should()
				.addSortingQuery(any(SelectConditionStep.class), sortCaptor.capture(), any(java.util.Map.class),
						any(Table.class));
			assertThat(sortCaptor.getValue().get(0).getField()).isEqualTo("genderid");
		}

	}

	@Nested
	@DisplayName("getJobsByContractorIds Tests")
	class GetJobsByContractorIdsTests {

		@Test
		@DisplayName("Should return empty list when contractor IDs is null")
		void testGetJobsByContractorIdsNullIdsReturnsEmptyList() {
			// When
			List<ContractorJobQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getJobsByContractorIds(null, ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEmpty();
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should(never()).fetch(any(ResultQuery.class));
		}

		@Test
		@DisplayName("Should return empty list when contractor IDs is empty")
		void testGetJobsByContractorIdsEmptyIdsReturnsEmptyList() {
			// When
			List<ContractorJobQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getJobsByContractorIds(Collections.emptyList(),
						ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEmpty();
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should(never()).fetch(any(ResultQuery.class));
		}

		@Test
		@DisplayName("Should execute JOOQ query and return jobs when IDs are valid")
		void testGetJobsByContractorIdsValidIdsExecutesJooqQueryReturnsJobs() {
			// Given
			List<Integer> contractorIds = Arrays.asList(1, 2);
			List<ContractorJobQueryResultDto> expectedList = ContractorRepositoryTestDataFactory
				.createContractorJobQueryResultList();

			Result<Record> mockResult = mock(Result.class);
			given(mockResult.into(ContractorJobQueryResultDto.class)).willReturn(expectedList);
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<ContractorJobQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getJobsByContractorIds(contractorIds, ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEqualTo(expectedList);
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should().fetch(any(ResultQuery.class));
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@DisplayName("Should order jobs by assignment order so the first badge is deterministic and matches the sort key")
		void testGetJobsByContractorIdsOrdersByAssignmentOrder() {
			// Given
			List<Integer> contractorIds = Arrays.asList(1, 2);

			// When
			ContractorRepositoryTests.this.contractorRepository.getJobsByContractorIds(contractorIds,
					ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			ArgumentCaptor<ResultQuery> queryCaptor = ArgumentCaptor.forClass(ResultQuery.class);
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should().fetch(queryCaptor.capture());
			String sql = queryCaptor.getValue().toString().toLowerCase(Locale.ROOT).replace("\"", "").replace("`", "");
			// The displayed badge order must be deterministic (assignment order) so the
			// first badge matches the Job Name sort key.
			int orderByIndex = sql.indexOf("order by");
			assertThat(orderByIndex).as("getJobsByContractorIds must specify an explicit ORDER BY").isGreaterThan(-1);
			assertThat(sql.substring(orderByIndex)).contains("tblassignjobcandidate.id");
		}

	}

	@Nested
	@DisplayName("getDealsByContractorIds Tests")
	class GetDealsByContractorIdsTests {

		@Test
		@DisplayName("Should return empty list when contractor IDs is null")
		void testGetDealsByContractorIdsNullIdsReturnsEmptyList() {
			// When
			List<ContractorDealQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getDealsByContractorIds(null, ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEmpty();
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should(never()).fetch(any(ResultQuery.class));
		}

		@Test
		@DisplayName("Should return empty list when contractor IDs is empty")
		void testGetDealsByContractorIdsEmptyIdsReturnsEmptyList() {
			// When
			List<ContractorDealQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getDealsByContractorIds(Collections.emptyList(),
						ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEmpty();
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should(never()).fetch(any(ResultQuery.class));
		}

		@Test
		@DisplayName("Should execute JOOQ query and return deals when IDs are valid")
		void testGetDealsByContractorIdsValidIdsExecutesJooqQueryReturnsDeals() {
			// Given
			List<Integer> contractorIds = Arrays.asList(1, 2);
			List<ContractorDealQueryResultDto> expectedList = ContractorRepositoryTestDataFactory
				.createContractorDealQueryResultList();

			Result<Record> mockResult = mock(Result.class);
			given(mockResult.into(ContractorDealQueryResultDto.class)).willReturn(expectedList);
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<ContractorDealQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getDealsByContractorIds(contractorIds, ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isEqualTo(expectedList);
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should().fetch(any(ResultQuery.class));
		}

	}

	@Nested
	@DisplayName("getAllContractorsByAccountId Tests")
	class GetAllContractorsByAccountIdTests {

		@Test
		@DisplayName("Should execute JOOQ query and return contractors for valid account ID")
		void testGetAllContractorsByAccountIdValidAccountIdExecutesJooqQueryReturnsContractors() {
			// Given
			Integer accountId = ContractorRepositoryTestDataFactory.getDefaultAccountId();
			Pageable pageable = PageRequest.of(0, 10);
			List<ContractorQueryResultDto> expectedList = ContractorRepositoryTestDataFactory
				.createContractorQueryResultList();

			Result<Record> mockResult = mock(Result.class);
			given(mockResult.into(ContractorQueryResultDto.class)).willReturn(expectedList);
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<ContractorQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getAllContractorsByAccountId(accountId, null, pageable);

			// Then
			assertThat(result).isEqualTo(expectedList);
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should().fetch(any(ResultQuery.class));
		}

		@Test
		@DisplayName("Should use sorting query builder when sort fields are valid")
		void testGetAllContractorsByAccountIdValidSortUsesSortingQueryBuilder() {
			// Given
			Integer accountId = ContractorRepositoryTestDataFactory.getDefaultAccountId();
			List<SortPriorityRequestBodyDto> sortPriorityList = Arrays
				.asList(new SortPriorityRequestBodyDto("updatedOn", "desc"));
			Pageable pageable = PageRequest.of(1, 5);
			List<ContractorQueryResultDto> expectedList = ContractorRepositoryTestDataFactory
				.createContractorQueryResultList();

			Result<Record> mockResult = mock(Result.class);
			given(mockResult.into(ContractorQueryResultDto.class)).willReturn(expectedList);
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<ContractorQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getAllContractorsByAccountId(accountId, sortPriorityList, pageable);

			// Then
			assertThat(result).isEqualTo(expectedList);
			then(ContractorRepositoryTests.this.sortingQueryBuilder).should()
				.addSortingQuery(any(SelectConditionStep.class), any(List.class), any(java.util.Map.class),
						any(Table.class));
		}

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName("Should remap gender sort onto gender id so the column orders by gender id, not label")
		void testGetAllContractorsByAccountIdGenderSortRemappedToGenderId() {
			// Given
			Integer accountId = ContractorRepositoryTestDataFactory.getDefaultAccountId();
			List<SortPriorityRequestBodyDto> sortPriorityList = Arrays
				.asList(new SortPriorityRequestBodyDto("gender", "desc"));
			Pageable pageable = PageRequest.of(0, 10);

			// When
			ContractorRepositoryTests.this.contractorRepository.getAllContractorsByAccountId(accountId,
					sortPriorityList, pageable);

			// Then
			ArgumentCaptor<List<SortPriorityRequestBodyDto>> sortCaptor = ArgumentCaptor.forClass(List.class);
			then(ContractorRepositoryTests.this.sortingQueryBuilder).should()
				.addSortingQuery(any(SelectConditionStep.class), sortCaptor.capture(), any(java.util.Map.class),
						any(Table.class));
			assertThat(sortCaptor.getValue()).hasSize(1);
			assertThat(sortCaptor.getValue().get(0).getField()).isEqualTo("genderid");
			assertThat(sortCaptor.getValue().get(0).getOrder()).isEqualTo("desc");
		}

		@Test
		@DisplayName("Should use default order by when sort fields are invalid")
		void testGetAllContractorsByAccountIdInvalidSortFieldsUsesDefaultOrderBy() {
			// Given
			Integer accountId = ContractorRepositoryTestDataFactory.getDefaultAccountId();
			List<SortPriorityRequestBodyDto> sortPriorityList = Arrays
				.asList(new SortPriorityRequestBodyDto(null, "asc"));
			Pageable pageable = PageRequest.of(0, 10);
			List<ContractorQueryResultDto> expectedList = ContractorRepositoryTestDataFactory
				.createContractorQueryResultList();

			Result<Record> mockResult = mock(Result.class);
			given(mockResult.into(ContractorQueryResultDto.class)).willReturn(expectedList);
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<ContractorQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getAllContractorsByAccountId(accountId, sortPriorityList, pageable);

			// Then
			assertThat(result).isEqualTo(expectedList);
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should().fetch(any(ResultQuery.class));
		}

		@Test
		@DisplayName("Should use default order by when sort list is empty")
		void testGetAllContractorsByAccountIdEmptySortListUsesDefaultOrderBy() {
			// Given
			Integer accountId = ContractorRepositoryTestDataFactory.getDefaultAccountId();
			List<SortPriorityRequestBodyDto> sortPriorityList = Collections.emptyList();
			Pageable pageable = PageRequest.of(0, 10);
			List<ContractorQueryResultDto> expectedList = ContractorRepositoryTestDataFactory
				.createContractorQueryResultList();

			Result<Record> mockResult = mock(Result.class);
			given(mockResult.into(ContractorQueryResultDto.class)).willReturn(expectedList);
			given(ContractorRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn(mockResult);

			// When
			List<ContractorQueryResultDto> result = ContractorRepositoryTests.this.contractorRepository
				.getAllContractorsByAccountId(accountId, sortPriorityList, pageable);

			// Then
			assertThat(result).isEqualTo(expectedList);
			then(ContractorRepositoryTests.this.auroraDbDSLContext).should().fetch(any(ResultQuery.class));
		}

	}

	@Nested
	@DisplayName("getAllContractorsCountByAccountId Tests")
	class GetAllContractorsCountByAccountIdTests {

		@Test
		@DisplayName("Should return count from JOOQ query for valid account ID")
		void testGetAllContractorsCountByAccountIdValidAccountIdReturnsCount() {
			// Given
			Integer accountId = ContractorRepositoryTestDataFactory.getDefaultAccountId();
			given(ContractorRepositoryTests.this.auroraDbDSLContext.select(any(SelectField.class))
				.from(any(TableLike.class))
				.innerJoin(any(TableLike.class))
				.on(any(Condition.class))
				.innerJoin(any(TableLike.class))
				.on(any(Condition.class))
				.innerJoin(any(TableLike.class))
				.on(any(Condition.class))
				.where(any(Condition.class))
				.and(any(Condition.class))
				.and(any(Condition.class))
				.and(any(Condition.class))
				.fetchOne(0, Long.class)).willReturn(5L);

			// When
			Long result = ContractorRepositoryTests.this.contractorRepository
				.getAllContractorsCountByAccountId(accountId);

			// Then
			assertThat(result).isEqualTo(5L);
			then(ContractorRepositoryTests.this.accessControlHelper).should()
				.buildCandidatesAccessControlCondition(any());
		}

		@Test
		@DisplayName("Should return zero when fetchOne returns null count")
		void testGetAllContractorsCountByAccountIdNullCountReturnsZero() {
			// Given
			given(ContractorRepositoryTests.this.auroraDbDSLContext.select(any(SelectField.class))
				.from(any(TableLike.class))
				.innerJoin(any(TableLike.class))
				.on(any(Condition.class))
				.innerJoin(any(TableLike.class))
				.on(any(Condition.class))
				.innerJoin(any(TableLike.class))
				.on(any(Condition.class))
				.where(any(Condition.class))
				.and(any(Condition.class))
				.and(any(Condition.class))
				.and(any(Condition.class))
				.fetchOne(0, Long.class)).willReturn(null);

			// When
			Long result = ContractorRepositoryTests.this.contractorRepository
				.getAllContractorsCountByAccountId(ContractorRepositoryTestDataFactory.getDefaultAccountId());

			// Then
			assertThat(result).isZero();
		}

	}

}
