package io.recruitcrm.microservice.timesheet.services.timesheet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCandidateEmailQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.EmailValidationErrorHelper;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetEmailValidationRepository;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetEmailValidationTestDataFactory;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractorEmailValidationServiceTests {

	private static final int ACCOUNT_ID = 45;

	private static final int ENTITY_TYPE_CONTRACTOR = 3;

	@Mock
	private AuthHolder auth;

	@Mock
	private TimesheetEmailValidationRepository timesheetEmailValidationRepository;

	private final EmailValidationErrorHelper errorHelper = new EmailValidationErrorHelper();

	private ContractorEmailValidationService contractorEmailValidationService;

	@BeforeEach
	void setUp() {
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		this.contractorEmailValidationService = new ContractorEmailValidationService(this.auth,
				this.timesheetEmailValidationRepository, this.errorHelper);
	}

	@Test
	@DisplayName("Validate contractor emails returns empty details when timesheet id list is empty")
	void testValidateContractorEmailsEmptyTimesheetIdListReturnsEmptyDetails() {
		// Given
		List<Integer> timesheetIds = Collections.emptyList();
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(Collections.emptyList());

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getReceiverType()).isEqualTo(ENTITY_TYPE_CONTRACTOR);
		assertThat(result.getTimesheetDetails()).isEmpty();
		then(this.timesheetEmailValidationRepository).should()
			.getTimesheetValidationData(timesheetIds, ACCOUNT_ID, ENTITY_TYPE_CONTRACTOR);
	}

	@Test
	@DisplayName("Validate contractor emails marks unknown timesheet as timesheet not exist")
	void testValidateContractorEmailsUnknownTimesheetReturnsTimesheetNotExist() {
		// Given
		List<Integer> timesheetIds = List.of(88888);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(Collections.emptyList());

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).getTimesheetId()).isEqualTo(88888);
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isNull();
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_exist");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate contractor emails returns submitted timesheet error when status is submitted")
	void testValidateContractorEmailsSubmittedStatusReturnsSubmittedError() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultSubmitted(timesheetId);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("submitted_timesheet");
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isEqualTo(7001);
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate contractor emails returns valid detail when contractor passes all checks")
	void testValidateContractorEmailsValidCandidateReturnsValidDetail() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(timesheetId);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).getContractorEntityId()).isEqualTo(500);
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isEqualTo(7001);
		assertThat(result.getTimesheetDetails().get(0).isValid()).isTrue();
		assertThat(result.getTimesheetDetails().get(0).getError()).isNull();
	}

	@Test
	@DisplayName("Validate contractor emails returns null ownerId when tblcandidate.ownerid is null")
	void testValidateContractorEmailsCandidateWithNullOwnerIdReturnsNullOwnerId() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(timesheetId);
		dto.setOwnerId(null);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).isValid()).isTrue();
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isNull();
	}

	@Test
	@DisplayName("Validate contractor emails returns portal is disabled when portal status is disabled")
	void testValidateContractorEmailsPortalDisabledReturnsPortalDisabledError() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultPortalDisabled(timesheetId);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("portal_is_disabled");
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isEqualTo(7001);
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate contractor emails returns portal does not exist when portal status row missing")
	void testValidateContractorEmailsPortalMissingReturnsPortalDoesNotExistError() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultPortalMissing(timesheetId);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("portal_does_not_exist");
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isEqualTo(7001);
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate contractor emails returns portal does not exist when portal status is invitation not sent")
	void testValidateContractorEmailsPortalInvitationNotSentReturnsPortalDoesNotExistError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultPortalInvitationNotSent(timesheetId);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("portal_does_not_exist");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate contractor emails returns portal does not exist when portal status is invitation sent")
	void testValidateContractorEmailsPortalInvitationSentReturnsPortalDoesNotExistError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultPortalInvitationSent(timesheetId);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("portal_does_not_exist");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate contractor emails unassigned takes priority over portal does not exist")
	void testValidateContractorEmailsUnassignedTakesPriorityOverPortalDoesNotExist() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultPortalInvitationNotSent(timesheetId);
		dto.setAssignmentId(null);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("contractor_unassigned_from_job");
	}

	@Test
	@DisplayName("Validate contractor emails returns unassigned error when assignment id is null")
	void testValidateContractorEmailsUnassignedReturnsUnassignedError() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultUnassigned(timesheetId);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("contractor_unassigned_from_job");
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isEqualTo(7001);
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate contractor emails preserves request order for multiple ids")
	void testValidateContractorEmailsMultipleIdsPreservesOrder() {
		// Given
		int firstId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		int secondId = TimesheetEmailValidationTestDataFactory.getSecondTimesheetId();
		List<Integer> timesheetIds = List.of(firstId, secondId);
		TimesheetCandidateEmailQueryResultDto dto1 = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(firstId);
		TimesheetCandidateEmailQueryResultDto dto2 = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(secondId);
		dto2.setCandidateId(501);
		dto2.setOwnerId(9002);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto1, dto2));

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(2);
		assertThat(result.getTimesheetDetails().get(0).getTimesheetId()).isEqualTo(firstId);
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isEqualTo(7001);
		assertThat(result.getTimesheetDetails().get(1).getTimesheetId()).isEqualTo(secondId);
		assertThat(result.getTimesheetDetails().get(1).getOwnerId()).isEqualTo(9002);
	}

	@Test
	@DisplayName("Validate contractor emails returns approved timesheet error when status is approved")
	void testValidateContractorEmailsApprovedStatusReturnsApprovedError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(timesheetId);
		dto.setLatestApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("approved_timesheet");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate contractor emails returns no email error when contractor email is missing")
	void testValidateContractorEmailsMissingEmailReturnsNoEmailError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(timesheetId);
		dto.setEmailId(null);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("no_email");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate contractor emails returns opted out error when contractor opted out of email")
	void testValidateContractorEmailsOptedOutReturnsOptedOutError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(timesheetId);
		dto.setEmailOptOut((byte) 1);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("opted_out_of_email");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate contractor emails returns deleted record error when contractor record is deleted")
	void testValidateContractorEmailsDeletedRecordReturnsDeletedRecordError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(timesheetId);
		dto.setDeleted((byte) 1);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("deleted_record");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate contractor emails returns deleted record when candidate is physically deleted")
	void testValidateContractorEmailsCandidatePhysicallyDeletedReturnsDeletedRecordError() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(timesheetId);
		dto.setCandidateId(null);
		dto.setFirstName(null);
		dto.setLastName(null);
		dto.setEmailId(null);
		dto.setSrno(null);
		dto.setSlug(null);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("deleted_record");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
		assertThat(result.getTimesheetDetails().get(0).getContractorEntityId()).isNull();
	}

	@Test
	@DisplayName("Validate contractor emails candidate physically deleted takes priority over email missing")
	void testValidateContractorEmailsCandidatePhysicallyDeletedTakesPriorityOverEmailMissing() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(timesheetId);
		dto.setCandidateId(null);
		dto.setEmailId(null);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("deleted_record");
	}

	@Test
	@DisplayName("Validate contractor emails status check takes priority over candidate physically deleted")
	void testValidateContractorEmailsSubmittedStatusTakesPriorityOverCandidateDeleted() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(timesheetId);
		dto.setCandidateId(null);
		dto.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("submitted_timesheet");
	}

	@Test
	@DisplayName("Validate contractor emails returns unassigned from job when job is deleted")
	void testValidateContractorEmailsJobDeletedReturnsUnassignedFromJobError() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(timesheetId);
		dto.setAssignmentId(null);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("contractor_unassigned_from_job");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
		assertThat(result.getTimesheetDetails().get(0).getName()).isEqualTo("A B");
		assertThat(result.getTimesheetDetails().get(0).getEmail()).isEqualTo("e@mail.com");
	}

	@Test
	@DisplayName("Validate contractor emails job deleted still returns candidate name and email")
	void testValidateContractorEmailsJobDeletedStillReturnsCandidateInfo() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetCandidateEmailQueryResultDto dto = TimesheetEmailValidationTestDataFactory
			.createCandidateQueryResultValid(timesheetId);
		dto.setAssignmentId(null);
		dto.setPortalStatusId(null);
		given(this.timesheetEmailValidationRepository.getTimesheetValidationData(timesheetIds, ACCOUNT_ID,
				ENTITY_TYPE_CONTRACTOR))
			.willReturn(List.of(dto));

		// When
		var result = this.contractorEmailValidationService.validateContractorEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails().get(0).getContractorEntityId()).isEqualTo(500);
		assertThat(result.getTimesheetDetails().get(0).getName()).isEqualTo("A B");
		assertThat(result.getTimesheetDetails().get(0).getEmail()).isEqualTo("e@mail.com");
		assertThat(result.getTimesheetDetails().get(0).getSerialNumber()).isEqualTo(1);
		assertThat(result.getTimesheetDetails().get(0).getSlug()).isEqualTo("slug");
	}

}
