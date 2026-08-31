package io.recruitcrm.microservice.timesheet.controllers.timesheet;

import io.recruitcrm.microservice.timesheet.dto.timesheet.ValidateTimesheetEmailRequestBodyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface ITimesheetEmailValidationController {

	ResponseEntity<?> validateTimesheetEmails(@Valid @RequestBody ValidateTimesheetEmailRequestBodyDto requestDto);

}
