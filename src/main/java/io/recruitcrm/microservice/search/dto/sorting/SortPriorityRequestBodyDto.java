package io.recruitcrm.microservice.search.dto.sorting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SortPriorityRequestBodyDto {

	private String field;

	private String order;

}