package io.recruitcrm.microservice.timesheet.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsQueryResultDto {

	private String name;

	private String profilePic;

}
