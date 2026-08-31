package io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup;

import java.util.ArrayList;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Select;
import org.jooq.Table;

import io.recruitcrm.microservice.timesheet.search.ast.nodes.filters.FilterNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SubGroupConjointNode {

	private List<FilterNode> filters = new ArrayList<>();

	public void addChild(FilterNode node) {
		this.filters.add(node);
	}

	public abstract Select<?> toSQL(DSLContext ctx, Table<?> table, Field<?> searchTableIdField);

}
