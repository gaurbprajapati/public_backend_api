package io.recruitcrm.microservice.timesheet.helpers;

import io.recruitcrm.microservice.timesheet.dto.time_log.TimeDetailSummaryDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.AccountUserEnum;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.BDDMockito.verify;

/**
 * Unit tests for TimesheetUpdateHelper class.
 */
@ExtendWith(MockitoExtension.class)
class TimesheetUpdateHelperTests {

	@Mock
	private TimesheetRepository timesheetRepository;

	private TimesheetUpdateHelper timesheetUpdateHelper;

	@BeforeEach
	void setUp() {
		this.timesheetUpdateHelper = new TimesheetUpdateHelper(this.timesheetRepository);
	}

	@ParameterizedTest
	@CsvSource({ "123, 456, 2", "789, 999, 2", "0, 456, 3" })
	@DisplayName("Update timesheet last modified - Success with different timesheet IDs, user IDs, and user type IDs")
	void testUpdateTimesheetLastModifiedSuccess(Integer timesheetId, Integer userId, Integer userTypeId) {
		// Act
		this.timesheetUpdateHelper.updateTimesheetLastModified(timesheetId, userId, userTypeId);

		// Assert
		verify(this.timesheetRepository).updateTimesheetLastModified(eq(timesheetId), eq(userId), eq(userTypeId),
				anyInt());
	}

	@Test
	@DisplayName("Update timesheet last modified - Null user ID")
	void testUpdateTimesheetLastModifiedNullUserId() {
		// Arrange
		Integer timesheetId = 123;
		Integer userId = null;
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();

		// Act
		this.timesheetUpdateHelper.updateTimesheetLastModified(timesheetId, userId, userTypeId);

		// Assert
		verify(this.timesheetRepository).updateTimesheetLastModified(eq(timesheetId), eq(null), eq(userTypeId),
				anyInt());
	}

	@Test
	@DisplayName("Update timesheet last modified - Uses correct user type ID for USER")
	void testUpdateTimesheetLastModifiedUsesCorrectUserTypeIdForUser() {
		// Arrange
		Integer timesheetId = 123;
		Integer userId = 456;
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();

		// Act
		this.timesheetUpdateHelper.updateTimesheetLastModified(timesheetId, userId, userTypeId);

		// Assert
		verify(this.timesheetRepository).updateTimesheetLastModified(eq(timesheetId), eq(userId), eq(userTypeId),
				anyInt());
	}

	@Test
	@DisplayName("Update timesheet last modified - Uses correct user type ID for CONTRACTOR")
	void testUpdateTimesheetLastModifiedUsesCorrectUserTypeIdForContractor() {
		// Arrange
		Integer timesheetId = 123;
		Integer userId = 456;
		Integer userTypeId = UserTypeEnum.CONTRACTOR.getId();

		// Act
		this.timesheetUpdateHelper.updateTimesheetLastModified(timesheetId, userId, userTypeId);

		// Assert
		verify(this.timesheetRepository).updateTimesheetLastModified(eq(timesheetId), eq(userId), eq(userTypeId),
				anyInt());
	}

	@Test
	@DisplayName("Update timesheet last modified - Uses correct user type ID for CONTACT")
	void testUpdateTimesheetLastModifiedUsesCorrectUserTypeIdForContact() {
		// Arrange
		Integer timesheetId = 123;
		Integer userId = 456;
		Integer userTypeId = UserTypeEnum.COMPANY_CONTACT.getId();

		// Act
		this.timesheetUpdateHelper.updateTimesheetLastModified(timesheetId, userId, userTypeId);

		// Assert
		verify(this.timesheetRepository).updateTimesheetLastModified(eq(timesheetId), eq(userId), eq(userTypeId),
				anyInt());
	}

	@Test
	@DisplayName("Update timesheet last modified - Verifies timestamp is passed")
	void testUpdateTimesheetLastModifiedVerifiesTimestamp() {
		// Arrange
		Integer timesheetId = 123;
		Integer userId = 456;
		Integer userTypeId = AccountUserEnum.USERTYPEID.getId();

		// Act
		this.timesheetUpdateHelper.updateTimesheetLastModified(timesheetId, userId, userTypeId);

		// Assert - Verify that a timestamp greater than 0 is passed
		verify(this.timesheetRepository).updateTimesheetLastModified(eq(timesheetId), eq(userId), eq(userTypeId),
				intThat((timestamp) -> timestamp > 0));
	}

	@Test
	@DisplayName("Update timesheet time details - delegates to repository with correct parameters")
	void testUpdateTimesheetTimeDetailsDelegatesToRepositoryWithCorrectParameters() {
		// Arrange
		Integer timesheetId = 13419;
		Integer totalTime = 172800;
		Integer totalWorkTime = 172800;

		// Act
		this.timesheetUpdateHelper.updateTimesheetTimeDetails(timesheetId, totalTime, totalWorkTime);

		// Assert
		verify(this.timesheetRepository).updateTimesheetTimeDetails(timesheetId, totalTime, totalWorkTime);
	}

	@ParameterizedTest
	@CsvSource({ "1, 3600, 3600, 0", "100, 86400, 72000, 14400", "999, 0, 0, 0" })
	@DisplayName("Update timesheet time details - Success with different time values")
	void testUpdateTimesheetTimeDetailsSuccessWithDifferentTimeValues(Integer timesheetId, Integer totalTime,
			Integer totalWorkTime, Integer totalOvertime) {
		// Act
		this.timesheetUpdateHelper.updateTimesheetTimeDetails(timesheetId, totalTime, totalWorkTime);

		// Assert
		verify(this.timesheetRepository).updateTimesheetTimeDetails(timesheetId, totalTime, totalWorkTime);
	}

	@Test
	@DisplayName("Batch update last modified with time details - delegates to repository")
	void testBatchUpdateTimesheetLastModifiedWithTimeDetailsDelegatesToRepository() {
		// Arrange
		List<Integer> timesheetIds = Arrays.asList(1, 2);
		Integer userId = 123;
		Integer userTypeId = 1;
		TimeDetailSummaryDto dto1 = new TimeDetailSummaryDto(1, 3600, 600, 4200);
		List<TimeDetailSummaryDto> timeDetails = List.of(dto1);

		// Act
		this.timesheetUpdateHelper.batchUpdateTimesheetLastModifiedWithTimeDetails(timesheetIds, userId, userTypeId,
				timeDetails);

		// Assert
		verify(this.timesheetRepository).batchUpdateTimesheetLastModifiedWithTimeDetails(eq(timesheetIds), eq(userId),
				eq(userTypeId), intThat((timestamp) -> timestamp > 0), eq(timeDetails));
	}

}