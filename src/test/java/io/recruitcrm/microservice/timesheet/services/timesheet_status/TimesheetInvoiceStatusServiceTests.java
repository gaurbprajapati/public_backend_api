package io.recruitcrm.microservice.timesheet.services.timesheet_status;

import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.BillStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.PaymentStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.PayBillTypeEnum;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_approval.TimesheetApprovalJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetPayBillStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.TimesheetUpdateHelper;
import io.recruitcrm.microservice.timesheet.kafka.KafkaProducerHelper;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;
import io.recruitcrm.microservice.timesheet.repositories.invoice.TimesheetInvoiceRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approval.TimesheetApprovalRepository;
import io.recruitcrm.microservice.timesheet.services.rule_engine.RuleEngineService;
import io.recruitcrm.microservice.timesheet.services.webhook_kafka_event.WebhookEvent;
import io.recruitcrm.microservice.timesheet.services.webhook_kafka_event.WebhookKafkaEventService;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetInvoiceStatusServiceTestDataFactory;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.dao.invoice.InvoicesJpaRepository;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import io.recruitcrm.microservice.timesheet.services.invoice.ITimesheetInvoiceService;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TimesheetInvoiceStatusServiceTests {

	@Mock
	private TimesheetJpaRepository timesheetJpaRepository;

	@Mock
	private TimesheetApprovalRepository timesheetApprovalRepository;

	@Mock
	private TimesheetApprovalJpaRepository timesheetApprovalJpaRepository;

	@Mock
	private TimesheetInvoiceRepository timesheetInvoiceRepository;

	@Mock
	private AuthHolder auth;

	@Mock
	private TimesheetUpdateHelper timesheetUpdateHelper;

	@Mock
	private RuleEngineService ruleEngineService;

	@Mock
	private AccessControlChecker contractStaffingAccessControlChecker;

	@Mock
	private InvoicesJpaRepository invoicesJpaRepository;

	@Mock
	private ITimesheetInvoiceService timesheetInvoiceService;

	@Mock
	private TimesheetRepository timesheetRepository;

	@Mock
	private io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService portalAccessControlService;

	@Mock
	private WebhookKafkaEventService webhookKafkaEventService;

	@Mock
	private KafkaProducerHelper kafkaProducerHelper;

	@InjectMocks
	private TimesheetInvoiceStatusService timesheetInvoiceStatusService;

	private static final Integer TIMESHEET_ID = 1;

	private static final Integer ACCOUNT_ID = 123;

	private static final Integer USER_ID = 456;

	private static final Integer USER_TYPE_ID = AccountUserEnum.USERTYPEID.getId();

	@BeforeEach
	void setUp() {
		// Lenient: only approve-path tests invoke the webhook; strict stubbing would fail
		// all other tests.
		lenient().doNothing().when(this.webhookKafkaEventService).triggerTimesheetWebhookEvent(any(), any());
	}

	@Test
	@DisplayName("Update timesheet status - approvalStatus must be 3 (rejected) or 4 (approved)")
	void testUpdateTimesheetStatusInvalidApprovalStatus() {
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(1);
		requestDto.setRemark(null);

		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("approvalStatus can be only 3 for rejected and 4 for approve timesheet");

		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Update timesheet status - approvalStatus null")
	void testUpdateTimesheetStatusNullApprovalStatus() {
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(null);
		requestDto.setRemark(null);

		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("approvalStatus can be only 3 for rejected and 4 for approve timesheet");

		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Update timesheet status - Timesheet already in approved state")
	void testUpdateTimesheetStatusAlreadyApproved() {
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());
		requestDto.setRemark("Trying to approve again");

		TimesheetApproval currentApproval = new TimesheetApproval();
		currentApproval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID))
			.willReturn(currentApproval);

		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet is already in approved state, no action performed");

		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Update timesheet status - Timesheet already approved, trying to reject")
	void testUpdateTimesheetStatusAlreadyApprovedTryingToReject() {
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.REJECTED.getId());
		requestDto.setRemark("Trying to reject approved timesheet");

		TimesheetApproval currentApproval = new TimesheetApproval();
		currentApproval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID))
			.willReturn(currentApproval);

		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet is already in approved state, no action performed");

		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Update timesheet status - Timesheet already in rejected state, no action can be performed")
	void testUpdateTimesheetStatusAlreadyRejectedThrowsValidationErrorException() {
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());
		requestDto.setRemark("Trying to approve rejected timesheet");

		TimesheetApproval currentApproval = new TimesheetApproval();
		currentApproval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.REJECTED.getId());

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID))
			.willReturn(currentApproval);

		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet is already in rejected state, no action can be performed");

		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Update timesheet status - Timesheet already rejected, trying to reject again")
	void testUpdateTimesheetStatusAlreadyRejectedTryingToRejectAgain() {
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.REJECTED.getId());
		requestDto.setRemark("Trying to reject again");

		TimesheetApproval currentApproval = new TimesheetApproval();
		currentApproval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.REJECTED.getId());

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID))
			.willReturn(currentApproval);

		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Timesheet is already in rejected state, no action can be performed");

		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Update timesheet status for USER persona succeeds when latest approval is OPEN")
	void testUpdateTimesheetStatusForUserPersonaLatestApprovalOpenProceedsSuccessfully() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		TimesheetApproval currentApproval = new TimesheetApproval();
		currentApproval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.OPEN.getId());

		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());
		requestDto.setRemark("Approved after open");

		AuthPrincipal userPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID))
			.willReturn(currentApproval);
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.APPROVED.getId()), eq("Approved after open"));
		willDoNothing().given(this.timesheetInvoiceService)
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));

		// When
		this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto);

		// Then
		then(this.timesheetApprovalRepository).should()
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.APPROVED.getId()), eq("Approved after open"));
		then(this.timesheetInvoiceService).should()
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		then(this.ruleEngineService).should().evaluateRules(any());
		then(this.webhookKafkaEventService).should()
			.triggerTimesheetWebhookEvent(eq(WebhookEvent.TIMESHEET_APPROVED),
					eq(Collections.singletonList(TIMESHEET_ID)));
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Update timesheet status for USER persona should update successfully")
	void testUpdateTimesheetStatusForUserPersonaUpdatesSuccessfully() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());
		requestDto.setRemark("Approved by manager");

		AuthPrincipal userPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID)).willReturn(null);
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.APPROVED.getId()), eq("Approved by manager"));
		willDoNothing().given(this.timesheetInvoiceService)
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));

		// When
		this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto);

		// Then
		then(this.contractStaffingAccessControlChecker).should().allows(any(), any(), any());
		then(this.timesheetApprovalRepository).should()
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.APPROVED.getId()), eq("Approved by manager"));
		then(this.timesheetInvoiceService).should()
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		then(this.ruleEngineService).should().evaluateRules(any());
		then(this.timesheetUpdateHelper).should()
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		then(this.webhookKafkaEventService).should()
			.triggerTimesheetWebhookEvent(eq(WebhookEvent.TIMESHEET_APPROVED),
					eq(Collections.singletonList(TIMESHEET_ID)));
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Update timesheet status for USER persona should throw ResourceNotFoundException when timesheet not found")
	void testUpdateTimesheetStatusForUserPersonaTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());

		AuthPrincipal userPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");

		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Update timesheet status for USER persona should throw ValidationErrorException when rejected without remark")
	void testUpdateTimesheetStatusForUserPersonaRejectedWithoutRemarkThrowsValidationErrorException() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.REJECTED.getId());
		requestDto.setRemark(""); // Empty remark

		AuthPrincipal userPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Remark is required");

		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Update timesheet status for USER persona with rejected status and valid remark should not trigger rule engine")
	void testUpdateTimesheetStatusForUserPersonaRejectedWithValidRemarkSkipsRuleEngine() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.REJECTED.getId());
		requestDto.setRemark("Rejected due to incomplete information");

		AuthPrincipal userPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID)).willReturn(null);
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.REJECTED.getId()), eq("Rejected due to incomplete information"));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));

		// When
		this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto);

		// Then
		then(this.timesheetApprovalRepository).should()
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.REJECTED.getId()), eq("Rejected due to incomplete information"));
		then(this.ruleEngineService).should(never()).evaluateRules(any());
		then(this.timesheetInvoiceService).should(never()).createTimesheetInvoice(any(), any(), any());
		then(this.timesheetUpdateHelper).should()
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		then(this.webhookKafkaEventService).should(never()).triggerTimesheetWebhookEvent(any(), any());
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Update timesheet pay/bill status successfully - Updates existing invoice")
	void testUpdateTimesheetPayBillStatusSuccessful() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		TimesheetInvoice existingInvoice = new TimesheetInvoice();
		existingInvoice.setTimesheetId(TIMESHEET_ID);
		existingInvoice.setPaymentStatusId(PaymentStatusEnum.UN_PAID.getId());
		existingInvoice.setBillingStatusId(BillStatusEnum.UN_BILLED.getId());

		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(PayBillTypeEnum.PAY.getId());
		requestDto.setPayStatusId(PaymentStatusEnum.PAID.getId());
		requestDto.setPayoutPaidOn(1633046400); // 2021-10-01
		requestDto.setPayoutNumber("PAY123");

		// Mock auth behavior for this test
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(existingInvoice);

		// Act
		this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto);

		// Assert - Verify that the existing invoice is updated, not a new one created
		then(this.timesheetInvoiceRepository).should().saveInvoice(existingInvoice);

		// Verify that the invoice fields were updated
		assertThat(existingInvoice.getPaymentStatusId()).isEqualTo(PaymentStatusEnum.PAID.getId());
		assertThat(existingInvoice.getPaymentPaidOn()).isEqualTo(1633046400);
		assertThat(existingInvoice.getPayoutNumber()).isEqualTo("PAY123");
		assertThat(existingInvoice.getUpdatedBy()).isEqualTo(USER_ID);
		assertThat(existingInvoice.getUserTypeId()).isEqualTo(USER_TYPE_ID);
	}

	@ParameterizedTest
	@CsvSource({ "0", "3", "-1", "10" })
	@DisplayName("Update timesheet pay/bill status - payBillType must be 1 or 2")
	void testUpdateTimesheetPayBillStatusInvalidPayBillType(Integer invalidPayBillType) {
		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(invalidPayBillType);

		assertThatThrownBy(
				() -> this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("payBillType is required and can be only 1 or 2");

		then(this.timesheetInvoiceRepository).should(never()).saveInvoice(any());
	}

	@Test
	@DisplayName("Update timesheet pay/bill status - payBillType null")
	void testUpdateTimesheetPayBillStatusNullPayBillType() {
		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(null);

		assertThatThrownBy(
				() -> this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("payBillType is required and can be only 1 or 2");

		then(this.timesheetInvoiceRepository).should(never()).saveInvoice(any());
	}

	@ParameterizedTest
	@CsvSource({ "0", "3", "-1", "10" })
	@DisplayName("Update timesheet pay/bill status - payStatusId must be 1 or 2")
	void testUpdateTimesheetPayBillStatusInvalidPayStatusId(Integer invalidPayStatusId) {
		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(PayBillTypeEnum.PAY.getId());
		requestDto.setPayStatusId(invalidPayStatusId);

		assertThatThrownBy(
				() -> this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("payStatusId is required and can be only 1(paid) or 2(unpaid)");

		then(this.timesheetInvoiceRepository).should(never()).saveInvoice(any());
	}

	@Test
	@DisplayName("Update timesheet pay/bill status - payStatusId null")
	void testUpdateTimesheetPayBillStatusNullPayStatusId() {
		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(PayBillTypeEnum.PAY.getId());
		requestDto.setPayStatusId(null);

		assertThatThrownBy(
				() -> this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("payStatusId is required and can be only 1(paid) or 2(unpaid)");

		then(this.timesheetInvoiceRepository).should(never()).saveInvoice(any());
	}

	@Test
	@DisplayName("Update timesheet pay/bill status - Timesheet not found")
	void testUpdateTimesheetPayBillStatusNotFound() {
		// Arrange
		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(PayBillTypeEnum.PAY.getId());
		requestDto.setPayStatusId(PaymentStatusEnum.PAID.getId());

		// Mock auth behavior for this test
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(
				() -> this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");

		then(this.timesheetInvoiceRepository).should(never()).saveInvoice(any());
	}

	@Test
	@DisplayName("Update timesheet pay/bill status - Create new invoice when not found")
	void testUpdateTimesheetPayBillStatusInvoiceNotFound() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(PayBillTypeEnum.PAY.getId());
		requestDto.setPayStatusId(PaymentStatusEnum.PAID.getId());
		requestDto.setPayoutPaidOn(1633046400); // 2021-10-01
		requestDto.setPayoutNumber("PAY123");

		// Mock auth behavior for this test
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(null);

		// Act
		this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto);

		// Assert - Verify that a new invoice is created and saved
		then(this.timesheetInvoiceRepository).should().saveInvoice(any(TimesheetInvoice.class));
	}

	@Test
	@DisplayName("Update timesheet pay/bill status - Missing payout number for paid status")
	void testUpdateTimesheetPayBillStatusMissingPayoutNumberForPaidStatus() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(TIMESHEET_ID);

		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(PayBillTypeEnum.PAY.getId());
		requestDto.setPayStatusId(PaymentStatusEnum.PAID.getId());
		// Missing payout number for PAID status
		requestDto.setBillStatusId(BillStatusEnum.BILLED.getId());
		requestDto.setInvoiceCreatedOn(1633046400);
		requestDto.setInvoiceNumber("INV123");

		// Mock auth behavior for this test
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(invoice);

		// Act & Assert
		assertThatThrownBy(
				() -> this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Payout number is required");

		then(this.timesheetInvoiceRepository).should(never()).saveInvoice(any());
	}

	@Test
	@DisplayName("Update timesheet status for USER persona with approved status and null remark should succeed")
	void testUpdateTimesheetStatusForUserPersonaApprovedWithNullRemarkSucceeds() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());
		requestDto.setRemark(null);

		AuthPrincipal userPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID)).willReturn(null);
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.APPROVED.getId()), eq(null));
		willDoNothing().given(this.timesheetInvoiceService)
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));

		// When
		this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto);

		// Then
		then(this.timesheetApprovalRepository).should()
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.APPROVED.getId()), eq(null));
		then(this.ruleEngineService).should().evaluateRules(any());
		then(this.timesheetInvoiceService).should()
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		then(this.timesheetUpdateHelper).should()
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		then(this.webhookKafkaEventService).should()
			.triggerTimesheetWebhookEvent(eq(WebhookEvent.TIMESHEET_APPROVED),
					eq(Collections.singletonList(TIMESHEET_ID)));
	}

	@Test
	@DisplayName("Update timesheet status for USER persona with submitted status and whitespace remark should trigger rule engine")
	void testUpdateTimesheetStatusForUserPersonaSubmittedWithWhitespaceRemarkTriggersRuleEngine() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());
		requestDto.setRemark("   ");

		AuthPrincipal userPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID)).willReturn(null);
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.APPROVED.getId()), eq("   "));
		willDoNothing().given(this.timesheetInvoiceService)
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));

		// When
		this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto);

		// Then
		then(this.timesheetApprovalRepository).should()
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.APPROVED.getId()), eq("   "));
		then(this.ruleEngineService).should().evaluateRules(any());
		then(this.timesheetInvoiceService).should()
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		then(this.timesheetUpdateHelper).should()
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		then(this.webhookKafkaEventService).should()
			.triggerTimesheetWebhookEvent(eq(WebhookEvent.TIMESHEET_APPROVED),
					eq(Collections.singletonList(TIMESHEET_ID)));
	}

	@Test
	@DisplayName("Update timesheet pay/bill status - Non-paid payment status should skip payment validations")
	void testUpdateTimesheetPayBillStatusNonPaidSkipsPaymentValidation() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(TIMESHEET_ID);

		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(PayBillTypeEnum.PAY.getId());
		requestDto.setPayStatusId(PaymentStatusEnum.UN_PAID.getId());
		requestDto.setBillStatusId(BillStatusEnum.BILLED.getId());
		requestDto.setInvoiceCreatedOn(1633046400);
		requestDto.setInvoiceNumber("INV123");

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(invoice);

		// When
		this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto);

		// Then
		then(this.timesheetInvoiceRepository).should().saveInvoice(any(TimesheetInvoice.class));
		assertThat(invoice.getPaymentStatusId()).isEqualTo(PaymentStatusEnum.UN_PAID.getId());
	}

	@Test
	@DisplayName("Update timesheet pay/bill status - Non-billed billing status should skip billing validations")
	void testUpdateTimesheetPayBillStatusNonBilledSkipsBillingValidation() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(TIMESHEET_ID);

		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(PayBillTypeEnum.PAY.getId());
		requestDto.setPayStatusId(PaymentStatusEnum.PAID.getId());
		requestDto.setPayoutPaidOn(1633046400);
		requestDto.setPayoutNumber("PAY123");
		requestDto.setBillStatusId(2); // Non-billed status (using ID 2 instead of
										// PENDING)
		// No billing date or invoice number should be required

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(invoice);

		// Act
		this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto);

		// Assert
		then(this.timesheetInvoiceRepository).should().saveInvoice(any(TimesheetInvoice.class));
	}

	@Test
	@DisplayName("Update timesheet pay/bill status - Unpaid persists payout fields from request without paid validation")
	void testUpdateTimesheetPayBillStatusNonPaidDoesNotSetPaymentFields() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(TIMESHEET_ID);

		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(PayBillTypeEnum.PAY.getId());
		requestDto.setPayStatusId(PaymentStatusEnum.UN_PAID.getId());
		requestDto.setPayoutPaidOn(1633046400);
		requestDto.setPayoutNumber("PAY123");
		requestDto.setBillStatusId(BillStatusEnum.UN_BILLED.getId());

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(invoice);

		// When
		this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto);

		// Then
		then(this.timesheetInvoiceRepository).should().saveInvoice(any(TimesheetInvoice.class));
		assertThat(invoice.getPaymentStatusId()).isEqualTo(PaymentStatusEnum.UN_PAID.getId());
		assertThat(invoice.getPayoutNumber()).isEqualTo("PAY123");
	}

	@Test
	@DisplayName("Update timesheet pay/bill status - Bill type unpaid persists without pay validation")
	void testUpdateTimesheetPayBillStatusNonBilledDoesNotSetBillingFields() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(TIMESHEET_ID);

		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = TimesheetInvoiceStatusServiceTestDataFactory
			.createPayBillBillUnpaidRequest();
		requestDto.setInvoiceCreatedOn(1633046400);
		requestDto.setInvoiceNumber("INV123");

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(invoice);

		// When
		this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto);

		// Then
		then(this.timesheetInvoiceRepository).should().saveInvoice(any(TimesheetInvoice.class));
		assertThat(invoice.getPaymentStatusId()).isNull();
		assertThat(invoice.getUpdatedBy()).isEqualTo(USER_ID);
	}

	@Test
	@DisplayName("Update timesheet pay/bill status - Non-pay bill type should skip payment validation")
	void testUpdateTimesheetPayBillStatusNonPayBillTypeSkipsPaymentValidation() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(TIMESHEET_ID);

		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(PayBillTypeEnum.BILL.getId());
		requestDto.setPayStatusId(PaymentStatusEnum.PAID.getId());

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(invoice);

		// When
		this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto);

		// Then
		then(this.timesheetInvoiceRepository).should().saveInvoice(any(TimesheetInvoice.class));
	}

	@Test
	@DisplayName("Update timesheet pay/bill status - Empty payout number should throw validation error")
	void testUpdateTimesheetPayBillStatusEmptyPayoutNumberThrowsValidationError() {
		// Arrange
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(TIMESHEET_ID);

		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayBillType(PayBillTypeEnum.PAY.getId());
		requestDto.setPayStatusId(PaymentStatusEnum.PAID.getId());
		requestDto.setPayoutNumber("   "); // Empty payout number

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);

		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(TIMESHEET_ID, ACCOUNT_ID)).willReturn(invoice);

		// Act & Assert
		assertThatThrownBy(
				() -> this.timesheetInvoiceStatusService.updateTimesheetPayBillStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Payout number is required");

		then(this.timesheetInvoiceRepository).should(never()).saveInvoice(any());
	}

	@Test
	@DisplayName("Update timesheet status for CONTACT persona should update successfully")
	void testUpdateTimesheetStatusForContactPersonaUpdatesSuccessfully() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());
		requestDto.setRemark("Approved by contact");

		Integer contactId = 789;
		Integer contactUserTypeId = UserTypeEnum.COMPANY_CONTACT.getId();

		ContactPrincipal contactPrincipal = org.mockito.Mockito.mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(contactId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		willDoNothing().given(this.portalAccessControlService)
			.validateApproverAccess(eq(TIMESHEET_ID), eq(contactId), eq(contactUserTypeId), eq(ACCOUNT_ID));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID)).willReturn(null);
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(contactId), eq(contactUserTypeId),
					eq(ApprovalStatusEnum.APPROVED.getId()), eq("Approved by contact"));
		willDoNothing().given(this.timesheetInvoiceService)
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(contactId), eq(contactUserTypeId));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(contactId), eq(contactUserTypeId));

		// When
		this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto);

		// Then
		then(this.portalAccessControlService).should()
			.validateApproverAccess(eq(TIMESHEET_ID), eq(contactId), eq(contactUserTypeId), eq(ACCOUNT_ID));
		then(this.timesheetApprovalRepository).should()
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(contactId), eq(contactUserTypeId),
					eq(ApprovalStatusEnum.APPROVED.getId()), eq("Approved by contact"));
		then(this.timesheetInvoiceService).should()
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(contactId), eq(contactUserTypeId));
		then(this.ruleEngineService).should().evaluateRules(any());
		then(this.timesheetUpdateHelper).should()
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(contactId), eq(contactUserTypeId));
		then(this.webhookKafkaEventService).should()
			.triggerTimesheetWebhookEvent(eq(WebhookEvent.TIMESHEET_APPROVED),
					eq(Collections.singletonList(TIMESHEET_ID)));
	}

	@Test
	@DisplayName("Update timesheet status for CONTACT persona should throw ResourceNotFoundException when timesheet not found")
	void testUpdateTimesheetStatusForContactPersonaTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());

		Integer contactId = 789;
		Integer contactUserTypeId = UserTypeEnum.COMPANY_CONTACT.getId();

		ContactPrincipal contactPrincipal = org.mockito.Mockito.mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(contactId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		org.mockito.BDDMockito.willThrow(new ResourceNotFoundException("Timesheet", TIMESHEET_ID))
			.given(this.portalAccessControlService)
			.validateApproverAccess(eq(TIMESHEET_ID), eq(contactId), eq(contactUserTypeId), eq(ACCOUNT_ID));

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");

		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Update timesheet status for CONTACT persona with rejected status should throw ValidationErrorException when remark is null")
	void testUpdateTimesheetStatusForContactPersonaRejectedWithNullRemarkThrowsValidationErrorException() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.REJECTED.getId());
		requestDto.setRemark(null);

		Integer contactId = 789;
		Integer contactUserTypeId = UserTypeEnum.COMPANY_CONTACT.getId();

		ContactPrincipal contactPrincipal = org.mockito.Mockito.mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(contactId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		willDoNothing().given(this.portalAccessControlService)
			.validateApproverAccess(eq(TIMESHEET_ID), eq(contactId), eq(contactUserTypeId), eq(ACCOUNT_ID));

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Remark is required");

		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Update timesheet status should throw UnauthorizedAccessException for unknown persona type")
	void testUpdateTimesheetStatusUnknownPersonaTypeThrowsUnauthorizedAccessException() {
		// Given
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID)).willReturn(null);

		AuthPrincipal unknownPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(unknownPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.auth.getUnifiedPrincipal()).willReturn(unknownPrincipal);

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Unknown persona type");

		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Update timesheet status for USER persona should throw UnauthorizedAccessException when access control denies")
	void testUpdateTimesheetStatusForUserPersonaAccessControlDeniesThrowsUnauthorizedAccessException() {
		// Given
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());
		requestDto.setRemark("Approved by manager");

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID)).willReturn(null);

		AuthPrincipal userPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

		org.mockito.BDDMockito.willThrow(new UnauthorizedAccessException())
			.given(this.contractStaffingAccessControlChecker)
			.allows(any(), any(), any());

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(UnauthorizedAccessException.class);

		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(any(), any());
		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Update timesheet status for CONTACT persona should throw ValidationErrorException when user is not an approver")
	void testUpdateTimesheetStatusForContactPersonaNotAnApproverThrowsValidationErrorException() {
		// Given
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());
		requestDto.setRemark("Approved by contact");

		Integer contactId = 789;

		ContactPrincipal contactPrincipal = org.mockito.Mockito.mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(contactId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		org.mockito.BDDMockito
			.willThrow(new ValidationErrorException("User is not authorized to approve this timesheet"))
			.given(this.portalAccessControlService)
			.validateApproverAccess(eq(TIMESHEET_ID), eq(contactId), eq(UserTypeEnum.COMPANY_CONTACT.getId()),
					eq(ACCOUNT_ID));

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("User is not authorized to approve this timesheet");

		then(this.portalAccessControlService).should()
			.validateApproverAccess(eq(TIMESHEET_ID), eq(contactId), eq(UserTypeEnum.COMPANY_CONTACT.getId()),
					eq(ACCOUNT_ID));
		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("performTimesheetStatusUpdate with SUBMITTED invokes invoice and rule engine without webhook")
	void testPerformTimesheetStatusUpdateSubmittedTriggersInvoiceAndRuleEngineViaReflection() throws Exception {
		// Given
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.SUBMITTED.getId());
		requestDto.setRemark("Workflow submitted");

		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq("Workflow submitted"));
		willDoNothing().given(this.timesheetInvoiceService)
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));

		Method method = TimesheetInvoiceStatusService.class.getDeclaredMethod("performTimesheetStatusUpdate",
				Integer.class, UpdateTimesheetStatusRequestBodyDto.class, Integer.class, Integer.class, Integer.class,
				String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetInvoiceStatusService, TIMESHEET_ID, requestDto, ACCOUNT_ID, USER_ID, USER_TYPE_ID,
				"Submitter Name");

		// Then
		then(this.timesheetInvoiceService).should()
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		then(this.ruleEngineService).should().evaluateRules(any());
		then(this.webhookKafkaEventService).should(never()).triggerTimesheetWebhookEvent(any(), any());
		then(this.timesheetUpdateHelper).should()
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
	}

	@Test
	@DisplayName("updatePaymentStatus with null payStatusId skips secondary payout assignments via reflection")
	void testUpdatePaymentStatusNullPayStatusIdSkipsInnerBlockViaReflection() throws Exception {
		// Given
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(TIMESHEET_ID);
		invoice.setPaymentStatusId(PaymentStatusEnum.PAID.getId());
		invoice.setPayoutNumber("UNCHANGED");

		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayStatusId(null);
		requestDto.setPayoutPaidOn(424242);
		requestDto.setPayoutNumber("SHOULD_NOT_APPLY");

		Method method = TimesheetInvoiceStatusService.class.getDeclaredMethod("updatePaymentStatus",
				TimesheetInvoice.class, UpdateTimesheetPayBillStatusRequestBodyDto.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetInvoiceStatusService, invoice, requestDto);

		// Then
		assertThat(invoice.getPaymentStatusId()).isNull();
		assertThat(invoice.getPayoutNumber()).isEqualTo("UNCHANGED");
	}

	@Test
	@DisplayName("Update timesheet status for USER persona should throw ValidationErrorException when rejected with null remark")
	void testUpdateTimesheetStatusForUserPersonaRejectedWithNullRemarkThrowsValidationErrorException() {
		// Given
		Timesheet timesheet = new Timesheet();
		timesheet.setId(TIMESHEET_ID);
		timesheet.setAccountId(ACCOUNT_ID);

		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.REJECTED.getId());
		requestDto.setRemark(null);

		AuthPrincipal userPrincipal = org.mockito.Mockito.mock(AuthPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(ACCOUNT_ID);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(USER_ID);

		willDoNothing().given(this.contractStaffingAccessControlChecker).allows(any(), any(), any());
		given(this.timesheetJpaRepository.findByIdAndAccountId(TIMESHEET_ID, ACCOUNT_ID))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(TIMESHEET_ID)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetInvoiceStatusService.updateTimesheetStatus(TIMESHEET_ID, requestDto))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Remark is required");

		then(this.timesheetApprovalRepository).should(never())
			.createTimesheetApproval(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("performTimesheetStatusUpdate with APPROVED invokes approved reminder notification via reflection")
	void testPerformTimesheetStatusUpdateApprovedTriggersApprovedReminderViaReflection() throws Exception {
		// Given
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.APPROVED.getId());
		requestDto.setRemark("Approved in workflow");

		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.APPROVED.getId()), eq("Approved in workflow"));
		willDoNothing().given(this.timesheetInvoiceService)
			.createTimesheetInvoice(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));

		Method method = TimesheetInvoiceStatusService.class.getDeclaredMethod("performTimesheetStatusUpdate",
				Integer.class, UpdateTimesheetStatusRequestBodyDto.class, Integer.class, Integer.class, Integer.class,
				String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetInvoiceStatusService, TIMESHEET_ID, requestDto, ACCOUNT_ID, USER_ID, USER_TYPE_ID,
				"Approver Name");

		// Then
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
		then(this.webhookKafkaEventService).should()
			.triggerTimesheetWebhookEvent(eq(WebhookEvent.TIMESHEET_APPROVED),
					eq(Collections.singletonList(TIMESHEET_ID)));
	}

	@Test
	@DisplayName("updatePaymentStatus with PAID status sets payout fields via reflection")
	void testUpdatePaymentStatusPaidStatusSetsPayoutFieldsViaReflection() throws Exception {
		// Given
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setTimesheetId(TIMESHEET_ID);

		UpdateTimesheetPayBillStatusRequestBodyDto requestDto = new UpdateTimesheetPayBillStatusRequestBodyDto();
		requestDto.setPayStatusId(PaymentStatusEnum.PAID.getId());
		requestDto.setPayoutPaidOn(1633046400);
		requestDto.setPayoutNumber("PAY-001");

		Method method = TimesheetInvoiceStatusService.class.getDeclaredMethod("updatePaymentStatus",
				TimesheetInvoice.class, UpdateTimesheetPayBillStatusRequestBodyDto.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetInvoiceStatusService, invoice, requestDto);

		// Then
		assertThat(invoice.getPaymentStatusId()).isEqualTo(PaymentStatusEnum.PAID.getId());
		assertThat(invoice.getPaymentPaidOn()).isEqualTo(Integer.valueOf(1633046400));
		assertThat(invoice.getPayoutNumber()).isEqualTo("PAY-001");
	}

	@Test
	@DisplayName("performTimesheetStatusUpdate with REJECTED invokes rejected reminder notification via reflection")
	void testPerformTimesheetStatusUpdateRejectedTriggersRejectedReminderViaReflection() throws Exception {
		// Given
		UpdateTimesheetStatusRequestBodyDto requestDto = new UpdateTimesheetStatusRequestBodyDto();
		requestDto.setApprovalStatus(ApprovalStatusEnum.REJECTED.getId());
		requestDto.setRemark("Rejected in workflow");

		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID),
					eq(ApprovalStatusEnum.REJECTED.getId()), eq("Rejected in workflow"));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(eq(TIMESHEET_ID), eq(USER_ID), eq(USER_TYPE_ID));

		Method method = TimesheetInvoiceStatusService.class.getDeclaredMethod("performTimesheetStatusUpdate",
				Integer.class, UpdateTimesheetStatusRequestBodyDto.class, Integer.class, Integer.class, Integer.class,
				String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetInvoiceStatusService, TIMESHEET_ID, requestDto, ACCOUNT_ID, USER_ID, USER_TYPE_ID,
				"Approver Name");

		// Then
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
		then(this.webhookKafkaEventService).should(never()).triggerTimesheetWebhookEvent(any(), any());
	}

	@Test
	@DisplayName("publishTimesheetNotification should return early when timesheet ids are null via reflection")
	void testPublishTimesheetNotificationNullIdsReturnsEarlyViaReflection() throws Exception {
		// Given
		Method method = TimesheetInvoiceStatusService.class.getDeclaredMethod("publishTimesheetNotification",
				List.class, Integer.class, Integer.class, String.class, String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetInvoiceStatusService, null, ACCOUNT_ID, USER_TYPE_ID,
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_APPROVED, "Performer");

		// Then
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("publishTimesheetNotification should return early when timesheet ids are empty via reflection")
	void testPublishTimesheetNotificationEmptyIdsReturnsEarlyViaReflection() throws Exception {
		// Given
		Method method = TimesheetInvoiceStatusService.class.getDeclaredMethod("publishTimesheetNotification",
				List.class, Integer.class, Integer.class, String.class, String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetInvoiceStatusService, Collections.emptyList(), ACCOUNT_ID, USER_TYPE_ID,
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_APPROVED, "Performer");

		// Then
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

}