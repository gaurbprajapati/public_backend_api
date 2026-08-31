package io.recruitcrm.microservice.timesheet.dto.contractor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractorJobQueryResultDto {

	private Integer contractorId;

	private Integer jobId;

	private String jobName;

	private String jobSlug;

	private String companyName;

	private String companySlug;

	private Integer jobStartDate;

	private Integer jobEndDate;

	private String jobStatus;

	private Integer jobSrno;

}
