/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.service;

import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.dto.UserMfaSettingsResponseBodyDto;

public interface IUserMfaSettingsService {

	UserMfaSettingsResponseBodyDto getUserMfaSettings(Integer userId);

}
