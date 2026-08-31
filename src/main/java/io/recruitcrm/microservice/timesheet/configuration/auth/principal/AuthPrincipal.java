/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.principal;

/**
 * Unified interface for all authentication principals in the system. This interface
 * abstracts the differences between different user types (RCRM Users, Contractors,
 * Contacts) and provides a common API for accessing their authentication information.
 *
 * All concrete implementations must provide: - Principal type identification - Unique
 * identifier (user ID, contractor ID, contact ID) - Organization/Account association -
 * Email and display name for logging/audit
 */
public interface AuthPrincipal {

	/**
	 * Get the type of this principal (USER, CONTRACTOR, or CONTACT)
	 * @return PrincipalType enum value
	 */
	PrincipalType getPrincipalType();

	/**
	 * Get the unique identifier for this principal For USER: user.id For CONTRACTOR:
	 * candidate.id For CONTACT: contact.id
	 * @return unique identifier as Integer
	 */
	Integer getUniqueIdentifier();

	/**
	 * Get the organization/account identifier this principal belongs to
	 * @return account ID as Integer
	 */
	Integer getOrganizationIdentifier();

	/**
	 * Get the email address of this principal (for logging and audit)
	 * @return email address as String
	 */
	String getEmail();

	/**
	 * Get the display name of this principal (for logging and UI)
	 * @return display name as String
	 */
	String getDisplayName();

	/**
	 * Get the full name of this principal (for logging and UI)
	 * @return full name as String
	 */
	String getFullName();

	/**
	 * Get the role identifier for this principal For USER: actual role ID For
	 * CONTRACTOR/CONTACT: virtual role ID or 0
	 * @return role ID as Integer
	 */
	Integer getRoleIdentifier();

	/**
	 * Check if this principal is a system user (RCRM user)
	 * @return true if USER type, false otherwise
	 */
	default boolean isSystemUser() {
		return getPrincipalType() == PrincipalType.USER;
	}

	/**
	 * Check if this principal is a contractor
	 * @return true if CONTRACTOR type, false otherwise
	 */
	default boolean isContractor() {
		return getPrincipalType() == PrincipalType.CONTRACTOR;
	}

	/**
	 * Check if this principal is a contact
	 * @return true if CONTACT type, false otherwise
	 */
	default boolean isContact() {
		return getPrincipalType() == PrincipalType.CONTACT;
	}

}
