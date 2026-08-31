package io.recruitcrm.microservice.timesheet.helpers.enums;

/**
 * Enum representing different entity types in the system. Used for identifying entity
 * types in Tblextrafields table.
 */
public enum EntityType {

	/**
	 * Candidate entity type (entitytypeid = 5)
	 */
	CANDIDATE(5);

	private final int id;

	EntityType(int id) {
		this.id = id;
	}

	/**
	 * Get the numeric ID for this entity type
	 * @return the entity type ID
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Get EntityType by ID
	 * @param id the entity type ID
	 * @return the corresponding EntityType, or null if not found
	 */
	public static EntityType fromId(int id) {
		for (EntityType type : values()) {
			if (type.getId() == id) {
				return type;
			}
		}
		return null;
	}

}
