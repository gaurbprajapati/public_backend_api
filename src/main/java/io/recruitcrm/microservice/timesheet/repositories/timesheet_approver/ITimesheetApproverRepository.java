package io.recruitcrm.microservice.timesheet.repositories.timesheet_approver;

import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;

import java.util.List;

public interface ITimesheetApproverRepository {

	TimesheetApprover createTimesheetApprover(Integer userTypeId, Integer timesheetSettingId, Integer entityId);

	List<TimesheetApprover> findByTimesheetSettingId(Integer timesheetSettingId);

	void deleteTimesheetApprovers(Integer jobId, List<Integer> contractorIds, List<Integer> agencyIds,
			Integer accountId);

	void addTimesheetApprovers(Integer jobId, List<Integer> contractorIds, List<Integer> agencyIds, Integer accountId);

	void deleteTimesheetApproversForClients(Integer jobId, List<Integer> contractorIds, List<Integer> clientIds,
			Integer accountId);

	void addTimesheetApproversForClients(Integer jobId, List<Integer> contractorIds, List<Integer> clientIds,
			Integer accountId);

	void createTimesheetApproverInBulk(List<TimesheetApprover> timesheetApprovers);

	List<TimesheetApprover> findByTimesheetSettingIds(List<Integer> timesheetSettingIds);

}