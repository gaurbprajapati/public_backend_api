package io.recruitcrm.microservice.timesheet.dao.kafka_consumer;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.CstTimesheetKafkaEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA repository for {@code cst_timesheet_kafka_event_log_t} maintenance operations.
 */
@Repository
public interface TimesheetKafkaEventLogJpaRepository extends JpaRepository<CstTimesheetKafkaEventLog, Integer> {

	/**
	 * Bulk-delete all event log records whose {@code createdOn} Unix timestamp is
	 * strictly less than the given cutoff (i.e. older than the cutoff instant).
	 * @param cutoff exclusive upper bound as a Unix epoch-second integer
	 * @return number of rows deleted
	 */
	@Modifying(clearAutomatically = true)
	@Transactional(propagation = Propagation.MANDATORY)
	@WriterRoute
	@Query("DELETE FROM CstTimesheetKafkaEventLog e WHERE e.createdOn < :cutoff")
	int deleteAllByCreatedOnBefore(@Param("cutoff") Integer cutoff);

}
