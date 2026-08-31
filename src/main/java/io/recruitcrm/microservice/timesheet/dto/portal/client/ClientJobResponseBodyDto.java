package io.recruitcrm.microservice.timesheet.dto.portal.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientJobResponseBodyDto {

	private Integer jobId;

	private String jobTitle;

	private String jobType;

	private String companyName;

	private String contactEmail;

	private String contactRole;

	private Boolean createAccess;

}
