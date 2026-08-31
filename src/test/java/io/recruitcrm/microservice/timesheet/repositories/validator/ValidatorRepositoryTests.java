/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetValidatorQueryResultDto;
import io.recruitcrm.microservice.timesheet.testdata.ValidatorRepositoryTestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Unit tests for {@link ValidatorRepository}: JPQL execution, parameter binding, empty
 * results, and exception propagation.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ValidatorRepositoryTests {

	private static final String JPQL_PRIMARY_TIMESHEET_BY_ID = "SELECT t FROM Timesheet t WHERE t.id = :primaryTimesheetId";

	private static final String JPQL_VALIDATE_TIME_LOGS_BEFORE_UPDATE = "SELECT tsa.contractorId, "
			+ "       tsa.jobId, " + "       ts.workLogType, " + "       t.periodStart, " + "       t.periodEnd, "
			+ "       CONCAT(c.firstName, ' ', c.lastName) AS fullName, " + "       c.profilePic, " + "       j.name, "
			+ "       ts.id, " + "       t.id, " + "       cm.logo, "
			+ "       COALESCE(MAX(ta.timesheetApprovalStatusTypeId), NULL) AS timesheetApprovalStatusTypeId, "
			+ "       ts.calculateBreakTime, " + "       ts.templateWorkDay, " + "c.serialNo   "
			+ "FROM TimesheetSetting ts " + "LEFT JOIN TimesheetSettingAssociation tsa ON ts.association.id = tsa.id "
			+ "LEFT JOIN Timesheet t ON t.timesheetSettingId = ts.id "
			+ "LEFT JOIN Candidate c ON c.id = tsa.contractorId " + "LEFT JOIN Job j ON j.id = tsa.jobId "
			+ "LEFT JOIN Company cm ON cm.id = j.company.id "
			+ "LEFT JOIN TimesheetApproval ta ON ta.timesheetId = t.id " + "WHERE t.id IN :timesheetIds "
			+ "GROUP BY tsa.contractorId, tsa.jobId, ts.workLogType, t.periodStart, t.periodEnd, c.firstName, c.lastName, c.profilePic, j.name, ts.id, t.id, cm.logo, ts.calculateBreakTime, ts.templateWorkDay, c.serialNo";

	private static final String JPQL_VALIDATE_CONTRACTOR_TIME_LOGS_BEFORE_UPDATE = "SELECT new io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetValidatorQueryResultDto("
			+ "t.id, " + "ts.id, " + "ts.workLogType, " + "ts.calculateBreakTime, " + "ts.templateWorkDay " + ") "
			+ "FROM TimesheetSetting ts " + "LEFT JOIN TimesheetSettingAssociation tsa ON ts.association.id = tsa.id "
			+ "LEFT JOIN Timesheet t ON t.timesheetSettingId = ts.id " + "WHERE t.id IN :timesheetIds";

	@Mock
	private EntityManager entityManager;

	private ValidatorRepository repository;

	@BeforeEach
	void setUp() {
		this.repository = new ValidatorRepository(this.entityManager);
	}

	@Test
	@DisplayName("getPrimaryTimesheetById returns first row when a timesheet exists")
	void testGetPrimaryTimesheetByIdWhenFoundReturnsFirstRow() {
		// Given
		Integer primaryId = ValidatorRepositoryTestDataFactory.getPrimaryTimesheetId();
		Timesheet expected = ValidatorRepositoryTestDataFactory.createTimesheetWithId(primaryId);
		TypedQuery<Timesheet> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_PRIMARY_TIMESHEET_BY_ID, Timesheet.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("primaryTimesheetId", primaryId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(expected));

		// When
		Timesheet result = this.repository.getPrimaryTimesheetById(primaryId);

		// Then
		assertThat(result).isSameAs(expected);
		then(this.entityManager).should().createQuery(JPQL_PRIMARY_TIMESHEET_BY_ID, Timesheet.class);
		then(mockQuery).should().setParameter("primaryTimesheetId", primaryId);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("getPrimaryTimesheetById returns null when no timesheet matches")
	void testGetPrimaryTimesheetByIdWhenMissingReturnsNull() {
		// Given
		Integer primaryId = ValidatorRepositoryTestDataFactory.getPrimaryTimesheetId();
		TypedQuery<Timesheet> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_PRIMARY_TIMESHEET_BY_ID, Timesheet.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("primaryTimesheetId", primaryId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		Timesheet result = this.repository.getPrimaryTimesheetById(primaryId);

		// Then
		assertThat(result).isNull();
		then(this.entityManager).should().createQuery(JPQL_PRIMARY_TIMESHEET_BY_ID, Timesheet.class);
		then(mockQuery).should().setParameter("primaryTimesheetId", primaryId);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("getPrimaryTimesheetById returns the first element when multiple rows are returned")
	void testGetPrimaryTimesheetByIdWhenMultipleRowsReturnsFirstElement() {
		// Given
		Integer primaryId = ValidatorRepositoryTestDataFactory.getPrimaryTimesheetId();
		Timesheet first = ValidatorRepositoryTestDataFactory.createTimesheetWithId(1);
		Timesheet second = ValidatorRepositoryTestDataFactory.createTimesheetWithId(2);
		TypedQuery<Timesheet> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_PRIMARY_TIMESHEET_BY_ID, Timesheet.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("primaryTimesheetId", primaryId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(first, second));

		// When
		Timesheet result = this.repository.getPrimaryTimesheetById(primaryId);

		// Then
		assertThat(result).isSameAs(first).isNotSameAs(second);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("getPrimaryTimesheetById propagates errors from the persistence provider")
	void testGetPrimaryTimesheetByIdWhenQueryFailsPropagatesException() {
		// Given
		Integer primaryId = ValidatorRepositoryTestDataFactory.getPrimaryTimesheetId();
		TypedQuery<Timesheet> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_PRIMARY_TIMESHEET_BY_ID, Timesheet.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("primaryTimesheetId", primaryId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willThrow(new DataAccessResourceFailureException("connection reset"));

		// When & Then
		assertThatThrownBy(() -> this.repository.getPrimaryTimesheetById(primaryId))
			.isInstanceOf(DataAccessResourceFailureException.class)
			.hasMessageContaining("connection reset");
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("validateTimeLogsBeforeUpdate returns rows from the aggregate validator query")
	void testValidateTimeLogsBeforeUpdateWhenRowsReturnedReturnsList() {
		// Given
		List<Integer> timesheetIds = ValidatorRepositoryTestDataFactory.getSampleTimesheetIdsForValidator();
		TimesheetAndSettingValidatorQueryResultDto row = ValidatorRepositoryTestDataFactory
			.createTimesheetAndSettingValidatorRow();
		TypedQuery<TimesheetAndSettingValidatorQueryResultDto> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_VALIDATE_TIME_LOGS_BEFORE_UPDATE,
				TimesheetAndSettingValidatorQueryResultDto.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetIds", timesheetIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(row));

		// When
		List<TimesheetAndSettingValidatorQueryResultDto> result = this.repository
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result).containsExactly(row);
		then(this.entityManager).should()
			.createQuery(JPQL_VALIDATE_TIME_LOGS_BEFORE_UPDATE, TimesheetAndSettingValidatorQueryResultDto.class);
		then(mockQuery).should().setParameter("timesheetIds", timesheetIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("validateTimeLogsBeforeUpdate returns empty list when no rows match")
	void testValidateTimeLogsBeforeUpdateWhenNoRowsReturnsEmptyList() {
		// Given
		List<Integer> timesheetIds = ValidatorRepositoryTestDataFactory.getSampleTimesheetIdsForValidator();
		TypedQuery<TimesheetAndSettingValidatorQueryResultDto> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_VALIDATE_TIME_LOGS_BEFORE_UPDATE,
				TimesheetAndSettingValidatorQueryResultDto.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetIds", timesheetIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		List<TimesheetAndSettingValidatorQueryResultDto> result = this.repository
			.validateTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result).isEmpty();
		then(mockQuery).should().setParameter("timesheetIds", timesheetIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("validateContractorTimeLogsBeforeUpdate returns constructor-mapped rows")
	void testValidateContractorTimeLogsBeforeUpdateWhenRowsReturnedReturnsList() {
		// Given
		List<Integer> timesheetIds = ValidatorRepositoryTestDataFactory.getSampleTimesheetIdsForValidator();
		ContractorTimesheetValidatorQueryResultDto row = ValidatorRepositoryTestDataFactory
			.createContractorValidatorRow();
		TypedQuery<ContractorTimesheetValidatorQueryResultDto> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_VALIDATE_CONTRACTOR_TIME_LOGS_BEFORE_UPDATE,
				ContractorTimesheetValidatorQueryResultDto.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetIds", timesheetIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(row));

		// When
		List<ContractorTimesheetValidatorQueryResultDto> result = this.repository
			.validateContractorTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result).containsExactly(row);
		then(this.entityManager).should()
			.createQuery(JPQL_VALIDATE_CONTRACTOR_TIME_LOGS_BEFORE_UPDATE,
					ContractorTimesheetValidatorQueryResultDto.class);
		then(mockQuery).should().setParameter("timesheetIds", timesheetIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("validateContractorTimeLogsBeforeUpdate returns empty list when no rows match")
	void testValidateContractorTimeLogsBeforeUpdateWhenNoRowsReturnsEmptyList() {
		// Given
		List<Integer> timesheetIds = ValidatorRepositoryTestDataFactory.getSampleTimesheetIdsForValidator();
		TypedQuery<ContractorTimesheetValidatorQueryResultDto> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_VALIDATE_CONTRACTOR_TIME_LOGS_BEFORE_UPDATE,
				ContractorTimesheetValidatorQueryResultDto.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("timesheetIds", timesheetIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		List<ContractorTimesheetValidatorQueryResultDto> result = this.repository
			.validateContractorTimeLogsBeforeUpdate(timesheetIds);

		// Then
		assertThat(result).isEmpty();
		then(mockQuery).should().setParameter("timesheetIds", timesheetIds);
		then(mockQuery).should().getResultList();
	}

}
