package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementDocumentViewResponseBodyDto {

	private String documentFileName;

	private String presignedViewUrl;

	private int expiresInMinutes;

	private List<Map<String, String>> type;

}
