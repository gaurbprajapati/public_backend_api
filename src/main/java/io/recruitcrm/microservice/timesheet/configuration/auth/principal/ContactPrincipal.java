/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.principal;

import io.recruitcrm.entity.model.Contact;

/**
 * Principal implementation for Contacts/Clients. These are external users (typically
 * hiring managers or company representatives) accessing the system via VMS portal with
 * limited permissions. They can view timesheets and data related to their
 * company/organization.
 */
public class ContactPrincipal implements AuthPrincipal {

	private final Contact contact;

	/**
	 * Email from the Keycloak JWT. When set, this takes precedence over the email stored
	 * on the Contact entity in RCRM — ensuring that if the client updated their email in
	 * the portal (Keycloak), the new email is used for all downstream lookups (e.g.
	 * timesheet sharing) rather than the stale RCRM-stored email.
	 */
	private final String jwtEmail;

	/**
	 * Virtual role ID for contacts - used for permission checks This can be configured to
	 * match a specific role in your system
	 */
	private static final Integer CONTACT_VIRTUAL_ROLE_ID = -2;

	public ContactPrincipal(Contact contact, String jwtEmail) {
		if (contact == null) {
			throw new IllegalArgumentException("Contact cannot be null");
		}
		this.contact = contact;
		this.jwtEmail = jwtEmail;
	}

	public ContactPrincipal(Contact contact) {
		this(contact, null);
	}

	@Override
	public PrincipalType getPrincipalType() {
		return PrincipalType.CONTACT;
	}

	@Override
	public Integer getUniqueIdentifier() {
		return this.contact.getId();
	}

	@Override
	public Integer getOrganizationIdentifier() {
		// Always use accountId field to avoid LazyInitializationException
		// when accessing lazy-loaded Account relationship
		return this.contact.getAccountId();
	}

	@Override
	public String getEmail() {
		return (this.jwtEmail != null && !this.jwtEmail.isEmpty()) ? this.jwtEmail : this.contact.getEmail();
	}

	@Override
	public String getDisplayName() {
		String firstName = (this.contact.getFirstName() != null) ? this.contact.getFirstName() : "";
		String lastName = (this.contact.getLastName() != null) ? this.contact.getLastName() : "";
		return (firstName + " " + lastName).trim();
	}

	@Override
	public String getFullName() {
		return getDisplayName();
	}

	@Override
	public Integer getRoleIdentifier() {
		// Contacts don't have traditional roles, return virtual role ID
		return CONTACT_VIRTUAL_ROLE_ID;
	}

	/**
	 * Get the underlying Contact entity
	 * @return Contact entity
	 */
	public Contact getContact() {
		return this.contact;
	}

	/**
	 * Get the contact ID
	 * @return contact ID
	 */
	public Integer getContactId() {
		return this.contact.getId();
	}

	/**
	 * Get the company ID this contact is associated with
	 * @return company ID
	 */
	public Integer getCompanyId() {
		return this.contact.getCompanyId();
	}

	/**
	 * Get the contact number
	 * @return contact number
	 */
	public String getContactNumber() {
		return this.contact.getContactNumber();
	}

	/**
	 * Get the designation/title of this contact
	 * @return designation
	 */
	public String getDesignation() {
		return this.contact.getDesignation();
	}

}
