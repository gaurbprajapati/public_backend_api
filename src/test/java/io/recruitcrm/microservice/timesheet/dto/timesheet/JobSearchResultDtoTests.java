package io.recruitcrm.microservice.timesheet.dto.timesheet;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JobSearchResultDto Tests")
class JobSearchResultDtoTests {

	private static final Integer JOB_ID = Integer.valueOf(301);

	private static final String JOB_NAME = "Senior Java Developer";

	private static final String JOB_SLUG = "senior-java-developer";

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		this.objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("Record constructor exposes all components")
	void testRecordConstructorExposesAllComponents() {
		// Given
		JobSearchResultDto dto = new JobSearchResultDto(JOB_ID, JOB_NAME, JOB_SLUG);

		// Then
		assertThat(dto.id()).isEqualTo(JOB_ID);
		assertThat(dto.name()).isEqualTo(JOB_NAME);
		assertThat(dto.slug()).isEqualTo(JOB_SLUG);
	}

	@Test
	@DisplayName("Record supports value equality and consistent hash code")
	void testRecordEqualityAndHashCode() {
		// Given
		JobSearchResultDto left = new JobSearchResultDto(JOB_ID, JOB_NAME, JOB_SLUG);
		JobSearchResultDto right = new JobSearchResultDto(JOB_ID, JOB_NAME, JOB_SLUG);

		// Then
		assertThat(left).isEqualTo(right).hasSameHashCodeAs(right);
	}

	@Test
	@DisplayName("Record is not equal when component values differ")
	void testRecordInequalityWhenComponentsDiffer() {
		// Given
		JobSearchResultDto baseline = new JobSearchResultDto(JOB_ID, JOB_NAME, JOB_SLUG);
		JobSearchResultDto differentId = new JobSearchResultDto(Integer.valueOf(999), JOB_NAME, JOB_SLUG);
		JobSearchResultDto differentName = new JobSearchResultDto(JOB_ID, "Other Job", JOB_SLUG);
		JobSearchResultDto differentSlug = new JobSearchResultDto(JOB_ID, JOB_NAME, "other-job");

		// Then
		assertThat(baseline).isNotEqualTo(differentId)
			.isNotEqualTo(differentName)
			.isNotEqualTo(differentSlug)
			.isNotEqualTo(new Object());
	}

	@Test
	@DisplayName("Record toString includes component names and values")
	void testRecordToStringContainsComponents() {
		// Given
		JobSearchResultDto dto = new JobSearchResultDto(JOB_ID, JOB_NAME, JOB_SLUG);

		// When
		String result = dto.toString();

		// Then
		assertThat(result).contains("JobSearchResultDto")
			.contains("id=" + JOB_ID)
			.contains("name=" + JOB_NAME)
			.contains("slug=" + JOB_SLUG);
	}

	@Test
	@DisplayName("Record allows null component values")
	void testRecordAllowsNullComponents() {
		// Given
		JobSearchResultDto dto = new JobSearchResultDto(null, null, null);

		// Then
		assertThat(dto.id()).isNull();
		assertThat(dto.name()).isNull();
		assertThat(dto.slug()).isNull();
	}

	@Test
	@DisplayName("JSON deserialization maps fields to record components")
	void testJsonDeserializationMapsFieldsToRecordComponents() throws Exception {
		// Given
		String json = "{\"id\":301,\"name\":\"Senior Java Developer\",\"slug\":\"senior-java-developer\"}";

		// When
		JobSearchResultDto dto = this.objectMapper.readValue(json, JobSearchResultDto.class);

		// Then
		assertThat(dto.id()).isEqualTo(JOB_ID);
		assertThat(dto.name()).isEqualTo(JOB_NAME);
		assertThat(dto.slug()).isEqualTo(JOB_SLUG);
	}

	@Test
	@DisplayName("JSON serialization writes record components")
	void testJsonSerializationWritesRecordComponents() throws Exception {
		// Given
		JobSearchResultDto dto = new JobSearchResultDto(JOB_ID, JOB_NAME, JOB_SLUG);

		// When
		String json = this.objectMapper.writeValueAsString(dto);

		// Then
		assertThat(json).contains("\"id\":301")
			.contains("\"name\":\"Senior Java Developer\"")
			.contains("\"slug\":\"senior-java-developer\"");
	}

}
