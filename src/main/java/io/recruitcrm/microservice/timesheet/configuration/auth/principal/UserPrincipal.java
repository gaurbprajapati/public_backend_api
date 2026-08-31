/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.principal;

import io.recruitcrm.entity.model.User;

/**
 * Principal implementation for RCRM system users. These are internal users with
 * role-based access control.
 */
public class UserPrincipal implements AuthPrincipal {

	private final User user;

	public UserPrincipal(User user) {
		if (user == null) {
			throw new IllegalArgumentException("User cannot be null");
		}
		this.user = user;
	}

	@Override
	public PrincipalType getPrincipalType() {
		return PrincipalType.USER;
	}

	@Override
	public Integer getUniqueIdentifier() {
		return this.user.getId();
	}

	@Override
	public Integer getOrganizationIdentifier() {
		return (this.user.getAccount() != null) ? this.user.getAccount().getId() : null;
	}

	@Override
	public String getEmail() {
		return this.user.getEmail();
	}

	@Override
	public String getDisplayName() {
		String username = this.user.getUsername();
		if (username != null && !username.isEmpty()) {
			return username;
		}
		// Construct name from firstname and lastname
		String firstName = (this.user.getFirstname() != null) ? this.user.getFirstname() : "";
		String lastName = (this.user.getLastname() != null) ? this.user.getLastname() : "";
		String fullName = (firstName + " " + lastName).trim();
		return (!fullName.isEmpty()) ? fullName : this.user.getEmail();
	}

	@Override
	public String getFullName() {
		String firstName = (this.user.getFirstname() != null) ? this.user.getFirstname() : "";
		String lastName = (this.user.getLastname() != null) ? this.user.getLastname() : "";
		String fullName = (firstName + " " + lastName).trim();
		return (!fullName.isEmpty()) ? fullName : this.user.getEmail();
	}

	@Override
	public Integer getRoleIdentifier() {
		return this.user.getRoleId();
	}

	/**
	 * Get the underlying User entity
	 * @return User entity
	 */
	public User getUser() {
		return this.user;
	}

	public String getUsername() {
		return this.user.getUsername();
	}

	public String getRoleLabel() {
		// Can be enhanced to return actual role label from role entity
		return "User";
	}

}
