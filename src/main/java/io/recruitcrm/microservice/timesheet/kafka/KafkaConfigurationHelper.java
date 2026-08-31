/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.kafka;

import io.recruitcrm.microservice.timesheet.kafka.constants.KafkaJaasConstants;
import org.apache.kafka.common.security.scram.ScramLoginModule;

public final class KafkaConfigurationHelper {

	private KafkaConfigurationHelper() {
	}

	public static String getJaasConfig(String saslUsername, String saslPassword) {
		return String.format("%s %s %s=\"%s\" %s=\"%s\";", ScramLoginModule.class.getName(),
				KafkaJaasConstants.CONTROL_FLAG_REQUIRED, KafkaJaasConstants.OPTION_USERNAME, saslUsername,
				KafkaJaasConstants.OPTION_PASSWORD, saslPassword);
	}

}
