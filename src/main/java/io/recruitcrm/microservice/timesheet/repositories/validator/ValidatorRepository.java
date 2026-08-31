package io.recruitcrm.microservice.timesheet.repositories.validator;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetValidatorQueryResultDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ValidatorRepository {

	private final EntityManager entityManager;

	public ValidatorRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Timesheet getPrimaryTimesheetById(Integer primaryTimesheetId) {
		String jpql = "SELECT t FROM Timesheet t WHERE t.id = :primaryTimesheetId";
		TypedQuery<Timesheet> query = this.entityManager.createQuery(jpql, Timesheet.class);
		query.setParameter("primaryTimesheetId", primaryTimesheetId);
		List<Timesheet> results = query.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

	public List<TimesheetAndSettingValidatorQueryResultDto> validateTimeLogsBeforeUpdate(List<Integer> timesheetIds) {
		String jpql = "SELECT tsa.contractorId, " + "       tsa.jobId, " + "       ts.workLogType, "
				+ "       t.periodStart, " + "       t.periodEnd, "
				+ "       CONCAT(c.firstName, ' ', c.lastName) AS fullName, " + "       c.profilePic, "
				+ "       j.name, " + "       ts.id, " + "       t.id, " + "       cm.logo, "
				+ "       COALESCE(MAX(ta.timesheetApprovalStatusTypeId), NULL) AS timesheetApprovalStatusTypeId, "
				+ "       ts.calculateBreakTime, " + "       ts.templateWorkDay, " + "c.serialNo   "
				+ "FROM TimesheetSetting ts "
				+ "LEFT JOIN TimesheetSettingAssociation tsa ON ts.association.id = tsa.id "
				+ "LEFT JOIN Timesheet t ON t.timesheetSettingId = ts.id "
				+ "LEFT JOIN Candidate c ON c.id = tsa.contractorId " + "LEFT JOIN Job j ON j.id = tsa.jobId "
				+ "LEFT JOIN Company cm ON cm.id = j.company.id "
				+ "LEFT JOIN TimesheetApproval ta ON ta.timesheetId = t.id " + "WHERE t.id IN :timesheetIds "
				+ "GROUP BY tsa.contractorId, tsa.jobId, ts.workLogType, t.periodStart, t.periodEnd, c.firstName, c.lastName, c.profilePic, j.name, ts.id, t.id, cm.logo, ts.calculateBreakTime, ts.templateWorkDay, c.serialNo";
		TypedQuery<TimesheetAndSettingValidatorQueryResultDto> query = this.entityManager.createQuery(jpql,
				TimesheetAndSettingValidatorQueryResultDto.class);
		query.setParameter("timesheetIds", timesheetIds);
		return query.getResultList();
	}

	// need to check below are duplicating are what
	public List<ContractorTimesheetValidatorQueryResultDto> validateContractorTimeLogsBeforeUpdate(
			List<Integer> timesheetIds) {
		String jpql = "SELECT new io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetValidatorQueryResultDto("
				+ "t.id, " // timesheetId
				+ "ts.id, " // timesheetSettingId
				+ "ts.workLogType, " // workTimeType
				+ "ts.calculateBreakTime, " // calculateBreakTime
				+ "ts.templateWorkDay " // templateWorkDays
				+ ") " + "FROM TimesheetSetting ts "
				+ "LEFT JOIN TimesheetSettingAssociation tsa ON ts.association.id = tsa.id "
				+ "LEFT JOIN Timesheet t ON t.timesheetSettingId = ts.id " + "WHERE t.id IN :timesheetIds";
		TypedQuery<ContractorTimesheetValidatorQueryResultDto> query = this.entityManager.createQuery(jpql,
				ContractorTimesheetValidatorQueryResultDto.class);
		query.setParameter("timesheetIds", timesheetIds);
		return query.getResultList();
	}

}
