package io.recruitcrm.microservice.timesheet.search.helpers;

import java.time.ZonedDateTime;

import org.jooq.Condition;
import org.jooq.Field;

import io.recruitcrm.microservice.timesheet.search.dto.ZonedDateTimeRangeDto;

public final class FieldConditionSpecifications {

	private FieldConditionSpecifications() {
		// Utility class
	}

	public static Condition isBetween(Field<Integer> field, ZonedDateTimeRangeDto dateRange) {
		return field.between(Math.toIntExact(dateRange.getFrom().toEpochSecond()),
				Math.toIntExact(dateRange.getTo().toEpochSecond()));
	}

	public static Condition isNotBetween(Field<Integer> field, ZonedDateTimeRangeDto dateRange) {
		return field.notBetween(Math.toIntExact(dateRange.getFrom().toEpochSecond()),
				Math.toIntExact(dateRange.getTo().toEpochSecond()));
	}

	public static Condition isAfter(Field<Integer> field, ZonedDateTime zonedDateTime) {
		return field.greaterThan(Math.toIntExact(zonedDateTime.toEpochSecond()));
	}

	public static Condition isBefore(Field<Integer> field, ZonedDateTime zonedDateTime) {
		return field.lessThan(Math.toIntExact(zonedDateTime.toEpochSecond()));
	}

	public static Condition isEqualTo(Field<Integer> field, ZonedDateTimeRangeDto dateRange) {
		return isBetween(field, dateRange);
	}

	public static Condition isEmpty(Field<Integer> field) {
		return field.isNull().or(field.eq(0));
	}

	public static Condition hasAnyValue(Field<Integer> field) {
		return field.isNotNull().and(field.notEqual(0));
	}

}
