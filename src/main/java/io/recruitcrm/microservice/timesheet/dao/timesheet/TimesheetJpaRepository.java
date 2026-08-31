package io.recruitcrm.microservice.timesheet.dao.timesheet;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetJpaRepository extends JpaRepository<Timesheet, Integer> {

	Optional<Timesheet> findByIdAndAccountId(Integer id, Integer accountId);

	List<Timesheet> findByIdInAndAccountId(List<Integer> ids, Integer accountId);

	@Transactional(propagation = Propagation.MANDATORY)
	@WriterRoute
	void deleteByIdAndAccountId(Integer id, Integer accountId);

	@Transactional(propagation = Propagation.MANDATORY)
	@WriterRoute
	void deleteByIdInAndAccountId(List<Integer> ids, Integer accountId);

	@Query("SELECT t FROM Timesheet t " + "JOIN TimesheetSetting ts ON t.timesheetSettingId = ts.id "
			+ "JOIN ts.association a " + "WHERE a.jobId IN :jobIds " + "AND a.contractorId IN :candidateIds "
			+ "AND t.accountId = :accountId")
	List<Timesheet> findByJobIdsAndCandidateIdsAndAccountId(@Param("jobIds") List<Integer> jobIds,
			@Param("candidateIds") List<Integer> candidateIds, @Param("accountId") Integer accountId);

	// fetch findByTimesheetSettingIdsAndAccountId
	@Query("SELECT t FROM Timesheet t " + "WHERE t.timesheetSettingId IN :timesheetSettingIds "
			+ "AND t.accountId = :accountId")
	List<Timesheet> findByTimesheetSettingIdsAndAccountId(
			@Param("timesheetSettingIds") List<Integer> timesheetSettingIds, @Param("accountId") Integer accountId);

}
