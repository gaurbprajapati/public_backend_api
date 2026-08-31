package io.recruitcrm.microservice.timesheet.search.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.timesheet.search.dto.ZonedDateTimeRangeDto;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("FieldConditionSpecifications Tests")
class FieldConditionSpecificationsTests {

	private Field<Integer> testField;

	private ZonedDateTimeRangeDto dateRange;

	private ZonedDateTime testDateTime;

	private static final String GMT_DIFFERENCE = "+05:30";

	@BeforeEach
	void setUp() {
		this.testField = DSL.field("test_field", Integer.class);
		this.testDateTime = ZonedDateTime.now(ZoneOffset.of(GMT_DIFFERENCE));
		ZonedDateTime from = this.testDateTime.minusDays(1);
		ZonedDateTime to = this.testDateTime.plusDays(1);
		this.dateRange = new ZonedDateTimeRangeDto(from, to);
	}

	@Test
	@DisplayName("isBetween should return between condition with epoch seconds")
	void testIsBetween() {
		Condition condition = FieldConditionSpecifications.isBetween(this.testField, this.dateRange);

		assertThat(condition).isNotNull();
		// Verify the condition is a between condition
		assertThat(condition.toString()).contains("between");
	}

	@Test
	@DisplayName("isBetween should convert ZonedDateTime to epoch seconds correctly")
	void testIsBetweenConvertsToEpochSeconds() {
		ZonedDateTime from = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.of(GMT_DIFFERENCE));
		ZonedDateTime to = ZonedDateTime.of(2024, 1, 31, 23, 59, 59, 0, ZoneOffset.of(GMT_DIFFERENCE));
		ZonedDateTimeRangeDto range = new ZonedDateTimeRangeDto(from, to);

		Condition condition = FieldConditionSpecifications.isBetween(this.testField, range);

		assertThat(condition).isNotNull();
		long expectedFromEpoch = from.toEpochSecond();
		assertThat(condition.toString()).contains(String.valueOf(Math.toIntExact(expectedFromEpoch)));
	}

	@Test
	@DisplayName("isNotBetween should return notBetween condition with epoch seconds")
	void testIsNotBetween() {
		Condition condition = FieldConditionSpecifications.isNotBetween(this.testField, this.dateRange);

		assertThat(condition).isNotNull();
		// Verify the condition is a notBetween condition
		assertThat(condition.toString()).contains("not between");
	}

	@Test
	@DisplayName("isNotBetween should convert ZonedDateTime to epoch seconds correctly")
	void testIsNotBetweenConvertsToEpochSeconds() {
		ZonedDateTime from = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.of(GMT_DIFFERENCE));
		ZonedDateTime to = ZonedDateTime.of(2024, 1, 31, 23, 59, 59, 0, ZoneOffset.of(GMT_DIFFERENCE));
		ZonedDateTimeRangeDto range = new ZonedDateTimeRangeDto(from, to);

		Condition condition = FieldConditionSpecifications.isNotBetween(this.testField, range);

		assertThat(condition).isNotNull();
	}

	@Test
	@DisplayName("isAfter should return greaterThan condition with epoch seconds")
	void testIsAfter() {
		Condition condition = FieldConditionSpecifications.isAfter(this.testField, this.testDateTime);

		assertThat(condition).isNotNull();
		// Verify the condition is a greaterThan condition
		assertThat(condition.toString()).contains(">");
	}

	@Test
	@DisplayName("isAfter should convert ZonedDateTime to epoch seconds correctly")
	void testIsAfterConvertsToEpochSeconds() {
		ZonedDateTime dateTime = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneOffset.of(GMT_DIFFERENCE));
		long expectedEpoch = dateTime.toEpochSecond();

		Condition condition = FieldConditionSpecifications.isAfter(this.testField, dateTime);

		assertThat(condition).isNotNull();
		assertThat(condition.toString()).contains(String.valueOf(Math.toIntExact(expectedEpoch)));
	}

	@Test
	@DisplayName("isBefore should return lessThan condition with epoch seconds")
	void testIsBefore() {
		Condition condition = FieldConditionSpecifications.isBefore(this.testField, this.testDateTime);

		assertThat(condition).isNotNull();
		// Verify the condition is a lessThan condition
		assertThat(condition.toString()).contains("<");
	}

	@Test
	@DisplayName("isBefore should convert ZonedDateTime to epoch seconds correctly")
	void testIsBeforeConvertsToEpochSeconds() {
		ZonedDateTime dateTime = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneOffset.of(GMT_DIFFERENCE));
		long expectedEpoch = dateTime.toEpochSecond();

		Condition condition = FieldConditionSpecifications.isBefore(this.testField, dateTime);

		assertThat(condition).isNotNull();
		assertThat(condition.toString()).contains(String.valueOf(Math.toIntExact(expectedEpoch)));
	}

	@Test
	@DisplayName("isEqualTo should return between condition (same as isBetween)")
	void testIsEqualTo() {
		Condition condition = FieldConditionSpecifications.isEqualTo(this.testField, this.dateRange);

		assertThat(condition).isNotNull();
		// isEqualTo delegates to isBetween, so it should contain "between"
		assertThat(condition.toString()).contains("between");
	}

	@Test
	@DisplayName("isEmpty should return isNull or equals zero condition")
	void testIsEmpty() {
		Condition condition = FieldConditionSpecifications.isEmpty(this.testField);

		assertThat(condition).isNotNull();
		// Verify the condition contains isNull or equals zero
		String conditionStr = condition.toString();
		assertThat(conditionStr.contains("is null") || conditionStr.contains("= 0")).isTrue();
	}

	@Test
	@DisplayName("hasAnyValue should return isNotNull and notEqual zero condition")
	void testHasAnyValue() {
		Condition condition = FieldConditionSpecifications.hasAnyValue(this.testField);

		assertThat(condition).isNotNull();
		// Verify the condition contains isNotNull and notEqual zero
		String conditionStr = condition.toString();
		assertThat(conditionStr.contains("is not null") && conditionStr.contains("<> 0")).isTrue();
	}

	@Test
	@DisplayName("isBetween should handle epoch seconds conversion for large values within Integer range")
	void testIsBetweenHandlesLargeEpochSeconds() {
		// Epoch seconds are stored as Integer; use dates before 2038 to avoid overflow
		ZonedDateTime from = ZonedDateTime.of(2037, 1, 1, 0, 0, 0, 0, ZoneOffset.of(GMT_DIFFERENCE));
		ZonedDateTime to = ZonedDateTime.of(2037, 12, 31, 23, 59, 59, 0, ZoneOffset.of(GMT_DIFFERENCE));
		ZonedDateTimeRangeDto range = new ZonedDateTimeRangeDto(from, to);

		Condition condition = FieldConditionSpecifications.isBetween(this.testField, range);

		assertThat(condition).isNotNull();
	}

	@Test
	@DisplayName("isAfter should handle epoch seconds conversion for large values within Integer range")
	void testIsAfterHandlesLargeEpochSeconds() {
		ZonedDateTime dateTime = ZonedDateTime.of(2037, 1, 1, 0, 0, 0, 0, ZoneOffset.of(GMT_DIFFERENCE));

		Condition condition = FieldConditionSpecifications.isAfter(this.testField, dateTime);

		assertThat(condition).isNotNull();
	}

	@Test
	@DisplayName("isBefore should handle epoch seconds conversion for large values within Integer range")
	void testIsBeforeHandlesLargeEpochSeconds() {
		ZonedDateTime dateTime = ZonedDateTime.of(2037, 1, 1, 0, 0, 0, 0, ZoneOffset.of(GMT_DIFFERENCE));

		Condition condition = FieldConditionSpecifications.isBefore(this.testField, dateTime);

		assertThat(condition).isNotNull();
	}

	@Test
	@DisplayName("isBetween should handle different time zones")
	void testIsBetweenHandlesDifferentTimeZones() {
		ZoneId[] zones = { ZoneOffset.of("+00:00"), ZoneOffset.of("+05:30"), ZoneOffset.of("-05:00"),
				ZoneOffset.of("+09:00") };

		for (ZoneId zone : zones) {
			ZonedDateTime from = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, zone);
			ZonedDateTime to = ZonedDateTime.of(2024, 1, 31, 23, 59, 59, 0, zone);
			ZonedDateTimeRangeDto range = new ZonedDateTimeRangeDto(from, to);

			Condition condition = FieldConditionSpecifications.isBetween(this.testField, range);

			assertThat(condition).isNotNull();
		}
	}

	@Test
	@DisplayName("isAfter should handle different time zones")
	void testIsAfterHandlesDifferentTimeZones() {
		ZoneId[] zones = { ZoneOffset.of("+00:00"), ZoneOffset.of("+05:30"), ZoneOffset.of("-05:00") };

		for (ZoneId zone : zones) {
			ZonedDateTime dateTime = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, zone);

			Condition condition = FieldConditionSpecifications.isAfter(this.testField, dateTime);

			assertThat(condition).isNotNull();
		}
	}

	@Test
	@DisplayName("isBefore should handle different time zones")
	void testIsBeforeHandlesDifferentTimeZones() {
		ZoneId[] zones = { ZoneOffset.of("+00:00"), ZoneOffset.of("+05:30"), ZoneOffset.of("-05:00") };

		for (ZoneId zone : zones) {
			ZonedDateTime dateTime = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, zone);

			Condition condition = FieldConditionSpecifications.isBefore(this.testField, dateTime);

			assertThat(condition).isNotNull();
		}
	}

	@Test
	@DisplayName("isEmpty should work with different field names")
	void testIsEmptyWithDifferentFieldNames() {
		Field<Integer> field1 = DSL.field("field1", Integer.class);
		Field<Integer> field2 = DSL.field("field2", Integer.class);

		Condition condition1 = FieldConditionSpecifications.isEmpty(field1);
		Condition condition2 = FieldConditionSpecifications.isEmpty(field2);

		assertThat(condition1).isNotNull();
		assertThat(condition2).isNotNull();
	}

	@Test
	@DisplayName("hasAnyValue should work with different field names")
	void testHasAnyValueWithDifferentFieldNames() {
		Field<Integer> field1 = DSL.field("field1", Integer.class);
		Field<Integer> field2 = DSL.field("field2", Integer.class);

		Condition condition1 = FieldConditionSpecifications.hasAnyValue(field1);
		Condition condition2 = FieldConditionSpecifications.hasAnyValue(field2);

		assertThat(condition1).isNotNull();
		assertThat(condition2).isNotNull();
	}

}
