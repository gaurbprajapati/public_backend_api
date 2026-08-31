package io.recruitcrm.microservice.timesheet.repositories.portals.client;

import io.recruitcrm.microservice.search.models.jooq.tables.CstJobTimesheetAccessT;
import io.recruitcrm.microservice.search.models.jooq.tables.JobSecondaryContactsT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcompany;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcontact;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbljob;
import io.recruitcrm.microservice.timesheet.dto.portal.client.ClientJobResponseBodyDto;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClientJobRepository implements IClientJobRepository {

	private static final Tbljob JOB = Tbljob.TBLJOB;

	private static final Tblcompany COMPANY = Tblcompany.TBLCOMPANY;

	private static final Tblcontact CONTACT = Tblcontact.TBLCONTACT;

	private static final JobSecondaryContactsT SEC = JobSecondaryContactsT.JOB_SECONDARY_CONTACTS_T;

	private static final CstJobTimesheetAccessT ACCESS = CstJobTimesheetAccessT.CST_JOB_TIMESHEET_ACCESS_T;

	private static final String CONTRACT = "contract";

	private static final String CONTRACT_TO_PERMANENT = "contracttopermanent";

	private static final String ROLE_PRIMARY = "primary";

	private static final String ROLE_SECONDARY = "secondary";

	private final DSLContext dslContext;

	public ClientJobRepository(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	public List<ClientJobResponseBodyDto> findJobsByEmail(String email, Integer accountId) {
		return this.dslContext
			.select(JOB.ID.as("jobId"), JOB.NAME.as("jobTitle"), JOB.JOB_TYPE.as("jobType"),
					COMPANY.COMPANYNAME.as("companyName"), CONTACT.EMAIL.as("contactEmail"),
					DSL.when(DSL.max(DSL.when(JOB.CONTACTID.eq(CONTACT.ID), DSL.inline(1)).otherwise(DSL.inline(0)))
						.eq(1), ROLE_PRIMARY).otherwise(ROLE_SECONDARY).as("contactRole"),
					DSL.coalesce(DSL.max(ACCESS.CAN_CREATE), DSL.inline((byte) 0)).as("createAccess"))
			.from(JOB)
			.join(COMPANY)
			.on(COMPANY.ID.eq(JOB.COMPANYID))
			.join(CONTACT)
			.on(CONTACT.ACCOUNTID.eq(JOB.ACCOUNTID).and(CONTACT.EMAIL.eq(email)).and(CONTACT.DELETED.eq((byte) 0)))
			.leftJoin(SEC)
			.on(SEC.JOB_ID.eq(JOB.ID).and(SEC.CONTACT_ID.eq(CONTACT.ID)))
			.leftJoin(ACCESS)
			.on(ACCESS.JOB_ID.eq(JOB.ID.coerce(UInteger.class)))
			.where(JOB.ACCOUNTID.eq(accountId))
			.and(JOB.DELETED.eq((byte) 0))
			.and(JOB.AUTHID.isNotNull())
			.and(JOB.AUTHID.ne(""))
			.and(JOB.JOB_TYPE.in(CONTRACT, CONTRACT_TO_PERMANENT))
			.and(JOB.CONTACTID.eq(CONTACT.ID).or(SEC.CONTACT_ID.isNotNull()))
			.groupBy(JOB.ID, JOB.NAME, JOB.JOB_TYPE, COMPANY.COMPANYNAME, CONTACT.EMAIL)
			.fetchInto(ClientJobResponseBodyDto.class);
	}

}
