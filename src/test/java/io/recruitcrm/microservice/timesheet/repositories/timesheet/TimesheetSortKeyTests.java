/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.timesheet;

import static org.assertj.core.api.Assertions.assertThat;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the pay-status / bill-status / expense-claim sort-key builders in
 * {@link TimesheetRepository}. These columns don't sort meaningfully by their raw stored
 * value, so each builder emits a CASE rank. The tests render real SQL (no DB connection)
 * and assert the rank mapping that drives the product-defined order:
 * <ul>
 * <li>pay/bill status: not available &rarr; unpaid/unbilled &rarr; paid/billed (&rarr;
 * collected)</li>
 * <li>expense claim: not available &rarr; ascending claim count</li>
 * </ul>
 */
class TimesheetSortKeyTests {

	private final DSLContext dsl = DSL.using(SQLDialect.MYSQL);

	private String renderLower(Field<?> field) {
		// Render inside a SELECT so the full aliased expression (the CASE definition) is
		// emitted; rendering an aliased field standalone would print only its alias name.
		return this.dsl.renderInlined(this.dsl.select(field)).toLowerCase();
	}

	@Test
	@DisplayName("Pay status sort key ranks: not approved=0, UNPAID(null/2)=1, PAID(1)=2")
	void testPayStatusSortKeyOrder() {
		String sql = this.renderLower(TimesheetRepository.buildPayStatusSortField());

		// the grid shows "Not available" until the timesheet is approved, so non-approved
		// rows (gated on the latest approval status) sort first
		assertThat(sql).contains("case")
			.contains("cst_timesheet_approval_t")
			.contains("then 0")
			// approved with no invoice row defaults to UNPAID -> rank 1
			.contains("is null then 1")
			// UN_PAID(id=2) -> 1, PAID(id=1) -> 2
			.contains("= 2 then 1")
			.contains("= 1 then 2")
			// exposed under the "payStatus" alias the ORDER BY resolves against
			.contains("paystatus");
	}

	@Test
	@DisplayName("Bill status sort key ranks: not approved=0, UNBILLED(null/2)=1, BILLED(1)=2, COLLECTED(3)=3")
	void testBillStatusSortKeyOrder() {
		String sql = this.renderLower(TimesheetRepository.buildBillStatusSortField());

		// non-approved rows render "Not available" and sort first
		assertThat(sql).contains("case")
			.contains("cst_timesheet_approval_t")
			.contains("then 0")
			// approved with no invoice row defaults to UNBILLED -> rank 1
			.contains("is null then 1")
			// UN_BILLED(2) -> 1, BILLED(1) -> 2, COLLECTED(3) -> 3
			.contains("= 2 then 1")
			.contains("= 1 then 2")
			.contains("= 3 then 3")
			.contains("billstatus");
	}

	@Test
	@DisplayName("Expense claim sort key: not available rows rank -1, enabled rows rank by claim count")
	void testExpenseClaimSortKeyOrder() {
		String sql = this.renderLower(TimesheetRepository.buildExpenseClaimSortField());

		// reimbursement-disabled / not-available rows sort before any enabled row
		assertThat(sql).contains("case")
			.contains("else -1")
			// enabled rows rank by their reimbursement count
			.contains("count(*)")
			.contains("is_reimbursement_enabled")
			.contains("expenseclaim");
	}

}
