package io.recruitcrm.microservice.timesheet.repositories.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactQueryResultDto;

import java.util.List;

public interface IInvitableContactsRepository {

	List<InvitableContactQueryResultDto> findContactsWithPortalStatus(Integer companyId, Integer accountId,
			String search, int limit);

	/**
	 * Returns true when the contact is mapped to the company in
	 * {@code contact_company_t}.
	 * @param contactId RCRM contact ID
	 * @param companyId RCRM company ID
	 * @param accountId RCRM tenant account ID
	 * @return true when the contact belongs to the company within the account
	 */
	boolean existsContactAssignedToCompany(Integer contactId, Integer companyId, Integer accountId);

}
