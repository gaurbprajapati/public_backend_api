package io.recruitcrm.microservice.timesheet.dao.invoice;

import io.recruitcrm.entity.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceEntityJpaRepository extends JpaRepository<Invoice, Integer> {

}
