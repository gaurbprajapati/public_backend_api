/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.testdata.IsNotFilterNodeTestDataFactory;
import java.util.List;
import org.jooq.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimesheetStatus IsNotFilterNode Tests")
class IsNotFilterNodeTests {

	private FilterNodeContext filterNodeContext;

	@BeforeEach
	void setUp() {
		this.filterNodeContext = new FilterNodeContext();
		this.filterNodeContext.setAccountId(IsNotFilterNodeTestDataFactory.DEFAULT_ACCOUNT_ID);
		this.filterNodeContext.setGmtDifference(IsNotFilterNodeTestDataFactory.DEFAULT_GMT_DIFFERENCE);
	}

	@Test
	@DisplayName("getFilterConditions should return one condition for plain integer string")
	void testGetFilterConditionsPlainIntegerStringReturnsSingleCondition() {
		// Given
		FilterDto filterDto = IsNotFilterNodeTestDataFactory.createTimesheetStatusIsNotFilterDto("1");
		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isNotNull()
			.hasSize(IsNotFilterNodeTestDataFactory.SINGLE_CONDITION_COUNT)
			.first()
			.isNotNull();
	}

	@ParameterizedTest
	@ValueSource(strings = { "[2]", "[\"3\"]", "4", "\"2\"", "  3  " })
	@DisplayName("getFilterConditions should return one condition for valid JSON status values")
	void testGetFilterConditionsValidJsonStatusValuesReturnSingleCondition(String filterValue) {
		// Given
		FilterDto filterDto = IsNotFilterNodeTestDataFactory.createTimesheetStatusIsNotFilterDto(filterValue);
		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).hasSize(IsNotFilterNodeTestDataFactory.SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("getFilterConditions should return one condition when invalid JSON token falls back to plain integer parse")
	void testGetFilterConditionsLeadingZeroRejectedByJsonThenPlainIntegerReturnsSingleCondition() {
		// Given
		FilterDto filterDto = IsNotFilterNodeTestDataFactory.createTimesheetStatusIsNotFilterDto(
				IsNotFilterNodeTestDataFactory.FILTER_VALUE_LEADING_ZERO_REJECTED_BY_JSON);
		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).hasSize(IsNotFilterNodeTestDataFactory.SINGLE_CONDITION_COUNT).first().isNotNull();
	}

	@Test
	@DisplayName("getFilterConditions should return empty list when JSON long root falls back to invalid plain integer")
	void testGetFilterConditionsJsonLongRootFallbackParseFailsReturnsEmptyList() {
		// Given
		FilterDto filterDto = IsNotFilterNodeTestDataFactory
			.createTimesheetStatusIsNotFilterDto(IsNotFilterNodeTestDataFactory.FILTER_VALUE_JSON_LONG_NUMBER_NOT_INT);
		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isEmpty();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   ", "[1,2]", "[{}]", "not-a-status-id", "{" })
	@DisplayName("getFilterConditions should return empty list for invalid or missing status values")
	void testGetFilterConditionsInvalidOrMissingStatusValuesReturnEmptyList(String filterValue) {
		// Given
		FilterDto filterDto = IsNotFilterNodeTestDataFactory.createTimesheetStatusIsNotFilterDto(filterValue);
		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		List<Condition> conditions = isNotFilterNode.getFilterConditions();

		// Then
		assertThat(conditions).isEmpty();
	}

	@Test
	@DisplayName("isSelectDistinct should return true")
	void testIsSelectDistinctReturnsTrue() {
		// Given
		FilterDto filterDto = IsNotFilterNodeTestDataFactory.createTimesheetStatusIsNotFilterDto("1");
		this.filterNodeContext.setFilterDto(filterDto);
		IsNotFilterNode isNotFilterNode = new IsNotFilterNode(this.filterNodeContext);

		// When
		Boolean isDistinct = isNotFilterNode.isSelectDistinct();

		// Then
		assertThat(isDistinct).isTrue();
	}

}
