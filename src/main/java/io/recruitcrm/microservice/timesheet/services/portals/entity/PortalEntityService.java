/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.portals.entity;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.dto.portal.PortalEntityInfoResponseDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.auth.PrincipalEntityExtractor;
import org.springframework.stereotype.Service;

/**
 * Service implementation for portal entity operations. Extracts entity type and entity ID
 * from authenticated principals for portal endpoints.
 */
@Service
public class PortalEntityService implements IPortalEntityService {

	private final AuthHolder auth;

	private final PrincipalEntityExtractor principalEntityExtractor;

	public PortalEntityService(AuthHolder auth, PrincipalEntityExtractor principalEntityExtractor) {
		this.auth = auth;
		this.principalEntityExtractor = principalEntityExtractor;
	}

	@Override
	public PortalEntityInfoResponseDto getPortalEntityInfo() {
		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		// Extract entityType and entityId from the authenticated principal (derived from
		// JWT token)
		Integer entityType = this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal);
		Integer entityId = this.principalEntityExtractor.extractEntityIdFromPrincipal(principal);

		if (entityType == null || entityId == null) {
			throw new ValidationErrorException(
					"Entity type and entity ID must be available in the access token. This endpoint is only available for contractors and contacts.");
		}

		return new PortalEntityInfoResponseDto(entityType, entityId);
	}

}
