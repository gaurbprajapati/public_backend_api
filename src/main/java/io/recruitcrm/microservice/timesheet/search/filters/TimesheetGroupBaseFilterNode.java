package io.recruitcrm.microservice.timesheet.search.filters;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetInvoiceT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcompany;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcurrency;
import io.recruitcrm.microservice.search.models.jooq.tables.Tbljob;
import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;

public abstract class TimesheetGroupBaseFilterNode extends BaseFilterNode {

	protected static final CstTimesheetT TS = CstTimesheetT.CST_TIMESHEET_T;

	protected static final CstTimesheetSettingT TS_SETTING = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;

	protected static final CstTimesheetSettingAssociationT TS_SETTING_ASSOC = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;

	protected static final Tblcandidate CANDIDATE = Tblcandidate.TBLCANDIDATE;

	protected static final Tbljob JOB = Tbljob.TBLJOB;

	protected static final Tblcompany COMPANY = Tblcompany.TBLCOMPANY;

	protected static final Tblcurrency PAY_CURRENCY = Tblcurrency.TBLCURRENCY.as("pay_currency");

	protected static final Tblcurrency BILL_CURRENCY = Tblcurrency.TBLCURRENCY.as("bill_currency");

	protected static final CstTimesheetInvoiceT TIMESHEET_INVOICE = CstTimesheetInvoiceT.CST_TIMESHEET_INVOICE_T;

	protected static final Table<?> INVOICE = DSL.table("invoice_t").as("invoice");

	protected TimesheetGroupBaseFilterNode(FilterNodeContext filterContext) {
		super(filterContext);
	}

	@Override
	public Table<?> getBaseTable() {
		return TS;
	}

	@Override
	public List<Field<?>> getSelectFields() {
		return List.of(TS.ID);
	}

	@Override
	public Condition getAccountIdFilterCondition() {
		return TS_SETTING.ACCOUNT_ID.eq(this.filterNodeContext.getAccountId());
	}

	@Override
	public List<TableJoinSpecification> getJoinTables() {
		// Default implementation returns all joins
		// Subclasses should override this to return only necessary joins
		return this.getAllJoins();
	}

	/**
	 * Returns all possible joins. Use this when you need all tables joined.
	 * @return List of all table join specifications
	 */
	protected List<TableJoinSpecification> getAllJoins() {
		List<TableJoinSpecification> joins = new ArrayList<>();

		// Required join for account_id filter (always needed)
		joins
			.add(new TableJoinSpecification(TableJoinType.LEFT, TS_SETTING, TS.TIMESHEET_SETTING_ID.eq(TS_SETTING.ID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, TS_SETTING_ASSOC,
				TS_SETTING.ASSOCIATION_ID.eq(TS_SETTING_ASSOC.ID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, CANDIDATE,
				CANDIDATE.ID.eq(TS_SETTING_ASSOC.CONTRACTOR_ID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, JOB, JOB.ID.eq(TS_SETTING_ASSOC.JOB_ID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, COMPANY,
				DSL.field("company.id", Integer.class).eq(JOB.COMPANYID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, PAY_CURRENCY,
				PAY_CURRENCY.ID.eq(TS_SETTING.PAY_CURRENCY_ID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, BILL_CURRENCY,
				BILL_CURRENCY.ID.eq(TS_SETTING.BILL_CURRENCY_ID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, TIMESHEET_INVOICE,
				TIMESHEET_INVOICE.CST_TIMESHEET_ID.eq(TS.ID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, INVOICE,
				DSL.field("invoice.id", Integer.class).eq(TIMESHEET_INVOICE.INVOICE_ID)));

		return joins;
	}

	/**
	 * Returns minimal joins needed for account_id filter (only cst_timesheet_setting_t).
	 * Use this for fields that are directly on cst_timesheet_t table.
	 * @return List with only the timesheet_setting join
	 */
	protected List<TableJoinSpecification> getMinimalJoins() {
		List<TableJoinSpecification> joins = new ArrayList<>();
		// Only join needed for account_id filter
		joins
			.add(new TableJoinSpecification(TableJoinType.LEFT, TS_SETTING, TS.TIMESHEET_SETTING_ID.eq(TS_SETTING.ID)));
		return joins;
	}

	/**
	 * Returns joins needed for timesheet_setting fields. Includes:
	 * cst_timesheet_setting_t
	 * @return List with timesheet_setting join
	 */
	protected List<TableJoinSpecification> getTimesheetSettingJoins() {
		return this.getMinimalJoins();
	}

	/**
	 * Returns joins needed for contractor/candidate fields. Includes:
	 * cst_timesheet_setting_t, cst_timesheet_setting_association_t, tblcandidate
	 * @return List with joins up to candidate table
	 */
	protected List<TableJoinSpecification> getContractorJoins() {
		List<TableJoinSpecification> joins = this.getMinimalJoins();
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, TS_SETTING_ASSOC,
				TS_SETTING.ASSOCIATION_ID.eq(TS_SETTING_ASSOC.ID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, CANDIDATE,
				CANDIDATE.ID.eq(TS_SETTING_ASSOC.CONTRACTOR_ID)));
		return joins;
	}

	/**
	 * Returns joins needed for job fields. Includes: cst_timesheet_setting_t,
	 * cst_timesheet_setting_association_t, tbljob
	 * @return List with joins up to job table
	 */
	protected List<TableJoinSpecification> getJobJoins() {
		List<TableJoinSpecification> joins = this.getMinimalJoins();
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, TS_SETTING_ASSOC,
				TS_SETTING.ASSOCIATION_ID.eq(TS_SETTING_ASSOC.ID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, JOB, JOB.ID.eq(TS_SETTING_ASSOC.JOB_ID)));
		return joins;
	}

	/**
	 * Returns joins needed for company fields. Includes: cst_timesheet_setting_t,
	 * cst_timesheet_setting_association_t, tbljob, tblcompany
	 * @return List with joins up to company table
	 */
	protected List<TableJoinSpecification> getCompanyJoins() {
		List<TableJoinSpecification> joins = this.getJobJoins();
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, COMPANY, Tblcompany.TBLCOMPANY.ID.eq(JOB.COMPANYID)));
		return joins;
	}

	/**
	 * Returns joins needed for currency fields. Includes: cst_timesheet_setting_t,
	 * pay_currency, bill_currency
	 * @return List with joins including currency tables
	 */
	protected List<TableJoinSpecification> getCurrencyJoins() {
		List<TableJoinSpecification> joins = this.getMinimalJoins();
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, PAY_CURRENCY,
				PAY_CURRENCY.ID.eq(TS_SETTING.PAY_CURRENCY_ID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, BILL_CURRENCY,
				BILL_CURRENCY.ID.eq(TS_SETTING.BILL_CURRENCY_ID)));
		return joins;
	}

	/**
	 * Returns joins needed for invoice fields. Includes: cst_timesheet_setting_t,
	 * cst_timesheet_invoice_t, invoice_t
	 * @return List with joins including invoice tables
	 */
	protected List<TableJoinSpecification> getInvoiceJoins() {
		List<TableJoinSpecification> joins = this.getMinimalJoins();
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, TIMESHEET_INVOICE,
				TIMESHEET_INVOICE.CST_TIMESHEET_ID.eq(TS.ID)));
		joins.add(new TableJoinSpecification(TableJoinType.LEFT, INVOICE,
				DSL.field("invoice.id", Integer.class).eq(TIMESHEET_INVOICE.INVOICE_ID)));
		return joins;
	}

}
