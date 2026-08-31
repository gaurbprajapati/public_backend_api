package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Access level DTO tests")
class AccessLevelDtosTests {

	private static final String KEY = "key";

	private static final Object VALUE = "value";

	@Test
	@DisplayName("CallLog additionalProperties getter/setter covers map mutation")
	void testCallLogAdditionalProperties() {
		CallLog dto = new CallLog();

		assertThat(dto.getAdditionalProperties()).isNotNull().isEmpty();

		dto.setAdditionalProperty(KEY, VALUE);

		assertThat(dto.getAdditionalProperties()).containsEntry(KEY, VALUE);
	}

	@Test
	@DisplayName("Candidates additionalProperties getter/setter covers map mutation")
	void testCandidatesAdditionalProperties() {
		Candidates dto = new Candidates();

		assertThat(dto.getAdditionalProperties()).isNotNull().isEmpty();

		dto.setAdditionalProperty(KEY, VALUE);

		assertThat(dto.getAdditionalProperties()).containsEntry(KEY, VALUE);
	}

	@Test
	@DisplayName("Companies additionalProperties getter/setter covers map mutation")
	void testCompaniesAdditionalProperties() {
		Companies dto = new Companies();

		assertThat(dto.getAdditionalProperties()).isNotNull().isEmpty();

		dto.setAdditionalProperty(KEY, VALUE);

		assertThat(dto.getAdditionalProperties()).containsEntry(KEY, VALUE);
	}

	@Test
	@DisplayName("Contacts additionalProperties getter/setter covers map mutation")
	void testContactsAdditionalProperties() {
		Contacts dto = new Contacts();

		assertThat(dto.getAdditionalProperties()).isNotNull().isEmpty();

		dto.setAdditionalProperty(KEY, VALUE);

		assertThat(dto.getAdditionalProperties()).containsEntry(KEY, VALUE);
	}

}
