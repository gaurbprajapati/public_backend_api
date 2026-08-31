package io.recruitcrm.microservice.timesheet.controllers.export;

import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ExportResult;
import io.recruitcrm.microservice.timesheet.dto.export.FileFormat;
import io.recruitcrm.microservice.timesheet.services.export.ITimesheetExportService;
import jakarta.validation.Valid;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST Controller for handling dynamic export operations using JPQL-based dynamic
 * queries. Provides endpoints for exporting data with dynamic field selection in CSV or
 * Excel formats.
 *
 * This controller supports any combination of fields without requiring fixed DTOs. New
 * fields can be added to the ExportFieldRegistry without changing this controller.
 */
@RestController
@RequestMapping("v1/timesheets")
public class TimesheetExportController implements ITimesheetExportController {

	private final ITimesheetExportService timesheetExportService;

	public TimesheetExportController(ITimesheetExportService timesheetExportService) {
		this.timesheetExportService = timesheetExportService;
	}

	/**
	 * Exports data based on dynamic field selection using JPQL dynamic queries. Returns a
	 * CSV or Excel file for immediate download.
	 * @param request Export request containing selected columns, filters, and format
	 * options
	 * @return ResponseEntity containing the generated file as a downloadable resource
	 */
	@PostMapping("/export")
	public ResponseEntity<Resource> exportDynamicData(@Valid @RequestBody DynamicExportRequestBodyDto request) {

		// Use optimized method that returns both resource and filename in one DB call
		ExportResult result = this.timesheetExportService.exportDataWithFilename(request);

		// Generate final filename with appropriate extension and MIME type
		String filename = this.generateFilenameFromResult(result, request);
		String mimeType = this.getMimeTypeForResult(result, request);

		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
			.header(HttpHeaders.CONTENT_TYPE, mimeType)
			.header("Content-Encoding", "UTF-8")
			.body(result.getResource());
	}

	/**
	 * Generates filename from ExportResult (optimized - no duplicate DB calls)
	 */
	private String generateFilenameFromResult(ExportResult result, DynamicExportRequestBodyDto request) {
		String fileExtension = this.getFileExtensionForResult(result, request);

		// Generate timestamp in the format: MM-dd-yyyy_HHMMSS (using hyphens instead of
		// slashes for filename compatibility)
		LocalDateTime now = LocalDateTime.now();
		String exportDate = now.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
		String exportTime = now.format(DateTimeFormatter.ofPattern("HH-mm-ss"));

		// Use consistent naming: Timesheets_<ExportDate>_<ExportTime>
		return "Timesheets_" + exportDate + "_" + exportTime + "." + fileExtension;
	}

	/**
	 * Determines the appropriate MIME type based on the export result and request
	 */
	private String getMimeTypeForResult(ExportResult result, DynamicExportRequestBodyDto request) {
		if (this.isZipResponse(result, request)) {
			return "application/zip";
		}
		return request.getFileFormat().getMimeType();
	}

	/**
	 * Determines the appropriate file extension based on the export result and request
	 */
	private String getFileExtensionForResult(ExportResult result, DynamicExportRequestBodyDto request) {
		if (this.isZipResponse(result, request)) {
			return "zip";
		}
		return request.getFileFormat().getFileExtension();
	}

	private boolean isZipResponse(ExportResult result, DynamicExportRequestBodyDto request) {
		if (request.getFileFormat() != FileFormat.CSV) {
			return false;
		}
		boolean groupedCsv = request.isExportEachDay() && result.isPeriodGrouped();
		boolean csvWithReimbursements = request.isIncludeReimbursements();
		return groupedCsv || csvWithReimbursements;
	}

}
