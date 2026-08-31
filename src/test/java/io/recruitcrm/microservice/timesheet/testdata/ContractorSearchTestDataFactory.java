package io.recruitcrm.microservice.timesheet.testdata;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.recruitcrm.microservice.timesheet.search.dto.FilterSearchListDto;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.services.search.ContractorSearchService}
 * tests.
 */
public final class ContractorSearchTestDataFactory {

	private static final Integer DEFAULT_ACCOUNT_ID = Integer.valueOf(1);

	private static final String DEFAULT_GMT_DIFFERENCE = "+05:30";

	private static final int DEFAULT_PAGE_NUMBER = 0;

	private static final int DEFAULT_PAGE_SIZE = 10;

	private ContractorSearchTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getDefaultAccountId() {
		return ContractorSearchTestDataFactory.DEFAULT_ACCOUNT_ID;
	}

	public static String getDefaultGmtDifference() {
		return ContractorSearchTestDataFactory.DEFAULT_GMT_DIFFERENCE;
	}

	public static Pageable createDefaultPageable() {
		return PageRequest.of(ContractorSearchTestDataFactory.DEFAULT_PAGE_NUMBER,
				ContractorSearchTestDataFactory.DEFAULT_PAGE_SIZE);
	}

	public static FilterSearchListDto createFilterSearchListDto() {
		return ContractorFilterSearchCteProviderTestDataFactory.createFilterSearchListSingleContractorStatus();
	}

}
