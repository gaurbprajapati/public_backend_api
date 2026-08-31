/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserMfaSettingsResponseBodyDto {

	private Integer userId;

	private Integer accountId;

	private Boolean webMfaLogin;

	private Boolean mobileMfaLogin;

	private String secretKey;

	private String mfaEnforceBy;

	private Integer mfaEnforcedDate;

}
