package io.recruitcrm.microservice.timesheet.services.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateResponseBodyDto;

public interface IBulkValidateService {

	BulkValidateResponseBodyDto bulkValidate(BulkValidateRequestBodyDto request);

}
