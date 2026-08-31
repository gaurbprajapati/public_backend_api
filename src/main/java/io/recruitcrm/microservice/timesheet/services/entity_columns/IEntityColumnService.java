package io.recruitcrm.microservice.timesheet.services.entity_columns;

import io.recruitcrm.microservice.timesheet.dto.entity_columns.AccountViewColumnResponseBodyDto;

public interface IEntityColumnService {

	AccountViewColumnResponseBodyDto getEntityColumns(String entity);

	AccountViewColumnResponseBodyDto getAccountViewColumnsByEntity(String entity);

}
