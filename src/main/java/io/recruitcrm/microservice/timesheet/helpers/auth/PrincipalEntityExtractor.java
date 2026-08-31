/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers.auth;

import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import org.springframework.stereotype.Component;

/**
 * Helper class for extracting entity type and entity ID from authenticated principals.
 * Used for portal endpoints and timesheet operations that need to identify the entity
 * (contractor or contact) making the request.
 */
@Component
public class PrincipalEntityExtractor {

	/**
	 * Extract entity type from authenticated principal (derived from JWT token).
	 * @param principal Authenticated principal
	 * @return Entity type (3 = contractor, 1 = contact) or null if not applicable
	 */
	public Integer extractEntityTypeFromPrincipal(AuthPrincipal principal) {
		if (principal == null) {
			return null;
		}

		PrincipalType principalType = principal.getPrincipalType();
		if (principalType == PrincipalType.CONTRACTOR) {
			return EntityAccessValidator.ENTITY_TYPE_CONTRACTOR;
		}
		if (principalType == PrincipalType.CONTACT) {
			return EntityAccessValidator.ENTITY_TYPE_CONTACT;
		}

		// Agency users don't have entityType/entityId in token for portal endpoints
		return null;
	}

	/**
	 * Extract entity ID from authenticated principal (derived from JWT token).
	 * @param principal Authenticated principal
	 * @return Entity ID (contractor ID or contact ID) or null if not applicable
	 */
	public Integer extractEntityIdFromPrincipal(AuthPrincipal principal) {
		if (principal == null) {
			return null;
		}

		PrincipalType principalType = principal.getPrincipalType();
		if (principalType == PrincipalType.CONTRACTOR && principal instanceof ContractorPrincipal contractorPrincipal) {
			return contractorPrincipal.getCandidateId();
		}
		if (principalType == PrincipalType.CONTACT && principal instanceof ContactPrincipal contactPrincipal) {
			return contactPrincipal.getContactId();
		}

		// Agency users don't have entityType/entityId in token for portal endpoints
		return null;
	}

	/**
	 * Resolves the correct {@code cst_user_type_t} database ID from the authenticated
	 * principal type. Use this instead of {@code getRoleIdentifier()} when storing the
	 * user type in audit columns (addedByUserTypeId / updatedByUserTypeId), because
	 * {@code getRoleIdentifier()} returns the RCRM system role ID for agency users (e.g.
	 * 4), not the {@code cst_user_type_t} value (1=Client, 2=Agency, 3=Contractor).
	 * @param principalType the type of the currently authenticated principal
	 * @return the matching {@code cst_user_type_t} ID
	 */
	public Integer resolveUserTypeId(PrincipalType principalType) {
		return switch (principalType) {
			case USER -> UserTypeEnum.AGENCY_RECRUITER.getId();
			case CONTRACTOR -> UserTypeEnum.CONTRACTOR.getId();
			case CONTACT -> UserTypeEnum.COMPANY_CONTACT.getId();
		};
	}

}
