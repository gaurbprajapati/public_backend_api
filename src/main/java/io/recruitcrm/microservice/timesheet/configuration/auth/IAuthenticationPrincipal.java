/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth;

public interface IAuthenticationPrincipal<T, U> {

	T getUniqueIdentifier();

	String getUserName();

	T getRoleIdentifier();

	String getRoleIdentifierLabel();

	T getOrganizationIdentifier();

	U getUser();

	void setUser(U user);

}
