package io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("AssociatedDeal IsNotFilterNode Tests")
class IsNotFilterNodeTests {

	private FilterNodeContext filterNodeContext;

	private final Integer accountId = 1;

	private final String gmtDifference = "+05:30";

	@BeforeEach
	void setUp() {
		this.filterNodeContext = new FilterNodeContext();
		this.filterNodeContext.setAccountId(this.accountId);
		this.filterNodeContext.setGmtDifference(this.gmtDifference);
	}

	@ParameterizedTest(name = "filterValue={0}")
	@ValueSource(
			strings = { "1,2,3", "[1,2,3]", "1,invalid,3", "[\"1\",\"2\"]", "[\"1\",\"abc\"]", "+5,6", "[true, 1]" })
	@DisplayName("Get filter conditions should return condition for valid filter value")
	void testGetFilterConditionsValidFilterValueReturnsCondition(String filterValue) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		assertThat(conditions).isNotNull().hasSize(1).first().isNotNull();
	}

	@Test
	@DisplayName("Get filter conditions should return empty list for null filter value")
	void testGetFilterConditionsNullFilterValueReturnsEmptyList() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue(null);
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isEmpty();
	}

	@Test
	@DisplayName("Get filter conditions should return empty list for empty filter value")
	void testGetFilterConditionsEmptyFilterValueReturnsEmptyList() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isEmpty();
	}

	@Test
	@DisplayName("Get join tables should return minimal joins with association join")
	void testGetJoinTablesReturnsMinimalJoinsWithAssociationJoin() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<TableJoinSpecification> joinTables = isNotFilterNode.getJoinTables();

		// Then
		// Verify INNER join exists (cannot access protected fields directly)
		assertThat(joinTables).isNotNull().hasSizeGreaterThanOrEqualTo(2);
		assertThat(joinTables.stream().anyMatch((join) -> join.getJoinType() == TableJoinType.INNER)).isTrue();
	}

	@Test
	@DisplayName("Get group by fields should return empty list")
	void testGetGroupByFieldsReturnsEmptyList() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Field<?>> groupByFields = isNotFilterNode.getGroupByFields();

		// Then
		assertThat(groupByFields).isEmpty();
	}

	@Test
	@DisplayName("Get group by having condition should return no condition")
	void testGetGroupByHavingConditionReturnsNoCondition() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		Condition havingCondition = isNotFilterNode.getGroupByHavingCondition();

		// Then
		assertThat(havingCondition).isNotNull().isEqualTo(DSL.noCondition());
	}

	@Test
	@DisplayName("Is select distinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		// Given
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("associatedDeal");
		filterDto.setFilterType(FilterTypes.IS_NOT);
		filterDto.setFilterValue("1,2,3");
		filterDto.setGroupType("AND");

		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = isNotFilterNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isTrue();
	}

}
