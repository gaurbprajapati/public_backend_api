package io.recruitcrm.microservice.timesheet.dto.timesheet;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DealSearchResultDto Tests")
class DealSearchResultDtoTests {

	private static final Integer DEAL_ID = Integer.valueOf(501);

	private static final String DEAL_NAME = "Enterprise Deal";

	private static final String DEAL_SLUG = "enterprise-deal";

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		this.objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("Record constructor exposes all components")
	void testRecordConstructorExposesAllComponents() {
		// Given
		DealSearchResultDto dto = new DealSearchResultDto(DEAL_ID, DEAL_NAME, DEAL_SLUG);

		// Then
		assertThat(dto.id()).isEqualTo(DEAL_ID);
		assertThat(dto.name()).isEqualTo(DEAL_NAME);
		assertThat(dto.slug()).isEqualTo(DEAL_SLUG);
	}

	@Test
	@DisplayName("Record supports value equality and consistent hash code")
	void testRecordEqualityAndHashCode() {
		// Given
		DealSearchResultDto left = new DealSearchResultDto(DEAL_ID, DEAL_NAME, DEAL_SLUG);
		DealSearchResultDto right = new DealSearchResultDto(DEAL_ID, DEAL_NAME, DEAL_SLUG);

		// Then
		assertThat(left).isEqualTo(right).hasSameHashCodeAs(right);
	}

	@Test
	@DisplayName("Record is not equal when component values differ")
	void testRecordInequalityWhenComponentsDiffer() {
		// Given
		DealSearchResultDto baseline = new DealSearchResultDto(DEAL_ID, DEAL_NAME, DEAL_SLUG);
		DealSearchResultDto differentId = new DealSearchResultDto(Integer.valueOf(999), DEAL_NAME, DEAL_SLUG);
		DealSearchResultDto differentName = new DealSearchResultDto(DEAL_ID, "Other Deal", DEAL_SLUG);
		DealSearchResultDto differentSlug = new DealSearchResultDto(DEAL_ID, DEAL_NAME, "other-deal");

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
		DealSearchResultDto dto = new DealSearchResultDto(DEAL_ID, DEAL_NAME, DEAL_SLUG);

		// When
		String result = dto.toString();

		// Then
		assertThat(result).contains("DealSearchResultDto")
			.contains("id=" + DEAL_ID)
			.contains("name=" + DEAL_NAME)
			.contains("slug=" + DEAL_SLUG);
	}

	@Test
	@DisplayName("Record allows null component values")
	void testRecordAllowsNullComponents() {
		// Given
		DealSearchResultDto dto = new DealSearchResultDto(null, null, null);

		// Then
		assertThat(dto.id()).isNull();
		assertThat(dto.name()).isNull();
		assertThat(dto.slug()).isNull();
	}

	@Test
	@DisplayName("JSON deserialization maps fields to record components")
	void testJsonDeserializationMapsFieldsToRecordComponents() throws Exception {
		// Given
		String json = "{\"id\":501,\"name\":\"Enterprise Deal\",\"slug\":\"enterprise-deal\"}";

		// When
		DealSearchResultDto dto = this.objectMapper.readValue(json, DealSearchResultDto.class);

		// Then
		assertThat(dto.id()).isEqualTo(DEAL_ID);
		assertThat(dto.name()).isEqualTo(DEAL_NAME);
		assertThat(dto.slug()).isEqualTo(DEAL_SLUG);
	}

	@Test
	@DisplayName("JSON serialization writes record components")
	void testJsonSerializationWritesRecordComponents() throws Exception {
		// Given
		DealSearchResultDto dto = new DealSearchResultDto(DEAL_ID, DEAL_NAME, DEAL_SLUG);

		// When
		String json = this.objectMapper.writeValueAsString(dto);

		// Then
		assertThat(json).contains("\"id\":501")
			.contains("\"name\":\"Enterprise Deal\"")
			.contains("\"slug\":\"enterprise-deal\"");
	}

}
