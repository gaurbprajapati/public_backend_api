package io.recruitcrm.microservice.timesheet.dto.extra_fields;

/**
 * DTO representing an extra field definition from Tblextrafields table.
 *
 * @param columnId The custom column ID (e.g., 1 for custcolumn1)
 * @param extrafieldname The display name of the extra field
 * @param extrafieldtype The type of the extra field (e.g., "date", "text", "number")
 * @param entitytypeid The entity type ID (5 for candidates)
 * @param accountid The account ID
 */
public record ExtraFieldDefinitionDto(Integer columnId, String extrafieldname, String extrafieldtype,
		Integer entitytypeid, Integer accountid) {

	/**
	 * Compact constructor for validation
	 */
	public ExtraFieldDefinitionDto {
		if (columnId == null || columnId < 1 || columnId > 150) {
			throw new IllegalArgumentException("Column ID must be between 1 and 150");
		}
		if (extrafieldname == null || extrafieldname.trim().isEmpty()) {
			throw new IllegalArgumentException("Extra field name cannot be null or empty");
		}
		if (extrafieldtype == null || extrafieldtype.trim().isEmpty()) {
			throw new IllegalArgumentException("Extra field type cannot be null or empty");
		}
	}

	/**
	 * Check if this extra field is a date type
	 * @return true if extrafieldtype is "date"
	 */
	public boolean isDateType() {
		return "date".contains(this.extrafieldtype);
	}

	/**
	 * Get the database column name for this extra field
	 * @return the database column name (e.g., "custcolumn1")
	 */
	public String getDatabaseColumnName() {
		return "custcolumn" + this.columnId;
	}

	/**
	 * Get the frontend field name for this extra field
	 * @return the frontend field name (e.g., "custcolumn1")
	 */
	public String getFrontendFieldName() {
		return "custcolumn" + this.columnId;
	}

}
