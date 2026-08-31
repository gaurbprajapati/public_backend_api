package io.recruitcrm.microservice.timesheet.dto.reimbursement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementDocumentUploadResponseBodyDto {

	private String documentToken;

	private String documentFileName;

	private String presignedUploadUrl;

	private int expiresInMinutes;

}
