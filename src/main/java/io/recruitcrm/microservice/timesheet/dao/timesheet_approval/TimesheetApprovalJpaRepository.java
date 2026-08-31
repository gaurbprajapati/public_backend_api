package io.recruitcrm.microservice.timesheet.dao.timesheet_approval;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.microservice.timesheet.dto.timesheet.StatusHistoryQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetApproverResponseBodyDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TimesheetApprovalJpaRepository extends JpaRepository<TimesheetApproval, Integer> {

	TimesheetApproval findFirstByTimesheetIdOrderByIdDesc(Integer timesheetId);

	@Query("SELECT new io.recruitcrm.microservice.timesheet.dto.timesheet.StatusHistoryQueryResultDto(" + "t1.id, "
			+ "t1.timesheetApprovalStatusTypeId, " + "t1.remark, " + "t1.userTypeId, " + "t1.createdOn, "
			+ "t1.entityId" + ") " + "FROM TimesheetApproval t1 " + "WHERE t1.timesheetId = :timesheetId "
			+ "ORDER BY t1.id DESC")
	List<StatusHistoryQueryResultDto> findByTimesheetIdOrderByIdDesc(@Param("timesheetId") Integer timesheetId);

	@Transactional(propagation = Propagation.MANDATORY)
	@WriterRoute
	void deleteByTimesheetId(Integer timesheetId);

	@Transactional(propagation = Propagation.MANDATORY)
	@WriterRoute
	void deleteByTimesheetIdIn(List<Integer> timesheetIds);

	List<TimesheetApproval> findByTimesheetIdIn(List<Integer> timesheetIds);

	@Query("SELECT new io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetApproverResponseBodyDto("
			+ "t1.timesheetApprovalStatusTypeId, t1.entityId, t1.userTypeId, t1.timesheetId) "
			+ "FROM TimesheetApproval t1 " + "WHERE t1.id = (SELECT MAX(t2.id) " + "FROM TimesheetApproval t2 "
			+ "WHERE t2.timesheetId = t1.timesheetId) " + "AND t1.timesheetId IN :timesheetIds "
			+ "ORDER BY t1.id DESC")
	List<TimesheetApproverResponseBodyDto> findLatestApprovalsByTimesheetIds(
			@Param("timesheetIds") List<Integer> timesheetIds);

}
