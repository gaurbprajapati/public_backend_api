/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers;

import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class EmailValidationErrorHelper {

	private static final int PORTAL_STATUS_INVITATION_NOT_SENT = 0;

	private static final int PORTAL_STATUS_INVITATION_SENT = 1;

	private static final int PORTAL_STATUS_DISABLED = 3;

	private static final Pattern EMAIL_PATTERN = Pattern
		.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

	private static final Pattern CLIENT_EMAIL_PATTERN = Pattern
		.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	public boolean isTimesheetSubmitted(Integer statusId) {
		return Objects.equals(statusId, ApprovalStatusEnum.SUBMITTED.getId());
	}

	public boolean isTimesheetApproved(Integer statusId) {
		return Objects.equals(statusId, ApprovalStatusEnum.APPROVED.getId());
	}

	public boolean isTimesheetOpen(Integer statusId) {
		return Objects.equals(statusId, ApprovalStatusEnum.OPEN.getId());
	}

	public boolean isTimesheetRejected(Integer statusId) {
		return Objects.equals(statusId, ApprovalStatusEnum.REJECTED.getId());
	}

	public boolean isEmailMissing(String email) {
		return email == null || email.isBlank();
	}

	public boolean isOptedOutOfEmail(Byte emailOptOut) {
		return emailOptOut != null && emailOptOut == 1;
	}

	public boolean isRecordDeleted(Byte deleted) {
		return deleted != null && deleted == 1;
	}

	public boolean isContractorUnassigned(Integer assignmentId) {
		return assignmentId == null;
	}

	public boolean isPortalNotExist(Integer portalStatusId) {
		return portalStatusId == null || portalStatusId == PORTAL_STATUS_INVITATION_NOT_SENT
				|| portalStatusId == PORTAL_STATUS_INVITATION_SENT;
	}

	public boolean isPortalDisabled(Integer portalStatusId) {
		return portalStatusId != null && portalStatusId == PORTAL_STATUS_DISABLED;
	}

	public boolean isTimesheetNotSharedWithClient(Byte sharedWithClient) {
		return sharedWithClient == null || sharedWithClient == 0;
	}

	public boolean isTimesheetNotSharedWithContact(Byte sharedWithContact) {
		return sharedWithContact == null || sharedWithContact == 0;
	}

	public String validateEmailFormat(String email) {
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			return "incorrect_email";
		}
		return null;
	}

	/**
	 * Validates email format.
	 * @param email email address to validate
	 * @return error message when invalid, otherwise {@code null}
	 */
	public String validateClientEmailFormat(String email) {
		if (!StringUtils.hasText(email)) {
			return "email is required";
		}
		if (!CLIENT_EMAIL_PATTERN.matcher(email.trim()).matches()) {
			return "email must be a valid email address";
		}
		return null;
	}

	public String buildFullName(String firstName, String lastName) {
		String first = (firstName != null) ? firstName.trim() : "";
		String last = (lastName != null) ? lastName.trim() : "";
		if (last.isEmpty()) {
			return first;
		}
		return (first + " " + last).trim();
	}

}
