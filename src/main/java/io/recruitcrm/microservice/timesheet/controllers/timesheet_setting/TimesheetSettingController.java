package io.recruitcrm.microservice.timesheet.controllers.timesheet_setting;

import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.EnableTimesheetSettingRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingBulkRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingPreferenceResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.timesheet_setting.TimesheetSettingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/timesheet-settings")
public class TimesheetSettingController implements ITimesheetSettingController {

	private final TimesheetSettingService timesheetSettingService;

	private final APIResponder apiResponder;

	public TimesheetSettingController(TimesheetSettingService timesheetSettingService, APIResponder apiResponder) {
		this.timesheetSettingService = timesheetSettingService;
		this.apiResponder = apiResponder;
	}

	@Override
	@GetMapping("/job/{jobId}/contractor/{contractorId}")
	public ResponseEntity<?> getTimesheetSettingByAssignmentId(@PathVariable Integer jobId,
			@PathVariable Integer contractorId) {
		TimesheetSettingResponseBodyDto timesheetSetting = this.timesheetSettingService
			.getTimesheetSettingByAssignmentId(jobId, contractorId);
		return this.apiResponder.respond(timesheetSetting, "Timesheet setting fetched successfully");
	}

	@Override
	@PostMapping("")
	public ResponseEntity<?> createBulkTimesheetSettings(
			@Valid @RequestBody TimesheetSettingBulkRequestBodyDto request) {
		this.timesheetSettingService.createBulkTimesheetSettings(request);
		return this.apiResponder.respond(null, "Timesheet setting created successfully");
	}

	@Override
	@GetMapping("/job/{jobId}/contractor/{contractorId}/validate")
	public ResponseEntity<?> getTimesheetSettingDateValidation(@PathVariable Integer jobId,
			@PathVariable Integer contractorId, @RequestParam(required = false) Long startDate,
			@RequestParam(required = false) Long endDate) {
		Boolean response = this.timesheetSettingService.getTimesheetSettingDateValidation(jobId, contractorId,
				startDate, endDate);
		if (Boolean.TRUE.equals(response)) {
			return this.apiResponder.respond(true, "Timesheet setting validation successfully");
		}
		return this.apiResponder.respondWithError(new ValidationErrorException("Timesheets exist for the given date"),
				APIResponseType.WARNING, HttpStatus.BAD_REQUEST);
	}

	@Override
	@PostMapping("/enable-status")
	public ResponseEntity<?> getEnabledAssigmentIds(@Valid @RequestBody EnableTimesheetSettingRequestBodyDto request) {
		List<Integer> enabledAssignmentIds = this.timesheetSettingService.getEnabledAssigmentIds(request);
		return this.apiResponder.respond(enabledAssignmentIds,
				"Timesheet setting enabled assignment ids successfully fetched");
	}

	@Override
	@GetMapping("/preferences")
	public ResponseEntity<?> getUserTimesheetSettingPreference() {
		TimesheetSettingPreferenceResponseBodyDto preference = this.timesheetSettingService
			.getUserTimesheetSettingPreference();
		return this.apiResponder.respond(preference, "Timesheet setting preference fetched successfully");
	}

}