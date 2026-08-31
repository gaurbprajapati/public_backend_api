package io.recruitcrm.microservice.timesheet.dto.portal.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkValidateResponseBodyDto {

	private List<ContactValidationResultDto> results;

	private int validCount;

	private int invalidCount;

}
