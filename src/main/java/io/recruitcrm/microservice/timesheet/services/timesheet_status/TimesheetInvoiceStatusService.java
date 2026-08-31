package io.recruitcrm.microservice.timesheet.services.timesheet_status;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.PaymentStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.PayBillTypeEnum;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.dao.invoice.InvoicesJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_approval.TimesheetApprovalJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.kafka.ReminderNotificationEventType;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetPayBillStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdateTimesheetStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.TimesheetUpdateHelper;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.repositories.invoice.TimesheetInvoiceRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approval.TimesheetApprovalRepository;
import io.recruitcrm.microservice.timesheet.kafka.KafkaProducerHelper;
import io.recruitcrm.microservice.timesheet.services.invoice.ITimesheetInvoiceService;
import io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineRequestBodyDto;
import io.recruitcrm.microservice.timesheet.services.rule_engine.RuleEngineService;
import io.recruitcrm.microservice.timesheet.services.webhook_kafka_event.WebhookEvent;
import io.recruitcrm.microservice.timesheet.services.webhook_kafka_event.WebhookKafkaEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.recruitcrm.microservice.timesheet.helpers.GenericHelper;

import java.time.Instant;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TimesheetInvoiceStatusService implements ITimesheetInvoiceStatusService {

	final AuthHolder auth;

	final TimesheetInvoiceRepository timesheetInvoiceRepository;

	final TimesheetJpaRepository timesheetJpaRepository;

	final TimesheetApprovalRepository timesheetApprovalRepository;

	final TimesheetApprovalJpaRepository timesheetApprovalJpaRepository;

	final TimesheetUpdateHelper timesheetUpdateHelper;

	private final RuleEngineService ruleEngineService;

	final AccessControlChecker contractStaffingAccessControlChecker;

	final InvoicesJpaRepository invoicesJpaRepository;

	final ITimesheetInvoiceService timesheetInvoiceService;

	final TimesheetRepository timesheetRepository;

	final PortalAccessControlService portalAccessControlService;

	final WebhookKafkaEventService webhookKafkaEventService;

	private final KafkaProducerHelper kafkaProducerHelper;

	public TimesheetInvoiceStatusService(TimesheetInvoiceRepository timesheetInvoiceRepository,
			TimesheetJpaRepository timesheetJpaRepository, TimesheetApprovalRepository timesheetApprovalRepository,
			TimesheetApprovalJpaRepository timesheetApprovalJpaRepository, AuthHolder auth,
			TimesheetUpdateHelper timesheetUpdateHelper, RuleEngineService ruleEngineService,
			AccessControlChecker contractStaffingAccessControlChecker, InvoicesJpaRepository invoicesJpaRepository,
			ITimesheetInvoiceService timesheetInvoiceService, TimesheetRepository timesheetRepository,
			PortalAccessControlService portalAccessControlService, WebhookKafkaEventService webhookKafkaEventService,
			KafkaProducerHelper kafkaProducerHelper) {
		this.timesheetInvoiceRepository = timesheetInvoiceRepository;
		this.timesheetJpaRepository = timesheetJpaRepository;
		this.timesheetApprovalRepository = timesheetApprovalRepository;
		this.timesheetApprovalJpaRepository = timesheetApprovalJpaRepository;
		this.auth = auth;
		this.contractStaffingAccessControlChecker = contractStaffingAccessControlChecker;
		this.timesheetUpdateHelper = timesheetUpdateHelper;
		this.ruleEngineService = ruleEngineService;
		this.timesheetInvoiceService = timesheetInvoiceService;
		this.invoicesJpaRepository = invoicesJpaRepository;
		this.timesheetRepository = timesheetRepository;
		this.portalAccessControlService = portalAccessControlService;
		this.webhookKafkaEventService = webhookKafkaEventService;
		this.kafkaProducerHelper = kafkaProducerHelper;

	}

	@Override
	@WriterRoute
	@Transactional
	public void updateTimesheetStatus(Integer timesheetId, UpdateTimesheetStatusRequestBodyDto requestDto) {
		Integer approvalStatus = requestDto.getApprovalStatus();
		if (approvalStatus == null || (!Objects.equals(approvalStatus, ApprovalStatusEnum.REJECTED.getId())
				&& !Objects.equals(approvalStatus, ApprovalStatusEnum.APPROVED.getId()))) {
			throw new ValidationErrorException("approvalStatus can be only 3 for rejected and 4 for approve timesheet");
		}

		// Check if timesheet is already in approved or rejected state
		TimesheetApproval currentApproval = this.timesheetApprovalJpaRepository
			.findFirstByTimesheetIdOrderByIdDesc(timesheetId);
		if (currentApproval != null && Objects.equals(currentApproval.getTimesheetApprovalStatusTypeId(),
				ApprovalStatusEnum.APPROVED.getId())) {
			throw new ValidationErrorException("Timesheet is already in approved state, no action performed");
		}
		if (currentApproval != null && Objects.equals(currentApproval.getTimesheetApprovalStatusTypeId(),
				ApprovalStatusEnum.REJECTED.getId())) {
			throw new ValidationErrorException("Timesheet is already in rejected state, no action can be performed");
		}

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();
		String performerDisplayName = principal.getFullName();

		switch (principal.getPrincipalType()) {
			case USER -> this.updateTimesheetStatusForUser(timesheetId, requestDto, performerDisplayName);
			case CONTACT ->
				this.updateTimesheetStatusForContact(timesheetId, requestDto, principal, performerDisplayName);
			default -> throw new UnauthorizedAccessException("Unknown persona type");
		}
	}

	/**
	 * Update timesheet status for USER persona (agency users) Performs role-based access
	 * control
	 */
	private void updateTimesheetStatusForUser(Integer timesheetId, UpdateTimesheetStatusRequestBodyDto requestDto,
			String performerDisplayName) {
		// Verify timesheet exists
		PermissionCheckContext permissionCheckContext = new PermissionCheckContext();
		permissionCheckContext.setPermission(Permission.APPROVE_TIMESHEET);
		permissionCheckContext.setPermissionLevel(PermissionLevel.YES);

		AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
		metadataContext.setTimesheetId(timesheetId);
		// No specific timesheet data needed for create permission

		this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, permissionCheckContext, metadataContext);

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Optional<Timesheet> timesheet = this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId);
		if (timesheet.isEmpty()) {
			throw new ResourceNotFoundException("Timesheet", timesheetId);
		}

		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();

		// Call common update logic
		this.performTimesheetStatusUpdate(timesheetId, requestDto, accountId, userId, userTypeId, performerDisplayName);
	}

	/**
	 * Update timesheet status for CONTACT persona Validates timesheet belongs to
	 * contact's company
	 */
	private void updateTimesheetStatusForContact(Integer timesheetId, UpdateTimesheetStatusRequestBodyDto requestDto,
			AuthPrincipal principal, String performerDisplayName) {
		ContactPrincipal contactPrincipal = (ContactPrincipal) principal;
		Integer accountId = contactPrincipal.getOrganizationIdentifier();
		Integer userId = contactPrincipal.getContactId();
		Integer userTypeId = UserTypeEnum.COMPANY_CONTACT.getId();

		// Validate approver access using portal access control service
		this.portalAccessControlService.validateApproverAccess(timesheetId, userId, userTypeId, accountId);

		// Call common update logic
		this.performTimesheetStatusUpdate(timesheetId, requestDto, accountId, userId, userTypeId, performerDisplayName);
	}

	/**
	 * Common timesheet status update logic Called after persona-specific access control
	 */
	private void performTimesheetStatusUpdate(Integer timesheetId, UpdateTimesheetStatusRequestBodyDto requestDto,
			Integer accountId, Integer userId, Integer userTypeId, String performerDisplayName) {
		// check if approvalStatus == 3 then remark is mandatory
		if (Objects.equals(requestDto.getApprovalStatus(), ApprovalStatusEnum.REJECTED.getId())
				&& (requestDto.getRemark() == null || requestDto.getRemark().trim().isEmpty())) {
			throw new ValidationErrorException("Remark is required when approval status is rejected");
		}

		// Create new timesheet approval record
		this.timesheetApprovalRepository.createTimesheetApproval(timesheetId, userId, userTypeId,
				requestDto.getApprovalStatus(), requestDto.getRemark());

		if (Objects.equals(requestDto.getApprovalStatus(), ApprovalStatusEnum.APPROVED.getId())
				|| Objects.equals(requestDto.getApprovalStatus(), ApprovalStatusEnum.SUBMITTED.getId())) {
			// Create timesheet invoice when approving timesheet
			this.timesheetInvoiceService.createTimesheetInvoice(timesheetId, userId, userTypeId);

			this.invokeAndCacheRuleEngineEvaluationResult(timesheetId);
		}

		if (Objects.equals(requestDto.getApprovalStatus(), ApprovalStatusEnum.APPROVED.getId())) {
			GenericHelper.runAfterCommitOrNow(() -> this.webhookKafkaEventService
				.triggerTimesheetWebhookEvent(WebhookEvent.TIMESHEET_APPROVED, Collections.singletonList(timesheetId)));
		}

		if (Objects.equals(requestDto.getApprovalStatus(), ApprovalStatusEnum.APPROVED.getId())) {
			this.publishTimesheetNotification(List.of(timesheetId), accountId, userTypeId,
					TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_APPROVED, performerDisplayName);
		}
		else if (Objects.equals(requestDto.getApprovalStatus(), ApprovalStatusEnum.REJECTED.getId())) {
			this.publishTimesheetNotification(List.of(timesheetId), accountId, userTypeId,
					TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_REJECTED, performerDisplayName);
		}

		// Update timesheets updated by and updated on
		updateTimesheetLastModified(timesheetId, userId, userTypeId);
	}

	private void publishTimesheetNotification(final List<Integer> timesheetIds, final Integer accountId,
			final Integer createdByUserTypeId, final String eventName, final String performerDisplayName) {
		if (timesheetIds == null || timesheetIds.isEmpty()) {
			return;
		}
		final TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				java.util.UUID.randomUUID().toString(), new ArrayList<>(timesheetIds), eventName, accountId,
				createdByUserTypeId, UserTypeEnum.CONTRACTOR.getId(), ReminderNotificationEventType.REALTIME, false,
				true, true, performerDisplayName, null);
		this.kafkaProducerHelper.sendTimesheetReminderNotification(payload);
	}

	private void invokeAndCacheRuleEngineEvaluationResult(Integer timesheetId) {
		RuleEngineRequestBodyDto ruleEngineRequestBodyDto = RuleEngineRequestBodyDto.builder()
			.timesheetId(timesheetId)
			.refreshStoredData(true)
			.build();
		this.ruleEngineService.evaluateRules(ruleEngineRequestBodyDto);
	}

	@Override
	@WriterRoute
	@Transactional
	public void updateTimesheetPayBillStatus(Integer timesheetId,
			UpdateTimesheetPayBillStatusRequestBodyDto requestDto) {
		Integer payBillType = requestDto.getPayBillType();
		if (payBillType == null || (!Objects.equals(payBillType, PayBillTypeEnum.PAY.getId())
				&& !Objects.equals(payBillType, PayBillTypeEnum.BILL.getId()))) {
			throw new ValidationErrorException("payBillType is required and can be only 1 or 2");
		}

		Integer payStatusId = requestDto.getPayStatusId();
		if (payStatusId == null || (!Objects.equals(payStatusId, PaymentStatusEnum.PAID.getId())
				&& !Objects.equals(payStatusId, PaymentStatusEnum.UN_PAID.getId()))) {
			throw new ValidationErrorException("payStatusId is required and can be only 1(paid) or 2(unpaid)");
		}

		// Verify timesheet exists
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Optional<Timesheet> timesheet = this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId);
		if (timesheet.isEmpty()) {
			throw new ResourceNotFoundException("Timesheet", timesheetId);
		}

		// Find existing invoice record or create new one if not found
		TimesheetInvoice invoice = this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId);
		if (invoice == null) {
			// Create new invoice if none exists
			invoice = createNewInvoice(timesheetId);
		}
		else {
			// Update audit fields for existing invoice
			updateInvoiceAuditFields(invoice);
		}

		if (Objects.equals(requestDto.getPayBillType(), PayBillTypeEnum.PAY.getId())) {
			validatePaymentStatus(requestDto);
			updatePaymentStatus(invoice, requestDto);
		}

		// Save the updated invoice record
		this.timesheetInvoiceRepository.saveInvoice(invoice);
	}

	private void updateTimesheetLastModified(Integer timesheetId, Integer userId, Integer userTypeId) {
		this.timesheetUpdateHelper.updateTimesheetLastModified(timesheetId, userId, userTypeId);
	}

	private void updateInvoiceAuditFields(TimesheetInvoice invoice) {
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		Integer currentTimestamp = getCurrentUnixTimestamp();
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();

		invoice.setUpdatedBy(userId);
		invoice.setUpdatedOn(currentTimestamp);
		invoice.setUserTypeId(userTypeId);
	}

	private TimesheetInvoice createNewInvoice(Integer timesheetId) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		Integer currentTimestamp = getCurrentUnixTimestamp();
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();

		TimesheetInvoice timesheetInvoice = new TimesheetInvoice();
		timesheetInvoice.setTimesheetId(timesheetId);
		timesheetInvoice.setAccountId(accountId);
		timesheetInvoice.setUpdatedBy(userId);
		timesheetInvoice.setUpdatedOn(currentTimestamp);
		timesheetInvoice.setUserTypeId(userTypeId);

		return timesheetInvoice;
	}

	private void validatePaymentStatus(UpdateTimesheetPayBillStatusRequestBodyDto requestDto) {
		if (Objects.equals(requestDto.getPayStatusId(), PaymentStatusEnum.PAID.getId())
				&& (requestDto.getPayoutNumber() == null || requestDto.getPayoutNumber().trim().isEmpty())) {
			throw new ValidationErrorException("Payout number is required when payment status is paid");
		}
	}

	private void updatePaymentStatus(TimesheetInvoice invoice, UpdateTimesheetPayBillStatusRequestBodyDto requestDto) {
		invoice.setPaymentStatusId(requestDto.getPayStatusId());
		if (Objects.equals(requestDto.getPayStatusId(), PaymentStatusEnum.PAID.getId())) {
			invoice.setPaymentPaidOn(requestDto.getPayoutPaidOn());
		}
		if (requestDto.getPayStatusId() != null) {
			invoice.setPaymentStatusId(requestDto.getPayStatusId());
			invoice.setPaymentPaidOn(requestDto.getPayoutPaidOn());
			invoice.setPayoutNumber(requestDto.getPayoutNumber());
		}
	}

	private Integer getCurrentUnixTimestamp() {
		return Math.toIntExact(Instant.now().getEpochSecond());
	}

}
