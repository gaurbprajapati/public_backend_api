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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Unit tests for PortalEntityService class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class PortalEntityServiceTests {

	@Mock
	private AuthHolder auth;

	@Mock
	private PrincipalEntityExtractor principalEntityExtractor;

	@Mock
	private AuthPrincipal unifiedPrincipal;

	@InjectMocks
	private PortalEntityService portalEntityService;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Get portal entity info should return entity info when entity type and id are present")
	void testGetPortalEntityInfoValidEntityTypeAndIdReturnsEntityInfo() {
		// Given
		Integer entityType = 3;
		Integer entityId = 1;

		given(this.auth.getUnifiedPrincipal()).willReturn(this.unifiedPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(this.unifiedPrincipal))
			.willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(this.unifiedPrincipal)).willReturn(entityId);

		// When
		PortalEntityInfoResponseDto result = this.portalEntityService.getPortalEntityInfo();

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getEntityType()).isEqualTo(entityType);
		assertThat(result.getEntityId()).isEqualTo(entityId);
		then(this.auth).should().getUnifiedPrincipal();
		then(this.principalEntityExtractor).should().extractEntityTypeFromPrincipal(this.unifiedPrincipal);
		then(this.principalEntityExtractor).should().extractEntityIdFromPrincipal(this.unifiedPrincipal);
	}

	@Test
	@DisplayName("Get portal entity info should throw ValidationErrorException when entity type is null")
	void testGetPortalEntityInfoNullEntityTypeThrowsValidationErrorException() {
		// Given
		given(this.auth.getUnifiedPrincipal()).willReturn(this.unifiedPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(this.unifiedPrincipal)).willReturn(null);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(this.unifiedPrincipal)).willReturn(1);

		// When & Then
		assertThatThrownBy(() -> this.portalEntityService.getPortalEntityInfo())
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Entity type and entity ID must be available in the access token");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.principalEntityExtractor).should().extractEntityTypeFromPrincipal(this.unifiedPrincipal);
		then(this.principalEntityExtractor).should().extractEntityIdFromPrincipal(this.unifiedPrincipal);
	}

	@Test
	@DisplayName("Get portal entity info should throw ValidationErrorException when entity id is null")
	void testGetPortalEntityInfoNullEntityIdThrowsValidationErrorException() {
		// Given
		given(this.auth.getUnifiedPrincipal()).willReturn(this.unifiedPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(this.unifiedPrincipal)).willReturn(3);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(this.unifiedPrincipal)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.portalEntityService.getPortalEntityInfo())
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Entity type and entity ID must be available in the access token");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.principalEntityExtractor).should().extractEntityTypeFromPrincipal(this.unifiedPrincipal);
		then(this.principalEntityExtractor).should().extractEntityIdFromPrincipal(this.unifiedPrincipal);
	}

	@Test
	@DisplayName("Get portal entity info should throw ValidationErrorException when both entity type and id are null")
	void testGetPortalEntityInfoBothNullThrowsValidationErrorException() {
		// Given
		given(this.auth.getUnifiedPrincipal()).willReturn(this.unifiedPrincipal);
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(this.unifiedPrincipal)).willReturn(null);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(this.unifiedPrincipal)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.portalEntityService.getPortalEntityInfo())
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Entity type and entity ID must be available in the access token");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.principalEntityExtractor).should().extractEntityTypeFromPrincipal(this.unifiedPrincipal);
		then(this.principalEntityExtractor).should().extractEntityIdFromPrincipal(this.unifiedPrincipal);
	}

}
