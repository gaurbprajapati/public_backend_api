package io.recruitcrm.microservice.timesheet.dao.settings;

import io.recruitcrm.microservice.timesheet.entities.SaSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaSettingsJpaRepository extends JpaRepository<SaSettings, Integer> {

	Optional<SaSettings> findBySettingKey(String settingKey);

}
