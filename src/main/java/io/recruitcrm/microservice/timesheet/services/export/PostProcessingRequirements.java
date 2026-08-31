package io.recruitcrm.microservice.timesheet.services.export;

/**
 * Helper record to encapsulate post-processing requirements for timesheet export data.
 * This record determines what types of post-processing operations are needed based on the
 * selected fields in the export request.
 *
 * @param needsWorkDaysProcessing true if workDays field conversion is required
 * @param needsResourceUrlProcessing true if resource_url field construction is required
 * @param needsUserFieldProcessing true if user field transformations are required
 * @param needsCustomColumnProcessing true if custom column type conversions are required
 */
public record PostProcessingRequirements(boolean needsWorkDaysProcessing, boolean needsResourceUrlProcessing,
		boolean needsUserFieldProcessing, boolean needsCustomColumnProcessing) {

	/**
	 * Checks if any post-processing is required.
	 * @return true if any type of post-processing is needed, false otherwise
	 */
	public boolean hasAnyProcessing() {
		return this.needsWorkDaysProcessing || this.needsResourceUrlProcessing || this.needsUserFieldProcessing
				|| this.needsCustomColumnProcessing;
	}

}
