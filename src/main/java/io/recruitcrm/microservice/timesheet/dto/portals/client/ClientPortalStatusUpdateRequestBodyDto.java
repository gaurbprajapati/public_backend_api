/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.portals.client;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /v1/portal/client/portal-status}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPortalStatusUpdateRequestBodyDto {

	@NotBlank(message = ClientPortalStatusUpdateRequestBodyDto.ACTION_VALIDATION_MESSAGE)
	private String action;

	@NotBlank(message = ClientPortalStatusUpdateRequestBodyDto.EMAIL_REQUIRED_MESSAGE)
	private String email;

	private String firstName;

	private String lastName;

	private Integer recruiterUserId;

	private String recruiterName;

	private String agencyName;

	private String companyName;

	private Integer companyId;

	private Integer rcrmContactId;

	public static final String ACTION_VALIDATION_MESSAGE = "action must be one of: send_invite, resend_invite, disable, re_enable";

	public static final String EMAIL_REQUIRED_MESSAGE = "email is required";

}
