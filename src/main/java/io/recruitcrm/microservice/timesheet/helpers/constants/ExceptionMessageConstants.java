package io.recruitcrm.microservice.timesheet.helpers.constants;

/**
 * Constants for exception and validation messages used across the application.
 */
public final class ExceptionMessageConstants {

	private ExceptionMessageConstants() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * Message when reimbursements are not enabled for a timesheet
	 */
	public static final String REIMBURSEMENTS_NOT_ENABLED = "Reimbursements are not enabled for this timesheet";

	public static final String REIMBURSEMENT_CANNOT_REOPEN = "Reimbursement cannot be reopened in its current state";

	public static final String REIMBURSEMENT_INVOICE_LINKED = "Reimbursement cannot be modified while an invoice is linked to this timesheet";

	public static final String REIMBURSEMENT_REOPEN_FORBIDDEN = "You are not authorized to reopen reimbursements";

	public static final String REIMBURSEMENT_PAYABLE_BILLABLE_REQUIRED = "At least one of isPayable or isBillable must be provided";

	public static final String REIMBURSEMENT_NOT_APPROVED_FOR_FLAGS = "Payable and billable flags can only be updated on approved reimbursements";

	public static final String REIMBURSEMENT_UPDATE_FLAGS_FORBIDDEN = "Only agency approvers can update payable and billable flags";

	public static final String REIMBURSEMENT_NOT_EDITABLE = "Reimbursement can only be updated when in Submitted status";

	public static final String REIMBURSEMENT_SUBMITTED_EDIT_REQUIRES_APPROVER = "Submitted reimbursements can only be edited by a timesheet approver";

	public static final String REIMBURSEMENT_INVALID_STATUS = "Invalid reimbursement status ID. Allowed values: 2 (Approved), 3 (Rejected)";

	public static final String REIMBURSEMENT_ALREADY_APPROVED = "Reimbursement is already approved";

	public static final String REIMBURSEMENT_ONLY_APPROVE_SUBMITTED = "Reimbursement can only be approved if status is SUBMITTED";

	public static final String REIMBURSEMENT_REJECT_REMARK_REQUIRED = "Remark is required when rejecting a reimbursement";

	public static final String REIMBURSEMENT_ALREADY_REJECTED = "Reimbursement is already rejected";

	public static final String REIMBURSEMENT_CANNOT_REJECT_APPROVED = "Cannot reject an approved reimbursement";

	public static final String REIMBURSEMENT_APPROVE_REJECT_FORBIDDEN = "Only agency and client users can approve or reject reimbursements";

	public static final String REIMBURSEMENT_CREATE_FORBIDDEN = "Only agency and contractor users can create reimbursements";

	public static final String REIMBURSEMENT_UPDATE_INVOICE_LINKED = "Cannot update reimbursement: invoice is linked";

	public static final String REIMBURSEMENT_UPDATE_FORBIDDEN = "Only agency and contractor users can update reimbursements";

	public static final String REIMBURSEMENT_NOT_DELETABLE = "Reimbursement can only be deleted when in Submitted or Rejected status";

	public static final String REIMBURSEMENT_NOT_OWNED = "You can only update or delete your own reimbursements";

	public static final String REIMBURSEMENT_MAX_LIMIT_REACHED = "Maximum of 10 reimbursements allowed per timesheet";

	public static final String REIMBURSEMENT_REMARK_MANDATORY_FOR_REJECTION = "Remark is mandatory when rejecting a reimbursement";

	public static final String REIMBURSEMENT_CONTRACTOR_CREATE_OWN_ONLY = "Contractor can only create reimbursements for their own timesheets";

	public static final String REIMBURSEMENT_CONTRACTOR_UPDATE_OWN_ONLY = "Contractor can only update reimbursements for their own timesheets";

	public static final String REIMBURSEMENT_CONTRACTOR_DELETE_OWN_ONLY = "Contractor can only delete reimbursements for their own timesheets";

	public static final String REIMBURSEMENT_CONTRACTOR_VIEW_HISTORY_OWN_ONLY = "Contractor can only view history of their own reimbursements";

	public static final String UNKNOWN_PERSONA_TYPE = "Unknown persona type";

	public static final String REIMBURSEMENT_CONTRACTOR_CANNOT_UPDATE_STATUS = "Contractor can't update the Status";

	public static final String REIMBURSEMENT_SHARE_WITH_CLIENT_FORBIDDEN = "Only agency users can update the Share with Client setting";

}
