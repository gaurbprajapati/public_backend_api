package io.recruitcrm.microservice.timesheet.dao.job_secondary_contact;

import io.recruitcrm.entity.model.JobSecondaryContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobSecondaryContactJpaRepository extends JpaRepository<JobSecondaryContact, Integer> {

	Optional<JobSecondaryContact> findByJobIdAndContactId(Integer jobId, Integer contactId);

	boolean existsByJobIdAndContactIdIn(Integer jobId, List<Integer> contactIds);

}
