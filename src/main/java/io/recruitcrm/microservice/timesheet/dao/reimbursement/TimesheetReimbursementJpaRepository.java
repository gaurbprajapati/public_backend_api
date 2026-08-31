package io.recruitcrm.microservice.timesheet.dao.reimbursement;

import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimesheetReimbursementJpaRepository extends JpaRepository<TimesheetReimbursement, Integer> {

	List<TimesheetReimbursement> findAllByTimesheetIdAndAccountId(Integer timesheetId, Integer accountId);

	List<TimesheetReimbursement> findAllByTimesheetIdAndAccountIdAndIsSharedWithClient(Integer timesheetId,
			Integer accountId, Integer isSharedWithClient);

	List<TimesheetReimbursement> findAllByTimesheetIdInAndAccountId(List<Integer> timesheetIds, Integer accountId);

	List<TimesheetReimbursement> findAllByTimesheetIdInAndAccountIdAndStatusOrderByTimesheetIdAscIdAsc(
			List<Integer> timesheetIds, Integer accountId, Integer status);

	Optional<TimesheetReimbursement> findByIdAndTimesheetIdAndAccountId(Integer id, Integer timesheetId,
			Integer accountId);

	long countByTimesheetIdAndAccountId(Integer timesheetId, Integer accountId);

}
