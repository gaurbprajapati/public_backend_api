package io.recruitcrm.microservice.timesheet.services.portals.client;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Contacts;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.portal.client.ClientPortalStatusEnum;
import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.mapper.InvitableContactsMapper;
import io.recruitcrm.microservice.timesheet.repositories.portals.client.IInvitableContactsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class InvitableContactsService implements IInvitableContactsService {

	private static final int DEFAULT_LIMIT = 25;

	private final IInvitableContactsRepository invitableContactsRepository;

	private final InvitableContactsMapper invitableContactsMapper;

	private final AuthHolder auth;

	private final AccessControlHelper accessControlHelper;

	public InvitableContactsService(IInvitableContactsRepository invitableContactsRepository,
			InvitableContactsMapper invitableContactsMapper, AuthHolder auth, AccessControlHelper accessControlHelper) {
		this.invitableContactsRepository = invitableContactsRepository;
		this.invitableContactsMapper = invitableContactsMapper;
		this.auth = auth;
		this.accessControlHelper = accessControlHelper;
	}

	@Override
	public InvitableContactsResponseBodyDto getInvitableContacts(Integer companyId, String search, Integer limit) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		int effectiveLimit = (limit != null) ? limit : DEFAULT_LIMIT;
		List<InvitableContactQueryResultDto> queryResults = this.invitableContactsRepository
			.findContactsWithPortalStatus(companyId, accountId, search, effectiveLimit);
		List<InvitableContactResponseBodyDto> allContacts = this.invitableContactsMapper
			.mapToResponseDtos(queryResults);
		setAccessControlFlags(allContacts, queryResults);
		boolean allActive = computeAllActive(allContacts);
		List<InvitableContactResponseBodyDto> invitableContacts = filterNotSent(allContacts);
		return new InvitableContactsResponseBodyDto(companyId, allActive, invitableContacts);
	}

	/**
	 * Adding this here as proper access control was not implemented on company details
	 * page for contacts thus to make it similar to company details page adding access
	 * control like this Resolves canView / canEdit / canDelete per contact based on the
	 * authenticated user's role permissions and each contact's owner. Team user IDs are
	 * fetched once and reused across all three permission checks.
	 */
	private void setAccessControlFlags(List<InvitableContactResponseBodyDto> contacts,
			List<InvitableContactQueryResultDto> queryResults) {
		Contacts acl = this.accessControlHelper.getContactsAccessControl();
		Integer currentUserId = this.auth.getAuthenticationPrincipalUniqueIdentifier();

		// Fetch team user IDs once only if at least one permission level requires it
		List<Integer> teamUserIds = null;
		if (this.accessControlHelper.requiresTeamLookup(acl.getCanView())
				|| this.accessControlHelper.requiresTeamLookup(acl.getCanEdit())
				|| this.accessControlHelper.requiresTeamLookup(acl.getCanDelete())) {
			teamUserIds = this.accessControlHelper.getTeamUserIds(currentUserId);
			if (!teamUserIds.contains(currentUserId)) {
				teamUserIds.add(currentUserId);
			}
		}

		for (int i = 0; i < contacts.size(); i++) {
			Integer ownerId = queryResults.get(i).getOwnerId();
			contacts.get(i)
				.setCanView(this.accessControlHelper.resolvePermission(acl.getCanView(), ownerId, currentUserId,
						teamUserIds));
			contacts.get(i)
				.setCanEdit(this.accessControlHelper.resolvePermission(acl.getCanEdit(), ownerId, currentUserId,
						teamUserIds));
			contacts.get(i)
				.setCanDelete(this.accessControlHelper.resolvePermission(acl.getCanDelete(), ownerId, currentUserId,
						teamUserIds));
		}
	}

	private boolean computeAllActive(List<InvitableContactResponseBodyDto> contacts) {
		if (contacts.isEmpty()) {
			return false;
		}
		return contacts.stream()
			.allMatch(
					(contact) -> ClientPortalStatusEnum.PORTAL_ENABLED.getValue().equals(contact.getPortalStatusId()));
	}

	private List<InvitableContactResponseBodyDto> filterNotSent(List<InvitableContactResponseBodyDto> contacts) {
		return contacts.stream()
			.filter((contact) -> ClientPortalStatusEnum.NOT_SENT.getValue().equals(contact.getPortalStatusId()))
			.toList();
	}

}
