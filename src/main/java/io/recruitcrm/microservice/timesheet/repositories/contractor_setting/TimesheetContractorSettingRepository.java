package io.recruitcrm.microservice.timesheet.repositories.contractor_setting;

import io.recruitcrm.microservice.timesheet.dto.contractor_setting.OccupiedSlotsQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TimesheetContractorSettingRepository implements ITimesheetContractorSettingRepository {

	private final EntityManager entityManager;

	public TimesheetContractorSettingRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<OccupiedSlotsQueryResultDto> findTimesheetsWithinDateRangeAndContractors(Integer startDate,
			Integer endDate, List<Integer> contractorIds, Integer jobId) {
		String jpql = "SELECT t.id, t.periodStart, t.periodEnd, a.contractorId, a.jobId, ts.timesheetStartDay "
				+ "FROM Timesheet t " + "LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId "
				+ "LEFT JOIN ts.association a "
				+ "WHERE t.periodEnd >= :startDate AND t.periodStart <= :endDate AND a.contractorId IN :contractorIds AND a.jobId = :jobId ";
		TypedQuery<OccupiedSlotsQueryResultDto> query = this.entityManager.createQuery(jpql,
				OccupiedSlotsQueryResultDto.class);
		query.setParameter("startDate", startDate);
		query.setParameter("endDate", endDate);
		query.setParameter("contractorIds", contractorIds);
		query.setParameter("jobId", jobId);
		return query.getResultList();
	}

	@Override
	public List<OccupiedSlotsQueryResultDto> findTimesheetsForContractorJobPairs(Integer startDate, Integer endDate,
			List<ContractorJobPairDto> contractorJobPairs) {
		if (contractorJobPairs == null || contractorJobPairs.isEmpty()) {
			return new ArrayList<>();
		}

		// Build dynamic OR conditions for each contractor-job pair
		StringBuilder jpqlBuilder = new StringBuilder();
		jpqlBuilder.append("SELECT t.id, t.periodStart, t.periodEnd, a.contractorId, a.jobId, ts.timesheetStartDay ");
		jpqlBuilder.append("FROM Timesheet t ");
		jpqlBuilder.append("LEFT JOIN TimesheetSetting ts ON ts.id = t.timesheetSettingId ");
		jpqlBuilder.append("LEFT JOIN ts.association a ");
		jpqlBuilder.append("WHERE t.periodEnd >= :startDate AND t.periodStart <= :endDate ");
		jpqlBuilder.append("AND (");

		for (int i = 0; i < contractorJobPairs.size(); i++) {
			if (i > 0) {
				jpqlBuilder.append(" OR ");
			}
			jpqlBuilder.append("(a.contractorId = :contractorId").append(i);
			jpqlBuilder.append(" AND a.jobId = :jobId").append(i).append(")");
		}
		jpqlBuilder.append(")");

		TypedQuery<OccupiedSlotsQueryResultDto> query = this.entityManager.createQuery(jpqlBuilder.toString(),
				OccupiedSlotsQueryResultDto.class);
		query.setParameter("startDate", startDate);
		query.setParameter("endDate", endDate);

		for (int i = 0; i < contractorJobPairs.size(); i++) {
			ContractorJobPairDto pair = contractorJobPairs.get(i);
			query.setParameter("contractorId" + i, pair.getContractorId());
			query.setParameter("jobId" + i, pair.getJobId());
		}

		return query.getResultList();
	}

}
