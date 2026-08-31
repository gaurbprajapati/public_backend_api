package io.recruitcrm.microservice.timesheet.repositories.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.recruitcrm.microservice.timesheet.dao.settings.SaSettingsJpaRepository;
import io.recruitcrm.microservice.timesheet.entities.SaSettings;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SettingsRepositoryTests {

	@Mock
	private SaSettingsJpaRepository saSettingsJpaRepository;

	@InjectMocks
	private SettingsRepository settingsRepository;

	@Test
	@DisplayName("getValueByKey should return setting value when key exists")
	void testGetValueByKeyFoundReturnsValue() {
		// Given
		SaSettings settings = new SaSettings();
		settings.setSettingKey("Sendgrid Key");
		settings.setSettingValue("SG.secret");
		given(this.saSettingsJpaRepository.findBySettingKey("Sendgrid Key")).willReturn(Optional.of(settings));

		// When
		String value = this.settingsRepository.getValueByKey("Sendgrid Key");

		// Then
		assertThat(value).isEqualTo("SG.secret");
	}

	@Test
	@DisplayName("getValueByKey should return empty string when key is missing")
	void testGetValueByKeyMissingReturnsEmpty() {
		// Given
		given(this.saSettingsJpaRepository.findBySettingKey("missing")).willReturn(Optional.empty());

		// When
		String value = this.settingsRepository.getValueByKey("missing");

		// Then
		assertThat(value).isEmpty();
	}

}
