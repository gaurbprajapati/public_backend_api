package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import io.recruitcrm.entity.model.UserRole;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AccessControlConfigHolderTests {

	@Mock
	private EntityManager entityManager;

	@Mock
	private UserRole userRole;

	private AccessControlConfigHolder accessControlConfigHolder;

	@BeforeEach
	void setUp() {
		this.accessControlConfigHolder = new AccessControlConfigHolder(this.entityManager);
	}

	@Test
	@DisplayName("Should expose bean name and initialized dependencies")
	void shouldExposeBeanNameAndInitializedDependencies() {
		assertThat(AccessControlConfigHolder.BEAN_NAME).isEqualTo("recruitcrmAccessControlConfigHolder");
		assertThat(this.accessControlConfigHolder.getEntityManager()).isSameAs(this.entityManager);
		assertThat(this.accessControlConfigHolder.getObjectMapper()).isNotNull();
		assertThat(this.accessControlConfigHolder.getAccessControlDto()).isNotNull();
	}

	@Test
	@DisplayName("Should initialize access control config from role")
	void shouldInitializeAccessControlConfigFromRole() {
		// given
		Integer roleId = 7;
		given(this.entityManager.find(UserRole.class, roleId)).willReturn(this.userRole);
		given(this.userRole.getUserAccessJson()).willReturn("{\"exporttocsv\":1}");

		// when
		this.accessControlConfigHolder.initAccessControlConfig(roleId);

		// then
		assertThat(this.accessControlConfigHolder.getAccessControlDto()).isNotNull();
		assertThat(this.accessControlConfigHolder.getAccessControlDto().getGlobalPermissions()).isNotNull();
		then(this.entityManager).should().find(UserRole.class, roleId);
		then(this.userRole).should().getUserAccessJson();
	}

	@Test
	@DisplayName("Should throw null pointer exception when user role is missing")
	void shouldThrowNullPointerExceptionWhenUserRoleIsMissing() {
		// given
		Integer roleId = 9;
		given(this.entityManager.find(UserRole.class, roleId)).willReturn(null);

		// when / then
		assertThatThrownBy(() -> this.accessControlConfigHolder.initAccessControlConfig(roleId))
			.isInstanceOf(NullPointerException.class);
		then(this.entityManager).should().find(UserRole.class, roleId);
	}

	@Test
	@DisplayName("Should throw unauthorized access exception for invalid access JSON")
	void shouldThrowUnauthorizedAccessExceptionForInvalidAccessJson() {
		// given
		Integer roleId = 8;
		given(this.entityManager.find(UserRole.class, roleId)).willReturn(this.userRole);
		given(this.userRole.getUserAccessJson()).willReturn("{invalid}");

		// when / then
		assertThatThrownBy(() -> this.accessControlConfigHolder.initAccessControlConfig(roleId))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Error while fetching access control:");
		then(this.entityManager).should().find(UserRole.class, roleId);
		then(this.userRole).should().getUserAccessJson();
	}

}
