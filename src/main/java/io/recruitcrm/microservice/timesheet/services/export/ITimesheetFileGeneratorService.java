package io.recruitcrm.microservice.timesheet.services.export;

import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.PeriodGroupedExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ReimbursementExportRowDto;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;

/**
 * Interface for file generation service operations. Handles generation of CSV and Excel
 * files from export data.
 */
public interface ITimesheetFileGeneratorService {

	/**
	 * Generate file based on format and data.
	 * @param data List of dynamic export data
	 * @param request Export request containing format and field information
	 * @return ByteArrayResource containing the generated file
	 */
	ByteArrayResource generateFile(List<DynamicExportResponseBodyDto> data, DynamicExportRequestBodyDto request);

	/**
	 * Generate grouped file with period-based organization.
	 * @param groupedData List of period-grouped export data
	 * @param request Export request containing format and field information
	 * @return ByteArrayResource containing the generated file
	 */
	ByteArrayResource generateGroupedFile(List<PeriodGroupedExportResponseBodyDto> groupedData,
			DynamicExportRequestBodyDto request);

	/**
	 * Generate Excel file with an additional "Reimbursements" sheet.
	 * @param data List of dynamic export data
	 * @param request Export request containing format and field information
	 * @param reimbursements reimbursement rows to include as a second sheet
	 * @return ByteArrayResource containing the generated Excel file
	 */
	ByteArrayResource generateExcelWithReimbursements(List<DynamicExportResponseBodyDto> data,
			DynamicExportRequestBodyDto request, List<ReimbursementExportRowDto> reimbursements);

	/**
	 * Generate grouped Excel file with an additional "Reimbursements" sheet.
	 * @param groupedData List of period-grouped export data
	 * @param request Export request containing format and field information
	 * @param reimbursements reimbursement rows to include as a second sheet
	 * @return ByteArrayResource containing the generated Excel file
	 */
	ByteArrayResource generateGroupedExcelWithReimbursements(List<PeriodGroupedExportResponseBodyDto> groupedData,
			DynamicExportRequestBodyDto request, List<ReimbursementExportRowDto> reimbursements);

	/**
	 * Generate a ZIP containing timesheets.csv + reimbursements.csv.
	 * @param data List of dynamic export data
	 * @param request Export request containing format and field information
	 * @param reimbursements reimbursement rows for the reimbursements CSV
	 * @return ByteArrayResource containing the ZIP archive
	 */
	ByteArrayResource generateCsvWithReimbursementsZip(List<DynamicExportResponseBodyDto> data,
			DynamicExportRequestBodyDto request, List<ReimbursementExportRowDto> reimbursements);

	/**
	 * Generate a ZIP containing per-period CSVs + reimbursements.csv.
	 * @param groupedData List of period-grouped export data
	 * @param request Export request containing format and field information
	 * @param reimbursements reimbursement rows for the reimbursements CSV
	 * @return ByteArrayResource containing the ZIP archive
	 */
	ByteArrayResource generateGroupedCsvWithReimbursementsZip(List<PeriodGroupedExportResponseBodyDto> groupedData,
			DynamicExportRequestBodyDto request, List<ReimbursementExportRowDto> reimbursements);

}
