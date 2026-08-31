package io.recruitcrm.microservice.timesheet.dto.export;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.testdata.FileFormatTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test cases for FileFormat enum following mandatory rule patterns. Tests all public
 * methods with 100% branch coverage using factory-based test data and BDD-style
 * assertions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileFormat Tests")
class FileFormatTests {

	// ==================== CSV Format Tests ====================

	@Test
	@DisplayName("CSV format should have correct file extension")
	void testCsvFormatHasCorrectFileExtension() {
		// Given
		FileFormat csvFormat = FileFormatTestDataFactory.getCsvFileFormat();

		// When
		String fileExtension = csvFormat.getFileExtension();

		// Then
		assertThat(fileExtension).isEqualTo(FileFormatTestDataFactory.getExpectedCsvFileExtension());
	}

	@Test
	@DisplayName("CSV format should have correct MIME type")
	void testCsvFormatHasCorrectMimeType() {
		// Given
		FileFormat csvFormat = FileFormatTestDataFactory.getCsvFileFormat();

		// When
		String mimeType = csvFormat.getMimeType();

		// Then
		assertThat(mimeType).isEqualTo(FileFormatTestDataFactory.getExpectedCsvMimeType());
	}

	// ==================== Excel Format Tests ====================

	@Test
	@DisplayName("Excel format should have correct file extension")
	void testExcelFormatHasCorrectFileExtension() {
		// Given
		FileFormat excelFormat = FileFormatTestDataFactory.getExcelFileFormat();

		// When
		String fileExtension = excelFormat.getFileExtension();

		// Then
		assertThat(fileExtension).isEqualTo(FileFormatTestDataFactory.getExpectedExcelFileExtension());
	}

	@Test
	@DisplayName("Excel format should have correct MIME type")
	void testExcelFormatHasCorrectMimeType() {
		// Given
		FileFormat excelFormat = FileFormatTestDataFactory.getExcelFileFormat();

		// When
		String mimeType = excelFormat.getMimeType();

		// Then
		assertThat(mimeType).isEqualTo(FileFormatTestDataFactory.getExpectedExcelMimeType());
	}

	// ==================== Enum Values Tests ====================

	@Test
	@DisplayName("File format enum should have expected number of values")
	void testFileFormatEnumHasExpectedNumberOfValues() {
		// Given & When
		FileFormat[] allFormats = FileFormatTestDataFactory.getAllFileFormats();

		// Then
		assertThat(allFormats).hasSize(FileFormatTestDataFactory.getExpectedFileFormatCount());
	}

	@Test
	@DisplayName("File format enum should contain CSV and Excel values")
	void testFileFormatEnumContainsCsvAndExcelValues() {
		// Given & When
		FileFormat[] allFormats = FileFormatTestDataFactory.getAllFileFormats();

		// Then
		assertThat(allFormats).contains(FileFormat.CSV, FileFormat.EXCEL);
	}

	@Test
	@DisplayName("CSV enum value should match factory constant")
	void testCsvEnumValueMatchesFactoryConstant() {
		// Given
		FileFormat csvFromFactory = FileFormatTestDataFactory.getCsvFileFormat();
		FileFormat csvFromEnum = FileFormat.CSV;

		// When & Then
		assertThat(csvFromFactory).isEqualTo(csvFromEnum);
	}

	@Test
	@DisplayName("Excel enum value should match factory constant")
	void testExcelEnumValueMatchesFactoryConstant() {
		// Given
		FileFormat excelFromFactory = FileFormatTestDataFactory.getExcelFileFormat();
		FileFormat excelFromEnum = FileFormat.EXCEL;

		// When & Then
		assertThat(excelFromFactory).isEqualTo(excelFromEnum);
	}

	// ==================== Enum Name Tests ====================

	@Test
	@DisplayName("CSV format should have correct enum name")
	void testCsvFormatHasCorrectEnumName() {
		// Given
		FileFormat csvFormat = FileFormatTestDataFactory.getCsvFileFormat();

		// When
		String enumName = csvFormat.name();

		// Then
		assertThat(enumName).isEqualTo("CSV");
	}

	@Test
	@DisplayName("Excel format should have correct enum name")
	void testExcelFormatHasCorrectEnumName() {
		// Given
		FileFormat excelFormat = FileFormatTestDataFactory.getExcelFileFormat();

		// When
		String enumName = excelFormat.name();

		// Then
		assertThat(enumName).isEqualTo("EXCEL");
	}

	// ==================== toString() Tests ====================

	@Test
	@DisplayName("CSV format toString should return enum name")
	void testCsvFormatToStringReturnsEnumName() {
		// Given
		FileFormat csvFormat = FileFormatTestDataFactory.getCsvFileFormat();

		// When
		String stringValue = csvFormat.toString();

		// Then
		assertThat(stringValue).isEqualTo("CSV");
	}

	@Test
	@DisplayName("Excel format toString should return enum name")
	void testExcelFormatToStringReturnsEnumName() {
		// Given
		FileFormat excelFormat = FileFormatTestDataFactory.getExcelFileFormat();

		// When
		String stringValue = excelFormat.toString();

		// Then
		assertThat(stringValue).isEqualTo("EXCEL");
	}

}
