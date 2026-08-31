package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactResponseBodyDto;
import io.recruitcrm.microservice.timesheet.testdata.InvitableContactsTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for InvitableContactsMapper. Tests field mapping for 100% line coverage.
 */
class InvitableContactsMapperTests {

	private InvitableContactsMapper invitableContactsMapper;

	@BeforeEach
	void setUp() {
		this.invitableContactsMapper = new InvitableContactsMapper();
	}

	@Test
	@DisplayName("Map to response dtos maps all fields correctly")
	void testMapToResponseDtosMapsAllFieldsCorrectly() {
		// Given
		List<InvitableContactQueryResultDto> queryResults = InvitableContactsTestDataFactory
			.createContactQueryResultList();

		// When
		List<InvitableContactResponseBodyDto> result = this.invitableContactsMapper.mapToResponseDtos(queryResults);

		// Then
		assertThat(result).hasSize(1);
		InvitableContactResponseBodyDto dto = result.get(0);
		assertThat(dto.getId()).isEqualTo(1234);
		assertThat(dto.getFirstName()).isEqualTo("Jane");
		assertThat(dto.getLastName()).isEqualTo("Doe");
		assertThat(dto.getEmail()).isEqualTo("jane@example.com");
		assertThat(dto.getPortalStatusId()).isZero();
		assertThat(dto.getPhoto()).isEqualTo("jane.jpg");
		assertThat(dto.getSrno()).isEqualTo(1);
		assertThat(dto.getCompanyName()).isEqualTo("Acme Corp");
	}

	@Test
	@DisplayName("Map to response dtos returns empty list when query results are empty")
	void testMapToResponseDtosEmptyQueryResultsReturnsEmptyList() {
		// Given
		List<InvitableContactQueryResultDto> queryResults = InvitableContactsTestDataFactory
			.createEmptyQueryResultList();

		// When
		List<InvitableContactResponseBodyDto> result = this.invitableContactsMapper.mapToResponseDtos(queryResults);

		// Then
		assertThat(result).isEmpty();
	}

}
