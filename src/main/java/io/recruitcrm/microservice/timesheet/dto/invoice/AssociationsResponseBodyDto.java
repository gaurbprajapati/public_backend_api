package io.recruitcrm.microservice.timesheet.dto.invoice;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssociationsResponseBodyDto {

	private Map<Integer, List<Integer>> associations;

	public AssociationsResponseBodyDto(Integer contactId, Integer companyId, Integer jobId, Integer contractorId,
			List<Integer> dealIds) {
		this.associations = Map.of(2, (contactId != null) ? List.of(contactId) : List.of(), 3,
				(companyId != null) ? List.of(companyId) : List.of(), 4, (jobId != null) ? List.of(jobId) : List.of(),
				5, (contractorId != null) ? List.of(contractorId) : List.of(), 11,
				(dealIds != null) ? dealIds : List.of());
	}

	@JsonValue
	public Map<Integer, List<Integer>> getAssociations() {
		return this.associations;
	}

}