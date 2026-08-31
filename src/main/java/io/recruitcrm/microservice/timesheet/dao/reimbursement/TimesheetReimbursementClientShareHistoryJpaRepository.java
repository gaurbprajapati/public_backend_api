package io.recruitcrm.microservice.timesheet.dao.reimbursement;

import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursementClientShareHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimesheetReimbursementClientShareHistoryJpaRepository
		extends JpaRepository<TimesheetReimbursementClientShareHistory, Integer> {

}
