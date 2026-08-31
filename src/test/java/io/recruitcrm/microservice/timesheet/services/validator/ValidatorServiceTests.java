package io.recruitcrm.microservice.timesheet.services.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.dao.timesheet_approval.TimesheetApprovalJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.FetchBulkTimelogValidatedResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingTemplateWorkDayDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.mapper.ValidatorMapper;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_setting.ITimesheetSettingRepository;
import io.recruitcrm.microservice.timesheet.repositories.validator.ValidatorRepository;
import io.recruitcrm.microservice.timesheet.testdata.ValidatorTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ValidatorServiceTests {

	@InjectMocks
	private ValidatorService validatorService;

	@Mock
	private ValidatorRepository validatorRepository;

	@Mock
	private ITimesheetSettingRepository timesheetSettingRepository;

	@Mock
	private ValidatorMapper validatorMapper;

	@Mock
	private TimesheetApprovalJpaRepository timesheetApprovalJpaRepository;

	@Mock
	private AccessControlChecker contractStaffingAccessControlChecker;

	private static final Integer DEFAULT_TIMESHEET_ID = ValidatorTestDataFactory.getDefaultTimesheetId();

	private static final Integer DELETED_CANDIDATE_TIMESHEET_ID = ValidatorTestDataFactory
		.getDeletedCandidateTimesheetId();

	private static final Integer DEFAULT_TIMESHEET_SETTING_ID = ValidatorTestDataFactory.getDefaultTimesheetSettingId();

	@BeforeEach
	void setUp() {
		// Common setup if needed
	}

	@Test
	@DisplayName("Validate time logs before update should bypass access control for deleted candidate")
	void testValidateTimeLogsBeforeUpdateDeletedCandidateBypassesAccessControl() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(DELETED_CANDIDATE_TIMESHEET_ID);
		TimesheetAndSettingValidatorQueryResultDto deletedCandidateResult = ValidatorTestDataFactory
			.createValidatorQueryResultWithDeletedCandidate();
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = Arrays.asList(deletedCandidateResult);
		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = ValidatorTestDataFactory
			.createValidatorResponseBodyDtoList();
		List<TimesheetSettingTemplateWorkDayDto> workDayData = Collections.emptyList();

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DELETED_CANDIDATE_TIMESHEET_ID))
			.willReturn(null);
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(anyList())).willReturn(validatorResults);
		given(this.timesheetSettingRepository.findTimesheetSettingsWithTemplateWorkDayByIds(anyList()))
			.willReturn(workDayData);
		given(this.validatorMapper.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults))
			.willReturn(responseDtos);

		// When
		FetchBulkTimelogValidatedResponseBodyDto result = this.validatorService
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos()).isEmpty();
		assertThat(result.getTimesheetAndSettingValidatorResponseBodyDtos()).hasSize(1);
		assertThat(result.getPrimaryTimesheetSettingId()).isEqualTo(DEFAULT_TIMESHEET_SETTING_ID);

		// Verify access control was never called for deleted candidate
		then(this.contractStaffingAccessControlChecker).should(never())
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));
		then(this.validatorRepository).should(times(2)).validateTimeLogsBeforeUpdate(anyList());
	}

	@Test
	@DisplayName("Validate time logs before update should check access control for active candidate")
	void testValidateTimeLogsBeforeUpdateActiveCandidateChecksAccessControl() {
		// Given
		List<Integer> timesheetIds = ValidatorTestDataFactory.createSingleTimesheetIdList();
		TimesheetAndSettingValidatorQueryResultDto activeCandidateResult = ValidatorTestDataFactory
			.createValidatorQueryResultWithActiveCandidate();
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = Arrays.asList(activeCandidateResult);
		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = ValidatorTestDataFactory
			.createValidatorResponseBodyDtoList();
		List<TimesheetSettingTemplateWorkDayDto> workDayData = Collections.emptyList();

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DEFAULT_TIMESHEET_ID))
			.willReturn(null);
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(anyList())).willReturn(validatorResults);
		given(this.timesheetSettingRepository.findTimesheetSettingsWithTemplateWorkDayByIds(anyList()))
			.willReturn(workDayData);
		given(this.validatorMapper.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults))
			.willReturn(responseDtos);

		// Access control passes for active candidate (void method - no stubbing needed,
		// won't throw exception)
		willDoNothing().given(this.contractStaffingAccessControlChecker)
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));

		// When
		FetchBulkTimelogValidatedResponseBodyDto result = this.validatorService
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos()).isEmpty();
		assertThat(result.getTimesheetAndSettingValidatorResponseBodyDtos()).hasSize(1);

		// Verify access control was called for active candidate
		then(this.contractStaffingAccessControlChecker).should(times(1))
			.allows(eq(Entity.TIMESHEET), any(PermissionCheckContext.class),
					any(AccessControlCheckMetadataContext.class));
	}

	@Test
	@DisplayName("Validate time logs before update should add error for active candidate without access")
	void testValidateTimeLogsBeforeUpdateActiveCandidateWithoutAccessAddsError() {
		// Given
		List<Integer> timesheetIds = ValidatorTestDataFactory.createSingleTimesheetIdList();
		TimesheetAndSettingValidatorQueryResultDto activeCandidateResult = ValidatorTestDataFactory
			.createValidatorQueryResultWithActiveCandidate();
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = Arrays.asList(activeCandidateResult);
		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = ValidatorTestDataFactory
			.createValidatorResponseBodyDtoList();
		List<TimesheetSettingTemplateWorkDayDto> workDayData = Collections.emptyList();

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DEFAULT_TIMESHEET_ID))
			.willReturn(null);
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(anyList())).willReturn(validatorResults);
		given(this.timesheetSettingRepository.findTimesheetSettingsWithTemplateWorkDayByIds(anyList()))
			.willReturn(workDayData);
		given(this.validatorMapper.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults))
			.willReturn(responseDtos);

		// Access control fails for active candidate
		willThrow(new RuntimeException("Access denied")).given(this.contractStaffingAccessControlChecker)
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));

		// When
		FetchBulkTimelogValidatedResponseBodyDto result = this.validatorService
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos()).hasSize(1);
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos().get(0).getError()).isEqualTo("no_edit_access");
		assertThat(result.getTimesheetAndSettingValidatorResponseBodyDtos()).hasSize(1);

		// Verify access control was called for active candidate
		then(this.contractStaffingAccessControlChecker).should(times(1))
			.allows(eq(Entity.TIMESHEET), any(PermissionCheckContext.class),
					any(AccessControlCheckMetadataContext.class));
	}

	@Test
	@DisplayName("Validate time logs before update should handle mixed active and deleted candidates")
	void testValidateTimeLogsBeforeUpdateMixedCandidatesHandlesCorrectly() {
		// Given
		List<Integer> timesheetIds = ValidatorTestDataFactory.createTimesheetIdList();
		List<TimesheetAndSettingValidatorQueryResultDto> mixedResults = ValidatorTestDataFactory
			.createMixedValidatorQueryResultList();
		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = Arrays.asList(
				new TimesheetAndSettingValidatorResponseBodyDto(), new TimesheetAndSettingValidatorResponseBodyDto());
		List<TimesheetSettingTemplateWorkDayDto> workDayData = Collections.emptyList();

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DEFAULT_TIMESHEET_ID))
			.willReturn(null);
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(anyList())).willReturn(mixedResults);
		given(this.timesheetSettingRepository.findTimesheetSettingsWithTemplateWorkDayByIds(anyList()))
			.willReturn(workDayData);
		given(this.validatorMapper.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(mixedResults))
			.willReturn(responseDtos);

		// Access control passes for active candidate
		willDoNothing().given(this.contractStaffingAccessControlChecker)
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));

		// When
		FetchBulkTimelogValidatedResponseBodyDto result = this.validatorService
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos()).isEmpty();
		assertThat(result.getTimesheetAndSettingValidatorResponseBodyDtos()).hasSize(2);

		// Verify access control was called only once (for active candidate, not for
		// deleted)
		then(this.contractStaffingAccessControlChecker).should(times(1))
			.allows(eq(Entity.TIMESHEET), any(PermissionCheckContext.class),
					any(AccessControlCheckMetadataContext.class));
	}

	@Test
	@DisplayName("Validate time logs before update should throw exception when first timesheet is approved")
	void testValidateTimeLogsBeforeUpdateApprovedFirstTimesheetThrowsException() {
		// Given
		List<Integer> timesheetIds = ValidatorTestDataFactory.createSingleTimesheetIdList();
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DEFAULT_TIMESHEET_ID))
			.willReturn(ValidatorTestDataFactory.createApprovedTimesheetApproval());

		// When & Then
		assertThatThrownBy(() -> this.validatorService.validateTimeLogsBeforeUpdate(timesheetIds))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining(ValidatorTestDataFactory.Messages.FIRST_TIMESHEET_APPROVED);

		// Verify validation stopped early and didn't proceed further
		then(this.validatorRepository).should(never()).validateTimeLogsBeforeUpdate(anyList());
		then(this.contractStaffingAccessControlChecker).should(never())
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));
	}

	@Test
	@DisplayName("Validate time logs before update should process when first timesheet is not approved")
	void testValidateTimeLogsBeforeUpdateNotApprovedFirstTimesheetProcessesSuccessfully() {
		// Given
		List<Integer> timesheetIds = ValidatorTestDataFactory.createSingleTimesheetIdList();
		TimesheetAndSettingValidatorQueryResultDto activeCandidateResult = ValidatorTestDataFactory
			.createValidatorQueryResultWithActiveCandidate();
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = Arrays.asList(activeCandidateResult);
		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = ValidatorTestDataFactory
			.createValidatorResponseBodyDtoList();
		List<TimesheetSettingTemplateWorkDayDto> workDayData = Collections.emptyList();

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DEFAULT_TIMESHEET_ID))
			.willReturn(ValidatorTestDataFactory.createPendingTimesheetApproval());
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(anyList())).willReturn(validatorResults);
		given(this.timesheetSettingRepository.findTimesheetSettingsWithTemplateWorkDayByIds(anyList()))
			.willReturn(workDayData);
		given(this.validatorMapper.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults))
			.willReturn(responseDtos);

		// Access control passes
		willDoNothing().given(this.contractStaffingAccessControlChecker)
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));

		// When
		FetchBulkTimelogValidatedResponseBodyDto result = this.validatorService
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos()).isEmpty();
		assertThat(result.getTimesheetAndSettingValidatorResponseBodyDtos()).hasSize(1);

		// Verify processing continued
		then(this.validatorRepository).should(times(2)).validateTimeLogsBeforeUpdate(anyList());
		then(this.contractStaffingAccessControlChecker).should(times(1))
			.allows(eq(Entity.TIMESHEET), any(PermissionCheckContext.class),
					any(AccessControlCheckMetadataContext.class));
	}

	@Test
	@DisplayName("Validate time logs before update should handle multiple deleted candidates")
	void testValidateTimeLogsBeforeUpdateMultipleDeletedCandidatesHandlesCorrectly() {
		// Given
		List<Integer> timesheetIds = Arrays.asList(DELETED_CANDIDATE_TIMESHEET_ID, DELETED_CANDIDATE_TIMESHEET_ID + 1);
		TimesheetAndSettingValidatorQueryResultDto deletedCandidate1 = ValidatorTestDataFactory
			.createValidatorQueryResultWithDeletedCandidate();
		TimesheetAndSettingValidatorQueryResultDto deletedCandidate2 = ValidatorTestDataFactory
			.createValidatorQueryResultWithDeletedCandidate();
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = Arrays.asList(deletedCandidate1,
				deletedCandidate2);
		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = Arrays.asList(
				new TimesheetAndSettingValidatorResponseBodyDto(), new TimesheetAndSettingValidatorResponseBodyDto());
		List<TimesheetSettingTemplateWorkDayDto> workDayData = Collections.emptyList();

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DELETED_CANDIDATE_TIMESHEET_ID))
			.willReturn(null);
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(anyList())).willReturn(validatorResults);
		given(this.timesheetSettingRepository.findTimesheetSettingsWithTemplateWorkDayByIds(anyList()))
			.willReturn(workDayData);
		given(this.validatorMapper.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults))
			.willReturn(responseDtos);

		// When
		FetchBulkTimelogValidatedResponseBodyDto result = this.validatorService
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos()).isEmpty();
		assertThat(result.getTimesheetAndSettingValidatorResponseBodyDtos()).hasSize(2);

		// Verify access control was never called for any deleted candidates
		then(this.contractStaffingAccessControlChecker).should(never())
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));
	}

	@Test
	@DisplayName("Validate time logs before update should add approved error for non primary approved timesheet")
	void testValidateTimeLogsBeforeUpdateNonPrimaryApprovedAddsApprovedError() {
		// Given - primary timesheet is the active one, secondary is approved
		List<Integer> timesheetIds = Arrays.asList(DEFAULT_TIMESHEET_ID,
				ValidatorTestDataFactory.getSecondaryTimesheetId());
		TimesheetAndSettingValidatorQueryResultDto primary = ValidatorTestDataFactory.createPrimaryActiveQueryResult();
		TimesheetAndSettingValidatorQueryResultDto secondaryApproved = ValidatorTestDataFactory
			.createNonPrimaryApprovedQueryResult();
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = Arrays.asList(primary, secondaryApproved);
		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = Arrays.asList(
				new TimesheetAndSettingValidatorResponseBodyDto(), new TimesheetAndSettingValidatorResponseBodyDto());

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DEFAULT_TIMESHEET_ID))
			.willReturn(null);
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(List.of(DEFAULT_TIMESHEET_ID)))
			.willReturn(Arrays.asList(primary));
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(timesheetIds)).willReturn(validatorResults);
		given(this.timesheetSettingRepository.findTimesheetSettingsWithTemplateWorkDayByIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.validatorMapper.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults))
			.willReturn(responseDtos);
		willDoNothing().given(this.contractStaffingAccessControlChecker)
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));

		// When
		FetchBulkTimelogValidatedResponseBodyDto result = this.validatorService
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos()).hasSize(1);
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos().get(0).getError()).isEqualTo("approved");
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos().get(0).getTimesheetId())
			.isEqualTo(ValidatorTestDataFactory.getSecondaryTimesheetId());
	}

	@Test
	@DisplayName("Validate time logs before update should add different period error for non primary timesheet")
	void testValidateTimeLogsBeforeUpdateNonPrimaryDifferentPeriodAddsPeriodError() {
		// Given - secondary timesheet has a different period than the primary
		List<Integer> timesheetIds = Arrays.asList(DEFAULT_TIMESHEET_ID,
				ValidatorTestDataFactory.getSecondaryTimesheetId());
		TimesheetAndSettingValidatorQueryResultDto primary = ValidatorTestDataFactory.createPrimaryActiveQueryResult();
		TimesheetAndSettingValidatorQueryResultDto secondaryDifferentPeriod = ValidatorTestDataFactory
			.createNonPrimaryDifferentPeriodQueryResult();
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = Arrays.asList(primary,
				secondaryDifferentPeriod);
		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = Arrays.asList(
				new TimesheetAndSettingValidatorResponseBodyDto(), new TimesheetAndSettingValidatorResponseBodyDto());

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DEFAULT_TIMESHEET_ID))
			.willReturn(null);
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(List.of(DEFAULT_TIMESHEET_ID)))
			.willReturn(Arrays.asList(primary));
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(timesheetIds)).willReturn(validatorResults);
		given(this.timesheetSettingRepository.findTimesheetSettingsWithTemplateWorkDayByIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.validatorMapper.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults))
			.willReturn(responseDtos);
		willDoNothing().given(this.contractStaffingAccessControlChecker)
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));

		// When
		FetchBulkTimelogValidatedResponseBodyDto result = this.validatorService
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos()).hasSize(1);
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos().get(0).getError()).isEqualTo("different_period");
		// The error DTO formats a non empty timesheet period for the secondary timesheet
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos().get(0).getTimesheetPeriod()).isNotEmpty();
	}

	@Test
	@DisplayName("Validate time logs before update should add different settings error when work log type differs")
	void testValidateTimeLogsBeforeUpdateNonPrimaryDifferentWorkLogTypeAddsSettingsError() {
		// Given - secondary timesheet has a different work log type than the primary
		List<Integer> timesheetIds = Arrays.asList(DEFAULT_TIMESHEET_ID,
				ValidatorTestDataFactory.getSecondaryTimesheetId());
		TimesheetAndSettingValidatorQueryResultDto primary = ValidatorTestDataFactory.createPrimaryActiveQueryResult();
		TimesheetAndSettingValidatorQueryResultDto secondaryDifferentType = ValidatorTestDataFactory
			.createNonPrimaryDifferentWorkLogTypeQueryResult();
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = Arrays.asList(primary,
				secondaryDifferentType);
		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = Arrays.asList(
				new TimesheetAndSettingValidatorResponseBodyDto(), new TimesheetAndSettingValidatorResponseBodyDto());

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DEFAULT_TIMESHEET_ID))
			.willReturn(null);
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(List.of(DEFAULT_TIMESHEET_ID)))
			.willReturn(Arrays.asList(primary));
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(timesheetIds)).willReturn(validatorResults);
		given(this.timesheetSettingRepository.findTimesheetSettingsWithTemplateWorkDayByIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.validatorMapper.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults))
			.willReturn(responseDtos);
		willDoNothing().given(this.contractStaffingAccessControlChecker)
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));

		// When
		FetchBulkTimelogValidatedResponseBodyDto result = this.validatorService
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos()).hasSize(1);
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos().get(0).getError()).isEqualTo("different_setting");
	}

	@Test
	@DisplayName("Validate time logs before update should use template work day data to match work days")
	void testValidateTimeLogsBeforeUpdateMatchingWorkDaysProducesNoSettingsError() {
		// Given - both timesheets share the same work log type and matching work days
		List<Integer> timesheetIds = Arrays.asList(DEFAULT_TIMESHEET_ID,
				ValidatorTestDataFactory.getSecondaryTimesheetId());
		TimesheetAndSettingValidatorQueryResultDto primary = ValidatorTestDataFactory.createPrimaryActiveQueryResult();
		TimesheetAndSettingValidatorQueryResultDto secondarySamePeriod = ValidatorTestDataFactory
			.createNonPrimaryApprovedQueryResult();
		// Reuse approved factory but force it not approved so it falls through to
		// settings
		secondarySamePeriod.setTimesheetApprovalStatusTypeId(null);
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = Arrays.asList(primary, secondarySamePeriod);
		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = Arrays.asList(
				new TimesheetAndSettingValidatorResponseBodyDto(), new TimesheetAndSettingValidatorResponseBodyDto());
		List<TimesheetSettingTemplateWorkDayDto> workDayData = Arrays.asList(
				ValidatorTestDataFactory.createTemplateWorkDay(DEFAULT_TIMESHEET_SETTING_ID, Arrays.asList(1, 2, 3)));

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DEFAULT_TIMESHEET_ID))
			.willReturn(null);
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(List.of(DEFAULT_TIMESHEET_ID)))
			.willReturn(Arrays.asList(primary));
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(timesheetIds)).willReturn(validatorResults);
		given(this.timesheetSettingRepository.findTimesheetSettingsWithTemplateWorkDayByIds(anyList()))
			.willReturn(workDayData);
		given(this.validatorMapper.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults))
			.willReturn(responseDtos);
		willDoNothing().given(this.contractStaffingAccessControlChecker)
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));

		// When
		FetchBulkTimelogValidatedResponseBodyDto result = this.validatorService
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// Then - same work log type and matching work days mean no settings error
		assertThat(result.getTimesheetSettingErrorResponseBodyDtos()).isEmpty();
		then(this.timesheetSettingRepository).should().findTimesheetSettingsWithTemplateWorkDayByIds(anyList());
	}

	@Test
	@DisplayName("Validate contractor time logs before update should return contractor response")
	void testValidateContractorTimeLogsBeforeUpdateReturnsContractorResponse() {
		// Given
		List<Integer> timesheetIds = ValidatorTestDataFactory.createSingleTimesheetIdList();
		TimesheetAndSettingValidatorQueryResultDto activeCandidateResult = ValidatorTestDataFactory
			.createValidatorQueryResultWithActiveCandidate();
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = Arrays.asList(activeCandidateResult);
		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = ValidatorTestDataFactory
			.createValidatorResponseBodyDtoList();

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DEFAULT_TIMESHEET_ID))
			.willReturn(null);
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(anyList())).willReturn(validatorResults);
		given(this.timesheetSettingRepository.findTimesheetSettingsWithTemplateWorkDayByIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.validatorMapper.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults))
			.willReturn(responseDtos);

		// When
		var result = this.validatorService.validateContractorTimeLogsBeforeUpdate(timesheetIds);

		// Then - contractor response DTOs are converted from the regular response DTOs
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetAndSettingValidatorResponseBodyDtos()).hasSize(1);
		assertThat(result.getTimesheetAndSettingValidatorResponseBodyDtos().get(0).getTimesheetId())
			.isEqualTo(DEFAULT_TIMESHEET_ID);
		assertThat(result.getPrimaryTimesheetSettingId()).isEqualTo(DEFAULT_TIMESHEET_SETTING_ID);
		assertThat(result.getErrorData()).isEmpty();
		// Contractor validation does not invoke access control
		then(this.contractStaffingAccessControlChecker).should(never())
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));
	}

	@Test
	@DisplayName("Validate contractor time logs before update should ignore period differences and convert errors")
	void testValidateContractorTimeLogsBeforeUpdateIgnoresPeriodDifferenceConvertsErrors() {
		// Given - secondary timesheet differs by work log type so an error is produced,
		// while a different period is allowed for contractor validation
		List<Integer> timesheetIds = Arrays.asList(DEFAULT_TIMESHEET_ID,
				ValidatorTestDataFactory.getSecondaryTimesheetId());
		TimesheetAndSettingValidatorQueryResultDto primary = ValidatorTestDataFactory.createPrimaryActiveQueryResult();
		TimesheetAndSettingValidatorQueryResultDto secondaryDifferentType = ValidatorTestDataFactory
			.createNonPrimaryDifferentWorkLogTypeQueryResult();
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = Arrays.asList(primary,
				secondaryDifferentType);
		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = Arrays.asList(
				new TimesheetAndSettingValidatorResponseBodyDto(), new TimesheetAndSettingValidatorResponseBodyDto());

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DEFAULT_TIMESHEET_ID))
			.willReturn(null);
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(List.of(DEFAULT_TIMESHEET_ID)))
			.willReturn(Arrays.asList(primary));
		given(this.validatorRepository.validateTimeLogsBeforeUpdate(timesheetIds)).willReturn(validatorResults);
		given(this.timesheetSettingRepository.findTimesheetSettingsWithTemplateWorkDayByIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.validatorMapper.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults))
			.willReturn(responseDtos);

		// When
		var result = this.validatorService.validateContractorTimeLogsBeforeUpdate(timesheetIds);

		// Then - work log type difference still produces an error, mapped to contractor
		// DTO
		assertThat(result.getErrorData()).hasSize(1);
		assertThat(result.getErrorData().get(0).getError()).isEqualTo("different_setting");
		assertThat(result.getErrorData().get(0).getTimesheetId())
			.isEqualTo(ValidatorTestDataFactory.getSecondaryTimesheetId());
	}

	@Test
	@DisplayName("Validate contractor time logs before update should throw exception when first timesheet is approved")
	void testValidateContractorTimeLogsBeforeUpdateApprovedFirstTimesheetThrowsException() {
		// Given
		List<Integer> timesheetIds = ValidatorTestDataFactory.createSingleTimesheetIdList();
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(DEFAULT_TIMESHEET_ID))
			.willReturn(ValidatorTestDataFactory.createApprovedTimesheetApproval());

		// When & Then
		assertThatThrownBy(() -> this.validatorService.validateContractorTimeLogsBeforeUpdate(timesheetIds))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining(ValidatorTestDataFactory.Messages.FIRST_TIMESHEET_APPROVED);

		then(this.validatorRepository).should(never()).validateTimeLogsBeforeUpdate(anyList());
	}

}
