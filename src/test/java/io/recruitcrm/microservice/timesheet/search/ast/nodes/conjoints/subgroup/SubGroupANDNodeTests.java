package io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import org.mockito.stubbing.Answer;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.filters.FilterNode;
import io.recruitcrm.microservice.timesheet.search.constants.SubGroupComparisonOperator;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.IFilterNode;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("SubGroupANDNode Tests")
class SubGroupANDNodeTests {

	private static final int SINGLE_FILTER_COUNT = 1;

	private static final int MULTIPLE_FILTERS_COUNT = 2;

	private SubGroupANDNode subGroupANDNode;

	private DSLContext dslContext;

	private Table<?> table;

	private Field<?> searchTableIdField;

	@BeforeEach
	void setUp() {
		this.subGroupANDNode = new SubGroupANDNode();
		this.dslContext = DSL.using(org.jooq.SQLDialect.MYSQL);
		this.table = CstTimesheetT.CST_TIMESHEET_T;
		this.searchTableIdField = CstTimesheetT.CST_TIMESHEET_T.ID;
	}

	@Test
	@DisplayName("toSQL should create query with single filter using IN operator")
	void testToSQLWithSingleFilterIN() {
		FilterNode filterNode = createMockFilterNode(SubGroupComparisonOperator.IN);
		this.subGroupANDNode.addChild(filterNode);

		Select<?> result = this.subGroupANDNode.toSQL(this.dslContext, this.table, this.searchTableIdField);

		assertThat(result).isNotNull();
		String sql = result.toString().toLowerCase();
		assertThat(sql).contains("in");
	}

	@Test
	@DisplayName("toSQL should create query with single filter using NOT_IN operator")
	void testToSQLWithSingleFilterNotIn() {
		FilterNode filterNode = createMockFilterNode(SubGroupComparisonOperator.NOT_IN);
		this.subGroupANDNode.addChild(filterNode);

		Select<?> result = this.subGroupANDNode.toSQL(this.dslContext, this.table, this.searchTableIdField);

		assertThat(result).isNotNull();
		String sql = result.toString().toLowerCase();
		assertThat(sql).contains("not in");
	}

	@Test
	@DisplayName("toSQL should create query with multiple filters using AND logic with IN")
	void testToSQLWithMultipleFiltersIN() {
		FilterNode filterNode1 = createMockFilterNode(SubGroupComparisonOperator.IN);
		FilterNode filterNode2 = createMockFilterNode(SubGroupComparisonOperator.IN);
		FilterNode filterNode3 = createMockFilterNode(SubGroupComparisonOperator.IN);

		this.subGroupANDNode.addChild(filterNode1);
		this.subGroupANDNode.addChild(filterNode2);
		this.subGroupANDNode.addChild(filterNode3);

		Select<?> result = this.subGroupANDNode.toSQL(this.dslContext, this.table, this.searchTableIdField);

		String sql = result.toString().toLowerCase();
		// Verify multiple CTEs are created (a0, a1, a2)
		assertThat(result).isNotNull();
		assertThat(sql).contains("and").contains("a0").contains("a1").contains("a2");
	}

	@Test
	@DisplayName("toSQL should create query with multiple filters using AND logic with NOT_IN")
	void testToSQLWithMultipleFiltersNotIn() {
		FilterNode filterNode1 = createMockFilterNode(SubGroupComparisonOperator.NOT_IN);
		FilterNode filterNode2 = createMockFilterNode(SubGroupComparisonOperator.NOT_IN);

		this.subGroupANDNode.addChild(filterNode1);
		this.subGroupANDNode.addChild(filterNode2);

		Select<?> result = this.subGroupANDNode.toSQL(this.dslContext, this.table, this.searchTableIdField);

		String sql = result.toString().toLowerCase();
		assertThat(result).isNotNull();
		assertThat(sql).contains("and").contains("not in");
	}

	@Test
	@DisplayName("toSQL should create query with mixed IN and NOT_IN operators")
	void testToSQLWithMixedOperators() {
		FilterNode filterNode1 = createMockFilterNode(SubGroupComparisonOperator.IN);
		FilterNode filterNode2 = createMockFilterNode(SubGroupComparisonOperator.NOT_IN);

		this.subGroupANDNode.addChild(filterNode1);
		this.subGroupANDNode.addChild(filterNode2);

		Select<?> result = this.subGroupANDNode.toSQL(this.dslContext, this.table, this.searchTableIdField);

		assertThat(result).isNotNull();
		String sql = result.toString().toLowerCase();
		assertThat(sql).contains("and");
	}

	@Test
	@DisplayName("addChild should add filter to list")
	void testAddChild() {
		FilterNode filterNode = new FilterNode(mock(IFilterNode.class), new FilterNodeContext());

		this.subGroupANDNode.addChild(filterNode);

		assertThat(this.subGroupANDNode.getFilters()).hasSize(SINGLE_FILTER_COUNT);
		assertThat(this.subGroupANDNode.getFilters().get(0)).isEqualTo(filterNode);
	}

	@Test
	@DisplayName("addChild should add multiple filters")
	void testAddChildMultiple() {
		FilterNode filterNode1 = new FilterNode(mock(IFilterNode.class), new FilterNodeContext());
		FilterNode filterNode2 = new FilterNode(mock(IFilterNode.class), new FilterNodeContext());

		this.subGroupANDNode.addChild(filterNode1);
		this.subGroupANDNode.addChild(filterNode2);

		assertThat(this.subGroupANDNode.getFilters()).hasSize(MULTIPLE_FILTERS_COUNT);
	}

	private FilterNode createMockFilterNode(SubGroupComparisonOperator operator) {
		IFilterNode filterNodeImpl = mock(IFilterNode.class);
		FilterNodeContext filterNodeContext = new FilterNodeContext();
		SelectQuery<?> mockQuery = DSL.select(DSL.field("id", Integer.class)).from(DSL.table("test")).getQuery();

		lenient().when(filterNodeImpl.getCteQuery()).thenAnswer((Answer<SelectQuery<?>>) (invocation) -> mockQuery);
		given(filterNodeImpl.getSubGroupComparisonOperator()).willReturn(operator);

		return new FilterNode(filterNodeImpl, filterNodeContext);
	}

}
