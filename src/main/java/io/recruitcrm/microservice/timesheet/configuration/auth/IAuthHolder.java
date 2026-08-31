/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth;

public interface IAuthHolder<T, U> {

	void setAuthenticationPrincipal(IAuthenticationPrincipal<T, U> authenticationPrincipal);

	IAuthenticationPrincipal<T, U> getAuthenticationPrincipal();

	T getAuthenticationPrincipalUniqueIdentifier();

	T getAuthenticationPrincipalRoleIdentifier();

	T getAuthenticationPrincipalOrganizationIdentifier();

}
