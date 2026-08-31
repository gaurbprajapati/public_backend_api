package io.recruitcrm.microservice.timesheet.dto.portal.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactValidationResultDto {

	private Integer contactId;

	private String email;

	private boolean valid;

	private String reason;

}
