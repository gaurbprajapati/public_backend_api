package io.recruitcrm.microservice.timesheet.dto.export;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dynamic export request that can handle any number of fields without requiring fixed
 * DTOs or field definitions.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DynamicExportRequestBodyDto {

	/**
	 * List of timesheet field names selected by the frontend for export e.g.,
	 * ["contractorName", "jobName", "timesheetPeriod"]
	 */
	@NotNull(message = "Timesheet fields cannot be null")
	@NotEmpty(message = "At least one timesheet field must be selected")
	private List<String> timesheetFields;

	@NotNull(message = "Candidate fields cannot be null")
	@NotEmpty(message = "At least one candidate field must be selected")
	private List<String> candidateFields;

	/**
	 * File format for export (CSV or EXCEL)
	 */
	@NotNull(message = "File format cannot be null")
	private FileFormat fileFormat;

	/**
	 * Maximum number of records to export
	 */
	@Min(value = 1, message = "Maximum records must be at least 1")
	@Max(value = 50000, message = "Maximum records cannot exceed 50,000")
	@Builder.Default
	private int maxRecords = 1000;

	/**
	 * Optional filters to apply to the export e.g., {"status": "APPROVED", "periodStart":
	 * "2024-01-01"}
	 */
	private Map<String, Object> filters;

	/**
	 * Optional list of specific timesheet IDs to export. When provided, only these
	 * timesheets will be included in the export, regardless of other filters. Limited to
	 * 1000 IDs maximum for performance reasons.
	 */
	@Size(min = 1, max = 1000, message = "TimesheetIds list must contain 1-1000 IDs when provided")
	private List<Integer> timesheetIds;

	/**
	 * Whether to group timesheets by period (period_start and period_end converted to
	 * UTC) When true, data will be grouped by timesheet periods and file/sheet names will
	 * use period format
	 */
	@Builder.Default
	private boolean exportEachDay = false;

	/**
	 * Controls reimbursement data inclusion in the export. false (default) = no
	 * reimbursement data, fully backwards compatible. true = include approved
	 * reimbursements. For Excel: adds a "Reimbursements" sheet. For CSV: produces a ZIP
	 * with timesheets CSV(s) + reimbursements.csv.
	 */
	@Builder.Default
	private boolean includeReimbursements = false;

	/**
	 * Get the selected fields for export processing with automatic effective_work_hours
	 * addition. Auto-adds effective_work_hours when exportEachDay is true.
	 * @return List of fields including effective_work_hours when needed
	 */
	public List<String> getSelectedFields() {
		if (this.timesheetFields == null) {
			return new ArrayList<>();
		}

		List<String> effectiveFields = new ArrayList<>(this.timesheetFields);
		effectiveFields.addAll(this.candidateFields);

		// Automatically add effectiveWorkHours when exportEachDay is true
		if (this.exportEachDay && !effectiveFields.contains("effectiveWorkHours")) {
			effectiveFields.add("effectiveWorkHours");
		}

		if (!effectiveFields.contains("timesheet")) {
			effectiveFields.add("timesheet");
		}

		return effectiveFields;
	}

	/**
	 * Get the original timesheet fields without automatic additions. Use this when you
	 * need the exact fields specified by the frontend.
	 * @return Original timesheet fields list
	 */
	public List<String> getOriginalTimesheetFields() {
		return this.timesheetFields;
	}

}