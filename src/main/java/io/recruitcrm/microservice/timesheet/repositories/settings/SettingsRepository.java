package io.recruitcrm.microservice.timesheet.repositories.settings;

import io.recruitcrm.microservice.timesheet.dao.settings.SaSettingsJpaRepository;
import io.recruitcrm.microservice.timesheet.entities.SaSettings;
import org.springframework.stereotype.Repository;

@Repository
public class SettingsRepository implements ISettingsRepository {

	private final SaSettingsJpaRepository saSettingsJpaRepository;

	public SettingsRepository(SaSettingsJpaRepository saSettingsJpaRepository) {
		this.saSettingsJpaRepository = saSettingsJpaRepository;
	}

	@Override
	public String getValueByKey(String settingKey) {
		return this.saSettingsJpaRepository.findBySettingKey(settingKey).map(SaSettings::getSettingValue).orElse("");
	}

}
