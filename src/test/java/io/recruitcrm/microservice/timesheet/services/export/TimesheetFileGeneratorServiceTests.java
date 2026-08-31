package io.recruitcrm.microservice.timesheet.services.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mockConstruction;

import com.opencsv.CSVWriter;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.DynamicExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ExportFieldDefinition;
import io.recruitcrm.microservice.timesheet.dto.export.PeriodGroupedExportResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.export.ReimbursementExportRowDto;
import io.recruitcrm.microservice.timesheet.exceptions.FileGeneratorException;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetFileGeneratorServiceTestDataFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ByteArrayResource;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TimesheetFileGeneratorService Tests")
class TimesheetFileGeneratorServiceTests {

	@InjectMocks
	private TimesheetFileGeneratorService timesheetFileGeneratorService;

	@Mock
	private ExportFieldRegistry exportFieldRegistry;

	@BeforeEach
	void setUp() {
		// Given — common registry labels used by most export fixtures
		given(this.exportFieldRegistry.getDisplayName(anyString())).willAnswer((invocation) -> {
			String fieldKey = invocation.getArgument(0);
			if ("timesheet".equals(fieldKey)) {
				return "Timesheet ID";
			}
			if (TimesheetFileGeneratorServiceTestDataFactory.TEST_FIELD_NAME.equals(fieldKey)) {
				return TimesheetFileGeneratorServiceTestDataFactory.TEST_DISPLAY_NAME;
			}
			return TimesheetFileGeneratorServiceTestDataFactory.TEST_DISPLAY_NAME;
		});
	}

	@Test
	@DisplayName("Generate CSV file should return UTF-8 CSV with headers and rows")
	void testGenerateFileCsvFormatReturnsCsvContent() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<DynamicExportResponseBodyDto> data = TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseList();

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(data, request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getByteArray()).isNotEmpty();

		String csvContent = new String(result.getByteArray(), java.nio.charset.StandardCharsets.UTF_8);
		assertThat(csvContent).contains("\"" + TimesheetFileGeneratorServiceTestDataFactory.TEST_DISPLAY_NAME + "\"")
			.contains(TimesheetFileGeneratorServiceTestDataFactory.TEST_CANDIDATE_NAME)
			.contains("Jane Smith")
			.doesNotContain("TS-456")
			.doesNotContain(TimesheetFileGeneratorServiceTestDataFactory.TEST_TIMESHEET_ID);
	}

	@Test
	@DisplayName("Generate Excel file should produce valid XLSX with header and data rows")
	void testGenerateFileExcelFormatReturnsExcelContent() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<DynamicExportResponseBodyDto> data = TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseList();

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(data, request);

		// Then
		assertThat(result.getByteArray()).startsWith("PK".getBytes(java.nio.charset.StandardCharsets.UTF_8));

		try (XSSFWorkbook workbook = new XSSFWorkbook(result.getInputStream())) {
			assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
			assertThat(workbook.getSheetName(0)).isEqualTo("Export Data");
			Sheet sheet = workbook.getSheetAt(0);
			assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3);

			Row headerRow = sheet.getRow(0);
			assertThat(headerRow.getCell(0).getStringCellValue())
				.isEqualTo(TimesheetFileGeneratorServiceTestDataFactory.TEST_DISPLAY_NAME);

			Row firstDataRow = sheet.getRow(1);
			assertThat(firstDataRow.getCell(0).getStringCellValue())
				.isEqualTo(TimesheetFileGeneratorServiceTestDataFactory.TEST_CANDIDATE_NAME);
		}
	}

	@Test
	@DisplayName("Generate CSV with empty data should write headers from selected fields only")
	void testGenerateFileCsvWithEmptyDataReturnsHeaderRowOnly() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<DynamicExportResponseBodyDto> data = TimesheetFileGeneratorServiceTestDataFactory
			.createEmptyExportResponseList();

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(data, request);

		// Then
		String csvContent = new String(result.getByteArray(), java.nio.charset.StandardCharsets.UTF_8).trim();
		assertThat(csvContent)
			.isEqualTo("\"" + TimesheetFileGeneratorServiceTestDataFactory.TEST_DISPLAY_NAME + "\",\"Timesheet ID\"");
	}

	@Test
	@DisplayName("Generate Excel with empty data should contain header row built from selected fields")
	void testGenerateFileExcelWithEmptyDataReturnsHeaderOnly() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<DynamicExportResponseBodyDto> data = TimesheetFileGeneratorServiceTestDataFactory
			.createEmptyExportResponseList();

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(data, request);

		// Then
		try (XSSFWorkbook workbook = new XSSFWorkbook(result.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);
			assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
			Row headerRow = sheet.getRow(0);
			assertThat(headerRow.getCell(0).getStringCellValue())
				.isEqualTo(TimesheetFileGeneratorServiceTestDataFactory.TEST_DISPLAY_NAME);
			assertThat(headerRow.getCell(1).getStringCellValue()).isEqualTo("Timesheet ID");
		}
	}

	@Test
	@DisplayName("Generate CSV with time-based columns should expand headers and values")
	void testGenerateFileCsvWithTimeBasedColumnsHandlesDynamicHeaders() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory
			.createTimeBasedExportRequest();
		List<DynamicExportResponseBodyDto> data = List
			.of(TimesheetFileGeneratorServiceTestDataFactory.createTimeBasedExportResponse());

		given(this.exportFieldRegistry.getDisplayName(TimesheetFileGeneratorServiceTestDataFactory.TEST_DATE_COLUMN))
			.willReturn(TimesheetFileGeneratorServiceTestDataFactory.TEST_DATE_COLUMN);
		given(this.exportFieldRegistry
			.getDisplayName(TimesheetFileGeneratorServiceTestDataFactory.TEST_WORK_HOURS_COLUMN))
			.willReturn(TimesheetFileGeneratorServiceTestDataFactory.TEST_WORK_HOURS_COLUMN);
		given(this.exportFieldRegistry
			.getDisplayName(TimesheetFileGeneratorServiceTestDataFactory.TEST_OVERTIME_COLUMN))
			.willReturn(TimesheetFileGeneratorServiceTestDataFactory.TEST_OVERTIME_COLUMN);

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(data, request);

		// Then
		String csvContent = new String(result.getByteArray(), java.nio.charset.StandardCharsets.UTF_8);
		assertThat(csvContent).contains(TimesheetFileGeneratorServiceTestDataFactory.TEST_DISPLAY_NAME)
			.contains(TimesheetFileGeneratorServiceTestDataFactory.TEST_DATE_COLUMN)
			.contains(TimesheetFileGeneratorServiceTestDataFactory.TEST_WORK_HOURS_COLUMN)
			.contains(TimesheetFileGeneratorServiceTestDataFactory.TEST_OVERTIME_COLUMN)
			.contains("08:00")
			.contains("07:30")
			.contains("00:30");
	}

	@Test
	@DisplayName("Generate Excel with time-based columns should mirror dynamic headers on first row")
	void testGenerateFileExcelWithTimeBasedColumnsWritesMatchingHeaders() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<DynamicExportResponseBodyDto> data = List
			.of(TimesheetFileGeneratorServiceTestDataFactory.createTimeBasedExportResponse());

		given(this.exportFieldRegistry.getDisplayName(TimesheetFileGeneratorServiceTestDataFactory.TEST_DATE_COLUMN))
			.willReturn(TimesheetFileGeneratorServiceTestDataFactory.TEST_DATE_COLUMN);
		given(this.exportFieldRegistry
			.getDisplayName(TimesheetFileGeneratorServiceTestDataFactory.TEST_WORK_HOURS_COLUMN))
			.willReturn(TimesheetFileGeneratorServiceTestDataFactory.TEST_WORK_HOURS_COLUMN);
		given(this.exportFieldRegistry
			.getDisplayName(TimesheetFileGeneratorServiceTestDataFactory.TEST_OVERTIME_COLUMN))
			.willReturn(TimesheetFileGeneratorServiceTestDataFactory.TEST_OVERTIME_COLUMN);

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(data, request);

		// Then
		try (XSSFWorkbook workbook = new XSSFWorkbook(result.getInputStream())) {
			Row headerRow = workbook.getSheetAt(0).getRow(0);
			assertThat(headerRow.getCell(0).getStringCellValue())
				.isEqualTo(TimesheetFileGeneratorServiceTestDataFactory.TEST_DISPLAY_NAME);
			assertThat(headerRow.getCell(1).getStringCellValue())
				.isEqualTo(TimesheetFileGeneratorServiceTestDataFactory.TEST_DATE_COLUMN);
			assertThat(headerRow.getCell(2).getStringCellValue())
				.isEqualTo(TimesheetFileGeneratorServiceTestDataFactory.TEST_WORK_HOURS_COLUMN);
			assertThat(headerRow.getCell(3).getStringCellValue())
				.isEqualTo(TimesheetFileGeneratorServiceTestDataFactory.TEST_OVERTIME_COLUMN);

			Row dataRow = workbook.getSheetAt(0).getRow(1);
			assertThat(dataRow.getCell(1).getStringCellValue()).isEqualTo("08:00");
			assertThat(dataRow.getCell(2).getStringCellValue()).isEqualTo("07:30");
			assertThat(dataRow.getCell(3).getStringCellValue()).isEqualTo("00:30");
		}
	}

	@Test
	@DisplayName("Generate CSV with empty column order should fall back to registry headers")
	void testGenerateFileCsvWithEmptyColumnOrderFallsBackToOriginalHeaders() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<DynamicExportResponseBodyDto> data = List
			.of(TimesheetFileGeneratorServiceTestDataFactory.createExportResponseWithEmptyColumnOrder());

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(data, request);

		// Then
		String csvContent = new String(result.getByteArray(), java.nio.charset.StandardCharsets.UTF_8);
		assertThat(csvContent).contains(TimesheetFileGeneratorServiceTestDataFactory.TEST_DISPLAY_NAME)
			.contains("Timesheet ID");
	}

	@Test
	@DisplayName("Generate CSV with null column order should fail when writing data rows")
	void testGenerateFileCsvWithNullColumnOrderThrowsNullPointerException() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<DynamicExportResponseBodyDto> data = List
			.of(TimesheetFileGeneratorServiceTestDataFactory.createExportResponseWithNullColumnOrder());

		// When & Then
		assertThatThrownBy(() -> this.timesheetFileGeneratorService.generateFile(data, request))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Generate Excel should map numbers, booleans, nulls, and strings to appropriate cell types")
	void testGenerateFileExcelWithVariousValueTypesHandlesCellValues() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<DynamicExportResponseBodyDto> data = List
			.of(TimesheetFileGeneratorServiceTestDataFactory.createExportResponseWithVariousValueTypes());

		given(this.exportFieldRegistry.getDisplayName("numberField")).willReturn("Number Field");
		given(this.exportFieldRegistry.getDisplayName("booleanField")).willReturn("Boolean Field");
		given(this.exportFieldRegistry.getDisplayName("nullField")).willReturn("Null Field");
		given(this.exportFieldRegistry.getDisplayName("stringField")).willReturn("String Field");

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(data, request);

		// Then
		try (XSSFWorkbook workbook = new XSSFWorkbook(result.getInputStream())) {
			Row dataRow = workbook.getSheetAt(0).getRow(1);
			assertThat(dataRow.getCell(1).getNumericCellValue()).isEqualTo(123.45);
			assertThat(dataRow.getCell(2).getBooleanCellValue()).isTrue();
			assertThat(dataRow.getCell(3).getStringCellValue()).isEmpty();
			assertThat(dataRow.getCell(4).getStringCellValue()).isEqualTo("Test String");
		}
	}

	@Test
	@DisplayName("CSV grouped export should ZIP one CSV per period with sanitized names")
	void testGenerateGroupedFileCsvReturnsZipWithMultipleCsvFiles() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = TimesheetFileGeneratorServiceTestDataFactory
			.createPeriodGroupedExportResponseList();

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request);

		// Then
		try (ZipInputStream zipInputStream = new ZipInputStream(result.getInputStream())) {
			ZipEntry entry1 = zipInputStream.getNextEntry();
			assertThat(entry1).isNotNull();
			assertThat(entry1.getName()).isEqualTo("Week_of_Jan_01,_2024.csv");

			ZipEntry entry2 = zipInputStream.getNextEntry();
			assertThat(entry2).isNotNull();
			assertThat(entry2.getName()).isEqualTo("Week_of_Jan_08,_2024.csv");

			assertThat(zipInputStream.getNextEntry()).isNull();
		}
	}

	@Test
	@DisplayName("Excel grouped export should create one sheet per period with matching labels")
	void testGenerateGroupedFileExcelReturnsExcelWithMultipleSheets() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = TimesheetFileGeneratorServiceTestDataFactory
			.createPeriodGroupedExportResponseList();

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request);

		// Then
		try (XSSFWorkbook workbook = new XSSFWorkbook(result.getInputStream())) {
			assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
			assertThat(workbook.getSheetName(0))
				.isEqualTo(TimesheetFileGeneratorServiceTestDataFactory.TEST_SANITIZED_SHEET_NAME);
			assertThat(workbook.getSheetName(1)).isEqualTo("Week of Jan 08, 2024");
		}
	}

	@Test
	@DisplayName("Grouped CSV with empty period should pull headers from field definitions")
	void testGenerateGroupedFileCsvWithEmptyPeriodDataFallsBackToFieldDefinitions() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = List
			.of(TimesheetFileGeneratorServiceTestDataFactory.createPeriodGroupedExportResponseWithEmptyTimesheets());

		List<ExportFieldDefinition> fieldDefinitions = TimesheetFileGeneratorServiceTestDataFactory
			.createExportFieldDefinitionList();
		given(this.exportFieldRegistry.getFieldDefinitions(anyList())).willReturn(fieldDefinitions);

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request);

		// Then
		try (ZipInputStream zipInputStream = new ZipInputStream(result.getInputStream())) {
			ZipEntry entry = zipInputStream.getNextEntry();
			assertThat(entry.getName()).endsWith(".csv");
			byte[] csvBytes = zipInputStream.readAllBytes();
			String csv = new String(csvBytes, java.nio.charset.StandardCharsets.UTF_8).trim();
			assertThat(csv.split(System.lineSeparator())[0]).contains("Candidate Name").contains("Timesheet ID");
		}

		then(this.exportFieldRegistry).should().getFieldDefinitions(anyList());
	}

	@Test
	@DisplayName("Grouped Excel with empty period should pull headers from field definitions")
	void testGenerateGroupedFileExcelWithEmptyPeriodDataFallsBackToFieldDefinitions() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = List
			.of(TimesheetFileGeneratorServiceTestDataFactory.createPeriodGroupedExportResponseWithEmptyTimesheets());

		List<ExportFieldDefinition> fieldDefinitions = TimesheetFileGeneratorServiceTestDataFactory
			.createExportFieldDefinitionList();
		given(this.exportFieldRegistry.getFieldDefinitions(anyList())).willReturn(fieldDefinitions);

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request);

		// Then
		try (XSSFWorkbook workbook = new XSSFWorkbook(result.getInputStream())) {
			Row headerRow = workbook.getSheetAt(0).getRow(0);
			assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("Candidate Name");
			assertThat(headerRow.getCell(1).getStringCellValue()).isEqualTo("Timesheet ID");
			assertThat(workbook.getSheetAt(0).getPhysicalNumberOfRows()).isEqualTo(1);
		}

		then(this.exportFieldRegistry).should().getFieldDefinitions(anyList());
	}

	@Test
	@DisplayName("Grouped export with null file format should throw NullPointerException")
	void testGenerateGroupedFileWithNullFormatThrowsNullPointerException() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetFileGeneratorServiceTestDataFactory.TEST_FIELD_NAME))
			.candidateFields(List.of())
			.fileFormat(null)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		List<PeriodGroupedExportResponseBodyDto> groupedData = TimesheetFileGeneratorServiceTestDataFactory
			.createPeriodGroupedExportResponseList();

		// When & Then
		assertThatThrownBy(() -> this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Generate file with null file format should throw NullPointerException")
	void testGenerateFileWithNullFormatThrowsNullPointerException() {
		// Given
		DynamicExportRequestBodyDto request = DynamicExportRequestBodyDto.builder()
			.timesheetFields(List.of(TimesheetFileGeneratorServiceTestDataFactory.TEST_FIELD_NAME))
			.candidateFields(List.of())
			.fileFormat(null)
			.exportEachDay(false)
			.maxRecords(1000)
			.build();
		List<DynamicExportResponseBodyDto> data = TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseList();

		// When & Then
		assertThatThrownBy(() -> this.timesheetFileGeneratorService.generateFile(data, request))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("CSV should use raw column key when registry returns null display name")
	void testGenerateFileCsvUsesRawColumnNameWhenDisplayNameNull() {
		// Given
		final String dynamicKey = "dynamicColumnNullDisplay";
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<DynamicExportResponseBodyDto> data = List.of(TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseWithSingleDynamicColumn(dynamicKey, "z"));

		given(this.exportFieldRegistry.getDisplayName(dynamicKey)).willReturn(null);

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(data, request);

		// Then
		String csvContent = new String(result.getByteArray(), StandardCharsets.UTF_8);
		assertThat(csvContent.split(System.lineSeparator())[0]).contains(dynamicKey);
		assertThat(csvContent).contains("z");
	}

	@Test
	@DisplayName("Generate CSV maps null data cell to empty quoted field")
	void testGenerateFileCsvMapsNullCellToEmptyString() {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<DynamicExportResponseBodyDto> data = List
			.of(TimesheetFileGeneratorServiceTestDataFactory.createExportResponseWithNullOptionalColumn());
		given(this.exportFieldRegistry.getDisplayName("optionalNote")).willReturn("Note");

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(data, request);

		// Then
		String csvContent = new String(result.getByteArray(), StandardCharsets.UTF_8);
		assertThat(csvContent).contains("\"Note\"")
			.contains(TimesheetFileGeneratorServiceTestDataFactory.TEST_CANDIDATE_NAME)
			.contains("\"\"");
	}

	@Test
	@DisplayName("Grouped CSV maps null data cell to empty quoted field")
	void testGenerateGroupedFileCsvMapsNullCellToEmptyString() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = List
			.of(TimesheetFileGeneratorServiceTestDataFactory.createPeriodGroupedExportResponse("Week_A", List
				.of(TimesheetFileGeneratorServiceTestDataFactory.createExportResponseWithNullOptionalColumn())));
		given(this.exportFieldRegistry.getDisplayName("optionalNote")).willReturn("Note");

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request);

		// Then
		try (ZipInputStream zipInputStream = new ZipInputStream(result.getInputStream())) {
			assertThat(zipInputStream.getNextEntry()).isNotNull();
			String csv = new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8);
			assertThat(csv).contains("\"Note\"")
				.contains(TimesheetFileGeneratorServiceTestDataFactory.TEST_CANDIDATE_NAME)
				.contains("\"\"");
		}
	}

	@Test
	@DisplayName("sanitizeCsvFilename returns export when argument is null")
	void testSanitizeCsvFilenameNullReturnsExportViaReflection() throws Exception {
		Method method = TimesheetFileGeneratorService.class.getDeclaredMethod("sanitizeCsvFilename", String.class);
		method.setAccessible(true);
		assertThat(method.invoke(this.timesheetFileGeneratorService, (Object) null)).isEqualTo("export");
	}

	@Test
	@DisplayName("CSV should use raw column key when registry returns empty display name")
	void testGenerateFileCsvUsesRawColumnNameWhenDisplayNameEmpty() {
		// Given
		final String dynamicKey = "dynamicOnlyColumn";
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<DynamicExportResponseBodyDto> data = List.of(TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseWithSingleDynamicColumn(dynamicKey, "x"));

		given(this.exportFieldRegistry.getDisplayName(dynamicKey)).willReturn("");

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(data, request);

		// Then
		String csvContent = new String(result.getByteArray(), java.nio.charset.StandardCharsets.UTF_8);
		assertThat(csvContent.split(System.lineSeparator())[0]).contains(dynamicKey);
	}

	@Test
	@DisplayName("CSV should use raw column key when registry throws for that column")
	void testGenerateFileCsvUsesRawColumnNameWhenRegistryThrows() {
		// Given
		final String problematicKey = "problematicColumn";
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<DynamicExportResponseBodyDto> data = List.of(TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseWithSingleDynamicColumn(problematicKey, "y"));

		given(this.exportFieldRegistry.getDisplayName(problematicKey))
			.willThrow(new IllegalStateException("registry failure"));

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(data, request);

		// Then
		String csvContent = new String(result.getByteArray(), java.nio.charset.StandardCharsets.UTF_8);
		assertThat(csvContent.split(System.lineSeparator())[0]).contains(problematicKey);
		assertThat(csvContent).contains("y");
	}

	@Test
	@DisplayName("Grouped CSV should sanitize unsafe characters in period filenames")
	void testGenerateGroupedFileCsvSanitizesUnsafeFilenameCharacters() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = List.of(TimesheetFileGeneratorServiceTestDataFactory
			.createPeriodGroupedExportResponse(TimesheetFileGeneratorServiceTestDataFactory.TEST_INVALID_FILENAME,
					List.of(TimesheetFileGeneratorServiceTestDataFactory.createBasicExportResponse())));

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request);

		// Then
		try (ZipInputStream zipInputStream = new ZipInputStream(result.getInputStream())) {
			ZipEntry entry = zipInputStream.getNextEntry();
			assertThat(entry.getName()).isEqualTo("Week_of_Jan_01_2024.csv");
		}
	}

	@Test
	@DisplayName("Grouped CSV should name file export when period label is blank whitespace")
	void testGenerateGroupedFileCsvUsesExportWhenPeriodLabelWhitespace() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = List.of(TimesheetFileGeneratorServiceTestDataFactory
			.createPeriodGroupedExportResponse(TimesheetFileGeneratorServiceTestDataFactory.createWhitespaceFilename(),
					List.of(TimesheetFileGeneratorServiceTestDataFactory.createBasicExportResponse())));

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request);

		// Then
		try (ZipInputStream zipInputStream = new ZipInputStream(result.getInputStream())) {
			assertThat(zipInputStream.getNextEntry().getName()).isEqualTo("export.csv");
		}
	}

	@Test
	@DisplayName("Grouped CSV should derive filename from UTC range when display name is absent")
	void testGenerateGroupedFileCsvUsesUtcFallbackForPeriodLabel() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = List
			.of(TimesheetFileGeneratorServiceTestDataFactory.createPeriodGroupedExportResponseWithUtcFallbackLabel());

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request);

		// Then
		try (ZipInputStream zipInputStream = new ZipInputStream(result.getInputStream())) {
			assertThat(zipInputStream.getNextEntry().getName()).isEqualTo("2024-01-01_-_2024-01-07.csv");
		}
	}

	@Test
	@DisplayName("Grouped Excel should truncate sheet titles longer than 31 characters")
	void testGenerateGroupedFileExcelTruncatesLongSheetName() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = List.of(TimesheetFileGeneratorServiceTestDataFactory
			.createPeriodGroupedExportResponse(TimesheetFileGeneratorServiceTestDataFactory.createLongSheetName(),
					List.of(TimesheetFileGeneratorServiceTestDataFactory.createBasicExportResponse())));

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request);

		// Then
		try (XSSFWorkbook workbook = new XSSFWorkbook(result.getInputStream())) {
			assertThat(workbook.getSheetName(0)).hasSize(31).isEqualTo("This is a very long sheet name ");
		}
	}

	@Test
	@DisplayName("Grouped Excel should replace invalid sheet name characters with dashes")
	void testGenerateGroupedFileExcelSanitizesInvalidSheetCharacters() throws IOException {
		// Given
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = List
			.of(TimesheetFileGeneratorServiceTestDataFactory.createPeriodGroupedExportResponse(
					TimesheetFileGeneratorServiceTestDataFactory.createTestSheetNameForSanitization(),
					List.of(TimesheetFileGeneratorServiceTestDataFactory.createBasicExportResponse())));

		// When
		ByteArrayResource result = this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request);

		// Then
		try (XSSFWorkbook workbook = new XSSFWorkbook(result.getInputStream())) {
			assertThat(workbook.getSheetName(0)).isEqualTo("Week-of-Jan-01-2024---");
		}
	}

	@Test
	@DisplayName("Generate Excel with reimbursements adds reimbursement sheet when rows present")
	void testGenerateExcelWithReimbursementsAddsSheetWhenRowsPresent() throws Exception {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<DynamicExportResponseBodyDto> data = TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseList();
		List<ReimbursementExportRowDto> reimbursements = List.of(
				ReimbursementExportRowDto.builder()
					.timesheetId("1")
					.timesheetPeriod("P1")
					.contractorName("C")
					.jobName("J")
					.companyName("Co")
					.jobDuration("1m")
					.reimbursementDescription("Mileage")
					.amount(new BigDecimal("12.50"))
					.currencySymbol("$")
					.payable("Y")
					.billable("N")
					.status("APPROVED")
					.build(),
				ReimbursementExportRowDto.builder()
					.timesheetId(null)
					.amount(null)
					.currencySymbol(null)
					.payable(null)
					.build());

		ByteArrayResource result = this.timesheetFileGeneratorService.generateExcelWithReimbursements(data, request,
				reimbursements);

		assertThat(result.getByteArray()).isNotEmpty();
		try (Workbook workbook = new XSSFWorkbook(result.getInputStream())) {
			assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
			Sheet reimbursementSheet = workbook.getSheet("Reimbursements");
			assertThat(reimbursementSheet).isNotNull();
			Row header = reimbursementSheet.getRow(0);
			assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Timesheet ID");
			assertThat(reimbursementSheet.getRow(1).getCell(7).getStringCellValue()).contains("12.50");
			assertThat(reimbursementSheet.getRow(2).getCell(7).getStringCellValue()).isEmpty();
		}
	}

	@Test
	@DisplayName("Generate Excel with reimbursements skips reimbursement sheet when list null or empty")
	void testGenerateExcelWithReimbursementsSkipsSheetWhenNullOrEmpty() throws Exception {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<DynamicExportResponseBodyDto> data = TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseList();

		try (Workbook wbNull = new XSSFWorkbook(
				this.timesheetFileGeneratorService.generateExcelWithReimbursements(data, request, null)
					.getInputStream())) {
			assertThat(wbNull.getNumberOfSheets()).isEqualTo(1);
		}
		try (Workbook wbEmpty = new XSSFWorkbook(
				this.timesheetFileGeneratorService.generateExcelWithReimbursements(data, request, List.of())
					.getInputStream())) {
			assertThat(wbEmpty.getNumberOfSheets()).isEqualTo(1);
		}
	}

	@Test
	@DisplayName("Generate grouped Excel with reimbursements uses sanitized sheet names and reimbursement sheet")
	void testGenerateGroupedExcelWithReimbursementsSanitizesNamesAndAddsReimbursements() throws Exception {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		PeriodGroupedExportResponseBodyDto longNamePeriod = new PeriodGroupedExportResponseBodyDto();
		longNamePeriod.setPeriodDisplayName(TimesheetFileGeneratorServiceTestDataFactory.TEST_LONG_SHEET_NAME);
		longNamePeriod
			.setTimesheetsInPeriod(List.of(TimesheetFileGeneratorServiceTestDataFactory.createBasicExportResponse()));
		List<PeriodGroupedExportResponseBodyDto> groupedData = List.of(longNamePeriod);
		List<ReimbursementExportRowDto> reimbursements = List
			.of(ReimbursementExportRowDto.builder().amount(new BigDecimal("1")).currencySymbol("").status("X").build());

		ByteArrayResource result = this.timesheetFileGeneratorService
			.generateGroupedExcelWithReimbursements(groupedData, request, reimbursements);

		try (Workbook workbook = new XSSFWorkbook(result.getInputStream())) {
			assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
			assertThat(workbook.getSheetName(0).length()).isLessThanOrEqualTo(31);
			assertThat(workbook.getSheet("Reimbursements")).isNotNull();
		}
	}

	@Test
	@DisplayName("Generate grouped Excel with reimbursements without reimbursement rows has only period sheets")
	void testGenerateGroupedExcelWithReimbursementsNullListSingleSheetOnly() throws Exception {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = TimesheetFileGeneratorServiceTestDataFactory
			.createPeriodGroupedExportResponseList();

		try (Workbook workbook = new XSSFWorkbook(
				this.timesheetFileGeneratorService.generateGroupedExcelWithReimbursements(groupedData, request, null)
					.getInputStream())) {
			assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
		}
	}

	@Test
	@DisplayName("Generate CSV with reimbursements ZIP contains main CSV and optional reimbursements CSV")
	void testGenerateCsvWithReimbursementsZipWritesEntries() throws IOException {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<DynamicExportResponseBodyDto> data = TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseList();

		ByteArrayResource withoutReimb = this.timesheetFileGeneratorService.generateCsvWithReimbursementsZip(data,
				request, null);
		try (ZipInputStream zis = new ZipInputStream(withoutReimb.getInputStream())) {
			assertThat(zis.getNextEntry().getName()).isEqualTo("timesheets.csv");
			assertThat(zis.getNextEntry()).isNull();
		}

		List<ReimbursementExportRowDto> rows = List
			.of(ReimbursementExportRowDto.builder().amount(new BigDecimal("2")).currencySymbol("€").build());
		ByteArrayResource withReimb = this.timesheetFileGeneratorService.generateCsvWithReimbursementsZip(data, request,
				rows);
		try (ZipInputStream zis = new ZipInputStream(withReimb.getInputStream())) {
			assertThat(zis.getNextEntry().getName()).isEqualTo("timesheets.csv");
			assertThat(zis.getNextEntry().getName()).isEqualTo("reimbursements.csv");
			assertThat(zis.getNextEntry()).isNull();
		}
	}

	@Test
	@DisplayName("Generate grouped CSV with reimbursements ZIP sanitizes period filenames and adds reimbursements")
	void testGenerateGroupedCsvWithReimbursementsZipSanitizesAndZips() throws IOException {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName(TimesheetFileGeneratorServiceTestDataFactory.TEST_INVALID_FILENAME);
		period.setTimesheetsInPeriod(List.of(TimesheetFileGeneratorServiceTestDataFactory.createBasicExportResponse()));
		List<ReimbursementExportRowDto> rows = List.of(ReimbursementExportRowDto.builder().status("S").build());

		ByteArrayResource result = this.timesheetFileGeneratorService
			.generateGroupedCsvWithReimbursementsZip(List.of(period), request, rows);

		try (ZipInputStream zis = new ZipInputStream(result.getInputStream())) {
			ZipEntry first = zis.getNextEntry();
			assertThat(first.getName()).endsWith(".csv").doesNotContain("\\").doesNotContain("/");
			assertThat(zis.getNextEntry().getName()).isEqualTo("reimbursements.csv");
		}
	}

	@Test
	@DisplayName("Generate grouped CSV uses export filename when period display name empty or whitespace-only")
	void testGenerateGroupedCsvSanitizesEmptyOrWhitespacePeriodNames() throws IOException {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<ExportFieldDefinition> fieldDefinitions = TimesheetFileGeneratorServiceTestDataFactory
			.createExportFieldDefinitionList();
		given(this.exportFieldRegistry.getFieldDefinitions(anyList())).willReturn(fieldDefinitions);

		for (String displayName : Arrays.asList(TimesheetFileGeneratorServiceTestDataFactory.createEmptyFilename(),
				TimesheetFileGeneratorServiceTestDataFactory.createWhitespaceFilename())) {
			PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
			period.setPeriodDisplayName(displayName);
			period.setTimesheetsInPeriod(new ArrayList<>());
			ByteArrayResource zip = this.timesheetFileGeneratorService.generateGroupedFile(List.of(period), request);
			try (ZipInputStream zis = new ZipInputStream(zip.getInputStream())) {
				assertThat(zis.getNextEntry().getName()).isEqualTo("export.csv");
			}
		}
	}

	@Test
	@DisplayName("CSV headers use column name when registry returns empty or throws for dynamic columns")
	void testGenerateFileCsvUsesColumnNameWhenRegistryReturnsEmptyOrThrows() {
		given(this.exportFieldRegistry.getDisplayName("throwsColumn")).willThrow(new IllegalStateException("missing"));
		given(this.exportFieldRegistry.getDisplayName("emptyDisplayColumn")).willReturn("");

		DynamicExportResponseBodyDto row = TimesheetFileGeneratorServiceTestDataFactory.createBasicExportResponse();
		List<String> order = new ArrayList<>();
		order.add("throwsColumn");
		order.add("emptyDisplayColumn");
		order.add(TimesheetFileGeneratorServiceTestDataFactory.TEST_FIELD_NAME);
		order.add("timesheet");
		row.getData().put("throwsColumn", "a");
		row.getData().put("emptyDisplayColumn", "b");
		row.setColumnOrder(order);

		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		ByteArrayResource result = this.timesheetFileGeneratorService.generateFile(List.of(row), request);
		String csv = new String(result.getByteArray());
		assertThat(csv).contains("throwsColumn").contains("emptyDisplayColumn");
	}

	// ----- IOException branches (CSV/ZIP -> FileGeneratorException; Excel -> POI
	// OpenXML4JRuntimeException) -----

	private static Answer<Object> answerThrowsIOException(String message) {
		return new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws IOException {
				throw new IOException(message);
			}
		};
	}

	/**
	 * POI {@link XSSFWorkbook} construction cannot be mocked reliably on modern JDKs
	 * ("Could not initialize mocked construction"). Stub {@link ByteArrayOutputStream} so
	 * {@code workbook.write(outputStream)} fails; Apache POI wraps the
	 * {@link IOException} in {@link OpenXML4JRuntimeException} (not
	 * {@link FileGeneratorException}).
	 */
	private static void stubByteArrayOutputStreamWriteToThrow(ByteArrayOutputStream stream) {
		willAnswer(TimesheetFileGeneratorServiceTests.answerThrowsIOException("simulated stream write failure"))
			.given(stream)
			.write(any(byte[].class), anyInt(), anyInt());
		willAnswer(TimesheetFileGeneratorServiceTests.answerThrowsIOException("simulated stream write failure"))
			.given(stream)
			.write(anyInt());
	}

	@Test
	@DisplayName("Generate CSV file wraps IOException in FileGeneratorException")
	void testGenerateFileCsvWrapsIOException() {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<DynamicExportResponseBodyDto> data = TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseList();

		try (MockedConstruction<CSVWriter> mocked = mockConstruction(CSVWriter.class, (mock, context) -> {
			willDoNothing().given(mock).writeNext(any());
			try {
				willAnswer(TimesheetFileGeneratorServiceTests.answerThrowsIOException("flush failed")).given(mock)
					.flush();
			}
			catch (IOException ex) {
				throw new AssertionError("Unreachable: Mockito stubbing does not invoke flush", ex);
			}
		})) {
			assertThatThrownBy(() -> this.timesheetFileGeneratorService.generateFile(data, request))
				.isInstanceOf(FileGeneratorException.class)
				.hasMessageContaining("Failed to generate CSV file")
				.hasCauseInstanceOf(IOException.class);
		}
	}

	@Test
	@DisplayName("Generate Excel file propagates OpenXML4JRuntimeException when stream write fails")
	void testGenerateFileExcelStreamFailureThrowsOpenXmlRuntimeException() {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<DynamicExportResponseBodyDto> data = TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseList();

		try (MockedConstruction<ByteArrayOutputStream> mocked = mockConstruction(ByteArrayOutputStream.class,
				(mock, context) -> TimesheetFileGeneratorServiceTests.stubByteArrayOutputStreamWriteToThrow(mock))) {
			assertThatThrownBy(() -> this.timesheetFileGeneratorService.generateFile(data, request))
				.isInstanceOf(OpenXML4JRuntimeException.class)
				.hasCauseInstanceOf(OpenXML4JException.class);
		}
	}

	@Test
	@DisplayName("Generate grouped CSV ZIP wraps IOException in FileGeneratorException")
	void testGenerateGroupedCsvZipWrapsIOException() {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = TimesheetFileGeneratorServiceTestDataFactory
			.createPeriodGroupedExportResponseList();

		try (MockedConstruction<ZipOutputStream> mocked = mockConstruction(ZipOutputStream.class, (mock, context) -> {
			try {
				willAnswer(TimesheetFileGeneratorServiceTests.answerThrowsIOException("zip entry")).given(mock)
					.putNextEntry(any(ZipEntry.class));
			}
			catch (IOException ex) {
				throw new AssertionError("Unreachable: Mockito stubbing does not invoke putNextEntry", ex);
			}
		})) {
			assertThatThrownBy(() -> this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request))
				.isInstanceOf(FileGeneratorException.class)
				.hasMessageContaining("Failed to generate grouped CSV ZIP file")
				.hasCauseInstanceOf(IOException.class);
		}
	}

	@Test
	@DisplayName("Generate grouped Excel propagates OpenXML4JRuntimeException when stream write fails")
	void testGenerateGroupedExcelStreamFailureThrowsOpenXmlRuntimeException() {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = TimesheetFileGeneratorServiceTestDataFactory
			.createPeriodGroupedExportResponseList();

		try (MockedConstruction<ByteArrayOutputStream> mocked = mockConstruction(ByteArrayOutputStream.class,
				(mock, context) -> TimesheetFileGeneratorServiceTests.stubByteArrayOutputStreamWriteToThrow(mock))) {
			assertThatThrownBy(() -> this.timesheetFileGeneratorService.generateGroupedFile(groupedData, request))
				.isInstanceOf(OpenXML4JRuntimeException.class)
				.hasCauseInstanceOf(OpenXML4JException.class);
		}
	}

	@Test
	@DisplayName("Generate Excel with reimbursements propagates OpenXML4JRuntimeException when stream write fails")
	void testGenerateExcelWithReimbursementsStreamFailureThrowsOpenXmlRuntimeException() {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<DynamicExportResponseBodyDto> data = TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseList();

		try (MockedConstruction<ByteArrayOutputStream> mocked = mockConstruction(ByteArrayOutputStream.class,
				(mock, context) -> TimesheetFileGeneratorServiceTests.stubByteArrayOutputStreamWriteToThrow(mock))) {
			assertThatThrownBy(
					() -> this.timesheetFileGeneratorService.generateExcelWithReimbursements(data, request, null))
				.isInstanceOf(OpenXML4JRuntimeException.class)
				.hasCauseInstanceOf(OpenXML4JException.class);
		}
	}

	@Test
	@DisplayName("Generate grouped Excel with reimbursements propagates OpenXML4JRuntimeException when stream write fails")
	void testGenerateGroupedExcelWithReimbursementsStreamFailureThrowsOpenXmlRuntimeException() {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		List<PeriodGroupedExportResponseBodyDto> groupedData = TimesheetFileGeneratorServiceTestDataFactory
			.createPeriodGroupedExportResponseList();

		try (MockedConstruction<ByteArrayOutputStream> mocked = mockConstruction(ByteArrayOutputStream.class,
				(mock, context) -> TimesheetFileGeneratorServiceTests.stubByteArrayOutputStreamWriteToThrow(mock))) {
			assertThatThrownBy(() -> this.timesheetFileGeneratorService
				.generateGroupedExcelWithReimbursements(groupedData, request, null))
				.isInstanceOf(OpenXML4JRuntimeException.class)
				.hasCauseInstanceOf(OpenXML4JException.class);
		}
	}

	@Test
	@DisplayName("Generate CSV with reimbursements ZIP wraps IOException in FileGeneratorException")
	void testGenerateCsvWithReimbursementsZipWrapsIOException() {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		List<DynamicExportResponseBodyDto> data = TimesheetFileGeneratorServiceTestDataFactory
			.createExportResponseList();

		try (MockedConstruction<ZipOutputStream> mocked = mockConstruction(ZipOutputStream.class, (mock, context) -> {
			try {
				willAnswer(TimesheetFileGeneratorServiceTests.answerThrowsIOException("zip")).given(mock)
					.putNextEntry(any(ZipEntry.class));
			}
			catch (IOException ex) {
				throw new AssertionError("Unreachable: Mockito stubbing does not invoke putNextEntry", ex);
			}
		})) {
			assertThatThrownBy(
					() -> this.timesheetFileGeneratorService.generateCsvWithReimbursementsZip(data, request, null))
				.isInstanceOf(FileGeneratorException.class)
				.hasMessageContaining("Failed to generate CSV+reimbursements ZIP")
				.hasCauseInstanceOf(IOException.class);
		}
	}

	@Test
	@DisplayName("Generate grouped CSV with reimbursements ZIP wraps IOException in FileGeneratorException")
	void testGenerateGroupedCsvWithReimbursementsZipWrapsIOException() {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createCsvExportRequest();
		PeriodGroupedExportResponseBodyDto period = new PeriodGroupedExportResponseBodyDto();
		period.setPeriodDisplayName(TimesheetFileGeneratorServiceTestDataFactory.TEST_PERIOD_DISPLAY_NAME);
		period.setTimesheetsInPeriod(List.of(TimesheetFileGeneratorServiceTestDataFactory.createBasicExportResponse()));
		List<PeriodGroupedExportResponseBodyDto> groupedPeriods = List.of(period);

		try (MockedConstruction<ZipOutputStream> mocked = mockConstruction(ZipOutputStream.class, (mock, context) -> {
			try {
				willAnswer(TimesheetFileGeneratorServiceTests.answerThrowsIOException("zip")).given(mock)
					.putNextEntry(any(ZipEntry.class));
			}
			catch (IOException ex) {
				throw new AssertionError("Unreachable: Mockito stubbing does not invoke putNextEntry", ex);
			}
		})) {
			assertThatThrownBy(() -> this.timesheetFileGeneratorService
				.generateGroupedCsvWithReimbursementsZip(groupedPeriods, request, null))
				.isInstanceOf(FileGeneratorException.class)
				.hasMessageContaining("Failed to generate grouped CSV+reimbursements ZIP")
				.hasCauseInstanceOf(IOException.class);
		}
	}

	@Test
	@DisplayName("Grouped Excel with reimbursements uses field definitions when period has no timesheets")
	void testGenerateGroupedExcelWithReimbursementsUsesFieldDefinitionsWhenPeriodEmpty() throws Exception {
		DynamicExportRequestBodyDto request = TimesheetFileGeneratorServiceTestDataFactory.createExcelExportRequest();
		PeriodGroupedExportResponseBodyDto period = TimesheetFileGeneratorServiceTestDataFactory
			.createPeriodGroupedExportResponseWithEmptyTimesheets();
		List<ExportFieldDefinition> fieldDefinitions = TimesheetFileGeneratorServiceTestDataFactory
			.createExportFieldDefinitionList();
		given(this.exportFieldRegistry.getFieldDefinitions(anyList())).willReturn(fieldDefinitions);

		ByteArrayResource result = this.timesheetFileGeneratorService
			.generateGroupedExcelWithReimbursements(List.of(period), request, null);

		assertThat(result.getByteArray()).isNotEmpty();
		then(this.exportFieldRegistry).should().getFieldDefinitions(anyList());
		try (Workbook workbook = new XSSFWorkbook(result.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);
			Row header = sheet.getRow(0);
			assertThat(header.getCell(0).getStringCellValue()).isEqualTo(fieldDefinitions.get(0).getDisplayName());
		}
	}

}