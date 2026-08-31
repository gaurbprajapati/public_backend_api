package io.recruitcrm.microservice.timesheet.controllers.timesheet;

import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateBulkTimesheetsForMultipleJobsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CreateTimesheetRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DeleteTimesheetsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCountResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAccessControlResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetSearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetMigrationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateJobTimesheetAccessControlRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.SearchEntityRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.SearchEntityResponseBodyDto;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.timesheet.ITimesheetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/timesheets")
public class TimesheetController implements ITimesheetController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimesheetController.class);

	private static final int AUTHORIZED_ACCOUNT_ID = 728;

	private static final int AUTHORIZED_USER_ID = 20206;

	private static final String TIMESHEETS_FETCHED_SUCCESSFULLY = "Timesheets fetched successfully";

	private final String env;

	private final AuthHolder auth;

	private final ITimesheetService timesheetService;

	private final APIResponder apiResponder;

	public TimesheetController(final ITimesheetService timesheetService, final APIResponder apiResponder,
			final AuthHolder auth, @Value("${application.env}") final String env) {
		this.timesheetService = timesheetService;
		this.apiResponder = apiResponder;
		this.auth = auth;
		this.env = env;
	}

	@Override
	@PostMapping("/jobs/{jobId}/contractors")
	public ResponseEntity<?> createTimesheets(@Validated @RequestBody CreateBulkTimesheetRequestBodyDto requestObject,
			@PathVariable("jobId") Integer jobId) {
		List<Integer> contractorIds = requestObject.getContractorIds();
		List<CreateTimesheetRequestBodyDto> timesheetDates = requestObject.getTimesheetDates();
		this.timesheetService.createTimesheets(jobId, contractorIds, timesheetDates);
		return this.apiResponder.respond(null, "Timesheets created successfully");
	}

	@Override
	@PostMapping("/bulk-create")
	public ResponseEntity<?> createBulkTimesheetsForMultipleJobs(
			@Validated @RequestBody CreateBulkTimesheetsForMultipleJobsRequestBodyDto requestDto) {
		this.timesheetService.createBulkTimesheetsForMultipleJobs(requestDto);
		return this.apiResponder.respond(null, "Timesheets created successfully for multiple jobs",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteTimesheet(@PathVariable("id") Integer id) {
		this.timesheetService.deleteTimesheet(id);
		return this.apiResponder.respond(null, "Timesheet deleted successfully");
	}

	@Override
	@GetMapping("/{id}/status-history")
	public ResponseEntity<?> getTimesheetStatusHistory(@PathVariable("id") Integer timesheetId) {
		TimesheetStatusHistoryResponseBodyDto statusHistory = this.timesheetService
			.getTimesheetStatusHistory(timesheetId);
		return this.apiResponder.respond(statusHistory, "Timesheet status history fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@PostMapping("/search/get")
	public ResponseEntity<?> searchTimesheets(@RequestBody TimesheetSearchRequestBodyDto searchRequestBodyDto,
			PaginationRequestBodyDto paginationRequestBodyDto) {
		List<TimesheetListResponseBodyDto> timesheets = this.timesheetService.searchTimesheets(searchRequestBodyDto,
				paginationRequestBodyDto.toPageable());
		return this.apiResponder.respond(timesheets, TIMESHEETS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PostMapping("/search/count")
	public ResponseEntity<?> searchTimesheetsCount(@RequestBody TimesheetSearchRequestBodyDto searchRequestBodyDto) {
		Long count = this.timesheetService.searchTimesheetsCount(searchRequestBodyDto);
		return this.apiResponder.respond(count, "Timesheet count fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@DeleteMapping
	public ResponseEntity<?> deleteTimesheets(@Validated @RequestBody DeleteTimesheetsRequestBodyDto requestDto) {
		this.timesheetService.deleteTimesheets(requestDto.getTimesheetIds());
		return this.apiResponder.respond(null, "Timesheets deleted successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PostMapping("/deal/{dealId}/get")
	public ResponseEntity<?> getTimesheetsListByDealId(@PathVariable("dealId") Integer dealId,
			@RequestBody SearchRequestBodyDto searchRequestBodyDto, PaginationRequestBodyDto paginationRequestBodyDto) {
		List<TimesheetListResponseBodyDto> timesheets = this.timesheetService.getTimesheetsListByDealId(dealId,
				searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		return this.apiResponder.respond(timesheets, TIMESHEETS_FETCHED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PostMapping("/job/contractor/get")
	public ResponseEntity<?> getTimesheetsListByJobAndContractorId(@RequestParam("jobId") Integer jobId,
			@RequestParam("contractorId") Integer contractorId, @RequestBody SearchRequestBodyDto searchRequestBodyDto,
			PaginationRequestBodyDto paginationRequestBodyDto) {
		List<TimesheetListResponseBodyDto> timesheetListResponseBodyDtos = this.timesheetService
			.getTimesheetsListByJobAndContractorId(jobId, contractorId, searchRequestBodyDto,
					paginationRequestBodyDto.toPageable());
		return this.apiResponder.respond(timesheetListResponseBodyDtos, TIMESHEETS_FETCHED_SUCCESSFULLY,
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@PostMapping("/access/job/{jobId}")
	public ResponseEntity<?> updateJobTimesheetAccessControl(@PathVariable("jobId") Integer jobId,
			@RequestBody UpdateJobTimesheetAccessControlRequestBodyDto updateJobTimesheetAccessControlRequestBodyDto) {
		this.timesheetService.updateJobTimesheetAccessControl(jobId, updateJobTimesheetAccessControlRequestBodyDto);
		return this.apiResponder.respond(null, "Timesheet Access Controls Updated successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@GetMapping("/access/job")
	public ResponseEntity<?> getTimesheetJobAccessInfo(@RequestParam Integer jobId) {
		TimesheetJobAccessControlResponseBodyDto timesheetJobAccessControl = this.timesheetService
			.getTimesheetJobAccessInfo(jobId);
		return this.apiResponder.respond(timesheetJobAccessControl, "Timesheet job access info fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@PostMapping("/entity/get")
	public ResponseEntity<?> getTimesheetsListByEntityId(@RequestBody SearchRequestBodyDto searchRequestBodyDto,
			PaginationRequestBodyDto paginationRequestBodyDto) {
		List<TimesheetListResponseBodyDto> timesheetListResponseBodyDtos = this.timesheetService
			.getTimesheetsListByEntityId(searchRequestBodyDto, paginationRequestBodyDto.toPageable());
		return this.apiResponder.respond(timesheetListResponseBodyDtos, TIMESHEETS_FETCHED_SUCCESSFULLY,
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@PostMapping("/entity/get/count")
	public ResponseEntity<?> getTimesheetsCountByEntityId(@RequestBody SearchRequestBodyDto searchRequestBodyDto) {
		TimesheetCountResponseBodyDto count = this.timesheetService.getTimesheetsCountByEntityId(searchRequestBodyDto);
		return this.apiResponder.respond(count, "Timesheet count fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PostMapping("/search-entity")
	public ResponseEntity<?> searchEntity(@Validated @RequestBody SearchEntityRequestBodyDto requestDto) {
		SearchEntityResponseBodyDto result = this.timesheetService.searchEntity(requestDto);
		return this.apiResponder.respond(result, "Entities searched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PostMapping("/migrate-total-columns")
	public ResponseEntity<?> migrateTimesheetTotalColumns(
			@Validated @RequestBody TimesheetMigrationRequestBodyDto requestDto) {
		try {
			Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
			Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();

			if ((this.env.equals("prod") || this.env.equals("production"))
					&& (!accountId.equals(AUTHORIZED_ACCOUNT_ID) || !userId.equals(AUTHORIZED_USER_ID))) {
				throw new UnauthorizedAccessException("You are not authorised to do this migration.");
			}

			var result = this.timesheetService.migrateTimesheetTotalColumns(requestDto);
			return this.apiResponder.respond(result, "Migration completed", APIResponseType.SUCCESS, HttpStatus.OK);
		}
		catch (Exception ex) {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			LOGGER.error("exception occurred inside migrateTimesheetTotalColumns() method :: {}", sw);
			return this.apiResponder.respondWithError(ex);
		}
	}

}
