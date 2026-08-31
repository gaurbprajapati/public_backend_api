package io.recruitcrm.microservice.timesheet.services.timesheet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetEmailValidationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ValidateTimesheetEmailRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetEmailValidationTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimesheetEmailValidationServiceTests {

	@Mock
	private ApproverEmailValidationService approverEmailValidationService;

	@Mock
	private ContractorEmailValidationService contractorEmailValidationService;

	@InjectMocks
	private TimesheetEmailValidationService timesheetEmailValidationService;

	@Test
	@DisplayName("Validate timesheet emails passes multiple ids to approver service")
	void testValidateTimesheetEmailsMultipleIdsPassesListToApproverService() {
		// Given
		ValidateTimesheetEmailRequestBodyDto request = TimesheetEmailValidationTestDataFactory
			.createValidateTimesheetEmailRequestApproverMultipleIds();
		TimesheetEmailValidationResponseBodyDto expected = TimesheetEmailValidationTestDataFactory
			.createEmptyTimesheetEmailValidationResponseBodyDto();
		given(this.approverEmailValidationService.validateApproverEmails(request.getTimesheetIds()))
			.willReturn(expected);

		// When
		TimesheetEmailValidationResponseBodyDto result = this.timesheetEmailValidationService
			.validateTimesheetEmails(request);

		// Then
		assertThat(result).isEqualTo(expected);
		then(this.approverEmailValidationService).should().validateApproverEmails(request.getTimesheetIds());
		assertThat(request.getTimesheetIds()).hasSize(2);
	}

	@Test
	@DisplayName("Validate timesheet emails delegates to approver service when entity type is approver")
	void testValidateTimesheetEmailsApproverEntityTypeDelegatesToApproverService() {
		// Given
		ValidateTimesheetEmailRequestBodyDto request = TimesheetEmailValidationTestDataFactory
			.createValidateTimesheetEmailRequestApprover();
		TimesheetEmailValidationResponseBodyDto expected = TimesheetEmailValidationTestDataFactory
			.createEmptyTimesheetEmailValidationResponseBodyDto();
		given(this.approverEmailValidationService.validateApproverEmails(request.getTimesheetIds()))
			.willReturn(expected);

		// When
		TimesheetEmailValidationResponseBodyDto result = this.timesheetEmailValidationService
			.validateTimesheetEmails(request);

		// Then
		assertThat(result).isEqualTo(expected);
		then(this.approverEmailValidationService).should().validateApproverEmails(request.getTimesheetIds());
		then(this.contractorEmailValidationService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Validate timesheet emails passes multiple ids to contractor service")
	void testValidateTimesheetEmailsMultipleIdsPassesListToContractorService() {
		// Given
		ValidateTimesheetEmailRequestBodyDto request = TimesheetEmailValidationTestDataFactory
			.createValidateTimesheetEmailRequestContractorMultipleIds();
		TimesheetEmailValidationResponseBodyDto expected = TimesheetEmailValidationTestDataFactory
			.createTimesheetEmailValidationResponseBodyDto();
		given(this.contractorEmailValidationService.validateContractorEmails(request.getTimesheetIds()))
			.willReturn(expected);

		// When
		TimesheetEmailValidationResponseBodyDto result = this.timesheetEmailValidationService
			.validateTimesheetEmails(request);

		// Then
		assertThat(result).isEqualTo(expected);
		then(this.contractorEmailValidationService).should().validateContractorEmails(request.getTimesheetIds());
		assertThat(request.getTimesheetIds()).hasSize(2);
	}

	@Test
	@DisplayName("Validate timesheet emails delegates to contractor service when entity type is contractor")
	void testValidateTimesheetEmailsContractorEntityTypeDelegatesToContractorService() {
		// Given
		ValidateTimesheetEmailRequestBodyDto request = TimesheetEmailValidationTestDataFactory
			.createValidateTimesheetEmailRequestContractor();
		TimesheetEmailValidationResponseBodyDto expected = TimesheetEmailValidationTestDataFactory
			.createTimesheetEmailValidationResponseBodyDto();
		given(this.contractorEmailValidationService.validateContractorEmails(request.getTimesheetIds()))
			.willReturn(expected);

		// When
		TimesheetEmailValidationResponseBodyDto result = this.timesheetEmailValidationService
			.validateTimesheetEmails(request);

		// Then
		assertThat(result).isEqualTo(expected);
		then(this.contractorEmailValidationService).should().validateContractorEmails(request.getTimesheetIds());
		then(this.approverEmailValidationService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Validate timesheet emails throws when entity type is unsupported")
	void testValidateTimesheetEmailsUnsupportedEntityTypeThrowsValidationErrorException() {
		// Given
		ValidateTimesheetEmailRequestBodyDto request = TimesheetEmailValidationTestDataFactory
			.createValidateTimesheetEmailRequestUnsupportedEntityType();

		// When & Then
		assertThatThrownBy(() -> this.timesheetEmailValidationService.validateTimesheetEmails(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("entity_type_id must be 1 or 3");
		then(this.approverEmailValidationService).shouldHaveNoInteractions();
		then(this.contractorEmailValidationService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Validate timesheet emails throws when entity type is agency only id two")
	void testValidateTimesheetEmailsEntityTypeTwoThrowsValidationErrorException() {
		// Given
		ValidateTimesheetEmailRequestBodyDto request = TimesheetEmailValidationTestDataFactory
			.createValidateTimesheetEmailRequestEntityTypeTwo();

		// When & Then
		assertThatThrownBy(() -> this.timesheetEmailValidationService.validateTimesheetEmails(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("entity_type_id must be 1 or 3");
		then(this.approverEmailValidationService).shouldHaveNoInteractions();
		then(this.contractorEmailValidationService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Validate timesheet emails throws when entity type id is null")
	void testValidateTimesheetEmailsNullEntityTypeThrowsValidationErrorException() {
		// Given
		ValidateTimesheetEmailRequestBodyDto request = TimesheetEmailValidationTestDataFactory
			.createValidateTimesheetEmailRequestNullEntityType();

		// When & Then
		assertThatThrownBy(() -> this.timesheetEmailValidationService.validateTimesheetEmails(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("entity_type_id must be 1 or 3");
		then(this.approverEmailValidationService).shouldHaveNoInteractions();
		then(this.contractorEmailValidationService).shouldHaveNoInteractions();
	}

}
