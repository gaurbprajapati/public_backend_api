/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import io.recruitcrm.microservice.timesheet.testdata.UserRepositoryTestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link UserRepository}: JPQL/native queries, parameter binding, and
 * error handling branches.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UserRepositoryTests {

	private static final String JPQL_GET_USER_DETAILS_MAP = "SELECT u.id, new io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto("
			+ "CASE WHEN u.lastname IS NULL OR u.lastname = '' THEN u.firstname ELSE CONCAT(u.firstname, ' ', u.lastname) END, u.photo) "
			+ "FROM User u WHERE u.id IN :ids";

	private static final String JPQL_GET_USER_DETAILS = "SELECT new io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto("
			+ "CASE WHEN u.lastname IS NULL OR u.lastname = '' THEN u.firstname ELSE CONCAT(u.firstname, ' ', u.lastname) END, u.photo) "
			+ "FROM User u WHERE u.id = :id";

	private static final String NATIVE_SQL_GMT = "SELECT tz.timezone FROM tbluserdetails ud "
			+ "LEFT JOIN tbltimezone tz ON tz.id = ud.timezone " + "WHERE ud.userid = :userId";

	private static final String NATIVE_SQL_TIME_FORMAT = "SELECT ud.time_format_type FROM tbluserdetails ud WHERE ud.userid = :userId";

	@Mock
	private EntityManager entityManager;

	private UserRepository repository;

	@BeforeEach
	void setUp() {
		this.repository = new UserRepository(this.entityManager);
	}

	@Test
	@DisplayName("getUserDetailsMap builds map from id and DTO columns")
	void testGetUserDetailsMapWhenRowsReturnedReturnsMap() {
		// Given
		Set<Integer> ids = UserRepositoryTestDataFactory.getSampleUserIds();
		UserDetailsQueryResultDto dto10 = UserRepositoryTestDataFactory.createUserDetailsQueryResultDto();
		UserDetailsQueryResultDto dto20 = new UserDetailsQueryResultDto("Other User", null);
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_GET_USER_DETAILS_MAP, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", ids)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(new Object[] { 10, dto10 }, new Object[] { 20, dto20 }));

		// When
		Map<Integer, UserDetailsQueryResultDto> result = this.repository.getUserDetailsMap(ids);

		// Then
		assertThat(result).hasSize(2).containsEntry(10, dto10).containsEntry(20, dto20);
		then(this.entityManager).should().createQuery(JPQL_GET_USER_DETAILS_MAP, Object[].class);
		then(mockQuery).should().setParameter("ids", ids);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("getUserDetailsMap returns empty map when result list is empty")
	void testGetUserDetailsMapWhenNoRowsReturnsEmptyMap() {
		// Given
		Set<Integer> ids = Set.of(99);
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_GET_USER_DETAILS_MAP, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", ids)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		Map<Integer, UserDetailsQueryResultDto> result = this.repository.getUserDetailsMap(ids);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getUserDetails returns single JPQL projection")
	void testGetUserDetailsWhenFoundReturnsDto() {
		// Given
		Integer id = UserRepositoryTestDataFactory.getDefaultUserId();
		UserDetailsQueryResultDto expected = UserRepositoryTestDataFactory.createUserDetailsQueryResultDto();
		TypedQuery<UserDetailsQueryResultDto> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_GET_USER_DETAILS, UserDetailsQueryResultDto.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("id", id)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(expected);

		// When
		UserDetailsQueryResultDto result = this.repository.getUserDetails(id);

		// Then
		assertThat(result).isSameAs(expected);
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("getUserDetails returns null when JPQL has no result")
	void testGetUserDetailsWhenNoResultReturnsNull() {
		// Given
		Integer id = UserRepositoryTestDataFactory.getDefaultUserId();
		TypedQuery<UserDetailsQueryResultDto> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL_GET_USER_DETAILS, UserDetailsQueryResultDto.class))
			.willReturn(mockQuery);
		given(mockQuery.setParameter("id", id)).willReturn(mockQuery);
		willThrow(new NoResultException()).given(mockQuery).getSingleResult();

		// When
		UserDetailsQueryResultDto result = this.repository.getUserDetails(id);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getGMTDifferenceByUserId converts native scalar to string")
	void testGetGmtDifferenceWhenFoundReturnsString() {
		// Given
		Integer userId = UserRepositoryTestDataFactory.getDefaultUserId();
		Query mockQuery = mock(Query.class);
		given(this.entityManager.createNativeQuery(NATIVE_SQL_GMT)).willReturn(mockQuery);
		given(mockQuery.setParameter("userId", userId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(UserRepositoryTestDataFactory.getSampleTimezoneOffset());

		// When
		String result = this.repository.getGMTDifferenceByUserId(userId);

		// Then
		assertThat(result).isEqualTo(UserRepositoryTestDataFactory.getSampleTimezoneOffset());
		then(mockQuery).should().getSingleResult();
	}

	@Test
	@DisplayName("getGMTDifferenceByUserId returns null when scalar is null")
	void testGetGmtDifferenceWhenScalarNullReturnsNull() {
		// Given
		Integer userId = UserRepositoryTestDataFactory.getDefaultUserId();
		Query mockQuery = mock(Query.class);
		given(this.entityManager.createNativeQuery(NATIVE_SQL_GMT)).willReturn(mockQuery);
		given(mockQuery.setParameter("userId", userId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(null);

		// When
		String result = this.repository.getGMTDifferenceByUserId(userId);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getGMTDifferenceByUserId returns null when native query has no row")
	void testGetGmtDifferenceWhenNoResultReturnsNull() {
		// Given
		Integer userId = UserRepositoryTestDataFactory.getDefaultUserId();
		Query mockQuery = mock(Query.class);
		given(this.entityManager.createNativeQuery(NATIVE_SQL_GMT)).willReturn(mockQuery);
		given(mockQuery.setParameter("userId", userId)).willReturn(mockQuery);
		willThrow(new NoResultException()).given(mockQuery).getSingleResult();

		// When
		String result = this.repository.getGMTDifferenceByUserId(userId);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getTimeFormatTypeByUserId returns null for null user id without querying")
	void testGetTimeFormatTypeWhenUserIdNullReturnsNull() {
		// When
		Integer result = this.repository.getTimeFormatTypeByUserId(null);

		// Then
		assertThat(result).isNull();
		then(this.entityManager).should(never()).createNativeQuery(anyString());
	}

	@Test
	@DisplayName("getTimeFormatTypeByUserId returns int from Number scalar")
	void testGetTimeFormatTypeWhenNumberReturnedUsesIntValue() {
		// Given
		Integer userId = UserRepositoryTestDataFactory.getDefaultUserId();
		Query mockQuery = mock(Query.class);
		given(this.entityManager.createNativeQuery(NATIVE_SQL_TIME_FORMAT)).willReturn(mockQuery);
		given(mockQuery.setParameter("userId", userId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(24L);

		// When
		Integer result = this.repository.getTimeFormatTypeByUserId(userId);

		// Then
		assertThat(result).isEqualTo(24);
	}

	@Test
	@DisplayName("getTimeFormatTypeByUserId parses non-Number scalar via toString")
	void testGetTimeFormatTypeWhenStringReturnedParsesInteger() {
		// Given
		Integer userId = UserRepositoryTestDataFactory.getDefaultUserId();
		Query mockQuery = mock(Query.class);
		given(this.entityManager.createNativeQuery(NATIVE_SQL_TIME_FORMAT)).willReturn(mockQuery);
		given(mockQuery.setParameter("userId", userId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn("1");

		// When
		Integer result = this.repository.getTimeFormatTypeByUserId(userId);

		// Then
		assertThat(result).isEqualTo(1);
	}

	@Test
	@DisplayName("getTimeFormatTypeByUserId returns null when scalar is null")
	void testGetTimeFormatTypeWhenScalarNullReturnsNull() {
		// Given
		Integer userId = UserRepositoryTestDataFactory.getDefaultUserId();
		Query mockQuery = mock(Query.class);
		given(this.entityManager.createNativeQuery(NATIVE_SQL_TIME_FORMAT)).willReturn(mockQuery);
		given(mockQuery.setParameter("userId", userId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn(null);

		// When
		Integer result = this.repository.getTimeFormatTypeByUserId(userId);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getTimeFormatTypeByUserId returns null when scalar is not parseable as integer")
	void testGetTimeFormatTypeWhenUnparseableScalarReturnsNull() {
		// Given
		Integer userId = UserRepositoryTestDataFactory.getDefaultUserId();
		Query mockQuery = mock(Query.class);
		given(this.entityManager.createNativeQuery(NATIVE_SQL_TIME_FORMAT)).willReturn(mockQuery);
		given(mockQuery.setParameter("userId", userId)).willReturn(mockQuery);
		given(mockQuery.getSingleResult()).willReturn("not-a-number");

		// When
		Integer result = this.repository.getTimeFormatTypeByUserId(userId);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getTimeFormatTypeByUserId returns null when native query throws")
	void testGetTimeFormatTypeWhenQueryThrowsReturnsNull() {
		// Given
		Integer userId = UserRepositoryTestDataFactory.getDefaultUserId();
		Query mockQuery = mock(Query.class);
		given(this.entityManager.createNativeQuery(NATIVE_SQL_TIME_FORMAT)).willReturn(mockQuery);
		given(mockQuery.setParameter("userId", userId)).willReturn(mockQuery);
		willThrow(new NoResultException()).given(mockQuery).getSingleResult();

		// When
		Integer result = this.repository.getTimeFormatTypeByUserId(userId);

		// Then
		assertThat(result).isNull();
	}

}
