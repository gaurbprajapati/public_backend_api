package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for deal search results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealSearchResponseDto {

	private String title;

	private String slug;

	private Integer id;

	private Integer srno;

	private String entitytype;

	private Integer owner;

	private String stagename;

}
