package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobResultBodyDto {

	private Integer id;

	private String name;

	private String slug;

	private Boolean isUnassigned;

	private String companyName;

	private String companySlug;

	private String status;

	private String jobType;

	private Integer srno;

}