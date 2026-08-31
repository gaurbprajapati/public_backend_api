/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDayType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineTimeLogMapperTests {

	private final RuleEngineTimeLogMapper mapper = RuleEngineTimeLogMapper.INSTANCE;

	@Test
	@DisplayName("To time log - Complete mapping")
	void testToTimeLogCompleteMapping() {
		// Arrange
		io.recruitcrm.contract_staffing.entity.model.TimeLog sourceTimeLog = new io.recruitcrm.contract_staffing.entity.model.TimeLog();
		sourceTimeLog.setId(1);
		sourceTimeLog.setDate(1703116800); // 2023-12-21 in seconds (19700 * 86400)
		sourceTimeLog.setDayTypeId(1); // Assuming 1 is a valid WorkDayType ID
		sourceTimeLog.setWorkTime(28800); // 8 hours in seconds
		sourceTimeLog.setWorkStartTime(32400); // 9:00 AM in seconds
		sourceTimeLog.setWorkEndTime(61200); // 5:00 PM in seconds
		sourceTimeLog.setBreakTime(3600); // 1 hour in seconds
		sourceTimeLog.setOverTime(7200); // 2 hours in seconds
		sourceTimeLog.setRemark("Test time log");
		sourceTimeLog.setTotalTime(36000); // 10 hours in seconds
		sourceTimeLog.setTimesheet(new Timesheet());
		sourceTimeLog.setPayData(100.0f);
		sourceTimeLog.setBillData(200.0f);

		// Act
		TimeLog result = this.mapper.toTimeLog(sourceTimeLog);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1);
		assertThat(result.getDate()).isEqualTo(LocalDate.of(2023, 12, 21)); // Fixed:
																			// 1703116800
																			// seconds =
																			// 2023-12-21
		assertThat(result.getDayType()).isNotNull();
		assertThat(result.getWorkTime()).isEqualTo(Duration.ofHours(8));
		assertThat(result.getWorkStartTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(result.getWorkEndTime()).isEqualTo(LocalTime.of(17, 0));
		assertThat(result.getBreakTime()).isEqualTo(Duration.ofHours(1));
		assertThat(result.getOverTime()).isEqualTo(Duration.ofHours(2));
		assertThat(result.getRemark()).isEqualTo("Test time log");
		assertThat(result.getTotalTime()).isEqualTo(Duration.ofHours(10));
		assertThat(result.getTimesheet()).isNotNull();
		assertThat(result.getPayData()).isEqualTo(100.0f); // Fixed: direct mapping, not
															// BigDecimal
		assertThat(result.getBillData()).isEqualTo(200.0f); // Fixed: direct mapping, not
															// BigDecimal
	}

	@Test
	@DisplayName("To time log - Null values")
	void testToTimeLogNullValues() {
		// Arrange
		io.recruitcrm.contract_staffing.entity.model.TimeLog sourceTimeLog = new io.recruitcrm.contract_staffing.entity.model.TimeLog();
		sourceTimeLog.setId(1);
		// All other fields are null

		// Act
		TimeLog result = this.mapper.toTimeLog(sourceTimeLog);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1);
		assertThat(result.getDate()).isNull();
		assertThat(result.getDayType()).isNull();
		assertThat(result.getWorkTime()).isNull();
		assertThat(result.getWorkStartTime()).isNull();
		assertThat(result.getWorkEndTime()).isNull();
		assertThat(result.getBreakTime()).isNull();
		assertThat(result.getOverTime()).isNull();
		assertThat(result.getRemark()).isNull();
		assertThat(result.getTotalTime()).isNull();
		assertThat(result.getTimesheet()).isNull();
		assertThat(result.getPayData()).isEqualTo(0.0f); // Fixed: null becomes 0.0f in
															// direct mapping
		assertThat(result.getBillData()).isEqualTo(0.0f); // Fixed: null becomes 0.0f in
															// direct mapping
	}

	@Test
	@DisplayName("From seconds to local date - Valid seconds")
	void testFromSecondsToLocalDateValidSeconds() {
		// Arrange
		Integer seconds = 1703116800; // 2023-12-21 in seconds (19700 * 86400)

		// Act
		LocalDate result = this.mapper.fromSecondsToLocalDate(seconds);

		// Assert
		assertThat(result).isEqualTo(LocalDate.of(2023, 12, 21)); // Fixed: 1703116800
																	// seconds =
																	// 2023-12-21
	}

	@Test
	@DisplayName("From seconds to local date - Null seconds")
	void testFromSecondsToLocalDateNullSeconds() {
		// Act
		LocalDate result = this.mapper.fromSecondsToLocalDate(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("From seconds to local date - Zero seconds")
	void testFromSecondsToLocalDateZeroSeconds() {
		// Arrange
		Integer seconds = 0;

		// Act
		LocalDate result = this.mapper.fromSecondsToLocalDate(seconds);

		// Assert
		assertThat(result).isEqualTo(LocalDate.ofEpochDay(0));
	}

	@Test
	@DisplayName("From ID to work day type - Valid ID")
	void testFromIdToWorkDayTypeValidId() {
		// Arrange
		Integer id = 1; // Assuming 1 is a valid WorkDayType ID

		// Act
		WorkDayType result = this.mapper.fromIdToWorkDayType(id);

		// Assert
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("From ID to work day type - Null ID")
	void testFromIdToWorkDayTypeNullId() {
		// Act
		WorkDayType result = this.mapper.fromIdToWorkDayType(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("From seconds to duration - Valid seconds")
	void testFromSecondsToDurationValidSeconds() {
		// Arrange
		Integer seconds = 3600; // 1 hour

		// Act
		Duration result = this.mapper.fromSecondsToDuration(seconds);

		// Assert
		assertThat(result).isEqualTo(Duration.ofHours(1));
	}

	@Test
	@DisplayName("From seconds to duration - Null seconds")
	void testFromSecondsToDurationNullSeconds() {
		// Act
		Duration result = this.mapper.fromSecondsToDuration(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("From seconds to duration - Zero seconds")
	void testFromSecondsToDurationZeroSeconds() {
		// Arrange
		Integer seconds = 0;

		// Act
		Duration result = this.mapper.fromSecondsToDuration(seconds);

		// Assert
		assertThat(result).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("From seconds to local time - Valid seconds")
	void testFromSecondsToLocalTimeValidSeconds() {
		// Arrange
		Integer seconds = 32400; // 9:00 AM

		// Act
		LocalTime result = this.mapper.fromSecondsToLocalTime(seconds);

		// Assert
		assertThat(result).isEqualTo(LocalTime.of(9, 0));
	}

	@Test
	@DisplayName("From seconds to local time - Null seconds")
	void testFromSecondsToLocalTimeNullSeconds() {
		// Act
		LocalTime result = this.mapper.fromSecondsToLocalTime(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("From seconds to local time - Zero seconds")
	void testFromSecondsToLocalTimeZeroSeconds() {
		// Arrange
		Integer seconds = 0;

		// Act
		LocalTime result = this.mapper.fromSecondsToLocalTime(seconds);

		// Assert
		assertThat(result).isEqualTo(LocalTime.MIDNIGHT);
	}

	@Test
	@DisplayName("From seconds to local time - End of day")
	void testFromSecondsToLocalTimeEndOfDay() {
		// Arrange
		Integer seconds = 86399; // 23:59:59

		// Act
		LocalTime result = this.mapper.fromSecondsToLocalTime(seconds);

		// Assert
		assertThat(result).isEqualTo(LocalTime.of(23, 59, 59));
	}

	@Test
	@DisplayName("From seconds to local date - Large value")
	void testFromSecondsToLocalDateLargeValue() {
		// Arrange
		Integer seconds = 1728000000; // Future date in seconds (20000 * 86400)

		// Act
		LocalDate result = this.mapper.fromSecondsToLocalDate(seconds);

		// Assert
		assertThat(result).isAfter(LocalDate.of(2023, 12, 1));
	}

	@Test
	@DisplayName("From seconds to duration - Large value")
	void testFromSecondsToDurationLargeValue() {
		// Arrange
		Integer seconds = 86400; // 24 hours

		// Act
		Duration result = this.mapper.fromSecondsToDuration(seconds);

		// Assert
		assertThat(result).isEqualTo(Duration.ofDays(1));
	}

	@Test
	@DisplayName("From seconds to local time - Large value")
	void testFromSecondsToLocalTimeLargeValue() {
		// Arrange
		Integer seconds = 43200; // 12:00 PM

		// Act
		LocalTime result = this.mapper.fromSecondsToLocalTime(seconds);

		// Assert
		assertThat(result).isEqualTo(LocalTime.NOON);
	}

}