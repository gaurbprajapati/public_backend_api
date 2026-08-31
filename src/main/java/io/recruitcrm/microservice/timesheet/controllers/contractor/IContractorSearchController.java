package io.recruitcrm.microservice.timesheet.controllers.contractor;

import io.recruitcrm.microservice.timesheet.dto.PaginationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto;
import org.springframework.http.ResponseEntity;

public interface IContractorSearchController {

	/**
	 * Search contractors based on filter criteria
	 * @param searchRequestBodyDto Search request with filters and sorting
	 * @param paginationRequestBodyDto Pagination information
	 * @return List of contractors matching the search criteria
	 */
	ResponseEntity<?> searchContractors(ContractorSearchRequestBodyDto searchRequestBodyDto,
			PaginationRequestBodyDto paginationRequestBodyDto);

	/**
	 * Get count of contractors matching the search criteria
	 * @param searchRequestBodyDto Search request with filters
	 * @return Count of contractors matching the search criteria
	 */
	ResponseEntity<?> searchContractorsCount(ContractorSearchRequestBodyDto searchRequestBodyDto);

}
