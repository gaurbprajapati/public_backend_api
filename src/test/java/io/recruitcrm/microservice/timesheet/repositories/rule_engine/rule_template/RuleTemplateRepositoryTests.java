/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.rule_engine.rule_template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import io.recruitcrm.contract_staffing.entity.model.RuleTemplate;
import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.search.models.jooq.tables.CstRuleTemplateT;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateNameQueryResultDto;
import io.recruitcrm.microservice.timesheet.repositories.SortingQueryBuilder;
import io.recruitcrm.microservice.timesheet.testdata.RuleTemplateRepositoryTestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectField;
import org.jooq.SelectJoinStep;
import org.jooq.SelectLimitAfterOffsetStep;
import org.jooq.SelectLimitPercentAfterOffsetStep;
import org.jooq.SelectLimitStep;
import org.jooq.SelectOrderByStep;
import org.jooq.SelectSeekStep3;
import org.jooq.SelectSelectStep;
import org.jooq.Table;
import org.jooq.UpdateConditionStep;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for RuleTemplateRepository class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class RuleTemplateRepositoryTests {

	@Mock
	private EntityManager entityManager;

	@Mock
	private DSLContext auroraDbDSLContext;

	@Mock
	private SortingQueryBuilder sortingQueryBuilder;

	@Mock
	private TypedQuery<RuleTemplate> ruleTemplateTypedQuery;

	@Mock
	private TypedQuery<Integer> integerTypedQuery;

	@Mock
	private TypedQuery<RuleTemplateNameQueryResultDto> ruleTemplateNameTypedQuery;

	private RuleTemplateRepository ruleTemplateRepository;

	@BeforeEach
	void setUp() {
		this.ruleTemplateRepository = new RuleTemplateRepository(this.entityManager, this.auroraDbDSLContext,
				this.sortingQueryBuilder);
	}

	@Nested
	@DisplayName("createRuleTemplate Tests")
	class CreateRuleTemplateTests {

		@Test
		@DisplayName("Should persist rule template")
		void testCreateRuleTemplateValidTemplatePersistsTemplate() {
			// Given
			RuleTemplate ruleTemplate = RuleTemplateRepositoryTestDataFactory.createRuleTemplateEntity();

			// When
			RuleTemplateRepositoryTests.this.ruleTemplateRepository.createRuleTemplate(ruleTemplate);

			// Then
			then(RuleTemplateRepositoryTests.this.entityManager).should().persist(ruleTemplate);
		}

	}

	@Nested
	@DisplayName("getRuleTemplate Tests")
	class GetRuleTemplateTests {

		@Test
		@DisplayName("Should return rule template when found")
		void testGetRuleTemplateTemplateFoundReturnsTemplate() {
			// Given
			RuleTemplate ruleTemplate = RuleTemplateRepositoryTestDataFactory.createRuleTemplateEntity();
			List<RuleTemplate> results = List.of(ruleTemplate);

			given(RuleTemplateRepositoryTests.this.entityManager.createQuery(anyString(), eq(RuleTemplate.class)))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateTypedQuery.setParameter("templateId",
					RuleTemplateRepositoryTestDataFactory.DEFAULT_TEMPLATE_ID))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateTypedQuery.setParameter(
					RuleTemplateRepositoryTestDataFactory.JPQL_PARAMETER_ACCOUNT_ID,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateTypedQuery.getResultList()).willReturn(results);

			// When
			RuleTemplate result = RuleTemplateRepositoryTests.this.ruleTemplateRepository.getRuleTemplate(
					RuleTemplateRepositoryTestDataFactory.DEFAULT_TEMPLATE_ID,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(ruleTemplate);
		}

		@Test
		@DisplayName("Should return null when template not found")
		void testGetRuleTemplateTemplateNotFoundReturnsNull() {
			// Given
			given(RuleTemplateRepositoryTests.this.entityManager.createQuery(anyString(), eq(RuleTemplate.class)))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateTypedQuery.setParameter("templateId",
					RuleTemplateRepositoryTestDataFactory.DEFAULT_TEMPLATE_ID))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateTypedQuery.setParameter(
					RuleTemplateRepositoryTestDataFactory.JPQL_PARAMETER_ACCOUNT_ID,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateTypedQuery.getResultList())
				.willReturn(new ArrayList<>());

			// When
			RuleTemplate result = RuleTemplateRepositoryTests.this.ruleTemplateRepository.getRuleTemplate(
					RuleTemplateRepositoryTestDataFactory.DEFAULT_TEMPLATE_ID,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isNull();
		}

	}

	@Nested
	@DisplayName("deleteRuleTemplate Tests")
	class DeleteRuleTemplateTests {

		@Test
		@DisplayName("Should remove rule template when found")
		void testDeleteRuleTemplateTemplateFoundRemovesTemplate() {
			// Given
			RuleTemplate ruleTemplate = RuleTemplateRepositoryTestDataFactory.createRuleTemplateEntity();
			given(RuleTemplateRepositoryTests.this.entityManager.find(RuleTemplate.class,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_TEMPLATE_ID))
				.willReturn(ruleTemplate);

			// When
			RuleTemplateRepositoryTests.this.ruleTemplateRepository
				.deleteRuleTemplate(RuleTemplateRepositoryTestDataFactory.DEFAULT_TEMPLATE_ID);

			// Then
			then(RuleTemplateRepositoryTests.this.entityManager).should().remove(ruleTemplate);
		}

		@Test
		@DisplayName("Should not remove when template not found")
		void testDeleteRuleTemplateTemplateNotFoundDoesNotRemove() {
			// Given
			given(RuleTemplateRepositoryTests.this.entityManager.find(RuleTemplate.class,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_TEMPLATE_ID))
				.willReturn(null);

			// When
			RuleTemplateRepositoryTests.this.ruleTemplateRepository
				.deleteRuleTemplate(RuleTemplateRepositoryTestDataFactory.DEFAULT_TEMPLATE_ID);

			// Then
			then(RuleTemplateRepositoryTests.this.entityManager).should(never()).remove(any());
		}

	}

	@Nested
	@DisplayName("getRuleTemplateNames Tests")
	class GetRuleTemplateNamesTests {

		@Test
		@DisplayName("Should return template names without search filter")
		void testGetRuleTemplateNamesNoSearchReturnsTemplateNames() {
			// Given
			Pageable pageable = PageRequest.of(0, 10);
			RuleTemplateNameQueryResultDto templateName = RuleTemplateRepositoryTestDataFactory
				.createRuleTemplateNameQueryResultDto();
			List<RuleTemplateNameQueryResultDto> results = List.of(templateName);

			given(RuleTemplateRepositoryTests.this.entityManager.createQuery(anyString(),
					eq(RuleTemplateNameQueryResultDto.class)))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.setParameter(
					RuleTemplateRepositoryTestDataFactory.JPQL_PARAMETER_ACCOUNT_ID,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.setFirstResult(0))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.setMaxResults(10))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.getResultList()).willReturn(results);

			// When
			List<RuleTemplateNameQueryResultDto> result = RuleTemplateRepositoryTests.this.ruleTemplateRepository
				.getRuleTemplateNames(null, pageable, RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(results);
		}

		@Test
		@DisplayName("Should return template names without search filter when search is empty")
		void testGetRuleTemplateNamesEmptySearchReturnsTemplateNames() {
			// Given
			Pageable pageable = PageRequest.of(0, 10);
			RuleTemplateNameQueryResultDto templateName = RuleTemplateRepositoryTestDataFactory
				.createRuleTemplateNameQueryResultDto();
			List<RuleTemplateNameQueryResultDto> results = List.of(templateName);

			given(RuleTemplateRepositoryTests.this.entityManager.createQuery(anyString(),
					eq(RuleTemplateNameQueryResultDto.class)))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.setParameter(
					RuleTemplateRepositoryTestDataFactory.JPQL_PARAMETER_ACCOUNT_ID,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.setFirstResult(0))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.setMaxResults(10))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.getResultList()).willReturn(results);

			// When
			List<RuleTemplateNameQueryResultDto> result = RuleTemplateRepositoryTests.this.ruleTemplateRepository
				.getRuleTemplateNames("  ", pageable, RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(results);
		}

		@Test
		@DisplayName("Should return filtered template names with search")
		void testGetRuleTemplateNamesWithSearchReturnsFilteredTemplateNames() {
			// Given
			String search = "Test";
			Pageable pageable = PageRequest.of(0, 10);
			RuleTemplateNameQueryResultDto templateName = RuleTemplateRepositoryTestDataFactory
				.createRuleTemplateNameQueryResultDto();
			List<RuleTemplateNameQueryResultDto> results = List.of(templateName);

			given(RuleTemplateRepositoryTests.this.entityManager.createQuery(anyString(),
					eq(RuleTemplateNameQueryResultDto.class)))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.setParameter(
					RuleTemplateRepositoryTestDataFactory.JPQL_PARAMETER_ACCOUNT_ID,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.setParameter("search", "%Test%"))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.setFirstResult(0))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.setMaxResults(10))
				.willReturn(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery);
			given(RuleTemplateRepositoryTests.this.ruleTemplateNameTypedQuery.getResultList()).willReturn(results);

			// When
			List<RuleTemplateNameQueryResultDto> result = RuleTemplateRepositoryTests.this.ruleTemplateRepository
				.getRuleTemplateNames(search, pageable, RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(results);
		}

	}

	@Nested
	@DisplayName("getAllRuleTemplates Tests")
	class GetAllRuleTemplatesTests {

		private SelectLimitStep paginationBaseStep;

		private SelectLimitAfterOffsetStep afterOffsetStep;

		private SelectLimitPercentAfterOffsetStep afterPaginationStep;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void stubJooqSelectFetchPipeline(Result<?> mockResult,
				List<RuleTemplateListQueryResultDto> expectedList) {
			/**
			 * Production code casts {@code orderBy(...)} to {@link SelectOrderByStep} and
			 * then to {@link SelectLimitStep} to apply pagination; a plain
			 * {@link SelectSeekStep3} mock is not assignable at runtime — register both
			 * super-interfaces.
			 */
			SelectSeekStep3 seekStep = Mockito.mock(SelectSeekStep3.class,
					Mockito.withSettings().extraInterfaces(SelectOrderByStep.class, SelectLimitStep.class));
			SelectSelectStep<?> selectStep = Mockito.mock(SelectSelectStep.class);
			SelectJoinStep<?> joinStep = Mockito.mock(SelectJoinStep.class);
			SelectConditionStep<?> conditionStep = Mockito.mock(SelectConditionStep.class);
			this.afterOffsetStep = Mockito.mock(SelectLimitAfterOffsetStep.class);
			this.afterPaginationStep = Mockito.mock(SelectLimitPercentAfterOffsetStep.class);

			given(RuleTemplateRepositoryTests.this.auroraDbDSLContext.select(any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class)))
				.willReturn((SelectSelectStep) selectStep);
			given(selectStep.from(any(Table.class))).willReturn((SelectJoinStep) joinStep);
			given(joinStep.where(any(Condition.class))).willReturn((SelectConditionStep) conditionStep);
			given(conditionStep.orderBy(any(OrderField.class), any(OrderField.class), any(OrderField.class)))
				.willReturn(seekStep);
			this.paginationBaseStep = (SelectLimitStep) seekStep;
			given(this.paginationBaseStep.offset(anyInt())).willReturn(this.afterOffsetStep);
			given(this.afterOffsetStep.limit(anyInt())).willReturn(this.afterPaginationStep);
			given(RuleTemplateRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn((Result) mockResult);
			given(mockResult.into(RuleTemplateListQueryResultDto.class)).willReturn(expectedList);
		}

		@Test
		@DisplayName("Should use default sort when search request is null")
		void testGetAllRuleTemplatesNullSearchRequestUsesDefaultSort() {
			// Given
			Pageable pageable = PageRequest.of(0, 20);
			List<RuleTemplateListQueryResultDto> expected = List
				.of(RuleTemplateRepositoryTestDataFactory.createRuleTemplateListQueryResultDto());
			Result<?> mockResult = Mockito.mock(Result.class);
			this.stubJooqSelectFetchPipeline(mockResult, expected);

			// When
			List<RuleTemplateListQueryResultDto> result = RuleTemplateRepositoryTests.this.ruleTemplateRepository
				.getAllRuleTemplates(null, null, pageable, RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(expected);
			then(RuleTemplateRepositoryTests.this.sortingQueryBuilder).should(never())
				.addSortingQuery(any(SelectConditionStep.class), any(List.class), any(Table.class));
			// Pagination must be applied: page 0, size 20 -> OFFSET 0 LIMIT 20.
			then(this.paginationBaseStep).should().offset(0);
			then(this.afterOffsetStep).should().limit(20);
		}

		@Test
		@DisplayName("Should use default sort when sort priority list is empty")
		void testGetAllRuleTemplatesEmptySortListUsesDefaultSort() {
			// Given
			SearchRequestBodyDto searchRequest = RuleTemplateRepositoryTestDataFactory
				.createSearchRequestWithEmptySortList();
			Pageable pageable = PageRequest.of(1, 5);
			List<RuleTemplateListQueryResultDto> expected = List
				.of(RuleTemplateRepositoryTestDataFactory.createRuleTemplateListQueryResultDto());
			Result<?> mockResult = Mockito.mock(Result.class);
			this.stubJooqSelectFetchPipeline(mockResult, expected);

			// When
			List<RuleTemplateListQueryResultDto> result = RuleTemplateRepositoryTests.this.ruleTemplateRepository
				.getAllRuleTemplates(searchRequest, null, pageable,
						RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(expected);
			then(RuleTemplateRepositoryTests.this.sortingQueryBuilder).should(never())
				.addSortingQuery(any(SelectConditionStep.class), any(List.class), any(Table.class));
			// Pagination must be applied: page 1, size 5 -> OFFSET 5 LIMIT 5.
			then(this.paginationBaseStep).should().offset(5);
			then(this.afterOffsetStep).should().limit(5);
		}

		@Test
		@DisplayName("Should use default sort when all sort fields are blank")
		void testGetAllRuleTemplatesOnlyInvalidSortFieldsUsesDefaultSort() {
			// Given
			SearchRequestBodyDto searchRequest = RuleTemplateRepositoryTestDataFactory
				.createSearchRequestWithOnlyInvalidSortFields();
			Pageable pageable = PageRequest.of(0, 20);
			List<RuleTemplateListQueryResultDto> expected = List
				.of(RuleTemplateRepositoryTestDataFactory.createRuleTemplateListQueryResultDto());
			Result<?> mockResult = Mockito.mock(Result.class);
			this.stubJooqSelectFetchPipeline(mockResult, expected);

			// When
			List<RuleTemplateListQueryResultDto> result = RuleTemplateRepositoryTests.this.ruleTemplateRepository
				.getAllRuleTemplates(searchRequest, null, pageable,
						RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(expected);
			then(RuleTemplateRepositoryTests.this.sortingQueryBuilder).should(never())
				.addSortingQuery(any(SelectConditionStep.class), any(List.class), any(Table.class));
			// Pagination must be applied: page 0, size 20 -> OFFSET 0 LIMIT 20.
			then(this.paginationBaseStep).should().offset(0);
			then(this.afterOffsetStep).should().limit(20);
		}

		@Test
		@DisplayName("Should delegate to sorting query builder when valid sort field present")
		void testGetAllRuleTemplatesValidSortUsesSortingQueryBuilder() {
			// Given
			SearchRequestBodyDto searchRequest = RuleTemplateRepositoryTestDataFactory
				.createSearchRequestWithValidSortField();
			Pageable pageable = PageRequest.of(0, 20);
			List<RuleTemplateListQueryResultDto> expected = List
				.of(RuleTemplateRepositoryTestDataFactory.createRuleTemplateListQueryResultDto());

			SelectSelectStep<?> selectStep = Mockito.mock(SelectSelectStep.class);
			SelectJoinStep<?> joinStep = Mockito.mock(SelectJoinStep.class);
			SelectConditionStep<?> conditionStep = Mockito.mock(SelectConditionStep.class);
			// The sorted query is cast to SelectLimitStep for pagination, so the mock
			// must
			// implement it too.
			SelectOrderByStep<?> orderByStep = Mockito.mock(SelectOrderByStep.class,
					Mockito.withSettings().extraInterfaces(SelectLimitStep.class));
			this.afterOffsetStep = Mockito.mock(SelectLimitAfterOffsetStep.class);
			this.afterPaginationStep = Mockito.mock(SelectLimitPercentAfterOffsetStep.class);
			Result<?> mockResult = Mockito.mock(Result.class);

			given(RuleTemplateRepositoryTests.this.auroraDbDSLContext.select(any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class),
					any(SelectField.class), any(SelectField.class), any(SelectField.class), any(SelectField.class)))
				.willReturn((SelectSelectStep) selectStep);
			given(selectStep.from(any(Table.class))).willReturn((SelectJoinStep) joinStep);
			given(joinStep.where(any(Condition.class))).willReturn((SelectConditionStep) conditionStep);
			given(RuleTemplateRepositoryTests.this.sortingQueryBuilder.addSortingQuery(any(SelectConditionStep.class),
					any(List.class), eq(CstRuleTemplateT.CST_RULE_TEMPLATE_T)))
				.willReturn((SelectOrderByStep) orderByStep);
			given(((SelectLimitStep) orderByStep).offset(anyInt())).willReturn(this.afterOffsetStep);
			given(this.afterOffsetStep.limit(anyInt())).willReturn(this.afterPaginationStep);
			given(RuleTemplateRepositoryTests.this.auroraDbDSLContext.fetch(any(ResultQuery.class)))
				.willReturn((Result) mockResult);
			given(mockResult.into(RuleTemplateListQueryResultDto.class)).willReturn(expected);

			// When
			List<RuleTemplateListQueryResultDto> result = RuleTemplateRepositoryTests.this.ruleTemplateRepository
				.getAllRuleTemplates(searchRequest, RuleTemplateRepositoryTestDataFactory.SEARCH_TERM_STANDARD,
						pageable, RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(expected);
			then(RuleTemplateRepositoryTests.this.sortingQueryBuilder).should()
				.addSortingQuery(any(SelectConditionStep.class), any(List.class),
						eq(CstRuleTemplateT.CST_RULE_TEMPLATE_T));
			then(conditionStep).should(never())
				.orderBy(any(OrderField.class), any(OrderField.class), any(OrderField.class));
			// Pagination must be applied on the sorted query too: page 0, size 20.
			then((SelectLimitStep) orderByStep).should().offset(0);
			then(this.afterOffsetStep).should().limit(20);
		}

	}

	@Nested
	@DisplayName("updateRuleTemplate Tests")
	class UpdateRuleTemplateTests {

		@Test
		@DisplayName("Should merge rule template")
		void testUpdateRuleTemplateValidTemplateMergesTemplate() {
			// Given
			RuleTemplate ruleTemplate = RuleTemplateRepositoryTestDataFactory.createRuleTemplateEntity();

			// When
			RuleTemplateRepositoryTests.this.ruleTemplateRepository.updateRuleTemplate(ruleTemplate);

			// Then
			then(RuleTemplateRepositoryTests.this.entityManager).should().merge(ruleTemplate);
		}

	}

	@Nested
	@DisplayName("markAsDefault Tests")
	class MarkAsDefaultTests {

		@Test
		@DisplayName("Should run account-wide reset then set default when marking as default")
		void testMarkAsDefaultTrueExecutesTwoUpdates() {
			// Given
			UpdateSetFirstStep<?> updateStep = Mockito.mock(UpdateSetFirstStep.class);
			UpdateSetMoreStep<?> setMoreStep = Mockito.mock(UpdateSetMoreStep.class);
			UpdateConditionStep<?> conditionStep = Mockito.mock(UpdateConditionStep.class);

			given(RuleTemplateRepositoryTests.this.auroraDbDSLContext.update(CstRuleTemplateT.CST_RULE_TEMPLATE_T))
				.willReturn((UpdateSetFirstStep) updateStep);
			given(updateStep.set(any(Field.class), anyInt())).willReturn((UpdateSetMoreStep) setMoreStep);
			given(setMoreStep.where(any(Condition.class))).willReturn((UpdateConditionStep) conditionStep);
			given(conditionStep.execute()).willReturn(1);

			// When
			RuleTemplateRepositoryTests.this.ruleTemplateRepository.markAsDefault(
					RuleTemplateRepositoryTestDataFactory.DEFAULT_TEMPLATE_ID,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID, true);

			// Then
			then(conditionStep).should(times(2)).execute();
		}

		@Test
		@DisplayName("Should clear default flag only for template when unmarking default")
		void testMarkAsDefaultFalseExecutesSingleUpdate() {
			// Given
			UpdateSetFirstStep<?> updateStep = Mockito.mock(UpdateSetFirstStep.class);
			UpdateSetMoreStep<?> setMoreStep = Mockito.mock(UpdateSetMoreStep.class);
			UpdateConditionStep<?> conditionStep = Mockito.mock(UpdateConditionStep.class);

			given(RuleTemplateRepositoryTests.this.auroraDbDSLContext.update(CstRuleTemplateT.CST_RULE_TEMPLATE_T))
				.willReturn((UpdateSetFirstStep) updateStep);
			given(updateStep.set(any(Field.class), anyInt())).willReturn((UpdateSetMoreStep) setMoreStep);
			given(setMoreStep.where(any(Condition.class))).willReturn((UpdateConditionStep) conditionStep);
			given(conditionStep.execute()).willReturn(1);

			// When
			RuleTemplateRepositoryTests.this.ruleTemplateRepository.markAsDefault(
					RuleTemplateRepositoryTestDataFactory.DEFAULT_TEMPLATE_ID,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID, false);

			// Then
			then(conditionStep).should(times(1)).execute();
		}

	}

	@Nested
	@DisplayName("findDefaultTemplateIdByAccountId Tests")
	class FindDefaultTemplateIdByAccountIdTests {

		@Test
		@DisplayName("Should return template ID when default template found")
		void testFindDefaultTemplateIdByAccountIdTemplateFoundReturnsId() {
			// Given
			List<Integer> results = List.of(RuleTemplateRepositoryTestDataFactory.DEFAULT_TEMPLATE_ID);

			given(RuleTemplateRepositoryTests.this.entityManager.createQuery(anyString(), eq(Integer.class)))
				.willReturn(RuleTemplateRepositoryTests.this.integerTypedQuery);
			given(RuleTemplateRepositoryTests.this.integerTypedQuery.setParameter(
					RuleTemplateRepositoryTestDataFactory.JPQL_PARAMETER_ACCOUNT_ID,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID))
				.willReturn(RuleTemplateRepositoryTests.this.integerTypedQuery);
			given(RuleTemplateRepositoryTests.this.integerTypedQuery.getResultList()).willReturn(results);

			// When
			Integer result = RuleTemplateRepositoryTests.this.ruleTemplateRepository
				.findDefaultTemplateIdByAccountId(RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isEqualTo(RuleTemplateRepositoryTestDataFactory.DEFAULT_TEMPLATE_ID);
		}

		@Test
		@DisplayName("Should return null when no default template found")
		void testFindDefaultTemplateIdByAccountIdNoTemplateFoundReturnsNull() {
			// Given
			given(RuleTemplateRepositoryTests.this.entityManager.createQuery(anyString(), eq(Integer.class)))
				.willReturn(RuleTemplateRepositoryTests.this.integerTypedQuery);
			given(RuleTemplateRepositoryTests.this.integerTypedQuery.setParameter(
					RuleTemplateRepositoryTestDataFactory.JPQL_PARAMETER_ACCOUNT_ID,
					RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID))
				.willReturn(RuleTemplateRepositoryTests.this.integerTypedQuery);
			given(RuleTemplateRepositoryTests.this.integerTypedQuery.getResultList()).willReturn(new ArrayList<>());

			// When
			Integer result = RuleTemplateRepositoryTests.this.ruleTemplateRepository
				.findDefaultTemplateIdByAccountId(RuleTemplateRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID);

			// Then
			assertThat(result).isNull();
		}

	}

}
