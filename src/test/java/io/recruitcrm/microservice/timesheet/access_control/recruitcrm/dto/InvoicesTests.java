package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Invoices Tests")
class InvoicesTests {

	@Test
	@DisplayName("getAdditionalProperties should return the backing map populated by setAdditionalProperty")
	void testAdditionalPropertiesGetterAndSetter() {
		// Given
		Invoices invoices = new Invoices();

		// When
		invoices.setAdditionalProperty("key", "value");

		// Then
		assertThat(invoices.getAdditionalProperties()).containsEntry("key", "value").hasSize(1);
	}

	@Test
	@DisplayName("standard property getters and setters should round-trip values")
	void testStandardPropertiesGetterAndSetter() {
		// Given
		Invoices invoices = new Invoices();

		// When
		invoices.setCanAdd("1");
		invoices.setCanEdit("1");
		invoices.setCanView("1");
		invoices.setCanDelete("0");

		// Then
		assertThat(invoices.getCanAdd()).isEqualTo("1");
		assertThat(invoices.getCanEdit()).isEqualTo("1");
		assertThat(invoices.getCanView()).isEqualTo("1");
		assertThat(invoices.getCanDelete()).isEqualTo("0");
	}

}
