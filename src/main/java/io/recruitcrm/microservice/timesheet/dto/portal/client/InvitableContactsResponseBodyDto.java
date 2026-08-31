package io.recruitcrm.microservice.timesheet.dto.portal.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitableContactsResponseBodyDto {

	private Integer companyId;

	private boolean allActive;

	private List<InvitableContactResponseBodyDto> contacts;

}
