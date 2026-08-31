package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TimeLogWorkInterval DTO Tests")
class TimeLogWorkIntervalTests {

	@Test
	@DisplayName("No-args constructor creates empty instance")
	void testNoArgsConstructor() {
		TimeLogWorkInterval interval = new TimeLogWorkInterval();

		assertThat(interval.getId()).isNull();
		assertThat(interval.getTimeLogId()).isNull();
		assertThat(interval.getTimeLog()).isNull();
		assertThat(interval.getWorkStartTime()).isNull();
		assertThat(interval.getWorkEndTime()).isNull();
		assertThat(interval.getWorkTime()).isNull();
		assertThat(interval.getNormalizedWorkStartTime()).isNull();
		assertThat(interval.getNormalizedWorkEndTime()).isNull();
		assertThat(interval.getRangeBasedRemark()).isNull();
	}

	@Test
	@DisplayName("All-args constructor sets all fields")
	void testAllArgsConstructor() {
		TimeLog timeLog = new TimeLog();
		LocalTime start = LocalTime.of(9, 0);
		LocalTime end = LocalTime.of(12, 0);
		Duration workTime = Duration.ofHours(3);

		TimeLogWorkInterval interval = new TimeLogWorkInterval(Integer.valueOf(1), Integer.valueOf(100), timeLog, start,
				end, workTime, start, end, "Morning shift");

		assertThat(interval.getId()).isEqualTo(Integer.valueOf(1));
		assertThat(interval.getTimeLogId()).isEqualTo(Integer.valueOf(100));
		assertThat(interval.getTimeLog()).isSameAs(timeLog);
		assertThat(interval.getWorkStartTime()).isEqualTo(start);
		assertThat(interval.getWorkEndTime()).isEqualTo(end);
		assertThat(interval.getWorkTime()).isEqualTo(workTime);
		assertThat(interval.getNormalizedWorkStartTime()).isEqualTo(start);
		assertThat(interval.getNormalizedWorkEndTime()).isEqualTo(end);
		assertThat(interval.getRangeBasedRemark()).isEqualTo("Morning shift");
	}

	@Test
	@DisplayName("Builder creates instance with specified fields")
	void testBuilder() {
		TimeLogWorkInterval interval = TimeLogWorkInterval.builder()
			.id(Integer.valueOf(2))
			.timeLogId(Integer.valueOf(200))
			.workStartTime(LocalTime.of(13, 0))
			.workEndTime(LocalTime.of(17, 0))
			.workTime(Duration.ofHours(4))
			.rangeBasedRemark("Afternoon shift")
			.build();

		assertThat(interval.getId()).isEqualTo(Integer.valueOf(2));
		assertThat(interval.getTimeLogId()).isEqualTo(Integer.valueOf(200));
		assertThat(interval.getWorkStartTime()).isEqualTo(LocalTime.of(13, 0));
		assertThat(interval.getWorkEndTime()).isEqualTo(LocalTime.of(17, 0));
		assertThat(interval.getWorkTime()).isEqualTo(Duration.ofHours(4));
		assertThat(interval.getRangeBasedRemark()).isEqualTo("Afternoon shift");
	}

	@Test
	@DisplayName("Setters update fields correctly")
	void testSetters() {
		TimeLogWorkInterval interval = new TimeLogWorkInterval();

		interval.setId(Integer.valueOf(3));
		interval.setTimeLogId(Integer.valueOf(300));
		interval.setWorkStartTime(LocalTime.of(5, 0));
		interval.setWorkEndTime(LocalTime.of(7, 0));
		interval.setWorkTime(Duration.ofHours(2));
		interval.setNormalizedWorkStartTime(LocalTime.of(5, 0));
		interval.setNormalizedWorkEndTime(LocalTime.of(7, 0));
		interval.setRangeBasedRemark("Early shift");

		assertThat(interval.getId()).isEqualTo(Integer.valueOf(3));
		assertThat(interval.getTimeLogId()).isEqualTo(Integer.valueOf(300));
		assertThat(interval.getWorkStartTime()).isEqualTo(LocalTime.of(5, 0));
		assertThat(interval.getWorkEndTime()).isEqualTo(LocalTime.of(7, 0));
		assertThat(interval.getWorkTime()).isEqualTo(Duration.ofHours(2));
		assertThat(interval.getNormalizedWorkStartTime()).isEqualTo(LocalTime.of(5, 0));
		assertThat(interval.getNormalizedWorkEndTime()).isEqualTo(LocalTime.of(7, 0));
		assertThat(interval.getRangeBasedRemark()).isEqualTo("Early shift");
	}

	@Test
	@DisplayName("Equals and hashCode work correctly")
	void testEqualsAndHashCode() {
		TimeLogWorkInterval interval1 = TimeLogWorkInterval.builder()
			.id(Integer.valueOf(1))
			.timeLogId(Integer.valueOf(100))
			.workStartTime(LocalTime.of(9, 0))
			.workEndTime(LocalTime.of(12, 0))
			.build();

		TimeLogWorkInterval interval2 = TimeLogWorkInterval.builder()
			.id(Integer.valueOf(1))
			.timeLogId(Integer.valueOf(100))
			.workStartTime(LocalTime.of(9, 0))
			.workEndTime(LocalTime.of(12, 0))
			.build();

		assertThat(interval1).isEqualTo(interval2).hasSameHashCodeAs(interval2);
	}

	@Test
	@DisplayName("ToString contains field values")
	void testToString() {
		TimeLogWorkInterval interval = TimeLogWorkInterval.builder()
			.id(Integer.valueOf(1))
			.timeLogId(Integer.valueOf(100))
			.build();

		String result = interval.toString();

		assertThat(result).contains("id=1").contains("timeLogId=100");
	}

}
