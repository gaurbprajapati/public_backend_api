package io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group;

import java.util.ArrayList;
import java.util.List;

import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupConjointNode;
import io.recruitcrm.microservice.timesheet.search.helpers.JooqFieldTypeUtils;

public class GroupANDNode extends GroupConjointNode {

	@Override
	public Select<?> toSQL(Table<?> table, Field<?> searchTableIdField) {
		List<SubGroupConjointNode> subGroups = this.getSubGroups();

		// A list to hold all CTEs
		List<CommonTableExpression<?>> cteList = new ArrayList<>();

		String groupAlias = "group";
		// Use the first subGroup (subGroups[0]) to initialize the final condition
		Select<?> initialQuery = subGroups.getFirst().toSQL(null, table, searchTableIdField);
		String initialCteName = groupAlias + "0";
		CommonTableExpression<?> initialCte = DSL.name(initialCteName).as(initialQuery);
		cteList.add(initialCte);

		// Initialize the final condition with the first subGroup's condition
		Condition finalCondition = JooqFieldTypeUtils.coerce(table.field(searchTableIdField), Integer.class)
			.in(DSL.select(DSL.field(searchTableIdField.getName(), Integer.class)).from(DSL.name(initialCteName)));

		// Loop through the remaining subGroups starting from index 1
		for (int i = 1; i < subGroups.size(); i++) {
			// Generate the JOOQ query from the SubGroupConjointNode
			Select<?> subGroupQuery = subGroups.get(i).toSQL(null, table, searchTableIdField);

			// Create a unique name for the CTE, like "group1", "group2", etc.
			String cteName = groupAlias + i;

			// Create the CTE and add it to the list
			CommonTableExpression<?> cte = DSL.name(cteName).as(subGroupQuery);
			cteList.add(cte);

			// Add the condition for each subsequent CTE
			finalCondition = finalCondition
				.and(JooqFieldTypeUtils.coerce(table.field(searchTableIdField), Integer.class)
					.in(DSL.select(DSL.field(searchTableIdField.getName(), Integer.class)).from(DSL.name(cteName))));
		}

		// Create the final query using the CTEs and the dynamically built condition
		return DSL.with(cteList.toArray(new CommonTableExpression<?>[0]))
			.select(JooqFieldTypeUtils.coerce(table.field(searchTableIdField), Integer.class))
			.from(table)
			.where(finalCondition);
	}

}
