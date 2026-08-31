package io.recruitcrm.microservice.timesheet.dto.entity_columns;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class EntityColumnResponseBodyDto {

	private AccountViewColumnResponseBodyDto columns;

}
