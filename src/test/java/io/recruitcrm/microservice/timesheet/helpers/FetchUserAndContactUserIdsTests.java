package io.recruitcrm.microservice.timesheet.helpers;

import io.recruitcrm.contract_staffing.entity.model.UserTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for FetchUserAndContactUserIds helper class.
 */
class FetchUserAndContactUserIdsTests {

	private FetchUserAndContactUserIds fetchUserAndContactUserIds;

	private Set<Integer> agencyUserIds;

	private Set<Integer> contactUserIds;

	private Set<Integer> contractorUserIds;

	@BeforeEach
	void setUp() {
		this.fetchUserAndContactUserIds = new FetchUserAndContactUserIds();
		this.agencyUserIds = new HashSet<>();
		this.contactUserIds = new HashSet<>();
		this.contractorUserIds = new HashSet<>();
	}

	@Test
	@DisplayName("Add agency recruiter user - Success")
	void addUserToAppropriateSetAgencyRecruiter() {
		// Arrange
		Integer userTypeId = UserTypeEnum.AGENCY_RECRUITER.getId();
		Integer userId = 123;

		// Act
		this.fetchUserAndContactUserIds.addUserToAppropriateSet(userTypeId, userId, this.agencyUserIds,
				this.contactUserIds, this.contractorUserIds);

		// Assert
		assertThat(this.agencyUserIds).contains(userId);
		assertThat(this.contactUserIds).isEmpty();
		assertThat(this.contractorUserIds).isEmpty();
	}

	@Test
	@DisplayName("Add company contact user - Success")
	void addUserToAppropriateSetCompanyContact() {
		// Arrange
		Integer userTypeId = UserTypeEnum.COMPANY_CONTACT.getId();
		Integer userId = 456;

		// Act
		this.fetchUserAndContactUserIds.addUserToAppropriateSet(userTypeId, userId, this.agencyUserIds,
				this.contactUserIds, this.contractorUserIds);

		// Assert
		assertThat(this.agencyUserIds).isEmpty();
		assertThat(this.contactUserIds).contains(userId);
		assertThat(this.contractorUserIds).isEmpty();
	}

	@Test
	@DisplayName("Add contractor user - Success")
	void addUserToAppropriateSetContractor() {
		// Arrange
		Integer userTypeId = 999; // Any other user type
		Integer userId = 789;

		// Act
		this.fetchUserAndContactUserIds.addUserToAppropriateSet(userTypeId, userId, this.agencyUserIds,
				this.contactUserIds, this.contractorUserIds);

		// Assert
		assertThat(this.agencyUserIds).isEmpty();
		assertThat(this.contactUserIds).isEmpty();
		assertThat(this.contractorUserIds).contains(userId);
	}

	@Test
	@DisplayName("Add multiple users of different types - Success")
	void addUserToAppropriateSetMultipleUsers() {
		// Arrange
		Integer agencyUserId = 111;
		Integer contactUserId = 222;
		Integer contractorUserId = 333;

		// Act
		this.fetchUserAndContactUserIds.addUserToAppropriateSet(UserTypeEnum.AGENCY_RECRUITER.getId(), agencyUserId,
				this.agencyUserIds, this.contactUserIds, this.contractorUserIds);

		this.fetchUserAndContactUserIds.addUserToAppropriateSet(UserTypeEnum.COMPANY_CONTACT.getId(), contactUserId,
				this.agencyUserIds, this.contactUserIds, this.contractorUserIds);

		this.fetchUserAndContactUserIds.addUserToAppropriateSet(999, contractorUserId, // Other
																						// user
																						// type
				this.agencyUserIds, this.contactUserIds, this.contractorUserIds);

		// Assert
		assertThat(this.agencyUserIds).containsExactly(agencyUserId);
		assertThat(this.contactUserIds).containsExactly(contactUserId);
		assertThat(this.contractorUserIds).containsExactly(contractorUserId);
	}

	@Test
	@DisplayName("Add multiple users of same type - Success")
	void addUserToAppropriateSetMultipleUsersSameType() {
		// Arrange
		Integer userId1 = 111;
		Integer userId2 = 222;
		Integer userId3 = 333;

		// Act
		this.fetchUserAndContactUserIds.addUserToAppropriateSet(UserTypeEnum.AGENCY_RECRUITER.getId(), userId1,
				this.agencyUserIds, this.contactUserIds, this.contractorUserIds);

		this.fetchUserAndContactUserIds.addUserToAppropriateSet(UserTypeEnum.AGENCY_RECRUITER.getId(), userId2,
				this.agencyUserIds, this.contactUserIds, this.contractorUserIds);

		this.fetchUserAndContactUserIds.addUserToAppropriateSet(UserTypeEnum.AGENCY_RECRUITER.getId(), userId3,
				this.agencyUserIds, this.contactUserIds, this.contractorUserIds);

		// Assert
		assertThat(this.agencyUserIds).containsExactlyInAnyOrder(userId1, userId2, userId3);
		assertThat(this.contactUserIds).isEmpty();
		assertThat(this.contractorUserIds).isEmpty();
	}

	@Test
	@DisplayName("Add user with null user type ID - Throws NullPointerException")
	void addUserToAppropriateSetNullUserTypeId() {
		// Arrange
		Integer userTypeId = null;
		Integer userId = 123;

		// Act & Assert - Should throw NullPointerException
		assertThatThrownBy(() -> this.fetchUserAndContactUserIds.addUserToAppropriateSet(userTypeId, userId,
				this.agencyUserIds, this.contactUserIds, this.contractorUserIds))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Add user with null user ID - No exception thrown")
	void addUserToAppropriateSetNullUserId() {
		// Arrange
		Integer userTypeId = UserTypeEnum.AGENCY_RECRUITER.getId();
		Integer userId = null;

		// Act & Assert - Should not throw exception
		this.fetchUserAndContactUserIds.addUserToAppropriateSet(userTypeId, userId, this.agencyUserIds,
				this.contactUserIds, this.contractorUserIds);

		assertThat(this.agencyUserIds).contains(userId);
	}

	@Test
	@DisplayName("Add user with zero user type ID - Adds to contractor set")
	void addUserToAppropriateSetZeroUserTypeId() {
		// Arrange
		Integer userTypeId = 0;
		Integer userId = 123;

		// Act
		this.fetchUserAndContactUserIds.addUserToAppropriateSet(userTypeId, userId, this.agencyUserIds,
				this.contactUserIds, this.contractorUserIds);

		// Assert
		assertThat(this.agencyUserIds).isEmpty();
		assertThat(this.contactUserIds).isEmpty();
		assertThat(this.contractorUserIds).contains(userId);
	}

	@Test
	@DisplayName("Add user with negative user type ID - Adds to contractor set")
	void addUserToAppropriateSetNegativeUserTypeId() {
		// Arrange
		Integer userTypeId = -1;
		Integer userId = 123;

		// Act
		this.fetchUserAndContactUserIds.addUserToAppropriateSet(userTypeId, userId, this.agencyUserIds,
				this.contactUserIds, this.contractorUserIds);

		// Assert
		assertThat(this.agencyUserIds).isEmpty();
		assertThat(this.contactUserIds).isEmpty();
		assertThat(this.contractorUserIds).contains(userId);
	}

}