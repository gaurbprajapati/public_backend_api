package io.recruitcrm.microservice.timesheet.repositories.portals.client;

import io.recruitcrm.microservice.search.models.jooq.tables.ClientPortalStatusT;
import io.recruitcrm.microservice.search.models.jooq.tables.ContactCompanyT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcompany;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcontact;
import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactQueryResultDto;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

import org.jooq.types.UInteger;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class InvitableContactsRepository implements IInvitableContactsRepository {

	private static final ContactCompanyT CC = ContactCompanyT.CONTACT_COMPANY_T;

	private static final Tblcontact CONTACT = Tblcontact.TBLCONTACT;

	private static final ClientPortalStatusT CPS = ClientPortalStatusT.CLIENT_PORTAL_STATUS_T;

	private static final Tblcompany COMPANY = Tblcompany.TBLCOMPANY;

	private static final String PORTAL_STATUS_ID_ALIAS = "portalStatusId";

	private final DSLContext dslContext;

	public InvitableContactsRepository(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	public boolean existsContactAssignedToCompany(Integer contactId, Integer companyId, Integer accountId) {
		return this.dslContext.fetchExists(this.dslContext.selectOne()
			.from(CC)
			.join(CONTACT)
			.on(CONTACT.ID.eq(CC.CONTACT_ID).and(CONTACT.ACCOUNTID.eq(accountId)).and(CONTACT.DELETED.eq((byte) 0)))
			.where(CC.CONTACT_ID.eq(contactId).and(CC.COMPANY_ID.eq(companyId))));
	}

	@Override
	public List<InvitableContactQueryResultDto> findContactsWithPortalStatus(Integer companyId, Integer accountId,
			String search, int limit) {
		Condition condition = CC.COMPANY_ID.eq(companyId);
		if (StringUtils.hasText(search)) {
			condition = condition.and(buildSearchCondition(search.trim()));
		}
		return this.dslContext
			.selectDistinct(CONTACT.ID, CONTACT.FIRSTNAME, CONTACT.LASTNAME, CONTACT.EMAIL, CONTACT.PHOTO, CONTACT.SRNO,
					CONTACT.OWNERID, COMPANY.COMPANYNAME,
					DSL.coalesce(CPS.PORTAL_STATUS_ID, DSL.val((byte) 0)).as(PORTAL_STATUS_ID_ALIAS))
			.from(CC)
			.join(CONTACT)
			.on(CONTACT.ID.eq(CC.CONTACT_ID).and(CONTACT.ACCOUNTID.eq(accountId)).and(CONTACT.DELETED.eq((byte) 0)))
			.leftJoin(CPS)
			.on(CPS.VMS_USER_EMAIL.eq(CONTACT.EMAIL).and(CPS.ACCOUNT_ID.eq(UInteger.valueOf(accountId))))
			.leftJoin(COMPANY)
			.on(COMPANY.ID.eq(CC.COMPANY_ID))
			.where(condition)
			.orderBy(CONTACT.UPDATEDBY.desc(), CONTACT.ID.desc())
			.limit(limit)
			.fetch()
			.stream()
			.map(this::mapRecord)
			.toList();
	}

	/**
	 * Mirrors the legacy PHP contact search: each whitespace-separated token must match
	 * first name or last name (tokens are AND-ed together), OR-ed with a whole-phrase
	 * match against email or contact reference number.
	 */
	private Condition buildSearchCondition(String search) {
		Condition nameCondition = null;
		for (String token : search.split("\\s+")) {
			if (token.isBlank()) {
				continue;
			}
			String tokenPattern = toLikePattern(token);
			Condition tokenCondition = CONTACT.FIRSTNAME.likeIgnoreCase(tokenPattern)
				.or(CONTACT.LASTNAME.likeIgnoreCase(tokenPattern));
			nameCondition = (nameCondition != null) ? nameCondition.and(tokenCondition) : tokenCondition;
		}

		String fullPattern = toLikePattern(search);
		Condition otherFieldsCondition = CONTACT.EMAIL.likeIgnoreCase(fullPattern)
			.or(DSL.cast(CONTACT.SRNO, String.class).likeIgnoreCase(fullPattern));
		return (nameCondition != null) ? nameCondition.or(otherFieldsCondition) : otherFieldsCondition;
	}

	/**
	 * Escapes LIKE metacharacters ({@code \}, {@code %}, {@code _}) so literal
	 * occurrences in user input aren't treated as wildcards, then wraps the token for a
	 * substring match.
	 */
	private static String toLikePattern(String token) {
		String escaped = token.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		return "%" + escaped + "%";
	}

	private InvitableContactQueryResultDto mapRecord(Record rec) {
		Byte portalStatus = rec.get(PORTAL_STATUS_ID_ALIAS, Byte.class);
		return new InvitableContactQueryResultDto(rec.get(CONTACT.ID), rec.get(CONTACT.FIRSTNAME),
				rec.get(CONTACT.LASTNAME), rec.get(CONTACT.EMAIL), (portalStatus != null) ? (int) portalStatus : 0,
				rec.get(CONTACT.PHOTO), rec.get(CONTACT.SRNO), rec.get(COMPANY.COMPANYNAME), rec.get(CONTACT.OWNERID));
	}

}
