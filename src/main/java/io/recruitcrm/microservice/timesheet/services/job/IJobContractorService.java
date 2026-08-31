package io.recruitcrm.microservice.timesheet.services.job;

import io.recruitcrm.microservice.timesheet.dto.job.TimesheetEnabledAssignedCandidateResponseBodyDto;

import java.util.List;

public interface IJobContractorService {

	List<TimesheetEnabledAssignedCandidateResponseBodyDto> getTimesheetEnabledAssignedCandidates(List<Integer> jobIds);

}
