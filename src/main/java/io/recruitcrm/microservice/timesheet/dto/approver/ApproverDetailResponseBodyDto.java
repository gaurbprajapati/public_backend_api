package io.recruitcrm.microservice.timesheet.dto.approver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for approver details including ID, name, photo, and user type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproverDetailResponseBodyDto {

	/**
	 * Entity ID of the approver (agency user ID or contact ID).
	 */
	private Integer id;

	/**
	 * Name of the approver.
	 */
	private String name;

	/**
	 * Profile photo URL of the approver.
	 */
	private String photo;

	/**
	 * User type ID indicating whether the approver is an agency user or contact.
	 */
	private Integer userTypeId;

	/**
	 * Email address of the approver. Populated only for client (contact) approvers.
	 */
	private String email;

}
