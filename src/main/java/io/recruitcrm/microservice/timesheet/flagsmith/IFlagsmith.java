/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.flagsmith;

import io.recruitcrm.entity.model.User;

public interface IFlagsmith {

	FlagsmithFlags getIdentityFlags(Integer userId, Integer accountId);

	FlagsmithFlags getIdentityFlags(User user);

	Boolean isFeatureEnabled(String featureName, User user);

	Object getFeatureValue(String featureName, User user);

}
