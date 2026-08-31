/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.AccessControlDto;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.GlobalPermissions;
import io.recruitcrm.microservice.timesheet.exceptions.access_control.InvalidPermissionLevelException;
import io.recruitcrm.microservice.timesheet.exceptions.access_control.UnknownAccessLevelException;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles access level logic for different entities in the system. This class is
 * responsible for determining access levels based on entity type and action.
 */
@Component(AccessLevelHandler.BEAN_NAME)
public class AccessLevelHandler {

	public static final String BEAN_NAME = "recruitcrmAccessLevelHandler";

	private final Map<Entity, Function<AccessControlDto, ?>> entityAccessLevelGetters;

	private final Map<GlobalPermission, Function<GlobalPermissions, PermissionLevel>> globalPermissionGetters;

	public AccessLevelHandler(AccessLevelInitializer initializer) {
		this.entityAccessLevelGetters = initializer.initializeEntityAccessLevelGetters();
		this.globalPermissionGetters = initializer.initializeGlobalPermissionGetters();
	}

	/**
	 * Gets the access level for a given entity and action.
	 * @param entity The entity to check access for
	 * @param accessControlDto The access control data transfer object
	 * @param permission The action to check access for
	 * @param globalPermission The global permission type to check access for
	 * @return The permission level for the entity and action
	 * @throws UnknownAccessLevelException if the entity or action is not supported
	 * @throws InvalidPermissionLevelException if the permission level is invalid for the
	 * given permission
	 */
	public PermissionLevel getAccessLevel(Entity entity, AccessControlDto accessControlDto, Permission permission,
			GlobalPermission globalPermission) {
		validateGlobalPermission(entity, permission);
		validateEntity(entity);

		Object accessLevel = getAccessLevelObject(entity, accessControlDto);
		PermissionLevel level = determinePermissionLevel(entity, accessLevel, permission, globalPermission);
		validatePermissionLevel(permission, level, entity);

		return level;
	}

	private void validateGlobalPermission(Entity entity, Permission permission) {
		if (entity == Entity.GLOBAL && permission != Permission.ALLOWED) {
			throw new UnknownAccessLevelException(MessageFormat
				.format("Only ALLOWED permission is supported for global permissions. Got: {0}", permission));
		}
	}

	private void validateEntity(Entity entity) {
		if (!this.entityAccessLevelGetters.containsKey(entity)) {
			throw new UnknownAccessLevelException(MessageFormat.format("Unknown entity: {0}", entity));
		}
	}

	private Object getAccessLevelObject(Entity entity, AccessControlDto accessControlDto) {
		return this.entityAccessLevelGetters.get(entity).apply(accessControlDto);
	}

	private PermissionLevel determinePermissionLevel(Entity entity, Object accessLevel, Permission permission,
			GlobalPermission globalPermission) {
		if (isGlobalAccessLevel(entity)) {
			return handleGlobalAccessLevel(accessLevel, globalPermission);
		}

		// Handle special permission fields
		if (isSpecialPermissionField(entity, permission)) {
			return handleSpecialPermissionField(accessLevel, permission);
		}

		String level = getPermissionLevelString(entity, accessLevel, permission);
		return PermissionLevel.fromLabel(level);
	}

	private String getPermissionLevelString(Entity entity, Object accessLevel, Permission permission) {
		if (accessLevel instanceof BasicAccessLevel basicLevel) {
			return switch (permission) {
				case CAN_ADD -> basicLevel.getCanAdd();
				case CAN_EDIT -> basicLevel.getCanEdit();
				case CAN_VIEW -> basicLevel.getCanView();
				case CAN_DELETE -> basicLevel.getCanDelete();
				case FILE_ACCESS, OWNER_CHANGE -> {
					if (accessLevel instanceof ExtendedAccessLevel extendedLevel) {
						/*
						 * The yield keyword is used in switch expressions to return a
						 * value from a case block that contains multiple statements.
						 * Unlike 'return', yield only exits the case block and returns
						 * the value to the switch expression, not the entire method.
						 *
						 * This is necessary because we can't use 'return' here as it
						 * would exit the method before the switch expression could
						 * complete.
						 */
						yield ((permission == Permission.FILE_ACCESS) ? extendedLevel.getFileAccess()
								: extendedLevel.getOwnerChange());
					}
					throw new UnknownAccessLevelException(
							MessageFormat.format("Entity {0} does not support {1}", entity, permission));
				}
				default -> throw new UnknownAccessLevelException(
						MessageFormat.format("Unknown action: {0} for entity: {1}", permission, entity));
			};
		}
		throw new UnknownAccessLevelException(MessageFormat.format("Invalid access level type for entity: {0}",
				(accessLevel != null) ? accessLevel.getClass().getName() : "null"));
	}

	/**
	 * Check if the given entity and permission combination represents a special
	 * permission field. Special permission fields are those that can have both string and
	 * integer values, and are registered in SpecialPermissionFieldHandler.
	 * @param entity The entity to check
	 * @param permission The permission to check
	 * @return true if this is a special permission field, false otherwise
	 */
	private boolean isSpecialPermissionField(Entity entity, Permission permission) {
		// Special permission fields are only applicable for global permissions
		if (entity != Entity.GLOBAL) {
			return false;
		}

		// Convert the permission to a field name format
		String fieldName = permission.name().toLowerCase(Locale.ROOT);

		// Check if this field is registered as a special permission field
		return io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.SpecialPermissionFieldHandler
			.isSpecialPermissionField(fieldName);
	}

	/**
	 * Handle special permission fields that can have both string and integer values. This
	 * method processes special permission values and converts them to appropriate
	 * PermissionLevel values based on the access level and permission type.
	 * @param entity The entity being checked (should be GLOBAL for special permissions)
	 * @param accessLevel The access level object containing the permission value
	 * @param permission The permission being checked
	 * @return The appropriate PermissionLevel for the given permission
	 * @throws UnknownAccessLevelException if the access level is invalid
	 */
	private PermissionLevel handleSpecialPermissionField(Object accessLevel, Permission permission) {
		if (!(accessLevel instanceof GlobalPermissions)) {
			throw new UnknownAccessLevelException(
					MessageFormat.format("Invalid access level type for special permission field: {0}",
							((accessLevel != null) ? accessLevel.getClass().getName() : "null")));
		}
		String fieldName = permission.name().toLowerCase(Locale.ROOT);

		try {
			// Get the raw value from the access level
			Object rawValue = getSpecialPermissionValue();
			if (rawValue == null) {
				return PermissionLevel.NO; // Default to NO if no value is set
			}

			// Process the value using SpecialPermissionFieldHandler
			int intValue = io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.SpecialPermissionFieldHandler
				.processSpecialPermissionValue(rawValue);

			// Convert to SpecialPermissionValue and then to PermissionLevel
			io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.SpecialPermissionValue specialValue = io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.SpecialPermissionValue
				.fromInt(intValue);

			if (specialValue == null) {
				throw new UnknownAccessLevelException(
						MessageFormat.format("Invalid permission value {0} for field {1}", intValue, fieldName));
			}

			return specialValue.toPermissionLevel();

		}
		catch (IllegalArgumentException ex) {
			throw new UnknownAccessLevelException(MessageFormat
				.format("Error processing special permission field {0}: {1}", fieldName, ex.getMessage()));
		}
	}

	/**
	 * Get the raw permission value from the GlobalPermissions object. This method needs
	 * to be implemented based on how the special permission values are stored in your
	 * GlobalPermissions class.
	 * @return The raw permission value (can be String or Integer)
	 */
	private Object getSpecialPermissionValue() {
		return null;
	}

	/**
	 * Validates that the permission level is appropriate for the given permission type.
	 * For special permission fields, this validation should be handled differently.
	 */
	private void validatePermissionLevel(Permission permission, PermissionLevel level, Entity entity) {
		// Skip validation for special permission fields
		if (isSpecialPermissionField(entity, permission)) {
			return;
		}

		boolean isBinaryPermission = (permission == Permission.CAN_ADD || permission == Permission.ALLOWED);
		boolean isBinaryLevel = (level == PermissionLevel.YES || level == PermissionLevel.NO);

		if (isBinaryPermission != isBinaryLevel) {
			throw new InvalidPermissionLevelException(
					MessageFormat.format("Invalid permission level {0} for {1} on entity {2}. {3}", level, permission,
							entity, isBinaryPermission ? "Only YES and NO are allowed."
									: "YES and NO are not allowed for this permission type."));
		}
	}

	private boolean isGlobalAccessLevel(Entity entity) {
		return entity == Entity.GLOBAL;
	}

	/**
	 * Utility method to handle global access level permissions. Global permissions are
	 * always checked with Permission.ALLOWED and return either YES or NO.
	 */
	private PermissionLevel handleGlobalAccessLevel(Object accessLevel, GlobalPermission globalPermission) {
		if (globalPermission == null) {
			throw new UnknownAccessLevelException("GlobalPermission must be specified for global permissions");
		}

		if (!(accessLevel instanceof GlobalPermissions globalPermissions)) {
			throw new UnknownAccessLevelException("Invalid access level type for global permissions");
		}

		Function<GlobalPermissions, PermissionLevel> permissionGetter = this.globalPermissionGetters
			.get(globalPermission);
		if (permissionGetter == null) {
			throw new UnknownAccessLevelException(
					MessageFormat.format("Unknown global permission type: {0}", globalPermission));
		}

		return permissionGetter.apply(globalPermissions);
	}

}