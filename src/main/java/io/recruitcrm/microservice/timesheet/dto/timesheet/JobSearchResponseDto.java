package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for job search results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchResponseDto {

	private String title;

	private String slug;

	private Integer id;

	private Integer srno;

	private String entitytype;

	private String companynameforjob;

	private String companyslug;

	private String location;

}
