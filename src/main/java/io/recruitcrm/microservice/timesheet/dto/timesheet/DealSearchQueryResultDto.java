package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query result DTO for deal search.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealSearchQueryResultDto {

	private Integer id;

	private String name;

	private String slug;

	private Integer srno;

	private Integer owner;

	private Integer dealstage;

	private String stagename;

}
