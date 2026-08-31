package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.helpers.EmailValidationErrorHelper} tests.
 */
public final class EmailValidationErrorHelperTestDataFactory {

	private EmailValidationErrorHelperTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getSubmittedStatusId() {
		return ApprovalStatusEnum.SUBMITTED.getId();
	}

	public static Integer getApprovedStatusId() {
		return ApprovalStatusEnum.APPROVED.getId();
	}

	public static Integer getOpenStatusId() {
		return ApprovalStatusEnum.OPEN.getId();
	}

	public static Integer getRejectedStatusId() {
		return ApprovalStatusEnum.REJECTED.getId();
	}

	public static Integer getUnknownStatusId() {
		return 99;
	}

	public static Integer getPortalDisabledStatusId() {
		return 3;
	}

	public static Integer getPortalInvitationNotSentStatusId() {
		return 0;
	}

	public static Integer getPortalInvitationSentStatusId() {
		return 1;
	}

	public static Integer getPortalEnabledStatusId() {
		return 2;
	}

	public static Integer getDefaultAssignmentId() {
		return 1001;
	}

	public static String getValidEmail() {
		return "user@example.com";
	}

	public static String getInvalidEmail() {
		return "not-an-email";
	}

	public static String getBlankEmail() {
		return "   ";
	}

	public static String getFirstName() {
		return "Jane";
	}

	public static String getLastName() {
		return "Doe";
	}

	public static String getWhitespaceFirstName() {
		return "  Jane  ";
	}

	public static String getWhitespaceLastName() {
		return "  Doe  ";
	}

	public static Byte getOptedOutFlag() {
		return Byte.valueOf((byte) 1);
	}

	public static Byte getNotOptedOutFlag() {
		return Byte.valueOf((byte) 0);
	}

	public static Byte getDeletedFlag() {
		return Byte.valueOf((byte) 1);
	}

	public static Byte getNotDeletedFlag() {
		return Byte.valueOf((byte) 0);
	}

	public static Byte getSharedWithContactFlag() {
		return Byte.valueOf((byte) 1);
	}

	public static Byte getNotSharedWithContactFlag() {
		return Byte.valueOf((byte) 0);
	}

	public static Byte getSharedWithClientFlag() {
		return Byte.valueOf((byte) 1);
	}

	public static Byte getNotSharedWithClientFlag() {
		return Byte.valueOf((byte) 0);
	}

}
