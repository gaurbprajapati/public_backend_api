package io.recruitcrm.microservice.timesheet.services.reimbursement;

import io.recruitcrm.microservice.timesheet.dto.reimbursement.CreateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementListItemResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReopenReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdatePayableBillableRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateShareWithClientRequestBodyDto;

import java.util.List;

public interface ITimesheetReimbursementService {

	List<ReimbursementListItemResponseBodyDto> listReimbursements(Integer timesheetId);

	ReimbursementResponseBodyDto createReimbursement(Integer timesheetId, CreateReimbursementRequestBodyDto request);

	ReimbursementResponseBodyDto reopenReimbursement(Integer timesheetId, Integer reimbursementId,
			ReopenReimbursementRequestBodyDto request);

	ReimbursementResponseBodyDto updatePayableBillable(Integer timesheetId, Integer reimbursementId,
			UpdatePayableBillableRequestBodyDto request);

	ReimbursementResponseBodyDto updateShareWithClient(Integer timesheetId, Integer reimbursementId,
			UpdateShareWithClientRequestBodyDto request);

	ReimbursementResponseBodyDto updateReimbursement(Integer timesheetId, Integer id,
			UpdateReimbursementRequestBodyDto request);

	void deleteReimbursement(Integer timesheetId, Integer id);

	ReimbursementResponseBodyDto updateReimbursementStatus(Integer timesheetId, Integer id,
			UpdateReimbursementStatusRequestBodyDto request);

	List<ReimbursementStatusHistoryResponseBodyDto> getReimbursementStatusHistory(Integer timesheetId, Integer id);

	Integer getReimbursementCount(Integer timesheetId);

}
