package io.recruitcrm.microservice.timesheet.controllers.contractor;

import io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.contractor.IContractorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/contractors")
public class ContractorSearchController implements IContractorSearchController {

	private final IContractorService contractorService;

	private final APIResponder apiResponder;

	public ContractorSearchController(final IContractorService contractorService, final APIResponder apiResponder) {
		this.contractorService = contractorService;
		this.apiResponder = apiResponder;
	}

	@Override
	@PostMapping("/search/get")
	public ResponseEntity<?> searchContractors(@RequestBody ContractorSearchRequestBodyDto searchRequestBodyDto,
			PaginationRequestBodyDto paginationRequestBodyDto) {
		List<ContractorListResponseBodyDto> contractors = this.contractorService.searchContractors(searchRequestBodyDto,
				paginationRequestBodyDto.toPageable());
		return this.apiResponder.respond(contractors, "Contractors fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PostMapping("/search/count")
	public ResponseEntity<?> searchContractorsCount(@RequestBody ContractorSearchRequestBodyDto searchRequestBodyDto) {
		Long count = this.contractorService.searchContractorsCount(searchRequestBodyDto);
		return this.apiResponder.respond(count, "Contractor count fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

}
