/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.principal;

/**
 * Simple POJO to store Contact data in SecurityContext. This avoids
 * LazyInitializationException by not storing Hibernate entities.
 */
public class SimpleContactPrincipal {

	private final Integer id;

	private final String email;

	private final Integer accountId;

	private final Integer companyId;

	private final String firstName;

	private final String lastName;

	public SimpleContactPrincipal(Integer id, String email, Integer accountId, Integer companyId, String firstName,
			String lastName) {
		this.id = id;
		this.email = email;
		this.accountId = accountId;
		this.companyId = companyId;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public Integer getId() {
		return this.id;
	}

	public String getEmail() {
		return this.email;
	}

	public Integer getAccountId() {
		return this.accountId;
	}

	public Integer getCompanyId() {
		return this.companyId;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	@Override
	public String toString() {
		return "Contact(" + this.email + ")";
	}

}
