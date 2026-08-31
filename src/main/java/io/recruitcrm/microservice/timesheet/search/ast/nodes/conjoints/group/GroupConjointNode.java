package io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Field;
import org.jooq.Select;
import org.jooq.Table;

import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupConjointNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class GroupConjointNode {

	private List<SubGroupConjointNode> subGroups = new ArrayList<>();

	public void addChild(SubGroupConjointNode node) {
		this.subGroups.add(node);
	}

	public abstract Select<?> toSQL(Table<?> table, Field<?> searchTableIdField);

}
