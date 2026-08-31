package io.recruitcrm.microservice.timesheet.repositories.candidate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.microservice.timesheet.dao.candidate.CandidateJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

/**
 * Unit test cases for CandidateRepository with 100% coverage. Tests all repository
 * methods following BDD-style testing patterns and strict checkstyle compliance.
 * <p>
 * Repository Method Coverage: 1. getContractorQueryResultMap(...) - Complex JPQL query
 * with CONCAT and stream transformation 2. getCandidate(...) - Simple JPA delegation to
 * CandidateJpaRepository
 * <p>
 * Each method is tested for: - Success scenarios with valid data - JPQL query
 * construction and parameter binding - Stream transformation and Map creation - Exception
 * handling for database errors - Empty and edge case scenarios - JPA repository
 * delegation
 */
@ExtendWith(MockitoExtension.class)
class CandidateRepositoryTests {

	@InjectMocks
	private CandidateRepository repository;

	@Mock
	private EntityManager entityManager;

	@Mock
	private CandidateJpaRepository candidateJpaRepository;

	@BeforeEach
	void setUp() {
		// Common setup for CandidateRepository tests
	}

	// ===== getContractorQueryResultMap Tests =====

	@Test
	@DisplayName("Get contractor query result map should execute JPQL with CASE and transform to Map successfully")
	void testGetContractorQueryResultMapValidIdsExecutesJpqlWithCaseAndTransformsToMapSuccessfully() {
		// Given
		Set<Integer> candidateIds = new HashSet<>(Arrays.asList(101, 102, 103));
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.profilePic, c.slug) "
				+ "FROM Candidate c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);

		// Create test result data matching the JPQL structure
		ContractorNamePhotoQueryResultDto dto1 = new ContractorNamePhotoQueryResultDto("John Doe", "profile1.jpg",
				"john-doe");
		ContractorNamePhotoQueryResultDto dto2 = new ContractorNamePhotoQueryResultDto("Jane Smith", "profile2.jpg",
				"jane-smith");
		ContractorNamePhotoQueryResultDto dto3 = new ContractorNamePhotoQueryResultDto("Bob Johnson", "profile3.jpg",
				"bob-johnson");

		Object[] result1 = { 101, dto1 };
		Object[] result2 = { 102, dto2 };
		Object[] result3 = { 103, dto3 };
		List<Object[]> queryResults = Arrays.asList(result1, result2, result3);

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", candidateIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(queryResults);

		// When
		Map<Integer, ContractorNamePhotoQueryResultDto> result = this.repository
			.getContractorQueryResultMap(candidateIds);

		// Then
		assertThat(result).hasSize(3).containsEntry(101, dto1).containsEntry(102, dto2).containsEntry(103, dto3);
		assertThat(result.get(101)).extracting("name", "profilePic", "slug")
			.containsExactly("John Doe", "profile1.jpg", "john-doe");
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", candidateIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get contractor query result map should return empty map when no results found")
	void testGetContractorQueryResultMapNoResultsFoundReturnsEmptyMap() {
		// Given
		Set<Integer> candidateIds = new HashSet<>(Arrays.asList(999, 998));
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.profilePic, c.slug) "
				+ "FROM Candidate c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);
		List<Object[]> emptyResults = Collections.emptyList();

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", candidateIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(emptyResults);

		// When
		Map<Integer, ContractorNamePhotoQueryResultDto> result = this.repository
			.getContractorQueryResultMap(candidateIds);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", candidateIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get contractor query result map should handle empty input IDs set")
	void testGetContractorQueryResultMapEmptyInputIdsSetHandlesCorrectly() {
		// Given
		Set<Integer> emptyIds = Collections.emptySet();
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.profilePic, c.slug) "
				+ "FROM Candidate c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);
		List<Object[]> emptyResults = Collections.emptyList();

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", emptyIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(emptyResults);

		// When
		Map<Integer, ContractorNamePhotoQueryResultDto> result = this.repository.getContractorQueryResultMap(emptyIds);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", emptyIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get contractor query result map should handle single ID in set")
	void testGetContractorQueryResultMapSingleIdInSetHandlesCorrectly() {
		// Given
		Set<Integer> singleId = Collections.singleton(101);
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.profilePic, c.slug) "
				+ "FROM Candidate c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);

		ContractorNamePhotoQueryResultDto dto = new ContractorNamePhotoQueryResultDto("John Doe", "profile.jpg",
				"john-doe");
		Object[] result = { 101, dto };
		List<Object[]> queryResults = Collections.singletonList(result);

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", singleId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(queryResults);

		// When
		Map<Integer, ContractorNamePhotoQueryResultDto> resultMap = this.repository
			.getContractorQueryResultMap(singleId);

		// Then
		assertThat(resultMap).hasSize(1).containsEntry(101, dto);
		assertThat(resultMap.get(101)).extracting("name", "profilePic", "slug")
			.containsExactly("John Doe", "profile.jpg", "john-doe");
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", singleId);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get contractor query result map should handle null values in DTO")
	void testGetContractorQueryResultMapNullValuesInDtoHandlesCorrectly() {
		// Given
		Set<Integer> candidateIds = Collections.singleton(101);
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.profilePic, c.slug) "
				+ "FROM Candidate c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);

		// Test with null profile picture
		ContractorNamePhotoQueryResultDto dtoWithNullPic = new ContractorNamePhotoQueryResultDto("John Doe", null,
				null);
		Object[] result = { 101, dtoWithNullPic };
		List<Object[]> queryResults = Collections.singletonList(result);

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", candidateIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(queryResults);

		// When
		Map<Integer, ContractorNamePhotoQueryResultDto> resultMap = this.repository
			.getContractorQueryResultMap(candidateIds);

		// Then
		assertThat(resultMap).hasSize(1).containsEntry(101, dtoWithNullPic);
		assertThat(resultMap.get(101)).extracting("name", "profilePic").containsExactly("John Doe", null);
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", candidateIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get contractor query result map should propagate DataAccessException from query execution")
	void testGetContractorQueryResultMapDataAccessExceptionFromQueryExecutionPropagatesException() {
		// Given
		Set<Integer> candidateIds = new HashSet<>(Arrays.asList(101, 102));
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.profilePic, c.slug) "
				+ "FROM Candidate c WHERE c.id IN :ids";

		willThrow(new DataAccessException("Database connection failed") {
		}).given(this.entityManager).createQuery(expectedJpql, Object[].class);

		// When & Then
		assertThatThrownBy(() -> this.repository.getContractorQueryResultMap(candidateIds))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database connection failed");

		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
	}

	@Test
	@DisplayName("Get contractor query result map should propagate DataAccessException from query result fetching")
	void testGetContractorQueryResultMapDataAccessExceptionFromQueryResultFetchingPropagatesException() {
		// Given
		Set<Integer> candidateIds = new HashSet<>(Arrays.asList(101, 102));
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.profilePic, c.slug) "
				+ "FROM Candidate c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", candidateIds)).willReturn(mockQuery);
		willThrow(new DataAccessException("Query execution timeout") {
		}).given(mockQuery).getResultList();

		// When & Then
		assertThatThrownBy(() -> this.repository.getContractorQueryResultMap(candidateIds))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Query execution timeout");

		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", candidateIds);
		then(mockQuery).should().getResultList();
	}

	// ===== getCandidate Tests =====

	@Test
	@DisplayName("Get candidate should delegate to JPA repository and return candidate")
	void testGetCandidateValidParametersDelegatesToJpaRepositoryAndReturnsCandidate() {
		// Given
		Integer candidateId = 101;
		Integer accountId = 1;
		Candidate expectedCandidate = createTestCandidate();

		given(this.candidateJpaRepository.getByIdAndAccountId(candidateId, accountId)).willReturn(expectedCandidate);

		// When
		Candidate result = this.repository.getCandidate(candidateId, accountId);

		// Then
		assertThat(result).isEqualTo(expectedCandidate);
		assertThat(result.getId()).isEqualTo(expectedCandidate.getId());
		assertThat(result.getFirstName()).isEqualTo(expectedCandidate.getFirstName());
		assertThat(result.getLastName()).isEqualTo(expectedCandidate.getLastName());
		assertThat(result.getAccountId()).isEqualTo(expectedCandidate.getAccountId());
		then(this.candidateJpaRepository).should().getByIdAndAccountId(candidateId, accountId);
	}

	@Test
	@DisplayName("Get candidate should return null when candidate not found")
	void testGetCandidateCandidateNotFoundReturnsNull() {
		// Given
		Integer candidateId = 999;
		Integer accountId = 1;

		given(this.candidateJpaRepository.getByIdAndAccountId(candidateId, accountId)).willReturn(null);

		// When
		Candidate result = this.repository.getCandidate(candidateId, accountId);

		// Then
		assertThat(result).isNull();
		then(this.candidateJpaRepository).should().getByIdAndAccountId(candidateId, accountId);
	}

	@Test
	@DisplayName("Get candidate should return null when account mismatch")
	void testGetCandidateAccountMismatchReturnsNull() {
		// Given
		Integer candidateId = 101;
		Integer wrongAccountId = 999;

		given(this.candidateJpaRepository.getByIdAndAccountId(candidateId, wrongAccountId)).willReturn(null);

		// When
		Candidate result = this.repository.getCandidate(candidateId, wrongAccountId);

		// Then
		assertThat(result).isNull();
		then(this.candidateJpaRepository).should().getByIdAndAccountId(candidateId, wrongAccountId);
	}

	@Test
	@DisplayName("Get candidate should handle null parameters correctly")
	void testGetCandidateNullParametersHandlesCorrectly() {
		// Given
		Integer nullCandidateId = null;
		Integer accountId = 1;

		given(this.candidateJpaRepository.getByIdAndAccountId(nullCandidateId, accountId)).willReturn(null);

		// When
		Candidate result = this.repository.getCandidate(nullCandidateId, accountId);

		// Then
		assertThat(result).isNull();
		then(this.candidateJpaRepository).should().getByIdAndAccountId(nullCandidateId, accountId);
	}

	@Test
	@DisplayName("Get candidate should propagate DataAccessException from JPA repository")
	void testGetCandidateDataAccessExceptionFromJpaRepositoryPropagatesException() {
		// Given
		Integer candidateId = 101;
		Integer accountId = 1;

		willThrow(new DataAccessException("Database access error") {
		}).given(this.candidateJpaRepository).getByIdAndAccountId(candidateId, accountId);

		// When & Then
		assertThatThrownBy(() -> this.repository.getCandidate(candidateId, accountId))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database access error");

		then(this.candidateJpaRepository).should().getByIdAndAccountId(candidateId, accountId);
	}

	// ===== Helper Methods =====

	/**
	 * Creates a test Candidate entity with default values
	 * @return Candidate entity for testing
	 */
	private Candidate createTestCandidate() {
		Candidate candidate = new Candidate();
		candidate.setId(101);
		candidate.setFirstName("John");
		candidate.setLastName("Doe");
		candidate.setEmailId("john.doe@example.com");
		candidate.setProfilePic("profile.jpg");
		candidate.setAccountId(1);
		candidate.setPosition("Software Engineer");
		candidate.setOwnerId(1);
		return candidate;
	}

}
