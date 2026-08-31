package io.recruitcrm.microservice.timesheet.services.search;

import java.util.List;

import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.repositories.contractor.IContractorRepository;
import io.recruitcrm.microservice.timesheet.search.cte.ContractorFilterSearchCteProvider;
import io.recruitcrm.microservice.timesheet.search.cte.IFilterSearchCteProvider;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;

@Service
public class ContractorSearchService {

	private final IContractorRepository contractorRepository;

	private final DSLContext auroraDbDSLContext;

	private final AccessControlHelper accessControlHelper;

	public ContractorSearchService(IContractorRepository contractorRepository, DSLContext auroraDbDSLContext,
			AccessControlHelper accessControlHelper) {
		this.contractorRepository = contractorRepository;
		this.auroraDbDSLContext = auroraDbDSLContext;
		this.accessControlHelper = accessControlHelper;
	}

	public List<ContractorQueryResultDto> searchContractors(FilterSearchListDto filterSearchListDto,
			List<SortPriorityRequestBodyDto> sortPriorityList, Integer accountId, String gmtDifference,
			Pageable pageable) {

		// Step 1: Build the filtered CTE and select one sorted page of contractor IDs in
		// the database.
		List<Integer> contractorIds = this.getContractorIdsPage(filterSearchListDto, sortPriorityList, accountId,
				gmtDifference, pageable);

		if (contractorIds.isEmpty()) {
			return List.of();
		}

		// Step 2: Hydrate the page. getContractorsListByIds re-applies the identical sort
		// to restore order over the IN (...) set (an IN list does not preserve order); it
		// does not paginate.
		return this.contractorRepository.getContractorsListByIds(contractorIds, sortPriorityList, accountId);
	}

	private List<Integer> getContractorIdsPage(FilterSearchListDto filterSearchListDto,
			List<SortPriorityRequestBodyDto> sortPriorityList, Integer accountId, String gmtDifference,
			Pageable pageable) {
		IFilterSearchCteProvider cteProvider = new ContractorFilterSearchCteProvider(accountId, gmtDifference);
		CommonTableExpression<?> cte = cteProvider.getCte(filterSearchListDto, accountId, gmtDifference);
		String cteName = cteProvider.getCteName();

		return this.contractorRepository.getContractorIdsPage(cte, cteName, sortPriorityList, accountId, pageable);
	}

	public Long getContractorsCount(FilterSearchListDto filterSearchListDto, Integer accountId, String gmtDifference) {
		IFilterSearchCteProvider cteProvider = new ContractorFilterSearchCteProvider(accountId, gmtDifference);
		CommonTableExpression<?> cte = cteProvider.getCte(filterSearchListDto, accountId, gmtDifference);

		// Build query to count IDs from CTE with access control
		var candidate = Tblcandidate.TBLCANDIDATE;
		String cteName = cteProvider.getCteName();
		Field<Integer> cteIdField = DSL.field(DSL.name(cteName, "id"), Integer.class);

		Condition accessControlCondition = this.accessControlHelper
			.buildCandidatesAccessControlCondition(candidate.OWNERID);

		Select<?> countQuery = DSL.with(cte)
			.select(DSL.count())
			.from(DSL.name(cteName))
			.innerJoin(candidate)
			.on(candidate.ID.eq(cteIdField))
			.where(accessControlCondition);

		// Execute query and get count directly - no cast needed
		Long count = this.auroraDbDSLContext.fetchOne(countQuery).get(0, Long.class);
		return (count != null) ? count : 0L;
	}

}
