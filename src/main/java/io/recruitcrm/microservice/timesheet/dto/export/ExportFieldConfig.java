package io.recruitcrm.microservice.timesheet.dto.export;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration DTO for loading export field definitions from JSON. Represents the root
 * structure of the export-field-definitions.json file.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportFieldConfig {

	@JsonProperty("timesheet_fields")
	private List<FieldDefinitionJson> timesheetFields;

	@JsonProperty("candidate_fields")
	private List<FieldDefinitionJson> candidateFields;

	@JsonProperty("job_fields")
	private List<FieldDefinitionJson> jobFields;

	@JsonProperty("company_fields")
	private List<FieldDefinitionJson> companyFields;

	@JsonProperty("approval_fields")
	private List<FieldDefinitionJson> approvalFields;

	/**
	 * Get all field definitions as a single list
	 */
	public List<FieldDefinitionJson> getAllFields() {
		List<FieldDefinitionJson> allFields = new java.util.ArrayList<>();
		if (this.timesheetFields != null) {
			allFields.addAll(this.timesheetFields);
		}
		if (this.candidateFields != null) {
			allFields.addAll(this.candidateFields);
		}
		if (this.jobFields != null) {
			allFields.addAll(this.jobFields);
		}
		if (this.companyFields != null) {
			allFields.addAll(this.companyFields);
		}
		if (this.approvalFields != null) {
			allFields.addAll(this.approvalFields);
		}
		return allFields;
	}

}
