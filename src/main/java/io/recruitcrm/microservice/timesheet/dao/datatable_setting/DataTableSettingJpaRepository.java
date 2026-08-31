package io.recruitcrm.microservice.timesheet.dao.datatable_setting;

import io.recruitcrm.entity.model.DataTableSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataTableSettingJpaRepository extends JpaRepository<DataTableSetting, Integer> {

	List<DataTableSetting> findByUserIdAndDatatableKeyOrderByIdAsc(Integer userId, String datatableKey);

}
