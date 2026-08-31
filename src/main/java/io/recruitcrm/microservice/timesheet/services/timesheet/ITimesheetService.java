package io.recruitcrm.microservice.timesheet.services.timesheet;

import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateTimesheetRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAccessControlResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateJobTimesheetAccessControlRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.SearchEntityRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.SearchEntityResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetMigrationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetMigrationResponseBodyDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ITimesheetService {

	void createTimesheets(Integer jobId, List<Integer> contractorIds,
			List<CreateTimesheetRequestBodyDto> timesheetDates);

	/**
	 * Creates timesheets for one job and contractor set. When
	 * {@code publishTimesheetCreatedReminderEvent} is {@code false}, no Kafka reminder is
	 * sent; callers (e.g. bulk multi-job create) aggregate IDs and publish once.
	 * @return IDs of timesheets created in this call (never null, possibly empty)
	 */
	List<Integer> createTimesheets(Integer jobId, List<Integer> contractorIds,
			List<CreateTimesheetRequestBodyDto> timesheetDates, boolean publishTimesheetCreatedReminderEvent);

	void createBulkTimesheetsForMultipleJobs(CreateBulkTimesheetsForMultipleJobsRequestBodyDto requestDto);

	void deleteTimesheet(Integer timesheetId);

	void deleteTimesheets(List<Integer> timesheetIds);

	void deletePortalTimesheets(Integer timesheetId, Integer jobId);

	TimesheetStatusHistoryResponseBodyDto getTimesheetStatusHistory(Integer timesheetId);

	List<TimesheetListResponseBodyDto> getTimesheetsListByDealId(Integer dealId,
			SearchRequestBodyDto searchRequestBodyDto, Pageable pageable);

	List<TimesheetListResponseBodyDto> getTimesheetsListByJobAndContractorId(Integer jobId, Integer contractorId,
			SearchRequestBodyDto searchRequestBodyDto, Pageable pageable);

	TimesheetJobAccessControlResponseBodyDto getTimesheetJobAccessInfo(Integer jobId);

	List<TimesheetListResponseBodyDto> getTimesheetsListByEntityId(SearchRequestBodyDto searchRequestBodyDto,
			Pageable pageable);

	TimesheetCountResponseBodyDto getTimesheetsCountByEntityId(SearchRequestBodyDto searchRequestBodyDto);

	void updateJobTimesheetAccessControl(Integer jobId,
			UpdateJobTimesheetAccessControlRequestBodyDto updateJobTimesheetAccessControlRequestBodyDto);

	List<TimesheetListResponseBodyDto> searchTimesheets(
			io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto searchRequestBodyDto,
			Pageable pageable);

	Long searchTimesheetsCount(
			io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto searchRequestBodyDto);

	SearchEntityResponseBodyDto searchEntity(SearchEntityRequestBodyDto requestDto);

	/**
	 * Migrates existing timesheet data to total_time, total_work_time, and total_overtime
	 * columns in cst_timesheet_t. Processes timesheets in batches.
	 * @param requestDto Migration request with batch size
	 * @return Migration result with successful and failed migrations
	 */
	TimesheetMigrationResponseBodyDto migrateTimesheetTotalColumns(TimesheetMigrationRequestBodyDto requestDto);

}
