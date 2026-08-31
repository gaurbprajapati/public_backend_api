/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.repository;

import io.recruitcrm.entity.model.UserMfaSettings;

public interface IUserMfaSettingsRepository {

	UserMfaSettings getUserMfaSettings(Integer userId);

}
