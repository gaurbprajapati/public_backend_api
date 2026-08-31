package io.recruitcrm.microservice.timesheet.dao.invoice;

import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceJpaRepository extends JpaRepository<TimesheetInvoice, Integer> {

	TimesheetInvoice findByTimesheetIdAndAccountId(Integer timesheetId, Integer accountId);

	@Query("SELECT i FROM TimesheetInvoice i WHERE i.timesheetId IN :timesheetIds AND i.accountId = :accountId")
	List<TimesheetInvoice> findByTimesheetIdIn(@Param("timesheetIds") List<Integer> timesheetIds, Integer accountId);

}