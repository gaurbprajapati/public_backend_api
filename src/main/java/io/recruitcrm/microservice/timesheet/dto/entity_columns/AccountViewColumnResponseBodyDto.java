package io.recruitcrm.microservice.timesheet.dto.entity_columns;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
public class AccountViewColumnResponseBodyDto extends HashMap<String, ColumnAccountViewResponseBodyDto> {

	private static final long serialVersionUID = 1L;

}
