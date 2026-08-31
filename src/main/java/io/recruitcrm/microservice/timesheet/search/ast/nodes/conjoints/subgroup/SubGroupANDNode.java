package io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.filters.FilterNode;
import io.recruitcrm.microservice.timesheet.search.constants.SubGroupComparisonOperator;
import io.recruitcrm.microservice.timesheet.search.helpers.JooqFieldTypeUtils;

public class SubGroupANDNode extends SubGroupConjointNode {

	@Override
	public Select<?> toSQL(DSLContext ctx, Table<?> table, Field<?> searchTableIdField) {
		List<FilterNode> filters = this.getFilters();
		List<CommonTableExpression<?>> cteList = new ArrayList<>();

		Condition finalCondition = null;

		for (int i = 0; i < filters.size(); i++) {
			FilterNode filterNode = filters.get(i);
			Select<?> query = filterNode.toSQL();
			String cteName = "a" + i;
			CommonTableExpression<?> cte = DSL.name(cteName).as(query);
			cteList.add(cte);

			if (finalCondition == null) {
				finalCondition = this.createInitialCondition(searchTableIdField, cteName, finalCondition, table,
						filterNode);
			}
			else {
				finalCondition = this.createCondition(searchTableIdField, cteName, finalCondition, table, filterNode);
			}
		}

		// Add account ID check for contractor table
		if (table != null && table.equals(Tblcandidate.TBLCANDIDATE) && !filters.isEmpty()) {
			Integer accountId = filters.getFirst().getFilterNodeContext().getAccountId();
			if (accountId != null) {
				Condition accountIdCondition = Tblcandidate.TBLCANDIDATE.ACCOUNTID.eq(accountId);
				finalCondition = finalCondition.and(accountIdCondition);
			}
		}

		return DSL.with(cteList.toArray(new CommonTableExpression<?>[0]))
			.select(JooqFieldTypeUtils.coerce(table.field(searchTableIdField), Integer.class))
			.from(table)
			.where(finalCondition);
	}

	private Condition createInitialCondition(Field<?> searchTableIdField, String initialCteName,
			Condition finalCondition, Table<?> table, FilterNode filterNode) {
		SubGroupComparisonOperator subgroupComparisonOperator = filterNode.getSubgroupComparisonOperator();
		if (Objects.requireNonNull(subgroupComparisonOperator) == SubGroupComparisonOperator.IN) {
			finalCondition = JooqFieldTypeUtils.coerce(table.field(searchTableIdField), Integer.class)
				.in(DSL.select(DSL.field(searchTableIdField.getName(), Integer.class)).from(DSL.name(initialCteName)));
		}
		else if (subgroupComparisonOperator == SubGroupComparisonOperator.NOT_IN) {
			finalCondition = JooqFieldTypeUtils.coerce(table.field(searchTableIdField), Integer.class)
				.notIn(DSL.select(DSL.field(searchTableIdField.getName(), Integer.class))
					.from(DSL.name(initialCteName)));
		}
		return finalCondition;
	}

	private Condition createCondition(Field<?> searchTableIdField, String initialCteName, Condition finalCondition,
			Table<?> table, FilterNode filterNode) {
		SubGroupComparisonOperator subgroupComparisonOperator = filterNode.getSubgroupComparisonOperator();
		if (Objects.requireNonNull(subgroupComparisonOperator) == SubGroupComparisonOperator.IN) {
			finalCondition = finalCondition.and(JooqFieldTypeUtils
				.coerce(table.field(searchTableIdField), Integer.class)
				.in(DSL.select(DSL.field(searchTableIdField.getName(), Integer.class)).from(DSL.name(initialCteName))));
		}
		else if (subgroupComparisonOperator == SubGroupComparisonOperator.NOT_IN) {
			finalCondition = finalCondition
				.and(JooqFieldTypeUtils.coerce(table.field(searchTableIdField), Integer.class)
					.notIn(DSL.select(DSL.field(searchTableIdField.getName(), Integer.class))
						.from(DSL.name(initialCteName))));
		}
		return finalCondition;
	}

}
