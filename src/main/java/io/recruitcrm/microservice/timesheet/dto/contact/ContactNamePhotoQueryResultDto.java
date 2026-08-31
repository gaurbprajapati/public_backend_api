package io.recruitcrm.microservice.timesheet.dto.contact;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactNamePhotoQueryResultDto {

	private String name;

	private String profilePic;

	private String email;

}
