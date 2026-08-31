package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

/**
 * Represents special permission values that can have both string and integer
 * representations. These values are used for special permission fields that can have
 * multiple access levels. The integer values are assigned in a way that makes logical
 * sense for permission levels: - 0: No access (Nothing) - 1: Basic access (Owned Only) -
 * 2: Team-level access (Team Only) - 3: Entity-specific access (Candidates Only, Contacts
 * Only) - 4: Combined access (Both) - 5: Full access (Everything)
 */
public enum SpecialPermissionValue {

	NOTHING("Nothing", 0), OWNED_ONLY("Owned Only", 1), TEAM_ONLY("Team Only", 2),
	CANDIDATES_ONLY("Candidates Only", 3), CONTACTS_ONLY("Contacts Only", 3), BOTH("Both", 4),
	EVERYTHING("Everything", 5);

	private final String stringValue;

	private final int intValue;

	SpecialPermissionValue(String stringValue, int intValue) {
		this.stringValue = stringValue;
		this.intValue = intValue;
	}

	public String getStringValue() {
		return this.stringValue;
	}

	public int getIntValue() {
		return this.intValue;
	}

	public static SpecialPermissionValue fromString(String value) {
		if (value == null) {
			return null;
		}
		for (SpecialPermissionValue permissionValue : values()) {
			if (permissionValue.stringValue.equalsIgnoreCase(value)) {
				return permissionValue;
			}
		}
		return null;
	}

	public static SpecialPermissionValue fromInt(int value) {
		for (SpecialPermissionValue permissionValue : values()) {
			if (permissionValue.intValue == value) {
				return permissionValue;
			}
		}
		return null;
	}

	public static boolean isValidStringValue(String value) {
		return fromString(value) != null;
	}

	public static boolean isValidIntValue(int value) {
		return fromInt(value) != null;
	}

	/**
	 * Get the permission level that corresponds to this special permission value. Maps
	 * special permission values to their corresponding PermissionLevel values: - NOTHING
	 * -> NO - OWNED_ONLY -> OWNED_ONLY - TEAM_ONLY -> TEAM_ONLY - EVERYTHING ->
	 * EVERYTHING - Others (CANDIDATES_ONLY, CONTACTS_ONLY, BOTH) -> YES
	 */
	public io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.PermissionLevel toPermissionLevel() {
		return switch (this) {
			case NOTHING -> io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.PermissionLevel.NO;
			case OWNED_ONLY ->
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.PermissionLevel.OWNED_ONLY;
			case TEAM_ONLY ->
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.PermissionLevel.TEAM_ONLY;
			case EVERYTHING ->
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.PermissionLevel.EVERYTHING;
			case CANDIDATES_ONLY, CONTACTS_ONLY, BOTH ->
				io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.PermissionLevel.YES;
		};
	}

}