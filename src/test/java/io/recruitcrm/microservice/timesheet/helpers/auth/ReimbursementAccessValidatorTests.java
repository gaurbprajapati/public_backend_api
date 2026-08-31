package io.recruitcrm.microservice.timesheet.helpers.auth;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.entity.model.Account;
import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.User;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.exceptions.ForbiddenAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.constants.ExceptionMessageConstants;
import io.recruitcrm.microservice.timesheet.helpers.constants.ReimbursementConstants;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.ITimesheetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReimbursementAccessValidator Tests")
class ReimbursementAccessValidatorTests {

	@Mock
	private AuthHolder auth;

	@Mock
	private TimesheetJpaRepository timesheetJpaRepository;

	@Mock
	private AccessControlChecker contractStaffingAccessControlChecker;

	@Mock
	private ITimesheetRepository timesheetRepository;

	private ReimbursementAccessValidator validator;

	private static final Integer TIMESHEET_ID = 1;

	private static final Integer ACCOUNT_ID = 100;

	private static final Integer CANDIDATE_ID = 200;

	private static final Integer USER_ID = 300;

	@BeforeEach
	void setUp() {
		this.validator = new ReimbursementAccessValidator(this.auth, this.timesheetJpaRepository,
				this.contractStaffingAccessControlChecker, this.timesheetRepository);
	}

	private Timesheet createMockTimesheet() {
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);
		return timesheet;
	}

	private UserPrincipal createUserPrincipal() {
		User user = mock(User.class);
		Account account = mock(Account.class);
		lenient().when(user.getId()).thenReturn(USER_ID);
		lenient().when(account.getId()).thenReturn(ACCOUNT_ID);
		lenient().when(user.getAccount()).thenReturn(account);
		return new UserPrincipal(user);
	}

	private ContractorPrincipal createContractorPrincipal(Integer candidateId) {
		Candidate candidate = mock(Candidate.class);
		lenient().when(candidate.getId()).thenReturn(candidateId);
		lenient().when(candidate.getAccountId()).thenReturn(ACCOUNT_ID);
		return new ContractorPrincipal(candidate);
	}

	private Candidate createCandidate(Integer candidateId) {
		Candidate candidate = mock(Candidate.class);
		given(candidate.getId()).willReturn(candidateId);
		return candidate;
	}

	private AuthPrincipal createContactPrincipal() {
		return new AuthPrincipal() {
			@Override
			public PrincipalType getPrincipalType() {
				return PrincipalType.CONTACT;
			}

			@Override
			public Integer getUniqueIdentifier() {
				return 500;
			}

			@Override
			public Integer getOrganizationIdentifier() {
				return ACCOUNT_ID;
			}

			@Override
			public String getEmail() {
				return "contact@test.com";
			}

			@Override
			public String getDisplayName() {
				return "Test Contact";
			}

			@Override
			public String getFullName() {
				return "Test Contact";
			}

			@Override
			public Integer getRoleIdentifier() {
				return 0;
			}
		};
	}

	@Nested
	@DisplayName("validateTimesheetViewAccess Tests")
	class ValidateTimesheetViewAccessTests {

		@Test
		@DisplayName("Should validate view access for USER principal")
		void testValidateViewAccessForUserPrincipal() {
			Timesheet timesheet = createMockTimesheet();
			UserPrincipal userPrincipal = createUserPrincipal();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateTimesheetViewAccess(TIMESHEET_ID, ACCOUNT_ID);

			ArgumentCaptor<PermissionCheckContext> ctxCaptor = ArgumentCaptor.forClass(PermissionCheckContext.class);
			ArgumentCaptor<AccessControlCheckMetadataContext> metaCaptor = ArgumentCaptor
				.forClass(AccessControlCheckMetadataContext.class);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker)
				.allows(eq(Entity.TIMESHEET), ctxCaptor.capture(), metaCaptor.capture());

			assertThat(ctxCaptor.getValue().getPermission()).isEqualTo(Permission.VIEW_TIMESHEET);
			assertThat(ctxCaptor.getValue().getPermissionLevel()).isEqualTo(PermissionLevel.YES);
			assertThat(metaCaptor.getValue().getTimesheetId()).isEqualTo(TIMESHEET_ID);
		}

		@Test
		@DisplayName("Should validate view access for CONTRACTOR principal without access control check")
		void testValidateViewAccessForContractorPrincipal() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateTimesheetViewAccess(TIMESHEET_ID, ACCOUNT_ID);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker, never()).allows(any(),
					any(), any());
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when timesheet not found")
		void testValidateViewAccessThrowsWhenTimesheetNotFound() {
			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.empty());

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateTimesheetViewAccess(TIMESHEET_ID, ACCOUNT_ID)).isInstanceOf(ResourceNotFoundException.class);
		}

	}

	@Nested
	@DisplayName("validateTimesheetEditAccess Tests")
	class ValidateTimesheetEditAccessTests {

		@Test
		@DisplayName("Should validate edit access for USER principal")
		void testValidateEditAccessForUserPrincipal() {
			Timesheet timesheet = createMockTimesheet();
			UserPrincipal userPrincipal = createUserPrincipal();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateTimesheetEditAccess(TIMESHEET_ID, ACCOUNT_ID);

			ArgumentCaptor<PermissionCheckContext> ctxCaptor = ArgumentCaptor.forClass(PermissionCheckContext.class);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker)
				.allows(eq(Entity.TIMESHEET), ctxCaptor.capture(), any(AccessControlCheckMetadataContext.class));

			assertThat(ctxCaptor.getValue().getPermission()).isEqualTo(Permission.EDIT_TIMESHEET);
		}

		@Test
		@DisplayName("Should validate edit access for CONTRACTOR principal without access control check")
		void testValidateEditAccessForContractorPrincipal() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateTimesheetEditAccess(TIMESHEET_ID, ACCOUNT_ID);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker, never()).allows(any(),
					any(), any());
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when timesheet not found")
		void testValidateEditAccessThrowsWhenTimesheetNotFound() {
			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.empty());

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateTimesheetEditAccess(TIMESHEET_ID, ACCOUNT_ID)).isInstanceOf(ResourceNotFoundException.class);
		}

	}

	@Nested
	@DisplayName("validateTimesheetApproveAccess Tests")
	class ValidateTimesheetApproveAccessTests {

		@Test
		@DisplayName("Should validate approve access for USER principal")
		void testValidateApproveAccessForUserPrincipal() {
			Timesheet timesheet = createMockTimesheet();
			UserPrincipal userPrincipal = createUserPrincipal();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateTimesheetApproveAccess(TIMESHEET_ID, ACCOUNT_ID);

			ArgumentCaptor<PermissionCheckContext> ctxCaptor = ArgumentCaptor.forClass(PermissionCheckContext.class);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker)
				.allows(eq(Entity.TIMESHEET), ctxCaptor.capture(), any(AccessControlCheckMetadataContext.class));

			assertThat(ctxCaptor.getValue().getPermission()).isEqualTo(Permission.APPROVE_TIMESHEET);
		}

		@Test
		@DisplayName("Should validate approve access for CONTRACTOR principal without access control check")
		void testValidateApproveAccessForContractorPrincipal() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateTimesheetApproveAccess(TIMESHEET_ID, ACCOUNT_ID);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker, never()).allows(any(),
					any(), any());
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when timesheet not found")
		void testValidateApproveAccessThrowsWhenTimesheetNotFound() {
			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.empty());

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateTimesheetApproveAccess(TIMESHEET_ID, ACCOUNT_ID))
				.isInstanceOf(ResourceNotFoundException.class);
		}

	}

	@Nested
	@DisplayName("validateTimesheetCreateAccess Tests")
	class ValidateTimesheetCreateAccessTests {

		@Test
		@DisplayName("Should validate create access for USER principal")
		void testValidateCreateAccessForUserPrincipal() {
			UserPrincipal userPrincipal = createUserPrincipal();
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateTimesheetCreateAccess(TIMESHEET_ID);

			ArgumentCaptor<PermissionCheckContext> ctxCaptor = ArgumentCaptor.forClass(PermissionCheckContext.class);
			ArgumentCaptor<AccessControlCheckMetadataContext> metaCaptor = ArgumentCaptor
				.forClass(AccessControlCheckMetadataContext.class);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker)
				.allows(eq(Entity.TIMESHEET), ctxCaptor.capture(), metaCaptor.capture());

			assertThat(ctxCaptor.getValue().getPermission()).isEqualTo(Permission.CREATE_TIMESHEET);
			assertThat(ctxCaptor.getValue().getPermissionLevel()).isEqualTo(PermissionLevel.YES);
			assertThat(metaCaptor.getValue().getTimesheetId()).isEqualTo(TIMESHEET_ID);
		}

		@Test
		@DisplayName("Should not check access control for CONTRACTOR principal")
		void testValidateCreateAccessForContractorPrincipal() {
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateTimesheetCreateAccess(TIMESHEET_ID);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker, never()).allows(any(),
					any(), any());
		}

	}

	@Nested
	@DisplayName("validateAccessControlForCreateReimbursement Tests")
	class ValidateAccessControlForCreateReimbursementTests {

		@Test
		@DisplayName("Should allow USER principal with CREATE_TIMESHEET permission")
		void testAllowUserPrincipalWithCreatePermission() {
			Timesheet timesheet = createMockTimesheet();
			UserPrincipal userPrincipal = createUserPrincipal();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForCreateReimbursement(TIMESHEET_ID,
					ACCOUNT_ID);

			ArgumentCaptor<PermissionCheckContext> ctxCaptor = ArgumentCaptor.forClass(PermissionCheckContext.class);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker)
				.allows(eq(Entity.TIMESHEET), ctxCaptor.capture(), any(AccessControlCheckMetadataContext.class));

			assertThat(ctxCaptor.getValue().getPermission()).isEqualTo(Permission.CREATE_TIMESHEET);
		}

		@Test
		@DisplayName("Should allow CONTRACTOR principal for their own timesheet")
		void testAllowContractorPrincipalForOwnTimesheet() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);
			Candidate candidate = createCandidate(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
			given(ReimbursementAccessValidatorTests.this.timesheetRepository.getCandidateLinkedToTimesheet(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(candidate);

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForCreateReimbursement(TIMESHEET_ID,
					ACCOUNT_ID);

			verify(ReimbursementAccessValidatorTests.this.timesheetRepository)
				.getCandidateLinkedToTimesheet(TIMESHEET_ID, ACCOUNT_ID);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when contractor tries to create for another timesheet")
		void testThrowWhenContractorCreatesForAnotherTimesheet() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);
			Candidate differentCandidate = createCandidate(999);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
			given(ReimbursementAccessValidatorTests.this.timesheetRepository.getCandidateLinkedToTimesheet(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(differentCandidate);

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateAccessControlForCreateReimbursement(TIMESHEET_ID, ACCOUNT_ID))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage(ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_CREATE_OWN_ONLY);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when candidate is null")
		void testThrowWhenCandidateIsNull() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
			given(ReimbursementAccessValidatorTests.this.timesheetRepository.getCandidateLinkedToTimesheet(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(null);

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateAccessControlForCreateReimbursement(TIMESHEET_ID, ACCOUNT_ID))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage(ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_CREATE_OWN_ONLY);
		}

		@Test
		@DisplayName("Should throw ForbiddenAccessException for CONTACT principal")
		void testThrowForContactPrincipal() {
			Timesheet timesheet = createMockTimesheet();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal())
				.willReturn(createContactPrincipal());

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateAccessControlForCreateReimbursement(TIMESHEET_ID, ACCOUNT_ID))
				.isInstanceOf(ForbiddenAccessException.class)
				.hasMessage(ExceptionMessageConstants.REIMBURSEMENT_CREATE_FORBIDDEN);
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when timesheet not found")
		void testThrowWhenTimesheetNotFound() {
			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.empty());

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateAccessControlForCreateReimbursement(TIMESHEET_ID, ACCOUNT_ID))
				.isInstanceOf(ResourceNotFoundException.class);
		}

	}

	@Nested
	@DisplayName("validateAccessControlForUpdateReimbursement Tests")
	class ValidateAccessControlForUpdateReimbursementTests {

		@Test
		@DisplayName("Should allow USER principal with SUBMITTED status")
		void testAllowUserPrincipalWithSubmittedStatus() {
			Timesheet timesheet = createMockTimesheet();
			UserPrincipal userPrincipal = createUserPrincipal();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForUpdateReimbursement(TIMESHEET_ID,
					ACCOUNT_ID, ReimbursementConstants.STATUS_SUBMITTED);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker).allows(
					eq(Entity.TIMESHEET), any(PermissionCheckContext.class),
					any(AccessControlCheckMetadataContext.class));
		}

		@Test
		@DisplayName("Should throw ValidationErrorException when USER tries to update APPROVED reimbursement")
		void testThrowWhenUserUpdatesApprovedReimbursement() {
			Timesheet timesheet = createMockTimesheet();
			UserPrincipal userPrincipal = createUserPrincipal();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

			assertThatThrownBy(
					() -> ReimbursementAccessValidatorTests.this.validator.validateAccessControlForUpdateReimbursement(
							TIMESHEET_ID, ACCOUNT_ID, ReimbursementConstants.STATUS_APPROVED))
				.isInstanceOf(ValidationErrorException.class)
				.hasMessage(ExceptionMessageConstants.REIMBURSEMENT_NOT_EDITABLE);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker).allows(
					eq(Entity.TIMESHEET), any(PermissionCheckContext.class),
					any(AccessControlCheckMetadataContext.class));
		}

		@Test
		@DisplayName("Should allow USER principal with REJECTED status")
		void testAllowUserPrincipalWithRejectedStatus() {
			Timesheet timesheet = createMockTimesheet();
			UserPrincipal userPrincipal = createUserPrincipal();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForUpdateReimbursement(TIMESHEET_ID,
					ACCOUNT_ID, ReimbursementConstants.STATUS_REJECTED);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker).allows(
					eq(Entity.TIMESHEET), any(PermissionCheckContext.class),
					any(AccessControlCheckMetadataContext.class));
		}

		@Test
		@DisplayName("Should allow CONTRACTOR principal for own timesheet with non-APPROVED status")
		void testAllowContractorForOwnTimesheetWithNonApprovedStatus() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);
			Candidate candidate = createCandidate(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
			given(ReimbursementAccessValidatorTests.this.timesheetRepository.getCandidateLinkedToTimesheet(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(candidate);

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForUpdateReimbursement(TIMESHEET_ID,
					ACCOUNT_ID, ReimbursementConstants.STATUS_SUBMITTED);

			verify(ReimbursementAccessValidatorTests.this.timesheetRepository)
				.getCandidateLinkedToTimesheet(TIMESHEET_ID, ACCOUNT_ID);
		}

		@Test
		@DisplayName("Should allow CONTRACTOR to update REJECTED reimbursement")
		void testAllowContractorToUpdateRejectedReimbursement() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);
			Candidate candidate = createCandidate(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
			given(ReimbursementAccessValidatorTests.this.timesheetRepository.getCandidateLinkedToTimesheet(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(candidate);

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForUpdateReimbursement(TIMESHEET_ID,
					ACCOUNT_ID, ReimbursementConstants.STATUS_REJECTED);

			verify(ReimbursementAccessValidatorTests.this.timesheetRepository)
				.getCandidateLinkedToTimesheet(TIMESHEET_ID, ACCOUNT_ID);
		}

		@Test
		@DisplayName("Should throw ValidationErrorException when CONTRACTOR tries to update APPROVED reimbursement")
		void testThrowWhenContractorUpdatesApprovedReimbursement() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);
			Candidate candidate = createCandidate(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
			given(ReimbursementAccessValidatorTests.this.timesheetRepository.getCandidateLinkedToTimesheet(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(candidate);

			assertThatThrownBy(
					() -> ReimbursementAccessValidatorTests.this.validator.validateAccessControlForUpdateReimbursement(
							TIMESHEET_ID, ACCOUNT_ID, ReimbursementConstants.STATUS_APPROVED))
				.isInstanceOf(ValidationErrorException.class)
				.hasMessage(ExceptionMessageConstants.REIMBURSEMENT_NOT_EDITABLE);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when CONTRACTOR tries to update another's timesheet")
		void testThrowWhenContractorUpdatesAnotherTimesheet() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);
			Candidate differentCandidate = createCandidate(999);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
			given(ReimbursementAccessValidatorTests.this.timesheetRepository.getCandidateLinkedToTimesheet(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(differentCandidate);

			assertThatThrownBy(
					() -> ReimbursementAccessValidatorTests.this.validator.validateAccessControlForUpdateReimbursement(
							TIMESHEET_ID, ACCOUNT_ID, ReimbursementConstants.STATUS_SUBMITTED))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage(ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_UPDATE_OWN_ONLY);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when candidate is null for CONTRACTOR")
		void testThrowWhenCandidateIsNullForContractor() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
			given(ReimbursementAccessValidatorTests.this.timesheetRepository.getCandidateLinkedToTimesheet(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(null);

			assertThatThrownBy(
					() -> ReimbursementAccessValidatorTests.this.validator.validateAccessControlForUpdateReimbursement(
							TIMESHEET_ID, ACCOUNT_ID, ReimbursementConstants.STATUS_SUBMITTED))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage(ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_UPDATE_OWN_ONLY);
		}

		@Test
		@DisplayName("Should throw ForbiddenAccessException for CONTACT principal")
		void testThrowForContactPrincipal() {
			Timesheet timesheet = createMockTimesheet();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal())
				.willReturn(createContactPrincipal());

			assertThatThrownBy(
					() -> ReimbursementAccessValidatorTests.this.validator.validateAccessControlForUpdateReimbursement(
							TIMESHEET_ID, ACCOUNT_ID, ReimbursementConstants.STATUS_SUBMITTED))
				.isInstanceOf(ForbiddenAccessException.class)
				.hasMessage(ExceptionMessageConstants.REIMBURSEMENT_UPDATE_FORBIDDEN);
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when timesheet not found")
		void testThrowWhenTimesheetNotFound() {
			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.empty());

			assertThatThrownBy(
					() -> ReimbursementAccessValidatorTests.this.validator.validateAccessControlForUpdateReimbursement(
							TIMESHEET_ID, ACCOUNT_ID, ReimbursementConstants.STATUS_SUBMITTED))
				.isInstanceOf(ResourceNotFoundException.class);
		}

	}

	@Nested
	@DisplayName("validateAccessControlForDeleteReimbursement Tests")
	class ValidateAccessControlForDeleteReimbursementTests {

		@Test
		@DisplayName("Should allow USER principal with DELETE_TIMESHEET permission")
		void testAllowUserPrincipalWithDeletePermission() {
			Timesheet timesheet = createMockTimesheet();
			UserPrincipal userPrincipal = createUserPrincipal();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForDeleteReimbursement(TIMESHEET_ID,
					ACCOUNT_ID);

			ArgumentCaptor<PermissionCheckContext> ctxCaptor = ArgumentCaptor.forClass(PermissionCheckContext.class);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker)
				.allows(eq(Entity.TIMESHEET), ctxCaptor.capture(), any(AccessControlCheckMetadataContext.class));

			assertThat(ctxCaptor.getValue().getPermission()).isEqualTo(Permission.DELETE_TIMESHEET);
		}

		@Test
		@DisplayName("Should allow CONTRACTOR to delete own SUBMITTED reimbursement")
		void testAllowContractorToDeleteOwnSubmittedReimbursement() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);
			Candidate candidate = createCandidate(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
			given(ReimbursementAccessValidatorTests.this.timesheetRepository.getCandidateLinkedToTimesheet(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(candidate);

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForDeleteReimbursement(TIMESHEET_ID,
					ACCOUNT_ID);

			verify(ReimbursementAccessValidatorTests.this.timesheetRepository)
				.getCandidateLinkedToTimesheet(TIMESHEET_ID, ACCOUNT_ID);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when CONTRACTOR tries to delete another's reimbursement")
		void testThrowWhenContractorDeletesAnotherReimbursement() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);
			Candidate differentCandidate = createCandidate(999);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
			given(ReimbursementAccessValidatorTests.this.timesheetRepository.getCandidateLinkedToTimesheet(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(differentCandidate);

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateAccessControlForDeleteReimbursement(TIMESHEET_ID, ACCOUNT_ID))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage(ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_DELETE_OWN_ONLY);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when candidate is null for CONTRACTOR delete")
		void testThrowWhenCandidateIsNullForContractorDelete() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);
			given(ReimbursementAccessValidatorTests.this.timesheetRepository.getCandidateLinkedToTimesheet(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(null);

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateAccessControlForDeleteReimbursement(TIMESHEET_ID, ACCOUNT_ID))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage(ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_DELETE_OWN_ONLY);
		}

		@Test
		@DisplayName("Should throw ForbiddenAccessException for CONTACT principal")
		void testThrowForContactPrincipal() {
			Timesheet timesheet = createMockTimesheet();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal())
				.willReturn(createContactPrincipal());

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateAccessControlForDeleteReimbursement(TIMESHEET_ID, ACCOUNT_ID))
				.isInstanceOf(ForbiddenAccessException.class)
				.hasMessage(ExceptionMessageConstants.REIMBURSEMENT_CREATE_FORBIDDEN);
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when timesheet not found")
		void testThrowWhenTimesheetNotFound() {
			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.empty());

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateAccessControlForDeleteReimbursement(TIMESHEET_ID, ACCOUNT_ID))
				.isInstanceOf(ResourceNotFoundException.class);
		}

	}

	@Nested
	@DisplayName("validateAccessControlForGetStatusHistory Tests")
	class ValidateAccessControlForGetStatusHistoryTests {

		@Test
		@DisplayName("Should validate status history access for USER principal")
		void testValidateStatusHistoryAccessForUserPrincipal() {
			Timesheet timesheet = createMockTimesheet();
			UserPrincipal userPrincipal = createUserPrincipal();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForGetStatusHistory(TIMESHEET_ID,
					ACCOUNT_ID);

			ArgumentCaptor<PermissionCheckContext> ctxCaptor = ArgumentCaptor.forClass(PermissionCheckContext.class);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker)
				.allows(eq(Entity.TIMESHEET), ctxCaptor.capture(), any(AccessControlCheckMetadataContext.class));

			assertThat(ctxCaptor.getValue().getPermission()).isEqualTo(Permission.VIEW_TIMESHEET);
		}

		@Test
		@DisplayName("Should skip access control check for CONTRACTOR principal")
		void testSkipAccessControlForContractorPrincipal() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForGetStatusHistory(TIMESHEET_ID,
					ACCOUNT_ID);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker, never()).allows(any(),
					any(), any());
		}

		@Test
		@DisplayName("Should skip access control check for CONTACT principal")
		void testSkipAccessControlForContactPrincipal() {
			Timesheet timesheet = createMockTimesheet();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal())
				.willReturn(createContactPrincipal());

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForGetStatusHistory(TIMESHEET_ID,
					ACCOUNT_ID);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker, never()).allows(any(),
					any(), any());
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when timesheet not found")
		void testThrowWhenTimesheetNotFound() {
			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.empty());

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateAccessControlForGetStatusHistory(TIMESHEET_ID, ACCOUNT_ID))
				.isInstanceOf(ResourceNotFoundException.class);
		}

	}

	@Nested
	@DisplayName("validateAccessControlForUpdateStatus Tests")
	class ValidateAccessControlForUpdateStatusTests {

		@Test
		@DisplayName("Should validate update status access for USER principal")
		void testValidateUpdateStatusAccessForUserPrincipal() {
			Timesheet timesheet = createMockTimesheet();
			UserPrincipal userPrincipal = createUserPrincipal();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForUpdateStatus(TIMESHEET_ID,
					ACCOUNT_ID);

			ArgumentCaptor<PermissionCheckContext> ctxCaptor = ArgumentCaptor.forClass(PermissionCheckContext.class);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker)
				.allows(eq(Entity.TIMESHEET), ctxCaptor.capture(), any(AccessControlCheckMetadataContext.class));

			assertThat(ctxCaptor.getValue().getPermission()).isEqualTo(Permission.APPROVE_TIMESHEET);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException for CONTRACTOR principal")
		void testThrowForContractorPrincipal() {
			Timesheet timesheet = createMockTimesheet();
			ContractorPrincipal contractorPrincipal = createContractorPrincipal(CANDIDATE_ID);

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateAccessControlForUpdateStatus(TIMESHEET_ID, ACCOUNT_ID))
				.isInstanceOf(UnauthorizedAccessException.class)
				.hasMessage(ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_CANNOT_UPDATE_STATUS);
		}

		@Test
		@DisplayName("Should allow CONTACT principal without throwing exception")
		void testAllowContactPrincipal() {
			Timesheet timesheet = createMockTimesheet();

			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.of(timesheet));
			given(ReimbursementAccessValidatorTests.this.auth.getUnifiedPrincipal())
				.willReturn(createContactPrincipal());

			ReimbursementAccessValidatorTests.this.validator.validateAccessControlForUpdateStatus(TIMESHEET_ID,
					ACCOUNT_ID);

			verify(ReimbursementAccessValidatorTests.this.contractStaffingAccessControlChecker, never()).allows(any(),
					any(), any());
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when timesheet not found")
		void testThrowWhenTimesheetNotFound() {
			given(ReimbursementAccessValidatorTests.this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID,
					ACCOUNT_ID))
				.willReturn(Optional.empty());

			assertThatThrownBy(() -> ReimbursementAccessValidatorTests.this.validator
				.validateAccessControlForUpdateStatus(TIMESHEET_ID, ACCOUNT_ID))
				.isInstanceOf(ResourceNotFoundException.class);
		}

	}

}
