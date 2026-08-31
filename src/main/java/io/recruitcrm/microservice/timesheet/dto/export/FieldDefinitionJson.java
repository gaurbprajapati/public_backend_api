package io.recruitcrm.microservice.timesheet.dto.export;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * JSON DTO for individual field definitions loaded from JSON configuration. Represents a
 * single field configuration in the export-field-definitions.json file.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldDefinitionJson {

	@JsonProperty("frontendName")
	private String frontendName;

	@JsonProperty("jooqExpression")
	private String jooqExpression;

	@JsonProperty("displayName")
	private String displayName;

	@JsonProperty("javaType")
	private String javaType;

	@JsonProperty("nullable")
	private boolean nullable;

	@JsonProperty("enabled")
	private boolean enabled;

	@JsonProperty("requiredEntities")
	private List<String> requiredEntities;

	@JsonProperty("dynamicColumns")
	private boolean dynamicColumns;

	@JsonProperty("description")
	private String description;

	@JsonProperty("requiresPostProcessing")
	private boolean requiresPostProcessing;

}
