package io.recruitcrm.microservice.timesheet.repositories.portals;

public interface IContractorTimesheetRepository {

	Long countTimesheetEnabledForContractor(Integer contractorId, Long currentEpoch, Integer accountId);

	Long countTimesheetEnabledForContractorWithoutDateCheck(Integer contractorId, Integer accountId);

	Long countContractJobsForContractor(Integer contractorId, Integer accountId);

}
