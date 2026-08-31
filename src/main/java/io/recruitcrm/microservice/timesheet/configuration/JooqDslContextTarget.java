package io.recruitcrm.microservice.timesheet.configuration;

public final class JooqDslContextTarget {

	// Private constructor to prevent instantiation
	private JooqDslContextTarget() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static final String RECRUITCRM_AURORA = "auroraDbDSLContext";

	public static final String RECRUITCRM_SINGLESTORE = "singleStoreDslContext";

}
