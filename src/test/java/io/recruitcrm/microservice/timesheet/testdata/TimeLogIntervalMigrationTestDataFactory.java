package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationResponseBodyDto;

/**
 * Test data for
 * {@link io.recruitcrm.microservice.timesheet.controllers.time_log.TimeLogIntervalMigrationController}.
 */
public final class TimeLogIntervalMigrationTestDataFactory {

	/** Must match {@code AUTHORIZED_ACCOUNT_ID} on the controller. */
	public static final int AUTHORIZED_ACCOUNT_ID = 728;

	/** Must match {@code AUTHORIZED_USER_ID} on the controller. */
	public static final int AUTHORIZED_USER_ID = 20206;

	public static final int DEFAULT_BATCH_SIZE = 100;

	public static final int DEFAULT_OFFSET = 0;

	public static final String ENV_DEV = "dev";

	public static final String ENV_LOCAL = "local";

	public static final String ENV_PRODUCTION = "production";

	/**
	 * Distinct from {@link #AUTHORIZED_USER_ID} for unauthorized-user scenarios in tests.
	 */
	public static final int WRONG_USER_ID = 999;

	public static final String MESSAGE_MIGRATION_SUCCESS = "Time log interval migration completed successfully";

	/**
	 * Must match the message thrown by TimeLogIntervalMigrationController for
	 * unauthorized migration calls.
	 */
	public static final String MESSAGE_UNAUTHORIZED_MIGRATION = "You are not authorised to do this migration.";

	private TimeLogIntervalMigrationTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static TimeLogIntervalMigrationRequestBodyDto createMigrationRequest() {
		return new TimeLogIntervalMigrationRequestBodyDto(DEFAULT_BATCH_SIZE, DEFAULT_OFFSET);
	}

	public static TimeLogIntervalMigrationRequestBodyDto createMigrationRequest(int batchSize, int offset) {
		return new TimeLogIntervalMigrationRequestBodyDto(batchSize, offset);
	}

	public static TimeLogIntervalMigrationResponseBodyDto createMigrationResponse() {
		TimeLogIntervalMigrationResponseBodyDto result = new TimeLogIntervalMigrationResponseBodyDto();
		result.setMigratedCount(50);
		result.setHasMore(true);
		return result;
	}

}
