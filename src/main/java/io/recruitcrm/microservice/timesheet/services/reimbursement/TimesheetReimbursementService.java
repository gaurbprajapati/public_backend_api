package io.recruitcrm.microservice.timesheet.services.reimbursement;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursement;
import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursementClientShareHistory;
import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursementStatusHistory;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.helpers.FetchUserAndContactUserIds;
import io.recruitcrm.microservice.timesheet.helpers.auth.EntityAccessValidator;
import io.recruitcrm.microservice.timesheet.helpers.auth.PrincipalEntityExtractor;
import io.recruitcrm.microservice.timesheet.helpers.auth.ReimbursementAccessValidator;
import io.recruitcrm.microservice.timesheet.helpers.constants.BooleanFlagEnum;
import io.recruitcrm.microservice.timesheet.dao.reimbursement.TimesheetReimbursementClientShareHistoryJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.reimbursement.TimesheetReimbursementJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.reimbursement.TimesheetReimbursementStatusHistoryJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_setting.TimesheetSettingJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.CreateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementListItemResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateShareWithClientRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.AddedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdatedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReopenReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ConflictException;
import io.recruitcrm.microservice.timesheet.exceptions.ForbiddenAccessException;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdatePayableBillableRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.kafka.ReminderNotificationEventType;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationChannelsDto;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.constants.EntityNameConstants;
import io.recruitcrm.microservice.timesheet.helpers.constants.ExceptionMessageConstants;
import io.recruitcrm.microservice.timesheet.helpers.constants.ReimbursementConstants;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.kafka.KafkaProducerHelper;
import io.recruitcrm.microservice.timesheet.mapper.TimesheetReimbursementMapper;
import io.recruitcrm.microservice.timesheet.repositories.candidate.CandidateRepository;
import io.recruitcrm.microservice.timesheet.repositories.contact.ContactRepository;
import io.recruitcrm.microservice.timesheet.repositories.invoice.ITimesheetInvoiceRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_reimbursement.ITimesheetReimbursementRepository;
import io.recruitcrm.microservice.timesheet.repositories.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.recruitcrm.microservice.timesheet.helpers.GenericHelper;

import java.time.Instant;
import java.util.ArrayList;

@Service
public class TimesheetReimbursementService implements ITimesheetReimbursementService {

	private final AuthHolder auth;

	private final TimesheetJpaRepository timesheetJpaRepository;

	private final TimesheetSettingJpaRepository timesheetSettingJpaRepository;

	private final TimesheetReimbursementJpaRepository reimbursementJpaRepository;

	private final TimesheetReimbursementStatusHistoryJpaRepository statusHistoryJpaRepository;

	private final TimesheetReimbursementClientShareHistoryJpaRepository clientShareHistoryJpaRepository;

	private final TimesheetReimbursementMapper reimbursementMapper;

	private final ITimesheetInvoiceRepository timesheetInvoiceRepository;

	private final PrincipalEntityExtractor principalEntityExtractor;

	private final ITimesheetReimbursementRepository timesheetReimbursementRepository;

	private final EntityAccessValidator entityAccessValidator;

	private final UserRepository userRepository;

	private final ContactRepository contactRepository;

	private final CandidateRepository candidateRepository;

	private final ReimbursementAccessValidator reimbursementAccessValidator;

	private static final String INVALID_TIMESHEET_ID_MESSAGE = "timesheetId must be a positive integer";

	private final IS3ReimbursementService s3ReimbursementService;

	final FetchUserAndContactUserIds fetchUserAndContactUserIds;

	private final KafkaProducerHelper kafkaProducerHelper;

	public TimesheetReimbursementService(AuthHolder auth, TimesheetJpaRepository timesheetJpaRepository,
			TimesheetSettingJpaRepository timesheetSettingJpaRepository,
			TimesheetReimbursementJpaRepository reimbursementJpaRepository,
			TimesheetReimbursementStatusHistoryJpaRepository statusHistoryJpaRepository,
			TimesheetReimbursementClientShareHistoryJpaRepository clientShareHistoryJpaRepository,
			TimesheetReimbursementMapper reimbursementMapper, ITimesheetInvoiceRepository timesheetInvoiceRepository,
			PrincipalEntityExtractor principalEntityExtractor,
			ITimesheetReimbursementRepository timesheetReimbursementRepository,
			EntityAccessValidator entityAccessValidator, IS3ReimbursementService s3ReimbursementService,
			UserRepository userRepository, ContactRepository contactRepository, CandidateRepository candidateRepository,
			ReimbursementAccessValidator reimbursementAccessValidator,
			FetchUserAndContactUserIds fetchUserAndContactUserIds, KafkaProducerHelper kafkaProducerHelper) {
		this.auth = auth;
		this.timesheetJpaRepository = timesheetJpaRepository;
		this.timesheetSettingJpaRepository = timesheetSettingJpaRepository;
		this.reimbursementJpaRepository = reimbursementJpaRepository;
		this.statusHistoryJpaRepository = statusHistoryJpaRepository;
		this.clientShareHistoryJpaRepository = clientShareHistoryJpaRepository;
		this.reimbursementMapper = reimbursementMapper;
		this.timesheetInvoiceRepository = timesheetInvoiceRepository;
		this.principalEntityExtractor = principalEntityExtractor;
		this.timesheetReimbursementRepository = timesheetReimbursementRepository;
		this.entityAccessValidator = entityAccessValidator;
		this.s3ReimbursementService = s3ReimbursementService;
		this.userRepository = userRepository;
		this.contactRepository = contactRepository;
		this.candidateRepository = candidateRepository;
		this.reimbursementAccessValidator = reimbursementAccessValidator;
		this.fetchUserAndContactUserIds = fetchUserAndContactUserIds;
		this.kafkaProducerHelper = kafkaProducerHelper;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReimbursementListItemResponseBodyDto> listReimbursements(Integer timesheetId) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		PrincipalType principalType = this.auth.getPrincipalType();

		this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.TIMESHEET, timesheetId));

		this.reimbursementAccessValidator.validateTimesheetViewAccess(timesheetId, accountId);

		List<TimesheetReimbursement> reimbursements = fetchReimbursementsForPrincipal(timesheetId, accountId,
				principalType);

		UserDetailsMaps userDetailsMaps = collectAndFetchUserDetails(reimbursements);

		return reimbursements.stream()
			.map((reimbursement) -> mapToListItemDto(reimbursement, principalType, userDetailsMaps))
			.toList();
	}

	private List<TimesheetReimbursement> fetchReimbursementsForPrincipal(Integer timesheetId, Integer accountId,
			PrincipalType principalType) {
		if (principalType == PrincipalType.CONTACT) {
			return this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountIdAndIsSharedWithClient(timesheetId,
					accountId, BooleanFlagEnum.TRUE.getValue());
		}
		return this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, accountId);
	}

	private UserDetailsMaps collectAndFetchUserDetails(List<TimesheetReimbursement> reimbursements) {
		Set<Integer> agencyUserIds = new HashSet<>();
		Set<Integer> contactUserIds = new HashSet<>();
		Set<Integer> contractorUserIds = new HashSet<>();

		for (TimesheetReimbursement reimbursement : reimbursements) {
			collectUserIds(reimbursement.getAddedByUserTypeId(), reimbursement.getAddedBy(), agencyUserIds,
					contactUserIds, contractorUserIds);
			collectUserIds(reimbursement.getUpdatedByUserTypeId(), reimbursement.getUpdatedBy(), agencyUserIds,
					contactUserIds, contractorUserIds);
		}

		return fetchUserDetailsMaps(agencyUserIds, contactUserIds, contractorUserIds);
	}

	private void collectUserIds(Integer userTypeId, Integer userId, Set<Integer> agencyUserIds,
			Set<Integer> contactUserIds, Set<Integer> contractorUserIds) {
		if (userTypeId != null && userId != null) {
			this.fetchUserAndContactUserIds.addUserToAppropriateSet(userTypeId, userId, agencyUserIds, contactUserIds,
					contractorUserIds);
		}
	}

	private ReimbursementListItemResponseBodyDto mapToListItemDto(TimesheetReimbursement reimbursement,
			PrincipalType principalType, UserDetailsMaps userDetailsMaps) {
		String statusLabel = resolveStatusLabel(reimbursement.getStatus(), principalType);

		ReimbursementListItemResponseBodyDto dto = this.reimbursementMapper.toListItemResponseDto(reimbursement,
				statusLabel);

		UserByDetails addedBy = getUserByDetails(reimbursement.getAddedBy(), reimbursement.getAddedByUserTypeId(),
				userDetailsMaps);
		dto.setAddedBy((addedBy != null) ? addedBy.toAddedBy() : null);

		UserByDetails updatedBy = getUserByDetails(reimbursement.getUpdatedBy(), reimbursement.getUpdatedByUserTypeId(),
				userDetailsMaps);
		dto.setUpdatedBy((updatedBy != null) ? updatedBy.toUpdatedBy() : null);

		if (principalType == PrincipalType.CONTRACTOR) {
			dto.setIsPayable(null);
			dto.setIsBillable(null);
		}

		return dto;
	}

	private String resolveStatusLabel(Integer status, PrincipalType principalType) {
		if (status == ReimbursementConstants.STATUS_SUBMITTED) {
			return (principalType == PrincipalType.CONTRACTOR) ? ReimbursementConstants.STATUS_SUBMITTED_LABEL
					: ReimbursementConstants.STATUS_PENDING_LABEL;
		}
		return ReimbursementConstants.getStatusLabel(status);
	}

	@Override
	@Transactional
	@WriterRoute
	public ReimbursementResponseBodyDto createReimbursement(Integer timesheetId,
			CreateReimbursementRequestBodyDto request) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		PrincipalType principalType = this.auth.getPrincipalType();
		Integer userTypeId = this.principalEntityExtractor.resolveUserTypeId(principalType);

		// Validate access control for creating reimbursements
		this.reimbursementAccessValidator.validateAccessControlForCreateReimbursement(timesheetId, accountId);

		TimesheetSetting timesheetSetting = validateCreateReimbursementRequest(timesheetId, accountId);

		// The "share with client" default comes from the latest timesheet setting that
		// shares this setting's association (settings are versioned per association), not
		// necessarily the setting the timesheet is pinned to.
		Integer isClientExpenseSharingEnabled = resolveClientExpenseSharingDefault(timesheetSetting, accountId);

		int currentUnixTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		TimesheetReimbursement savedReimbursement = buildAndSaveReimbursement(request, timesheetId,
				timesheetSetting.getPayCurrencyId(), isClientExpenseSharingEnabled, accountId, userId, userTypeId,
				currentUnixTimestamp);

		createStatusHistory(savedReimbursement.getId(), ReimbursementConstants.STATUS_SUBMITTED, null, userId,
				userTypeId, currentUnixTimestamp, accountId);

		this.scheduleReimbursementSubmittedNotificationAfterCommit(timesheetId, savedReimbursement.getId(), accountId,
				userTypeId);
		return this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementConstants.STATUS_SUBMITTED_LABEL);
	}

	@Override
	@Transactional
	@WriterRoute
	public ReimbursementResponseBodyDto reopenReimbursement(Integer timesheetId, Integer reimbursementId,
			ReopenReimbursementRequestBodyDto request) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		PrincipalType principalType = this.auth.getPrincipalType();
		Integer userTypeId = this.principalEntityExtractor.resolveUserTypeId(principalType);

		validateReopenAccess(principalType, timesheetId, accountId);
		this.reimbursementAccessValidator.validateTimesheetEditAccess(timesheetId, accountId);

		TimesheetReimbursement reimbursement = this.reimbursementJpaRepository
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.REIMBURSEMENT, reimbursementId));

		validateReopenStatus(reimbursement);

		int currentUnixTimestamp = Math.toIntExact(Instant.now().getEpochSecond());

		resetReimbursementForReopen(reimbursement, userId, userTypeId, currentUnixTimestamp);
		TimesheetReimbursement savedReimbursement = this.reimbursementJpaRepository.save(reimbursement);

		createStatusHistory(savedReimbursement.getId(), ReimbursementConstants.STATUS_SUBMITTED, request.getRemark(),
				userId, userTypeId, currentUnixTimestamp, accountId);

		this.scheduleReimbursementSubmittedNotificationAfterCommit(timesheetId, savedReimbursement.getId(), accountId,
				userTypeId);
		return this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementConstants.STATUS_SUBMITTED_LABEL);
	}

	private void validateReopenAccess(PrincipalType principalType, Integer timesheetId, Integer accountId) {
		if (principalType != PrincipalType.USER) {
			throw new ForbiddenAccessException(ExceptionMessageConstants.REIMBURSEMENT_REOPEN_FORBIDDEN);
		}

		this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.TIMESHEET, timesheetId));

		TimesheetInvoice invoice = this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId);
		if (invoice != null && invoice.getInvoiceId() != null) {
			throw new ConflictException(ExceptionMessageConstants.REIMBURSEMENT_INVOICE_LINKED);
		}
	}

	private void validateReopenStatus(TimesheetReimbursement reimbursement) {
		if (reimbursement.getStatus() != ReimbursementConstants.STATUS_APPROVED) {
			throw new ConflictException(ExceptionMessageConstants.REIMBURSEMENT_CANNOT_REOPEN);
		}
	}

	private void resetReimbursementForReopen(TimesheetReimbursement reimbursement, Integer userId, Integer userTypeId,
			int currentUnixTimestamp) {
		reimbursement.setStatus(ReimbursementConstants.STATUS_SUBMITTED);
		reimbursement.setIsPayable(BooleanFlagEnum.FALSE.getValue());
		reimbursement.setIsBillable(BooleanFlagEnum.FALSE.getValue());
		reimbursement.setUpdatedBy(userId);
		reimbursement.setUpdatedOn(currentUnixTimestamp);
		reimbursement.setUpdatedByUserTypeId(userTypeId);
	}

	@Override
	@Transactional
	@WriterRoute
	public ReimbursementResponseBodyDto updatePayableBillable(Integer timesheetId, Integer reimbursementId,
			UpdatePayableBillableRequestBodyDto request) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		PrincipalType principalType = this.auth.getPrincipalType();
		Integer userTypeId = this.principalEntityExtractor.resolveUserTypeId(principalType);

		validateAgencyAccess(userTypeId, timesheetId, accountId);
		validatePayableBillableRequest(request);
		this.reimbursementAccessValidator.validateTimesheetApproveAccess(timesheetId, accountId);

		TimesheetReimbursement reimbursement = this.reimbursementJpaRepository
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.REIMBURSEMENT, reimbursementId));

		validateApprovedStatus(reimbursement);

		int currentUnixTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		applyPayableBillableFlags(reimbursement, request, userId, userTypeId, currentUnixTimestamp);

		TimesheetReimbursement savedReimbursement = this.reimbursementJpaRepository.save(reimbursement);

		return this.reimbursementMapper.toResponseDto(savedReimbursement, ReimbursementConstants.STATUS_APPROVED_LABEL);
	}

	private void validateAgencyAccess(Integer userTypeId, Integer timesheetId, Integer accountId) {
		if (userTypeId != ReimbursementConstants.USER_TYPE_AGENCY) {
			throw new ForbiddenAccessException(ExceptionMessageConstants.REIMBURSEMENT_UPDATE_FLAGS_FORBIDDEN);
		}

		this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.TIMESHEET, timesheetId));

		TimesheetInvoice invoice = this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId);
		if (invoice != null && invoice.getInvoiceId() != null) {
			throw new ConflictException(ExceptionMessageConstants.REIMBURSEMENT_INVOICE_LINKED);
		}
	}

	private void validatePayableBillableRequest(UpdatePayableBillableRequestBodyDto request) {
		if (request.getIsPayable() == null && request.getIsBillable() == null) {
			throw new ValidationErrorException(ExceptionMessageConstants.REIMBURSEMENT_PAYABLE_BILLABLE_REQUIRED);
		}
	}

	private void validateApprovedStatus(TimesheetReimbursement reimbursement) {
		if (reimbursement.getStatus() != ReimbursementConstants.STATUS_APPROVED) {
			throw new ConflictException(ExceptionMessageConstants.REIMBURSEMENT_NOT_APPROVED_FOR_FLAGS);
		}
	}

	private void applyPayableBillableFlags(TimesheetReimbursement reimbursement,
			UpdatePayableBillableRequestBodyDto request, Integer userId, Integer userTypeId, int currentUnixTimestamp) {
		if (request.getIsPayable() != null) {
			reimbursement.setIsPayable(request.getIsPayable());
		}
		if (request.getIsBillable() != null) {
			reimbursement.setIsBillable(request.getIsBillable());
		}
		reimbursement.setUpdatedBy(userId);
		reimbursement.setUpdatedOn(currentUnixTimestamp);
		reimbursement.setUpdatedByUserTypeId(userTypeId);
	}

	@Override
	@Transactional
	@WriterRoute
	public ReimbursementResponseBodyDto updateShareWithClient(Integer timesheetId, Integer reimbursementId,
			UpdateShareWithClientRequestBodyDto request) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		PrincipalType principalType = this.auth.getPrincipalType();
		Integer userTypeId = this.principalEntityExtractor.resolveUserTypeId(principalType);

		validateShareWithClientAccess(userTypeId, timesheetId, accountId);

		TimesheetReimbursement reimbursement = this.reimbursementJpaRepository
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.REIMBURSEMENT, reimbursementId));

		Integer previousValue = reimbursement.getIsSharedWithClient();
		Integer newValue = request.getIsSharedWithClient();

		int currentUnixTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		reimbursement.setIsSharedWithClient(newValue);
		reimbursement.setUpdatedBy(userId);
		reimbursement.setUpdatedOn(currentUnixTimestamp);
		reimbursement.setUpdatedByUserTypeId(userTypeId);

		TimesheetReimbursement savedReimbursement = this.reimbursementJpaRepository.save(reimbursement);

		if (!previousValue.equals(newValue)) {
			createClientShareHistory(savedReimbursement.getId(), newValue, userId, userTypeId, currentUnixTimestamp,
					accountId);
		}

		if (isShareWithClientNotificationRequired(savedReimbursement.getStatus(), previousValue, newValue)) {
			this.scheduleReimbursementSharedWithClientUpdatedNotificationAfterCommit(timesheetId,
					savedReimbursement.getId(), accountId, userTypeId);
		}

		return this.reimbursementMapper.toResponseDto(savedReimbursement,
				resolveStatusLabel(savedReimbursement.getStatus(), principalType));
	}

	private void validateShareWithClientAccess(Integer userTypeId, Integer timesheetId, Integer accountId) {
		if (!Integer.valueOf(ReimbursementConstants.USER_TYPE_AGENCY).equals(userTypeId)) {
			throw new ForbiddenAccessException(ExceptionMessageConstants.REIMBURSEMENT_SHARE_WITH_CLIENT_FORBIDDEN);
		}

		this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.TIMESHEET, timesheetId));
	}

	private boolean isShareWithClientNotificationRequired(Integer status, Integer previousValue, Integer newValue) {
		boolean turnedOn = !BooleanFlagEnum.TRUE.getValue().equals(previousValue)
				&& BooleanFlagEnum.TRUE.getValue().equals(newValue);
		return Integer.valueOf(ReimbursementConstants.STATUS_SUBMITTED).equals(status) && turnedOn;
	}

	@Override
	@Transactional
	@WriterRoute
	public ReimbursementResponseBodyDto updateReimbursement(Integer timesheetId, Integer id,
			UpdateReimbursementRequestBodyDto request) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		PrincipalType principalType = this.auth.getPrincipalType();
		Integer userTypeId = this.principalEntityExtractor.resolveUserTypeId(principalType);

		TimesheetReimbursement reimbursement = this.reimbursementJpaRepository
			.findByIdAndTimesheetIdAndAccountId(id, timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.REIMBURSEMENT, id));

		this.reimbursementAccessValidator.validateAccessControlForUpdateReimbursement(timesheetId, accountId,
				reimbursement.getStatus());

		validateInvoiceNotLinked(timesheetId, accountId);

		String existingDocumentToken = reimbursement.getDocumentToken();
		String newDocumentToken = request.getDocumentToken();

		this.reimbursementMapper.updateEntityFromDto(request, reimbursement);

		if (reimbursement.getStatus() == ReimbursementConstants.STATUS_REJECTED) {
			reimbursement.setStatus(ReimbursementConstants.STATUS_SUBMITTED);
		}

		int currentUnixTimestamp = Math.toIntExact(Instant.now().getEpochSecond());
		reimbursement.setUpdatedBy(userId);
		reimbursement.setUpdatedOn(currentUnixTimestamp);
		reimbursement.setUpdatedByUserTypeId(userTypeId);

		TimesheetReimbursement savedReimbursement = this.reimbursementJpaRepository.save(reimbursement);

		if (!Boolean.TRUE.equals(request.getSkipStatusHistory())) {
			createStatusHistory(savedReimbursement.getId(), savedReimbursement.getStatus(), null, userId, userTypeId,
					currentUnixTimestamp, accountId);
		}

		deleteOldFileIfChanged(existingDocumentToken, newDocumentToken);

		if (!Boolean.TRUE.equals(request.getSkipStatusHistory())) {
			this.scheduleReimbursementSubmittedNotificationAfterCommit(timesheetId, savedReimbursement.getId(),
					accountId, userTypeId);
		}
		return this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementConstants.STATUS_SUBMITTED_LABEL);
	}

	@Override
	@Transactional
	@WriterRoute
	public ReimbursementResponseBodyDto updateReimbursementStatus(Integer timesheetId, Integer id,
			UpdateReimbursementStatusRequestBodyDto request) {
		PrincipalType principalType = this.auth.getPrincipalType();

		if (principalType == PrincipalType.CONTRACTOR) {
			throw new ForbiddenAccessException(ExceptionMessageConstants.REIMBURSEMENT_APPROVE_REJECT_FORBIDDEN);
		}

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		Integer userTypeId = this.principalEntityExtractor.resolveUserTypeId(principalType);

		TimesheetReimbursement reimbursement = this.reimbursementJpaRepository
			.findByIdAndTimesheetIdAndAccountId(id, timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.REIMBURSEMENT, id));

		this.reimbursementAccessValidator.validateAccessControlForUpdateStatus(timesheetId, accountId);

		validateInvoiceNotLinked(timesheetId, accountId);

		Integer newStatus = request.getStatus();
		validateNewStatus(newStatus, request.getRemark());

		applyStatusTransition(reimbursement, newStatus);

		int currentUnixTimestamp = Math.toIntExact(Instant.now().getEpochSecond());

		reimbursement.setStatus(newStatus);
		reimbursement.setUpdatedBy(userId);
		reimbursement.setUpdatedOn(currentUnixTimestamp);
		reimbursement.setUpdatedByUserTypeId(userTypeId);

		TimesheetReimbursement savedReimbursement = this.reimbursementJpaRepository.save(reimbursement);

		createStatusHistory(id, newStatus, request.getRemark(), userId, userTypeId, currentUnixTimestamp, accountId);

		this.scheduleReimbursementStatusNotificationAfterCommit(timesheetId, id, accountId, userTypeId, newStatus);

		return this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementConstants.getStatusLabel(newStatus));
	}

	private void validateNewStatus(Integer newStatus, String remark) {
		if (newStatus == null || (newStatus != ReimbursementConstants.STATUS_APPROVED
				&& newStatus != ReimbursementConstants.STATUS_REJECTED)) {
			throw new ValidationErrorException(ExceptionMessageConstants.REIMBURSEMENT_INVALID_STATUS);
		}

		if (newStatus == ReimbursementConstants.STATUS_REJECTED && (remark == null || remark.trim().isEmpty())) {
			throw new ValidationErrorException(ExceptionMessageConstants.REIMBURSEMENT_REMARK_MANDATORY_FOR_REJECTION);
		}
	}

	private void applyStatusTransition(TimesheetReimbursement reimbursement, Integer newStatus) {
		Integer currentStatus = reimbursement.getStatus();

		if (newStatus == ReimbursementConstants.STATUS_APPROVED) {
			validateApprovalTransition(currentStatus);
			reimbursement.setIsPayable(BooleanFlagEnum.TRUE.getValue());
			reimbursement.setIsBillable(BooleanFlagEnum.TRUE.getValue());
		}
		else {
			validateRejectionTransition(currentStatus);
			reimbursement.setIsPayable(BooleanFlagEnum.FALSE.getValue());
			reimbursement.setIsBillable(BooleanFlagEnum.FALSE.getValue());
		}
	}

	private void validateApprovalTransition(Integer currentStatus) {
		if (Integer.valueOf(ReimbursementConstants.STATUS_APPROVED).equals(currentStatus)) {
			throw new ConflictException(ExceptionMessageConstants.REIMBURSEMENT_ALREADY_APPROVED);
		}
		if (Integer.valueOf(ReimbursementConstants.STATUS_REJECTED).equals(currentStatus)) {
			throw new ConflictException(ExceptionMessageConstants.REIMBURSEMENT_ONLY_APPROVE_SUBMITTED);
		}
	}

	private void validateRejectionTransition(Integer currentStatus) {
		if (Integer.valueOf(ReimbursementConstants.STATUS_REJECTED).equals(currentStatus)) {
			throw new ConflictException(ExceptionMessageConstants.REIMBURSEMENT_ALREADY_REJECTED);
		}
		if (Integer.valueOf(ReimbursementConstants.STATUS_APPROVED).equals(currentStatus)) {
			throw new ConflictException(ExceptionMessageConstants.REIMBURSEMENT_CANNOT_REJECT_APPROVED);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReimbursementStatusHistoryResponseBodyDto> getReimbursementStatusHistory(Integer timesheetId,
			Integer id) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		findTimesheetOrThrow(timesheetId, accountId);

		this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(id, timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.REIMBURSEMENT, id));

		this.reimbursementAccessValidator.validateAccessControlForGetStatusHistory(timesheetId, accountId);

		List<TimesheetReimbursementStatusHistory> statusHistories = this.statusHistoryJpaRepository
			.findByTimesheetReimbursementIdAndAccountIdOrderByCreatedOnDesc(id, accountId);

		Set<Integer> agencyUserIds = new HashSet<>();
		Set<Integer> contactUserIds = new HashSet<>();
		Set<Integer> contractorUserIds = new HashSet<>();

		for (TimesheetReimbursementStatusHistory history : statusHistories) {
			if (history.getCreatedByUserTypeId() != null && history.getCreatedBy() != null) {
				this.fetchUserAndContactUserIds.addUserToAppropriateSet(history.getCreatedByUserTypeId(),
						history.getCreatedBy(), agencyUserIds, contactUserIds, contractorUserIds);
			}
		}

		UserDetailsMaps userDetailsMaps = fetchUserDetailsMaps(agencyUserIds, contactUserIds, contractorUserIds);

		return statusHistories.stream().map((history) -> {
			ReimbursementStatusHistoryResponseBodyDto dto = this.reimbursementMapper.toStatusHistoryResponseDto(history,
					ReimbursementConstants.getStatusLabel(history.getReimbursementStatusTypeId()));

			UserByDetails createdBy = getUserByDetails(history.getCreatedBy(), history.getCreatedByUserTypeId(),
					userDetailsMaps);
			dto.setCreatedBy((createdBy != null) ? createdBy.toAddedBy() : null);

			return dto;
		}).toList();
	}

	private UserDetailsMaps fetchUserDetailsMaps(Set<Integer> agencyUserIds, Set<Integer> contactUserIds,
			Set<Integer> contractorUserIds) {
		Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = agencyUserIds.isEmpty() ? Map.of()
				: this.userRepository.getUserDetailsMap(agencyUserIds);
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = contactUserIds.isEmpty() ? Map.of()
				: this.contactRepository.getContactNamePhotoMap(contactUserIds);
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap = contractorUserIds.isEmpty() ? Map.of()
				: this.candidateRepository.getContractorQueryResultMap(contractorUserIds);
		return new UserDetailsMaps(agencyUsersMap, contactUsersMap, contractorUsersMap);
	}

	private UserByDetails getUserByDetails(Integer userId, Integer userTypeId, UserDetailsMaps userDetailsMaps) {
		if (userId == null) {
			return null;
		}

		if (UserTypeEnum.AGENCY_RECRUITER.getId().equals(userTypeId)) {
			UserDetailsQueryResultDto userDetails = userDetailsMaps.agencyUsersMap().get(userId);
			if (userDetails != null) {
				return new UserByDetails(userId, userDetails.getName(), userDetails.getProfilePic(), userTypeId);
			}
		}
		else if (UserTypeEnum.COMPANY_CONTACT.getId().equals(userTypeId)) {
			ContactNamePhotoQueryResultDto contactDetails = userDetailsMaps.contactUsersMap().get(userId);
			if (contactDetails != null) {
				return new UserByDetails(userId, contactDetails.getName(), contactDetails.getProfilePic(), userTypeId);
			}
		}
		else if (UserTypeEnum.CONTRACTOR.getId().equals(userTypeId)) {
			ContractorNamePhotoQueryResultDto contractorDetails = userDetailsMaps.contractorUsersMap().get(userId);
			if (contractorDetails != null) {
				return new UserByDetails(userId, contractorDetails.getName(), contractorDetails.getProfilePic(),
						userTypeId);
			}
		}

		return new UserByDetails(userId, null, null, userTypeId);
	}

	@Override
	@Transactional
	@WriterRoute
	public void deleteReimbursement(Integer timesheetId, Integer id) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		TimesheetReimbursement reimbursement = this.reimbursementJpaRepository
			.findByIdAndTimesheetIdAndAccountId(id, timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.REIMBURSEMENT, id));

		// Validate access control for deleting reimbursements
		this.reimbursementAccessValidator.validateAccessControlForDeleteReimbursement(timesheetId, accountId);

		validateInvoiceNotLinked(timesheetId, accountId);

		// Delete the uploaded file from S3 if present
		this.s3ReimbursementService.deleteReimbursementFile(reimbursement.getDocumentToken());

		// Delete all status history records for this reimbursement
		this.statusHistoryJpaRepository.deleteByTimesheetReimbursementId(id);
		this.reimbursementJpaRepository.delete(reimbursement);
	}

	private void deleteOldFileIfChanged(String existingDocumentToken, String newDocumentToken) {
		if (existingDocumentToken != null && !existingDocumentToken.isEmpty()
				&& !existingDocumentToken.equals(newDocumentToken)) {
			this.s3ReimbursementService.deleteReimbursementFile(existingDocumentToken);
		}
	}

	private Timesheet findTimesheetOrThrow(Integer timesheetId, Integer accountId) {
		return this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.TIMESHEET, timesheetId));
	}

	private TimesheetSetting fetchTimesheetAndSetting(Integer timesheetId, Integer accountId) {
		Timesheet timesheet = findTimesheetOrThrow(timesheetId, accountId);
		return this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheet.getTimesheetSettingId(), accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.TIMESHEET_SETTING,
					timesheet.getTimesheetSettingId()));
	}

	/**
	 * Resolve the "share with client" flag for a new reimbursement. Timesheet settings
	 * are versioned per association, so the effective setting is the latest (highest id)
	 * that shares the given setting's association rather than the (possibly older)
	 * setting the timesheet is pinned to; falls back to the passed setting if no rows are
	 * found. The claim is shared with the client only when that latest setting has BOTH
	 * client expense sharing enabled AND reimbursements enabled — if either is off, the
	 * claim is created with client sharing off.
	 */
	private Integer resolveClientExpenseSharingDefault(TimesheetSetting timesheetSetting, Integer accountId) {
		Integer associationId = timesheetSetting.getAssociation().getId();
		List<TimesheetSetting> latestSettings = this.timesheetSettingJpaRepository
			.findLatestByAssociationIdAndAccountId(associationId, accountId, PageRequest.of(0, 1));
		TimesheetSetting effectiveSetting = latestSettings.isEmpty() ? timesheetSetting : latestSettings.get(0);

		boolean shareWithClient = BooleanFlagEnum.TRUE.getValue()
			.equals(effectiveSetting.getIsClientExpenseSharingEnabled())
				&& BooleanFlagEnum.TRUE.getValue().equals(effectiveSetting.getIsReimbursementEnabled());

		return shareWithClient ? BooleanFlagEnum.TRUE.getValue() : BooleanFlagEnum.FALSE.getValue();
	}

	private TimesheetSetting validateCreateReimbursementRequest(Integer timesheetId, Integer accountId) {
		// Fetch timesheet and its settings; throws 404 if either is not found
		TimesheetSetting timesheetSetting = fetchTimesheetAndSetting(timesheetId, accountId);

		// Reject if reimbursements are not enabled for this timesheet
		if (!BooleanFlagEnum.TRUE.getValue().equals(timesheetSetting.getIsReimbursementEnabled())) {
			throw new ValidationErrorException(ExceptionMessageConstants.REIMBURSEMENTS_NOT_ENABLED);
		}

		validateInvoiceNotLinked(timesheetId, accountId);

		long currentCount = this.reimbursementJpaRepository.countByTimesheetIdAndAccountId(timesheetId, accountId);
		if (currentCount >= 10) {
			throw new ValidationErrorException(ExceptionMessageConstants.REIMBURSEMENT_MAX_LIMIT_REACHED);
		}

		return timesheetSetting;
	}

	private TimesheetReimbursement buildAndSaveReimbursement(CreateReimbursementRequestBodyDto request,
			Integer timesheetId, Integer currencyId, Integer isSharedWithClient, Integer accountId, Integer userId,
			Integer userTypeId, int currentUnixTimestamp) {
		TimesheetReimbursement reimbursement = this.reimbursementMapper.toEntity(request);
		reimbursement.setTimesheetId(timesheetId);
		reimbursement.setStatus(ReimbursementConstants.STATUS_SUBMITTED);
		reimbursement.setIsPayable(BooleanFlagEnum.FALSE.getValue());
		reimbursement.setIsBillable(BooleanFlagEnum.FALSE.getValue());
		reimbursement.setIsSharedWithClient(isSharedWithClient);
		reimbursement.setCurrencyId(currencyId);
		reimbursement.setAccountId(accountId);
		reimbursement.setAddedBy(userId);
		reimbursement.setAddedOn(currentUnixTimestamp);
		reimbursement.setAddedByUserTypeId(userTypeId);
		reimbursement.setUpdatedBy(userId);
		reimbursement.setUpdatedOn(currentUnixTimestamp);
		reimbursement.setUpdatedByUserTypeId(userTypeId);
		return this.reimbursementJpaRepository.save(reimbursement);
	}

	private void createStatusHistory(Integer reimbursementId, Integer status, String remark, Integer userId,
			Integer userTypeId, int currentUnixTimestamp, Integer accountId) {
		TimesheetReimbursementStatusHistory statusHistory = new TimesheetReimbursementStatusHistory();
		statusHistory.setTimesheetReimbursementId(reimbursementId);
		statusHistory.setReimbursementStatusTypeId(status);
		statusHistory.setRemark(remark);
		statusHistory.setCreatedBy(userId);
		statusHistory.setCreatedByUserTypeId(userTypeId);
		statusHistory.setCreatedOn(currentUnixTimestamp);
		statusHistory.setAccountId(accountId);
		this.statusHistoryJpaRepository.save(statusHistory);
	}

	private void createClientShareHistory(Integer reimbursementId, Integer isSharedWithClient, Integer userId,
			Integer userTypeId, int currentUnixTimestamp, Integer accountId) {
		TimesheetReimbursementClientShareHistory clientShareHistory = new TimesheetReimbursementClientShareHistory();
		clientShareHistory.setTimesheetReimbursementId(reimbursementId);
		clientShareHistory.setIsSharedWithClient(isSharedWithClient);
		clientShareHistory.setCreatedBy(userId);
		clientShareHistory.setCreatedByUserTypeId(userTypeId);
		clientShareHistory.setCreatedOn(currentUnixTimestamp);
		clientShareHistory.setAccountId(accountId);
		this.clientShareHistoryJpaRepository.save(clientShareHistory);
	}

	private void scheduleReimbursementSubmittedNotificationAfterCommit(final Integer timesheetId,
			final Integer reimbursementId, final Integer accountId, final Integer createdByUserTypeId) {
		GenericHelper.runAfterCommitOrNow(() -> this.publishReimbursementSubmittedNotification(timesheetId,
				reimbursementId, accountId, createdByUserTypeId));
	}

	private void publishReimbursementSubmittedNotification(final Integer timesheetId, final Integer reimbursementId,
			final Integer accountId, final Integer createdByUserTypeId) {
		AuthPrincipal principal = this.auth.getUnifiedPrincipal();
		String performerDisplayName = principal.getFullName();
		TimesheetReminderNotificationChannelsDto channels = TimesheetReminderNotificationChannelsDto.SUBMITTED;
		TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				java.util.UUID.randomUUID().toString(), new ArrayList<>(List.of(timesheetId)),
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_REIMBURSEMENT_SUBMITTED, accountId,
				createdByUserTypeId, UserTypeEnum.CONTRACTOR.getId(), ReminderNotificationEventType.REALTIME,
				channels.sendInappNotification(), channels.sendEmailNotification(), channels.sendPortalNotification(),
				performerDisplayName, null, new ArrayList<>(List.of(reimbursementId)));
		this.kafkaProducerHelper.sendTimesheetReminderNotification(payload);
	}

	private void scheduleReimbursementSharedWithClientUpdatedNotificationAfterCommit(final Integer timesheetId,
			final Integer reimbursementId, final Integer accountId, final Integer createdByUserTypeId) {
		GenericHelper
			.runAfterCommitOrNow(() -> this.publishReimbursementSharedWithClientUpdatedNotification(timesheetId,
					reimbursementId, accountId, createdByUserTypeId));
	}

	private void publishReimbursementSharedWithClientUpdatedNotification(final Integer timesheetId,
			final Integer reimbursementId, final Integer accountId, final Integer createdByUserTypeId) {
		AuthPrincipal principal = this.auth.getUnifiedPrincipal();
		String performerDisplayName = principal.getFullName();
		TimesheetReminderNotificationChannelsDto channels = TimesheetReminderNotificationChannelsDto.SHARED_WITH_CLIENT_UPDATED;
		TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				java.util.UUID.randomUUID().toString(), new ArrayList<>(List.of(timesheetId)),
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_REIMBURSEMENT_SHARED_WITH_CLIENT_UPDATED, accountId,
				createdByUserTypeId, UserTypeEnum.CONTRACTOR.getId(), ReminderNotificationEventType.REALTIME,
				channels.sendInappNotification(), channels.sendEmailNotification(), channels.sendPortalNotification(),
				performerDisplayName, null, new ArrayList<>(List.of(reimbursementId)));
		this.kafkaProducerHelper.sendTimesheetReminderNotification(payload);
	}

	private void scheduleReimbursementStatusNotificationAfterCommit(final Integer timesheetId,
			final Integer reimbursementId, final Integer accountId, final Integer updatedByUserTypeId,
			final Integer newStatus) {
		GenericHelper.runAfterCommitOrNow(() -> this.publishReimbursementStatusNotification(timesheetId,
				reimbursementId, accountId, updatedByUserTypeId, newStatus));
	}

	private void publishReimbursementStatusNotification(final Integer timesheetId, final Integer reimbursementId,
			final Integer accountId, final Integer createdByUserTypeId, final Integer newStatus) {
		AuthPrincipal principal = this.auth.getUnifiedPrincipal();
		String performerDisplayName = principal.getFullName();

		String eventName;
		TimesheetReminderNotificationChannelsDto channels = TimesheetReminderNotificationChannelsDto.REIMBURSEMENT_STATUS;

		if (newStatus == ReimbursementConstants.STATUS_APPROVED) {
			eventName = TimesheetReminderNotificationPayloadDto.EVENT_NAME_REIMBURSEMENT_APPROVED;
		}
		else {
			eventName = TimesheetReminderNotificationPayloadDto.EVENT_NAME_REIMBURSEMENT_REJECTED;
		}

		TimesheetReminderNotificationPayloadDto payload = new TimesheetReminderNotificationPayloadDto(
				java.util.UUID.randomUUID().toString(), new ArrayList<>(List.of(timesheetId)), eventName, accountId,
				createdByUserTypeId, UserTypeEnum.CONTRACTOR.getId(), ReminderNotificationEventType.REALTIME,
				channels.sendInappNotification(), channels.sendEmailNotification(), channels.sendPortalNotification(),
				performerDisplayName, null, new ArrayList<>(List.of(reimbursementId)));
		this.kafkaProducerHelper.sendTimesheetReminderNotification(payload);
	}

	@Override
	@Transactional
	public Integer getReimbursementCount(Integer timesheetId) {
		if (timesheetId == null || timesheetId <= 0) {
			throw new ValidationErrorException(INVALID_TIMESHEET_ID_MESSAGE);
		}

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		this.reimbursementAccessValidator.validateTimesheetViewAccess(timesheetId, accountId);

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();
		Integer entityType = this.principalEntityExtractor.extractEntityTypeFromPrincipal(principal);
		Integer entityId = this.principalEntityExtractor.extractEntityIdFromPrincipal(principal);

		if (entityType != null && entityId != null) {
			this.entityAccessValidator.validateEntityAccess(entityType, entityId);
		}

		// The entity type is derived from the authenticated principal. When the caller is
		// a client/contact, the count includes only expense claims shared with the client
		// (see the repository's is_shared_with_client filter).
		return this.timesheetReimbursementRepository.getReimbursementCountByTimesheetIdAndEntity(timesheetId,
				entityType, entityId, accountId);
	}

	private void validateInvoiceNotLinked(Integer timesheetId, Integer accountId) {
		TimesheetInvoice timesheetInvoice = this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId);
		if (timesheetInvoice != null && timesheetInvoice.getInvoiceId() != null) {
			throw new ConflictException(ExceptionMessageConstants.REIMBURSEMENT_INVOICE_LINKED);
		}
	}

	private record UserDetailsMaps(Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap,
			Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap) {
	}

	private record UserByDetails(Integer id, String name, String photo, Integer userTypeId) {

		AddedByResponseBodyDto toAddedBy() {
			return new AddedByResponseBodyDto(this.id, this.name, this.photo, this.userTypeId);
		}

		UpdatedByResponseBodyDto toUpdatedBy() {
			return new UpdatedByResponseBodyDto(this.id, this.name, this.photo, this.userTypeId);
		}

	}

}