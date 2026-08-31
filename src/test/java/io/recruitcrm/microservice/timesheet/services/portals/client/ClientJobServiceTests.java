package io.recruitcrm.microservice.timesheet.services.portals.client;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.dto.portal.client.ClientJobResponseBodyDto;
import io.recruitcrm.microservice.timesheet.repositories.portals.client.IClientJobRepository;
import io.recruitcrm.microservice.timesheet.testdata.ClientJobTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyInt;

/**
 * Unit tests for ClientJobService. Tests fetchClientJobs method and all branches for 100%
 * line and branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class ClientJobServiceTests {

	@Mock
	private IClientJobRepository clientJobRepository;

	@Mock
	private AuthHolder auth;

	@Mock
	private AuthPrincipal principal;

	@InjectMocks
	private ClientJobService clientJobService;

	@BeforeEach
	void setUp() {
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(ClientJobTestDataFactory.getDefaultAccountId());
		given(this.auth.getUnifiedPrincipal()).willReturn(this.principal);
	}

	@Test
	@DisplayName("Fetch client jobs returns job list when email resolves to jobs")
	void testFetchClientJobsValidEmailReturnsJobList() {
		// Given
		String email = ClientJobTestDataFactory.getDefaultEmail();
		List<ClientJobResponseBodyDto> expectedJobs = ClientJobTestDataFactory.createJobResponseList();

		given(this.principal.getEmail()).willReturn(email);
		given(this.clientJobRepository.findJobsByEmail(email, ClientJobTestDataFactory.getDefaultAccountId()))
			.willReturn(expectedJobs);

		// When
		List<ClientJobResponseBodyDto> result = this.clientJobService.fetchClientJobs();

		// Then
		assertThat(result).isEqualTo(expectedJobs);
		then(this.clientJobRepository).should().findJobsByEmail(email, ClientJobTestDataFactory.getDefaultAccountId());
	}

	@Test
	@DisplayName("Fetch client jobs returns empty list when email is null")
	void testFetchClientJobsNullEmailReturnsEmptyList() {
		// Given
		given(this.principal.getEmail()).willReturn(null);

		// When
		List<ClientJobResponseBodyDto> result = this.clientJobService.fetchClientJobs();

		// Then
		assertThat(result).isEmpty();
		then(this.clientJobRepository).should(never()).findJobsByEmail(anyString(), anyInt());
	}

	@Test
	@DisplayName("Fetch client jobs returns empty list when email is blank")
	void testFetchClientJobsBlankEmailReturnsEmptyList() {
		// Given
		given(this.principal.getEmail()).willReturn("");

		// When
		List<ClientJobResponseBodyDto> result = this.clientJobService.fetchClientJobs();

		// Then
		assertThat(result).isEmpty();
		then(this.clientJobRepository).should(never()).findJobsByEmail(anyString(), anyInt());
	}

}
