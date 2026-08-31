package io.recruitcrm.microservice.timesheet.dao.timesheet_setting;

import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TimesheetSettingJpaRepository extends JpaRepository<TimesheetSetting, Integer> {

	Optional<TimesheetSetting> findByIdAndAccountId(Integer id, Integer accountId);

	List<TimesheetSetting> findByIdInAndAccountId(List<Integer> ids, Integer accountId);

	// find by jobid and contractorid and accountid
	@Query("SELECT t FROM TimesheetSetting t " + "JOIN t.association a " + "WHERE a.jobId = :jobId "
			+ "AND a.contractorId = :contractorId " + "AND t.accountId = :accountId")
	Optional<TimesheetSetting> findByJobIdAndContractorIdAndAccountId(@Param("jobId") Integer jobId,
			@Param("contractorId") Integer contractorId, @Param("accountId") Integer accountId);

	@Query("SELECT ts FROM TimesheetSetting ts JOIN ts.association a WHERE a.contractorId IN :contractorIds AND ts.accountId = :accountId")
	List<TimesheetSetting> findByContractorIdAndAccountId(@Param("contractorIds") List<Integer> contractorIds,
			@Param("accountId") Integer accountId);

	// Timesheet settings sharing an association are versioned; the highest id is the
	// latest. Use Pageable (size 1) at the call site to fetch only the latest row.
	@Query("SELECT ts FROM TimesheetSetting ts WHERE ts.association.id = :associationId "
			+ "AND ts.accountId = :accountId ORDER BY ts.id DESC")
	List<TimesheetSetting> findLatestByAssociationIdAndAccountId(@Param("associationId") Integer associationId,
			@Param("accountId") Integer accountId, Pageable pageable);

}
