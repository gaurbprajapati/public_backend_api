package io.recruitcrm.microservice.timesheet.repositories.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateQueryResultDto;

import java.util.List;
import java.util.Map;

public interface IBulkValidateRepository {

	List<BulkValidateQueryResultDto> findPortalStatusByEmails(List<String> emails);

	Map<Integer, Integer> findOwnerIdsByContactIds(List<Integer> contactIds);

}
