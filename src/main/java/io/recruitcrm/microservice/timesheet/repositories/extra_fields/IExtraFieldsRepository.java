package io.recruitcrm.microservice.timesheet.repositories.extra_fields;

import io.recruitcrm.microservice.timesheet.dto.extra_fields.ExtraFieldDefinitionDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityType;
import java.util.List;
import java.util.Map;

/**
 * Repository interface for managing extra fields (custom columns) from Tblextrafields
 * table.
 */
public interface IExtraFieldsRepository {

	/**
	 * Get extra field definitions for specific column IDs and entity type.
	 * @param columnIds List of column IDs (e.g., [1, 5, 10] for custcolumn1, custcolumn5,
	 * custcolumn10)
	 * @param entityType The entity type (e.g., CANDIDATE)
	 * @param accountId The account ID
	 * @return Map of columnId to ExtraFieldDefinitionDto
	 */
	Map<Integer, ExtraFieldDefinitionDto> getExtraFieldDefinitions(List<Integer> columnIds, EntityType entityType,
			Integer accountId);

	/**
	 * Check if extra field definitions exist for specific column IDs and entity type.
	 * @param columnIds List of column IDs to check
	 * @param entityType The entity type (e.g., CANDIDATE)
	 * @param accountId The account ID
	 * @return Map of columnId to boolean (true if exists, false otherwise)
	 */
	Map<Integer, Boolean> checkExtraFieldsExist(List<Integer> columnIds, EntityType entityType, Integer accountId);

}
