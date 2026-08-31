package io.recruitcrm.microservice.timesheet.repositories.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.ClientJobResponseBodyDto;

import java.util.List;

public interface IClientJobRepository {

	List<ClientJobResponseBodyDto> findJobsByEmail(String email, Integer accountId);

}
