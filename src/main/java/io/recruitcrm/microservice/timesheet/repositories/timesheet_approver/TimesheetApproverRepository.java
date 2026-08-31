package io.recruitcrm.microservice.timesheet.repositories.timesheet_approver;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.contract_staffing.entity.model.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.dao.timesheet_approval.TimesheetApproverJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class TimesheetApproverRepository implements ITimesheetApproverRepository {

	private final EntityManager entityManager;

	private final TimesheetApproverJpaRepository timesheetApproverJpaRepository;

	public TimesheetApproverRepository(EntityManager entityManager,
			TimesheetApproverJpaRepository timesheetApproverJpaRepository) {
		this.entityManager = entityManager;
		this.timesheetApproverJpaRepository = timesheetApproverJpaRepository;
	}

	@Override
	@WriterRoute
	@Transactional
	public TimesheetApprover createTimesheetApprover(Integer userTypeId, Integer timesheetSettingId, Integer entityId) {
		TimesheetApprover timesheetApprover = new TimesheetApprover();

		timesheetApprover.setUserTypeId(userTypeId);
		timesheetApprover.setTimesheetSettingId(timesheetSettingId);
		timesheetApprover.setEntityId(entityId);

		return this.timesheetApproverJpaRepository.save(timesheetApprover);
	}

	@Override
	public List<TimesheetApprover> findByTimesheetSettingId(Integer timesheetSettingId) {
		String jpql = "SELECT t FROM TimesheetApprover t " + "WHERE t.timesheetSettingId = :timesheetSettingId";
		return this.entityManager.createQuery(jpql, TimesheetApprover.class)
			.setParameter("timesheetSettingId", timesheetSettingId)
			.getResultList();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteTimesheetApprovers(Integer jobId, List<Integer> contractorIds, List<Integer> agencyIds,
			Integer accountId) {
		String jpql = "DELETE FROM TimesheetApprover t " + "WHERE t.timesheetSettingId IN ("
				+ "SELECT c.id FROM TimesheetSetting c " + "JOIN c.association a " + "WHERE c.accountId = :accountId "
				+ "AND a.jobId = :jobId " + "AND a.contractorId IN (:contractorIds)" + ") "
				+ "AND t.entityId NOT IN :agencyIds " + "AND t.userTypeId = :userTypeId";

		this.entityManager.createQuery(jpql)
			.setParameter("accountId", accountId)
			.setParameter("jobId", jobId)
			.setParameter("contractorIds", contractorIds)
			.setParameter("agencyIds", agencyIds)
			.setParameter("userTypeId", UserTypeEnum.AGENCY_RECRUITER.getId())
			.executeUpdate();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void addTimesheetApprovers(Integer jobId, List<Integer> contractorIds, List<Integer> agencyIds,
			Integer accountId) {
		// First, fetch all relevant timesheetSettingIds
		String fetchQuery = "SELECT ts.id FROM TimesheetSetting ts " + "JOIN ts.association a "
				+ "WHERE ts.accountId = :accountId AND a.jobId = :jobId AND a.contractorId IN (:contractorIds)";

		List<Integer> timesheetSettingIds = this.entityManager.createQuery(fetchQuery, Integer.class)
			.setParameter("accountId", accountId)
			.setParameter("jobId", jobId)
			.setParameter("contractorIds", contractorIds)
			.getResultList();

		// Then, loop and insert IGNORE each combination
		String insertQuery = "INSERT IGNORE INTO cst_timesheet_approver_t (user_type_id, timesheet_setting_id, entity_id) "
				+ "SELECT :userTypeId, :tsId, :agencyId " + "FROM DUAL WHERE NOT EXISTS ("
				+ "SELECT 1 FROM cst_timesheet_approver_t ta "
				+ "WHERE ta.timesheet_setting_id = :tsId AND ta.entity_id = :agencyId AND ta.user_type_id = :userTypeId)";

		for (Integer tsId : timesheetSettingIds) {
			for (Integer agencyId : agencyIds) {
				this.entityManager.createNativeQuery(insertQuery)
					.setParameter("userTypeId", UserTypeEnum.AGENCY_RECRUITER.getId())
					.setParameter("tsId", tsId)
					.setParameter("agencyId", agencyId)
					.executeUpdate();
			}
		}
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteTimesheetApproversForClients(Integer jobId, List<Integer> contractorIds, List<Integer> clientIds,
			Integer accountId) {
		String jpql = "DELETE FROM TimesheetApprover t " + "WHERE t.timesheetSettingId IN ("
				+ "SELECT c.id FROM TimesheetSetting c " + "JOIN c.association a " + "WHERE c.accountId = :accountId "
				+ "AND a.jobId = :jobId " + "AND a.contractorId IN (:contractorIds)" + ") "
				+ "AND t.entityId NOT IN :clientIds " + "AND t.userTypeId = :userTypeId";

		this.entityManager.createQuery(jpql)
			.setParameter("accountId", accountId)
			.setParameter("jobId", jobId)
			.setParameter("contractorIds", contractorIds)
			.setParameter("clientIds", clientIds)
			.setParameter("userTypeId", UserTypeEnum.COMPANY_CONTACT.getId())
			.executeUpdate();
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void addTimesheetApproversForClients(Integer jobId, List<Integer> contractorIds, List<Integer> clientIds,
			Integer accountId) {
		// First, fetch all relevant timesheetSettingIds
		String fetchQuery = "SELECT ts.id FROM TimesheetSetting ts " + "JOIN ts.association a "
				+ "WHERE ts.accountId = :accountId AND a.jobId = :jobId AND a.contractorId IN (:contractorIds)";

		List<Integer> timesheetSettingIds = this.entityManager.createQuery(fetchQuery, Integer.class)
			.setParameter("accountId", accountId)
			.setParameter("jobId", jobId)
			.setParameter("contractorIds", contractorIds)
			.getResultList();

		// Then, loop and insert IGNORE each combination
		String insertQuery = "INSERT IGNORE INTO cst_timesheet_approver_t (user_type_id, timesheet_setting_id, entity_id) "
				+ "SELECT :userTypeId, :tsId, :clientId " + "FROM DUAL WHERE NOT EXISTS ("
				+ "SELECT 1 FROM cst_timesheet_approver_t ta "
				+ "WHERE ta.timesheet_setting_id = :tsId AND ta.entity_id = :clientId AND ta.user_type_id = :userTypeId)";

		for (Integer tsId : timesheetSettingIds) {
			for (Integer clientId : clientIds) {
				this.entityManager.createNativeQuery(insertQuery)
					.setParameter("userTypeId", UserTypeEnum.COMPANY_CONTACT.getId())
					.setParameter("tsId", tsId)
					.setParameter("clientId", clientId)
					.executeUpdate();
			}
		}
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public void createTimesheetApproverInBulk(List<TimesheetApprover> timesheetApprovers) {
		this.timesheetApproverJpaRepository.saveAll(timesheetApprovers);
	}

	@Override
	public List<TimesheetApprover> findByTimesheetSettingIds(List<Integer> timesheetSettingIds) {
		if (timesheetSettingIds == null || timesheetSettingIds.isEmpty()) {
			return List.of();
		}

		String jpql = "SELECT t FROM TimesheetApprover t " + "WHERE t.timesheetSettingId IN :timesheetSettingIds";
		return this.entityManager.createQuery(jpql, TimesheetApprover.class)
			.setParameter("timesheetSettingIds", timesheetSettingIds)
			.getResultList();
	}

}