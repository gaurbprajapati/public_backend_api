package io.recruitcrm.microservice.timesheet.helpers.constants;

/**
 * Constants for reimbursement status and labels.
 */
public final class ReimbursementConstants {

	private ReimbursementConstants() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * Reimbursement status ID for submitted
	 */
	public static final int STATUS_SUBMITTED = 1;

	public static final int STATUS_APPROVED = 2;

	public static final int STATUS_REJECTED = 3;

	public static final int USER_TYPE_AGENCY = 2;

	public static final String STATUS_SUBMITTED_LABEL = "Submitted";

	public static final String STATUS_APPROVED_LABEL = "Approved";

	public static final String STATUS_REJECTED_LABEL = "Rejected";

	public static final String STATUS_PENDING_LABEL = "Pending";

	/**
	 * Returns the human-readable label for a given reimbursement status ID.
	 */
	public static String getStatusLabel(Integer status) {
		if (status == null) {
			return "";
		}
		return switch (status) {
			case STATUS_SUBMITTED -> STATUS_SUBMITTED_LABEL;
			case STATUS_APPROVED -> STATUS_APPROVED_LABEL;
			case STATUS_REJECTED -> STATUS_REJECTED_LABEL;
			default -> "Unknown";
		};
	}

}
