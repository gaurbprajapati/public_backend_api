package io.recruitcrm.microservice.timesheet.search.ast.nodes.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.mockito.stubbing.Answer;

import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.SubGroupComparisonOperator;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.IFilterNode;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("FilterNode Tests")
class FilterNodeTests {

	private IFilterNode filterNodeImpl;

	private FilterNodeContext filterNodeContext;

	private FilterNode filterNode;

	@BeforeEach
	void setUp() {
		this.filterNodeImpl = mock(IFilterNode.class);
		this.filterNodeContext = new FilterNodeContext();
		this.filterNode = new FilterNode(this.filterNodeImpl, this.filterNodeContext);
	}

	@Test
	@DisplayName("toSQL should return CTE query from filterNodeImpl")
	void testToSQL() {
		SelectQuery<?> mockQuery = DSL.select(DSL.field("id", Integer.class)).from(DSL.table("test")).getQuery();
		given(this.filterNodeImpl.getCteQuery()).willAnswer((Answer<SelectQuery<?>>) (invocation) -> mockQuery);

		SelectQuery<?> result = this.filterNode.toSQL();

		assertThat(result).isNotNull().isEqualTo(mockQuery);
	}

	@Test
	@DisplayName("getSubgroupComparisonOperator should return operator from filterNodeImpl")
	void testGetSubgroupComparisonOperator() {
		given(this.filterNodeImpl.getSubGroupComparisonOperator()).willReturn(SubGroupComparisonOperator.IN);

		SubGroupComparisonOperator result = this.filterNode.getSubgroupComparisonOperator();

		assertThat(result).isNotNull().isEqualTo(SubGroupComparisonOperator.IN);
	}

	@Test
	@DisplayName("getSubgroupComparisonOperator should return NOT_IN operator")
	void testGetSubgroupComparisonOperatorNotIn() {
		given(this.filterNodeImpl.getSubGroupComparisonOperator()).willReturn(SubGroupComparisonOperator.NOT_IN);

		SubGroupComparisonOperator result = this.filterNode.getSubgroupComparisonOperator();

		assertThat(result).isNotNull().isEqualTo(SubGroupComparisonOperator.NOT_IN);
	}

	@Test
	@DisplayName("getFilterNodeImpl should return the filterNodeImpl")
	void testGetFilterNodeImpl() {
		assertThat(this.filterNode.getFilterNodeImpl()).isEqualTo(this.filterNodeImpl);
	}

	@Test
	@DisplayName("getFilterNodeContext should return the filterNodeContext")
	void testGetFilterNodeContext() {
		assertThat(this.filterNode.getFilterNodeContext()).isEqualTo(this.filterNodeContext);
	}

}
