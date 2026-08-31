/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers;

import io.recruitcrm.microservice.timesheet.helpers.constants.DecimalPrecisionConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for formatting BigDecimal values with consistent precision and rounding.
 * This class provides methods to format BigDecimal values according to the application's
 * decimal precision configuration.
 */
public final class BigDecimalFormatter {

	private BigDecimalFormatter() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * Formats a BigDecimal value for monetary amounts using the configured scale and
	 * rounding mode.
	 * @param value the BigDecimal value to format
	 * @return the formatted BigDecimal value, or null if input is null
	 */
	public static BigDecimal formatMonetaryAmount(BigDecimal value) {
		if (value == null) {
			return null;
		}
		return value.setScale(DecimalPrecisionConstants.MONETARY_DECIMAL_SCALE,
				DecimalPrecisionConstants.MONETARY_ROUNDING_MODE);
	}

	/**
	 * Formats a BigDecimal value for percentage values using the configured scale and
	 * rounding mode.
	 * @param value the BigDecimal value to format
	 * @return the formatted BigDecimal value, or null if input is null
	 */
	public static BigDecimal formatPercentage(BigDecimal value) {
		if (value == null) {
			return null;
		}
		return value.setScale(DecimalPrecisionConstants.PERCENTAGE_DECIMAL_SCALE,
				DecimalPrecisionConstants.PERCENTAGE_ROUNDING_MODE);
	}

	/**
	 * Formats a BigDecimal value for general decimal values using the configured scale
	 * and rounding mode.
	 * @param value the BigDecimal value to format
	 * @return the formatted BigDecimal value, or null if input is null
	 */
	public static BigDecimal formatGeneralDecimal(BigDecimal value) {
		if (value == null) {
			return null;
		}
		return value.setScale(DecimalPrecisionConstants.GENERAL_DECIMAL_SCALE,
				DecimalPrecisionConstants.GENERAL_ROUNDING_MODE);
	}

	/**
	 * Formats a BigDecimal value with custom scale and rounding mode.
	 * @param value the BigDecimal value to format
	 * @param scale the number of decimal places
	 * @param roundingMode the rounding mode to use
	 * @return the formatted BigDecimal value, or null if input is null
	 */
	public static BigDecimal formatWithCustomPrecision(BigDecimal value, int scale, RoundingMode roundingMode) {
		if (value == null) {
			return null;
		}
		return value.setScale(scale, roundingMode);
	}

	/**
	 * Formats a BigDecimal value with custom scale using HALF_UP rounding mode.
	 * @param value the BigDecimal value to format
	 * @param scale the number of decimal places
	 * @return the formatted BigDecimal value, or null if input is null
	 */
	public static BigDecimal formatWithCustomScale(BigDecimal value, int scale) {
		return formatWithCustomPrecision(value, scale, RoundingMode.HALF_UP);
	}

}