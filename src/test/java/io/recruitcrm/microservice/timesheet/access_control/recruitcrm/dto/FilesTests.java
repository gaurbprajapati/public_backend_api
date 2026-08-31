package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Files Tests")
class FilesTests {

	@Test
	@DisplayName("getAdditionalProperties should return the backing map populated by setAdditionalProperty")
	void testAdditionalPropertiesGetterAndSetter() {
		// Given
		Files files = new Files();

		// When
		files.setAdditionalProperty("key", "value");

		// Then
		assertThat(files.getAdditionalProperties()).containsEntry("key", "value").hasSize(1);
	}

	@Test
	@DisplayName("standard property getters and setters should round-trip values")
	void testStandardPropertiesGetterAndSetter() {
		// Given
		Files files = new Files();

		// When
		files.setCanAdd("1");
		files.setCanEdit("1");
		files.setCanView("1");
		files.setCanDelete("0");

		// Then
		assertThat(files.getCanAdd()).isEqualTo("1");
		assertThat(files.getCanEdit()).isEqualTo("1");
		assertThat(files.getCanView()).isEqualTo("1");
		assertThat(files.getCanDelete()).isEqualTo("0");
	}

}
