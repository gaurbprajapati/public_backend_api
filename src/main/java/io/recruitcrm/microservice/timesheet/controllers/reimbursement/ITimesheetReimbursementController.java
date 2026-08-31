package io.recruitcrm.microservice.timesheet.controllers.reimbursement;

import io.recruitcrm.microservice.timesheet.dto.reimbursement.CreateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReopenReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdatePayableBillableRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateShareWithClientRequestBodyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface ITimesheetReimbursementController {

	ResponseEntity<?> listReimbursements(@PathVariable Integer timesheetId);

	ResponseEntity<?> createReimbursement(@PathVariable Integer timesheetId, CreateReimbursementRequestBodyDto request);

	ResponseEntity<?> reopenReimbursement(@PathVariable Integer timesheetId, @PathVariable Integer reimbursementId,
			ReopenReimbursementRequestBodyDto request);

	ResponseEntity<?> updatePayableBillable(@PathVariable Integer timesheetId, @PathVariable Integer reimbursementId,
			UpdatePayableBillableRequestBodyDto request);

	ResponseEntity<?> updateShareWithClient(@PathVariable Integer timesheetId, @PathVariable Integer reimbursementId,
			UpdateShareWithClientRequestBodyDto request);

	ResponseEntity<?> updateReimbursement(@PathVariable Integer timesheetId, @PathVariable Integer id,
			UpdateReimbursementRequestBodyDto request);

	ResponseEntity<?> deleteReimbursement(@PathVariable Integer timesheetId, @PathVariable Integer id);

	ResponseEntity<?> updateReimbursementStatus(@PathVariable Integer timesheetId, @PathVariable Integer id,
			UpdateReimbursementStatusRequestBodyDto request);

	ResponseEntity<?> getReimbursementStatusHistory(@PathVariable Integer timesheetId, @PathVariable Integer id);

	ResponseEntity<?> getReimbursementCount(@PathVariable Integer timesheetId);

}
