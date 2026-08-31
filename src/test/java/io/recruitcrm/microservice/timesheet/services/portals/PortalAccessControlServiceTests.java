/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.portals;

import io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.entity.model.Contact;
import io.recruitcrm.entity.model.Job;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.dao.contact.ContactJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.job.JobJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.job_secondary_contact.JobSecondaryContactJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.job_timesheet_access.JobTimesheetAccessJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approver.TimesheetApproverRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PortalAccessControlServiceTests {

	@Mock
	private JobJpaRepository jobJpaRepository;

	@Mock
	private JobTimesheetAccessJpaRepository jobTimesheetAccessJpaRepository;

	@Mock
	private JobSecondaryContactJpaRepository jobSecondaryContactJpaRepository;

	@Mock
	private TimesheetJpaRepository timesheetJpaRepository;

	@Mock
	private TimesheetApproverRepository timesheetApproverRepository;

	@Mock
	private ContactJpaRepository contactJpaRepository;

	@Mock
	private AuthHolder auth;

	@Mock
	private AuthPrincipal authPrincipal;

	@InjectMocks
	private PortalAccessControlService portalAccessControlService;

	private static final Integer JOB_ID = 1;

	private static final Integer CLIENT_ID = 100;

	private static final Integer TIMESHEET_ID = 1;

	private static final Integer ACCOUNT_ID = 123;

	private static final Integer USER_ID = 456;

	private static final Integer USER_TYPE_ID = 3;

	private static final Integer TIMESHEET_SETTING_ID = 10;

	private static final String EMAIL = "client@example.com";

	private static final String VALID_JOB_TYPE = "contract";

	/**
	 * Builds a valid contract Job whose primary contactId matches the given clientId.
	 * @param contactId the primary contact id to set on the job
	 * @return a Job with a valid (non-fulltime/parttime/empty) job type
	 */
	private Job validJob(Integer contactId) {
		Job job = new Job();
		job.setId(JOB_ID);
		job.setJobType(VALID_JOB_TYPE);
		job.setContactId(contactId);
		return job;
	}

	private Contact contact(Integer id, Integer accountId) {
		Contact contact = new Contact();
		contact.setId(id);
		contact.setAccountId(accountId);
		return contact;
	}

	/**
	 * Stubs the email-resolution path: auth principal email lookup and the email-based
	 * contact id resolution returning a contact whose id matches the given clientId.
	 */
	private void givenEmailResolvesToContactIds(List<Contact> resolvedContacts) {
		given(this.auth.getUnifiedPrincipal()).willReturn(this.authPrincipal);
		given(this.authPrincipal.getEmail()).willReturn(EMAIL);
		given(this.contactJpaRepository.findAllByEmailAndAccountId(EMAIL, ACCOUNT_ID)).willReturn(resolvedContacts);
	}

	// ==================== validatePortalAccessControl Tests ====================

	@Test
	@DisplayName("validatePortalAccessControl should return permissions when client is the primary contact")
	void testValidatePortalAccessControlPrimaryContactReturnsPermissions() {
		// Given
		Job job = this.validJob(CLIENT_ID);
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanCreate(1);
		access.setCanEdit(1);
		access.setCanDelete(0);

		given(this.jobJpaRepository.findById(JOB_ID)).willReturn(Optional.of(job));
		given(this.contactJpaRepository.findById(CLIENT_ID))
			.willReturn(Optional.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		this.givenEmailResolvesToContactIds(List.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		given(this.jobTimesheetAccessJpaRepository.findByJobId(JOB_ID)).willReturn(Optional.of(access));

		// When
		PortalTimesheetPermissionDto result = this.portalAccessControlService.validatePortalAccessControl(JOB_ID,
				CLIENT_ID);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getCanCreate()).isEqualTo(Integer.valueOf(1));
		assertThat(result.getCanEdit()).isEqualTo(Integer.valueOf(1));
		assertThat(result.getCanDelete()).isEqualTo(Integer.valueOf(0));
		then(this.jobJpaRepository).should().findById(JOB_ID);
		then(this.jobTimesheetAccessJpaRepository).should().findByJobId(JOB_ID);
	}

	@Test
	@DisplayName("validatePortalAccessControl should return permissions when client is a secondary contact")
	void testValidatePortalAccessControlSecondaryContactReturnsPermissions() {
		// Given
		Integer primaryContactId = 999;
		Job job = this.validJob(primaryContactId);
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanCreate(1);
		access.setCanEdit(0);
		access.setCanDelete(1);

		given(this.jobJpaRepository.findById(JOB_ID)).willReturn(Optional.of(job));
		given(this.contactJpaRepository.findById(CLIENT_ID))
			.willReturn(Optional.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		this.givenEmailResolvesToContactIds(List.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		given(this.jobSecondaryContactJpaRepository.existsByJobIdAndContactIdIn(JOB_ID, List.of(CLIENT_ID)))
			.willReturn(true);
		given(this.jobTimesheetAccessJpaRepository.findByJobId(JOB_ID)).willReturn(Optional.of(access));

		// When
		PortalTimesheetPermissionDto result = this.portalAccessControlService.validatePortalAccessControl(JOB_ID,
				CLIENT_ID);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getCanCreate()).isEqualTo(Integer.valueOf(1));
		assertThat(result.getCanEdit()).isEqualTo(Integer.valueOf(0));
		assertThat(result.getCanDelete()).isEqualTo(Integer.valueOf(1));
		then(this.jobSecondaryContactJpaRepository).should().existsByJobIdAndContactIdIn(JOB_ID, List.of(CLIENT_ID));
	}

	@Test
	@DisplayName("validatePortalAccessControl should throw ResourceNotFoundException when job not found")
	void testValidatePortalAccessControlJobNotFoundThrowsResourceNotFoundException() {
		// Given
		given(this.jobJpaRepository.findById(JOB_ID)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.portalAccessControlService.validatePortalAccessControl(JOB_ID, CLIENT_ID))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Job");

		then(this.jobJpaRepository).should().findById(JOB_ID);
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "fulltime", "parttime" })
	@DisplayName("validatePortalAccessControl should throw UnauthorizedAccessException for non-contract job types")
	void testValidatePortalAccessControlInvalidJobTypeThrowsUnauthorizedAccessException(String jobType) {
		// Given
		Job job = new Job();
		job.setId(JOB_ID);
		job.setJobType(jobType);
		job.setContactId(CLIENT_ID);

		given(this.jobJpaRepository.findById(JOB_ID)).willReturn(Optional.of(job));

		// When & Then
		assertThatThrownBy(() -> this.portalAccessControlService.validatePortalAccessControl(JOB_ID, CLIENT_ID))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Unauthorized access: Client ID " + CLIENT_ID);

		then(this.jobJpaRepository).should().findById(JOB_ID);
	}

	@Test
	@DisplayName("validatePortalAccessControl should succeed when job type is a valid contract type")
	void testValidatePortalAccessControlValidJobTypeSucceeds() {
		// Given
		Job job = this.validJob(CLIENT_ID);
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanCreate(1);
		access.setCanEdit(1);
		access.setCanDelete(1);

		given(this.jobJpaRepository.findById(JOB_ID)).willReturn(Optional.of(job));
		given(this.contactJpaRepository.findById(CLIENT_ID))
			.willReturn(Optional.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		this.givenEmailResolvesToContactIds(List.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		given(this.jobTimesheetAccessJpaRepository.findByJobId(JOB_ID)).willReturn(Optional.of(access));

		// When
		PortalTimesheetPermissionDto result = this.portalAccessControlService.validatePortalAccessControl(JOB_ID,
				CLIENT_ID);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getCanCreate()).isEqualTo(Integer.valueOf(1));
	}

	@Test
	@DisplayName("validatePortalAccessControl should throw UnauthorizedAccessException when client contact not found")
	void testValidatePortalAccessControlContactNotFoundThrowsUnauthorizedAccessException() {
		// Given
		Job job = this.validJob(CLIENT_ID);
		given(this.jobJpaRepository.findById(JOB_ID)).willReturn(Optional.of(job));
		given(this.contactJpaRepository.findById(CLIENT_ID)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.portalAccessControlService.validatePortalAccessControl(JOB_ID, CLIENT_ID))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Unauthorized access: Client ID " + CLIENT_ID);

		then(this.contactJpaRepository).should().findById(CLIENT_ID);
	}

	@Test
	@DisplayName("validatePortalAccessControl should throw UnauthorizedAccessException when client is neither primary nor secondary")
	void testValidatePortalAccessControlClientNotAuthorizedThrowsUnauthorizedAccessException() {
		// Given
		Integer differentContactId = 999;
		Job job = this.validJob(differentContactId);

		given(this.jobJpaRepository.findById(JOB_ID)).willReturn(Optional.of(job));
		given(this.contactJpaRepository.findById(CLIENT_ID))
			.willReturn(Optional.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		this.givenEmailResolvesToContactIds(List.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		given(this.jobSecondaryContactJpaRepository.existsByJobIdAndContactIdIn(JOB_ID, List.of(CLIENT_ID)))
			.willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.portalAccessControlService.validatePortalAccessControl(JOB_ID, CLIENT_ID))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Unauthorized access: Client ID " + CLIENT_ID);
	}

	@Test
	@DisplayName("validatePortalAccessControl should throw UnauthorizedAccessException when job primary contactId is null and not secondary")
	void testValidatePortalAccessControlNullPrimaryContactIdNotSecondaryThrowsUnauthorizedAccessException() {
		// Given
		Job job = this.validJob(null);

		given(this.jobJpaRepository.findById(JOB_ID)).willReturn(Optional.of(job));
		given(this.contactJpaRepository.findById(CLIENT_ID))
			.willReturn(Optional.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		this.givenEmailResolvesToContactIds(List.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		given(this.jobSecondaryContactJpaRepository.existsByJobIdAndContactIdIn(JOB_ID, List.of(CLIENT_ID)))
			.willReturn(false);

		// When & Then
		assertThatThrownBy(() -> this.portalAccessControlService.validatePortalAccessControl(JOB_ID, CLIENT_ID))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Unauthorized access: Client ID " + CLIENT_ID);
	}

	@Test
	@DisplayName("validatePortalAccessControl should throw UnauthorizedAccessException when access record not found")
	void testValidatePortalAccessControlAccessRecordNotFoundThrowsUnauthorizedAccessException() {
		// Given
		Job job = this.validJob(CLIENT_ID);

		given(this.jobJpaRepository.findById(JOB_ID)).willReturn(Optional.of(job));
		given(this.contactJpaRepository.findById(CLIENT_ID))
			.willReturn(Optional.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		this.givenEmailResolvesToContactIds(List.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		given(this.jobTimesheetAccessJpaRepository.findByJobId(JOB_ID)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.portalAccessControlService.validatePortalAccessControl(JOB_ID, CLIENT_ID))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Unauthorized access for delete timesheet");

		then(this.jobTimesheetAccessJpaRepository).should().findByJobId(JOB_ID);
	}

	@Test
	@DisplayName("validatePortalAccessControl should return permissions with all null values when access has all nulls")
	void testValidatePortalAccessControlAllNullPermissionsReturnsNullPermissions() {
		// Given
		Job job = this.validJob(CLIENT_ID);
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanCreate(null);
		access.setCanEdit(null);
		access.setCanDelete(null);

		given(this.jobJpaRepository.findById(JOB_ID)).willReturn(Optional.of(job));
		given(this.contactJpaRepository.findById(CLIENT_ID))
			.willReturn(Optional.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		this.givenEmailResolvesToContactIds(List.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		given(this.jobTimesheetAccessJpaRepository.findByJobId(JOB_ID)).willReturn(Optional.of(access));

		// When
		PortalTimesheetPermissionDto result = this.portalAccessControlService.validatePortalAccessControl(JOB_ID,
				CLIENT_ID);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getCanCreate()).isNull();
		assertThat(result.getCanEdit()).isNull();
		assertThat(result.getCanDelete()).isNull();
	}

	// ==================== hasPermission Tests ====================

	private void givenAccessGranted(JobTimesheetAccess access) {
		Job job = this.validJob(CLIENT_ID);
		given(this.jobJpaRepository.findById(JOB_ID)).willReturn(Optional.of(job));
		given(this.contactJpaRepository.findById(CLIENT_ID))
			.willReturn(Optional.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		this.givenEmailResolvesToContactIds(List.of(this.contact(CLIENT_ID, ACCOUNT_ID)));
		given(this.jobTimesheetAccessJpaRepository.findByJobId(JOB_ID)).willReturn(Optional.of(access));
	}

	@Test
	@DisplayName("hasPermission should return true when CREATE_TIMESHEET permission is 1")
	void testHasPermissionCreateTimesheetAllowedReturnsTrue() {
		// Given
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanCreate(1);
		this.givenAccessGranted(access);

		// When
		boolean result = this.portalAccessControlService.hasPermission(JOB_ID, CLIENT_ID, "CREATE_TIMESHEET");

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("hasPermission should return false when CREATE_TIMESHEET permission is 0")
	void testHasPermissionCreateTimesheetNotAllowedReturnsFalse() {
		// Given
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanCreate(0);
		this.givenAccessGranted(access);

		// When
		boolean result = this.portalAccessControlService.hasPermission(JOB_ID, CLIENT_ID, "CREATE_TIMESHEET");

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("hasPermission should return false when CREATE_TIMESHEET permission is null")
	void testHasPermissionCreateTimesheetNullReturnsFalse() {
		// Given
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanCreate(null);
		this.givenAccessGranted(access);

		// When
		boolean result = this.portalAccessControlService.hasPermission(JOB_ID, CLIENT_ID, "CREATE_TIMESHEET");

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("hasPermission should return true when EDIT_TIMESHEET permission is 1")
	void testHasPermissionEditTimesheetAllowedReturnsTrue() {
		// Given
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanEdit(1);
		this.givenAccessGranted(access);

		// When
		boolean result = this.portalAccessControlService.hasPermission(JOB_ID, CLIENT_ID, "EDIT_TIMESHEET");

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("hasPermission should return false when EDIT_TIMESHEET permission is 0")
	void testHasPermissionEditTimesheetNotAllowedReturnsFalse() {
		// Given
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanEdit(0);
		this.givenAccessGranted(access);

		// When
		boolean result = this.portalAccessControlService.hasPermission(JOB_ID, CLIENT_ID, "EDIT_TIMESHEET");

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("hasPermission should return false when EDIT_TIMESHEET permission is null")
	void testHasPermissionEditTimesheetNullReturnsFalse() {
		// Given
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanEdit(null);
		this.givenAccessGranted(access);

		// When
		boolean result = this.portalAccessControlService.hasPermission(JOB_ID, CLIENT_ID, "EDIT_TIMESHEET");

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("hasPermission should return true when DELETE_TIMESHEET permission is 1")
	void testHasPermissionDeleteTimesheetAllowedReturnsTrue() {
		// Given
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanDelete(1);
		this.givenAccessGranted(access);

		// When
		boolean result = this.portalAccessControlService.hasPermission(JOB_ID, CLIENT_ID, "DELETE_TIMESHEET");

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("hasPermission should return false when DELETE_TIMESHEET permission is 0")
	void testHasPermissionDeleteTimesheetNotAllowedReturnsFalse() {
		// Given
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanDelete(0);
		this.givenAccessGranted(access);

		// When
		boolean result = this.portalAccessControlService.hasPermission(JOB_ID, CLIENT_ID, "DELETE_TIMESHEET");

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("hasPermission should return false when DELETE_TIMESHEET permission is null")
	void testHasPermissionDeleteTimesheetNullReturnsFalse() {
		// Given
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanDelete(null);
		this.givenAccessGranted(access);

		// When
		boolean result = this.portalAccessControlService.hasPermission(JOB_ID, CLIENT_ID, "DELETE_TIMESHEET");

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("hasPermission should return false for an unknown permission type")
	void testHasPermissionUnknownPermissionTypeReturnsFalse() {
		// Given
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanCreate(1);
		access.setCanEdit(1);
		access.setCanDelete(1);
		this.givenAccessGranted(access);

		// When
		boolean result = this.portalAccessControlService.hasPermission(JOB_ID, CLIENT_ID, "UNKNOWN_PERMISSION");

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("hasPermission should propagate ResourceNotFoundException when job not found")
	void testHasPermissionJobNotFoundPropagatesResourceNotFoundException() {
		// Given
		given(this.jobJpaRepository.findById(JOB_ID)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.portalAccessControlService.hasPermission(JOB_ID, CLIENT_ID, "CREATE_TIMESHEET"))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Job");
	}

	@Test
	@DisplayName("hasPermission should return false when permission value is greater than 1")
	void testHasPermissionValueGreaterThanOneReturnsFalse() {
		// Given
		JobTimesheetAccess access = new JobTimesheetAccess();
		access.setCanCreate(2);
		this.givenAccessGranted(access);

		// When
		boolean result = this.portalAccessControlService.hasPermission(JOB_ID, CLIENT_ID, "CREATE_TIMESHEET");

		// Then
		assertThat(result).isFalse();
	}

	// ==================== validateApproverAccess Tests ====================

	@Test
	@DisplayName("validateApproverAccess should succeed for non-company-contact when userId matches an approver")
	void testValidateApproverAccessNonCompanyContactUserIsApproverSucceeds() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setTimesheetSettingId(TIMESHEET_SETTING_ID);

		TimesheetApprover approver = new TimesheetApprover();
		approver.setEntityId(USER_ID);
		approver.setUserTypeId(USER_TYPE_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID))
			.willReturn(List.of(approver));

		// When
		this.portalAccessControlService.validateApproverAccess(TIMESHEET_ID, USER_ID, USER_TYPE_ID, ACCOUNT_ID);

		// Then
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(TIMESHEET_SETTING_ID);
		then(this.contactJpaRepository).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("validateApproverAccess should resolve email-based contact ids for company contact persona")
	void testValidateApproverAccessCompanyContactResolvesEmailContactIdsSucceeds() {
		// Given
		Integer companyContactTypeId = UserTypeEnum.COMPANY_CONTACT.getId();
		Integer registeredApproverContactId = 789;

		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setTimesheetSettingId(TIMESHEET_SETTING_ID);

		TimesheetApprover approver = new TimesheetApprover();
		approver.setEntityId(registeredApproverContactId);
		approver.setUserTypeId(companyContactTypeId);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID))
			.willReturn(List.of(approver));
		this.givenEmailResolvesToContactIds(List.of(this.contact(registeredApproverContactId, ACCOUNT_ID)));

		// When
		this.portalAccessControlService.validateApproverAccess(TIMESHEET_ID, USER_ID, companyContactTypeId, ACCOUNT_ID);

		// Then
		then(this.contactJpaRepository).should().findAllByEmailAndAccountId(EMAIL, ACCOUNT_ID);
	}

	@Test
	@DisplayName("validateApproverAccess should throw ValidationErrorException when company contact is not a registered approver")
	void testValidateApproverAccessCompanyContactNotApproverThrowsValidationErrorException() {
		// Given
		Integer companyContactTypeId = UserTypeEnum.COMPANY_CONTACT.getId();

		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setTimesheetSettingId(TIMESHEET_SETTING_ID);

		TimesheetApprover approver = new TimesheetApprover();
		approver.setEntityId(999);
		approver.setUserTypeId(companyContactTypeId);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID))
			.willReturn(List.of(approver));
		this.givenEmailResolvesToContactIds(List.of(this.contact(CLIENT_ID, ACCOUNT_ID)));

		// When & Then
		assertThatThrownBy(() -> this.portalAccessControlService.validateApproverAccess(TIMESHEET_ID, USER_ID,
				companyContactTypeId, ACCOUNT_ID))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("User is not authorized to approve this timesheet");
	}

	@Test
	@DisplayName("validateApproverAccess should throw ResourceNotFoundException when timesheet not found")
	void testValidateApproverAccessTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.portalAccessControlService.validateApproverAccess(TIMESHEET_ID, USER_ID,
				USER_TYPE_ID, ACCOUNT_ID))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");
	}

	@Test
	@DisplayName("validateApproverAccess should throw ValidationErrorException when non-company-contact userId is not an approver")
	void testValidateApproverAccessNonCompanyContactNotApproverThrowsValidationErrorException() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setTimesheetSettingId(TIMESHEET_SETTING_ID);

		TimesheetApprover approver = new TimesheetApprover();
		approver.setEntityId(200);
		approver.setUserTypeId(USER_TYPE_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID))
			.willReturn(List.of(approver));

		// When & Then
		assertThatThrownBy(() -> this.portalAccessControlService.validateApproverAccess(TIMESHEET_ID, USER_ID,
				USER_TYPE_ID, ACCOUNT_ID))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("User is not authorized to approve this timesheet");
	}

	@Test
	@DisplayName("validateApproverAccess should throw ValidationErrorException when entity matches but user type differs")
	void testValidateApproverAccessUserTypeMismatchThrowsValidationErrorException() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setTimesheetSettingId(TIMESHEET_SETTING_ID);

		TimesheetApprover approver = new TimesheetApprover();
		approver.setEntityId(USER_ID);
		approver.setUserTypeId(USER_TYPE_ID + 1);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID))
			.willReturn(List.of(approver));

		// When & Then
		assertThatThrownBy(() -> this.portalAccessControlService.validateApproverAccess(TIMESHEET_ID, USER_ID,
				USER_TYPE_ID, ACCOUNT_ID))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("User is not authorized to approve this timesheet");
	}

	@Test
	@DisplayName("validateApproverAccess should throw ValidationErrorException when approver list is empty")
	void testValidateApproverAccessEmptyApproverListThrowsValidationErrorException() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setTimesheetSettingId(TIMESHEET_SETTING_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(TIMESHEET_SETTING_ID))
			.willReturn(Collections.emptyList());

		// When & Then
		assertThatThrownBy(() -> this.portalAccessControlService.validateApproverAccess(TIMESHEET_ID, USER_ID,
				USER_TYPE_ID, ACCOUNT_ID))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("User is not authorized to approve this timesheet");
	}

	// ==================== resolveContactIds Tests ====================

	@Test
	@DisplayName("resolveContactIds should map resolved contacts to their ids")
	void testResolveContactIdsReturnsContactIds() {
		// Given
		given(this.auth.getUnifiedPrincipal()).willReturn(this.authPrincipal);
		given(this.authPrincipal.getEmail()).willReturn(EMAIL);
		given(this.contactJpaRepository.findAllByEmailAndAccountId(EMAIL, ACCOUNT_ID))
			.willReturn(List.of(this.contact(100, ACCOUNT_ID), this.contact(200, ACCOUNT_ID)));

		// When
		List<Integer> result = this.portalAccessControlService.resolveContactIds(ACCOUNT_ID);

		// Then
		assertThat(result).containsExactly(100, 200);
		then(this.contactJpaRepository).should().findAllByEmailAndAccountId(EMAIL, ACCOUNT_ID);
	}

	@Test
	@DisplayName("resolveContactIds should return empty list when no contacts share the email")
	void testResolveContactIdsReturnsEmptyWhenNoContacts() {
		// Given
		given(this.auth.getUnifiedPrincipal()).willReturn(this.authPrincipal);
		given(this.authPrincipal.getEmail()).willReturn(EMAIL);
		given(this.contactJpaRepository.findAllByEmailAndAccountId(EMAIL, ACCOUNT_ID))
			.willReturn(Collections.emptyList());

		// When
		List<Integer> result = this.portalAccessControlService.resolveContactIds(ACCOUNT_ID);

		// Then
		assertThat(result).isEmpty();
	}

}
