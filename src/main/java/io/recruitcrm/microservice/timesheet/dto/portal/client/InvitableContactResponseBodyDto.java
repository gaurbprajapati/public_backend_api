package io.recruitcrm.microservice.timesheet.dto.portal.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitableContactResponseBodyDto {

	private Integer id;

	private String firstName;

	private String lastName;

	private String email;

	private Integer portalStatusId;

	private String photo;

	private Integer srno;

	private String companyName;

	private Boolean canView;

	private Boolean canEdit;

	private Boolean canDelete;

}
