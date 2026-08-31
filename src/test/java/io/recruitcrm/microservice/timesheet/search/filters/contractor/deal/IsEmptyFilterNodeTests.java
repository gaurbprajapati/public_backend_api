package io.recruitcrm.microservice.timesheet.search.filters.contractor.deal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("ContractorDeal IsEmptyFilterNode Tests")
class IsEmptyFilterNodeTests {

	private static final int SINGLE_CONDITION_COUNT = 1;

	private FilterNodeContext filterNodeContext;

	private final Integer accountId = 1;

	private final String gmtDifference = "+05:30";

	@BeforeEach
	void setUp() {
		this.filterNodeContext = new FilterNodeContext();
		this.filterNodeContext.setAccountId(this.accountId);
		this.filterNodeContext.setGmtDifference(this.gmtDifference);
	}

	@Test
	@DisplayName("Get filter conditions should return IS NULL condition")
	void testGetFilterConditionsReturnsIsNullCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_EMPTY);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsEmptyFilterNode isEmptyFilterNode = new IsEmptyFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isEmptyFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull().hasSize(SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("Get join tables should return LEFT joins for deal-related tables")
	void testGetJoinTablesReturnsLeftJoinsForDealRelatedTables() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_EMPTY);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsEmptyFilterNode isEmptyFilterNode = new IsEmptyFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> joinTables = isEmptyFilterNode.getJoinTables();

		// Then
		long leftJoinCount = joinTables.stream().filter((join) -> join.getJoinType() == TableJoinType.LEFT).count();
		assertThat(joinTables).isNotNull().hasSizeGreaterThanOrEqualTo(2);
		assertThat(leftJoinCount).isGreaterThanOrEqualTo(2);
	}

	@Test
	@DisplayName("Get group by fields should return empty list")
	void testGetGroupByFieldsReturnsEmptyList() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_EMPTY);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsEmptyFilterNode isEmptyFilterNode = new IsEmptyFilterNode(this.filterNodeContext);

		// When
		List<org.jooq.Field<?>> groupByFields = isEmptyFilterNode.getGroupByFields();

		// Then
		assertThat(groupByFields).isEmpty();
	}

	@Test
	@DisplayName("Get group by having condition should return no condition")
	void testGetGroupByHavingConditionReturnsNoCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_EMPTY);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsEmptyFilterNode isEmptyFilterNode = new IsEmptyFilterNode(this.filterNodeContext);

		// When
		Condition havingCondition = isEmptyFilterNode.getGroupByHavingCondition();

		// Then
		assertThat(havingCondition).isNotNull().isEqualTo(DSL.noCondition());
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("dealName");
		filterDto.setFilterType(FilterTypes.IS_EMPTY);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("contractor");

		this.filterNodeContext.setFilterDto(filterDto);
		IsEmptyFilterNode isEmptyFilterNode = new IsEmptyFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = isEmptyFilterNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isTrue();
	}

}
