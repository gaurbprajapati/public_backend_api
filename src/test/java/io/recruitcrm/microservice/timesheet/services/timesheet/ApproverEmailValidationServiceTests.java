package io.recruitcrm.microservice.timesheet.services.timesheet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.lenient;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetApproverEmailQueryRowDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetEmailValidationDetailDto;
import io.recruitcrm.microservice.timesheet.helpers.EmailValidationErrorHelper;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetEmailValidationRepository;
import io.recruitcrm.microservice.timesheet.testdata.EmailValidationErrorHelperTestDataFactory;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetEmailValidationTestDataFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ApproverEmailValidationServiceTests {

	private static final int ACCOUNT_ID = 45;

	@Mock
	private AuthHolder auth;

	@Mock
	private TimesheetEmailValidationRepository timesheetEmailValidationRepository;

	private final EmailValidationErrorHelper errorHelper = new EmailValidationErrorHelper();

	private ApproverEmailValidationService approverEmailValidationService;

	@BeforeEach
	void setUp() {
		lenient().when(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).thenReturn(ACCOUNT_ID);
		lenient().when(this.timesheetEmailValidationRepository.getClientPortalStatusByEmails(anyList(), eq(ACCOUNT_ID)))
			.thenAnswer((invocation) -> {
				List<String> emails = invocation.getArgument(0);
				Map<String, Integer> statusByEmail = new HashMap<>();
				for (String email : emails) {
					statusByEmail.put(email, EmailValidationErrorHelperTestDataFactory.getPortalEnabledStatusId());
				}
				return statusByEmail;
			});
		this.approverEmailValidationService = new ApproverEmailValidationService(this.auth,
				this.timesheetEmailValidationRepository, this.errorHelper);
	}

	private void stubClientPortalStatusByEmail(Map<String, Integer> statusByEmail) {
		given(this.timesheetEmailValidationRepository.getClientPortalStatusByEmails(anyList(), eq(ACCOUNT_ID)))
			.willReturn(statusByEmail);
	}

	@Test
	@DisplayName("Validate approver emails returns empty details when timesheet id list is empty")
	void testValidateApproverEmailsEmptyTimesheetIdListReturnsEmptyDetails() {
		// Given
		List<Integer> timesheetIds = Collections.emptyList();
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(Collections.emptyList());

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getReceiverType()).isEqualTo(1);
		assertThat(result.getTimesheetDetails()).isEmpty();
		then(this.timesheetEmailValidationRepository).should().getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID);
	}

	@Test
	@DisplayName("Validate approver emails marks missing timesheet as timesheet not exist")
	void testValidateApproverEmailsUnknownTimesheetIdReturnsTimesheetNotExist() {
		// Given
		List<Integer> timesheetIds = List.of(99999);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(Collections.emptyList());

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).getTimesheetId()).isEqualTo(99999);
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isNull();
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_exist");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails marks timesheet without approver as no timesheet approver")
	void testValidateApproverEmailsTimesheetWithoutApproverReturnsNoApproverError() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowTimesheetWithoutApprover(timesheetId);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("no_timesheet_approver");
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isNull();
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails returns valid detail for company contact approver")
	void testValidateApproverEmailsCompanyContactApproverReturnsValidDetail() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).getApproverTypeId())
			.isEqualTo(UserTypeEnum.COMPANY_CONTACT.getId());
		assertThat(result.getTimesheetDetails().get(0).getApproverEntityId()).isEqualTo(11652556);
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isEqualTo(8001);
		assertThat(result.getTimesheetDetails().get(0).getSerialNumber()).isNull();
		assertThat(result.getTimesheetDetails().get(0).getSlug()).isEqualTo("slug-abc");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isTrue();
		assertThat(result.getTimesheetDetails().get(0).getError()).isNull();
	}

	@Test
	@DisplayName("Validate approver emails omits serial number and clears slug for agency recruiter approver")
	void testValidateApproverEmailsAgencyRecruiterApproverClearsSerialAndSlug() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowAgencyRecruiter(timesheetId);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).getApproverTypeId())
			.isEqualTo(UserTypeEnum.AGENCY_RECRUITER.getId());
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isNull();
		assertThat(result.getTimesheetDetails().get(0).getSerialNumber()).isNull();
		assertThat(result.getTimesheetDetails().get(0).getSlug()).isNull();
		assertThat(result.getTimesheetDetails().get(0).isValid()).isTrue();
	}

	@Test
	@DisplayName("Validate approver emails returns approved timesheet error when latest status is approved")
	void testValidateApproverEmailsApprovedStatusReturnsApprovedTimesheetError() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("approved_timesheet");
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isEqualTo(8001);
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails returns null ownerId for company contact when tblcontact.ownerid is null")
	void testValidateApproverEmailsCompanyContactWithNullDbOwnerIdReturnsNullOwnerId() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValidNullOwnerId(timesheetId);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).isValid()).isTrue();
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isNull();
		assertThat(result.getTimesheetDetails().get(0).getApproverTypeId())
			.isEqualTo(UserTypeEnum.COMPANY_CONTACT.getId());
	}

	@Test
	@DisplayName("Validate approver emails emits contact then agency rows with aligned owner ids for mixed approvers")
	void testValidateApproverEmailsSameTimesheetMixedContactAndAgencyOwnerIds() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto contactRow = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		TimesheetApproverEmailQueryRowDto agencyRow = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowAgencyRecruiter(timesheetId);
		agencyRow.setTimesheetApproverId(9100);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(contactRow, agencyRow));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(2);
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isEqualTo(8001);
		assertThat(result.getTimesheetDetails().get(1).getOwnerId()).isNull();
	}

	@Test
	@DisplayName("Validate approver emails processes multiple timesheet ids in request order")
	void testValidateApproverEmailsMultipleTimesheetIdsReturnsOrderedDetails() {
		// Given
		int firstId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		int secondId = TimesheetEmailValidationTestDataFactory.getSecondTimesheetId();
		List<Integer> timesheetIds = List.of(firstId, secondId);
		TimesheetApproverEmailQueryRowDto row1 = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(firstId);
		TimesheetApproverEmailQueryRowDto row2 = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(secondId);
		row2.setTimesheetApproverId(9002);
		row2.setEntityId(11652557);
		row2.setOwnerId(8002);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row1, row2));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(2);
		assertThat(result.getTimesheetDetails().get(0).getTimesheetId()).isEqualTo(firstId);
		assertThat(result.getTimesheetDetails().get(1).getTimesheetId()).isEqualTo(secondId);
		assertThat(result.getTimesheetDetails().get(0).getOwnerId()).isEqualTo(8001);
		assertThat(result.getTimesheetDetails().get(1).getOwnerId()).isEqualTo(8002);
	}

	@Test
	@DisplayName("Validate approver emails returns open timesheet error when latest status is open")
	void testValidateApproverEmailsOpenStatusReturnsOpenTimesheetError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.OPEN.getId());
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("open_timesheet");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails returns rejected timesheet error when latest status is rejected")
	void testValidateApproverEmailsRejectedStatusReturnsRejectedTimesheetError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.REJECTED.getId());
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("rejected_timesheet");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails returns no email error when approver email is missing")
	void testValidateApproverEmailsMissingEmailReturnsNoEmailError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setEmailId("");
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("no_email");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails returns opted out error when approver opted out of email")
	void testValidateApproverEmailsOptedOutReturnsOptedOutError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setEmailOptOut((byte) 1);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("opted_out_of_email");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails returns deleted record error when approver record is deleted")
	void testValidateApproverEmailsDeletedRecordReturnsDeletedRecordError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setDeleted((byte) 1);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("deleted_record");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails returns not shared with client error when job AUTHID is missing")
	void testValidateApproverEmailsNotSharedWithClientReturnsNotSharedWithClientError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setSharedWithClient((byte) 0);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_shared_with_contact");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails not shared with client takes priority over missing email")
	void testValidateApproverEmailsNotSharedWithClientTakesPriorityOverMissingEmail() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setSharedWithClient((byte) 0);
		row.setEmailId(null);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_shared_with_contact");
	}

	@Test
	@DisplayName("Validate approver emails job deleted takes priority over not shared with client")
	void testValidateApproverEmailsJobDeletedTakesPriorityOverNotSharedWithClient() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setJobId(null);
		row.setSharedWithClient((byte) 0);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("deleted_record");
	}

	@Test
	@DisplayName("Validate approver emails returns not shared error when timesheet is not shared with contact")
	void testValidateApproverEmailsNotSharedWithContactReturnsNotSharedError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setSharedWithContact((byte) 0);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_shared_with_contact");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails returns deleted record error when job is deleted")
	void testValidateApproverEmailsJobDeletedReturnsDeletedRecordError() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setJobId(null);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("deleted_record");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
		assertThat(result.getTimesheetDetails().get(0).getName()).isEqualTo("contact Name");
		assertThat(result.getTimesheetDetails().get(0).getEmail()).isEqualTo("contact@example.com");
	}

	@Test
	@DisplayName("Validate approver emails returns deleted record for job deleted even when email is missing")
	void testValidateApproverEmailsJobDeletedTakesPriorityOverMissingEmail() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setJobId(null);
		row.setEmailId(null);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("deleted_record");
	}

	@Test
	@DisplayName("Validate approver emails status check takes priority over job deleted")
	void testValidateApproverEmailsApprovedStatusTakesPriorityOverJobDeleted() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		row.setJobId(null);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("approved_timesheet");
	}

	@Test
	@DisplayName("Validate approver emails returns deleted record for agency recruiter when job is deleted")
	void testValidateApproverEmailsAgencyRecruiterJobDeletedReturnsDeletedRecordError() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowAgencyRecruiter(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setJobId(null);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("deleted_record");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
		assertThat(result.getTimesheetDetails().get(0).getSlug()).isNull();
	}

	@Test
	@DisplayName("Validate approver emails not shared error only applies to company contact approverTypeId 1")
	void testValidateApproverEmailsNotSharedErrorOnlyForCompanyContact() {
		// Given
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setSharedWithContact((byte) 0);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails().get(0).getApproverTypeId())
			.isEqualTo(UserTypeEnum.COMPANY_CONTACT.getId());
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_shared_with_contact");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails agency recruiter never gets not shared with client or contact errors")
	void testValidateApproverEmailsAgencyRecruiterNeverGetsNotSharedError() {
		// Given - agency recruiter always gets sharedWithContact=1 and sharedWithClient=1
		// from DB query
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowAgencyRecruiter(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then - sharedWithContact=1 and sharedWithClient=1 for agency recruiter, so no
		// error
		assertThat(result.getTimesheetDetails().get(0).getApproverTypeId())
			.isEqualTo(UserTypeEnum.AGENCY_RECRUITER.getId());
		assertThat(result.getTimesheetDetails().get(0).getError()).isNull();
		assertThat(result.getTimesheetDetails().get(0).isValid()).isTrue();
	}

	@Test
	@DisplayName("Validate approver emails company contact shared with job returns no error")
	void testValidateApproverEmailsCompanyContactSharedWithJobReturnsNoError() {
		// Given - company contact is primary/secondary contact (sharedWithContact=1)
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setSharedWithContact((byte) 1);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then
		assertThat(result.getTimesheetDetails().get(0).getApproverTypeId())
			.isEqualTo(UserTypeEnum.COMPANY_CONTACT.getId());
		assertThat(result.getTimesheetDetails().get(0).getError()).isNull();
		assertThat(result.getTimesheetDetails().get(0).isValid()).isTrue();
	}

	@Test
	@DisplayName("Validate approver emails mixed approvers only company contact gets not shared error")
	void testValidateApproverEmailsMixedApproversOnlyCompanyContactGetsNotSharedError() {
		// Given - same timesheet with both a company contact (not shared) and agency
		// recruiter
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto contactRow = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		contactRow.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		contactRow.setSharedWithContact((byte) 0);
		TimesheetApproverEmailQueryRowDto agencyRow = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowAgencyRecruiter(timesheetId);
		agencyRow.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		agencyRow.setTimesheetApproverId(9100);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(contactRow, agencyRow));

		// When
		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		// Then - company contact gets error, agency recruiter does not
		assertThat(result.getTimesheetDetails()).hasSize(2);
		assertThat(result.getTimesheetDetails().get(0).getApproverTypeId())
			.isEqualTo(UserTypeEnum.COMPANY_CONTACT.getId());
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_shared_with_contact");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
		assertThat(result.getTimesheetDetails().get(1).getApproverTypeId())
			.isEqualTo(UserTypeEnum.AGENCY_RECRUITER.getId());
		assertThat(result.getTimesheetDetails().get(1).getError()).isNull();
		assertThat(result.getTimesheetDetails().get(1).isValid()).isTrue();
	}

	@Test
	@DisplayName("Validate approver emails company contact shared with client returns valid detail")
	void testValidateApproverEmailsCompanyContactSharedWithClientReturnsValidDetail() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setSharedWithClient((byte) 1);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails()).hasSize(1);
		assertThat(result.getTimesheetDetails().get(0).getApproverTypeId())
			.isEqualTo(UserTypeEnum.COMPANY_CONTACT.getId());
		assertThat(result.getTimesheetDetails().get(0).getError()).isNull();
		assertThat(result.getTimesheetDetails().get(0).isValid()).isTrue();
	}

	@Test
	@DisplayName("Validate approver emails company contact null sharedWithClient returns not shared with client error")
	void testValidateApproverEmailsCompanyContactNullSharedWithClientReturnsNotSharedWithClientError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setSharedWithClient(null);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getApproverTypeId())
			.isEqualTo(UserTypeEnum.COMPANY_CONTACT.getId());
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_shared_with_contact");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails not shared with client takes priority over opted out of email")
	void testValidateApproverEmailsNotSharedWithClientTakesPriorityOverOptedOut() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactNotSharedWithClient(timesheetId);
		row.setEmailOptOut((byte) 1);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_shared_with_contact");
	}

	@Test
	@DisplayName("Validate approver emails not shared with client takes priority over soft deleted record")
	void testValidateApproverEmailsNotSharedWithClientTakesPriorityOverSoftDeletedRecord() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactNotSharedWithClient(timesheetId);
		row.setDeleted((byte) 1);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_shared_with_contact");
	}

	@Test
	@DisplayName("Validate approver emails not shared with client takes priority over not shared with contact")
	void testValidateApproverEmailsNotSharedWithClientTakesPriorityOverNotSharedWithContact() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactNotSharedWithClient(timesheetId);
		row.setSharedWithContact((byte) 0);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_shared_with_contact");
	}

	@Test
	@DisplayName("Validate approver emails approved status takes priority over not shared with client")
	void testValidateApproverEmailsApprovedStatusTakesPriorityOverNotSharedWithClient() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactNotSharedWithClient(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("approved_timesheet");
	}

	@Test
	@DisplayName("Validate approver emails open status takes priority over not shared with client")
	void testValidateApproverEmailsOpenStatusTakesPriorityOverNotSharedWithClient() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactNotSharedWithClient(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.OPEN.getId());
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("open_timesheet");
	}

	@Test
	@DisplayName("Validate approver emails rejected status takes priority over not shared with client")
	void testValidateApproverEmailsRejectedStatusTakesPriorityOverNotSharedWithClient() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactNotSharedWithClient(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.REJECTED.getId());
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("rejected_timesheet");
	}

	@Test
	@DisplayName("Validate approver emails mixed approvers only company contact gets not shared with client error")
	void testValidateApproverEmailsMixedApproversOnlyCompanyContactGetsNotSharedWithClientError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto contactRow = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactNotSharedWithClient(timesheetId);
		TimesheetApproverEmailQueryRowDto agencyRow = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowAgencyRecruiter(timesheetId);
		agencyRow.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		agencyRow.setTimesheetApproverId(9100);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(contactRow, agencyRow));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails()).hasSize(2);
		assertThat(result.getTimesheetDetails().get(0).getApproverTypeId())
			.isEqualTo(UserTypeEnum.COMPANY_CONTACT.getId());
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_shared_with_contact");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
		assertThat(result.getTimesheetDetails().get(1).getApproverTypeId())
			.isEqualTo(UserTypeEnum.AGENCY_RECRUITER.getId());
		assertThat(result.getTimesheetDetails().get(1).getError()).isNull();
		assertThat(result.getTimesheetDetails().get(1).isValid()).isTrue();
	}

	@Test
	@DisplayName("Validate approver emails multiple timesheets only company contact not shared with client is flagged")
	void testValidateApproverEmailsMultipleTimesheetsOnlyCompanyContactNotSharedWithClientFlagged() {
		int firstId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		int secondId = TimesheetEmailValidationTestDataFactory.getSecondTimesheetId();
		List<Integer> timesheetIds = List.of(firstId, secondId);
		TimesheetApproverEmailQueryRowDto row1 = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactNotSharedWithClient(firstId);
		TimesheetApproverEmailQueryRowDto row2 = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(secondId);
		row2.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row2.setTimesheetApproverId(9003);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row1, row2));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails()).hasSize(2);
		assertThat(result.getTimesheetDetails().get(0).getTimesheetId()).isEqualTo(firstId);
		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("timesheet_not_shared_with_contact");
		assertThat(result.getTimesheetDetails().get(1).getTimesheetId()).isEqualTo(secondId);
		assertThat(result.getTimesheetDetails().get(1).getError()).isNull();
		assertThat(result.getTimesheetDetails().get(1).isValid()).isTrue();
	}

	@Test
	@DisplayName("Validate approver emails does not load portal status when only agency recruiters exist")
	void testValidateApproverEmailsAgencyRecruiterOnlyDoesNotLoadPortalStatus() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowAgencyRecruiter(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		then(this.timesheetEmailValidationRepository).should(never())
			.getClientPortalStatusByEmails(anyList(), eq(ACCOUNT_ID));
	}

	@Test
	@DisplayName("Validate approver emails returns portal does not exist when portal status row is missing")
	void testValidateApproverEmailsPortalMissingReturnsPortalDoesNotExistError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));
		this.stubClientPortalStatusByEmail(Collections.emptyMap());

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("portal_does_not_exist");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails returns portal does not exist when portal status is invitation not sent")
	void testValidateApproverEmailsPortalInvitationNotSentReturnsPortalDoesNotExistError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));
		this.stubClientPortalStatusByEmail(Map.of(row.getEmailId(),
				EmailValidationErrorHelperTestDataFactory.getPortalInvitationNotSentStatusId()));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("portal_does_not_exist");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails returns portal does not exist when portal status is invitation sent")
	void testValidateApproverEmailsPortalInvitationSentReturnsPortalDoesNotExistError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));
		this.stubClientPortalStatusByEmail(
				Map.of(row.getEmailId(), EmailValidationErrorHelperTestDataFactory.getPortalInvitationSentStatusId()));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("portal_does_not_exist");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails returns portal is disabled when portal status is disabled")
	void testValidateApproverEmailsPortalDisabledReturnsPortalDisabledError() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));
		this.stubClientPortalStatusByEmail(
				Map.of(row.getEmailId(), EmailValidationErrorHelperTestDataFactory.getPortalDisabledStatusId()));

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("portal_is_disabled");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails portal does not exist takes priority over not shared with contact")
	void testValidateApproverEmailsPortalDoesNotExistTakesPriorityOverNotSharedWithContact() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setSharedWithContact((byte) 0);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));
		this.stubClientPortalStatusByEmail(Collections.emptyMap());

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("portal_does_not_exist");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails deleted record takes priority over portal does not exist")
	void testValidateApproverEmailsDeletedRecordTakesPriorityOverPortalDoesNotExist() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setDeleted((byte) 1);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));
		this.stubClientPortalStatusByEmail(Collections.emptyMap());

		var result = this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		assertThat(result.getTimesheetDetails().get(0).getError()).isEqualTo("deleted_record");
		assertThat(result.getTimesheetDetails().get(0).isValid()).isFalse();
	}

	@Test
	@DisplayName("Validate approver emails does not load portal status when company contact email is missing")
	void testValidateApproverEmailsMissingEmailDoesNotLoadPortalStatus() {
		int timesheetId = TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = List.of(timesheetId);
		TimesheetApproverEmailQueryRowDto row = TimesheetEmailValidationTestDataFactory
			.createApproverQueryRowCompanyContactValid(timesheetId);
		row.setLatestApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		row.setEmailId(null);
		given(this.timesheetEmailValidationRepository.getApproverEmailValidationRows(timesheetIds, ACCOUNT_ID))
			.willReturn(List.of(row));

		this.approverEmailValidationService.validateApproverEmails(timesheetIds);

		then(this.timesheetEmailValidationRepository).should(never())
			.getClientPortalStatusByEmails(anyList(), eq(ACCOUNT_ID));
	}

	@Test
	@DisplayName("(Reflection) buildApproverValidationDetailsForTimesheet empty group returns timesheet not exist error")
	void testBuildApproverValidationDetailsForTimesheetReflectionEmptyGroupReturnsTimesheetNotExist() {
		@SuppressWarnings("unchecked")
		List<TimesheetEmailValidationDetailDto> result = (List<TimesheetEmailValidationDetailDto>) ReflectionTestUtils
			.invokeMethod(this.approverEmailValidationService, "buildApproverValidationDetailsForTimesheet",
					TimesheetEmailValidationTestDataFactory.getDefaultTimesheetId(), Collections.emptyList(),
					UserTypeEnum.AGENCY_RECRUITER.getId(), Collections.emptyMap());

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getError()).isEqualTo("timesheet_not_exist");
		assertThat(result.get(0).isValid()).isFalse();
	}

}
