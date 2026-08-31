package io.recruitcrm.microservice.timesheet.dto.timesheet;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealResponseBodyDto {

	@NotNull(message = "Deal id cannot be null")
	private Integer dealId;

	@NotNull(message = "Deal name cannot be null")
	private String name;

	private String ownerName;

	@NotNull(message = "Serial number cannot be null")
	private Integer serialNumber;

	@NotNull(message = "Slug cannot be null")
	private String slug;

	private String status;

}
