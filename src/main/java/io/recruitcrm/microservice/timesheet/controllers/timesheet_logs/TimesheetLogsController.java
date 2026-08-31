package io.recruitcrm.microservice.timesheet.controllers.timesheet_logs;

import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.BulkTimeLogRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.FetchBulkTimelogResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.FetchBulkTimelogValidatedResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.contractor.FetchBulkContractorTimelogResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.contractor.FetchContractorBulkTimelogValidatedResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.timesheet_logs.TimesheetLogsService;
import io.recruitcrm.microservice.timesheet.services.timesheet_status.TimesheetInvoiceStatusService;
import io.recruitcrm.microservice.timesheet.services.validator.ValidatorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/timesheets")
public class TimesheetLogsController implements ITimesheetLogsController {

	private final TimesheetLogsService timesheetLogsService;

	private final APIResponder apiResponder;

	private final ValidatorService validatorService;

	private final TimesheetInvoiceStatusService timesheetInvoiceStatusService;

	public TimesheetLogsController(final TimesheetLogsService timesheetLogsService, final APIResponder apiResponder,
			ValidatorService validatorService, final TimesheetInvoiceStatusService timesheetInvoiceStatusService) {
		this.timesheetLogsService = timesheetLogsService;
		this.apiResponder = apiResponder;
		this.validatorService = validatorService;
		this.timesheetInvoiceStatusService = timesheetInvoiceStatusService;
	}

	@Override
	@GetMapping("/{id}/time-logs")
	public ResponseEntity<?> getTimeLogsByTimesheetId(@PathVariable("id") Integer id) {
		TimesheetResponseBodyDto timesheet = this.timesheetLogsService.getTimeLogsByTimesheetId(id);
		return this.apiResponder.respond(timesheet, "Time logs fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PatchMapping("/bulk/time-logs")
	public ResponseEntity<?> bulkUpdateTimeLogs(@RequestBody BulkUpdateTimeLogsRequestBodyDto requestDto) {
		this.timesheetLogsService.bulkUpdateTimeLogs(requestDto);
		return this.apiResponder.respond(null, "Time logs bulk updated successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PostMapping("/deal/validate/time-logs")
	public ResponseEntity<?> getBulkTimeLogs(@RequestBody BulkTimeLogRequestBodyDto requestBodyDto) {
		if (requestBodyDto.getTimesheetIds() == null || requestBodyDto.getTimesheetIds().isEmpty()) {
			throw new ValidationErrorException("At least one timesheet id is required");
		}
		FetchBulkTimelogValidatedResponseBodyDto fetchBulkTimelogValidatedResponseBodyDto = this.validatorService
			.validateTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds());
		FetchBulkTimelogResultBodyDto fetchBulkTimelogResultBodyDto = this.timesheetLogsService.getAllTimeLogs(
				requestBodyDto.getTimesheetIds(),
				fetchBulkTimelogValidatedResponseBodyDto.getTimesheetAndSettingValidatorResponseBodyDtos(),
				fetchBulkTimelogValidatedResponseBodyDto.getTimesheetSettingErrorResponseBodyDtos());

		fetchBulkTimelogResultBodyDto.setContractorsErrorData(
				fetchBulkTimelogValidatedResponseBodyDto.getTimesheetSettingErrorResponseBodyDtos());
		return this.apiResponder.respond(fetchBulkTimelogResultBodyDto, "Time logs fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@PostMapping("/contractor/validate/time-logs")
	public ResponseEntity<?> getContractorBulkTimeLogs(@RequestBody BulkTimeLogRequestBodyDto requestBodyDto) {
		if (requestBodyDto.getTimesheetIds() == null || requestBodyDto.getTimesheetIds().isEmpty()) {
			throw new ValidationErrorException("At least one timesheet id is required");
		}
		FetchContractorBulkTimelogValidatedResponseBodyDto fetchContractorBulkTimelogValidatedResponseBodyDto = this.validatorService
			.validateContractorTimeLogsBeforeUpdate(requestBodyDto.getTimesheetIds());

		FetchBulkContractorTimelogResultBodyDto fetchBulkContractorTimelogResultBodyDto = this.timesheetLogsService
			.getContractorAllTimeLogs(requestBodyDto.getTimesheetIds(),
					fetchContractorBulkTimelogValidatedResponseBodyDto
						.getTimesheetAndSettingValidatorResponseBodyDtos(),
					fetchContractorBulkTimelogValidatedResponseBodyDto.getErrorData());

		fetchBulkContractorTimelogResultBodyDto
			.setErrorData(fetchContractorBulkTimelogValidatedResponseBodyDto.getErrorData());
		return this.apiResponder.respond(fetchBulkContractorTimelogResultBodyDto, "Time logs fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@PostMapping("/{id}/status")
	public ResponseEntity<?> updateTimesheetStatus(@PathVariable("id") Integer timesheetId,
			@Validated @RequestBody UpdateTimesheetStatusRequestBodyDto requestDto) {
		this.timesheetInvoiceStatusService.updateTimesheetStatus(timesheetId, requestDto);
		return this.apiResponder.respond(null, "Timesheet status updated successfully", APIResponseType.SUCCESS,
				HttpStatus.CREATED);
	}

}
