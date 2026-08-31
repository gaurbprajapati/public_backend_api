package io.recruitcrm.microservice.timesheet.services.portals.client;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Contacts;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.ContactBulkValidateItemDto;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.repositories.portals.client.IBulkValidateRepository;
import io.recruitcrm.microservice.timesheet.testdata.BulkValidateTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Unit tests for BulkValidateService. Tests bulkValidate method and all branches for 100%
 * line and branch coverage.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BulkValidateServiceTests {

	private static final Integer CURRENT_USER_ID = 7;

	private static final Integer OWNER_ID = 7;

	@Mock
	private IBulkValidateRepository bulkValidateRepository;

	@Mock
	private AuthHolder auth;

	@Mock
	private AccessControlHelper accessControlHelper;

	@InjectMocks
	private BulkValidateService bulkValidateService;

	@BeforeEach
	void setUp() {
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(BulkValidateTestDataFactory.getDefaultAccountId());
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(CURRENT_USER_ID);
		Contacts contactsAcl = new Contacts();
		contactsAcl.setCanEdit("Everything");
		given(this.accessControlHelper.getContactsAccessControl()).willReturn(contactsAcl);
		given(this.accessControlHelper.requiresTeamLookup(any())).willReturn(false);
		given(this.bulkValidateRepository.findOwnerIdsByContactIds(anyList()))
			.willReturn(Map.of(BulkValidateTestDataFactory.getDefaultContactId(), OWNER_ID, 1235, OWNER_ID));
		given(this.accessControlHelper.resolvePermission(any(), any(), any(), anyList())).willReturn(true);
	}

	@Test
	@DisplayName("Bulk validate with no matching DB rows returns contact as valid")
	void testBulkValidateNoDbRowsReturnsValidResult() {
		// Given
		BulkValidateRequestBodyDto request = BulkValidateTestDataFactory.createBulkValidateRequest();
		given(this.bulkValidateRepository
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail())))
			.willReturn(List.of());

		// When
		BulkValidateResponseBodyDto result = this.bulkValidateService.bulkValidate(request);

		// Then
		assertThat(result.getValidCount()).isEqualTo(1);
		assertThat(result.getInvalidCount()).isZero();
		assertThat(result.getResults()).hasSize(1);
		assertThat(result.getResults().get(0).isValid()).isTrue();
		assertThat(result.getResults().get(0).getReason()).isNull();
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.bulkValidateRepository).should()
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail()));
	}

	@Test
	@DisplayName("Bulk validate returns no_edit_access reason when user lacks edit permission")
	void testBulkValidateNoEditAccessReturnsNoEditAccessReason() {
		// Given
		BulkValidateRequestBodyDto request = BulkValidateTestDataFactory.createBulkValidateRequest();
		given(this.accessControlHelper.resolvePermission(any(), any(), any(), anyList())).willReturn(false);

		// When
		BulkValidateResponseBodyDto result = this.bulkValidateService.bulkValidate(request);

		// Then
		assertThat(result.getValidCount()).isZero();
		assertThat(result.getInvalidCount()).isEqualTo(1);
		assertThat(result.getResults().get(0).isValid()).isFalse();
		assertThat(result.getResults().get(0).getReason()).isEqualTo("no_edit_access");
		then(this.bulkValidateRepository).should().findPortalStatusByEmails(anyList());
	}

	@Test
	@DisplayName("Bulk validate with null email contact returns email_missing reason")
	void testBulkValidateNullEmailContactReturnsEmailMissingReason() {
		// Given
		BulkValidateRequestBodyDto request = BulkValidateTestDataFactory.createBulkValidateRequestAllNullEmails();

		// When
		BulkValidateResponseBodyDto result = this.bulkValidateService.bulkValidate(request);

		// Then
		assertThat(result.getValidCount()).isZero();
		assertThat(result.getInvalidCount()).isEqualTo(1);
		assertThat(result.getResults().get(0).isValid()).isFalse();
		assertThat(result.getResults().get(0).getReason()).isEqualTo("email_missing");
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.bulkValidateRepository).should(never()).findPortalStatusByEmails(anyList());
	}

	@Test
	@DisplayName("Bulk validate with email existing under different account returns email_taken reason")
	void testBulkValidateEmailUnderDifferentAccountReturnsEmailTakenReason() {
		// Given
		BulkValidateRequestBodyDto request = BulkValidateTestDataFactory.createBulkValidateRequest();
		List<BulkValidateQueryResultDto> rows = List
			.of(BulkValidateTestDataFactory.createQueryResultDifferentAccount());
		given(this.bulkValidateRepository
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail()))).willReturn(rows);

		// When
		BulkValidateResponseBodyDto result = this.bulkValidateService.bulkValidate(request);

		// Then
		assertThat(result.getValidCount()).isZero();
		assertThat(result.getInvalidCount()).isEqualTo(1);
		assertThat(result.getResults().get(0).isValid()).isFalse();
		assertThat(result.getResults().get(0).getReason()).isEqualTo("email_taken");
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.bulkValidateRepository).should()
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail()));
	}

	@Test
	@DisplayName("Bulk validate with portal status active for same account returns portal_active reason")
	void testBulkValidatePortalActiveForSameAccountReturnsPortalActiveReason() {
		// Given
		BulkValidateRequestBodyDto request = BulkValidateTestDataFactory.createBulkValidateRequest();
		List<BulkValidateQueryResultDto> rows = List.of(BulkValidateTestDataFactory.createQueryResultPortalActive());
		given(this.bulkValidateRepository
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail()))).willReturn(rows);

		// When
		BulkValidateResponseBodyDto result = this.bulkValidateService.bulkValidate(request);

		// Then
		assertThat(result.getValidCount()).isZero();
		assertThat(result.getInvalidCount()).isEqualTo(1);
		assertThat(result.getResults().get(0).isValid()).isFalse();
		assertThat(result.getResults().get(0).getReason()).isEqualTo("portal_active");
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.bulkValidateRepository).should()
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail()));
	}

	@Test
	@DisplayName("Bulk validate with invite count at threshold today returns rate_limit reason")
	void testBulkValidateInviteCountAtThresholdTodayReturnsRateLimitReason() {
		// Given
		BulkValidateRequestBodyDto request = BulkValidateTestDataFactory.createBulkValidateRequest();
		List<BulkValidateQueryResultDto> rows = List.of(BulkValidateTestDataFactory.createQueryResultRateLimited());
		given(this.bulkValidateRepository
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail()))).willReturn(rows);

		// When
		BulkValidateResponseBodyDto result = this.bulkValidateService.bulkValidate(request);

		// Then
		assertThat(result.getValidCount()).isZero();
		assertThat(result.getInvalidCount()).isEqualTo(1);
		assertThat(result.getResults().get(0).isValid()).isFalse();
		assertThat(result.getResults().get(0).getReason()).isEqualTo("rate_limit");
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.bulkValidateRepository).should()
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail()));
	}

	@Test
	@DisplayName("Bulk validate treats invite count at threshold on previous day as valid")
	void testBulkValidateInviteCountAtThresholdPreviousDayReturnsValid() {
		// Given
		BulkValidateRequestBodyDto request = BulkValidateTestDataFactory.createBulkValidateRequest();
		long yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		BulkValidateQueryResultDto row = new BulkValidateQueryResultDto(
				BulkValidateTestDataFactory.getDefaultContactEmail(), 0,
				BulkValidateTestDataFactory.getDefaultAccountId().longValue(), 3L, yesterday);
		given(this.bulkValidateRepository
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail())))
			.willReturn(List.of(row));

		// When
		BulkValidateResponseBodyDto result = this.bulkValidateService.bulkValidate(request);

		// Then
		assertThat(result.getValidCount()).isEqualTo(1);
		assertThat(result.getInvalidCount()).isZero();
		assertThat(result.getResults().get(0).isValid()).isTrue();
		assertThat(result.getResults().get(0).getReason()).isNull();
	}

	@Test
	@DisplayName("Bulk validate treats invite count below threshold today as valid")
	void testBulkValidateInviteCountBelowThresholdTodayReturnsValid() {
		// Given
		BulkValidateRequestBodyDto request = BulkValidateTestDataFactory.createBulkValidateRequest();
		long startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		BulkValidateQueryResultDto row = new BulkValidateQueryResultDto(
				BulkValidateTestDataFactory.getDefaultContactEmail(), 0,
				BulkValidateTestDataFactory.getDefaultAccountId().longValue(), 2L, startOfToday);
		given(this.bulkValidateRepository
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail())))
			.willReturn(List.of(row));

		// When
		BulkValidateResponseBodyDto result = this.bulkValidateService.bulkValidate(request);

		// Then
		assertThat(result.getValidCount()).isEqualTo(1);
		assertThat(result.getResults().get(0).isValid()).isTrue();
	}

	@Test
	@DisplayName("Bulk validate with mixed contacts returns correct valid and invalid counts")
	void testBulkValidateMixedContactsReturnsCorrectCounts() {
		// Given
		BulkValidateRequestBodyDto request = BulkValidateTestDataFactory.createBulkValidateRequestMixed();
		given(this.bulkValidateRepository
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail())))
			.willReturn(List.of());

		// When
		BulkValidateResponseBodyDto result = this.bulkValidateService.bulkValidate(request);

		// Then
		assertThat(result.getValidCount()).isEqualTo(1);
		assertThat(result.getInvalidCount()).isEqualTo(1);
		assertThat(result.getResults()).hasSize(2);
		then(this.auth).should().getAuthenticationPrincipalOrganizationIdentifier();
		then(this.bulkValidateRepository).should()
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail()));
	}

	@Test
	@DisplayName("Bulk validate de-duplicates repeated emails before querying portal status")
	void testBulkValidateDeduplicatesEmailsBeforeQuery() {
		// Given
		ContactBulkValidateItemDto first = new ContactBulkValidateItemDto(1234,
				BulkValidateTestDataFactory.getDefaultContactEmail());
		ContactBulkValidateItemDto second = new ContactBulkValidateItemDto(1235,
				BulkValidateTestDataFactory.getDefaultContactEmail());
		BulkValidateRequestBodyDto request = new BulkValidateRequestBodyDto(List.of(first, second));
		given(this.bulkValidateRepository
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail())))
			.willReturn(List.of());

		// When
		BulkValidateResponseBodyDto result = this.bulkValidateService.bulkValidate(request);

		// Then
		assertThat(result.getResults()).hasSize(2);
		assertThat(result.getValidCount()).isEqualTo(2);
		then(this.bulkValidateRepository).should()
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail()));
	}

	@Test
	@DisplayName("Bulk validate resolves team user ids and adds current user when team lookup required")
	void testBulkValidateTeamLookupAddsCurrentUser() {
		// Given
		BulkValidateRequestBodyDto request = BulkValidateTestDataFactory.createBulkValidateRequest();
		Contacts contactsAcl = new Contacts();
		contactsAcl.setCanEdit("Team Only");
		given(this.accessControlHelper.getContactsAccessControl()).willReturn(contactsAcl);
		given(this.accessControlHelper.requiresTeamLookup("Team Only")).willReturn(true);
		List<Integer> teamUserIds = new java.util.ArrayList<>(List.of(99, 100));
		given(this.accessControlHelper.getTeamUserIds(CURRENT_USER_ID)).willReturn(teamUserIds);
		given(this.bulkValidateRepository
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail())))
			.willReturn(List.of());

		// When
		BulkValidateResponseBodyDto result = this.bulkValidateService.bulkValidate(request);

		// Then
		assertThat(result.getValidCount()).isEqualTo(1);
		assertThat(teamUserIds).contains(CURRENT_USER_ID);
		then(this.accessControlHelper).should().getTeamUserIds(CURRENT_USER_ID);
	}

	@Test
	@DisplayName("Bulk validate does not re-add current user when already present in team user ids")
	void testBulkValidateTeamLookupKeepsExistingCurrentUser() {
		// Given
		BulkValidateRequestBodyDto request = BulkValidateTestDataFactory.createBulkValidateRequest();
		Contacts contactsAcl = new Contacts();
		contactsAcl.setCanEdit("Team Only");
		given(this.accessControlHelper.getContactsAccessControl()).willReturn(contactsAcl);
		given(this.accessControlHelper.requiresTeamLookup("Team Only")).willReturn(true);
		List<Integer> teamUserIds = new java.util.ArrayList<>(List.of(CURRENT_USER_ID, 100));
		given(this.accessControlHelper.getTeamUserIds(CURRENT_USER_ID)).willReturn(teamUserIds);
		given(this.bulkValidateRepository
			.findPortalStatusByEmails(List.of(BulkValidateTestDataFactory.getDefaultContactEmail())))
			.willReturn(List.of());

		// When
		BulkValidateResponseBodyDto result = this.bulkValidateService.bulkValidate(request);

		// Then
		assertThat(result.getValidCount()).isEqualTo(1);
		assertThat(teamUserIds).containsExactly(CURRENT_USER_ID, 100);
		then(this.accessControlHelper).should().getTeamUserIds(CURRENT_USER_ID);
	}

}
