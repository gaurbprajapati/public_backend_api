package io.recruitcrm.microservice.timesheet.controllers.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateRequestBodyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface IBulkValidateController {

	ResponseEntity<?> bulkValidate(@Valid @RequestBody BulkValidateRequestBodyDto request);

}
