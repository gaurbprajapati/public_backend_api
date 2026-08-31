package io.recruitcrm.microservice.timesheet.services.contractor;

import java.util.List;

import org.springframework.data.domain.Pageable;

import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorSearchRequestBodyDto;

public interface IContractorService {

	/**
	 * Search contractors based on filter criteria
	 * @param searchRequestBodyDto Search request with filters and sorting
	 * @param pageable Pagination information
	 * @return List of contractors matching the search criteria
	 */
	List<ContractorListResponseBodyDto> searchContractors(ContractorSearchRequestBodyDto searchRequestBodyDto,
			Pageable pageable);

	/**
	 * Get count of contractors matching the search criteria
	 * @param searchRequestBodyDto Search request with filters
	 * @return Count of contractors matching the search criteria
	 */
	Long searchContractorsCount(ContractorSearchRequestBodyDto searchRequestBodyDto);

}
