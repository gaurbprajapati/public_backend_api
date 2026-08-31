package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Handles special permission fields that can have both string and integer values. This
 * class maintains a registry of special permission fields and provides methods to process
 * their values.
 */
public final class SpecialPermissionFieldHandler {

	private SpecialPermissionFieldHandler() {
		throw new IllegalStateException("Utility class");
	}

	private static final Set<String> SPECIAL_PERMISSION_FIELDS = new HashSet<>();

	static {
		// Register known special permission fields
		registerSpecialField("recruiterperformancereportaccess");
		registerSpecialField("targetreportaccess");
		registerSpecialField("dataEnrichmentReportAccess");
	}

	/**
	 * Register a new special permission field. This method should be called during
	 * application startup for any new special permission fields.
	 * @param fieldName The name of the special permission field to register
	 */
	public static void registerSpecialField(String fieldName) {
		SPECIAL_PERMISSION_FIELDS.add(fieldName.toLowerCase(Locale.ROOT));
	}

	/**
	 * Check if a field is a special permission field.
	 * @param fieldName The name of the field to check
	 * @return true if the field is a special permission field, false otherwise
	 */
	public static boolean isSpecialPermissionField(String fieldName) {
		return SPECIAL_PERMISSION_FIELDS.contains(fieldName.toLowerCase(Locale.ROOT));
	}

	/**
	 * Process a special permission field value and convert it to an integer. Handles both
	 * string and integer inputs.
	 * @param value The value to process (can be String or Integer)
	 * @return The integer representation of the permission value
	 * @throws IllegalArgumentException if the value is invalid
	 */
	public static int processSpecialPermissionValue(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("Permission value cannot be null");
		}

		if (value instanceof Integer intValue) {
			if (SpecialPermissionValue.isValidIntValue(intValue)) {
				return intValue;
			}
			throw new IllegalArgumentException("Invalid integer permission value: " + intValue);
		}

		if (value instanceof String stringValue) {
			SpecialPermissionValue permissionValue = SpecialPermissionValue.fromString(stringValue);
			if (permissionValue != null) {
				return permissionValue.getIntValue();
			}
			throw new IllegalArgumentException("Invalid string permission value: " + stringValue);
		}

		throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
	}

	/**
	 * Get the string representation of a special permission value.
	 * @param intValue The integer value to convert
	 * @return The string representation of the permission value
	 * @throws IllegalArgumentException if the integer value is invalid
	 */
	public static String getStringRepresentation(int intValue) {
		SpecialPermissionValue permissionValue = SpecialPermissionValue.fromInt(intValue);
		if (permissionValue == null) {
			throw new IllegalArgumentException("Invalid integer permission value: " + intValue);
		}
		return permissionValue.getStringValue();
	}

}