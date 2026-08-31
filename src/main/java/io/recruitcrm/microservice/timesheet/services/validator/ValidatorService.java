package io.recruitcrm.microservice.timesheet.services.validator;

import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;

import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.FetchBulkTimelogValidatedResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.contractor.FetchContractorBulkTimelogValidatedResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingTemplateWorkDayDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetSettingErrorResponseBodyDto;

import io.recruitcrm.microservice.timesheet.mapper.ValidatorMapper;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_setting.ITimesheetSettingRepository;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingErrorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.ValidationErrorEnum;
import io.recruitcrm.microservice.timesheet.repositories.validator.ValidatorRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_approval.TimesheetApprovalJpaRepository;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ValidatorService {

	private final ValidatorRepository validatorRepository;

	private final ITimesheetSettingRepository timesheetSettingRepository;

	private final ValidatorMapper validatorMapper;

	private final TimesheetApprovalJpaRepository timesheetApprovalJpaRepository;

	final AccessControlChecker contractStaffingAccessControlChecker;

	public ValidatorService(ValidatorRepository validatorRepository,
			ITimesheetSettingRepository timesheetSettingRepository, ValidatorMapper validatorMapper,
			TimesheetApprovalJpaRepository timesheetApprovalJpaRepository,
			AccessControlChecker contractStaffingAccessControlChecker) {
		this.validatorRepository = validatorRepository;
		this.timesheetSettingRepository = timesheetSettingRepository;
		this.validatorMapper = validatorMapper;
		this.timesheetApprovalJpaRepository = timesheetApprovalJpaRepository;
		this.contractStaffingAccessControlChecker = contractStaffingAccessControlChecker;
	}

	public FetchBulkTimelogValidatedResponseBodyDto validateTimeLogsBeforeUpdate(List<Integer> timesheetIds) {

		// Fetch primary timesheet first.
		Integer primaryTimesheetId = timesheetIds.getFirst();

		// Check if the first timesheet is approved using existing logic pattern from
		// RuleEngineService
		TimesheetApproval timesheetApproval = this.timesheetApprovalJpaRepository
			.findFirstByTimesheetIdOrderByIdDesc(primaryTimesheetId);

		if (timesheetApproval != null && Objects.equals(timesheetApproval.getTimesheetApprovalStatusTypeId(),
				ApprovalStatusEnum.APPROVED.getId())) {
			throw new ValidationErrorException("First timesheet id is approved");
		}

		TimesheetAndSettingValidatorQueryResultDto primaryContractorTimesheet = this.validatorRepository
			.validateTimeLogsBeforeUpdate(List.of(primaryTimesheetId))
			.getFirst();
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = this.validatorRepository
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// ACCESS CONTROL LOGIC
		PermissionCheckContext permissionCheckContext = new PermissionCheckContext();
		permissionCheckContext.setPermission(Permission.EDIT_TIMESHEET);
		permissionCheckContext.setPermissionLevel(PermissionLevel.YES);
		Set<Integer> editTimesheetPermissionExceptionTimesheetIds = new HashSet<>();
		AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();

		for (TimesheetAndSettingValidatorQueryResultDto validatorResult : validatorResults) {
			// Skip access control check if candidate is deleted (contractorName is null)
			// This allows editing timesheets for deleted candidates without access
			// control restrictions
			if (validatorResult.getContractorName() == null) {
				continue;
			}

			metadataContext.setTimesheetId(validatorResult.getTimesheetId());
			try {
				this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, permissionCheckContext,
						metadataContext);
			}
			catch (Exception ex) {
				editTimesheetPermissionExceptionTimesheetIds.add(validatorResult.getTimesheetId());
			}
		}

		Set<Integer> timesheetSettingIdsWithSameWorkDays = getTimesheetSettingIdsWithSameWorkDays(validatorResults,
				primaryContractorTimesheet.getTimesheetSettingId());

		List<TimesheetAndSettingValidatorResponseBodyDto> responseDtos = this.validatorMapper
			.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults);

		List<TimesheetSettingErrorResponseBodyDto> errorDtos = fillAllValidationErrors(primaryContractorTimesheet,
				primaryTimesheetId, validatorResults, timesheetSettingIdsWithSameWorkDays, false,
				editTimesheetPermissionExceptionTimesheetIds);

		return new FetchBulkTimelogValidatedResponseBodyDto(errorDtos, responseDtos,
				primaryContractorTimesheet.getTimesheetSettingId());
	}

	public FetchContractorBulkTimelogValidatedResponseBodyDto validateContractorTimeLogsBeforeUpdate(
			List<Integer> timesheetIds) {

		// Fetch primary timesheet first.
		Integer primaryTimesheetId = timesheetIds.getFirst();

		// Check if the first timesheet is approved using existing logic pattern from
		// RuleEngineService
		TimesheetApproval timesheetApproval = this.timesheetApprovalJpaRepository
			.findFirstByTimesheetIdOrderByIdDesc(primaryTimesheetId);

		if (timesheetApproval != null && Objects.equals(timesheetApproval.getTimesheetApprovalStatusTypeId(),
				ApprovalStatusEnum.APPROVED.getId())) {
			throw new ValidationErrorException("First timesheet id is approved");
		}

		TimesheetAndSettingValidatorQueryResultDto primaryContractorTimesheet = this.validatorRepository
			.validateTimeLogsBeforeUpdate(List.of(primaryTimesheetId))
			.getFirst();
		List<TimesheetAndSettingValidatorQueryResultDto> validatorResults = this.validatorRepository
			.validateTimeLogsBeforeUpdate(timesheetIds);

		Set<Integer> timesheetSettingIdsWithSameWorkDays = getTimesheetSettingIdsWithSameWorkDays(validatorResults,
				primaryContractorTimesheet.getTimesheetSettingId());

		List<TimesheetAndSettingValidatorResponseBodyDto> tempResponseDtos = this.validatorMapper
			.timesheetAndSettingValidatorQueryResultDtoToResponseBodyDto(validatorResults);

		// Convert to contractor response DTOs
		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> responseDtos = convertToContractorResponseDtos(
				tempResponseDtos);

		// Use contractor-specific validation logic that allows different periods
		List<TimesheetSettingErrorResponseBodyDto> dealErrorDtos = fillAllValidationErrors(primaryContractorTimesheet,
				primaryTimesheetId, validatorResults, timesheetSettingIdsWithSameWorkDays, true, new HashSet<>());
		List<ContractorTimesheetSettingErrorResponseBodyDto> errorDtos = convertToContractorErrorDtos(dealErrorDtos);

		return new FetchContractorBulkTimelogValidatedResponseBodyDto(errorDtos, responseDtos,
				primaryContractorTimesheet.getTimesheetSettingId());
	}

	/**
	 * Gets timesheet setting IDs that have the same work days as the primary timesheet
	 * setting. Uses the new template_work_day JSON approach instead of the old
	 * TimesheetSettingWorkDay table.
	 */
	private Set<Integer> getTimesheetSettingIdsWithSameWorkDays(
			List<TimesheetAndSettingValidatorQueryResultDto> validatorResults, Integer primaryTimesheetSettingId) {

		// Extract all unique timesheet setting IDs from validator results
		List<Integer> allTimesheetSettingIds = validatorResults.stream()
			.map(TimesheetAndSettingValidatorQueryResultDto::getTimesheetSettingId)
			.distinct()
			.toList();

		// Fetch template work day data for all timesheet settings
		List<TimesheetSettingTemplateWorkDayDto> templateWorkDayData = this.timesheetSettingRepository
			.findTimesheetSettingsWithTemplateWorkDayByIds(allTimesheetSettingIds);

		// Find the primary timesheet setting's work day IDs
		List<Integer> primaryWorkDayIds = templateWorkDayData.stream()
			.filter((data) -> data.getTimesheetSettingId().equals(primaryTimesheetSettingId))
			.findFirst()
			.map(TimesheetSettingTemplateWorkDayDto::getWorkDayIds)
			.orElse(new ArrayList<>());

		// Find all timesheet settings with matching work day IDs
		Set<Integer> timesheetSettingIdsWithSameWorkDays = templateWorkDayData.stream()
			.filter((data) -> hasSameWorkDays(data.getWorkDayIds(), primaryWorkDayIds))
			.map(TimesheetSettingTemplateWorkDayDto::getTimesheetSettingId)
			.collect(Collectors.toSet());

		// Always include the primary timesheet setting
		timesheetSettingIdsWithSameWorkDays.add(primaryTimesheetSettingId);

		return timesheetSettingIdsWithSameWorkDays;
	}

	/**
	 * Checks if two lists of work day IDs represent the same set of work days. Order
	 * doesn't matter, only the content.
	 */
	private boolean hasSameWorkDays(List<Integer> workDayIds1, List<Integer> workDayIds2) {
		if (workDayIds1 == null || workDayIds2 == null) {
			return workDayIds1 == workDayIds2; // Both null or one null
		}

		// Convert to sets to ignore order and duplicates
		Set<Integer> set1 = new HashSet<>(workDayIds1);
		Set<Integer> set2 = new HashSet<>(workDayIds2);

		return set1.equals(set2);
	}

	public List<TimesheetSettingErrorResponseBodyDto> fillAllValidationErrors(
			TimesheetAndSettingValidatorQueryResultDto primaryContractorTimesheet, Integer primaryTimesheetId,
			List<TimesheetAndSettingValidatorQueryResultDto> validatorResults,
			Set<Integer> timesheetSettingIdsWithSameWorkDays, boolean isContractorValidation,
			Set<Integer> editTimesheetPermissionExceptionTimesheetIds) {

		List<TimesheetSettingErrorResponseBodyDto> errorDtos = new ArrayList<>();

		for (TimesheetAndSettingValidatorQueryResultDto dto : validatorResults) {
			if (isNotPrimaryTimesheet(dto, primaryTimesheetId)
					|| !editTimesheetPermissionExceptionTimesheetIds.isEmpty()) {
				validateTimesheetAndSetting(dto, primaryContractorTimesheet, timesheetSettingIdsWithSameWorkDays,
						errorDtos, isContractorValidation, editTimesheetPermissionExceptionTimesheetIds);
			}
		}
		return errorDtos;
	}

	private boolean isNotPrimaryTimesheet(TimesheetAndSettingValidatorQueryResultDto dto, Integer primaryTimesheetId) {
		return !dto.getTimesheetId().equals(primaryTimesheetId);
	}

	private void validateTimesheetAndSetting(TimesheetAndSettingValidatorQueryResultDto dto,
			TimesheetAndSettingValidatorQueryResultDto primaryContractorTimesheet,
			Set<Integer> timesheetSettingIdsWithSameWorkDays, List<TimesheetSettingErrorResponseBodyDto> errorDtos,
			boolean isContractorValidation, Set<Integer> editTimesheetPermissionExceptionTimesheetIds) {

		// Priority 1: Timesheet is approved (highest priority)
		if (isTimesheetApproved(dto)) {
			errorDtos.add(createErrorDto(dto, ValidationErrorEnum.TIMESHEET_APPROVED.getMessage()));
			return; // Stop further validation for this timesheet
		}

		// Priority 2: Timesheet period is different
		// For contractor validation, ignore period differences as they are allowed
		if (!isContractorValidation && hasDifferentPeriod(dto, primaryContractorTimesheet)) {
			errorDtos.add(createErrorDto(dto, ValidationErrorEnum.TIMESHEET_DIFFERENT_PERIOD.getMessage()));
			return; // Stop further validation for this timesheet
		}

		// Priority 3: Timesheet setting is different
		if (hasDifferentWorkTimeType(dto, primaryContractorTimesheet)) {
			errorDtos.add(createErrorDto(dto, ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS.getMessage()));
			return; // Stop further validation for this timesheet
		}

		if (!isContractorValidation && !timesheetSettingIdsWithSameWorkDays.contains(dto.getTimesheetSettingId())) {
			errorDtos.add(createErrorDto(dto, ValidationErrorEnum.TIMESHEET_DIFFERENT_SETTINGS.getMessage()));
			return; // Stop further validation for this timesheet
		}

		// Priority 4: No edit access for the Timesheet
		if (editTimesheetPermissionExceptionTimesheetIds.contains(dto.getTimesheetId())) {
			errorDtos.add(createErrorDto(dto, ValidationErrorEnum.NO_EDIT_ACCESS.getMessage()));
		}

	}

	private boolean hasDifferentPeriod(TimesheetAndSettingValidatorQueryResultDto dto,
			TimesheetAndSettingValidatorQueryResultDto primaryContractorTimesheet) {
		// Compare UTC dates instead of epoch values to handle timezone differences
		// properly
		LocalDate dtoPeriodStart = convertEpochToUtcLocalDate(dto.getPeriodStart());
		LocalDate dtoPeriodEnd = convertEpochToUtcLocalDate(dto.getPeriodEnd());
		LocalDate primaryPeriodStart = convertEpochToUtcLocalDate(primaryContractorTimesheet.getPeriodStart());
		LocalDate primaryPeriodEnd = convertEpochToUtcLocalDate(primaryContractorTimesheet.getPeriodEnd());

		return !Objects.equals(dtoPeriodStart, primaryPeriodStart) || !Objects.equals(dtoPeriodEnd, primaryPeriodEnd);
	}

	/**
	 * Converts epoch timestamp to UTC LocalDate for consistent date comparison. This
	 * ensures that period validation is based on actual dates rather than
	 * timezone-sensitive epoch values.
	 */
	private LocalDate convertEpochToUtcLocalDate(Integer epochSeconds) {
		if (epochSeconds == null) {
			return null;
		}
		return Instant.ofEpochSecond(epochSeconds).atZone(ZoneOffset.UTC).toLocalDate();
	}

	private boolean hasDifferentWorkTimeType(TimesheetAndSettingValidatorQueryResultDto dto,
			TimesheetAndSettingValidatorQueryResultDto primaryContractorTimesheet) {
		return !Objects.equals(dto.getWorkLogType(), primaryContractorTimesheet.getWorkLogType());
	}

	private boolean isTimesheetApproved(TimesheetAndSettingValidatorQueryResultDto dto) {
		return Objects.equals(dto.getTimesheetApprovalStatusTypeId(), ApprovalStatusEnum.APPROVED.getId());
	}

	private TimesheetSettingErrorResponseBodyDto createErrorDto(TimesheetAndSettingValidatorQueryResultDto dto,
			String errorMessage) {
		String timesheetPeriod = formatTimesheetPeriod(dto.getPeriodStart(), dto.getPeriodEnd());
		return new TimesheetSettingErrorResponseBodyDto(dto.getContractorId(), dto.getTimesheetId(), timesheetPeriod,
				errorMessage, dto.getContractorName(), dto.getContractorPhoto(), dto.getJobName(),
				dto.getCompanyProfilePicUrl(), dto.getContractorSerialNumber());
	}

	private String formatTimesheetPeriod(Integer startDateEpoch, Integer endDateEpoch) {
		if (startDateEpoch == null || endDateEpoch == null) {
			return "";
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

		// Use UTC timezone consistently for period formatting
		LocalDate startDate = convertEpochToUtcLocalDate(startDateEpoch);
		LocalDate endDate = convertEpochToUtcLocalDate(endDateEpoch);

		return String.format("%s - %s", startDate.format(formatter), endDate.format(formatter));
	}

	private List<ContractorTimesheetAndSettingValidatorResponseBodyDto> convertToContractorResponseDtos(
			List<TimesheetAndSettingValidatorResponseBodyDto> regularDtos) {
		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorDtos = new ArrayList<>();

		for (TimesheetAndSettingValidatorResponseBodyDto regularDto : regularDtos) {
			ContractorTimesheetAndSettingValidatorResponseBodyDto contractorDto = new ContractorTimesheetAndSettingValidatorResponseBodyDto();
			contractorDto.setTimesheetSettingId(regularDto.getTimesheetSettingId());
			contractorDto.setTimesheetId(regularDto.getTimesheetId());
			contractorDto.setWorkLogType(regularDto.getWorkLogType());
			contractorDtos.add(contractorDto);
		}

		return contractorDtos;
	}

	private List<ContractorTimesheetSettingErrorResponseBodyDto> convertToContractorErrorDtos(
			List<TimesheetSettingErrorResponseBodyDto> dealErrorDtos) {
		List<ContractorTimesheetSettingErrorResponseBodyDto> contractorErrorDtos = new ArrayList<>();

		for (TimesheetSettingErrorResponseBodyDto dealError : dealErrorDtos) {
			// Convert deal error DTO to contractor error DTO by removing extra fields
			ContractorTimesheetSettingErrorResponseBodyDto contractorError = new ContractorTimesheetSettingErrorResponseBodyDto(
					dealError.getId(), // contractorId
					dealError.getTimesheetId(), dealError.getTimesheetPeriod(), dealError.getError(),
					dealError.getContractorSerialNumber()
			// Note: contractorName, contractorProfilePicUrl, contractorJobName,
			// companyProfilePicUrl are not included
			);
			contractorErrorDtos.add(contractorError);
		}

		return contractorErrorDtos;
	}

}
