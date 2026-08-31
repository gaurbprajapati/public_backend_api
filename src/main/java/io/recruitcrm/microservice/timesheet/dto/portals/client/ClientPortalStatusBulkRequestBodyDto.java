/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.portals.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /v1/portal/client/portal-status/bulk}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPortalStatusBulkRequestBodyDto {

	/**
	 * Deprecated: account is resolved from the authenticated principal. Kept for
	 * backward-compatible request payloads.
	 */
	private Integer accountId;

	@NotNull(message = ClientPortalStatusBulkRequestBodyDto.RECRUITER_USER_ID_REQUIRED_MESSAGE)
	@Positive(message = ClientPortalStatusBulkRequestBodyDto.RECRUITER_USER_ID_REQUIRED_MESSAGE)
	private Integer recruiterUserId;

	private String recruiterName;

	private String agencyName;

	private String companyName;

	@NotNull(message = ClientPortalStatusBulkRequestBodyDto.COMPANY_ID_REQUIRED_MESSAGE)
	@Positive(message = ClientPortalStatusBulkRequestBodyDto.COMPANY_ID_REQUIRED_MESSAGE)
	private Integer companyId;

	@NotNull(message = ClientPortalStatusBulkRequestBodyDto.CONTACTS_REQUIRED_MESSAGE)
	@NotEmpty(message = ClientPortalStatusBulkRequestBodyDto.CONTACTS_REQUIRED_MESSAGE)
	@Valid
	private List<ClientPortalStatusBulkContactDto> contacts;

	public static final String ACCOUNT_ID_REQUIRED_MESSAGE = "account_id is required";

	public static final String CONTACTS_REQUIRED_MESSAGE = "contacts must be a non-empty list";

	public static final String RECRUITER_USER_ID_REQUIRED_MESSAGE = "recruiterUserId is required";

	public static final String COMPANY_ID_REQUIRED_MESSAGE = "companyId is required";

}
