package io.recruitcrm.microservice.timesheet.services.portals.client;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Contacts;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.ContactBulkValidateItemDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.ContactValidationResultDto;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.repositories.portals.client.IBulkValidateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BulkValidateService implements IBulkValidateService {

	private static final String REASON_NO_EDIT_ACCESS = "no_edit_access";

	private static final String REASON_EMAIL_MISSING = "email_missing";

	private static final String REASON_EMAIL_TAKEN = "email_taken";

	private static final String REASON_PORTAL_ACTIVE = "portal_active";

	private static final String REASON_RATE_LIMIT = "rate_limit";

	private static final int PORTAL_ACTIVE_STATUS = 2;

	private static final long RATE_LIMIT_THRESHOLD = 3L;

	private final IBulkValidateRepository bulkValidateRepository;

	private final AuthHolder auth;

	private final AccessControlHelper accessControlHelper;

	public BulkValidateService(IBulkValidateRepository bulkValidateRepository, AuthHolder auth,
			AccessControlHelper accessControlHelper) {
		this.bulkValidateRepository = bulkValidateRepository;
		this.auth = auth;
		this.accessControlHelper = accessControlHelper;
	}

	@Override
	public BulkValidateResponseBodyDto bulkValidate(BulkValidateRequestBodyDto request) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer currentUserId = this.auth.getAuthenticationPrincipalUniqueIdentifier();

		// Batch-fetch ownerIds for all contact IDs in one query
		List<Integer> contactIds = request.getContacts().stream().map(ContactBulkValidateItemDto::getId).toList();
		Map<Integer, Integer> ownerIdByContactId = this.bulkValidateRepository.findOwnerIdsByContactIds(contactIds);

		// Resolve edit-access check context once for the whole batch
		Contacts contactsAcl = this.accessControlHelper.getContactsAccessControl();
		List<Integer> teamUserIds = resolveTeamUserIds(contactsAcl, currentUserId);

		List<String> emails = extractNonNullEmails(request.getContacts());
		Map<String, List<BulkValidateQueryResultDto>> rowsByEmail = fetchRowsByEmail(emails);
		List<ContactValidationResultDto> results = buildResults(request.getContacts(), accountId, rowsByEmail,
				ownerIdByContactId, contactsAcl, currentUserId, teamUserIds);
		return buildResponse(results);
	}

	private List<String> extractNonNullEmails(List<ContactBulkValidateItemDto> contacts) {
		return contacts.stream().map(ContactBulkValidateItemDto::getEmail).filter(Objects::nonNull).distinct().toList();
	}

	private Map<String, List<BulkValidateQueryResultDto>> fetchRowsByEmail(List<String> emails) {
		if (emails.isEmpty()) {
			return Map.of();
		}
		return this.bulkValidateRepository.findPortalStatusByEmails(emails)
			.stream()
			.collect(Collectors.groupingBy(BulkValidateQueryResultDto::getVmsUserEmail));
	}

	private List<ContactValidationResultDto> buildResults(List<ContactBulkValidateItemDto> contacts, Integer accountId,
			Map<String, List<BulkValidateQueryResultDto>> rowsByEmail, Map<Integer, Integer> ownerIdByContactId,
			Contacts contactsAcl, Integer currentUserId, List<Integer> teamUserIds) {
		return contacts.stream()
			.map((contact) -> validateContact(contact, accountId, rowsByEmail, ownerIdByContactId, contactsAcl,
					currentUserId, teamUserIds))
			.toList();
	}

	private ContactValidationResultDto validateContact(ContactBulkValidateItemDto contact, Integer accountId,
			Map<String, List<BulkValidateQueryResultDto>> rowsByEmail, Map<Integer, Integer> ownerIdByContactId,
			Contacts contactsAcl, Integer currentUserId, List<Integer> teamUserIds) {
		// no_edit_access is top-priority — checked before all other validations
		Integer ownerId = ownerIdByContactId.get(contact.getId());
		if (!this.accessControlHelper.resolvePermission(contactsAcl.getCanEdit(), ownerId, currentUserId,
				teamUserIds)) {
			return new ContactValidationResultDto(contact.getId(), contact.getEmail(), false, REASON_NO_EDIT_ACCESS);
		}
		if (contact.getEmail() == null) {
			return new ContactValidationResultDto(contact.getId(), null, false, REASON_EMAIL_MISSING);
		}
		List<BulkValidateQueryResultDto> emailRows = rowsByEmail.getOrDefault(contact.getEmail(), List.of());
		return validateWithRows(contact, accountId, emailRows);
	}

	private ContactValidationResultDto validateWithRows(ContactBulkValidateItemDto contact, Integer accountId,
			List<BulkValidateQueryResultDto> emailRows) {
		for (BulkValidateQueryResultDto row : emailRows) {
			if (isEmailTaken(row, accountId)) {
				return new ContactValidationResultDto(contact.getId(), contact.getEmail(), false, REASON_EMAIL_TAKEN);
			}
			if (isPortalActive(row)) {
				return new ContactValidationResultDto(contact.getId(), contact.getEmail(), false, REASON_PORTAL_ACTIVE);
			}
			if (isRateLimited(row)) {
				return new ContactValidationResultDto(contact.getId(), contact.getEmail(), false, REASON_RATE_LIMIT);
			}
		}
		return new ContactValidationResultDto(contact.getId(), contact.getEmail(), true, null);
	}

	private boolean isEmailTaken(BulkValidateQueryResultDto row, Integer accountId) {
		return row.getAccountId() != accountId.longValue();
	}

	private boolean isPortalActive(BulkValidateQueryResultDto row) {
		return row.getPortalStatusId() == PORTAL_ACTIVE_STATUS;
	}

	private boolean isRateLimited(BulkValidateQueryResultDto row) {
		long startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		return row.getInviteCount() >= RATE_LIMIT_THRESHOLD && row.getInviteSentOn() >= startOfToday;
	}

	private BulkValidateResponseBodyDto buildResponse(List<ContactValidationResultDto> results) {
		int validCount = (int) results.stream().filter(ContactValidationResultDto::isValid).count();
		int invalidCount = results.size() - validCount;
		return new BulkValidateResponseBodyDto(results, validCount, invalidCount);
	}

	private List<Integer> resolveTeamUserIds(Contacts contactsAcl, Integer currentUserId) {
		if (!this.accessControlHelper.requiresTeamLookup(contactsAcl.getCanEdit())) {
			return List.of();
		}
		List<Integer> teamUserIds = this.accessControlHelper.getTeamUserIds(currentUserId);
		if (!teamUserIds.contains(currentUserId)) {
			teamUserIds.add(currentUserId);
		}
		return teamUserIds;
	}

}
