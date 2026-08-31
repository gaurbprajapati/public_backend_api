/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.portals.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.recruitcrm.microservice.search.models.jooq.tables.ClientPortalStatusT;
import io.recruitcrm.microservice.search.models.jooq.tables.Tblcontact;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateQueryResultDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.UInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link BulkValidateRepository}.
 */
@ExtendWith(MockitoExtension.class)
class BulkValidateRepositoryTests {

	private static final ClientPortalStatusT CPS = ClientPortalStatusT.CLIENT_PORTAL_STATUS_T;

	private static final Tblcontact CONTACT = Tblcontact.TBLCONTACT;

	/**
	 * Builds a {@link DSLContext} mock whose fluent {@code select → from → where → and →
	 * fetch} chain returns the supplied fetch result.
	 */
	private DSLContext createDslContext(Object fetchResult) {
		Object chain = java.lang.reflect.Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { org.jooq.SelectSelectStep.class, org.jooq.SelectFromStep.class,
						org.jooq.SelectJoinStep.class, org.jooq.SelectWhereStep.class,
						org.jooq.SelectConditionStep.class },
				(proxy, method, args) -> {
					if ("fetch".equals(method.getName())) {
						return fetchResult;
					}
					return proxy;
				});

		return mock(DSLContext.class, (invocation) -> {
			if (invocation.getMethod().getName().startsWith("select")) {
				return chain;
			}
			return Answers.RETURNS_DEFAULTS.answer(invocation);
		});
	}

	@Test
	@DisplayName("Find portal status by emails maps records with non-null values")
	void testFindPortalStatusByEmailsNonNullValuesMapsRecords() {
		// Given
		Record rec = mock(org.jooq.Record5.class);
		given(rec.get(CPS.VMS_USER_EMAIL)).willReturn("jane@example.com");
		given(rec.get(CPS.PORTAL_STATUS_ID)).willReturn((byte) 2);
		given(rec.get(CPS.ACCOUNT_ID)).willReturn(UInteger.valueOf(42));
		given(rec.get(CPS.INVITE_COUNT)).willReturn(UInteger.valueOf(3));
		given(rec.get(CPS.INVITE_SENT_ON)).willReturn(UInteger.valueOf(1700000000));
		DSLContext dslContext = this.createDslContext(this.mockResult(rec));
		BulkValidateRepository repository = new BulkValidateRepository(dslContext);

		// When
		List<BulkValidateQueryResultDto> result = repository.findPortalStatusByEmails(List.of("jane@example.com"));

		// Then
		assertThat(result).containsExactly(new BulkValidateQueryResultDto("jane@example.com", 2, 42L, 3L, 1700000000L));
	}

	@Test
	@DisplayName("Find portal status by emails defaults to zero when values are null")
	void testFindPortalStatusByEmailsNullValuesDefaultsToZero() {
		// Given
		Record rec = mock(org.jooq.Record5.class);
		given(rec.get(CPS.VMS_USER_EMAIL)).willReturn("john@example.com");
		given(rec.get(CPS.PORTAL_STATUS_ID)).willReturn(null);
		given(rec.get(CPS.ACCOUNT_ID)).willReturn(null);
		given(rec.get(CPS.INVITE_COUNT)).willReturn(null);
		given(rec.get(CPS.INVITE_SENT_ON)).willReturn(null);
		DSLContext dslContext = this.createDslContext(this.mockResult(rec));
		BulkValidateRepository repository = new BulkValidateRepository(dslContext);

		// When
		List<BulkValidateQueryResultDto> result = repository.findPortalStatusByEmails(List.of("john@example.com"));

		// Then
		assertThat(result).containsExactly(new BulkValidateQueryResultDto("john@example.com", 0, 0L, 0L, 0L));
	}

	@Test
	@DisplayName("Find owner ids by contact ids returns empty map when contact ids empty")
	void testFindOwnerIdsByContactIdsEmptyContactIdsReturnsEmptyMap() {
		// Given
		DSLContext dslContext = this.createDslContext(null);
		BulkValidateRepository repository = new BulkValidateRepository(dslContext);

		// When
		Map<Integer, Integer> result = repository.findOwnerIdsByContactIds(List.of());

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Find owner ids by contact ids maps contact id to owner id")
	void testFindOwnerIdsByContactIdsValidContactIdsMapsOwnerIds() {
		// Given
		Record rec = mock(org.jooq.Record2.class);
		given(rec.get(CONTACT.ID)).willReturn(10);
		given(rec.get(CONTACT.OWNERID)).willReturn(99);
		DSLContext dslContext = this.createDslContext(this.mockResult(rec));
		BulkValidateRepository repository = new BulkValidateRepository(dslContext);

		// When
		Map<Integer, Integer> result = repository.findOwnerIdsByContactIds(List.of(10));

		// Then
		assertThat(result).containsExactly(Map.entry(10, 99));
	}

	@Test
	@DisplayName("Find owner ids by contact ids skips records with null owner id")
	void testFindOwnerIdsByContactIdsNullOwnerIdSkipsRecord() {
		// Given
		Record rec = mock(org.jooq.Record2.class);
		given(rec.get(CONTACT.OWNERID)).willReturn(null);
		DSLContext dslContext = this.createDslContext(this.mockResult(rec));
		BulkValidateRepository repository = new BulkValidateRepository(dslContext);

		// When
		Map<Integer, Integer> result = repository.findOwnerIdsByContactIds(List.of(10));

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Find owner ids by contact ids keeps existing owner when duplicate contact ids collide")
	void testFindOwnerIdsByContactIdsDuplicateContactIdsKeepsExisting() {
		// Given
		Record firstRecord = mock(org.jooq.Record2.class);
		given(firstRecord.get(CONTACT.ID)).willReturn(10);
		given(firstRecord.get(CONTACT.OWNERID)).willReturn(99);
		Record duplicateRecord = mock(org.jooq.Record2.class);
		given(duplicateRecord.get(CONTACT.ID)).willReturn(10);
		given(duplicateRecord.get(CONTACT.OWNERID)).willReturn(77);
		DSLContext dslContext = this.createDslContext(this.mockResult(firstRecord, duplicateRecord));
		BulkValidateRepository repository = new BulkValidateRepository(dslContext);

		// When
		Map<Integer, Integer> result = repository.findOwnerIdsByContactIds(List.of(10));

		// Then
		assertThat(result).containsExactly(Map.entry(10, 99));
	}

	@SuppressWarnings("unchecked")
	private Result<Record> mockResult(Record... records) {
		Result<Record> fetchResult = mock(Result.class);
		given(fetchResult.stream()).willReturn(Stream.of(records));
		return fetchResult;
	}

}
