package io.recruitcrm.microservice.timesheet.controllers.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.portals.client.IInvitableContactsService;
import io.recruitcrm.microservice.timesheet.testdata.InvitableContactsTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Unit tests for InvitableContactsController. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class InvitableContactsControllerTests {

	@Mock
	private IInvitableContactsService invitableContactsService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private InvitableContactsController invitableContactsController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Get invitable contacts returns contacts response successfully")
	void testGetInvitableContactsValidCompanyIdReturnsContactsResponse() {
		// Given
		Integer companyId = InvitableContactsTestDataFactory.getDefaultCompanyId();
		InvitableContactsResponseBodyDto expectedResponse = InvitableContactsTestDataFactory
			.createInvitableContactsResponse();
		ResponseEntity<APINormalResponse<InvitableContactsResponseBodyDto>> expectedResponseEntity = InvitableContactsTestDataFactory
			.createInvitableContactsSuccessResponse(expectedResponse);

		given(this.invitableContactsService.getInvitableContacts(companyId, null, null)).willReturn(expectedResponse);
		given(this.apiResponder.respond(expectedResponse,
				InvitableContactsTestDataFactory.Messages.INVITABLE_CONTACTS_FETCHED_SUCCESSFULLY,
				APIResponseType.SUCCESS, HttpStatus.OK))
			.willReturn(expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.invitableContactsController.getInvitableContacts(companyId, null, null);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.invitableContactsService).should().getInvitableContacts(companyId, null, null);
		then(this.apiResponder).should()
			.respond(expectedResponse,
					InvitableContactsTestDataFactory.Messages.INVITABLE_CONTACTS_FETCHED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

}
