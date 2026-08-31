/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkLogTypeTests {

	@Test
	@DisplayName("Value of - Valid START_AND_END_TIME ID")
	void testValueOfValidStartAndEndTimeIdReturnsWorkLogType() {
		// Act
		WorkLogType workLogType = WorkLogType.valueOf(2);

		// Assert
		assertThat(workLogType).isEqualTo(WorkLogType.START_AND_END_TIME);
	}

	@Test
	@DisplayName("Value of - Valid WORK_HOUR ID")
	void testValueOfValidWorkHourIdReturnsWorkLogType() {
		// Act
		WorkLogType workLogType = WorkLogType.valueOf(1);

		// Assert
		assertThat(workLogType).isEqualTo(WorkLogType.WORK_HOUR);
	}

	@Test
	@DisplayName("Value of - Invalid ID throws IllegalArgumentException")
	void testValueOfInvalidIdThrowsIllegalArgumentException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkLogType.valueOf(999)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid WorkLogType id: 999");
	}

	@Test
	@DisplayName("Get type ID - Returns correct type ID")
	void testGetTypeIdReturnsCorrectTypeId() {
		// Act & Assert
		assertThat(WorkLogType.START_AND_END_TIME.getTypeId()).isEqualTo(2);
		assertThat(WorkLogType.WORK_HOUR.getTypeId()).isEqualTo(1);
	}

}