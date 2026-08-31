package io.recruitcrm.microservice.timesheet.services.export;

import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ExportResult;

/**
 * Interface for timesheet export service operations. Provides methods for exporting
 * timesheet data in various formats with period grouping support.
 */
public interface ITimesheetExportService {

	/**
	 * Export data and return both file resource and suggested filename in a single
	 * operation. This avoids duplicate database calls for filename generation.
	 * @param request Export request containing selected fields, format, and filters
	 * @return ExportResult containing the file resource, filename, and metadata
	 */
	ExportResult exportDataWithFilename(DynamicExportRequestBodyDto request);

}
