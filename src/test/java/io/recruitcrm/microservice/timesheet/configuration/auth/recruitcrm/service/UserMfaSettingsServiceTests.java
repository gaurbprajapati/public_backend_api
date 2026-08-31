package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.service;

import io.recruitcrm.entity.model.UserMfaSettings;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.dto.UserMfaSettingsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.mapper.Mapper;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.repository.UserMfaSettingsRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserMfaSettingsServiceTests {

	@Mock
	private UserMfaSettingsRepository userMFASettingsRepository;

	@Mock
	private EntityManager entityManager;

	@Mock
	private Mapper mapper;

	@InjectMocks
	private UserMfaSettingsService userMfaSettingsService;

	private static final Integer USER_ID = 123;

	private static final Integer ACCOUNT_ID = 456;

	private UserMfaSettings mockUserMfaSettings;

	private UserMfaSettingsResponseBodyDto expectedDto;

	@BeforeEach
	void setUp() {
		// Setup mock UserMfaSettings
		this.mockUserMfaSettings = new UserMfaSettings();
		this.mockUserMfaSettings.setUserId(USER_ID);
		this.mockUserMfaSettings.setAccountId(ACCOUNT_ID);
		this.mockUserMfaSettings.setWebMfaLogin(true);
		this.mockUserMfaSettings.setMobileMfaLogin(true);
		this.mockUserMfaSettings.setSecretKey("test-secret-key");
		this.mockUserMfaSettings.setMfaEnforceBy("admin");
		this.mockUserMfaSettings.setMfaEnforcedDate(1625097600); // Example Unix timestamp

		// Setup expected DTO
		this.expectedDto = new UserMfaSettingsResponseBodyDto();
		this.expectedDto.setUserId(USER_ID);
		this.expectedDto.setAccountId(ACCOUNT_ID);
		this.expectedDto.setWebMfaLogin(true);
		this.expectedDto.setMobileMfaLogin(true);
		this.expectedDto.setSecretKey("test-secret-key");
		this.expectedDto.setMfaEnforceBy("admin");
		this.expectedDto.setMfaEnforcedDate(1625097600);

		// Setup mapper mock with lenient stubbing for both null and non-null cases
		lenient().when(this.mapper.toUserMfaSettingsResultBodyDto(this.mockUserMfaSettings))
			.thenReturn(this.expectedDto);
		lenient().when(this.mapper.toUserMfaSettingsResultBodyDto(null)).thenReturn(null);
	}

	@Test
	@DisplayName("Get user MFA settings successfully")
	void testGetUserMfaSettings() {
		// Arrange
		given(this.userMFASettingsRepository.getUserMfaSettings(USER_ID)).willReturn(this.mockUserMfaSettings);

		// Act
		UserMfaSettingsResponseBodyDto result = this.userMfaSettingsService.getUserMfaSettings(USER_ID);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getUserId()).isEqualTo(USER_ID);
		assertThat(result.getAccountId()).isEqualTo(ACCOUNT_ID);
		assertThat(result.getWebMfaLogin()).isTrue();
		assertThat(result.getMobileMfaLogin()).isTrue();
		assertThat(result.getSecretKey()).isEqualTo("test-secret-key");
		assertThat(result.getMfaEnforceBy()).isEqualTo("admin");
		assertThat(result.getMfaEnforcedDate()).isEqualTo(1625097600);
	}

	@Test
	@DisplayName("Get user MFA settings - User not found")
	void testGetUserMfaSettingsNotFound() {
		// Arrange
		given(this.userMFASettingsRepository.getUserMfaSettings(USER_ID)).willReturn(null);

		// Act
		UserMfaSettingsResponseBodyDto result = this.userMfaSettingsService.getUserMfaSettings(USER_ID);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Get user MFA settings - MFA disabled")
	void testGetUserMfaSettingsDisabled() {
		// Arrange
		this.mockUserMfaSettings.setWebMfaLogin(false);
		this.mockUserMfaSettings.setMobileMfaLogin(false);
		this.expectedDto.setWebMfaLogin(false);
		this.expectedDto.setMobileMfaLogin(false);

		given(this.userMFASettingsRepository.getUserMfaSettings(USER_ID)).willReturn(this.mockUserMfaSettings);

		// Act
		UserMfaSettingsResponseBodyDto result = this.userMfaSettingsService.getUserMfaSettings(USER_ID);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getUserId()).isEqualTo(USER_ID);
		assertThat(result.getAccountId()).isEqualTo(ACCOUNT_ID);
		assertThat(result.getWebMfaLogin()).isFalse();
		assertThat(result.getMobileMfaLogin()).isFalse();
		assertThat(result.getSecretKey()).isEqualTo("test-secret-key");
		assertThat(result.getMfaEnforceBy()).isEqualTo("admin");
		assertThat(result.getMfaEnforcedDate()).isEqualTo(1625097600);
	}

}