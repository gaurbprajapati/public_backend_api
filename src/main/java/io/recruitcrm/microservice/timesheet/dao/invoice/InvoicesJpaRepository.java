package io.recruitcrm.microservice.timesheet.dao.invoice;

import io.recruitcrm.entity.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoicesJpaRepository extends JpaRepository<Invoice, Integer> {

}
