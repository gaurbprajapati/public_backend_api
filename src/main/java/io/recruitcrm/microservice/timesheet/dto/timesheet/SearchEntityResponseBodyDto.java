package io.recruitcrm.microservice.timesheet.dto.timesheet;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for search entity endpoint. Keys: "3" for Companies, "4" for Jobs, "11"
 * for Deals
 */
public record SearchEntityResponseBodyDto(Map<String, List<?>> data) {
	public SearchEntityResponseBodyDto {
		if (data == null) {
			data = Map.of();
		}
	}
}
