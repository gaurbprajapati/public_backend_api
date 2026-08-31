package io.recruitcrm.microservice.timesheet.repositories.rule_engine.rule_template;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.RuleTemplate;
import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.search.models.jooq.tables.CstRuleTemplateT;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateNameQueryResultDto;
import io.recruitcrm.microservice.timesheet.repositories.SortingQueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.jooq.SelectLimitStep;
import org.jooq.SelectOrderByStep;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Repository
public class RuleTemplateRepository implements IRuleTemplateRepository {

	private static final String ACCOUNT_ID_QUERY_PARAM = "accountId";

	private static final String IS_DEFAULT_COLUMN_NAME = "is_default";

	private final EntityManager entityManager;

	private final DSLContext auroraDbDSLContext;

	private final SortingQueryBuilder sortingQueryBuilder;

	public RuleTemplateRepository(EntityManager entityManager, DSLContext auroraDbDSLContext,
			SortingQueryBuilder sortingQueryBuilder) {
		this.entityManager = entityManager;
		this.auroraDbDSLContext = auroraDbDSLContext;
		this.sortingQueryBuilder = sortingQueryBuilder;
	}

	@Override
	@Transactional
	@WriterRoute
	public void createRuleTemplate(RuleTemplate ruleTemplate) {
		this.entityManager.persist(ruleTemplate);
	}

	@Override
	public RuleTemplate getRuleTemplate(Integer templateId, Integer accountId) {
		TypedQuery<RuleTemplate> query = this.entityManager
			.createQuery("SELECT rt FROM RuleTemplate rt WHERE rt.id = :templateId AND rt.accountId = :"
					+ ACCOUNT_ID_QUERY_PARAM, RuleTemplate.class)
			.setParameter("templateId", templateId)
			.setParameter(ACCOUNT_ID_QUERY_PARAM, accountId);

		List<RuleTemplate> results = query.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

	@Override
	@Transactional
	@WriterRoute
	public void deleteRuleTemplate(Integer templateId) {
		RuleTemplate ruleTemplate = this.entityManager.find(RuleTemplate.class, templateId);
		if (ruleTemplate != null) {
			this.entityManager.remove(ruleTemplate);
		}
	}

	@Override
	public List<RuleTemplateNameQueryResultDto> getRuleTemplateNames(String search, Pageable pageable,
			Integer accountId) {
		if (search == null || search.trim().isEmpty()) {
			return this.entityManager.createQuery(
					"SELECT new io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateNameQueryResultDto(rt.id, rt.templateName, rt.isDefault) "
							+ "FROM RuleTemplate rt WHERE rt.accountId = :" + ACCOUNT_ID_QUERY_PARAM
							+ " ORDER BY rt.isDefault DESC, rt.id DESC",
					RuleTemplateNameQueryResultDto.class)
				.setParameter(ACCOUNT_ID_QUERY_PARAM, accountId)
				.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
				.setMaxResults(pageable.getPageSize())
				.getResultList();
		}

		return this.entityManager.createQuery(
				"SELECT new io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.query_result.RuleTemplateNameQueryResultDto(rt.id, rt.templateName, rt.isDefault) "
						+ "FROM RuleTemplate rt WHERE rt.accountId = :" + ACCOUNT_ID_QUERY_PARAM
						+ " AND LOWER(rt.templateName) LIKE LOWER(:search) ORDER BY rt.isDefault DESC, rt.id DESC",
				RuleTemplateNameQueryResultDto.class)
			.setParameter(ACCOUNT_ID_QUERY_PARAM, accountId)
			.setParameter("search", "%" + search.trim() + "%")
			.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
			.setMaxResults(pageable.getPageSize())
			.getResultList();
	}

	public List<RuleTemplateListQueryResultDto> getAllRuleTemplates(SearchRequestBodyDto searchRequestBodyDto,
			String search, Pageable pageable, Integer accountId) {
		Condition condition = CstRuleTemplateT.CST_RULE_TEMPLATE_T.ACCOUNT_ID.eq(accountId);

		if (search != null && !search.trim().isEmpty()) {
			condition = condition.and(DSL.lower(CstRuleTemplateT.CST_RULE_TEMPLATE_T.TEMPLATE_NAME)
				.like("%" + search.trim().toLowerCase(Locale.ROOT) + "%"));
		}

		// Build the base query
		var baseQuery = this.auroraDbDSLContext
			.select(CstRuleTemplateT.CST_RULE_TEMPLATE_T.ID,
					CstRuleTemplateT.CST_RULE_TEMPLATE_T.TEMPLATE_NAME.as("templateName"),
					DSL.field(IS_DEFAULT_COLUMN_NAME, Integer.class).as("isDefault"),
					CstRuleTemplateT.CST_RULE_TEMPLATE_T.ADDED_ON.as("addedOn"),
					CstRuleTemplateT.CST_RULE_TEMPLATE_T.ADDED_BY,
					CstRuleTemplateT.CST_RULE_TEMPLATE_T.ADDED_BY_USER_TYPE_ID,
					CstRuleTemplateT.CST_RULE_TEMPLATE_T.UPDATED_ON.as("updatedOn"),
					CstRuleTemplateT.CST_RULE_TEMPLATE_T.UPDATED_BY,
					CstRuleTemplateT.CST_RULE_TEMPLATE_T.UPDATED_BY_USER_TYPE_ID)
			.from(CstRuleTemplateT.CST_RULE_TEMPLATE_T)
			.where(condition);

		// Apply sorting
		SelectOrderByStep<?> sortedQuery;
		if (searchRequestBodyDto != null && searchRequestBodyDto.getSortPriorityList() != null
				&& !searchRequestBodyDto.getSortPriorityList().isEmpty()) {
			// Check if there are any valid (non-null, non-empty) sort fields
			boolean hasValidSortFields = searchRequestBodyDto.getSortPriorityList()
				.stream()
				.anyMatch((sort) -> sort.getField() != null && !sort.getField().trim().isEmpty());

			if (hasValidSortFields) {
				sortedQuery = this.sortingQueryBuilder.addSortingQuery(baseQuery,
						searchRequestBodyDto.getSortPriorityList(), CstRuleTemplateT.CST_RULE_TEMPLATE_T);
			}
			else {
				// All sort fields are invalid, fall back to default sorting
				sortedQuery = (SelectOrderByStep<?>) baseQuery.orderBy(
						DSL.field(IS_DEFAULT_COLUMN_NAME, Integer.class).desc(),
						CstRuleTemplateT.CST_RULE_TEMPLATE_T.UPDATED_ON.desc(),
						CstRuleTemplateT.CST_RULE_TEMPLATE_T.ID.desc());
			}
		}
		else {
			sortedQuery = (SelectOrderByStep<?>) baseQuery.orderBy(
					DSL.field(IS_DEFAULT_COLUMN_NAME, Integer.class).desc(),
					CstRuleTemplateT.CST_RULE_TEMPLATE_T.UPDATED_ON.desc(),
					CstRuleTemplateT.CST_RULE_TEMPLATE_T.ID.desc());
		}

		/**
		 * Apply pagination.The `id` tiebreaker in every ORDER BY branch above keeps the
		 * ordering total so a row never straddles two pages.
		 */
		Select<?> paginatedQuery = ((SelectLimitStep<?>) sortedQuery)
			.offset(pageable.getPageNumber() * pageable.getPageSize())
			.limit(pageable.getPageSize());
		return this.auroraDbDSLContext.fetch(paginatedQuery).into(RuleTemplateListQueryResultDto.class);
	}

	@Override
	@Transactional
	@WriterRoute
	public void updateRuleTemplate(RuleTemplate ruleTemplate) {
		this.entityManager.merge(ruleTemplate);
	}

	@Override
	@Transactional
	@WriterRoute
	public void markAsDefault(Integer templateId, Integer accountId, Boolean isDefault) {
		if (Boolean.TRUE.equals(isDefault)) {
			/**
			 * Mark as default: First, set all templates in the account to non-default (0)
			 * Then set the specified template as default (1)
			 */
			this.auroraDbDSLContext.update(CstRuleTemplateT.CST_RULE_TEMPLATE_T)
				.set(DSL.field(IS_DEFAULT_COLUMN_NAME), 0)
				.where(CstRuleTemplateT.CST_RULE_TEMPLATE_T.ACCOUNT_ID.eq(accountId))
				.execute();

			this.auroraDbDSLContext.update(CstRuleTemplateT.CST_RULE_TEMPLATE_T)
				.set(DSL.field(IS_DEFAULT_COLUMN_NAME), 1)
				.where(CstRuleTemplateT.CST_RULE_TEMPLATE_T.ID.eq(templateId)
					.and(CstRuleTemplateT.CST_RULE_TEMPLATE_T.ACCOUNT_ID.eq(accountId)))
				.execute();
		}
		else {
			/**
			 * Unmark as default: Set only the specified template to non-default (0)
			 */
			this.auroraDbDSLContext.update(CstRuleTemplateT.CST_RULE_TEMPLATE_T)
				.set(DSL.field(IS_DEFAULT_COLUMN_NAME), 0)
				.where(CstRuleTemplateT.CST_RULE_TEMPLATE_T.ID.eq(templateId)
					.and(CstRuleTemplateT.CST_RULE_TEMPLATE_T.ACCOUNT_ID.eq(accountId)))
				.execute();
		}
	}

	@Override
	public Integer findDefaultTemplateIdByAccountId(Integer accountId) {
		TypedQuery<Integer> query = this.entityManager
			.createQuery("SELECT rt.id FROM RuleTemplate rt WHERE rt.accountId = :" + ACCOUNT_ID_QUERY_PARAM
					+ "  AND rt.isDefault = 1", Integer.class)
			.setParameter(ACCOUNT_ID_QUERY_PARAM, accountId);

		List<Integer> results = query.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

}
