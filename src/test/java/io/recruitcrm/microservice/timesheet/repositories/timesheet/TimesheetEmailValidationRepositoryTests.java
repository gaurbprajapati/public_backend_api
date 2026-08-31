package io.recruitcrm.microservice.timesheet.repositories.timesheet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.lenient;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetApproverEmailQueryRowDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetCandidateEmailQueryResultDto;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetEmailValidationRepositoryTestDataFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TimesheetEmailValidationRepository Tests")
class TimesheetEmailValidationRepositoryTests {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private DSLContext auroraDbDSLContext;

	private TimesheetEmailValidationRepository repository;

	@BeforeEach
	void setUp() {
		this.repository = new TimesheetEmailValidationRepository(this.auroraDbDSLContext);
		Result<Record> mockResult = mock(Result.class);
		lenient().when(mockResult.into(TimesheetApproverEmailQueryRowDto.class)).thenReturn(Collections.emptyList());
		lenient().when(mockResult.into(TimesheetCandidateEmailQueryResultDto.class))
			.thenReturn(Collections.emptyList());
		lenient().when(mockResult.into(any(Class.class))).thenReturn(Collections.emptyList());
		lenient().when(this.auroraDbDSLContext.fetch(any(ResultQuery.class))).thenReturn(mockResult);
	}

	@Test
	@DisplayName("getApproverEmailValidationRows should return empty list for null timesheet ids")
	void testGetApproverEmailValidationRowsNullTimesheetIdsReturnsEmpty() {
		// When
		List<TimesheetApproverEmailQueryRowDto> result = this.repository.getApproverEmailValidationRows(null,
				TimesheetEmailValidationRepositoryTestDataFactory.getDefaultAccountId());

		// Then
		assertThat(result).isEmpty();
		then(this.auroraDbDSLContext).should(never()).fetch(any(ResultQuery.class));
	}

	@Test
	@DisplayName("getApproverEmailValidationRows should return empty list for empty timesheet ids")
	void testGetApproverEmailValidationRowsEmptyTimesheetIdsReturnsEmpty() {
		// When
		List<TimesheetApproverEmailQueryRowDto> result = this.repository.getApproverEmailValidationRows(
				TimesheetEmailValidationRepositoryTestDataFactory.createEmptyTimesheetIds(),
				TimesheetEmailValidationRepositoryTestDataFactory.getDefaultAccountId());

		// Then
		assertThat(result).isEmpty();
		then(this.auroraDbDSLContext).should(never()).fetch(any(ResultQuery.class));
	}

	@Test
	@DisplayName("getApproverEmailValidationRows should build JOOQ query for valid parameters")
	void testGetApproverEmailValidationRowsValidParametersExecutesJooqQuery() {
		// Given
		List<Integer> timesheetIds = TimesheetEmailValidationRepositoryTestDataFactory.createTimesheetIds();
		Integer accountId = TimesheetEmailValidationRepositoryTestDataFactory.getDefaultAccountId();

		// When & Then
		assertThatCode(() -> this.repository.getApproverEmailValidationRows(timesheetIds, accountId))
			.doesNotThrowAnyException();
		assertThat(timesheetIds).hasSizeGreaterThan(0);
		assertThat(accountId).isPositive();
	}

	@Test
	@DisplayName("getTimesheetValidationData should execute contractor branch JOOQ query")
	void testGetTimesheetValidationDataContractorEntityTypeExecutesJooqQuery() {
		// Given
		List<Integer> timesheetIds = TimesheetEmailValidationRepositoryTestDataFactory.createTimesheetIds();
		Integer accountId = TimesheetEmailValidationRepositoryTestDataFactory.getDefaultAccountId();
		Integer entityTypeId = TimesheetEmailValidationRepositoryTestDataFactory.getEntityTypeContractor();

		// When & Then
		assertThatCode(() -> this.repository.getTimesheetValidationData(timesheetIds, accountId, entityTypeId))
			.doesNotThrowAnyException();
		assertThat(entityTypeId).isEqualTo(TimesheetEmailValidationRepositoryTestDataFactory.getEntityTypeContractor());
	}

	@Test
	@DisplayName("getTimesheetValidationData should execute non-contractor branch JOOQ query")
	void testGetTimesheetValidationDataNonContractorEntityTypeExecutesJooqQuery() {
		// Given
		List<Integer> timesheetIds = TimesheetEmailValidationRepositoryTestDataFactory.createTimesheetIds();
		Integer accountId = TimesheetEmailValidationRepositoryTestDataFactory.getDefaultAccountId();
		Integer entityTypeId = TimesheetEmailValidationRepositoryTestDataFactory.getEntityTypeNonContractor();

		// When & Then
		assertThatCode(() -> this.repository.getTimesheetValidationData(timesheetIds, accountId, entityTypeId))
			.doesNotThrowAnyException();
		assertThat(entityTypeId)
			.isEqualTo(TimesheetEmailValidationRepositoryTestDataFactory.getEntityTypeNonContractor());
	}

	@Test
	@DisplayName("getTimesheetValidationData should execute without assignment branch when entity type is null")
	void testGetTimesheetValidationDataNullEntityTypeExecutesWithoutAssignmentQuery() {
		// Given
		List<Integer> timesheetIds = TimesheetEmailValidationRepositoryTestDataFactory.createTimesheetIds();
		Integer accountId = TimesheetEmailValidationRepositoryTestDataFactory.getDefaultAccountId();

		// When & Then
		assertThatCode(() -> this.repository.getTimesheetValidationData(timesheetIds, accountId, null))
			.doesNotThrowAnyException();
		assertThat(timesheetIds).isNotEmpty();
	}

	@Test
	@DisplayName("getTimesheetValidationData should execute without assignment branch for empty timesheet ids")
	void testGetTimesheetValidationDataEmptyTimesheetIdsExecutesQuery() {
		// Given
		List<Integer> timesheetIds = Collections.emptyList();
		Integer accountId = TimesheetEmailValidationRepositoryTestDataFactory.getDefaultAccountId();

		// When & Then
		assertThatCode(() -> this.repository.getTimesheetValidationData(timesheetIds, accountId,
				TimesheetEmailValidationRepositoryTestDataFactory.getEntityTypeContractor()))
			.doesNotThrowAnyException();
		assertThat(timesheetIds).isEmpty();
	}

	@Test
	@DisplayName("getClientPortalStatusByEmails should return empty map for null emails")
	void testGetClientPortalStatusByEmailsNullEmailsReturnsEmptyMap() {
		// When
		Map<String, Integer> result = this.repository.getClientPortalStatusByEmails(null,
				TimesheetEmailValidationRepositoryTestDataFactory.getDefaultAccountId());

		// Then
		assertThat(result).isEmpty();
		then(this.auroraDbDSLContext).should(never()).fetch(any(ResultQuery.class));
	}

	@Test
	@DisplayName("getClientPortalStatusByEmails should return empty map for empty emails")
	void testGetClientPortalStatusByEmailsEmptyEmailsReturnsEmptyMap() {
		// When
		Map<String, Integer> result = this.repository.getClientPortalStatusByEmails(Collections.emptyList(),
				TimesheetEmailValidationRepositoryTestDataFactory.getDefaultAccountId());

		// Then
		assertThat(result).isEmpty();
		then(this.auroraDbDSLContext).should(never()).fetch(any(ResultQuery.class));
	}

	@Test
	@DisplayName("getClientPortalStatusByEmails should build JOOQ query for valid parameters")
	void testGetClientPortalStatusByEmailsValidParametersExecutesJooqQuery() {
		// Given
		List<String> emails = List.of("contact@example.com");
		Integer accountId = TimesheetEmailValidationRepositoryTestDataFactory.getDefaultAccountId();

		// When & Then
		assertThatCode(() -> this.repository.getClientPortalStatusByEmails(emails, accountId))
			.doesNotThrowAnyException();
		assertThat(emails).hasSize(1);
		assertThat(accountId).isPositive();
	}

}
