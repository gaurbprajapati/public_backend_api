/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.access_control.UnknownAccessLevelException;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * Checks access control permissions for entities in the system. This class is responsible
 * for validating if a given permission level is allowed for a specific entity and
 * permission type.
 */
@Component(AccessControlChecker.BEAN_NAME)
public class AccessControlChecker {

	public static final String BEAN_NAME = "recruitcrmAccessControlChecker";

	private final AccessControlConfigHolder accessControlConfigHolder;

	private final AccessLevelHandler accessLevelHandler;

	private final AuthHolder authHolder;

	private final PermissionValidator permissionValidator;

	public AccessControlChecker(AccessControlConfigHolder accessControlConfigHolder,
			AccessLevelHandler accessLevelHandler, EntityManager entityManager, AuthHolder authHolder) {
		this.accessControlConfigHolder = accessControlConfigHolder;
		this.accessLevelHandler = accessLevelHandler;
		this.authHolder = authHolder;
		TeamMembershipChecker teamMembershipChecker = new TeamMembershipChecker(entityManager);
		this.permissionValidator = new PermissionValidator(teamMembershipChecker);
	}

	/**
	 * Simplified method to check if the given permission is allowed for the specified
	 * entity. This method is useful for common permission checks where the requested
	 * level is not needed. For global permissions (Entity.GLOBAL), the permission must be
	 * ALLOWED and a GlobalPermission must be specified.
	 * @param entity The entity to check access for
	 * @param permission The type of permission being checked
	 * @param ownerId The ID of the user who owns the resource (null if not applicable)
	 * @param globalPermission The global permission to check (required only for
	 * Entity.GLOBAL)
	 * @throws UnauthorizedAccessException if the permission is denied
	 * @throws UnknownAccessLevelException if the entity or permission is not supported
	 * @throws IllegalArgumentException if ownerId is provided for global permissions or
	 * if globalPermission is missing for Entity.GLOBAL
	 */
	public void allows(Entity entity, Permission permission, Integer ownerId, GlobalPermission globalPermission) {
		// For global permissions, validate inputs
		if (entity == Entity.GLOBAL) {
			if (ownerId != null) {
				throw new IllegalArgumentException("Global permissions do not support owner ID");
			}
			if (permission != Permission.ALLOWED) {
				throw new IllegalArgumentException("Global permissions only support ALLOWED permission type");
			}
			if (globalPermission == null) {
				throw new IllegalArgumentException("Global permission must be specified for Entity.GLOBAL");
			}
			// Use the global permission check
			allows(globalPermission, PermissionLevel.YES);
			return;
		}

		// For entity permissions, globalPermission should be null
		if (globalPermission != null) {
			throw new IllegalArgumentException("Global permission should not be specified for non-global entities");
		}

		// For entity permissions, use the actual permission level from configuration
		PermissionLevel actualLevel = getActualPermissionLevel(entity, permission, null);
		Integer currentUserId = this.authHolder.getAuthenticationPrincipalUniqueIdentifier();

		if (!this.permissionValidator.isAllowed(actualLevel, permission, ownerId, currentUserId)) {
			throw new UnauthorizedAccessException();
		}
	}

	/**
	 * Simplified method to check if the given permission is allowed for the specified
	 * entity. This method is useful for common permission checks where the requested
	 * level is not needed. For entity permissions only (not for Entity.GLOBAL).
	 * @param entity The entity to check access for (must not be Entity.GLOBAL)
	 * @param permission The type of permission being checked
	 * @param ownerId The ID of the user who owns the resource (null if not applicable)
	 * @throws UnauthorizedAccessException if the permission is denied
	 * @throws UnknownAccessLevelException if the entity or permission is not supported
	 * @throws IllegalArgumentException if entity is Entity.GLOBAL
	 */
	public void allows(Entity entity, Permission permission, Integer ownerId) {
		if (entity == Entity.GLOBAL) {
			throw new IllegalArgumentException(
					"For global permissions, use allows(Entity.GLOBAL, Permission.ALLOWED, null, GlobalPermission)");
		}
		allows(entity, permission, ownerId, null);
	}

	/**
	 * Checks if the given permission level is allowed for the specified entity and
	 * permission. Throws UnauthorizedAccessException if the permission is denied.
	 * @param entity The entity to check access for
	 * @param permission The type of permission being checked
	 * @param requestedLevel The permission level being requested
	 * @param ownerId The ID of the user who owns the resource (null if not applicable)
	 * @param globalPermission The global permission to check (required only for
	 * Entity.GLOBAL)
	 * @throws UnauthorizedAccessException if the permission is denied
	 * @throws UnknownAccessLevelException if the entity or permission is not supported
	 */
	public void allows(Entity entity, Permission permission, PermissionLevel requestedLevel, Integer ownerId,
			GlobalPermission globalPermission) {
		// For global permissions, validate inputs
		if (entity == Entity.GLOBAL) {
			if (ownerId != null) {
				throw new IllegalArgumentException("Global permissions do not support owner ID");
			}
			if (permission != Permission.ALLOWED) {
				throw new IllegalArgumentException("Global permissions only support ALLOWED permission type");
			}
			if (globalPermission == null) {
				throw new IllegalArgumentException("Global permission must be specified for Entity.GLOBAL");
			}
			// Use the global permission check
			allows(globalPermission, requestedLevel);
			return;
		}

		// For entity permissions, globalPermission should be null
		if (globalPermission != null) {
			throw new IllegalArgumentException("Global permission should not be specified for non-global entities");
		}

		PermissionLevel actualLevel = getActualPermissionLevel(entity, permission, null);
		Integer currentUserId = this.authHolder.getAuthenticationPrincipalUniqueIdentifier();

		if (!this.permissionValidator.isAllowed(actualLevel, permission, ownerId, currentUserId)) {
			throw new UnauthorizedAccessException();
		}
	}

	/**
	 * Checks if the given permission level is allowed for the specified entity and
	 * permission. Throws UnauthorizedAccessException if the permission is denied. For
	 * entity permissions only (not for Entity.GLOBAL).
	 * @param entity The entity to check access for (must not be Entity.GLOBAL)
	 * @param permission The type of permission being checked
	 * @param requestedLevel The permission level being requested
	 * @param ownerId The ID of the user who owns the resource (null if not applicable)
	 * @throws UnauthorizedAccessException if the permission is denied
	 * @throws UnknownAccessLevelException if the entity or permission is not supported
	 * @throws IllegalArgumentException if entity is Entity.GLOBAL
	 */
	public void allows(Entity entity, Permission permission, PermissionLevel requestedLevel, Integer ownerId) {
		if (entity == Entity.GLOBAL) {
			throw new IllegalArgumentException(
					"For global permissions, use allows(Entity.GLOBAL, Permission.ALLOWED, requestedLevel, null, GlobalPermission)");
		}
		allows(entity, permission, requestedLevel, ownerId, null);
	}

	/**
	 * Checks if the given global permission is allowed. This is a convenience method
	 * specifically for checking global permissions.
	 * @param globalPermission The type of global permission to check
	 * @param requestedLevel The permission level being requested (should be YES or NO)
	 * @throws UnauthorizedAccessException if the permission is denied
	 * @throws UnknownAccessLevelException if the global permission is not supported
	 * @throws IllegalArgumentException if requestedLevel is not YES or NO
	 */
	public void allows(GlobalPermission globalPermission, PermissionLevel requestedLevel) {
		if (requestedLevel != PermissionLevel.YES && requestedLevel != PermissionLevel.NO) {
			throw new IllegalArgumentException("Global permissions only support YES or NO permission levels");
		}

		PermissionLevel actualLevel = getActualPermissionLevel(Entity.GLOBAL, Permission.ALLOWED, globalPermission);
		Integer currentUserId = this.authHolder.getAuthenticationPrincipalUniqueIdentifier();

		if (!this.permissionValidator.isAllowed(actualLevel, Permission.ALLOWED, null, currentUserId)) {
			throw new UnauthorizedAccessException();
		}
	}

	/**
	 * Gets the actual permission level from the access control configuration. This will
	 * also validate the permission level through AccessLevelHandler.
	 */
	private PermissionLevel getActualPermissionLevel(Entity entity, Permission permission,
			GlobalPermission globalPermission) {
		return this.accessLevelHandler.getAccessLevel(entity, this.accessControlConfigHolder.getAccessControlDto(),
				permission, globalPermission);
	}

}
