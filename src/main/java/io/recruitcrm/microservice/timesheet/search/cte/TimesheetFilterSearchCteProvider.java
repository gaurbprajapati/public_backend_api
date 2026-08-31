package io.recruitcrm.microservice.timesheet.search.cte;

import org.jooq.CommonTableExpression;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.timesheet.search.FilterSearchASTBuilder;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group.GroupConjointNode;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;

public class TimesheetFilterSearchCteProvider implements IFilterSearchCteProvider {

	private final FilterSearchASTBuilder filterSearchASTBuilder;

	private final Table<?> searchTable;

	private final Field<?> searchTableIdField;

	public TimesheetFilterSearchCteProvider(Integer accountId, String gmtDifference) {
		this.filterSearchASTBuilder = new FilterSearchASTBuilder(accountId, gmtDifference);
		this.searchTable = CstTimesheetT.CST_TIMESHEET_T;
		this.searchTableIdField = CstTimesheetT.CST_TIMESHEET_T.ID;
	}

	@Override
	public CommonTableExpression<?> getCte(FilterSearchListDto filterSearchListDto, Integer accountId,
			String gmtDifference) {
		GroupConjointNode root = this.filterSearchASTBuilder.buildFilterASTTree(filterSearchListDto);
		return DSL.name(this.getCteName()).as(root.toSQL(this.searchTable, this.searchTableIdField));
	}

	@Override
	public String getCteName() {
		return "filterSearchCte";
	}

}
