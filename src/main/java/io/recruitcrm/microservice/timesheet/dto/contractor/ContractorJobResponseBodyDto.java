package io.recruitcrm.microservice.timesheet.dto.contractor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractorJobResponseBodyDto {

	private Integer jobId;

	private String jobName;

	private String companyName;

}
