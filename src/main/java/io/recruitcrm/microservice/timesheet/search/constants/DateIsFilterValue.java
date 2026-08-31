package io.recruitcrm.microservice.timesheet.search.constants;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;

@Getter
public enum DateIsFilterValue {

	TODAY("today"), YESTERDAY("yesterday"), THIS_WEEK("this_week"), LAST_WEEK("last_week"), THIS_MONTH("this_month"),
	LAST_MONTH("last_month"), THIS_QUARTER("this_quarter"), LAST_QUARTER("last_quarter"), THIS_YEAR("this_year"),
	LAST_YEAR("last_year"), ALL_TIME("all_time"), LAST_30("last_30"), LAST_60("last_60"), LAST_90("last_90"),
	LAST_365("last_365");

	private final String label;

	DateIsFilterValue(String label) {
		this.label = label;
	}

	private static final Map<String, DateIsFilterValue> LOOKUP = Arrays.stream(values())
		.collect(Collectors.toUnmodifiableMap((f) -> f.label.toUpperCase(Locale.ROOT), Function.identity()));

	public static DateIsFilterValue fromValue(String value) {
		if (value == null) {
			return null;
		}
		DateIsFilterValue type = LOOKUP.get(value.toUpperCase(Locale.ROOT));
		if (type == null) {
			throw new IllegalArgumentException("Invalid value type: " + value);
		}
		return type;
	}

}
