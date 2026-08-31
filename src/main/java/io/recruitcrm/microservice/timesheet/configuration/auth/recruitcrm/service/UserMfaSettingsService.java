/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.service;

import io.recruitcrm.entity.model.UserMfaSettings;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.dto.UserMfaSettingsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.mapper.Mapper;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.repository.UserMfaSettingsRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class UserMfaSettingsService implements IUserMfaSettingsService {

	final UserMfaSettingsRepository userMFASettingsRepository;

	final EntityManager entityManager;

	final Mapper mapper;

	public UserMfaSettingsService(UserMfaSettingsRepository userMFASettingsRepository, EntityManager entityManager,
			Mapper mapper) {
		this.userMFASettingsRepository = userMFASettingsRepository;
		this.entityManager = entityManager;
		this.mapper = mapper;
	}

	@Override
	public UserMfaSettingsResponseBodyDto getUserMfaSettings(Integer userId) {
		UserMfaSettings userMFASettings = this.userMFASettingsRepository.getUserMfaSettings(userId);
		return this.mapper.toUserMfaSettingsResultBodyDto(userMFASettings);
	}

}
