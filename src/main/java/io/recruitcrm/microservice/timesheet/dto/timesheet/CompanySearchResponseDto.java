package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for company search results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanySearchResponseDto {

	private String title;

	private String slug;

	private Integer id;

	private Integer srno;

	private String photo;

	private String entitytype;

	private String address;

	private String city;

	private Integer haschildren;

	private String industryname;

	private String link;

	private String mlink;

	private Integer owner;

	private String website;

}
