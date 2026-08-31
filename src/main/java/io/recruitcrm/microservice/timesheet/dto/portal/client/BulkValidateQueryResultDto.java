package io.recruitcrm.microservice.timesheet.dto.portal.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkValidateQueryResultDto {

	private String vmsUserEmail;

	private int portalStatusId;

	private long accountId;

	private long inviteCount;

	private long inviteSentOn;

}
