package io.recruitcrm.microservice.timesheet.services.time_log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.TimeLogBreakInterval;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogMigrationQueryResultDto;
import io.recruitcrm.microservice.timesheet.repositories.time_log.ITimeLogIntervalRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.ITimeLogRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.ITimeLogBreakIntervalRepository;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for migrating time log data from cst_time_log_t and
 * cst_time_log_break_interval_t to cst_time_log_interval_t. Supports batch migration with
 * configurable batch size and offset.
 */
@Service
public class TimeLogIntervalMigrationService implements ITimeLogIntervalMigrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimeLogIntervalMigrationService.class);

	private final ITimeLogRepository timeLogRepository;

	private final ITimeLogBreakIntervalRepository timeLogBreakIntervalRepository;

	private final ITimeLogIntervalRepository timeLogIntervalRepository;

	private final ObjectMapper objectMapper;

	public TimeLogIntervalMigrationService(ITimeLogRepository timeLogRepository,
			ITimeLogBreakIntervalRepository timeLogBreakIntervalRepository,
			ITimeLogIntervalRepository timeLogIntervalRepository, ObjectMapper objectMapper) {
		this.timeLogRepository = timeLogRepository;
		this.timeLogBreakIntervalRepository = timeLogBreakIntervalRepository;
		this.timeLogIntervalRepository = timeLogIntervalRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	@WriterRoute
	@Transactional
	public TimeLogIntervalMigrationResponseBodyDto migrateTimeLogsToIntervalTable(
			TimeLogIntervalMigrationRequestBodyDto request) {
		int batchSize = request.getBatchSize();
		int offset = request.getOffset();

		LOGGER.info("Starting time log interval migration: batchSize={}, offset={}", batchSize, offset);

		List<TimeLogMigrationQueryResultDto> timeLogs = this.timeLogRepository.findTimeLogsForMigration(batchSize,
				offset);

		if (timeLogs.isEmpty()) {
			LOGGER.info("No time logs to migrate for offset={}", offset);
			long remaining = this.timeLogRepository.countUnmigratedTimeLogs();
			TimeLogIntervalMigrationResponseBodyDto response = new TimeLogIntervalMigrationResponseBodyDto(0, false);
			response.setNextOffset(offset + batchSize);
			response.setRemainingRecords(remaining);
			response.setTotalInBatch(0);
			response.setSkippedCount(0);
			return response;
		}

		List<TimeLogMigrationQueryResultDto> toMigrate = timeLogs.stream()
			.filter((tl) -> tl.getId() != null && !this.isWorkHourType(tl.getWorkLogType())
					&& !this.shouldSkipOpenTimesheetTimeLog(tl))
			.toList();

		int skippedCount = timeLogs.size() - toMigrate.size();

		List<Integer> toMigrateIds = toMigrate.stream().map(TimeLogMigrationQueryResultDto::getId).toList();

		List<Integer> alreadyMigratedIds = this.timeLogIntervalRepository
			.findTimeLogIdsWithExistingIntervals(toMigrateIds);
		if (!alreadyMigratedIds.isEmpty()) {
			LOGGER.info("Re-migrating {} time log(s) that already have intervals — deleting existing records first",
					alreadyMigratedIds.size());
			this.timeLogIntervalRepository.deleteByTimeLogIntervalIdIn(alreadyMigratedIds);
		}

		Map<Integer, List<TimeLogBreakInterval>> breakIntervalsByTimeLogId = this
			.fetchBreakIntervalsByTimeLogId(toMigrateIds);

		List<Object[]> valuesToInsert = this.buildInsertValues(toMigrate, breakIntervalsByTimeLogId);

		List<Integer> migratedTimeLogIds = new ArrayList<>();
		List<Integer> failedTimeLogIds = new ArrayList<>();

		if (valuesToInsert.isEmpty()) {
			LOGGER.info("All {} records in batch were skipped (WORK_HOUR type), skipping insert", timeLogs.size());
		}
		else {
			try {
				int inserted = this.timeLogIntervalRepository.batchInsert(valuesToInsert);
				collectIdsFromMigrationDtos(toMigrate, migratedTimeLogIds);
				LOGGER.debug("Batch insert completed: attempted {}, inserted {}", toMigrate.size(), inserted);
			}
			catch (Exception ex) {
				LOGGER.warn("Batch insert failed: {}", ex.getMessage());
				collectIdsFromMigrationDtos(toMigrate, failedTimeLogIds);
			}
		}

		int nextOffset = offset + batchSize;
		long remainingRecords = this.timeLogRepository.countUnmigratedTimeLogs();

		LOGGER.info("Migration completed: migrated {} time logs, failed {}, nextOffset={}, remaining={}",
				migratedTimeLogIds.size(), failedTimeLogIds.size(), nextOffset, remainingRecords);

		TimeLogIntervalMigrationResponseBodyDto response = new TimeLogIntervalMigrationResponseBodyDto();
		response.setMigratedCount(migratedTimeLogIds.size());
		response.setMigratedTimeLogIds(migratedTimeLogIds);
		response.setFailedTimeLogIds(failedTimeLogIds);
		response.setNextOffset(nextOffset);
		response.setTotalInBatch(timeLogs.size());
		response.setSkippedCount(skippedCount);
		return response;
	}

	private void collectIdsFromMigrationDtos(List<TimeLogMigrationQueryResultDto> toMigrate, List<Integer> outputIds) {
		for (TimeLogMigrationQueryResultDto timeLog : toMigrate) {
			if (timeLog.getId() != null) {
				outputIds.add(timeLog.getId());
			}
		}
	}

	private boolean isWorkHourType(Integer workLogType) {
		return workLogType != null && workLogType == WorkLogType.WORK_HOUR.getTypeId();
	}

	/**
	 * Returns true if the time log belongs to an open (not submitted) timesheet AND all
	 * of work_time, work_start_time, work_end_time, and remark are null, meaning there is
	 * nothing to migrate. Returns false (i.e. should migrate) if any of those fields is
	 * non-null, or if the timesheet is not in the open state.
	 */
	private boolean shouldSkipOpenTimesheetTimeLog(TimeLogMigrationQueryResultDto timeLog) {
		Integer statusTypeId = timeLog.getTimesheetApprovalStatusTypeId();
		boolean isOpenState = statusTypeId == null || statusTypeId.equals(ApprovalStatusEnum.OPEN.getId());
		if (!isOpenState) {
			return false;
		}
		return timeLog.getWorkTime() == null && timeLog.getWorkStartTime() == null && timeLog.getWorkEndTime() == null
				&& timeLog.getRemark() == null;
	}

	private Map<Integer, List<TimeLogBreakInterval>> fetchBreakIntervalsByTimeLogId(List<Integer> timeLogIds) {
		if (timeLogIds.isEmpty()) {
			return new HashMap<>();
		}

		List<TimeLogBreakInterval> allBreakIntervals = this.timeLogBreakIntervalRepository
			.findBreakIntervalsByTimeLogIdIn(timeLogIds);

		return allBreakIntervals.stream()
			.filter((bi) -> bi.getTimeLogId() != null)
			.collect(Collectors.groupingBy(TimeLogBreakInterval::getTimeLogId));
	}

	private List<Object[]> buildInsertValues(List<TimeLogMigrationQueryResultDto> timeLogs,
			Map<Integer, List<TimeLogBreakInterval>> breakIntervalsByTimeLogId) {
		List<Object[]> values = new ArrayList<>();

		for (TimeLogMigrationQueryResultDto timeLog : timeLogs) {
			Integer timeLogId = timeLog.getId();
			if (timeLogId == null) {
				continue;
			}

			Integer workStartTime = timeLog.getWorkStartTime();
			Integer workEndTime = timeLog.getWorkEndTime();
			String rangeBasedRemark = timeLog.getRemark();

			String breakIntervalJson = this
				.buildBreakIntervalJson(breakIntervalsByTimeLogId.getOrDefault(timeLogId, List.of()));

			Object[] row = new Object[] { timeLogId, workStartTime, workEndTime, rangeBasedRemark, breakIntervalJson };
			values.add(row);
		}

		return values;
	}

	/**
	 * Builds break interval JSON in format: [{"id": 1, "breakStartTime": 32400,
	 * "breakEndTime": 34200}, ...] where breakStartTime = break_start_time, breakEndTime
	 * = break_end_time from cst_time_log_break_interval_t.
	 */
	private String buildBreakIntervalJson(List<TimeLogBreakInterval> breakIntervals) {
		if (breakIntervals == null || breakIntervals.isEmpty()) {
			return "[]";
		}

		List<Map<String, Object>> breakIntervalList = new ArrayList<>();
		int id = 1;
		for (TimeLogBreakInterval bi : breakIntervals) {
			Map<String, Object> entry = new HashMap<>();
			entry.put("id", id++);
			entry.put("breakStartTime", bi.getBreakStartTime());
			entry.put("breakEndTime", bi.getBreakEndTime());
			breakIntervalList.add(entry);
		}

		try {
			return this.objectMapper.writeValueAsString(breakIntervalList);
		}
		catch (JsonProcessingException ex) {
			LOGGER.warn("Failed to serialize break interval JSON, using empty array", ex);
			return "[]";
		}
	}

}
