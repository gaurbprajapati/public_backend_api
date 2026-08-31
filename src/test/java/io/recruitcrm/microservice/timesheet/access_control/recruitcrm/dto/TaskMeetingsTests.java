package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.BasicAccessLevel;
import io.recruitcrm.microservice.timesheet.testdata.TaskMeetingsTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TaskMeetings Tests")
class TaskMeetingsTests {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("No args constructor initializes empty access values and properties map")
	void testNoArgsConstructorInitializesExpectedDefaults() {
		// Given
		TaskMeetings taskMeetings = new TaskMeetings();

		// When
		Map<String, Object> additionalProperties = taskMeetings.getAdditionalProperties();

		// Then
		assertThat(taskMeetings.getCanAdd()).isNull();
		assertThat(taskMeetings.getCanEdit()).isNull();
		assertThat(taskMeetings.getCanView()).isNull();
		assertThat(taskMeetings.getCanDelete()).isNull();
		assertThat(additionalProperties).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Setters and getters update all permission values")
	void testSettersAndGettersUpdateAllPermissionValues() {
		// Given
		TaskMeetings taskMeetings = TaskMeetingsTestDataFactory.createTaskMeetings();

		// When
		String canAdd = taskMeetings.getCanAdd();
		String canEdit = taskMeetings.getCanEdit();
		String canView = taskMeetings.getCanView();
		String canDelete = taskMeetings.getCanDelete();

		// Then
		assertThat(canAdd).isEqualTo(TaskMeetingsTestDataFactory.DEFAULT_CAN_ADD);
		assertThat(canEdit).isEqualTo(TaskMeetingsTestDataFactory.DEFAULT_CAN_EDIT);
		assertThat(canView).isEqualTo(TaskMeetingsTestDataFactory.DEFAULT_CAN_VIEW);
		assertThat(canDelete).isEqualTo(TaskMeetingsTestDataFactory.DEFAULT_CAN_DELETE);
	}

	@Test
	@DisplayName("setAdditionalProperty stores entries in additionalProperties map")
	void testSetAdditionalPropertyStoresEntriesInAdditionalPropertiesMap() {
		// Given
		TaskMeetings taskMeetings = new TaskMeetings();
		Map<String, Object> properties = TaskMeetingsTestDataFactory.createAdditionalProperties();

		// When
		properties.forEach(taskMeetings::setAdditionalProperty);

		// Then
		assertThat(taskMeetings.getAdditionalProperties()).containsEntry("customFlag", Boolean.TRUE)
			.containsEntry("permissionGroup", "task_meetings");
	}

	@Test
	@DisplayName("getAdditionalProperties returns mutable map reference")
	void testGetAdditionalPropertiesReturnsMutableMapReference() {
		// Given
		TaskMeetings taskMeetings = new TaskMeetings();

		// When
		Map<String, Object> additionalProperties = taskMeetings.getAdditionalProperties();
		additionalProperties.put("dynamicScope", "enabled");

		// Then
		assertThat(taskMeetings.getAdditionalProperties()).containsEntry("dynamicScope", "enabled");
	}

	@Test
	@DisplayName("TaskMeetings implements BasicAccessLevel")
	void testTaskMeetingsImplementsBasicAccessLevel() {
		// Given
		TaskMeetings taskMeetings = new TaskMeetings();

		// When
		boolean result = taskMeetings instanceof BasicAccessLevel;

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Serialization includes json properties and additional properties")
	void testSerializationIncludesJsonAndAdditionalProperties() throws Exception {
		// Given
		TaskMeetings taskMeetings = TaskMeetingsTestDataFactory.createTaskMeetings("yes", "no", "yes", "no");
		taskMeetings.setAdditionalProperty("customKey", "customValue");

		// When
		String json = this.objectMapper.writeValueAsString(taskMeetings);
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
				  "customScope": "taskmeeting"
				}
				""";

		// When
		TaskMeetings taskMeetings = this.objectMapper.readValue(json, TaskMeetings.class);

		// Then
		assertThat(taskMeetings.getCanAdd()).isEqualTo("1");
		assertThat(taskMeetings.getCanEdit()).isEqualTo("0");
		assertThat(taskMeetings.getCanView()).isEqualTo("1");
		assertThat(taskMeetings.getCanDelete()).isEqualTo("0");
		assertThat(taskMeetings.getAdditionalProperties()).containsEntry("customScope", "taskmeeting");
	}

}
