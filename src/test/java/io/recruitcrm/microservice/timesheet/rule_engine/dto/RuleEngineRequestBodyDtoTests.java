/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineRequestBodyDtoTests {

	@Test
	@DisplayName("Builder - Creates DTO with all fields")
	void testBuilderCreatesDtoWithAllFields() {
		// Act
		RuleEngineRequestBodyDto dto = RuleEngineRequestBodyDto.builder()
			.timesheetId(123)
			.refreshStoredData(true)
			.build();

		// Assert
		assertThat(dto.getTimesheetId()).isEqualTo(123);
		assertThat(dto.getRefreshStoredData()).isTrue();
	}

	@Test
	@DisplayName("Builder - Creates DTO with default refreshStoredData")
	void testBuilderCreatesDtoWithDefaultRefreshStoredData() {
		// Act
		RuleEngineRequestBodyDto dto = RuleEngineRequestBodyDto.builder().timesheetId(123).build();

		// Assert
		assertThat(dto.getTimesheetId()).isEqualTo(123);
		assertThat(dto.getRefreshStoredData()).isFalse();
	}

	@Test
	@DisplayName("No args constructor - Creates DTO with null values")
	void testNoArgsConstructorCreatesDtoWithNullValues() {
		// Act
		RuleEngineRequestBodyDto dto = new RuleEngineRequestBodyDto();

		// Assert
		assertThat(dto.getTimesheetId()).isNull();
		assertThat(dto.getRefreshStoredData()).isFalse(); // @Builder.Default sets this to
															// false
	}

	@Test
	@DisplayName("All args constructor - Creates DTO with provided values")
	void testAllArgsConstructorCreatesDtoWithProvidedValues() {
		// Act
		RuleEngineRequestBodyDto dto = new RuleEngineRequestBodyDto(123, true);

		// Assert
		assertThat(dto.getTimesheetId()).isEqualTo(123);
		assertThat(dto.getRefreshStoredData()).isTrue();
	}

	@Test
	@DisplayName("Setters - Update DTO values")
	void testSettersUpdateDtoValues() {
		// Arrange
		RuleEngineRequestBodyDto dto = new RuleEngineRequestBodyDto();

		// Act
		dto.setTimesheetId(456);
		dto.setRefreshStoredData(false);

		// Assert
		assertThat(dto.getTimesheetId()).isEqualTo(456);
		assertThat(dto.getRefreshStoredData()).isFalse();
	}

}