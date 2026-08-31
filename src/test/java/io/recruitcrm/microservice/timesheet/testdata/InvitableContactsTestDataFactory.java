package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Test data factory for InvitableContacts-related test objects. Provides factory methods
 * to create consistent test data across all invitable-contacts tests.
 */
public final class InvitableContactsTestDataFactory {

	private InvitableContactsTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getDefaultCompanyId() {
		return 78;
	}

	public static Integer getDefaultAccountId() {
		return 1;
	}

	public static InvitableContactQueryResultDto createContactQueryResultWithStatus(Integer portalStatusId) {
		return new InvitableContactQueryResultDto(1234, "Jane", "Doe", "jane@example.com", portalStatusId, "jane.jpg",
				1, "Acme Corp", null);
	}

	public static InvitableContactQueryResultDto createContactQueryResultNullEmail() {
		return new InvitableContactQueryResultDto(1235, "Bob", "Lee", null, 0, null, 2, "Acme Corp", null);
	}

	public static List<InvitableContactQueryResultDto> createContactQueryResultList() {
		return List.of(createContactQueryResultWithStatus(0));
	}

	public static List<InvitableContactQueryResultDto> createAllActiveQueryResultList() {
		return List.of(createContactQueryResultWithStatus(2));
	}

	public static List<InvitableContactQueryResultDto> createEmptyQueryResultList() {
		return List.of();
	}

	public static InvitableContactResponseBodyDto createContactResponseWithStatus(Integer portalStatusId) {
		return new InvitableContactResponseBodyDto(1234, "Jane", "Doe", "jane@example.com", portalStatusId, "jane.jpg",
				1, "Acme Corp", null, null, null);
	}

	public static InvitableContactsResponseBodyDto createInvitableContactsResponse() {
		return new InvitableContactsResponseBodyDto(getDefaultCompanyId(), false,
				List.of(createContactResponseWithStatus(0)));
	}

	public static InvitableContactsResponseBodyDto createAllActiveInvitableContactsResponse() {
		return new InvitableContactsResponseBodyDto(getDefaultCompanyId(), true, List.of());
	}

	public static InvitableContactsResponseBodyDto createEmptyInvitableContactsResponse() {
		return new InvitableContactsResponseBodyDto(getDefaultCompanyId(), false, List.of());
	}

	public static ResponseEntity<APINormalResponse<InvitableContactsResponseBodyDto>> createInvitableContactsSuccessResponse(
			InvitableContactsResponseBodyDto data) {
		APINormalResponse<InvitableContactsResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Message constants for test assertions. Note: Inner types must be placed at the end
	 * to comply with InnerTypeLast checkstyle rule.
	 */
	public static final class Messages {

		public static final String INVITABLE_CONTACTS_FETCHED_SUCCESSFULLY = "Invitable contacts fetched successfully";

		private Messages() {
		}

	}

}
