/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.repository;

import io.recruitcrm.entity.model.UserMfaSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMfaSettingsJpaRepository extends JpaRepository<UserMfaSettings, Integer> {

	UserMfaSettings findByUserId(Integer userId);

}
