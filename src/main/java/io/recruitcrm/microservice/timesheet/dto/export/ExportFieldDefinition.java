package io.recruitcrm.microservice.timesheet.dto.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jooq.Field;

import java.util.Set;

/**
 * Defines a field that can be exported, including its mapping from frontend name to
 * database expression and required tables for joins.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExportFieldDefinition {

	/**
	 * The name used by the frontend/API e.g., "candidate_name"
	 */
	private String frontendName;

	/**
	 * The JOOQ Field object for this export field e.g., DSL.concat(CANDIDATE.FIRSTNAME,
	 * DSL.val(" "), CANDIDATE.LASTNAME) or TIMESHEET.ID
	 */
	private Field<?> jooqField;

	/**
	 * Human-readable display name for the field e.g., "Candidate Name"
	 */
	private String displayName;

	/**
	 * Java type of the field value e.g., String.class, Integer.class, BigDecimal.class
	 */
	private Class<?> javaType;

	/**
	 * Entity aliases required for this field (for joins) e.g., ["t", "c", "j"] for
	 * timesheet, candidate, job
	 */
	private Set<String> requiredEntities;

	/**
	 * Whether the field can be null
	 */
	@Builder.Default
	private boolean nullable = true;

	/**
	 * Whether the field is enabled for export
	 */
	@Builder.Default
	private boolean enabled = true;

}
