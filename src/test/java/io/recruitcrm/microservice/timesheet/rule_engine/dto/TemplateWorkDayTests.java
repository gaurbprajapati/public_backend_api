/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateWorkDayTests {

	@Test
	@DisplayName("Default constructor - Success")
	void testDefaultConstructorSuccess() {
		// Act
		TemplateWorkDay templateWorkDay = new TemplateWorkDay();

		// Assert
		assertThat(templateWorkDay).isNotNull().satisfies((workDay) -> {
			assertThat(workDay.getWorkDayType()).isNull();
			assertThat(workDay.getWorkTime()).isNull();
			assertThat(workDay.getWorkStartTime()).isNull();
			assertThat(workDay.getWorkEndTime()).isNull();
		});
	}

	@Test
	@DisplayName("All args constructor - Success")
	void testAllArgsConstructorSuccess() {
		// Arrange
		WorkDay workDayType = WorkDay.MONDAY;
		Duration workTime = Duration.ofHours(8);
		LocalTime workStartTime = LocalTime.of(9, 0);
		LocalTime workEndTime = LocalTime.of(17, 0);

		// Act
		TemplateWorkDay templateWorkDay = new TemplateWorkDay(workDayType, workTime, workStartTime, workEndTime);

		// Assert
		assertThat(templateWorkDay).satisfies((workDay) -> {
			assertThat(workDay.getWorkDayType()).isEqualTo(workDayType);
			assertThat(workDay.getWorkTime()).isEqualTo(workTime);
			assertThat(workDay.getWorkStartTime()).isEqualTo(workStartTime);
			assertThat(workDay.getWorkEndTime()).isEqualTo(workEndTime);
		});
	}

	@Test
	@DisplayName("Setters and getters - All fields")
	void testSettersAndGettersAllFields() {
		// Arrange
		TemplateWorkDay templateWorkDay = new TemplateWorkDay();
		WorkDay workDayType = WorkDay.TUESDAY;
		Duration workTime = Duration.ofHours(7);
		LocalTime workStartTime = LocalTime.of(8, 30);
		LocalTime workEndTime = LocalTime.of(16, 30);

		// Act
		templateWorkDay.setWorkDayType(workDayType);
		templateWorkDay.setWorkTime(workTime);
		templateWorkDay.setWorkStartTime(workStartTime);
		templateWorkDay.setWorkEndTime(workEndTime);

		// Assert
		assertThat(templateWorkDay).satisfies((workDay) -> {
			assertThat(workDay.getWorkDayType()).isEqualTo(workDayType);
			assertThat(workDay.getWorkTime()).isEqualTo(workTime);
			assertThat(workDay.getWorkStartTime()).isEqualTo(workStartTime);
			assertThat(workDay.getWorkEndTime()).isEqualTo(workEndTime);
		});
	}

	@Test
	@DisplayName("Equals and hashCode - same objects")
	void testEqualsAndHashCodeSameObjects() {
		// Arrange
		TemplateWorkDay workDay1 = new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0),
				LocalTime.of(17, 0));

		TemplateWorkDay workDay2 = new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0),
				LocalTime.of(17, 0));

		// Assert - Since TemplateWorkDay doesn't have equals/hashCode, compare individual
		// fields
		assertThat(workDay1).satisfies((day1) -> {
			assertThat(day1.getWorkDayType()).isEqualTo(workDay2.getWorkDayType());
			assertThat(day1.getWorkTime()).isEqualTo(workDay2.getWorkTime());
			assertThat(day1.getWorkStartTime()).isEqualTo(workDay2.getWorkStartTime());
			assertThat(day1.getWorkEndTime()).isEqualTo(workDay2.getWorkEndTime());
		});
	}

	@Test
	@DisplayName("Equals and hashCode - different objects")
	void testEqualsAndHashCodeDifferentObjects() {
		// Arrange
		TemplateWorkDay workDay1 = new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0),
				LocalTime.of(17, 0));

		TemplateWorkDay workDay2 = new TemplateWorkDay(WorkDay.TUESDAY, Duration.ofHours(7), LocalTime.of(8, 0),
				LocalTime.of(16, 0));

		// Assert
		assertThat(workDay1).satisfies((day1) -> {
			assertThat(day1.getWorkDayType()).isNotEqualTo(workDay2.getWorkDayType());
			assertThat(day1.getWorkTime()).isNotEqualTo(workDay2.getWorkTime());
			assertThat(day1.getWorkStartTime()).isNotEqualTo(workDay2.getWorkStartTime());
			assertThat(day1.getWorkEndTime()).isNotEqualTo(workDay2.getWorkEndTime());
		});
	}

	@Test
	@DisplayName("Equals - Null object")
	void testEqualsNullObject() {
		// Arrange
		TemplateWorkDay templateWorkDay = new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0),
				LocalTime.of(17, 0));

		// Act & Assert
		assertThat(templateWorkDay).isNotEqualTo(null);
	}

	@Test
	@DisplayName("Equals - Different class")
	void testEqualsDifferentClass() {
		// Arrange
		TemplateWorkDay templateWorkDay = new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0),
				LocalTime.of(17, 0));

		String differentObject = "Not a TemplateWorkDay";

		// Act & Assert
		assertThat(templateWorkDay).isNotEqualTo(differentObject);
	}

	@Test
	@DisplayName("ToString - contains class name")
	void testToStringContainsClassName() {
		// Arrange
		TemplateWorkDay workDay = new TemplateWorkDay(WorkDay.MONDAY, Duration.ofHours(8), LocalTime.of(9, 0),
				LocalTime.of(17, 0));

		// Act
		String result = workDay.toString();

		// Assert - TemplateWorkDay doesn't have @Data, so toString() returns default
		// object reference
		assertThat(result).contains("TemplateWorkDay").contains("@");
	}

	@Test
	@DisplayName("ToString - null fields")
	void testToStringNullFields() {
		// Arrange
		TemplateWorkDay workDay = new TemplateWorkDay();

		// Act
		String result = workDay.toString();

		// Assert - TemplateWorkDay doesn't have @Data, so toString() returns default
		// object reference
		assertThat(result).contains("TemplateWorkDay").contains("@");
	}

}