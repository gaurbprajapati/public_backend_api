package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query result DTO for job search.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchQueryResultDto {

	private Integer id;

	private String name;

	private String slug;

	private Integer srno;

	private String companyname;

	private String companyslug;

	private String location;

}
