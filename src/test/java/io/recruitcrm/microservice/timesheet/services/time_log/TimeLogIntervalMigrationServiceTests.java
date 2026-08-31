package io.recruitcrm.microservice.timesheet.services.time_log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.TimeLogBreakInterval;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationQueryResultDto;
import io.recruitcrm.microservice.timesheet.repositories.time_log.ITimeLogBreakIntervalRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.ITimeLogIntervalRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.ITimeLogRepository;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TimeLogIntervalMigrationServiceTests {

	@Mock
	private ITimeLogRepository timeLogRepository;

	@Mock
	private ITimeLogBreakIntervalRepository timeLogBreakIntervalRepository;

	@Mock
	private ITimeLogIntervalRepository timeLogIntervalRepository;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private TimeLogIntervalMigrationService timeLogIntervalMigrationService;

	@BeforeEach
	void setUp() {
		// Common setup
	}

	@Test
	@DisplayName("Migrate when no time logs returns empty response with nextOffset and remaining")
	void testMigrateTimeLogsToIntervalTableEmptyTimeLogsReturnsEmptyResponse() {
		// Given
		TimeLogIntervalMigrationRequestBodyDto request = new TimeLogIntervalMigrationRequestBodyDto(100, 0);
		given(this.timeLogRepository.findTimeLogsForMigration(100, 0)).willReturn(Collections.emptyList());
		given(this.timeLogRepository.countUnmigratedTimeLogs()).willReturn(0L);

		// When
		TimeLogIntervalMigrationResponseBodyDto result = this.timeLogIntervalMigrationService
			.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getMigratedCount()).isZero();
		assertThat(result.getTotalInBatch()).isZero();
		assertThat(result.getSkippedCount()).isZero();
		assertThat(result.getNextOffset()).isEqualTo(100);
		assertThat(result.getRemainingRecords()).isZero();
		then(this.timeLogRepository).should().findTimeLogsForMigration(100, 0);
		then(this.timeLogRepository).should().countUnmigratedTimeLogs();
	}

	@Test
	@DisplayName("Migrate with time logs all WORK_HOUR skipped and no insert")
	void testMigrateTimeLogsToIntervalTableAllWorkHourSkippedNoInsert() {
		// Given - WORK_HOUR type is skipped by filter
		TimeLogIntervalMigrationRequestBodyDto request = new TimeLogIntervalMigrationRequestBodyDto(100, 0);
		TimeLogMigrationQueryResultDto dto = new TimeLogMigrationQueryResultDto();
		dto.setId(1);
		dto.setWorkLogType(WorkLogType.WORK_HOUR.getTypeId());
		dto.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLogMigrationQueryResultDto> timeLogs = List.of(dto);
		given(this.timeLogRepository.findTimeLogsForMigration(100, 0)).willReturn(timeLogs);
		given(this.timeLogIntervalRepository.findTimeLogIdsWithExistingIntervals(any()))
			.willReturn(Collections.emptyList());
		given(this.timeLogRepository.countUnmigratedTimeLogs()).willReturn(0L);

		// When
		TimeLogIntervalMigrationResponseBodyDto result = this.timeLogIntervalMigrationService
			.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getMigratedCount()).isZero();
		assertThat(result.getSkippedCount()).isEqualTo(1);
		assertThat(result.getTotalInBatch()).isEqualTo(1);
		then(this.timeLogRepository).should().findTimeLogsForMigration(100, 0);
		then(this.timeLogIntervalRepository).should().findTimeLogIdsWithExistingIntervals(Collections.emptyList());
	}

	@Test
	@DisplayName("Migrate with migratable time log and no existing intervals inserts successfully")
	void testMigrateTimeLogsToIntervalTableOneMigratableInsertsSuccessfully() throws JsonProcessingException {
		// Given
		TimeLogIntervalMigrationRequestBodyDto request = new TimeLogIntervalMigrationRequestBodyDto(100, 0);
		TimeLogMigrationQueryResultDto dto = new TimeLogMigrationQueryResultDto();
		dto.setId(10);
		dto.setWorkStartTime(32400);
		dto.setWorkEndTime(34200);
		dto.setWorkLogType(WorkLogType.START_AND_END_TIME.getTypeId());
		dto.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLogMigrationQueryResultDto> timeLogs = List.of(dto);
		given(this.timeLogRepository.findTimeLogsForMigration(100, 0)).willReturn(timeLogs);
		given(this.timeLogIntervalRepository.findTimeLogIdsWithExistingIntervals(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(Collections.emptyList());
		given(this.objectMapper.writeValueAsString(any())).willReturn("[]");
		given(this.timeLogIntervalRepository.batchInsert(any())).willReturn(1);
		given(this.timeLogRepository.countUnmigratedTimeLogs()).willReturn(0L);

		// When
		TimeLogIntervalMigrationResponseBodyDto result = this.timeLogIntervalMigrationService
			.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getMigratedCount()).isEqualTo(1);
		assertThat(result.getMigratedTimeLogIds()).containsExactlyInAnyOrder(10);
		assertThat(result.getFailedTimeLogIds()).isEmpty();
		assertThat(result.getNextOffset()).isEqualTo(100);
		then(this.timeLogIntervalRepository).should().batchInsert(any());
	}

	@Test
	@DisplayName("Migrate when some time logs already have intervals deletes existing then inserts")
	void testMigrateTimeLogsToIntervalTableAlreadyMigratedDeletesThenInserts() throws JsonProcessingException {
		// Given
		TimeLogIntervalMigrationRequestBodyDto request = new TimeLogIntervalMigrationRequestBodyDto(50, 10);
		TimeLogMigrationQueryResultDto dto = new TimeLogMigrationQueryResultDto();
		dto.setId(20);
		dto.setWorkStartTime(36000);
		dto.setWorkEndTime(39600);
		dto.setWorkLogType(WorkLogType.START_AND_END_TIME.getTypeId());
		dto.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLogMigrationQueryResultDto> timeLogs = List.of(dto);
		given(this.timeLogRepository.findTimeLogsForMigration(50, 10)).willReturn(timeLogs);
		given(this.timeLogIntervalRepository.findTimeLogIdsWithExistingIntervals(anyList())).willReturn(List.of(20));
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(Collections.emptyList());
		given(this.objectMapper.writeValueAsString(any())).willReturn("[]");
		given(this.timeLogIntervalRepository.batchInsert(any())).willReturn(1);
		given(this.timeLogRepository.countUnmigratedTimeLogs()).willReturn(0L);

		// When
		TimeLogIntervalMigrationResponseBodyDto result = this.timeLogIntervalMigrationService
			.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(result.getMigratedCount()).isEqualTo(1);
		assertThat(result.getMigratedTimeLogIds()).containsExactlyInAnyOrder(20);
		then(this.timeLogIntervalRepository).should().deleteByTimeLogIntervalIdIn(anyList());
		then(this.timeLogIntervalRepository).should().batchInsert(any());
	}

	@Test
	@DisplayName("Migrate when batch insert throws adds time logs to failed list")
	void testMigrateTimeLogsToIntervalTableBatchInsertThrowsAddsToFailedList() throws JsonProcessingException {
		// Given
		TimeLogIntervalMigrationRequestBodyDto request = new TimeLogIntervalMigrationRequestBodyDto(100, 0);
		TimeLogMigrationQueryResultDto dto = new TimeLogMigrationQueryResultDto();
		dto.setId(30);
		dto.setWorkStartTime(32400);
		dto.setWorkEndTime(34200);
		dto.setWorkLogType(WorkLogType.START_AND_END_TIME.getTypeId());
		dto.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLogMigrationQueryResultDto> timeLogs = List.of(dto);
		given(this.timeLogRepository.findTimeLogsForMigration(100, 0)).willReturn(timeLogs);
		given(this.timeLogIntervalRepository.findTimeLogIdsWithExistingIntervals(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(Collections.emptyList());
		given(this.objectMapper.writeValueAsString(any())).willReturn("[]");
		willThrow(new RuntimeException("DB error")).given(this.timeLogIntervalRepository).batchInsert(any());
		given(this.timeLogRepository.countUnmigratedTimeLogs()).willReturn(5L);

		// When
		TimeLogIntervalMigrationResponseBodyDto result = this.timeLogIntervalMigrationService
			.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(result.getMigratedCount()).isZero();
		assertThat(result.getFailedTimeLogIds()).containsExactlyInAnyOrder(30);
		then(this.timeLogRepository).should().countUnmigratedTimeLogs();
	}

	@Test
	@DisplayName("Migrate with open timesheet and all null work fields skips time log")
	void testMigrateTimeLogsToIntervalTableOpenTimesheetAllNullSkipsTimeLog() {
		// Given - open status and workTime/workStartTime/workEndTime/remark all null ->
		// shouldSkip true
		TimeLogIntervalMigrationRequestBodyDto request = new TimeLogIntervalMigrationRequestBodyDto(100, 0);
		TimeLogMigrationQueryResultDto dto = new TimeLogMigrationQueryResultDto();
		dto.setId(40);
		dto.setWorkLogType(WorkLogType.START_AND_END_TIME.getTypeId());
		dto.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.OPEN.getId());
		dto.setWorkTime(null);
		dto.setWorkStartTime(null);
		dto.setWorkEndTime(null);
		dto.setRemark(null);
		List<TimeLogMigrationQueryResultDto> timeLogs = List.of(dto);
		given(this.timeLogRepository.findTimeLogsForMigration(100, 0)).willReturn(timeLogs);
		given(this.timeLogRepository.countUnmigratedTimeLogs()).willReturn(0L);

		// When
		TimeLogIntervalMigrationResponseBodyDto result = this.timeLogIntervalMigrationService
			.migrateTimeLogsToIntervalTable(request);

		// Then - filtered out so toMigrate empty, valuesToInsert empty
		assertThat(result.getSkippedCount()).isEqualTo(1);
		assertThat(result.getMigratedCount()).isZero();
	}

	@Test
	@DisplayName("Migrate with break intervals serializes break interval JSON")
	void testMigrateTimeLogsToIntervalTableWithBreakIntervalsSerializesJson() throws JsonProcessingException {
		// Given
		TimeLogIntervalMigrationRequestBodyDto request = new TimeLogIntervalMigrationRequestBodyDto(100, 0);
		TimeLogMigrationQueryResultDto dto = new TimeLogMigrationQueryResultDto();
		dto.setId(50);
		dto.setWorkStartTime(32400);
		dto.setWorkEndTime(34200);
		dto.setWorkLogType(WorkLogType.START_AND_END_TIME.getTypeId());
		dto.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());
		TimeLogBreakInterval breakInterval = mock(TimeLogBreakInterval.class);
		given(breakInterval.getTimeLogId()).willReturn(50);
		given(breakInterval.getBreakStartTime()).willReturn(32400);
		given(breakInterval.getBreakEndTime()).willReturn(34200);
		List<TimeLogMigrationQueryResultDto> timeLogs = List.of(dto);
		given(this.timeLogRepository.findTimeLogsForMigration(100, 0)).willReturn(timeLogs);
		given(this.timeLogIntervalRepository.findTimeLogIdsWithExistingIntervals(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(List.of(breakInterval));
		given(this.objectMapper.writeValueAsString(any())).willReturn("[{\"id\":1,\"breakStartTime\":32400}]");
		given(this.timeLogIntervalRepository.batchInsert(any())).willReturn(1);
		given(this.timeLogRepository.countUnmigratedTimeLogs()).willReturn(0L);

		// When
		TimeLogIntervalMigrationResponseBodyDto result = this.timeLogIntervalMigrationService
			.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(result.getMigratedCount()).isEqualTo(1);
		then(this.objectMapper).should().writeValueAsString(any());
	}

	@Test
	@DisplayName("Migrate when ObjectMapper writeValueAsString throws uses empty array")
	void testMigrateTimeLogsToIntervalTableJsonExceptionUsesEmptyArray() throws JsonProcessingException {
		// Given
		TimeLogIntervalMigrationRequestBodyDto request = new TimeLogIntervalMigrationRequestBodyDto(100, 0);
		TimeLogMigrationQueryResultDto dto = new TimeLogMigrationQueryResultDto();
		dto.setId(60);
		dto.setWorkStartTime(32400);
		dto.setWorkEndTime(34200);
		dto.setWorkLogType(WorkLogType.START_AND_END_TIME.getTypeId());
		dto.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());
		TimeLogBreakInterval breakInterval = mock(TimeLogBreakInterval.class);
		given(breakInterval.getTimeLogId()).willReturn(60);
		given(breakInterval.getBreakStartTime()).willReturn(32400);
		given(breakInterval.getBreakEndTime()).willReturn(34200);
		List<TimeLogMigrationQueryResultDto> timeLogs = List.of(dto);
		given(this.timeLogRepository.findTimeLogsForMigration(100, 0)).willReturn(timeLogs);
		given(this.timeLogIntervalRepository.findTimeLogIdsWithExistingIntervals(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(List.of(breakInterval));
		given(this.objectMapper.writeValueAsString(any())).willThrow(new JsonProcessingException("serialize fail") {
		});
		given(this.timeLogIntervalRepository.batchInsert(any())).willReturn(1);
		given(this.timeLogRepository.countUnmigratedTimeLogs()).willReturn(0L);

		// When
		TimeLogIntervalMigrationResponseBodyDto result = this.timeLogIntervalMigrationService
			.migrateTimeLogsToIntervalTable(request);

		// Then - buildBreakIntervalJson catches and returns "[]", insert still proceeds
		assertThat(result.getMigratedCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("Migrate with null workLogType is not filtered as WORK_HOUR")
	void testMigrateTimeLogsToIntervalTableNullWorkLogTypeMigrates() throws JsonProcessingException {
		// Given - workLogType null -> isWorkHourType returns false, so not skipped
		TimeLogIntervalMigrationRequestBodyDto request = new TimeLogIntervalMigrationRequestBodyDto(100, 0);
		TimeLogMigrationQueryResultDto dto = new TimeLogMigrationQueryResultDto();
		dto.setId(70);
		dto.setWorkStartTime(32400);
		dto.setWorkEndTime(34200);
		dto.setWorkLogType(null);
		dto.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLogMigrationQueryResultDto> timeLogs = List.of(dto);
		given(this.timeLogRepository.findTimeLogsForMigration(100, 0)).willReturn(timeLogs);
		given(this.timeLogIntervalRepository.findTimeLogIdsWithExistingIntervals(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(Collections.emptyList());
		given(this.objectMapper.writeValueAsString(any())).willReturn("[]");
		given(this.timeLogIntervalRepository.batchInsert(any())).willReturn(1);
		given(this.timeLogRepository.countUnmigratedTimeLogs()).willReturn(0L);

		// When
		TimeLogIntervalMigrationResponseBodyDto result = this.timeLogIntervalMigrationService
			.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(result.getMigratedCount()).isEqualTo(1);
		assertThat(result.getMigratedTimeLogIds()).containsExactlyInAnyOrder(70);
	}

	@Test
	@DisplayName("Migrate with open timesheet and non-null work start does not skip")
	void testMigrateTimeLogsToIntervalTableOpenTimesheetWithWorkStartDoesNotSkip() throws JsonProcessingException {
		// Given - open but workStartTime non-null -> shouldSkip false
		TimeLogIntervalMigrationRequestBodyDto request = new TimeLogIntervalMigrationRequestBodyDto(100, 0);
		TimeLogMigrationQueryResultDto dto = new TimeLogMigrationQueryResultDto();
		dto.setId(80);
		dto.setWorkStartTime(32400);
		dto.setWorkEndTime(34200);
		dto.setWorkLogType(WorkLogType.START_AND_END_TIME.getTypeId());
		dto.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.OPEN.getId());
		dto.setWorkTime(null);
		dto.setRemark(null);
		List<TimeLogMigrationQueryResultDto> timeLogs = List.of(dto);
		given(this.timeLogRepository.findTimeLogsForMigration(100, 0)).willReturn(timeLogs);
		given(this.timeLogIntervalRepository.findTimeLogIdsWithExistingIntervals(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(Collections.emptyList());
		given(this.objectMapper.writeValueAsString(any())).willReturn("[]");
		given(this.timeLogIntervalRepository.batchInsert(any())).willReturn(1);
		given(this.timeLogRepository.countUnmigratedTimeLogs()).willReturn(0L);

		// When
		TimeLogIntervalMigrationResponseBodyDto result = this.timeLogIntervalMigrationService
			.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(result.getMigratedCount()).isEqualTo(1);
		assertThat(result.getMigratedTimeLogIds()).containsExactlyInAnyOrder(80);
	}

	@Test
	@DisplayName("Migrate with null timesheet status treated as open")
	void testMigrateTimeLogsToIntervalTableNullTimesheetStatusTreatedAsOpen() {
		// Given - timesheetApprovalStatusTypeId null -> isOpenState true; all work fields
		// null -> skip
		TimeLogIntervalMigrationRequestBodyDto request = new TimeLogIntervalMigrationRequestBodyDto(100, 0);
		TimeLogMigrationQueryResultDto dto = new TimeLogMigrationQueryResultDto();
		dto.setId(90);
		dto.setWorkLogType(WorkLogType.START_AND_END_TIME.getTypeId());
		dto.setTimesheetApprovalStatusTypeId(null);
		dto.setWorkTime(null);
		dto.setWorkStartTime(null);
		dto.setWorkEndTime(null);
		dto.setRemark(null);
		List<TimeLogMigrationQueryResultDto> timeLogs = List.of(dto);
		given(this.timeLogRepository.findTimeLogsForMigration(100, 0)).willReturn(timeLogs);
		given(this.timeLogRepository.countUnmigratedTimeLogs()).willReturn(0L);

		// When
		TimeLogIntervalMigrationResponseBodyDto result = this.timeLogIntervalMigrationService
			.migrateTimeLogsToIntervalTable(request);

		// Then
		assertThat(result.getSkippedCount()).isEqualTo(1);
		assertThat(result.getMigratedCount()).isZero();
	}

}
