package io.recruitcrm.microservice.timesheet.repositories.settings;

/**
 * System settings stored in {@code tblsasettings} (same as Albatross
 * {@code Settings::getValueByKey}).
 */
public interface ISettingsRepository {

	String getValueByKey(String settingKey);

}
