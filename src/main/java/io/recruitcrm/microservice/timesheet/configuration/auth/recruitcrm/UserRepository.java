/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm;

import io.recruitcrm.entity.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository(UserRepository.BEAN_NAME)
public interface UserRepository extends JpaRepository<User, Integer> {

	String BEAN_NAME = "recruitcrmAuthUserRepository";

	Optional<User> findByEmail(String email);

	@NotNull
	Optional<User> findById(@NotNull Integer userId);

}
