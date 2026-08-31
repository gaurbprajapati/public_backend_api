package io.recruitcrm.microservice.timesheet.testdata;

/**
 * Test constants for
 * {@link io.recruitcrm.microservice.timesheet.helpers.enums.EntityTypeEnum} tests.
 */
public final class EntityTypeEnumTestDataFactory {

	public static final String INVALID_ENTITY_NAME = "nonexistent_entity";

	public static final int INVALID_ENTITY_ID = 99999;

	public static final String MIXED_CASE_COMPANY_NAME = "CoMpAnY";

	private EntityTypeEnumTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

}
