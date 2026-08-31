package io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.mockito.stubbing.Answer;

import org.jooq.Field;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupANDNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupConjointNode;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("GroupORNode Tests")
class GroupORNodeTests {

	private static final int SINGLE_SUBGROUP_COUNT = 1;

	private static final int MULTIPLE_SUBGROUPS_COUNT = 2;

	private GroupORNode groupORNode;

	private Table<?> table;

	private Field<?> searchTableIdField;

	@BeforeEach
	void setUp() {
		this.groupORNode = new GroupORNode();
		this.table = CstTimesheetT.CST_TIMESHEET_T;
		this.searchTableIdField = CstTimesheetT.CST_TIMESHEET_T.ID;
	}

	@Test
	@DisplayName("toSQL should create query with single subGroup")
	void testToSQLWithSingleSubGroup() {
		SubGroupConjointNode subGroup = mock(SubGroupConjointNode.class);
		Select<?> mockSelect = DSL.select(DSL.field("id", Integer.class)).from(DSL.table("test"));
		given(subGroup.toSQL(any(), any(Table.class), any(Field.class)))
			.willAnswer((Answer<Select<?>>) (invocation) -> mockSelect);

		this.groupORNode.addChild(subGroup);

		Select<?> result = this.groupORNode.toSQL(this.table, this.searchTableIdField);

		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("toSQL should create query with multiple subGroups using OR logic")
	void testToSQLWithMultipleSubGroups() {
		SubGroupConjointNode subGroup1 = mock(SubGroupConjointNode.class);
		SubGroupConjointNode subGroup2 = mock(SubGroupConjointNode.class);
		SubGroupConjointNode subGroup3 = mock(SubGroupConjointNode.class);

		Select<?> mockSelect1 = DSL.select(DSL.field("id", Integer.class)).from(DSL.table("test1"));
		Select<?> mockSelect2 = DSL.select(DSL.field("id", Integer.class)).from(DSL.table("test2"));
		Select<?> mockSelect3 = DSL.select(DSL.field("id", Integer.class)).from(DSL.table("test3"));

		given(subGroup1.toSQL(any(), any(Table.class), any(Field.class)))
			.willAnswer((Answer<Select<?>>) (invocation) -> mockSelect1);
		given(subGroup2.toSQL(any(), any(Table.class), any(Field.class)))
			.willAnswer((Answer<Select<?>>) (invocation) -> mockSelect2);
		given(subGroup3.toSQL(any(), any(Table.class), any(Field.class)))
			.willAnswer((Answer<Select<?>>) (invocation) -> mockSelect3);

		this.groupORNode.addChild(subGroup1);
		this.groupORNode.addChild(subGroup2);
		this.groupORNode.addChild(subGroup3);

		Select<?> result = this.groupORNode.toSQL(this.table, this.searchTableIdField);

		// Verify that multiple CTEs are created (group0, group1, group2)
		String sql = result.toString();
		assertThat(result).isNotNull();
		assertThat(sql).contains("group0").contains("group1").contains("group2");
	}

	@Test
	@DisplayName("toSQL should use OR condition for multiple subGroups")
	void testToSQLUsesOrCondition() {
		SubGroupConjointNode subGroup1 = mock(SubGroupConjointNode.class);
		SubGroupConjointNode subGroup2 = mock(SubGroupConjointNode.class);

		Select<?> mockSelect1 = DSL.select(DSL.field("id", Integer.class)).from(DSL.table("test1"));
		Select<?> mockSelect2 = DSL.select(DSL.field("id", Integer.class)).from(DSL.table("test2"));

		given(subGroup1.toSQL(any(), any(Table.class), any(Field.class)))
			.willAnswer((Answer<Select<?>>) (invocation) -> mockSelect1);
		given(subGroup2.toSQL(any(), any(Table.class), any(Field.class)))
			.willAnswer((Answer<Select<?>>) (invocation) -> mockSelect2);

		this.groupORNode.addChild(subGroup1);
		this.groupORNode.addChild(subGroup2);

		Select<?> result = this.groupORNode.toSQL(this.table, this.searchTableIdField);

		assertThat(result).isNotNull();
		String sql = result.toString().toLowerCase();
		// Verify OR logic is used (check for "or" in SQL)
		assertThat(sql).contains("or");
	}

	@Test
	@DisplayName("addChild should add subGroup to list")
	void testAddChild() {
		SubGroupConjointNode subGroup = new SubGroupANDNode();

		this.groupORNode.addChild(subGroup);

		assertThat(this.groupORNode.getSubGroups()).hasSize(SINGLE_SUBGROUP_COUNT);
		assertThat(this.groupORNode.getSubGroups().get(0)).isEqualTo(subGroup);
	}

	@Test
	@DisplayName("addChild should add multiple subGroups")
	void testAddChildMultiple() {
		SubGroupConjointNode subGroup1 = new SubGroupANDNode();
		SubGroupConjointNode subGroup2 = new SubGroupANDNode();

		this.groupORNode.addChild(subGroup1);
		this.groupORNode.addChild(subGroup2);

		assertThat(this.groupORNode.getSubGroups()).hasSize(MULTIPLE_SUBGROUPS_COUNT);
	}

}
