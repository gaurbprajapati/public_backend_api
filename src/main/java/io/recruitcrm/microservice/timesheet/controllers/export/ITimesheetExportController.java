package io.recruitcrm.microservice.timesheet.controllers.export;

import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Interface for Timesheet Export Controller operations. Defines contract for dynamic
 * export functionality with field selection support.
 *
 * <h3>Timesheet ID Filtering:</h3>
 * <ul>
 * <li><strong>timesheetIds</strong> - Optional array of specific timesheet IDs to
 * export</li>
 * <li>When provided, only these timesheets will be included (1-1000 IDs max)</li>
 * <li>Takes priority over other filters but can be combined with them</li>
 * <li>Useful for exporting specific timesheets selected by users</li>
 * </ul>
 *
 * <h3>Work Hours Field Behavior:</h3>
 * <ul>
 * <li><strong>exportEachDay: false</strong> - work_hours field is ignored (no dynamic
 * columns)</li>
 * <li><strong>exportEachDay: true</strong> - work_hours field expands into separate date
 * columns:
 * <ul>
 * <li>Each date becomes a column: "Thursday, 10 Jul 2025", "Friday, 11 Jul 2025",
 * etc.</li>
 * <li>WORK_HOUR (1): Shows "8.50 hours" in each date column</li>
 * <li>START_AND_END_TIME (2): Shows "09:00-17:30" in each date column</li>
 * <li>Each period sheet shows only its own relevant date columns</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h3>Performance Optimization:</h3>
 * <p>
 * Time log data is fetched using batch optimization to minimize database calls:
 * </p>
 * <ul>
 * <li>Base timesheet data: 1 query</li>
 * <li>Time log data (if requested): 1 batch query for all timesheets</li>
 * <li>Total: 2 queries instead of N+1 queries</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <h4>Regular Export (Single Sheet):</h4> <pre>
 * {
 *   "timesheetFields": [
 *     "timesheetId",
 *     "contractorName",
 *     "timesheetPeriod",
 *     "job_duration",
 *     "job_name",
 *     "payData",
 *     "billData"
 *   ],
 *   "fileFormat": "EXCEL",
 *   "maxRecords": 100,
 *   "exportEachDay": false
 * }
 * </pre>
 *
 * <h4>Export Specific Timesheet IDs:</h4> <pre>
 * {
 *   "timesheetFields": [
 *     "timesheetId",
 *     "contractorName",
 *     "timesheetPeriod",
 *     "addedOn",
 *     "updatedOn",
 *     "timesheetStatusId",
 *     "allApprovers",
 *     "custcolumn1",
 *     "custcolumn10",
 *     "payData",
 *     "billData"
 *   ],
 *   "timesheetIds": [123, 456, 789],
 *   "fileFormat": "EXCEL",
 *   "maxRecords": 100,
 *   "exportEachDay": false
 * }
 * </pre>
 *
 * <h4>Grouped Export with Dynamic Time Log Columns:</h4> <pre>
 * {
 *   "timesheetFields": [
 *     "timesheetId",
 *     "contractorName",
 *     "timesheetPeriod",
 *     "job_name",
 *     "overtimeHours",
 *     "totalOvertime",
 *     "totalWorkTime",
 *     "totalTime",
 *     "payData",
 *     "billData"
 *   ],
 *   "fileFormat": "EXCEL",
 *   "maxRecords": 100,
 *   "exportEachDay": true
 * }
 * </pre>
 * <p>
 * <strong>Note:</strong> work_hours field must be explicitly included in timesheetFields
 * to get date columns
 * </p>
 * <p>
 * <strong>Note:</strong> overtime_hours and total_time fields create separate date
 * columns when exportEachDay: true<br>
 * <strong>Note:</strong> effective_work_hours is automatically added when exportEachDay:
 * true (shows total_time as decimal hours per day)<br>
 * <strong>Note:</strong> total_overtime_hours, total_work_hours, and totalTime are
 * aggregate totals that work with both exportEachDay: true/false<br>
 * <strong>Note:</strong> timesheet_approvers shows comma-separated list of approver names
 * from timesheet settings<br>
 * <strong>Note:</strong> Custom columns can be specified directly in payload (e.g.,
 * "custcolumn1", "custcolumn10"). Only columns that exist in Tblextrafields for the
 * current account will be displayed. Column headers use the actual field names from
 * extrafieldname. Date fields (extrafieldtype = "date") are automatically formatted as
 * MM/dd/yyyy UTC format.
 * </p>
 *
 * <h4>Specific Timesheet IDs with Time Logs (Grouped):</h4> <pre>
 * {
 *   "timesheetFields": [
 *     "timesheetId",
 *     "contractorName",
 *     "timesheetPeriod",
 *     "job_name"
 *   ],
 *   "timesheetIds": [123, 456],
 *   "fileFormat": "EXCEL",
 *   "maxRecords": 100,
 *   "exportEachDay": true
 * }
 * </pre>
 * <p>
 * <strong>Note:</strong> work_hours field must be explicitly included in timesheetFields
 * to get date columns
 * </p>
 */
public interface ITimesheetExportController {

	/**
	 * Exports data based on dynamic field selection using JPQL dynamic queries. Returns a
	 * CSV, Excel, or ZIP file for immediate download.
	 *
	 * <h4>File Format Behavior:</h4>
	 * <ul>
	 * <li><strong>Excel:</strong> Always returns .xlsx file (single file with multiple
	 * sheets when exportEachDay=true)</li>
	 * <li><strong>CSV with exportEachDay=false:</strong> Returns single .csv file</li>
	 * <li><strong>CSV with exportEachDay=true:</strong> Returns .zip file containing
	 * multiple CSV files (one per timesheet period)</li>
	 * </ul>
	 * @param request Export request containing selected columns, filters, and format
	 * options
	 * @return ResponseEntity containing the generated file as a downloadable resource
	 */
	ResponseEntity<Resource> exportDynamicData(@Valid @RequestBody DynamicExportRequestBodyDto request);

}
