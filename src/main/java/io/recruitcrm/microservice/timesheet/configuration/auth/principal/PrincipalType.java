/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.principal;

/**
 * Enum to differentiate between different types of authenticated principals.
 *
 * - USER: RCRM system users (authenticated via legacy JWT or Keycloak) - CONTRACTOR:
 * Contractors/Candidates accessing via VMS portal (Keycloak) - CONTACT: Contacts/Clients
 * accessing via VMS portal (Keycloak)
 */
public enum PrincipalType {

	/**
	 * RCRM system user with full access based on role permissions
	 */
	USER,

	/**
	 * Contractor/Candidate with limited access to their own data
	 */
	CONTRACTOR,

	/**
	 * Contact/Client with limited access based on company association
	 */
	CONTACT

}
