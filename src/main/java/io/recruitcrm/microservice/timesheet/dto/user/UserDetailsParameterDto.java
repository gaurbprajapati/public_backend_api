package io.recruitcrm.microservice.timesheet.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsParameterDto {

	private Integer userId;

	private Integer accountId;

	private Integer userTypeId;

}
