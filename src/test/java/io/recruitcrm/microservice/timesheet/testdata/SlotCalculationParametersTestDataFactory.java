package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.helpers.record.SlotCalculationParameters;

/**
 * Test data factory for {@link SlotCalculationParameters}.
 */
public final class SlotCalculationParametersTestDataFactory {

	public static final int DEFAULT_SLOT_SECONDS = 86400;

	private SlotCalculationParametersTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Creates default slot calculation parameters.
	 * @return default record instance
	 */
	public static SlotCalculationParameters createParameters() {
		return new SlotCalculationParameters(DEFAULT_SLOT_SECONDS);
	}

	/**
	 * Creates custom slot calculation parameters.
	 * @param slotSeconds slot duration in seconds
	 * @return custom record instance
	 */
	public static SlotCalculationParameters createParameters(int slotSeconds) {
		return new SlotCalculationParameters(slotSeconds);
	}

}
