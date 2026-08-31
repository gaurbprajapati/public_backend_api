/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.timesheet;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for UserDetailsMaps record class. Tests all accessor methods for 100% line
 * and branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsMapsTests {

	private static final Integer DEFAULT_USER_ID = 1;

	private static final Integer DEFAULT_CONTACT_ID = 2;

	private static final Integer DEFAULT_CONTRACTOR_ID = 3;

	private static Map<Integer, UserDetailsQueryResultDto> createAgencyUsersMap() {
		Map<Integer, UserDetailsQueryResultDto> map = new HashMap<>();
		UserDetailsQueryResultDto userDetails = new UserDetailsQueryResultDto("John Doe", "http://photo.url/john.jpg");
		map.put(DEFAULT_USER_ID, userDetails);
		return map;
	}

	private static Map<Integer, ContactNamePhotoQueryResultDto> createContactUsersMap() {
		Map<Integer, ContactNamePhotoQueryResultDto> map = new HashMap<>();
		ContactNamePhotoQueryResultDto contactDetails = new ContactNamePhotoQueryResultDto("Jane Contact",
				"http://photo.url/jane.jpg", null);
		map.put(DEFAULT_CONTACT_ID, contactDetails);
		return map;
	}

	private static Map<Integer, ContractorNamePhotoQueryResultDto> createContractorUsersMap() {
		Map<Integer, ContractorNamePhotoQueryResultDto> map = new HashMap<>();
		ContractorNamePhotoQueryResultDto contractorDetails = new ContractorNamePhotoQueryResultDto("Bob Contractor",
				"http://photo.url/bob.jpg", "bob-contractor");
		map.put(DEFAULT_CONTRACTOR_ID, contractorDetails);
		return map;
	}

	@Nested
	@DisplayName("Constructor and Accessor Tests")
	class ConstructorAndAccessorTests {

		@Test
		@DisplayName("Should create UserDetailsMaps with all maps")
		void testConstructorValidMapsCreatesUserDetailsMaps() {
			// Given
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = createAgencyUsersMap();
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = createContactUsersMap();
			Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap = createContractorUsersMap();

			// When
			UserDetailsMaps userDetailsMaps = new UserDetailsMaps(agencyUsersMap, contactUsersMap, contractorUsersMap);

			// Then
			assertThat(userDetailsMaps).isNotNull();
			assertThat(userDetailsMaps.agencyUsersMap()).isEqualTo(agencyUsersMap);
			assertThat(userDetailsMaps.contactUsersMap()).isEqualTo(contactUsersMap);
			assertThat(userDetailsMaps.contractorUsersMap()).isEqualTo(contractorUsersMap);
		}

		@Test
		@DisplayName("Should return agencyUsersMap")
		void testAgencyUsersMapReturnsAgencyUsersMap() {
			// Given
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = createAgencyUsersMap();
			UserDetailsMaps userDetailsMaps = new UserDetailsMaps(agencyUsersMap, new HashMap<>(), new HashMap<>());

			// When
			Map<Integer, UserDetailsQueryResultDto> result = userDetailsMaps.agencyUsersMap();

			// Then
			assertThat(result).isEqualTo(agencyUsersMap).containsKey(DEFAULT_USER_ID);
		}

		@Test
		@DisplayName("Should return contactUsersMap")
		void testContactUsersMapReturnsContactUsersMap() {
			// Given
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = createContactUsersMap();
			UserDetailsMaps userDetailsMaps = new UserDetailsMaps(new HashMap<>(), contactUsersMap, new HashMap<>());

			// When
			Map<Integer, ContactNamePhotoQueryResultDto> result = userDetailsMaps.contactUsersMap();

			// Then
			assertThat(result).isEqualTo(contactUsersMap).containsKey(DEFAULT_CONTACT_ID);
		}

		@Test
		@DisplayName("Should return contractorUsersMap")
		void testContractorUsersMapReturnsContractorUsersMap() {
			// Given
			Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap = createContractorUsersMap();
			UserDetailsMaps userDetailsMaps = new UserDetailsMaps(new HashMap<>(), new HashMap<>(), contractorUsersMap);

			// When
			Map<Integer, ContractorNamePhotoQueryResultDto> result = userDetailsMaps.contractorUsersMap();

			// Then
			assertThat(result).isEqualTo(contractorUsersMap).containsKey(DEFAULT_CONTRACTOR_ID);
		}

		@Test
		@DisplayName("Should handle null maps")
		void testConstructorNullMapsHandlesNullMaps() {
			// When
			UserDetailsMaps userDetailsMaps = new UserDetailsMaps(null, null, null);

			// Then
			assertThat(userDetailsMaps.agencyUsersMap()).isNull();
			assertThat(userDetailsMaps.contactUsersMap()).isNull();
			assertThat(userDetailsMaps.contractorUsersMap()).isNull();
		}

		@Test
		@DisplayName("Should handle empty maps")
		void testConstructorEmptyMapsHandlesEmptyMaps() {
			// When
			UserDetailsMaps userDetailsMaps = new UserDetailsMaps(new HashMap<>(), new HashMap<>(), new HashMap<>());

			// Then
			assertThat(userDetailsMaps.agencyUsersMap()).isEmpty();
			assertThat(userDetailsMaps.contactUsersMap()).isEmpty();
			assertThat(userDetailsMaps.contractorUsersMap()).isEmpty();
		}

	}

	@Nested
	@DisplayName("Record Equality Tests")
	class RecordEqualityTests {

		@Test
		@DisplayName("Should be equal when all maps are equal")
		void testEqualsEqualMapsReturnsTrue() {
			// Given
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = createAgencyUsersMap();
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = createContactUsersMap();
			Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap = createContractorUsersMap();

			UserDetailsMaps userDetailsMaps1 = new UserDetailsMaps(agencyUsersMap, contactUsersMap, contractorUsersMap);
			UserDetailsMaps userDetailsMaps2 = new UserDetailsMaps(agencyUsersMap, contactUsersMap, contractorUsersMap);

			// Then
			assertThat(userDetailsMaps1).isEqualTo(userDetailsMaps2);
		}

		@Test
		@DisplayName("Should have same hashCode when equal")
		void testHashCodeEqualObjectsReturnsSameHashCode() {
			// Given
			Map<Integer, UserDetailsQueryResultDto> agencyUsersMap = createAgencyUsersMap();
			Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap = createContactUsersMap();
			Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap = createContractorUsersMap();

			UserDetailsMaps userDetailsMaps1 = new UserDetailsMaps(agencyUsersMap, contactUsersMap, contractorUsersMap);
			UserDetailsMaps userDetailsMaps2 = new UserDetailsMaps(agencyUsersMap, contactUsersMap, contractorUsersMap);

			// Then
			assertThat(userDetailsMaps1).hasSameHashCodeAs(userDetailsMaps2);
		}

		@Test
		@DisplayName("Should have non-null toString")
		void testToStringReturnsNonNullString() {
			// Given
			UserDetailsMaps userDetailsMaps = new UserDetailsMaps(createAgencyUsersMap(), createContactUsersMap(),
					createContractorUsersMap());

			// When
			String result = userDetailsMaps.toString();

			// Then
			assertThat(result).isNotNull().contains("UserDetailsMaps");
		}

	}

}
