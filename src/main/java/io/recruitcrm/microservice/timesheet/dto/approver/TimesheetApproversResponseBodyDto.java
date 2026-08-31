package io.recruitcrm.microservice.timesheet.dto.approver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO containing detailed approver information for timesheets. Includes separate
 * lists for agency approvers and client approvers with their full details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetApproversResponseBodyDto {

	/**
	 * List of agency approvers with their details (id, name, photo, userTypeId).
	 */
	private List<ApproverDetailResponseBodyDto> agencyApproverDetails;

	/**
	 * List of client approvers with their details (id, name, photo, userTypeId).
	 */
	private List<ApproverDetailResponseBodyDto> clientApproverDetails;

}
