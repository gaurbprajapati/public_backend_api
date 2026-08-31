package io.recruitcrm.microservice.timesheet.services.export;

import com.opencsv.CSVWriter;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ExportFieldDefinition;
import io.recruitcrm.microservice.timesheet.dto.export.PeriodGroupedExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ReimbursementExportRowDto;
import io.recruitcrm.microservice.timesheet.exceptions.FileGeneratorException;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for generating files (CSV/Excel) from dynamic export data. Handles any
 * combination of fields without requiring fixed DTOs.
 */
@Service
public class TimesheetFileGeneratorService implements ITimesheetFileGeneratorService {

	private final ExportFieldRegistry fieldRegistry;

	public TimesheetFileGeneratorService(ExportFieldRegistry fieldRegistry) {
		this.fieldRegistry = fieldRegistry;
	}

	/**
	 * Generate file based on format and data
	 */
	@Override
	public ByteArrayResource generateFile(List<DynamicExportResponseBodyDto> data,
			DynamicExportRequestBodyDto request) {

		return switch (request.getFileFormat()) {
			case CSV -> this.generateCsvFile(data, request);
			case EXCEL -> this.generateExcelFile(data, request);
		};
	}

	/**
	 * Generate CSV file using OpenCSV
	 */
	private ByteArrayResource generateCsvFile(List<DynamicExportResponseBodyDto> data,
			DynamicExportRequestBodyDto request) {

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
				CSVWriter csvWriter = new CSVWriter(writer)) {

			// Write headers
			String[] headers = this.buildDynamicHeaders(data, request.getSelectedFields());
			csvWriter.writeNext(headers);

			// Write data rows
			for (DynamicExportResponseBodyDto exportData : data) {
				String[] row = this.buildDynamicCsvRow(exportData);
				csvWriter.writeNext(row);
			}

			// Ensure all data is flushed to the output stream
			csvWriter.flush();
			writer.flush();
			outputStream.flush();

			return new ByteArrayResource(outputStream.toByteArray());

		}
		catch (IOException exception) {

			throw new FileGeneratorException("Failed to generate CSV file", exception);
		}
	}

	/**
	 * Generate Excel file using Apache POI
	 */
	private ByteArrayResource generateExcelFile(List<DynamicExportResponseBodyDto> data,
			DynamicExportRequestBodyDto request) {

		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("Export Data");

			// Create header style
			CellStyle headerStyle = this.createHeaderStyle(workbook);

			// Create header row
			Row headerRow = sheet.createRow(0);
			String[] headers = this.buildDynamicHeaders(data, request.getSelectedFields());

			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(headerStyle);
			}

			// Create data rows
			for (int i = 0; i < data.size(); i++) {
				Row row = sheet.createRow(i + 1);
				DynamicExportResponseBodyDto exportData = data.get(i);

				List<Object> values = exportData.getValuesInOrderExcludingTimesheet();
				for (int j = 0; j < values.size(); j++) {
					Cell cell = row.createCell(j);
					this.setCellValue(cell, values.get(j));
				}
			}

			// Auto-size columns
			for (int i = 0; i < headers.length; i++) {
				sheet.autoSizeColumn(i);
			}

			workbook.write(outputStream);

			return new ByteArrayResource(outputStream.toByteArray());

		}
		catch (IOException exception) {

			throw new FileGeneratorException("Failed to generate Excel file", exception);
		}
	}

	/**
	 * Build headers array from field names
	 */
	private String[] buildHeaders(List<String> selectedFields) {
		return selectedFields.stream().map(this.fieldRegistry::getDisplayName).toArray(String[]::new);
	}

	/**
	 * Build dynamic headers that handle expanded time log columns. Uses actual column
	 * order from data instead of original selected fields. Excludes internal 'timesheet'
	 * field.
	 */
	private String[] buildDynamicHeaders(List<DynamicExportResponseBodyDto> data, List<String> originalSelectedFields) {
		if (data.isEmpty()) {
			// Fallback to original headers if no data
			return this.buildHeaders(originalSelectedFields);
		}

		// Get column order from first data row (all rows should have same structure)
		DynamicExportResponseBodyDto firstRow = data.get(0);
		List<String> actualColumns = firstRow.getColumnOrder();

		if (actualColumns == null || actualColumns.isEmpty()) {
			// Fallback to original headers if no column order
			return this.buildHeaders(originalSelectedFields);
		}

		// Build headers for actual columns (including dynamic date columns)
		// Exclude internal 'timesheet' field from final export
		return actualColumns.stream()
			.filter((columnName) -> !"timesheet".equals(columnName))
			.map(this::getHeaderForColumn)
			.toArray(String[]::new);
	}

	/**
	 * Get display header for a column name. Handles both regular fields and dynamic date
	 * columns.
	 */
	private String getHeaderForColumn(String columnName) {
		// Check if it's a regular field first
		try {
			String displayName = this.fieldRegistry.getDisplayName(columnName);
			if (displayName != null && !displayName.isEmpty()) {
				return displayName;
			}
		}
		catch (Exception ex) {
			// Field not found in registry, might be a dynamic column
		}

		// If not a regular field, assume it's a dynamic date column
		// Date columns are already formatted as "Thursday, 10 Jul 2025"
		return columnName;
	}

	/**
	 * Build dynamic CSV row that uses the actual column order from the data. This handles
	 * expanded time log columns properly. Excludes internal 'timesheet' field.
	 */
	private String[] buildDynamicCsvRow(DynamicExportResponseBodyDto exportData) {
		return exportData.getValuesInOrderExcludingTimesheet()
			.stream()
			.map((value) -> (value != null) ? value.toString() : "")
			.toArray(String[]::new);
	}

	/**
	 * Create header style for Excel
	 */
	private CellStyle createHeaderStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();

		// Set background color
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		// Set font
		Font font = workbook.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 12);
		style.setFont(font);

		// Set borders
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);

		return style;
	}

	/**
	 * Set cell value with appropriate type handling
	 */
	private void setCellValue(Cell cell, Object value) {
		if (value == null) {
			cell.setCellValue("");
		}
		else if (value instanceof Number number) {
			cell.setCellValue(number.doubleValue());
		}
		else if (value instanceof Boolean bool) {
			cell.setCellValue(bool);
		}
		else {
			cell.setCellValue(value.toString());
		}
	}

	/**
	 * Generate grouped file with period-based organization
	 */
	@Override
	public ByteArrayResource generateGroupedFile(List<PeriodGroupedExportResponseBodyDto> groupedData,
			DynamicExportRequestBodyDto request) {

		switch (request.getFileFormat()) {
			case CSV:
				return generateGroupedCsv(groupedData, request);
			case EXCEL:
				return generateGroupedExcel(groupedData, request);
			default:
				throw new IllegalArgumentException("Unsupported file format: " + request.getFileFormat());
		}
	}

	/**
	 * Generate CSV with period grouping (ZIP file containing multiple CSV files, one per
	 * period)
	 */
	private ByteArrayResource generateGroupedCsv(List<PeriodGroupedExportResponseBodyDto> groupedData,
			DynamicExportRequestBodyDto request) {
		try (ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
				ZipOutputStream zipOut = new ZipOutputStream(zipOutputStream)) {

			for (PeriodGroupedExportResponseBodyDto periodGroup : groupedData) {
				// Create individual CSV for this period
				byte[] csvData = this.generateSinglePeriodCsv(periodGroup, request);

				// Create sanitized filename for this period
				String filename = this.sanitizeCsvFilename(periodGroup.getPeriodDisplayName()) + ".csv";

				// Add CSV file to ZIP
				ZipEntry zipEntry = new ZipEntry(filename);
				zipOut.putNextEntry(zipEntry);
				zipOut.write(csvData);
				zipOut.closeEntry();
			}

			zipOut.finish();
			return new ByteArrayResource(zipOutputStream.toByteArray());

		}
		catch (IOException exception) {
			throw new FileGeneratorException("Failed to generate grouped CSV ZIP file", exception);
		}
	}

	/**
	 * Generate a single CSV file for one timesheet period
	 */
	private byte[] generateSinglePeriodCsv(PeriodGroupedExportResponseBodyDto periodGroup,
			DynamicExportRequestBodyDto request) throws IOException {

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
				CSVWriter csvWriter = new CSVWriter(writer)) {

			// Get dynamic headers for this period's data
			String[] headers;
			if (!periodGroup.getTimesheetsInPeriod().isEmpty()) {
				headers = this.buildDynamicHeaders(periodGroup.getTimesheetsInPeriod(), request.getSelectedFields());
			}
			else {
				// Fallback to original headers if no data
				List<ExportFieldDefinition> fieldDefs = this.fieldRegistry
					.getFieldDefinitions(request.getSelectedFields());
				headers = fieldDefs.stream().map(ExportFieldDefinition::getDisplayName).toArray(String[]::new);
			}

			// Write column headers
			csvWriter.writeNext(headers);

			// Write data for this period
			for (DynamicExportResponseBodyDto exportData : periodGroup.getTimesheetsInPeriod()) {
				String[] row = exportData.getValuesInOrderExcludingTimesheet()
					.stream()
					.map((value) -> (value != null) ? value.toString() : "")
					.toArray(String[]::new);
				csvWriter.writeNext(row);
			}

			csvWriter.flush();
			return outputStream.toByteArray();
		}
	}

	/**
	 * Sanitize filename for CSV files (similar to sheet name sanitization)
	 */
	private String sanitizeCsvFilename(String filename) {
		if (filename == null || filename.trim().isEmpty()) {
			return "export";
		}

		// Remove invalid characters for filenames
		return filename.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_").trim();
	}

	/**
	 * Generate Excel with separate sheets for each period
	 */
	private ByteArrayResource generateGroupedExcel(List<PeriodGroupedExportResponseBodyDto> groupedData,
			DynamicExportRequestBodyDto request) {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			// Create header style
			CellStyle headerStyle = this.createHeaderStyle(workbook);

			for (PeriodGroupedExportResponseBodyDto periodGroup : groupedData) {
				// Create sheet for this period (sanitize sheet name for Excel)
				String sheetName = this.sanitizeSheetName(periodGroup.getPeriodDisplayName());
				Sheet sheet = workbook.createSheet(sheetName);

				// Get dynamic headers for this period's data
				String[] headers;
				if (!periodGroup.getTimesheetsInPeriod().isEmpty()) {
					headers = this.buildDynamicHeaders(periodGroup.getTimesheetsInPeriod(),
							request.getSelectedFields());
				}
				else {
					// Fallback to original headers if no data
					List<ExportFieldDefinition> fieldDefs = this.fieldRegistry
						.getFieldDefinitions(request.getSelectedFields());
					headers = fieldDefs.stream().map(ExportFieldDefinition::getDisplayName).toArray(String[]::new);
				}

				// Create header row
				Row headerRow = sheet.createRow(0);
				for (int i = 0; i < headers.length; i++) {
					Cell cell = headerRow.createCell(i);
					cell.setCellValue(headers[i]);
					cell.setCellStyle(headerStyle);
				}

				// Create data rows for this period
				int rowNum = 1;
				for (DynamicExportResponseBodyDto exportData : periodGroup.getTimesheetsInPeriod()) {
					Row row = sheet.createRow(rowNum++);
					List<Object> values = exportData.getValuesInOrderExcludingTimesheet();
					for (int i = 0; i < values.size(); i++) {
						Cell cell = row.createCell(i);
						this.setCellValue(cell, values.get(i));
					}
				}

				// Auto-size columns
				for (int i = 0; i < headers.length; i++) {
					sheet.autoSizeColumn(i);
				}
			}

			workbook.write(outputStream);

			return new ByteArrayResource(outputStream.toByteArray());

		}
		catch (IOException exception) {

			throw new FileGeneratorException("Failed to generate grouped Excel file", exception);
		}
	}

	/**
	 * Sanitize sheet name for Excel (remove invalid characters and limit length)
	 */
	private String sanitizeSheetName(String name) {
		// Excel sheet names cannot contain: [ ] * ? / \ :
		// and must be 31 characters or less
		String sanitized = name.replaceAll("[\\[\\]*?/\\\\:]", "-");
		return (sanitized.length() > 31) ? sanitized.substring(0, 31) : sanitized;
	}

	// =========================================================================
	// Reimbursement sheet support (Excel only)
	// =========================================================================

	private static final String[] REIMBURSEMENT_HEADERS = { "Timesheet ID", "Timesheet Period", "Contractor Name",
			"Job Name", "Company Name", "Job Duration", "Reimbursement Description", "Amount", "Payable", "Billable",
			"Status" };

	@Override
	public ByteArrayResource generateExcelWithReimbursements(List<DynamicExportResponseBodyDto> data,
			DynamicExportRequestBodyDto request, List<ReimbursementExportRowDto> reimbursements) {

		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			CellStyle headerStyle = this.createHeaderStyle(workbook);
			this.writeMainExportSheet(workbook, data, request, headerStyle);

			if (reimbursements != null && !reimbursements.isEmpty()) {
				this.writeReimbursementSheet(workbook, reimbursements, headerStyle);
			}

			workbook.write(outputStream);
			return new ByteArrayResource(outputStream.toByteArray());

		}
		catch (IOException ex) {
			throw new FileGeneratorException("Failed to generate Excel file with reimbursements", ex);
		}
	}

	@Override
	public ByteArrayResource generateGroupedExcelWithReimbursements(
			List<PeriodGroupedExportResponseBodyDto> groupedData, DynamicExportRequestBodyDto request,
			List<ReimbursementExportRowDto> reimbursements) {

		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			CellStyle headerStyle = this.createHeaderStyle(workbook);

			for (PeriodGroupedExportResponseBodyDto periodGroup : groupedData) {
				String sheetName = this.sanitizeSheetName(periodGroup.getPeriodDisplayName());
				this.writePeriodSheet(workbook, sheetName, periodGroup, request, headerStyle);
			}

			if (reimbursements != null && !reimbursements.isEmpty()) {
				this.writeReimbursementSheet(workbook, reimbursements, headerStyle);
			}

			workbook.write(outputStream);
			return new ByteArrayResource(outputStream.toByteArray());

		}
		catch (IOException ex) {
			throw new FileGeneratorException("Failed to generate grouped Excel with reimbursements", ex);
		}
	}

	// =========================================================================
	// Shared Excel sheet helpers
	// =========================================================================

	private void writeMainExportSheet(Workbook workbook, List<DynamicExportResponseBodyDto> data,
			DynamicExportRequestBodyDto request, CellStyle headerStyle) {

		Sheet sheet = workbook.createSheet("Export Data");
		String[] headers = this.buildDynamicHeaders(data, request.getSelectedFields());

		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}

		for (int i = 0; i < data.size(); i++) {
			Row row = sheet.createRow(i + 1);
			List<Object> values = data.get(i).getValuesInOrderExcludingTimesheet();
			for (int j = 0; j < values.size(); j++) {
				Cell cell = row.createCell(j);
				this.setCellValue(cell, values.get(j));
			}
		}

		for (int i = 0; i < headers.length; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private void writePeriodSheet(Workbook workbook, String sheetName, PeriodGroupedExportResponseBodyDto periodGroup,
			DynamicExportRequestBodyDto request, CellStyle headerStyle) {

		Sheet sheet = workbook.createSheet(sheetName);

		String[] headers;
		if (!periodGroup.getTimesheetsInPeriod().isEmpty()) {
			headers = this.buildDynamicHeaders(periodGroup.getTimesheetsInPeriod(), request.getSelectedFields());
		}
		else {
			List<ExportFieldDefinition> fieldDefs = this.fieldRegistry.getFieldDefinitions(request.getSelectedFields());
			headers = fieldDefs.stream().map(ExportFieldDefinition::getDisplayName).toArray(String[]::new);
		}

		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
		}

		int rowNum = 1;
		for (DynamicExportResponseBodyDto exportData : periodGroup.getTimesheetsInPeriod()) {
			Row row = sheet.createRow(rowNum++);
			List<Object> values = exportData.getValuesInOrderExcludingTimesheet();
			for (int i = 0; i < values.size(); i++) {
				Cell cell = row.createCell(i);
				this.setCellValue(cell, values.get(i));
			}
		}

		for (int i = 0; i < headers.length; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private void writeReimbursementSheet(Workbook workbook, List<ReimbursementExportRowDto> reimbursements,
			CellStyle headerStyle) {

		Sheet sheet = workbook.createSheet("Reimbursements");

		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < REIMBURSEMENT_HEADERS.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(REIMBURSEMENT_HEADERS[i]);
			cell.setCellStyle(headerStyle);
		}

		for (int i = 0; i < reimbursements.size(); i++) {
			Row row = sheet.createRow(i + 1);
			ReimbursementExportRowDto r = reimbursements.get(i);

			row.createCell(0).setCellValue(nullSafe(r.getTimesheetId()));
			row.createCell(1).setCellValue(nullSafe(r.getTimesheetPeriod()));
			row.createCell(2).setCellValue(nullSafe(r.getContractorName()));
			row.createCell(3).setCellValue(nullSafe(r.getJobName()));
			row.createCell(4).setCellValue(nullSafe(r.getCompanyName()));
			row.createCell(5).setCellValue(nullSafe(r.getJobDuration()));
			row.createCell(6).setCellValue(nullSafe(r.getReimbursementDescription()));
			row.createCell(7).setCellValue(formatAmountWithCurrency(r.getCurrencySymbol(), r.getAmount()));
			row.createCell(8).setCellValue(nullSafe(r.getPayable()));
			row.createCell(9).setCellValue(nullSafe(r.getBillable()));
			row.createCell(10).setCellValue(nullSafe(r.getStatus()));
		}

		for (int i = 0; i < REIMBURSEMENT_HEADERS.length; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	// =========================================================================
	// CSV + Reimbursements ZIP support
	// =========================================================================

	@Override
	public ByteArrayResource generateCsvWithReimbursementsZip(List<DynamicExportResponseBodyDto> data,
			DynamicExportRequestBodyDto request, List<ReimbursementExportRowDto> reimbursements) {

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zipOut = new ZipOutputStream(baos)) {

			byte[] mainCsv = this.generateCsvBytes(data, request);
			zipOut.putNextEntry(new ZipEntry("timesheets.csv"));
			zipOut.write(mainCsv);
			zipOut.closeEntry();

			if (reimbursements != null && !reimbursements.isEmpty()) {
				byte[] reimbursementCsv = this.generateReimbursementCsvBytes(reimbursements);
				zipOut.putNextEntry(new ZipEntry("reimbursements.csv"));
				zipOut.write(reimbursementCsv);
				zipOut.closeEntry();
			}

			zipOut.finish();
			return new ByteArrayResource(baos.toByteArray());

		}
		catch (IOException ex) {
			throw new FileGeneratorException("Failed to generate CSV+reimbursements ZIP", ex);
		}
	}

	@Override
	public ByteArrayResource generateGroupedCsvWithReimbursementsZip(
			List<PeriodGroupedExportResponseBodyDto> groupedData, DynamicExportRequestBodyDto request,
			List<ReimbursementExportRowDto> reimbursements) {

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zipOut = new ZipOutputStream(baos)) {

			for (PeriodGroupedExportResponseBodyDto periodGroup : groupedData) {
				byte[] csvData = this.generateSinglePeriodCsv(periodGroup, request);
				String filename = this.sanitizeCsvFilename(periodGroup.getPeriodDisplayName()) + ".csv";
				zipOut.putNextEntry(new ZipEntry(filename));
				zipOut.write(csvData);
				zipOut.closeEntry();
			}

			if (reimbursements != null && !reimbursements.isEmpty()) {
				byte[] reimbursementCsv = this.generateReimbursementCsvBytes(reimbursements);
				zipOut.putNextEntry(new ZipEntry("reimbursements.csv"));
				zipOut.write(reimbursementCsv);
				zipOut.closeEntry();
			}

			zipOut.finish();
			return new ByteArrayResource(baos.toByteArray());

		}
		catch (IOException ex) {
			throw new FileGeneratorException("Failed to generate grouped CSV+reimbursements ZIP", ex);
		}
	}

	private byte[] generateCsvBytes(List<DynamicExportResponseBodyDto> data, DynamicExportRequestBodyDto request)
			throws IOException {

		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
				CSVWriter csvWriter = new CSVWriter(writer)) {

			csvWriter.writeNext(this.buildDynamicHeaders(data, request.getSelectedFields()));
			for (DynamicExportResponseBodyDto exportData : data) {
				csvWriter.writeNext(this.buildDynamicCsvRow(exportData));
			}
			csvWriter.flush();
			return out.toByteArray();
		}
	}

	private byte[] generateReimbursementCsvBytes(List<ReimbursementExportRowDto> reimbursements) throws IOException {

		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
				CSVWriter csvWriter = new CSVWriter(writer)) {

			csvWriter.writeNext(REIMBURSEMENT_HEADERS);

			for (ReimbursementExportRowDto r : reimbursements) {
				String[] row = { nullSafe(r.getTimesheetId()), nullSafe(r.getTimesheetPeriod()),
						nullSafe(r.getContractorName()), nullSafe(r.getJobName()), nullSafe(r.getCompanyName()),
						nullSafe(r.getJobDuration()), nullSafe(r.getReimbursementDescription()),
						formatAmountWithCurrency(r.getCurrencySymbol(), r.getAmount()), nullSafe(r.getPayable()),
						nullSafe(r.getBillable()), nullSafe(r.getStatus()) };
				csvWriter.writeNext(row);
			}
			csvWriter.flush();
			return out.toByteArray();
		}
	}

	// =========================================================================
	// Cell helpers
	// =========================================================================

	private static String formatAmountWithCurrency(String currencySymbol, BigDecimal amount) {
		if (amount == null) {
			return "";
		}
		String symbol = (currencySymbol != null && !currencySymbol.isEmpty()) ? currencySymbol + " " : "";
		return symbol + amount.toPlainString();
	}

	private static String nullSafe(String value) {
		return (value != null) ? value : "";
	}

}
