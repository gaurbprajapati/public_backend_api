package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.BasicAccessLevel;
import io.recruitcrm.microservice.timesheet.testdata.PlacementBillingTestDataFactory;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PlacementBilling Tests")
class PlacementBillingTests {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("No args constructor initializes empty access values and properties map")
	void testNoArgsConstructorInitializesExpectedDefaults() {
		// Given
		PlacementBilling placementBilling = new PlacementBilling();

		// When
		Map<String, Object> additionalProperties = placementBilling.getAdditionalProperties();

		// Then
		assertThat(placementBilling.getCanAdd()).isNull();
		assertThat(placementBilling.getCanEdit()).isNull();
		assertThat(placementBilling.getCanView()).isNull();
		assertThat(placementBilling.getCanDelete()).isNull();
		assertThat(additionalProperties).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Setters and getters update all permission values")
	void testSettersAndGettersUpdateAllPermissionValues() {
		// Given
		PlacementBilling placementBilling = PlacementBillingTestDataFactory.createPlacementBilling();

		// When
		String canAdd = placementBilling.getCanAdd();
		String canEdit = placementBilling.getCanEdit();
		String canView = placementBilling.getCanView();
		String canDelete = placementBilling.getCanDelete();

		// Then
		assertThat(canAdd).isEqualTo(PlacementBillingTestDataFactory.DEFAULT_CAN_ADD);
		assertThat(canEdit).isEqualTo(PlacementBillingTestDataFactory.DEFAULT_CAN_EDIT);
		assertThat(canView).isEqualTo(PlacementBillingTestDataFactory.DEFAULT_CAN_VIEW);
		assertThat(canDelete).isEqualTo(PlacementBillingTestDataFactory.DEFAULT_CAN_DELETE);
	}

	@Test
	@DisplayName("setAdditionalProperty stores entries in additionalProperties map")
	void testSetAdditionalPropertyStoresEntriesInAdditionalPropertiesMap() {
		// Given
		PlacementBilling placementBilling = new PlacementBilling();
		Map<String, Object> properties = PlacementBillingTestDataFactory.createAdditionalProperties();

		// When
		properties.forEach(placementBilling::setAdditionalProperty);

		// Then
		assertThat(placementBilling.getAdditionalProperties()).containsEntry("customFlag", Boolean.TRUE)
			.containsEntry("permissionGroup", "placement_billing");
	}

	@Test
	@DisplayName("getAdditionalProperties returns mutable map reference")
	void testGetAdditionalPropertiesReturnsMutableMapReference() {
		// Given
		PlacementBilling placementBilling = new PlacementBilling();

		// When
		Map<String, Object> additionalProperties = placementBilling.getAdditionalProperties();
		additionalProperties.put("dynamicScope", "enabled");

		// Then
		assertThat(placementBilling.getAdditionalProperties()).containsEntry("dynamicScope", "enabled");
	}

	@Test
	@DisplayName("PlacementBilling implements BasicAccessLevel")
	void testPlacementBillingImplementsBasicAccessLevel() {
		// Given
		PlacementBilling placementBilling = new PlacementBilling();

		// When
		boolean result = placementBilling instanceof BasicAccessLevel;

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Serialization includes json properties and additional properties")
	void testSerializationIncludesJsonAndAdditionalProperties() throws Exception {
		// Given
		PlacementBilling placementBilling = PlacementBillingTestDataFactory.createPlacementBilling("yes", "no", "yes",
				"no");
		placementBilling.setAdditionalProperty("customKey", "customValue");

		// When
		String json = this.objectMapper.writeValueAsString(placementBilling);
		JsonNode jsonNode = this.objectMapper.readTree(json);

		// Then
		assertThat(jsonNode.get("canadd").asText()).isEqualTo("yes");
		assertThat(jsonNode.get("canedit").asText()).isEqualTo("no");
		assertThat(jsonNode.get("canview").asText()).isEqualTo("yes");
		assertThat(jsonNode.get("candelete").asText()).isEqualTo("no");
		assertThat(jsonNode.get("customKey").asText()).isEqualTo("customValue");
	}

	@Test
	@DisplayName("Deserialization maps known and unknown properties correctly")
	void testDeserializationMapsKnownAndUnknownPropertiesCorrectly() throws Exception {
		// Given
		String json = """
				{
				  "canadd": "1",
				  "canedit": "0",
				  "canview": "1",
				  "candelete": "0",
				  "customScope": "billing"
				}
				""";

		// When
		PlacementBilling placementBilling = this.objectMapper.readValue(json, PlacementBilling.class);

		// Then
		assertThat(placementBilling.getCanAdd()).isEqualTo("1");
		assertThat(placementBilling.getCanEdit()).isEqualTo("0");
		assertThat(placementBilling.getCanView()).isEqualTo("1");
		assertThat(placementBilling.getCanDelete()).isEqualTo("0");
		assertThat(placementBilling.getAdditionalProperties()).containsEntry("customScope", "billing");
	}

}
