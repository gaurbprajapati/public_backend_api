/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers.auth;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import org.springframework.stereotype.Component;

/**
 * Helper class for validating entity access based on multi-persona authentication.
 *
 * This validator ensures that: - Contractors can only access their own data
 * (entityType=3, entityId must match contractor ID) - Contacts can only access their own
 * data (entityType=1, entityId must match contact ID) - Agency users can access any
 * entity based on their role permissions
 *
 * Usage: <pre>
 * {@code
 * &#64;Autowired
 * private EntityAccessValidator entityAccessValidator;
 *
 * public void someMethod(Integer entityType, Integer entityId) {
 *     entityAccessValidator.validateEntityAccess(entityType, entityId);
 *     // Continue with business logic...
 * }
 * }
 * </pre>
 */
@Component
public class EntityAccessValidator {

	private final AuthHolder authHolder;

	/**
	 * Entity type constants
	 */
	public static final Integer ENTITY_TYPE_CONTACT = 1;

	public static final Integer ENTITY_TYPE_CONTRACTOR = 3;

	public EntityAccessValidator(AuthHolder authHolder) {
		this.authHolder = authHolder;
	}

	/**
	 * Validate entity access based on multi-persona authentication.
	 *
	 * Rules: - Contractors can only access their own data (entityType=3, entityId must
	 * match contractor ID) - Contacts can only access their own data (entityType=1,
	 * entityId must match contact ID) - Agency users can access any entity based on their
	 * role permissions
	 * @param entityType Entity type (3 = contractor, 1 = contact)
	 * @param entityId Entity ID to access
	 * @throws ValidationErrorException if access is denied
	 */
	public void validateEntityAccess(Integer entityType, Integer entityId) {
		// Check if unified principal is available (Keycloak authentication)
		AuthPrincipal unifiedPrincipal = this.authHolder.getUnifiedPrincipal();

		if (unifiedPrincipal == null) {
			// Legacy authentication - allow agency users
			return;
		}

		// Agency users can access any entity based on their role permissions
		if (unifiedPrincipal.isSystemUser()) {
			return;
		}

		// Contractor accessing timesheet data
		if (unifiedPrincipal.isContractor()) {
			validateContractorAccess(entityType, entityId, unifiedPrincipal);
		}

		// Contact/Client accessing timesheet data
		if (unifiedPrincipal.isContact()) {
			validateContactAccess(entityType, entityId, unifiedPrincipal);
		}
	}

	/**
	 * Validate contractor access to entity data.
	 * @param entityType Entity type requested
	 * @param entityId Entity ID requested
	 * @param unifiedPrincipal Authenticated contractor principal
	 * @throws ValidationErrorException if access is denied
	 */
	private void validateContractorAccess(Integer entityType, Integer entityId, AuthPrincipal unifiedPrincipal) {
		// Must be requesting contractor data (entityType = 3)
		if (!ENTITY_TYPE_CONTRACTOR.equals(entityType)) {
			throw new ValidationErrorException("Contractors can only access contractor timesheet data (entityType="
					+ ENTITY_TYPE_CONTRACTOR + ")");
		}

		// Must be requesting their own data
		ContractorPrincipal contractorPrincipal = (ContractorPrincipal) unifiedPrincipal;
		Integer contractorId = contractorPrincipal.getCandidateId();

		if (!entityId.equals(contractorId)) {
			throw new ValidationErrorException(
					"Contractors can only access their own timesheet data. Requested entityId: " + entityId
							+ ", Authenticated contractor ID: " + contractorId);
		}
	}

	/**
	 * Validate contact/client access to entity data.
	 * @param entityType Entity type requested
	 * @param entityId Entity ID requested
	 * @param unifiedPrincipal Authenticated contact principal
	 * @throws ValidationErrorException if access is denied
	 */
	private void validateContactAccess(Integer entityType, Integer entityId, AuthPrincipal unifiedPrincipal) {
		// Must be requesting contact data (entityType = 1)
		if (!ENTITY_TYPE_CONTACT.equals(entityType)) {
			throw new ValidationErrorException(
					"Contacts can only access contact timesheet data (entityType=" + ENTITY_TYPE_CONTACT + ")");
		}

		// Must be requesting their own data
		ContactPrincipal contactPrincipal = (ContactPrincipal) unifiedPrincipal;
		Integer contactId = contactPrincipal.getContactId();

		if (!entityId.equals(contactId)) {
			throw new ValidationErrorException(
					"Contacts can only access their own company's timesheet data. Requested entityId: " + entityId
							+ ", Authenticated contact ID: " + contactId);
		}
	}

}
