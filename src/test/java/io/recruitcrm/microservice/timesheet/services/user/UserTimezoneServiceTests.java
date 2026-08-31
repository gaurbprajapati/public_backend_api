/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.repositories.user.UserRepository;
import io.recruitcrm.microservice.timesheet.testdata.UserTimezoneServiceTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserTimezoneService Tests")
class UserTimezoneServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthHolder auth;

	@InjectMocks
	private UserTimezoneService userTimezoneService;

	@Test
	@DisplayName("getGmtDifferenceByUserId returns UTC default when user id is null")
	void testGetGmtDifferenceByUserIdNullUserIdReturnsDefaultOffset() {
		// Given
		// Null user id must short-circuit before any repository access.

		// When
		String result = this.userTimezoneService.getGmtDifferenceByUserId(null);

		// Then
		assertThat(result).isEqualTo(UserTimezoneServiceTestDataFactory.getDefaultGmtDifference());
		then(this.userRepository).should(never()).getGMTDifferenceByUserId(any());
	}

	@Test
	@DisplayName("getGmtDifferenceByUserId returns repository value when offset is configured")
	void testGetGmtDifferenceByUserIdConfiguredOffsetReturnsStoredValue() {
		// Given
		Integer userId = UserTimezoneServiceTestDataFactory.getSampleUserId();
		String stored = UserTimezoneServiceTestDataFactory.getNonBlankTimezoneOffset();
		given(this.userRepository.getGMTDifferenceByUserId(userId)).willReturn(stored);

		// When
		String result = this.userTimezoneService.getGmtDifferenceByUserId(userId);

		// Then
		assertThat(result).isEqualTo(stored);
		then(this.userRepository).should().getGMTDifferenceByUserId(userId);
	}

	@ParameterizedTest(name = "storedValue={0}")
	@NullAndEmptySource
	@ValueSource(strings = { "   ", "\t" })
	@DisplayName("getGmtDifferenceByUserId returns UTC default when repository yields blank data")
	void testGetGmtDifferenceByUserIdBlankStoredValueReturnsDefaultOffset(String storedValue) {
		// Given
		Integer userId = UserTimezoneServiceTestDataFactory.getSampleUserId();
		given(this.userRepository.getGMTDifferenceByUserId(userId)).willReturn(storedValue);

		// When
		String result = this.userTimezoneService.getGmtDifferenceByUserId(userId);

		// Then
		assertThat(result).isEqualTo(UserTimezoneServiceTestDataFactory.getDefaultGmtDifference());
		then(this.userRepository).should().getGMTDifferenceByUserId(userId);
	}

	@Test
	@DisplayName("getCurrentUserGmtDifference delegates to repository using authenticated user id")
	void testGetCurrentUserGmtDifferenceAuthenticatedUserLoadsOffsetFromRepository() {
		// Given
		Integer userId = UserTimezoneServiceTestDataFactory.getSampleUserId();
		String stored = UserTimezoneServiceTestDataFactory.getNonBlankTimezoneOffset();
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.userRepository.getGMTDifferenceByUserId(userId)).willReturn(stored);

		// When
		String result = this.userTimezoneService.getCurrentUserGmtDifference();

		// Then
		assertThat(result).isEqualTo(stored);
		then(this.auth).should().getAuthenticationPrincipalUniqueIdentifier();
		then(this.userRepository).should().getGMTDifferenceByUserId(userId);
	}

	@Test
	@DisplayName("getCurrentUserGmtDifference returns UTC default when principal user id is null")
	void testGetCurrentUserGmtDifferenceNullPrincipalUserIdReturnsDefaultOffset() {
		// Given
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(null);

		// When
		String result = this.userTimezoneService.getCurrentUserGmtDifference();

		// Then
		assertThat(result).isEqualTo(UserTimezoneServiceTestDataFactory.getDefaultGmtDifference());
		then(this.auth).should().getAuthenticationPrincipalUniqueIdentifier();
		then(this.userRepository).should(never()).getGMTDifferenceByUserId(any());
	}

}
