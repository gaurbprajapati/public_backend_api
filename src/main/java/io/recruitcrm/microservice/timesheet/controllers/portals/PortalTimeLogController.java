package io.recruitcrm.microservice.timesheet.controllers.portals;

import io.recruitcrm.microservice.timesheet.dto.portal.DeletePortalTimesheetsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.timesheet.TimesheetService;
import io.recruitcrm.microservice.timesheet.services.timesheet_logs.TimesheetLogsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/portal/timesheets")
public class PortalTimeLogController implements IPortalTimeLogController {

	private final TimesheetLogsService timesheetLogsService;

	private final TimesheetService timesheetService;

	private final APIResponder apiResponder;

	public PortalTimeLogController(final TimesheetLogsService timesheetLogsService,
			final TimesheetService timesheetService, final APIResponder apiResponder) {
		this.timesheetLogsService = timesheetLogsService;
		this.timesheetService = timesheetService;
		this.apiResponder = apiResponder;
	}

	@Override
	@GetMapping("/{id}/time-logs")
	public ResponseEntity<?> getPortalTimeLogs(@PathVariable("id") Integer id) {
		PortalTimesheetResponseBodyDto timesheet = this.timesheetLogsService.getPortalTimeLogs(id);
		return this.apiResponder.respond(timesheet, "Time logs fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@DeleteMapping
	public ResponseEntity<?> deletePortalTimesheets(@RequestBody DeletePortalTimesheetsRequestBodyDto requestDto) {
		this.timesheetService.deletePortalTimesheets(requestDto.getTimesheetId(), requestDto.getJobId());
		return this.apiResponder.respond(null, "Timesheets deleted successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

}
