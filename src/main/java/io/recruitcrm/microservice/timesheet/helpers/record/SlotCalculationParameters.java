package io.recruitcrm.microservice.timesheet.helpers.record;

/**
 * Parameters for slot calculation containing duration-based configuration for different
 * timesheet frequency types.
 */
public record SlotCalculationParameters(int slotSeconds) {
}