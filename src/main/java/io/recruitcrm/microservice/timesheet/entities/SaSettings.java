package io.recruitcrm.microservice.timesheet.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "tblsasettings")
@Data
public class SaSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "settingkey")
	private String settingKey;

	@Column(name = "settingvalue")
	private String settingValue;

	@Column(name = "systemsetting")
	private Integer systemSetting;

}
