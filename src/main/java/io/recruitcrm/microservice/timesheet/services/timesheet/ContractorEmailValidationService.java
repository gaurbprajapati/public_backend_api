package io.recruitcrm.microservice.timesheet.services.timesheet;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCandidateEmailQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetEmailValidationDetailDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetEmailValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.EmailValidationErrorHelper;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetEmailValidationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContractorEmailValidationService {

	private static final int ENTITY_TYPE_CONTRACTOR = 3;

	private final AuthHolder auth;

	private final TimesheetEmailValidationRepository timesheetEmailValidationRepository;

	private final EmailValidationErrorHelper errorHelper;

	public ContractorEmailValidationService(AuthHolder auth,
			TimesheetEmailValidationRepository timesheetEmailValidationRepository,
			EmailValidationErrorHelper errorHelper) {
		this.auth = auth;
		this.timesheetEmailValidationRepository = timesheetEmailValidationRepository;
		this.errorHelper = errorHelper;
	}

	public TimesheetEmailValidationResponseBodyDto validateContractorEmails(List<Integer> timesheetIds) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		List<TimesheetCandidateEmailQueryResultDto> queryResults = this.timesheetEmailValidationRepository
			.getTimesheetValidationData(timesheetIds, accountId, ENTITY_TYPE_CONTRACTOR);

		Map<Integer, TimesheetCandidateEmailQueryResultDto> resultByTimesheetId = queryResults.stream()
			.collect(Collectors.toMap(TimesheetCandidateEmailQueryResultDto::getTimesheetId, (r) -> r, (a, b) -> a));

		List<TimesheetEmailValidationDetailDto> details = new ArrayList<>();
		for (Integer timesheetId : timesheetIds) {
			TimesheetCandidateEmailQueryResultDto result = resultByTimesheetId.get(timesheetId);
			if (result == null) {
				details.add(TimesheetEmailValidationDetailDto.builder()
					.timesheetId(timesheetId)
					.error("timesheet_not_exist")
					.valid(false)
					.build());
				continue;
			}
			String name = this.errorHelper.buildFullName(result.getFirstName(), result.getLastName());
			String error = resolveContractorError(result);

			details.add(TimesheetEmailValidationDetailDto.builder()
				.timesheetId(result.getTimesheetId())
				.contractorEntityId(result.getCandidateId())
				.name(name)
				.serialNumber(result.getSrno())
				.slug(result.getSlug())
				.email(result.getEmailId())
				.ownerId(result.getOwnerId())
				.error(error)
				.valid(error == null)
				.build());
		}

		return new TimesheetEmailValidationResponseBodyDto(ENTITY_TYPE_CONTRACTOR, details);
	}

	/**
	 * Priority order for contractor (entityTypeId=3): 1. Timesheet is Submitted 2.
	 * Timesheet is Approved (only open and rejected are eligible) 3. Candidate record
	 * does not exist (physically deleted) 4. Contractor email is missing 5. They have
	 * opted out of email 6. The record is soft-deleted 7. The Contractor is unassigned 8.
	 * Portal does not exist 9. Portal is disabled
	 */
	private String resolveContractorError(TimesheetCandidateEmailQueryResultDto result) {
		Integer statusId = result.getLatestApprovalStatusId();
		if (this.errorHelper.isTimesheetSubmitted(statusId)) {
			return "submitted_timesheet";
		}
		if (this.errorHelper.isTimesheetApproved(statusId)) {
			return "approved_timesheet";
		}
		if (result.getCandidateId() == null) {
			return "deleted_record";
		}
		if (this.errorHelper.isEmailMissing(result.getEmailId())) {
			return "no_email";
		}
		if (this.errorHelper.isOptedOutOfEmail(result.getEmailOptOut())) {
			return "opted_out_of_email";
		}
		if (this.errorHelper.isRecordDeleted(result.getDeleted())) {
			return "deleted_record";
		}
		if (this.errorHelper.isContractorUnassigned(result.getAssignmentId())) {
			return "contractor_unassigned_from_job";
		}
		if (this.errorHelper.isPortalNotExist(result.getPortalStatusId())) {
			return "portal_does_not_exist";
		}
		if (this.errorHelper.isPortalDisabled(result.getPortalStatusId())) {
			return "portal_is_disabled";
		}
		return null;
	}

}
