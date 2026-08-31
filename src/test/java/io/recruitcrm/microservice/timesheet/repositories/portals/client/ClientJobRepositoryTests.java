/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.portals.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.recruitcrm.microservice.timesheet.dto.portal.client.ClientJobResponseBodyDto;
import io.recruitcrm.microservice.timesheet.testdata.ClientJobTestDataFactory;
import java.util.List;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ClientJobRepository}.
 */
@ExtendWith(MockitoExtension.class)
class ClientJobRepositoryTests {

	/**
	 * Builds a {@link DSLContext} mock whose fluent {@code select → from → join → on →
	 * leftJoin → where → and → groupBy → fetchInto} chain returns the supplied list.
	 */
	private DSLContext createDslContext(List<?> fetchIntoResult) {
		// Condition-phase proxy: handles where → and → groupBy → fetchInto.
		Object conditionChain = java.lang.reflect.Proxy
			.newProxyInstance(
					this.getClass().getClassLoader(), new Class<?>[] { org.jooq.SelectConditionStep.class,
							org.jooq.SelectGroupByStep.class, org.jooq.SelectHavingStep.class },
					(proxy, method, args) -> {
						if ("fetchInto".equals(method.getName())) {
							return fetchIntoResult;
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
					if ("fetchInto".equals(method.getName())) {
						return fetchIntoResult;
					}
					return proxy;
				});

		return mock(DSLContext.class, (invocation) -> {
			if (invocation.getMethod().getName().startsWith("select")) {
				return mainChain;
			}
			return Answers.RETURNS_DEFAULTS.answer(invocation);
		});
	}

	@Test
	@DisplayName("Find jobs by email returns mapped jobs from fetchInto")
	void testFindJobsByEmailValidEmailReturnsMappedJobs() {
		// Given
		List<ClientJobResponseBodyDto> expected = ClientJobTestDataFactory.createJobResponseList();
		DSLContext dslContext = this.createDslContext(expected);
		ClientJobRepository repository = new ClientJobRepository(dslContext);

		// When
		List<ClientJobResponseBodyDto> result = repository.findJobsByEmail(ClientJobTestDataFactory.getDefaultEmail(),
				ClientJobTestDataFactory.getDefaultAccountId());

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("Find jobs by email returns empty list when no jobs found")
	void testFindJobsByEmailNoJobsReturnsEmptyList() {
		// Given
		DSLContext dslContext = this.createDslContext(List.of());
		ClientJobRepository repository = new ClientJobRepository(dslContext);

		// When
		List<ClientJobResponseBodyDto> result = repository.findJobsByEmail(ClientJobTestDataFactory.getDefaultEmail(),
				ClientJobTestDataFactory.getDefaultAccountId());

		// Then
		assertThat(result).isEmpty();
	}

}
