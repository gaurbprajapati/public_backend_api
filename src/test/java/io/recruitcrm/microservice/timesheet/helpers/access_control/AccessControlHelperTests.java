/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers.access_control;

import io.recruitcrm.entity.model.UserRole;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.CallLog;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Candidates;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Companies;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Contacts;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Deals;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.EmailTemplates;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Files;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Invoices;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Jobs;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Notes;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.PlacementBilling;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.TaskMeetings;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dao.team_member.TeamMemberJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.user_role.UserRoleJpaRepository;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AccessControlHelperTests {

	@Mock
	private AuthHolder authHolder;

	@Mock
	private UserRoleJpaRepository userRoleJpaRepository;

	@Mock
	private TeamMemberJpaRepository teamMemberJpaRepository;

	@InjectMocks
	private AccessControlHelper accessControlHelper;

	private static final Integer TEST_ROLE_ID = 1;

	private static final Integer TEST_USER_ID = 100;

	private static final String VALID_ACCESS_CONTROL_JSON = "{\"jobs\":{\"canview\":\"Everything\"},"
			+ "\"candidates\":{\"canview\":\"Team Only\"},\"companies\":{\"canview\":\"Owned Only\"},"
			+ "\"deals\":{\"canview\":\"Everything\"},\"contacts\":{},\"notes\":{},\"invoices\":{},"
			+ "\"placementbilling\":{},\"taskmeetings\":{},\"calllog\":{},\"files\":{},\"emailtemplates\":{}}";

	private static final String INVALID_JSON = "{invalid json}";

	@BeforeEach
	void setUp() {
		// Reset cached DTO before each test
		ReflectionTestUtils.setField(this.accessControlHelper, "cachedAccessControlDto", null);
	}

	// ===== getAccessControlDto() Tests =====

	@Test
	@DisplayName("Get access control DTO successfully with valid JSON")
	void testGetAccessControlDtoWithValidJsonReturnsDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Candidates result = this.accessControlHelper.getCandidatesAccessControl();
		Candidates result2 = this.accessControlHelper.getCandidatesAccessControl();

		// Then
		assertThat(result).isNotNull();
		assertThat(result2).isSameAs(result); // Verify caching
		then(this.userRoleJpaRepository).should().findById(TEST_ROLE_ID);
	}

	@Test
	@DisplayName("Get access control DTO caches the result")
	void testGetAccessControlDtoCachesResult() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		this.accessControlHelper.getCandidatesAccessControl();
		this.accessControlHelper.getJobsAccessControl();

		// Then
		then(this.userRoleJpaRepository).should().findById(TEST_ROLE_ID); // Should only
																			// be called
																			// once
	}

	@Test
	@DisplayName("Get access control DTO throws ResourceNotFoundException when UserRole not found")
	void testGetAccessControlDtoWhenUserRoleNotFoundThrowsResourceNotFoundException() {
		// Given
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.accessControlHelper.getCandidatesAccessControl())
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("UserRole not found for roleId: " + TEST_ROLE_ID);
	}

	@Test
	@DisplayName("Get access control DTO throws UnauthorizedAccessException when JSON is null")
	void testGetAccessControlDtoWhenJsonIsNullThrowsUnauthorizedAccessException() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(null);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When & Then
		assertThatThrownBy(() -> this.accessControlHelper.getCandidatesAccessControl())
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("User access JSON is empty or null");
	}

	@Test
	@DisplayName("Get access control DTO throws UnauthorizedAccessException when JSON is empty")
	void testGetAccessControlDtoWhenJsonIsEmptyThrowsUnauthorizedAccessException() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson("");
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When & Then
		assertThatThrownBy(() -> this.accessControlHelper.getCandidatesAccessControl())
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("User access JSON is empty or null");
	}

	@Test
	@DisplayName("Get access control DTO throws UnauthorizedAccessException when JSON parsing fails")
	void testGetAccessControlDtoWhenJsonParsingFailsThrowsUnauthorizedAccessException() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(INVALID_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When & Then
		assertThatThrownBy(() -> this.accessControlHelper.getCandidatesAccessControl())
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Error while parsing access control JSON");
	}

	// ===== Entity Access Control Getter Tests =====

	@Test
	@DisplayName("Get candidates access control returns correct DTO")
	void testGetCandidatesAccessControlReturnsCorrectDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Candidates result = this.accessControlHelper.getCandidatesAccessControl();

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getCanView()).isEqualTo("Team Only");
	}

	@Test
	@DisplayName("Get contacts access control returns correct DTO")
	void testGetContactsAccessControlReturnsCorrectDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Contacts result = this.accessControlHelper.getContactsAccessControl();

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Get jobs access control returns correct DTO")
	void testGetJobsAccessControlReturnsCorrectDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Jobs result = this.accessControlHelper.getJobsAccessControl();

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getCanView()).isEqualTo("Everything");
	}

	@Test
	@DisplayName("Get companies access control returns correct DTO")
	void testGetCompaniesAccessControlReturnsCorrectDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Companies result = this.accessControlHelper.getCompaniesAccessControl();

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getCanView()).isEqualTo("Owned Only");
	}

	@Test
	@DisplayName("Get deals access control returns correct DTO")
	void testGetDealsAccessControlReturnsCorrectDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Deals result = this.accessControlHelper.getDealsAccessControl();

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getCanView()).isEqualTo("Everything");
	}

	@Test
	@DisplayName("Get notes access control returns correct DTO")
	void testGetNotesAccessControlReturnsCorrectDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Notes result = this.accessControlHelper.getNotesAccessControl();

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Get invoices access control returns correct DTO")
	void testGetInvoicesAccessControlReturnsCorrectDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Invoices result = this.accessControlHelper.getInvoicesAccessControl();

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Get placement billing access control returns correct DTO")
	void testGetPlacementBillingAccessControlReturnsCorrectDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		PlacementBilling result = this.accessControlHelper.getPlacementBillingAccessControl();

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Get task meetings access control returns correct DTO")
	void testGetTaskMeetingsAccessControlReturnsCorrectDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		TaskMeetings result = this.accessControlHelper.getTaskMeetingsAccessControl();

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Get call log access control returns correct DTO")
	void testGetCallLogAccessControlReturnsCorrectDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		CallLog result = this.accessControlHelper.getCallLogAccessControl();

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Get files access control returns correct DTO")
	void testGetFilesAccessControlReturnsCorrectDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Files result = this.accessControlHelper.getFilesAccessControl();

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Get email templates access control returns correct DTO")
	void testGetEmailTemplatesAccessControlReturnsCorrectDto() {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(VALID_ACCESS_CONTROL_JSON);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		EmailTemplates result = this.accessControlHelper.getEmailTemplatesAccessControl();

		// Then
		assertThat(result).isNotNull();
	}

	// ===== buildJobsAccessControlCondition() Tests =====

	@ParameterizedTest(name = "{0}")
	@MethodSource("jobsAccessControlFalseConditionJsonProvider")
	@DisplayName("Build jobs access control condition returns false for invalid access JSON")
	void testBuildJobsAccessControlConditionReturnsFalseForInvalidAccessJson(String displayName, String json) {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		Field<Integer> jobIdField = DSL.field("job_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Condition result = this.accessControlHelper.buildJobsAccessControlCondition(ownerIdField, jobIdField);

		// Then
		assertThat(result).isEqualTo(DSL.falseCondition());
	}

	static Stream<Arguments> jobsAccessControlFalseConditionJsonProvider() {
		return Stream.of(Arguments.of("jobs access control is null", "{\"jobs\":null}"),
				Arguments.of("canView is null", "{\"jobs\":{}}"),
				Arguments.of("permission level is invalid", "{\"jobs\":{\"canview\":\"Invalid\"}}"), Arguments
					.of("permission level does not match any case", "{\"jobs\":{\"canview\":\"InvalidPermission\"}}"));
	}

	@Test
	@DisplayName("Build jobs access control condition returns true condition for Everything permission")
	void testBuildJobsAccessControlConditionForEverythingReturnsTrueCondition() {
		// Given
		String json = "{\"jobs\":{\"canview\":\"Everything\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		Field<Integer> jobIdField = DSL.field("job_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Condition result = this.accessControlHelper.buildJobsAccessControlCondition(ownerIdField, jobIdField);

		// Then
		assertThat(result).isEqualTo(DSL.trueCondition());
	}

	@Test
	@DisplayName("Build jobs access control condition returns owner condition for Owned Only permission")
	void testBuildJobsAccessControlConditionForOwnedOnlyReturnsOwnerCondition() {
		// Given
		String json = "{\"jobs\":{\"canview\":\"Owned Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		Field<Integer> jobIdField = DSL.field("job_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);

		// When
		Condition result = this.accessControlHelper.buildJobsAccessControlCondition(ownerIdField, jobIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build jobs access control condition returns owner condition for Team Only when no team members")
	void testBuildJobsAccessControlConditionForTeamOnlyWithNoTeamMembersReturnsOwnerCondition() {
		// Given
		String json = "{\"jobs\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		Field<Integer> jobIdField = DSL.field("job_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(new ArrayList<>());

		// When
		Condition result = this.accessControlHelper.buildJobsAccessControlCondition(ownerIdField, jobIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build jobs access control condition returns team condition for Team Only when team members exist")
	void testBuildJobsAccessControlConditionForTeamOnlyWithTeamMembersReturnsTeamCondition() {
		// Given
		String json = "{\"jobs\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		Field<Integer> jobIdField = DSL.field("job_id", Integer.class);
		List<Integer> teamUserIds = new ArrayList<>(List.of(200, 300));
		List<Integer> teamIds = List.of(10, 20);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);
		given(this.teamMemberJpaRepository.findAllTeamIdsByUserId(TEST_USER_ID)).willReturn(teamIds);

		// When
		Condition result = this.accessControlHelper.buildJobsAccessControlCondition(ownerIdField, jobIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
		assertThat(teamUserIds).containsExactly(200, 300, TEST_USER_ID);
	}

	@Test
	@DisplayName("Build jobs access control condition includes current user in team list when not present")
	void testBuildJobsAccessControlConditionForTeamOnlyIncludesCurrentUserWhenNotInTeam() {
		// Given
		String json = "{\"jobs\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		Field<Integer> jobIdField = DSL.field("job_id", Integer.class);
		List<Integer> teamUserIds = new ArrayList<>(List.of(200, 300)); // Current user
																		// not in list
		List<Integer> teamIds = List.of(10, 20);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);
		given(this.teamMemberJpaRepository.findAllTeamIdsByUserId(TEST_USER_ID)).willReturn(teamIds);

		// When
		Condition result = this.accessControlHelper.buildJobsAccessControlCondition(ownerIdField, jobIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build jobs access control condition returns owner condition when team IDs are empty")
	void testBuildJobsAccessControlConditionForTeamOnlyWithEmptyTeamIdsReturnsOwnerCondition() {
		// Given
		String json = "{\"jobs\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		Field<Integer> jobIdField = DSL.field("job_id", Integer.class);
		List<Integer> teamUserIds = new ArrayList<>(List.of(200, 300));
		List<Integer> teamIds = new ArrayList<>();
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);
		given(this.teamMemberJpaRepository.findAllTeamIdsByUserId(TEST_USER_ID)).willReturn(teamIds);

		// When
		Condition result = this.accessControlHelper.buildJobsAccessControlCondition(ownerIdField, jobIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "Yes", "No", "Nothing" })
	@DisplayName("Build jobs access control condition returns false for unhandled permission levels")
	void testBuildJobsAccessControlConditionForUnhandledPermissionReturnsFalse(String permissionLabel) {
		// Given
		String json = "{\"jobs\":{\"canview\":\"" + permissionLabel + "\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		Field<Integer> jobIdField = DSL.field("job_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);

		// When
		Condition result = this.accessControlHelper.buildJobsAccessControlCondition(ownerIdField, jobIdField);

		// Then
		assertThat(result).isEqualTo(DSL.falseCondition());
	}

	// ===== buildCompaniesAccessControlCondition() Tests =====

	@ParameterizedTest(name = "{0}")
	@MethodSource("companiesAccessControlFalseConditionJsonProvider")
	@DisplayName("Build companies access control condition returns false for invalid access JSON")
	void testBuildCompaniesAccessControlConditionReturnsFalseForInvalidAccessJson(String displayName, String json) {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Condition result = this.accessControlHelper.buildCompaniesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isEqualTo(DSL.falseCondition());
	}

	static Stream<Arguments> companiesAccessControlFalseConditionJsonProvider() {
		return Stream.of(Arguments.of("companies access control is null", "{\"companies\":null}"),
				Arguments.of("canView is null", "{\"companies\":{}}"),
				Arguments.of("permission level is invalid", "{\"companies\":{\"canview\":\"Invalid\"}}"));
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "Yes", "No", "Nothing" })
	@DisplayName("Build companies access control condition returns false for unhandled permission levels")
	void testBuildCompaniesAccessControlConditionForUnhandledPermissionReturnsFalse(String permissionLabel) {
		// Given
		String json = "{\"companies\":{\"canview\":\"" + permissionLabel + "\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);

		// When
		Condition result = this.accessControlHelper.buildCompaniesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build companies access control condition returns true condition for Everything permission")
	void testBuildCompaniesAccessControlConditionForEverythingReturnsTrueCondition() {
		// Given
		String json = "{\"companies\":{\"canview\":\"Everything\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Condition result = this.accessControlHelper.buildCompaniesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isEqualTo(DSL.trueCondition());
	}

	@Test
	@DisplayName("Build companies access control condition returns owner condition for Owned Only permission")
	void testBuildCompaniesAccessControlConditionForOwnedOnlyReturnsOwnerCondition() {
		// Given
		String json = "{\"companies\":{\"canview\":\"Owned Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);

		// When
		Condition result = this.accessControlHelper.buildCompaniesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build companies access control condition returns owner condition for Team Only when no team members")
	void testBuildCompaniesAccessControlConditionForTeamOnlyWithNoTeamMembersReturnsOwnerCondition() {
		// Given
		String json = "{\"companies\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(new ArrayList<>());

		// When
		Condition result = this.accessControlHelper.buildCompaniesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build companies access control condition returns team condition for Team Only when team members exist")
	void testBuildCompaniesAccessControlConditionForTeamOnlyWithTeamMembersReturnsTeamCondition() {
		// Given
		String json = "{\"companies\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		List<Integer> teamUserIds = new ArrayList<>(List.of(200, 300));
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);

		// When
		Condition result = this.accessControlHelper.buildCompaniesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
		assertThat(teamUserIds).containsExactly(200, 300, TEST_USER_ID);
	}

	@Test
	@DisplayName("Build companies access control condition for Team Only when current user already in team list")
	void testBuildCompaniesAccessControlConditionForTeamOnlyWhenCurrentUserAlreadyInTeamList() {
		// Given
		String json = "{\"companies\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		List<Integer> teamUserIds = new ArrayList<>(List.of(TEST_USER_ID, 200, 300));
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);

		// When
		Condition result = this.accessControlHelper.buildCompaniesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
		assertThat(teamUserIds).containsExactly(TEST_USER_ID, 200, 300);
	}

	// ===== buildDealsAccessControlCondition() Tests =====

	@ParameterizedTest(name = "{0}")
	@MethodSource("dealsAccessControlFalseConditionJsonProvider")
	@DisplayName("Build deals access control condition returns false for invalid access JSON")
	void testBuildDealsAccessControlConditionReturnsFalseForInvalidAccessJson(String displayName, String json) {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Condition result = this.accessControlHelper.buildDealsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isEqualTo(DSL.falseCondition());
	}

	static Stream<Arguments> dealsAccessControlFalseConditionJsonProvider() {
		return Stream.of(Arguments.of("deals access control is null", "{\"deals\":null}"),
				Arguments.of("canView is null", "{\"deals\":{}}"),
				Arguments.of("permission level is invalid", "{\"deals\":{\"canview\":\"Invalid\"}}"));
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "Yes", "No", "Nothing" })
	@DisplayName("Build deals access control condition returns false for unhandled permission levels")
	void testBuildDealsAccessControlConditionForUnhandledPermissionReturnsFalse(String permissionLabel) {
		// Given
		String json = "{\"deals\":{\"canview\":\"" + permissionLabel + "\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);

		// When
		Condition result = this.accessControlHelper.buildDealsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build deals access control condition returns true condition for Everything permission")
	void testBuildDealsAccessControlConditionForEverythingReturnsTrueCondition() {
		// Given
		String json = "{\"deals\":{\"canview\":\"Everything\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Condition result = this.accessControlHelper.buildDealsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isEqualTo(DSL.trueCondition());
	}

	@Test
	@DisplayName("Build deals access control condition returns owner condition for Owned Only permission")
	void testBuildDealsAccessControlConditionForOwnedOnlyReturnsOwnerCondition() {
		// Given
		String json = "{\"deals\":{\"canview\":\"Owned Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);

		// When
		Condition result = this.accessControlHelper.buildDealsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build deals access control condition returns owner condition for Team Only when no team members")
	void testBuildDealsAccessControlConditionForTeamOnlyWithNoTeamMembersReturnsOwnerCondition() {
		// Given
		String json = "{\"deals\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(new ArrayList<>());

		// When
		Condition result = this.accessControlHelper.buildDealsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build deals access control condition returns team condition for Team Only when team members exist")
	void testBuildDealsAccessControlConditionForTeamOnlyWithTeamMembersReturnsTeamCondition() {
		// Given
		String json = "{\"deals\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		List<Integer> teamUserIds = new ArrayList<>(List.of(200, 300));
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);

		// When
		Condition result = this.accessControlHelper.buildDealsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
		assertThat(teamUserIds).containsExactly(200, 300, TEST_USER_ID);
	}

	@Test
	@DisplayName("Build deals access control condition for Team Only when current user already in team list")
	void testBuildDealsAccessControlConditionForTeamOnlyWhenCurrentUserAlreadyInTeamList() {
		// Given
		String json = "{\"deals\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		List<Integer> teamUserIds = new ArrayList<>(List.of(TEST_USER_ID, 200, 300));
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);

		// When
		Condition result = this.accessControlHelper.buildDealsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
		assertThat(teamUserIds).containsExactly(TEST_USER_ID, 200, 300);
	}

	// ===== buildContactsAccessControlCondition() Tests =====

	@ParameterizedTest(name = "{0}")
	@MethodSource("contactsAccessControlFalseConditionJsonProvider")
	@DisplayName("Build contacts access control condition returns false for invalid access JSON")
	void testBuildContactsAccessControlConditionReturnsFalseForInvalidAccessJson(String displayName, String json) {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Condition result = this.accessControlHelper.buildContactsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isEqualTo(DSL.falseCondition());
	}

	static Stream<Arguments> contactsAccessControlFalseConditionJsonProvider() {
		return Stream.of(Arguments.of("contacts access control is null", "{\"contacts\":null}"),
				Arguments.of("canView is null", "{\"contacts\":{}}"),
				Arguments.of("permission level is invalid", "{\"contacts\":{\"canview\":\"Invalid\"}}"));
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "Yes", "No", "Nothing" })
	@DisplayName("Build contacts access control condition returns false for unhandled permission levels")
	void testBuildContactsAccessControlConditionForUnhandledPermissionReturnsFalse(String permissionLabel) {
		// Given
		String json = "{\"contacts\":{\"canview\":\"" + permissionLabel + "\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);

		// When
		Condition result = this.accessControlHelper.buildContactsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build contacts access control condition returns true condition for Everything permission")
	void testBuildContactsAccessControlConditionForEverythingReturnsTrueCondition() {
		// Given
		String json = "{\"contacts\":{\"canview\":\"Everything\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Condition result = this.accessControlHelper.buildContactsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isEqualTo(DSL.trueCondition());
	}

	@Test
	@DisplayName("Build contacts access control condition returns owner condition for Owned Only permission")
	void testBuildContactsAccessControlConditionForOwnedOnlyReturnsOwnerCondition() {
		// Given
		String json = "{\"contacts\":{\"canview\":\"Owned Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);

		// When
		Condition result = this.accessControlHelper.buildContactsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build contacts access control condition returns owner condition for Team Only when no team members")
	void testBuildContactsAccessControlConditionForTeamOnlyWithNoTeamMembersReturnsOwnerCondition() {
		// Given
		String json = "{\"contacts\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(new ArrayList<>());

		// When
		Condition result = this.accessControlHelper.buildContactsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build contacts access control condition returns team condition for Team Only when team members exist")
	void testBuildContactsAccessControlConditionForTeamOnlyWithTeamMembersReturnsTeamCondition() {
		// Given
		String json = "{\"contacts\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		List<Integer> teamUserIds = new ArrayList<>(List.of(200, 300));
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);

		// When
		Condition result = this.accessControlHelper.buildContactsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
		assertThat(teamUserIds).containsExactly(200, 300, TEST_USER_ID);
	}

	@Test
	@DisplayName("Build contacts access control condition for Team Only when current user already in team list")
	void testBuildContactsAccessControlConditionForTeamOnlyWhenCurrentUserAlreadyInTeamList() {
		// Given
		String json = "{\"contacts\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		List<Integer> teamUserIds = new ArrayList<>(List.of(TEST_USER_ID, 200, 300));
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);

		// When
		Condition result = this.accessControlHelper.buildContactsAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
		assertThat(teamUserIds).containsExactly(TEST_USER_ID, 200, 300);
	}

	// ===== buildCandidatesAccessControlCondition() Tests =====

	@ParameterizedTest(name = "{0}")
	@MethodSource("candidatesAccessControlFalseConditionJsonProvider")
	@DisplayName("Build candidates access control condition returns false for invalid access JSON")
	void testBuildCandidatesAccessControlConditionReturnsFalseForInvalidAccessJson(String displayName, String json) {
		// Given
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Condition result = this.accessControlHelper.buildCandidatesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isEqualTo(DSL.falseCondition());
	}

	static Stream<Arguments> candidatesAccessControlFalseConditionJsonProvider() {
		return Stream.of(Arguments.of("candidates access control is null", "{\"candidates\":null}"),
				Arguments.of("canView is null", "{\"candidates\":{}}"),
				Arguments.of("permission level is invalid", "{\"candidates\":{\"canview\":\"Invalid\"}}"));
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "Yes", "No", "Nothing" })
	@DisplayName("Build candidates access control condition returns false for unhandled permission levels")
	void testBuildCandidatesAccessControlConditionForUnhandledPermissionReturnsFalse(String permissionLabel) {
		// Given
		String json = "{\"candidates\":{\"canview\":\"" + permissionLabel + "\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);

		// When
		Condition result = this.accessControlHelper.buildCandidatesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build candidates access control condition returns true condition for Everything permission")
	void testBuildCandidatesAccessControlConditionForEverythingReturnsTrueCondition() {
		// Given
		String json = "{\"candidates\":{\"canview\":\"Everything\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));

		// When
		Condition result = this.accessControlHelper.buildCandidatesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isEqualTo(DSL.trueCondition());
	}

	@Test
	@DisplayName("Build candidates access control condition returns owner condition for Owned Only permission")
	void testBuildCandidatesAccessControlConditionForOwnedOnlyReturnsOwnerCondition() {
		// Given
		String json = "{\"candidates\":{\"canview\":\"Owned Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);

		// When
		Condition result = this.accessControlHelper.buildCandidatesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build candidates access control condition returns owner condition for Team Only when no team members")
	void testBuildCandidatesAccessControlConditionForTeamOnlyWithNoTeamMembersReturnsOwnerCondition() {
		// Given
		String json = "{\"candidates\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(new ArrayList<>());

		// When
		Condition result = this.accessControlHelper.buildCandidatesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("Build candidates access control condition returns team condition for Team Only when team members exist")
	void testBuildCandidatesAccessControlConditionForTeamOnlyWithTeamMembersReturnsTeamCondition() {
		// Given
		String json = "{\"candidates\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		List<Integer> teamUserIds = new ArrayList<>(List.of(200, 300));
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);

		// When
		Condition result = this.accessControlHelper.buildCandidatesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
		assertThat(teamUserIds).containsExactly(200, 300, TEST_USER_ID);
	}

	@Test
	@DisplayName("Build candidates access control condition for Team Only when current user already in team list")
	void testBuildCandidatesAccessControlConditionForTeamOnlyWhenCurrentUserAlreadyInTeamList() {
		// Given
		String json = "{\"candidates\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		List<Integer> teamUserIds = new ArrayList<>(List.of(TEST_USER_ID, 200, 300));
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);

		// When
		Condition result = this.accessControlHelper.buildCandidatesAccessControlCondition(ownerIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
		assertThat(teamUserIds).containsExactly(TEST_USER_ID, 200, 300);
	}

	// ===== Edge Cases and Branch Coverage =====

	@Test
	@DisplayName("Build jobs access control condition for Team Only when current user already in team list")
	void testBuildJobsAccessControlConditionForTeamOnlyWhenCurrentUserAlreadyInTeamList() {
		// Given
		String json = "{\"jobs\":{\"canview\":\"Team Only\"}}";
		UserRole userRole = new UserRole();
		userRole.setUserAccessJson(json);
		Field<Integer> ownerIdField = DSL.field("owner_id", Integer.class);
		Field<Integer> jobIdField = DSL.field("job_id", Integer.class);
		List<Integer> teamUserIds = new ArrayList<>(List.of(TEST_USER_ID, 200, 300)); // Current
																						// user
																						// already
																						// in
																						// list
		List<Integer> teamIds = List.of(10, 20);
		given(this.authHolder.getAuthenticationPrincipalRoleIdentifier()).willReturn(TEST_ROLE_ID);
		given(this.userRoleJpaRepository.findById(TEST_ROLE_ID)).willReturn(Optional.of(userRole));
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(TEST_USER_ID);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);
		given(this.teamMemberJpaRepository.findAllTeamIdsByUserId(TEST_USER_ID)).willReturn(teamIds);

		// When
		Condition result = this.accessControlHelper.buildJobsAccessControlCondition(ownerIdField, jobIdField);

		// Then
		assertThat(result).isNotNull().isNotEqualTo(DSL.trueCondition()).isNotEqualTo(DSL.falseCondition());
	}

	// ===== getTeamUserIds() Tests =====

	@Test
	@DisplayName("Get team user IDs delegates to the team member repository")
	void testGetTeamUserIdsDelegatesToRepository() {
		// Given
		List<Integer> teamUserIds = List.of(TEST_USER_ID, 200, 300);
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(teamUserIds);

		// When
		List<Integer> result = this.accessControlHelper.getTeamUserIds(TEST_USER_ID);

		// Then
		assertThat(result).isEqualTo(teamUserIds);
		then(this.teamMemberJpaRepository).should().findAllUserIdsByTeamMembership(TEST_USER_ID);
	}

	@Test
	@DisplayName("Get team user IDs returns empty list when user has no team membership")
	void testGetTeamUserIdsReturnsEmptyWhenNoMembership() {
		// Given
		given(this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(TEST_USER_ID)).willReturn(List.of());

		// When
		List<Integer> result = this.accessControlHelper.getTeamUserIds(TEST_USER_ID);

		// Then
		assertThat(result).isEmpty();
	}

	// ===== resolvePermission() Tests =====

	@Test
	@DisplayName("Resolve permission returns false when permission level string is null")
	void testResolvePermissionNullStringReturnsFalse() {
		// When
		boolean result = this.accessControlHelper.resolvePermission(null, 1, TEST_USER_ID, List.of());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Resolve permission returns false when permission level string is unparseable")
	void testResolvePermissionInvalidStringReturnsFalse() {
		// When
		boolean result = this.accessControlHelper.resolvePermission("Garbage", 1, TEST_USER_ID, List.of());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Resolve permission returns true for Everything regardless of owner")
	void testResolvePermissionEverythingReturnsTrue() {
		// When
		boolean result = this.accessControlHelper.resolvePermission("Everything", 999, TEST_USER_ID, List.of());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Resolve permission returns true for Owned Only when current user owns the record")
	void testResolvePermissionOwnedOnlyOwnerMatchReturnsTrue() {
		// When
		boolean result = this.accessControlHelper.resolvePermission("Owned Only", TEST_USER_ID, TEST_USER_ID,
				List.of());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Resolve permission returns false for Owned Only when current user does not own the record")
	void testResolvePermissionOwnedOnlyOwnerMismatchReturnsFalse() {
		// When
		boolean result = this.accessControlHelper.resolvePermission("Owned Only", 999, TEST_USER_ID, List.of());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Resolve permission returns true for Team Only when owner is within team user IDs")
	void testResolvePermissionTeamOnlyOwnerInTeamReturnsTrue() {
		// When
		boolean result = this.accessControlHelper.resolvePermission("Team Only", 200, TEST_USER_ID,
				List.of(TEST_USER_ID, 200));

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Resolve permission returns false for Team Only when owner is not within team user IDs")
	void testResolvePermissionTeamOnlyOwnerNotInTeamReturnsFalse() {
		// When
		boolean result = this.accessControlHelper.resolvePermission("Team Only", 999, TEST_USER_ID,
				List.of(TEST_USER_ID, 200));

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Resolve permission returns false for Team Only when team user IDs is null")
	void testResolvePermissionTeamOnlyNullTeamIdsReturnsFalse() {
		// When
		boolean result = this.accessControlHelper.resolvePermission("Team Only", 200, TEST_USER_ID, null);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Resolve permission returns false for a non-record permission level such as Yes")
	void testResolvePermissionNonRecordLevelReturnsFalse() {
		// When
		boolean result = this.accessControlHelper.resolvePermission("Yes", TEST_USER_ID, TEST_USER_ID, List.of());

		// Then
		assertThat(result).isFalse();
	}

	// ===== requiresTeamLookup() Tests =====

	@Test
	@DisplayName("Requires team lookup returns true only for Team Only")
	void testRequiresTeamLookupTeamOnlyReturnsTrue() {
		// When & Then
		assertThat(this.accessControlHelper.requiresTeamLookup("Team Only")).isTrue();
	}

	@Test
	@DisplayName("Requires team lookup returns false for a non-Team Only level")
	void testRequiresTeamLookupOwnedOnlyReturnsFalse() {
		// When & Then
		assertThat(this.accessControlHelper.requiresTeamLookup("Owned Only")).isFalse();
	}

	@Test
	@DisplayName("Requires team lookup returns false for an unparseable level")
	void testRequiresTeamLookupInvalidReturnsFalse() {
		// When & Then
		assertThat(this.accessControlHelper.requiresTeamLookup("Garbage")).isFalse();
	}

}