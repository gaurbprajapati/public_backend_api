package io.recruitcrm.microservice.timesheet.controllers.timesheet;

import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DeleteTimesheetsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetMigrationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateJobTimesheetAccessControlRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.SearchEntityRequestBodyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface ITimesheetController {

	ResponseEntity<?> createTimesheets(@RequestBody CreateBulkTimesheetRequestBodyDto requestObject,
			@PathVariable("jobId") Integer jobId);

	ResponseEntity<?> createBulkTimesheetsForMultipleJobs(
			@Valid @RequestBody CreateBulkTimesheetsForMultipleJobsRequestBodyDto requestDto);

	ResponseEntity<?> deleteTimesheet(@PathVariable("id") Integer timesheetId);

	ResponseEntity<?> deleteTimesheets(@Valid @RequestBody DeleteTimesheetsRequestBodyDto requestDto);

	ResponseEntity<?> getTimesheetStatusHistory(@PathVariable("id") Integer timesheetId);

	ResponseEntity<?> getTimesheetsListByDealId(@PathVariable("dealId") Integer dealId,
			@RequestBody SearchRequestBodyDto searchRequestBodyDto, PaginationRequestBodyDto paginationRequestBodyDto);

	ResponseEntity<?> getTimesheetsListByJobAndContractorId(@RequestParam("jobId") Integer jobId,
			@RequestParam("contractorId") Integer contractorId, @RequestBody SearchRequestBodyDto searchRequestBodyDto,
			PaginationRequestBodyDto paginationRequestBodyDto);

	ResponseEntity<?> updateJobTimesheetAccessControl(@PathVariable("jobId") Integer jobId,
			@RequestBody UpdateJobTimesheetAccessControlRequestBodyDto updateJobTimesheetAccessControlRequestBodyDto);

	ResponseEntity<?> getTimesheetsListByEntityId(@RequestBody SearchRequestBodyDto searchRequestBodyDto,
			PaginationRequestBodyDto paginationRequestBodyDto);

	ResponseEntity<?> getTimesheetsCountByEntityId(@RequestBody SearchRequestBodyDto searchRequestBodyDto);

	ResponseEntity<?> getTimesheetJobAccessInfo(@RequestParam("jobId") Integer jobId);

	ResponseEntity<?> searchTimesheets(
			@RequestBody io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto searchRequestBodyDto,
			PaginationRequestBodyDto paginationRequestBodyDto);

	ResponseEntity<?> searchTimesheetsCount(
			@RequestBody io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto searchRequestBodyDto);

	ResponseEntity<?> searchEntity(@Valid @RequestBody SearchEntityRequestBodyDto requestDto);

	ResponseEntity<?> migrateTimesheetTotalColumns(@Valid @RequestBody TimesheetMigrationRequestBodyDto requestDto);

}
