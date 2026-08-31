package io.recruitcrm.microservice.timesheet.services.timesheet;

import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetEmailValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ValidateTimesheetEmailRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import org.springframework.stereotype.Service;

@Service
public class TimesheetEmailValidationService {

	private static final int ENTITY_TYPE_APPROVER = 1;

	private static final int ENTITY_TYPE_CONTRACTOR = 3;

	private final ApproverEmailValidationService approverEmailValidationService;

	private final ContractorEmailValidationService contractorEmailValidationService;

	public TimesheetEmailValidationService(ApproverEmailValidationService approverEmailValidationService,
			ContractorEmailValidationService contractorEmailValidationService) {
		this.approverEmailValidationService = approverEmailValidationService;
		this.contractorEmailValidationService = contractorEmailValidationService;
	}

	public TimesheetEmailValidationResponseBodyDto validateTimesheetEmails(
			ValidateTimesheetEmailRequestBodyDto requestDto) {
		Integer entityTypeId = requestDto.getEntityTypeId();

		if (entityTypeId == null || (entityTypeId != ENTITY_TYPE_APPROVER && entityTypeId != ENTITY_TYPE_CONTRACTOR)) {
			throw new ValidationErrorException("entity_type_id must be 1 or 3");
		}

		if (entityTypeId == ENTITY_TYPE_APPROVER) {
			return this.approverEmailValidationService.validateApproverEmails(requestDto.getTimesheetIds());
		}

		return this.contractorEmailValidationService.validateContractorEmails(requestDto.getTimesheetIds());
	}

}
