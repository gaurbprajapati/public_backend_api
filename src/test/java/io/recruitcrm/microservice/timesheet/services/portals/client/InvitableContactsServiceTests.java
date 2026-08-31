package io.recruitcrm.microservice.timesheet.services.portals.client;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Contacts;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.mapper.InvitableContactsMapper;
import io.recruitcrm.microservice.timesheet.repositories.portals.client.IInvitableContactsRepository;
import io.recruitcrm.microservice.timesheet.testdata.InvitableContactsTestDataFactory;
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

/**
 * Unit tests for InvitableContactsService. Tests getInvitableContacts method and all
 * branches of computeAllActive and filterNotSent for 100% line and branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class InvitableContactsServiceTests {

	@Mock
	private IInvitableContactsRepository invitableContactsRepository;

	@Mock
	private InvitableContactsMapper invitableContactsMapper;

	@Mock
	private AuthHolder auth;

	@Mock
	private AccessControlHelper accessControlHelper;

	@InjectMocks
	private InvitableContactsService invitableContactsService;

	@BeforeEach
	void setUp() {
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(InvitableContactsTestDataFactory.getDefaultAccountId());
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		Contacts contactsAcl = new Contacts();
		contactsAcl.setCanView("Everything");
		contactsAcl.setCanEdit("Everything");
		contactsAcl.setCanDelete("Everything");
		given(this.accessControlHelper.getContactsAccessControl()).willReturn(contactsAcl);
	}

	@Test
	@DisplayName("Get invitable contacts returns only status-0 contacts with allActive false")
	void testGetInvitableContactsNotAllActiveReturnsAllActiveFalse() {
		// Given
		Integer companyId = InvitableContactsTestDataFactory.getDefaultCompanyId();
		Integer accountId = InvitableContactsTestDataFactory.getDefaultAccountId();
		List<InvitableContactQueryResultDto> queryResults = InvitableContactsTestDataFactory
			.createContactQueryResultList();
		List<InvitableContactResponseBodyDto> contacts = List
			.of(InvitableContactsTestDataFactory.createContactResponseWithStatus(0));

		given(this.invitableContactsRepository.findContactsWithPortalStatus(companyId, accountId, null, 25))
			.willReturn(queryResults);
		given(this.invitableContactsMapper.mapToResponseDtos(queryResults)).willReturn(contacts);

		// When
		InvitableContactsResponseBodyDto result = this.invitableContactsService.getInvitableContacts(companyId, null,
				null);

		// Then
		assertThat(result.getCompanyId()).isEqualTo(companyId);
		assertThat(result.isAllActive()).isFalse();
		assertThat(result.getContacts()).isEqualTo(contacts);
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.invitableContactsRepository).should().findContactsWithPortalStatus(companyId, accountId, null, 25);
		then(this.invitableContactsMapper).should().mapToResponseDtos(queryResults);
	}

	@Test
	@DisplayName("Get invitable contacts passes through explicit search and limit to the repository")
	void testGetInvitableContactsWithExplicitSearchAndLimitPassesThemToRepository() {
		// Given
		Integer companyId = InvitableContactsTestDataFactory.getDefaultCompanyId();
		Integer accountId = InvitableContactsTestDataFactory.getDefaultAccountId();
		List<InvitableContactQueryResultDto> queryResults = InvitableContactsTestDataFactory
			.createContactQueryResultList();
		List<InvitableContactResponseBodyDto> contacts = List
			.of(InvitableContactsTestDataFactory.createContactResponseWithStatus(0));

		given(this.invitableContactsRepository.findContactsWithPortalStatus(companyId, accountId, "jane", 10))
			.willReturn(queryResults);
		given(this.invitableContactsMapper.mapToResponseDtos(queryResults)).willReturn(contacts);

		// When
		this.invitableContactsService.getInvitableContacts(companyId, "jane", 10);

		// Then
		then(this.invitableContactsRepository).should().findContactsWithPortalStatus(companyId, accountId, "jane", 10);
	}

	@Test
	@DisplayName("Get invitable contacts returns allActive true and empty contacts when all contacts are portal enabled")
	void testGetInvitableContactsAllContactsEnabledReturnsAllActiveTrueAndEmptyContacts() {
		// Given
		Integer companyId = InvitableContactsTestDataFactory.getDefaultCompanyId();
		Integer accountId = InvitableContactsTestDataFactory.getDefaultAccountId();
		List<InvitableContactQueryResultDto> queryResults = InvitableContactsTestDataFactory
			.createAllActiveQueryResultList();
		List<InvitableContactResponseBodyDto> contacts = List
			.of(InvitableContactsTestDataFactory.createContactResponseWithStatus(2));

		given(this.invitableContactsRepository.findContactsWithPortalStatus(companyId, accountId, null, 25))
			.willReturn(queryResults);
		given(this.invitableContactsMapper.mapToResponseDtos(queryResults)).willReturn(contacts);

		// When
		InvitableContactsResponseBodyDto result = this.invitableContactsService.getInvitableContacts(companyId, null,
				null);

		// Then
		assertThat(result.isAllActive()).isTrue();
		assertThat(result.getContacts()).isEmpty();
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.invitableContactsRepository).should().findContactsWithPortalStatus(companyId, accountId, null, 25);
		then(this.invitableContactsMapper).should().mapToResponseDtos(queryResults);
	}

	@Test
	@DisplayName("Get invitable contacts returns allActive false when contacts list is empty")
	void testGetInvitableContactsEmptyContactListReturnsAllActiveFalse() {
		// Given
		Integer companyId = InvitableContactsTestDataFactory.getDefaultCompanyId();
		Integer accountId = InvitableContactsTestDataFactory.getDefaultAccountId();
		List<InvitableContactQueryResultDto> queryResults = InvitableContactsTestDataFactory
			.createEmptyQueryResultList();
		List<InvitableContactResponseBodyDto> contacts = List.of();

		given(this.invitableContactsRepository.findContactsWithPortalStatus(companyId, accountId, null, 25))
			.willReturn(queryResults);
		given(this.invitableContactsMapper.mapToResponseDtos(queryResults)).willReturn(contacts);

		// When
		InvitableContactsResponseBodyDto result = this.invitableContactsService.getInvitableContacts(companyId, null,
				null);

		// Then
		assertThat(result.getContacts()).isEmpty();
		assertThat(result.isAllActive()).isFalse();
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.invitableContactsRepository).should().findContactsWithPortalStatus(companyId, accountId, null, 25);
		then(this.invitableContactsMapper).should().mapToResponseDtos(queryResults);
	}

	@Test
	@DisplayName("Get invitable contacts performs team lookup and adds current user when team list excludes them")
	void testGetInvitableContactsTeamLookupAddsCurrentUserWhenAbsent() {
		// Given
		Integer companyId = InvitableContactsTestDataFactory.getDefaultCompanyId();
		Integer accountId = InvitableContactsTestDataFactory.getDefaultAccountId();
		Integer currentUserId = 1;
		Contacts teamAcl = new Contacts();
		teamAcl.setCanView("Team Only");
		teamAcl.setCanEdit("Team Only");
		teamAcl.setCanDelete("Team Only");
		given(this.accessControlHelper.getContactsAccessControl()).willReturn(teamAcl);
		given(this.accessControlHelper.requiresTeamLookup("Team Only")).willReturn(true);
		List<Integer> teamUserIds = new java.util.ArrayList<>(List.of(20, 30));
		given(this.accessControlHelper.getTeamUserIds(currentUserId)).willReturn(teamUserIds);

		List<InvitableContactQueryResultDto> queryResults = InvitableContactsTestDataFactory
			.createContactQueryResultList();
		List<InvitableContactResponseBodyDto> contacts = List
			.of(InvitableContactsTestDataFactory.createContactResponseWithStatus(0));

		given(this.invitableContactsRepository.findContactsWithPortalStatus(companyId, accountId, null, 25))
			.willReturn(queryResults);
		given(this.invitableContactsMapper.mapToResponseDtos(queryResults)).willReturn(contacts);

		// When
		this.invitableContactsService.getInvitableContacts(companyId, null, null);

		// Then
		assertThat(teamUserIds).contains(currentUserId);
		then(this.accessControlHelper).should().getTeamUserIds(currentUserId);
	}

	@Test
	@DisplayName("Get invitable contacts performs team lookup without re-adding current user when already present")
	void testGetInvitableContactsTeamLookupDoesNotReAddCurrentUserWhenPresent() {
		// Given
		Integer companyId = InvitableContactsTestDataFactory.getDefaultCompanyId();
		Integer accountId = InvitableContactsTestDataFactory.getDefaultAccountId();
		Integer currentUserId = 1;
		Contacts teamAcl = new Contacts();
		teamAcl.setCanView("Team Only");
		teamAcl.setCanEdit("Team Only");
		teamAcl.setCanDelete("Team Only");
		given(this.accessControlHelper.getContactsAccessControl()).willReturn(teamAcl);
		given(this.accessControlHelper.requiresTeamLookup("Team Only")).willReturn(true);
		List<Integer> teamUserIds = new java.util.ArrayList<>(List.of(currentUserId, 30));
		given(this.accessControlHelper.getTeamUserIds(currentUserId)).willReturn(teamUserIds);

		List<InvitableContactQueryResultDto> queryResults = InvitableContactsTestDataFactory
			.createContactQueryResultList();
		List<InvitableContactResponseBodyDto> contacts = List
			.of(InvitableContactsTestDataFactory.createContactResponseWithStatus(0));

		given(this.invitableContactsRepository.findContactsWithPortalStatus(companyId, accountId, null, 25))
			.willReturn(queryResults);
		given(this.invitableContactsMapper.mapToResponseDtos(queryResults)).willReturn(contacts);

		// When
		this.invitableContactsService.getInvitableContacts(companyId, null, null);

		// Then
		assertThat(teamUserIds).containsExactly(currentUserId, 30);
		then(this.accessControlHelper).should().getTeamUserIds(currentUserId);
	}

	@Test
	@DisplayName("Get invitable contacts with mixed statuses returns allActive false and only not-sent contacts")
	void testGetInvitableContactsMixedStatusesReturnsAllActiveFalseAndNotSentContacts() {
		// Given
		Integer companyId = InvitableContactsTestDataFactory.getDefaultCompanyId();
		Integer accountId = InvitableContactsTestDataFactory.getDefaultAccountId();
		List<InvitableContactQueryResultDto> queryResults = List.of(
				InvitableContactsTestDataFactory.createContactQueryResultWithStatus(0),
				InvitableContactsTestDataFactory.createContactQueryResultWithStatus(2));
		InvitableContactResponseBodyDto notSentContact = InvitableContactsTestDataFactory
			.createContactResponseWithStatus(0);
		InvitableContactResponseBodyDto enabledContact = InvitableContactsTestDataFactory
			.createContactResponseWithStatus(2);
		List<InvitableContactResponseBodyDto> contacts = List.of(notSentContact, enabledContact);

		given(this.invitableContactsRepository.findContactsWithPortalStatus(companyId, accountId, null, 25))
			.willReturn(queryResults);
		given(this.invitableContactsMapper.mapToResponseDtos(queryResults)).willReturn(contacts);

		// When
		InvitableContactsResponseBodyDto result = this.invitableContactsService.getInvitableContacts(companyId, null,
				null);

		// Then
		assertThat(result.isAllActive()).isFalse();
		assertThat(result.getContacts()).containsExactly(notSentContact);
		then(this.invitableContactsRepository).should().findContactsWithPortalStatus(companyId, accountId, null, 25);
		then(this.invitableContactsMapper).should().mapToResponseDtos(queryResults);
	}

}
