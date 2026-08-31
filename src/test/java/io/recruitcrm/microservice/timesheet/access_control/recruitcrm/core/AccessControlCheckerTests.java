/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.AccessControlDto;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.access_control.UnknownAccessLevelException;
import io.recruitcrm.microservice.timesheet.testdata.AccessControlCheckerTestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccessControlChecker Tests")
class AccessControlCheckerTests {

	@Mock
	private AccessControlConfigHolder accessControlConfigHolder;

	@Mock
	private AccessLevelHandler accessLevelHandler;

	@Mock
	private AuthHolder authHolder;

	@Mock
	private EntityManager entityManager;

	@InjectMocks
	private AccessControlChecker accessControlChecker;

	private AccessControlDto accessControlDto;

	@BeforeEach
	void setUp() {
		this.accessControlDto = new AccessControlDto();
	}

	@Test
	@DisplayName("allows(GlobalPermission, YES) should succeed when configuration grants YES")
	void testAllowsGlobalPermissionYesWhenConfiguredYesSucceeds() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED,
				GlobalPermission.REPORTS))
			.willReturn(PermissionLevel.YES);

		// When
		this.accessControlChecker.allows(GlobalPermission.REPORTS, PermissionLevel.YES);

		// Then
		then(this.accessControlConfigHolder).should().getAccessControlDto();
		then(this.accessLevelHandler).should()
			.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED, GlobalPermission.REPORTS);
	}

	@Test
	@DisplayName("allows(GlobalPermission, YES) should throw when configuration is NO")
	void testAllowsGlobalPermissionYesWhenConfiguredNoThrowsUnauthorizedAccessException() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED,
				GlobalPermission.REPORTS))
			.willReturn(PermissionLevel.NO);

		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(GlobalPermission.REPORTS, PermissionLevel.YES))
			.isInstanceOf(UnauthorizedAccessException.class);
	}

	@Test
	@DisplayName("allows(GlobalPermission, level) should throw when requested level is not YES or NO")
	void testAllowsGlobalPermissionInvalidRequestedLevelThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(GlobalPermission.REPORTS, PermissionLevel.EVERYTHING))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(AccessControlCheckerTestDataFactory.MSG_GLOBAL_LEVELS_YES_OR_NO_ONLY);
	}

	@Test
	@DisplayName("allows(Entity.GLOBAL, ALLOWED, null, GlobalPermission) should delegate to global check")
	void testAllowsEntityGlobalFourArgSimplifiedSucceeds() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED,
				GlobalPermission.REPORTS))
			.willReturn(PermissionLevel.YES);

		// When
		this.accessControlChecker.allows(Entity.GLOBAL, Permission.ALLOWED, null, GlobalPermission.REPORTS);

		// Then
		then(this.accessControlConfigHolder).should().getAccessControlDto();
		then(this.accessLevelHandler).should()
			.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED, GlobalPermission.REPORTS);
	}

	@Test
	@DisplayName("allows(Entity, Permission, ownerId) should succeed for entity when access allowed")
	void testAllowsEntityThreeArgWhenAccessAllowedSucceeds() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(AccessControlCheckerTestDataFactory.CURRENT_USER_ID);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.OWNED_ONLY);

		// When
		this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW,
				AccessControlCheckerTestDataFactory.CURRENT_USER_ID);

		// Then
		then(this.accessControlConfigHolder).should().getAccessControlDto();
		then(this.authHolder).should().getAuthenticationPrincipalUniqueIdentifier();
		then(this.accessLevelHandler).should()
			.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW, null);
	}

	@Test
	@DisplayName("allows(Entity.GLOBAL, Permission, ownerId) should throw directing caller to four-arg overload")
	void testAllowsEntityThreeArgGlobalThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.GLOBAL, Permission.ALLOWED,
				AccessControlCheckerTestDataFactory.OWNER_ID))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(AccessControlCheckerTestDataFactory.MSG_USE_GLOBAL_FOUR_ARG_SIMPLIFIED);
	}

	@Test
	@DisplayName("allows(Entity, Permission, PermissionLevel, ownerId) should throw when entity is GLOBAL")
	void testAllowsEntityFourArgWithLevelGlobalThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(
				() -> this.accessControlChecker.allows(Entity.GLOBAL, Permission.ALLOWED, PermissionLevel.YES, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(AccessControlCheckerTestDataFactory.MSG_USE_GLOBAL_FIVE_ARG_WITH_LEVEL);
	}

	@Test
	@DisplayName("allows(Entity, Permission, PermissionLevel, ownerId, null) should succeed when owned-only matches")
	void testAllowsEntityFourArgOwnedOnlyOwnerMatchesSucceeds() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(AccessControlCheckerTestDataFactory.CURRENT_USER_ID);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.OWNED_ONLY);

		// When
		this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW, PermissionLevel.OWNED_ONLY,
				AccessControlCheckerTestDataFactory.CURRENT_USER_ID, null);

		// Then
		then(this.accessControlConfigHolder).should().getAccessControlDto();
		then(this.authHolder).should().getAuthenticationPrincipalUniqueIdentifier();
		then(this.accessLevelHandler).should()
			.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW, null);
	}

	@Test
	@DisplayName("allows(Entity, Permission, PermissionLevel, ownerId, null) should succeed for team-only same team")
	void testAllowsEntityFourArgTeamOnlySameTeamSucceeds() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(AccessControlCheckerTestDataFactory.CURRENT_USER_ID);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.TEAM_ONLY);
		TypedQuery<Integer> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(anyString(), eq(Integer.class))).willReturn(mockQuery);
		given(mockQuery.setParameter(anyString(), any())).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(AccessControlCheckerTestDataFactory.CURRENT_USER_ID,
				AccessControlCheckerTestDataFactory.OWNER_ID));

		// When
		this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW, PermissionLevel.TEAM_ONLY,
				AccessControlCheckerTestDataFactory.OWNER_ID, null);

		// Then
		then(this.accessControlConfigHolder).should().getAccessControlDto();
		then(this.authHolder).should().getAuthenticationPrincipalUniqueIdentifier();
		then(this.accessLevelHandler).should()
			.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW, null);
		then(this.entityManager).should().createQuery(anyString(), eq(Integer.class));
	}

	@Test
	@DisplayName("allows(Entity, Permission, PermissionLevel, ownerId, null) should succeed for EVERYTHING level")
	void testAllowsEntityFourArgEverythingSucceeds() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.EVERYTHING);

		// When
		this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW, PermissionLevel.EVERYTHING,
				AccessControlCheckerTestDataFactory.OWNER_ID, null);

		// Then
		then(this.accessControlConfigHolder).should().getAccessControlDto();
		then(this.accessLevelHandler).should()
			.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW, null);
	}

	@Test
	@DisplayName("allows(Entity, Permission, PermissionLevel, ownerId, null) should throw when OWNED_ONLY and different owner")
	void testAllowsEntityFourArgOwnedOnlyDifferentOwnerThrowsUnauthorizedAccessException() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(AccessControlCheckerTestDataFactory.CURRENT_USER_ID);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.OWNED_ONLY);

		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW,
				PermissionLevel.OWNED_ONLY, AccessControlCheckerTestDataFactory.OTHER_OWNER_ID, null))
			.isInstanceOf(UnauthorizedAccessException.class);
	}

	@Test
	@DisplayName("allows(Entity, Permission, PermissionLevel, ownerId, GlobalPermission) should throw when GLOBAL has owner ID")
	void testAllowsEntityFiveArgGlobalWithOwnerIdThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.GLOBAL, Permission.ALLOWED,
				PermissionLevel.YES, AccessControlCheckerTestDataFactory.OWNER_ID, GlobalPermission.REPORTS))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(AccessControlCheckerTestDataFactory.MSG_GLOBAL_NO_OWNER_ID);
	}

	@Test
	@DisplayName("allows(Entity, Permission, PermissionLevel, ownerId, GlobalPermission) should throw when GLOBAL permission is not ALLOWED")
	void testAllowsEntityFiveArgGlobalNonAllowedPermissionThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.GLOBAL, Permission.CAN_VIEW,
				PermissionLevel.YES, null, GlobalPermission.REPORTS))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(AccessControlCheckerTestDataFactory.MSG_GLOBAL_ONLY_ALLOWED_PERMISSION);
	}

	@Test
	@DisplayName("allows(Entity, Permission, PermissionLevel, ownerId, GlobalPermission) should throw when GLOBAL misses GlobalPermission")
	void testAllowsEntityFiveArgGlobalMissingGlobalPermissionThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.GLOBAL, Permission.ALLOWED,
				PermissionLevel.YES, null, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(AccessControlCheckerTestDataFactory.MSG_GLOBAL_PERMISSION_REQUIRED);
	}

	@Test
	@DisplayName("allows(Entity, Permission, PermissionLevel, ownerId, GlobalPermission) should throw when entity is not GLOBAL but GlobalPermission set")
	void testAllowsEntityFiveArgNonGlobalWithGlobalPermissionThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW,
				PermissionLevel.OWNED_ONLY, AccessControlCheckerTestDataFactory.OWNER_ID, GlobalPermission.REPORTS))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(AccessControlCheckerTestDataFactory.MSG_NON_GLOBAL_NO_GLOBAL_PERMISSION);
	}

	@Test
	@DisplayName("allows(Entity, Permission, ownerId, GlobalPermission) four-arg should throw when entity is not GLOBAL but GlobalPermission set")
	void testAllowsEntityFourArgNonGlobalWithGlobalPermissionThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW,
				AccessControlCheckerTestDataFactory.OWNER_ID, GlobalPermission.REPORTS))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(AccessControlCheckerTestDataFactory.MSG_NON_GLOBAL_NO_GLOBAL_PERMISSION);
	}

	@Test
	@DisplayName("getAccessLevel failure should propagate UnknownAccessLevelException")
	void testAllowsPropagatesUnknownAccessLevelExceptionFromHandler() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willThrow(new UnknownAccessLevelException("unknown"));

		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW,
				AccessControlCheckerTestDataFactory.OWNER_ID))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("unknown");
	}

	@Test
	@DisplayName("allows(GLOBAL, ALLOWED, ownerId, GlobalPermission) four-arg should throw when owner ID set")
	void testAllowsEntityGlobalFourArgWithOwnerIdThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.GLOBAL, Permission.ALLOWED,
				AccessControlCheckerTestDataFactory.OWNER_ID, GlobalPermission.REPORTS))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(AccessControlCheckerTestDataFactory.MSG_GLOBAL_NO_OWNER_ID);
	}

	@Test
	@DisplayName("allows(GLOBAL, non-ALLOWED, null, GlobalPermission) four-arg should throw")
	void testAllowsEntityGlobalFourArgNonAllowedPermissionThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.GLOBAL, Permission.CAN_VIEW, (Integer) null,
				GlobalPermission.REPORTS))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(AccessControlCheckerTestDataFactory.MSG_GLOBAL_ONLY_ALLOWED_PERMISSION);
	}

	@Test
	@DisplayName("allows(GLOBAL, ALLOWED, null, null) four-arg should throw when GlobalPermission missing")
	void testAllowsEntityGlobalFourArgMissingGlobalPermissionThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.GLOBAL, Permission.ALLOWED, (Integer) null,
				(GlobalPermission) null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(AccessControlCheckerTestDataFactory.MSG_GLOBAL_PERMISSION_REQUIRED);
	}

	@Test
	@DisplayName("allows(Entity, Permission, PermissionLevel, ownerId) should delegate when entity is not GLOBAL")
	void testAllowsEntityFourArgWithLevelNonGlobalDelegatesAndSucceeds() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(AccessControlCheckerTestDataFactory.CURRENT_USER_ID);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.OWNED_ONLY);

		// When
		this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW, PermissionLevel.OWNED_ONLY,
				AccessControlCheckerTestDataFactory.CURRENT_USER_ID);

		// Then
		then(this.accessControlConfigHolder).should().getAccessControlDto();
		then(this.accessLevelHandler).should()
			.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW, null);
	}

	@Test
	@DisplayName("allows(GLOBAL, ALLOWED, YES, null, GlobalPermission) five-arg should succeed")
	void testAllowsEntityFiveArgGlobalYesRequestedLevelSucceeds() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED,
				GlobalPermission.REPORTS))
			.willReturn(PermissionLevel.YES);

		// When
		this.accessControlChecker.allows(Entity.GLOBAL, Permission.ALLOWED, PermissionLevel.YES, null,
				GlobalPermission.REPORTS);

		// Then
		then(this.accessLevelHandler).should()
			.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED, GlobalPermission.REPORTS);
	}

	@Test
	@DisplayName("allows(GLOBAL, ALLOWED, NO, null, GlobalPermission) five-arg should succeed when config is YES")
	void testAllowsEntityFiveArgGlobalNoRequestedLevelSucceedsWhenConfiguredYes() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED,
				GlobalPermission.REPORTS))
			.willReturn(PermissionLevel.YES);

		// When
		this.accessControlChecker.allows(Entity.GLOBAL, Permission.ALLOWED, PermissionLevel.NO, null,
				GlobalPermission.REPORTS);

		// Then
		then(this.accessLevelHandler).should()
			.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED, GlobalPermission.REPORTS);
	}

	@Test
	@DisplayName("allows(GlobalPermission, NO) should succeed when configuration grants YES")
	void testAllowsGlobalPermissionNoWhenConfiguredYesSucceeds() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED,
				GlobalPermission.REPORTS))
			.willReturn(PermissionLevel.YES);

		// When
		this.accessControlChecker.allows(GlobalPermission.REPORTS, PermissionLevel.NO);

		// Then
		then(this.accessLevelHandler).should()
			.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED, GlobalPermission.REPORTS);
	}

	@Test
	@DisplayName("allows(GlobalPermission, NO) should throw when configuration is NO")
	void testAllowsGlobalPermissionNoWhenConfiguredNoThrowsUnauthorizedAccessException() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED,
				GlobalPermission.REPORTS))
			.willReturn(PermissionLevel.NO);

		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(GlobalPermission.REPORTS, PermissionLevel.NO))
			.isInstanceOf(UnauthorizedAccessException.class);
	}

	@Test
	@DisplayName("allows(GlobalPermission, level) should reject NOTHING like other non YES NO levels")
	void testAllowsGlobalPermissionNothingRequestedThrowsIllegalArgumentException() {
		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(GlobalPermission.REPORTS, PermissionLevel.NOTHING))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(AccessControlCheckerTestDataFactory.MSG_GLOBAL_LEVELS_YES_OR_NO_ONLY);
	}

	@Test
	@DisplayName("allows(Entity, Permission, ownerId) three-arg should throw when access denied")
	void testAllowsEntityThreeArgWhenAccessDeniedThrowsUnauthorizedAccessException() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(AccessControlCheckerTestDataFactory.CURRENT_USER_ID);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.OWNED_ONLY);

		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW,
				AccessControlCheckerTestDataFactory.OTHER_OWNER_ID))
			.isInstanceOf(UnauthorizedAccessException.class);
	}

	@Test
	@DisplayName("allows(Entity, Permission, PermissionLevel, ownerId, null) TEAM_ONLY should deny when users not same team")
	void testAllowsEntityFourArgTeamOnlyDifferentTeamThrowsUnauthorizedAccessException() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(AccessControlCheckerTestDataFactory.CURRENT_USER_ID);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.TEAM_ONLY);
		TypedQuery<Integer> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(anyString(), eq(Integer.class))).willReturn(mockQuery);
		given(mockQuery.setParameter(anyString(), any())).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(AccessControlCheckerTestDataFactory.CURRENT_USER_ID));

		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW,
				PermissionLevel.TEAM_ONLY, AccessControlCheckerTestDataFactory.OWNER_ID, null))
			.isInstanceOf(UnauthorizedAccessException.class);
	}

	@Test
	@DisplayName("allows(Entity, CAN_ADD, ownerId) should succeed when configuration level is YES")
	void testAllowsEntityThreeArgCanAddWhenConfiguredYesSucceeds() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.JOBS, this.accessControlDto, Permission.CAN_ADD, null))
			.willReturn(PermissionLevel.YES);

		// When
		this.accessControlChecker.allows(Entity.JOBS, Permission.CAN_ADD, AccessControlCheckerTestDataFactory.OWNER_ID);

		// Then
		then(this.accessLevelHandler).should()
			.getAccessLevel(Entity.JOBS, this.accessControlDto, Permission.CAN_ADD, null);
	}

	@Test
	@DisplayName("allows(Entity, CAN_ADD, ownerId) should throw when configuration level is not YES")
	void testAllowsEntityThreeArgCanAddWhenConfiguredNoThrowsUnauthorizedAccessException() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.JOBS, this.accessControlDto, Permission.CAN_ADD, null))
			.willReturn(PermissionLevel.NO);

		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.JOBS, Permission.CAN_ADD,
				AccessControlCheckerTestDataFactory.OWNER_ID))
			.isInstanceOf(UnauthorizedAccessException.class);
	}

	@Test
	@DisplayName("allows(GlobalPermission, YES) should propagate UnknownAccessLevelException from handler")
	void testAllowsGlobalPermissionYesPropagatesUnknownAccessLevelExceptionFromHandler() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.GLOBAL, this.accessControlDto, Permission.ALLOWED,
				GlobalPermission.REPORTS))
			.willThrow(new UnknownAccessLevelException("global unsupported"));

		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(GlobalPermission.REPORTS, PermissionLevel.YES))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("global unsupported");
	}

	@Test
	@DisplayName("allows(Entity, CAN_VIEW, PermissionLevel, null owner) should deny OWNED_ONLY")
	void testAllowsEntityFourArgOwnedOnlyNullOwnerThrowsUnauthorizedAccessException() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(AccessControlCheckerTestDataFactory.CURRENT_USER_ID);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.OWNED_ONLY);

		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW,
				PermissionLevel.OWNED_ONLY, null, null))
			.isInstanceOf(UnauthorizedAccessException.class);
	}

	@Test
	@DisplayName("UnknownAccessLevelException from handler should propagate through five-arg entity path")
	void testAllowsEntityFiveArgPropagatesUnknownAccessLevelExceptionFromHandler() {
		// Given
		given(this.accessControlConfigHolder.getAccessControlDto()).willReturn(this.accessControlDto);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willThrow(new UnknownAccessLevelException("bad entity"));

		// When & Then
		assertThatThrownBy(() -> this.accessControlChecker.allows(Entity.CANDIDATES, Permission.CAN_VIEW,
				PermissionLevel.EVERYTHING, AccessControlCheckerTestDataFactory.OWNER_ID, null))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("bad entity");
	}

}
