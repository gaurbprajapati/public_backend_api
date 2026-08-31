package io.recruitcrm.microservice.timesheet.services.export;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.extra_fields.ExtraFieldDefinitionDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityType;
import io.recruitcrm.microservice.timesheet.repositories.extra_fields.IExtraFieldsRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service to manage custom column field types and handle post-processing conversions.
 * Caches field type information and provides conversion methods for different data types.
 */
@Service
public class CustomColumnTypeService {

	private final IExtraFieldsRepository extraFieldsRepository;

	private final AuthHolder authHolder;

	// Cache for field types per account (accountId -> Map<columnId, fieldType>)
	private final Map<Integer, Map<Integer, String>> fieldTypeCache = new ConcurrentHashMap<>();

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy")
		.withZone(ZoneOffset.UTC);

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
		.withZone(ZoneOffset.UTC);

	public CustomColumnTypeService(IExtraFieldsRepository extraFieldsRepository, AuthHolder authHolder) {
		this.extraFieldsRepository = extraFieldsRepository;
		this.authHolder = authHolder;
	}

	/**
	 * Get field types for a list of custom column IDs. Uses caching for performance.
	 * @param columnIds List of column IDs (e.g., [1, 5, 10] for custcolumn1, custcolumn5,
	 * custcolumn10)
	 * @return Map of columnId to field type (e.g., {1: "text", 5: "date", 10: "number"})
	 */
	public Map<Integer, String> getFieldTypes(List<Integer> columnIds) {
		Integer accountId = this.authHolder.getAuthenticationPrincipalOrganizationIdentifier();

		// Get cached field types for this account
		Map<Integer, String> accountFieldTypes = this.fieldTypeCache.computeIfAbsent(accountId,
				(k) -> new ConcurrentHashMap<>());

		// Check if we need to fetch any missing field types
		List<Integer> missingColumnIds = columnIds.stream()
			.filter((columnId) -> !accountFieldTypes.containsKey(columnId))
			.toList();

		if (!missingColumnIds.isEmpty()) {
			// Fetch missing field definitions from database
			Map<Integer, ExtraFieldDefinitionDto> extraFields = this.extraFieldsRepository
				.getExtraFieldDefinitions(missingColumnIds, EntityType.CANDIDATE, accountId);

			// Update cache with fetched field types
			extraFields.forEach((columnId, extraField) -> accountFieldTypes.put(columnId, extraField.extrafieldtype()));

			// Mark non-existent fields as "text" (default)
			missingColumnIds.forEach((columnId) -> accountFieldTypes.putIfAbsent(columnId, "text"));
		}

		// Return only the requested field types
		return columnIds.stream().collect(Collectors.toMap((columnId) -> columnId, accountFieldTypes::get));
	}

	/**
	 * Convert a raw custom column value based on its field type.
	 * @param rawValue The raw value from the database
	 * @param fieldType The field type from Tblextrafields.extrafieldtype
	 * @return Converted value as String for display
	 */
	public String convertValue(Object rawValue, String fieldType) {
		if (rawValue == null) {
			return "";
		}

		String stringValue = rawValue.toString().trim();
		if (stringValue.isEmpty()) {
			return "";
		}

		return switch (fieldType.toLowerCase(Locale.ROOT)) {
			case "date" -> convertDateValue(stringValue);
			case "date_time", "datetime", "timestamp" -> convertDateTimeValue(stringValue);
			case "checkbox" -> convertCheckbox(stringValue);
			default -> stringValue; // Default: return as-is
		};
	}

	/**
	 * Convert Unix timestamp to MM/dd/yyyy UTC format.
	 */
	private String convertDateValue(String value) {
		try {
			// Try parsing as Unix timestamp (seconds)
			long timestamp = Long.parseLong(value);
			Instant instant = Instant.ofEpochSecond(timestamp);
			return DATE_FORMATTER.format(instant);
		}
		catch (NumberFormatException ex) {
			// If not a valid timestamp, return as-is
			return value;
		}
	}

	/**
	 * Convert Unix timestamp to MM/dd/yyyy HH:mm:ss UTC format (date and time).
	 */
	private String convertDateTimeValue(String value) {
		try {
			// Try parsing as Unix timestamp (seconds)
			long timestamp = Long.parseLong(value);
			Instant instant = Instant.ofEpochSecond(timestamp);
			return DATE_TIME_FORMATTER.format(instant);
		}
		catch (NumberFormatException ex) {
			// If not a valid timestamp, return as-is
			return value;
		}
	}

	/**
	 * Convert checkbox values to boolean string representation. Returns "true" for 1 and
	 * "false" for 0.
	 */
	private String convertCheckbox(String value) {
		return switch (value.toLowerCase(Locale.ROOT).trim()) {
			case "1" -> "true";
			case "0" -> "false";
			default -> value; // Return as-is if not 0 or 1
		};
	}

}
