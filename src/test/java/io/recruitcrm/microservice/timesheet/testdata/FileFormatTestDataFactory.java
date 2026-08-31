package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.export.FileFormat;

/**
 * Test data factory for FileFormat unit tests. Provides factory methods for creating test
 * data and constants for comprehensive test coverage.
 */
public final class FileFormatTestDataFactory {

	private FileFormatTestDataFactory() {
		// Private constructor to prevent instantiation
	}

	// ==================== Test Constants ====================

	public static final String CSV_FILE_EXTENSION = "csv";

	public static final String CSV_MIME_TYPE = "text/csv";

	public static final String EXCEL_FILE_EXTENSION = "xlsx";

	public static final String EXCEL_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	// ==================== Factory Methods ====================

	/**
	 * Gets the CSV file format for testing
	 */
	public static FileFormat getCsvFileFormat() {
		return FileFormat.CSV;
	}

	/**
	 * Gets the Excel file format for testing
	 */
	public static FileFormat getExcelFileFormat() {
		return FileFormat.EXCEL;
	}

	/**
	 * Gets all available file formats for testing
	 */
	public static FileFormat[] getAllFileFormats() {
		return FileFormat.values();
	}

	// ==================== Expected Results ====================

	/**
	 * Gets the expected file extension for CSV format
	 */
	public static String getExpectedCsvFileExtension() {
		return CSV_FILE_EXTENSION;
	}

	/**
	 * Gets the expected MIME type for CSV format
	 */
	public static String getExpectedCsvMimeType() {
		return CSV_MIME_TYPE;
	}

	/**
	 * Gets the expected file extension for Excel format
	 */
	public static String getExpectedExcelFileExtension() {
		return EXCEL_FILE_EXTENSION;
	}

	/**
	 * Gets the expected MIME type for Excel format
	 */
	public static String getExpectedExcelMimeType() {
		return EXCEL_MIME_TYPE;
	}

	/**
	 * Gets the expected total number of file formats
	 */
	public static int getExpectedFileFormatCount() {
		return 2; // CSV and EXCEL
	}

}
