package io.recruitcrm.microservice.timesheet.repositories.timesheet;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Job;
import io.recruitcrm.microservice.search.dto.sorting.SearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetDealListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAndContractorListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPermissionDataDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobSearchQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.CompanySearchQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeDetailSummaryDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DealSearchQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetForMigrationDto;
import org.jooq.CommonTableExpression;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ITimesheetRepository {

	List<Timesheet> createTimesheets(List<Timesheet> timesheets);

	List<TimesheetDealListQueryResultDto> getTimesheetsListByDealId(List<ContractorJobQueryResultDto> contractorJobs,
			Integer accountId, SearchRequestBodyDto searchRequestBodyDto, Pageable pageable);

	List<TimesheetJobAndContractorListQueryResultDto> getTimesheetsListByJobAndContractorId(Integer jobId,
			Integer contractorId, Integer accountId, SearchRequestBodyDto searchRequestBodyDto, Pageable pageable);

	List<TimesheetJobAndContractorListQueryResultDto> getTimesheetsListByEntityId(Integer entityType, Integer entityId,
			List<Integer> contactIds, Integer accountId, SearchRequestBodyDto searchRequestBodyDto, Pageable pageable);

	Boolean validateTimesheetsExist(List<Integer> timeLogDates, Integer accountId, Integer jobId,
			List<Integer> contractorIds);

	Boolean validateIsApprover(Integer timesheetId, Integer userId, Integer userTypeId);

	Candidate getCandidateLinkedToTimesheet(Integer timesheetId, Integer accountId);

	Job getJobLinkedToTimesheet(Integer timesheetId, Integer accountId);

	Integer getCompanyIdLinkedToTimesheet(Integer timesheetId, Integer accountId);

	/**
	 * Fetches all permission-related data for multiple timesheets in a single query This
	 * is much more efficient than individual queries for each timesheet
	 * @param timesheetIds List of timesheet IDs to fetch data for
	 * @param accountId Account ID for filtering
	 * @return List of timesheet permission data DTOs
	 */
	List<TimesheetPermissionDataDto> getTimesheetPermissionDataBulk(List<Integer> timesheetIds, Integer accountId);

	/**
	 * Fetches all permission-related data for multiple candidates in a single query
	 * @param candidateIds List of candidate IDs to fetch data for
	 * @param accountId Account ID for filtering
	 * @return List of timesheet permission data DTOs (with candidate data only)
	 */
	List<TimesheetPermissionDataDto> getCandidatePermissionDataBulk(List<Integer> candidateIds, Integer accountId);

	/**
	 * Fetches all permission-related data for multiple jobs in a single query
	 * @param jobIds List of job IDs to fetch data for
	 * @param accountId Account ID for filtering
	 * @return List of timesheet permission data DTOs (with job data only)
	 */
	List<TimesheetPermissionDataDto> getJobPermissionDataBulk(List<Integer> jobIds, Integer accountId);

	void updateTimesheetLastModified(Integer timesheetId, Integer userId, Integer userTypeId, Integer currentTimestamp);

	/**
	 * Updates the timesheet's totalTime and totalWorkTime columns. Note:
	 * {@code total_overtime} is intentionally excluded — it is computed server-side by
	 * the rule engine and written via JPA save.
	 * @param timesheetId the timesheet ID to update
	 * @param totalTime total time in seconds
	 * @param totalWorkTime total work time in seconds
	 */
	void updateTimesheetTimeDetails(Integer timesheetId, Integer totalTime, Integer totalWorkTime);

	/**
	 * Combined batch update: updates last modified metadata AND time details for multiple
	 * timesheets in a single query. Timesheets without time details still get their last
	 * modified fields updated.
	 * @param timesheetIds all timesheet IDs whose last modified fields should be updated
	 * @param userId user who made the modification
	 * @param userTypeId user type of the modifier
	 * @param currentTimestamp current UNIX timestamp
	 * @param timeDetails optional per-timesheet time details (may be null or empty)
	 */
	void batchUpdateTimesheetLastModifiedWithTimeDetails(List<Integer> timesheetIds, Integer userId, Integer userTypeId,
			Integer currentTimestamp, List<TimeDetailSummaryDto> timeDetails);

	List<ContractorJobQueryResultDto> getCommonCandidatesByDealId(Integer dealId);

	/**
	 * Fetches job IDs associated with a contact/client where VMS link is enabled
	 * @param contactId The contact/client ID
	 * @param accountId Account ID for filtering
	 * @return List of job IDs
	 */
	List<Integer> getJobIdsByContactId(Integer contactId, Integer accountId);

	/**
	 * Fetches job-contractor pairs associated with a contact/client where VMS link is
	 * enabled
	 * @param contactId The contact/client ID
	 * @param accountId Account ID for filtering
	 * @return List of job-contractor pairs
	 */
	List<ContractorJobQueryResultDto> getJobContractorPairsByContactId(Integer contactId, Integer accountId);

	/**
	 * Get total count of timesheets for an entity without any filters
	 * @param entityType The entity type (1 = Contact/Client, 3 = Contractor)
	 * @param entityId The entity ID
	 * @param accountId Account ID for filtering
	 * @return Total count of timesheets
	 */
	List<Integer> findContactIdsByEmail(String email, Integer accountId);

	Long getTimesheetsCountByEntityId(Integer entityType, Integer entityId, List<Integer> contactIds,
			Integer accountId);

	/**
	 * Get count of timesheets for an entity with filters applied
	 * @param entityType The entity type (1 = Contact/Client, 3 = Contractor)
	 * @param entityId The entity ID
	 * @param accountId Account ID for filtering
	 * @param searchRequestBodyDto Search and filter criteria
	 * @return Filtered count of timesheets
	 */
	Long getTimesheetsCountByEntityIdWithFilters(Integer entityType, Integer entityId, List<Integer> contactIds,
			Integer accountId, SearchRequestBodyDto searchRequestBodyDto);

	/**
	 * Fetches full timesheet data for given IDs with all joins and sorting
	 * @param timesheetIds List of timesheet IDs to fetch
	 * @param sortPriorityList Sort criteria
	 * @param pageable Pagination information
	 * @return List of timesheet data with all related information
	 */
	List<TimesheetJobAndContractorListQueryResultDto> getTimesheetsListByIds(List<Integer> timesheetIds,
			List<io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto> sortPriorityList,
			Pageable pageable);

	/**
	 * Selects one sorted page of timesheet IDs from the pre-filtered CTE, applying
	 * candidate access control, the requested sort and pagination in the database. Only
	 * the page's IDs are returned, so the full filtered id set is never materialised
	 * (bounding memory and the downstream {@code IN (...)} list passed to
	 * {@link #getTimesheetsListByIds}). The sort keys are the exact same shared query the
	 * hydration path uses, so the two queries order the page identically.
	 * @param cte The filtered timesheet CTE
	 * @param cteName The CTE name (its {@code id} column drives the join)
	 * @param sortPriorityList Sort criteria
	 * @param pageable Pagination information
	 * @return The IDs of the requested page, in sort order
	 */
	List<Integer> getTimesheetIdsPage(CommonTableExpression<?> cte, String cteName,
			List<io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto> sortPriorityList,
			Pageable pageable);

	/**
	 * Fetches all deals associated with timesheets. Returns all deals for the given
	 * timesheet IDs.
	 * @param timesheetIds List of timesheet IDs
	 * @param accountId Account ID for filtering
	 * @return List of deal query results with timesheet association
	 */
	List<io.recruitcrm.microservice.timesheet.dto.timesheet.DealQueryResultDto> getDealsByTimesheetIds(
			List<Integer> timesheetIds, Integer accountId);

	/**
	 * Fetches all timesheets for an account with sorting and pagination, without any
	 * filters applied.
	 * @param accountId Account ID for filtering
	 * @param sortPriorityList Sort criteria
	 * @param pageable Pagination information
	 * @return List of timesheet data with all related information
	 */
	List<TimesheetJobAndContractorListQueryResultDto> getAllTimesheetsByAccountId(Integer accountId,
			List<io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto> sortPriorityList,
			Pageable pageable);

	/**
	 * Gets the total count of all timesheets for an account without any filters applied.
	 * @param accountId Account ID for filtering
	 * @return Total count of timesheets
	 */
	Long getAllTimesheetsCountByAccountId(Integer accountId);

	/**
	 * Gets the count of timesheets where the auth user is a configured approver. Looks up
	 * the userId in cst_timesheet_approver_t as entity_id, fetches all matching
	 * timesheet_setting_ids, then counts timesheets in cst_timesheet_t.
	 * @param userId Auth user ID (matched against entity_id in cst_timesheet_approver_t)
	 * @param accountId Account ID for filtering
	 * @return Count of timesheets for which the user is a configured approver
	 */
	Long getTimesheetsCountByApproverUserId(Integer userId, Integer accountId);

	/**
	 * Fetches paginated timesheet IDs where the auth user is a configured approver. Looks
	 * up the userId in cst_timesheet_approver_t as entity_id, fetches all matching
	 * timesheet_setting_ids, then returns IDs of timesheets in cst_timesheet_t.
	 * @param userId Auth user ID (matched against entity_id in cst_timesheet_approver_t)
	 * @param accountId Account ID for filtering
	 * @param pageable Pagination information
	 * @return Paginated list of timesheet IDs
	 */
	List<Integer> getTimesheetIdsByApproverUserId(Integer userId, Integer accountId, Pageable pageable);

	/**
	 * Gets the count of timesheets that have at least one reimbursement in pending
	 * (submitted) status for the given account. Applies the same candidate access control
	 * as {@link #getTimesheetsCountByApproverUserId}.
	 * @param accountId Account ID for filtering
	 * @return Count of timesheets with pending reimbursements
	 */
	Long getTimesheetsCountWithPendingReimbursements(Integer accountId);

	/**
	 * Fetches paginated timesheet IDs that have at least one reimbursement in pending
	 * (submitted) status for the given account. Applies the same candidate access control
	 * as {@link #getTimesheetIdsByApproverUserId} before pagination.
	 * @param accountId Account ID for filtering
	 * @param pageable Pagination information
	 * @return Paginated list of timesheet IDs with pending reimbursements
	 */
	List<Integer> getTimesheetIdsWithPendingReimbursements(Integer accountId, Pageable pageable);

	/**
	 * Search jobs by account ID and job type, optionally filtered by search keyword.
	 * @param accountId Account ID for filtering
	 * @param searchKeyword Optional search keyword to filter by job name
	 * @param fromContractorsListPage Flag to conditionally apply job type filter
	 * @return List of job search results (max 6)
	 */
	List<JobSearchQueryResultDto> searchJobs(Integer accountId, String searchKeyword, Boolean fromContractorsListPage);

	/**
	 * Search companies by account ID that have jobs with contract or
	 * contract-to-permanent job types, optionally filtered by search keyword.
	 * @param accountId Account ID for filtering
	 * @param searchKeyword Optional search keyword to filter by company name
	 * @return List of company search results (max 6)
	 */
	List<CompanySearchQueryResultDto> searchCompanies(Integer accountId, String searchKeyword);

	/**
	 * Search deals by account ID that have jobs with contract or contract-to-permanent
	 * job types, optionally filtered by search keyword.
	 * @param accountId Account ID for filtering
	 * @param searchKeyword Optional search keyword to filter by deal name
	 * @return List of deal search results (max 6)
	 */
	List<DealSearchQueryResultDto> searchDeals(Integer accountId, String searchKeyword,
			Boolean fromContractorsListPage);

	/**
	 * Batch delete timesheets by IDs and account ID using JOOQ bulk delete
	 * @param ids List of timesheet IDs to delete
	 * @param accountId Account ID for security filtering
	 */
	void deleteByIdInAndAccountId(List<Integer> ids, Integer accountId);

	/**
	 * Bulk fetch timesheets by IDs and account ID using JPQL
	 * @param ids List of timesheet IDs to fetch
	 * @param accountId Account ID for filtering
	 * @return List of timesheets matching the IDs and account ID
	 */
	List<Timesheet> findByIdInAndAccountId(List<Integer> ids, Integer accountId);

	/**
	 * Fetches timesheet IDs for migration in batches (ordered by id).
	 * @param limit Maximum number of timesheet IDs to fetch
	 * @param offset Number of records to skip
	 * @return List of timesheet IDs for migration
	 */
	List<TimesheetForMigrationDto> findTimesheetsForMigration(int limit, int offset);

	/**
	 * Fetches a single timesheet for migration by ID.
	 * @param timesheetId Timesheet ID to fetch
	 * @return TimesheetForMigrationDto if found, empty otherwise
	 */
	Optional<TimesheetForMigrationDto> findTimesheetForMigrationById(Integer timesheetId);

	/**
	 * Updates total_time, total_work_time, and total_overtime columns for a timesheet.
	 * @param timesheetId Timesheet ID to update
	 * @param totalTime Total time in seconds
	 * @param totalWorkTime Total work time in seconds
	 * @param totalOvertime Total overtime in seconds
	 */
	void updateTimesheetTotalColumns(Integer timesheetId, int totalTime, int totalWorkTime, int totalOvertime);

	/**
	 * Counts how many of the given timesheet IDs actually exist for the given account in
	 * a single query.
	 * @param timesheetIds List of timesheet IDs to verify
	 * @param accountId Account ID for security filtering
	 * @return Count of timesheets that exist for the account
	 */
	Long countTimesheetsByIdsAndAccountId(List<Integer> timesheetIds, Integer accountId);

	/**
	 * Filters the given timesheet IDs to those that exist for the account and pass the
	 * same candidate-based access control as {@code getAllTimesheetsByAccountId}.
	 * @param timesheetIds List of timesheet IDs to filter
	 * @param accountId Account ID for security filtering
	 * @return IDs the caller is allowed to see for that account
	 */
	List<Integer> filterTimesheetIdsByAccountAndCandidateAccess(List<Integer> timesheetIds, Integer accountId);

}
