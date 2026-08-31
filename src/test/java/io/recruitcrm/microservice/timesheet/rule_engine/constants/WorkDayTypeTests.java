package io.recruitcrm.microservice.timesheet.rule_engine.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkDayType Tests")
class WorkDayTypeTests {

	@Test
	@DisplayName("fromId - with valid WORK_DAY id")
	void testFromIdWithValidWorkDayId() {
		// Act
		WorkDayType result = WorkDayType.fromId(1);

		// Assert
		assertThat(result).isEqualTo(WorkDayType.WORK_DAY);
	}

	@Test
	@DisplayName("fromId - with valid DAY_OFF id")
	void testFromIdWithValidDayOffId() {
		// Act
		WorkDayType result = WorkDayType.fromId(2);

		// Assert
		assertThat(result).isEqualTo(WorkDayType.DAY_OFF);
	}

	@Test
	@DisplayName("fromId - with invalid id throws exception")
	void testFromIdWithInvalidIdThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkDayType.fromId(999)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid WorkDayType id: 999");
	}

	@Test
	@DisplayName("fromId - with negative id throws exception")
	void testFromIdWithNegativeIdThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkDayType.fromId(-1)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid WorkDayType id: -1");
	}

	@Test
	@DisplayName("fromId - with zero id throws exception")
	void testFromIdWithZeroIdThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkDayType.fromId(0)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid WorkDayType id: 0");
	}

	@Test
	@DisplayName("Enum values - verify all values exist")
	void testEnumValuesVerifyAllValuesExist() {
		// Act
		WorkDayType[] values = WorkDayType.values();

		// Assert
		assertThat(values).hasSize(2).contains(WorkDayType.WORK_DAY).contains(WorkDayType.DAY_OFF);
	}

	@Test
	@DisplayName("Enum valueOf - with valid string")
	void testValueOfWithValidString() {
		// Act
		WorkDayType result = WorkDayType.valueOf("WORK_DAY");

		// Assert
		assertThat(result).isEqualTo(WorkDayType.WORK_DAY);
	}

	@Test
	@DisplayName("Enum valueOf - with invalid string throws exception")
	void testValueOfWithInvalidStringThrowsException() {
		// Act & Assert
		assertThatThrownBy(() -> WorkDayType.valueOf("INVALID")).isInstanceOf(IllegalArgumentException.class);
	}

}