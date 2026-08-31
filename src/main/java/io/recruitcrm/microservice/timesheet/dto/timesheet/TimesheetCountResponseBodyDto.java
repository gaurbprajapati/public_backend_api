package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for timesheet count endpoint. Contains both total count and filtered count
 * of timesheets.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetCountResponseBodyDto {

	/**
	 * Total count of timesheets without any filters (only entity type and entity ID
	 * filtering applied).
	 */
	private Long totalCount;

	/**
	 * Count of timesheets with filters applied. If no filters are applied in the request,
	 * this will be equal to totalCount.
	 */
	private Long filteredCount;

}
