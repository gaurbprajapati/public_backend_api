package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query result DTO for company search.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanySearchQueryResultDto {

	private Integer id;

	private String name;

	private String slug;

	private Integer srno;

	private String address;

	private String city;

	private Integer haschildren;

	private Integer industryid;

	private Integer ownerid;

	private String logo;

	private String website;

}
