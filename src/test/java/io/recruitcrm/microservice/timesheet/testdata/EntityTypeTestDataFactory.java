package io.recruitcrm.microservice.timesheet.testdata;

/**
 * Test constants for
 * {@link io.recruitcrm.microservice.timesheet.helpers.enums.EntityType} tests.
 */
public final class EntityTypeTestDataFactory {

	/** Matches {@code EntityType.CANDIDATE} / Tblextrafields candidate entitytypeid. */
	public static final int CANDIDATE_ENTITY_TYPE_ID = 5;

	public static final int UNKNOWN_ENTITY_TYPE_ID = 99999;

	private EntityTypeTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

}
