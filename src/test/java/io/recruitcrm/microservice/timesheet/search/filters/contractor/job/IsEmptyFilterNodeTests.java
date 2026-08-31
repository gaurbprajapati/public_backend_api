package io.recruitcrm.microservice.timesheet.search.filters.contractor.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractorJob IsEmptyFilterNode Tests")
class IsEmptyFilterNodeTests {

	private static final int SINGLE_CONDITION_COUNT = 1;

	private static final String DB_FIELD = "jobName";

	private static final String GROUP_TYPE = "contractor";

	private FilterNodeContext filterNodeContext;

	private final Integer accountId = Integer.valueOf(1);

	private final String gmtDifference = "+05:30";

	@BeforeEach
	void setUp() {
		this.filterNodeContext = new FilterNodeContext();
		this.filterNodeContext.setAccountId(this.accountId);
		this.filterNodeContext.setGmtDifference(this.gmtDifference);
	}

	@Test
	@DisplayName("Get filter conditions should return timesheet enabled but not active condition")
	void testGetFilterConditionsReturnsTimesheetEnabledButNotActiveCondition() {
		IsEmptyFilterNode isEmptyFilterNode = this.createNode();
		List<Condition> conditions = isEmptyFilterNode.getFilterConditions();
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Get join tables should return minimal joins")
	void testGetJoinTablesReturnsMinimalJoins() {
		IsEmptyFilterNode isEmptyFilterNode = this.createNode();
		List<TableJoinSpecification> joinTables = isEmptyFilterNode.getJoinTables();
		assertThat(joinTables).isNotNull();
	}

	@Test
	@DisplayName("Get common filter condition should return empty list")
	void testGetCommonFilterConditionReturnsEmptyList() {
		IsEmptyFilterNode isEmptyFilterNode = this.createNode();
		List<Condition> commonConditions = isEmptyFilterNode.getCommonFilterCondition();
		assertThat(commonConditions).isEmpty();
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		IsEmptyFilterNode isEmptyFilterNode = this.createNode();
		assertThat(isEmptyFilterNode.isSelectDistinct()).isTrue();
	}

	private IsEmptyFilterNode createNode() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField(DB_FIELD);
		filterDto.setFilterType(FilterTypes.IS_EMPTY);
		filterDto.setGroupType(GROUP_TYPE);
		this.filterNodeContext.setFilterDto(filterDto);
		return new IsEmptyFilterNode(this.filterNodeContext);
	}

}
