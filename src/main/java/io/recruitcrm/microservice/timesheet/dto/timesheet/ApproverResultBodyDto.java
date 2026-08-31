package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproverResultBodyDto {

	private Integer id;

	private String name;

	private String photo;

	private Integer userTypeId;

}