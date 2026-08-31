/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth;

public interface IAuthenticationPrincipalAuthorizationCheck<T> {

	boolean canAccess(Object accessObject, T entityIdentifier, T userIdentifier, T organizationIdentifier,
			AccessPrivilege privilege);

}
