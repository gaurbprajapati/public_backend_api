package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.FetchBulkTimelogValidatedResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingErrorResponseBodyDto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test data factory for Validator-related test objects. Provides factory methods to
 * create consistent test data across all validator tests.
 */
public final class ValidatorTestDataFactory {

	private ValidatorTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Creates a TimesheetAndSettingValidatorQueryResultDto with active candidate
	 * @return TimesheetAndSettingValidatorQueryResultDto
	 */
	public static TimesheetAndSettingValidatorQueryResultDto createValidatorQueryResultWithActiveCandidate() {
		return new TimesheetAndSettingValidatorQueryResultDto(getDefaultContractorId(), getDefaultJobId(),
				getDefaultWorkLogType(), // START_AND_END_TIME type
				getPeriodStartEpoch(), getPeriodEndEpoch(), getDefaultContractorName(), getDefaultContractorPhoto(),
				getDefaultJobName(), getDefaultTimesheetSettingId(), getDefaultTimesheetId(),
				getDefaultCompanyProfilePicUrl(), null, // Not approved
				true, // calculateBreakTime
				new ArrayList<>(), // templateWorkDays
				getDefaultContractorSerialNumber());
	}

	/**
	 * Creates a TimesheetAndSettingValidatorQueryResultDto with deleted candidate
	 * (contractorName is null)
	 * @return TimesheetAndSettingValidatorQueryResultDto
	 */
	public static TimesheetAndSettingValidatorQueryResultDto createValidatorQueryResultWithDeletedCandidate() {
		return new TimesheetAndSettingValidatorQueryResultDto(getDefaultContractorId(), getDefaultJobId(),
				getDefaultWorkLogType(), // START_AND_END_TIME type
				getPeriodStartEpoch(), getPeriodEndEpoch(), null, // contractorName is
																	// null for deleted
																	// candidate
				null, // contractorPhoto is null
				getDefaultJobName(), getDefaultTimesheetSettingId(), getDeletedCandidateTimesheetId(),
				getDefaultCompanyProfilePicUrl(), null, // Not approved
				true, // calculateBreakTime
				new ArrayList<>(), // templateWorkDays
				null // contractorSerialNumber is null
		);
	}

	/**
	 * Creates a TimesheetAndSettingValidatorQueryResultDto with approved timesheet
	 * @return TimesheetAndSettingValidatorQueryResultDto
	 */
	public static TimesheetAndSettingValidatorQueryResultDto createValidatorQueryResultWithApprovedTimesheet() {
		return new TimesheetAndSettingValidatorQueryResultDto(getDefaultContractorId(), getDefaultJobId(),
				getDefaultWorkLogType(), // START_AND_END_TIME type
				getPeriodStartEpoch(), getPeriodEndEpoch(), getDefaultContractorName(), getDefaultContractorPhoto(),
				getDefaultJobName(), getDefaultTimesheetSettingId(), getApprovedTimesheetId(),
				getDefaultCompanyProfilePicUrl(), ApprovalStatusEnum.APPROVED.getId(), // Approved
																						// status
				true, // calculateBreakTime
				new ArrayList<>(), // templateWorkDays
				getDefaultContractorSerialNumber());
	}

	/**
	 * Creates a list with both active and deleted candidate query results
	 * @return List of TimesheetAndSettingValidatorQueryResultDto
	 */
	public static List<TimesheetAndSettingValidatorQueryResultDto> createMixedValidatorQueryResultList() {
		return Arrays.asList(createValidatorQueryResultWithActiveCandidate(),
				createValidatorQueryResultWithDeletedCandidate());
	}

	/**
	 * Creates a list with only active candidate query results
	 * @return List of TimesheetAndSettingValidatorQueryResultDto
	 */
	public static List<TimesheetAndSettingValidatorQueryResultDto> createActiveValidatorQueryResultList() {
		return Arrays.asList(createValidatorQueryResultWithActiveCandidate());
	}

	/**
	 * Creates a list with only deleted candidate query results
	 * @return List of TimesheetAndSettingValidatorQueryResultDto
	 */
	public static List<TimesheetAndSettingValidatorQueryResultDto> createDeletedValidatorQueryResultList() {
		return Arrays.asList(createValidatorQueryResultWithDeletedCandidate());
	}

	/**
	 * Creates a TimesheetApproval entity with approved status
	 * @return TimesheetApproval
	 */
	public static TimesheetApproval createApprovedTimesheetApproval() {
		TimesheetApproval approval = new TimesheetApproval();
		approval.setId(1);
		approval.setTimesheetId(getDefaultTimesheetId());
		approval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());
		approval.setCreatedOn(Math.toIntExact(Instant.now().getEpochSecond()));
		return approval;
	}

	/**
	 * Creates a TimesheetApproval entity with pending/open status
	 * @return TimesheetApproval
	 */
	public static TimesheetApproval createPendingTimesheetApproval() {
		TimesheetApproval approval = new TimesheetApproval();
		approval.setId(1);
		approval.setTimesheetId(getDefaultTimesheetId());
		approval.setTimesheetApprovalStatusTypeId(1); // 1 = Open/Pending status
		approval.setCreatedOn(Math.toIntExact(Instant.now().getEpochSecond()));
		return approval;
	}

	/**
	 * Creates a list of TimesheetAndSettingValidatorResponseBodyDto
	 * @return List of response DTOs
	 */
	public static List<TimesheetAndSettingValidatorResponseBodyDto> createValidatorResponseBodyDtoList() {
		TimesheetAndSettingValidatorResponseBodyDto dto = new TimesheetAndSettingValidatorResponseBodyDto();
		dto.setTimesheetSettingId(getDefaultTimesheetSettingId());
		dto.setTimesheetId(getDefaultTimesheetId());
		dto.setWorkLogType(getDefaultWorkLogType());
		return Arrays.asList(dto);
	}

	public static Integer getDefaultWorkLogType() {
		// START_AND_END_TIME workLogType = 2
		return 2;
	}

	/**
	 * Creates a non-primary active candidate query result that is approved. Used to
	 * exercise the TIMESHEET_APPROVED validation branch.
	 * @return TimesheetAndSettingValidatorQueryResultDto
	 */
	public static TimesheetAndSettingValidatorQueryResultDto createNonPrimaryApprovedQueryResult() {
		return new TimesheetAndSettingValidatorQueryResultDto(getDefaultContractorId(), getDefaultJobId(),
				getDefaultWorkLogType(), getPeriodStartEpoch(), getPeriodEndEpoch(), getDefaultContractorName(),
				getDefaultContractorPhoto(), getDefaultJobName(), getDefaultTimesheetSettingId(),
				getSecondaryTimesheetId(), getDefaultCompanyProfilePicUrl(), ApprovalStatusEnum.APPROVED.getId(), true,
				new ArrayList<>(), getDefaultContractorSerialNumber());
	}

	/**
	 * Creates a non-primary active candidate query result with a different period. Used
	 * to exercise the TIMESHEET_DIFFERENT_PERIOD validation branch.
	 * @return TimesheetAndSettingValidatorQueryResultDto
	 */
	public static TimesheetAndSettingValidatorQueryResultDto createNonPrimaryDifferentPeriodQueryResult() {
		return new TimesheetAndSettingValidatorQueryResultDto(getDefaultContractorId(), getDefaultJobId(),
				getDefaultWorkLogType(), getDifferentPeriodStartEpoch(), getDifferentPeriodEndEpoch(),
				getDefaultContractorName(), getDefaultContractorPhoto(), getDefaultJobName(),
				getDefaultTimesheetSettingId(), getSecondaryTimesheetId(), getDefaultCompanyProfilePicUrl(), null, true,
				new ArrayList<>(), getDefaultContractorSerialNumber());
	}

	/**
	 * Creates a non-primary active candidate query result with a different work log type.
	 * Used to exercise the TIMESHEET_DIFFERENT_SETTINGS (work time type) validation
	 * branch.
	 * @return TimesheetAndSettingValidatorQueryResultDto
	 */
	public static TimesheetAndSettingValidatorQueryResultDto createNonPrimaryDifferentWorkLogTypeQueryResult() {
		return new TimesheetAndSettingValidatorQueryResultDto(getDefaultContractorId(), getDefaultJobId(),
				getDifferentWorkLogType(), getPeriodStartEpoch(), getPeriodEndEpoch(), getDefaultContractorName(),
				getDefaultContractorPhoto(), getDefaultJobName(), getDefaultTimesheetSettingId(),
				getSecondaryTimesheetId(), getDefaultCompanyProfilePicUrl(), null, true, new ArrayList<>(),
				getDefaultContractorSerialNumber());
	}

	/**
	 * Creates a primary active candidate query result whose timesheet id matches the
	 * default primary timesheet id.
	 * @return TimesheetAndSettingValidatorQueryResultDto
	 */
	public static TimesheetAndSettingValidatorQueryResultDto createPrimaryActiveQueryResult() {
		return createValidatorQueryResultWithActiveCandidate();
	}

	/**
	 * Creates a template work day DTO for the supplied timesheet setting and work day
	 * IDs.
	 * @param timesheetSettingId timesheet setting id
	 * @param workDayIds work day ids
	 * @return TimesheetSettingTemplateWorkDayDto
	 */
	public static io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingTemplateWorkDayDto createTemplateWorkDay(
			Integer timesheetSettingId, List<Integer> workDayIds) {
		io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingTemplateWorkDayDto dto = new io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingTemplateWorkDayDto();
		dto.setTimesheetSettingId(timesheetSettingId);
		dto.setWorkDayIds(workDayIds);
		return dto;
	}

	public static Integer getSecondaryTimesheetId() {
		return 8800;
	}

	public static Integer getSecondaryTimesheetSettingId() {
		return 2;
	}

	public static Integer getDifferentWorkLogType() {
		// DAILY_HOURS workLogType = 1 (different from default 2)
		return 1;
	}

	public static Integer getDifferentPeriodStartEpoch() {
		// Feb 02, 2026
		LocalDate startDate = LocalDate.of(2026, 2, 2);
		return (int) startDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
	}

	public static Integer getDifferentPeriodEndEpoch() {
		// Feb 08, 2026
		LocalDate endDate = LocalDate.of(2026, 2, 8);
		return (int) endDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
	}

	/**
	 * Creates a FetchBulkTimelogValidatedResponseBodyDto
	 * @return FetchBulkTimelogValidatedResponseBodyDto
	 */
	public static FetchBulkTimelogValidatedResponseBodyDto createFetchBulkTimelogValidatedResponse() {
		return new FetchBulkTimelogValidatedResponseBodyDto(Collections.emptyList(),
				createValidatorResponseBodyDtoList(), getDefaultTimesheetSettingId());
	}

	/**
	 * Creates a FetchBulkTimelogValidatedResponseBodyDto with error data
	 * @return FetchBulkTimelogValidatedResponseBodyDto
	 */
	public static FetchBulkTimelogValidatedResponseBodyDto createFetchBulkTimelogValidatedResponseWithErrors() {
		List<TimesheetSettingErrorResponseBodyDto> errorDtos = Arrays
			.asList(createTimesheetSettingErrorResponseBodyDto());
		return new FetchBulkTimelogValidatedResponseBodyDto(errorDtos, createValidatorResponseBodyDtoList(),
				getDefaultTimesheetSettingId());
	}

	/**
	 * Creates a TimesheetSettingErrorResponseBodyDto for no_edit_access error
	 * @return TimesheetSettingErrorResponseBodyDto
	 */
	public static TimesheetSettingErrorResponseBodyDto createTimesheetSettingErrorResponseBodyDto() {
		return new TimesheetSettingErrorResponseBodyDto(getDefaultContractorId(), getDefaultTimesheetId(),
				"Jan 05, 2026 - Jan 11, 2026", "no_edit_access", getDefaultContractorName(),
				getDefaultContractorPhoto(), getDefaultJobName(), getDefaultCompanyProfilePicUrl(),
				getDefaultContractorSerialNumber());
	}

	/**
	 * Creates a list of timesheet IDs
	 * @return List of timesheet IDs
	 */
	public static List<Integer> createTimesheetIdList() {
		return Arrays.asList(getDefaultTimesheetId(), getDeletedCandidateTimesheetId());
	}

	/**
	 * Creates a single timesheet ID list
	 * @return List with single timesheet ID
	 */
	public static List<Integer> createSingleTimesheetIdList() {
		return Arrays.asList(getDefaultTimesheetId());
	}

	public static Integer getDefaultTimesheetId() {
		return 7721;
	}

	public static Integer getDeletedCandidateTimesheetId() {
		return 7724;
	}

	public static Integer getApprovedTimesheetId() {
		return 7730;
	}

	public static Integer getDefaultTimesheetSettingId() {
		return 1;
	}

	public static Integer getDefaultContractorId() {
		return 57492777;
	}

	public static Integer getDefaultJobId() {
		return 100;
	}

	public static String getDefaultContractorName() {
		return "John Doe";
	}

	public static String getDefaultContractorPhoto() {
		return "https://example.com/photo.jpg";
	}

	public static String getDefaultJobName() {
		return "testing - yash";
	}

	public static String getDefaultCompanyProfilePicUrl() {
		return "https://example.com/company.jpg";
	}

	public static Integer getDefaultContractorSerialNumber() {
		return 12345;
	}

	public static Integer getPeriodStartEpoch() {
		// Jan 05, 2026
		LocalDate startDate = LocalDate.of(2026, 1, 5);
		return (int) startDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
	}

	public static Integer getPeriodEndEpoch() {
		// Jan 11, 2026
		LocalDate endDate = LocalDate.of(2026, 1, 11);
		return (int) endDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
	}

	/**
	 * Message constants for test assertions.
	 */
	public static final class Messages {

		public static final String FIRST_TIMESHEET_APPROVED = "First timesheet id is approved";

		public static final String TIMESHEET_NOT_FOUND = "Timesheet not found";

		private Messages() {
			// Messages class - prevent instantiation
		}

	}

}
