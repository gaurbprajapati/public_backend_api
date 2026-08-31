package io.recruitcrm.microservice.timesheet.controllers.reimbursement;

import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentViewResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.reimbursement.IS3ReimbursementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/reimbursements/documents")
public class ReimbursementDocumentController implements IReimbursementDocumentController {

	private final IS3ReimbursementService s3ReimbursementService;

	private final APIResponder apiResponder;

	public ReimbursementDocumentController(IS3ReimbursementService s3ReimbursementService, APIResponder apiResponder) {
		this.s3ReimbursementService = s3ReimbursementService;
		this.apiResponder = apiResponder;
	}

	@Override
	@PostMapping("")
	public ResponseEntity<?> generateUploadUrl(@Valid @RequestBody ReimbursementDocumentUploadRequestBodyDto request) {
		ReimbursementDocumentUploadResponseBodyDto result = this.s3ReimbursementService.generateUploadUrl(request);
		return this.apiResponder.respond(result, "Upload URL generated successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@GetMapping("/view")
	public ResponseEntity<?> viewDocument(@RequestParam("documentToken") String documentToken,
			@RequestParam(value = "fileName", required = false) String fileName,
			@RequestParam(value = "download", defaultValue = "false") Boolean download) {
		ReimbursementDocumentViewResponseBodyDto result = this.s3ReimbursementService.viewFile(documentToken, fileName,
				download);
		return this.apiResponder.respond(result, "Document view URL generated successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

}
