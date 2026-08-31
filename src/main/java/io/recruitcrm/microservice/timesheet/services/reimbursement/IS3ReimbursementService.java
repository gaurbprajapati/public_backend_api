package io.recruitcrm.microservice.timesheet.services.reimbursement;

import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentViewResponseBodyDto;

public interface IS3ReimbursementService {

	ReimbursementDocumentUploadResponseBodyDto generateUploadUrl(ReimbursementDocumentUploadRequestBodyDto request);

	ReimbursementDocumentViewResponseBodyDto viewFile(String documentToken, String fileName, Boolean download);

	void deleteReimbursementFile(String documentToken);

}
