package io.recruitcrm.microservice.timesheet.services.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.ClientJobResponseBodyDto;

import java.util.List;

public interface IClientJobService {

	List<ClientJobResponseBodyDto> fetchClientJobs();

}
