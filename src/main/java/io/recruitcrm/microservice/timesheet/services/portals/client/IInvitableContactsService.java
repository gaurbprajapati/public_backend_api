package io.recruitcrm.microservice.timesheet.services.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactsResponseBodyDto;

public interface IInvitableContactsService {

	InvitableContactsResponseBodyDto getInvitableContacts(Integer companyId, String search, Integer limit);

}
