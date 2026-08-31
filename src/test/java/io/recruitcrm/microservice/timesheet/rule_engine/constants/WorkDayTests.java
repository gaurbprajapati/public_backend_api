package io.recruitcrm.microservice.timesheet.rule_engine.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkDay Tests")
class WorkDayTests {

	@Test
	@DisplayName("getWorkDayType - with valid string day")
	void testGetWorkDayTypeWithValidStringDay() {
		// Act
		WorkDay result = WorkDay.getWorkDayType("Monday");

		// Assert
		assertThat(result).isEqualTo(WorkDay.MONDAY);
	}

	@Test
	@DisplayName("getWorkDayType - with case insensitive string day")
	void testGetWorkDayTypeWithCaseInsensitiveStringDay() {
		// Act
		WorkDay result = WorkDay.getWorkDayType("monday");

		// Assert
		assertThat(result).isEqualTo(WorkDay.MONDAY);
	}

	@Test
	@DisplayName("getWorkDayType - with valid integer day")
	void testGetWorkDayTypeWithValidIntegerDay() {
		// Act
		WorkDay result = WorkDay.getWorkDayType(1);

		// Assert
		assertThat(result).isEqualTo(WorkDay.MONDAY);
	}

	@Test
	@DisplayName("getWorkDayType - with null string throws exception")
	void testGetWorkDayTypeWithNullStringThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkDay.getWorkDayType((String) null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Day cannot be null or empty");
	}

	@Test
	@DisplayName("getWorkDayType - with empty string throws exception")
	void testGetWorkDayTypeWithEmptyStringThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkDay.getWorkDayType("")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Day cannot be null or empty");
	}

	@Test
	@DisplayName("getWorkDayType - with invalid string throws exception")
	void testGetWorkDayTypeWithInvalidStringThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkDay.getWorkDayType("InvalidDay")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid day: InvalidDay");
	}

	@Test
	@DisplayName("getWorkDayType - with null integer throws exception")
	void testGetWorkDayTypeWithNullIntegerThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkDay.getWorkDayType((Integer) null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Day cannot be null");
	}

	@Test
	@DisplayName("getWorkDayType - with invalid integer throws exception")
	void testGetWorkDayTypeWithInvalidIntegerThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkDay.getWorkDayType(999)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid day id: 999");
	}

	@Test
	@DisplayName("getWorkDayType - with negative integer throws exception")
	void testGetWorkDayTypeWithNegativeIntegerThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkDay.getWorkDayType(-1)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid day id: -1");
	}

	@Test
	@DisplayName("getWorkDayType - with zero integer throws exception")
	void testGetWorkDayTypeWithZeroIntegerThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkDay.getWorkDayType(0)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid day id: 0");
	}

	@Test
	@DisplayName("Enum values - verify all values exist")
	void testEnumValuesVerifyAllValuesExist() {
		// Act
		WorkDay[] values = WorkDay.values();

		// Assert
		assertThat(values).hasSize(7)
			.contains(WorkDay.MONDAY)
			.contains(WorkDay.TUESDAY)
			.contains(WorkDay.WEDNESDAY)
			.contains(WorkDay.THURSDAY)
			.contains(WorkDay.FRIDAY)
			.contains(WorkDay.SATURDAY)
			.contains(WorkDay.SUNDAY);
	}

	@Test
	@DisplayName("Enum valueOf - with valid string")
	void testValueOfWithValidString() {
		// Act
		WorkDay result = WorkDay.valueOf("MONDAY");

		// Assert
		assertThat(result).isEqualTo(WorkDay.MONDAY);
	}

	@Test
	@DisplayName("Enum valueOf - with invalid string throws exception")
	void testValueOfWithInvalidStringThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkDay.valueOf("INVALID")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("Getter methods - verify id and day values")
	void testGetterMethodsVerifyIdAndDayValues() {
		// Assert
		assertThat(WorkDay.MONDAY).satisfies((day) -> assertThat(day.getId()).isEqualTo(1))
			.satisfies((day) -> assertThat(day.getDay()).isEqualTo("Monday"));

		assertThat(WorkDay.TUESDAY).satisfies((day) -> assertThat(day.getId()).isEqualTo(2))
			.satisfies((day) -> assertThat(day.getDay()).isEqualTo("Tuesday"));

		assertThat(WorkDay.WEDNESDAY).satisfies((day) -> assertThat(day.getId()).isEqualTo(3))
			.satisfies((day) -> assertThat(day.getDay()).isEqualTo("Wednesday"));

		assertThat(WorkDay.THURSDAY).satisfies((day) -> assertThat(day.getId()).isEqualTo(4))
			.satisfies((day) -> assertThat(day.getDay()).isEqualTo("Thursday"));

		assertThat(WorkDay.FRIDAY).satisfies((day) -> assertThat(day.getId()).isEqualTo(5))
			.satisfies((day) -> assertThat(day.getDay()).isEqualTo("Friday"));

		assertThat(WorkDay.SATURDAY).satisfies((day) -> assertThat(day.getId()).isEqualTo(6))
			.satisfies((day) -> assertThat(day.getDay()).isEqualTo("Saturday"));

		assertThat(WorkDay.SUNDAY).satisfies((day) -> assertThat(day.getId()).isEqualTo(7))
			.satisfies((day) -> assertThat(day.getDay()).isEqualTo("Sunday"));
	}

}