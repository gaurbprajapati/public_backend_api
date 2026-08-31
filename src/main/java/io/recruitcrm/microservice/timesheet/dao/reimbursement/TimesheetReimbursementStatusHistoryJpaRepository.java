package io.recruitcrm.microservice.timesheet.dao.reimbursement;

import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursementStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TimesheetReimbursementStatusHistoryJpaRepository
		extends JpaRepository<TimesheetReimbursementStatusHistory, Integer> {

	@Transactional
	void deleteByTimesheetReimbursementId(Integer timesheetReimbursementId);

	List<TimesheetReimbursementStatusHistory> findByTimesheetReimbursementIdAndAccountIdOrderByCreatedOnDesc(
			Integer reimbursementId, Integer accountId);

}
