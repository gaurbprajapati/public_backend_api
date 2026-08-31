package io.recruitcrm.microservice.timesheet.testdata;

import java.util.Arrays;
import java.util.List;

import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorDealQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorJobQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.contractor.ContractorQueryResultDto;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.repositories.contractor.ContractorRepository}
 * tests.
 */
public final class ContractorRepositoryTestDataFactory {

	private static final Integer DEFAULT_ACCOUNT_ID = Integer.valueOf(100);

	private static final Integer DEFAULT_CONTRACTOR_ID = Integer.valueOf(1);

	private ContractorRepositoryTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getDefaultAccountId() {
		return ContractorRepositoryTestDataFactory.DEFAULT_ACCOUNT_ID;
	}

	public static Integer getDefaultContractorId() {
		return ContractorRepositoryTestDataFactory.DEFAULT_CONTRACTOR_ID;
	}

	public static ContractorQueryResultDto createContractorQueryResult() {
		ContractorQueryResultDto dto = new ContractorQueryResultDto();
		dto.setId(ContractorRepositoryTestDataFactory.DEFAULT_CONTRACTOR_ID);
		dto.setName("Test Contractor");
		return dto;
	}

	public static List<ContractorQueryResultDto> createContractorQueryResultList() {
		return Arrays.asList(createContractorQueryResult());
	}

	public static ContractorJobQueryResultDto createContractorJobQueryResult() {
		return new ContractorJobQueryResultDto(ContractorRepositoryTestDataFactory.DEFAULT_CONTRACTOR_ID,
				Integer.valueOf(10), "Contract Job", "job-slug", "Acme Corp", "acme-slug", Integer.valueOf(1704067200),
				Integer.valueOf(1735689600), "Active", Integer.valueOf(1));
	}

	public static List<ContractorJobQueryResultDto> createContractorJobQueryResultList() {
		return Arrays.asList(createContractorJobQueryResult());
	}

	public static ContractorDealQueryResultDto createContractorDealQueryResult() {
		return new ContractorDealQueryResultDto(ContractorRepositoryTestDataFactory.DEFAULT_CONTRACTOR_ID,
				Integer.valueOf(20), "Test Deal", "Deal Owner", Integer.valueOf(1), "deal-slug", "Open");
	}

	public static List<ContractorDealQueryResultDto> createContractorDealQueryResultList() {
		return Arrays.asList(createContractorDealQueryResult());
	}

}
