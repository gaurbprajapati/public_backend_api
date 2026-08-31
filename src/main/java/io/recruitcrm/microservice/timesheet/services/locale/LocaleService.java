package io.recruitcrm.microservice.timesheet.services.locale;

import io.recruitcrm.microservice.timesheet.dto.entity_columns.AccountViewColumnResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.ColumnAccountViewResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LabelResponseBodyDto;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LocaleService implements ILocaleService {

	@Override
	public void mergeLocaleLabelsIntoEntityColumns(AccountViewColumnResponseBodyDto entityColumns,
			LocaleResponseBodyDto localeLabels) {
		if (entityColumns == null || localeLabels == null) {
			return;
		}

		Map<String, LabelResponseBodyDto> localeValues = localeLabels.getTimesheet();
		if (localeValues == null) {
			return;
		}

		for (Map.Entry<String, ColumnAccountViewResponseBodyDto> entry : entityColumns.entrySet()) {
			String key = entry.getKey();
			ColumnAccountViewResponseBodyDto column = entry.getValue();

			LabelResponseBodyDto label = localeValues.get(key);
			if (label != null) {
				if (label.getLabel() != null) {
					column.setLabel(label.getLabel());
				}
				if (label.getLonglabel() != null) {
					column.setLonglabel(label.getLonglabel());
				}
			}
		}
	}

}
