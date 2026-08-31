/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.kafka.constants;

/**
 * Constants for Kafka SASL/JAAS configuration string fragments. Centralizes fixed option
 * names and control flags to avoid magic strings.
 */
public final class KafkaJaasConstants {

	private KafkaJaasConstants() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * JAAS control flag indicating the login module must succeed.
	 */
	public static final String CONTROL_FLAG_REQUIRED = "required";

	/**
	 * JAAS option key for the SASL username.
	 */
	public static final String OPTION_USERNAME = "username";

	/**
	 * JAAS option key for the SASL password.
	 */
	public static final String OPTION_PASSWORD = "password";

}
