/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.entity.model.UserRole;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.AccessControlDto;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.text.MessageFormat;

@Component(AccessControlConfigHolder.BEAN_NAME)
@RequestScope
@Getter
public class AccessControlConfigHolder {

	public static final String BEAN_NAME = "recruitcrmAccessControlConfigHolder";

	private final EntityManager entityManager;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private AccessControlDto accessControlDto = new AccessControlDto();

	public AccessControlConfigHolder(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void initAccessControlConfig(Integer roleId) {
		UserRole userRole = this.entityManager.find(UserRole.class, roleId);
		String userAccessControlJson = userRole.getUserAccessJson();
		try {
			this.accessControlDto = this.objectMapper.readValue(userAccessControlJson, AccessControlDto.class);
			this.accessControlDto.initializeGlobalPermissions();
		}
		catch (Exception ex) {
			throw new UnauthorizedAccessException(
					MessageFormat.format("Error while fetching access control: {0}", ex.getMessage()));
		}
	}

}
