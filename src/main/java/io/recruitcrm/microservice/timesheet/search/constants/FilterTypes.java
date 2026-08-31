package io.recruitcrm.microservice.timesheet.search.constants;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FilterTypes {

	IS("is"), IS_NOT("is_not"), IS_BEFORE("is_before"), IS_AFTER("is_after"), IS_BETWEEN("is_between"),
	IS_NOT_BETWEEN("is_not_between"), IS_EQUAL_TO("is_equal_to"), IS_EMPTY("is_empty"), NOT_EMPTY("not_empty"),
	HAS_ANY_VALUE("has_any_value"), CONTAINS_AT_LEAST_ONE("contains_at_least_one"),
	DOES_NOT_CONTAIN("does_not_contain"), CONTAINS("contains"), IS_LESS_THAN("is_lt"), IS_MORE_THAN("is_mt");

	private final String value;

	FilterTypes(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return this.value;
	}

	@JsonCreator
	public static FilterTypes fromValue(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		// Try exact match first (case-insensitive)
		String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
		for (FilterTypes filterType : FilterTypes.values()) {
			if (filterType.value.equalsIgnoreCase(normalizedValue)) {
				return filterType;
			}
		}

		// Fallback: try uppercase enum name match (for backward compatibility)
		try {
			return FilterTypes.valueOf(value.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException(
					"Unknown filter type: " + value + ". Valid values are: " + getValidFilterTypes());
		}
	}

	private static String getValidFilterTypes() {
		StringBuilder sb = new StringBuilder();
		FilterTypes[] values = FilterTypes.values();
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(values[i].value);
		}
		return sb.toString();
	}

}
