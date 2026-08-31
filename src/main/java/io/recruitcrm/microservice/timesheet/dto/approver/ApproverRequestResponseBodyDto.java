package io.recruitcrm.microservice.timesheet.dto.approver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@AtLeastOneApprover
public class ApproverRequestResponseBodyDto {

	private List<Integer> agencyIds;

	private List<Integer> clientIds;

}
