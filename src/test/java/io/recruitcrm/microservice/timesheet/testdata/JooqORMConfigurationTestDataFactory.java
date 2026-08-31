/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.configuration.JooqORMConfiguration}.
 */
public final class JooqORMConfigurationTestDataFactory {

	/**
	 * Number of table names in {@code requiredTables} (must stay in sync with production
	 * list size).
	 */
	public static final int REQUIRED_TABLE_NAME_COUNT = 33;

	public static final String TEST_JDBC_URL = "jdbc:mysql://localhost:3306/testdb";

	public static final String TEST_USERNAME = "jooq_test_user";

	/**
	 * Synthetic credential for codegen JDBC config in tests only (not a production
	 * secret).
	 */
	public static final String TEST_JDBC_CREDENTIAL = "jooq_test_secret";

	public static final String TEST_INPUT_SCHEMA = "test_schema";

	/**
	 * Driver string embedded in codegen JDBC config (see
	 * {@code JooqORMConfiguration#jooqConfig}).
	 */
	public static final String EXPECTED_CODEGEN_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

	public static final String EXPECTED_JOOQ_META_DATABASE_CLASS_NAME = "org.jooq.meta.mysql.MySQLDatabase";

	public static final String EXPECTED_GENERATOR_PACKAGE_NAME = "io.recruitcrm.microservice.search.models.jooq";

	public static final String EXPECTED_GENERATOR_DIRECTORY = "src/main/java";

	public static final String EXPECTED_GENERATOR_ENCODING = "UTF-8";

	/** Known entry from the {@code requiredTables} list for lightweight assertions. */
	public static final String SAMPLE_INCLUDED_TABLE_NAME = "cst_timesheet_t";

	private JooqORMConfigurationTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

}
