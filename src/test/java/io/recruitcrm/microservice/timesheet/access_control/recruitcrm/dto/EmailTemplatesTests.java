package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailTemplates Tests")
class EmailTemplatesTests {

	@Test
	@DisplayName("getAdditionalProperties should return the backing map populated by setAdditionalProperty")
	void testAdditionalPropertiesGetterAndSetter() {
		// Given
		EmailTemplates emailTemplates = new EmailTemplates();

		// When
		emailTemplates.setAdditionalProperty("key", "value");

		// Then
		assertThat(emailTemplates.getAdditionalProperties()).containsEntry("key", "value").hasSize(1);
	}

	@Test
	@DisplayName("standard property getters and setters should round-trip values")
	void testStandardPropertiesGetterAndSetter() {
		// Given
		EmailTemplates emailTemplates = new EmailTemplates();

		// When
		emailTemplates.setCanAdd("1");
		emailTemplates.setCanEdit("1");
		emailTemplates.setCanView("1");
		emailTemplates.setCanDelete("0");

		// Then
		assertThat(emailTemplates.getCanAdd()).isEqualTo("1");
		assertThat(emailTemplates.getCanEdit()).isEqualTo("1");
		assertThat(emailTemplates.getCanView()).isEqualTo("1");
		assertThat(emailTemplates.getCanDelete()).isEqualTo("0");
	}

}
