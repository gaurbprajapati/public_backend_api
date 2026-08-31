package io.recruitcrm.microservice.timesheet.dto.timesheet;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CompanySearchResultDto Tests")
class CompanySearchResultDtoTests {

	private static final Integer COMPANY_ID = Integer.valueOf(201);

	private static final String COMPANY_NAME = "Recruit CRM";

	private static final String COMPANY_SLUG = "recruit-crm";

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		this.objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("Record constructor exposes all components")
	void testRecordConstructorExposesAllComponents() {
		// Given
		CompanySearchResultDto dto = new CompanySearchResultDto(COMPANY_ID, COMPANY_NAME, COMPANY_SLUG);

		// Then
		assertThat(dto.id()).isEqualTo(COMPANY_ID);
		assertThat(dto.name()).isEqualTo(COMPANY_NAME);
		assertThat(dto.slug()).isEqualTo(COMPANY_SLUG);
	}

	@Test
	@DisplayName("Record supports value equality and consistent hash code")
	void testRecordEqualityAndHashCode() {
		// Given
		CompanySearchResultDto left = new CompanySearchResultDto(COMPANY_ID, COMPANY_NAME, COMPANY_SLUG);
		CompanySearchResultDto right = new CompanySearchResultDto(COMPANY_ID, COMPANY_NAME, COMPANY_SLUG);

		// Then
		assertThat(left).isEqualTo(right).hasSameHashCodeAs(right);
	}

	@Test
	@DisplayName("Record is not equal when component values differ")
	void testRecordInequalityWhenComponentsDiffer() {
		// Given
		CompanySearchResultDto baseline = new CompanySearchResultDto(COMPANY_ID, COMPANY_NAME, COMPANY_SLUG);
		CompanySearchResultDto differentId = new CompanySearchResultDto(Integer.valueOf(999), COMPANY_NAME,
				COMPANY_SLUG);
		CompanySearchResultDto differentName = new CompanySearchResultDto(COMPANY_ID, "Other Company", COMPANY_SLUG);
		CompanySearchResultDto differentSlug = new CompanySearchResultDto(COMPANY_ID, COMPANY_NAME, "other-company");

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
		CompanySearchResultDto dto = new CompanySearchResultDto(COMPANY_ID, COMPANY_NAME, COMPANY_SLUG);

		// When
		String result = dto.toString();

		// Then
		assertThat(result).contains("CompanySearchResultDto")
			.contains("id=" + COMPANY_ID)
			.contains("name=" + COMPANY_NAME)
			.contains("slug=" + COMPANY_SLUG);
	}

	@Test
	@DisplayName("Record allows null component values")
	void testRecordAllowsNullComponents() {
		// Given
		CompanySearchResultDto dto = new CompanySearchResultDto(null, null, null);

		// Then
		assertThat(dto.id()).isNull();
		assertThat(dto.name()).isNull();
		assertThat(dto.slug()).isNull();
	}

	@Test
	@DisplayName("JSON deserialization maps fields to record components")
	void testJsonDeserializationMapsFieldsToRecordComponents() throws Exception {
		// Given
		String json = "{\"id\":201,\"name\":\"Recruit CRM\",\"slug\":\"recruit-crm\"}";

		// When
		CompanySearchResultDto dto = this.objectMapper.readValue(json, CompanySearchResultDto.class);

		// Then
		assertThat(dto.id()).isEqualTo(COMPANY_ID);
		assertThat(dto.name()).isEqualTo(COMPANY_NAME);
		assertThat(dto.slug()).isEqualTo(COMPANY_SLUG);
	}

	@Test
	@DisplayName("JSON serialization writes record components")
	void testJsonSerializationWritesRecordComponents() throws Exception {
		// Given
		CompanySearchResultDto dto = new CompanySearchResultDto(COMPANY_ID, COMPANY_NAME, COMPANY_SLUG);

		// When
		String json = this.objectMapper.writeValueAsString(dto);

		// Then
		assertThat(json).contains("\"id\":201")
			.contains("\"name\":\"Recruit CRM\"")
			.contains("\"slug\":\"recruit-crm\"");
	}

}
