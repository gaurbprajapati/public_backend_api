package io.recruitcrm.microservice.timesheet.dto.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PayBillTypeResponseBodyDto {

	private Integer id;

	private String label;

	private String value;

}
