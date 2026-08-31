package io.recruitcrm.microservice.timesheet.controllers.portals.client;

import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.portals.client.IInvitableContactsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/portal/client")
public class InvitableContactsController implements IInvitableContactsController {

	private final IInvitableContactsService invitableContactsService;

	private final APIResponder apiResponder;

	public InvitableContactsController(IInvitableContactsService invitableContactsService, APIResponder apiResponder) {
		this.invitableContactsService = invitableContactsService;
		this.apiResponder = apiResponder;
	}

	@Override
	@GetMapping("/invitable-contacts")
	public ResponseEntity<?> getInvitableContacts(@RequestParam @NotNull @Positive Integer companyId,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) @Positive @Max(100) Integer limit) {
		InvitableContactsResponseBodyDto response = this.invitableContactsService.getInvitableContacts(companyId,
				search, limit);
		return this.apiResponder.respond(response, "Invitable contacts fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

}
