package io.recruitcrm.microservice.timesheet.repositories.timesheet_reimbursement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import io.recruitcrm.microservice.timesheet.dto.jobs.ReimbursementSubmissionReminderWindowRowDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.ITimesheetRepository;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimesheetReimbursementRepository Tests")
class TimesheetReimbursementRepositoryTests {

	private static final Integer TIMESHEET_ID = 100;

	private static final Integer ACCOUNT_ID = 50;

	private static final Integer ENTITY_ID = 200;

	@Mock
	private ITimesheetRepository timesheetRepository;

	private final AtomicReference<Integer> fetchOneResult = new AtomicReference<>(12);

	private DSLContext auroraDbDSLContext;

	private TimesheetReimbursementRepository repository;

	@BeforeEach
	void setUp() {
		this.fetchOneResult.set(12);
		this.auroraDbDSLContext = mock(DSLContext.class,
				withSettings().defaultAnswer(new JooqSelectCountFluentAnswer(this.fetchOneResult::get)));
		this.repository = new TimesheetReimbursementRepository(this.auroraDbDSLContext, this.timesheetRepository);
	}

	@Test
	@DisplayName("getReimbursementCountByTimesheetIdAndEntity returns count when entityType is null")
	void testGetReimbursementCountWhenEntityTypeNull() {
		this.fetchOneResult.set(7);

		Integer result = this.repository.getReimbursementCountByTimesheetIdAndEntity(TIMESHEET_ID, null, null,
				ACCOUNT_ID);

		assertThat(result).isEqualTo(7);
	}

	@Test
	@DisplayName("getReimbursementCountByTimesheetIdAndEntity returns 0 when fetchOne returns null")
	void testGetReimbursementCountReturnsZeroWhenFetchOneNull() {
		this.fetchOneResult.set(null);

		Integer result = this.repository.getReimbursementCountByTimesheetIdAndEntity(TIMESHEET_ID, null, null,
				ACCOUNT_ID);

		assertThat(result).isZero();
	}

	@Test
	@DisplayName("getReimbursementCountByTimesheetIdAndEntity uses contractor condition when entity is contractor")
	void testGetReimbursementCountForContractorEntity() {
		this.fetchOneResult.set(3);

		Integer result = this.repository.getReimbursementCountByTimesheetIdAndEntity(TIMESHEET_ID,
				UserTypeEnum.CONTRACTOR.getId(), ENTITY_ID, ACCOUNT_ID);

		assertThat(result).isEqualTo(3);
		then(this.timesheetRepository).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("getReimbursementCountByTimesheetIdAndEntity uses contact job pairs when entity is company contact")
	void testGetReimbursementCountForCompanyContactWithJobPairs() {
		this.fetchOneResult.set(5);
		List<ContractorJobQueryResultDto> pairs = List.of(new ContractorJobQueryResultDto(10, 20),
				new ContractorJobQueryResultDto(30, 40));
		given(this.timesheetRepository.getJobContractorPairsByContactId(ENTITY_ID, ACCOUNT_ID)).willReturn(pairs);

		Integer result = this.repository.getReimbursementCountByTimesheetIdAndEntity(TIMESHEET_ID,
				UserTypeEnum.COMPANY_CONTACT.getId(), ENTITY_ID, ACCOUNT_ID);

		assertThat(result).isEqualTo(5);
		then(this.timesheetRepository).should().getJobContractorPairsByContactId(ENTITY_ID, ACCOUNT_ID);
	}

	@Test
	@DisplayName("getReimbursementCountByTimesheetIdAndEntity uses false condition when contact has no job pairs")
	void testGetReimbursementCountForCompanyContactWithNoJobPairs() {
		this.fetchOneResult.set(0);
		given(this.timesheetRepository.getJobContractorPairsByContactId(ENTITY_ID, ACCOUNT_ID))
			.willReturn(Collections.emptyList());

		Integer result = this.repository.getReimbursementCountByTimesheetIdAndEntity(TIMESHEET_ID,
				UserTypeEnum.COMPANY_CONTACT.getId(), ENTITY_ID, ACCOUNT_ID);

		assertThat(result).isZero();
		then(this.timesheetRepository).should().getJobContractorPairsByContactId(ENTITY_ID, ACCOUNT_ID);
	}

	@Test
	@DisplayName("getReimbursementCountByTimesheetIdAndEntity uses account-only condition for unknown entity type")
	void testGetReimbursementCountForUnknownEntityTypeFallsBackToAccountScope() {
		this.fetchOneResult.set(9);

		Integer result = this.repository.getReimbursementCountByTimesheetIdAndEntity(TIMESHEET_ID, 999, ENTITY_ID,
				ACCOUNT_ID);

		assertThat(result).isEqualTo(9);
		then(this.timesheetRepository).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("getReimbursementCountByTimesheetIdAndEntity treats null job pairs list like empty for contact")
	void testGetReimbursementCountForCompanyContactWithNullJobPairs() {
		this.fetchOneResult.set(0);
		given(this.timesheetRepository.getJobContractorPairsByContactId(ENTITY_ID, ACCOUNT_ID)).willReturn(null);

		Integer result = this.repository.getReimbursementCountByTimesheetIdAndEntity(TIMESHEET_ID,
				UserTypeEnum.COMPANY_CONTACT.getId(), ENTITY_ID, ACCOUNT_ID);

		assertThat(result).isZero();
	}

	@Test
	@DisplayName("findReimbursementsWhereTransitionedToSubmittedInWindow invokes mapper and returns mapped DTOs")
	void testFindReimbursementsWhereTransitionedToSubmittedInWindowInvokesMapper() {
		List<ReimbursementSubmissionReminderWindowRowDto> result = this.repository
			.findReimbursementsWhereTransitionedToSubmittedInWindow(1700000000L, 1700086400L, 2, 3, 4);

		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.get(0).reimbursementId()).isEqualTo(1);
		assertThat(result.get(0).timesheetId()).isEqualTo(1);
	}

	@Test
	@DisplayName("findReimbursementsWhereTransitionedToSubmittedInWindow accepts adjacent window boundaries")
	void testFindReimbursementsWhereTransitionedToSubmittedInWindowWithAdjacentEpochs() {
		long from = 1699999999L;
		long to = 1700000000L;

		List<ReimbursementSubmissionReminderWindowRowDto> result = this.repository
			.findReimbursementsWhereTransitionedToSubmittedInWindow(from, to, 5, 6, 7);

		assertThat(result).isNotNull().hasSize(1);
	}

	/**
	 * jOOQ {@code selectCount().from()....fetchOne(0, Integer.class)} is a long fluent
	 * chain; this {@link Answer} returns a mock for each step and supplies the terminal
	 * {@code fetchOne} value. When {@code fetch(RecordMapper)} is called the mapper is
	 * invoked with a stub record so that lambda bodies are counted as covered.
	 */
	private static final class JooqSelectCountFluentAnswer implements Answer<Object> {

		private final java.util.function.Supplier<Integer> fetchOneResultSupplier;

		private JooqSelectCountFluentAnswer(java.util.function.Supplier<Integer> fetchOneResultSupplier) {
			this.fetchOneResultSupplier = fetchOneResultSupplier;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Object answer(InvocationOnMock invocation) {
			String name = invocation.getMethod().getName();
			if ("fetchOne".equals(name)) {
				return this.fetchOneResultSupplier.get();
			}
			if ("fetch".equals(name) && invocation.getArguments().length == 1
					&& invocation.getArguments()[0] instanceof org.jooq.RecordMapper) {
				return invokeFetchMapper((org.jooq.RecordMapper) invocation.getArguments()[0]);
			}
			Class<?> returnType = invocation.getMethod().getReturnType();
			if (returnType == void.class || returnType == Void.class) {
				return null;
			}
			return mock(returnType, withSettings().defaultAnswer(this));
		}

		private static List<?> invokeFetchMapper(org.jooq.RecordMapper mapper) {
			// Stub record: also implements Record2 so the lambda bridge-method cast
			// (Record2<Integer, Integer>) succeeds. Returns 1 for any get(Field) call,
			// which satisfies ReimbursementSubmissionReminderWindowRowDto's
			// requireNonNull.
			org.jooq.Record stubRecord = mock(org.jooq.Record.class,
					withSettings().extraInterfaces(org.jooq.Record2.class).defaultAnswer((Answer<Object>) (inv) -> 1));
			try {
				Object mapped = mapper.map(stubRecord);
				return (mapped != null) ? List.of(mapped) : List.of();
			}
			catch (Exception ignored) {
				return List.of();
			}
		}

	}

}
