package io.recruitcrm.microservice.timesheet.controllers.reimbursement;

import io.recruitcrm.microservice.timesheet.dto.reimbursement.CreateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementListItemResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdatePayableBillableRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateShareWithClientRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReopenReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.reimbursement.TimesheetReimbursementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/timesheets/{timesheetId}/reimbursements")
public class TimesheetReimbursementController implements ITimesheetReimbursementController {

	private static final String REIMBURSEMENT_UPDATED_SUCCESSFULLY = "Reimbursement updated successfully";

	private final TimesheetReimbursementService timesheetReimbursementService;

	private final APIResponder apiResponder;

	public TimesheetReimbursementController(TimesheetReimbursementService timesheetReimbursementService,
			APIResponder apiResponder) {
		this.timesheetReimbursementService = timesheetReimbursementService;
		this.apiResponder = apiResponder;
	}

	@Override
	@GetMapping("")
	public ResponseEntity<?> listReimbursements(@PathVariable Integer timesheetId) {
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);
		return this.apiResponder.respond(result, "Reimbursements fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PostMapping("")
	public ResponseEntity<?> createReimbursement(@PathVariable Integer timesheetId,
			@Valid @RequestBody CreateReimbursementRequestBodyDto request) {
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.createReimbursement(timesheetId,
				request);
		return this.apiResponder.respond(result, "Reimbursement created successfully", APIResponseType.SUCCESS,
				HttpStatus.CREATED);
	}

	@Override
	@PostMapping("/{reimbursementId}/reopen")
	public ResponseEntity<?> reopenReimbursement(@PathVariable Integer timesheetId,
			@PathVariable Integer reimbursementId, @Valid @RequestBody ReopenReimbursementRequestBodyDto request) {
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.reopenReimbursement(timesheetId,
				reimbursementId, request);
		return this.apiResponder.respond(result, "Reimbursement reopened successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PatchMapping("/{reimbursementId}/payable-billable")
	public ResponseEntity<?> updatePayableBillable(@PathVariable Integer timesheetId,
			@PathVariable Integer reimbursementId, @Valid @RequestBody UpdatePayableBillableRequestBodyDto request) {
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updatePayableBillable(timesheetId,
				reimbursementId, request);
		return this.apiResponder.respond(result, REIMBURSEMENT_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PatchMapping("/{reimbursementId}/share-with-client")
	public ResponseEntity<?> updateShareWithClient(@PathVariable Integer timesheetId,
			@PathVariable Integer reimbursementId, @Valid @RequestBody UpdateShareWithClientRequestBodyDto request) {
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateShareWithClient(timesheetId,
				reimbursementId, request);
		return this.apiResponder.respond(result, REIMBURSEMENT_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PatchMapping("/{id}")
	public ResponseEntity<?> updateReimbursement(@PathVariable Integer timesheetId, @PathVariable Integer id,
			@Valid @RequestBody UpdateReimbursementRequestBodyDto request) {
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateReimbursement(timesheetId, id,
				request);
		return this.apiResponder.respond(result, REIMBURSEMENT_UPDATED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteReimbursement(@PathVariable Integer timesheetId, @PathVariable Integer id) {
		this.timesheetReimbursementService.deleteReimbursement(timesheetId, id);
		return this.apiResponder.respond(null, "Reimbursement deleted successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PatchMapping("/{id}/status")
	public ResponseEntity<?> updateReimbursementStatus(@PathVariable Integer timesheetId, @PathVariable Integer id,
			@Valid @RequestBody UpdateReimbursementStatusRequestBodyDto request) {
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateReimbursementStatus(timesheetId,
				id, request);
		return this.apiResponder.respond(result, "Reimbursement status updated successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@GetMapping("/{id}/status-history")
	public ResponseEntity<?> getReimbursementStatusHistory(@PathVariable Integer timesheetId,
			@PathVariable Integer id) {
		List<ReimbursementStatusHistoryResponseBodyDto> result = this.timesheetReimbursementService
			.getReimbursementStatusHistory(timesheetId, id);
		return this.apiResponder.respond(result, "Status history fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@GetMapping("/count")
	public ResponseEntity<?> getReimbursementCount(@PathVariable Integer timesheetId) {
		Integer reimbursementCount = this.timesheetReimbursementService.getReimbursementCount(timesheetId);
		return this.apiResponder.respond(reimbursementCount, "Reimbursement count fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
