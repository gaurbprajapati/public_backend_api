package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.services.export.PostProcessingRequirements;

/**
 * Test data factory for PostProcessingRequirements tests. Provides test data for various
 * post-processing requirement scenarios.
 */
public final class PostProcessingRequirementsTestDataFactory {

	private PostProcessingRequirementsTestDataFactory() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Creates PostProcessingRequirements with all processing enabled
	 */
	public static PostProcessingRequirements createAllProcessingEnabled() {
		return new PostProcessingRequirements(true, true, true, true);
	}

	/**
	 * Creates PostProcessingRequirements with all processing disabled
	 */
	public static PostProcessingRequirements createAllProcessingDisabled() {
		return new PostProcessingRequirements(false, false, false, false);
	}

	/**
	 * Creates PostProcessingRequirements with only work days processing enabled
	 */
	public static PostProcessingRequirements createOnlyWorkDaysProcessing() {
		return new PostProcessingRequirements(true, false, false, false);
	}

	/**
	 * Creates PostProcessingRequirements with only resource URL processing enabled
	 */
	public static PostProcessingRequirements createOnlyResourceUrlProcessing() {
		return new PostProcessingRequirements(false, true, false, false);
	}

	/**
	 * Creates PostProcessingRequirements with only user field processing enabled
	 */
	public static PostProcessingRequirements createOnlyUserFieldProcessing() {
		return new PostProcessingRequirements(false, false, true, false);
	}

	/**
	 * Creates PostProcessingRequirements with only custom column processing enabled
	 */
	public static PostProcessingRequirements createOnlyCustomColumnProcessing() {
		return new PostProcessingRequirements(false, false, false, true);
	}

	/**
	 * Creates PostProcessingRequirements with work days and resource URL processing
	 * enabled
	 */
	public static PostProcessingRequirements createWorkDaysAndResourceUrlProcessing() {
		return new PostProcessingRequirements(true, true, false, false);
	}

	/**
	 * Creates PostProcessingRequirements with user field and custom column processing
	 * enabled
	 */
	public static PostProcessingRequirements createUserFieldAndCustomColumnProcessing() {
		return new PostProcessingRequirements(false, false, true, true);
	}

	/**
	 * Creates PostProcessingRequirements with three processing types enabled
	 */
	public static PostProcessingRequirements createThreeProcessingTypesEnabled() {
		return new PostProcessingRequirements(true, true, true, false);
	}

	/**
	 * Success messages for tests
	 */
	public static final class Messages {

		public static final String PROCESSING_REQUIREMENTS_CREATED = "Processing requirements created successfully";

		public static final String HAS_ANY_PROCESSING_VERIFIED = "Has any processing verified successfully";

		public static final String NO_PROCESSING_REQUIRED = "No processing required";

		public static final String MULTIPLE_PROCESSING_REQUIRED = "Multiple processing types required";

	}

}
