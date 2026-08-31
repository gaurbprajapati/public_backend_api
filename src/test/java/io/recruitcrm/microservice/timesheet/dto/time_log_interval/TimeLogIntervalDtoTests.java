package io.recruitcrm.microservice.timesheet.dto.time_log_interval;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TimeLogIntervalDto Tests")
class TimeLogIntervalDtoTests {

	@Test
	@DisplayName("No-args constructor creates empty instance")
	void testNoArgsConstructor() {
		TimeLogIntervalDto dto = new TimeLogIntervalDto();

		assertThat(dto.getId()).isNull();
		assertThat(dto.getTimeLogId()).isNull();
		assertThat(dto.getWorkStartTime()).isNull();
		assertThat(dto.getWorkEndTime()).isNull();
		assertThat(dto.getRangeBasedRemark()).isNull();
		assertThat(dto.getBreakInterval()).isNull();
	}

	@Test
	@DisplayName("All-args constructor sets all fields")
	void testAllArgsConstructor() {
		TimeLogIntervalDto dto = new TimeLogIntervalDto(Integer.valueOf(1), Integer.valueOf(100),
				Integer.valueOf(32400), Integer.valueOf(43200), "Morning shift",
				"[{\"id\": 1, \"breakStartTime\": 36000, \"breakEndTime\": 37800}]");

		assertThat(dto.getId()).isEqualTo(Integer.valueOf(1));
		assertThat(dto.getTimeLogId()).isEqualTo(Integer.valueOf(100));
		assertThat(dto.getWorkStartTime()).isEqualTo(Integer.valueOf(32400));
		assertThat(dto.getWorkEndTime()).isEqualTo(Integer.valueOf(43200));
		assertThat(dto.getRangeBasedRemark()).isEqualTo("Morning shift");
		assertThat(dto.getBreakInterval()).contains("breakStartTime");
	}

	@Test
	@DisplayName("Builder creates instance with specified fields")
	void testBuilder() {
		TimeLogIntervalDto dto = TimeLogIntervalDto.builder()
			.id(2)
			.timeLogId(200)
			.workStartTime(46800)
			.workEndTime(61200)
			.rangeBasedRemark("Afternoon")
			.breakInterval(null)
			.build();

		assertThat(dto.getId()).isEqualTo(Integer.valueOf(2));
		assertThat(dto.getTimeLogId()).isEqualTo(Integer.valueOf(200));
		assertThat(dto.getWorkStartTime()).isEqualTo(Integer.valueOf(46800));
		assertThat(dto.getWorkEndTime()).isEqualTo(Integer.valueOf(61200));
		assertThat(dto.getRangeBasedRemark()).isEqualTo("Afternoon");
		assertThat(dto.getBreakInterval()).isNull();
	}

	@Test
	@DisplayName("Setters update fields correctly")
	void testSetters() {
		TimeLogIntervalDto dto = new TimeLogIntervalDto();

		dto.setId(Integer.valueOf(3));
		dto.setTimeLogId(Integer.valueOf(300));
		dto.setWorkStartTime(Integer.valueOf(18000));
		dto.setWorkEndTime(Integer.valueOf(25200));
		dto.setRangeBasedRemark("Early shift");
		dto.setBreakInterval("[]");

		assertThat(dto.getId()).isEqualTo(Integer.valueOf(3));
		assertThat(dto.getTimeLogId()).isEqualTo(Integer.valueOf(300));
		assertThat(dto.getWorkStartTime()).isEqualTo(Integer.valueOf(18000));
		assertThat(dto.getWorkEndTime()).isEqualTo(Integer.valueOf(25200));
		assertThat(dto.getRangeBasedRemark()).isEqualTo("Early shift");
		assertThat(dto.getBreakInterval()).isEqualTo("[]");
	}

	@Test
	@DisplayName("Equals and hashCode work correctly")
	void testEqualsAndHashCode() {
		TimeLogIntervalDto dto1 = TimeLogIntervalDto.builder()
			.id(Integer.valueOf(1))
			.timeLogId(Integer.valueOf(100))
			.workStartTime(Integer.valueOf(32400))
			.workEndTime(Integer.valueOf(43200))
			.build();

		TimeLogIntervalDto dto2 = TimeLogIntervalDto.builder()
			.id(Integer.valueOf(1))
			.timeLogId(Integer.valueOf(100))
			.workStartTime(Integer.valueOf(32400))
			.workEndTime(Integer.valueOf(43200))
			.build();

		assertThat(dto1).isEqualTo(dto2).hasSameHashCodeAs(dto2);
	}

	@Test
	@DisplayName("ToString contains field values")
	void testToString() {
		TimeLogIntervalDto dto = TimeLogIntervalDto.builder()
			.id(Integer.valueOf(1))
			.timeLogId(Integer.valueOf(100))
			.build();

		String result = dto.toString();

		assertThat(result).contains("id=1").contains("timeLogId=100");
	}

}
