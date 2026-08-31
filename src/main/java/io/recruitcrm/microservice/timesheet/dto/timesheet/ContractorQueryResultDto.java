package io.recruitcrm.microservice.timesheet.dto.timesheet;

import io.recruitcrm.microservice.timesheet.dto.off_limit.OffLimitInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ContractorQueryResultDto extends OffLimitInfoDto {

	private Integer id;

	private String name;

	private String photo;

	private String slug;

	private String position;

	private String ownerId;

	private Integer assignmentId;

}