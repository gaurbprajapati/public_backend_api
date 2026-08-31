package io.recruitcrm.microservice.timesheet.repositories.portals.client;

import io.recruitcrm.microservice.search.models.jooq.tables.ClientPortalStatusT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcontact;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateQueryResultDto;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.UInteger;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class BulkValidateRepository implements IBulkValidateRepository {

	private static final ClientPortalStatusT CPS = ClientPortalStatusT.CLIENT_PORTAL_STATUS_T;

	private static final Tblcontact CONTACT = Tblcontact.TBLCONTACT;

	private final DSLContext dslContext;

	public BulkValidateRepository(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	public List<BulkValidateQueryResultDto> findPortalStatusByEmails(List<String> emails) {
		return this.dslContext
			.select(CPS.VMS_USER_EMAIL, CPS.PORTAL_STATUS_ID, CPS.ACCOUNT_ID, CPS.INVITE_COUNT, CPS.INVITE_SENT_ON)
			.from(CPS)
			.where(CPS.VMS_USER_EMAIL.in(emails))
			.fetch()
			.stream()
			.map(this::mapRecord)
			.toList();
	}

	@Override
	public Map<Integer, Integer> findOwnerIdsByContactIds(List<Integer> contactIds) {
		if (contactIds.isEmpty()) {
			return Map.of();
		}
		return this.dslContext.select(CONTACT.ID, CONTACT.OWNERID)
			.from(CONTACT)
			.where(CONTACT.ID.in(contactIds))
			.and(CONTACT.DELETED.eq((byte) 0))
			.fetch()
			.stream()
			// Skip contacts whose ownerId is null: Collectors.toMap rejects null
			// values with an NPE. Absent entries are treated downstream as "no owner",
			// which the access-control checks already handle safely.
			.filter((rec) -> rec.get(CONTACT.OWNERID) != null)
			.collect(Collectors.toMap((rec) -> rec.get(CONTACT.ID), (rec) -> rec.get(CONTACT.OWNERID),
					(existing, replacement) -> existing));
	}

	private BulkValidateQueryResultDto mapRecord(Record rec) {
		Byte portalStatusId = rec.get(CPS.PORTAL_STATUS_ID);
		UInteger accountId = rec.get(CPS.ACCOUNT_ID);
		UInteger inviteCount = rec.get(CPS.INVITE_COUNT);
		UInteger inviteSentOn = rec.get(CPS.INVITE_SENT_ON);
		return new BulkValidateQueryResultDto(rec.get(CPS.VMS_USER_EMAIL),
				(portalStatusId != null) ? portalStatusId.intValue() : 0,
				(accountId != null) ? accountId.longValue() : 0L, (inviteCount != null) ? inviteCount.longValue() : 0L,
				(inviteSentOn != null) ? inviteSentOn.longValue() : 0L);
	}

}
