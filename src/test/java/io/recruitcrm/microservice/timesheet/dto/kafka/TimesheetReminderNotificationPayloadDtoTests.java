package io.recruitcrm.microservice.timesheet.dto.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DisplayName("TimesheetReminderNotificationPayloadDto Tests")
class TimesheetReminderNotificationPayloadDtoTests {

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		this.objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("Backward-compatible constructor should default reimbursementIds to null")
	void testBackwardCompatibleConstructorDefaultsReimbursementIdsToNull() {
		TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				UUID.randomUUID().toString(), new ArrayList<>(List.of(1)),
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_CREATED, 10, 2, 3,
				ReminderNotificationEventType.REALTIME, false, true, true, null, null);

		assertThat(payload.reimbursementIds()).isNull();
	}

	@Test
	@DisplayName("Full constructor should accept reimbursementIds")
	void testFullConstructorAcceptsReimbursementIds() {
		TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				UUID.randomUUID().toString(), new ArrayList<>(List.of(1)),
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_REIMBURSEMENT_SUBMITTED, 10, 2, 3,
				ReminderNotificationEventType.REALTIME, true, false, true, "Performer", null,
				new ArrayList<>(List.of(55)));

		assertThat(payload.reimbursementIds()).containsExactly(55);
	}

	@Test
	@DisplayName("Serialization should omit reimbursementIds when null")
	void testSerializationOmitsReimbursementIdsWhenNull() throws Exception {
		TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				UUID.randomUUID().toString(), new ArrayList<>(List.of(1)),
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_CREATED, 10, 2, 3,
				ReminderNotificationEventType.REALTIME, false, true, true, null, null);

		JsonNode json = this.objectMapper.readTree(this.objectMapper.writeValueAsString(payload));

		assertThat(json.has("reimbursementIds")).isFalse();
	}

	@Test
	@DisplayName("Serialization should include reimbursementIds when provided")
	void testSerializationIncludesReimbursementIdsWhenProvided() throws Exception {
		TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				UUID.randomUUID().toString(), new ArrayList<>(List.of(1)),
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_REIMBURSEMENT_SUBMITTED, 10, 2, 3,
				ReminderNotificationEventType.REALTIME, true, false, true, "Performer", null,
				new ArrayList<>(List.of(55, 56)));

		JsonNode json = this.objectMapper.readTree(this.objectMapper.writeValueAsString(payload));

		assertThat(json.get("reimbursementIds").isArray()).isTrue();
		assertThat(json.get("reimbursementIds")).hasSize(2);
		assertThat(json.get("reimbursementIds").get(0).asInt()).isEqualTo(55);
		assertThat(json.get("reimbursementIds").get(1).asInt()).isEqualTo(56);
	}

	@Test
	@DisplayName("Deserialization should tolerate missing reimbursementIds")
	void testDeserializationToleratesMissingReimbursementIds() throws Exception {
		String json = """
				{
				  "eventId": "%s",
				  "timesheetIds": [1],
				  "eventName": "timesheet.created",
				  "accountId": 10,
				  "createdByUserTypeId": 2,
				  "reminderForUserTypeId": 3,
				  "eventType": "timesheet.realtime",
				  "sendInappNotification": false,
				  "sendEmailNotification": true,
				  "sendPortalNotification": true
				}
				""".formatted(UUID.randomUUID());

		TimesheetReminderNotificationPayloadDto payload = this.objectMapper.readValue(json,
				TimesheetReminderNotificationPayloadDto.class);

		assertThat(payload.reimbursementIds()).isNull();
	}

	@Test
	@DisplayName("Validation should reject empty reimbursementIds when provided")
	void testValidationRejectsEmptyReimbursementIdsWhenProvided() {
		String eventId = UUID.randomUUID().toString();
		ArrayList<Integer> timesheetIds = new ArrayList<>(List.of(1));
		ArrayList<Integer> emptyReimbursementIds = new ArrayList<>();
		assertThatThrownBy(() -> new TimesheetReminderNotificationPayloadDto(eventId, timesheetIds,
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_REIMBURSEMENT_SUBMITTED, 10, 2, 3,
				ReminderNotificationEventType.REALTIME, true, false, true, "Performer", null, emptyReimbursementIds))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("reimbursementIds must not be empty when provided");
	}

	@Test
	@DisplayName("Validation should allow null reimbursementIds for timesheet events")
	void testValidationAllowsNullReimbursementIdsForTimesheetEvents() {
		assertThatCode(() -> new TimesheetReminderNotificationPayloadDto(UUID.randomUUID().toString(),
				new ArrayList<>(List.of(1)), TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_CREATED, 10,
				2, 3, ReminderNotificationEventType.REALTIME, false, true, true, null, null))
			.doesNotThrowAnyException();
	}

}
