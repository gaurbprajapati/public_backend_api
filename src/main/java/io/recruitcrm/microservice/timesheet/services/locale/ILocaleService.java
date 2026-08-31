package io.recruitcrm.microservice.timesheet.services.locale;

import io.recruitcrm.microservice.timesheet.dto.entity_columns.AccountViewColumnResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto;

public interface ILocaleService {

	/**
	 * Merge translated locale labels into entity columns.
	 * @param entityColumns columns that need labels (AccountViewColumnResponseBodyDto)
	 * @param localeLabels translated labels from LocaleResponseBodyDto
	 */
	void mergeLocaleLabelsIntoEntityColumns(AccountViewColumnResponseBodyDto entityColumns,
			LocaleResponseBodyDto localeLabels);

}