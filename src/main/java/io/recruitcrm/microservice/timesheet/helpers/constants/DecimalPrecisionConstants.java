/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers.constants;

import java.math.RoundingMode;

/**
 * Constants class for decimal precision configuration used across the application. This
 * class contains configuration for BigDecimal formatting and precision.
 */
public final class DecimalPrecisionConstants {

	private DecimalPrecisionConstants() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * Default decimal scale for monetary amounts (2 decimal places)
	 */
	public static final int MONETARY_DECIMAL_SCALE = 2;

	/**
	 * Default rounding mode for monetary calculations
	 */
	public static final RoundingMode MONETARY_ROUNDING_MODE = RoundingMode.HALF_UP;

	/**
	 * Default decimal scale for percentage values (4 decimal places)
	 */
	public static final int PERCENTAGE_DECIMAL_SCALE = 4;

	/**
	 * Default rounding mode for percentage calculations
	 */
	public static final RoundingMode PERCENTAGE_ROUNDING_MODE = RoundingMode.HALF_UP;

	/**
	 * Default decimal scale for general decimal values (3 decimal places)
	 */
	public static final int GENERAL_DECIMAL_SCALE = 3;

	/**
	 * Default rounding mode for general decimal calculations
	 */
	public static final RoundingMode GENERAL_ROUNDING_MODE = RoundingMode.HALF_UP;

}