package io.recruitcrm.microservice.timesheet.services.timesheet_logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyIterable;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import io.recruitcrm.contract_staffing.entity.model.ApprovalStatusEnum;
import io.recruitcrm.contract_staffing.entity.model.CustomRule;
import io.recruitcrm.contract_staffing.entity.model.BreakInterval;
import io.recruitcrm.contract_staffing.entity.model.TimeLog;
import io.recruitcrm.contract_staffing.entity.model.TimeLogInterval;
import io.recruitcrm.contract_staffing.entity.model.TimeLogBreakInterval;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApproval;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.workTimeEnum;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckRequest;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContactPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.dao.invoice.InvoicesJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.time_log.TimeLogJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_approval.TimesheetApprovalJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet_setting.TimesheetSettingJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BatchOperationData;
import io.recruitcrm.microservice.timesheet.dto.time_log.BreakIntervalDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BreakIntervalResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalUpsertDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogUpsertDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.WorkTimeDetailDto;
import io.recruitcrm.microservice.timesheet.dto.time_log_interval.TimeLogIntervalDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkTimeLogRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.BulkUpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeDetailSummaryDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.WorkTimeDetailResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogWorkSummaryQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.DayTimelogQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.FetchBulkTimelogResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimesheetJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.TimelogWithSettingQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.bulk.contractor.FetchBulkContractorTimelogResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetLogQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetSettingErrorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.TimelogsMetaDataDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetSettingErrorResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.TimesheetUpdateHelper;
import io.recruitcrm.microservice.timesheet.kafka.KafkaProducerHelper;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationPayloadDto;
import io.recruitcrm.microservice.timesheet.dto.kafka.TimesheetReminderNotificationChannelsDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;

import io.recruitcrm.microservice.timesheet.mapper.TimeLogMapper;
import io.recruitcrm.microservice.timesheet.repositories.candidate.CandidateRepository;
import io.recruitcrm.microservice.timesheet.repositories.contact.ContactRepository;
import io.recruitcrm.microservice.timesheet.repositories.invoice.TimesheetInvoiceRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogBreakIntervalRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogIntervalRepository;
import io.recruitcrm.microservice.timesheet.repositories.time_log.TimeLogRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approval.TimesheetApprovalRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approver.TimesheetApproverRepository;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_setting.TimesheetSettingRepository;
import io.recruitcrm.microservice.timesheet.repositories.user.UserRepository;
import io.recruitcrm.microservice.timesheet.services.rule_engine.RuleEngineService;
import io.recruitcrm.microservice.timesheet.services.webhook_kafka_event.WebhookEvent;
import io.recruitcrm.microservice.timesheet.services.webhook_kafka_event.WebhookKafkaEventService;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEngineRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.rule_engine.RuleEngineResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetLogsTestDataFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.support.SendResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TimesheetLogsServiceTests {

	@InjectMocks
	private TimesheetLogsService timesheetLogsService;

	@Mock
	private TimesheetJpaRepository timesheetJpaRepository;

	@Mock
	private TimesheetSettingJpaRepository timesheetSettingJpaRepository;

	@Mock
	private TimeLogMapper timeLogMapper;

	@Mock
	private TimesheetInvoiceRepository timesheetInvoiceRepository;

	@Mock
	private InvoicesJpaRepository invoicesJpaRepository;

	@Mock
	private TimesheetApprovalJpaRepository timesheetApprovalJpaRepository;

	@Mock
	private TimeLogJpaRepository timeLogJpaRepository;

	@Mock
	private TimesheetApprovalRepository timesheetApprovalRepository;

	@Mock
	private TimeLogRepository timeLogRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ContactRepository contactRepository;

	@Mock
	private CandidateRepository candidateRepository;

	@Mock
	private TimesheetApproverRepository timesheetApproverRepository;

	@Mock
	private TimesheetRepository timesheetRepository;

	@Mock
	private TimeLogBreakIntervalRepository timeLogBreakIntervalRepository;

	@Mock
	private TimeLogIntervalRepository timeLogIntervalRepository;

	@Mock
	private AuthHolder auth;

	@Mock
	private TimesheetUpdateHelper timesheetUpdateHelper;

	@Mock
	private AccessControlChecker contractStaffingAccessControlChecker;

	@Mock
	private TimesheetSettingRepository timesheetSettingRepository;

	@Mock
	private RuleEngineService ruleEngineService;

	@Mock
	private KafkaProducerHelper kafkaProducerHelper;

	@Mock
	private io.recruitcrm.microservice.timesheet.services.invoice.ITimesheetInvoiceService timesheetInvoiceService;

	@Mock
	private io.recruitcrm.microservice.timesheet.services.portals.PortalAccessControlService portalAccessControlService;

	@Mock
	private WebhookKafkaEventService webhookKafkaEventService;

	@Mock
	private jakarta.persistence.EntityManager entityManager;

	@Mock
	private PlatformTransactionManager platformTransactionManager;

	@Spy
	private ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		// Inject EntityManager mock (PersistenceContext fields not injected by Mockito)
		ReflectionTestUtils.setField(this.timesheetLogsService, "entityManager", this.entityManager);

		@SuppressWarnings("unchecked")
		SendResult<String, String> kafkaSendResult = mock(SendResult.class);
		lenient()
			.when(this.kafkaProducerHelper.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class)))
			.thenReturn(CompletableFuture.completedFuture(kafkaSendResult));

		TransactionStatus transactionStatus = mock(TransactionStatus.class);
		lenient().when(this.platformTransactionManager.getTransaction(any(TransactionDefinition.class)))
			.thenReturn(transactionStatus);
		lenient().doNothing().when(this.platformTransactionManager).commit(transactionStatus);
		lenient().doNothing().when(this.platformTransactionManager).rollback(transactionStatus);

		lenient().when(this.timesheetJpaRepository.findByIdInAndAccountId(anyList(), anyInt()))
			.thenAnswer((invocation) -> {
				@SuppressWarnings("unchecked")
				List<Integer> ids = invocation.getArgument(0);
				List<Timesheet> out = new ArrayList<>();
				for (Integer id : ids) {
					Timesheet t = new Timesheet();
					t.setId(id);
					t.setTimesheetSettingId(TimesheetLogsTestDataFactory.getDefaultTimesheetSettingId());
					t.setPeriodStart(1704067200);
					t.setPeriodEnd(1704153600);
					out.add(t);
				}
				return out;
			});
		lenient().when(this.timesheetSettingJpaRepository.findByIdInAndAccountId(anyList(), anyInt()))
			.thenAnswer((invocation) -> {
				@SuppressWarnings("unchecked")
				List<Integer> settingIds = invocation.getArgument(0);
				return settingIds.stream().map((sid) -> {
					TimesheetSetting s = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
					s.setId(sid);
					return s;
				}).toList();
			});

		// Given - Setup auth mock in all test methods
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(TimesheetLogsTestDataFactory.getDefaultAccountId());
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier())
			.willReturn(TimesheetLogsTestDataFactory.getDefaultUserId());

		// Setup rule engine service mock to prevent null pointer exceptions
		given(this.ruleEngineService.evaluateRules(any(RuleEngineRequestBodyDto.class)))
			.willReturn(mock(RuleEngineResponseBodyDto.class));

		// Setup timeLogIntervalRepository mock for getTimeLogsByTimesheetId and related
		// flows
		given(this.timeLogIntervalRepository.findByTimeLogIdIn(anyList())).willReturn(Collections.emptyList());

		// Lenient stub for getAllTimeLogs company data (used when tests don't override)
		lenient().when(this.timeLogRepository.findCompanyByTimesheetIds(anyList(), anyInt()))
			.thenReturn(new HashMap<>());

		// Lenient stub for bulk update batch flow: findAllById returns TimeLogs for
		// requested ids
		lenient().when(this.timeLogJpaRepository.findAllById(anyIterable())).thenAnswer((invocation) -> {
			Iterable<Integer> ids = invocation.getArgument(0);
			Map<Integer, TimeLog> uniqueById = new LinkedHashMap<>();
			for (Integer id : ids) {
				if (id == null || uniqueById.containsKey(id)) {
					continue;
				}
				TimeLog tl = TimesheetLogsTestDataFactory.createTimeLog();
				tl.setId(id);
				tl.setTimesheetId((id == 2) ? 2 : TimesheetLogsTestDataFactory.getDefaultTimesheetId());
				uniqueById.put(id, tl);
			}
			return new ArrayList<>(uniqueById.values());
		});

		// Lenient stub for batch upsert (bulk update flow)
		lenient().when(this.timeLogRepository.batchUpsert(anyList())).thenReturn(1);
		lenient().when(this.timeLogIntervalRepository.batchUpsert(anyList())).thenReturn(1);
		lenient().doNothing().when(this.timeLogIntervalRepository).deleteByIdIn(anyList());

		lenient().doNothing()
			.when(this.webhookKafkaEventService)
			.triggerTimesheetWebhookEvent(any(WebhookEvent.class), anyList());

		lenient().when(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.thenReturn(Collections.emptyList());
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return success response")
	void testGetTimeLogsByTimesheetIdValidRequestReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		TimesheetResponseBodyDto expectedResponse = TimesheetLogsTestDataFactory.createTimesheetResponse();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers)).willReturn(expectedResponse.getApprovers());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getTimesheetStartDay()).isEqualTo(queryResult.getTimesheetStartDay());
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		assertThat(result.getApprovedByUserTypeId()).isEqualTo(approval.getUserTypeId());
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
		then(this.timeLogJpaRepository).should().findByTimesheetId(timesheetId);
		then(this.timesheetApprovalJpaRepository).should().findFirstByTimesheetIdOrderByIdDesc(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should throw ResourceNotFoundException when timesheet not found")
	void testGetTimeLogsByTimesheetIdTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timeLogRepository).should(never()).getTimeLogByTimesheetId(anyInt());
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should throw ResourceNotFoundException when time logs not found")
	void testGetTimeLogsByTimesheetIdTimeLogsNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(Arrays.asList());

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Time logs for timesheet");

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
		then(this.timeLogJpaRepository).should().findByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should throw ResourceNotFoundException when timesheet approval not found")
	void testGetTimeLogsByTimesheetIdTimesheetApprovalNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("TimesheetApproval for timesheet");

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
		then(this.timeLogJpaRepository).should().findByTimesheetId(timesheetId);
		then(this.timesheetApprovalJpaRepository).should().findFirstByTimesheetIdOrderByIdDesc(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle approved status with weekly overtime")
	void testGetTimeLogsByTimesheetIdApprovedStatusWithWeeklyOvertimeReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<CustomRule> customRules = TimesheetLogsTestDataFactory.createCustomRuleList();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		queryResult.setCustomRules(customRules);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummary);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId))).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.APPROVED.getId());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		assertThat(result.getCreatedOn()).isEqualTo(1704067200);
		assertThat(result.getIsWeeklyEnabled()).isTrue();
		assertThat(result.getPayCurrencySymbol()).isEqualTo("$");
		assertThat(result.getPayCurrencyCode()).isEqualTo("USD");
		assertThat(result.getBillCurrencySymbol()).isEqualTo("$");
		assertThat(result.getBillCurrencyCode()).isEqualTo("USD");
		then(this.timeLogJpaRepository).should().getTimeLogWorkSummaries(Arrays.asList(timesheetId));
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return isRemarkMandatory true")
	void testGetTimeLogsByTimesheetIdWithIsRemarkMandatoryTrue() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsRemarkMandatory(1);
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers)).willReturn(null);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(1);
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return isRemarkMandatory null")
	void testGetTimeLogsByTimesheetIdWithIsRemarkMandatoryNull() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsRemarkMandatory(null);
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers)).willReturn(null);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return isUnplannedHoursPayEnabled as 1 when enabled")
	void testGetTimeLogsByTimesheetIdWithIsUnplannedHoursPayEnabled() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsUnplannedHoursPayEnabled(1);
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers)).willReturn(null);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsUnplannedHoursPayEnabled()).isEqualTo(1);
		assertThat(result.getIsUnplannedHoursPayEnabled()).isEqualTo(queryResult.getIsUnplannedHoursPayEnabled());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return isUnplannedHoursPayEnabled as 0 when disabled")
	void testGetTimeLogsByTimesheetIdWithIsUnplannedHoursPayDisabled() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsUnplannedHoursPayEnabled(0);
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers)).willReturn(null);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsUnplannedHoursPayEnabled()).isZero();
		assertThat(result.getIsUnplannedHoursPayEnabled()).isEqualTo(queryResult.getIsUnplannedHoursPayEnabled());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return isUnplannedHoursPayEnabled as null when not set")
	void testGetTimeLogsByTimesheetIdWithIsUnplannedHoursPayEnabledNull() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsUnplannedHoursPayEnabled(null);
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers)).willReturn(null);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsUnplannedHoursPayEnabled()).isNull();
		assertThat(result.getIsUnplannedHoursPayEnabled()).isEqualTo(queryResult.getIsUnplannedHoursPayEnabled());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return isRemarkMandatory with calculateBreakTime true")
	void testGetTimeLogsByTimesheetIdWithIsRemarkMandatoryAndCalculateBreakTimeTrue() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsRemarkMandatory(1);
		queryResult.setCalculateBreakTime(true);
		queryResult.setBreakTimeThreshold(30);
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers)).willReturn(null);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(1);
		assertThat(result.getCalculateBreakTime()).isTrue();
		assertThat(result.getBreakTimeThreshold()).isEqualTo(Integer.valueOf(30));
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return isRemarkMandatory false with calculateBreakTime false")
	void testGetTimeLogsByTimesheetIdWithIsRemarkMandatoryFalseAndCalculateBreakTimeFalse() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsRemarkMandatory(0);
		queryResult.setCalculateBreakTime(false);
		queryResult.setBreakTimeThreshold(null);
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers)).willReturn(null);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isZero();
		assertThat(result.getCalculateBreakTime()).isFalse();
		assertThat(result.getBreakTimeThreshold()).isNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return isRemarkMandatory with approved status")
	void testGetTimeLogsByTimesheetIdWithIsRemarkMandatoryAndApprovedStatus() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsRemarkMandatory(1);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummary);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId))).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(1);
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.APPROVED.getId());
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return isRemarkMandatory with rejected status")
	void testGetTimeLogsByTimesheetIdWithIsRemarkMandatoryAndRejectedStatus() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForContractor();
		queryResult.setIsRemarkMandatory(0);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.REJECTED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithRejectedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		ContractorNamePhotoQueryResultDto contractorDetails = TimesheetLogsTestDataFactory
			.createContractorNamePhotoQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.candidateRepository.getContractorQueryResultMap(Set.of(queryResult.getEntityId())))
			.willReturn(Map.of(queryResult.getEntityId(), contractorDetails));

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isZero();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.REJECTED.getId());
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return isRemarkMandatory with all timesheet setting fields")
	void testGetTimeLogsByTimesheetIdWithIsRemarkMandatoryAndAllTimesheetSettingFields() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsRemarkMandatory(1);
		queryResult.setCalculateBreakTime(true);
		queryResult.setBreakTimeThreshold(60);
		queryResult.setWorkLogType(workTimeEnum.ENTER_WORK_TIME.getId());
		queryResult.setTimesheetFrequency(1);
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers)).willReturn(null);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(1);
		assertThat(result.getCalculateBreakTime()).isTrue();
		assertThat(result.getBreakTimeThreshold()).isEqualTo(Integer.valueOf(60));
		assertThat(result.getWorkLogType()).isEqualTo(queryResult.getWorkLogType());
		assertThat(result.getTimesheetFrequency()).isEqualTo(queryResult.getTimesheetFrequency());
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return isRemarkMandatory with agency recruiter approver")
	void testGetTimeLogsByTimesheetIdWithIsRemarkMandatoryAndAgencyRecruiterApprover() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForAgencyRecruiter();
		queryResult.setIsRemarkMandatory(1);
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		UserDetailsQueryResultDto userDetails = TimesheetLogsTestDataFactory.createUserDetailsQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId())).willReturn(userDetails);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(1);
		assertThat(result.getApprovedBy()).isEqualTo(userDetails.getName());
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return isRemarkMandatory with company contact approver")
	void testGetTimeLogsByTimesheetIdWithIsRemarkMandatoryAndCompanyContactApprover() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setIsRemarkMandatory(0);
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		ContactNamePhotoQueryResultDto contactDetails = TimesheetLogsTestDataFactory
			.createContactNamePhotoQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.contactRepository.getContactNamePhotoMap(Set.of(queryResult.getEntityId())))
			.willReturn(Map.of(queryResult.getEntityId(), contactDetails));

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isZero();
		assertThat(result.getApprovedBy()).isEqualTo(contactDetails.getName());
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle agency recruiter user type")
	void testGetTimeLogsByTimesheetIdAgencyRecruiterUserTypeReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForAgencyRecruiter();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		UserDetailsQueryResultDto userDetails = TimesheetLogsTestDataFactory.createUserDetailsQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId())).willReturn(userDetails);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovedBy()).isEqualTo(userDetails.getName());
		then(this.userRepository).should().getUserDetails(queryResult.getEntityId());
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle company contact user type")
	void testGetTimeLogsByTimesheetIdCompanyContactUserTypeReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		ContactNamePhotoQueryResultDto contactDetails = TimesheetLogsTestDataFactory
			.createContactNamePhotoQueryResult();
		Map<Integer, ContactNamePhotoQueryResultDto> contactMap = Map.of(queryResult.getEntityId(), contactDetails);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.contactRepository.getContactNamePhotoMap(Set.of(queryResult.getEntityId()))).willReturn(contactMap);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovedBy()).isEqualTo(contactDetails.getName());
		then(this.contactRepository).should().getContactNamePhotoMap(Set.of(queryResult.getEntityId()));
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle company contact when contact not found")
	void testGetTimeLogsByTimesheetIdCompanyContactNotFoundReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		Map<Integer, ContactNamePhotoQueryResultDto> contactMap = new HashMap<>();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.contactRepository.getContactNamePhotoMap(Set.of(queryResult.getEntityId()))).willReturn(contactMap);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovedBy()).isNull();
		then(this.contactRepository).should().getContactNamePhotoMap(Set.of(queryResult.getEntityId()));
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should set approver name for contractor user type with approved status")
	void testGetTimeLogsByTimesheetIdContractorUserTypeApprovedStatusSetsApproverName() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForContractor();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		ContractorNamePhotoQueryResultDto contractorDetails = TimesheetLogsTestDataFactory
			.createContractorNamePhotoQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.candidateRepository.getContractorQueryResultMap(Set.of(queryResult.getEntityId())))
			.willReturn(Map.of(queryResult.getEntityId(), contractorDetails));

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovedBy()).isEqualTo(contractorDetails.getName());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		then(this.candidateRepository).should().getContractorQueryResultMap(Set.of(queryResult.getEntityId()));
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should set approver name for contractor user type with rejected status")
	void testGetTimeLogsByTimesheetIdContractorUserTypeRejectedStatusSetsApproverName() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForContractor();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.REJECTED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithRejectedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		ContractorNamePhotoQueryResultDto contractorDetails = TimesheetLogsTestDataFactory
			.createContractorNamePhotoQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.candidateRepository.getContractorQueryResultMap(Set.of(queryResult.getEntityId())))
			.willReturn(Map.of(queryResult.getEntityId(), contractorDetails));

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.REJECTED.getId());
		assertThat(result.getApprovedBy()).isEqualTo(contractorDetails.getName());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		assertThat(result.getRemark()).isEqualTo("Rejected for testing");
		then(this.candidateRepository).should().getContractorQueryResultMap(Set.of(queryResult.getEntityId()));
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should set approver name for contractor user type with submitted status")
	void testGetTimeLogsByTimesheetIdContractorUserTypeSubmittedStatusSetsApproverName() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForContractor();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		ContractorNamePhotoQueryResultDto contractorDetails = TimesheetLogsTestDataFactory
			.createContractorNamePhotoQueryResult();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.candidateRepository.getContractorQueryResultMap(Set.of(queryResult.getEntityId())))
			.willReturn(Map.of(queryResult.getEntityId(), contractorDetails));
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId))).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.SUBMITTED.getId());
		assertThat(result.getApprovedBy()).isEqualTo(contractorDetails.getName());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		then(this.candidateRepository).should().getContractorQueryResultMap(Set.of(queryResult.getEntityId()));
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle contractor not found gracefully")
	void testGetTimeLogsByTimesheetIdContractorUserTypeNotFoundHandlesGracefully() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForContractor();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorMap = new HashMap<>();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.candidateRepository.getContractorQueryResultMap(Set.of(queryResult.getEntityId())))
			.willReturn(contractorMap);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovedBy()).isNull();
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		then(this.candidateRepository).should().getContractorQueryResultMap(Set.of(queryResult.getEntityId()));
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should not set approver name for other user types")
	void testGetTimeLogsByTimesheetIdOtherUserTypeDoesNotSetApproverName() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setUserTypeId(5); // Some other user type
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovedBy()).isNull();
		then(this.userRepository).should(never()).getUserDetails(anyInt());
		then(this.contactRepository).should(never()).getContactNamePhotoMap(anySet());
	}
	// ===== Tests for bulkUpdateTimeLogs =====

	@Test
	@DisplayName("Bulk update with empty timeLogs and timesheetIdNoLogChanges returns early without changes")
	void testBulkUpdateTimeLogsWithEmptyPayloadReturnsEarly() {
		// Given: No time log changes and no metadata-only timesheets
		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setTimeLogs(Collections.emptyList());
		request.setTimesheetIdNoLogChanges(Collections.emptyList());
		request.setIsApproved(false);

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then: No exception, no downstream calls (auth, access control, repositories)
		then(this.auth).shouldHaveNoInteractions();
		then(this.contractStaffingAccessControlChecker).shouldHaveNoInteractions();
		then(this.timeLogRepository).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Bulk update with null timeLogs and null timesheetIdNoLogChanges returns early without changes")
	void testBulkUpdateTimeLogsWithNullPayloadReturnsEarly() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setTimeLogs(null);
		request.setTimesheetIdNoLogChanges(null);
		request.setIsApproved(false);

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then: No exception, no downstream calls
		then(this.auth).shouldHaveNoInteractions();
		then(this.contractStaffingAccessControlChecker).shouldHaveNoInteractions();
		then(this.timeLogRepository).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Bulk update with timesheetIdNoLogChanges updates metadata (status history, last modified) only")
	void testBulkUpdateTimeLogsWithTimesheetIdNoLogChangesUpdatesMetadataOnly() {
		// Given: Submit without changing any log - timesheetIdNoLogChanges carries the ID
		Integer timesheetId = 789;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setTimeLogs(Collections.emptyList());
		request.setTimesheetIdNoLogChanges(Collections.singletonList(timesheetId));
		request.setIsApproved(false);
		request.setSave(false); // Submit

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		TimesheetApproval latestApproval = new TimesheetApproval();
		latestApproval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.OPEN.getId());
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(latestApproval);

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then: Status history and last modified updated, no time log updates
		then(this.timesheetApprovalRepository).should()
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		then(this.timesheetUpdateHelper).should()
			.updateTimesheetLastModified(timesheetId, userId, AccountUserEnum.USERTYPEID.getId());
		then(this.timeLogRepository).shouldHaveNoInteractions();
		then(this.webhookKafkaEventService).should(never())
			.triggerTimesheetWebhookEvent(any(WebhookEvent.class), anyList());
	}

	@Test
	@DisplayName("Metadata-only bulk update should trigger timesheet approved webhook when isApproved is true")
	void testBulkUpdateTimeLogsMetadataOnlyIsApprovedTrueTriggersTimesheetApprovedWebhookEvent() {
		Integer timesheetId = 789;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setTimeLogs(Collections.emptyList());
		request.setTimesheetIdNoLogChanges(Collections.singletonList(timesheetId));
		request.setIsApproved(true);
		request.setSave(false);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Approver User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		timesheet.setId(timesheetId);
		given(this.timesheetJpaRepository.findById(timesheetId)).willReturn(Optional.of(timesheet));

		TimesheetApprover approver = TimesheetLogsTestDataFactory.createTimesheetApprover();
		approver.setEntityId(userId);
		approver.setUserTypeId(AccountUserEnum.USERTYPEID.getId());
		given(this.timesheetApproverRepository.findByTimesheetSettingId(timesheet.getTimesheetSettingId()))
			.willReturn(List.of(approver));

		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.APPROVED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId, userId, AccountUserEnum.USERTYPEID.getId());
		willDoNothing().given(this.timesheetInvoiceService)
			.createTimesheetInvoice(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()));

		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.webhookKafkaEventService).should()
			.triggerTimesheetWebhookEvent(WebhookEvent.TIMESHEET_APPROVED, List.of(timesheetId));
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Bulk update time logs for USER persona should update successfully")
	void testBulkUpdateTimeLogsForUserPersonaValidRequestUpdatesSuccessfully() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(userPrincipal.getFullName()).willReturn("Test User");
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(0).getId(), timesheetId))
			.willReturn(Optional.of(timeLog));
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId, userId, AccountUserEnum.USERTYPEID.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - Service uses batch operations for bulk updates
		then(this.auth).should().getUnifiedPrincipal();
		then(this.contractStaffingAccessControlChecker).should().allowsBulk(any(BulkPermissionCheckRequest.class));
		// Service uses batch methods: findByIdInAndAccountId for timesheets and settings
		then(this.timesheetJpaRepository).should().findByIdInAndAccountId(anyList(), eq(accountId));
		then(this.timesheetSettingJpaRepository).should().findByIdInAndAccountId(anyList(), eq(accountId));
		// Service uses findAllById for time logs
		then(this.timeLogJpaRepository).should().findAllById(anyIterable());
		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Bulk update time logs should trigger timesheet approved webhook when isApproved is true")
	void testBulkUpdateTimeLogsIsApprovedTrueTriggersTimesheetApprovedWebhookEvent() {
		// Given — USER persona, default bulk request (isApproved=true, two time logs on
		// timesheets 1 and 2)
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		Integer timesheetId1 = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer timesheetId2 = 2;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		Timesheet timesheet1 = TimesheetLogsTestDataFactory.createTimesheet();
		Timesheet timesheet2 = TimesheetLogsTestDataFactory.createTimesheet();
		timesheet2.setId(timesheetId2);
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		TimeLog timeLog1 = TimesheetLogsTestDataFactory.createTimeLog();
		TimeLog timeLog2 = TimesheetLogsTestDataFactory.createTimeLog();
		timeLog2.setId(2);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId1)).willReturn(null);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId2)).willReturn(null);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId1, accountId))
			.willReturn(Optional.of(timesheet1));
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId2, accountId))
			.willReturn(Optional.of(timesheet2));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(0).getId(), timesheetId1))
			.willReturn(Optional.of(timeLog1));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(1).getId(), timesheetId2))
			.willReturn(Optional.of(timeLog2));
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId1), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId2), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId1, userId, AccountUserEnum.USERTYPEID.getId());
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId2, userId, AccountUserEnum.USERTYPEID.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then — performBulkTimeLogUpdate invokes webhook when isApproved is true
		then(this.webhookKafkaEventService).should()
			.triggerTimesheetWebhookEvent(WebhookEvent.TIMESHEET_APPROVED, List.of(timesheetId1, timesheetId2));
	}

	@Test
	@DisplayName("Bulk update time logs for CONTRACTOR persona should update successfully and set isApproved to false")
	void testBulkUpdateTimeLogsForContractorPersonaValidRequestUpdatesSuccessfully() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setIsApproved(true); // Contractors cannot approve, will be overridden
		// Note: createBulkUpdateTimeLogsRequest creates 2 time logs with timesheet IDs 1
		// and 2
		Integer timesheetId1 = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer timesheetId2 = 2;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 200;
		Timesheet timesheet1 = TimesheetLogsTestDataFactory.createTimesheet();
		Timesheet timesheet2 = TimesheetLogsTestDataFactory.createTimesheet();
		timesheet2.setId(timesheetId2);
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		TimeLog timeLog1 = TimesheetLogsTestDataFactory.createTimeLog();
		TimeLog timeLog2 = TimesheetLogsTestDataFactory.createTimeLog();
		timeLog2.setId(2);

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId1, accountId)).willReturn(candidate);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId2, accountId)).willReturn(candidate);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId1)).willReturn(null);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId2)).willReturn(null);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId1, accountId))
			.willReturn(Optional.of(timesheet1));
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId2, accountId))
			.willReturn(Optional.of(timesheet2));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(0).getId(), timesheetId1))
			.willReturn(Optional.of(timeLog1));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(1).getId(), timesheetId2))
			.willReturn(Optional.of(timeLog2));
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId1), eq(contractorId), eq(UserTypeEnum.CONTRACTOR.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId2), eq(contractorId), eq(UserTypeEnum.CONTRACTOR.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId1, contractorId, UserTypeEnum.CONTRACTOR.getId());
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId2, contractorId, UserTypeEnum.CONTRACTOR.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - Service uses batch operations
		assertThat(request.getIsApproved()).isFalse(); // Verify it was overridden to
														// false
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetJpaRepository).should().findByIdInAndAccountId(anyList(), eq(accountId));
		then(this.timeLogRepository).should().batchUpsert(anyList());
		// Service now uses batch approvals
		then(this.timesheetApprovalRepository).should().createBulkTimesheetApprovals(anyList());
	}

	@Test
	@DisplayName("Bulk update time logs for CONTACT persona should throw UnauthorizedAccessException when canEdit is null")
	void testBulkUpdateTimeLogsForContactPersonaNullCanEditThrowsUnauthorizedAccessException() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setJobId(100);
		Integer clientId = 200;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanEdit(null);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(request.getJobId(), clientId))
			.willReturn(permissions);

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Unauthorized access for edit timesheet");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(request.getJobId(), clientId);
		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Bulk update time logs for CONTACT persona should throw UnauthorizedAccessException when canEdit is not 1")
	void testBulkUpdateTimeLogsForContactPersonaCanEditNotOneThrowsUnauthorizedAccessException() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setJobId(100);
		Integer clientId = 200;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanEdit(0);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(request.getJobId(), clientId))
			.willReturn(permissions);

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Unauthorized access for edit timesheet");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(request.getJobId(), clientId);
		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Bulk update time logs for CONTACT persona should update successfully when canEdit is allowed")
	void testBulkUpdateTimeLogsForContactPersonaValidRequestUpdatesSuccessfully() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setJobId(100);
		request.setIsApproved(false);
		Integer clientId = 200;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto();
		permissions.setCanEdit(1);

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(contactPrincipal.getFullName()).willReturn("Contact User");
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(request.getJobId(), clientId))
			.willReturn(permissions);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId)).willReturn(null);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(2)).willReturn(null);
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(clientId), eq(UserTypeEnum.COMPANY_CONTACT.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(2), eq(clientId), eq(UserTypeEnum.COMPANY_CONTACT.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId, clientId, UserTypeEnum.COMPANY_CONTACT.getId());
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(2, clientId, UserTypeEnum.COMPANY_CONTACT.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then
		then(this.portalAccessControlService).should().validatePortalAccessControl(request.getJobId(), clientId);
		then(this.timesheetJpaRepository).should().findByIdInAndAccountId(anyList(), eq(accountId));
		then(this.timeLogRepository).should().batchUpsert(anyList());
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("Bulk update time logs should throw NullPointerException when principal type is null")
	void testBulkUpdateTimeLogsUnknownPersonaTypeThrowsUnauthorizedAccessException() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();

		AuthPrincipal unknownPrincipal = mock(AuthPrincipal.class);
		given(unknownPrincipal.getPrincipalType()).willReturn(null);
		given(this.auth.getUnifiedPrincipal()).willReturn(unknownPrincipal);

		// When & Then
		// When getPrincipalType() returns null, the switch statement throws
		// NullPointerException
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(NullPointerException.class);

		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Bulk update time logs for CONTACT persona should propagate ResourceNotFoundException from portal access control when job not found")
	void testBulkUpdateTimeLogsForContactPersonaJobNotFoundThrowsResourceNotFoundException() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setJobId(100);
		Integer clientId = 200;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(request.getJobId(), clientId))
			.willThrow(new ResourceNotFoundException("Job", request.getJobId()));

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Job");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(request.getJobId(), clientId);
		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Bulk update time logs for CONTACT persona should propagate ValidationErrorException from portal access control when portal not enabled")
	void testBulkUpdateTimeLogsForContactPersonaPortalNotEnabledThrowsValidationErrorException() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setJobId(100);
		Integer clientId = 200;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(request.getJobId(), clientId))
			.willThrow(new ValidationErrorException("Portal is not enabled for this job"));

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Portal is not enabled for this job");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(request.getJobId(), clientId);
		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Bulk update time logs for CONTACT persona should propagate UnauthorizedAccessException from portal access control when client ID mismatch")
	void testBulkUpdateTimeLogsForContactPersonaClientIdMismatchThrowsUnauthorizedAccessException() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setJobId(100);
		Integer clientId = 200;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.portalAccessControlService.validatePortalAccessControl(request.getJobId(), clientId))
			.willThrow(new UnauthorizedAccessException("Client ID does not match job contact"));

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Client ID does not match job contact");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.portalAccessControlService).should().validatePortalAccessControl(request.getJobId(), clientId);
		then(this.timesheetJpaRepository).should(never()).findByIdAndAccountId(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Bulk update time logs should extract unique timesheet IDs correctly with duplicates")
	void testBulkUpdateTimeLogsExtractsUniqueTimesheetIdsWithDuplicates() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();

		// Create multiple time logs for the same timesheet (IDs must not match global
		// findAllById stub that maps id 2 -> timesheet 2)
		BulkTimeLogRequestBodyDto timeLog1 = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		timeLog1.setId(101);
		timeLog1.setTimesheetId(timesheetId);
		BulkTimeLogRequestBodyDto timeLog2 = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		timeLog2.setId(102);
		timeLog2.setTimesheetId(timesheetId);
		BulkTimeLogRequestBodyDto timeLog3 = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		timeLog3.setId(103);
		timeLog3.setTimesheetId(timesheetId);
		request.setTimeLogs(Arrays.asList(timeLog1, timeLog2, timeLog3));

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId)).willReturn(null);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(anyInt(), eq(timesheetId)))
			.willReturn(Optional.of(timeLog));
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		willDoNothing().given(this.timesheetUpdateHelper)
			.batchUpdateTimesheetLastModifiedWithTimeDetails(anyList(), eq(userId),
					eq(AccountUserEnum.USERTYPEID.getId()), anyList());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - should only process timesheet once despite 3 time logs
		then(this.timesheetJpaRepository).should(times(1))
			.findByIdInAndAccountId(eq(List.of(timesheetId)), eq(accountId));
		then(this.timeLogRepository).should(times(1)).batchUpsert(anyList());
		then(this.timesheetApprovalRepository).should(times(1)).createBulkTimesheetApprovals(anyList());
		then(this.timesheetUpdateHelper).should(times(1))
			.batchUpdateTimesheetLastModifiedWithTimeDetails(eq(List.of(timesheetId)), eq(userId),
					eq(AccountUserEnum.USERTYPEID.getId()), anyList());
	}

	@Test
	@DisplayName("Bulk update time logs should invoke rule engine for all timesheets")
	void testBulkUpdateTimeLogsInvokesRuleEngineForAllTimesheets() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		Integer timesheetId1 = 1;
		Integer timesheetId2 = 2;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();

		BulkTimeLogRequestBodyDto timeLog1 = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		timeLog1.setTimesheetId(timesheetId1);
		BulkTimeLogRequestBodyDto timeLog2 = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		timeLog2.setId(2);
		timeLog2.setTimesheetId(timesheetId2);
		request.setTimeLogs(Arrays.asList(timeLog1, timeLog2));

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		// Mock approval lookups for validateTimesheetsNotApproved
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId1)).willReturn(null);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId2)).willReturn(null);
		given(this.timesheetJpaRepository.findByIdAndAccountId(anyInt(), eq(accountId)))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(anyInt(), anyInt())).willReturn(Optional.of(timeLog));
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(anyInt(), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(anyInt(), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()));

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - rule engine should be invoked for both timesheets
		then(this.ruleEngineService).should(times(2)).evaluateRules(any(RuleEngineRequestBodyDto.class));
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle non-agency recruiter user type")
	void testGetTimeLogsByTimesheetIdNonAgencyRecruiterUserTypeReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId()); // Non-agency
																			// recruiter
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovedBy()).isNull(); // Should not set approver name for
														// non-agency recruiters
		then(this.userRepository).should(never()).getUserDetails(anyInt());
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle rejected status")
	void testGetTimeLogsByTimesheetIdRejectedStatusReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithRejectedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.REJECTED.getId());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		assertThat(result.getRemark()).isEqualTo(approval.getRemark());
		assertThat(result.getCreatedOn()).isEqualTo(approval.getCreatedOn());
		assertThat(result.getPayCurrencySymbol()).isEqualTo("$");
		assertThat(result.getPayCurrencyCode()).isEqualTo("USD");
		assertThat(result.getBillCurrencySymbol()).isEqualTo("$");
		assertThat(result.getBillCurrencyCode()).isEqualTo("USD");
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle work time entry type for work summaries")
	void testGetTimeLogsByTimesheetIdWorkTimeEntryTypeReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setWorkLogType(workTimeEnum.ENTER_WORK_TIME.getId()); // Work time
																			// entry type
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId))).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetTotalPayBill()).isNotNull();
		then(this.timeLogJpaRepository).should().getTimeLogWorkSummaries(Arrays.asList(timesheetId));
		then(this.timeLogJpaRepository).should(never()).getTimeLogWorkDurationSummaries(anyList());
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should return breakTimeThreshold when calculateBreakTime is false")
	void testGetTimeLogsByTimesheetIdWithBreakTimeThresholdReturnsThresholdValue() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultWithBreakTimeThreshold();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getCalculateBreakTime()).isFalse();
		assertThat(result.getBreakTimeThreshold()).isEqualTo(30);
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get contractor all time logs should populate breakTimeThreshold in timelogsMetaData")
	void testGetContractorAllTimeLogsPopulatesBreakTimeThresholdInMetaData() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());

		// Create contractor validator DTO for the timesheet ID
		ContractorTimesheetAndSettingValidatorResponseBodyDto contractorValidatorDto = TimesheetLogsTestDataFactory
			.createContractorTimesheetAndSettingValidatorResponse();
		contractorValidatorDto.setTimesheetId(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorValidatorDtos = Arrays
			.asList(contractorValidatorDto);

		List<ContractorTimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		// Create time log with setting data that includes breakTimeThreshold
		TimelogWithSettingQueryResultDto timelogWithSetting = TimesheetLogsTestDataFactory
			.createTimelogWithSettingQueryResultWithBreakTimeThreshold();
		List<TimelogWithSettingQueryResultDto> timeLogsWithSettings = Arrays.asList(timelogWithSetting);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithSettingDetails(timesheetIds, accountId))
			.willReturn(timeLogsWithSettings);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(timelogWithSetting.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));

		// When
		FetchBulkContractorTimelogResultBodyDto result = this.timesheetLogsService
			.getContractorAllTimeLogs(timesheetIds, contractorValidatorDtos, errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData()).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData().getTimelogsMetaData()).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData().getTimelogsMetaData()).hasSize(1);

		TimelogsMetaDataDto metaData = result.getTimesheetSettingsMetaData().getTimelogsMetaData().getFirst();
		assertThat(metaData.getTimesheetId()).isEqualTo(timesheetIds.getFirst());
		assertThat(metaData.getCalculateBreakTime()).isFalse();
		assertThat(metaData.getBreakTimeThreshold()).isEqualTo(30);

		then(this.timeLogRepository).should().findTimeLogsWithSettingDetails(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get all time logs should populate breakTimeThreshold in contractorsLogData")
	void testGetAllTimeLogsPopulatesBreakTimeThresholdInContractorsLogData() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());

		// Create validator DTO for the timesheet ID
		TimesheetAndSettingValidatorResponseBodyDto validatorDto = TimesheetLogsTestDataFactory
			.createTimesheetAndSettingValidatorResponse();
		validatorDto.setTimesheetId(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays.asList(validatorDto);

		List<TimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		// Create time log query results
		TimelogQueryResultDto timelogQueryResult = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(timelogQueryResult);

		// Create timesheet settings data including breakTimeThreshold
		Object[] settingData = new Object[] { timesheetIds.getFirst(), // timesheetId
				false, // calculateBreakTime
				30, // breakTimeThreshold
				Arrays.asList(TimesheetLogsTestDataFactory.createTemplateWorkDay()), // templateWorkDays
				0 // isRemarkMandatory
		};
		List<Object[]> timesheetSettingsData = Arrays.<Object[]>asList(settingData);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorDetailsMap = new HashMap<>();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(timesheetIds, accountId)).willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(timesheetIds, accountId))
			.willReturn(timesheetSettingsData);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(contractorDetailsMap);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getContractorsLogData()).isNotNull();
		assertThat(result.getContractorsLogData()).hasSize(1);

		DayTimelogQueryResultDto contractorData = result.getContractorsLogData().getFirst();
		assertThat(contractorData.getCalculateBreakTime()).isFalse();
		assertThat(contractorData.getBreakTimeThreshold()).isEqualTo(30);

		then(this.timeLogRepository).should().findTimesheetSettingsForTimesheets(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get all time logs should populate jobName and jobSlug in contractorsLogData")
	void testGetAllTimeLogsPopulatesJobNameAndSlugInContractorsLogData() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		List<Integer> timesheetIds = Arrays.asList(timesheetId);

		Integer contractorId = 100;
		TimesheetAndSettingValidatorResponseBodyDto validatorDto = TimesheetLogsTestDataFactory
			.createTimesheetAndSettingValidatorResponse();
		validatorDto.setTimesheetId(timesheetId);
		validatorDto.setContractorId(contractorId);
		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays.asList(validatorDto);

		List<TimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogQueryResultDto timelogQueryResult = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(timelogQueryResult);

		Object[] settingData = new Object[] { timesheetId, false, 30,
				Arrays.asList(TimesheetLogsTestDataFactory.createTemplateWorkDay()), 0 };
		List<Object[]> timesheetSettingsData = Arrays.<Object[]>asList(settingData);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		ContractorNamePhotoQueryResultDto contractorWithSlug = new ContractorNamePhotoQueryResultDto("John Doe",
				"photo.jpg", "john-contractor");
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorDetailsMap = new HashMap<>();
		contractorDetailsMap.put(contractorId, contractorWithSlug);
		Integer jobId = 1;
		TimesheetJobQueryResultDto companyResult = new TimesheetJobQueryResultDto(timesheetId, jobId,
				"Software Engineer", "software-engineer", 500);
		Map<Integer, TimesheetJobQueryResultDto> companyDetailsMap = new HashMap<>();
		companyDetailsMap.put(timesheetId, companyResult);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(timesheetIds, accountId)).willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(timesheetIds, accountId))
			.willReturn(timesheetSettingsData);
		given(this.timeLogRepository.findCompanyByTimesheetIds(timesheetIds, accountId)).willReturn(companyDetailsMap);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(contractorDetailsMap);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getContractorsLogData()).isNotNull().hasSize(1);
		DayTimelogQueryResultDto contractorData = result.getContractorsLogData().getFirst();
		assertThat(contractorData.getJobName()).isEqualTo("Software Engineer");
		assertThat(contractorData.getJobSlug()).isEqualTo("software-engineer");
		assertThat(contractorData.getAssignmentId()).isEqualTo(500);
		assertThat(contractorData.getContractorSlug()).isEqualTo("john-contractor");
		then(this.timeLogRepository).should().findCompanyByTimesheetIds(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle empty work summary results")
	void testGetTimeLogsByTimesheetIdEmptyWorkSummaryResultsReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		// Default workLogType is ENTER_WORK_TIME, so getTimeLogWorkSummaries will be
		// called
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId)))
			.willReturn(Arrays.asList()); // Empty results

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetTotalPayBill()).isNull(); // Should be null when no
																// work summary
		then(this.timeLogJpaRepository).should().getTimeLogWorkSummaries(Arrays.asList(timesheetId));
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle null custom rules")
	void testGetTimeLogsByTimesheetIdNullCustomRulesReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setCustomRules(null); // Null custom rules
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummary);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId))).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isFalse();
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle empty custom rules list")
	void testGetTimeLogsByTimesheetIdEmptyCustomRulesReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setCustomRules(Arrays.asList()); // Empty custom rules list
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummary);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId))).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isFalse();
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle non-weekly overtime custom rules")
	void testGetTimeLogsByTimesheetIdNonWeeklyOvertimeCustomRulesReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();

		// Create custom rule that is NOT weekly overtime
		CustomRule nonWeeklyRule = TimesheetLogsTestDataFactory.createCustomRule();
		nonWeeklyRule.setRuleType(4); // RANGE_BASED_DAILY_OVERTIME - Not a weekly
										// overtime rule
		queryResult.setCustomRules(Arrays.asList(nonWeeklyRule));
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummary);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId))).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isFalse(); // Should be false since rule
															// is not weekly overtime
	}

	@Test
	@DisplayName("Get time logs by timesheet ID with OPEN status should still populate isWeeklyEnabled")
	void testGetTimeLogsByTimesheetIdOpenStatusSetsWeeklyEnabled() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.OPEN.getId());

		CustomRule weeklyRule = new CustomRule();
		weeklyRule.setId(1);
		weeklyRule.setRuleType(5); // RANGE_BASED_WEEKLY_OVERTIME
		queryResult.setCustomRules(Arrays.asList(weeklyRule));

		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithOpenStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then — isWeeklyEnabled must be populated even for OPEN timesheets
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isTrue();
	}

	@Test
	@DisplayName("Bulk update time logs should throw IllegalArgumentException when timesheet ID mismatch")
	void testBulkUpdateTimeLogsTimesheetIdMismatchThrowsIllegalArgumentException() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.getTimeLogs().get(0).setTimesheetId(999);
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Timesheet ID mismatch for time log ID: 1");

		then(this.contractStaffingAccessControlChecker).should().allowsBulk(any(BulkPermissionCheckRequest.class));
	}

	@Test
	@DisplayName("Bulk update time logs should handle null break intervals")
	void testBulkUpdateTimeLogsNullBreakIntervalsHandlesCorrectly() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		// Null break intervals - set on workTimeDetails if they exist
		if (request.getTimeLogs().get(0).getWorkTimeDetails() != null
				&& !request.getTimeLogs().get(0).getWorkTimeDetails().isEmpty()) {
			request.getTimeLogs().get(0).getWorkTimeDetails().get(0).setBreakIntervals(null);
		}
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForWorkTimeEntry();
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(0).getId(), timesheetId))
			.willReturn(Optional.of(timeLog));
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId, userId, AccountUserEnum.USERTYPEID.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - Service uses timeLogIntervalRepository for intervals now, not
		// timeLogBreakIntervalRepository
		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Bulk update time logs should handle empty break intervals list")
	void testBulkUpdateTimeLogsEmptyBreakIntervalsHandlesCorrectly() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		// Empty break intervals list - set on workTimeDetails
		if (request.getTimeLogs().get(0).getWorkTimeDetails() != null
				&& !request.getTimeLogs().get(0).getWorkTimeDetails().isEmpty()) {
			request.getTimeLogs().get(0).getWorkTimeDetails().get(0).setBreakIntervals(Arrays.asList());
		}
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForWorkTimeEntry();
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(0).getId(), timesheetId))
			.willReturn(Optional.of(timeLog));
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId, userId, AccountUserEnum.USERTYPEID.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - Service uses batch operations and timeLogIntervalRepository
		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Bulk update time logs should handle new break intervals with no existing ID")
	void testBulkUpdateTimeLogsNewBreakIntervalsCreatesNewEntries() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		// Set break intervals without IDs to test creation of new break intervals
		if (request.getTimeLogs().get(0).getWorkTimeDetails() != null
				&& !request.getTimeLogs().get(0).getWorkTimeDetails().isEmpty()
				&& request.getTimeLogs().get(0).getWorkTimeDetails().get(0).getBreakIntervals() != null
				&& !request.getTimeLogs().get(0).getWorkTimeDetails().get(0).getBreakIntervals().isEmpty()) {
			request.getTimeLogs().get(0).getWorkTimeDetails().get(0).getBreakIntervals().get(0).setId(null); // No
			// existing
			// ID
		}
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(0).getId(), timesheetId))
			.willReturn(Optional.of(timeLog));
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(Arrays.asList(timeLog.getId())))
			.willReturn(Arrays.asList()); // No existing break intervals
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		given(this.timeLogBreakIntervalRepository.saveBreakIntervals(anyList()))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogBreakIntervalList());
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId, userId, AccountUserEnum.USERTYPEID.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - Service uses batch interval operations via timeLogIntervalRepository
		then(this.timeLogIntervalRepository).should().batchUpsert(anyList());
		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle null period dates in formatTimesheetPeriod")
	void testGetTimeLogsByTimesheetIdNullPeriodDatesReturnsEmptyString() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		timesheet.setPeriodStart(null); // Null start date
		timesheet.setPeriodEnd(null); // Null end date
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimeLogs()).isNotEmpty();
		// Verify that timesheet period is empty string when dates are null
		assertThat(result.getTimeLogs().get(0).getTimesheetPeriod()).isEmpty();
	}

	@Test
	@DisplayName("Get all time logs should return success response")
	void testGetAllTimeLogsValidRequestReturnsSuccess() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays
			.asList(TimesheetLogsTestDataFactory.createTimesheetAndSettingValidatorResponse());
		List<TimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogQueryResultDto timelogQueryResult = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(timelogQueryResult);

		Object[] settingData = new Object[] { timesheetIds.get(0), // timesheetId
				false, // calculateBreakTime
				30, // breakTimeThreshold
				Arrays.asList(TimesheetLogsTestDataFactory.createTemplateWorkDay()), // templateWorkDays
				0 // isRemarkMandatory
		};
		List<Object[]> timesheetSettingsData = Arrays.<Object[]>asList(settingData);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorDetailsMap = new HashMap<>();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(timesheetIds, accountId)).willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(timesheetIds, accountId))
			.willReturn(timesheetSettingsData);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(contractorDetailsMap);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getContractorsLogData()).isNotNull();
		then(this.timeLogRepository).should().findTimeLogsWithDetails(timesheetIds, accountId);
		then(this.timeLogRepository).should().findTimesheetSettingsForTimesheets(timesheetIds, accountId);
		then(this.candidateRepository).should().getContractorQueryResultMap(anySet());
	}

	@Test
	@DisplayName("Get all time logs should return isRemarkMandatory true")
	void testGetAllTimeLogsWithIsRemarkMandatoryTrueReturnsSuccess() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays
			.asList(TimesheetLogsTestDataFactory.createTimesheetAndSettingValidatorResponse());
		List<TimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogQueryResultDto timelogQueryResult = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(timelogQueryResult);

		Object[] settingData = new Object[] { timesheetIds.get(0), // timesheetId
				true, // calculateBreakTime
				30, // breakTimeThreshold
				Arrays.asList(TimesheetLogsTestDataFactory.createTemplateWorkDay()), // templateWorkDays
				1 // isRemarkMandatory
		};
		List<Object[]> timesheetSettingsData = Arrays.<Object[]>asList(settingData);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorDetailsMap = new HashMap<>();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(timesheetIds, accountId)).willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(timesheetIds, accountId))
			.willReturn(timesheetSettingsData);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(contractorDetailsMap);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getContractorsLogData()).isNotNull().hasSizeGreaterThan(0);
		assertThat(result.getContractorsLogData().get(0).getIsRemarkMandatory()).isEqualTo(1);
		then(this.timeLogRepository).should().findTimeLogsWithDetails(timesheetIds, accountId);
		then(this.timeLogRepository).should().findTimesheetSettingsForTimesheets(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get all time logs should return isRemarkMandatory false")
	void testGetAllTimeLogsWithIsRemarkMandatoryFalseReturnsSuccess() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays
			.asList(TimesheetLogsTestDataFactory.createTimesheetAndSettingValidatorResponse());
		List<TimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogQueryResultDto timelogQueryResult = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(timelogQueryResult);

		Object[] settingData = new Object[] { timesheetIds.get(0), // timesheetId
				false, // calculateBreakTime
				30, // breakTimeThreshold
				Arrays.asList(TimesheetLogsTestDataFactory.createTemplateWorkDay()), // templateWorkDays
				0 // isRemarkMandatory
		};
		List<Object[]> timesheetSettingsData = Arrays.<Object[]>asList(settingData);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorDetailsMap = new HashMap<>();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(timesheetIds, accountId)).willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(timesheetIds, accountId))
			.willReturn(timesheetSettingsData);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(contractorDetailsMap);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getContractorsLogData()).isNotNull().hasSizeGreaterThan(0);
		assertThat(result.getContractorsLogData().get(0).getIsRemarkMandatory()).isZero();
		then(this.timeLogRepository).should().findTimeLogsWithDetails(timesheetIds, accountId);
		then(this.timeLogRepository).should().findTimesheetSettingsForTimesheets(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get all time logs should return isRemarkMandatory null")
	void testGetAllTimeLogsWithIsRemarkMandatoryNullReturnsSuccess() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays
			.asList(TimesheetLogsTestDataFactory.createTimesheetAndSettingValidatorResponse());
		List<TimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogQueryResultDto timelogQueryResult = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(timelogQueryResult);

		Object[] settingData = new Object[] { timesheetIds.get(0), // timesheetId
				true, // calculateBreakTime
				30, // breakTimeThreshold
				Arrays.asList(TimesheetLogsTestDataFactory.createTemplateWorkDay()), // templateWorkDays
				null // isRemarkMandatory
		};
		List<Object[]> timesheetSettingsData = Arrays.<Object[]>asList(settingData);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorDetailsMap = new HashMap<>();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(timesheetIds, accountId)).willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(timesheetIds, accountId))
			.willReturn(timesheetSettingsData);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(contractorDetailsMap);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getContractorsLogData()).isNotNull().hasSizeGreaterThan(0);
		assertThat(result.getContractorsLogData().get(0).getIsRemarkMandatory()).isNull();
		then(this.timeLogRepository).should().findTimeLogsWithDetails(timesheetIds, accountId);
		then(this.timeLogRepository).should().findTimesheetSettingsForTimesheets(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get all time logs should return different isRemarkMandatory values for multiple contractors")
	void testGetAllTimeLogsWithMultipleContractorsDifferentIsRemarkMandatoryReturnsSuccess() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(1, 2);
		TimesheetAndSettingValidatorResponseBodyDto validator1 = TimesheetLogsTestDataFactory
			.createTimesheetAndSettingValidatorResponse();
		validator1.setTimesheetId(1);
		validator1.setContractorId(100);
		TimesheetAndSettingValidatorResponseBodyDto validator2 = TimesheetLogsTestDataFactory
			.createTimesheetAndSettingValidatorResponse();
		validator2.setTimesheetId(2);
		validator2.setContractorId(200);
		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays.asList(validator1, validator2);
		List<TimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogQueryResultDto timelog1 = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		timelog1.setTimesheetId(1);
		TimelogQueryResultDto timelog2 = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		timelog2.setTimesheetId(2);
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(timelog1, timelog2);

		Object[] settingData1 = new Object[] { 1, // timesheetId
				true, // calculateBreakTime
				30, // breakTimeThreshold
				Arrays.asList(TimesheetLogsTestDataFactory.createTemplateWorkDay()), // templateWorkDays
				1 // isRemarkMandatory
		};
		Object[] settingData2 = new Object[] { 2, // timesheetId
				false, // calculateBreakTime
				60, // breakTimeThreshold
				Arrays.asList(TimesheetLogsTestDataFactory.createTemplateWorkDay()), // templateWorkDays
				0 // isRemarkMandatory
		};
		List<Object[]> timesheetSettingsData = Arrays.<Object[]>asList(settingData1, settingData2);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();
		ContractorNamePhotoQueryResultDto contractor1 = TimesheetLogsTestDataFactory
			.createContractorNamePhotoQueryResult();
		ContractorNamePhotoQueryResultDto contractor2 = TimesheetLogsTestDataFactory
			.createContractorNamePhotoQueryResult();
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorDetailsMap = Map.of(100, contractor1, 200,
				contractor2);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(timesheetIds, accountId)).willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(timesheetIds, accountId))
			.willReturn(timesheetSettingsData);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(contractorDetailsMap);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		this.givenTimeLogMapperReturnsOneResponsePerInputTimelog();
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getContractorsLogData()).isNotNull().hasSize(2);
		assertThat(result.getContractorsLogData().get(0).getIsRemarkMandatory()).isEqualTo(1);
		assertThat(result.getContractorsLogData().get(1).getIsRemarkMandatory()).isZero();
		then(this.timeLogRepository).should().findTimeLogsWithDetails(timesheetIds, accountId);
		then(this.timeLogRepository).should().findTimesheetSettingsForTimesheets(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get all time logs should handle timesheets with errors")
	void testGetAllTimeLogsHandlesTimesheetsWithErrors() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(1, 2, 3);

		// Create validator DTOs for all timesheet IDs
		TimesheetAndSettingValidatorResponseBodyDto validator1 = TimesheetLogsTestDataFactory
			.createTimesheetAndSettingValidatorResponse();
		validator1.setTimesheetId(1);

		TimesheetAndSettingValidatorResponseBodyDto validator2 = TimesheetLogsTestDataFactory
			.createTimesheetAndSettingValidatorResponse();
		validator2.setTimesheetId(2);

		TimesheetAndSettingValidatorResponseBodyDto validator3 = TimesheetLogsTestDataFactory
			.createTimesheetAndSettingValidatorResponse();
		validator3.setTimesheetId(3);

		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays.asList(validator1, validator2,
				validator3);
		// Create error data for timesheet ID 2
		TimesheetSettingErrorResponseBodyDto errorDto = new TimesheetSettingErrorResponseBodyDto();
		errorDto.setTimesheetId(2);
		List<TimesheetSettingErrorResponseBodyDto> errorData = Arrays.asList(errorDto);

		// Only timesheet IDs 1 and 3 should be queried (2 has errors)
		List<Integer> expectedValidTimesheetIds = Arrays.asList(1, 3);
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(TimesheetLogsTestDataFactory.createTimelogQueryResult());
		List<Object[]> timesheetSettingsData = Arrays.asList(new Object[] { 1, false, 30, Collections.emptyList(), 0 },
				new Object[] { 3, false, 30, Collections.emptyList(), 0 });
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(expectedValidTimesheetIds, accountId))
			.willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(expectedValidTimesheetIds, accountId))
			.willReturn(timesheetSettingsData);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(new HashMap<>());
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		// Then
		assertThat(result).isNotNull();
		then(this.timeLogRepository).should().findTimeLogsWithDetails(expectedValidTimesheetIds, accountId);
		then(this.timeLogRepository).should().findTimesheetSettingsForTimesheets(expectedValidTimesheetIds, accountId);
	}

	@Test
	@DisplayName("Get contractor all time logs should return success response")
	void testGetContractorAllTimeLogsValidRequestReturnsSuccess() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorValidatorDtos = Arrays
			.asList(TimesheetLogsTestDataFactory.createContractorTimesheetAndSettingValidatorResponse());
		List<ContractorTimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogWithSettingQueryResultDto timelogWithSetting = TimesheetLogsTestDataFactory
			.createTimelogWithSettingQueryResult();
		List<TimelogWithSettingQueryResultDto> timeLogsWithSettings = Arrays.asList(timelogWithSetting);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithSettingDetails(timesheetIds, accountId))
			.willReturn(timeLogsWithSettings);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(timelogWithSetting.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkContractorTimelogResultBodyDto result = this.timesheetLogsService
			.getContractorAllTimeLogs(timesheetIds, contractorValidatorDtos, errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimeLogs()).isNotNull();
		then(this.timeLogRepository).should().findTimeLogsWithSettingDetails(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get contractor all time logs should return isRemarkMandatory true")
	void testGetContractorAllTimeLogsWithIsRemarkMandatoryTrueReturnsSuccess() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorValidatorDtos = Arrays
			.asList(TimesheetLogsTestDataFactory.createContractorTimesheetAndSettingValidatorResponse());
		List<ContractorTimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogWithSettingQueryResultDto timelogWithSetting = TimesheetLogsTestDataFactory
			.createTimelogWithSettingQueryResult();
		timelogWithSetting.setIsRemarkMandatory(1);
		List<TimelogWithSettingQueryResultDto> timeLogsWithSettings = Arrays.asList(timelogWithSetting);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithSettingDetails(timesheetIds, accountId))
			.willReturn(timeLogsWithSettings);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(timelogWithSetting.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkContractorTimelogResultBodyDto result = this.timesheetLogsService
			.getContractorAllTimeLogs(timesheetIds, contractorValidatorDtos, errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimeLogs()).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData()).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData().getTimelogsMetaData()).isNotNull().hasSizeGreaterThan(0);
		assertThat(result.getTimesheetSettingsMetaData().getTimelogsMetaData().get(0).getIsRemarkMandatory())
			.isEqualTo(1);
		then(this.timeLogRepository).should().findTimeLogsWithSettingDetails(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get contractor all time logs should return isRemarkMandatory false")
	void testGetContractorAllTimeLogsWithIsRemarkMandatoryFalseReturnsSuccess() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorValidatorDtos = Arrays
			.asList(TimesheetLogsTestDataFactory.createContractorTimesheetAndSettingValidatorResponse());
		List<ContractorTimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogWithSettingQueryResultDto timelogWithSetting = TimesheetLogsTestDataFactory
			.createTimelogWithSettingQueryResult();
		timelogWithSetting.setIsRemarkMandatory(0);
		List<TimelogWithSettingQueryResultDto> timeLogsWithSettings = Arrays.asList(timelogWithSetting);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithSettingDetails(timesheetIds, accountId))
			.willReturn(timeLogsWithSettings);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(timelogWithSetting.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkContractorTimelogResultBodyDto result = this.timesheetLogsService
			.getContractorAllTimeLogs(timesheetIds, contractorValidatorDtos, errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimeLogs()).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData()).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData().getTimelogsMetaData()).isNotNull().hasSizeGreaterThan(0);
		assertThat(result.getTimesheetSettingsMetaData().getTimelogsMetaData().get(0).getIsRemarkMandatory()).isZero();
		then(this.timeLogRepository).should().findTimeLogsWithSettingDetails(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get contractor all time logs should return isRemarkMandatory null")
	void testGetContractorAllTimeLogsWithIsRemarkMandatoryNullReturnsSuccess() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorValidatorDtos = Arrays
			.asList(TimesheetLogsTestDataFactory.createContractorTimesheetAndSettingValidatorResponse());
		List<ContractorTimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogWithSettingQueryResultDto timelogWithSetting = TimesheetLogsTestDataFactory
			.createTimelogWithSettingQueryResult();
		timelogWithSetting.setIsRemarkMandatory(null);
		List<TimelogWithSettingQueryResultDto> timeLogsWithSettings = Arrays.asList(timelogWithSetting);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithSettingDetails(timesheetIds, accountId))
			.willReturn(timeLogsWithSettings);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(timelogWithSetting.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkContractorTimelogResultBodyDto result = this.timesheetLogsService
			.getContractorAllTimeLogs(timesheetIds, contractorValidatorDtos, errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimeLogs()).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData()).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData().getTimelogsMetaData()).isNotNull().hasSizeGreaterThan(0);
		assertThat(result.getTimesheetSettingsMetaData().getTimelogsMetaData().get(0).getIsRemarkMandatory()).isNull();
		then(this.timeLogRepository).should().findTimeLogsWithSettingDetails(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get contractor all time logs should return different isRemarkMandatory values for multiple timesheets")
	void testGetContractorAllTimeLogsWithMultipleTimesheetsDifferentIsRemarkMandatoryReturnsSuccess() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(1, 2);
		ContractorTimesheetAndSettingValidatorResponseBodyDto validator1 = TimesheetLogsTestDataFactory
			.createContractorTimesheetAndSettingValidatorResponse();
		validator1.setTimesheetId(1);
		ContractorTimesheetAndSettingValidatorResponseBodyDto validator2 = TimesheetLogsTestDataFactory
			.createContractorTimesheetAndSettingValidatorResponse();
		validator2.setTimesheetId(2);
		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorValidatorDtos = Arrays.asList(validator1,
				validator2);
		List<ContractorTimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogWithSettingQueryResultDto timelog1 = TimesheetLogsTestDataFactory.createTimelogWithSettingQueryResult();
		timelog1.setTimesheetId(1);
		timelog1.setIsRemarkMandatory(1);
		TimelogWithSettingQueryResultDto timelog2 = TimesheetLogsTestDataFactory.createTimelogWithSettingQueryResult();
		timelog2.setTimesheetId(2);
		timelog2.setIsRemarkMandatory(0);
		List<TimelogWithSettingQueryResultDto> timeLogsWithSettings = Arrays.asList(timelog1, timelog2);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithSettingDetails(timesheetIds, accountId))
			.willReturn(timeLogsWithSettings);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		this.givenTimeLogMapperReturnsOneResponsePerInputTimelog();
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkContractorTimelogResultBodyDto result = this.timesheetLogsService
			.getContractorAllTimeLogs(timesheetIds, contractorValidatorDtos, errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimeLogs()).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData()).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData().getTimelogsMetaData()).isNotNull().hasSize(2);
		assertThat(result.getTimesheetSettingsMetaData().getTimelogsMetaData())
			.extracting(TimelogsMetaDataDto::getIsRemarkMandatory)
			.containsExactlyInAnyOrder(1, 0);
		then(this.timeLogRepository).should().findTimeLogsWithSettingDetails(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get contractor all time logs should handle timesheets with errors")
	void testGetContractorAllTimeLogsHandlesTimesheetsWithErrors() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(1, 2, 3);

		// Create validator DTOs for all timesheet IDs
		ContractorTimesheetAndSettingValidatorResponseBodyDto validator1 = TimesheetLogsTestDataFactory
			.createContractorTimesheetAndSettingValidatorResponse();
		validator1.setTimesheetId(1);

		ContractorTimesheetAndSettingValidatorResponseBodyDto validator2 = TimesheetLogsTestDataFactory
			.createContractorTimesheetAndSettingValidatorResponse();
		validator2.setTimesheetId(2);

		ContractorTimesheetAndSettingValidatorResponseBodyDto validator3 = TimesheetLogsTestDataFactory
			.createContractorTimesheetAndSettingValidatorResponse();
		validator3.setTimesheetId(3);

		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorValidatorDtos = Arrays.asList(validator1,
				validator2, validator3);
		// Create error data for timesheet ID 2
		ContractorTimesheetSettingErrorResponseBodyDto errorDto = new ContractorTimesheetSettingErrorResponseBodyDto();
		errorDto.setTimesheetId(2);
		List<ContractorTimesheetSettingErrorResponseBodyDto> errorData = Arrays.asList(errorDto);

		// Only timesheet IDs 1 and 3 should be queried (2 has errors)
		List<Integer> expectedValidTimesheetIds = Arrays.asList(1, 3);

		// Create time logs for both valid timesheet IDs
		TimelogWithSettingQueryResultDto timelog1 = TimesheetLogsTestDataFactory.createTimelogWithSettingQueryResult();
		timelog1.setTimesheetId(1);

		TimelogWithSettingQueryResultDto timelog3 = TimesheetLogsTestDataFactory.createTimelogWithSettingQueryResult();
		timelog3.setTimesheetId(3);

		List<TimelogWithSettingQueryResultDto> timeLogsWithSettings = Arrays.asList(timelog1, timelog3);
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithSettingDetails(expectedValidTimesheetIds, accountId))
			.willReturn(timeLogsWithSettings);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkContractorTimelogResultBodyDto result = this.timesheetLogsService
			.getContractorAllTimeLogs(timesheetIds, contractorValidatorDtos, errorData);

		// Then
		assertThat(result).isNotNull();
		then(this.timeLogRepository).should().findTimeLogsWithSettingDetails(expectedValidTimesheetIds, accountId);
	}

	@Test
	@DisplayName("Get contractor all time logs should handle empty approvers for common approvers")
	void testGetContractorAllTimeLogsHandlesEmptyApproversForCommonApprovers() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorValidatorDtos = Arrays
			.asList(TimesheetLogsTestDataFactory.createContractorTimesheetAndSettingValidatorResponse());
		List<ContractorTimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogWithSettingQueryResultDto timelogWithSetting = TimesheetLogsTestDataFactory
			.createTimelogWithSettingQueryResult();
		List<TimelogWithSettingQueryResultDto> timeLogsWithSettings = Arrays.asList(timelogWithSetting);

		List<TimesheetApprover> emptyApprovers = Collections.emptyList(); // No approvers

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithSettingDetails(timesheetIds, accountId))
			.willReturn(timeLogsWithSettings);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(timelogWithSetting.getTimesheetSettingId()))
			.willReturn(emptyApprovers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));

		// When
		FetchBulkContractorTimelogResultBodyDto result = this.timesheetLogsService
			.getContractorAllTimeLogs(timesheetIds, contractorValidatorDtos, errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData().getApprovers()).isNull(); // Should
																					// be
																					// null
																					// when
																					// no
																					// common
																					// approvers
		then(this.timeLogRepository).should().findTimeLogsWithSettingDetails(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get contractor all time logs should find common approvers across multiple timesheet settings")
	void testGetContractorAllTimeLogsFindsCommonApproversAcrossMultipleTimesheetSettings() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(1, 2);

		// Create validator DTOs with different timesheet setting IDs
		ContractorTimesheetAndSettingValidatorResponseBodyDto validator1 = TimesheetLogsTestDataFactory
			.createContractorTimesheetAndSettingValidatorResponse();
		validator1.setTimesheetId(1);
		validator1.setTimesheetSettingId(100);

		ContractorTimesheetAndSettingValidatorResponseBodyDto validator2 = TimesheetLogsTestDataFactory
			.createContractorTimesheetAndSettingValidatorResponse();
		validator2.setTimesheetId(2);
		validator2.setTimesheetSettingId(200);

		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorValidatorDtos = Arrays.asList(validator1,
				validator2);
		List<ContractorTimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		// Create time logs with settings for both timesheet IDs
		TimelogWithSettingQueryResultDto timelog1 = TimesheetLogsTestDataFactory.createTimelogWithSettingQueryResult();
		timelog1.setTimesheetId(1);
		timelog1.setTimesheetSettingId(100);

		TimelogWithSettingQueryResultDto timelog2 = TimesheetLogsTestDataFactory.createTimelogWithSettingQueryResult();
		timelog2.setTimesheetId(2);
		timelog2.setTimesheetSettingId(200);

		List<TimelogWithSettingQueryResultDto> timeLogsWithSettings = Arrays.asList(timelog1, timelog2);

		// Create common approver
		TimesheetApprover commonApprover = TimesheetLogsTestDataFactory.createTimesheetApprover();
		commonApprover.setEntityId(1);
		commonApprover.setUserTypeId(1);

		// Different approver for second setting
		TimesheetApprover differentApprover = TimesheetLogsTestDataFactory.createTimesheetApprover();
		differentApprover.setEntityId(2);
		differentApprover.setUserTypeId(1);

		List<TimesheetApprover> approvers1 = Arrays.asList(commonApprover, differentApprover);
		List<TimesheetApprover> approvers2 = Arrays.asList(commonApprover); // Only common
																			// approver

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithSettingDetails(timesheetIds, accountId))
			.willReturn(timeLogsWithSettings);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(100)).willReturn(approvers1);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(200)).willReturn(approvers2);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(Arrays.asList(commonApprover)))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkContractorTimelogResultBodyDto result = this.timesheetLogsService
			.getContractorAllTimeLogs(timesheetIds, contractorValidatorDtos, errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData().getApprovers()).isNotNull(); // Should
																						// have
																						// common
																						// approvers
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(100);
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(200);
		then(this.timeLogMapper).should().mapApprovers(Arrays.asList(commonApprover));
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle null contractor details")
	void testGetTimeLogsByTimesheetIdNullContractorDetailsHandlesCorrectly() {
		// Given - This test covers the getAllTimeLogs scenario with null contractor
		// details
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays
			.asList(TimesheetLogsTestDataFactory.createTimesheetAndSettingValidatorResponse());
		List<TimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogQueryResultDto timelogQueryResult = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(timelogQueryResult);

		Object[] settingData = new Object[] { timesheetIds.get(0), // timesheetId
				false, // calculateBreakTime
				30, // breakTimeThreshold
				Arrays.asList(TimesheetLogsTestDataFactory.createTemplateWorkDay()), // templateWorkDays
				0 // isRemarkMandatory
		};
		List<Object[]> timesheetSettingsData = Arrays.<Object[]>asList(settingData);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogBreakInterval> breakIntervals = Collections.emptyList();

		// Return empty map to simulate null contractor details
		Map<Integer, ContractorNamePhotoQueryResultDto> emptyContractorDetailsMap = new HashMap<>();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(timesheetIds, accountId)).willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(timesheetIds, accountId))
			.willReturn(timesheetSettingsData);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(emptyContractorDetailsMap);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(breakIntervals);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getContractorsLogData()).isNotNull();
		// Verify that contractor details are handled gracefully when null
		then(this.candidateRepository).should().getContractorQueryResultMap(anySet());
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle null total time and overtime in work summary")
	void testGetTimeLogsByTimesheetIdNullTotalTimeAndOvertimeInWorkSummaryHandlesCorrectly() {
		// Given - This test covers the getAllTimeLogs scenario with null total time and
		// overtime
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays
			.asList(TimesheetLogsTestDataFactory.createTimesheetAndSettingValidatorResponse());
		List<TimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		// Create time log with null total time and overtime
		TimelogQueryResultDto timelogQueryResult = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		timelogQueryResult.setTotalTime(null); // Null total time
		timelogQueryResult.setOverTime(null); // Null overtime
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(timelogQueryResult);

		Object[] settingData = new Object[] { timesheetIds.get(0), // timesheetId
				false, // calculateBreakTime
				30, // breakTimeThreshold
				Arrays.asList(TimesheetLogsTestDataFactory.createTemplateWorkDay()), // templateWorkDays
				0 // isRemarkMandatory
		};
		List<Object[]> timesheetSettingsData = Arrays.<Object[]>asList(settingData);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorDetailsMap = new HashMap<>();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(timesheetIds, accountId)).willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(timesheetIds, accountId))
			.willReturn(timesheetSettingsData);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(contractorDetailsMap);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getContractorsLogData()).isNotNull();
		// Verify that null values are handled correctly (converted to 0)
		then(this.timeLogRepository).should().findTimeLogsWithDetails(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle null setting data")
	void testGetTimeLogsByTimesheetIdNullSettingDataHandlesCorrectly() {
		// Given - This test covers the getAllTimeLogs scenario with null setting data
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays
			.asList(TimesheetLogsTestDataFactory.createTimesheetAndSettingValidatorResponse());
		List<TimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogQueryResultDto timelogQueryResult = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(timelogQueryResult);

		// Return empty list to simulate no setting data found
		List<Object[]> emptyTimesheetSettingsData = Collections.emptyList();

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorDetailsMap = new HashMap<>();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(timesheetIds, accountId)).willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(timesheetIds, accountId))
			.willReturn(emptyTimesheetSettingsData);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(contractorDetailsMap);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getContractorsLogData()).isNotNull();
		// Verify that missing setting data is handled gracefully
		then(this.timeLogRepository).should().findTimesheetSettingsForTimesheets(timesheetIds, accountId);
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle safeConvertToInt with null Long value")
	void testGetTimeLogsByTimesheetIdSafeConvertToIntNullValueReturnsZero() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		// Default workLogType is ENTER_WORK_TIME, so getTimeLogWorkSummaries will be
		// called
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		// Create work summary with null values to test safeConvertToInt
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		TimeLogWorkSummaryQueryResultDto workSummary = workSummaryList.get(0);
		workSummary.setTotalWorkTime(null); // This will test the safeConvertToInt method
		workSummary.setTotalBreakTime(null);
		workSummary.setTotalOvertime(null);
		workSummary.setTotalTime(null);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetTotalPayBill()).isNotNull();
		// Verify that null Long values are converted to 0 as Integer
		assertThat(result.getTimesheetTotalPayBill().getTotalWorkTime()).isZero();
		assertThat(result.getTimesheetTotalPayBill().getTotalBreakTime()).isZero();
		assertThat(result.getTimesheetTotalPayBill().getTotalOvertime()).isZero();
		assertThat(result.getTimesheetTotalPayBill().getTotalTime()).isZero();
		then(this.timeLogJpaRepository).should().getTimeLogWorkSummaries(Arrays.asList(timesheetId));
	}

	// ===== Tests for validateTimesheetsNotApproved =====
	// ===== Tests for getPortalTimeLogs =====

	@Test
	@DisplayName("Get portal time logs should return success response")
	void testGetPortalTimeLogsValidRequestReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.SUBMITTED.getId());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		assertThat(result.getBillCurrencySymbol()).isEqualTo("$");
		assertThat(result.getBillCurrencyCode()).isEqualTo("USD");
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
		then(this.timeLogJpaRepository).should().findByTimesheetId(timesheetId);
		then(this.timesheetApprovalJpaRepository).should().findFirstByTimesheetIdOrderByIdDesc(timesheetId);
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(queryResult.getTimesheetSettingId());
		then(this.timeLogMapper).should().mapApprovers(approvers);
	}

	@Test
	@DisplayName("Get portal time logs should throw ResourceNotFoundException when timesheet not found")
	void testGetPortalTimeLogsTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.getPortalTimeLogs(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timeLogRepository).should(never()).getTimeLogByTimesheetId(anyInt());
	}

	@Test
	@DisplayName("Get portal time logs should throw ResourceNotFoundException when time logs not found")
	void testGetPortalTimeLogsTimeLogsNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(Collections.emptyList());

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.getPortalTimeLogs(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Time logs for timesheet");

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
		then(this.timeLogJpaRepository).should().findByTimesheetId(timesheetId);
		then(this.timesheetApprovalJpaRepository).should(never()).findFirstByTimesheetIdOrderByIdDesc(anyInt());
	}

	@Test
	@DisplayName("Get portal time logs should throw ResourceNotFoundException when approval not found")
	void testGetPortalTimeLogsApprovalNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.getPortalTimeLogs(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("TimesheetApproval for timesheet");

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
		then(this.timeLogJpaRepository).should().findByTimesheetId(timesheetId);
		then(this.timesheetApprovalJpaRepository).should().findFirstByTimesheetIdOrderByIdDesc(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs should set approver name for agency recruiter")
	void testGetPortalTimeLogsAgencyRecruiterSetsApproverName() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForAgencyRecruiter();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovedBy()).isEqualTo("Test User");
		then(this.userRepository).should().getUserDetails(queryResult.getEntityId());
	}

	@Test
	@DisplayName("Get portal time logs should set approver name for company contact")
	void testGetPortalTimeLogsCompanyContactSetsApproverName() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.timesheetRepository.getCompanyIdLinkedToTimesheet(timesheetId, accountId)).willReturn(companyId);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovedBy()).isEqualTo("Test Contact");
		assertThat(result.getBillCurrencySymbol()).isEqualTo("$");
		assertThat(result.getBillCurrencyCode()).isEqualTo("USD");
		then(this.contactRepository).should().getContactNamePhotoMap(anySet());
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(queryResult.getTimesheetSettingId());
		then(this.timeLogMapper).should().mapApprovers(approvers);
	}

	@Test
	@DisplayName("Get portal time logs should process approved status for contact user")
	void testGetPortalTimeLogsApprovedStatusProcessesCorrectly() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.timesheetRepository.getCompanyIdLinkedToTimesheet(timesheetId, accountId)).willReturn(companyId);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId)).willReturn(null);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(TimesheetLogsTestDataFactory.createTimesheetApproverList());
		given(this.timeLogMapper.mapApprovers(any())).willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.APPROVED.getId());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		assertThat(result.getCreatedOn()).isEqualTo(1704067200);
		assertThat(result.getBillCurrencySymbol()).isEqualTo("$");
		assertThat(result.getBillCurrencyCode()).isEqualTo("USD");
		then(this.timeLogJpaRepository).should().getTimeLogWorkSummaries(Arrays.asList(timesheetId));
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(timesheetId, accountId);
		then(this.timesheetApproverRepository).should().findByTimesheetSettingId(queryResult.getTimesheetSettingId());
	}

	@Test
	@DisplayName("Get portal time logs should set approver name for contractor user type with approved status")
	void testGetPortalTimeLogsContractorUserTypeApprovedStatusSetsApproverName() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.timesheetRepository.getCompanyIdLinkedToTimesheet(timesheetId, accountId)).willReturn(companyId);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForContractor();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContractorNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.APPROVED.getId());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		assertThat(result.getApprovedBy()).isEqualTo("Test Contractor");
		then(this.candidateRepository).should().getContractorQueryResultMap(anySet());
	}

	@Test
	@DisplayName("Get portal time logs should set approver name for contractor user type with rejected status")
	void testGetPortalTimeLogsContractorUserTypeRejectedStatusSetsApproverName() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.timesheetRepository.getCompanyIdLinkedToTimesheet(timesheetId, accountId)).willReturn(companyId);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForContractor();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.REJECTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithRejectedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContractorNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.REJECTED.getId());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		assertThat(result.getApprovedBy()).isEqualTo("Test Contractor");
		assertThat(result.getRemark()).isEqualTo("Rejected for testing");
		then(this.candidateRepository).should().getContractorQueryResultMap(anySet());
	}

	@Test
	@DisplayName("Get portal time logs should set approver name for contractor user type with submitted status")
	void testGetPortalTimeLogsContractorUserTypeSubmittedStatusSetsApproverName() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.timesheetRepository.getCompanyIdLinkedToTimesheet(timesheetId, accountId)).willReturn(companyId);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForContractor();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContractorNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.SUBMITTED.getId());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		assertThat(result.getApprovedBy()).isEqualTo("Test Contractor");
		then(this.candidateRepository).should().getContractorQueryResultMap(anySet());
	}

	@Test
	@DisplayName("Get portal time logs should handle contractor not found gracefully")
	void testGetPortalTimeLogsContractorUserTypeNotFoundHandlesGracefully() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.timesheetRepository.getCompanyIdLinkedToTimesheet(timesheetId, accountId)).willReturn(companyId);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForContractor();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(new HashMap<>());
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.APPROVED.getId());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		assertThat(result.getApprovedBy()).isNull();
		then(this.candidateRepository).should().getContractorQueryResultMap(anySet());
	}

	@Test
	@DisplayName("Get portal time logs should handle null custom rules")
	void testGetPortalTimeLogsNullCustomRulesSetsWeeklyEnabledFalse() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.timesheetRepository.getCompanyIdLinkedToTimesheet(timesheetId, accountId)).willReturn(companyId);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(null); // Null custom rules
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isFalse();
	}

	@Test
	@DisplayName("Get portal time logs should handle empty custom rules")
	void testGetPortalTimeLogsEmptyCustomRulesSetsWeeklyEnabledFalse() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.timesheetRepository.getCompanyIdLinkedToTimesheet(timesheetId, accountId)).willReturn(companyId);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(Collections.emptyList()); // Empty custom rules
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isFalse();
	}

	@Test
	@DisplayName("Get portal time logs should handle break intervals for start end time work log type")
	void testGetPortalTimeLogsStartEndTimeLoadsBreakIntervals() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		given(this.timesheetRepository.getCompanyIdLinkedToTimesheet(timesheetId, accountId)).willReturn(companyId);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		// Note: Service uses timeLogIntervalRepository.findByTimeLogIdIn() which is
		// stubbed in setUp
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		then(this.timeLogIntervalRepository).should(times(1)).findByTimeLogIdIn(anyList());
	}

	@Test
	@DisplayName("Get portal time logs should process contractor user type logic correctly")
	void testGetPortalTimeLogsContractorUserTypeProcessesCorrectly() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 200;

		// Mock ContractorPrincipal
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate); // userTypeId
																														// =
																														// 3

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		queryResult.setPayCurrencySymbol("$");
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.SUBMITTED.getId());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		assertThat(result.getPayCurrencySymbol()).isEqualTo("$");
		assertThat(result.getPayCurrencyCode()).isEqualTo("USD");
		assertThat(result.getCreatedOn()).isEqualTo(1704067200);
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
		then(this.timeLogJpaRepository).should().findByTimesheetId(timesheetId);
		then(this.timesheetApprovalJpaRepository).should().findFirstByTimesheetIdOrderByIdDesc(timesheetId);
		then(this.timeLogJpaRepository).should().getTimeLogWorkSummaries(Arrays.asList(timesheetId));
	}

	@Test
	@DisplayName("Get portal time logs should set contractor approved details for approved status with contractor user type")
	void testGetPortalTimeLogsContractorUserTypeApprovedStatusSetsDetails() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 200;

		// Mock ContractorPrincipal
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate); // userTypeId
																														// =
																														// 3

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		queryResult.setPayCurrencySymbol("$");
		queryResult.setPaymentStatusId(1);
		queryResult.setPaymentPaidOn(1704067200);
		queryResult.setPayoutNumber("PAYOUT-001");
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.APPROVED.getId());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		assertThat(result.getPayCurrencySymbol()).isEqualTo("$");
		assertThat(result.getPayCurrencyCode()).isEqualTo("USD");
		assertThat(result.getPayStatusId()).isEqualTo(1);
		assertThat(result.getPayoutPaidOn()).isEqualTo(1704067200);
		assertThat(result.getPayoutNumber()).isEqualTo("PAYOUT-001");
		assertThat(result.getCreatedOn()).isEqualTo(1704067200);
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
		then(this.timeLogJpaRepository).should().findByTimesheetId(timesheetId);
		then(this.timesheetApprovalJpaRepository).should().findFirstByTimesheetIdOrderByIdDesc(timesheetId);
		then(this.timeLogJpaRepository).should().getTimeLogWorkSummaries(Arrays.asList(timesheetId));
	}

	@Test
	@DisplayName("Get portal time logs should return isRemarkMandatory true")
	void testGetPortalTimeLogsWithIsRemarkMandatoryTrueReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setIsRemarkMandatory(1);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(1);
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs should return isRemarkMandatory false")
	void testGetPortalTimeLogsWithIsRemarkMandatoryFalseReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setIsRemarkMandatory(0);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isZero();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs should return isRemarkMandatory null")
	void testGetPortalTimeLogsWithIsRemarkMandatoryNullReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setIsRemarkMandatory(null);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isNull();
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs for CONTRACTOR persona should return isRemarkMandatory true")
	void testGetPortalTimeLogsForContractorWithIsRemarkMandatoryTrueReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 1;

		// Mock ContractorPrincipal
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsRemarkMandatory(1);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(1);
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs for CONTRACTOR persona should return isRemarkMandatory false")
	void testGetPortalTimeLogsForContractorWithIsRemarkMandatoryFalseReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 1;

		// Mock ContractorPrincipal
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsRemarkMandatory(0);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isZero();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs for CONTRACTOR persona should return isRemarkMandatory null")
	void testGetPortalTimeLogsForContractorWithIsRemarkMandatoryNullReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 1;

		// Mock ContractorPrincipal
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsRemarkMandatory(null);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isNull();
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs should return isRemarkMandatory true with calculateBreakTime true")
	void testGetPortalTimeLogsWithIsRemarkMandatoryTrueAndCalculateBreakTimeTrueReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setIsRemarkMandatory(1);
		queryResult.setCalculateBreakTime(true);
		queryResult.setBreakTimeThreshold(Integer.valueOf(30));
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(1);
		assertThat(result.getCalculateBreakTime()).isTrue();
		assertThat(result.getBreakTimeThreshold()).isEqualTo(Integer.valueOf(30));
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs should return isRemarkMandatory false with calculateBreakTime false")
	void testGetPortalTimeLogsWithIsRemarkMandatoryFalseAndCalculateBreakTimeFalseReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setIsRemarkMandatory(0);
		queryResult.setCalculateBreakTime(false);
		queryResult.setBreakTimeThreshold(null);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isZero();
		assertThat(result.getCalculateBreakTime()).isFalse();
		assertThat(result.getBreakTimeThreshold()).isNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs should return isRemarkMandatory true with approved status")
	void testGetPortalTimeLogsWithIsRemarkMandatoryTrueAndApprovedStatusReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setIsRemarkMandatory(1);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(1);
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.APPROVED.getId());
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs should return isRemarkMandatory false with rejected status")
	void testGetPortalTimeLogsWithIsRemarkMandatoryFalseAndRejectedStatusReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setIsRemarkMandatory(0);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.REJECTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithRejectedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isZero();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.REJECTED.getId());
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs for CONTRACTOR persona should return isRemarkMandatory true with approved status")
	void testGetPortalTimeLogsForContractorWithIsRemarkMandatoryTrueAndApprovedStatusReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 1;

		// Mock ContractorPrincipal
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsRemarkMandatory(1);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(1);
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.APPROVED.getId());
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs for CONTRACTOR persona should return isRemarkMandatory false with rejected status")
	void testGetPortalTimeLogsForContractorWithIsRemarkMandatoryFalseAndRejectedStatusReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 1;

		// Mock ContractorPrincipal
		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setIsRemarkMandatory(0);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.REJECTED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithRejectedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isZero();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.REJECTED.getId());
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Get portal time logs should return isRemarkMandatory with all timesheet setting fields")
	void testGetPortalTimeLogsWithIsRemarkMandatoryAndAllTimesheetSettingFieldsReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		// Mock ContactPrincipal
		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		queryResult.setIsRemarkMandatory(1);
		queryResult.setCalculateBreakTime(true);
		queryResult.setBreakTimeThreshold(Integer.valueOf(60));
		queryResult.setWorkLogType(workTimeEnum.ENTER_WORK_TIME.getId());
		queryResult.setTimesheetFrequency(Integer.valueOf(1));
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setCustomRules(TimesheetLogsTestDataFactory.createCustomRuleList());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.contactRepository.getContactNamePhotoMap(anySet())).willReturn(
				Map.of(queryResult.getEntityId(), TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsRemarkMandatory()).isEqualTo(1);
		assertThat(result.getCalculateBreakTime()).isTrue();
		assertThat(result.getBreakTimeThreshold()).isEqualTo(Integer.valueOf(60));
		assertThat(result.getWorkLogType()).isEqualTo(workTimeEnum.ENTER_WORK_TIME.getId());
		assertThat(result.getTimesheetFrequency()).isEqualTo(Integer.valueOf(1));
		assertThat(result.getIsRemarkMandatory()).isEqualTo(queryResult.getIsRemarkMandatory());
		then(this.timeLogRepository).should().getTimeLogByTimesheetId(timesheetId);
	}

	@Test
	@DisplayName("Bulk update time logs should throw NullPointerException when principal type is null")
	void testBulkUpdateTimeLogsNullPrincipalTypeThrowsNullPointerException() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();

		// Mock principal type that returns null - this will cause NullPointerException in
		// switch
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal unknownPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal.class);
		given(unknownPrincipal.getPrincipalType()).willReturn(null); // Null type causes
																		// NPE in switch
		given(this.auth.getUnifiedPrincipal()).willReturn(unknownPrincipal);

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(NullPointerException.class);

		then(this.auth).should().getUnifiedPrincipal();
	}

	@Test
	@DisplayName("Bulk update time logs should update existing break interval")
	void testBulkUpdateTimeLogsUpdateExistingBreakIntervalHandlesCorrectly() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		// Create break interval with existing ID (update scenario)
		BreakIntervalDto updateInterval = new BreakIntervalDto();
		updateInterval.setId(1); // Existing ID
		updateInterval.setBreakStartTime(600); // Updated times
		updateInterval.setBreakEndTime(630);
		// Ensure workTimeDetails exists
		if (request.getTimeLogs().get(0).getWorkTimeDetails() == null
				|| request.getTimeLogs().get(0).getWorkTimeDetails().isEmpty()) {
			WorkTimeDetailDto workTimeDetail = new WorkTimeDetailDto();
			request.getTimeLogs().get(0).setWorkTimeDetails(Arrays.asList(workTimeDetail));
		}
		request.getTimeLogs().get(0).getWorkTimeDetails().get(0).setBreakIntervals(Arrays.asList(updateInterval));

		TimeLogBreakInterval existingInterval = TimesheetLogsTestDataFactory.createTimeLogBreakInterval();
		existingInterval.setId(1);
		List<TimeLogBreakInterval> existingBreakIntervals = Arrays.asList(existingInterval);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(0).getId(), timesheetId))
			.willReturn(Optional.of(timeLog));
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(Arrays.asList(timeLog.getId())))
			.willReturn(existingBreakIntervals);
		given(this.timeLogBreakIntervalRepository.saveBreakIntervals(anyList())).willReturn(existingBreakIntervals);
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId, userId, AccountUserEnum.USERTYPEID.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - Service uses batch interval operations via timeLogIntervalRepository
		then(this.timeLogIntervalRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Bulk update time logs should handle empty existing break intervals when deleting")
	void testBulkUpdateTimeLogsEmptyExistingBreakIntervalsWhenDeletingHandlesCorrectly() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		// Set empty break intervals (should delete all existing, but none exist)
		// Empty break intervals - ensure workTimeDetails exists
		if (request.getTimeLogs().get(0).getWorkTimeDetails() == null
				|| request.getTimeLogs().get(0).getWorkTimeDetails().isEmpty()) {
			WorkTimeDetailDto workTimeDetail = new WorkTimeDetailDto();
			request.getTimeLogs().get(0).setWorkTimeDetails(Arrays.asList(workTimeDetail));
		}
		request.getTimeLogs().get(0).getWorkTimeDetails().get(0).setBreakIntervals(Arrays.asList());

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(0).getId(), timesheetId))
			.willReturn(Optional.of(timeLog));
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(Arrays.asList(timeLog.getId())))
			.willReturn(Arrays.asList()); // No existing break intervals
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId, userId, AccountUserEnum.USERTYPEID.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - Service uses batch operations via timeLogIntervalRepository
		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Bulk update time logs should handle break intervals with mixed operations")
	void testBulkUpdateTimeLogsBreakIntervalsMixedOperationsHandlesCorrectly() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		// Create mixed break intervals: one to update, one to create, one to delete
		BreakIntervalDto updateInterval = new BreakIntervalDto();
		updateInterval.setId(1); // Existing ID - update
		updateInterval.setBreakStartTime(600);
		updateInterval.setBreakEndTime(630);

		BreakIntervalDto newInterval = new BreakIntervalDto();
		newInterval.setId(null); // No ID - create new
		newInterval.setBreakStartTime(720);
		newInterval.setBreakEndTime(750);

		BreakIntervalDto deleteInterval = new BreakIntervalDto();
		deleteInterval.setId(2); // Existing ID - delete (not in payload after filtering)
		deleteInterval.setBreakStartTime(-1);
		deleteInterval.setBreakEndTime(-1);

		// Ensure workTimeDetails exists
		if (request.getTimeLogs().get(0).getWorkTimeDetails() == null
				|| request.getTimeLogs().get(0).getWorkTimeDetails().isEmpty()) {
			WorkTimeDetailDto workTimeDetail = new WorkTimeDetailDto();
			request.getTimeLogs().get(0).setWorkTimeDetails(Arrays.asList(workTimeDetail));
		}
		request.getTimeLogs()
			.get(0)
			.getWorkTimeDetails()
			.get(0)
			.setBreakIntervals(Arrays.asList(updateInterval, newInterval, deleteInterval));
		request.getTimeLogs().get(0).getWorkTimeDetails().get(0).setWorkStartTime(null);
		request.getTimeLogs().get(0).getWorkTimeDetails().get(0).setWorkEndTime(null);

		// Existing break intervals: ID 1 (to update) and ID 2 (to delete)
		TimeLogBreakInterval existingInterval1 = TimesheetLogsTestDataFactory.createTimeLogBreakInterval();
		existingInterval1.setId(1);
		TimeLogBreakInterval existingInterval2 = TimesheetLogsTestDataFactory.createTimeLogBreakInterval();
		existingInterval2.setId(2);
		List<TimeLogBreakInterval> existingBreakIntervals = Arrays.asList(existingInterval1, existingInterval2);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(0).getId(), timesheetId))
			.willReturn(Optional.of(timeLog));
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(Arrays.asList(timeLog.getId())))
			.willReturn(existingBreakIntervals);
		given(this.timeLogBreakIntervalRepository.saveBreakIntervals(anyList())).willReturn(existingBreakIntervals);
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId, userId, AccountUserEnum.USERTYPEID.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - Service uses batch interval operations via timeLogIntervalRepository
		then(this.timeLogIntervalRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Bulk update time logs should handle break interval with null ID creates new interval")
	void testBulkUpdateTimeLogsBreakIntervalNullIdCreatesNewInterval() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		// Create break interval with null ID (new interval)
		BreakIntervalDto newInterval = new BreakIntervalDto();
		newInterval.setId(null); // Null ID - should create new
		newInterval.setBreakStartTime(600);
		newInterval.setBreakEndTime(630);
		// Ensure workTimeDetails exists
		if (request.getTimeLogs().get(0).getWorkTimeDetails() == null
				|| request.getTimeLogs().get(0).getWorkTimeDetails().isEmpty()) {
			WorkTimeDetailDto workTimeDetail = new WorkTimeDetailDto();
			request.getTimeLogs().get(0).setWorkTimeDetails(Arrays.asList(workTimeDetail));
		}
		request.getTimeLogs().get(0).getWorkTimeDetails().get(0).setBreakIntervals(Arrays.asList(newInterval));

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(0).getId(), timesheetId))
			.willReturn(Optional.of(timeLog));
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(Arrays.asList(timeLog.getId())))
			.willReturn(Arrays.asList()); // No existing intervals
		given(this.timeLogBreakIntervalRepository.saveBreakIntervals(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createTimeLogBreakInterval()));
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId, userId, AccountUserEnum.USERTYPEID.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - Service uses batch interval operations via timeLogIntervalRepository
		then(this.timeLogIntervalRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Bulk update time logs should handle break interval with null breakStartTime and breakEndTime")
	void testBulkUpdateTimeLogsBreakIntervalNullTimesHandlesCorrectly() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		// Create break interval with null times (should not be treated as deletion
		// marker)
		BreakIntervalDto interval = new BreakIntervalDto();
		interval.setId(null);
		interval.setBreakStartTime(null);
		interval.setBreakEndTime(null);
		// Ensure workTimeDetails exists
		if (request.getTimeLogs().get(0).getWorkTimeDetails() == null
				|| request.getTimeLogs().get(0).getWorkTimeDetails().isEmpty()) {
			WorkTimeDetailDto workTimeDetail = new WorkTimeDetailDto();
			request.getTimeLogs().get(0).setWorkTimeDetails(Arrays.asList(workTimeDetail));
		}
		request.getTimeLogs().get(0).getWorkTimeDetails().get(0).setBreakIntervals(Arrays.asList(interval));

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(0).getId(), timesheetId))
			.willReturn(Optional.of(timeLog));
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(Arrays.asList(timeLog.getId())))
			.willReturn(Arrays.asList());
		given(this.timeLogBreakIntervalRepository.saveBreakIntervals(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createTimeLogBreakInterval()));
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId, userId, AccountUserEnum.USERTYPEID.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - Service uses batch interval operations via timeLogIntervalRepository
		then(this.timeLogIntervalRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Bulk update time logs should handle break interval with only one time as -1")
	void testBulkUpdateTimeLogsBreakIntervalOneTimeMinusOneHandlesCorrectly() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetSetting timesheetSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();

		// Create break interval with only one time as -1 (should not be deletion marker)
		BreakIntervalDto interval = new BreakIntervalDto();
		interval.setId(1);
		interval.setBreakStartTime(-1);
		interval.setBreakEndTime(630); // Not -1, so not a deletion marker
		// Ensure workTimeDetails exists
		if (request.getTimeLogs().get(0).getWorkTimeDetails() == null
				|| request.getTimeLogs().get(0).getWorkTimeDetails().isEmpty()) {
			WorkTimeDetailDto workTimeDetail = new WorkTimeDetailDto();
			request.getTimeLogs().get(0).setWorkTimeDetails(Arrays.asList(workTimeDetail));
		}
		request.getTimeLogs().get(0).getWorkTimeDetails().get(0).setBreakIntervals(Arrays.asList(interval));
		request.getTimeLogs().get(0).getWorkTimeDetails().get(0).setWorkStartTime(null);
		request.getTimeLogs().get(0).getWorkTimeDetails().get(0).setWorkEndTime(null);

		TimeLogBreakInterval existingInterval = TimesheetLogsTestDataFactory.createTimeLogBreakInterval();
		existingInterval.setId(1);
		List<TimeLogBreakInterval> existingBreakIntervals = Arrays.asList(existingInterval);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdAndAccountId(timesheetSetting.getId(), accountId))
			.willReturn(Optional.of(timesheetSetting));
		given(this.timeLogJpaRepository.findByIdAndTimesheetId(request.getTimeLogs().get(0).getId(), timesheetId))
			.willReturn(Optional.of(timeLog));
		given(this.timeLogBreakIntervalRepository.findBreakIntervalsByTimeLogIdIn(Arrays.asList(timeLog.getId())))
			.willReturn(existingBreakIntervals);
		given(this.timeLogBreakIntervalRepository.saveBreakIntervals(anyList())).willReturn(existingBreakIntervals);
		willDoNothing().given(this.timeLogRepository).saveTimeLog(any(TimeLog.class));
		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.SUBMITTED.getId()), eq(null));
		willDoNothing().given(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(timesheetId, userId, AccountUserEnum.USERTYPEID.getId());

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - Service uses batch interval operations via timeLogIntervalRepository
		then(this.timeLogIntervalRepository).should().batchUpsert(anyList()); // Should
																				// update,
																				// not
																				// skip
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle SUBMITTED status with work time entry")
	void testGetTimeLogsByTimesheetIdSubmittedStatusWithWorkTimeEntryReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		queryResult.setWorkLogType(workTimeEnum.ENTER_WORK_TIME.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId))).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.SUBMITTED.getId());
		assertThat(result.getApprovedByUserId()).isEqualTo(TimesheetLogsTestDataFactory.getDefaultUserId());
		assertThat(result.getCreatedOn()).isEqualTo(1704067200);
		assertThat(result.getPayCurrencySymbol()).isEqualTo("$");
		assertThat(result.getPayCurrencyCode()).isEqualTo("USD");
		assertThat(result.getBillCurrencySymbol()).isEqualTo("$");
		assertThat(result.getBillCurrencyCode()).isEqualTo("USD");
		then(this.timeLogJpaRepository).should().getTimeLogWorkSummaries(Arrays.asList(timesheetId));
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle COMPANY_CONTACT approver type")
	void testGetTimeLogsByTimesheetIdCompanyContactApproverTypeReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		ContactNamePhotoQueryResultDto contactDetails = TimesheetLogsTestDataFactory
			.createContactNamePhotoQueryResult();
		Map<Integer, ContactNamePhotoQueryResultDto> contactMap = Map.of(queryResult.getEntityId(), contactDetails);

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.contactRepository.getContactNamePhotoMap(Set.of(queryResult.getEntityId()))).willReturn(contactMap);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getApprovedBy()).isEqualTo("Test Contact");
		then(this.contactRepository).should().getContactNamePhotoMap(Set.of(queryResult.getEntityId()));
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle null invoice status")
	void testGetTimeLogsByTimesheetIdNullInvoiceStatusReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setInvoiceStatus(null); // Null invoice status
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getInvoiceStatusId()).isNull();
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle empty work summary")
	void testGetTimeLogsByTimesheetIdEmptyWorkSummaryReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId)))
			.willReturn(Arrays.asList()); // Empty work summary

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetTotalPayBill()).isNull();
	}

	@Test
	@DisplayName("Get time logs by timesheet ID should handle START_END_TIME work summary")
	void testGetTimeLogsByTimesheetIdStartEndTimeWorkSummaryReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		then(this.timeLogJpaRepository).should().getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId));
		then(this.timeLogJpaRepository).should(never()).getTimeLogWorkSummaries(anyList());
	}

	// ===== Tests for getPortalTimeLogs =====

	@Test
	@DisplayName("Get portal time logs for CONTRACTOR persona should return success")
	void testGetPortalTimeLogsForContractorPersonaReturnsSuccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer contractorId = 1;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(java.util.Optional.of(timesheet));

		TimesheetLogQueryResultDto timesheetLogQueryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResult();
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(timesheetLogQueryResult);

		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);

		TimesheetApproval timesheetApproval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(timesheetApproval);

		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getPayCurrencySymbol()).isEqualTo("$");
		assertThat(result.getPayCurrencyCode()).isEqualTo("USD");
		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
	}

	@Test
	@DisplayName("Get portal time logs for CONTACT persona should throw ResourceNotFoundException when timesheet not found")
	void testGetPortalTimeLogsForContactPersonaJobNotFoundThrowsResourceNotFoundException() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		// The service calls validateTimesheetExists first, which throws
		// ResourceNotFoundException
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.getPortalTimeLogs(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");

		then(this.auth).should().getUnifiedPrincipal();
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(timesheetId, accountId);
	}

	@Test
	@DisplayName("Get portal time logs should throw UnauthorizedAccessException for USER persona")
	void testGetPortalTimeLogsForUserPersonaThrowsUnauthorizedAccessException() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.getPortalTimeLogs(timesheetId))
			.isInstanceOf(io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException.class)
			.hasMessageContaining("Only contractors and contacts can access portal timesheets");

		then(this.auth).should().getUnifiedPrincipal();
	}

	@Test
	@DisplayName("Get portal time logs should throw NullPointerException when principal type is null")
	void testGetPortalTimeLogsForUnknownPersonaTypeThrowsUnauthorizedAccessException() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();

		AuthPrincipal unknownPrincipal = mock(AuthPrincipal.class);
		given(unknownPrincipal.getPrincipalType()).willReturn(null);
		given(this.auth.getUnifiedPrincipal()).willReturn(unknownPrincipal);

		// When & Then
		// When getPrincipalType() returns null, the switch statement throws
		// NullPointerException
		assertThatThrownBy(() -> this.timesheetLogsService.getPortalTimeLogs(timesheetId))
			.isInstanceOf(NullPointerException.class);

		then(this.auth).should().getUnifiedPrincipal();
	}

	// ===== Tests for validateTimeLogIntervalConflicts (interval count, overlap, break
	// count, break bounds, mandatory remark) =====

	private void setupUserPrincipalForValidation() {
		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
	}

	private void setupTimesheetAndSettingMocks(Integer timesheetId, Integer accountId,
			TimesheetSetting timesheetSetting) {
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		timesheet.setId(timesheetId);
		given(this.timesheetJpaRepository.findByIdInAndAccountId(anyList(), eq(accountId)))
			.willReturn(List.of(timesheet));
		given(this.timesheetSettingJpaRepository.findByIdInAndAccountId(anyList(), eq(accountId)))
			.willReturn(List.of(timesheetSetting));
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId)).willReturn(null);
		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		// save=true filters to OPEN timesheets — without this stub, no logs are updated
		// and e.g. batchUpsert is never called
		lenient().when(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.thenAnswer((invocation) -> {
				@SuppressWarnings("unchecked")
				List<Integer> ids = invocation.getArgument(0);
				List<TimesheetApproval> list = new ArrayList<>();
				for (Integer id : ids) {
					TimesheetApproval approval = new TimesheetApproval();
					approval.setTimesheetId(id);
					approval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.OPEN.getId());
					list.add(approval);
				}
				return list;
			});
	}

	private void givenTimeLogMapperReturnsOneResponsePerInputTimelog() {
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList())).willAnswer((invocation) -> {
			@SuppressWarnings("unchecked")
			List<TimelogQueryResultDto> in = invocation.getArgument(0);
			List<TimelogResponseBodyDto> out = new ArrayList<>();
			for (int i = 0; i < in.size(); i++) {
				out.add(TimesheetLogsTestDataFactory.createBulkTimeLogResponse());
			}
			return out;
		});
	}

	private BulkUpdateTimeLogsRequestBodyDto buildRequestWithIntervals(Integer timeLogId, Integer timesheetId,
			boolean isSave, List<WorkTimeDetailDto> workTimeDetails) {
		BulkTimeLogRequestBodyDto timeLog = new BulkTimeLogRequestBodyDto();
		timeLog.setId(timeLogId);
		timeLog.setTimesheetId(timesheetId);
		timeLog.setDate(1704067200);
		timeLog.setDayTypeId(1);
		timeLog.setWorkTimeDetails(workTimeDetails);

		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setTimeLogs(List.of(timeLog));
		request.setSave(isSave);
		request.setIsApproved(false);
		return request;
	}

	private WorkTimeDetailDto createWorkTimeDetail(Integer id, int startSeconds, int endSeconds) {
		WorkTimeDetailDto detail = new WorkTimeDetailDto();
		detail.setId(id);
		detail.setWorkStartTime(startSeconds);
		detail.setWorkEndTime(endSeconds);
		return detail;
	}

	private WorkTimeDetailDto createWorkTimeDetailWithRemark(Integer id, int startSeconds, int endSeconds,
			String remark) {
		WorkTimeDetailDto detail = createWorkTimeDetail(id, startSeconds, endSeconds);
		detail.setRangeBasedRemark(remark);
		return detail;
	}

	private WorkTimeDetailDto createDeletionMarker(Integer id) {
		WorkTimeDetailDto detail = new WorkTimeDetailDto();
		detail.setId(id);
		detail.setWorkStartTime(-1);
		detail.setWorkEndTime(-1);
		return detail;
	}

	private TimeLogIntervalDto createExistingInterval(Integer id, Integer timeLogId, int startSeconds, int endSeconds) {
		return TimeLogIntervalDto.builder()
			.id(id)
			.timeLogId(timeLogId)
			.workStartTime(startSeconds)
			.workEndTime(endSeconds)
			.build();
	}

	@Test
	@DisplayName("Interval count validation: should fail when new intervals + existing exceed max of 10")
	void testValidationFailsWhenIntervalCountExceedsMax() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);

		List<TimeLogIntervalDto> existingIntervals = new ArrayList<>();
		for (int i = 1; i <= 8; i++) {
			existingIntervals
				.add(createExistingInterval(i, timeLogId, 32400 + (i - 1) * 3600, 32400 + (i - 1) * 3600 + 1800));
		}
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList()))
			.willReturn(Map.of(timeLogId, existingIntervals));

		List<WorkTimeDetailDto> newIntervals = List.of(createWorkTimeDetail(null, 72000, 73800),
				createWorkTimeDetail(null, 75600, 77400), createWorkTimeDetail(null, 79200, 81000));

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				newIntervals);

		// When & Then - 8 existing + 3 new = 11 > 10
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("exceeds the maximum of 10");
	}

	@Test
	@DisplayName("Interval count validation: should pass when count is exactly 10")
	void testValidationPassesWhenIntervalCountIsExactlyMax() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);

		List<TimeLogIntervalDto> existingIntervals = new ArrayList<>();
		for (int i = 1; i <= 8; i++) {
			existingIntervals
				.add(createExistingInterval(i, timeLogId, 32400 + (i - 1) * 3600, 32400 + (i - 1) * 3600 + 1800));
		}
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList()))
			.willReturn(Map.of(timeLogId, existingIntervals));

		List<WorkTimeDetailDto> newIntervals = List.of(createWorkTimeDetail(null, 72000, 73800),
				createWorkTimeDetail(null, 75600, 77400));

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				newIntervals);

		// When - 8 existing + 2 new = 10, should not throw
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - verification that it passed validation and proceeded
		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Interval count validation: deletions should reduce the count")
	void testValidationAccountsForDeletionsInCount() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);

		List<TimeLogIntervalDto> existingIntervals = new ArrayList<>();
		for (int i = 1; i <= 9; i++) {
			existingIntervals
				.add(createExistingInterval(i, timeLogId, 32400 + (i - 1) * 3600, 32400 + (i - 1) * 3600 + 1800));
		}
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList()))
			.willReturn(Map.of(timeLogId, existingIntervals));

		List<WorkTimeDetailDto> details = List.of(createDeletionMarker(1), createDeletionMarker(2),
				createWorkTimeDetail(null, 72000, 73800), createWorkTimeDetail(null, 75600, 77400),
				createWorkTimeDetail(null, 79200, 81000));

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true, details);

		// When - 9 existing - 2 deleted + 3 new = 10, should pass
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Work interval overlap: should fail when new interval overlaps with existing")
	void testValidationFailsWhenWorkIntervalsOverlap() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);

		// Existing: 09:00-17:00 (32400-61200)
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList()))
			.willReturn(Map.of(timeLogId, List.of(createExistingInterval(1, timeLogId, 32400, 61200))));

		// New: 15:00-18:00 (54000-64800) — overlaps with existing
		List<WorkTimeDetailDto> newIntervals = List.of(createWorkTimeDetail(null, 54000, 64800));

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				newIntervals);

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("overlapping intervals");
	}

	@Test
	@DisplayName("Work interval overlap: should pass when intervals are adjacent (no gap, no overlap)")
	void testValidationPassesWhenIntervalsAreAdjacent() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);

		// Existing: 09:00-12:00 (32400-43200)
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList()))
			.willReturn(Map.of(timeLogId, List.of(createExistingInterval(1, timeLogId, 32400, 43200))));

		// New: 12:00-17:00 (43200-61200) — adjacent, not overlapping
		List<WorkTimeDetailDto> newIntervals = List.of(createWorkTimeDetail(null, 43200, 61200));

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				newIntervals);

		// When - should not throw
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Work interval overlap: updated interval should be checked with new time range")
	void testValidationChecksUpdatedIntervalTimeRange() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);

		// Existing: id=1 09:00-12:00, id=2 13:00-17:00
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList()))
			.willReturn(Map.of(timeLogId, List.of(createExistingInterval(1, timeLogId, 32400, 43200),
					createExistingInterval(2, timeLogId, 46800, 61200))));

		// Update id=1 to 09:00-14:00 (32400-50400) — now overlaps with id=2 (13:00-17:00)
		List<WorkTimeDetailDto> details = List.of(createWorkTimeDetail(1, 32400, 50400));

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true, details);

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("overlapping intervals");
	}

	@Test
	@DisplayName("Break count validation: should fail when break intervals exceed max of 5")
	void testValidationFailsWhenBreakCountExceedsMax() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		WorkTimeDetailDto detail = createWorkTimeDetail(null, 32400, 61200); // 09:00-17:00
		List<BreakIntervalDto> breaks = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			BreakIntervalDto b = new BreakIntervalDto();
			b.setBreakStartTime(34200 + i * 3600);
			b.setBreakEndTime(34200 + i * 3600 + 900);
			breaks.add(b);
		}
		detail.setBreakIntervals(breaks);

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				List.of(detail));

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("exceeds the maximum of 5");
	}

	@Test
	@DisplayName("Break count validation: should pass with exactly 5 break intervals")
	void testValidationPassesWithExactlyMaxBreaks() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		WorkTimeDetailDto detail = createWorkTimeDetail(null, 32400, 61200); // 09:00-17:00
		List<BreakIntervalDto> breaks = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			BreakIntervalDto b = new BreakIntervalDto();
			b.setBreakStartTime(34200 + i * 3600);
			b.setBreakEndTime(34200 + i * 3600 + 900);
			breaks.add(b);
		}
		detail.setBreakIntervals(breaks);

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				List.of(detail));

		// When - should not throw
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Break bounds validation: should fail when break falls outside parent work interval")
	void testValidationFailsWhenBreakOutsideParentBounds() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		WorkTimeDetailDto detail = createWorkTimeDetail(null, 50400, 64800); // 14:00-18:00
		BreakIntervalDto breakOutside = new BreakIntervalDto();
		breakOutside.setBreakStartTime(64800); // 18:00 — at the boundary end
		breakOutside.setBreakEndTime(66600); // 18:30 — outside parent
		detail.setBreakIntervals(List.of(breakOutside));

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				List.of(detail));

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("falls outside its parent work interval");
	}

	@Test
	@DisplayName("Break bounds validation: should fail when break starts before parent work interval")
	void testValidationFailsWhenBreakStartsBeforeParent() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		WorkTimeDetailDto detail = createWorkTimeDetail(null, 50400, 64800); // 14:00-18:00
		BreakIntervalDto breakBefore = new BreakIntervalDto();
		breakBefore.setBreakStartTime(48600); // 13:30 — before parent start
		breakBefore.setBreakEndTime(51000); // 14:10
		detail.setBreakIntervals(List.of(breakBefore));

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				List.of(detail));

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("falls outside its parent work interval");
	}

	@Test
	@DisplayName("Break bounds validation: should pass when break is within parent bounds")
	void testValidationPassesWhenBreakWithinParentBounds() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		WorkTimeDetailDto detail = createWorkTimeDetail(null, 50400, 64800); // 14:00-18:00
		BreakIntervalDto breakInside = new BreakIntervalDto();
		breakInside.setBreakStartTime(54000); // 15:00
		breakInside.setBreakEndTime(55800); // 15:30
		detail.setBreakIntervals(List.of(breakInside));

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				List.of(detail));

		// When - should not throw
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Mandatory remark: should fail on submit when remark is missing and setting is enabled")
	void testValidationFailsWhenRemarkMissingOnSubmit() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		setting.setIsRemarkMandatory(1);

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		// No remark set on the work time detail
		WorkTimeDetailDto detail = createWorkTimeDetail(null, 32400, 61200); // 09:00-17:00

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, false,
				List.of(detail));

		// When & Then - save=false means submit
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("requires a remark");
	}

	@Test
	@DisplayName("Mandatory remark: should pass on save even when remark is missing")
	void testValidationPassesWhenRemarkMissingOnSave() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		setting.setIsRemarkMandatory(1);

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		WorkTimeDetailDto detail = createWorkTimeDetail(null, 32400, 61200); // 09:00-17:00,
																				// no
																				// remark

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				List.of(detail));

		// When - save=true means draft save, remark not required
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Mandatory remark: should pass on submit when remark is provided")
	void testValidationPassesWhenRemarkProvidedOnSubmit() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		setting.setIsRemarkMandatory(1);

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		WorkTimeDetailDto detail = createWorkTimeDetailWithRemark(null, 32400, 61200, "Worked on project X");

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, false,
				List.of(detail));

		// When - submit with remark present
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Mandatory remark: should fail when remark is blank/whitespace on submit")
	void testValidationFailsWhenRemarkIsBlankOnSubmit() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		setting.setIsRemarkMandatory(1);

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		WorkTimeDetailDto detail = createWorkTimeDetailWithRemark(null, 32400, 61200, "   ");

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, false,
				List.of(detail));

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("requires a remark");
	}

	@Test
	@DisplayName("Mandatory remark: should not require remark when setting is disabled")
	void testValidationPassesWhenRemarkNotMandatory() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		setting.setIsRemarkMandatory(0);

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		WorkTimeDetailDto detail = createWorkTimeDetail(null, 32400, 61200); // No remark

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, false,
				List.of(detail));

		// When - submit but remark not mandatory
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Validation skipped for ENTER_WORK_TIME type (duration-based)")
	void testValidationSkippedForDurationBasedTimeEntry() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForWorkTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);

		// Even though workTimeDetails are provided, ENTER_WORK_TIME should skip
		// interval validation entirely
		WorkTimeDetailDto detail = createWorkTimeDetail(null, 32400, 61200);

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				List.of(detail));

		// When - should not throw even without mocking findIntervalsByTimeLogIds
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - interval repo should NOT be called for interval validation
		then(this.timeLogIntervalRepository).should(never()).findIntervalsByTimeLogIds(anyList());
	}

	@Test
	@DisplayName("Interval count validation: duplicate time log IDs in payload should aggregate intervals")
	void testValidationAggregatesIntervalsForDuplicateTimeLogIds() {
		// Given — same time log ID appears twice in the payload, each with 10 new
		// intervals
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		List<WorkTimeDetailDto> batch1 = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			batch1.add(createWorkTimeDetail(null, 21600 + i * 3600, 21600 + (i + 1) * 3600));
		}
		List<WorkTimeDetailDto> batch2 = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			batch2.add(createWorkTimeDetail(null, 21600 + i * 3600, 21600 + (i + 1) * 3600));
		}

		BulkTimeLogRequestBodyDto timeLog1 = new BulkTimeLogRequestBodyDto();
		timeLog1.setId(timeLogId);
		timeLog1.setTimesheetId(timesheetId);
		timeLog1.setDate(1704067200);
		timeLog1.setDayTypeId(1);
		timeLog1.setWorkTimeDetails(batch1);

		BulkTimeLogRequestBodyDto timeLog2 = new BulkTimeLogRequestBodyDto();
		timeLog2.setId(timeLogId);
		timeLog2.setTimesheetId(timesheetId);
		timeLog2.setDate(1704067200);
		timeLog2.setDayTypeId(1);
		timeLog2.setWorkTimeDetails(batch2);

		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setTimeLogs(List.of(timeLog1, timeLog2));
		request.setSave(true);
		request.setIsApproved(false);

		// When & Then — 10 + 10 = 20 > 10
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("exceeds the maximum of 10");
	}

	@Test
	@DisplayName("Concurrent scenario: second user's overlapping interval is rejected against DB state")
	void testConcurrentOverlapDetectedAgainstExistingDbIntervals() {
		// Given — simulates User A already saved 09:00-17:00, now User B sends
		// 15:00-18:00
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);

		// User A's interval already persisted: 09:00-17:00
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList()))
			.willReturn(Map.of(timeLogId, List.of(createExistingInterval(100, timeLogId, 32400, 61200))));

		// User B sends 15:00-18:00 (54000-64800)
		List<WorkTimeDetailDto> userBIntervals = List.of(createWorkTimeDetail(null, 54000, 64800));

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				userBIntervals);

		// When & Then — overlap between 09:00-17:00 and 15:00-18:00
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("overlapping intervals")
			.hasMessageContaining("09:00")
			.hasMessageContaining("17:00")
			.hasMessageContaining("15:00")
			.hasMessageContaining("18:00");
	}

	// ===== Additional tests for 100% coverage =====

	@Test
	@DisplayName("Bulk update with empty timeLogs and empty timesheetIdNoLogChanges should return early")
	void testBulkUpdateTimeLogsEmptyTimeLogsAndEmptyNoLogChangesReturnsEarly() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setTimeLogs(Collections.emptyList());
		request.setTimesheetIdNoLogChanges(Collections.emptyList());

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - should return early without any DB operations
		then(this.timeLogRepository).should(never()).batchUpsert(anyList());
		then(this.timesheetApprovalRepository).should(never()).createBulkTimesheetApprovals(anyList());
	}

	@Test
	@DisplayName("Bulk update with null timeLogs and null timesheetIdNoLogChanges should return early")
	void testBulkUpdateTimeLogsNullTimeLogsAndNullNoLogChangesReturnsEarly() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setTimeLogs(null);
		request.setTimesheetIdNoLogChanges(null);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then - should return early without any DB operations
		then(this.timeLogRepository).should(never()).batchUpsert(anyList());
	}

	@Test
	@DisplayName("Get time logs should handle duration-based weekly overtime rule")
	void testGetTimeLogsByTimesheetIdWithDurationBasedWeeklyOvertimeRule() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		// Create duration-based weekly overtime rule (type 8)
		CustomRule durationBasedRule = new CustomRule();
		durationBasedRule.setId(1);
		durationBasedRule.setRuleType(8); // DURATION_BASED_WEEKLY_OVERTIME
		durationBasedRule.setRuleName("Duration Weekly Overtime");
		queryResult.setCustomRules(Arrays.asList(durationBasedRule));
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());

		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(anyList())).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isTrue();
	}

	@Test
	@DisplayName("Get time logs should handle null custom rule in list")
	void testGetTimeLogsByTimesheetIdWithNullCustomRuleInList() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		// Create list with null rule
		List<CustomRule> customRulesWithNull = new ArrayList<>();
		customRulesWithNull.add(null);
		CustomRule validRule = new CustomRule();
		validRule.setId(1);
		validRule.setRuleType(1); // Non-weekly rule
		customRulesWithNull.add(validRule);
		queryResult.setCustomRules(customRulesWithNull);
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());

		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(anyList())).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isFalse();
	}

	@Test
	@DisplayName("Get time logs should handle null invoice status")
	void testGetTimeLogsByTimesheetIdWithNullInvoiceStatus() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setInvoiceStatus(null);
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers)).willReturn(null);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getInvoiceStatusId()).isNull();
	}

	@Test
	@DisplayName("Get time logs should handle null period start and end for timesheet")
	void testGetTimeLogsByTimesheetIdWithNullPeriodStartEnd() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		timesheet.setPeriodStart(null);
		timesheet.setPeriodEnd(null);
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApproval();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers)).willReturn(null);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		// Timesheet period should be empty when period start/end are null
		assertThat(result.getTimeLogs()).isNotEmpty();
	}

	@Test
	@DisplayName("Break bounds validation: should skip validation when work start or end is null")
	void testValidationSkipsBreakBoundsWhenWorkTimesAreNull() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		WorkTimeDetailDto detail = new WorkTimeDetailDto();
		detail.setId(null);
		detail.setWorkStartTime(null);
		detail.setWorkEndTime(null);

		BreakIntervalDto breakInterval = new BreakIntervalDto();
		breakInterval.setBreakStartTime(32400);
		breakInterval.setBreakEndTime(33300);
		detail.setBreakIntervals(List.of(breakInterval));

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				List.of(detail));

		// When - should not throw because work times are null
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Break bounds validation: should skip validation when break times are null")
	void testValidationSkipsBreakBoundsWhenBreakTimesAreNull() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		WorkTimeDetailDto detail = createWorkTimeDetail(null, 32400, 61200);

		BreakIntervalDto breakIntervalWithNullTimes = new BreakIntervalDto();
		breakIntervalWithNullTimes.setBreakStartTime(null);
		breakIntervalWithNullTimes.setBreakEndTime(null);
		detail.setBreakIntervals(List.of(breakIntervalWithNullTimes));

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				List.of(detail));

		// When - should not throw because break times are null
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Get portal time logs for CONTACT should return client data with approved status and invoice details")
	void testGetPortalTimeLogsForContactWithApprovedStatusAndInvoice() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer clientId = 300;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));

		TimesheetLogQueryResultDto timesheetLogQueryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		timesheetLogQueryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(timesheetLogQueryResult);

		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);

		TimesheetApproval timesheetApproval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(timesheetApproval);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		given(this.timesheetApproverRepository
			.findByTimesheetSettingId(timesheetLogQueryResult.getTimesheetSettingId())).willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		given(this.contactRepository.getContactNamePhotoMap(anySet()))
			.willReturn(Map.of(timesheetLogQueryResult.getEntityId(),
					TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));

		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(anyList())).willReturn(workSummary);

		// Mock invoice data
		io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice timesheetInvoice = new io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice();
		timesheetInvoice.setBillingStatusId(2);
		timesheetInvoice.setInvoiceId(100);
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId)).willReturn(timesheetInvoice);

		io.recruitcrm.entity.model.Invoice invoice = new io.recruitcrm.entity.model.Invoice();
		invoice.setInvoiceIdNumber("INV-001");
		invoice.setCreatedOn(1704067200);
		given(this.invoicesJpaRepository.findById(100)).willReturn(Optional.of(invoice));

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.APPROVED.getId());
		assertThat(result.getBillStatusId()).isEqualTo(2);
		assertThat(result.getInvoiceNumber()).isEqualTo("INV-001");
	}

	@Test
	@DisplayName("Get portal time logs for CONTACT should handle null invoice")
	void testGetPortalTimeLogsForContactWithNullInvoice() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer clientId = 300;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(clientId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));

		TimesheetLogQueryResultDto timesheetLogQueryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		timesheetLogQueryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(timesheetLogQueryResult);

		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);

		TimesheetApproval timesheetApproval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(timesheetApproval);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		given(this.timesheetApproverRepository
			.findByTimesheetSettingId(timesheetLogQueryResult.getTimesheetSettingId())).willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		given(this.contactRepository.getContactNamePhotoMap(anySet()))
			.willReturn(Map.of(timesheetLogQueryResult.getEntityId(),
					TimesheetLogsTestDataFactory.createContactNamePhotoQueryResult()));

		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(anyList())).willReturn(workSummary);

		// No invoice data
		given(this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId)).willReturn(null);

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimesheetId()).isEqualTo(timesheetId);
		assertThat(result.getBillStatusId()).isNull();
		assertThat(result.getInvoiceNumber()).isNull();
	}

	@Test
	@DisplayName("Bulk update should handle work time details deletion markers correctly")
	void testBulkUpdateTimeLogsHandlesWorkTimeDetailsDeletionMarkers() {
		// Given
		Integer timeLogId = 1;
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();

		setupUserPrincipalForValidation();
		setupTimesheetAndSettingMocks(timesheetId, accountId, setting);
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList()))
			.willReturn(Map.of(timeLogId, List.of(createExistingInterval(100, timeLogId, 32400, 43200))));

		// Create deletion marker (id=100 with -1,-1 times)
		WorkTimeDetailDto deletionMarker = createDeletionMarker(100);

		BulkUpdateTimeLogsRequestBodyDto request = buildRequestWithIntervals(timeLogId, timesheetId, true,
				List.of(deletionMarker));

		// When
		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		// Then
		then(this.timeLogIntervalRepository).should().deleteByIdIn(anyList());
		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Get time logs should use duration summaries for ENTER_START_END_TIME")
	void testGetTimeLogsByTimesheetIdUsesDurationSummariesForStartEndTimeType() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		queryResult.setApprovalStatusId(ApprovalStatusEnum.APPROVED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithApprovedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		then(this.timeLogJpaRepository).should().getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId));
		then(this.timeLogJpaRepository).should(never()).getTimeLogWorkSummaries(anyList());
	}

	@Test
	@DisplayName("Bulk update should throw NPE when time log list contains null entries")
	void testBulkUpdateTimeLogsHandlesNullTimeLogEntries() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();

		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		BulkTimeLogRequestBodyDto validTimeLog = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		List<BulkTimeLogRequestBodyDto> timeLogsWithNull = new ArrayList<>();
		timeLogsWithNull.add(validTimeLog);
		timeLogsWithNull.add(null);
		request.setTimeLogs(timeLogsWithNull);
		request.setSave(false);
		request.setIsApproved(false);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());

		// When & Then
		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("Time log entry cannot be null");
	}

	@Test
	@DisplayName("Get all time logs should handle empty valid timesheet IDs after filtering errors")
	void testGetAllTimeLogsWithAllTimesheetsHavingErrors() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(1, 2);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);

		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = new ArrayList<>();
		TimesheetAndSettingValidatorResponseBodyDto dto1 = TimesheetLogsTestDataFactory
			.createTimesheetAndSettingValidatorResponse();
		dto1.setTimesheetId(1);
		validatorDtos.add(dto1);

		// All timesheet IDs have errors
		List<TimesheetSettingErrorResponseBodyDto> errorData = new ArrayList<>();
		TimesheetSettingErrorResponseBodyDto error1 = new TimesheetSettingErrorResponseBodyDto();
		error1.setTimesheetId(1);
		error1.setError("ERROR_1");
		TimesheetSettingErrorResponseBodyDto error2 = new TimesheetSettingErrorResponseBodyDto();
		error2.setTimesheetId(2);
		error2.setError("ERROR_2");
		errorData.add(error1);
		errorData.add(error2);

		given(this.timeLogRepository.findTimeLogsWithDetails(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());

		// When
		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getContractorsLogData()).isEmpty();
	}

	@Test
	@DisplayName("Get contractor all time logs should handle empty valid timesheet IDs after filtering errors")
	void testGetContractorAllTimeLogsWithAllTimesheetsHavingErrors() {
		// Given
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(1, 2);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);

		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> validatorDtos = new ArrayList<>();
		ContractorTimesheetAndSettingValidatorResponseBodyDto dto1 = TimesheetLogsTestDataFactory
			.createContractorTimesheetAndSettingValidatorResponse();
		dto1.setTimesheetId(1);
		validatorDtos.add(dto1);

		// All timesheet IDs have errors
		List<ContractorTimesheetSettingErrorResponseBodyDto> errorData = new ArrayList<>();
		ContractorTimesheetSettingErrorResponseBodyDto error1 = new ContractorTimesheetSettingErrorResponseBodyDto();
		error1.setTimesheetId(1);
		error1.setError("ERROR_1");
		ContractorTimesheetSettingErrorResponseBodyDto error2 = new ContractorTimesheetSettingErrorResponseBodyDto();
		error2.setTimesheetId(2);
		error2.setError("ERROR_2");
		errorData.add(error1);
		errorData.add(error2);

		given(this.timeLogRepository.findTimeLogsWithSettingDetails(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());

		// When
		FetchBulkContractorTimelogResultBodyDto result = this.timesheetLogsService
			.getContractorAllTimeLogs(timesheetIds, validatorDtos, errorData);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getTimeLogs()).isEmpty();
	}

	@Test
	@DisplayName("Portal contractor: START_END time loads intervals and break JSON via interval repository")
	void testGetPortalTimeLogsContractorStartEndTimeWithIntervalsAndBreaks() {
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 200;

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimeLogWorkSummaryQueryResultDto> workSummaryList = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		TimeLogInterval intervalEnt = mock(TimeLogInterval.class);
		given(intervalEnt.getTimeLogId()).willReturn(1);
		given(intervalEnt.getId()).willReturn(10);
		given(intervalEnt.getWorkStartTime()).willReturn(540);
		given(intervalEnt.getWorkEndTime()).willReturn(1020);
		BreakInterval br = new BreakInterval();
		br.setId(1);
		br.setBreakStartTime(540);
		br.setBreakEndTime(570);
		given(intervalEnt.getBreakInterval()).willReturn(List.of(br));
		given(this.timeLogIntervalRepository.findByTimeLogIdIn(anyList())).willReturn(List.of(intervalEnt));

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogJpaRepository.getTimeLogWorkDurationSummaries(Arrays.asList(timesheetId)))
			.willReturn(workSummaryList);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		assertThat(result).isNotNull();
		then(this.timeLogIntervalRepository).should(times(1)).findByTimeLogIdIn(anyList());
	}

	@Test
	@DisplayName("Bulk update: contractor persona completes batch save path")
	void testBulkUpdateTimeLogsContractorPersonaBatchUpsertPath() {
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setIsApproved(false);
		request.setSave(false);

		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 300;

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		TimesheetApproval a1 = new TimesheetApproval();
		a1.setTimesheetId(1);
		a1.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.SUBMITTED.getId());
		TimesheetApproval a2 = new TimesheetApproval();
		a2.setTimesheetId(2);
		a2.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(List.of(a1, a2));

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timeLogRepository).should().batchUpsert(anyList());
		then(this.timesheetApprovalRepository).should().createBulkTimesheetApprovals(anyList());
	}

	@Test
	@DisplayName("Bulk update: contact persona completes batch save path when portal allows edit")
	void testBulkUpdateTimeLogsContactPersonaBatchUpsertPath() {
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setIsApproved(false);
		request.setSave(false);
		request.setJobId(500);

		Integer contactId = 400;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(contactId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto(
				null, 1, null);
		given(this.portalAccessControlService.validatePortalAccessControl(500, contactId)).willReturn(permissions);

		TimesheetApproval a1 = new TimesheetApproval();
		a1.setTimesheetId(1);
		a1.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.SUBMITTED.getId());
		TimesheetApproval a2 = new TimesheetApproval();
		a2.setTimesheetId(2);
		a2.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(List.of(a1, a2));

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.portalAccessControlService).should().validatePortalAccessControl(500, contactId);
		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Bulk update: missing timesheet in batch fetch throws ResourceNotFoundException")
	void testBulkUpdateTimeLogsMissingTimesheetInBatchFetchThrows() {
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setIsApproved(false);
		request.setSave(false);
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		given(this.timesheetJpaRepository.findByIdInAndAccountId(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());

		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet");
	}

	@Test
	@DisplayName("Bulk update: missing timesheet setting in batch fetch throws ResourceNotFoundException")
	void testBulkUpdateTimeLogsMissingTimesheetSettingInBatchFetchThrows() {
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setIsApproved(false);
		request.setSave(false);
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());

		Timesheet ts1 = TimesheetLogsTestDataFactory.createTimesheet();
		ts1.setId(1);
		Timesheet ts2 = TimesheetLogsTestDataFactory.createTimesheet();
		ts2.setId(2);
		given(this.timesheetJpaRepository.findByIdInAndAccountId(anyList(), eq(accountId)))
			.willReturn(List.of(ts1, ts2));
		given(this.timesheetSettingJpaRepository.findByIdInAndAccountId(anyList(), eq(accountId)))
			.willReturn(Collections.emptyList());

		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("TimesheetSetting");
	}

	@Test
	@DisplayName("Bulk update: approved timesheet rejects edit when save is false")
	void testBulkUpdateTimeLogsApprovedStatusThrowsValidationError() {
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setSave(false);
		request.setIsApproved(false);
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());

		TimesheetApproval approved = new TimesheetApproval();
		approved.setTimesheetId(1);
		approved.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());
		TimesheetApproval openOther = new TimesheetApproval();
		openOther.setTimesheetId(2);
		openOther.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(List.of(approved, openOther));

		assertThatThrownBy(() -> this.timesheetLogsService.bulkUpdateTimeLogs(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Cannot edit time logs for approved timesheet");
	}

	@Test
	@DisplayName("Bulk update: metadata-only path with save=true persists bulk approvals")
	void testBulkUpdateTimeLogsMetadataOnlySaveTrueCreatesBulkApprovals() {
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();

		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setTimeLogs(Collections.emptyList());
		request.setTimesheetIdNoLogChanges(List.of(timesheetId));
		request.setSave(true);
		request.setIsApproved(false);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		TimesheetApproval openApproval = new TimesheetApproval();
		openApproval.setTimesheetId(timesheetId);
		openApproval.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.OPEN.getId());
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(List.of(openApproval));

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());

		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(anyInt(), anyInt(), anyInt(), anyInt(), any());
		lenient().doNothing()
			.when(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(anyInt(), anyInt(), anyInt());

		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timesheetApprovalRepository).should()
			.createTimesheetApproval(eq(timesheetId), eq(userId), eq(AccountUserEnum.USERTYPEID.getId()),
					eq(ApprovalStatusEnum.OPEN.getId()), eq(null));
	}

	@Test
	@DisplayName("Get all time logs: START_END work log maps intervals with breaks to work time details")
	void testGetAllTimeLogsStartEndWorkLogTypePopulatesWorkTimeDetailsFromIntervals() {
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		TimesheetAndSettingValidatorResponseBodyDto validator = TimesheetLogsTestDataFactory
			.createTimesheetAndSettingValidatorResponse();
		validator.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		validator.setContractorId(999);
		TimesheetAndSettingValidatorResponseBodyDto duplicateTimesheetValidator = TimesheetLogsTestDataFactory
			.createTimesheetAndSettingValidatorResponse();
		duplicateTimesheetValidator.setWorkLogType(workTimeEnum.ENTER_WORK_TIME.getId());
		duplicateTimesheetValidator.setContractorId(888);
		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays.asList(validator,
				duplicateTimesheetValidator);
		List<TimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogQueryResultDto timelogQueryResult = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		timelogQueryResult.setId(1);
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(timelogQueryResult);

		Object[] settingData = new Object[] { timesheetIds.get(0), Boolean.TRUE, 30,
				Arrays.asList(TimesheetLogsTestDataFactory.createTemplateWorkDay()), 1 };
		List<Object[]> timesheetSettingsData = Arrays.<Object[]>asList(settingData);

		TimeLogInterval interval = new TimeLogInterval();
		interval.setId(500);
		interval.setTimeLogId(1);
		interval.setWorkStartTime(540);
		interval.setWorkEndTime(1020);
		interval.setRangeBasedRemark("interval remark");
		BreakInterval br = new BreakInterval();
		br.setId(700);
		br.setBreakStartTime(600);
		br.setBreakEndTime(620);
		interval.setBreakInterval(List.of(br));

		TimeLogInterval orphanInterval = new TimeLogInterval();
		orphanInterval.setId(501);
		orphanInterval.setTimeLogId(99999);

		Map<Integer, ContractorNamePhotoQueryResultDto> contractorDetailsMap = new HashMap<>();
		ContractorNamePhotoQueryResultDto contractor = new ContractorNamePhotoQueryResultDto();
		contractor.setName("Test Contractor");
		contractorDetailsMap.put(999, contractor);
		contractorDetailsMap.put(888, contractor);

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(timesheetIds, accountId)).willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(timesheetIds, accountId))
			.willReturn(timesheetSettingsData);
		given(this.timeLogRepository.findCompanyByTimesheetIds(timesheetIds, accountId)).willReturn(new HashMap<>());
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(contractorDetailsMap);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogIntervalRepository.findByTimeLogIdIn(anyList()))
			.willReturn(List.of(interval, orphanInterval));
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(TimesheetLogsTestDataFactory.createBulkTimeLogResponse()));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		assertThat(result.getTimesheetSettingsMetaData().getContractorId()).isEqualTo(999);
		assertThat(result.getContractorsLogData()).isNotEmpty();
		assertThat(result.getContractorsLogData().get(0).getTimeLogs()).isNotEmpty();
		assertThat(result.getContractorsLogData().get(0).getTimeLogs().get(0).getWorkTimeDetails()).isNotNull()
			.isNotEmpty();
		then(this.timeLogIntervalRepository).should().findByTimeLogIdIn(anyList());
	}

	@Test
	@DisplayName("Get all time logs: ENTER_WORK_TIME maps break intervals from JSON onto time log DTOs")
	void testGetAllTimeLogsEnterWorkTimePopulatesBreakIntervalsFromIntervals() {
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		TimesheetAndSettingValidatorResponseBodyDto validator = TimesheetLogsTestDataFactory
			.createTimesheetAndSettingValidatorResponse();
		validator.setWorkLogType(workTimeEnum.ENTER_WORK_TIME.getId());
		List<TimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays.asList(validator);
		List<TimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogQueryResultDto timelogQueryResult = TimesheetLogsTestDataFactory.createTimelogQueryResult();
		timelogQueryResult.setId(42);
		List<TimelogQueryResultDto> timeLogs = Arrays.asList(timelogQueryResult);

		Object[] settingData = new Object[] { timesheetIds.get(0), Boolean.TRUE, 30,
				Arrays.asList(TimesheetLogsTestDataFactory.createTemplateWorkDay()), 0 };
		List<Object[]> timesheetSettingsData = Arrays.<Object[]>asList(settingData);

		TimeLogInterval interval = new TimeLogInterval();
		interval.setId(1);
		interval.setTimeLogId(42);
		interval.setWorkStartTime(540);
		interval.setWorkEndTime(1020);
		BreakInterval br = new BreakInterval();
		br.setId(55);
		br.setBreakStartTime(600);
		br.setBreakEndTime(660);
		interval.setBreakInterval(List.of(br));

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithDetails(timesheetIds, accountId)).willReturn(timeLogs);
		given(this.timeLogRepository.findTimesheetSettingsForTimesheets(timesheetIds, accountId))
			.willReturn(timesheetSettingsData);
		given(this.timeLogRepository.findCompanyByTimesheetIds(timesheetIds, accountId)).willReturn(new HashMap<>());
		given(this.candidateRepository.getContractorQueryResultMap(anySet())).willReturn(new HashMap<>());
		given(this.timesheetApproverRepository.findByTimesheetSettingId(anyInt())).willReturn(approvers);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogIntervalRepository.findByTimeLogIdIn(anyList())).willReturn(List.of(interval));

		TimelogResponseBodyDto responseDto = TimesheetLogsTestDataFactory.createBulkTimeLogResponse();
		responseDto.setId(42);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(responseDto));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		FetchBulkTimelogResultBodyDto result = this.timesheetLogsService.getAllTimeLogs(timesheetIds, validatorDtos,
				errorData);

		assertThat(result.getContractorsLogData().get(0).getTimeLogs().get(0).getBreakIntervals()).isNotNull()
			.isNotEmpty();
	}

	@Test
	@DisplayName("Get contractor all time logs: ENTER_WORK_TIME maps break intervals from interval entities")
	void testGetContractorAllTimeLogsEnterWorkTimePopulatesBreakIntervals() {
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		ContractorTimesheetAndSettingValidatorResponseBodyDto contractorValidator = TimesheetLogsTestDataFactory
			.createContractorTimesheetAndSettingValidatorResponse();
		contractorValidator.setWorkLogType(workTimeEnum.ENTER_WORK_TIME.getId());
		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> contractorValidatorDtos = Arrays
			.asList(contractorValidator);
		List<ContractorTimesheetSettingErrorResponseBodyDto> errorData = Collections.emptyList();

		TimelogWithSettingQueryResultDto row = TimesheetLogsTestDataFactory.createTimelogWithSettingQueryResult();
		row.setId(77);
		row.setTimesheetId(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		List<TimelogWithSettingQueryResultDto> timeLogsWithSettings = Arrays.asList(row);

		TimeLogInterval interval = new TimeLogInterval();
		interval.setId(1);
		interval.setTimeLogId(77);
		BreakInterval br = new BreakInterval();
		br.setId(2);
		br.setBreakStartTime(480);
		br.setBreakEndTime(510);
		interval.setBreakInterval(List.of(br));

		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithSettingDetails(timesheetIds, accountId))
			.willReturn(timeLogsWithSettings);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(row.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogIntervalRepository.findByTimeLogIdIn(anyList())).willReturn(List.of(interval));

		TimelogResponseBodyDto responseDto = TimesheetLogsTestDataFactory.createBulkTimeLogResponse();
		responseDto.setId(77);
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList()))
			.willReturn(Arrays.asList(responseDto));
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		FetchBulkContractorTimelogResultBodyDto result = this.timesheetLogsService
			.getContractorAllTimeLogs(timesheetIds, contractorValidatorDtos, errorData);

		assertThat(result.getTimeLogs()).isNotEmpty();
		assertThat(result.getTimeLogs().get(0).getBreakIntervals()).isNotEmpty();
	}

	@Test
	@DisplayName("Get contractor all time logs: shared approver across settings is mapped to metadata")
	void testGetContractorAllTimeLogsFindsCommonApproversAcrossTimesheetSettings() {
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		List<Integer> timesheetIds = Arrays.asList(1, 2);

		ContractorTimesheetAndSettingValidatorResponseBodyDto v1 = TimesheetLogsTestDataFactory
			.createContractorTimesheetAndSettingValidatorResponse();
		v1.setTimesheetId(1);
		v1.setTimesheetSettingId(101);
		v1.setWorkLogType(workTimeEnum.ENTER_WORK_TIME.getId());

		ContractorTimesheetAndSettingValidatorResponseBodyDto v2 = TimesheetLogsTestDataFactory
			.createContractorTimesheetAndSettingValidatorResponse();
		v2.setTimesheetId(2);
		v2.setTimesheetSettingId(102);
		v2.setWorkLogType(workTimeEnum.ENTER_WORK_TIME.getId());

		List<ContractorTimesheetAndSettingValidatorResponseBodyDto> validatorDtos = Arrays.asList(v1, v2);

		TimelogWithSettingQueryResultDto row1 = TimesheetLogsTestDataFactory.createTimelogWithSettingQueryResult();
		row1.setId(1);
		row1.setTimesheetId(1);
		row1.setTimesheetSettingId(101);
		TimelogWithSettingQueryResultDto row2 = TimesheetLogsTestDataFactory.createTimelogWithSettingQueryResult();
		row2.setId(2);
		row2.setTimesheetId(2);
		row2.setTimesheetSettingId(102);
		List<TimelogWithSettingQueryResultDto> rows = Arrays.asList(row1, row2);

		TimesheetApprover sharedApprover = TimesheetLogsTestDataFactory.createTimesheetApprover();
		sharedApprover.setEntityId(900);
		sharedApprover.setUserTypeId(3);
		sharedApprover.setTimesheetSettingId(101);
		TimesheetApprover sharedApprover2 = TimesheetLogsTestDataFactory.createTimesheetApprover();
		sharedApprover2.setEntityId(900);
		sharedApprover2.setUserTypeId(3);
		sharedApprover2.setTimesheetSettingId(102);

		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogRepository.findTimeLogsWithSettingDetails(anyList(), eq(accountId))).willReturn(rows);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(101)).willReturn(List.of(sharedApprover));
		given(this.timesheetApproverRepository.findByTimesheetSettingId(102)).willReturn(List.of(sharedApprover2));
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(Collections.emptyList());
		given(this.timeLogIntervalRepository.findByTimeLogIdIn(anyList())).willReturn(Collections.emptyList());
		given(this.timeLogMapper.timeLogQueryResultDtoToResponseBodyDto(anyList())).willAnswer((invocation) -> {
			@SuppressWarnings("unchecked")
			List<TimelogQueryResultDto> in = invocation.getArgument(0);
			List<TimelogResponseBodyDto> out = new ArrayList<>();
			for (TimelogQueryResultDto q : in) {
				TimelogResponseBodyDto dto = TimesheetLogsTestDataFactory.createBulkTimeLogResponse();
				dto.setId(q.getId());
				dto.setTimesheetId(q.getTimesheetId());
				out.add(dto);
			}
			return out;
		});
		given(this.timeLogMapper.mapApprovers(anyList()))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());

		FetchBulkContractorTimelogResultBodyDto result = this.timesheetLogsService
			.getContractorAllTimeLogs(timesheetIds, validatorDtos, Collections.emptyList());

		assertThat(result.getTimesheetSettingsMetaData()).isNotNull();
		assertThat(result.getTimesheetSettingsMetaData().getApprovers()).isNotNull();
		then(this.timeLogMapper).should().mapApprovers(anyList());
	}

	@Test
	@DisplayName("Bulk update: USER isApproved true verifies approver and creates invoice in batch metadata path")
	void testBulkUpdateTimeLogsUserIsApprovedTrueRunsApproverChecksAndInvoiceCreation() {
		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setIsApproved(true);
		request.setSave(false);
		request.setTimeLogs(List.of(TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest()));

		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		TimesheetApproval latest = new TimesheetApproval();
		latest.setTimesheetId(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		latest.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(List.of(latest));

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		Timesheet ts = TimesheetLogsTestDataFactory.createTimesheet();
		given(this.timesheetJpaRepository.findAllById(anyList())).willReturn(List.of(ts));

		TimesheetApprover approver = TimesheetLogsTestDataFactory.createTimesheetApprover();
		approver.setUserTypeId(AccountUserEnum.USERTYPEID.getId());
		approver.setEntityId(userId);
		given(this.timesheetApproverRepository.findByTimesheetSettingIds(anyList())).willReturn(List.of(approver));

		lenient().doNothing().when(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		lenient().doNothing().when(this.timesheetInvoiceService).createTimesheetInvoice(anyInt(), anyInt(), anyInt());
		lenient().doNothing()
			.when(this.timesheetUpdateHelper)
			.batchUpdateTimesheetLastModifiedWithTimeDetails(anyList(), anyInt(), anyInt(), anyList());

		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timesheetJpaRepository).should().findAllById(anyList());
		then(this.timesheetApproverRepository).should().findByTimesheetSettingIds(anyList());
		then(this.timesheetInvoiceService).should(times(1))
			.createTimesheetInvoice(eq(TimesheetLogsTestDataFactory.getDefaultTimesheetId()), eq(userId),
					eq(AccountUserEnum.USERTYPEID.getId()));
	}

	@Test
	@DisplayName("Bulk update: contact persona with save true filters to open timesheets")
	void testBulkUpdateTimeLogsContactPersonaSaveTrueUsesOpenFilter() {
		BulkUpdateTimeLogsRequestBodyDto request = TimesheetLogsTestDataFactory.createBulkUpdateTimeLogsRequest();
		request.setSave(true);
		request.setIsApproved(false);
		request.setJobId(888);

		Integer contactId = 555;
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getContactId()).willReturn(contactId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto permissions = new io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto(
				null, 1, null);
		given(this.portalAccessControlService.validatePortalAccessControl(888, contactId)).willReturn(permissions);

		TimesheetApproval open1 = new TimesheetApproval();
		open1.setTimesheetId(1);
		open1.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.OPEN.getId());
		TimesheetApproval open2 = new TimesheetApproval();
		open2.setTimesheetId(2);
		open2.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.OPEN.getId());
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(List.of(open1, open2));

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());
		given(this.timeLogIntervalRepository.findIntervalsByTimeLogIds(anyList())).willReturn(Map.of());

		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timeLogRepository).should().batchUpsert(anyList());
	}

	@Test
	@DisplayName("Bulk update: metadata-only submit applies time detail totals from request")
	void testBulkUpdateTimeLogsMetadataOnlyWithTimeDetailsDelegatesToHelper() {
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();

		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setTimeLogs(Collections.emptyList());
		request.setTimesheetIdNoLogChanges(List.of(timesheetId));
		request.setSave(false);
		request.setIsApproved(false);
		TimeDetailSummaryDto td = new TimeDetailSummaryDto();
		td.setTimesheetId(timesheetId);
		td.setTotalTime(40);
		td.setTotalWorkTime(38);
		request.setTimeDetails(List.of(td));

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		TimesheetApproval submitted = new TimesheetApproval();
		submitted.setTimesheetId(timesheetId);
		submitted.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(List.of(submitted));

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());

		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(anyInt(), anyInt(), anyInt(), anyInt(), any());
		lenient().doNothing()
			.when(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(anyInt(), anyInt(), anyInt());
		lenient().doNothing().when(this.timesheetUpdateHelper).updateTimesheetTimeDetails(anyInt(), anyInt(), anyInt());

		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.timesheetUpdateHelper).should().updateTimesheetTimeDetails(timesheetId, 40, 38);
	}

	@Test
	@DisplayName("Bulk update: metadata-only skips null timesheet id when invoking rule engine")
	void testBulkUpdateTimeLogsMetadataOnlySkipsNullTimesheetIdForRuleEngine() {
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer userId = TimesheetLogsTestDataFactory.getDefaultUserId();

		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setTimeLogs(Collections.emptyList());
		request.setTimesheetIdNoLogChanges(Arrays.asList(timesheetId, null));
		request.setSave(false);
		request.setIsApproved(false);

		io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal userPrincipal = mock(
				io.recruitcrm.microservice.timesheet.configuration.auth.principal.UserPrincipal.class);
		given(userPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getUnifiedPrincipal()).willReturn(userPrincipal);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(userId);

		TimesheetApproval submitted = new TimesheetApproval();
		submitted.setTimesheetId(timesheetId);
		submitted.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(anyList()))
			.willReturn(List.of(submitted));

		given(this.contractStaffingAccessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class)))
			.willReturn(TimesheetLogsTestDataFactory.createSuccessfulBulkPermissionCheckResult());

		willDoNothing().given(this.timesheetApprovalRepository)
			.createTimesheetApproval(anyInt(), anyInt(), anyInt(), anyInt(), any());
		lenient().doNothing()
			.when(this.timesheetUpdateHelper)
			.updateTimesheetLastModified(anyInt(), anyInt(), anyInt());

		this.timesheetLogsService.bulkUpdateTimeLogs(request);

		then(this.ruleEngineService).should(times(1)).evaluateRules(any(RuleEngineRequestBodyDto.class));
	}

	// ===== Reflection coverage for private branches (unreachable via public API or rare
	// guards) =====

	@Test
	@DisplayName("(Reflection) validateTimesheetsNotApproved: empty or null ids returns without lookup")
	void testValidateTimesheetsNotApprovedReflectionEmptyOrNullIdsNoOp() {
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"validateTimesheetsNotApproved", Collections.<Integer>emptyList(), Boolean.FALSE))
			.doesNotThrowAnyException();
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"validateTimesheetsNotApproved", null, Boolean.FALSE))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("(Reflection) validateTimesheetsNotApproved save=true throws when no approval row")
	void testValidateTimesheetsNotApprovedReflectionSaveTrueMissingApprovalThrows() {
		List<Integer> ids = List.of(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(ids))
			.willReturn(Collections.emptyList());
		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"validateTimesheetsNotApproved", ids, Boolean.TRUE))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("not in open state");
	}

	@Test
	@DisplayName("(Reflection) validateTimesheetsNotApproved save=true succeeds when latest is OPEN")
	void testValidateTimesheetsNotApprovedReflectionSaveTrueOpenOk() {
		List<Integer> ids = List.of(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		TimesheetApproval open = new TimesheetApproval();
		open.setTimesheetId(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		open.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.OPEN.getId());
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(ids)).willReturn(List.of(open));
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"validateTimesheetsNotApproved", ids, Boolean.TRUE))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("(Reflection) validateTimesheetsNotApproved save=true throws when latest is not OPEN")
	void testValidateTimesheetsNotApprovedReflectionSaveTrueSubmittedThrows() {
		List<Integer> ids = List.of(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		TimesheetApproval submitted = new TimesheetApproval();
		submitted.setTimesheetId(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		submitted.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.SUBMITTED.getId());
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(ids))
			.willReturn(List.of(submitted));
		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"validateTimesheetsNotApproved", ids, Boolean.TRUE))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("not in open state");
	}

	@Test
	@DisplayName("(Reflection) invokeRuleEngineIfNeeded: null request, null ids, or empty ids is no-op")
	void testInvokeRuleEngineIfNeededReflectionEarlyExits() {
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		req.setSave(false);
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "invokeRuleEngineIfNeeded",
				null, List.of(1)))
			.doesNotThrowAnyException();
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "invokeRuleEngineIfNeeded",
				req, null))
			.doesNotThrowAnyException();
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "invokeRuleEngineIfNeeded",
				req, Collections.<Integer>emptyList()))
			.doesNotThrowAnyException();
		then(this.ruleEngineService).should(never()).evaluateRules(any(RuleEngineRequestBodyDto.class));
	}

	@Test
	@DisplayName("(Reflection) invokeRuleEngineIfNeeded: save=true skips rule engine")
	void testInvokeRuleEngineIfNeededReflectionSaveTrueSkips() {
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		req.setSave(true);
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "invokeRuleEngineIfNeeded", req,
				List.of(TimesheetLogsTestDataFactory.getDefaultTimesheetId()));
		then(this.ruleEngineService).should(never()).evaluateRules(any(RuleEngineRequestBodyDto.class));
	}

	@Test
	@DisplayName("(Reflection) invokeRuleEngineIfNeeded: wraps rule engine failures")
	void testInvokeRuleEngineIfNeededReflectionWrapsEvaluatorException() {
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		req.setSave(false);
		given(this.ruleEngineService.evaluateRules(any(RuleEngineRequestBodyDto.class)))
			.willThrow(new IllegalStateException("boom"));
		List<Integer> timesheetIdsForRuleEngine = List.of(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "invokeRuleEngineIfNeeded",
				req, timesheetIdsForRuleEngine))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to invoke rule engine");
	}

	@Test
	@DisplayName("(Reflection) updateTimesheetTimeDetailsFromRequest: null, empty, and skipped rows")
	void testUpdateTimesheetTimeDetailsFromRequestReflectionGuards() {
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"updateTimesheetTimeDetailsFromRequest", new Object[] { null }))
			.doesNotThrowAnyException();
		BulkUpdateTimeLogsRequestBodyDto emptyDetails = new BulkUpdateTimeLogsRequestBodyDto();
		emptyDetails.setTimeDetails(Collections.emptyList());
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"updateTimesheetTimeDetailsFromRequest", emptyDetails))
			.doesNotThrowAnyException();
		TimeDetailSummaryDto nullTs = new TimeDetailSummaryDto();
		nullTs.setTimesheetId(null);
		BulkUpdateTimeLogsRequestBodyDto withNullRow = new BulkUpdateTimeLogsRequestBodyDto();
		withNullRow.setTimeDetails(Arrays.asList(null, nullTs));
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"updateTimesheetTimeDetailsFromRequest", withNullRow))
			.doesNotThrowAnyException();
		then(this.timesheetUpdateHelper).should(never()).updateTimesheetTimeDetails(anyInt(), anyInt(), anyInt());
	}

	@Test
	@DisplayName("(Reflection) updateTimesheetMetadata: no valid timesheets returns without bulk approvals")
	void testUpdateTimesheetMetadataReflectionEmptyValidTimesheetIdsNoOp() {
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		req.setSave(false);
		req.setIsApproved(false);
		Map<Integer, List<BulkTimeLogRequestBodyDto>> map = new HashMap<>();
		map.put(null, List.of(TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest()));
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "updateTimesheetMetadata", req,
				map, 1, AccountUserEnum.USERTYPEID.getId()))
			.doesNotThrowAnyException();
		then(this.timesheetApprovalRepository).should(never()).createBulkTimesheetApprovals(anyList());
	}

	@Test
	@DisplayName("(Reflection) validateAndFetchTimeLogs: null or empty payload throws IAE")
	void testValidateAndFetchTimeLogsReflectionInvalidPayload() {
		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "validateAndFetchTimeLogs",
				new Object[] { null }))
			.isInstanceOf(IllegalArgumentException.class);
		BulkUpdateTimeLogsRequestBodyDto empty = new BulkUpdateTimeLogsRequestBodyDto();
		empty.setTimeLogs(Collections.emptyList());
		assertThatThrownBy(
				() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "validateAndFetchTimeLogs", empty))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("(Reflection) validateAndFetchTimeLogs: no log ids after null entries throws IAE")
	void testValidateAndFetchTimeLogsReflectionNoValidIds() {
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		BulkTimeLogRequestBodyDto nullId = new BulkTimeLogRequestBodyDto();
		nullId.setId(null);
		req.setTimeLogs(List.of(nullId));
		assertThatThrownBy(
				() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "validateAndFetchTimeLogs", req))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("(Reflection) validateAndFetchTimeLogs: null time log entry throws ValidationErrorException")
	void testValidateAndFetchTimeLogsReflectionNullTimeLogEntry() {
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		req.setTimeLogs(Arrays.asList(null, TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest()));
		assertThatThrownBy(
				() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "validateAndFetchTimeLogs", req))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("Time log entry cannot be null");
	}

	@Test
	@DisplayName("(Reflection) validateAndFetchTimeLogs: null time log id throws ValidationErrorException")
	void testValidateAndFetchTimeLogsReflectionNullTimeLogId() {
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		BulkTimeLogRequestBodyDto validLog = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		BulkTimeLogRequestBodyDto nullIdLog = new BulkTimeLogRequestBodyDto();
		nullIdLog.setId(null);
		nullIdLog.setTimesheetId(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		req.setTimeLogs(List.of(validLog, nullIdLog));
		assertThatThrownBy(
				() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "validateAndFetchTimeLogs", req))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("Time log ID cannot be null");
	}

	@Test
	@DisplayName("(Reflection) validateAndFetchTimeLogs: null timesheet id throws ValidationErrorException")
	void testValidateAndFetchTimeLogsReflectionNullTimesheetId() {
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		BulkTimeLogRequestBodyDto logDto = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		logDto.setTimesheetId(null);
		req.setTimeLogs(List.of(logDto));
		assertThatThrownBy(
				() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "validateAndFetchTimeLogs", req))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessage("Timesheet ID cannot be null for time log ID: 1");
	}

	@Test
	@DisplayName("(Reflection) validateAndFetchTimeLogs: missing TimeLog entity throws")
	void testValidateAndFetchTimeLogsReflectionMissingEntity() {
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		req.setTimeLogs(List.of(TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest()));
		given(this.timeLogJpaRepository.findAllById(anyIterable())).willReturn(Collections.emptyList());
		assertThatThrownBy(
				() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "validateAndFetchTimeLogs", req))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("(Reflection) formatWorkTimeDetailsAsCommaSeparated: null or empty returns null")
	void testFormatWorkTimeDetailsAsCommaSeparatedReflectionNullOrEmpty() {
		assertThat((String) ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"formatWorkTimeDetailsAsCommaSeparated", new Object[] { null }))
			.isNull();
		assertThat((String) ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"formatWorkTimeDetailsAsCommaSeparated", Collections.<WorkTimeDetailResponseBodyDto>emptyList()))
			.isNull();
	}

	@Test
	@DisplayName("(Reflection) formatWorkTimeDetailsAsCommaSeparated: skips invalid intervals in stream")
	void testFormatWorkTimeDetailsAsCommaSeparatedReflectionFiltersInvalidEntries() {
		WorkTimeDetailResponseBodyDto valid = new WorkTimeDetailResponseBodyDto();
		valid.setWorkStartTime(3600);
		valid.setWorkEndTime(7200);
		WorkTimeDetailResponseBodyDto nullTimes = new WorkTimeDetailResponseBodyDto();
		WorkTimeDetailResponseBodyDto negative = new WorkTimeDetailResponseBodyDto();
		negative.setWorkStartTime(-1);
		negative.setWorkEndTime(100);
		List<WorkTimeDetailResponseBodyDto> details = Arrays.asList(null, nullTimes, negative, valid);
		String formatted = (String) ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"formatWorkTimeDetailsAsCommaSeparated", details);
		assertThat(formatted).isNotNull().contains("01:00").contains("02:00");
	}

	@Test
	@DisplayName("(Reflection) formatSecondsToTime: null or negative returns empty string (Integer overload)")
	void testFormatSecondsToTimeReflectionEdgeCases() throws Exception {
		assertThat((String) ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "formatSecondsToTime",
				new Object[] { null }))
			.isEmpty();
		Method formatSecondsToTimeInteger = TimesheetLogsService.class.getDeclaredMethod("formatSecondsToTime",
				Integer.class);
		formatSecondsToTimeInteger.setAccessible(true);
		assertThat((String) formatSecondsToTimeInteger.invoke(this.timesheetLogsService, -10)).isEmpty();
	}

	@Test
	@DisplayName("(Reflection) filterToOpenTimesheetIds: null or empty returns empty list")
	void testFilterToOpenTimesheetIdsReflectionGuards() {
		@SuppressWarnings("unchecked")
		List<Integer> nullArgResult = ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"filterToOpenTimesheetIds", (Object) null);
		assertThat(nullArgResult).isEmpty();
		@SuppressWarnings("unchecked")
		List<Integer> emptyArgResult = ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"filterToOpenTimesheetIds", Collections.<Integer>emptyList());
		assertThat(emptyArgResult).isEmpty();
	}

	@Test
	@DisplayName("(Reflection) extractTimesheetIds: null request throws IAE")
	void testExtractTimesheetIdsReflectionNullRequest() {
		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "extractTimesheetIds",
				new Object[] { null }))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("(Reflection) getTimesheetIdsFromLogs: null request returns empty list")
	void testGetTimesheetIdsFromLogsReflectionNullRequest() {
		@SuppressWarnings("unchecked")
		List<Integer> out = ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "getTimesheetIdsFromLogs",
				new Object[] { null });
		assertThat(out).isEmpty();
	}

	@Test
	@DisplayName("Get time logs by timesheet ID throws when JPA returns null list")
	void testGetTimeLogsByTimesheetIdJpaReturnsNullThrowsResourceNotFound() {
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(null);

		assertThatThrownBy(() -> this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Time logs for timesheet");
	}

	@Test
	@DisplayName("(Reflection) validateTimesheetsNotApproved save=false throws for approved timesheet")
	void testValidateTimesheetsNotApprovedReflectionSaveFalseApprovedThrows() {
		List<Integer> ids = List.of(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		TimesheetApproval approved = new TimesheetApproval();
		approved.setTimesheetId(TimesheetLogsTestDataFactory.getDefaultTimesheetId());
		approved.setTimesheetApprovalStatusTypeId(ApprovalStatusEnum.APPROVED.getId());
		given(this.timesheetApprovalRepository.findLatestApprovalEntitiesByTimesheetIds(ids))
			.willReturn(List.of(approved));
		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"validateTimesheetsNotApproved", ids, Boolean.FALSE))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("approved timesheet");
	}

	@Test
	@DisplayName("(Reflection) getWorkTimeDetailsMap: start-end with empty breaks still maps interval")
	void testGetWorkTimeDetailsMapReflectionStartEndNoBreaks() {
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setWorkLogType(workTimeEnum.ENTER_START_END_TIME.getId());
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();
		timeLog.setId(502);
		TimeLogInterval interval = new TimeLogInterval();
		interval.setId(11);
		interval.setTimeLogId(502);
		interval.setWorkStartTime(400);
		interval.setWorkEndTime(800);
		interval.setRangeBasedRemark("r");
		interval.setBreakInterval(Collections.emptyList());
		given(this.timeLogIntervalRepository.findByTimeLogIdIn(List.of(502))).willReturn(List.of(interval));

		@SuppressWarnings("unchecked")
		Map<Integer, List<WorkTimeDetailResponseBodyDto>> out = ReflectionTestUtils
			.invokeMethod(this.timesheetLogsService, "getWorkTimeDetailsMap", queryResult, List.of(timeLog));
		assertThat(out).containsKey(502);
		assertThat(out.get(502).get(0).getRangeBasedBreakTime()).isZero();
	}

	@Test
	@DisplayName("(Reflection) getWorkTimeDetailsMapForBulk skips unknown time log or non range type")
	void testGetWorkTimeDetailsMapForBulkReflectionFiltering() {
		TimeLogInterval orphan = new TimeLogInterval();
		orphan.setTimeLogId(9000);
		TimeLogInterval wrongType = new TimeLogInterval();
		wrongType.setTimeLogId(1);
		wrongType.setWorkStartTime(1);
		wrongType.setWorkEndTime(2);
		Map<Integer, Integer> tlToTs = new HashMap<>();
		tlToTs.put(1, 77);
		Map<Integer, Integer> tsWlt = new HashMap<>();
		tsWlt.put(77, workTimeEnum.ENTER_WORK_TIME.getId());
		@SuppressWarnings("unchecked")
		Map<Integer, List<WorkTimeDetailResponseBodyDto>> out = ReflectionTestUtils.invokeMethod(
				this.timesheetLogsService, "getWorkTimeDetailsMapForBulk", List.of(orphan, wrongType), tlToTs, tsWlt);
		assertThat(out).isEmpty();
	}

	@Test
	@DisplayName("(Reflection) populateWorkTimeDetailsOrBreakIntervals sets range and hourly payloads")
	void testPopulateWorkTimeDetailsOrBreakIntervalsReflectionBranches() {
		TimelogResponseBodyDto rangeLog = TimesheetLogsTestDataFactory.createBulkTimeLogResponse();
		rangeLog.setId(800);
		Integer tsId = rangeLog.getTimesheetId();
		Map<Integer, Integer> tlToTs = Map.of(800, tsId);
		Map<Integer, Integer> tsWltRange = Map.of(tsId, workTimeEnum.ENTER_START_END_TIME.getId());
		WorkTimeDetailResponseBodyDto wd = new WorkTimeDetailResponseBodyDto(null, 0, 3600, null, 0,
				Collections.emptyList());
		Map<Integer, List<WorkTimeDetailResponseBodyDto>> wdm = Map.of(800, List.of(wd));
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "populateWorkTimeDetailsOrBreakIntervals",
				List.of(rangeLog), wdm, new HashMap<>(), tlToTs, tsWltRange);
		assertThat(rangeLog.getWorkTimeDetails()).hasSize(1);

		TimelogResponseBodyDto hourLog = TimesheetLogsTestDataFactory.createBulkTimeLogResponse();
		hourLog.setId(801);
		hourLog.setTimesheetId(tsId);
		BreakIntervalResponseBodyDto br = new BreakIntervalResponseBodyDto(1, 801, 10, 20);
		Map<Integer, List<BreakIntervalResponseBodyDto>> bmap = Map.of(801, List.of(br));
		Map<Integer, Integer> tsWltHour = Map.of(tsId, workTimeEnum.ENTER_WORK_TIME.getId());
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "populateWorkTimeDetailsOrBreakIntervals",
				List.of(hourLog), new HashMap<>(), bmap, Map.of(801, tsId), tsWltHour);
		assertThat(hourLog.getBreakIntervals()).hasSize(1);
	}

	@Test
	@DisplayName("(Reflection) setWorkTimeDetails sets hours only when list is non-empty")
	void testSetWorkTimeDetailsReflectionBranches() {
		TimeLogResponseBodyDto dto = TimesheetLogsTestDataFactory.createTimeLogResponse();
		dto.setId(1);
		Map<Integer, List<WorkTimeDetailResponseBodyDto>> map = new HashMap<>();
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "setWorkTimeDetails", dto, 1, map);
		assertThat(dto.getWorkTimeDetails()).isNull();

		map.put(1, Collections.emptyList());
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "setWorkTimeDetails", dto, 1, map);
		assertThat(dto.getWorkTimeDetails()).isNull();

		WorkTimeDetailResponseBodyDto wd = new WorkTimeDetailResponseBodyDto(null, 0, 60, null, 0,
				Collections.emptyList());
		map.put(1, List.of(wd));
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "setWorkTimeDetails", dto, 1, map);
		assertThat(dto.getWorkTimeDetails()).hasSize(1);
		assertThat(dto.getWorkHoursDisplay()).isNotNull();
	}

	@Test
	@DisplayName("(Reflection) fetchTimesheetSettings: invalid arguments throw")
	void testFetchTimesheetSettingsReflectionGuards() {
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		assertThat(catchThrowable(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"fetchTimesheetSettings", null, accountId)))
			.isInstanceOf(IllegalArgumentException.class);
		assertThat(catchThrowable(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"fetchTimesheetSettings", Collections.<Integer>emptyList(), accountId)))
			.isInstanceOf(IllegalArgumentException.class);
		List<Integer> idsWithNull = new ArrayList<>();
		idsWithNull.add(1);
		idsWithNull.add(null);
		assertThat(catchThrowable(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"fetchTimesheetSettings", idsWithNull, accountId)))
			.isInstanceOf(IllegalArgumentException.class);
		assertThat(catchThrowable(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"fetchTimesheetSettings", List.of(1), (Integer) null)))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("(Reflection) fetchTimesheetSettings: missing TimesheetSetting row throws")
	void testFetchTimesheetSettingsReflectionMissingSettingThrows() {
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet ts = new Timesheet();
		ts.setId(4242);
		ts.setTimesheetSettingId(909090);
		given(this.timesheetJpaRepository.findByIdInAndAccountId(List.of(4242), accountId)).willReturn(List.of(ts));
		given(this.timesheetSettingJpaRepository.findByIdInAndAccountId(List.of(909090), accountId))
			.willReturn(Collections.emptyList());
		List<Integer> fetchTimesheetIds = List.of(4242);
		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "fetchTimesheetSettings",
				fetchTimesheetIds, accountId))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("(Reflection) validateAndFetchTimeLogs: timesheet mismatch throws IAE")
	void testValidateAndFetchTimeLogsReflectionTimesheetMismatch() {
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		BulkTimeLogRequestBodyDto logDto = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		logDto.setTimesheetId(99999);
		req.setTimeLogs(List.of(logDto));
		assertThatThrownBy(
				() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "validateAndFetchTimeLogs", req))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("mismatch");
	}

	@Test
	@DisplayName("(Reflection) getTimesheetIdNoLogChanges filters nulls and deduplicates")
	void testGetTimesheetIdNoLogChangesReflectionDistinctNonNull() {
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		req.setTimesheetIdNoLogChanges(Arrays.asList(5, null, 5, 7));
		@SuppressWarnings("unchecked")
		List<Integer> out = ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "getTimesheetIdNoLogChanges",
				req);
		assertThat(out).containsExactly(5, 7);
	}

	@Test
	@DisplayName("(Reflection) unionDistinct preserves order and merges")
	void testUnionDistinctReflection() {
		@SuppressWarnings("unchecked")
		List<Integer> out = ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "unionDistinct", List.of(3, 1),
				List.of(1, 4));
		assertThat(out).containsExactly(3, 1, 4);
	}

	@Test
	@DisplayName("(Reflection) resolveRemarkRequiredTimeLogIds when remark mandatory and not save")
	void testResolveRemarkRequiredTimeLogIdsReflection() {
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		req.setSave(false);
		BulkTimeLogRequestBodyDto logDto = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		req.setTimeLogs(List.of(logDto));
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		setting.setIsRemarkMandatory(1);
		Map<Integer, TimesheetSetting> map = Map.of(logDto.getTimesheetId(), setting);
		@SuppressWarnings("unchecked")
		Set<Integer> ids = ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"resolveRemarkRequiredTimeLogIds", req, map);
		assertThat(ids).contains(logDto.getId());
	}

	@Test
	@DisplayName("(Reflection) collectRangeBasedWorkDetails skips duration-based settings")
	void testCollectRangeBasedWorkDetailsReflectionSkipsDurationType() {
		BulkTimeLogRequestBodyDto logDto = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		req.setTimeLogs(List.of(logDto));
		TimesheetSetting duration = TimesheetLogsTestDataFactory.createTimesheetSettingForWorkTimeEntry();
		Map<Integer, TimesheetSetting> map = Map.of(logDto.getTimesheetId(), duration);
		@SuppressWarnings("unchecked")
		Map<Integer, List<WorkTimeDetailDto>> out = ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"collectRangeBasedWorkDetails", req, map);
		assertThat(out).isEmpty();
	}

	@Test
	@DisplayName("(Reflection) isEnterWorkTimeType distinguishes setting work log type")
	void testIsEnterWorkTimeTypeReflection() {
		Map<Integer, TimesheetSetting> map = new HashMap<>();
		assertThat((Boolean) ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "isEnterWorkTimeType", map, 9))
			.isFalse();
		TimesheetSetting enterWork = TimesheetLogsTestDataFactory.createTimesheetSettingForWorkTimeEntry();
		map.put(4, enterWork);
		assertThat((Boolean) ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "isEnterWorkTimeType", map, 4))
			.isTrue();
	}

	@Test
	@DisplayName("(Reflection) serializeBreakIntervals null detail, empty breaks, and JSON failure")
	void testSerializeBreakIntervalsReflection() throws Exception {
		assertThat((String) ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "serializeBreakIntervals",
				new Object[] { null, 1 }))
			.isNull();
		WorkTimeDetailDto emptyBreaks = new WorkTimeDetailDto();
		emptyBreaks.setBreakIntervals(Collections.emptyList());
		assertThat((String) ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "serializeBreakIntervals",
				emptyBreaks, 1))
			.isNull();

		WorkTimeDetailDto withBreaks = new WorkTimeDetailDto();
		withBreaks.setBreakIntervals(List.of(new BreakIntervalDto()));
		ObjectMapper failingMapper = mock(ObjectMapper.class);
		given(failingMapper.writeValueAsString(any())).willThrow(new JsonProcessingException("fail") {

			private static final long serialVersionUID = 1L;

		});
		ObjectMapper original = (ObjectMapper) ReflectionTestUtils.getField(this.timesheetLogsService, "objectMapper");
		try {
			ReflectionTestUtils.setField(this.timesheetLogsService, "objectMapper", failingMapper);
			final WorkTimeDetailDto breakIntervalsDetail = withBreaks;
			assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
					"serializeBreakIntervals", breakIntervalsDetail, 99))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Failed to serialize break intervals");
		}
		finally {
			ReflectionTestUtils.setField(this.timesheetLogsService, "objectMapper", original);
		}
	}

	@Test
	@DisplayName("(Reflection) collectDeletionMarkers handles null request and collects ids")
	void testCollectDeletionMarkersReflection() {
		@SuppressWarnings("unchecked")
		Set<Integer> empty = ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "collectDeletionMarkers",
				new Object[] { null });
		assertThat(empty).isEmpty();

		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		WorkTimeDetailDto marker = new WorkTimeDetailDto();
		marker.setId(606);
		marker.setWorkStartTime(-1);
		marker.setWorkEndTime(-1);
		BulkTimeLogRequestBodyDto log = new BulkTimeLogRequestBodyDto();
		log.setWorkTimeDetails(List.of(marker));
		req.setTimeLogs(List.of(log));
		@SuppressWarnings("unchecked")
		Set<Integer> ids = ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "collectDeletionMarkers", req);
		assertThat(ids).containsExactly(606);
	}

	@Test
	@DisplayName("(Reflection) deleteIntervals no-op for null or empty; wraps failure")
	void testDeleteIntervalsReflection() {
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "deleteIntervals",
				new Object[] { null }))
			.doesNotThrowAnyException();
		then(this.timeLogIntervalRepository).should(never()).deleteByIdIn(anyList());

		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "deleteIntervals",
				Collections.<Integer>emptySet()))
			.doesNotThrowAnyException();

		willThrow(new RuntimeException("db")).given(this.timeLogIntervalRepository).deleteByIdIn(anyList());
		java.util.Set<Integer> intervalIdsToDelete = Set.of(1);
		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "deleteIntervals",
				intervalIdsToDelete))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to delete time log intervals");
		lenient().doNothing().when(this.timeLogIntervalRepository).deleteByIdIn(anyList());
	}

	@Test
	@DisplayName("(Reflection) upsertTimeLogs and upsertIntervals empty guards and failures")
	void testUpsertBatchReflectionFailures() {
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "upsertTimeLogs",
				new Object[] { null }))
			.doesNotThrowAnyException();
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "upsertTimeLogs",
				Collections.emptyList()))
			.doesNotThrowAnyException();
		then(this.timeLogRepository).should(never()).batchUpsert(anyList());

		TimeLogUpsertDto upsert = new TimeLogUpsertDto(1, 1, 1, 1, null, null, null, null, null);
		willThrow(new RuntimeException("u")).given(this.timeLogRepository).batchUpsert(anyList());
		List<TimeLogUpsertDto> upsertBatch = List.of(upsert);
		assertThatThrownBy(
				() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "upsertTimeLogs", upsertBatch))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to batch upsert time logs");
		Mockito.reset(this.timeLogRepository);
		lenient().when(this.timeLogRepository.batchUpsert(anyList())).thenReturn(1);

		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "upsertIntervals",
				new Object[] { null }))
			.doesNotThrowAnyException();
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "upsertIntervals",
				Collections.emptyList()))
			.doesNotThrowAnyException();
		then(this.timeLogIntervalRepository).should(never()).batchUpsert(anyList());

		TimeLogIntervalUpsertDto intervalUpsert = new TimeLogIntervalUpsertDto(1, 1, 0, 60, null, null);
		willThrow(new RuntimeException("v")).given(this.timeLogIntervalRepository).batchUpsert(anyList());
		List<TimeLogIntervalUpsertDto> intervalUpsertBatch = List.of(intervalUpsert);
		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "upsertIntervals",
				intervalUpsertBatch))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to batch upsert time log intervals");
		Mockito.reset(this.timeLogIntervalRepository);
		lenient().when(this.timeLogIntervalRepository.batchUpsert(anyList())).thenReturn(1);
		lenient().doNothing().when(this.timeLogIntervalRepository).deleteByIdIn(anyList());
	}

	@Test
	@DisplayName("(Reflection) prepareTimeLogUpsertValues normalizes -1 and empty remark")
	void testPrepareTimeLogUpsertValuesReflection() {
		BulkTimeLogRequestBodyDto logDto = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		logDto.setRemark("");
		logDto.setBreakTime(-1);
		logDto.setOverTime(-1);
		logDto.setTotalTime(-1);
		logDto.setWorkTime(-1);
		TimeLog timeLog = TimesheetLogsTestDataFactory.createTimeLog();
		timeLog.setWorkTime(8);
		TimesheetSetting setting = TimesheetLogsTestDataFactory.createTimesheetSettingForWorkTimeEntry();
		TimeLogUpsertDto dto = ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "prepareTimeLogUpsertValues",
				logDto, timeLog, setting);
		assertThat(dto)
			.extracting(TimeLogUpsertDto::getRemark, TimeLogUpsertDto::getBreakTime, TimeLogUpsertDto::getOverTime,
					TimeLogUpsertDto::getTotalTime, TimeLogUpsertDto::getWorkTime)
			.containsExactly(null, null, null, null, null);
	}

	@Test
	@DisplayName("(Reflection) checkForOverlaps allows non-overlapping intervals")
	void testCheckForOverlapsReflectionNoOverlap() {
		List<int[]> intervals = List.of(new int[] { 0, 1 }, new int[] { 2, 3 });
		assertThatCode(
				() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "checkForOverlaps", 1, intervals))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("(Reflection) resolveIntervalTimeRange returns null when update has partial times")
	void testResolveIntervalTimeRangeReflectionPartialUpdate() {
		TimeLogIntervalDto existing = TimeLogIntervalDto.builder()
			.id(10)
			.timeLogId(1)
			.workStartTime(100)
			.workEndTime(200)
			.build();
		Map<Integer, WorkTimeDetailDto> updates = new HashMap<>();
		WorkTimeDetailDto partial = new WorkTimeDetailDto();
		partial.setId(10);
		partial.setWorkStartTime(null);
		partial.setWorkEndTime(300);
		updates.put(10, partial);
		Object resolved = ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "resolveIntervalTimeRange",
				existing, updates);
		assertThat(resolved).isNull();
	}

	@Test
	@DisplayName("(Execution) invokeRuleEngineIfNeeded calls engine twice when list has null gap")
	void testInvokeRuleEngineIfNeededSkipsNullTimesheetIdsVerifyTwice() {
		org.mockito.Mockito.clearInvocations(this.ruleEngineService);
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		req.setSave(false);
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "invokeRuleEngineIfNeeded", req,
				Arrays.asList(1, null, 2));
		then(this.ruleEngineService).should(times(2)).evaluateRules(any(RuleEngineRequestBodyDto.class));
	}

	@Test
	@DisplayName("(Reflection) processWorkTimeDetails skips duration type and persists range rows")
	void testProcessWorkTimeDetailsReflection() {
		BatchOperationData batchDuration = new BatchOperationData();
		BulkTimeLogRequestBodyDto logDto = TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest();
		TimeLog tl = TimesheetLogsTestDataFactory.createTimeLog();
		TimesheetSetting durationSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForWorkTimeEntry();
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "processWorkTimeDetails", logDto, tl,
				durationSetting, batchDuration);
		assertThat(batchDuration.getIntervalUpsertValues()).isEmpty();

		BatchOperationData batchRange = new BatchOperationData();
		TimesheetSetting rangeSetting = TimesheetLogsTestDataFactory.createTimesheetSettingForStartEndTimeEntry();
		WorkTimeDetailDto row = new WorkTimeDetailDto();
		row.setWorkStartTime(10);
		row.setWorkEndTime(20);
		row.setBreakIntervals(Collections.emptyList());
		logDto.setWorkTimeDetails(List.of(row));
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "processWorkTimeDetails", logDto, tl, rangeSetting,
				batchRange);
		assertThat(batchRange.getTimeLogIdsWithIntervals()).contains(tl.getId());
		assertThat(batchRange.getIntervalUpsertValues()).isNotEmpty();
	}

	@Test
	@DisplayName("(Reflection) validateSingleIntervalBreakBounds skips when work bounds null")
	void testValidateSingleIntervalBreakBoundsReflectionEarlyReturn() {
		WorkTimeDetailDto detail = new WorkTimeDetailDto();
		detail.setWorkStartTime(null);
		detail.setWorkEndTime(100);
		BreakIntervalDto bi = new BreakIntervalDto();
		bi.setBreakStartTime(10);
		bi.setBreakEndTime(20);
		detail.setBreakIntervals(List.of(bi));
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"validateSingleIntervalBreakBounds", 1, detail))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("(Reflection) updateTimesheetMetadata creates approvals when valid ids present")
	void testUpdateTimesheetMetadataReflectionHappyPath() {
		org.mockito.Mockito.clearInvocations(this.timesheetApprovalRepository, this.timesheetUpdateHelper);
		BulkUpdateTimeLogsRequestBodyDto req = new BulkUpdateTimeLogsRequestBodyDto();
		req.setSave(true);
		req.setIsApproved(false);
		Map<Integer, List<BulkTimeLogRequestBodyDto>> byTs = new HashMap<>();
		byTs.put(TimesheetLogsTestDataFactory.getDefaultTimesheetId(),
				List.of(TimesheetLogsTestDataFactory.createIndividualBulkTimeLogRequest()));
		willDoNothing().given(this.timesheetApprovalRepository).createBulkTimesheetApprovals(anyList());
		willDoNothing().given(this.timesheetUpdateHelper)
			.batchUpdateTimesheetLastModifiedWithTimeDetails(anyList(), anyInt(), anyInt(), anyList());
		assertThatCode(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "updateTimesheetMetadata", req,
				byTs, TimesheetLogsTestDataFactory.getDefaultUserId(), AccountUserEnum.USERTYPEID.getId()))
			.doesNotThrowAnyException();
		then(this.timesheetApprovalRepository).should().createBulkTimesheetApprovals(anyList());
	}

	@Test
	@DisplayName("(Reflection) formatSecondsToTime int overload formats non-negative seconds")
	void testFormatSecondsToTimePrimitiveOverloadReflection() throws Exception {
		Method intOverload = TimesheetLogsService.class.getDeclaredMethod("formatSecondsToTime", int.class);
		intOverload.setAccessible(true);
		assertThat((String) intOverload.invoke(this.timesheetLogsService, 3665)).isEqualTo("01:01");
	}

	// ===== Tests for setWeeklyOvertimeEnabled / hasWeeklyOvertimeRule (lines 496-497,
	// 587, 598, 2441, 2486, 2516) =====

	@Test
	@DisplayName("Staff portal: SUBMITTED status with RANGE_BASED_WEEKLY_OVERTIME rule should set isWeeklyEnabled true")
	void testGetTimeLogsByTimesheetIdSubmittedStatusWithRangeBasedWeeklyOvertimeRuleSetsIsWeeklyEnabledTrue() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		CustomRule weeklyRule = new CustomRule();
		weeklyRule.setId(1);
		weeklyRule.setRuleType(5); // RANGE_BASED_WEEKLY_OVERTIME
		queryResult.setCustomRules(Arrays.asList(weeklyRule));
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId))).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isTrue();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.SUBMITTED.getId());
	}

	@Test
	@DisplayName("Staff portal: REJECTED status with RANGE_BASED_WEEKLY_OVERTIME rule should set isWeeklyEnabled true")
	void testGetTimeLogsByTimesheetIdRejectedStatusWithRangeBasedWeeklyOvertimeRuleSetsIsWeeklyEnabledTrue() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithRejectedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		CustomRule weeklyRule = new CustomRule();
		weeklyRule.setId(1);
		weeklyRule.setRuleType(5); // RANGE_BASED_WEEKLY_OVERTIME
		queryResult.setCustomRules(Arrays.asList(weeklyRule));
		queryResult.setApprovalStatusId(ApprovalStatusEnum.REJECTED.getId());

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId))).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isTrue();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.REJECTED.getId());
	}

	@Test
	@DisplayName("Staff portal: SUBMITTED status with DURATION_BASED_WEEKLY_OVERTIME rule should set isWeeklyEnabled true")
	void testGetTimeLogsByTimesheetIdSubmittedStatusWithDurationBasedWeeklyOvertimeRuleSetsIsWeeklyEnabledTrue() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithSubmittedStatus();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();
		List<TimeLogWorkSummaryQueryResultDto> workSummary = TimesheetLogsTestDataFactory
			.createTimeLogWorkSummaryQueryResult();

		CustomRule durationWeeklyRule = new CustomRule();
		durationWeeklyRule.setId(2);
		durationWeeklyRule.setRuleType(8); // DURATION_BASED_WEEKLY_OVERTIME
		queryResult.setCustomRules(Arrays.asList(durationWeeklyRule));
		queryResult.setApprovalStatusId(ApprovalStatusEnum.SUBMITTED.getId());

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());
		given(this.userRepository.getUserDetails(queryResult.getEntityId()))
			.willReturn(TimesheetLogsTestDataFactory.createUserDetailsQueryResult());
		given(this.timeLogJpaRepository.getTimeLogWorkSummaries(Arrays.asList(timesheetId))).willReturn(workSummary);

		// When
		TimesheetResponseBodyDto result = this.timesheetLogsService.getTimeLogsByTimesheetId(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isTrue();
		assertThat(result.getApprovalStatusId()).isEqualTo(ApprovalStatusEnum.SUBMITTED.getId());
	}

	@Test
	@DisplayName("Contractor portal: RANGE_BASED_WEEKLY_OVERTIME rule should set isWeeklyEnabled true (covers line 2441, 2486)")
	void testGetPortalTimeLogsContractorPersonaWithRangeBasedWeeklyOvertimeRuleSetsIsWeeklyEnabledTrue() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 1;

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithOpenStatus();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();

		CustomRule weeklyRule = new CustomRule();
		weeklyRule.setId(1);
		weeklyRule.setRuleType(5); // RANGE_BASED_WEEKLY_OVERTIME
		queryResult.setCustomRules(Arrays.asList(weeklyRule));

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isTrue();
	}

	@Test
	@DisplayName("Contractor portal: DURATION_BASED_WEEKLY_OVERTIME rule should set isWeeklyEnabled true (covers line 2441, 2486)")
	void testGetPortalTimeLogsContractorPersonaWithDurationBasedWeeklyOvertimeRuleSetsIsWeeklyEnabledTrue() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 1;

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithOpenStatus();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();

		CustomRule durationWeeklyRule = new CustomRule();
		durationWeeklyRule.setId(2);
		durationWeeklyRule.setRuleType(8); // DURATION_BASED_WEEKLY_OVERTIME
		queryResult.setCustomRules(Arrays.asList(durationWeeklyRule));

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isTrue();
	}

	@Test
	@DisplayName("Contractor portal: null custom rules should set isWeeklyEnabled false (covers line 2441, 2486 null branch)")
	void testGetPortalTimeLogsContractorPersonaWithNullCustomRulesSetsIsWeeklyEnabledFalse() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 1;

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		queryResult.setCustomRules(null);
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithOpenStatus();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isFalse();
	}

	@Test
	@DisplayName("Contractor portal: non-weekly custom rule should set isWeeklyEnabled false (covers line 2441 loop false path)")
	void testGetPortalTimeLogsContractorPersonaWithNonWeeklyCustomRuleSetsIsWeeklyEnabledFalse() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer contractorId = 1;

		ContractorPrincipal contractorPrincipal = mock(ContractorPrincipal.class);
		given(contractorPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);
		given(contractorPrincipal.getCandidateId()).willReturn(contractorId);
		given(contractorPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contractorPrincipal);

		io.recruitcrm.entity.model.Candidate candidate = new io.recruitcrm.entity.model.Candidate();
		candidate.setId(contractorId);
		given(this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId)).willReturn(candidate);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory.createTimesheetLogQueryResult();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithOpenStatus();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();

		CustomRule nonWeeklyRule = new CustomRule();
		nonWeeklyRule.setId(1);
		nonWeeklyRule.setRuleType(4); // RANGE_BASED_DAILY_OVERTIME — not a weekly
										// overtime rule
		queryResult.setCustomRules(Arrays.asList(nonWeeklyRule));

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isFalse();
	}

	@Test
	@DisplayName("Client portal: RANGE_BASED_WEEKLY_OVERTIME rule should set isWeeklyEnabled true (covers line 2516, 2486)")
	void testGetPortalTimeLogsClientPersonaWithRangeBasedWeeklyOvertimeRuleSetsIsWeeklyEnabledTrue() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithOpenStatus();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		CustomRule weeklyRule = new CustomRule();
		weeklyRule.setId(1);
		weeklyRule.setRuleType(5); // RANGE_BASED_WEEKLY_OVERTIME
		queryResult.setCustomRules(Arrays.asList(weeklyRule));

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isTrue();
	}

	@Test
	@DisplayName("Client portal: DURATION_BASED_WEEKLY_OVERTIME rule should set isWeeklyEnabled true (covers line 2516, 2486)")
	void testGetPortalTimeLogsClientPersonaWithDurationBasedWeeklyOvertimeRuleSetsIsWeeklyEnabledTrue() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithOpenStatus();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		CustomRule durationWeeklyRule = new CustomRule();
		durationWeeklyRule.setId(2);
		durationWeeklyRule.setRuleType(8); // DURATION_BASED_WEEKLY_OVERTIME
		queryResult.setCustomRules(Arrays.asList(durationWeeklyRule));

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isTrue();
	}

	@Test
	@DisplayName("Client portal: non-weekly custom rule should set isWeeklyEnabled false (covers line 2516 loop false path)")
	void testGetPortalTimeLogsClientPersonaWithNonWeeklyCustomRuleSetsIsWeeklyEnabledFalse() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		Integer companyId = 100;

		ContactPrincipal contactPrincipal = mock(ContactPrincipal.class);
		given(contactPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);
		given(contactPrincipal.getCompanyId()).willReturn(companyId);
		given(contactPrincipal.getOrganizationIdentifier()).willReturn(accountId);
		given(this.auth.getUnifiedPrincipal()).willReturn(contactPrincipal);

		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		TimesheetLogQueryResultDto queryResult = TimesheetLogsTestDataFactory
			.createTimesheetLogQueryResultForCompanyContact();
		TimesheetApproval approval = TimesheetLogsTestDataFactory.createTimesheetApprovalWithOpenStatus();
		List<TimeLog> timeLogs = TimesheetLogsTestDataFactory.createTimeLogList();
		List<TimesheetApprover> approvers = TimesheetLogsTestDataFactory.createTimesheetApproverList();

		CustomRule nonWeeklyRule = new CustomRule();
		nonWeeklyRule.setId(1);
		nonWeeklyRule.setRuleType(4); // RANGE_BASED_DAILY_OVERTIME — not a weekly
										// overtime rule
		queryResult.setCustomRules(Arrays.asList(nonWeeklyRule));

		given(this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timeLogRepository.getTimeLogByTimesheetId(timesheetId)).willReturn(queryResult);
		given(this.timeLogJpaRepository.findByTimesheetId(timesheetId)).willReturn(timeLogs);
		given(this.timesheetApprovalJpaRepository.findFirstByTimesheetIdOrderByIdDesc(timesheetId))
			.willReturn(approval);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(queryResult.getTimesheetSettingId()))
			.willReturn(approvers);
		given(this.timeLogMapper.mapApprovers(approvers))
			.willReturn(TimesheetLogsTestDataFactory.createApproverResponse());
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.timeLogMapper.toDto(any(TimeLog.class)))
			.willReturn(TimesheetLogsTestDataFactory.createTimeLogResponse());

		// When
		PortalTimesheetResponseBodyDto result = this.timesheetLogsService.getPortalTimeLogs(timesheetId);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getIsWeeklyEnabled()).isFalse();
	}

	@Test
	@DisplayName("(Reflection) triggerBulkTimesheetNotifications should publish approved reminder when isApproved is true")
	void testTriggerBulkTimesheetNotificationsApprovedPublishesApprovedReminderViaReflection() throws Exception {
		// Given
		BulkUpdateTimeLogsRequestBodyDto requestDto = new BulkUpdateTimeLogsRequestBodyDto();
		requestDto.setIsApproved(true);
		requestDto.setSave(false);
		Method method = TimesheetLogsService.class.getDeclaredMethod("triggerBulkTimesheetNotifications",
				BulkUpdateTimeLogsRequestBodyDto.class, List.class, List.class, Integer.class, Integer.class,
				String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetLogsService, requestDto, List.of(1, 2), Collections.emptyList(),
				TimesheetLogsTestDataFactory.getDefaultAccountId(), AccountUserEnum.USERTYPEID.getId(), "Approver");

		// Then
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("(Reflection) triggerBulkTimesheetNotifications should publish submitted reminder when not approved and not save")
	void testTriggerBulkTimesheetNotificationsSubmittedPublishesSubmittedReminderViaReflection() throws Exception {
		// Given
		BulkUpdateTimeLogsRequestBodyDto requestDto = new BulkUpdateTimeLogsRequestBodyDto();
		requestDto.setIsApproved(false);
		requestDto.setSave(false);
		Method method = TimesheetLogsService.class.getDeclaredMethod("triggerBulkTimesheetNotifications",
				BulkUpdateTimeLogsRequestBodyDto.class, List.class, List.class, Integer.class, Integer.class,
				String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetLogsService, requestDto, Collections.emptyList(), List.of(3),
				TimesheetLogsTestDataFactory.getDefaultAccountId(), AccountUserEnum.USERTYPEID.getId(), "Submitter");

		// Then
		then(this.kafkaProducerHelper).should()
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("(Reflection) triggerBulkTimesheetNotifications should skip submitted reminder when save is true")
	void testTriggerBulkTimesheetNotificationsSaveTrueSkipsSubmittedReminderViaReflection() throws Exception {
		// Given
		BulkUpdateTimeLogsRequestBodyDto requestDto = new BulkUpdateTimeLogsRequestBodyDto();
		requestDto.setIsApproved(false);
		requestDto.setSave(true);
		Method method = TimesheetLogsService.class.getDeclaredMethod("triggerBulkTimesheetNotifications",
				BulkUpdateTimeLogsRequestBodyDto.class, List.class, List.class, Integer.class, Integer.class,
				String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetLogsService, requestDto, List.of(4), Collections.emptyList(),
				TimesheetLogsTestDataFactory.getDefaultAccountId(), AccountUserEnum.USERTYPEID.getId(), "Submitter");

		// Then
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("(Reflection) triggerBulkTimesheetNotifications should return early when merged ids are empty")
	void testTriggerBulkTimesheetNotificationsEmptyMergedIdsReturnsEarlyViaReflection() throws Exception {
		// Given
		BulkUpdateTimeLogsRequestBodyDto requestDto = new BulkUpdateTimeLogsRequestBodyDto();
		requestDto.setIsApproved(false);
		requestDto.setSave(false);
		Method method = TimesheetLogsService.class.getDeclaredMethod("triggerBulkTimesheetNotifications",
				BulkUpdateTimeLogsRequestBodyDto.class, List.class, List.class, Integer.class, Integer.class,
				String.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetLogsService, requestDto, Collections.emptyList(), Collections.emptyList(),
				TimesheetLogsTestDataFactory.getDefaultAccountId(), AccountUserEnum.USERTYPEID.getId(), "Submitter");

		// Then
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("(Reflection) publishTimesheetNotification should return early when timesheet ids are null")
	void testPublishTimesheetNotificationNullIdsReturnsEarlyViaReflection() throws Exception {
		// Given
		Method method = TimesheetLogsService.class.getDeclaredMethod("publishTimesheetNotification", List.class,
				Integer.class, Integer.class, String.class, String.class,
				TimesheetReminderNotificationChannelsDto.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetLogsService, null, TimesheetLogsTestDataFactory.getDefaultAccountId(),
				AccountUserEnum.USERTYPEID.getId(),
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_SUBMITTED, "Submitter",
				TimesheetReminderNotificationChannelsDto.SUBMITTED);

		// Then
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

	@Test
	@DisplayName("(Reflection) publishTimesheetNotification should return early when timesheet ids are empty")
	void testPublishTimesheetNotificationEmptyIdsReturnsEarlyViaReflection() throws Exception {
		// Given
		Method method = TimesheetLogsService.class.getDeclaredMethod("publishTimesheetNotification", List.class,
				Integer.class, Integer.class, String.class, String.class,
				TimesheetReminderNotificationChannelsDto.class);
		method.setAccessible(true);

		// When
		method.invoke(this.timesheetLogsService, Collections.emptyList(),
				TimesheetLogsTestDataFactory.getDefaultAccountId(), AccountUserEnum.USERTYPEID.getId(),
				TimesheetReminderNotificationPayloadDto.EVENT_NAME_TIMESHEET_SUBMITTED, "Submitter",
				TimesheetReminderNotificationChannelsDto.SUBMITTED);

		// Then
		then(this.kafkaProducerHelper).should(never())
			.sendTimesheetReminderNotification(
					org.mockito.ArgumentMatchers.any(TimesheetReminderNotificationPayloadDto.class));
	}

	// ===== Reflection coverage: updateTimesheetMetadata /
	// appendApprovalEntriesForApprovedTimesheets /
	// isAuthorizedApprover =====

	private Map<Integer, List<BulkTimeLogRequestBodyDto>> timeLogsMapWithIds(List<Integer> timesheetIds) {
		Map<Integer, List<BulkTimeLogRequestBodyDto>> map = new LinkedHashMap<>();
		for (Integer id : timesheetIds) {
			map.put(id, List.of(new BulkTimeLogRequestBodyDto()));
		}
		return map;
	}

	@Test
	@DisplayName("(Reflection) updateTimesheetMetadata returns early when no valid timesheet ids are present")
	void testUpdateTimesheetMetadataReflectionEmptyValidIdsReturnsEarly() {
		// Given
		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setSave(true);
		Map<Integer, List<BulkTimeLogRequestBodyDto>> emptyValues = new LinkedHashMap<>();
		emptyValues.put(1, Collections.emptyList());
		emptyValues.put(null, List.of(new BulkTimeLogRequestBodyDto()));

		// When
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "updateTimesheetMetadata", request, emptyValues, 10,
				UserTypeEnum.CONTRACTOR.getId());

		// Then
		then(this.timesheetApprovalRepository).should(never()).createBulkTimesheetApprovals(anyList());
		then(this.timesheetUpdateHelper).should(never())
			.batchUpdateTimesheetLastModifiedWithTimeDetails(anyList(), anyInt(), anyInt(), anyList());
	}

	@Test
	@DisplayName("(Reflection) updateTimesheetMetadata not-approved path persists submitted approvals only")
	void testUpdateTimesheetMetadataReflectionNotApprovedPersistsApprovals() {
		// Given
		Integer timesheetId = 500;
		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setSave(false);
		request.setIsApproved(false);
		Map<Integer, List<BulkTimeLogRequestBodyDto>> values = this.timeLogsMapWithIds(List.of(timesheetId));

		// When
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "updateTimesheetMetadata", request, values, 10,
				UserTypeEnum.CONTRACTOR.getId());

		// Then
		then(this.timesheetApprovalRepository).should(times(1)).createBulkTimesheetApprovals(anyList());
		then(this.timesheetUpdateHelper).should(times(1))
			.batchUpdateTimesheetLastModifiedWithTimeDetails(eq(List.of(timesheetId)), eq(10),
					eq(UserTypeEnum.CONTRACTOR.getId()), anyList());
		then(this.timesheetInvoiceService).should(never()).createTimesheetInvoice(anyInt(), anyInt(), anyInt());
	}

	@Test
	@DisplayName("(Reflection) updateTimesheetMetadata approved path appends approvals and creates invoices")
	void testUpdateTimesheetMetadataReflectionApprovedCreatesInvoices() {
		// Given
		Integer timesheetId = 600;
		Integer userId = 42;
		Integer userTypeId = UserTypeEnum.AGENCY_RECRUITER.getId();
		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setSave(false);
		request.setIsApproved(true);
		Map<Integer, List<BulkTimeLogRequestBodyDto>> values = this.timeLogsMapWithIds(List.of(timesheetId));

		Timesheet timesheet = new Timesheet();
		timesheet.setId(timesheetId);
		timesheet.setTimesheetSettingId(70);

		TimesheetApprover approver = new TimesheetApprover();
		approver.setTimesheetSettingId(70);
		approver.setEntityId(userId);
		approver.setUserTypeId(userTypeId);

		given(this.timesheetJpaRepository.findAllById(List.of(timesheetId))).willReturn(List.of(timesheet));
		given(this.timesheetApproverRepository.findByTimesheetSettingIds(List.of(70))).willReturn(List.of(approver));

		// When
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "updateTimesheetMetadata", request, values, userId,
				userTypeId);

		// Then
		then(this.timesheetApprovalRepository).should(times(1)).createBulkTimesheetApprovals(anyList());
		then(this.timesheetInvoiceService).should(times(1)).createTimesheetInvoice(timesheetId, userId, userTypeId);
	}

	@Test
	@DisplayName("(Reflection) updateTimesheetMetadata wraps downstream failures in IllegalStateException")
	void testUpdateTimesheetMetadataReflectionWrapsFailures() {
		// Given
		Integer timesheetId = 700;
		BulkUpdateTimeLogsRequestBodyDto request = new BulkUpdateTimeLogsRequestBodyDto();
		request.setSave(false);
		request.setIsApproved(false);
		Map<Integer, List<BulkTimeLogRequestBodyDto>> values = this.timeLogsMapWithIds(List.of(timesheetId));
		willThrow(new RuntimeException("db down")).given(this.timesheetApprovalRepository)
			.createBulkTimesheetApprovals(anyList());

		// When & Then
		Integer contractorUserTypeId = UserTypeEnum.CONTRACTOR.getId();
		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "updateTimesheetMetadata",
				request, values, 10, contractorUserTypeId))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Failed to update timesheet metadata");
	}

	@Test
	@DisplayName("(Reflection) appendApprovalEntriesForApprovedTimesheets adds approved entries for company contact approver")
	void testAppendApprovalEntriesReflectionCompanyContactApproverAddsEntry() {
		// Given
		Integer timesheetId = 800;
		Integer userId = 11;
		Integer userTypeId = UserTypeEnum.COMPANY_CONTACT.getId();
		Integer registeredContactId = 999;

		Timesheet timesheet = new Timesheet();
		timesheet.setId(timesheetId);
		timesheet.setTimesheetSettingId(90);

		TimesheetApprover approver = new TimesheetApprover();
		approver.setTimesheetSettingId(90);
		approver.setEntityId(registeredContactId);
		approver.setUserTypeId(userTypeId);

		given(this.timesheetJpaRepository.findAllById(List.of(timesheetId))).willReturn(List.of(timesheet));
		given(this.timesheetApproverRepository.findByTimesheetSettingIds(List.of(90))).willReturn(List.of(approver));
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(TimesheetLogsTestDataFactory.getDefaultAccountId());
		given(this.portalAccessControlService.resolveContactIds(TimesheetLogsTestDataFactory.getDefaultAccountId()))
			.willReturn(List.of(registeredContactId));

		List<TimesheetApproval> approvalsToSave = new ArrayList<>();

		// When
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "appendApprovalEntriesForApprovedTimesheets",
				approvalsToSave, List.of(timesheetId), userId, userTypeId, 1700000000);

		// Then
		assertThat(approvalsToSave).hasSize(1);
		assertThat(approvalsToSave.get(0).getTimesheetId()).isEqualTo(timesheetId);
		assertThat(approvalsToSave.get(0).getTimesheetApprovalStatusTypeId())
			.isEqualTo(ApprovalStatusEnum.APPROVED.getId());
	}

	@Test
	@DisplayName("(Reflection) appendApprovalEntriesForApprovedTimesheets throws when contact is not a registered approver")
	void testAppendApprovalEntriesReflectionCompanyContactNotApproverThrows() {
		// Given
		Integer timesheetId = 810;
		Integer userTypeId = UserTypeEnum.COMPANY_CONTACT.getId();

		Timesheet timesheet = new Timesheet();
		timesheet.setId(timesheetId);
		timesheet.setTimesheetSettingId(91);

		TimesheetApprover approver = new TimesheetApprover();
		approver.setTimesheetSettingId(91);
		approver.setEntityId(555);
		approver.setUserTypeId(userTypeId);

		given(this.timesheetJpaRepository.findAllById(List.of(timesheetId))).willReturn(List.of(timesheet));
		given(this.timesheetApproverRepository.findByTimesheetSettingIds(List.of(91))).willReturn(List.of(approver));
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier())
			.willReturn(TimesheetLogsTestDataFactory.getDefaultAccountId());
		given(this.portalAccessControlService.resolveContactIds(TimesheetLogsTestDataFactory.getDefaultAccountId()))
			.willReturn(List.of(111, 222));

		List<TimesheetApproval> approvalsToSave = new ArrayList<>();
		List<Integer> validTimesheetIds = List.of(timesheetId);

		// When & Then
		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"appendApprovalEntriesForApprovedTimesheets", approvalsToSave, validTimesheetIds, 11, userTypeId,
				1700000000))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("User is not authorized to approve this timesheet");
	}

	@Test
	@DisplayName("(Reflection) appendApprovalEntriesForApprovedTimesheets uses userId match for non-company-contact persona")
	void testAppendApprovalEntriesReflectionNonCompanyContactUsesUserId() {
		// Given
		Integer timesheetId = 820;
		Integer userId = 33;
		Integer userTypeId = UserTypeEnum.AGENCY_RECRUITER.getId();

		Timesheet timesheet = new Timesheet();
		timesheet.setId(timesheetId);
		timesheet.setTimesheetSettingId(92);

		TimesheetApprover approver = new TimesheetApprover();
		approver.setTimesheetSettingId(92);
		approver.setEntityId(userId);
		approver.setUserTypeId(userTypeId);

		given(this.timesheetJpaRepository.findAllById(List.of(timesheetId))).willReturn(List.of(timesheet));
		given(this.timesheetApproverRepository.findByTimesheetSettingIds(List.of(92))).willReturn(List.of(approver));

		List<TimesheetApproval> approvalsToSave = new ArrayList<>();

		// When
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "appendApprovalEntriesForApprovedTimesheets",
				approvalsToSave, List.of(timesheetId), userId, userTypeId, 1700000000);

		// Then
		assertThat(approvalsToSave).hasSize(1);
		then(this.portalAccessControlService).should(never()).resolveContactIds(anyInt());
	}

	@Test
	@DisplayName("(Reflection) isAuthorizedApprover returns true via contact ids when approverContactIds provided")
	void testIsAuthorizedApproverReflectionContactIdsMatch() {
		// Given
		TimesheetApprover approver = new TimesheetApprover();
		approver.setEntityId(999);
		approver.setUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());

		// When
		Boolean result = ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "isAuthorizedApprover",
				List.of(approver), List.of(999), 11, UserTypeEnum.COMPANY_CONTACT.getId());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("(Reflection) isAuthorizedApprover returns false via contact ids when no contact id matches")
	void testIsAuthorizedApproverReflectionContactIdsNoMatch() {
		// Given
		TimesheetApprover approver = new TimesheetApprover();
		approver.setEntityId(999);
		approver.setUserTypeId(UserTypeEnum.COMPANY_CONTACT.getId());

		// When
		Boolean result = ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "isAuthorizedApprover",
				List.of(approver), List.of(111), 11, UserTypeEnum.COMPANY_CONTACT.getId());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("(Reflection) isAuthorizedApprover returns true via userId when approverContactIds is null")
	void testIsAuthorizedApproverReflectionUserIdMatch() {
		// Given
		TimesheetApprover approver = new TimesheetApprover();
		approver.setEntityId(33);
		approver.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());

		// When
		Boolean result = ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "isAuthorizedApprover",
				List.of(approver), null, 33, UserTypeEnum.AGENCY_RECRUITER.getId());

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("(Reflection) isAuthorizedApprover returns false via userId when no userId or type matches")
	void testIsAuthorizedApproverReflectionUserIdNoMatch() {
		// Given
		TimesheetApprover approver = new TimesheetApprover();
		approver.setEntityId(99);
		approver.setUserTypeId(UserTypeEnum.AGENCY_RECRUITER.getId());

		// When
		Boolean result = ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "isAuthorizedApprover",
				List.of(approver), null, 33, UserTypeEnum.AGENCY_RECRUITER.getId());

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("(Reflection) updateTimesheetApprovalStatus delegates to portal approver access for company contact")
	void testUpdateTimesheetApprovalStatusReflectionCompanyContactValidatesPortalAccess() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer userId = 555;
		Integer userTypeId = UserTypeEnum.COMPANY_CONTACT.getId();
		Integer accountId = TimesheetLogsTestDataFactory.getDefaultAccountId();
		given(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);

		// When
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "updateTimesheetApprovalStatus", timesheetId,
				Boolean.TRUE, userId, userTypeId, Boolean.FALSE);

		// Then
		then(this.portalAccessControlService).should()
			.validateApproverAccess(timesheetId, userId, userTypeId, accountId);
		then(this.timesheetApprovalRepository).should()
			.createTimesheetApproval(timesheetId, userId, userTypeId, ApprovalStatusEnum.APPROVED.getId(), null);
		then(this.timesheetInvoiceService).should().createTimesheetInvoice(timesheetId, userId, userTypeId);
	}

	@Test
	@DisplayName("(Reflection) updateTimesheetApprovalStatus approves agency user when registered as an approver")
	void testUpdateTimesheetApprovalStatusReflectionAgencyUserIsApprover() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer userId = 333;
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		given(this.timesheetJpaRepository.findById(timesheetId)).willReturn(Optional.of(timesheet));
		TimesheetApprover approver = TimesheetLogsTestDataFactory.createTimesheetApprover();
		approver.setEntityId(userId);
		approver.setUserTypeId(userTypeId);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(timesheet.getTimesheetSettingId()))
			.willReturn(List.of(approver));

		// When
		ReflectionTestUtils.invokeMethod(this.timesheetLogsService, "updateTimesheetApprovalStatus", timesheetId,
				Boolean.TRUE, userId, userTypeId, Boolean.FALSE);

		// Then
		then(this.timesheetApprovalRepository).should()
			.createTimesheetApproval(timesheetId, userId, userTypeId, ApprovalStatusEnum.APPROVED.getId(), null);
		then(this.timesheetInvoiceService).should().createTimesheetInvoice(timesheetId, userId, userTypeId);
	}

	@Test
	@DisplayName("(Reflection) updateTimesheetApprovalStatus throws when agency user is not a registered approver")
	void testUpdateTimesheetApprovalStatusReflectionAgencyUserNotApproverThrows() {
		// Given
		Integer timesheetId = TimesheetLogsTestDataFactory.getDefaultTimesheetId();
		Integer userId = 333;
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();
		Timesheet timesheet = TimesheetLogsTestDataFactory.createTimesheet();
		given(this.timesheetJpaRepository.findById(timesheetId)).willReturn(Optional.of(timesheet));
		TimesheetApprover approver = TimesheetLogsTestDataFactory.createTimesheetApprover();
		approver.setEntityId(999);
		approver.setUserTypeId(userTypeId);
		given(this.timesheetApproverRepository.findByTimesheetSettingId(timesheet.getTimesheetSettingId()))
			.willReturn(List.of(approver));

		// When / Then
		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(this.timesheetLogsService,
				"updateTimesheetApprovalStatus", timesheetId, Boolean.TRUE, userId, userTypeId, Boolean.FALSE))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("not authorized to approve");
	}

}
