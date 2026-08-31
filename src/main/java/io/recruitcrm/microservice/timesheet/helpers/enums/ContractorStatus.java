package io.recruitcrm.microservice.timesheet.helpers.enums;

import io.recruitcrm.microservice.timesheet.exceptions.InvalidEnumValueException;
import lombok.Getter;

@Getter
public enum ContractorStatus {

	AVAILABLE(0), ASSIGNED(1);

	private final Integer value;

	ContractorStatus(Integer value) {
		this.value = value;
	}

	public static ContractorStatus fromValue(Integer value) {
		for (ContractorStatus status : ContractorStatus.values()) {
			if (status.value.equals(value)) {
				return status;
			}
		}
		throw new InvalidEnumValueException("ContractorStatus", value);
	}

}
