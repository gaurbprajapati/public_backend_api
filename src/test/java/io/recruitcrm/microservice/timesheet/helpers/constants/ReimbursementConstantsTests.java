package io.recruitcrm.microservice.timesheet.helpers.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReimbursementConstants Tests")
class ReimbursementConstantsTests {

	@Test
	@DisplayName("Constructor throws UnsupportedOperationException")
	void testConstructorThrowsUnsupportedOperationException() throws NoSuchMethodException {
		Constructor<ReimbursementConstants> constructor = ReimbursementConstants.class.getDeclaredConstructor();
		constructor.setAccessible(true);

		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class)
			.hasRootCauseMessage("This is a utility class and cannot be instantiated");
	}

	@Test
	@DisplayName("STATUS_SUBMITTED constant has correct value")
	void testStatusSubmittedConstant() {
		assertThat(ReimbursementConstants.STATUS_SUBMITTED).isEqualTo(1);
	}

	@Test
	@DisplayName("STATUS_APPROVED constant has correct value")
	void testStatusApprovedConstant() {
		assertThat(ReimbursementConstants.STATUS_APPROVED).isEqualTo(2);
	}

	@Test
	@DisplayName("STATUS_REJECTED constant has correct value")
	void testStatusRejectedConstant() {
		assertThat(ReimbursementConstants.STATUS_REJECTED).isEqualTo(3);
	}

	@Test
	@DisplayName("USER_TYPE_AGENCY constant has correct value")
	void testUserTypeAgencyConstant() {
		assertThat(ReimbursementConstants.USER_TYPE_AGENCY).isEqualTo(2);
	}

	@Test
	@DisplayName("STATUS_SUBMITTED_LABEL constant has correct value")
	void testStatusSubmittedLabelConstant() {
		assertThat(ReimbursementConstants.STATUS_SUBMITTED_LABEL).isEqualTo("Submitted");
	}

	@Test
	@DisplayName("STATUS_APPROVED_LABEL constant has correct value")
	void testStatusApprovedLabelConstant() {
		assertThat(ReimbursementConstants.STATUS_APPROVED_LABEL).isEqualTo("Approved");
	}

	@Test
	@DisplayName("STATUS_REJECTED_LABEL constant has correct value")
	void testStatusRejectedLabelConstant() {
		assertThat(ReimbursementConstants.STATUS_REJECTED_LABEL).isEqualTo("Rejected");
	}

	@Test
	@DisplayName("STATUS_PENDING_LABEL constant has correct value")
	void testStatusPendingLabelConstant() {
		assertThat(ReimbursementConstants.STATUS_PENDING_LABEL).isEqualTo("Pending");
	}

	@Test
	@DisplayName("getStatusLabel returns Submitted for status 1")
	void testGetStatusLabelReturnsSubmittedForStatus1() {
		String result = ReimbursementConstants.getStatusLabel(ReimbursementConstants.STATUS_SUBMITTED);
		assertThat(result).isEqualTo("Submitted");
	}

	@Test
	@DisplayName("getStatusLabel returns Approved for status 2")
	void testGetStatusLabelReturnsApprovedForStatus2() {
		String result = ReimbursementConstants.getStatusLabel(ReimbursementConstants.STATUS_APPROVED);
		assertThat(result).isEqualTo("Approved");
	}

	@Test
	@DisplayName("getStatusLabel returns Rejected for status 3")
	void testGetStatusLabelReturnsRejectedForStatus3() {
		String result = ReimbursementConstants.getStatusLabel(ReimbursementConstants.STATUS_REJECTED);
		assertThat(result).isEqualTo("Rejected");
	}

	@Test
	@DisplayName("getStatusLabel returns Unknown for unknown status")
	void testGetStatusLabelReturnsUnknownForUnknownStatus() {
		String result = ReimbursementConstants.getStatusLabel(999);
		assertThat(result).isEqualTo("Unknown");
	}

	@Test
	@DisplayName("getStatusLabel returns empty string for null status")
	void testGetStatusLabelReturnsEmptyStringForNullStatus() {
		String result = ReimbursementConstants.getStatusLabel(null);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("getStatusLabel returns Unknown for negative status")
	void testGetStatusLabelReturnsUnknownForNegativeStatus() {
		String result = ReimbursementConstants.getStatusLabel(-1);
		assertThat(result).isEqualTo("Unknown");
	}

	@Test
	@DisplayName("getStatusLabel returns Unknown for zero status")
	void testGetStatusLabelReturnsUnknownForZeroStatus() {
		String result = ReimbursementConstants.getStatusLabel(0);
		assertThat(result).isEqualTo("Unknown");
	}

}
