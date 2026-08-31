package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Deals Tests")
class DealsTests {

	@Test
	@DisplayName("getAdditionalProperties should return the backing map populated by setAdditionalProperty")
	void testAdditionalPropertiesGetterAndSetter() {
		// Given
		Deals deals = new Deals();

		// When
		deals.setAdditionalProperty("key", "value");

		// Then
		assertThat(deals.getAdditionalProperties()).containsEntry("key", "value").hasSize(1);
	}

	@Test
	@DisplayName("standard property getters and setters should round-trip values")
	void testStandardPropertiesGetterAndSetter() {
		// Given
		Deals deals = new Deals();

		// When
		deals.setCanAdd("1");
		deals.setCanEdit("1");
		deals.setCanView("1");
		deals.setCanDelete("0");
		deals.setOwnerChange("1");
		deals.setFileAccess("0");

		// Then
		assertThat(deals.getCanAdd()).isEqualTo("1");
		assertThat(deals.getCanEdit()).isEqualTo("1");
		assertThat(deals.getCanView()).isEqualTo("1");
		assertThat(deals.getCanDelete()).isEqualTo("0");
		assertThat(deals.getOwnerChange()).isEqualTo("1");
		assertThat(deals.getFileAccess()).isEqualTo("0");
	}

}
