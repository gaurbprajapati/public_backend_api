/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.recruitcrm.entity.model.UserMfaSettings;
import io.recruitcrm.microservice.timesheet.testdata.UserMfaSettingsRepositoryTestDataFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

/**
 * Unit tests for {@link UserMfaSettingsRepository}: JPA delegation and error propagation.
 */
@ExtendWith(MockitoExtension.class)
class UserMfaSettingsRepositoryTests {

	@Mock
	private UserMfaSettingsJpaRepository userMfaSettingsJpaRepository;

	@Mock
	private EntityManager entityManager;

	private UserMfaSettingsRepository repository;

	@BeforeEach
	void setUp() {
		this.repository = new UserMfaSettingsRepository(this.userMfaSettingsJpaRepository, this.entityManager);
	}

	@Test
	@DisplayName("Get user MFA settings delegates findByUserId and returns entity from JPA")
	void testGetUserMfaSettingsWhenFoundReturnsEntity() {
		// Given
		Integer userId = UserMfaSettingsRepositoryTestDataFactory.getDefaultUserId();
		UserMfaSettings expected = UserMfaSettingsRepositoryTestDataFactory.createUserMfaSettings();
		given(this.userMfaSettingsJpaRepository.findByUserId(userId)).willReturn(expected);

		// When
		UserMfaSettings result = this.repository.getUserMfaSettings(userId);

		// Then
		assertThat(result).isSameAs(expected);
		then(this.userMfaSettingsJpaRepository).should().findByUserId(userId);
	}

	@Test
	@DisplayName("Get user MFA settings returns null when JPA finds no row")
	void testGetUserMfaSettingsWhenNotFoundReturnsNull() {
		// Given
		Integer userId = UserMfaSettingsRepositoryTestDataFactory.getDefaultUserId();
		given(this.userMfaSettingsJpaRepository.findByUserId(userId)).willReturn(null);

		// When
		UserMfaSettings result = this.repository.getUserMfaSettings(userId);

		// Then
		assertThat(result).isNull();
		then(this.userMfaSettingsJpaRepository).should().findByUserId(userId);
	}

	@Test
	@DisplayName("Get user MFA settings propagates data access errors from JPA")
	void testGetUserMfaSettingsWhenJpaFailsPropagates() {
		// Given
		Integer userId = UserMfaSettingsRepositoryTestDataFactory.getDefaultUserId();
		willThrow(new DataAccessException("mfa lookup failed") {
		}).given(this.userMfaSettingsJpaRepository).findByUserId(userId);

		// When & Then
		assertThatThrownBy(() -> this.repository.getUserMfaSettings(userId)).isInstanceOf(DataAccessException.class)
			.hasMessageContaining("mfa lookup failed");
		then(this.userMfaSettingsJpaRepository).should().findByUserId(userId);
	}

}
