package io.recruitcrm.microservice.timesheet.controllers.reimbursement;

import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadRequestBodyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

public interface IReimbursementDocumentController {

	ResponseEntity<?> generateUploadUrl(ReimbursementDocumentUploadRequestBodyDto request);

	ResponseEntity<?> viewDocument(@RequestParam("documentToken") String documentToken,
			@RequestParam(value = "fileName", required = false) String fileName,
			@RequestParam(value = "download", defaultValue = "false") Boolean download);

}
