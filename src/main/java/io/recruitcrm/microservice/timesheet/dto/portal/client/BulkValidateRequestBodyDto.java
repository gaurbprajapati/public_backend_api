package io.recruitcrm.microservice.timesheet.dto.portal.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkValidateRequestBodyDto {

	@NotNull(message = "contacts must be a non-empty list")
	@NotEmpty(message = "contacts must be a non-empty list")
	@Valid
	private List<ContactBulkValidateItemDto> contacts;

}
