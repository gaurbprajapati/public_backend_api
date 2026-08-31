package io.recruitcrm.microservice.timesheet.services.export;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility service for converting work days JSON data to readable format. Handles
 * conversion of work day IDs to day names.
 */
@Component
public class WorkDaysConverter {

	private final ObjectMapper objectMapper;

	public WorkDaysConverter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Convert work days JSON to comma-separated day names.
	 * @param workDaysJson JSON string containing work day objects
	 * @return Comma-separated day names (e.g., "Monday, Tuesday, Wednesday")
	 */
	public String convertWorkDaysToNames(String workDaysJson) {
		if (workDaysJson == null || workDaysJson.trim().isEmpty()) {
			return "";
		}

		try {
			// Parse JSON array of work day objects
			List<Map<String, Object>> workDays = this.objectMapper.readValue(workDaysJson,
					new TypeReference<List<Map<String, Object>>>() {
					});

			// Extract workDayId values and convert to day names
			return workDays.stream()
				.map((workDay) -> workDay.get("workDayId"))
				.filter(Objects::nonNull)
				.map(this::convertDayIdToName)
				.filter((dayName) -> !dayName.isEmpty())
				.collect(Collectors.joining(", "));

		}
		catch (Exception ex) {
			// If JSON parsing fails, return empty string
			return "";
		}
	}

	/**
	 * Convert day ID to day name.
	 * @param dayId Day ID (can be Integer or String)
	 * @return Day name
	 */
	private String convertDayIdToName(Object dayId) {
		if (dayId == null) {
			return "";
		}

		// Handle both Integer and String day IDs
		String dayIdStr = dayId.toString();

		switch (dayIdStr) {
			case "1":
				return "Monday";
			case "2":
				return "Tuesday";
			case "3":
				return "Wednesday";
			case "4":
				return "Thursday";
			case "5":
				return "Friday";
			case "6":
				return "Saturday";
			case "7":
				return "Sunday";
			default:
				return "";
		}
	}

}
