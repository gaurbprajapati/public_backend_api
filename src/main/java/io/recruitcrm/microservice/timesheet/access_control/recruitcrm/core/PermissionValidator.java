/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import org.springframework.stereotype.Component;

/**
 * Validates permissions based on actual level and team membership. This class is
 * responsible for determining if a user has the required permission based on the
 * permission level, type of permission, and ownership/team membership. For all
 * permissions, access is granted if ANY of these conditions are met: 1. The permission
 * level is EVERYTHING 2. The user is in the same team as the owner (for TEAM_ONLY level)
 * 3. The user is the owner (for OWNED_ONLY level)
 */
@Component(PermissionValidator.BEAN_NAME)
public class PermissionValidator {

	public static final String BEAN_NAME = "recruitcrmPermissionValidator";

	private final TeamMembershipChecker teamMembershipChecker;

	public PermissionValidator(TeamMembershipChecker teamMembershipChecker) {
		this.teamMembershipChecker = teamMembershipChecker;
	}

	/**
	 * Checks if the given permission level is allowed for the specified user and owner.
	 * For all permissions, grants access if ANY of these conditions are met: 1. The
	 * permission level is EVERYTHING 2. The user is in the same team as the owner (for
	 * TEAM_ONLY level) 3. The user is the owner (for OWNED_ONLY level)
	 *
	 * Special cases: - CAN_ADD permission requires YES level - Global permissions
	 * (ALLOWED) only support YES/NO levels
	 * @param actualLevel The actual permission level from the configuration
	 * @param permission The type of permission being checked
	 * @param ownerId The ID of the user who owns the resource (null if not applicable)
	 * @param currentUserId The ID of the user requesting access
	 * @return true if the permission is allowed, false otherwise
	 */
	public boolean isAllowed(PermissionLevel actualLevel, Permission permission, Integer ownerId,
			Integer currentUserId) {
		// Special handling for CAN_ADD permission
		if (permission == Permission.CAN_ADD) {
			return actualLevel == PermissionLevel.YES;
		}

		// Special handling for global permissions (ALLOWED)
		if (permission == Permission.ALLOWED) {
			return actualLevel == PermissionLevel.YES;
		}

		// For all other permissions, check all conditions
		// If level is EVERYTHING, grant access
		if (actualLevel == PermissionLevel.EVERYTHING) {
			return true;
		}

		// If no owner, only EVERYTHING level grants access
		if (ownerId == null) {
			return false;
		}

		// Check if user is the owner
		if (isOwner(ownerId, currentUserId)) {
			return true;
		}

		// Check if user is in the same team as the owner
		if (actualLevel == PermissionLevel.TEAM_ONLY) {
			return this.teamMembershipChecker.isInSameTeam(currentUserId, ownerId);
		}

		// For OWNED_ONLY level, only owner has access (already checked above)
		if (actualLevel == PermissionLevel.OWNED_ONLY) {
			return false;
		}

		// For YES/NO levels, use standard permission check
		return actualLevel == PermissionLevel.YES;
	}

	private boolean isOwner(Integer ownerId, Integer currentUserId) {
		return ownerId != null && ownerId.equals(currentUserId);
	}

}