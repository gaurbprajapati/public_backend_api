package io.recruitcrm.microservice.timesheet.dto.contractor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractorUpdatedByResponseBodyDto {

	private Integer id;

	private String name;

}
