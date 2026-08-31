package io.recruitcrm.microservice.timesheet.dto.contractor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractorDealQueryResultDto {

	private Integer contractorId;

	private Integer dealId;

	private String dealName;

	private String ownerName;

	private Integer serialNumber;

	private String slug;

	private String status;

}
