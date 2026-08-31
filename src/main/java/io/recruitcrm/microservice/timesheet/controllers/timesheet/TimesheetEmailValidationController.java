package io.recruitcrm.microservice.timesheet.controllers.timesheet;

import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetEmailValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ValidateTimesheetEmailRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.timesheet.TimesheetEmailValidationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/timesheet/custom/email")
public class TimesheetEmailValidationController implements ITimesheetEmailValidationController {

	private final TimesheetEmailValidationService timesheetEmailValidationService;

	private final APIResponder apiResponder;

	public TimesheetEmailValidationController(TimesheetEmailValidationService timesheetEmailValidationService,
			APIResponder apiResponder) {
		this.timesheetEmailValidationService = timesheetEmailValidationService;
		this.apiResponder = apiResponder;
	}

	@Override
	@PostMapping("/validate")
	public ResponseEntity<?> validateTimesheetEmails(
			@Valid @RequestBody ValidateTimesheetEmailRequestBodyDto requestDto) {
		TimesheetEmailValidationResponseBodyDto result = this.timesheetEmailValidationService
			.validateTimesheetEmails(requestDto);
		return this.apiResponder.respond(result, "Timesheet validation fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

}
