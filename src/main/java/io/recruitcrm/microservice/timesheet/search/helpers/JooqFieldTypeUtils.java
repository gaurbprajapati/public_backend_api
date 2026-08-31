package io.recruitcrm.microservice.timesheet.search.helpers;

import org.jooq.Field;
import org.jooq.impl.DSL;

public final class JooqFieldTypeUtils {

	private JooqFieldTypeUtils() {
		// Utility class - prevent instantiation
	}

	@SuppressWarnings("unchecked")
	public static <T> Field<T> cast(Field<?> field, Class<T> type) {
		if (type.isAssignableFrom(field.getType())) {
			return (Field<T>) field;
		}
		else {
			throw new IllegalArgumentException("Field type mismatch: expected " + type + " but got " + field.getType());
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> Field<T> safeExact(Field<?> field, Class<T> type) {
		if (!type.equals(field.getType())) {
			throw new IllegalArgumentException("Expected " + type + " but got " + field.getType());
		}
		return (Field<T>) field;
	}

	public static <T> Field<T> coerce(Field<?> field, Class<T> type) {
		return DSL.field(field.getName(), type);
	}

}
