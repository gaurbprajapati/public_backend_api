package io.recruitcrm.microservice.timesheet.search.filters;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SelectQuery;
import org.jooq.Table;

import io.recruitcrm.microservice.timesheet.search.constants.SubGroupComparisonOperator;

public interface IFilterNode {

	@NotNull
	SelectQuery<?> getCteQuery();

	Table<?> getBaseTable();

	List<Field<?>> getSelectFields();

	Condition getAccountIdFilterCondition();

	List<Condition> getFilterConditions();

	List<TableJoinSpecification> getJoinTables();

	List<Condition> getCommonFilterCondition();

	List<Field<?>> getGroupByFields();

	Condition getGroupByHavingCondition();

	Field<?> getSearchField();

	SubGroupComparisonOperator getSubGroupComparisonOperator();

	<T> Field<T> getSearchField(Class<T> clazz);

	<T> Field<T> getCoercedSearchField(Class<T> clazz);

	Boolean isSelectDistinct();

}
