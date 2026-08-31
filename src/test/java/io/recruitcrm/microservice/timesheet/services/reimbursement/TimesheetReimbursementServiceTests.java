package io.recruitcrm.microservice.timesheet.services.reimbursement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.eq;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursement;
import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursementClientShareHistory;
import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursementStatusHistory;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal;
import io.recruitcrm.microservice.timesheet.dao.reimbursement.TimesheetReimbursementClientShareHistoryJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.reimbursement.TimesheetReimbursementJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.reimbursement.TimesheetReimbursementStatusHistoryJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_setting.TimesheetSettingJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.CreateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementListItemResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateShareWithClientRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReopenReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ConflictException;
import io.recruitcrm.microservice.timesheet.exceptions.ForbiddenAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdatePayableBillableRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementStatusRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.auth.EntityAccessValidator;
import io.recruitcrm.microservice.timesheet.helpers.auth.PrincipalEntityExtractor;
import io.recruitcrm.microservice.timesheet.helpers.constants.ExceptionMessageConstants;
import io.recruitcrm.microservice.timesheet.helpers.constants.ReimbursementConstants;
import io.recruitcrm.microservice.timesheet.helpers.FetchUserAndContactUserIds;
import io.recruitcrm.microservice.timesheet.mapper.TimesheetReimbursementMapper;
import io.recruitcrm.microservice.timesheet.repositories.candidate.CandidateRepository;
import io.recruitcrm.microservice.timesheet.repositories.contact.ContactRepository;
import io.recruitcrm.microservice.timesheet.repositories.invoice.ITimesheetInvoiceRepository;
import io.recruitcrm.microservice.timesheet.helpers.auth.ReimbursementAccessValidator;
import io.recruitcrm.microservice.timesheet.kafka.KafkaProducerHelper;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_reimbursement.ITimesheetReimbursementRepository;
import io.recruitcrm.microservice.timesheet.repositories.user.UserRepository;
import io.recruitcrm.microservice.timesheet.testdata.ReimbursementTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TimesheetReimbursementServiceTests {

	@InjectMocks
	private TimesheetReimbursementService timesheetReimbursementService;

	@Mock
	private AuthHolder auth;

	@Mock
	private TimesheetJpaRepository timesheetJpaRepository;

	@Mock
	private TimesheetSettingJpaRepository timesheetSettingJpaRepository;

	@Mock
	private TimesheetReimbursementJpaRepository reimbursementJpaRepository;

	@Mock
	private TimesheetReimbursementStatusHistoryJpaRepository statusHistoryJpaRepository;

	@Mock
	private TimesheetReimbursementClientShareHistoryJpaRepository clientShareHistoryJpaRepository;

	@Mock
	private TimesheetReimbursementMapper reimbursementMapper;

	@Mock
	private ITimesheetInvoiceRepository timesheetInvoiceRepository;

	@Mock
	private PrincipalEntityExtractor principalEntityExtractor;

	@Mock
	private IS3ReimbursementService s3ReimbursementService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ContactRepository contactRepository;

	@Mock
	private CandidateRepository candidateRepository;

	@Mock
	private ReimbursementAccessValidator reimbursementAccessValidator;

	@Mock
	private FetchUserAndContactUserIds fetchUserAndContactUserIds;

	@Mock
	private EntityAccessValidator entityAccessValidator;

	@Mock
	private ITimesheetReimbursementRepository timesheetReimbursementRepository;

	@Mock
	private KafkaProducerHelper kafkaProducerHelper;

	private Integer accountId;

	private Integer userId;

	private Integer userTypeId;

	@BeforeEach
	void setUp() {
		this.accountId = ReimbursementTestDataFactory.getDefaultAccountId();
		this.userId = ReimbursementTestDataFactory.getDefaultUserId();
		this.userTypeId = ReimbursementTestDataFactory.getDefaultUserTypeId();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(this.accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(this.userTypeId);
		this.givenAuthenticatedPerformer("Test User");
	}

	private void givenFetchUserAndContactUserIdsDelegatesToRealComponent() {
		willAnswer((invocation) -> {
			new FetchUserAndContactUserIds().addUserToAppropriateSet(invocation.getArgument(0),
					invocation.getArgument(1), invocation.getArgument(2), invocation.getArgument(3),
					invocation.getArgument(4));
			return null;
		}).given(this.fetchUserAndContactUserIds).addUserToAppropriateSet(any(), any(), any(), any(), any());
	}

	private void givenAuthenticatedPerformer(String performerDisplayName) {
		AuthPrincipal authPrincipal = mock(AuthPrincipal.class);
		given(authPrincipal.getFullName()).willReturn(performerDisplayName);
		given(this.auth.getUnifiedPrincipal()).willReturn(authPrincipal);
	}

	@Test
	@DisplayName("Create reimbursement should return success response")
	void testCreateReimbursementValidRequestReturnsSuccess() {
		// Given
		this.givenAuthenticatedPerformer("Contractor User");
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetSetting timesheetSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(1);
		TimesheetReimbursement reimbursementEntity = ReimbursementTestDataFactory.createReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.createReimbursementResponse();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheet.getTimesheetSettingId(),
				this.accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.reimbursementMapper.toEntity(request)).willReturn(reimbursementEntity);
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.statusHistoryJpaRepository.save(any(TimesheetReimbursementStatusHistory.class)))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryEntity());
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.createReimbursement(timesheetId,
				request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetSettingJpaRepository).should()
			.findByIdAndAccountId(timesheet.getTimesheetSettingId(), this.accountId);
		then(this.reimbursementMapper).should().toEntity(request);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
		then(this.statusHistoryJpaRepository).should().save(any(TimesheetReimbursementStatusHistory.class));
		then(this.reimbursementMapper).should()
			.toResponseDto(savedReimbursement, ReimbursementTestDataFactory.getStatusSubmittedLabel());
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Create reimbursement should take share-with-client default from the latest association setting")
	void testCreateReimbursementUsesLatestAssociationSettingForShareWithClient() {
		// Given
		this.givenAuthenticatedPerformer("Contractor User");
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		// Pinned setting shares with client (1); the latest setting for the same
		// association does not (0) — the latest value must win.
		TimesheetSetting pinnedSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(1, 1);
		TimesheetSetting latestSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(1, 0);
		TimesheetReimbursement reimbursementEntity = ReimbursementTestDataFactory.createReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheet.getTimesheetSettingId(),
				this.accountId))
			.willReturn(Optional.of(pinnedSetting));
		given(this.timesheetSettingJpaRepository.findLatestByAssociationIdAndAccountId(
				eq(ReimbursementTestDataFactory.getDefaultAssociationId()), eq(this.accountId), any()))
			.willReturn(List.of(latestSetting));
		given(this.reimbursementMapper.toEntity(request)).willReturn(reimbursementEntity);
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.statusHistoryJpaRepository.save(any(TimesheetReimbursementStatusHistory.class)))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryEntity());
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(ReimbursementTestDataFactory.createReimbursementResponse());

		// When
		this.timesheetReimbursementService.createReimbursement(timesheetId, request);

		// Then
		ArgumentCaptor<TimesheetReimbursement> captor = ArgumentCaptor.forClass(TimesheetReimbursement.class);
		then(this.reimbursementJpaRepository).should().save(captor.capture());
		assertThat(captor.getValue().getIsSharedWithClient()).isZero();
	}

	@Test
	@DisplayName("Create reimbursement should turn client sharing off when latest setting has reimbursements disabled")
	void testCreateReimbursementSharesOffWhenLatestSettingReimbursementDisabled() {
		// Given
		this.givenAuthenticatedPerformer("Contractor User");
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		// Pinned setting passes validation (reimbursement enabled) and shares with
		// client;
		// the latest association setting shares with client but has reimbursements
		// disabled,
		// so the claim must be created with client sharing off.
		TimesheetSetting pinnedSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(1, 1);
		TimesheetSetting latestSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(0, 1);
		TimesheetReimbursement reimbursementEntity = ReimbursementTestDataFactory.createReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheet.getTimesheetSettingId(),
				this.accountId))
			.willReturn(Optional.of(pinnedSetting));
		given(this.timesheetSettingJpaRepository.findLatestByAssociationIdAndAccountId(
				eq(ReimbursementTestDataFactory.getDefaultAssociationId()), eq(this.accountId), any()))
			.willReturn(List.of(latestSetting));
		given(this.reimbursementMapper.toEntity(request)).willReturn(reimbursementEntity);
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.statusHistoryJpaRepository.save(any(TimesheetReimbursementStatusHistory.class)))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryEntity());
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(ReimbursementTestDataFactory.createReimbursementResponse());

		// When
		this.timesheetReimbursementService.createReimbursement(timesheetId, request);

		// Then
		ArgumentCaptor<TimesheetReimbursement> captor = ArgumentCaptor.forClass(TimesheetReimbursement.class);
		then(this.reimbursementJpaRepository).should().save(captor.capture());
		assertThat(captor.getValue().getIsSharedWithClient()).isZero();
	}

	@Test
	@DisplayName("Create reimbursement should share with client when latest setting has both flags enabled")
	void testCreateReimbursementSharesWithClientWhenLatestSettingBothFlagsEnabled() {
		// Given: pinned setting and latest setting both have sharing + reimbursement on
		TimesheetSetting pinnedSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(1, 1);
		TimesheetSetting latestSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(1, 1);

		// When
		TimesheetReimbursement saved = createReimbursementWithSettings(pinnedSetting, List.of(latestSetting));

		// Then
		assertThat(saved.getIsSharedWithClient()).isEqualTo(1);
	}

	@Test
	@DisplayName("Create reimbursement should fall back to the pinned setting when no association setting is found")
	void testCreateReimbursementFallsBackToPinnedSettingWhenNoLatestFound() {
		// Given: pinned setting has both flags on; the association lookup returns no rows
		TimesheetSetting pinnedSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(1, 1);

		// When
		TimesheetReimbursement saved = createReimbursementWithSettings(pinnedSetting, List.of());

		// Then: value comes from the pinned setting (both flags on -> shared)
		assertThat(saved.getIsSharedWithClient()).isEqualTo(1);
	}

	/**
	 * Drives createReimbursement with the given pinned setting and association-latest
	 * result, and returns the reimbursement entity captured at save time.
	 */
	private TimesheetReimbursement createReimbursementWithSettings(TimesheetSetting pinnedSetting,
			List<TimesheetSetting> latestSettings) {
		this.givenAuthenticatedPerformer("Contractor User");
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursementEntity = ReimbursementTestDataFactory.createReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheet.getTimesheetSettingId(),
				this.accountId))
			.willReturn(Optional.of(pinnedSetting));
		given(this.timesheetSettingJpaRepository.findLatestByAssociationIdAndAccountId(
				eq(ReimbursementTestDataFactory.getDefaultAssociationId()), eq(this.accountId), any()))
			.willReturn(latestSettings);
		given(this.reimbursementMapper.toEntity(request)).willReturn(reimbursementEntity);
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.statusHistoryJpaRepository.save(any(TimesheetReimbursementStatusHistory.class)))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryEntity());
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(ReimbursementTestDataFactory.createReimbursementResponse());

		this.timesheetReimbursementService.createReimbursement(timesheetId, request);

		ArgumentCaptor<TimesheetReimbursement> captor = ArgumentCaptor.forClass(TimesheetReimbursement.class);
		then(this.reimbursementJpaRepository).should().save(captor.capture());
		return captor.getValue();
	}

	@Test
	@DisplayName("Create reimbursement should not publish Kafka when save fails before notification")
	void testCreateReimbursementDoesNotPublishKafkaWhenSaveFails() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetSetting timesheetSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(1);
		TimesheetReimbursement reimbursementEntity = ReimbursementTestDataFactory.createReimbursementEntity();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheet.getTimesheetSettingId(),
				this.accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.reimbursementMapper.toEntity(request)).willReturn(reimbursementEntity);
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class)))
			.willThrow(new RuntimeException("db error"));

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.createReimbursement(timesheetId, request))
			.isInstanceOf(RuntimeException.class);
		then(this.kafkaProducerHelper).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Create reimbursement should throw ResourceNotFoundException when timesheet not found")
	void testCreateReimbursementTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReimbursementRequest();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.createReimbursement(timesheetId, request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet")
			.hasMessageContaining(timesheetId.toString());

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetSettingJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
		then(this.statusHistoryJpaRepository).should(never()).save(any(TimesheetReimbursementStatusHistory.class));
		then(this.kafkaProducerHelper).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Create reimbursement should throw ResourceNotFoundException when timesheet setting not found")
	void testCreateReimbursementTimesheetSettingNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheet.getTimesheetSettingId(),
				this.accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.createReimbursement(timesheetId, request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("TimesheetSetting")
			.hasMessageContaining(timesheet.getTimesheetSettingId().toString());

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetSettingJpaRepository).should()
			.findByIdAndAccountId(timesheet.getTimesheetSettingId(), this.accountId);
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
		then(this.statusHistoryJpaRepository).should(never()).save(any(TimesheetReimbursementStatusHistory.class));
	}

	@Test
	@DisplayName("Create reimbursement should throw ValidationErrorException when reimbursements not enabled")
	void testCreateReimbursementReimbursementsNotEnabledThrowsValidationErrorException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetSetting timesheetSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(0);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheet.getTimesheetSettingId(),
				this.accountId))
			.willReturn(Optional.of(timesheetSetting));

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.createReimbursement(timesheetId, request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENTS_NOT_ENABLED);

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetSettingJpaRepository).should()
			.findByIdAndAccountId(timesheet.getTimesheetSettingId(), this.accountId);
		then(this.reimbursementMapper).should(never()).toEntity(any());
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
		then(this.statusHistoryJpaRepository).should(never()).save(any(TimesheetReimbursementStatusHistory.class));
	}

	@Test
	@DisplayName("Create reimbursement should throw ValidationErrorException when reimbursements enabled is null")
	void testCreateReimbursementReimbursementsEnabledNullThrowsValidationErrorException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetSetting timesheetSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(null);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheet.getTimesheetSettingId(),
				this.accountId))
			.willReturn(Optional.of(timesheetSetting));

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.createReimbursement(timesheetId, request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENTS_NOT_ENABLED);

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetSettingJpaRepository).should()
			.findByIdAndAccountId(timesheet.getTimesheetSettingId(), this.accountId);
		then(this.reimbursementMapper).should(never()).toEntity(any());
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
		then(this.statusHistoryJpaRepository).should(never()).save(any(TimesheetReimbursementStatusHistory.class));
	}

	@Test
	@DisplayName("Create reimbursement should throw ValidationErrorException when limit is reached")
	void testCreateReimbursementLimitReachedThrowsValidationErrorException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetSetting timesheetSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(1);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheet.getTimesheetSettingId(),
				this.accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.reimbursementJpaRepository.countByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(10L);

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.createReimbursement(timesheetId, request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_MAX_LIMIT_REACHED);

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetSettingJpaRepository).should()
			.findByIdAndAccountId(timesheet.getTimesheetSettingId(), this.accountId);
		then(this.reimbursementJpaRepository).should().countByTimesheetIdAndAccountId(timesheetId, this.accountId);
		then(this.reimbursementMapper).should(never()).toEntity(any());
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
		then(this.statusHistoryJpaRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("Create reimbursement without file should return success response")
	void testCreateReimbursementWithoutFileValidRequestReturnsSuccess() {
		// Given
		this.givenAuthenticatedPerformer("Contractor User");
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto request = ReimbursementTestDataFactory
			.createReimbursementRequestWithoutFile();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetSetting timesheetSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(1);
		TimesheetReimbursement reimbursementEntity = ReimbursementTestDataFactory
			.createReimbursementEntityWithoutFile();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntityWithoutFile();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createReimbursementResponseWithoutFile();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheet.getTimesheetSettingId(),
				this.accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.reimbursementMapper.toEntity(request)).willReturn(reimbursementEntity);
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.statusHistoryJpaRepository.save(any(TimesheetReimbursementStatusHistory.class)))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryEntity());
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.createReimbursement(timesheetId,
				request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetSettingJpaRepository).should()
			.findByIdAndAccountId(timesheet.getTimesheetSettingId(), this.accountId);
		then(this.reimbursementMapper).should().toEntity(request);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
		then(this.statusHistoryJpaRepository).should().save(any(TimesheetReimbursementStatusHistory.class));
		then(this.reimbursementMapper).should()
			.toResponseDto(savedReimbursement, ReimbursementTestDataFactory.getStatusSubmittedLabel());
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Update reimbursement should return success response")
	void testUpdateReimbursementValidRequestReturnsSuccess() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement existingReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.updateReimbursementResponse();

		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(existingReimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateReimbursement(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
		then(this.reimbursementMapper).should().updateEntityFromDto(request, existingReimbursement);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
		then(this.reimbursementMapper).should()
			.toResponseDto(savedReimbursement, ReimbursementTestDataFactory.getStatusSubmittedLabel());
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Update reimbursement should throw ResourceNotFoundException when reimbursement not found")
	void testUpdateReimbursementNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequest();

		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("TimesheetReimbursement")
			.hasMessageContaining(reimbursementId.toString());

		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
		then(this.reimbursementMapper).should(never()).updateEntityFromDto(any(), any());
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update reimbursement should throw ValidationErrorException when status is approved")
	void testUpdateReimbursementApprovedStatusThrowsValidationErrorException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement nonSubmittedReimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityNonSubmitted();

		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(nonSubmittedReimbursement));
		willThrow(new ValidationErrorException(ExceptionMessageConstants.REIMBURSEMENT_NOT_EDITABLE))
			.given(this.reimbursementAccessValidator)
			.validateAccessControlForUpdateReimbursement(timesheetId, this.accountId,
					nonSubmittedReimbursement.getStatus());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_NOT_EDITABLE);

		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
		then(this.reimbursementMapper).should(never()).updateEntityFromDto(any(), any());
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update reimbursement with partial fields should return success response")
	void testUpdateReimbursementPartialFieldsReturnsSuccess() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequestPartial();
		TimesheetReimbursement existingReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.updateReimbursementResponse();

		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(existingReimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateReimbursement(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.reimbursementMapper).should().updateEntityFromDto(request, existingReimbursement);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update reimbursement with skipStatusHistory true should not insert status history "
			+ "or publish Kafka notification")
	void testUpdateReimbursementSkipStatusHistoryDoesNotInsertStatusHistory() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory
			.updateReimbursementRequestSkipStatusHistory();
		TimesheetReimbursement existingReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.updateReimbursementResponse();

		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(existingReimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateReimbursement(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.statusHistoryJpaRepository).should(never()).save(any(TimesheetReimbursementStatusHistory.class));
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
	}

	// ===== reopenReimbursement Tests =====

	@Test
	@DisplayName("Reopen reimbursement with approved status should return success response")
	void testReopenReimbursementApprovedStatusReturnsSuccess() {
		// Given
		this.givenAuthenticatedPerformer("Agency User");
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		Integer agencyUserTypeId = ReimbursementTestDataFactory.getAgencyUserTypeId();
		ReopenReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReopenReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithStatus(ReimbursementConstants.STATUS_APPROVED);
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.createReimbursementResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(agencyUserTypeId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.statusHistoryJpaRepository.save(any(TimesheetReimbursementStatusHistory.class)))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryEntity());
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.reopenReimbursement(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
		then(this.statusHistoryJpaRepository).should().save(any(TimesheetReimbursementStatusHistory.class));
		then(this.reimbursementMapper).should()
			.toResponseDto(savedReimbursement, ReimbursementTestDataFactory.getStatusSubmittedLabel());
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
	}

	void testReopenReimbursementNonAgencyUserThrowsForbiddenAccessException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		Integer nonAgencyUserTypeId = ReimbursementTestDataFactory.getNonAgencyUserTypeId();
		ReopenReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReopenReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(nonAgencyUserTypeId);

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.reopenReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(ForbiddenAccessException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_REOPEN_FORBIDDEN);

		then(this.reimbursementJpaRepository).should(never())
			.findByIdAndTimesheetIdAndAccountId(anyInt(), anyInt(), anyInt());
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Reopen reimbursement should throw ResourceNotFoundException when timesheet not found")
	void testReopenReimbursementTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		Integer agencyUserTypeId = ReimbursementTestDataFactory.getAgencyUserTypeId();
		ReopenReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReopenReimbursementRequest();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(agencyUserTypeId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.reopenReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet")
			.hasMessageContaining(timesheetId.toString());

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetInvoiceRepository).should(never()).findByTimesheetId(anyInt(), anyInt());
		then(this.reimbursementJpaRepository).should(never())
			.findByIdAndTimesheetIdAndAccountId(anyInt(), anyInt(), anyInt());
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Reopen reimbursement should throw ConflictException when invoice is linked to timesheet")
	void testReopenReimbursementInvoiceLinkedThrowsConflictException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		Integer agencyUserTypeId = ReimbursementTestDataFactory.getAgencyUserTypeId();
		ReopenReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReopenReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(agencyUserTypeId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId))
			.willReturn(ReimbursementTestDataFactory.createTimesheetInvoiceEntity());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.reopenReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_INVOICE_LINKED);

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should(never())
			.findByIdAndTimesheetIdAndAccountId(anyInt(), anyInt(), anyInt());
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Reopen reimbursement should succeed when timesheet invoice row exists but invoiceId is null")
	void testReopenReimbursementSucceedsWhenInvoiceRowHasNullInvoiceId() {
		// Given
		this.givenAuthenticatedPerformer("Agency User");
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		Integer agencyUserTypeId = ReimbursementTestDataFactory.getAgencyUserTypeId();
		ReopenReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReopenReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithStatus(ReimbursementConstants.STATUS_APPROVED);
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.createReimbursementResponse();
		TimesheetInvoice invoiceWithNullId = new TimesheetInvoice();
		invoiceWithNullId.setId(1);
		invoiceWithNullId.setInvoiceId(null);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(agencyUserTypeId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId))
			.willReturn(invoiceWithNullId);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.statusHistoryJpaRepository.save(any(TimesheetReimbursementStatusHistory.class)))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryEntity());
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.reopenReimbursement(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, this.accountId);
	}

	@Test
	@DisplayName("Reopen reimbursement should throw ResourceNotFoundException when reimbursement not found")
	void testReopenReimbursementNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		Integer agencyUserTypeId = ReimbursementTestDataFactory.getAgencyUserTypeId();
		ReopenReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReopenReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(agencyUserTypeId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.reopenReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Reimbursement")
			.hasMessageContaining(reimbursementId.toString());

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Reopen reimbursement should throw ConflictException when status is submitted")
	void testReopenReimbursementSubmittedStatusThrowsConflictException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		Integer agencyUserTypeId = ReimbursementTestDataFactory.getAgencyUserTypeId();
		ReopenReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReopenReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithStatus(ReimbursementConstants.STATUS_SUBMITTED);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(agencyUserTypeId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.reopenReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_CANNOT_REOPEN);

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
		then(this.statusHistoryJpaRepository).should(never()).save(any(TimesheetReimbursementStatusHistory.class));
	}

	// ===== updatePayableBillable Tests =====

	@Test
	@DisplayName("Update payable billable should return success response with both flags")
	void testUpdatePayableBillableBothFlagsReturnsSuccess() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto request = ReimbursementTestDataFactory.createUpdatePayableBillableRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createApprovedReimbursementResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusApprovedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updatePayableBillable(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
		then(this.reimbursementMapper).should()
			.toResponseDto(savedReimbursement, ReimbursementTestDataFactory.getStatusApprovedLabel());
	}

	@Test
	@DisplayName("Update payable billable with only isPayable should return success response")
	void testUpdatePayableBillablePayableOnlyReturnsSuccess() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto request = ReimbursementTestDataFactory.createUpdatePayableOnlyRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createApprovedReimbursementResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusApprovedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updatePayableBillable(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update payable billable with only isBillable should return success response")
	void testUpdatePayableBillableBillableOnlyReturnsSuccess() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto request = ReimbursementTestDataFactory.createUpdateBillableOnlyRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createApprovedReimbursementResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusApprovedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updatePayableBillable(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update payable billable should throw ResourceNotFoundException when timesheet not found")
	void testUpdatePayableBillableTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto request = ReimbursementTestDataFactory.createUpdatePayableBillableRequest();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updatePayableBillable(timesheetId, reimbursementId, request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet")
			.hasMessageContaining(timesheetId.toString());

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update payable billable should throw ConflictException when invoice is linked")
	void testUpdatePayableBillableInvoiceLinkedThrowsConflictException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto request = ReimbursementTestDataFactory.createUpdatePayableBillableRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId))
			.willReturn(ReimbursementTestDataFactory.createTimesheetInvoiceEntity());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updatePayableBillable(timesheetId, reimbursementId, request))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_INVOICE_LINKED);

		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update payable billable should succeed when timesheet invoice row exists but invoiceId is null")
	void testUpdatePayableBillableSucceedsWhenInvoiceRowHasNullInvoiceId() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto request = ReimbursementTestDataFactory.createUpdatePayableBillableRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createApprovedReimbursementResponse();
		TimesheetInvoice invoiceWithNullId = new TimesheetInvoice();
		invoiceWithNullId.setId(1);
		invoiceWithNullId.setInvoiceId(null);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId))
			.willReturn(invoiceWithNullId);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusApprovedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updatePayableBillable(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update payable billable should throw ValidationErrorException when no fields provided")
	void testUpdatePayableBillableNoFieldsThrowsValidationErrorException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto request = ReimbursementTestDataFactory
			.createUpdatePayableBillableEmptyRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updatePayableBillable(timesheetId, reimbursementId, request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_PAYABLE_BILLABLE_REQUIRED);

		then(this.reimbursementJpaRepository).should(never())
			.findByIdAndTimesheetIdAndAccountId(anyInt(), anyInt(), anyInt());
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update payable billable should throw ResourceNotFoundException when reimbursement not found")
	void testUpdatePayableBillableReimbursementNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto request = ReimbursementTestDataFactory.createUpdatePayableBillableRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updatePayableBillable(timesheetId, reimbursementId, request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Reimbursement")
			.hasMessageContaining(reimbursementId.toString());

		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update payable billable should throw ConflictException when status is not approved")
	void testUpdatePayableBillableNotApprovedThrowsConflictException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto request = ReimbursementTestDataFactory.createUpdatePayableBillableRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithStatus(ReimbursementConstants.STATUS_SUBMITTED);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updatePayableBillable(timesheetId, reimbursementId, request))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_NOT_APPROVED_FOR_FLAGS);

		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update payable billable should throw ForbiddenAccessException when principal is not agency user type")
	void testUpdatePayableBillableNonAgencyUserThrowsForbiddenAccessException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto request = ReimbursementTestDataFactory.createUpdatePayableBillableRequest();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementTestDataFactory.getNonAgencyUserTypeId());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updatePayableBillable(timesheetId, reimbursementId, request))
			.isInstanceOf(ForbiddenAccessException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_UPDATE_FLAGS_FORBIDDEN);

		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	// ===== updateShareWithClient Tests =====

	@Test
	@DisplayName("Update share with client should publish a fresh notification when turned ON while Pending")
	void testUpdateShareWithClientPendingTurnedOnPublishesFreshNotification() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateShareWithClientRequestBodyDto request = ReimbursementTestDataFactory.createShareWithClientOnRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithSharedStatus(ReimbursementTestDataFactory.getNotSharedWithClient());
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithSharedStatus(ReimbursementTestDataFactory.getDefaultIsSharedWithClient());
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.updateShareWithClientResponse(ReimbursementTestDataFactory.getDefaultIsSharedWithClient());

		this.givenAuthenticatedPerformer("Agency User");
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusPendingLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateShareWithClient(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
		then(this.clientShareHistoryJpaRepository).should().save(any(TimesheetReimbursementClientShareHistory.class));
		ArgumentCaptor<TimesheetReminderNotificationPayloadDto> payloadCaptor = ArgumentCaptor
			.forClass(TimesheetReminderNotificationPayloadDto.class);
		then(this.kafkaProducerHelper).should().sendTimesheetReminderNotification(payloadCaptor.capture());
		assertThat(payloadCaptor.getValue().eventName())
			.isEqualTo(TimesheetReminderNotificationPayloadDto.EVENT_NAME_REIMBURSEMENT_SHARED_WITH_CLIENT_UPDATED);
	}

	@Test
	@DisplayName("Update share with client should not publish a notification when turned OFF while Pending")
	void testUpdateShareWithClientPendingTurnedOffDoesNotPublishNotification() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateShareWithClientRequestBodyDto request = ReimbursementTestDataFactory.createShareWithClientOffRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithSharedStatus(ReimbursementTestDataFactory.getDefaultIsSharedWithClient());
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithSharedStatus(ReimbursementTestDataFactory.getNotSharedWithClient());
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.updateShareWithClientResponse(ReimbursementTestDataFactory.getNotSharedWithClient());

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusPendingLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateShareWithClient(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.clientShareHistoryJpaRepository).should().save(any(TimesheetReimbursementClientShareHistory.class));
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Update share with client should stay editable and not publish a notification when claim is Approved")
	void testUpdateShareWithClientApprovedStatusAllowedNoNotification() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateShareWithClientRequestBodyDto request = ReimbursementTestDataFactory.createShareWithClientOnRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		reimbursement.setIsSharedWithClient(ReimbursementTestDataFactory.getNotSharedWithClient());
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		savedReimbursement.setIsSharedWithClient(ReimbursementTestDataFactory.getDefaultIsSharedWithClient());
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.updateShareWithClientResponse(ReimbursementTestDataFactory.getDefaultIsSharedWithClient());

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusApprovedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateShareWithClient(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
		then(this.clientShareHistoryJpaRepository).should().save(any(TimesheetReimbursementClientShareHistory.class));
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Update share with client should not duplicate notification when toggle is already ON")
	void testUpdateShareWithClientAlreadyOnStaysOnDoesNotDuplicateNotification() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateShareWithClientRequestBodyDto request = ReimbursementTestDataFactory.createShareWithClientOnRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithSharedStatus(ReimbursementTestDataFactory.getDefaultIsSharedWithClient());
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithSharedStatus(ReimbursementTestDataFactory.getDefaultIsSharedWithClient());
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.updateShareWithClientResponse(ReimbursementTestDataFactory.getDefaultIsSharedWithClient());

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusPendingLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateShareWithClient(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.clientShareHistoryJpaRepository).should(never())
			.save(any(TimesheetReimbursementClientShareHistory.class));
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Update share with client should throw ForbiddenAccessException for non-agency users")
	void testUpdateShareWithClientNonAgencyUserThrowsForbiddenAccessException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateShareWithClientRequestBodyDto request = ReimbursementTestDataFactory.createShareWithClientOnRequest();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementTestDataFactory.getNonAgencyUserTypeId());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updateShareWithClient(timesheetId, reimbursementId, request))
			.isInstanceOf(ForbiddenAccessException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_SHARE_WITH_CLIENT_FORBIDDEN);

		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Update share with client should throw ResourceNotFoundException when timesheet not found")
	void testUpdateShareWithClientTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateShareWithClientRequestBodyDto request = ReimbursementTestDataFactory.createShareWithClientOnRequest();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updateShareWithClient(timesheetId, reimbursementId, request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet")
			.hasMessageContaining(timesheetId.toString());

		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update share with client should throw ResourceNotFoundException when reimbursement not found")
	void testUpdateShareWithClientReimbursementNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateShareWithClientRequestBodyDto request = ReimbursementTestDataFactory.createShareWithClientOnRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updateShareWithClient(timesheetId, reimbursementId, request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Reimbursement")
			.hasMessageContaining(reimbursementId.toString());

		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	// ===== listReimbursements Tests =====

	@Test
	@DisplayName("List reimbursements should return list for contractor with hidden payable and billable fields")
	void testListReimbursementsContractorReturnsListWithHiddenFields() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setIsPayable(1);
		reimbursement.setIsBillable(1);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Map.of());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), any()))
			.willReturn(ReimbursementTestDataFactory.createReimbursementListItemResponse());

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getIsPayable()).isNull();
		assertThat(result.get(0).getIsBillable()).isNull();
		then(this.reimbursementJpaRepository).should().findAllByTimesheetIdAndAccountId(timesheetId, this.accountId);
	}

	@Test
	@DisplayName("List reimbursements should only return shared claims when caller is a client (CONTACT) principal")
	void testListReimbursementsContactPrincipalFiltersToSharedClaimsOnly() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement sharedReimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithSharedStatus(ReimbursementTestDataFactory.getDefaultIsSharedWithClient());

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTACT);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountIdAndIsSharedWithClient(timesheetId,
				this.accountId, ReimbursementTestDataFactory.getDefaultIsSharedWithClient()))
			.willReturn(List.of(sharedReimbursement));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Map.of());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), any()))
			.willReturn(ReimbursementTestDataFactory.createReimbursementListItemResponse());

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).hasSize(1);
		then(this.reimbursementJpaRepository).should()
			.findAllByTimesheetIdAndAccountIdAndIsSharedWithClient(timesheetId, this.accountId,
					ReimbursementTestDataFactory.getDefaultIsSharedWithClient());
		then(this.reimbursementJpaRepository).should(never())
			.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId);
	}

	@Test
	@DisplayName("List reimbursements should return list for agency with visible payable and billable fields")
	void testListReimbursementsAgencyReturnsListWithVisibleFields() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setStatus(ReimbursementConstants.STATUS_SUBMITTED);

		ReimbursementListItemResponseBodyDto responseDto = ReimbursementTestDataFactory
			.createReimbursementListItemResponse();
		responseDto.setIsPayable(1);
		responseDto.setIsBillable(1);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Map.of());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), any()))
			.willReturn(responseDto);

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getIsPayable()).isEqualTo(1);
		assertThat(result.get(0).getIsBillable()).isEqualTo(1);
	}

	@Test
	@DisplayName("List reimbursements should show pending status for agency when status is submitted")
	void testListReimbursementsAgencyShowsPendingForSubmitted() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setStatus(ReimbursementConstants.STATUS_SUBMITTED);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Map.of());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), eq("Pending")))
			.willReturn(ReimbursementTestDataFactory.createReimbursementListItemResponse());

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).hasSize(1);
		then(this.reimbursementMapper).should().toListItemResponseDto(any(TimesheetReimbursement.class), eq("Pending"));
	}

	@Test
	@DisplayName("List reimbursements should throw ResourceNotFoundException when timesheet not found")
	void testListReimbursementsTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();

		willThrow(new ResourceNotFoundException("Timesheet", timesheetId)).given(this.reimbursementAccessValidator)
			.validateTimesheetViewAccess(timesheetId, this.accountId);

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.listReimbursements(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet")
			.hasMessageContaining(timesheetId.toString());

		then(this.reimbursementJpaRepository).should(never()).findAllByTimesheetIdAndAccountId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("List reimbursements should return empty list when no reimbursements exist")
	void testListReimbursementsNoReimbursementsReturnsEmptyList() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of());
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Map.of());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).isEmpty();
		then(this.reimbursementJpaRepository).should().findAllByTimesheetIdAndAccountId(timesheetId, this.accountId);
	}

	@Test
	@DisplayName("List reimbursements should populate addedBy and updatedBy from agency and contractor detail maps")
	void testListReimbursementsPopulatesUserNamesFromAgencyAndContractorMaps() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setAddedBy(50);
		reimbursement.setAddedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());
		reimbursement.setUpdatedBy(60);
		reimbursement.setUpdatedByUserTypeId(UserTypeEnum.CONTRACTOR.getId());

		this.givenFetchUserAndContactUserIdsDelegatesToRealComponent();
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		given(this.userRepository.getUserDetailsMap(any()))
			.willReturn(Map.of(50, new UserDetailsQueryResultDto("Agency Recruiter Name", "agency.png")));
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());
		given(this.candidateRepository.getContractorQueryResultMap(any()))
			.willReturn(Map.of(60, new ContractorNamePhotoQueryResultDto("Contractor Name", "con.png", "c-slug")));
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), any()))
			.willReturn(ReimbursementTestDataFactory.createReimbursementListItemResponse());

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getAddedBy().getName()).isEqualTo("Agency Recruiter Name");
		assertThat(result.get(0).getAddedBy().getPhoto()).isEqualTo("agency.png");
		assertThat(result.get(0).getUpdatedBy().getName()).isEqualTo("Contractor Name");
		assertThat(result.get(0).getUpdatedBy().getPhoto()).isEqualTo("con.png");
	}

	@Test
	@DisplayName("List reimbursements should populate addedBy from company contact detail map")
	void testListReimbursementsPopulatesAddedByFromContactMap() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setAddedBy(77);
		reimbursement.setAddedByUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());
		reimbursement.setUpdatedBy(null);
		reimbursement.setUpdatedByUserTypeId(null);

		this.givenFetchUserAndContactUserIdsDelegatesToRealComponent();
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Map.of());
		given(this.contactRepository.getContactNamePhotoMap(any()))
			.willReturn(Map.of(77, new ContactNamePhotoQueryResultDto("Contact Full Name", "contact.jpg", null)));
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), any()))
			.willReturn(ReimbursementTestDataFactory.createReimbursementListItemResponse());

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result.get(0).getAddedBy().getName()).isEqualTo("Contact Full Name");
		assertThat(result.get(0).getAddedBy().getPhoto()).isEqualTo("contact.jpg");
	}

	@Test
	@DisplayName("List reimbursements should map agency recruiter without repository hit to user with null name")
	void testListReimbursementsAgencyRecruiterMissingFromUserMapUsesNullName() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setAddedBy(42);
		reimbursement.setAddedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Map.of());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), any()))
			.willReturn(ReimbursementTestDataFactory.createReimbursementListItemResponse());

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result.get(0).getAddedBy().getId()).isEqualTo(42);
		assertThat(result.get(0).getAddedBy().getName()).isNull();
		assertThat(result.get(0).getAddedBy().getPhoto()).isNull();
	}

	// ===== updateReimbursement Tests =====

	@Test
	@DisplayName("Update reimbursement by contractor should throw UnauthorizedAccessException when not owner")
	void testUpdateReimbursementContractorNotOwnerThrowsUnauthorizedAccessException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setAddedBy(999); // Different from current user

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(this.userTypeId);
		willThrow(new UnauthorizedAccessException(ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_UPDATE_OWN_ONLY))
			.given(this.reimbursementAccessValidator)
			.validateAccessControlForUpdateReimbursement(timesheetId, this.accountId, reimbursement.getStatus());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_UPDATE_OWN_ONLY);

		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
		then(this.s3ReimbursementService).should(never()).deleteReimbursementFile(any());
	}

	@Test
	@DisplayName("Update reimbursement should throw ConflictException when invoice linked")
	void testUpdateReimbursementInvoiceLinkedThrowsConflictException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(this.userTypeId);
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId))
			.willReturn(ReimbursementTestDataFactory.createTimesheetInvoiceEntity());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_INVOICE_LINKED);

		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, this.accountId);
		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
		then(this.s3ReimbursementService).should(never()).deleteReimbursementFile(any());
	}

	@Test
	@DisplayName("Update reimbursement by contractor owner should return success")
	void testUpdateReimbursementContractorOwnerReturnsSuccess() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setAddedBy(this.userId);
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.updateReimbursementResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(this.userTypeId);
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateReimbursement(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.reimbursementMapper).should().updateEntityFromDto(request, reimbursement);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update reimbursement should throw ForbiddenAccessException for contact user")
	void testUpdateReimbursementContactUserThrowsForbiddenAccessException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		willThrow(new ForbiddenAccessException(ExceptionMessageConstants.REIMBURSEMENT_UPDATE_FORBIDDEN))
			.given(this.reimbursementAccessValidator)
			.validateAccessControlForUpdateReimbursement(timesheetId, this.accountId, reimbursement.getStatus());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(ForbiddenAccessException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_UPDATE_FORBIDDEN);

		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
	}

	@Test
	@DisplayName("Update reimbursement should delete old file from S3 when file URL changes")
	void testUpdateReimbursementDeletesOldFileWhenDocumentTokenChanges() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement existingReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		String oldDocumentToken = existingReimbursement.getDocumentToken();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.updateReimbursementResponse();

		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(existingReimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.statusHistoryJpaRepository.save(any(TimesheetReimbursementStatusHistory.class)))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryEntity());
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request);

		// Then
		var order = inOrder(this.reimbursementJpaRepository, this.statusHistoryJpaRepository,
				this.s3ReimbursementService);
		order.verify(this.reimbursementJpaRepository).save(any(TimesheetReimbursement.class));
		order.verify(this.statusHistoryJpaRepository).save(any(TimesheetReimbursementStatusHistory.class));
		order.verify(this.s3ReimbursementService).deleteReimbursementFile(oldDocumentToken);
	}

	@Test
	@DisplayName("Update reimbursement should delete old file from S3 when file is removed")
	void testUpdateReimbursementDeletesOldFileWhenFileRemoved() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = new UpdateReimbursementRequestBodyDto("Updated description", null,
				null, null, null);
		TimesheetReimbursement existingReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		String oldDocumentToken = existingReimbursement.getDocumentToken();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.updateReimbursementResponse();

		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(existingReimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request);

		// Then
		then(this.s3ReimbursementService).should().deleteReimbursementFile(oldDocumentToken);
	}

	@Test
	@DisplayName("Update reimbursement should not delete S3 file when reimbursement save fails")
	void testUpdateReimbursementSaveFailsDoesNotDeleteS3File() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement existingReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();

		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(existingReimbursement));
		willThrow(new RuntimeException("save failed")).given(this.reimbursementJpaRepository)
			.save(any(TimesheetReimbursement.class));

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("save failed");

		then(this.s3ReimbursementService).should(never()).deleteReimbursementFile(any());
	}

	@Test
	@DisplayName("Update reimbursement should not delete S3 file when status history save fails")
	void testUpdateReimbursementStatusHistoryFailsDoesNotDeleteS3File() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement existingReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();

		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(existingReimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		willThrow(new RuntimeException("status history failed")).given(this.statusHistoryJpaRepository)
			.save(any(TimesheetReimbursementStatusHistory.class));

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("status history failed");

		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
		then(this.s3ReimbursementService).should(never()).deleteReimbursementFile(any());
	}

	@Test
	@DisplayName("Update reimbursement should not delete when existing has no file and request has no file")
	void testUpdateReimbursementNoDeleteWhenBothHaveNoFile() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequestPartial();
		TimesheetReimbursement existingReimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithoutFile();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntityWithoutFile();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createReimbursementResponseWithoutFile();

		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(existingReimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request);

		// Then
		then(this.s3ReimbursementService).should(never()).deleteReimbursementFile(any());
	}

	@Test
	@DisplayName("Update reimbursement should not delete when file URL is unchanged")
	void testUpdateReimbursementNoDeleteWhenDocumentTokenUnchanged() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		TimesheetReimbursement existingReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		UpdateReimbursementRequestBodyDto request = new UpdateReimbursementRequestBodyDto("Updated description", null,
				existingReimbursement.getDocumentToken(), existingReimbursement.getFileName(), null);
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.updateReimbursementResponse();

		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(existingReimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request);

		// Then
		then(this.s3ReimbursementService).should(never()).deleteReimbursementFile(any());
	}

	@Test
	@DisplayName("Update reimbursement should reset rejected status to submitted after successful update")
	void testUpdateReimbursementRejectedStatusResetsToSubmitted() {
		// Given
		this.givenAuthenticatedPerformer("Agency User");
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithStatus(ReimbursementConstants.STATUS_REJECTED);
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithStatus(ReimbursementConstants.STATUS_SUBMITTED);
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.updateReimbursementResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(this.userTypeId);
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		willAnswer((invocation) -> {
			UpdateReimbursementRequestBodyDto req = invocation.getArgument(0);
			TimesheetReimbursement entity = invocation.getArgument(1);
			entity.setDescription(req.getDescription());
			return null;
		}).given(this.reimbursementMapper)
			.updateEntityFromDto(any(UpdateReimbursementRequestBodyDto.class), any(TimesheetReimbursement.class));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateReimbursement(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		assertThat(reimbursement.getStatus()).isEqualTo(ReimbursementConstants.STATUS_SUBMITTED);
		ArgumentCaptor<TimesheetReimbursement> captor = ArgumentCaptor.forClass(TimesheetReimbursement.class);
		then(this.reimbursementJpaRepository).should().save(captor.capture());
		assertThat(captor.getValue().getStatus()).isEqualTo(ReimbursementConstants.STATUS_SUBMITTED);
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
	}

	// ===== updateReimbursementStatus Tests =====

	@Test
	@DisplayName("Approve reimbursement should return success response")
	void testApproveReimbursementReturnsSuccess() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementStatusRequestBodyDto request = new UpdateReimbursementStatusRequestBodyDto(
				ReimbursementConstants.STATUS_APPROVED, "Approved by manager");
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		savedReimbursement.setStatus(ReimbursementConstants.STATUS_APPROVED);
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createApprovedReimbursementResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.statusHistoryJpaRepository.save(any(TimesheetReimbursementStatusHistory.class)))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryEntity());
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusApprovedLabel()))
			.willReturn(expectedResponse);
		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(this.userTypeId);
		givenAuthenticatedPerformer("Test User");

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateReimbursementStatus(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		assertThat(reimbursement.getStatus()).isEqualTo(ReimbursementConstants.STATUS_APPROVED);
		assertThat(reimbursement.getIsPayable()).isEqualTo(1);
		assertThat(reimbursement.getIsBillable()).isEqualTo(1);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
		then(this.statusHistoryJpaRepository).should().save(any(TimesheetReimbursementStatusHistory.class));
		ArgumentCaptor<TimesheetReminderNotificationPayloadDto> payloadCaptor = ArgumentCaptor
			.forClass(TimesheetReminderNotificationPayloadDto.class);
		then(this.kafkaProducerHelper).should().sendTimesheetReminderNotification(payloadCaptor.capture());
		TimesheetReminderNotificationPayloadDto payload = payloadCaptor.getValue();
		assertThat(payload.eventName())
			.isEqualTo(TimesheetReminderNotificationPayloadDto.EVENT_NAME_REIMBURSEMENT_APPROVED);
		assertThat(payload.timesheetIds()).containsExactly(timesheetId);
		assertThat(payload.reimbursementIds()).containsExactly(reimbursementId);
	}

	@Test
	@DisplayName("Reject reimbursement should return success response")
	void testRejectReimbursementReturnsSuccess() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementStatusRequestBodyDto request = new UpdateReimbursementStatusRequestBodyDto(
				ReimbursementConstants.STATUS_REJECTED, "Rejected due to policy");
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		savedReimbursement.setStatus(ReimbursementConstants.STATUS_REJECTED);
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.createReimbursementResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.statusHistoryJpaRepository.save(any(TimesheetReimbursementStatusHistory.class)))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryEntity());
		given(this.reimbursementMapper.toResponseDto(any(TimesheetReimbursement.class), any()))
			.willReturn(expectedResponse);
		given(this.principalEntityExtractor.resolveUserTypeId(any())).willReturn(this.userTypeId);
		givenAuthenticatedPerformer("Test User");

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updateReimbursementStatus(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		assertThat(reimbursement.getStatus()).isEqualTo(ReimbursementConstants.STATUS_REJECTED);
		assertThat(reimbursement.getIsPayable()).isZero();
		assertThat(reimbursement.getIsBillable()).isZero();
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
		ArgumentCaptor<TimesheetReminderNotificationPayloadDto> payloadCaptor = ArgumentCaptor
			.forClass(TimesheetReminderNotificationPayloadDto.class);
		then(this.kafkaProducerHelper).should().sendTimesheetReminderNotification(payloadCaptor.capture());
		TimesheetReminderNotificationPayloadDto payload = payloadCaptor.getValue();
		assertThat(payload.eventName())
			.isEqualTo(TimesheetReminderNotificationPayloadDto.EVENT_NAME_REIMBURSEMENT_REJECTED);
		assertThat(payload.timesheetIds()).containsExactly(timesheetId);
		assertThat(payload.reimbursementIds()).containsExactly(reimbursementId);
	}

	@Test
	@DisplayName("Approve already approved reimbursement should throw ConflictException")
	void testApproveAlreadyApprovedThrowsConflictException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementStatusRequestBodyDto request = new UpdateReimbursementStatusRequestBodyDto(
				ReimbursementConstants.STATUS_APPROVED, "Trying to approve again");
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithStatus(ReimbursementConstants.STATUS_APPROVED);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.updateReimbursementStatus(timesheetId,
				reimbursementId, request))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_ALREADY_APPROVED);

		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Approve rejected reimbursement should throw ConflictException")
	void testApproveRejectedThrowsConflictException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementStatusRequestBodyDto request = new UpdateReimbursementStatusRequestBodyDto(
				ReimbursementConstants.STATUS_APPROVED, "Trying to approve rejected");
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithStatus(ReimbursementConstants.STATUS_REJECTED);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.updateReimbursementStatus(timesheetId,
				reimbursementId, request))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_ONLY_APPROVE_SUBMITTED);

		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Reject already rejected reimbursement should throw ConflictException")
	void testRejectAlreadyRejectedThrowsConflictException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementStatusRequestBodyDto request = new UpdateReimbursementStatusRequestBodyDto(
				ReimbursementConstants.STATUS_REJECTED, "Trying to reject again");
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithStatus(ReimbursementConstants.STATUS_REJECTED);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.updateReimbursementStatus(timesheetId,
				reimbursementId, request))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_ALREADY_REJECTED);

		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Reject approved reimbursement should throw ConflictException")
	void testRejectApprovedThrowsConflictException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementStatusRequestBodyDto request = new UpdateReimbursementStatusRequestBodyDto(
				ReimbursementConstants.STATUS_REJECTED, "Trying to reject approved");
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithStatus(ReimbursementConstants.STATUS_APPROVED);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.updateReimbursementStatus(timesheetId,
				reimbursementId, request))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_CANNOT_REJECT_APPROVED);

		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update status with null status should throw ValidationErrorException")
	void testUpdateStatusNullThrowsValidationErrorException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementStatusRequestBodyDto request = new UpdateReimbursementStatusRequestBodyDto(null,
				"Null status");
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.updateReimbursementStatus(timesheetId,
				reimbursementId, request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_INVALID_STATUS);

		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update status with invalid status value should throw ValidationErrorException")
	void testUpdateStatusInvalidValueThrowsValidationErrorException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementStatusRequestBodyDto request = new UpdateReimbursementStatusRequestBodyDto(99,
				"Invalid status");
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.updateReimbursementStatus(timesheetId,
				reimbursementId, request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_INVALID_STATUS);

		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@ParameterizedTest(name = "remark={0}")
	@MethodSource("blankRemarksForRejection")
	@DisplayName("Reject reimbursement without remark should throw ValidationErrorException")
	void testRejectReimbursementWithoutRemarkThrowsValidationErrorException(String remark) {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementStatusRequestBodyDto request = new UpdateReimbursementStatusRequestBodyDto(
				ReimbursementConstants.STATUS_REJECTED, remark);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.updateReimbursementStatus(timesheetId,
				reimbursementId, request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_REMARK_MANDATORY_FOR_REJECTION);

		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	private static Stream<Arguments> blankRemarksForRejection() {
		return Stream.of(Arguments.of((String) null), Arguments.of(""), Arguments.of("   "));
	}

	@Test
	@DisplayName("Update status by contractor should throw ForbiddenAccessException")
	void testUpdateStatusContractorThrowsForbiddenAccessException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementStatusRequestBodyDto request = new UpdateReimbursementStatusRequestBodyDto(
				ReimbursementConstants.STATUS_APPROVED, "Trying to approve");

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.updateReimbursementStatus(timesheetId,
				reimbursementId, request))
			.isInstanceOf(ForbiddenAccessException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_APPROVE_REJECT_FORBIDDEN);

		then(this.reimbursementJpaRepository).should(never())
			.findByIdAndTimesheetIdAndAccountId(anyInt(), anyInt(), anyInt());
	}

	// ===== getReimbursementStatusHistory Tests =====

	@Test
	@DisplayName("Get reimbursement status history should return list")
	void testGetReimbursementStatusHistoryReturnsList() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		TimesheetReimbursementStatusHistory history = ReimbursementTestDataFactory.createStatusHistoryEntity();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(ReimbursementTestDataFactory.createTimesheetEntity(timesheetId)));
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(ReimbursementTestDataFactory.createReimbursementEntity()));
		given(this.statusHistoryJpaRepository
			.findByTimesheetReimbursementIdAndAccountIdOrderByCreatedOnDesc(reimbursementId, this.accountId))
			.willReturn(List.of(history));
		given(this.reimbursementMapper.toStatusHistoryResponseDto(any(), any()))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryResponse());

		// When
		List<io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementStatusHistoryResponseBodyDto> result = this.timesheetReimbursementService
			.getReimbursementStatusHistory(timesheetId, reimbursementId);

		// Then
		assertThat(result).hasSize(1);
		then(this.statusHistoryJpaRepository).should()
			.findByTimesheetReimbursementIdAndAccountIdOrderByCreatedOnDesc(reimbursementId, this.accountId);
	}

	@Test
	@DisplayName("Get reimbursement status history should populate createdBy from agency user detail map")
	void testGetReimbursementStatusHistoryPopulatesCreatedByFromAgencyMap() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		TimesheetReimbursementStatusHistory history = ReimbursementTestDataFactory.createStatusHistoryEntity();
		history.setCreatedBy(200);
		history.setCreatedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());

		this.givenFetchUserAndContactUserIdsDelegatesToRealComponent();
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(ReimbursementTestDataFactory.createTimesheetEntity(timesheetId)));
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(ReimbursementTestDataFactory.createReimbursementEntity()));
		given(this.statusHistoryJpaRepository
			.findByTimesheetReimbursementIdAndAccountIdOrderByCreatedOnDesc(reimbursementId, this.accountId))
			.willReturn(List.of(history));
		given(this.userRepository.getUserDetailsMap(any()))
			.willReturn(Map.of(200, new UserDetailsQueryResultDto("History Author", "hist.png")));
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toStatusHistoryResponseDto(any(), any()))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryResponse());

		// When
		List<io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementStatusHistoryResponseBodyDto> result = this.timesheetReimbursementService
			.getReimbursementStatusHistory(timesheetId, reimbursementId);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getCreatedBy().getName()).isEqualTo("History Author");
		assertThat(result.get(0).getCreatedBy().getPhoto()).isEqualTo("hist.png");
	}

	@Test
	@DisplayName("Get status history should skip user fetch when createdByUserTypeId set but createdBy is null")
	void testGetReimbursementStatusHistorySkipsUserFetchWhenCreatedByNullWithUserTypeSet() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		TimesheetReimbursementStatusHistory history = ReimbursementTestDataFactory.createStatusHistoryEntity();
		history.setCreatedBy(null);
		history.setCreatedByUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(ReimbursementTestDataFactory.createTimesheetEntity(timesheetId)));
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(ReimbursementTestDataFactory.createReimbursementEntity()));
		given(this.statusHistoryJpaRepository
			.findByTimesheetReimbursementIdAndAccountIdOrderByCreatedOnDesc(reimbursementId, this.accountId))
			.willReturn(List.of(history));
		given(this.reimbursementMapper.toStatusHistoryResponseDto(any(), any()))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryResponse());

		// When
		List<io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementStatusHistoryResponseBodyDto> result = this.timesheetReimbursementService
			.getReimbursementStatusHistory(timesheetId, reimbursementId);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getCreatedBy()).isNull();
		then(this.fetchUserAndContactUserIds).should(never())
			.addUserToAppropriateSet(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Get status history should skip user fetch when createdByUserTypeId null but createdBy is set")
	void testGetReimbursementStatusHistorySkipsUserFetchWhenUserTypeNullWithCreatedBySet() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		TimesheetReimbursementStatusHistory history = ReimbursementTestDataFactory.createStatusHistoryEntity();
		history.setCreatedBy(88);
		history.setCreatedByUserTypeId(null);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(ReimbursementTestDataFactory.createTimesheetEntity(timesheetId)));
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(ReimbursementTestDataFactory.createReimbursementEntity()));
		given(this.statusHistoryJpaRepository
			.findByTimesheetReimbursementIdAndAccountIdOrderByCreatedOnDesc(reimbursementId, this.accountId))
			.willReturn(List.of(history));
		given(this.reimbursementMapper.toStatusHistoryResponseDto(any(), any()))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryResponse());

		// When
		List<io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementStatusHistoryResponseBodyDto> result = this.timesheetReimbursementService
			.getReimbursementStatusHistory(timesheetId, reimbursementId);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getCreatedBy().getId()).isEqualTo(88);
		assertThat(result.get(0).getCreatedBy().getName()).isNull();
		then(this.fetchUserAndContactUserIds).should(never())
			.addUserToAppropriateSet(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("Get status history should throw ResourceNotFoundException when timesheet not found")
	void testGetStatusHistoryTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.getReimbursementStatusHistory(timesheetId, reimbursementId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, this.accountId);
		then(this.statusHistoryJpaRepository).should(never())
			.findByTimesheetReimbursementIdAndAccountIdOrderByCreatedOnDesc(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Get status history should throw ResourceNotFoundException when reimbursement not found")
	void testGetStatusHistoryReimbursementNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(ReimbursementTestDataFactory.createTimesheetEntity(timesheetId)));
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.getReimbursementStatusHistory(timesheetId, reimbursementId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Reimbursement");

		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
	}

	// ===== deleteReimbursement Tests =====

	@Test
	@DisplayName("Delete submitted reimbursement by owner should succeed")
	void testDeleteReimbursementSubmittedByOwnerSucceeds() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setAddedBy(this.userId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);

		// When
		this.timesheetReimbursementService.deleteReimbursement(timesheetId, reimbursementId);

		// Then
		then(this.s3ReimbursementService).should().deleteReimbursementFile(reimbursement.getDocumentToken());
		then(this.statusHistoryJpaRepository).should().deleteByTimesheetReimbursementId(reimbursementId);
		then(this.reimbursementJpaRepository).should().delete(reimbursement);
	}

	@Test
	@DisplayName("Delete approved reimbursement should succeed if no invoice is linked")
	void testDeleteApprovedReimbursementSuccess() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		reimbursement.setAddedBy(this.userId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);

		// When
		this.timesheetReimbursementService.deleteReimbursement(timesheetId, reimbursementId);

		// Then
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, this.accountId);
		then(this.s3ReimbursementService).should().deleteReimbursementFile(reimbursement.getDocumentToken());
		then(this.statusHistoryJpaRepository).should().deleteByTimesheetReimbursementId(reimbursementId);
		then(this.reimbursementJpaRepository).should().delete(reimbursement);
	}

	@Test
	@DisplayName("Delete reimbursement when invoice linked should throw ConflictException")
	void testDeleteReimbursementInvoiceLinkedThrowsConflictException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		reimbursement.setAddedBy(this.userId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId))
			.willReturn(ReimbursementTestDataFactory.createTimesheetInvoiceEntity());

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.deleteReimbursement(timesheetId, reimbursementId))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_INVOICE_LINKED);

		then(this.s3ReimbursementService).should(never()).deleteReimbursementFile(any());
		then(this.reimbursementJpaRepository).should(never()).delete(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Delete reimbursement by contact user should throw ForbiddenAccessException")
	void testDeleteReimbursementContactUserThrowsForbiddenAccessException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		willThrow(new ForbiddenAccessException(ExceptionMessageConstants.REIMBURSEMENT_CREATE_FORBIDDEN))
			.given(this.reimbursementAccessValidator)
			.validateAccessControlForDeleteReimbursement(timesheetId, this.accountId);

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.deleteReimbursement(timesheetId, reimbursementId))
			.isInstanceOf(ForbiddenAccessException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_CREATE_FORBIDDEN);

		then(this.reimbursementJpaRepository).should()
			.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId, this.accountId);
	}

	@Test
	@DisplayName("Delete rejected reimbursement should succeed")
	void testDeleteRejectedReimbursementSucceeds() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory
			.createReimbursementEntityWithStatus(ReimbursementConstants.STATUS_REJECTED);
		reimbursement.setAddedBy(this.userId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);

		// When
		this.timesheetReimbursementService.deleteReimbursement(timesheetId, reimbursementId);

		// Then
		then(this.s3ReimbursementService).should().deleteReimbursementFile(reimbursement.getDocumentToken());
		then(this.statusHistoryJpaRepository).should().deleteByTimesheetReimbursementId(reimbursementId);
		then(this.reimbursementJpaRepository).should().delete(reimbursement);
	}

	@Test
	@DisplayName("Delete reimbursement without document should skip S3 deletion and succeed")
	void testDeleteReimbursementWithoutDocumentSucceeds() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setDocumentToken(null);
		reimbursement.setFileName(null);
		reimbursement.setAddedBy(this.userId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);

		// When
		this.timesheetReimbursementService.deleteReimbursement(timesheetId, reimbursementId);

		// Then
		then(this.s3ReimbursementService).should().deleteReimbursementFile(null);
		then(this.statusHistoryJpaRepository).should().deleteByTimesheetReimbursementId(reimbursementId);
		then(this.reimbursementJpaRepository).should().delete(reimbursement);
	}

	// ===== Access Control Tests =====

	@Test
	@DisplayName("List reimbursements should throw UnauthorizedAccessException when access checker denies access")
	void testListReimbursementsUserAccessDeniedThrowsUnauthorizedAccessException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		willThrow(new UnauthorizedAccessException("Access denied")).given(this.reimbursementAccessValidator)
			.validateTimesheetViewAccess(timesheetId, this.accountId);

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.listReimbursements(timesheetId))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Access denied");

		then(this.reimbursementJpaRepository).should(never()).findAllByTimesheetIdAndAccountId(anyInt(), anyInt());
	}

	// ===== Access Control: getReimbursementCount Tests =====

	@Test
	@DisplayName("Get reimbursement count should succeed for USER with VIEW_TIMESHEET access")
	void testGetReimbursementCountUserWithViewAccessReturnsSuccess() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(mock(UserPrincipal.class));
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(any())).willReturn(null);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(any())).willReturn(null);
		given(this.timesheetReimbursementRepository.getReimbursementCountByTimesheetIdAndEntity(timesheetId, null, null,
				this.accountId))
			.willReturn(5);

		// When
		Integer result = this.timesheetReimbursementService.getReimbursementCount(timesheetId);

		// Then
		assertThat(result).isEqualTo(5);
	}

	@Test
	@DisplayName("Get reimbursement count should throw ResourceNotFoundException when timesheet not found")
	void testGetReimbursementCountTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();

		willThrow(new ResourceNotFoundException("Timesheet", timesheetId)).given(this.reimbursementAccessValidator)
			.validateTimesheetViewAccess(timesheetId, this.accountId);

		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.getReimbursementCount(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");
	}

	@Test
	@DisplayName("Get reimbursement count should throw ValidationErrorException for null timesheet ID")
	void testGetReimbursementCountNullTimesheetIdThrowsValidationErrorException() {
		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.getReimbursementCount(null))
			.isInstanceOf(ValidationErrorException.class);
	}

	@Test
	@DisplayName("Get reimbursement count should throw ValidationErrorException for invalid timesheet ID")
	void testGetReimbursementCountInvalidTimesheetIdThrowsValidationErrorException() {
		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.getReimbursementCount(0))
			.isInstanceOf(ValidationErrorException.class);
	}

	@Test
	@DisplayName("Update payable billable should throw UnauthorizedAccessException when approve access denied")
	void testUpdatePayableBillableApproveAccessDeniedThrowsUnauthorizedAccessException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto request = ReimbursementTestDataFactory.createUpdatePayableBillableRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);

		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		willThrow(new UnauthorizedAccessException("Approve access denied")).given(this.reimbursementAccessValidator)
			.validateTimesheetApproveAccess(timesheetId, this.accountId);

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.updatePayableBillable(timesheetId, reimbursementId, request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Approve access denied");

		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update payable billable with only billable field should update only billable")
	void testUpdatePayableBillableOnlyBillableFieldUpdatesOnlyBillable() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdatePayableBillableRequestBodyDto request = ReimbursementTestDataFactory.createUpdateBillableOnlyRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		reimbursement.setIsPayable(0);
		reimbursement.setIsBillable(0);
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		savedReimbursement.setIsBillable(1);
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createApprovedReimbursementResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(this.userId);
		given(this.principalEntityExtractor.resolveUserTypeId(any()))
			.willReturn(ReimbursementConstants.USER_TYPE_AGENCY);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(reimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement, ReimbursementConstants.STATUS_APPROVED_LABEL))
			.willReturn(expectedResponse);

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.updatePayableBillable(timesheetId,
				reimbursementId, request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
		then(this.reimbursementJpaRepository).should().save(any(TimesheetReimbursement.class));
	}

	@Test
	@DisplayName("Update reimbursement should not delete file when existing document token is empty string")
	void testUpdateReimbursementEmptyExistingDocumentTokenSkipsS3Deletion() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		UpdateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement existingReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		existingReimbursement.setDocumentToken("");
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.updateReimbursementResponse();

		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(existingReimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request);

		// Then
		then(this.s3ReimbursementService).should(never()).deleteReimbursementFile(any());
	}

	@Test
	@DisplayName("Update reimbursement should not delete file when document token is same")
	void testUpdateReimbursementSameDocumentTokenSkipsS3Deletion() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		String sameToken = "https://example.com/receipt.pdf";
		UpdateReimbursementRequestBodyDto request = new UpdateReimbursementRequestBodyDto("Updated description",
				new BigDecimal("200.00"), sameToken, "receipt.pdf", null);
		TimesheetReimbursement existingReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		existingReimbursement.setDocumentToken(sameToken);
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.updateReimbursementResponse();

		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(existingReimbursement));
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);

		// When
		this.timesheetReimbursementService.updateReimbursement(timesheetId, reimbursementId, request);

		// Then
		then(this.s3ReimbursementService).should(never()).deleteReimbursementFile(any());
	}

	@Test
	@DisplayName("Get reimbursement count should validate entity access when entityType and entityId are present")
	void testGetReimbursementCountValidatesEntityAccessWhenPresent() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer entityType = 3;
		Integer entityId = 100;

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.auth.getUnifiedPrincipal()).willReturn(mock(UserPrincipal.class));
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(any())).willReturn(entityType);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(any())).willReturn(entityId);
		given(this.timesheetReimbursementRepository.getReimbursementCountByTimesheetIdAndEntity(timesheetId, entityType,
				entityId, this.accountId))
			.willReturn(3);

		// When
		Integer result = this.timesheetReimbursementService.getReimbursementCount(timesheetId);

		// Then
		assertThat(result).isEqualTo(3);
		then(this.entityAccessValidator).should().validateEntityAccess(entityType, entityId);
	}

	@Test
	@DisplayName("Get reimbursement count should skip entity validation when entityType is null")
	void testGetReimbursementCountSkipsEntityValidationWhenEntityTypeNull() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(mock(UserPrincipal.class));
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(any())).willReturn(null);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(any())).willReturn(100);
		given(this.timesheetReimbursementRepository.getReimbursementCountByTimesheetIdAndEntity(timesheetId, null, 100,
				this.accountId))
			.willReturn(2);

		// When
		Integer result = this.timesheetReimbursementService.getReimbursementCount(timesheetId);

		// Then
		assertThat(result).isEqualTo(2);
		then(this.entityAccessValidator).should(never()).validateEntityAccess(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Get reimbursement count should skip entity validation when entityId is null")
	void testGetReimbursementCountSkipsEntityValidationWhenEntityIdNull() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(mock(UserPrincipal.class));
		given(this.principalEntityExtractor.extractEntityTypeFromPrincipal(any())).willReturn(3);
		given(this.principalEntityExtractor.extractEntityIdFromPrincipal(any())).willReturn(null);
		given(this.timesheetReimbursementRepository.getReimbursementCountByTimesheetIdAndEntity(timesheetId, 3, null,
				this.accountId))
			.willReturn(1);

		// When
		Integer result = this.timesheetReimbursementService.getReimbursementCount(timesheetId);

		// Then
		assertThat(result).isEqualTo(1);
		then(this.entityAccessValidator).should(never()).validateEntityAccess(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Get reimbursement count should throw ValidationErrorException for negative timesheet ID")
	void testGetReimbursementCountNegativeTimesheetIdThrowsValidationErrorException() {
		// When & Then
		assertThatThrownBy(() -> this.timesheetReimbursementService.getReimbursementCount(-1))
			.isInstanceOf(ValidationErrorException.class);
	}

	@Test
	@DisplayName("Reopen reimbursement should throw UnauthorizedAccessException when edit access denied")
	void testReopenReimbursementEditAccessDeniedThrowsUnauthorizedAccessException() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		ReopenReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReopenReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId)).willReturn(null);
		willThrow(new UnauthorizedAccessException("Edit access denied")).given(this.reimbursementAccessValidator)
			.validateTimesheetEditAccess(timesheetId, this.accountId);

		// When & Then
		assertThatThrownBy(
				() -> this.timesheetReimbursementService.reopenReimbursement(timesheetId, reimbursementId, request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Edit access denied");

		then(this.reimbursementJpaRepository).should(never()).save(any(TimesheetReimbursement.class));
	}

	// ===== Additional Branch Coverage Tests =====

	@Test
	@DisplayName("List reimbursements with reimbursement having null addedBy and updatedBy should handle gracefully")
	void testListReimbursementsWithNullAddedByAndUpdatedByHandlesGracefully() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setAddedBy(null);
		reimbursement.setUpdatedBy(null);
		reimbursement.setAddedByUserTypeId(null);
		reimbursement.setUpdatedByUserTypeId(null);

		ReimbursementListItemResponseBodyDto responseDto = ReimbursementTestDataFactory
			.createReimbursementListItemResponse();
		responseDto.setAddedBy(null);
		responseDto.setUpdatedBy(null);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Map.of());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());
		given(this.candidateRepository.getContractorQueryResultMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), any()))
			.willReturn(responseDto);

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getAddedBy()).isNull();
		assertThat(result.get(0).getUpdatedBy()).isNull();
	}

	@Test
	@DisplayName("List reimbursements with agency user having null details in map should return user with null name")
	@SuppressWarnings("unchecked")
	void testListReimbursementsAgencyUserNotFoundInMapReturnsNullName() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setAddedByUserTypeId(2);
		reimbursement.setAddedBy(999);

		ReimbursementListItemResponseBodyDto responseDto = ReimbursementTestDataFactory
			.createReimbursementListItemResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		willAnswer((invocation) -> {
			Set<Integer> agencyUserIds = invocation.getArgument(2);
			agencyUserIds.add(999);
			return null;
		}).given(this.fetchUserAndContactUserIds).addUserToAppropriateSet(any(), any(), any(), any(), any());
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), any()))
			.willReturn(responseDto);

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).hasSize(1);
		then(this.userRepository).should().getUserDetailsMap(any());
	}

	@Test
	@DisplayName("List reimbursements with contact user having null details in map should return user with null name")
	@SuppressWarnings("unchecked")
	void testListReimbursementsContactUserNotFoundInMapReturnsNullName() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setAddedByUserTypeId(1);
		reimbursement.setAddedBy(999);

		ReimbursementListItemResponseBodyDto responseDto = ReimbursementTestDataFactory
			.createReimbursementListItemResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		willAnswer((invocation) -> {
			Set<Integer> contactUserIds = invocation.getArgument(3);
			contactUserIds.add(999);
			return null;
		}).given(this.fetchUserAndContactUserIds).addUserToAppropriateSet(any(), any(), any(), any(), any());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), any()))
			.willReturn(responseDto);

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).hasSize(1);
		then(this.contactRepository).should().getContactNamePhotoMap(any());
	}

	@Test
	@DisplayName("List reimbursements with contractor user having null details in map should return user with null name")
	@SuppressWarnings("unchecked")
	void testListReimbursementsContractorUserNotFoundInMapReturnsNullName() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setAddedByUserTypeId(3);
		reimbursement.setAddedBy(999);

		ReimbursementListItemResponseBodyDto responseDto = ReimbursementTestDataFactory
			.createReimbursementListItemResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		willAnswer((invocation) -> {
			Set<Integer> contractorUserIds = invocation.getArgument(4);
			contractorUserIds.add(999);
			return null;
		}).given(this.fetchUserAndContactUserIds).addUserToAppropriateSet(any(), any(), any(), any(), any());
		given(this.candidateRepository.getContractorQueryResultMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), any()))
			.willReturn(responseDto);

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).hasSize(1);
		then(this.candidateRepository).should().getContractorQueryResultMap(any());
	}

	@Test
	@DisplayName("List reimbursements with unknown user type should return user with id and null name")
	void testListReimbursementsUnknownUserTypeReturnsUserWithIdOnly() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setAddedByUserTypeId(99);
		reimbursement.setAddedBy(999);

		ReimbursementListItemResponseBodyDto responseDto = ReimbursementTestDataFactory
			.createReimbursementListItemResponse();

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Map.of());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());
		given(this.candidateRepository.getContractorQueryResultMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), any()))
			.willReturn(responseDto);

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).hasSize(1);
	}

	@Test
	@DisplayName("List reimbursements shows submitted label for contractor when status is submitted")
	void testListReimbursementsContractorShowsSubmittedForSubmittedStatus() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setStatus(ReimbursementConstants.STATUS_SUBMITTED);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Map.of());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), eq("Submitted")))
			.willReturn(ReimbursementTestDataFactory.createReimbursementListItemResponse());

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).hasSize(1);
		then(this.reimbursementMapper).should()
			.toListItemResponseDto(any(TimesheetReimbursement.class), eq("Submitted"));
	}

	@Test
	@DisplayName("List reimbursements shows correct label for non-submitted status")
	void testListReimbursementsNonSubmittedStatusShowsCorrectLabel() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetReimbursement reimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		reimbursement.setStatus(ReimbursementConstants.STATUS_APPROVED);

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.reimbursementJpaRepository.findAllByTimesheetIdAndAccountId(timesheetId, this.accountId))
			.willReturn(List.of(reimbursement));
		given(this.userRepository.getUserDetailsMap(any())).willReturn(Map.of());
		given(this.contactRepository.getContactNamePhotoMap(any())).willReturn(Map.of());
		given(this.reimbursementMapper.toListItemResponseDto(any(TimesheetReimbursement.class), eq("Approved")))
			.willReturn(ReimbursementTestDataFactory.createReimbursementListItemResponse());

		// When
		List<ReimbursementListItemResponseBodyDto> result = this.timesheetReimbursementService
			.listReimbursements(timesheetId);

		// Then
		assertThat(result).hasSize(1);
		then(this.reimbursementMapper).should()
			.toListItemResponseDto(any(TimesheetReimbursement.class), eq("Approved"));
	}

	@Test
	@DisplayName("Get status history with history having null createdBy should handle gracefully")
	void testGetStatusHistoryWithNullCreatedByHandlesGracefully() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		TimesheetReimbursementStatusHistory history = ReimbursementTestDataFactory.createStatusHistoryEntity();
		history.setCreatedBy(null);
		history.setCreatedByUserTypeId(null);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(ReimbursementTestDataFactory.createTimesheetEntity(timesheetId)));
		given(this.reimbursementJpaRepository.findByIdAndTimesheetIdAndAccountId(reimbursementId, timesheetId,
				this.accountId))
			.willReturn(Optional.of(ReimbursementTestDataFactory.createReimbursementEntity()));
		given(this.statusHistoryJpaRepository
			.findByTimesheetReimbursementIdAndAccountIdOrderByCreatedOnDesc(reimbursementId, this.accountId))
			.willReturn(List.of(history));
		given(this.reimbursementMapper.toStatusHistoryResponseDto(any(), any()))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryResponse());

		// When
		List<io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementStatusHistoryResponseBodyDto> result = this.timesheetReimbursementService
			.getReimbursementStatusHistory(timesheetId, reimbursementId);

		// Then
		assertThat(result).hasSize(1);
	}

	@Test
	@DisplayName("Validate invoice not linked when invoice exists but invoiceId is null should pass")
	void testValidateInvoiceNotLinkedWhenInvoiceIdNullShouldPass() {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		CreateReimbursementRequestBodyDto request = ReimbursementTestDataFactory.createReimbursementRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(timesheetId);
		TimesheetSetting timesheetSetting = ReimbursementTestDataFactory.createTimesheetSettingEntity(1);
		TimesheetReimbursement reimbursementEntity = ReimbursementTestDataFactory.createReimbursementEntity();
		TimesheetReimbursement savedReimbursement = ReimbursementTestDataFactory.createReimbursementEntity();
		ReimbursementResponseBodyDto expectedResponse = ReimbursementTestDataFactory.createReimbursementResponse();
		io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice invoiceWithNullId = new io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice();
		invoiceWithNullId.setId(1);
		invoiceWithNullId.setInvoiceId(null);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheet.getTimesheetSettingId(),
				this.accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, this.accountId))
			.willReturn(invoiceWithNullId);
		given(this.reimbursementMapper.toEntity(request)).willReturn(reimbursementEntity);
		given(this.reimbursementJpaRepository.save(any(TimesheetReimbursement.class))).willReturn(savedReimbursement);
		given(this.statusHistoryJpaRepository.save(any(TimesheetReimbursementStatusHistory.class)))
			.willReturn(ReimbursementTestDataFactory.createStatusHistoryEntity());
		given(this.reimbursementMapper.toResponseDto(savedReimbursement,
				ReimbursementTestDataFactory.getStatusSubmittedLabel()))
			.willReturn(expectedResponse);
		givenAuthenticatedPerformer("Test User");

		// When
		ReimbursementResponseBodyDto result = this.timesheetReimbursementService.createReimbursement(timesheetId,
				request);

		// Then
		assertThat(result).isEqualTo(expectedResponse);
	}

	@Test
	@DisplayName("(Reflection) scheduleReimbursementSubmittedNotificationAfterCommit should defer Kafka until after commit")
	void testScheduleReimbursementSubmittedNotificationAfterCommitDefersUntilCommit() throws Exception {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		Method method = TimesheetReimbursementService.class.getDeclaredMethod(
				"scheduleReimbursementSubmittedNotificationAfterCommit", Integer.class, Integer.class, Integer.class,
				Integer.class);
		method.setAccessible(true);
		TransactionSynchronizationManager.initSynchronization();

		try {
			// When
			method.invoke(this.timesheetReimbursementService, timesheetId, reimbursementId, this.accountId,
					this.userTypeId);

			// Then
			then(this.kafkaProducerHelper).shouldHaveNoInteractions();
			TransactionSynchronizationManager.getSynchronizations().forEach((sync) -> sync.afterCommit());
			then(this.kafkaProducerHelper).should()
				.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
		}
		finally {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	@DisplayName("(Reflection) runAfterCommitOrNow should publish immediately when no transaction synchronization is active")
	void testRunAfterCommitOrNowPublishesImmediatelyWithoutActiveSynchronization() throws Exception {
		// Given
		Integer timesheetId = ReimbursementTestDataFactory.getDefaultTimesheetId();
		Integer reimbursementId = ReimbursementTestDataFactory.getDefaultReimbursementId();
		Method method = TimesheetReimbursementService.class.getDeclaredMethod(
				"scheduleReimbursementSubmittedNotificationAfterCommit", Integer.class, Integer.class, Integer.class,
				Integer.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetReimbursementService, timesheetId, reimbursementId, this.accountId,
				this.userTypeId);

		// Then
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(any(TimesheetReminderNotificationPayloadDto.class));
	}

}
