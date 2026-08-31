/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.repository;

import io.recruitcrm.entity.model.UserMfaSettings;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class UserMfaSettingsRepository implements IUserMfaSettingsRepository {

	final UserMfaSettingsJpaRepository userMfaSettingsJpaRepository;

	final EntityManager entityManager;

	public UserMfaSettingsRepository(UserMfaSettingsJpaRepository userMfaSettingsJpaRepository,
			EntityManager entityManager) {
		this.userMfaSettingsJpaRepository = userMfaSettingsJpaRepository;
		this.entityManager = entityManager;
	}

	@Override
	public UserMfaSettings getUserMfaSettings(Integer userId) {
		return this.userMfaSettingsJpaRepository.findByUserId(userId);
	}

}
