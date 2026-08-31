package io.recruitcrm.microservice.timesheet.repositories.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
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
 * Unit test cases for ContactRepository with 100% coverage. Tests the repository's JPQL
 * query method following BDD-style testing patterns and strict checkstyle compliance.
 *
 * Repository Method Coverage: 1. getContactNamePhotoMap(...) - Complex JPQL query with
 * CASE WHEN and CONCAT and stream transformation to Map
 *
 * The method is tested for: - Success scenarios with valid data - JPQL query construction
 * and parameter binding - Stream transformation and Map creation - Exception handling for
 * database errors - Empty and edge case scenarios - Complex JPQL with CASE WHEN and
 * CONCAT function
 */
@ExtendWith(MockitoExtension.class)
class ContactRepositoryTests {

	@InjectMocks
	private ContactRepository repository;

	@Mock
	private EntityManager entityManager;

	@BeforeEach
	void setUp() {
		// Common setup for ContactRepository tests
	}

	// ===== getContactNamePhotoMap Tests =====

	@Test
	@DisplayName("Get contact name photo map should execute JPQL with CASE WHEN and transform to Map successfully")
	void testGetContactNamePhotoMapValidIdsExecutesJpqlWithCaseWhenAndTransformsToMapSuccessfully() {
		// Given
		Set<Integer> contactIds = new HashSet<>(Arrays.asList(201, 202, 203));
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.photo, c.email) "
				+ "FROM Contact c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);

		// Create test result data matching the JPQL structure
		ContactNamePhotoQueryResultDto dto1 = new ContactNamePhotoQueryResultDto("Alice Johnson", "contact1.jpg", null);
		ContactNamePhotoQueryResultDto dto2 = new ContactNamePhotoQueryResultDto("Bob Wilson", "contact2.jpg", null);
		ContactNamePhotoQueryResultDto dto3 = new ContactNamePhotoQueryResultDto("Carol Davis", "contact3.jpg", null);

		Object[] result1 = { 201, dto1 };
		Object[] result2 = { 202, dto2 };
		Object[] result3 = { 203, dto3 };
		List<Object[]> queryResults = Arrays.asList(result1, result2, result3);

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", contactIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(queryResults);

		// When
		Map<Integer, ContactNamePhotoQueryResultDto> result = this.repository.getContactNamePhotoMap(contactIds);

		// Then
		assertThat(result).hasSize(3).containsEntry(201, dto1).containsEntry(202, dto2).containsEntry(203, dto3);
		assertThat(result.get(201)).extracting("name", "profilePic").containsExactly("Alice Johnson", "contact1.jpg");
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", contactIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get contact name photo map should return empty map when no results found")
	void testGetContactNamePhotoMapNoResultsFoundReturnsEmptyMap() {
		// Given
		Set<Integer> contactIds = new HashSet<>(Arrays.asList(999, 998));
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.photo, c.email) "
				+ "FROM Contact c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);
		List<Object[]> emptyResults = Collections.emptyList();

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", contactIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(emptyResults);

		// When
		Map<Integer, ContactNamePhotoQueryResultDto> result = this.repository.getContactNamePhotoMap(contactIds);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", contactIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get contact name photo map should handle empty input IDs set")
	void testGetContactNamePhotoMapEmptyInputIdsSetHandlesCorrectly() {
		// Given
		Set<Integer> emptyIds = Collections.emptySet();
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.photo, c.email) "
				+ "FROM Contact c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);
		List<Object[]> emptyResults = Collections.emptyList();

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", emptyIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(emptyResults);

		// When
		Map<Integer, ContactNamePhotoQueryResultDto> result = this.repository.getContactNamePhotoMap(emptyIds);

		// Then
		assertThat(result).isEmpty();
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", emptyIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get contact name photo map should handle single ID in set")
	void testGetContactNamePhotoMapSingleIdInSetHandlesCorrectly() {
		// Given
		Set<Integer> singleId = Collections.singleton(201);
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.photo, c.email) "
				+ "FROM Contact c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);

		ContactNamePhotoQueryResultDto dto = new ContactNamePhotoQueryResultDto("Alice Johnson", "contact.jpg", null);
		Object[] result = { 201, dto };
		List<Object[]> queryResults = Collections.singletonList(result);

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", singleId)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(queryResults);

		// When
		Map<Integer, ContactNamePhotoQueryResultDto> resultMap = this.repository.getContactNamePhotoMap(singleId);

		// Then
		assertThat(resultMap).hasSize(1).containsEntry(201, dto);
		assertThat(resultMap.get(201)).extracting("name", "profilePic").containsExactly("Alice Johnson", "contact.jpg");
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", singleId);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get contact name photo map should handle null values in DTO")
	void testGetContactNamePhotoMapNullValuesInDtoHandlesCorrectly() {
		// Given
		Set<Integer> contactIds = Collections.singleton(201);
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.photo, c.email) "
				+ "FROM Contact c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);

		// Test with null profile picture
		ContactNamePhotoQueryResultDto dtoWithNullPhoto = new ContactNamePhotoQueryResultDto("Alice Johnson", null,
				null);
		Object[] result = { 201, dtoWithNullPhoto };
		List<Object[]> queryResults = Collections.singletonList(result);

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", contactIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(queryResults);

		// When
		Map<Integer, ContactNamePhotoQueryResultDto> resultMap = this.repository.getContactNamePhotoMap(contactIds);

		// Then
		assertThat(resultMap).hasSize(1).containsEntry(201, dtoWithNullPhoto);
		assertThat(resultMap.get(201)).extracting("name", "profilePic").containsExactly("Alice Johnson", null);
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", contactIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get contact name photo map should handle empty string values in DTO")
	void testGetContactNamePhotoMapEmptyStringValuesInDtoHandlesCorrectly() {
		// Given
		Set<Integer> contactIds = Collections.singleton(201);
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.photo, c.email) "
				+ "FROM Contact c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);

		// Test with empty string profile picture
		ContactNamePhotoQueryResultDto dtoWithEmptyPhoto = new ContactNamePhotoQueryResultDto("Alice Johnson", "",
				null);
		Object[] result = { 201, dtoWithEmptyPhoto };
		List<Object[]> queryResults = Collections.singletonList(result);

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", contactIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(queryResults);

		// When
		Map<Integer, ContactNamePhotoQueryResultDto> resultMap = this.repository.getContactNamePhotoMap(contactIds);

		// Then
		assertThat(resultMap).hasSize(1).containsEntry(201, dtoWithEmptyPhoto);
		assertThat(resultMap.get(201)).extracting("name", "profilePic").containsExactly("Alice Johnson", "");
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", contactIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get contact name photo map should handle multiple contacts with same data correctly")
	void testGetContactNamePhotoMapMultipleContactsWithSameDataCorrectly() {
		// Given
		Set<Integer> contactIds = new HashSet<>(Arrays.asList(201, 202));
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.photo, c.email) "
				+ "FROM Contact c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);

		// Test with same name but different IDs
		ContactNamePhotoQueryResultDto dto1 = new ContactNamePhotoQueryResultDto("John Smith", "photo1.jpg", null);
		ContactNamePhotoQueryResultDto dto2 = new ContactNamePhotoQueryResultDto("John Smith", "photo2.jpg", null);

		Object[] result1 = { 201, dto1 };
		Object[] result2 = { 202, dto2 };
		List<Object[]> queryResults = Arrays.asList(result1, result2);

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", contactIds)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(queryResults);

		// When
		Map<Integer, ContactNamePhotoQueryResultDto> result = this.repository.getContactNamePhotoMap(contactIds);

		// Then
		assertThat(result).hasSize(2).containsEntry(201, dto1).containsEntry(202, dto2);
		assertThat(result.get(201)).extracting("name", "profilePic").containsExactly("John Smith", "photo1.jpg");
		assertThat(result.get(202)).extracting("name", "profilePic").containsExactly("John Smith", "photo2.jpg");
		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", contactIds);
		then(mockQuery).should().getResultList();
	}

	@Test
	@DisplayName("Get contact name photo map should propagate DataAccessException from query creation")
	void testGetContactNamePhotoMapDataAccessExceptionFromQueryCreationPropagatesException() {
		// Given
		Set<Integer> contactIds = new HashSet<>(Arrays.asList(201, 202));
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.photo, c.email) "
				+ "FROM Contact c WHERE c.id IN :ids";

		willThrow(new DataAccessException("Database connection failed") {
		}).given(this.entityManager).createQuery(expectedJpql, Object[].class);

		// When & Then
		assertThatThrownBy(() -> this.repository.getContactNamePhotoMap(contactIds))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Database connection failed");

		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
	}

	@Test
	@DisplayName("Get contact name photo map should propagate DataAccessException from parameter setting")
	void testGetContactNamePhotoMapDataAccessExceptionFromParameterSettingPropagatesException() {
		// Given
		Set<Integer> contactIds = new HashSet<>(Arrays.asList(201, 202));
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.photo, c.email) "
				+ "FROM Contact c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		willThrow(new DataAccessException("Parameter binding failed") {
		}).given(mockQuery).setParameter("ids", contactIds);

		// When & Then
		assertThatThrownBy(() -> this.repository.getContactNamePhotoMap(contactIds))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Parameter binding failed");

		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", contactIds);
	}

	@Test
	@DisplayName("Get contact name photo map should propagate DataAccessException from query result fetching")
	void testGetContactNamePhotoMapDataAccessExceptionFromQueryResultFetchingPropagatesException() {
		// Given
		Set<Integer> contactIds = new HashSet<>(Arrays.asList(201, 202));
		String expectedJpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.photo, c.email) "
				+ "FROM Contact c WHERE c.id IN :ids";
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);

		given(this.entityManager.createQuery(expectedJpql, Object[].class)).willReturn(mockQuery);
		given(mockQuery.setParameter("ids", contactIds)).willReturn(mockQuery);
		willThrow(new DataAccessException("Query execution timeout") {
		}).given(mockQuery).getResultList();

		// When & Then
		assertThatThrownBy(() -> this.repository.getContactNamePhotoMap(contactIds))
			.isInstanceOf(DataAccessException.class)
			.hasMessageContaining("Query execution timeout");

		then(this.entityManager).should().createQuery(expectedJpql, Object[].class);
		then(mockQuery).should().setParameter("ids", contactIds);
		then(mockQuery).should().getResultList();
	}

}
