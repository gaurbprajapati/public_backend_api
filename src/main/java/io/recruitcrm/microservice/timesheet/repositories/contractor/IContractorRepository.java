package io.recruitcrm.microservice.timesheet.repositories.contractor;

import java.util.List;

import org.jooq.CommonTableExpression;
import org.springframework.data.domain.Pageable;

import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorDealQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto;

public interface IContractorRepository {

	/**
	 * Selects one sorted page of contractor IDs from the pre-filtered CTE, applying
	 * access control, the requested sort and pagination in the database. Only the page's
	 * IDs are returned, so the full filtered id set is never materialised (bounding
	 * memory and the downstream {@code IN (...)} list passed to
	 * {@link #getContractorsListByIds}). The sort keys are built from the same shared
	 * expressions the data query uses, so the two queries order the page identically.
	 * @param cte The filtered contractor CTE
	 * @param cteName The CTE name (its {@code id} column drives the join)
	 * @param sortPriorityList Sort criteria
	 * @param accountId Account ID used to resolve the computed sort keys
	 * @param pageable Pagination information
	 * @return The IDs of the requested page, in sort order
	 */
	List<Integer> getContractorIdsPage(CommonTableExpression<?> cte, String cteName,
			List<SortPriorityRequestBodyDto> sortPriorityList, Integer accountId, Pageable pageable);

	/**
	 * Fetches full contractor data for an already-paginated page of IDs with all joins
	 * and sorting. The {@code accountId} is required so the Status / Job Name / Deal Name
	 * sort keys are computed from the same account-scoped, active-assignment values the
	 * grid displays (matching {@link #getAllContractorsByAccountId}). This method does
	 * NOT paginate: {@link #getContractorIdsPage} has already selected the page; the sort
	 * here only restores order over the {@code IN (...)} set.
	 * @param contractorIds The page of contractor IDs to fetch
	 * @param sortPriorityList Sort criteria
	 * @param accountId Account ID used to resolve the computed sort keys
	 * @return List of contractor data with all related information
	 */
	List<ContractorQueryResultDto> getContractorsListByIds(List<Integer> contractorIds,
			List<SortPriorityRequestBodyDto> sortPriorityList, Integer accountId);

	/**
	 * Fetches all jobs associated with contractors. Returns all jobs for the given
	 * contractor IDs.
	 * @param contractorIds List of contractor IDs
	 * @param accountId Account ID for filtering
	 * @return List of contractor job query results
	 */
	List<ContractorJobQueryResultDto> getJobsByContractorIds(List<Integer> contractorIds, Integer accountId);

	/**
	 * Fetches all deals associated with contractors. Returns all deals for the given
	 * contractor IDs.
	 * @param contractorIds List of contractor IDs
	 * @param accountId Account ID for filtering
	 * @return List of contractor deal query results
	 */
	List<ContractorDealQueryResultDto> getDealsByContractorIds(List<Integer> contractorIds, Integer accountId);

	/**
	 * Fetches all contractors for an account with sorting and pagination, without any
	 * filters applied.
	 * @param accountId Account ID for filtering
	 * @param sortPriorityList Sort criteria
	 * @param pageable Pagination information
	 * @return List of contractor data with all related information
	 */
	List<ContractorQueryResultDto> getAllContractorsByAccountId(Integer accountId,
			List<SortPriorityRequestBodyDto> sortPriorityList, Pageable pageable);

	/**
	 * Gets the total count of all contractors for an account without any filters applied.
	 * @param accountId Account ID for filtering
	 * @return Total count of contractors
	 */
	Long getAllContractorsCountByAccountId(Integer accountId);

}
