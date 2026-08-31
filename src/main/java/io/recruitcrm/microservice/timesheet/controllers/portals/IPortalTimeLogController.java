package io.recruitcrm.microservice.timesheet.controllers.portals;

import io.recruitcrm.microservice.timesheet.dto.portal.DeletePortalTimesheetsRequestBodyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface IPortalTimeLogController {

	ResponseEntity<?> getPortalTimeLogs(@PathVariable("id") Integer id);

	ResponseEntity<?> deletePortalTimesheets(@RequestBody DeletePortalTimesheetsRequestBodyDto requestDto);

}
