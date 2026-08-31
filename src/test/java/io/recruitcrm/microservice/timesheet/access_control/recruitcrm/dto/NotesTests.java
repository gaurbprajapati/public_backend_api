package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Notes Tests")
class NotesTests {

	@Test
	@DisplayName("getAdditionalProperties should return the backing map populated by setAdditionalProperty")
	void testAdditionalPropertiesGetterAndSetter() {
		// Given
		Notes notes = new Notes();

		// When
		notes.setAdditionalProperty("key", "value");

		// Then
		assertThat(notes.getAdditionalProperties()).containsEntry("key", "value").hasSize(1);
	}

	@Test
	@DisplayName("standard property getters and setters should round-trip values")
	void testStandardPropertiesGetterAndSetter() {
		// Given
		Notes notes = new Notes();

		// When
		notes.setCanAdd("1");
		notes.setCanEdit("1");
		notes.setCanView("1");
		notes.setCanDelete("0");

		// Then
		assertThat(notes.getCanAdd()).isEqualTo("1");
		assertThat(notes.getCanEdit()).isEqualTo("1");
		assertThat(notes.getCanView()).isEqualTo("1");
		assertThat(notes.getCanDelete()).isEqualTo("0");
	}

}
