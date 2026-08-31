/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.portals.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.contract_staffing.entity.model.ClientPortalStatus;
import io.recruitcrm.microservice.timesheet.dao.portals.client.ClientPortalStatusJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.portals.client.ClientPortalStatusQueryResultDto;
import io.recruitcrm.microservice.timesheet.helpers.constants.ClientPortalStatusConstants;
import io.recruitcrm.microservice.timesheet.testdata.ClientPortalStatusTestDataFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ClientPortalStatusRepository}.
 */
@ExtendWith(MockitoExtension.class)
class ClientPortalStatusRepositoryTests {

	@Mock
	private ClientPortalStatusJpaRepository clientPortalStatusJpaRepository;

	private ClientPortalStatusRepository clientPortalStatusRepository;

	@BeforeEach
	void setUp() {
		this.clientPortalStatusRepository = new ClientPortalStatusRepository(this.clientPortalStatusJpaRepository);
	}

	@Test
	@DisplayName("Find by email and account id returns empty optional when no record found")
	void testFindByVmsUserEmailAndAccountIdNoRecordReturnsEmptyOptional() {
		// Given
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusJpaRepository.findByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.empty());

		// When
		Optional<ClientPortalStatusQueryResultDto> result = this.clientPortalStatusRepository
			.findByVmsUserEmailAndAccountId(email, accountId);

		// Then
		assertThat(result).isEmpty();
		then(this.clientPortalStatusJpaRepository).should().findByVmsUserEmailAndAccountId(email, accountId);
	}

	@Test
	@DisplayName("Find by email and account id returns mapped result when record found")
	void testFindByVmsUserEmailAndAccountIdExistingRecordReturnsMappedResult() {
		// Given
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatus entity = ClientPortalStatusTestDataFactory.createClientPortalStatusEntity();
		given(this.clientPortalStatusJpaRepository.findByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(entity));

		// When
		Optional<ClientPortalStatusQueryResultDto> result = this.clientPortalStatusRepository
			.findByVmsUserEmailAndAccountId(email, accountId);

		// Then
		assertThat(result).contains(ClientPortalStatusTestDataFactory.createQueryResult());
		then(this.clientPortalStatusJpaRepository).should().findByVmsUserEmailAndAccountId(email, accountId);
	}

	@Test
	@DisplayName("Find entity by email and account id returns entity optional")
	void testFindEntityByVmsUserEmailAndAccountIdReturnsEntityOptional() {
		// Given
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatus entity = ClientPortalStatusTestDataFactory.createClientPortalStatusEntity();
		given(this.clientPortalStatusJpaRepository.findByVmsUserEmailAndAccountId(email, accountId))
			.willReturn(Optional.of(entity));

		// When
		Optional<ClientPortalStatus> result = this.clientPortalStatusRepository
			.findEntityByVmsUserEmailAndAccountId(email, accountId);

		// Then
		assertThat(result).contains(entity);
	}

	@Test
	@DisplayName("SaveAll returns empty list when entities empty")
	void testSaveAllEmptyEntitiesReturnsEmptyList() {
		// When
		List<ClientPortalStatus> result = this.clientPortalStatusRepository.saveAll(List.of());

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("SaveAll delegates to JPA repository")
	void testSaveAllDelegatesToJpaRepository() {
		// Given
		ClientPortalStatus entity = ClientPortalStatusTestDataFactory.createClientPortalStatusEntity();
		given(this.clientPortalStatusJpaRepository.saveAll(List.of(entity))).willReturn(List.of(entity));

		// When
		List<ClientPortalStatus> result = this.clientPortalStatusRepository.saveAll(List.of(entity));

		// Then
		assertThat(result).containsExactly(entity);
		then(this.clientPortalStatusJpaRepository).should().saveAll(List.of(entity));
	}

	@Test
	@DisplayName("Save delegates to JPA repository")
	void testSaveDelegatesToJpaRepository() {
		// Given
		ClientPortalStatus entity = ClientPortalStatusTestDataFactory.createClientPortalStatusEntity();
		given(this.clientPortalStatusJpaRepository.save(entity)).willReturn(entity);

		// When
		ClientPortalStatus result = this.clientPortalStatusRepository.save(entity);

		// Then
		assertThat(result).isEqualTo(entity);
		then(this.clientPortalStatusJpaRepository).should().save(entity);
	}

	@Test
	@DisplayName("Exists portal status under different account delegates to JPA query")
	void testExistsPortalStatusUnderDifferentAccountDelegatesToJpaQuery() {
		// Given
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusJpaRepository.existsByVmsUserEmailAndAccountIdNot(email, accountId))
			.willReturn(true);

		// When
		boolean result = this.clientPortalStatusRepository.existsPortalStatusUnderDifferentAccount(email, accountId);

		// Then
		assertThat(result).isTrue();
		then(this.clientPortalStatusJpaRepository).should().existsByVmsUserEmailAndAccountIdNot(email, accountId);
	}

	@Test
	@DisplayName("Find entities by emails and account id returns empty list when emails empty")
	void testFindEntitiesByVmsUserEmailInAndAccountIdEmptyEmailsReturnsEmptyList() {
		// When
		List<ClientPortalStatus> result = this.clientPortalStatusRepository.findEntitiesByVmsUserEmailInAndAccountId(
				List.of(), ClientPortalStatusTestDataFactory.getDefaultAccountId());

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Find entities by emails and account id delegates to JPA repository")
	void testFindEntitiesByVmsUserEmailInAndAccountIdDelegatesToJpaRepository() {
		// Given
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		ClientPortalStatus entity = ClientPortalStatusTestDataFactory.createClientPortalStatusEntity();
		given(this.clientPortalStatusJpaRepository.findByVmsUserEmailInAndAccountId(List.of(email), accountId))
			.willReturn(List.of(entity));

		// When
		List<ClientPortalStatus> result = this.clientPortalStatusRepository
			.findEntitiesByVmsUserEmailInAndAccountId(List.of(email), accountId);

		// Then
		assertThat(result).containsExactly(entity);
		then(this.clientPortalStatusJpaRepository).should().findByVmsUserEmailInAndAccountId(List.of(email), accountId);
	}

	@Test
	@DisplayName("Find emails portal enabled under different account returns empty list when emails empty")
	void testFindEmailsPortalEnabledUnderDifferentAccountEmptyEmailsReturnsEmptyList() {
		// When
		List<String> result = this.clientPortalStatusRepository.findEmailsPortalEnabledUnderDifferentAccount(List.of(),
				ClientPortalStatusTestDataFactory.getDefaultAccountId());

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Find emails portal enabled under different account delegates to JPA query")
	void testFindEmailsPortalEnabledUnderDifferentAccountDelegatesToJpaQuery() {
		// Given
		String email = ClientPortalStatusTestDataFactory.getDefaultEmail();
		Integer accountId = ClientPortalStatusTestDataFactory.getDefaultAccountId();
		given(this.clientPortalStatusJpaRepository.findVmsUserEmailsPortalEnabledUnderDifferentAccount(List.of(email),
				ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_ENABLED, accountId))
			.willReturn(List.of(email));

		// When
		List<String> result = this.clientPortalStatusRepository
			.findEmailsPortalEnabledUnderDifferentAccount(List.of(email), accountId);

		// Then
		assertThat(result).containsExactly(email);
		then(this.clientPortalStatusJpaRepository).should()
			.findVmsUserEmailsPortalEnabledUnderDifferentAccount(List.of(email),
					ClientPortalStatusConstants.PORTAL_STATUS_PORTAL_ENABLED, accountId);
	}

}
