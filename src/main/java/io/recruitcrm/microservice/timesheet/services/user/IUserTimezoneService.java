package io.recruitcrm.microservice.timesheet.services.user;

/**
 * Service interface for user timezone operations.
 */
public interface IUserTimezoneService {

	/**
	 * Gets the GMT difference (timezone offset) for the currently authenticated user.
	 * Falls back to "+00:00" (UTC) if no timezone is configured for the user.
	 * @return The GMT difference string (e.g., "+05:30", "-08:00")
	 */
	String getCurrentUserGmtDifference();

	/**
	 * Gets the GMT difference (timezone offset) for a specific user. Falls back to
	 * "+00:00" (UTC) if no timezone is configured for the user.
	 * @param userId The ID of the user
	 * @return The GMT difference string (e.g., "+05:30", "-08:00")
	 */
	String getGmtDifferenceByUserId(Integer userId);

}
