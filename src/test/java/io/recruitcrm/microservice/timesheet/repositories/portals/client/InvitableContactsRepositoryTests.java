/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.portals.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.recruitcrm.microservice.search.models.jooq.tables.Tblcompany;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcontact;
import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactQueryResultDto;
import java.util.List;
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link InvitableContactsRepository}.
 */
@ExtendWith(MockitoExtension.class)
class InvitableContactsRepositoryTests {

	private static final Tblcontact CONTACT = Tblcontact.TBLCONTACT;

	private static final Tblcompany COMPANY = Tblcompany.TBLCOMPANY;

	private static final String PORTAL_STATUS_ID_ALIAS = "portalStatusId";

	/**
	 * Builds a {@link DSLContext} mock whose fluent {@code select → from → join → on →
	 * leftJoin → where → orderBy → fetch} chain returns the supplied fetch result, while
	 * {@code fetchExists} returns the supplied boolean.
	 */
	private DSLContext createDslContext(Object fetchResult, boolean fetchExistsResult) {
		// Condition-phase proxy: handles where → orderBy → seek → limit → fetch.
		Object conditionChain = java.lang.reflect.Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { org.jooq.SelectConditionStep.class, org.jooq.SelectOrderByStep.class,
						org.jooq.SelectSeekStep1.class, org.jooq.SelectSeekStep2.class, org.jooq.SelectSeekStepN.class,
						org.jooq.SelectLimitStep.class, org.jooq.SelectLimitPercentStep.class },
				(proxy, method, args) -> {
					if ("fetch".equals(method.getName())) {
						return fetchResult;
					}
					return proxy;
				});

		// Main-chain proxy: handles select → from → join → on → leftJoin, delegating to
		// the condition chain on where().
		Object mainChain = java.lang.reflect.Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { org.jooq.SelectSelectStep.class, org.jooq.SelectFromStep.class,
						org.jooq.SelectJoinStep.class, org.jooq.SelectOnStep.class, org.jooq.SelectOptionalOnStep.class,
						org.jooq.SelectOnConditionStep.class, org.jooq.SelectJoinPartitionByStep.class,
						org.jooq.SelectWhereStep.class },
				(proxy, method, args) -> {
					if ("where".equals(method.getName())) {
						return conditionChain;
					}
					if ("fetch".equals(method.getName())) {
						return fetchResult;
					}
					return proxy;
				});

		return mock(DSLContext.class, (invocation) -> {
			String name = invocation.getMethod().getName();
			if ("fetchExists".equals(name)) {
				return fetchExistsResult;
			}
			if (name.startsWith("select")) {
				return mainChain;
			}
			return Answers.RETURNS_DEFAULTS.answer(invocation);
		});
	}

	@Test
	@DisplayName("Exists contact assigned to company returns true when fetchExists is true")
	void testExistsContactAssignedToCompanyRecordPresentReturnsTrue() {
		// Given
		DSLContext dslContext = this.createDslContext(null, true);
		InvitableContactsRepository repository = new InvitableContactsRepository(dslContext);

		// When
		boolean result = repository.existsContactAssignedToCompany(1, 2, 3);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Exists contact assigned to company returns false when fetchExists is false")
	void testExistsContactAssignedToCompanyNoRecordReturnsFalse() {
		// Given
		DSLContext dslContext = this.createDslContext(null, false);
		InvitableContactsRepository repository = new InvitableContactsRepository(dslContext);

		// When
		boolean result = repository.existsContactAssignedToCompany(1, 2, 3);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Find contacts with portal status maps record with non-null portal status")
	void testFindContactsWithPortalStatusNonNullPortalStatusMapsRecord() {
		// Given
		Record rec = mock(org.jooq.Record9.class);
		given(rec.get(PORTAL_STATUS_ID_ALIAS, Byte.class)).willReturn((byte) 2);
		given(rec.get(CONTACT.ID)).willReturn(10);
		given(rec.get(CONTACT.FIRSTNAME)).willReturn("Jane");
		given(rec.get(CONTACT.LASTNAME)).willReturn("Doe");
		given(rec.get(CONTACT.EMAIL)).willReturn("jane@example.com");
		given(rec.get(CONTACT.PHOTO)).willReturn("photo.png");
		given(rec.get(CONTACT.SRNO)).willReturn(5);
		given(rec.get(COMPANY.COMPANYNAME)).willReturn("Acme Corp");
		given(rec.get(CONTACT.OWNERID)).willReturn(7);
		DSLContext dslContext = this.createDslContext(this.mockResult(rec), false);
		InvitableContactsRepository repository = new InvitableContactsRepository(dslContext);

		// When
		List<InvitableContactQueryResultDto> result = repository.findContactsWithPortalStatus(100, 200, null, 100);

		// Then
		assertThat(result).containsExactly(new InvitableContactQueryResultDto(10, "Jane", "Doe", "jane@example.com", 2,
				"photo.png", 5, "Acme Corp", 7));
	}

	@Test
	@DisplayName("Find contacts with portal status defaults to zero when portal status is null")
	void testFindContactsWithPortalStatusNullPortalStatusDefaultsToZero() {
		// Given
		Record rec = mock(org.jooq.Record9.class);
		given(rec.get(PORTAL_STATUS_ID_ALIAS, Byte.class)).willReturn(null);
		given(rec.get(CONTACT.ID)).willReturn(11);
		given(rec.get(CONTACT.FIRSTNAME)).willReturn("John");
		given(rec.get(CONTACT.LASTNAME)).willReturn("Smith");
		given(rec.get(CONTACT.EMAIL)).willReturn("john@example.com");
		given(rec.get(CONTACT.PHOTO)).willReturn(null);
		given(rec.get(CONTACT.SRNO)).willReturn(6);
		given(rec.get(COMPANY.COMPANYNAME)).willReturn(null);
		given(rec.get(CONTACT.OWNERID)).willReturn(null);
		DSLContext dslContext = this.createDslContext(this.mockResult(rec), false);
		InvitableContactsRepository repository = new InvitableContactsRepository(dslContext);

		// When
		List<InvitableContactQueryResultDto> result = repository.findContactsWithPortalStatus(100, 200, null, 100);

		// Then
		assertThat(result).containsExactly(
				new InvitableContactQueryResultDto(11, "John", "Smith", "john@example.com", 0, null, 6, null, null));
	}

	@ParameterizedTest(name = "Find contacts with portal status returns empty list: {0}")
	@MethodSource("emptyResultSearchScenarios")
	void testFindContactsWithPortalStatusReturnsEmptyList(String scenario, String search, int limit) {
		// Given
		DSLContext dslContext = this.createDslContext(this.mockResult(), false);
		InvitableContactsRepository repository = new InvitableContactsRepository(dslContext);

		// When
		List<InvitableContactQueryResultDto> result = repository.findContactsWithPortalStatus(100, 200, search, limit);

		// Then
		assertThat(result).isEmpty();
	}

	/**
	 * Scenarios covering: no records found, a plain search filter, a blank search that
	 * skips the filter, a multi-token search that ANDs the {@code nameCondition} tokens
	 * together, and LIKE metacharacters (%, _, \) escaped by {@code toLikePattern}.
	 */
	private static Stream<Arguments> emptyResultSearchScenarios() {
		return Stream.of(Arguments.of("no records found", null, 100),
				Arguments.of("applies search condition when search text is provided", "  jane  ", 10),
				Arguments.of("ignores blank search text", "   ", 10),
				Arguments.of("ANDs multiple search tokens together", "jane doe", 10),
				Arguments.of("escapes LIKE metacharacters in search text", "a%_\\b", 10));
	}

	@Test
	@DisplayName("Find contacts with portal status skips blank tokens between valid ones")
	void testFindContactsWithPortalStatusSkipsBlankTokens() {
		// Given: U+2028 (line separator) is treated as whitespace by String#isBlank but
		// is
		// NOT matched by the \s+ split, so it survives as its own blank token and
		// exercises
		// the "token.isBlank()" continue branch. A leading space keeps it a standalone
		// token
		// after the surrounding real tokens are split off.
		DSLContext dslContext = this.createDslContext(this.mockResult(), false);
		InvitableContactsRepository repository = new InvitableContactsRepository(dslContext);
		// U+2028 built numerically: a literal unicode escape would be lexed as a line
		// terminator inside the source string.
		String searchWithBlankToken = "jane " + (char) 0x2028 + " doe";

		// When
		List<InvitableContactQueryResultDto> result = repository.findContactsWithPortalStatus(100, 200,
				searchWithBlankToken, 10);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Find contacts with portal status returns mapped record when search text matches")
	void testFindContactsWithPortalStatusWithSearchTextReturnsMappedRecord() {
		// Given: exercises the full map path together with an applied search condition.
		Record rec = mock(org.jooq.Record9.class);
		given(rec.get(PORTAL_STATUS_ID_ALIAS, Byte.class)).willReturn((byte) 1);
		given(rec.get(CONTACT.ID)).willReturn(12);
		given(rec.get(CONTACT.FIRSTNAME)).willReturn("Jane");
		given(rec.get(CONTACT.LASTNAME)).willReturn("Doe");
		given(rec.get(CONTACT.EMAIL)).willReturn("jane@example.com");
		given(rec.get(CONTACT.PHOTO)).willReturn("photo.png");
		given(rec.get(CONTACT.SRNO)).willReturn(8);
		given(rec.get(COMPANY.COMPANYNAME)).willReturn("Acme Corp");
		given(rec.get(CONTACT.OWNERID)).willReturn(9);
		DSLContext dslContext = this.createDslContext(this.mockResult(rec), false);
		InvitableContactsRepository repository = new InvitableContactsRepository(dslContext);

		// When
		List<InvitableContactQueryResultDto> result = repository.findContactsWithPortalStatus(100, 200, "jane", 10);

		// Then
		assertThat(result).containsExactly(new InvitableContactQueryResultDto(12, "Jane", "Doe", "jane@example.com", 1,
				"photo.png", 8, "Acme Corp", 9));
	}

	@SuppressWarnings("unchecked")
	private Result<Record> mockResult(Record... records) {
		Result<Record> fetchResult = mock(Result.class);
		given(fetchResult.stream()).willReturn(Stream.of(records));
		return fetchResult;
	}

}
