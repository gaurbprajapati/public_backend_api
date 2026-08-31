/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth;

import io.recruitcrm.entity.model.User;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Enhanced AuthHolder to support multi-persona authentication.
 *
 * This component maintains authentication state for the current request and supports
 * both: 1. Legacy IAuthenticationPrincipal (for backward compatibility) 2. New
 * AuthPrincipal (for multi-persona support)
 *
 * The unified AuthPrincipal interface allows handling different user types (RCRM Users,
 * Contractors, Contacts) with a common API.
 */
@RequestScope
@Component(AuthHolder.BEAN_NAME)
public class AuthHolder implements IAuthHolder<Integer, User> {

	public static final String BEAN_NAME = "recruitcrmAuthHolder";

	// Legacy authentication principal (for backward compatibility)
	private IAuthenticationPrincipal<Integer, User> authenticationPrincipal = null;

	// New unified principal (for multi-persona support)
	private AuthPrincipal unifiedPrincipal = null;

	/* ========== Legacy Methods (Backward Compatibility) ========== */

	@Override
	public void setAuthenticationPrincipal(IAuthenticationPrincipal<Integer, User> authenticationPrincipal) {
		if (authenticationPrincipal.getUniqueIdentifier() == null
				|| authenticationPrincipal.getOrganizationIdentifier() == null) {
			return;
		}
		this.authenticationPrincipal = authenticationPrincipal;
	}

	@Override
	public IAuthenticationPrincipal<Integer, User> getAuthenticationPrincipal() {
		if (this.authenticationPrincipal == null) {
			throw new ResourceNotFoundException("Authentication principal not found.");
		}
		return this.authenticationPrincipal;
	}

	@Override
	public Integer getAuthenticationPrincipalUniqueIdentifier() {
		// Try unified principal first, then fall back to legacy
		if (this.unifiedPrincipal != null) {
			return this.unifiedPrincipal.getUniqueIdentifier();
		}
		if (this.authenticationPrincipal == null) {
			throw new ResourceNotFoundException("Authentication principal not found.");
		}
		return this.authenticationPrincipal.getUniqueIdentifier();
	}

	@Override
	public Integer getAuthenticationPrincipalRoleIdentifier() {
		// Try unified principal first, then fall back to legacy
		if (this.unifiedPrincipal != null) {
			return this.unifiedPrincipal.getRoleIdentifier();
		}
		if (this.authenticationPrincipal == null) {
			throw new ResourceNotFoundException("Authentication principal not found.");
		}
		return this.authenticationPrincipal.getRoleIdentifier();
	}

	@Override
	public Integer getAuthenticationPrincipalOrganizationIdentifier() {
		// Try unified principal first, then fall back to legacy
		if (this.unifiedPrincipal != null) {
			return this.unifiedPrincipal.getOrganizationIdentifier();
		}
		if (this.authenticationPrincipal == null) {
			throw new ResourceNotFoundException("Authentication principal not found.");
		}
		return this.authenticationPrincipal.getOrganizationIdentifier();
	}

	/* ========== New Unified Principal Methods ========== */

	/**
	 * Set the unified authentication principal (AuthPrincipal)
	 * @param principal AuthPrincipal instance (UserPrincipal, ContractorPrincipal, or
	 * ContactPrincipal)
	 */
	public void setUnifiedPrincipal(AuthPrincipal principal) {
		if (principal == null) {
			throw new IllegalArgumentException("Principal cannot be null");
		}
		if (principal.getUniqueIdentifier() == null || principal.getOrganizationIdentifier() == null) {
			throw new IllegalArgumentException("Principal must have valid unique and organization identifiers");
		}
		this.unifiedPrincipal = principal;
	}

	/**
	 * Get the unified authentication principal. Falls back to converting legacy principal
	 * if unified principal is not set.
	 * @return AuthPrincipal instance
	 * @throws ResourceNotFoundException if no principal is set
	 */
	public AuthPrincipal getUnifiedPrincipal() {
		// Check unified principal first (new system)
		if (this.unifiedPrincipal != null) {
			return this.unifiedPrincipal;
		}

		// FALLBACK: Convert legacy principal to unified principal for backward
		// compatibility
		if (this.authenticationPrincipal != null) {
			User user = this.authenticationPrincipal.getUser();
			return new UserPrincipal(user);
		}

		// No principal set at all - error
		throw new ResourceNotFoundException("Unified authentication principal not found.");
	}

	/**
	 * Get the type of the current principal (USER, CONTRACTOR, or CONTACT)
	 * @return PrincipalType enum value
	 * @throws ResourceNotFoundException if principal not set
	 */
	public PrincipalType getPrincipalType() {
		if (this.unifiedPrincipal == null) {
			// If legacy principal is set, assume USER type
			if (this.authenticationPrincipal != null) {
				return PrincipalType.USER;
			}
			throw new ResourceNotFoundException("Authentication principal not found.");
		}
		return this.unifiedPrincipal.getPrincipalType();
	}

	/**
	 * Check if a unified principal is set
	 * @return true if unified principal is set, false otherwise
	 */
	public boolean hasUnifiedPrincipal() {
		return this.unifiedPrincipal != null;
	}

	/**
	 * Check if any principal (legacy or unified) is set
	 * @return true if any principal is set, false otherwise
	 */
	public boolean hasPrincipal() {
		return this.authenticationPrincipal != null || this.unifiedPrincipal != null;
	}

	/**
	 * Check if the current principal is a system user (RCRM user with User entity)
	 * Contractors and contacts are NOT system users.
	 * @return true if USER persona, false otherwise
	 */
	public boolean isSystemUserPrincipal() {
		if (this.unifiedPrincipal != null) {
			return this.unifiedPrincipal.isSystemUser();
		}
		// If only legacy principal is set, it's a system user
		return this.authenticationPrincipal != null;
	}

	/**
	 * Clear all authentication state (for testing/cleanup)
	 */
	public void clear() {
		this.authenticationPrincipal = null;
		this.unifiedPrincipal = null;
	}

}
