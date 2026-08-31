package io.recruitcrm.microservice.timesheet.dto.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@DisplayName("ReminderNotificationEventType Tests")
class ReminderNotificationEventTypeTests {

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		this.objectMapper = new ObjectMapper();
	}

	@ParameterizedTest
	@MethodSource("displayNameProvider")
	@DisplayName("getDisplayName should return configured value for each enum constant")
	void testGetDisplayNameReturnsConfiguredValue(ReminderNotificationEventType eventType, String expectedDisplayName) {
		// When
		String displayName = eventType.getDisplayName();

		// Then
		assertThat(displayName).isEqualTo(expectedDisplayName);
	}

	private static Stream<Arguments> displayNameProvider() {
		return Stream.of(Arguments.of(ReminderNotificationEventType.REALTIME, "timesheet.realtime"),
				Arguments.of(ReminderNotificationEventType.CRON, "timesheet.cron"));
	}

	@Test
	@DisplayName("REALTIME should serialize to timesheet.realtime via JsonValue")
	void testRealtimeSerializesToDisplayName() throws Exception {
		// When
		String serialized = this.objectMapper.writeValueAsString(ReminderNotificationEventType.REALTIME);

		// Then
		assertThat(serialized).isEqualTo("\"timesheet.realtime\"");
	}

	@Test
	@DisplayName("CRON should serialize to timesheet.cron via JsonValue")
	void testCronSerializesToDisplayName() throws Exception {
		// When
		String serialized = this.objectMapper.writeValueAsString(ReminderNotificationEventType.CRON);

		// Then
		assertThat(serialized).isEqualTo("\"timesheet.cron\"");
	}

	@ParameterizedTest
	@EnumSource(ReminderNotificationEventType.class)
	@DisplayName("valueOf should resolve each enum constant by name")
	void testValueOfReturnsMatchingConstant(ReminderNotificationEventType eventType) {
		// When
		ReminderNotificationEventType resolved = ReminderNotificationEventType.valueOf(eventType.name());

		// Then
		assertThat(resolved).isEqualTo(eventType);
	}

	@Test
	@DisplayName("name should return enum constant identifiers")
	void testNameReturnsConstantIdentifiers() {
		// Then
		assertThat(ReminderNotificationEventType.REALTIME.name()).isEqualTo("REALTIME");
		assertThat(ReminderNotificationEventType.CRON.name()).isEqualTo("CRON");
	}

	@Test
	@DisplayName("ordinal should distinguish REALTIME and CRON order")
	void testOrdinalReturnsExpectedOrder() {
		// Then
		assertThat(ReminderNotificationEventType.REALTIME.ordinal()).isZero();
		assertThat(ReminderNotificationEventType.CRON.ordinal()).isEqualTo(1);
	}

	@Test
	@DisplayName("values should include REALTIME and CRON constants")
	void testValuesContainsAllConstants() {
		// When
		ReminderNotificationEventType[] values = ReminderNotificationEventType.values();

		// Then
		assertThat(values).containsExactly(ReminderNotificationEventType.REALTIME, ReminderNotificationEventType.CRON);
	}

}
