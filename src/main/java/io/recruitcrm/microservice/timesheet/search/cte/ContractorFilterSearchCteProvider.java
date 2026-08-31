package io.recruitcrm.microservice.timesheet.search.cte;

import org.jooq.CommonTableExpression;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.timesheet.search.FilterSearchASTBuilder;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group.GroupConjointNode;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;

public class ContractorFilterSearchCteProvider implements IFilterSearchCteProvider {

	private final FilterSearchASTBuilder filterSearchASTBuilder;

	private final Table<?> searchTable;

	private final Field<?> searchTableIdField;

	public ContractorFilterSearchCteProvider(Integer accountId, String gmtDifference) {
		this.filterSearchASTBuilder = new FilterSearchASTBuilder(accountId, gmtDifference);
		this.searchTable = Tblcandidate.TBLCANDIDATE;
		this.searchTableIdField = Tblcandidate.TBLCANDIDATE.ID;
	}

	@Override
	public CommonTableExpression<?> getCte(FilterSearchListDto filterSearchListDto, Integer accountId,
			String gmtDifference) {
		GroupConjointNode root = this.filterSearchASTBuilder.buildFilterASTTree(filterSearchListDto);
		return DSL.name(this.getCteName()).as(root.toSQL(this.searchTable, this.searchTableIdField));
	}

	@Override
	public String getCteName() {
		return "contractorFilterSearchCte";
	}

}
