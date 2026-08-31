package io.recruitcrm.microservice.timesheet.services.locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.recruitcrm.microservice.timesheet.dto.entity_columns.AccountViewColumnResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.ColumnAccountViewResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LabelResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto;

@DisplayName("LocaleService Tests")
class LocaleServiceTests {

	private LocaleService localeService;

	@BeforeEach
	void setUp() {
		this.localeService = new LocaleService();
	}

	@Test
	@DisplayName("Merge should do nothing when entity columns are null")
	void testMergeNullEntityColumnsDoesNothing() {
		// Given
		LocaleResponseBodyDto localeLabels = new LocaleResponseBodyDto(new HashMap<>());

		// When and Then
		assertThatCode(() -> this.localeService.mergeLocaleLabelsIntoEntityColumns(null, localeLabels))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("Merge should do nothing when locale labels are null")
	void testMergeNullLocaleLabelsDoesNothing() {
		// Given
		AccountViewColumnResponseBodyDto entityColumns = new AccountViewColumnResponseBodyDto();

		// When and Then
		assertThatCode(() -> this.localeService.mergeLocaleLabelsIntoEntityColumns(entityColumns, null))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("Merge should do nothing when timesheet locale map is null")
	void testMergeNullTimesheetMapDoesNothing() {
		// Given
		AccountViewColumnResponseBodyDto entityColumns = new AccountViewColumnResponseBodyDto();
		ColumnAccountViewResponseBodyDto column = new ColumnAccountViewResponseBodyDto();
		column.setLabel("original");
		entityColumns.put("hours", column);
		LocaleResponseBodyDto localeLabels = new LocaleResponseBodyDto(null);

		// When
		this.localeService.mergeLocaleLabelsIntoEntityColumns(entityColumns, localeLabels);

		// Then
		assertThat(column.getLabel()).isEqualTo("original");
	}

	@Test
	@DisplayName("Merge should override label and long label when locale values are present")
	void testMergeOverridesLabelsWhenLocaleValuesPresent() {
		// Given
		AccountViewColumnResponseBodyDto entityColumns = new AccountViewColumnResponseBodyDto();
		ColumnAccountViewResponseBodyDto column = new ColumnAccountViewResponseBodyDto();
		column.setLabel("original");
		column.setLonglabel("originalLong");
		entityColumns.put("hours", column);

		Map<String, LabelResponseBodyDto> localeValues = new HashMap<>();
		localeValues.put("hours", new LabelResponseBodyDto("Heures", "Heures travaillees"));
		LocaleResponseBodyDto localeLabels = new LocaleResponseBodyDto(localeValues);

		// When
		this.localeService.mergeLocaleLabelsIntoEntityColumns(entityColumns, localeLabels);

		// Then
		assertThat(column.getLabel()).isEqualTo("Heures");
		assertThat(column.getLonglabel()).isEqualTo("Heures travaillees");
	}

	@Test
	@DisplayName("Merge should keep original values when label entry is missing or its fields are null")
	void testMergeKeepsOriginalWhenLabelMissingOrNull() {
		// Given
		AccountViewColumnResponseBodyDto entityColumns = new AccountViewColumnResponseBodyDto();
		ColumnAccountViewResponseBodyDto missingColumn = new ColumnAccountViewResponseBodyDto();
		missingColumn.setLabel("missingOriginal");
		entityColumns.put("missing", missingColumn);

		ColumnAccountViewResponseBodyDto nullFieldsColumn = new ColumnAccountViewResponseBodyDto();
		nullFieldsColumn.setLabel("nullOriginal");
		nullFieldsColumn.setLonglabel("nullOriginalLong");
		entityColumns.put("present", nullFieldsColumn);

		Map<String, LabelResponseBodyDto> localeValues = new HashMap<>();
		localeValues.put("present", new LabelResponseBodyDto(null, null));
		LocaleResponseBodyDto localeLabels = new LocaleResponseBodyDto(localeValues);

		// When
		this.localeService.mergeLocaleLabelsIntoEntityColumns(entityColumns, localeLabels);

		// Then
		assertThat(missingColumn.getLabel()).isEqualTo("missingOriginal");
		assertThat(nullFieldsColumn.getLabel()).isEqualTo("nullOriginal");
		assertThat(nullFieldsColumn.getLonglabel()).isEqualTo("nullOriginalLong");
	}

}
