package io.recruitcrm.microservice.timesheet.repositories.timesheet_setting;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingUserPreference;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobPairDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet_setting.TimesheetSettingBreakInfoDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingTemplateWorkDayDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TimesheetSettingRepository implements ITimesheetSettingRepository {

	private static final String CONTRACTOR_ID_PARAM = "contractorId";

	private static final String CONTRACTOR_IDS_QUERY_PARAM = "contractorIds";

	private static final String JOB_ID_PARAM = "jobId";

	private static final String ACCOUNT_ID_PARAM = "accountId";

	final EntityManager entityManager;

	public TimesheetSettingRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public Boolean validateTimesheetSettingsConsistency(List<Integer> contractorIds, int contractorCount) {
		String jpql = "SELECT COUNT(ts) FROM TimesheetSetting ts " + "JOIN ts.association a "
				+ "WHERE a.contractorId IN :" + CONTRACTOR_IDS_QUERY_PARAM + " "
				+ "GROUP BY a.jobId, ts.timesheetStartDay, ts.timesheetFrequency "
				+ "HAVING COUNT(DISTINCT a.contractorId) = :contractorCount";

		TypedQuery<Long> query = this.entityManager.createQuery(jpql, Long.class);
		query.setParameter(CONTRACTOR_IDS_QUERY_PARAM, contractorIds);
		query.setParameter("contractorCount", contractorCount);

		List<Long> results = query.getResultList();
		return !results.isEmpty();
	}

	@Override
	public List<TimesheetSetting> findLatestTimesheetSettingsByJobIdAndContractorIds(Integer jobId,
			List<Integer> contractorIds) {
		String jpql = "SELECT ts FROM TimesheetSetting ts " + "JOIN ts.association a "
				+ "WHERE a.jobId = :jobId AND a.contractorId IN :" + CONTRACTOR_IDS_QUERY_PARAM + " "
				+ "AND ts.id IN (SELECT MAX(innerTs.id) FROM TimesheetSetting innerTs "
				+ "JOIN innerTs.association innerA " + "WHERE innerA.jobId = :jobId AND innerA.contractorId IN :"
				+ CONTRACTOR_IDS_QUERY_PARAM + " " + "GROUP BY innerA.contractorId)";

		TypedQuery<TimesheetSetting> query = this.entityManager.createQuery(jpql, TimesheetSetting.class);
		query.setParameter(JOB_ID_PARAM, jobId);
		query.setParameter(CONTRACTOR_IDS_QUERY_PARAM, contractorIds);

		return query.getResultList();
	}

	@Override
	public Optional<TimesheetSetting> findByJobIdContractorId(Integer jobId, Integer contractorId) {
		String jpql = "SELECT t FROM TimesheetSetting t " + "JOIN t.association a "
				+ "WHERE a.jobId = :jobId AND a.contractorId = :contractorId " + "ORDER BY t.id DESC";
		TypedQuery<TimesheetSetting> query = this.entityManager.createQuery(jpql, TimesheetSetting.class);
		query.setParameter(JOB_ID_PARAM, jobId);
		query.setParameter(CONTRACTOR_ID_PARAM, contractorId);
		query.setMaxResults(1);

		List<TimesheetSetting> results = query.getResultList();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}

	public Optional<TimesheetSetting> findFirstByJobIdContractorId(Integer jobId, Integer contractorId) {
		String jpql = "SELECT t FROM TimesheetSetting t WHERE t.association.jobId = :jobId AND t.association.contractorId = :contractorId ORDER BY t.id ASC";
		TypedQuery<TimesheetSetting> query = this.entityManager.createQuery(jpql, TimesheetSetting.class);
		query.setParameter(JOB_ID_PARAM, jobId);
		query.setParameter(CONTRACTOR_ID_PARAM, contractorId);
		query.setMaxResults(1);

		List<TimesheetSetting> results = query.getResultList();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public TimesheetSetting createTimesheetSetting(TimesheetSetting timesheetSetting) {
		if (timesheetSetting.getId() == null) {
			this.entityManager.persist(timesheetSetting);
			return timesheetSetting;
		}
		else {
			return this.entityManager.merge(timesheetSetting);
		}
	}

	@Override
	public Optional<TimesheetSetting> findByIdAndAccountId(Integer id, Integer accountId) {
		String jpql = "SELECT t FROM TimesheetSetting t WHERE t.id = :id AND t.accountId = :accountId ORDER BY t.id DESC";
		TypedQuery<TimesheetSetting> query = this.entityManager.createQuery(jpql, TimesheetSetting.class);
		query.setParameter("id", id);
		query.setParameter(ACCOUNT_ID_PARAM, accountId);
		query.setMaxResults(1);

		List<TimesheetSetting> results = query.getResultList();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}

	@Override
	public Boolean validateTimesheetsExist(List<Integer> timeLogDates, Integer accountId, Integer jobId,
			List<Integer> contractorIds) {
		// Step 1: Fetch count of TimeLog entries based on Timesheet and TimesheetSetting
		// conditions
		Long count = this.entityManager.createQuery(
				"SELECT COUNT(tl.id) " + "FROM TimeLog tl " + "JOIN tl.timesheet t " + "JOIN t.timesheetSetting ts "
						+ "JOIN ts.association a " + "WHERE a.jobId = :jobId " + "AND a.contractorId IN :"
						+ CONTRACTOR_IDS_QUERY_PARAM + " " + "AND ts.accountId = :accountId " + "AND tl.date IN :dates",
				Long.class)
			.setParameter(JOB_ID_PARAM, jobId)
			.setParameter(CONTRACTOR_IDS_QUERY_PARAM, contractorIds)
			.setParameter(ACCOUNT_ID_PARAM, accountId)
			.setParameter("dates", timeLogDates)
			.getSingleResult();

		// Step 2: Convert to boolean based on count
		return count > 0;
	}

	@Override
	public List<Integer> fetchEnabledAssignmentIds(List<Integer> assignmentIds, Integer accountId) {
		String jpql = "SELECT ajc.id FROM AssignCandidateJob ajc JOIN TimesheetSettingAssociation tsa ON ajc.jobId = tsa.jobId AND ajc.candidateId = tsa.contractorId WHERE ajc.id IN :assignmentIds AND ajc.accountId = :accountId ";
		TypedQuery<Integer> query = this.entityManager.createQuery(jpql, Integer.class);
		query.setParameter("assignmentIds", assignmentIds);
		query.setParameter(ACCOUNT_ID_PARAM, accountId);
		return query.getResultList();
	}

	@Override
	public List<TimesheetSettingTemplateWorkDayDto> findTimesheetSettingsWithTemplateWorkDayByIds(
			List<Integer> timesheetSettingIds) {
		if (timesheetSettingIds == null || timesheetSettingIds.isEmpty()) {
			return new ArrayList<>();
		}

		// Use native SQL to extract workDayId from JSON array
		String sql = "SELECT ts.id as timesheetSettingId, "
				+ "GROUP_CONCAT(JSON_EXTRACT(json_item.value, '$.workDayId') ORDER BY JSON_EXTRACT(json_item.value, '$.workDayId')) as workDayIds "
				+ "FROM cst_timesheet_setting_t ts "
				+ "CROSS JOIN JSON_TABLE(ts.template_work_day, '$[*]' COLUMNS(value JSON PATH '$')) as json_item "
				+ "WHERE ts.id IN :timesheetSettingIds " + "GROUP BY ts.id";

		Query query = this.entityManager.createNativeQuery(sql);
		query.setParameter("timesheetSettingIds", timesheetSettingIds);

		@SuppressWarnings("unchecked")
		List<Object[]> results = query.getResultList();

		return results.stream().map((row) -> {
			Integer timesheetSettingId = (Integer) row[0];
			String workDayIdsStr = (String) row[1];

			List<Integer> workDayIds = new ArrayList<>();
			if (workDayIdsStr != null && !workDayIdsStr.trim().isEmpty()) {
				workDayIds = Arrays.stream(workDayIdsStr.split(",")).map(String::trim).map(Integer::parseInt).toList();
			}

			return new TimesheetSettingTemplateWorkDayDto(timesheetSettingId, workDayIds);
		}).toList();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveUserPreference(String timesheetSettingJson, Integer addedBy, Integer addedByUserTypeId,
			Integer accountId) {
		TimesheetSettingUserPreference preference = new TimesheetSettingUserPreference();
		preference.setTimesheetSettingJson(timesheetSettingJson);
		preference.setAddedBy(addedBy);
		preference.setAddedByUserTypeId(addedByUserTypeId);
		preference.setAccountId(accountId);

		this.entityManager.persist(preference);
	}

	@Override
	public Optional<TimesheetSettingUserPreference> findUserPreferenceByAccountIdAndUserId(Integer accountId,
			Integer userId) {
		String jpql = "SELECT p FROM TimesheetSettingUserPreference p WHERE p.accountId = :accountId AND p.addedBy = :userId ORDER BY p.id DESC";
		TypedQuery<TimesheetSettingUserPreference> query = this.entityManager.createQuery(jpql,
				TimesheetSettingUserPreference.class);
		query.setParameter(ACCOUNT_ID_PARAM, accountId);
		query.setParameter("userId", userId);
		query.setMaxResults(1);

		List<TimesheetSettingUserPreference> results = query.getResultList();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}

	@Override
	public List<TimesheetSetting> findLatestTimesheetSettingsForContractorJobPairs(
			List<ContractorJobPairDto> contractorJobPairs) {
		if (contractorJobPairs == null || contractorJobPairs.isEmpty()) {
			return new ArrayList<>();
		}

		// Build dynamic OR conditions for each contractor-job pair
		StringBuilder jpqlBuilder = new StringBuilder();
		jpqlBuilder.append("SELECT ts FROM TimesheetSetting ts ");
		jpqlBuilder.append("JOIN ts.association a ");
		jpqlBuilder.append("WHERE (");

		// Build OR conditions for outer query
		for (int i = 0; i < contractorJobPairs.size(); i++) {
			if (i > 0) {
				jpqlBuilder.append(" OR ");
			}
			jpqlBuilder.append("(a.jobId = :jobId").append(i);
			jpqlBuilder.append(" AND a.contractorId = :contractorId").append(i).append(")");
		}
		jpqlBuilder.append(") ");

		// Subquery to get latest setting per contractor-job pair
		jpqlBuilder.append("AND ts.id IN (SELECT MAX(innerTs.id) FROM TimesheetSetting innerTs ");
		jpqlBuilder.append("JOIN innerTs.association innerA ");
		jpqlBuilder.append("WHERE (");

		// Build OR conditions for subquery
		for (int i = 0; i < contractorJobPairs.size(); i++) {
			if (i > 0) {
				jpqlBuilder.append(" OR ");
			}
			jpqlBuilder.append("(innerA.jobId = :jobId").append(i);
			jpqlBuilder.append(" AND innerA.contractorId = :contractorId").append(i).append(")");
		}
		jpqlBuilder.append(") ");
		jpqlBuilder.append("GROUP BY innerA.jobId, innerA.contractorId)");

		TypedQuery<TimesheetSetting> query = this.entityManager.createQuery(jpqlBuilder.toString(),
				TimesheetSetting.class);

		for (int i = 0; i < contractorJobPairs.size(); i++) {
			ContractorJobPairDto pair = contractorJobPairs.get(i);
			query.setParameter(JOB_ID_PARAM + i, pair.getJobId());
			query.setParameter(CONTRACTOR_ID_PARAM + i, pair.getContractorId());
		}

		return query.getResultList();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateIsRemarkMandatoryByAssociationIds(List<Integer> associationIds, Integer isRemarkMandatory) {
		if (associationIds == null || associationIds.isEmpty()) {
			return;
		}
		String jpql = "UPDATE TimesheetSetting ts SET ts.isRemarkMandatory = :isRemarkMandatory WHERE ts.association.id IN :associationIds";
		Query query = this.entityManager.createQuery(jpql);
		query.setParameter("isRemarkMandatory", isRemarkMandatory);
		query.setParameter("associationIds", associationIds);
		query.executeUpdate();
		this.entityManager.flush();
		this.entityManager.clear();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateIsUnplannedHoursPayEnabledByAssociationIds(List<Integer> associationIds,
			Integer isUnplannedHoursPayEnabled) {
		if (associationIds == null || associationIds.isEmpty()) {
			return;
		}
		String jpql = "UPDATE TimesheetSetting ts SET ts.isUnplannedHoursPayEnabled = :isUnplannedHoursPayEnabled WHERE ts.association.id IN :associationIds";
		Query query = this.entityManager.createQuery(jpql);
		query.setParameter("isUnplannedHoursPayEnabled", isUnplannedHoursPayEnabled);
		query.setParameter("associationIds", associationIds);
		query.executeUpdate();
		this.entityManager.flush();
		this.entityManager.clear();
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Integer, Integer> findWorkLogTypeByIdIn(List<Integer> timesheetSettingIds) {
		if (timesheetSettingIds == null || timesheetSettingIds.isEmpty()) {
			return new HashMap<>();
		}

		String jpql = "SELECT ts.id, ts.workLogType FROM TimesheetSetting ts WHERE ts.id IN :ids";
		TypedQuery<Object[]> query = this.entityManager.createQuery(jpql, Object[].class);
		query.setParameter("ids", timesheetSettingIds);

		Map<Integer, Integer> result = new HashMap<>();
		List<Object[]> rows = query.getResultList();
		for (Object[] row : rows) {
			Integer id = (Integer) row[0];
			Integer workLogType = (Integer) row[1];
			if (id != null && workLogType != null) {
				result.put(id, workLogType);
			}
		}
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Integer, TimesheetSettingBreakInfoDto> findBreakInfoByIdIn(List<Integer> timesheetSettingIds) {
		if (timesheetSettingIds == null || timesheetSettingIds.isEmpty()) {
			return new HashMap<>();
		}

		String jpql = "SELECT ts.id, ts.calculateBreakTime, ts.breakTimeThreshold "
				+ "FROM TimesheetSetting ts WHERE ts.id IN :ids";
		TypedQuery<Object[]> query = this.entityManager.createQuery(jpql, Object[].class);
		query.setParameter("ids", timesheetSettingIds);

		Map<Integer, TimesheetSettingBreakInfoDto> result = new HashMap<>();
		List<Object[]> rows = query.getResultList();
		for (Object[] row : rows) {
			Integer id = (Integer) row[0];
			Boolean calculateBreakTime = (Boolean) row[1];
			Integer breakTimeThreshold = (Integer) row[2];
			if (id != null) {
				result.put(id, new TimesheetSettingBreakInfoDto(calculateBreakTime, breakTimeThreshold));
			}
		}
		return result;
	}

}
