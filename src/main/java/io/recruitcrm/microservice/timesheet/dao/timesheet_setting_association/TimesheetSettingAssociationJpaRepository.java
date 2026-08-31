package io.recruitcrm.microservice.timesheet.dao.timesheet_setting_association;

import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimesheetSettingAssociationJpaRepository extends JpaRepository<TimesheetSettingAssociation, Integer> {

	List<TimesheetSettingAssociation> findByJobIdAndContractorIdIn(Integer jobId, List<Integer> contractorIds);

}