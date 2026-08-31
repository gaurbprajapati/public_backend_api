package io.recruitcrm.microservice.timesheet.dto.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;

/**
 * Result container for export operations that includes both the generated file and the
 * suggested filename to avoid duplicate database calls.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportResult {

	/**
	 * The generated file content as a ByteArrayResource
	 */
	private ByteArrayResource resource;

	/**
	 * Suggested filename based on the export data (includes period names for grouped
	 * exports)
	 */
	private String suggestedFilename;

	/**
	 * Number of records exported (for logging/metrics)
	 */
	private long recordCount;

	/**
	 * Whether this was a period-grouped export
	 */
	private boolean periodGrouped;

}
