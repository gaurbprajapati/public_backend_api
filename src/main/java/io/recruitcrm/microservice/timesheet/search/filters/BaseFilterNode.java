package io.recruitcrm.microservice.timesheet.search.filters;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.SelectHavingConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectQuery;
import org.jooq.SelectSelectStep;
import org.jooq.Table;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.constants.SubGroupComparisonOperator;
import io.recruitcrm.microservice.timesheet.search.helpers.FilterValueParser;
import io.recruitcrm.microservice.timesheet.search.helpers.JooqFieldTypeUtils;

public abstract class BaseFilterNode implements IFilterNode {

	protected final FilterNodeContext filterNodeContext;

	protected BaseFilterNode(FilterNodeContext filterNodeContext) {
		this.filterNodeContext = filterNodeContext;
	}

	@Override
	public @NotNull SelectQuery<?> getCteQuery() {
		Table<?> baseTable = this.getBaseTable();
		SelectSelectStep<?> selectStep = Boolean.FALSE.equals(this.isSelectDistinct())
				? DSL.select(this.getSelectFields()) : DSL.selectDistinct(this.getSelectFields());
		SelectJoinStep<?> selectJoinStep = selectStep.from(baseTable);
		for (TableJoinSpecification joinTableSpecification : this.getJoinTables()) {
			selectJoinStep = this.applyJoin(selectJoinStep, joinTableSpecification);
		}
		SelectConditionStep<?> whereConditionStep = selectJoinStep.where(this.getAccountIdFilterCondition());
		for (Condition condition : this.getCommonFilterCondition()) {
			whereConditionStep = whereConditionStep.and(condition);
		}
		for (Condition condition : this.getFilterConditions()) {
			whereConditionStep = whereConditionStep.and(condition);
		}
		if (this.getGroupByFields().isEmpty()) {
			return whereConditionStep.getQuery();
		}
		else {
			SelectHavingConditionStep<?> havingConditionStep = whereConditionStep.groupBy(this.getGroupByFields())
				.having(this.getGroupByHavingCondition());
			return havingConditionStep.getQuery();
		}
	}

	@Override
	public List<Field<?>> getGroupByFields() {
		return List.of();
	}

	@Override
	public Condition getGroupByHavingCondition() {
		return DSL.noCondition();
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		return Collections.emptyList();
	}

	@Override
	public List<Condition> getCommonFilterCondition() {
		return List.of();
	}

	@Override
	public SubGroupComparisonOperator getSubGroupComparisonOperator() {
		return SubGroupComparisonOperator.IN;
	}

	@Override
	public final <T> Field<T> getSearchField(Class<T> clazz) {
		return JooqFieldTypeUtils.safeExact(this.getSearchField(), clazz);
	}

	@Override
	public <T> Field<T> getCoercedSearchField(Class<T> clazz) {
		return JooqFieldTypeUtils.coerce(this.getSearchField(), clazz);
	}

	@Override
	public Boolean isSelectDistinct() {
		return false;
	}

	/**
	 * Parses the current filter's value as a list of integers (comma-separated or JSON
	 * array).
	 * @return parsed integer IDs, or an empty list when the value is null, blank, or
	 * contains no valid integers
	 */
	protected List<Integer> parseIntegerFilterValue() {
		return FilterValueParser.parseIntegerList(this.filterNodeContext.getFilterDto().getFilterValue());
	}

	private SelectJoinStep<?> applyJoin(SelectJoinStep<?> selectJoinStep,
			TableJoinSpecification joinTableSpecification) {
		return switch (joinTableSpecification.getJoinType()) {
			case INNER -> selectJoinStep.innerJoin(joinTableSpecification.getJoinTable())
				.on(joinTableSpecification.getJoinCondition());
			case LEFT -> selectJoinStep.leftJoin(joinTableSpecification.getJoinTable())
				.on(joinTableSpecification.getJoinCondition());
			case RIGHT -> selectJoinStep.rightJoin(joinTableSpecification.getJoinTable())
				.on(joinTableSpecification.getJoinCondition());
		};
	}

}
