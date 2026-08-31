package io.recruitcrm.microservice.timesheet.dao.timesheet_approval;

import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimesheetApproverJpaRepository extends JpaRepository<TimesheetApprover, Integer> {

}
