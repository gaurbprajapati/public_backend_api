/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.principal;

import io.recruitcrm.entity.model.Candidate;

/**
 * Principal implementation for Contractors/Candidates. These are external users accessing
 * the system via VMS portal with limited permissions. They can only view/edit their own
 * timesheets and related data.
 */
public class ContractorPrincipal implements AuthPrincipal {

	private final Candidate candidate;

	/**
	 * Virtual role ID for contractors - used for permission checks This can be configured
	 * to match a specific role in your system
	 */
	private static final Integer CONTRACTOR_VIRTUAL_ROLE_ID = -1;

	public ContractorPrincipal(Candidate candidate) {
		if (candidate == null) {
			throw new IllegalArgumentException("Candidate cannot be null");
		}
		this.candidate = candidate;
	}

	@Override
	public PrincipalType getPrincipalType() {
		return PrincipalType.CONTRACTOR;
	}

	@Override
	public Integer getUniqueIdentifier() {
		return this.candidate.getId();
	}

	@Override
	public Integer getOrganizationIdentifier() {
		// Always use accountId field to avoid LazyInitializationException
		// when accessing lazy-loaded Account relationship
		return this.candidate.getAccountId();
	}

	@Override
	public String getEmail() {
		return this.candidate.getEmailId();
	}

	@Override
	public String getDisplayName() {
		String firstName = (this.candidate.getFirstName() != null) ? this.candidate.getFirstName() : "";
		String lastName = (this.candidate.getLastName() != null) ? this.candidate.getLastName() : "";
		return (firstName + " " + lastName).trim();
	}

	@Override
	public String getFullName() {
		return getDisplayName();
	}

	@Override
	public Integer getRoleIdentifier() {
		// Contractors don't have traditional roles, return virtual role ID
		return CONTRACTOR_VIRTUAL_ROLE_ID;
	}

	/**
	 * Get the underlying Candidate entity
	 * @return Candidate entity
	 */
	public Candidate getCandidate() {
		return this.candidate;
	}

	/**
	 * Get the candidate/contractor ID
	 * @return candidate ID
	 */
	public Integer getCandidateId() {
		return this.candidate.getId();
	}

	/**
	 * Get the contact number
	 * @return contact number
	 */
	public String getContactNumber() {
		return this.candidate.getContactNumber();
	}

}
