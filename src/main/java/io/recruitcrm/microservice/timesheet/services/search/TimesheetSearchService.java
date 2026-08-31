package io.recruitcrm.microservice.timesheet.services.search;

import java.util.List;

import org.jooq.CommonTableExpression;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingT;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetSettingAssociationT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAndContractorListQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.access_control.AccessControlHelper;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.ITimesheetRepository;
import io.recruitcrm.microservice.timesheet.search.cte.IFilterSearchCteProvider;
import io.recruitcrm.microservice.timesheet.search.cte.TimesheetFilterSearchCteProvider;
import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;
import org.jooq.Condition;

@Service
public class TimesheetSearchService {

	private final ITimesheetRepository timesheetRepository;

	private final DSLContext auroraDbDSLContext;

	private final AccessControlHelper accessControlHelper;

	public TimesheetSearchService(ITimesheetRepository timesheetRepository, DSLContext auroraDbDSLContext,
			AccessControlHelper accessControlHelper) {
		this.timesheetRepository = timesheetRepository;
		this.auroraDbDSLContext = auroraDbDSLContext;
		this.accessControlHelper = accessControlHelper;
	}

	public List<TimesheetJobAndContractorListQueryResultDto> searchTimesheets(FilterSearchListDto filterSearchListDto,
			List<SortPriorityRequestBodyDto> sortPriorityList, Integer accountId, String gmtDifference,
			Pageable pageable) {

		// Step 1: Build the filtered CTE and select one sorted page of timesheet IDs in
		// the database
		List<Integer> timesheetIds = this.getTimesheetIdsPage(filterSearchListDto, sortPriorityList, accountId,
				gmtDifference, pageable);

		if (timesheetIds.isEmpty()) {
			return List.of();
		}

		// Step 2: Hydrate the page. getTimesheetsListByIds re-applies the identical sort
		// to restore order over the IN (...) set (an IN list does not preserve order); it
		// does not paginate.
		return this.timesheetRepository.getTimesheetsListByIds(timesheetIds, sortPriorityList, pageable);
	}

	public Long getTimesheetsCount(FilterSearchListDto filterSearchListDto, Integer accountId, String gmtDifference) {
		IFilterSearchCteProvider cteProvider = new TimesheetFilterSearchCteProvider(accountId, gmtDifference);
		CommonTableExpression<?> cte = cteProvider.getCte(filterSearchListDto, accountId, gmtDifference);

		// Join with timesheet table and related tables to access candidate for access
		// control
		var ts = CstTimesheetT.CST_TIMESHEET_T;
		var tsSetting = CstTimesheetSettingT.CST_TIMESHEET_SETTING_T;
		var tsSettingAssoc = CstTimesheetSettingAssociationT.CST_TIMESHEET_SETTING_ASSOCIATION_T;
		var candidate = Tblcandidate.TBLCANDIDATE;
		String cteName = cteProvider.getCteName();
		Field<Integer> cteIdField = DSL.field(DSL.name(cteName, "id"), Integer.class);

		Condition accessControlCondition = this.accessControlHelper
			.buildCandidatesAccessControlCondition(candidate.OWNERID);

		// Build query to count IDs from CTE with access control condition
		Select<?> countQuery = DSL.with(cte)
			.select(DSL.count())
			.from(DSL.name(cteName))
			.innerJoin(ts)
			.on(ts.ID.eq(cteIdField))
			.leftJoin(tsSetting)
			.on(ts.TIMESHEET_SETTING_ID.eq(tsSetting.ID))
			.leftJoin(tsSettingAssoc)
			.on(tsSetting.ASSOCIATION_ID.eq(tsSettingAssoc.ID))
			.leftJoin(candidate)
			.on(candidate.ID.eq(tsSettingAssoc.CONTRACTOR_ID))
			.where(accessControlCondition);

		// Execute query and get count directly - no cast needed
		Long count = this.auroraDbDSLContext.fetchOne(countQuery).get(0, Long.class);
		return (count != null) ? count : 0L;
	}

	private List<Integer> getTimesheetIdsPage(FilterSearchListDto filterSearchListDto,
			List<SortPriorityRequestBodyDto> sortPriorityList, Integer accountId, String gmtDifference,
			Pageable pageable) {
		IFilterSearchCteProvider cteProvider = new TimesheetFilterSearchCteProvider(accountId, gmtDifference);
		CommonTableExpression<?> cte = cteProvider.getCte(filterSearchListDto, accountId, gmtDifference);
		String cteName = cteProvider.getCteName();

		return this.timesheetRepository.getTimesheetIdsPage(cte, cteName, sortPriorityList, pageable);
	}

}
