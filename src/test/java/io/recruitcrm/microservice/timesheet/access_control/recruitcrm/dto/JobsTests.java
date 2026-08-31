package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Jobs Tests")
class JobsTests {

	@Test
	@DisplayName("getAdditionalProperties should return the backing map populated by setAdditionalProperty")
	void testAdditionalPropertiesGetterAndSetter() {
		// Given
		Jobs jobs = new Jobs();

		// When
		jobs.setAdditionalProperty("key", "value");

		// Then
		assertThat(jobs.getAdditionalProperties()).containsEntry("key", "value").hasSize(1);
	}

	@Test
	@DisplayName("standard property getters and setters should round-trip values")
	void testStandardPropertiesGetterAndSetter() {
		// Given
		Jobs jobs = new Jobs();

		// When
		jobs.setCanAdd("1");
		jobs.setCanEdit("1");
		jobs.setCanView("1");
		jobs.setCanDelete("0");
		jobs.setOwnerChange("1");
		jobs.setFileAccess("0");

		// Then
		assertThat(jobs.getCanAdd()).isEqualTo("1");
		assertThat(jobs.getCanEdit()).isEqualTo("1");
		assertThat(jobs.getCanView()).isEqualTo("1");
		assertThat(jobs.getCanDelete()).isEqualTo("0");
		assertThat(jobs.getOwnerChange()).isEqualTo("1");
		assertThat(jobs.getFileAccess()).isEqualTo("0");
	}

}
