package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetApproverEmailQueryRowDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCandidateEmailQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetEmailValidationDetailDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetEmailValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ValidateTimesheetEmailRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test data factory for timesheet email validation API tests.
 */
public final class TimesheetEmailValidationTestDataFactory {

	private TimesheetEmailValidationTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getDefaultTimesheetId() {
		return 53981;
	}

	public static Integer getSecondTimesheetId() {
		return 53982;
	}

	public static Integer getEntityTypeContractor() {
		return 3;
	}

	public static Integer getEntityTypeApprover() {
		return 1;
	}

	public static ValidateTimesheetEmailRequestBodyDto createValidateTimesheetEmailRequestContractor() {
		return new ValidateTimesheetEmailRequestBodyDto(List.of(getDefaultTimesheetId()), getEntityTypeContractor());
	}

	public static ValidateTimesheetEmailRequestBodyDto createValidateTimesheetEmailRequestContractorMultipleIds() {
		return new ValidateTimesheetEmailRequestBodyDto(List.of(getDefaultTimesheetId(), getSecondTimesheetId()),
				getEntityTypeContractor());
	}

	public static ValidateTimesheetEmailRequestBodyDto createValidateTimesheetEmailRequestApproverMultipleIds() {
		return new ValidateTimesheetEmailRequestBodyDto(List.of(getDefaultTimesheetId(), getSecondTimesheetId()),
				getEntityTypeApprover());
	}

	public static ValidateTimesheetEmailRequestBodyDto createValidateTimesheetEmailRequestEntityTypeTwo() {
		return new ValidateTimesheetEmailRequestBodyDto(List.of(getDefaultTimesheetId()), 2);
	}

	public static ValidateTimesheetEmailRequestBodyDto createValidateTimesheetEmailRequestApprover() {
		return new ValidateTimesheetEmailRequestBodyDto(List.of(getDefaultTimesheetId()), getEntityTypeApprover());
	}

	public static ValidateTimesheetEmailRequestBodyDto createValidateTimesheetEmailRequestUnsupportedEntityType() {
		return new ValidateTimesheetEmailRequestBodyDto(List.of(getDefaultTimesheetId()), 99);
	}

	public static ValidateTimesheetEmailRequestBodyDto createValidateTimesheetEmailRequestNullEntityType() {
		return new ValidateTimesheetEmailRequestBodyDto(List.of(getDefaultTimesheetId()), null);
	}

	public static TimesheetEmailValidationResponseBodyDto createTimesheetEmailValidationResponseBodyDto() {
		TimesheetEmailValidationDetailDto detail = TimesheetEmailValidationDetailDto.builder()
			.timesheetId(getDefaultTimesheetId())
			.contractorEntityId(100)
			.ownerId(7001)
			.name("Test Contractor")
			.email("test@example.com")
			.valid(true)
			.build();
		return new TimesheetEmailValidationResponseBodyDto(getEntityTypeContractor(), List.of(detail));
	}

	public static TimesheetEmailValidationResponseBodyDto createEmptyTimesheetEmailValidationResponseBodyDto() {
		return new TimesheetEmailValidationResponseBodyDto(getEntityTypeApprover(), Collections.emptyList());
	}

	/** Row when timesheet exists but has no approver rows (LEFT JOIN placeholder). */
	public static TimesheetApproverEmailQueryRowDto createApproverQueryRowTimesheetWithoutApprover(int timesheetId) {
		return new TimesheetApproverEmailQueryRowDto(timesheetId, null, null, null, null, null, null, null, (byte) 0,
				(byte) 0, null, 100, null, (byte) 1, null);
	}

	public static TimesheetApproverEmailQueryRowDto createApproverQueryRowCompanyContactValid(int timesheetId) {
		return new TimesheetApproverEmailQueryRowDto(timesheetId, 9001, UserTypeEnum.COMPANY_CONTACT.getId(), 11652556,
				"contact", "Name", "contact@example.com", "slug-abc", (byte) 0, (byte) 0, null, 100, (byte) 1, (byte) 1,
				8001);
	}

	public static TimesheetApproverEmailQueryRowDto createApproverQueryRowAgencyRecruiter(int timesheetId) {
		return new TimesheetApproverEmailQueryRowDto(timesheetId, 9002, UserTypeEnum.AGENCY_RECRUITER.getId(), 20001,
				"Agency", "User", "agency@example.com", "agency-slug", (byte) 0, (byte) 0, null, 100, (byte) 1,
				(byte) 1, null);
	}

	/**
	 * Company contact approver on a job that is not shared with the client (job.AUTHID
	 * null or empty).
	 */
	public static TimesheetApproverEmailQueryRowDto createApproverQueryRowCompanyContactNotSharedWithClient(
			int timesheetId) {
		TimesheetApproverEmailQueryRowDto row = createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setSharedWithClient((byte) 0);
		return row;
	}

	public static TimesheetCandidateEmailQueryResultDto createCandidateQueryResultSubmitted(int timesheetId) {
		return new TimesheetCandidateEmailQueryResultDto(timesheetId, 500, "A", "B", 1, "slug", "e@mail.com", (byte) 0,
				(byte) 0, ApprovalStatusEnum.SUBMITTED.getId(), 10, null, 2, 7001);
	}

	public static TimesheetCandidateEmailQueryResultDto createCandidateQueryResultValid(int timesheetId) {
		return new TimesheetCandidateEmailQueryResultDto(timesheetId, 500, "A", "B", 1, "slug", "e@mail.com", (byte) 0,
				(byte) 0, ApprovalStatusEnum.OPEN.getId(), 10, null, 2, 7001);
	}

	public static TimesheetCandidateEmailQueryResultDto createCandidateQueryResultPortalDisabled(int timesheetId) {
		return new TimesheetCandidateEmailQueryResultDto(timesheetId, 500, "A", "B", 1, "slug", "e@mail.com", (byte) 0,
				(byte) 0, ApprovalStatusEnum.OPEN.getId(), 10, null, 3, 7001);
	}

	public static TimesheetCandidateEmailQueryResultDto createCandidateQueryResultPortalMissing(int timesheetId) {
		return new TimesheetCandidateEmailQueryResultDto(timesheetId, 500, "A", "B", 1, "slug", "e@mail.com", (byte) 0,
				(byte) 0, ApprovalStatusEnum.OPEN.getId(), 10, null, null, 7001);
	}

	public static TimesheetCandidateEmailQueryResultDto createCandidateQueryResultPortalInvitationNotSent(
			int timesheetId) {
		return new TimesheetCandidateEmailQueryResultDto(timesheetId, 500, "A", "B", 1, "slug", "e@mail.com", (byte) 0,
				(byte) 0, ApprovalStatusEnum.OPEN.getId(), 10, null, 0, 7001);
	}

	public static TimesheetCandidateEmailQueryResultDto createCandidateQueryResultPortalInvitationSent(
			int timesheetId) {
		return new TimesheetCandidateEmailQueryResultDto(timesheetId, 500, "A", "B", 1, "slug", "e@mail.com", (byte) 0,
				(byte) 0, ApprovalStatusEnum.OPEN.getId(), 10, null, 1, 7001);
	}

	public static TimesheetCandidateEmailQueryResultDto createCandidateQueryResultUnassigned(int timesheetId) {
		return new TimesheetCandidateEmailQueryResultDto(timesheetId, 500, "A", "B", 1, "slug", "e@mail.com", (byte) 0,
				(byte) 0, ApprovalStatusEnum.OPEN.getId(), null, null, 2, 7001);
	}

	/**
	 * Same as {@link #createApproverQueryRowCompanyContactValid(int)} but with null
	 * tblcontact.ownerid from DB.
	 */
	public static TimesheetApproverEmailQueryRowDto createApproverQueryRowCompanyContactValidNullOwnerId(
			int timesheetId) {
		TimesheetApproverEmailQueryRowDto row = createApproverQueryRowCompanyContactValid(timesheetId);
		row.setOwnerId(null);
		return row;
	}

	public static ResponseEntity<APINormalResponse<TimesheetEmailValidationResponseBodyDto>> createTimesheetEmailValidationSuccessResponse(
			TimesheetEmailValidationResponseBodyDto data) {
		APINormalResponse<TimesheetEmailValidationResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static final class Messages {

		public static final String TIMESHEET_VALIDATION_FETCHED_SUCCESSFULLY = "Timesheet validation fetched successfully";

		private Messages() {
			throw new UnsupportedOperationException("Utility class");
		}

	}

}
