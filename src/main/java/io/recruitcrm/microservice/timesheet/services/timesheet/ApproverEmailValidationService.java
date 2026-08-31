package io.recruitcrm.microservice.timesheet.services.timesheet;

import io.recruitcrm.contract_staffing.entity.model.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetApproverEmailQueryRowDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetEmailValidationDetailDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetEmailValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.EmailValidationErrorHelper;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetEmailValidationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApproverEmailValidationService {

	private static final int ENTITY_TYPE_APPROVER = 1;

	private final AuthHolder auth;

	private final TimesheetEmailValidationRepository timesheetEmailValidationRepository;

	private final EmailValidationErrorHelper errorHelper;

	public ApproverEmailValidationService(AuthHolder auth,
			TimesheetEmailValidationRepository timesheetEmailValidationRepository,
			EmailValidationErrorHelper errorHelper) {
		this.auth = auth;
		this.timesheetEmailValidationRepository = timesheetEmailValidationRepository;
		this.errorHelper = errorHelper;
	}

	public TimesheetEmailValidationResponseBodyDto validateApproverEmails(List<Integer> timesheetIds) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		List<TimesheetApproverEmailQueryRowDto> rows = this.timesheetEmailValidationRepository
			.getApproverEmailValidationRows(timesheetIds, accountId);

		Map<String, Integer> clientPortalStatusByEmail = this.loadClientPortalStatusByEmail(rows, accountId);

		Map<Integer, List<TimesheetApproverEmailQueryRowDto>> rowsByTimesheetId = this
			.groupApproverRowsByTimesheetId(rows, timesheetIds.size());

		Integer agencyRecruiterTypeId = UserTypeEnum.AGENCY_RECRUITER.getId();
		List<TimesheetEmailValidationDetailDto> details = new ArrayList<>(rows.size());
		for (Integer timesheetId : timesheetIds) {
			details.addAll(this.buildApproverValidationDetailsForTimesheet(timesheetId,
					rowsByTimesheetId.get(timesheetId), agencyRecruiterTypeId, clientPortalStatusByEmail));
		}

		return new TimesheetEmailValidationResponseBodyDto(ENTITY_TYPE_APPROVER, details);
	}

	private Map<String, Integer> loadClientPortalStatusByEmail(List<TimesheetApproverEmailQueryRowDto> rows,
			Integer accountId) {
		List<String> clientApproverEmails = rows.stream()
			.filter((row) -> UserTypeEnum.COMPANY_CONTACT.getId().equals(row.getUserTypeId()))
			.map(TimesheetApproverEmailQueryRowDto::getEmailId)
			.filter((email) -> !this.errorHelper.isEmailMissing(email))
			.map(String::trim)
			.distinct()
			.toList();

		if (clientApproverEmails.isEmpty()) {
			return Collections.emptyMap();
		}

		return this.timesheetEmailValidationRepository.getClientPortalStatusByEmails(clientApproverEmails, accountId);
	}

	private Map<Integer, List<TimesheetApproverEmailQueryRowDto>> groupApproverRowsByTimesheetId(
			List<TimesheetApproverEmailQueryRowDto> rows, int expectedTimesheetCount) {
		Map<Integer, List<TimesheetApproverEmailQueryRowDto>> rowsByTimesheetId = LinkedHashMap
			.newLinkedHashMap(expectedTimesheetCount);
		for (TimesheetApproverEmailQueryRowDto row : rows) {
			rowsByTimesheetId.computeIfAbsent(row.getTimesheetId(), (k) -> new ArrayList<>()).add(row);
		}
		return rowsByTimesheetId;
	}

	private List<TimesheetEmailValidationDetailDto> buildApproverValidationDetailsForTimesheet(Integer timesheetId,
			List<TimesheetApproverEmailQueryRowDto> group, Integer agencyRecruiterTypeId,
			Map<String, Integer> clientPortalStatusByEmail) {
		if ((group == null) || group.isEmpty()) {
			return List.of(TimesheetEmailValidationDetailDto.builder()
				.timesheetId(timesheetId)
				.error("timesheet_not_exist")
				.valid(false)
				.build());
		}

		TimesheetApproverEmailQueryRowDto first = group.get(0);
		if (first.getTimesheetApproverId() == null) {
			return List.of(TimesheetEmailValidationDetailDto.builder()
				.timesheetId(timesheetId)
				.error("no_timesheet_approver")
				.valid(false)
				.build());
		}

		List<TimesheetEmailValidationDetailDto> approverDetails = new ArrayList<>(group.size());
		for (TimesheetApproverEmailQueryRowDto row : group) {
			approverDetails
				.add(this.buildApproverValidationDetail(row, agencyRecruiterTypeId, clientPortalStatusByEmail));
		}
		return approverDetails;
	}

	private TimesheetEmailValidationDetailDto buildApproverValidationDetail(TimesheetApproverEmailQueryRowDto row,
			Integer agencyRecruiterTypeId, Map<String, Integer> clientPortalStatusByEmail) {
		String name = this.errorHelper.buildFullName(row.getFirstName(), row.getLastName());
		String error = this.resolveApproverError(row, clientPortalStatusByEmail);
		boolean isAgencyApprover = agencyRecruiterTypeId.equals(row.getUserTypeId());

		return TimesheetEmailValidationDetailDto.builder()
			.timesheetId(row.getTimesheetId())
			.contractorEntityId(null)
			.approverTypeId(row.getUserTypeId())
			.approverEntityId(row.getEntityId())
			.name(name)
			.serialNumber(null)
			.slug((isAgencyApprover) ? null : row.getSlug())
			.email(row.getEmailId())
			.ownerId(row.getOwnerId())
			.error(error)
			.valid(error == null)
			.build();
	}

	/**
	 * Priority order for approver (entityTypeId=1): 1. Timesheet is Approved 2. Timesheet
	 * is Open 3. Timesheet is Rejected (only submitted timesheets are eligible) 4. Job is
	 * deleted 5. Timesheet is not shared with the contact (job.AUTHID missing or approver
	 * not on job contacts) 6. Approver email is missing 7. They have opted out of email
	 * 8. The record is deleted 9. Portal does not exist (company contact only) 10. Portal
	 * is disabled (company contact only) 11. The timesheet is not shared with the contact
	 */
	private String resolveApproverError(TimesheetApproverEmailQueryRowDto row,
			Map<String, Integer> clientPortalStatusByEmail) {
		if (this.errorHelper.isTimesheetApproved(row.getLatestApprovalStatusId())) {
			return "approved_timesheet";
		}
		if (this.errorHelper.isTimesheetOpen(row.getLatestApprovalStatusId())) {
			return "open_timesheet";
		}
		if (this.errorHelper.isTimesheetRejected(row.getLatestApprovalStatusId())) {
			return "rejected_timesheet";
		}
		if (row.getJobId() == null) {
			return "deleted_record";
		}
		if (this.errorHelper.isTimesheetNotSharedWithClient(row.getSharedWithClient())) {
			return "timesheet_not_shared_with_contact";
		}
		if (this.errorHelper.isEmailMissing(row.getEmailId())) {
			return "no_email";
		}
		if (this.errorHelper.isOptedOutOfEmail(row.getEmailOptOut())) {
			return "opted_out_of_email";
		}
		if (this.errorHelper.isRecordDeleted(row.getDeleted())) {
			return "deleted_record";
		}
		if (UserTypeEnum.COMPANY_CONTACT.getId().equals(row.getUserTypeId())) {
			Integer portalStatusId = clientPortalStatusByEmail.get(row.getEmailId().trim());
			if (this.errorHelper.isPortalNotExist(portalStatusId)) {
				return "portal_does_not_exist";
			}
			if (this.errorHelper.isPortalDisabled(portalStatusId)) {
				return "portal_is_disabled";
			}
		}
		if (this.errorHelper.isTimesheetNotSharedWithContact(row.getSharedWithContact())) {
			return "timesheet_not_shared_with_contact";
		}
		return null;
	}

}
