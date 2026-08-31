package io.recruitcrm.microservice.timesheet.search.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Parses filter values that represent lists of integers from comma-separated strings or
 * JSON arrays.
 */
public final class FilterValueParser {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private FilterValueParser() {
		// Utility class - prevent instantiation
	}

	/**
	 * Parses a filter value which can be either: 1. Comma-separated string: "1,2,3" or
	 * "1, 2, 3" 2. JSON array: [1, 2, 3] or ["1", "2", "3"]
	 * @param filterValue raw filter value from the request
	 * @return parsed integer IDs, or an empty list when the value is null, blank, or
	 * contains no valid integers
	 */
	public static List<Integer> parseIntegerList(String filterValue) {
		if (filterValue == null || filterValue.trim().isEmpty()) {
			return List.of();
		}

		// Try parsing as JSON array first
		try {
			JsonNode jsonNode = OBJECT_MAPPER.readTree(filterValue);
			if (jsonNode.isArray()) {
				List<Integer> result = new ArrayList<>();
				jsonNode.elements().forEachRemaining((node) -> {
					if (node.isInt()) {
						result.add(node.asInt());
					}
					else if (node.isTextual()) {
						try {
							result.add(Integer.parseInt(node.asText().trim()));
						}
						catch (NumberFormatException ex) {
							// Skip invalid numbers
						}
					}
				});
				return result;
			}
		}
		catch (Exception ex) {
			// Not JSON, try comma-separated string
		}

		// Parse as comma-separated string
		return Arrays.stream(filterValue.split(",")).map(String::trim).filter((s) -> !s.isEmpty()).map((s) -> {
			try {
				return Integer.parseInt(s);
			}
			catch (NumberFormatException ex) {
				return null;
			}
		}).filter(Objects::nonNull).toList();
	}

}
