package io.recruitcrm.microservice.timesheet.helpers.enums;

import lombok.Getter;

/**
 * Enum for entity types used in entity_off_limit_t table and entity search functionality.
 * Maps entity names to their corresponding numeric IDs in the database
 */
@Getter
public enum EntityTypeEnum {

	/**
	 * Company entity type (entitytype = 3)
	 */
	COMPANY(3, "company"),

	/**
	 * Job entity type (entitytype = 4)
	 */
	JOB(4, "job"),

	/**
	 * Entity type for candidates (entitytype = 5)
	 */
	CANDIDATE(5, "candidate"),

	/**
	 * Deal entity type (entitytype = 11)
	 */
	DEAL(11, "deal");

	private final Integer id;

	private final String entityName;

	EntityTypeEnum(Integer id, String entityName) {
		this.id = id;
		this.entityName = entityName;
	}

	/**
	 * Get the entity type ID as a String (for search functionality)
	 * @return the entity type ID as String
	 */
	public String getIdAsString() {
		return String.valueOf(this.id);
	}

	/**
	 * Get EntityType by entity name
	 * @param entityName The entity name to lookup
	 * @return The corresponding EntityType
	 * @throws IllegalArgumentException if entity name is not found
	 */
	public static EntityTypeEnum fromEntityName(String entityName) {
		for (EntityTypeEnum entityType : EntityTypeEnum.values()) {
			if (entityType.entityName.equalsIgnoreCase(entityName)) {
				return entityType;
			}
		}
		throw new IllegalArgumentException("No EntityType found for entity name: " + entityName);
	}

	/**
	 * Get EntityType by ID
	 * @param id The entity type ID
	 * @return The corresponding EntityType
	 * @throws IllegalArgumentException if ID is not found
	 */
	public static EntityTypeEnum fromId(Integer id) {
		for (EntityTypeEnum entityType : EntityTypeEnum.values()) {
			if (entityType.id.equals(id)) {
				return entityType;
			}
		}
		throw new IllegalArgumentException("No EntityType found for ID: " + id);
	}

}
