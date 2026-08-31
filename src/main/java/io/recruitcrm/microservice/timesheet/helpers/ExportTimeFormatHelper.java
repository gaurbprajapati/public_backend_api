package io.recruitcrm.microservice.timesheet.helpers;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Helper for converting time values to 12-hour (AM/PM) or 24-hour format in exports.
 * time_format_type 0 = 12-hour, 1 = 24-hour (no conversion).
 */
public final class ExportTimeFormatHelper {

	private static final Pattern HH_MM_PATTERN = Pattern.compile("\\d{1,2}:\\d{2}");

	private static final DateTimeFormatter INPUT_24H = DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH);

	private static final DateTimeFormatter OUTPUT_12H = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

	private ExportTimeFormatHelper() {
		// utility class
	}

	/**
	 * Convert a time string that may contain 24-hour times to 12-hour AM/PM format. Only
	 * converts strings matching HH:MM or HH:MM-HH:MM patterns. Leaves decimal values
	 * (e.g. "8.00", "0.50") and empty strings unchanged.
	 * @param value Time string in 24-hour format (e.g. "09:00-12:00", "07:00-19:00,
	 * 20:00-21:00")
	 * @return Converted string in 12-hour format (e.g. "09:00 AM-12:00 PM") or original
	 * if not a time pattern
	 */
	public static String convertTo12HourFormat(String value) {
		if (value == null || value.isBlank()) {
			return (value != null) ? value : "";
		}

		String trimmed = value.trim();

		// Skip decimal values (hours like "8.00", "0.50")
		if (isDecimalHours(trimmed)) {
			return value;
		}

		// Split by ", " to handle comma-separated ranges
		String[] parts = trimmed.split(",\\s*");
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < parts.length; i++) {
			if (i > 0) {
				result.append(", ");
			}
			result.append(convertPartTo12Hour(parts[i].trim()));
		}

		return result.toString();
	}

	private static boolean isDecimalHours(String value) {
		// Match "8.00", "0.50", "10.5" - decimal hour values
		return value.matches("\\d+\\.\\d+");
	}

	private static String convertPartTo12Hour(String part) {
		if (part.isEmpty()) {
			return part;
		}

		// Range: "HH:MM-HH:MM"
		if (part.contains("-")) {
			String[] rangeParts = part.split("-", 2);
			String start = convertSingleTimeTo12Hour(rangeParts[0].trim());
			String end = (rangeParts.length > 1) ? convertSingleTimeTo12Hour(rangeParts[1].trim()) : "";
			if (!start.isEmpty() && !end.isEmpty()) {
				return start + "-" + end;
			}
			if (!start.isEmpty()) {
				return start;
			}
			return part;
		}

		// Single time: "HH:MM"
		String converted = convertSingleTimeTo12Hour(part);
		return !converted.isEmpty() ? converted : part;
	}

	private static String convertSingleTimeTo12Hour(String timeStr) {
		if (timeStr == null || !HH_MM_PATTERN.matcher(timeStr).matches()) {
			return "";
		}

		try {
			LocalTime time = LocalTime.parse(timeStr, INPUT_24H);
			return time.format(OUTPUT_12H);
		}
		catch (Exception ex) {
			return "";
		}
	}

	/**
	 * Apply time format conversion to a value if use12HourFormat is true.
	 * @param value The time value (24-hour format)
	 * @param use12HourFormat Whether to convert to 12-hour AM/PM
	 * @return Converted value or original if use12HourFormat is false
	 */
	public static String applyTimeFormat(String value, boolean use12HourFormat) {
		if (!use12HourFormat) {
			return (value != null) ? value : "";
		}
		return convertTo12HourFormat(value);
	}

}
